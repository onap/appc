/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modification Copyright (C) 2018 IBM
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

package org.onap.appc.interfaceService.serviceExecutor;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.appc.interfaces.service.executorImpl.ServiceExecutorImpl;
import org.onap.appc.interfaces.service.data.ScopeOverlap;
import org.onap.appc.interfaces.service.executor.ServiceExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.powermock.reflect.Whitebox;

public class TestServiceExecutor {

    @Test
    public void serviceExecutorTest() throws Exception {
        ServiceExecutorImpl sei = new ServiceExecutorImpl();
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        sei.isRequestOverLap(requestData);
        assertNotNull(sei);
    }

    @Test
    public void isVserverOrVnfcIdOverLapTest() throws Exception{
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"vnf-id\" : \"vnf-id\",\"vf-module-id\" : \"vf-module-1234\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"vnf-id\" : \"vnf-id1\",\"vf-module-id\":\"vf-module-1234\"}}]}";
        ServiceExecutorImpl sei = new ServiceExecutorImpl();
        ScopeOverlap scopeOverlap = new ScopeOverlap();
        ObjectMapper mapper = new ObjectMapper();
        scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        boolean result = Whitebox.invokeMethod(sei, "isVserverOrVnfcIdOverLap",scopeOverlap);
        assertEquals(true, result);
    }

    @Test
    public void serviceExecutor() throws Exception {
        ServiceExecutor sei = new ServiceExecutor();
        String action ="getdatabymodel";
        String requestDataType = "";
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
       String result = sei.execute(action, requestData, requestDataType);
       assertEquals(null, result);
    }

    @Test(expected = Exception.class)
    public void serviceExecutorException() throws Exception {
        ServiceExecutor sei = new ServiceExecutor();
        String action = "";
        String requestDataType = "";
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        sei.execute(action, requestData, requestDataType);
    }

    @Test
    public void serviceExecutorRequest() throws Exception {
        ServiceExecutor sei = new ServiceExecutor();
        String action = "isScopeOverlap";
        String requestDataType = "";
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        String actual ="\"requestOverlap\"  : true";
        String result = sei.execute(action, requestData, requestDataType);
        assertEquals(actual, result);
    }

    @Test
    public void serviceExecutorRqsFal() throws Exception {
        ServiceExecutor sei = new ServiceExecutor();
        String action = "isScopeOverlap";
        String requestDataType = "";
        String actual ="\"requestOverlap\"  : false";
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}}}";        
        String result = sei.execute(action, requestData, requestDataType);
        assertEquals(actual, result);
    }

    @Test(expected = Exception.class)
    public void serviceExecutorRqstEx() throws Exception {
        ServiceExecutor sei = new ServiceExecutor();
        String action = "isScopeOverlap";
        String requestData = "";
        String requestDataType = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"},\"target-id\":\"ibcx0001v\"}]}";        
        sei.execute(action, requestData, requestDataType);
    }

    @Test
    public void isVserverOrVnfcIdOverlap() throws Exception{
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        ServiceExecutorImpl sei = new ServiceExecutorImpl();
        ScopeOverlap scopeOverlap = new ScopeOverlap();
        ObjectMapper mapper = new ObjectMapper();
        scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        boolean result = Whitebox.invokeMethod(sei, "isVserverOrVnfcIdOverLap", scopeOverlap);
        assertEquals(true, result);
    }

    @Test
    public void isVserverOrVnfcIdO() throws Exception{
        String requestData = "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vserver-id\": \"vserver-id1\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        ServiceExecutorImpl sei = new ServiceExecutorImpl();
        ScopeOverlap scopeOverlap = new ScopeOverlap();
        ObjectMapper mapper = new ObjectMapper();
        scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        boolean result = Whitebox.invokeMethod(sei, "isVserverOrVnfcIdOverLap", scopeOverlap);
        assertEquals(true, result);
    }

    @Test(expected = Exception.class)
    public void isVserverOrVnfcId() throws Exception{
        String requestData =  "{\"vnf-id\":\"ibcx8888v\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnfc-name\" : \"vnfc-name2\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnfc-name\" : \"vnfc-name2\"}}]}";
        ServiceExecutorImpl sei = new ServiceExecutorImpl();
        ScopeOverlap scopeOverlap = new ScopeOverlap();
        ObjectMapper mapper = new ObjectMapper();
        scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        Whitebox.invokeMethod(sei, "isVserverOrVnfcIdOverLap", scopeOverlap);
    }
}
