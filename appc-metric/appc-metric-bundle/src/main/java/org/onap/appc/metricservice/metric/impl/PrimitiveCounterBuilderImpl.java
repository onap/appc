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

package org.onap.appc.metricservice.metric.impl;

import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.PrimitiveCounter;
import org.onap.appc.metricservice.metric.PrimitiveCounterBuilder;


public class PrimitiveCounterBuilderImpl implements PrimitiveCounterBuilder {
    private  String name;
    private  MetricType metricType;
    private  long counter;

    @Override
    public PrimitiveCounterBuilder withName(String name) {
        this.name=name;
        return this;
    }

    @Override
    public PrimitiveCounterBuilder withValue(long value) {
        this.counter=value;
        return this;
    }

    @Override
    public PrimitiveCounterBuilder withType(MetricType type) {
        this.metricType=type;
        return this;
    }

    @Override
    public PrimitiveCounter build() {
        return new DefaultPrimitiveCounter(this.name,this.metricType,this.counter);
    }
}
