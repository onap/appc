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
package org.onap.sdnc.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.sdnc.config.params.ParamsHandlerActivator;
import org.onap.sdnc.config.params.parser.PropertyDefinitionNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@RunWith(MockitoJUnitRunner.class)
public class TestParamsHandlerActivator {

    ParamsHandlerActivator paramsHandlerActivator;
    Field registrationsField;

    @Mock
    BundleContext ctx;

    @Mock
    ServiceRegistration serviceRegistration;

    @Before
    public void setUp() throws Exception {
        this.paramsHandlerActivator = new ParamsHandlerActivator();
        this.registrationsField = ParamsHandlerActivator.class.getDeclaredField("registrations");
        registrationsField.setAccessible(true);
        MockitoAnnotations.initMocks(this);
        Mockito.when(ctx.registerService(Mockito.anyString(), Mockito.any(PropertyDefinitionNode.class),
                Mockito.any(Dictionary.class))).thenReturn(serviceRegistration);
    }

    @Test
    public void start() throws Exception {
        paramsHandlerActivator.start(ctx);
        List<ServiceRegistration> registrations = (List<ServiceRegistration>) registrationsField
                .get(paramsHandlerActivator);
        assertEquals(1, registrations.size());
        assertThat(registrations.get(0), is(serviceRegistration));
    }

    @Test
    public void stop() throws Exception {
        paramsHandlerActivator.start(ctx);
        paramsHandlerActivator.stop(ctx);
        Mockito.verify(serviceRegistration).unregister();
    }

    @After
    public void stopActivator() throws Exception {
        paramsHandlerActivator.stop(ctx);
    }
}
