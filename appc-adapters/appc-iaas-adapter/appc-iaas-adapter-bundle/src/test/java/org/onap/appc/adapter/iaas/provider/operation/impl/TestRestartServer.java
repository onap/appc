/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
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
package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.mockito.Mockito.inOrder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.UnknownProviderException;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestRestartServer {

    @Test
    public void restartServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        RestartServer rbs = new RestartServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RestartServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).resume();
    }

    @Test
    public void restartServerRunning() throws ZoneException, UnknownProviderException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        RestartServer rbs = new RestartServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).start();
    }
    
    @Test
    public void pauseServerRunning() throws ZoneException, UnknownProviderException {
        MockGenerator mg = new MockGenerator(Status.READY);
        Server server = mg.getServer();
        RestartServer rbs = new RestartServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).start();
    }
    
    @Test
    public void pauseServerError() throws ZoneException, UnknownProviderException {
        MockGenerator mg = new MockGenerator(Status.ERROR);
        Server server = mg.getServer();
        RestartServer rbs = new RestartServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
            
    }
}