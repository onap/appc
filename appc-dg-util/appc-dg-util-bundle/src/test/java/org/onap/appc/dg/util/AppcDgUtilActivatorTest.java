/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AppcDgUtilActivatorTest {

    @Mock
    private BundleContext bundleContext;

    private AppcDgUtilActivator appcDgUtilActivator;

    @Before
    public void setUp() throws Exception {
        appcDgUtilActivator = new AppcDgUtilActivator();
    }

    @Test
    public void start() {
        appcDgUtilActivator.start(bundleContext);
        PowerMockito.verifyStatic();
    }

    @Test
    public void stop() {
        appcDgUtilActivator.stop(bundleContext);
        PowerMockito.verifyStatic();
    }
}