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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.iaas.provider.operation.impl;

import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import java.io.IOException;
import java.util.Map;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;

public class LookupServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();


    public Server lookupServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive(); // should we test the return and fail if false?

        String vm_url = null;
        try {
            // process vm_url
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) {
                return null;
            }
            server = lookupServerNested(params, server, rc, ctx, appName, vm, vm_url);
        } catch (RequestFailedException e) {
            // parameters not valid, unable to connect to provider
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            ctx.setAttribute("serverFound", "failure");
        }
        return server;
    }

    private Server lookupServerNested(Map<String, String> params, Server server, RequestContext RqstCtx, SvcLogicContext ctx,
            String appName, VMURL vm, String vm_url)
            throws APPCException {

        // use try with resource to ensure context is closed (returned to pool)
        try (Context context = resolveContext(RqstCtx, params, appName, vm_url)) {
            // resloveContext & getContext call doFailure and log errors before returning null
            if (context != null) {
                RqstCtx.reset();
                server = lookupServer(RqstCtx, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                ctx.setAttribute("serverFound", "success");
                String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "LookupServer", vm_url);
                ctx.setAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                doSuccess(RqstCtx);
            }
        } catch (ZoneException e) {
            // server not found
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(RqstCtx, HttpStatus.NOT_FOUND_404, msg);
            ctx.setAttribute("serverFound", "failure");
        } catch (IOException e) {
            // exception closing context
            String msg = EELFResourceManager.format(Msg.CLOSE_CONTEXT_FAILED, e, vm_url);
            logger.error(msg);
        } catch (Exception e1) {
            String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1,
                    e1.getClass().getSimpleName(), Operation.LOOKUP_SERVICE.toString(), vm_url, "Unknown");
            logger.error(msg, e1);
            doFailure(RqstCtx, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.LOOKUP_SERVICE.toString(), "App-C IaaS Adapter:LookupServer", ADAPTER_NAME);
        logOperation(Msg.LOOKING_SERVER_UP, params, context);
        return lookupServer(params, context);
    }
}
