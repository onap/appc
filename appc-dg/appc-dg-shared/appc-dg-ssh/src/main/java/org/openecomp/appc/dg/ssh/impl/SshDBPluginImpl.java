/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.ssh.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import org.openecomp.appc.adapter.ssh.Constants;
import org.openecomp.appc.adapter.ssh.SshConnectionDetails;
import org.openecomp.appc.adapter.ssh.SshDataAccessException;
import org.openecomp.appc.adapter.ssh.SshDataAccessService;
import org.openecomp.appc.dg.ssh.SshDBPlugin;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class SshDBPluginImpl implements SshDBPlugin {

    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
    private static ObjectMapper mapper = new ObjectMapper();

    private SshDataAccessService dataAccessService;

    public void setDataAccessService(SshDataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    public void retrieveConnectionDetails(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        SshConnectionDetails connectionDetails = new SshConnectionDetails();
        String vnfType = ctx.getAttribute("aai.prefix")+"."+"vnf-type";
        try {
            if (!dataAccessService.retrieveConnectionDetails(ctx.getAttribute(vnfType), connectionDetails)) {
                logger.error("Missing configuration for " + params.get(vnfType));
                throw new APPCException("Missing configuration for " + params.get(vnfType) + " in " + Constants.DEVICE_AUTHENTICATION_TABLE_NAME);
            }
            connectionDetails.setHost(params.get(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME));
            ctx.setAttribute(Constants.CONNECTION_DETAILS_FIELD_NAME, mapper.writeValueAsString(connectionDetails));
        } catch(APPCException e) {
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw e;
        } catch(SshDataAccessException e) {
            logger.error("Error " + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            logger.error("Error " + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw new APPCException(e);
        }
    }

}
