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
package org.onap.appc.adapter.restHealthcheck;

import java.util.Map;
import com.att.cdp.zones.model.Server;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;


public interface RestHealthcheckAdapter extends SvcLogicJavaPlugin {


    static final String PROPERTY_PROVIDER_TYPE = "org.onap.appc.provider.type";


    static final String PROPERTY_PROVIDER_NAME = "org.onap.appc.provider.name";

    static final String PROPERTY_INSTANCE_URL = "org.onap.appc.instance.url";


    static final String PROPERTY_IDENTITY_URL = "org.onap.appc.identity.url";

    String getAdapterName();

    void checkHealth(Map<String, String> params, SvcLogicContext ctx) ;

}
