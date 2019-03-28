/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.aai.client.aai;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestVServerInfo {

    VServerInfo vServerInfo;

    @Before
    public void setUp() throws MissingParameterException {
        Map<String, String> params = new HashMap<>();
        params.put("vserverId", "testVserverId");
        params.put("tenantId", "testTenantId");
        params.put("cloudOwner", "testCloudOwner");
        params.put("cloudRegionId", "testCloudRegionId");
        vServerInfo = new VServerInfo(params);
    }

    @Test
    public void testVserverId() {
        assertEquals("testVserverId", vServerInfo.getVserverId());
    }

    @Test
    public void testTenantId() {
        assertEquals("testTenantId", vServerInfo.getTenantId());
    }

    @Test
    public void testCloudOwner() {
        assertEquals("testCloudOwner", vServerInfo.getCloudOwner());
    }

    @Test
    public void testCloudRegionId() {
        assertEquals("testCloudRegionId", vServerInfo.getCloudRegionId());
    }
    
    @Test(expected = MissingParameterException.class)
    public void testConstructorWithEmptyVserverId() throws MissingParameterException {
        Map<String, String> params = new HashMap<>();
        params.put("vserverId", "");
        vServerInfo = new VServerInfo(params);
    }
    
    @Test(expected = MissingParameterException.class)
    public void testConstructorWithEmptyTenantId() throws MissingParameterException {
        Map<String, String> params = new HashMap<>();
        params.put("vserverId", "testVserverId");
        params.put("tenantId", "");
        vServerInfo = new VServerInfo(params);
    }
    
    @Test(expected = MissingParameterException.class)
    public void testConstructorWithEmptyCloudOwner() throws MissingParameterException {
        Map<String, String> params = new HashMap<>();
        params.put("vserverId", "testVserverId");
        params.put("tenantId", "testTenantId");
        params.put("cloudOwner", "");
        vServerInfo = new VServerInfo(params);
    }
    
    @Test(expected = MissingParameterException.class)
    public void testConstructorWithEmptyCloudRegionId() throws MissingParameterException {
        Map<String, String> params = new HashMap<>();
        params.put("vserverId", "testVserverId");
        params.put("tenantId", "testTenantId");
        params.put("cloudOwner", "testCloudOwner");
        params.put("cloudRegionId", "");
        vServerInfo = new VServerInfo(params);
    }

}
