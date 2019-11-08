/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.listener.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.onap.appc.adapter.factory.MessageService;
import org.onap.appc.adapter.message.Consumer;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.adapter.messaging.dmaap.http.HttpDmaapConsumerImpl;
import org.onap.appc.adapter.messaging.dmaap.http.HttpDmaapProducerImpl;
import org.onap.appc.listener.EventHandler;
import org.onap.appc.listener.ListenerProperties;
import org.onap.appc.listener.util.Mapper;
import org.onap.appc.logging.LoggingConstants;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class is a wrapper for the DMaaP client provided in appc-dmaap-adapter. Its aim is to ensure
 * that only well formed messages are sent and received on DMaaP.
 */
public class EventHandlerImpl implements EventHandler {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(EventHandlerImpl.class);

    /*
     * The amount of time in seconds to keep a connection to a topic open while waiting for data
     */
    private int READ_TIMEOUT = 60;

    /*
     * The pool of hosts to query against
     */
    private Collection<String> pool;

    /*
     * The topic to read messages from
     */
    private String readTopic;

    /*
     * The topic to write messages to
     */
    private String writeTopic;

    /*
     * The client (group) name to use for reading messages
     */
    private String clientName;

    /*
     * The id of the client (group) that is reading messages
     */
    private String clientId;

    /*
     * The api public key to use for authentication
     */
    private String apiKey;

    /*
     * The api secret key to use for authentication
     */
    private String apiSecret;

    /*
     * A json object containing filter arguments.
     */
    private String filter_json;


    /*
     * Blacklist time for a server with response problem in seconds
     */
    private String responseProblemBlacklistTime;

    /*
     *  Blacklist time for a server with server problem in seconds
     */
    private String serverProblemBlacklistTime;

    /*
     * Blacklist time for a server with DNS problem in seconds
     */
    private String dnsIssueBlacklistTime;

    /*
     * Blacklist time for a server with IO Exception problem in seconds
     */
    private String ioExceptionBlacklistTime;

    private MessageService messageService;

    private Consumer reader = null;
    private Producer producer = null;

    public EventHandlerImpl(ListenerProperties props) {
        pool = new HashSet<>();

        if (props != null) {
            readTopic = props.getProperty(ListenerProperties.KEYS.TOPIC_READ);
            clientName = props.getProperty(ListenerProperties.KEYS.CLIENT_NAME, "APP-C");
            clientId = props.getProperty(ListenerProperties.KEYS.CLIENT_ID, "0");
            apiKey = props.getProperty(ListenerProperties.KEYS.AUTH_USER_KEY);
            apiSecret = props.getProperty(ListenerProperties.KEYS.AUTH_SECRET_KEY);
            responseProblemBlacklistTime = props.getProperty(ListenerProperties.KEYS.PROBLEM_WITH_RESPONSE_BLACKLIST_TIME);
            serverProblemBlacklistTime = props.getProperty(ListenerProperties.KEYS.PROBLEM_SERVERSIDE_ERROR_BLACKLIST_TIME);
            dnsIssueBlacklistTime = props.getProperty(ListenerProperties.KEYS.PROBLEM_DNS_BLACKLIST_TIME);
            ioExceptionBlacklistTime = props.getProperty(ListenerProperties.KEYS.PROBLEM_IO_EXCEPTION_BLACKLIST_TIME);

            filter_json = props.getProperty(ListenerProperties.KEYS.TOPIC_READ_FILTER);

            READ_TIMEOUT = Integer
                    .valueOf(props.getProperty(ListenerProperties.KEYS.TOPIC_READ_TIMEOUT, String.valueOf(READ_TIMEOUT)));

            String hostnames = props.getProperty(ListenerProperties.KEYS.HOSTS);
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }

            String writeTopicStr = props.getProperty(ListenerProperties.KEYS.TOPIC_WRITE);
            if (writeTopicStr != null) {
                for (String topic : writeTopicStr.split(",")) {
                    writeTopic = topic;
                }
            }

            messageService = MessageService.parse(props.getProperty(ListenerProperties.KEYS.MESSAGE_SERVICE));

            LOG.info(String.format(
                    "Configured to use %s client on host pool [%s]. Reading from [%s] filtered by %s. Writing to [%s]. Authenticated using %s",
                    messageService, hostnames, readTopic, filter_json, writeTopic, apiKey));
        }
    }

    @Override
    public List<String> getIncomingEvents() {
        return getIncomingEvents(1000);
    }

    @Override
    public List<String> getIncomingEvents(int limit) {
        List<String> out = new ArrayList<>();
        LOG.info(String.format("Getting up to %d incoming events", limit));
        // reuse the consumer object instead of creating a new one every time
        if (reader == null) {
            LOG.info("Getting Consumer...");
            reader = getConsumer();
        }
        if (reader != null) {
            List<String> items = reader.fetch(READ_TIMEOUT * 1000, limit);
            for (String item : items) {
                out.add(item);
            }
        }
        LOG.info(String.format("Read %d messages from %s as %s/%s.", out.size(), readTopic, clientName, clientId));
        return out;
    }

    @Override
    public <T> List<T> getIncomingEvents(Class<T> cls) {
        return getIncomingEvents(cls, 1000);
    }

    @Override
    public <T> List<T> getIncomingEvents(Class<T> cls, int limit) {
        List<String> incomingStrings = getIncomingEvents(limit);
        return Mapper.mapList(incomingStrings, cls);
    }

    @Override
    public void postStatus(String event) {
        postStatus(null, event);
    }

    @Override
    public void postStatus(String partition, String event) {
        LOG.debug(String.format("Posting Message [%s]", event));
        if (producer == null) {
            LOG.info("Getting Producer...");
            producer = getProducer();
        }
        producer.post(partition, event);
    }

    /**
     * Returns a consumer object for direct access to our Cambria consumer interface
     *
     * @return An instance of the consumer interface
     */
    protected Consumer getConsumer() {
        LOG.debug(String.format("Getting Consumer: %s  %s/%s/%s", pool, readTopic, clientName, clientId));
        if (filter_json == null && writeTopic.equals(readTopic)) {
            LOG.error(
                    "*****We will be writing and reading to the same topic without a filter. This will cause an infinite loop.*****");
        }

        Consumer out = null;
        out = new HttpDmaapConsumerImpl(pool, readTopic, clientName, clientId, filter_json, apiKey, apiSecret);

                    if (out != null && responseProblemBlacklistTime != null && responseProblemBlacklistTime.length() > 0)
                    {
                        out.setResponseProblemBlacklistTime(responseProblemBlacklistTime);
                    }

                    if (out != null && serverProblemBlacklistTime != null && serverProblemBlacklistTime.length() > 0)
                    {
                        out.setServerProblemBlacklistTime(serverProblemBlacklistTime);
                    }

                    if (out != null && dnsIssueBlacklistTime != null && dnsIssueBlacklistTime.length() > 0)
                    {
                        out.setDnsIssueBlacklistTime(dnsIssueBlacklistTime);
                    }

                    if (out != null && ioExceptionBlacklistTime != null && ioExceptionBlacklistTime.length() > 0)
                    {
                        out.setIOExceptionBlacklistTime(ioExceptionBlacklistTime);
                    }
                if (out != null) {
                    for (String url : pool) {
                        if (url.contains("3905") || url.contains("https")) {
                            out.useHttps(true);
                            break;
                        }
                    }
                }
        return out;
    }

    /**
     * Returns a consumer object for direct access to our Cambria producer interface
     *
     * @return An instance of the producer interface
     */
    protected Producer getProducer() {
        LOG.debug(String.format("Getting Producer: %s  %s", pool, readTopic));

        Producer out = null;
        out = new HttpDmaapProducerImpl(pool, writeTopic, apiKey, apiSecret);
                if (out != null && responseProblemBlacklistTime != null && responseProblemBlacklistTime.length() > 0)
                {
                    out.setResponseProblemBlacklistTime(responseProblemBlacklistTime);
                }

                if (out != null && serverProblemBlacklistTime != null && serverProblemBlacklistTime.length() > 0)
                {
                    out.setServerProblemBlacklistTime(serverProblemBlacklistTime);
                }

                if (out != null && dnsIssueBlacklistTime != null && dnsIssueBlacklistTime.length() > 0)
                {
                    out.setDnsIssueBlacklistTime(dnsIssueBlacklistTime);
                }

                if (out != null && ioExceptionBlacklistTime != null && ioExceptionBlacklistTime.length() > 0)
                {
                    out.setIOExceptionBlacklistTime(ioExceptionBlacklistTime);
                }
                if (out != null) {
                    for (String url : pool) {
                        if (url.contains("3905") || url.contains("https")) {
                            out.useHttps(true);
                            break;
                        }
                    }
                }
        return out;
    }

    @Override
    public void closeClients() {
        LOG.debug("Closing Consumer and Producer DMaaP clients");
        if (reader != null) {
            reader.close();
        }
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void setClientName(String clientName) {
        this.clientName = clientName;
        MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, clientName);
    }

    @Override
    public void addToPool(String hostname) {
        pool.add(hostname);
    }

    @Override
    public Collection<String> getPool() {
        return pool;
    }

    @Override
    public void removeFromPool(String hostname) {
        pool.remove(hostname);
    }

    @Override
    public String getReadTopic() {
        return readTopic;
    }

    @Override
    public void setReadTopic(String readTopic) {
        this.readTopic = readTopic;
    }

    @Override
    public String getWriteTopic() {
        return writeTopic;
    }

    @Override
    public void setWriteTopic(String writeTopic) {
        this.writeTopic = writeTopic;
    }

    @Override
    public void setResponseProblemBlacklistTime(String duration){
        this.responseProblemBlacklistTime = duration;
    }

    @Override
    public void setServerProblemBlacklistTime(String duration){
        this.serverProblemBlacklistTime = duration;
    }

    @Override
    public void setDnsIssueBlacklistTime(String duration){
        this.dnsIssueBlacklistTime = duration;
    }

    @Override
    public void setIOExceptionBlacklistTime(String duration){
        this.ioExceptionBlacklistTime = duration;
    }

    @Override
    public void clearCredentials() {
        apiKey = null;
        apiSecret = null;
    }

    @Override
    public void setCredentials(String key, String secret) {
        apiKey = key;
        apiSecret = secret;
    }
}
