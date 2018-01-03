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

package org.onap.appc.dg.dependencymanager.impl;

import org.onap.appc.metadata.MetadataService;
import org.onap.appc.metadata.objects.DependencyModelIdentifier;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.onap.appc.dg.dependencymanager.DependencyType;
import org.onap.appc.dg.dependencymanager.exception.DependencyModelNotFound;
import org.onap.appc.dg.dependencymanager.helper.DependencyModelParser;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class ResourceDependency implements DependencyType{

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ResourceDependency.class);


    private MetadataService metadataService;

    public ResourceDependency(){
        getMetadataServiceRef();
    }

    private void getMetadataServiceRef() {
        BundleContext bctx = FrameworkUtil.getBundle(MetadataService.class).getBundleContext();
        // Get MetadataService reference
        ServiceReference sref = bctx.getServiceReference(MetadataService.class.getName());
        if (sref != null) {
            logger.info("MetadataService from bundlecontext");
            metadataService = (MetadataService) bctx.getService(sref);

        } else {
            logger.info("MetadataService error from bundlecontext");
            logger.warn("Cannot find service reference for org.onap.appc.metadata.MetadataService");
        }
    }

    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public VnfcDependencyModel getVnfcDependencyModel(DependencyModelIdentifier modelIdentifier) throws InvalidDependencyModelException, DependencyModelNotFound {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getVnfcDependencyModel with DependencyModelIdentifier = "+ modelIdentifier);
        }
        String vnfModel = metadataService.getVnfModel(modelIdentifier);
        if(vnfModel ==null){
            logger.debug("Vnf model not found from metadata service");
            throw new DependencyModelNotFound("Invalid or Empty VNF Model");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Building dependency model for Vnf Model : " + vnfModel);
        }
        DependencyModelParser modelParser = new DependencyModelParser();
        return modelParser.generateDependencyModel(vnfModel,modelIdentifier.getVnfType());
    }
}
