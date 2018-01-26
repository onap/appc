/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.executorImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.*;


public class RestExecutor implements FlowExecutorInterface {

    private static final int HTTP_RESPONSE_OK = 200;

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RestExecutor.class);

    @Override
    public Map<String, String> execute(Transaction transaction, SvcLogicContext ctx) throws Exception{
        log.info("Configuring Rest Operation....." + transaction.toString());
        Map<String, String> outputMessage = new HashMap<>();
        Client client = null;

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[] {new SecureRestClientTrustManager()}, null);
            DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
            defaultClientConfig.getProperties().put(
                    HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new HTTPSProperties(getHostnameVerifier(), sslContext));
            client = Client.create(defaultClientConfig);
            client.addFilter(new HTTPBasicAuthFilter(transaction.getuId(), transaction.getPswd()));
            WebResource webResource = client.resource(new URI(transaction.getExecutionEndPoint()));
            webResource.setProperty("Content-Type", "application/json;charset=UTF-8");

            ClientResponse clientResponse = getClientResponse(transaction, webResource)
                    .orElseThrow(() -> new Exception(
                    "Cannot determine the state of : "
                    + transaction.getActionLevel()
                    + " HTTP response is null"));

            processClientResponse(clientResponse, transaction, outputMessage);

            log.info("Completed Rest Operation.....");

        } catch (Exception e) {
            log.debug("failed in RESTCONT Action ("
                    + transaction.getExecutionRPC()
                    + ") for the resource "
                    + transaction.getExecutionEndPoint()
                    + ", fault message :"
                    + e.getMessage());
            throw new Exception("Error While Sending Rest Request", e);

        } finally {
            if(client != null){
                client.destroy();
            }
        }
        return outputMessage;
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }

    private Optional<ClientResponse> getClientResponse(Transaction transaction, WebResource webResource) {
        String responseDataType=MediaType.APPLICATION_JSON;
        String requestDataType=MediaType.APPLICATION_JSON;
        ClientResponse clientResponse = null;

        log.info("Starting Rest Operation.....");
        if(HttpMethod.GET.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.accept(responseDataType).get(ClientResponse.class);
        }else if(HttpMethod.POST.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.type(requestDataType).post(ClientResponse.class, transaction.getPayload());
        }else if(HttpMethod.PUT.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.type(requestDataType).put(ClientResponse.class,transaction.getPayload());
        }else if(HttpMethod.DELETE.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.delete(ClientResponse.class);
        }
        return Optional.ofNullable(clientResponse);
    }

    private void processClientResponse (ClientResponse clientResponse,
                                        Transaction transaction,
                                        Map<String, String> outputMessage ) throws Exception {

        if(clientResponse.getStatus() == HTTP_RESPONSE_OK) {
            Response response = new Response();
            response.setResponseCode(String.valueOf(HTTP_RESPONSE_OK));
            transaction.setResponses(Collections.singletonList(response));
            outputMessage.put("restResponse", clientResponse.getEntity(String.class));
        } else {
            String errorMsg = clientResponse.getEntity(String.class);
            if (StringUtils.isNotBlank(errorMsg)) {
                log.debug("Error Message from Client Response" + errorMsg);
            }
            throw new Exception("Cannot determine the state of : "
                    + transaction.getActionLevel()
                    + " HTTP error code : "
                    + clientResponse.getStatus());
        }
    }
}
