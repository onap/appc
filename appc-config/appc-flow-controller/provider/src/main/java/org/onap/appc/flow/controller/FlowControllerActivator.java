/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.flow.controller;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.LinkedList;
import java.util.List;
import org.onap.appc.flow.controller.node.FlowControlNode;
import org.onap.appc.flow.controller.node.JsonParsingNode;
import org.onap.appc.flow.controller.node.RestServiceNode;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class FlowControllerActivator implements BundleActivator {

    private List<ServiceRegistration> registrations = new LinkedList<>();

    private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowControllerActivator.class);

    private static final String REGISTERING = "Registering service ";
    private static final String REGISTERING_OK = REGISTERING + "successful for ";


    @Override
    public void start(BundleContext ctx) {
        try {
            FlowControlNode flowExecutorNode = new FlowControlNode();
            log.debug(REGISTERING + flowExecutorNode.getClass().getName());
            registrations.add(ctx.registerService(flowExecutorNode.getClass().getName(), flowExecutorNode, null));
            log.debug(REGISTERING_OK + flowExecutorNode.getClass().getName());

            RestServiceNode restServiceNode = new RestServiceNode();
            log.debug(REGISTERING + restServiceNode.getClass().getName());
            registrations.add(ctx.registerService(restServiceNode.getClass().getName(), restServiceNode, null));
            log.debug(REGISTERING_OK + restServiceNode.getClass().getName());

            JsonParsingNode jsonParsingNode = new JsonParsingNode();
            log.debug(REGISTERING + jsonParsingNode.getClass().getName());
            registrations.add(ctx.registerService(jsonParsingNode.getClass().getName(), jsonParsingNode, null));
            log.debug(REGISTERING_OK + jsonParsingNode.getClass().getName());

        } catch (Exception e) {
            log.debug("Exeception occured in FlowControllerActivator", e);
        }
    }

    @Override
    public void stop(BundleContext arg0) {
        for (ServiceRegistration registration: registrations) {
            registration.unregister();
        }
    }
}
