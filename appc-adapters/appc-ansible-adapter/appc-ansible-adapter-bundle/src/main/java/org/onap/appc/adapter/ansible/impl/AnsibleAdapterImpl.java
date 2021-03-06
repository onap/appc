/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
 * =============================================================================
 * Modifications Copyright (C) 2019 Orange
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

package org.onap.appc.adapter.ansible.impl;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.onap.appc.adapter.ansible.AnsibleAdapter;
import org.onap.appc.adapter.ansible.model.AnsibleMessageParser;
import org.onap.appc.adapter.ansible.model.AnsibleResult;
import org.onap.appc.adapter.ansible.model.AnsibleResultCodes;
import org.onap.appc.adapter.ansible.model.AnsibleServerEmulator;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.encryption.EncryptionTool;

/**
 * This class implements the {@link AnsibleAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class AnsibleAdapterImpl implements AnsibleAdapter {

    /**
     * The constant used to define the service name in the mapped diagnostic context
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
     * Adapter Name
     */
    private static final String ADAPTER_NAME = "Ansible Adapter";
    private static final String APPC_EXCEPTION_CAUGHT = "APPCException caught";

    private static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.result.code";
    private static final String MESSAGE_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.message";
    private static final String RESULTS_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.results";
    private static final String OUTPUT_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.output";
    private static final String ID_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.Id";
    private static final String LOG_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.log";

    private static final String CLIENT_TYPE_PROPERTY_NAME = "org.onap.appc.adapter.ansible.clientType";
    private static final String TRUSTSTORE_PROPERTY_NAME = "org.onap.appc.adapter.ansible.trustStore";
    private static final String TRUSTPASSWD_PROPERTY_NAME = "org.onap.appc.adapter.ansible.trustStore.trustPasswd";
    private static final String TIMEOUT_PROPERTY_NAME = "org.onap.appc.adapter.ansible.timeout";
    private static final String POLL_INTERVAL_PROPERTY_NAME = "org.onap.appc.adapter.ansible.pollInterval";
    private static final String SOCKET_TIMEOUT_PROPERTY_NAME = "org.onap.appc.adapter.ansible.socketTimeout";
    private static final String PASSWORD = "Password";
    private static final String APPC_PROPS = "/appc.properties";
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String PROPDIR = System.getenv(SDNC_CONFIG_DIR);
    private static final String SERVERIP = "ServerIP";
    private Properties props;
    private int defaultTimeout = 600 * 1000;
    private int defaultSocketTimeout = 60 * 1000;
    private int defaultPollInterval = 60 * 1000;

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleAdapterImpl.class);
    /**
     * Connection object
     **/
    //private ConnectionBuilder httpClient;

    /**
     * Ansible API Message Handlers
     **/
    private AnsibleMessageParser messageProcessor;

    /**
     * indicator whether in test mode
     **/
    private boolean testMode = false;

    /**
     * server emulator object to be used if in test mode
     **/
    private AnsibleServerEmulator testServer;

    /**
     * This default constructor is used as a work around because the activator
     * wasn't getting called
     */
    public AnsibleAdapterImpl() {
        initialize();
    }

    /**
     * Used for jUnit test and testing interface
     */
    public AnsibleAdapterImpl(boolean mode) {
        testMode = mode;
        testServer = new AnsibleServerEmulator();
        messageProcessor = new AnsibleMessageParser();
    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.onap.appc.adapter.rest.AnsibleAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return ADAPTER_NAME;
    }

    /**
     * @param rc
     *            Method posts info to Context memory in case of an error and throws
     *            a SvcLogicException causing SLI to register this as a failure
     */
    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {

        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        svcLogic.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);

        throw new SvcLogicException("Ansible Adapter Error = " + message);
    }

    /**
     * initialize the Ansible adapter based on default and over-ride configuration
     * data
     */
    private void initialize() {
        String path = PROPDIR + APPC_PROPS;
        File propFile = new File(path);
        props = new Properties();
        try {
            InputStream input = new FileInputStream(propFile);
            props.load(input);
        } catch (Exception ex) {
            logger.error("Error while reading appc.properties file" + ex.getMessage());
        }
        // Create the message processor instance 
        messageProcessor = new AnsibleMessageParser();
        //continuing for checking timeout
        try {
            String timeoutStr = props.getProperty(TIMEOUT_PROPERTY_NAME);
            defaultTimeout = Integer.parseInt(timeoutStr) * 1000;

        } catch (Exception e) {
            defaultTimeout = 600 * 1000;
            logger.error("Error while reading time out property" , e);
        }
        //continuing for checking timeout
        try {
            String timeoutStr = props.getProperty(SOCKET_TIMEOUT_PROPERTY_NAME);
            defaultSocketTimeout = Integer.parseInt(timeoutStr) * 1000;

        } catch (Exception e) {
            defaultSocketTimeout = 60 * 1000;
            logger.error("Error while reading socket time out property" , e);
        }
        //continuing for checking timeout
        try {
            String timeoutStr = props.getProperty(POLL_INTERVAL_PROPERTY_NAME);
            defaultPollInterval = Integer.parseInt(timeoutStr) * 1000;

        } catch (Exception e) {
            defaultPollInterval = 60 * 1000;
            logger.error("Error while reading poll interval property" , e);
        }
        logger.info("Initialized Ansible Adapter");
    }

    private ConnectionBuilder getHttpConn(int timeout, String serverIP) {

        String path = PROPDIR + APPC_PROPS;
        File propFile = new File(path);
        props = new Properties();
        InputStream input;
        try {
            input = new FileInputStream(propFile);
            props.load(input);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            logger.error("Error while reading appc.properties file" + ex.getMessage());
        }
        // Create the http client instance
        // type of client is extracted from the property file parameter
        // org.onap.appc.adapter.ansible.clientType
        // It can be :
        // 1. TRUST_ALL (trust all SSL certs). To be used ONLY in dev
        // 2. TRUST_CERT (trust only those whose certificates have been stored in the
        // trustStore file)
        // 3. DEFAULT (trust only well known certificates). This is standard behavior to
        // which it will
        // revert. To be used in PROD
        ConnectionBuilder httpClientLocal = null;
        try {
            String clientType = props.getProperty(CLIENT_TYPE_PROPERTY_NAME);
            logger.info("Ansible http client type set to " + clientType);

            if ("TRUST_ALL".equals(clientType)) {
                logger.info(
                        "Creating http client to trust ALL ssl certificates. WARNING. This should be done only in dev environments");
                httpClientLocal = new ConnectionBuilder(1, timeout);
            } else if ("TRUST_CERT".equals(clientType)) {
                // set path to keystore file
                String trustStoreFile = props.getProperty(TRUSTSTORE_PROPERTY_NAME);
                String key = props.getProperty(TRUSTPASSWD_PROPERTY_NAME);
                char[] trustStorePasswd = EncryptionTool.getInstance().decrypt(key).toCharArray();
                logger.info("Creating http client with trustmanager from " + trustStoreFile);
                httpClientLocal = new ConnectionBuilder(trustStoreFile, trustStorePasswd, timeout, serverIP);
            } else {
                logger.info("Creating http client with default behaviour");
                httpClientLocal = new ConnectionBuilder(0, timeout);
            }
        } catch (Exception e) {
            logger.error("Error Getting HTTP Connection Builder due to Unknown Exception", e);
        }

        logger.info("Got HTTP Connection Builder");
        return httpClientLocal;
    }

    // Public Method to post request to execute playbook. Posts the following back
    // to Svc context memory
    // org.onap.appc.adapter.ansible.req.code : 100 if successful
    // org.onap.appc.adapter.ansible.req.messge : any message
    // org.onap.appc.adapter.ansible.req.Id : a unique uuid to reference the request
    @Override
    public void reqExec(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        String playbookName = StringUtils.EMPTY;
        String payload = StringUtils.EMPTY;
        String agentUrl = StringUtils.EMPTY;
        String user = StringUtils.EMPTY;
        String password = StringUtils.EMPTY;
        String id = StringUtils.EMPTY;
        String timeout = StringUtils.EMPTY;
        JSONObject jsonPayload;

        try {
            // create json object to send request
            jsonPayload = messageProcessor.reqMessage(params);

            logger.info("Initial Payload  = " + jsonPayload.toString());
            agentUrl = (String) jsonPayload.remove("AgentUrl");
            user = (String) jsonPayload.remove("User");
            password = (String)jsonPayload.remove(PASSWORD);
            if(StringUtils.isNotBlank(password))
            password = EncryptionTool.getInstance().decrypt(password);
            id = jsonPayload.getString("Id");
            timeout = jsonPayload.getString("Timeout");
            if (StringUtils.isBlank(timeout))
                timeout = "600";

            String autoNodeList = (String) jsonPayload.remove("AutoNodeList");
            if (autoNodeList != null && Boolean.parseBoolean(autoNodeList)) {
                JSONArray generatedNodeList = generateNodeList(params, ctx);
                if (generatedNodeList.length() > 0) {
                    jsonPayload.put("NodeList", generatedNodeList);
                    jsonPayload.put("InventoryNames", "VM");
                } else {
                    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                            "Auto generation of Node List Failed - no elements on the list");
                }
            } else
                logger.debug("Auto Node List is DISABLED");

            payload = jsonPayload.toString();
            ctx.setAttribute("AnsibleTimeout", timeout);
            logger.info("Updated Payload  = " + payload + " timeout = " + timeout);
        } catch (APPCException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to missing mandatory parameters. Reason = "
                            + e.getMessage());
        } catch (JSONException e) {
            logger.error("JSONException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid JSON block. Reason = "
                            + e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid parameter values. Reason = "
                            + e.getMessage());
        }

        int code = -1;
        String message = StringUtils.EMPTY;

        try {
            // post the test request
            logger.info("Posting ansible request = " + payload + " to url = " + agentUrl);
            AnsibleResult testResult = postExecRequest(agentUrl, payload, user, password,ctx);
          if (testResult != null) {
            logger.info("Received response on ansible post request " + testResult.getStatusMessage());
            // Process if HTTP was successful
            if (testResult.getStatusCode() == 200) {
              testResult = messageProcessor.parsePostResponse(testResult.getStatusMessage());
            } else {
              doFailure(ctx, testResult.getStatusCode(),
                        "Error posting request. Reason = " + testResult.getStatusMessage());
            }
            code = testResult.getStatusCode();
            message = testResult.getStatusMessage();
            String output = testResult.getOutput();
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, output);
            String serverIp = testResult.getServerIp();
            if (StringUtils.isBlank(serverIp))
            ctx.setAttribute(SERVERIP, serverIp);
            else
              ctx.setAttribute(SERVERIP, "");
            // Check status of test request returned by Agent
            if (code == AnsibleResultCodes.PENDING.getValue()) {
              logger.info(String.format("Submission of Test %s successful.", playbookName));
              // test request accepted. We are in asynchronous case
            } else {
              doFailure(ctx, code, "Request for execution of playbook rejected. Reason = " + message);
            }
          } else {
            doFailure(ctx, code, "Ansible Test result is null");
          }
        } catch (APPCException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered when posting request for execution of playbook. Reason = " + e.getMessage());
        }

        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(ID_ATTRIBUTE_NAME, id);
    }

    /**
     * Method is used to automatically generate NodeList section base on the svc context
     *
     * @see org.onap.appc.adapter.ansible.AnsibleAdapter#reqExecResult(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    private JSONArray generateNodeList(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String vfModuleId = params.get("vf-module-id");
        String vnfcName = params.get("vnfc-name");
        String vServerId = params.get("vserver-id");
        String vnfcType = params.get("vnfc-type");
        logger.info("GENERATING NODE LIST");
        JSONArray result = new JSONArray();

        if (vServerId != null && !vServerId.equals(""))
            logger.debug("Auto Node List filtering parameter vserver-id " + vServerId);
        else
            vServerId = null;
        if (vnfcName != null && !vnfcName.equals(""))
            logger.debug("Auto Node List filtering parameter vnfc-name " + vnfcName);
        else
            vnfcName = null;
        if (vnfcType != null && !vnfcType.equals(""))
            logger.debug("Auto Node List filtering parameter vnfc-type " + vnfcType);
        else
            vnfcType = null;
        if (vfModuleId != null && !vfModuleId.equals(""))
            logger.debug("Auto Node List filtering parameter vf-module-id " + vfModuleId);
        else
            vfModuleId = null;

        Map<String, JSONObject> candidates = new HashMap<String, JSONObject>();
        for (int i = 0; ; i++) {
            String vmKey = "tmp.vnfInfo.vm[" + Integer.toString(i) + "]";
            logger.info("Looking for attributes of: " + vmKey);
            if (ctx.getAttribute(vmKey + ".vnfc-name") != null) {
                String debugText = "Auto Node List candidate ";
                String vmVnfcName = ctx.getAttribute(vmKey + ".vnfc-name");
                String vmVnfcIpv4Address = ctx.getAttribute(vmKey + ".vnfc-ipaddress-v4-oam-vip");
                String vmVnfcType = ctx.getAttribute(vmKey + ".vnfc-type");

                if (vmVnfcName != null && vmVnfcIpv4Address != null && vmVnfcType != null
                    && !vmVnfcName.equals("") && !vmVnfcIpv4Address.equals("") && !vmVnfcType.equals("")) {
                    if (vServerId != null) {
                        String vmVserverId = ctx.getAttribute(vmKey + ".vserver-id");
                        if (vmVserverId == null || !vmVserverId.equals(vServerId)) {
                            logger.debug(debugText + vmVnfcName + " dropped. vserver-id mismatch");
                            continue;
                        }
                    }
                    if (vfModuleId != null) {
                        String vmVfModuleId = ctx.getAttribute(vmKey + ".vf-module-id");
                        if (vmVfModuleId == null || !vmVfModuleId.equals(vfModuleId)) {
                            logger.debug(debugText + vmVnfcName + " dropped. vf-module-id mismatch");
                            continue;
                        }
                    }
                    if (vnfcName != null) {
                        if (!vmVnfcName.equals(vnfcName)) {
                            logger.debug(debugText + vmVnfcName + " dropped. vnfc-name mismatch");
                            continue;
                        }
                    }
                    if (vnfcType != null) {
                        if (!vmVnfcType.equals(vnfcType)) {
                            logger.debug(debugText + vmVnfcType + " dropped. vnfc-type mismatch");
                            continue;
                        }
                    }

                    logger.info(debugText + vmVnfcName + " [" + vmVnfcIpv4Address + "," + vmVnfcType + "]");

                    JSONObject vnfTypeCandidates = null;
                    JSONArray vmList = null;
                    if (!candidates.containsKey(vmVnfcType)) {
                        vnfTypeCandidates = new JSONObject();
                        vmList = new JSONArray();
                        vnfTypeCandidates.put("site", "site");
                        vnfTypeCandidates.put("vnfc-type", vmVnfcType);
                        vnfTypeCandidates.put("vm-info", vmList);
                        candidates.put(vmVnfcType, vnfTypeCandidates);
                    } else {
                        vnfTypeCandidates = candidates.get(vmVnfcType);
                        vmList = (JSONArray) vnfTypeCandidates.get("vm-info");
                    }

                    JSONObject candidate = new JSONObject();
                    candidate.put("ne_id", vmVnfcName);
                    candidate.put("fixed_ip_address", vmVnfcIpv4Address);
                    vmList.put(candidate);
                } else {
                    logger.warn("Incomplete information for Auto Node List candidate " + vmKey);
                }
            } else
                break;
        }

        for(JSONObject vnfcCandidates : candidates.values()) {
            result.put(vnfcCandidates);
        }

        logger.info("GENERATING NODE LIST COMPLETED");
        return result;
    }

    /**
     * Public method to query status of a specific request It blocks till the
     * Ansible Server responds or the session times out (non-Javadoc)
     *
     * @see org.onap.appc.adapter.ansible.AnsibleAdapter#reqExecResult(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public void reqExecResult(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        // Get URI
        String reqUri = StringUtils.EMPTY;

        try {
            String serverIp = ctx.getAttribute(SERVERIP);
            if (StringUtils.isNotBlank(serverIp))
                reqUri = messageProcessor.reqUriResultWithIP(params, serverIp);
            else
                reqUri = messageProcessor.reqUriResult(params);
            logger.info("Got uri " + reqUri);
        } catch (APPCException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to missing parameters. Reason = "
                            + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to invalid parameters value. Reason = "
                            + e.getMessage());
            return;
        }

        int code = -1;
        String message = StringUtils.EMPTY;
        String results = StringUtils.EMPTY;
        String output = StringUtils.EMPTY;
        String configData = StringUtils.EMPTY;
        String finalResponse = StringUtils.EMPTY;
        try {
            // Try to retrieve the test results (modify the URL for that)
            AnsibleResult testResult = queryServer(reqUri, params.get("User"),
                    EncryptionTool.getInstance().decrypt(params.get(PASSWORD)), ctx);
            code = testResult.getStatusCode();
            message = testResult.getStatusMessage();

            if (code == 200 || code == 400 || "FINISHED".equalsIgnoreCase(message)) {
                logger.info("Parsing response from ansible Server = " + message);
                // Valid HTTP. process the Ansible message
                testResult = messageProcessor.parseGetResponse(message);
                code = testResult.getStatusCode();
                message = testResult.getStatusMessage();
                results = testResult.getResults();
                output = testResult.getOutput();
                configData = testResult.getconfigData();            
                if ((StringUtils.isBlank(output) ) ||(output.trim().equalsIgnoreCase("{}"))) {
                      finalResponse = results;
                } else {
                    finalResponse = output;
                }
                logger.info("configData from ansible's response = " + configData);
                ctx.setAttribute("device-running-config", configData);
            }
            logger.info("Request response = " + message);
        } catch (APPCException e) {
            ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retrieving result : " + e.getMessage());
            return;
        }

        // We were able to get and process the results. Determine if playbook succeeded

        if (code == AnsibleResultCodes.FINAL_SUCCESS.getValue()) {
            message = String.format("Ansible Request  %s finished with Result = %s, Message = %s", params.get("Id"),
                    OUTCOME_SUCCESS, message);
            logger.info(message);
        } else {
            logger.info(String.format("Ansible Request  %s finished with Result %s, Message = %s", params.get("Id"),
                    OUTCOME_FAILURE, message));
            ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
            doFailure(ctx, code, message);
            return;
        }

        // In case of 200,400,FINISHED return 400
        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(400));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
        ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
        ctx.setStatus(OUTCOME_SUCCESS);
    }

    /**
     * Public method to get logs from playbook execution for a specific request
     *
     * It blocks till the Ansible Server responds or the session times out very
     * similar to reqExecResult logs are returned in the DG context variable
     * org.onap.appc.adapter.ansible.log
     */
    @Override
    public void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        String reqUri = StringUtils.EMPTY;
        try {
            reqUri = messageProcessor.reqUriLog(params);
            logger.info("Retrieving results from " + reqUri);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
        }

        String message = StringUtils.EMPTY;
        try {
            // Try to retrieve the test results (modify the url for that)
            AnsibleResult testResult = queryServer(reqUri, params.get("User"),
                    EncryptionTool.getInstance().decrypt(params.get(PASSWORD)), ctx);
            message = testResult.getStatusMessage();
            logger.info("Request output = " + message);
            ctx.setAttribute(LOG_ATTRIBUTE_NAME, message);
            ctx.setStatus(OUTCOME_SUCCESS);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retreiving output : " + e.getMessage());
        }
    }

    /**
     * Method that posts the request
     */
    private AnsibleResult postExecRequest(String agentUrl, String payload, String user, String password,
            SvcLogicContext ctx) {

        AnsibleResult testResult = null;
        ConnectionBuilder httpClientLocal = getHttpConn(defaultSocketTimeout, "");
        if (!testMode) {
          if(httpClientLocal!=null) {
            httpClientLocal.setHttpContext(user, password);
            testResult = httpClientLocal.post(agentUrl, payload);
            httpClientLocal.close();
          }
        } else {
            testResult = testServer.post(agentUrl, payload);
        }
        return testResult;
    }

    /**
     * Method to query Ansible server
     */
    private AnsibleResult queryServer(String agentUrl, String user, String password, SvcLogicContext ctx) {

        AnsibleResult testResult = new AnsibleResult();
        int timeout = 600 * 1000;
        try {
            timeout = Integer.parseInt(ctx.getAttribute("AnsibleTimeout")) * 1000;

        } catch (Exception e) {
            timeout = defaultTimeout;
        }
        long endTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < endTime) {
            String serverIP = ctx.getAttribute(SERVERIP);
            ConnectionBuilder httpClientLocal = getHttpConn(defaultSocketTimeout, serverIP);
            logger.info("Querying ansible GetResult URL = " + agentUrl);

            if (!testMode) {
              if(httpClientLocal!=null) {
                httpClientLocal.setHttpContext(user, password);
                testResult = httpClientLocal.get(agentUrl);
                httpClientLocal.close();
              }
            } else {
                testResult = testServer.get(agentUrl);
            }
            if (testResult.getStatusCode() != AnsibleResultCodes.IO_EXCEPTION.getValue()
                    && testResult.getStatusCode() != AnsibleResultCodes.PENDING.getValue()) {
                break;
            }
            
            try {
                Thread.sleep(defaultPollInterval);
            } catch (InterruptedException ex) {
              logger.error("Thread Interrupted Exception", ex);
              Thread.currentThread().interrupt();
            }

        }
        if (testResult.getStatusCode() == AnsibleResultCodes.PENDING.getValue()) {
            testResult.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
            testResult.setStatusMessage("Request timed out");
        }

        return testResult;
    }
}
