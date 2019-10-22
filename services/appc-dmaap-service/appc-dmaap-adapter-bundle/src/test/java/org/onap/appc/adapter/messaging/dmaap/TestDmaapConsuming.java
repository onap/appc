/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.adapter.messaging.dmaap;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.appc.adapter.message.Consumer;
import org.onap.appc.adapter.messaging.dmaap.http.HttpDmaapConsumerImpl;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

/**
 * Must have a DMaaP cluster or simulator up and running
 * Update the hostname, topic, client properties in
 * resources/org/onap/appc/default.properties
 *
 */
public class TestDmaapConsuming {

    private static Consumer httpConsumer;

    @BeforeClass
    public static void setUp() {

        Configuration configuration = ConfigurationFactory.getConfiguration();

        List<String> hosts = Arrays.asList(configuration.getProperty("poolMembers").split(","));
        String topic = configuration.getProperty("topic.read");
        String consumerName = configuration.getProperty("client.name");
        String consumerId = configuration.getProperty("client.name.id");
        String msgFilter = configuration.getProperty("message.filter");
        String user = configuration.getProperty("dmaap.appc.username");
        String password = configuration.getProperty("dmaap.appc.password");

        httpConsumer = new HttpDmaapConsumerImpl(hosts, topic, consumerName, consumerId, msgFilter);
    }

    @Test
    @Ignore
    public void testHttpFetchMessages() {
        testFetchMessages(httpConsumer);
    }

     @Test
     @Ignore
    public void testFetchMessages() {
        testFetchMessages(httpConsumer);
    }

    private void testFetchMessages(Consumer consumer) {
        List<String> messages = consumer.fetch(1000, 100);
        Assert.assertNotNull(messages);
        Assert.assertFalse(messages.isEmpty());
    }

}
