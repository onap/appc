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

import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.ProviderAdapterImpl;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Hypervisor;
import com.att.cdp.zones.model.Hypervisor.Status;
import com.att.cdp.zones.model.Hypervisor.State;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.MDC;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;

public class EvacuateServer extends ProviderServerOperation {

    private static final String EVACUATE_STATUS = "EVACUATE_STATUS";
    private static final String EVACUATE_SERVER = "Evacuate Server";

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    private static final Configuration configuration = ConfigurationFactory.getConfiguration();
    private ProviderAdapterImpl paImpl = null;

    private void evacuateServer(RequestContext rc, @SuppressWarnings("unused") Server server, String targetHost)
            throws ZoneException, RequestFailedException {

        Context ctx = server.getContext();
        Provider provider = ctx.getProvider();
        ComputeService service = ctx.getComputeService();

        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        try {
            if (server.getStatus().equals(Server.Status.PENDING)) {
                waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                        Server.Status.SUSPENDED, Server.Status.PAUSED);
            }
        } catch (RequestFailedException e) {
            // evacuate is a special case. If the server is still in a Pending state, we want to
            // continue with evacuate
            logger.info("Evacuate server - ignore RequestFailedException from waitForStateChange() ..." ,e);
        }

        setTimeForMetricsLogger();

        String msg;
        try {
            while (rc.attempt()) {
                try {
                    logger.debug("Calling CDP moveServer - server id = " + server.getId());
                    service.moveServer(server.getId(), targetHost);
                    // Wait for completion, expecting the server to go to a non pending state
                    waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                            Server.Status.SUSPENDED, Server.Status.PAUSED);
                    break;
                } catch (ContextConnectionException e) {
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                            ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
                            Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                            Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    metricsLogger.error(msg, e);
                    rc.delay();
                }
            }

        } catch (ZoneException e) {
            msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    e.getMessage());
            logger.error(msg, e);
            metricsLogger.error(msg);
            throw new RequestFailedException(EVACUATE_SERVER, msg, HttpStatus.BAD_GATEWAY_502, server);
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException(EVACUATE_SERVER, msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }


    /**
     * @see org.onap.appc.adapter.iaas.ProviderAdapter#evacuateServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    private Server evacuateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        setTimeForMetricsLogger();

        String msg;
        ctx.setAttribute(EVACUATE_STATUS, "ERROR");
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            VMURL vm = VMURL.parseURL(vmUrl);

            if (validateVM(rc, appName, vmUrl, vm)) {
                return null;
            }

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            // retrieve the optional parameters
            String rebuildVm = params.get(ProviderAdapter.PROPERTY_REBUILD_VM);
            String targetHostId = params.get(ProviderAdapter.PROPERTY_TARGETHOST_ID);

            Context context;
            String tenantName = "Unknown";//to be used also in case of exception
            try {
                context = getContext(rc, vmUrl, identStr);
                if (context != null) {
                    tenantName = context.getTenantName();//this varaible also is used in case of exception
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vmUrl, tenantName, server.getStatus().toString());

                    // check target host status
                    checkHostStatus(server, targetHostId, context);

                    // save hypervisor name before evacuate
                    String hypervisor = server.getHypervisor().getHostName();

                    evacuateServer(rc, server, targetHostId);

                    server.refreshAll();
                    String hypervisorAfterEvacuate = server.getHypervisor().getHostName();
                    logger.debug("Hostname before evacuate: " + hypervisor + ", After evacuate: " + hypervisorAfterEvacuate);

                    // check hypervisor host name after evacuate. If it is unchanged, the evacuate
                    // failed.
                    checkHypervisor(server, hypervisor, hypervisorAfterEvacuate);

                    // check VM status after evacuate
                    checkStatus(server);
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute(EVACUATE_STATUS, "SUCCESS");

                    // If a snapshot exists, do a rebuild to apply the latest snapshot to the evacuated server.
                    // This is the default behavior unless the optional parameter is set to FALSE.
                    if (rebuildVm == null || !"false".equalsIgnoreCase(rebuildVm)) {
                        List<Image> snapshots = server.getSnapshots();
                        if (snapshots == null || snapshots.isEmpty()) {
                            logger.debug("No snapshots available - skipping rebuild after evacuate");
                        } else if (paImpl != null) {
                            logger.debug("Executing a rebuild after evacuate");
                            paImpl.rebuildServer(params, ctx);
                            // Check error code for rebuild errors. Evacuate had set it to 200 after
                            // a successful evacuate. Rebuild updates the error code.
                            String rebuildErrorCode = ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_ERROR_CODE);
                            if (rebuildErrorCode != null) {
                                try {
                                    int errorCode = Integer.parseInt(rebuildErrorCode);
                                    if (errorCode != HttpStatus.OK_200.getStatusCode()) {
                                        logger.debug("Rebuild after evacuate failed - error code=" + errorCode
                                                + ", message=" + ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_ERROR_MESSAGE));
                                        msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_REBUILD_FAILED,
                                                server.getName(), hypervisor, hypervisorAfterEvacuate,
                                                ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_ERROR_MESSAGE));
                                        logger.error(msg);
                                        metricsLogger.error(msg);
                                        ctx.setAttribute(EVACUATE_STATUS, "ERROR");
                                        // update error message while keeping the error code the
                                        // same as before
                                        doFailure(rc, HttpStatus.getHttpStatus(errorCode), msg);
                                    }
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                        }
                    }

                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (RequestFailedException e) {
                logger.error("Request failed", e);
                doFailure(rc, e.getStatus(), e.getMessage());
            } catch (IOException | ZoneException e1) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                        Operation.EVACUATE_SERVICE.toString(), vmUrl, tenantName);
                logger.error(msg, e1);
                metricsLogger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, "n/a", "n/a", e.getMessage());
            logger.error(msg, e);
            metricsLogger.error(msg);
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    private void checkHostStatus(Server server, String targetHostId, Context context)
        throws ZoneException, RequestFailedException {
        if (isComputeNodeDown(context, targetHostId)) {
            String msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    "Target host " + targetHostId + " status is not UP/ENABLED");
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException(EVACUATE_SERVER, msg, HttpStatus.BAD_REQUEST_400, server);
        }
    }

    private void checkHypervisor(Server server, String hypervisor, String hypervisorAfterEvacuate)
        throws RequestFailedException {
        if (hypervisor != null && hypervisor.equals(hypervisorAfterEvacuate)) {
            String msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    "Hypervisor host " + hypervisor
                            + " after evacuate is the same as before evacuate. Provider (ex. Openstack) recovery actions may be needed.");
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException(EVACUATE_SERVER, msg, HttpStatus.INTERNAL_SERVER_ERROR_500, server);
        }
    }

    private void checkStatus(Server server) throws RequestFailedException {
        if (server.getStatus() == Server.Status.ERROR) {
            String msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    "VM is in ERROR state after evacuate. Provider (ex. Openstack) recovery actions may be needed.");
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException(EVACUATE_SERVER, msg, HttpStatus.INTERNAL_SERVER_ERROR_500, server);
        }
    }

    /**
     * Check if a Compute node is down.
     * 
     * This method attempts to find a given host in the list of hypervisors for a given context. The only case where a
     * node is considered down is if a matching hypervisor is found and it's state and status are not UP/ENABLED.
     * 
     * @param context The current context
     * 
     * @param host The host name (short or fully qualified) of a compute node
     * 
     * @return true if the node is determined as down, false for all other cases
     */
    private boolean isComputeNodeDown(Context context, String host) throws ZoneException {
        ComputeService service = context.getComputeService();
        boolean nodeDown = false;

        if (host != null && ! host.isEmpty()) {
            List<Hypervisor> hypervisors = service.getHypervisors();
            logger.debug("List of Hypervisors retrieved: " + Arrays.toString(hypervisors.toArray()));
            for (Hypervisor hv : hypervisors) {
                nodeDown = isNodeDown(host, nodeDown, hv);
            }
        }
        return nodeDown;
    }

    private boolean isNodeDown(String host, boolean nodeDown, Hypervisor hv) {
        if (isHostMatchesHypervisor(host, hv)) {
            State hstate = hv.getState();
            Status hstatus = hv.getStatus();
            logger.debug("Host matching hypervisor: " + hv.getHostName() + ", State/Status: " + hstate.toString()
                    + "/" + hstatus.toString());
            if (hstate != State.UP || hstatus != Status.ENABLED) {
                return true;
            }
        }
        return nodeDown;
    }

    private boolean isHostMatchesHypervisor(String host, Hypervisor hypervisor) {
        return hypervisor.getHostName().startsWith(host);
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.EVACUATE_SERVICE.toString(), "App-C IaaS Adapter:Evacuate", ADAPTER_NAME);
        logOperation(Msg.EVACUATING_SERVER, params, context);

        setTimeForMetricsLogger();

        metricsLogger.info("Executing Provider Operation: Evacuate");
        return evacuateServer(params, context);
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
        MDC.put("TargetServiceName", "evacuate server");
        MDC.put("ClassName", "org.onap.appc.adapter.iaas.provider.operation.impl.EvacuateServer");
    }

    public void setProvideAdapterRef(ProviderAdapterImpl pai) {
        paImpl = pai;
    }
}