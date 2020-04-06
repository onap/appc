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

package org.onap.appc.dg.flowbuilder.impl;

import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.flowbuilder.FlowBuilder;
import org.onap.appc.dg.flowbuilder.FlowStrategy;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.dg.objects.VnfcFlowModel;

public class FlowBuilderImpl implements FlowBuilder {



    private FlowStrategy strategy;

    FlowBuilderImpl(FlowStrategy strategy){
        this.strategy = strategy;
    }

    public VnfcFlowModel buildFlowModel(VnfcDependencyModel dependencyModel, InventoryModel inventoryModel) throws InvalidDependencyModelException{
        return strategy.buildFlowModel(dependencyModel, inventoryModel);
    }


}
