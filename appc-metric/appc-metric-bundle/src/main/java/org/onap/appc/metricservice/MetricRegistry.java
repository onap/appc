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

package org.onap.appc.metricservice;

import org.onap.appc.metricservice.metric.Counter;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.MetricBuilderFactory;
import org.onap.appc.metricservice.policy.PolicyBuilderFactory;
import org.onap.appc.metricservice.policy.PublishingPolicy;

/**
 *
 *  A named group of metrics which might be related to a specific domain. The service doesn't limit number of metric registries per instance.
 *  It's up to application to decide on domain scope. The registry instances are independent.
 *
 */
public interface MetricRegistry {
    boolean register(Metric metric);
    void attach (PublishingPolicy publishPolicy);
    MetricBuilderFactory metricBuilderFactory();
    PolicyBuilderFactory policyBuilderFactory();
    Counter counter(String value);
    Counter[] counters();
    Metric[] metrics();
    Metric metric(String metricName);
    void dispose();
}
