/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
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
package org.onap.appc.lifecyclemanager.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLCMResponse {
    private LCMResponse lcmresponse=LCMResponse.INVALID_INPUT_PARAMETERS;

    @Test
    public void testName() {
        assertEquals("INVALID_INPUT_PARAMETERS",lcmresponse.name());
    }

    @Test
    public void testEquals()  {
        assertTrue(lcmresponse.equals(LCMResponse.INVALID_INPUT_PARAMETERS));
        assertFalse(lcmresponse.equals(null));
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(lcmresponse.toString(), "");
        assertNotEquals(lcmresponse.toString(), null);
    }

    @Test
    public void testTostring() {
        assertTrue(lcmresponse.toString().contains(lcmresponse.name()));
    }

}
