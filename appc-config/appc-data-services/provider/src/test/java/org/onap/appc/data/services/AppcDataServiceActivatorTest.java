/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2019 IBM
 *=================================================================================
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

package org.onap.appc.data.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.data.services.node.ConfigResourceNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppcDataServiceActivatorTest {

    AppcDataServiceActivator appcDataServiceActivator;
    Field registrationsField;
    @Mock
    BundleContext ctx;
    @Mock
    ServiceRegistration serviceRegistration;

    @Before
    public void setUp() throws Exception {
        this.appcDataServiceActivator = new AppcDataServiceActivator();
        this.registrationsField = AppcDataServiceActivator.class.getDeclaredField("registrations");
        registrationsField.setAccessible(true);
        when(ctx.registerService(anyString(), any(ConfigResourceNode.class), any(Dictionary.class)))
                .thenReturn(serviceRegistration);
    }

    @Test
    public void start() throws Exception {
        appcDataServiceActivator.start(ctx);
        List<ServiceRegistration> registrations = (List<ServiceRegistration>) registrationsField.get(appcDataServiceActivator);
        assertEquals(1, registrations.size());
        assertThat(registrations.get(0), is(serviceRegistration));
    }

    @Test
    public void stop() throws Exception {
        appcDataServiceActivator.start(ctx);
        appcDataServiceActivator.stop(ctx);
        verify(serviceRegistration).unregister();
    }
}
