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

package org.onap.appc.provider;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.util.StringHelper;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

public class AppcProviderClient {

    private final EELFLogger LOG = EELFManager.getInstance().getApplicationLogger();
    private final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    private SvcLogicService svcLogic = null;

    public AppcProviderClient() {
        BundleContext bctx = FrameworkUtil.getBundle(SvcLogicService.class).getBundleContext();
        // Handle BundleContext returning null
        if (bctx == null){
            LOG.warn("Cannot find bundle context for " + SvcLogicService.NAME);
        } else {
            // Get SvcLogicService reference
            ServiceReference sref = bctx.getServiceReference(SvcLogicService.NAME);
            if (sref != null) {
                svcLogic = (SvcLogicService) bctx.getService(sref);

            } else {
                LOG.warn("Cannot find service reference for " + SvcLogicService.NAME);

            }
        }
    }

    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        LOG.debug(String.format("Checking for graph. %s %s %s %s", module, rpc, version, mode));
        return (svcLogic.hasGraph(module, rpc, version, mode));
    }

    public Properties execute(String module, String rpc, String version, String mode, Properties parms)
        throws SvcLogicException {

        /*
         * Set End time for Metrics Logger
         */
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String durationStr = String.valueOf(duration);
        String endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);
        MDC.put("TargetEntity", "sli");
        MDC.put("TargetServiceName", "execute");
        MDC.put("ClassName", "org.onap.appc.provider.AppcProviderClient");

        LOG.debug("Parameters passed to SLI: " + StringHelper.propertiesToString(parms));
        metricsLogger.info("Parameters passed to SLI: " + StringHelper.propertiesToString(parms));

        Properties respProps = svcLogic.execute(module, rpc, version, mode, parms);

        /*
         * Set End time for Metrics Logger
         */
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        durationStr = String.valueOf(duration);
        endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);

        LOG.debug("Parameters returned by SLI: " + StringHelper.propertiesToString(respProps));
        metricsLogger.info("Parameters returned by SLI: " + StringHelper.propertiesToString(respProps));

        return respProps;
    }
}
