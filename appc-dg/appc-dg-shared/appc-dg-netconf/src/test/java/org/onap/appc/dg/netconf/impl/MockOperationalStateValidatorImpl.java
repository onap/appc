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

package org.onap.appc.dg.netconf.impl;

import org.onap.appc.adapter.netconf.OperationalStateValidator;
import org.onap.appc.adapter.netconf.VnfType;
import org.onap.appc.exceptions.APPCException;


public class MockOperationalStateValidatorImpl implements OperationalStateValidator {

    private boolean validated;
    private String configurationFileName;

    public boolean isValidated() {
        return validated;
    }

    @Override
    public VnfType getVnfType() {
        return null;
    }

    @Override
    public String getConfigurationFileName() {
        return configurationFileName;
    }

    @Override
    public void validateResponse(String response) throws APPCException {
        if (response.equals("wrong")) {
            throw new APPCException();
        } else {
            this.validated = true;
        }

    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }
}
