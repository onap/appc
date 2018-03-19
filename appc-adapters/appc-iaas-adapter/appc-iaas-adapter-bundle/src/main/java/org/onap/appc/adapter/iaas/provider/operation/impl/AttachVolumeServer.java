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

import static org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation.ATTACHVOLUME_SERVICE;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.VolumeService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Volume;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import com.att.cdp.exceptions.TimeoutException;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import com.woorea.openstack.base.client.OpenStackBaseException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class AttachVolumeServer extends ProviderServerOperation {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AttachVolumeServer.class);
    private static final Configuration config = ConfigurationFactory.getConfiguration();
    private Server attachVolume(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext requestContext = new RequestContext(ctx);
        requestContext.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String volumeId = params.get(ProviderAdapter.VOLUME_ID);
        String device = params.get(ProviderAdapter.DEVICE);
        VMURL vm = VMURL.parseURL(vmUrl);
        Context context;
        String tenantName = "Unknown";// to be used also in case of exception
        try {
            if (validateVM(requestContext, appName, vmUrl, vm)) {
                return null;
            }
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            context = getContext(requestContext, vmUrl, identStr);
            if (context != null) {
                tenantName = context.getTenantName();// this variable also is
                                                        // used in case of
                                                        // exception
                requestContext.reset();
                server = lookupServer(requestContext, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                Context contx = server.getContext();
                ComputeService service = contx.getComputeService();
                Volume volume = new Volume();
                boolean flag = false;
				if (validateAttach(service, vm.getServerId(), volumeId, device)) {
					String msg = "Volume with volume id " + volumeId + " cannot be attached as it already exists";
					logger.info("Already volumes exists:");
					ctx.setAttribute("VOLUME_STATUS", "FAILURE");
					doFailure(requestContext, HttpStatus.METHOD_NOT_ALLOWED_405, msg);
					flag = false;
				} else {
					volume.setId(volumeId);
					logger.info("Ready to Attach Volume to the server:");
					service.attachVolume(server, volume, device);
					flag = true;
				}
				if (flag) {
					if (validateAttach(requestContext, service, vm.getServerId(), volumeId, device)) {
						ctx.setAttribute("VOLUME_STATUS", "SUCCESS");
						doSuccess(requestContext);
					} else {
						String msg = "Volume with " + volumeId + " unable  to attach";
						logger.info("Volume with " + volumeId + " unable to attach");
						ctx.setAttribute("VOLUME_STATUS", "FAILURE");
						doFailure(requestContext, HttpStatus.CONFLICT_409, msg);
					}
				}
                context.close();
            } else {
                ctx.setAttribute("VOLUME_STATUS", "CONTEXT_NOT_FOUND");
            }
        } catch (ZoneException e) {
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
            doFailure(requestContext, HttpStatus.NOT_FOUND_404, msg);
        } catch (RequestFailedException e) {
            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
            doFailure(requestContext, e.getStatus(), e.getMessage());
        } catch (Exception ex) {
            String msg = EELFResourceManager.format(Msg.ATTACHINGVOLUME_SERVER, ex, ex.getClass().getSimpleName(),
                    ATTACHVOLUME_SERVICE.toString(), vmUrl, tenantName);
            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
			try {
				ExceptionMapper.mapException((OpenStackBaseException) ex);
			} catch (ZoneException e1) {
				logger.error(e1.getMessage());
			}
            doFailure(requestContext, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.ATTACHVOLUME_SERVICE.toString(), "App-C IaaS Adapter:attachVolume", ADAPTER_NAME);
        logOperation(Msg.ATTACHINGVOLUME_SERVER, params, context);
        return attachVolume(params, context);
    }
    
	protected boolean validateAttach(ComputeService ser, String vm, String volumeId, String device)
			throws RequestFailedException, ZoneException {
		boolean flag = false;
		Map<String, String> map = ser.getAttachments(vm);
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry volumes = (Map.Entry) it.next();
			if (map != null && !(map.isEmpty())) {
				logger.info("volumes available before attach");
				logger.info("device" + volumes.getKey() + "Values" + volumes.getValue());
				if (volumes.getKey().equals(device) && (volumes.getValue().equals(volumeId))) {
					logger.info("Device " + volumes.getKey() + "Volumes" + volumes.getValue());
					flag = true;
				}
			}
		}
		logger.info("AttachVolumeFlag" + flag);
		return flag;
	}
	
    protected boolean validateAttach(RequestContext rc, ComputeService ser, String vm, String volumeId, String device)
			throws RequestFailedException, ZoneException {
		boolean flag = false;
		String msg = null;
		config.setProperty(Constants.PROPERTY_RETRY_DELAY, "10");
		config.setProperty(Constants.PROPERTY_RETRY_LIMIT, "30");
		while (rc.attempt()) {
			Map<String, String> map = ser.getAttachments(vm);
			if (map != null && !(map.isEmpty())) {
				Iterator<Entry<String, String>> it = map.entrySet().iterator();
				logger.info("volumes available after attach ");
				while (it.hasNext()) {
					Map.Entry volumes = (Map.Entry) it.next();
					logger.info(" devices " + volumes.getKey() + "volumes" + volumes.getValue());
					if (volumes.getKey().equals(device) && (volumes.getValue().equals(volumeId))) {
						logger.info("Device" + volumes.getKey() + "Volume" + volumes.getValue());
						flag = true;
						break;
					}
				}
				if (flag) {
					logger.info("AttachVolume" + rc.getAttempts() + "No.of attempts");
					break;
				} else {
					rc.delay();
				}
			}
		}
		if ((rc.getAttempts() == 30) && (!flag)) {

			msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, Long.toString(rc.getRetryDelay()),
					Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
			logger.error(msg);
			throw new TimeoutException(msg);
		}
		logger.info("AttachVolume Flag -->" + flag);
		return flag;
	}

}
