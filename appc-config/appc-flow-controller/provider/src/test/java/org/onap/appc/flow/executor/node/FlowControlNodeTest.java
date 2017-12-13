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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.node.FlowControlNode;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

public class FlowControlNodeTest {
	@Mock
	FlowControlDBService dbservice = FlowControlDBService.initialise();
	@Mock
	FlowControlNode f = new FlowControlNode();
	
	Properties props = new Properties();
	private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
	@Before
	public void setUp() throws Exception 

	{
		FlowControlDBService dbservice = FlowControlDBService.initialise();
	}
	@Ignore("Test is taking 60 seconds")
	@Test(expected=Exception.class)
	public final void testProcessFlow() throws Exception {
		SvcLogicContext  ctx = new SvcLogicContext();
		
		ctx.setAttribute("request-id","test");
		ctx.setAttribute("vnf-type","test");
		ctx.setAttribute("action-level","HealthCheck");
				ctx.setAttribute("request-action","HealthCheck");
				ctx.setAttribute("response-prefix","response-prefix");
		
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put("responsePrefix", "responsePrefix");
		

		Whitebox.invokeMethod(f, "processFlow",inParams, ctx);
		/*Properties props = new Properties();
		PowerMockito.spy(FlowControlNode.class);
	      Transactions trans =null;
	      HashMap<Integer, Transaction> transactionMap = null;
	    	 String  artifact_content="{‘PlaybookName’:’service_start’,‘EnvParameters’:{‘vnf_instance’:’$vnf_instance’},’Timeout’:600}";
			String capabilitiesData = "SUCCESS";
	      System.out.println("End Test when");*/
	      
	      
	}
	@Test 
	public void testgetInventoryInfo() throws Exception 
	{
		SvcLogicContext  ctx = new SvcLogicContext();
		 String  vnfid = "test";
	      ctx.setAttribute( " tmp.vnfInfo.vnf.vnf-name","test");
	      ctx.setAttribute("tmp.vnfInfo.vm-count", "0");
	      ctx.setAttribute( " tmp.vnfInfo.vnf.vnf-type","test");
	      ctx.setAttribute( "tmp.vnfInfo.vm[0 ].vserverId","test" );
	      ctx.setAttribute( "tmp.vnfInfo.vm[0 ].vnfc-name","test" );
	      ctx.setAttribute( "tmp.vnfInfo.vm[0].vnfc-type","test" );
	      ctx.setAttribute( " tmp.vnfInfo.vm[0].vnfc-count","1");
	     
		Whitebox.invokeMethod(f, "getInventoryInfo", ctx, vnfid);
		
	}
	@Ignore("Test is taking 60 seconds")
	@Test(expected=Exception.class)
	public void testprocessFlowSequence() throws Exception 
	{
		Map<String, String> inparams = new HashMap<String,String>();
		SvcLogicContext  ctx = new SvcLogicContext();
	      ctx.setAttribute( " SEQUENCE-TYPE","test");
	      ctx.setAttribute("flow-sequence", "1");
	      ctx.setAttribute( "DesignTime","test");
	      ctx.setAttribute( "vnf-type","test" );
	     
		Whitebox.invokeMethod(f, "processFlowSequence",inparams, ctx, ctx);
		
	}
	@Test
	public void testexeuteAllTransaction() throws Exception 
	{
		Map<Integer, Transaction> transactionMap = new HashMap<Integer,Transaction>();
		SvcLogicContext  ctx = new SvcLogicContext();
		Whitebox.invokeMethod(f, "exeuteAllTransaction",transactionMap, ctx);
		
	}
	@Test
	public void testexeutepreProcessor() throws Exception 
	{
	Map<Integer, Transaction> transactionMap = new HashMap<Integer,Transaction>();
	Transaction transaction = new Transaction();
	Whitebox.invokeMethod(f, "preProcessor",transactionMap, transaction);
	
	}
		@Test(expected=Exception.class)
	public void testcollectInputParams() throws Exception 
	{
	SvcLogicContext  ctx = new SvcLogicContext();

	Transaction transaction = new Transaction();
	Whitebox.invokeMethod(f, "collectInputParams",ctx, transaction);
	
	}
	@Ignore("Test is taking 60 seconds")
	@Test(expected=Exception.class)
	public void testgetDependencyInfo() throws Exception
	{
		SvcLogicContext  ctx = new SvcLogicContext();
		Whitebox.invokeMethod(f, "getDependencyInfo",ctx);
		
	}
	public void testgetCapabilitesDatass() throws Exception
	{
		SvcLogicContext  ctx = new SvcLogicContext();
		Whitebox.invokeMethod(f, "getDependencyInfo",ctx);
		
	}
	
	
}
