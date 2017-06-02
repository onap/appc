/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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



package org.openecomp.appc.adapter.iaas.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.appc.adapter.iaas.impl.VMURL;
import org.openecomp.appc.configuration.ConfigurationFactory;

public class TestVMURL {

    private static String IP;
    private static String PORT;
    private static String TENANTID;
    private static String VMID;
    private static String URL;

    @BeforeClass
    public static void before() {
        IP = "192.168.1.2";
        PORT = "5000";
        TENANTID = "abcde12345fghijk6789lmnopq123rst";
        VMID = "abc12345-1234-5678-890a-abcdefg12345";
        URL = String.format("http://%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID);
    }

    /**
     * Test that we can parse and interpret valid URLs
     */
    @Test
    public void testValidURLs() {
        VMURL url = VMURL.parseURL(URL);

        assertEquals("http", url.getScheme());
        assertEquals(IP, url.getHost());
        assertEquals(PORT, url.getPort());
        assertEquals(TENANTID, url.getTenantId());
        assertEquals(VMID, url.getServerId());

        url = VMURL.parseURL(String.format("http://%s/v2/%s/servers/%s", IP, TENANTID, VMID));
        assertEquals("http", url.getScheme());
        assertEquals(IP, url.getHost());
        assertNull(url.getPort());
        assertEquals(TENANTID, url.getTenantId());
        assertEquals(VMID, url.getServerId());
    }

    /**
     * Test that we ignore and return null for invalid URLs
     */
    @Test
    public void testInvalidURLs() {
        VMURL url = VMURL.parseURL(null);
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http:/%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http:///%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http://v2/%s/servers/%s", TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/servers/%s", IP, PORT, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/servers", IP, PORT, TENANTID));
        assertNull(url);
    }
}
