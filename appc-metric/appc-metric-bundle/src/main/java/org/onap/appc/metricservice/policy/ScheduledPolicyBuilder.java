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

import org.onap.appc.metricservice.Publisher;
import org.onap.appc.metricservice.metric.Metric;

/**
 *
 * An auxiliary class to instantiate scheduler-based policy delivered along with the initial release of the service.
 *
 */
public interface ScheduledPolicyBuilder {
    ScheduledPolicyBuilder withStartTime(long time);
    ScheduledPolicyBuilder withPeriod(long period);
    ScheduledPolicyBuilder withPublishers(Publisher[] publishers);
    ScheduledPolicyBuilder withMetrics(Metric[] metrics);
    PublishingPolicy build();
}
