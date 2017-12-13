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

package org.onap.appc.instar.interfaceImpl;

import java.util.HashMap;
import java.util.List;

import org.onap.appc.instar.interfaces.ResponseHandlerInterface;
import org.onap.appc.instar.interfaces.RestClientInterface;
import org.onap.appc.instar.interfaces.RuleHandlerInterface;
import org.onap.appc.instar.node.InstarClientNode;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.openecomp.sdnc.config.params.data.Parameter;
import org.openecomp.sdnc.config.params.data.ResponseKey;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class InterfaceIpAddressImpl implements RuleHandlerInterface {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(InterfaceIpAddressImpl.class);
	private  Parameter parameters;
	private SvcLogicContext context; 

	public InterfaceIpAddressImpl(Parameter params, SvcLogicContext ctx) {			
		this.parameters = params;
		this.context = ctx;
	}

	@Override
	public void processRule() throws Exception {

		String fn = "InterfaceIpAddressHandler.processRule";
		log.info(fn + "Processing rule :" + parameters.getRuleType());
		String operationName ;
		
		RestClientInterface restClient = null;
		ResponseHandlerInterface responseHandler = null;

		List<ResponseKey> responseKeyList = parameters.getResponseKeys();
		if(responseKeyList != null && responseKeyList.size() > 0){
			for(ResponseKey filterKeys : responseKeyList){			
				//response.setUniqueKeyValue(response.getUniqueKeyValue()+ context.getAttribute(InstarClientConstant.VNF_NAME));
				switch(parameters.getSource()){
				case InstarClientConstant.SOURCE_SYSTEM_INSTAR:						
					restClient = new InstarRestClientImpl(createInstarRequestData(context));
					responseHandler = new InstarResponseHandlerImpl(filterKeys, context );
					operationName = "getIpAddressByVnf";
					break;
				default:
					throw new Exception("No Client registered for : " + parameters.getSource());
	
				}
				responseHandler.processResponse(restClient.sendRequest(operationName),parameters.getName() );
			}
		}
		else
		{
			throw new Exception("NO response Keys set  for : "  + parameters.getRuleType());
		}
	}

	private HashMap<String, String> createInstarRequestData(SvcLogicContext ctxt) {
		HashMap<String, String> requestParams = new HashMap<String, String>();		
		requestParams.put(InstarClientConstant.VNF_NAME, ctxt.getAttribute(InstarClientConstant.VNF_NAME));		
		return requestParams;
	}
}
