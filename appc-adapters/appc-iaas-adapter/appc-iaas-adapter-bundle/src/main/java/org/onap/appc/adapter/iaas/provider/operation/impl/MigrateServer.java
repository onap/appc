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

package org.onap.appc.adapter.iaas.provider.operation.impl;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import com.att.cdp.exceptions.ContextConnectionException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.constants.Property;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.slf4j.MDC;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import static org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation.MIGRATE_SERVICE;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;

public class MigrateServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * A list of valid initial VM statuses for a migrate operations
     */
    private final Collection<Server.Status> migratableStatuses = Arrays.asList(Server.Status.READY,
            Server.Status.RUNNING, Server.Status.SUSPENDED);

    private String getConnectionExceptionMessage(RequestContext rc, Context ctx, ContextConnectionException e)
            throws ZoneException {
        return EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, ctx.getProvider().getName(),
                ctx.getComputeService().getURL(), ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
                Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                Integer.toString(rc.getRetryLimit()));
    }

    private void migrateServer(RequestContext rc, Server server, SvcLogicContext svcCtx)
            throws ZoneException, RequestFailedException, APPCException {
        String msg;
        Context ctx = server.getContext();
        ComputeService service = ctx.getComputeService();
        // Init status will equal final status
        Server.Status initialStatus = server.getStatus();
        if (initialStatus == null) {
            throw new ZoneException("Failed to determine server's starting status");
        }
        // We can only migrate certain statuses
        if (!migratableStatuses.contains(initialStatus)) {
            throw new ZoneException(String.format("Cannot migrate server that is in %s state. Must be in one of [%s]",
                    initialStatus, migratableStatuses));
        }
        setTimeForMetricsLogger();
        // Is the skip Hypervisor check attribute populated?
        String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
        if (skipHypervisorCheck == null && svcCtx != null) {
            skipHypervisorCheck = svcCtx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
        }
        // Always perform Hypervisor check
        // unless the skip is set to true
        if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {
            // Check of the Hypervisor for the VM Server is UP and reachable
            checkHypervisor(server);
        }
        boolean inConfirmPhase = false;
        try {
            while (rc.attempt()) {
                try {
                    if (!inConfirmPhase) {
                        // Initial migrate request
                        service.migrateServer(server.getId());
                        // Wait for change to verify resize
                        waitForStateChange(rc, server, Server.Status.READY);
                        inConfirmPhase = true;
                    }
                    // Verify resize
                    if (server.getStatus().equals(Server.Status.ERROR)) {
                        msg = "Cannot Perform 'processResize' in  vm_state " + Server.Status.ERROR;
                        logger.info(msg);
                        msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, service.getURL());
                        logger.error(msg);
                        throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.CONFLICT_409,
                                server);
                    } else {
                        service.processResize(server);
                        // Wait for complete. will go back to init status
                        waitForStateChange(rc, server, initialStatus);
                        logger.info("Completed migrate request successfully");
                        metricsLogger.info("Completed migrate request successfully");

                        break;
                    }
                } catch (ContextConnectionException e) {
                    msg = getConnectionExceptionMessage(rc, ctx, e);
                    logger.error(msg, e);
                    if (server.getStatus().equals(Server.Status.ERROR)) {
                        doFailure(rc, HttpStatus.EXPECTATION_FAILED_417, e.getMessage());
                        throw new RequestFailedException("Migrate Server", msg, HttpStatus.CONFLICT_409, server);
                    } else {
                        rc.delay();
                    }
                }
            }

        } catch (ZoneException e) {
            String phase = inConfirmPhase ? "VERIFY MIGRATE" : "REQUEST MIGRATE";
            msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, server.getName(), server.getId(), phase,
                    e.getMessage());
            generateEvent(rc, false, msg);
            logger.error(msg, e);
            doFailure(rc, HttpStatus.EXPECTATION_FAILED_417, e.getMessage());
            throw new RequestFailedException("Migrate Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        } catch (Exception ex) {
            String phase = inConfirmPhase ? "VERIFY MIGRATE" : "REQUEST MIGRATE";
            msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, server.getName(), server.getId(), phase,
                    ex.getMessage());
            generateEvent(rc, false, msg);
            logger.error(msg, ex);
            doFailure(rc, HttpStatus.EXPECTATION_FAILED_417, ex.getMessage());
        }
    

    }

    /**
     * @see org.onap.appc.adapter.iaas.ProviderAdapter#migrateServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    private Server migrateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        setTimeForMetricsLogger();
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            server = conductServerMigration(rc, vm_url, identStr, ctx);
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.MIGRATE_SERVICE.toString(), "App-C IaaS Adapter:Migrate", ADAPTER_NAME);
        logOperation(Msg.MIGRATING_SERVER, params, context);
        setTimeForMetricsLogger();
        metricsLogger.info("Executing Provider Operation: Migrate");
        return migrateServer(params, context);
    }

    private void setTimeForMetricsLogger() {
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
        MDC.put("TargetEntity", "cdp");
        MDC.put("TargetServiceName", "migrate server");
        MDC.put("ClassName", "org.onap.appc.adapter.iaas.provider.operation.impl.MigrateServer");
    }

    private Server conductServerMigration(RequestContext rc, String vm_url, String identStr, SvcLogicContext ctx)
            throws RequestFailedException,APPCException  {
        String msg;
        Context context = getContext(rc, vm_url, identStr);
        VMURL vm = VMURL.parseURL(vm_url);
        Server server = null;
        try {
            if (context != null) {
                server = lookupServer(rc, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                migrateServer(rc, server, ctx);
                server.refreshStatus();
                context.close();
                doSuccess(rc);
            }
        } catch (IOException | ZoneException e1) {
            msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                    MIGRATE_SERVICE.toString(), vm_url, context.getTenantName());
            logger.error(msg, e1);
            metricsLogger.error(msg);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }
}
