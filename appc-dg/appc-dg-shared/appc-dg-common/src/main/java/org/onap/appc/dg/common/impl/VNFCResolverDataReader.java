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

package org.onap.appc.dg.common.impl;

import java.util.Arrays;
import java.util.Collection;

public class VNFCResolverDataReader extends AbstractResolverDataReader {

    @Override
    protected Collection<String> getAttributeNames() {
        return Arrays.asList("action", "api_version", "vnf_type", "vnfc_type");
    }

    @Override
    protected String getQueryStmt() {
        return "select vnf_type,vnfc_type,api_version,action,dg_name,dg_version,dg_module FROM VNFC_DG_MAPPING";
    }
}
