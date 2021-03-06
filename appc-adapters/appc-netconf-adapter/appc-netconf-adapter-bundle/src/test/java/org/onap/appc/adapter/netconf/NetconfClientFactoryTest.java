/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.netconf;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.appc.adapter.netconf.jsch.NetconfClientJsch;
import org.onap.appc.adapter.netconf.odlconnector.NetconfClientRestconfImpl;

public class NetconfClientFactoryTest {

    @Test
    public void getNetconfClient_shouldCreateRestClient_forRestClientType() {
        NetconfClient netconfClient = new NetconfClientFactory().getNetconfClient(NetconfClientType.RESTCONF);
        assertTrue(netconfClient instanceof NetconfClientRestconfImpl);
    }

    @Test
    public void getNetconfClient_shouldCreateJschClient_forSshClientType() {
        NetconfClient netconfClient = new NetconfClientFactory().getNetconfClient(NetconfClientType.SSH);
        assertTrue(netconfClient instanceof NetconfClientJsch);
    }

    @Test
    public void getNetconfClient_shouldReturnNullForInvalidClientType() {
        NetconfClient netconfClient = new NetconfClientFactory().getNetconfClient(null);
        assertNull(netconfClient);
    }
}