/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider.lcm.service;

import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LicenseManagementInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LicenseManagementOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LicenseManagementOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.Map;
/**
 * Provide LCM command service for LicenseManagement VNF Service
 */
public class LicenseManagementService extends AbstractBaseService {

    /**
     * Constructor
     */
    public LicenseManagementService() {
        super(Action.LicenseManagement);
        logger.debug("LicenseManagementService starts");
    }

    /**
     * Process the LicenseManagement request
     * @param input of LicenseManagementInput from the REST API input
     * @return LicenseManagementOutputBuilder which has the process results
     */
    public LicenseManagementOutputBuilder process(LicenseManagementInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader,actionIdentifiers,payload);
        }

        LicenseManagementOutputBuilder outputBuilder = new LicenseManagementOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }

    /**
     * Validate the input.
     * Set Status if any error occurs.
     *
     * @param input of LicenseManagementInput from the REST API input
     */
    void validate(CommonHeader commonHeader,
            Action action,
            ActionIdentifiers actionIdentifiers,
            Payload payload) {
        status = validateVnfId(commonHeader, action, actionIdentifiers);
        if (status != null) {
            return;
        }

        // validate payload
        String keyName = "payload";
        if (payload == null) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            return;
        }
        String payloadString = payload.getValue();
        status = validateMustHaveParamValue(payloadString == null ? payloadString : payloadString.trim(), "payload");
        if (status != null) {
            return;
        }

        try {
            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
            validateMustHaveParamValue(payloadMap.get(keyName), keyName);
        } catch (IOException e) {
            logger.error(String.format("LicenseManagementService (%s) got IOException when converting payload", rpcName), e);
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
        }
    }

    /**
     * Proceed to action for the LicenseManagement VNF action.
     *
     * @param input of LicenseManagementInput from the REST API input
     */
    void proceedAction(CommonHeader commonHeader,
            ActionIdentifiers actionIdentifiers,
            Payload payload) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(commonHeader, actionIdentifiers, payload,
                this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }
}