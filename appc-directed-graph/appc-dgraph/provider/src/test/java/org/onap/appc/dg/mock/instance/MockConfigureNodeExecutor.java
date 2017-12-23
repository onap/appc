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

package org.onap.appc.dg.mock.instance;


import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class MockConfigureNodeExecutor implements SvcLogicAdaptor{

    @Override
    public ConfigStatus configure(String key, Map<String, String> parameters, SvcLogicContext ctx) {

        if ( "get".equals(key)) {

            if ("135.1.1.1".equals(parameters.get("host") )) {
                    String fullPathFileName = parameters.get("fullPathFileName");
                    if (("SuccessFile").equals(fullPathFileName ) )
                        return ConfigStatus.FAILURE;
                    if (("FailureFile").equals(fullPathFileName ) )
                        return ConfigStatus.SUCCESS;
                    if (("FailureLogFile").equals(fullPathFileName ) )
                        return ConfigStatus.SUCCESS;
            }
            else if ("135.2.2.2".equals(parameters.get("host")) ) {
                String fullPathFileName = parameters.get("fullPathFileName");
                if (("SuccessFile").equals(fullPathFileName ) )
                    return ConfigStatus.FAILURE;
                if (("FailureFile").equals(fullPathFileName ) )
                    return ConfigStatus.FAILURE;
            }

            return ConfigStatus.SUCCESS;
        }
        else {
            ctx.setAttribute("Status", "Success");

            return ConfigStatus.SUCCESS;
        }
    }

    @Override
    public ConfigStatus activate(String key, SvcLogicContext ctx) {
        return null;
    }

    @Override
    public ConfigStatus deactivate(String key, SvcLogicContext ctx) {
        return null;
    }
}
