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

package org.onap.appc.dg.common.impl;

import org.onap.appc.Constants;
import org.onap.appc.dg.common.VNFConfigurator;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.exception.MDSALStoreException;
import org.onap.appc.mdsal.impl.MDSALStoreFactory;
import org.onap.appc.mdsal.objects.BundleInfo;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class VNFConfiguratorImpl implements VNFConfigurator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(VNFConfiguratorImpl.class);
    private static final String STATUS = "STATUS";
    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    @Override
    public void storeConfig(Map<String, String> params, SvcLogicContext context) throws APPCException {
        String uniqueId = params.get("uniqueId");
        String yang = params.get("yang");
        String configJSON = params.get("configJSON");
        String requestId = params.get("requestId");
        String prefix = params.get("prefix");
        prefix = StringUtils.isEmpty(prefix)? "":prefix+".";

        MDSALStore store = MDSALStoreFactory.createMDSALStore();

        logger.debug("Inputs Received : uniqueId = " + uniqueId +
                                        " , yang = " + yang +
                                        " , configJSON = " + configJSON +
                                        " , requestId = " + requestId +
                                        " , prefix = " +prefix);

        try {

            if(StringUtils.isEmpty(uniqueId)
                    ||StringUtils.isEmpty(yang)
                    || StringUtils.isEmpty(configJSON)
                    || StringUtils.isEmpty(requestId)){
                throw new APPCException("One or more input parameters are empty : uniqueId = " + uniqueId + " " +
                        ", yang = " + yang +
                        " , configJSON = " + configJSON +
                        " , requestId = " + requestId);
            }

            Date revision = new SimpleDateFormat(Constants.YANG_REVISION_FORMAT).parse(Constants.YANG_REVISION);

            boolean isYangAlreadyLoaded = store.isModulePresent(uniqueId,revision);

            if(!isYangAlreadyLoaded){
                BundleInfo bundleInfo = getBundleInfo(uniqueId);
                store.storeYangModule(yang,bundleInfo);
            }
            store.storeJson(uniqueId, requestId , configJSON);
            context.setAttribute(prefix + STATUS, SUCCESS);
        } catch (ParseException e) {
            String errorMessage ="Error parsing the date : " + Constants.YANG_REVISION + " into format " + Constants.YANG_REVISION_FORMAT;
            logger.error(errorMessage,e);
            context.setAttribute(prefix + STATUS, FAILURE);
            context.setAttribute(prefix + ERROR_MESSAGE, errorMessage);
            throw new APPCException(e.getMessage());
        }catch (MDSALStoreException e){
            String errorMessage = "Error while adding yang to MD-SAL store." + e.getMessage();
            logger.error(errorMessage,e);
            context.setAttribute(prefix + STATUS,FAILURE);
            context.setAttribute(prefix + ERROR_MESSAGE, errorMessage);
            throw new APPCException(e.getMessage());
        }

    }

    private BundleInfo getBundleInfo(String uniqueId) {
        BundleInfo bundleInfo = new BundleInfo();
        bundleInfo.setDescription(uniqueId);
        bundleInfo.setName(uniqueId);
        bundleInfo.setLocation(uniqueId);
        return bundleInfo;
    }
}
