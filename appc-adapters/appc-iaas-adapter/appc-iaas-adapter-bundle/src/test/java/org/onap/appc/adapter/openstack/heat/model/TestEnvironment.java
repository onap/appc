/*
 * ============LICENSE_START==========================================
 * org.onap.music
 * ===================================================================
 *  Copyright (c) 2019 IBM.
 * ===================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * ============LICENSE_END=============================================
 * ====================================================================
 */
package org.onap.appc.adapter.openstack.heat.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class TestEnvironment {

    private Environment environment;
    private Parameters parameters;
    private ResourceRegistry resourceRegistry;

    @Before
    public void setUp() {
        environment = new Environment();
        parameters = new Parameters();
        resourceRegistry = new ResourceRegistry();
    }

    @Test
    public void testGetParameters() {
        environment.setParameters(parameters);
        assertNotNull(environment.getParameters());
        assertSame(parameters, environment.getParameters());
    }

    @Test
    public void testGetResourceRegistry() {
        environment.setResourceRegistry(resourceRegistry);
        assertNotNull(environment.getResourceRegistry());
        assertSame(resourceRegistry, environment.getResourceRegistry());
    }

    @Test
    public void testToString() {
        environment.setResourceRegistry(resourceRegistry);
        assertNotNull(environment.toString());
    }
}
