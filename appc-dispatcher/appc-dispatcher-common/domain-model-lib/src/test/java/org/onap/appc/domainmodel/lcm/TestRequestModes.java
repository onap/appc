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
package org.onap.appc.domainmodel.lcm;

import org.junit.Assert;
import org.junit.Test;

public class TestRequestModes {
    private RequestModes requestModes = RequestModes.EXCLUSIVE;

    @Test
    public void testName() {
        Assert.assertEquals("EXCLUSIVE", requestModes.name());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(requestModes.equals(RequestModes.EXCLUSIVE));
        Assert.assertFalse(requestModes.equals(null));
    }

    @Test
    public void testGetMode() {
        Assert.assertEquals("Exclusive", requestModes.getMode());
    }

}
