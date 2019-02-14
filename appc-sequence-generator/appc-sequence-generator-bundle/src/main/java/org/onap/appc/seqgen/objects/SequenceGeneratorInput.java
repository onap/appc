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

package org.onap.appc.seqgen.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;

import javax.validation.constraints.NotNull;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequenceGeneratorInput {

    @NotNull
    @JsonProperty("request-info")
    private RequestInfo requestInfo;

    private InventoryModel inventoryModel;

    private VnfcDependencyModel dependencyModel;

    @JsonProperty("tunable-parameters")
    private Map<String,String> tunableParams;

    private CapabilityModel capabilityModel;

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public InventoryModel getInventoryModel() {
        return inventoryModel;
    }

    public void setInventoryModel(InventoryModel inventoryModel) {
        this.inventoryModel = inventoryModel;
    }

    public VnfcDependencyModel getDependencyModel() {
        return dependencyModel;
    }

    public void setDependencyModel(VnfcDependencyModel dependencyModel) {
        this.dependencyModel = dependencyModel;
    }

    public Map<String, String> getTunableParams() {
        return tunableParams;
    }

    public void setTunableParams(Map<String, String> tunableParams) {
        this.tunableParams = tunableParams;
    }

    public CapabilityModel getCapability() {
        return capabilityModel;
    }

    public void setCapabilityModel(CapabilityModel capabilityModel) {
        this.capabilityModel = capabilityModel;
    }
}
