/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.junit.Assert.assertTrue;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestLookupServer {

    @Test
    public void lookupServer() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        LookupServer rbs = new LookupServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during LookupServer.executeProviderOperation");
        }
        boolean correctServerReturned = false;
        try {
            Server returnedServer = (Server) mo;
            correctServerReturned = returnedServer == server;
        } catch (Exception e) {
            Assert.fail();
        }
        assertTrue(correctServerReturned);
    }
}