/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.common.impl;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.dg.common.IntermediateMessageSender;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.srvcomm.messaging.MessagingConnector;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


public class IntermediateMessageSenderImpl implements IntermediateMessageSender {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(IntermediateMessageSenderImpl.class);

    private static final String PARAM_MESSAGE = "message";
    private static final String ATTR_REQUEST_ID = "input.common-header.request-id";
    private static final String PROPERTIES_PREFIX = "appc.LCM";

    private MessagingConnector messageService;

    private static final String STATUS = "STATUS";
    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private static final String RESPONSE = "response";
    private static final String MSO = "MSO";

    public void init() {
        messageService = new MessagingConnector();
    }
    public void init(MessagingConnector messagingConnector) {
        messageService = messagingConnector;
    }

    @Override
    public void sendMessage(Map<String, String> params, SvcLogicContext context) {
        String prefix = params.get("prefix");
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
        try {
            validateInputs(params, context);
            String jsonMessage = getJsonMessage(params, context);
            logger.debug("Constructed JSON Message: " + jsonMessage);
            messageService.publishMessage(PROPERTIES_PREFIX, "", jsonMessage);
            context.setAttribute(prefix + STATUS, SUCCESS);
        } catch (Exception e) {
            String errorMessage = "Error sending intermediate message to initiator " + e.getMessage();
            context.setAttribute(prefix + STATUS, FAILURE);
            context.setAttribute(prefix + ERROR_MESSAGE, errorMessage);
            logger.error(errorMessage, e);
        }
    }

    private void validateInputs(Map<String, String> params, SvcLogicContext context) throws APPCException {
        String code = params.get("code");
        String message = params.get(PARAM_MESSAGE);
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(message)) {
            throw new APPCException("code or message is empty");
        }
        String requestId = context.getAttribute(ATTR_REQUEST_ID);
        if (StringUtils.isEmpty(requestId)) {
            throw new APPCException("requestId is empty");
        }
    }

    private String getJsonMessage(Map<String, String> params, SvcLogicContext context) {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode commonHeader = getCommonHeader(context);
        ObjectNode status = getStatus(params);

        ObjectNode output = objectMapper.createObjectNode();
        output.put("common-header", commonHeader);
        output.put("status", status);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("output", output);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", RESPONSE);
        root.put("rpc-name", context.getAttribute("input.action"));
        root.put("cambria.partition", MSO);
        root.put("correlation-id", getCorrelationId(context));
        root.put("body", body);

        return root.toString();
    }

    private String getCorrelationId(SvcLogicContext context) {
        String requestId = context.getAttribute(ATTR_REQUEST_ID);
        String subRequestId = context.getAttribute("input.common-header.sub-request-id");
        return requestId + (StringUtils.isEmpty(subRequestId) ? "" : ("-" + subRequestId));
    }

    private ObjectNode getStatus(Map<String, String> params) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode status = objectMapper.createObjectNode();
        status.put("code", params.get("code"));
        status.put(PARAM_MESSAGE, params.get(PARAM_MESSAGE));
        return status;
    }

    private ObjectNode getCommonHeader(SvcLogicContext context) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode commonHeader = objectMapper.createObjectNode();
        commonHeader.put("api-ver", context.getAttribute("input.common-header.api-ver"));
        commonHeader.put("timestamp", context.getAttribute("input.common-header.timestamp"));
        commonHeader.put("originator-id", context.getAttribute("input.common-header.originator-id"));
        commonHeader.put("request-id", context.getAttribute(ATTR_REQUEST_ID));
        commonHeader.put("sub-request-id", context.getAttribute("input.common-header.sub-request-id"));
        return commonHeader;
    }
}
