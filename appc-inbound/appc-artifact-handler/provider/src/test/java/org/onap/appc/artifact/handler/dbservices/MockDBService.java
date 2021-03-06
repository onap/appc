/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.artifact.handler.dbservices;

public class MockDBService extends DBService {
    private static MockDBService mockDgGeneralDBService = null;
    private static MockDBService mockDgGeneralDBServiceFailure = null;
    private static MockDbLibServiceQueries serviceLogic = new MockDbLibServiceQueries();
    private static MockDbLibServiceQueriesFailure serviceLogicFailure = new MockDbLibServiceQueriesFailure();


    public MockDBService() {
        super(serviceLogic);
        if (mockDgGeneralDBService != null) {
            mockDgGeneralDBService = new MockDBService(serviceLogic);
        }
    }

    public MockDBService(MockDbLibServiceQueries serviceLogic2) {
        super(serviceLogic);
    }

    public MockDBService(MockDbLibServiceQueriesFailure serviceLogic2) {
        super(serviceLogicFailure);
    }

    public static MockDBService initialise() {
        System.out.println("tesateas");
        if (mockDgGeneralDBService == null) {
            mockDgGeneralDBService = new MockDBService(serviceLogic);
        }
        return mockDgGeneralDBService;
    }

    public static MockDBService initialise(boolean failure) {
        if(failure == true) {
            mockDgGeneralDBServiceFailure = new MockDBService(serviceLogicFailure);
        }
        return mockDgGeneralDBServiceFailure;
    }
}
