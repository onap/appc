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

package org.onap.appc.requesthandler.constant;

import java.util.concurrent.TimeUnit;

public class Constants {

    public Constants() {
    }

    public static final int DEFAULT_TTL = 30;
    public static final String SUCCESS_MSG="SUCCESS";
    public static final String FAILURE_MSG="FAILURE";
    public static final String DEFAULT_LOGGING_FLAG="true";
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES);
    public static final String DEFAULT_TTL_KEY="org.onap.appc.workflow.default.ttl";
}
