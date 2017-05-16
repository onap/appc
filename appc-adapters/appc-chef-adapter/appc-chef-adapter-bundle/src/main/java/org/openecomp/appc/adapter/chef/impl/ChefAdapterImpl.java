/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.adapter.chef.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.chef.ChefAdapter;
import org.openecomp.appc.adapter.chef.chefapi.*;
import org.openecomp.appc.adapter.chef.chefclient.*;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolExtensionException;
import org.openecomp.appc.util.StructuredPropertyHelper;
import org.openecomp.appc.util.StructuredPropertyHelper.Node;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.pal.util.StringHelper;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.cdp.zones.model.Server.Status;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import static com.att.eelf.configuration.Configuration.*;

import java.io.IOException;

import java.net.InetAddress;
/**
 * This class implements the {@link ChefAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class ChefAdapterImpl implements ChefAdapter {

	/**
	 * The constant used to define the adapter name in the mapped diagnostic
	 * context
	 */
	
	//chef server Initialize variable
	public String clientName="";
	public String clientPrivatekey="";
	public String chefserver="";
	public String serverAddress="";
	public String organizations="";
	@SuppressWarnings("nls")
	public static final String MDC_ADAPTER = "adapter";

	/**
	 * The constant used to define the service name in the mapped diagnostic
	 * context
	 */
	@SuppressWarnings("nls")
	public static final String MDC_SERVICE = "service";

	/**
	 * The constant for the status code for a failed outcome
	 */
	@SuppressWarnings("nls")
	public static final String OUTCOME_FAILURE = "failure";

	/**
	 * The constant for the status code for a successful outcome
	 */
	@SuppressWarnings("nls")
	public static final String OUTCOME_SUCCESS = "success";

	/**
	 * A constant for the property token "provider" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER = "provider";

	/**
	 * A constant for the property token "identity" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_IDENTITY = "identity";

	/**
	 * A constant for the property token "name" used in the structured property
	 * specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_NAME = "name";

	/**
	 * A constant for the property token "tenant" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT = "tenant";

	/**
	 * A constant for the property token "tenant name" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_NAME = "name";

	/**
	 * A constant for the property token "password" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR

	/**
	 * A constant for the property token "userid" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";

	/**
	 * A constant for the property token "type" used in the structured property
	 * specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TYPE = "type";

	/**
	 * The name of the service to restart a server
	 */
	@SuppressWarnings("nls")
	public static final String PING_SERVICE = "pingServer";

	/**
	 * The logger to be used
	 */
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefAdapterImpl.class);

	/**
	 * The constant for a left parenthesis
	 */
	private static final char LPAREN = '(';

	/**
	 * The constant for a new line control code
	 */
	private static final char NL = '\n';

	/**
	 * The constant for a single quote
	 */
	private static final char QUOTE = '\'';

	/**
	 * The constant for a right parenthesis
	 */
	private static final char RPAREN = ')';

	/**
	 * The constant for a space
	 */
	private static final char SPACE = ' ';

	/**
	 * A reference to the adapter configuration object.
	 */
	private Configuration configuration;

	/**
	 * This default constructor is used as a work around because the activator
	 * wasnt getting called
	 */
	public ChefAdapterImpl() {
		initialize();

	}

	/**
	 * This constructor is used primarily in the test cases to bypass
	 * initialization of the adapter for isolated, disconnected testing
	 * 
	 * @param initialize
	 *            True if the adapter is to be initialized, can false if not
	 */
	public ChefAdapterImpl(boolean initialize) {
		configuration = ConfigurationFactory.getConfiguration();
		if (initialize) {
			initialize();

		}
	}

	/**
	 * @param props
	 *            not used
	 */
	public ChefAdapterImpl(Properties props) {
		initialize();

	}

	public ChefAdapterImpl(String key) {
		initialize(key);

	}
	
	/**
	 * Returns the symbolic name of the adapter
	 * 
	 * @return The adapter name
	 * @see org.openecomp.appc.adapter.chef.ChefAdapter#getAdapterName()
	 */
	@Override
	public String getAdapterName() {
		return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
	}

	private void X__________________________________X() {
	}

	/**
	 * @see org.openecomp.appc.adapter.chef.ChefAdapter#evacuateServer(java.util.Map,
	 *      org.openecomp.sdnc.sli.SvcLogicContext)
	 */

	private void X___________________________________X() {
	}

	/**
	 * @see org.openecomp.appc.adapter.chef.ProviderAdapter#rebuildServer(java.util.Map,
	 *      org.openecomp.sdnc.sli.SvcLogicContext)
	 */

	/**
	 * Send GET request to chef server
	 */

	@SuppressWarnings("nls")
	@Override
	public void chefGet(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("chef get method");
		String chefAction= params.get("org.openecomp.appc.instance.chefAction");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		//should load pem from somewhere else
		ChefApiClient cac = new ChefApiClient(clientName,clientPrivatekey,chefserver,organizations);
		// need pass path into it
		//"/nodes"
		ApiMethod am = cac.get(chefAction);
		am.execute();
		int code = am.getReturnCode();
		String message = am.getResponseBodyAsString();
		logger.info(code + "   " + message);
		chefServerResult(rc,Integer.toString(code),message);
	}

	/**
	 * Send PUT request to chef server
	 */

	@SuppressWarnings("nls")
	@Override
	public void chefPut(Map<String, String> params, SvcLogicContext ctx) {

		logger.info("chef PUT method");
		logger.info(clientName+" "+clientPrivatekey+" "+chefserver+" "+organizations);
		String chefAction= params.get("org.openecomp.appc.instance.chefAction");
		String runList= params.get("org.openecomp.appc.instance.runList");
		String attributes= params.get("org.openecomp.appc.instance.attributes");
		logger.info(attributes);
		String CHEF_NODE_STR = "{\"json_class\":\"Chef::Node\",\"default\":{},\"chef_type\":\"node\",\"run_list\":[\""+runList+"\"],\"override\":{},\"automatic\":{},\"normal\":{"+attributes+"},\"name\":\"testnode\",\"chef_environment\":\"_default\"}";
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		//should load pem from somewhere else
		ChefApiClient cac = new ChefApiClient(clientName,clientPrivatekey,chefserver,organizations);
		
		// need pass path into it
		//"/nodes/testnode"
		ApiMethod am = cac.put(chefAction).body(CHEF_NODE_STR);
		am.execute();
		int code = am.getReturnCode();
		String message = am.getResponseBodyAsString();
		logger.info(code + "   " + message);
		chefServerResult(rc,Integer.toString(code),message);
	}
	
	
	
	/**
	 * Trigger target vm run chef
	 */

	@SuppressWarnings("nls")
	@Override
	public void trigger(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run trigger method");
		String tVmIp = params.get("org.openecomp.appc.instance.ip");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();

		try {
			HttpGet httpGet = new HttpGet(tVmIp);
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpGet);
			int responseCode=response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput=EntityUtils.toString(entity);
			chefClientResult(rc,Integer.toString(responseCode),responseOutput);
			doSuccess(rc);
		} catch (Exception ex) {
			doFailure(rc, 500 , ex.toString());
		}
	}
	


	
	@SuppressWarnings("static-method")
	private void doFailure(RequestContext rc, int code, String message) {
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		String msg = (message == null) ? Integer.toString(code) : message;
		if (msg.contains("\n")) {
			msg = msg.substring(msg.indexOf("\n"));
		}

		String status;
		try {
			status = Integer.toString(code);
		} catch (Exception e) {
			status = "500";
		}
		svcLogic.setAttribute("org.openecomp.appc.chefAgent.code", status);
		svcLogic.setAttribute("org.openecomp.appc.chefAgent.message", msg);
	}

	/**
	 * @param rc
	 *            The request context that manages the state and recovery of the
	 *            request for the life of its processing.
	 */

	@SuppressWarnings("static-method")
	private void doSuccess(RequestContext rc) {
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setAttribute("org.openecomp.appc.chefAgent.code", "200");
	}
	
	
	@SuppressWarnings("static-method")
	private void chefServerResult(RequestContext rc, String code ,String message) {
		String msg = (message == null) ? " " : message;
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setStatus(OUTCOME_SUCCESS);
		svcLogic.setAttribute("org.openecomp.appc.chefServerResult.code", code);
		svcLogic.setAttribute("org.openecomp.appc.chefServerResult.message", message);
	}

	@SuppressWarnings("static-method")
	private void chefClientResult(RequestContext rc, String code ,String message) {
		String msg = (message == null) ? " " : message;
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setStatus(OUTCOME_SUCCESS);
		svcLogic.setAttribute("org.openecomp.appc.chefClientResult.code", code);
		svcLogic.setAttribute("org.openecomp.appc.chefClientResult.message", message);
	}

	/**
	 * initialize the provider adapter by building the context cache
	 */
	private void initialize() {
		configuration = ConfigurationFactory.getConfiguration();
		//need to fetch data from appc configurator or form some file in the appc vms
		clientName="testnode";
	    clientPrivatekey="/etc/chef/client.pem";
	    serverAddress="http://example.com";
	    organizations="test";    
		chefserver=serverAddress+"/organizations/"+organizations;	
		logger.info("Initialize Chef Adapter");
	}
	
	private void initialize(String key) {
		configuration = ConfigurationFactory.getConfiguration();
		//need to fetch data from appc configurator or form some file in the appc vms
		clientName="testnode";
	    clientPrivatekey=key;
	    serverAddress="http://example.com";
	    organizations="test";    
		chefserver=serverAddress+"/organizations/"+organizations;	
		logger.info("Initialize Chef Adapter");
	}

}
