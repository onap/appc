/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg;


import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.dg.mock.instance.MockConfigureNodeExecutor;
import org.onap.appc.dg.mock.instance.MockSvcLogicJavaPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* need move to open source
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.onap.sdnc.dgtestlibrary.AbstractDGTestCase;
import org.onap.sdnc.dgtestlibrary.DGTestCase;
import org.onap.sdnc.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.provider.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicAdaptorFactory;


public class TestGetVfModuleInfo /* extends AbstractDGTestCase */ {

    //private final static Logger logger = LoggerFactory.getLogger(TestGetParams.class);

/*
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestGetVfModuleInfo.class);
    public static String GetVfModuleInfoXML = "src/main/resources/xml/APPC_GetVfModuleInfo.xml";



    @Test
    public void testGetVfModuleInfoSuccess() {
        try {

            String propertyfileName = "APPC/GetVfModuleInfo/GetVfModuleInfo_Success.properties";
            Map<String, Object> serviceReferences = new HashMap<String, Object>();

            // Register Call graphs
            String injectGraphXmls[] = new String[] { GetVfModuleInfoXML };


            GraphKey  graphKey = new GraphKey("APPC", null, "GetVfModuleInfo", null);
            DGTestCase tc = new DGTestCase(graphKey);
            tc.setInjectGraphXmls(injectGraphXmls);
            tc.setServiceReferences(serviceReferences);
            tc.setPropertyfileName(propertyfileName);

            SvcLogicContext ctx = new SvcLogicContext();
            processTestCase(tc, ctx);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    @Test
    public void testGetVfModuleInfoFailure() {
        try {

            String propertyfileName = "APPC/GetVfModuleInfo/GetVfModuleInfo_Failure.properties";
            Map<String, Object> serviceReferences = new HashMap<String, Object>();

            // Register Call graphs
            String injectGraphXmls[] = new String[] { GetVfModuleInfoXML };


            GraphKey  graphKey = new GraphKey("APPC", null, "GetVfModuleInfo", null);
            DGTestCase tc = new DGTestCase(graphKey);
            tc.setInjectGraphXmls(injectGraphXmls);
            tc.setServiceReferences(serviceReferences);
            tc.setPropertyfileName(propertyfileName);

            SvcLogicContext ctx = new SvcLogicContext();
            processTestCase(tc, ctx);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


*/

}
