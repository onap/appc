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

package org.onap.appc.workflow.impl;

import java.util.concurrent.locks.ReentrantLock;

import org.onap.appc.rankingframework.RankedAttributesContext;
import org.onap.appc.rankingframework.RankedAttributesResolver;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

class WorkflowResolver {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(WorkFlowManagerImpl.class);

    private long interval;

    private volatile long lastUpdate = 0L;
    private volatile boolean isUpdateInProgress = false;
    private volatile RankedAttributesResolver<WorkflowKey> dgResolver;

    private final ReentrantLock INIT_LOCK = new ReentrantLock();

    WorkflowResolver(int interval) {
        this.interval = interval * 1000l;
    }

    private RankedAttributesResolver<WorkflowKey> createResolver() {
        WorkflowResolverDataReader reader = new WorkflowResolverDataReader();
        return reader.read();
    }

    private boolean isExpired() {
        return (System.currentTimeMillis() - lastUpdate) > interval;
    }

    private RankedAttributesResolver<WorkflowKey> resolver() {

        /*
         * In general case, the method implementation is non-blocking. The first
         * thread that identifies data expiration will be used to refresh it. In
         * meanwhile, any other thread will get the old instance without waiting
         * for the updated one. The only exception is the very first time when
         * previous instance doesn't exist - in such a cases all the threads
         * will be waiting on INIT_LOCK while one of them initializes the
         * resolver instance. NOTE: The initialization is intentionally
         * implemented in lazy manner to make sure the bundle is initialized
         * properly on startup regardless whether or not the data is correct.
         * Afterwards, the resolver may be instantiated as many times as needed.
         */

        try {

            if (dgResolver == null) {
                INIT_LOCK.lock();
                if (dgResolver != null) {
                    INIT_LOCK.unlock();
                }
            }

            if (!isUpdateInProgress && isExpired()) {

                boolean doUpgrade = false;

                synchronized (this) {
                    if (!isUpdateInProgress) {
                        isUpdateInProgress = true;
                        doUpgrade = true;
                    }
                }

                if (doUpgrade) {

                    logger.info("DG resolver configuration data has expired - initiating refresh");

                    try {
                        dgResolver = createResolver();
                        lastUpdate = System.currentTimeMillis();

                        logger.info("DG resolver configuration data has been refreshed successfully");
                    } finally {
                        isUpdateInProgress = false;
                    }
                }
            }
        } finally {
            if (INIT_LOCK.isHeldByCurrentThread()) {
                INIT_LOCK.unlock();
            }
        }

        return dgResolver;
    }

    WorkflowKey resolve(final String action, final String vnfType, final String vnfVersion,final String apiVersion) {

        RankedAttributesContext context = new RankedAttributesContext() {
            @Override
            public Object getAttributeValue(String name) {
                switch (name) {
                    case "action":
                        return action;
                    case "api_version":
                        return apiVersion;
                    case "vnf_type":
                        return vnfType;
                    case "vnf_version":
                        return vnfVersion;
                    default:
                        throw new IllegalStateException(name);
                }
            }
        };

        return resolver().resolve(context);
    }
}
