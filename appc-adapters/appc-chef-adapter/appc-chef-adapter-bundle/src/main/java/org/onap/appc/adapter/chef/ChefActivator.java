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

package org.onap.appc.adapter.chef;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.Constants;
import org.onap.appc.adapter.chef.impl.ChefAdapterFactory;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.i18n.Msg;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This activator is used to initialize and terminate the connection pool to one or more providers.
 * <p>
 * The CDP abstraction layer supports multiple types of providers, with each provider supporting multiple tenants. The
 * "connection" to a specific tenant on a specific provider is represented by a "context" object. These context objects
 * are authenticated to a specific tenant on the provider, but can be reused from one request to another. Contexts are
 * slow to set up and are resource intensive, so they are cached. However, the contexts for a specific tenant on a
 * specific provider must be cached separately.
 * </p>
 * <p>
 * Activation of the bundle creates an empty cache which is organized first by provider type, then by tenant name, with
 * the contents being an empty pool of contexts for that provider/tenant combination. The pool is created on first use,
 * and retained for as long as the bundle is active.
 * </p>
 * <p>
 * When the bundle is deactivated, the cache is torn down with all contexts being closed.
 * </p>
 */
public class ChefActivator implements BundleActivator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefActivator.class);
    private static final String NAME = "APPC Chef Adapter";
    private static final String ADAPTER_NAME = "CHEF adapter";

    /**
     * The bundle registration
     */
    private ServiceRegistration registration = null;

    /**
     * The reference to the actual implementation object that implements the services
     */
    private ChefAdapter adapter;

    /**
     * The configuration object used to configure this bundle
     */
    private Configuration configuration = ConfigurationFactory.getConfiguration();


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
        logger.info("Starting bundle " + NAME);

        String appName = readApplicationNameFromProperties();
        logger.info(Msg.COMPONENT_INITIALIZING, appName, ADAPTER_NAME);
        adapter = new ChefAdapterFactory().create();
        if (registration == null) {
            logger.info(Msg.REGISTERING_SERVICE, appName, ADAPTER_NAME,
                ChefAdapter.class.getSimpleName());
            registration = context.registerService(ChefAdapter.class, adapter, null);
        }

        logger.info(Msg.COMPONENT_INITIALIZED, appName, ADAPTER_NAME);
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
        logger.info("Stopping bundle " + NAME);

        if (registration != null) {
            String appName = readApplicationNameFromProperties();
            logger.info(Msg.COMPONENT_TERMINATING, appName, ADAPTER_NAME);
            logger.info(Msg.UNREGISTERING_SERVICE, appName, ADAPTER_NAME);
            registration.unregister();
            registration = null;
            logger.info(Msg.COMPONENT_TERMINATED, appName, ADAPTER_NAME);
        }
    }

    private String readApplicationNameFromProperties() {
        return configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
    }
}
