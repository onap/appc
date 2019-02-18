/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * Modifications Copyright (C) 2019 Amdocs
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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.appc.adapter.ansible.AnsibleAdapter;
import org.onap.appc.adapter.ansible.model.AnsibleMessageParser;
import org.onap.appc.adapter.ansible.model.AnsibleResult;
import org.onap.appc.adapter.ansible.model.AnsibleResultCodes;
import org.onap.appc.adapter.ansible.model.AnsibleServerEmulator;
import org.onap.appc.encryption.EncryptionTool;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
    private static final String propDir = System.getenv(SDNC_CONFIG_DIR);
    private Properties props;
    private int defaultTimeout = 600 * 1000;
    private int defaultSocketTimeout = 60 * 1000;
    private int defaultPollInterval = 60 * 1000;

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleAdapterImpl.class);

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
        String path = propDir + APPC_PROPS;
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
        }
        //continuing for checking timeout
        try {
            String timeoutStr = props.getProperty(SOCKET_TIMEOUT_PROPERTY_NAME);
            defaultSocketTimeout = Integer.parseInt(timeoutStr) * 1000;

        } catch (Exception e) {
            defaultSocketTimeout = 60 * 1000;
        }
        //continuing for checking timeout
        try {
            String timeoutStr = props.getProperty(POLL_INTERVAL_PROPERTY_NAME);
            defaultPollInterval = Integer.parseInt(timeoutStr) * 1000;

        } catch (Exception e) {
            defaultPollInterval = 60 * 1000;
        }
        logger.info("Initialized Ansible Adapter");
    }

    private ConnectionBuilder getHttpConn(int timeout, String serverIP) {

        String path = propDir + APPC_PROPS;
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
        ConnectionBuilder httpClient = null;
        try {
            String clientType = props.getProperty(CLIENT_TYPE_PROPERTY_NAME);
            logger.info("Ansible http client type set to " + clientType);

            if ("TRUST_ALL".equals(clientType)) {
                logger.info(
                        "Creating http client to trust ALL ssl certificates. WARNING. This should be done only in dev environments");
                httpClient = new ConnectionBuilder(1, timeout);
            } else if ("TRUST_CERT".equals(clientType)) {
                // set path to keystore file
                String trustStoreFile = props.getProperty(TRUSTSTORE_PROPERTY_NAME);
                String key = props.getProperty(TRUSTPASSWD_PROPERTY_NAME);
                char[] trustStorePasswd = EncryptionTool.getInstance().decrypt(key).toCharArray();
                logger.info("Creating http client with trustmanager from " + trustStoreFile);
                httpClient = new ConnectionBuilder(trustStoreFile, trustStorePasswd, timeout, serverIP);
            } else {
                logger.info("Creating http client with default behaviour");
                httpClient = new ConnectionBuilder(0, timeout);
            }
        } catch (Exception e) {
            logger.error("Error Getting HTTP Connection Builder due to Unknown Exception", e);
        }

        logger.info("Got HTTP Connection Builder");
        return httpClient;
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

            agentUrl = (String) jsonPayload.remove("AgentUrl");
            user = (String) jsonPayload.remove("User");
            password = (String)jsonPayload.remove(PASSWORD);
            if(StringUtils.isNotBlank(password)) {
                password = EncryptionTool.getInstance().decrypt(password);
            }
            id = jsonPayload.getString("Id");
            timeout = jsonPayload.getString("Timeout");
            if (StringUtils.isBlank(timeout))
                timeout = "600";
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
                ctx.setAttribute("ServerIP", serverIp);
            else
                ctx.setAttribute("ServerIP", "");
            // Check status of test request returned by Agent
            if (code == AnsibleResultCodes.PENDING.getValue()) {
                logger.info(String.format("Submission of Test %s successful.", playbookName));
                // test request accepted. We are in asynchronous case
            } else {
                doFailure(ctx, code, "Request for execution of playbook rejected. Reason = " + message);
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
            String serverIp = ctx.getAttribute("ServerIP");
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
                ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, output);
            }
            logger.info("Request response = " + message);
        } catch (APPCException e) {
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
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, output);
            doFailure(ctx, code, message);
            return;
        }

        // In case of 200,400,FINISHED return 400
        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, "400");
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
        ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, output);
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

        AnsibleResult testResult;
        ConnectionBuilder httpClient = getHttpConn(defaultSocketTimeout, "");
        if (!testMode && null != httpClient) {
            httpClient.setHttpContext(user, password);
            testResult = httpClient.post(agentUrl, payload);
            httpClient.close();
        } else {
            testResult = testServer.Post(agentUrl, payload);
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
            String serverIP = ctx.getAttribute("ServerIP");
            ConnectionBuilder httpClient = getHttpConn(defaultSocketTimeout, serverIP);
            logger.info("Querying ansible GetResult URL = " + agentUrl);

            if (!testMode && null != httpClient) {
                httpClient.setHttpContext(user, password);
                testResult = httpClient.get(agentUrl);
                httpClient.close();
            } else {
                testResult = testServer.Get(agentUrl);
            }
            if (testResult.getStatusCode() != AnsibleResultCodes.IO_EXCEPTION.getValue()
                    && testResult.getStatusCode() != AnsibleResultCodes.PENDING.getValue()) {
                break;
            }
            
            try {
                Thread.sleep(defaultPollInterval);
            } catch (InterruptedException ex) {
                logger.log(EELFLogger.Level.ERROR, ex.getMessage(), ex);
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
