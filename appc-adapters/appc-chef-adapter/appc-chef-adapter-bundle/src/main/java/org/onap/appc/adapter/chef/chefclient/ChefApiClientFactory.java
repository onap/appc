/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.chef.chefclient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.appc.adapter.chef.chefclient.api.ChefApiClient;
import org.onap.appc.adapter.chef.chefclient.impl.ChefApiClientImpl;
import org.onap.appc.adapter.chef.chefclient.impl.ChefApiHeaderFactory;

public final class ChefApiClientFactory {

    private HttpClient httpClient = HttpClients.createDefault();
    private ChefApiHeaderFactory chefApiHeaderFactory = new ChefApiHeaderFactory();

    public ChefApiClient create(String endPoint, String organizations, String userId, String pemPath) {
        return new ChefApiClientImpl(httpClient,
            chefApiHeaderFactory,
            endPoint,
            organizations,
            userId,
            pemPath);
    }
}
