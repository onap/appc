/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.adapter.netconf;

import org.openecomp.appc.adapter.netconf.internal.NetconfDataAccessServiceImpl;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;
import org.openecomp.appc.i18n.Msg;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class AppcNetconfAdapterActivator implements BundleActivator {

    /**
     * The bundle registration
     */
    private ServiceRegistration registration = null;
    private ServiceRegistration reporterRegistration = null;
    private ServiceRegistration factoryRegistration = null;
    private ServiceRegistration dbRegistration = null;

    /**
     * The reference to the actual implementation object that implements the services
     */
    private NetconfClientFactory clientFactory;
    private NetconfDataAccessService DAService;

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AppcNetconfAdapterActivator.class);

    /**
     * Called when this bundle is started so the Framework can perform the bundle-specific activities necessary to start
     * this bundle. This method can be used to register services or to allocate any resources that this bundle needs.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * </p>
     *
     * @param context
     *            The execution context of the bundle being started.
     * @throws java.lang.Exception
     *             If this method throws an exception, this bundle is marked as stopped and the Framework will remove
     *             this bundle's listeners, unregister all services registered by this bundle, and release all services
     *             used by this bundle.
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {

        if (registration == null) {
            clientFactory = new NetconfClientFactory();
            factoryRegistration = context.registerService(NetconfClientFactory.class, clientFactory, null);

            DAService = new NetconfDataAccessServiceImpl();
            //set dblib service
            DbLibService dblibSvc = null;
            ServiceReference sref = context.getServiceReference(DbLibService.class.getName());
            dblibSvc  = (DbLibService)context.getService(sref);
            DAService.setDbLibService(dblibSvc);
            ///////////////////////////////////
            factoryRegistration = context.registerService(NetconfDataAccessService.class, DAService, null);
        }

        logger.info(Msg.COMPONENT_INITIALIZED, "NETCONF adapter");
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the bundle-specific activities necessary to stop
     * the bundle. In general, this method should undo the work that the BundleActivator.start method started. There
     * should be no active threads that were started by this bundle when this bundle returns. A stopped bundle must not
     * call any Framework objects.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * </p>
     *
     * @param context
     *            The execution context of the bundle being stopped.
     * @throws java.lang.Exception
     *             If this method throws an exception, the bundle is still marked as stopped, and the Framework will
     *             remove the bundle's listeners, unregister all services registered by the bundle, and release all
     *             services used by the bundle. *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        if (reporterRegistration != null) {
            reporterRegistration.unregister();
            reporterRegistration = null;
        }

        if (null != factoryRegistration) {
            factoryRegistration.unregister();
            factoryRegistration = null;
        }

        if (dbRegistration != null) {
            dbRegistration.unregister();
            dbRegistration = null;
        }
    }

    public String getName() {
        return "APPC NETCONF adapter";
    }

}
