/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
* ================================================================================
* Modifications Copyright (c) 2019 IBM
* ================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.adapter.openstack.heat.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestVolume {
    private Volume volume;
    private Properties properties;

    @Before
    public void setUp() {
        volume = new Volume();
        properties = new Properties();
    }

    @Test
    public void testGetType() {
        volume.setType("A");
        assertEquals("A",volume.getType());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals("",volume.toString());
        assertNotEquals(null,volume.toString());
    }

    @Test
    public void testGetProperties() {
        properties.setSize(2);
        volume.setProperties(properties);
        assertSame(2, volume.getProperties().getSize());
        assertSame(properties, volume.getProperties());
    }
}
