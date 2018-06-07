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

package org.onap.appc.listener.LCM.operation;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.LCM.model.InputBody;
import org.onap.appc.listener.LCM.model.ResponseStatus;

import java.net.URL;


public interface ProviderOperationRequestFormatter {

    String buildPath(URL url, String rpcName);

    String buildRequest(InputBody msg);

    ResponseStatus getResponseStatus(JsonNode json)throws APPCException;

    String getLocked(JSONObject json)throws APPCException;
}
