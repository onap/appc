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
package org.onap.appc.flow.executor.node;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.flow.controller.node.FlowControlNode;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFlowExecutorNode {

    private final Logger logger = LoggerFactory.getLogger(TestFlowExecutorNode.class);


    //@Before
            public void setUp() {
                Properties props = new Properties();
                InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");
                if (propStr == null) {
                }
                try {
                    props.load(propStr);
                    propStr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Add properties to global properties

                Enumeration propNames = props.keys();

                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    System.setProperty(propName, props.getProperty(propName));
                }

	}

    @Ignore("Test is taking 60 seconds")
	@Test(expected=Exception.class)
	public void testFlowExecutorNode() throws Exception {
		
		SvcLogicContext ctx = new SvcLogicContext();
		ctx.setAttribute(FlowControllerConstants.VNF_TYPE, "vUSP - vDBE-IPX HUB");
		ctx.setAttribute(FlowControllerConstants.REQUEST_ACTION, "Configure");
		ctx.setAttribute(FlowControllerConstants.VNFC_TYPE, "");;
		ctx.setAttribute(FlowControllerConstants.REQUEST_ID,"TESTCOMMONFRMWK");
		HashMap inParams = new HashMap();
		FlowControlNode fen = new FlowControlNode();
		fen.processFlow(inParams, ctx);
	}
}
