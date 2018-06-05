/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.sdnc.config.audit.node;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class CompareNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(CompareNode.class);

    public void compare(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.debug("Starting Compare Node Analysis");

        Map<String, String> status = new HashMap<>();
        Parameters params = new Parameters(inParams);
        try {
            if (params.getCompareDataType() != null) {
                if (params.getPayloadX() != null && params.getPayloadY() != null) {
                    status = getCompareResults(params);
                    log.debug("Compare Result : " + status);
                } else {
                    status.put(CompareConstants.RESPONSE_STATUS, CompareConstants.STATUS_FAILURE);
                    status.put(CompareConstants.ERROR_CODE, "200");
                    status.put(CompareConstants.ERROR_MESSAGE, "One of the Data Received by CompareNode is Empty");
                }
            } else {
                status.put(CompareConstants.RESPONSE_STATUS, CompareConstants.STATUS_FAILURE);
                status.put(CompareConstants.ERROR_CODE, "200");
                status.put(CompareConstants.ERROR_MESSAGE,
                    "Missing compareDataType value in input request: Expecting at least one of  CLI/RESTCONF/XML");
            }

        } catch (Exception e) {
            status.put(CompareConstants.RESPONSE_STATUS, CompareConstants.STATUS_FAILURE);
            status.put(CompareConstants.ERROR_CODE, "200");
            status.put(CompareConstants.ERROR_MESSAGE, CompareConstants.ERROR_MESSAGE_DETAIL);
            log.debug("Error in Comapre Node Execution", e);
        }
        createContextResponse(status, ctx, params.getRequestIdentifier());
    }

    private HashMap<String, String> getCompareResults(Parameters params) throws Exception {
        HashMap<String, String> resultMap = new HashMap<>();
        boolean cmpResult;
        CompareDataInterface handler;

        if (params.getCompareDataType().equalsIgnoreCase(CompareConstants.FORMAT_JSON)) {
            handler = new CompareJsonData(params.getPayloadX(), params.getPayloadY());
        } else if ((params.getCompareDataType().equalsIgnoreCase(CompareConstants.FORMAT_XML))
            || (params.getCompareDataType().equalsIgnoreCase(CompareConstants.NETCONF_XML))
            || (params.getCompareDataType().equalsIgnoreCase(CompareConstants.RESTCONF_XML))) {
            handler = new CompareXmlData(params.getPayloadX(), params.getPayloadY());
        } else if (params.getCompareDataType().equalsIgnoreCase(CompareConstants.FORMAT_CLI)) {
            handler = new CompareCliData(params.getPayloadX(), params.getPayloadY());
        } else {
            throw new SvcLogicException("Format " + params.getCompareDataType() + " not supported");
        }
        try {
            log.debug("Received Format to compare : " + params.getCompareDataType());

            cmpResult = handler.compare();
            if (cmpResult) {
                resultMap.put(CompareConstants.RESPONSE_STATUS, CompareConstants.STATUS_SUCCESS);

            } else {
                resultMap.put(CompareConstants.RESPONSE_STATUS, CompareConstants.STATUS_FAILURE);
                resultMap.put(CompareConstants.ERROR_CODE, "500");
                resultMap.put(CompareConstants.ERROR_MESSAGE, CompareConstants.NO_MATCH_MESSAGE);
            }
        } catch (Exception e) {
            throw e;
        }
        return resultMap;
    }

    private void createContextResponse(Map<String, String> status, SvcLogicContext ctx, String requestIdentifier) {
        String requestId = requestIdentifier == null ? "" : requestIdentifier + ".";

        ctx.setAttribute(requestId.concat(CompareConstants.RESPONSE_STATUS),
            status.get(CompareConstants.RESPONSE_STATUS));
        ctx.setAttribute(requestId.concat(CompareConstants.ERROR_CODE),
            status.get(CompareConstants.ERROR_CODE));
        ctx.setAttribute(requestId.concat(CompareConstants.ERROR_MESSAGE),
            status.get(CompareConstants.ERROR_MESSAGE));
    }
}
