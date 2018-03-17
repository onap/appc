/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ResponseStatusTest {

    private ResponseStatus responseStatus;

    @Before
    public void setup() {
        responseStatus = new ResponseStatus();
    }

    @Test
    public void should_set_properties() {

        responseStatus.setCode(200);
        responseStatus.setValue("OK");

        assertEquals(Integer.valueOf(200), responseStatus.getCode());
        assertEquals("OK", responseStatus.getValue());
    }

    @Test
    public void should_initialize_parameters_from_constructor() {
        responseStatus = new ResponseStatus(200, "OK");

        assertEquals(Integer.valueOf(200), responseStatus.getCode());
        assertEquals("OK", responseStatus.getValue());
    }
}
