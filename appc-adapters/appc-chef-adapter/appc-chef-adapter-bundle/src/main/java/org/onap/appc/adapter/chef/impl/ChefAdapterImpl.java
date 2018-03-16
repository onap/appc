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
package org.onap.appc.adapter.chef.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.appc.adapter.chef.ChefAdapter;
import org.onap.appc.adapter.chef.chefclient.ChefApiClientFactory;
import org.onap.appc.adapter.chef.chefclient.api.ChefApiClient;
import org.onap.appc.adapter.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * This class implements the {@link ChefAdapter} interface. This interface defines the behaviors that our service
 * provides.
 */
public class ChefAdapterImpl implements ChefAdapter {

    // chef server Initialize variable
    private String username = StringUtils.EMPTY;
    private String clientPrivatekey = StringUtils.EMPTY;
    private String chefserver = StringUtils.EMPTY;
    private String serverAddress = StringUtils.EMPTY;
    private String organizations = StringUtils.EMPTY;

    @SuppressWarnings("nls")
    public static final String MDC_ADAPTER = "adapter";

    @SuppressWarnings("nls")
    public static final String MDC_SERVICE = "service";

    @SuppressWarnings("nls")
    public static final String OUTCOME_FAILURE = "failure";

    @SuppressWarnings("nls")
    public static final String OUTCOME_SUCCESS = "success";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER = "provider";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_IDENTITY = "identity";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_NAME = "name";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT = "tenant";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_NAME = "name";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";

    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TYPE = "type";


    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefAdapterImpl.class);

    private static final String CANNOT_FIND_PRIVATE_KEY_STR =
        "Cannot find the private key in the APPC file system, please load the private key to ";

    private static final String POSTING_REQUEST_JSON_ERROR_STR = "Error posting request due to invalid JSON block: ";
    private static final String POSTING_REQUEST_ERROR_STR = "Error posting request: ";
    private static final String CHEF_CLIENT_RESULT_CODE_STR = "chefClientResult.code";
    private static final String CHEF_SERVER_RESULT_CODE_STR = "chefServerResult.code";
    private static final String CHEF_CLIENT_RESULT_MSG_STR = "chefClientResult.message";
    private static final String CHEF_SERVER_RESULT_MSG_STR = "chefServerResult.message";
    private static final String CHEF_ACTION_STR = "chefAction";
    private static final String NODE_LIST_STR = "NodeList";
    private final ChefApiClientFactory chefApiClientFactory;
    private final PrivateKeyChecker privateKeyChecker;

    ChefAdapterImpl(ChefApiClientFactory chefApiClientFactory, PrivateKeyChecker privateKeyChecker) {
        this.chefApiClientFactory = chefApiClientFactory;
        this.privateKeyChecker = privateKeyChecker;
        logger.info("Initialize Chef Adapter");
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcEnvironment(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            logger.info("environment of VNF-C");
            chefInfo(params, ctx);
            RequestContext rc = new RequestContext(ctx);
            logger.info("Context" + ctx);
            rc.isAlive();
            String env = params.get("Environment");
            logger.info("Environmnet" + env);
            if (env.equals(StringUtils.EMPTY)) {
                chefServerResult(rc, 200, "Skip Environment block ");
            } else {
                JSONObject envJ = new JSONObject(env);
                String envName = envJ.getString("name");
                String message;
                if (privateKeyChecker.doesExist(clientPrivatekey)) {
                    // update the details of an environment on the Chef server.
                    ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
                    ChefResponse chefResponse = chefApiClient.put("/environments/" + envName, env);
                    code = chefResponse.getStatusCode();
                    message = chefResponse.getBody();
                    if (code == 404) {
                        // need create a new environment
                        chefResponse = chefApiClient.post("/environments", env);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                        logger.info("requestbody {}", chefResponse.getBody());
                    }

                } else {
                    code = 500;
                    message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                    doFailure(ctx, code, message);
                }
                chefServerResult(rc, code, message);
            }
        } catch (JSONException e) {
            code = 401;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            logger.error(POSTING_REQUEST_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcNodeobjects(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("update the nodeObjects of VNF-C");
        int code;
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            String nodeS = params.get("Node");
            if (StringUtils.isNotBlank(nodeListS) && StringUtils.isNotBlank(nodeS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                code = 200;
                String message = null;
                if (privateKeyChecker.doesExist(clientPrivatekey)) {
                    ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);

                    for (String nodeName: nodes) {
                        JSONObject nodeJ = new JSONObject(nodeS);
                        nodeJ.remove("name");
                        nodeJ.put("name", nodeName);
                        String nodeObject = nodeJ.toString();
                        logger.info(nodeObject);
                        ChefResponse chefResponse = cac.put("/nodes/" + nodeName, nodeObject);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                        if (code != 200) {
                            break;
                        }
                    }
                } else {
                    code = 500;
                    message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                    doFailure(ctx, code, message);
                }
                chefServerResult(rc, code, message);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s) Node , NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            logger.error(POSTING_REQUEST_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    @Override
    public void vnfcPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String nodeList = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeList)) {
                String isCallback = params.get("CallbackCapable");
                String chefAction = "/pushy/jobs";
                // need work on this
                String pushRequest;
                if ("true".equals(isCallback)) {
                    String requestId = params.get("RequestId");
                    String callbackUrl = params.get("CallbackUrl");
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                        + nodeList + "," + "\"env\": {\"RequestId\": \"" + requestId + "\", \"CallbackUrl\": \""
                        + callbackUrl + "\"}," + "\"capture_output\": true" + "}";
                } else {
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                        + nodeList + "," + "\"env\": {}," + "\"capture_output\": true" + "}";
                }
                RequestContext rc = new RequestContext(ctx);

                rc.isAlive();
                SvcLogicContext svcLogic = rc.getSvcLogicContext();
                ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
                ChefResponse chefResponse = cac.post(chefAction, pushRequest);
                code = chefResponse.getStatusCode();
                logger.info("pushRequest:" + pushRequest);
                logger.info("requestbody: {}", chefResponse.getBody());
                String message = chefResponse.getBody();
                if (code == 201) {
                    int startIndex = message.indexOf("jobs") + 5;
                    int endIndex = message.length() - 2;
                    String jobID = message.substring(startIndex, endIndex);
                    svcLogic.setAttribute("jobID", jobID);
                    logger.info(jobID);
                }
                chefServerResult(rc, code, message);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            logger.error(POSTING_REQUEST_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void fetchResults(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code = 200;
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeListS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                JSONObject result = new JSONObject();
                String returnMessage = StringUtils.EMPTY;

                for (String node : nodes) {
                    String chefAction = "/nodes/" + node;
                    String message;
                    if (privateKeyChecker.doesExist(clientPrivatekey)) {
                        ChefResponse chefResponse = getApiMethod(chefAction);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                    } else {
                        code = 500;
                        message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                        doFailure(ctx, code, message);
                    }
                    if (code == 200) {
                        JSONObject nodeResult = new JSONObject();
                        JSONObject allNodeData = new JSONObject(message);
                        allNodeData = allNodeData.getJSONObject("normal");
                        String attribute = "PushJobOutput";

                        String resultData = allNodeData.optString(attribute);
                        if (resultData == null) {
                            resultData = allNodeData.optJSONObject(attribute).toString();

                            if (resultData == null) {
                                resultData = allNodeData.optJSONArray(attribute).toString();

                                if (resultData == null) {
                                    code = 500;
                                    returnMessage = "Cannot find " + attribute;
                                    break;
                                }
                            }
                        }
                        nodeResult.put(attribute, resultData);
                        result.put(node, nodeResult);
                        returnMessage = result.toString();
                    } else {
                        code = 500;
                        returnMessage = message + " Cannot access: " + node;
                        doFailure(ctx, code, message);
                        break;
                    }
                }

                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                chefServerResult(rc, code, returnMessage);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            logger.error(POSTING_REQUEST_ERROR_STR , e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    private ChefResponse getApiMethod(String chefAction) {
        ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
        return cac.get(chefAction);
    }

    /**
     * build node object
     */
    @SuppressWarnings("nls")
    @Override
    public void nodeObejctBuilder(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("nodeObejctBuilder");
        String name = params.get("nodeobject.name");
        String normal = params.get("nodeobject.normal");
        String overrides = params.get("nodeobject.overrides");
        String defaults = params.get("nodeobject.defaults");
        String runList = params.get("nodeobject.run_list");
        String chefEnvironment = params.get("nodeobject.chef_environment");
        String nodeObject = "{\"json_class\":\"Chef::Node\",\"default\":{" + defaults
            + "},\"chef_type\":\"node\",\"run_list\":[" + runList + "],\"override\":{" + overrides
            + "},\"normal\": {" + normal + "},\"automatic\":{},\"name\":\"" + name + "\",\"chef_environment\":\""
            + chefEnvironment + "\",}";
        logger.info(nodeObject);
        ctx.setAttribute("chef.nodeObject", nodeObject);
    }

    /**
     * send get request to chef server
     */
    private void chefInfo(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        username = params.get("username");
        serverAddress = params.get("serverAddress");
        organizations = params.get("organizations");
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(serverAddress)
            && StringUtils.isNotBlank(organizations)) {
            chefserver = "https://" + serverAddress + "/organizations/" + organizations;
            clientPrivatekey = "/opt/onap/appc/chef/" + serverAddress + "/" + organizations + "/" + username + ".pem";
            logger.info(" clientPrivatekey  " + clientPrivatekey);
        } else {
            doFailure(ctx, 401, "Missing mandatory param(s) such as username, serverAddress, organizations");
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void retrieveData(Map<String, String> params, SvcLogicContext ctx) {
        String allConfigData = params.get("allConfig");
        String key = params.get("key");
        String dgContext = params.get("dgContext");
        JSONObject jsonConfig = new JSONObject(allConfigData);
        String contextData = fetchContextData(key, jsonConfig);

        ctx.setAttribute(dgContext, contextData);
    }

    private String fetchContextData(String key, JSONObject jsonConfig) {
        try {
            return jsonConfig.getString(key);
        } catch (Exception e) {
            logger.error("Failed getting string value corresponding to " + key + ". Trying to fetch nested json object", e);
            try {
                return jsonConfig.getJSONObject(key).toString();
            } catch (Exception ex) {
                logger.error("Failed getting json object corresponding to " + key + ". Trying to fetch array", ex);
                return jsonConfig.getJSONArray(key).toString();
            }
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void combineStrings(Map<String, String> params, SvcLogicContext ctx) {
        String string1 = params.get("String1");
        String string2 = params.get("String2");
        String dgContext = params.get("dgContext");
        String contextData = string1 + string2;
        ctx.setAttribute(dgContext, contextData);
    }

    /**
     * Send GET request to chef server
     */
    @SuppressWarnings("nls")

    @Override
    public void chefGet(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef get method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;

        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefResponse chefResponse = getApiMethod(chefAction);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        chefServerResult(rc, code, message);
    }

    /**
     * Send PUT request to chef server
     */
    @SuppressWarnings("nls")

    @Override
    public void chefPut(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        String chefNodeStr = params.get("chefRequestBody");
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);

            ChefResponse chefResponse = chefApiClient.put(chefAction, chefNodeStr);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * send Post request to chef server
     */
    @Override
    public void chefPost(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        logger.info("chef Post method");
        logger.info(username + " " + clientPrivatekey + " " + chefserver + " " + organizations);
        String chefNodeStr = params.get("chefRequestBody");
        String chefAction = params.get(CHEF_ACTION_STR);

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        // should load pem from somewhere else
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);

            // need pass path into it
            // "/nodes/testnode"
            ChefResponse chefResponse = chefApiClient.post(chefAction, chefNodeStr);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * send delete request to chef server
     */
    @Override
    public void chefDelete(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef delete method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
            ChefResponse chefResponse = chefApiClient.delete(chefAction);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * Trigger target vm run chef
     */
    @Override
    public void trigger(Map<String, String> params, SvcLogicContext svcLogicContext) {
        logger.info("Run trigger method");
        String tVmIp = params.get("ip");
        RequestContext rc = new RequestContext(svcLogicContext);
        rc.isAlive();

        try {
            ChefResponse chefResponse = chefApiClientFactory.create(tVmIp).get("");
            chefClientResult(rc, chefResponse.getStatusCode(), chefResponse.getBody());
            svcLogicContext.setAttribute("chefAgent.code", "200");
        } catch (Exception e) {
            logger.error("An error occurred when executing trigger method", e);
            svcLogicContext.setAttribute("chefAgent.code", "500");
            svcLogicContext.setAttribute("chefAgent.message", e.toString());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void checkPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String jobID = params.get("jobid");
            String retry = params.get("retryTimes");
            String intrva = params.get("retryInterval");
            if (StringUtils.isNotBlank(jobID) && StringUtils.isNotBlank(retry) && StringUtils.isNotBlank(intrva)) {

                int retryTimes = Integer.parseInt(params.get("retryTimes"));
                int retryInterval = Integer.parseInt(params.get("retryInterval"));

                String chefAction = "/pushy/jobs/" + jobID;

                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                SvcLogicContext svcLogic = rc.getSvcLogicContext();
                String message = StringUtils.EMPTY;
                String status = StringUtils.EMPTY;
                for (int i = 0; i < retryTimes; i++) {
                    sleepFor(retryInterval);
                    ChefResponse chefResponse = getApiMethod(chefAction);
                    code = chefResponse.getStatusCode();
                    message = chefResponse.getBody();
                    JSONObject obj = new JSONObject(message);
                    status = obj.getString("status");
                    if (!"running".equals(status)) {
                        logger.info(i + " time " + code + "   " + status);
                        break;
                    }
                }
                resolveSvcLogicAttributes(svcLogic, message, status);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s) retryTimes , retryInterval ");
            }
        } catch (Exception e) {
            code = 401;
            logger.error("An error occurred when executing checkPushJob method", e);
            doFailure(ctx, code, e.getMessage());
        }
    }

    private void resolveSvcLogicAttributes(SvcLogicContext svcLogic, String message, String status) {
        if ("complete".equals(status)) {
            svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "200");
            svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
        } else if ("running".equals(status)) {
            svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "202");
            svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, "chef client runtime out");
        } else {
            svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "500");
            svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
        }
    }

    private void sleepFor(int retryInterval) {
        try {
            Thread.sleep(retryInterval); // 1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void pushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String pushRequest = params.get("pushRequest");
            String chefAction = "/pushy/jobs";
            RequestContext rc = new RequestContext(ctx);
            rc.isAlive();
            SvcLogicContext svcLogic = rc.getSvcLogicContext();
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
            ChefResponse chefResponse = chefApiClient.post(chefAction, pushRequest);

            code = chefResponse.getStatusCode();
            String message = chefResponse.getBody();
            if (code == 201) {
                int startIndex = message.indexOf("jobs") + 6;
                int endIndex = message.length() - 2;
                String jobID = message.substring(startIndex, endIndex);
                svcLogic.setAttribute("jobID", jobID);
                logger.info(jobID);
            }
            chefServerResult(rc, code, message);
        } catch (Exception e) {
            code = 401;
            logger.error("An error occurred when executing pushJob method", e);
            doFailure(ctx, code, e.getMessage());
        }
    }

    @SuppressWarnings("static-method")
    private void chefServerResult(RequestContext rc, int code, String message) {
        initSvcLogic(rc, code, message, "server");
    }

    @SuppressWarnings("static-method")
    private void chefClientResult(RequestContext rc, int code, String message) {
        initSvcLogic(rc, code, message, "client");
    }

    private void initSvcLogic(RequestContext rc, int code, String message, String target) {

        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String codeStr = "server".equals(target) ? CHEF_SERVER_RESULT_CODE_STR : CHEF_CLIENT_RESULT_CODE_STR;
        String messageStr = "client".equals(target) ? CHEF_CLIENT_RESULT_MSG_STR : CHEF_SERVER_RESULT_MSG_STR;

        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute(codeStr, Integer.toString(code));
        svcLogic.setAttribute(messageStr, message);
        logger.info(codeStr + ": " + svcLogic.getAttribute(codeStr));
        logger.info(messageStr + ": " + svcLogic.getAttribute(messageStr));
    }

    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {

        String cutMessage = message.contains("\n") ? message.substring(message.indexOf('\n')) : message;

        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, Integer.toString(code));
        svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, cutMessage);

        throw new SvcLogicException("Chef Adapter error:" + cutMessage);
    }
}
