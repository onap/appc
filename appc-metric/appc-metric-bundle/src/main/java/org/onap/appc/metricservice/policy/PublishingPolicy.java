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

package org.onap.appc.metricservice.policy;

import org.onap.appc.exceptions.APPCException;
import org.onap.appc.metricservice.metric.Metric;

/**
 *
 * A custom logic that determines strategy of exposing metric values.
 * Policy is associated with a specific registry and its implementation can very from the simplest options, such as 'real-time' behavior (upon each value change) to more sophisticated approaches such as thresholds or recurrent scheduling rules.
 * The policy can also decide whether or not to publish the entire metric registry or just a subset of the it (e.g. changed ones).
 * Policy supports execution of one or more Publishers.
 *
 */
public interface PublishingPolicy {
    void        onMetricChange(Metric meric) throws APPCException;
    Metric[]    metrics();

    void init();
}
