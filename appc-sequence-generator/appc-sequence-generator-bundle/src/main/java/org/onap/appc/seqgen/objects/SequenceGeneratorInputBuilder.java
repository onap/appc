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

import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequenceGeneratorInputBuilder {

    private RequestInfo requestInfo;

    private InventoryModel inventoryModel;

    private VnfcDependencyModel dependencyModel;

    private Map<String,String> tunableParams;

    private CapabilityModel capabilityModel;
    
    public SequenceGeneratorInputBuilder requestInfo(RequestInfo requestInfo){
        this.requestInfo = requestInfo;
        return this;
    }

    public SequenceGeneratorInputBuilder tunableParameter(String key,String value){
        if(this.tunableParams ==null){
            this.tunableParams = new HashMap<>();
        }
        this.tunableParams.put(key,value);
        return this;
    }

    public SequenceGeneratorInputBuilder inventoryModel(InventoryModel model){
        this.inventoryModel = model;
        return this;
    }

    public SequenceGeneratorInputBuilder dependendcyModel(VnfcDependencyModel model){
        this.dependencyModel = model;
        return this;
    }

    public SequenceGeneratorInputBuilder capabilityModel(CapabilityModel model) {
        this.capabilityModel = model;
        return this;
    }
    public SequenceGeneratorInput build(){
        SequenceGeneratorInput input = new SequenceGeneratorInput();
        input.setRequestInfo(this.requestInfo);
        input.setCapabilityModel(this.capabilityModel);
        input.setInventoryModel(this.inventoryModel);
        input.setDependencyModel(this.dependencyModel);
        input.setTunableParams(this.tunableParams);
        return input;
    }



}
