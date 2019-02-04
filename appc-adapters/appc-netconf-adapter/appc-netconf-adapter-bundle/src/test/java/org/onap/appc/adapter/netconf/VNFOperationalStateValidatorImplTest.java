/*
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

package org.onap.appc.adapter.netconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class VNFOperationalStateValidatorImplTest {
    
    private VNFOperationalStateValidatorImpl vnfOperationalStateValidatorImpl;
    
    @Before
    public void setUp()
    {
        vnfOperationalStateValidatorImpl= new VNFOperationalStateValidatorImpl();
    }
    
    @Test
    public void testGetVnfType()
    {
        VnfType vnfType= VnfType.VNF;
        assertEquals(vnfType, vnfOperationalStateValidatorImpl.getVnfType());
    }
    
    @Test
    public void testgetConfigurationFileName()
    {
        String resultStr= "VnfGetOperationalStates";
        assertEquals(resultStr, vnfOperationalStateValidatorImpl.getConfigurationFileName());
    }

}
