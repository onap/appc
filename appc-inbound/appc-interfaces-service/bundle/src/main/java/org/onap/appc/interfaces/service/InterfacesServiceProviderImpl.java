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

package org.onap.appc.interfaces.service;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.InterfacesServiceService;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.response.info.ResponseInfoBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.onap.appc.interfaces.service.executor.ServiceExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

public class InterfacesServiceProviderImpl implements InterfacesServiceService{

    private static final Logger log = LoggerFactory.getLogger(InterfacesServiceProviderImpl.class);

    @Override
    public Future<RpcResult<ExecuteServiceOutput>> executeService(ExecuteServiceInput input) {

        log.info("Received Request: " + input.getRequest().getRequestId() + " Action : " + 
                input.getRequest().getAction() + " with RequestData  :" + input.getRequest().getRequestData() + " and data-Type : " + input.getRequest().getRequestDataType());
        String request_id = input.getRequest().getRequestId();
        String action = input.getRequest().getAction();
        ResponseInfoBuilder responseInfoBuilder = new ResponseInfoBuilder();
        ExecuteServiceOutputBuilder executeServicebuilder = new ExecuteServiceOutputBuilder();
        ServiceExecutor serviceExecutor = new ServiceExecutor();
        StatusBuilder statusBuilder = new StatusBuilder();
        try{
            String response = serviceExecutor.execute(action, input.getRequest().getRequestData(), input.getRequest().getRequestDataType());
            responseInfoBuilder.setBlock(response);
            responseInfoBuilder.setRequestId(request_id);
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");
        }
        catch(Exception e){
            log.error("Error" + e.getMessage());
            e.printStackTrace();
            statusBuilder.setCode("401");
            statusBuilder.setMessage("failuer");
        }
        executeServicebuilder.setResponseInfo(responseInfoBuilder.build());
        executeServicebuilder.setStatus(statusBuilder.build());
        RpcResult<ExecuteServiceOutput> result  = RpcResultBuilder.<ExecuteServiceOutput>status(true).withResult(executeServicebuilder.build()).build();
        return Futures.immediateFuture(result);
    }
}
