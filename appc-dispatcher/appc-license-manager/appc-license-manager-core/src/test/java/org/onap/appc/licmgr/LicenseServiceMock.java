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

package org.onap.appc.licmgr;

import org.onap.appc.licmgr.exception.DataAccessException;

import java.util.HashMap;
import java.util.Map;

class LicenseServiceMock implements LicenseDataAccessService {

    private String configFile;
    private HashMap<String, String> backupConf;

    @Override
    public void setSchema(String schema) {
    }

    void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public HashMap<String, String> getBackupConf() {
        return backupConf;
    }

    @Override
    public Map<String, String> retrieveLicenseModelData(String vnfType, String vnfVersion, String... fields) throws DataAccessException {
        return null;
    }

}
