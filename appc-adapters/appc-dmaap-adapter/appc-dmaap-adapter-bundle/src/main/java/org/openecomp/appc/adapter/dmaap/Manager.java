/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
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

package org.openecomp.appc.adapter.dmaap;

import java.util.Set;

public interface Manager {

    /**
     * Updates the api credentials for making authenticated requests
     * 
     * @param apiKey
     *            The public key to authenticate with
     * @param apiSecret
     *            The secret key to authenticate with
     */
    public void updateCredentials(String apiKey, String apiSecret);

    /**
     * Return a set of strings representing topics that the user can see
     *
     * @return A set of strings with topic names or an empty set if no topics are visible
     */
    public Set<String> getTopics();

}
