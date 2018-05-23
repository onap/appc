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

package org.onap.appc.adapter.ansible;

import org.onap.appc.Constants;
import org.onap.appc.adapter.ansible.impl.AnsibleAdapterImpl;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.i18n.Msg;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This activator is used to initialize and terminate an instance of AnsibleAdapter class
 */
public class AnsibleActivator implements BundleActivator {

    private static final String ANSIBLE_ADAPTER = "Ansible Adapter";
    private static final String BUNDLE_NAME = "APPC Ansible Adapter";
    /**
     * The bundle registration
     */
    private ServiceRegistration registration = null;

    /**
     * The reference to the actual implementation object that implements the services
     */
    private AnsibleAdapter adapter;

    /**
     * The logger to be used
     */
    private final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleActivator.class);

    /**
     * The configuration object used to configure this bundle
     */
    private final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * Called when this bundle is started so the Framework can perform the bundle-specific activities necessary to start
     * this bundle. This method can be used to register services or to allocate any resources that this bundle needs.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * </p>
     *
     * @param context The execution context of the bundle being started.
     * @throws java.lang.Exception If this method throws an exception, this bundle is marked as stopped and the
     *         Framework will remove this bundle's listeners, unregister all services registered
     *         by this bundle, and release all services used by this bundle.
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {

        logger.info("Starting bundle " + BUNDLE_NAME);
        String appName = "APPC: ";
        logger.info(Msg.COMPONENT_INITIALIZING, appName, ANSIBLE_ADAPTER);
        adapter = new AnsibleAdapterImpl();

        if (registration == null) {
            logger.info(Msg.REGISTERING_SERVICE, appName, adapter.getAdapterName(),
                    AnsibleAdapter.class.getSimpleName());
            registration = context.registerService(AnsibleAdapter.class, adapter, null);
        }

        logger.info(Msg.COMPONENT_INITIALIZED, appName, ANSIBLE_ADAPTER);
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
     * @param context The execution context of the bundle being stopped.
     * @throws java.lang.Exception If this method throws an exception, the bundle is still marked as stopped, and the
     *         Framework will remove the bundle's listeners, unregister all services registered
     *         by the bundle, and release all services used by the bundle.
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping bundle " + BUNDLE_NAME);

        if (registration != null) {
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            logger.info(Msg.COMPONENT_TERMINATING, appName, ANSIBLE_ADAPTER);
            logger.info(Msg.UNREGISTERING_SERVICE, appName, adapter.getAdapterName());
            registration.unregister();
            registration = null;
            logger.info(Msg.COMPONENT_TERMINATED, appName, ANSIBLE_ADAPTER);
        }
    }
}
