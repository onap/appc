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

package org.onap.appc.adapter.ssh;

/**
 * Factory class for creating SshConnection instances.
 */
public interface SshAdapter {

    /**
     * Creates instance of SshConnection.
     *
     * @param host remote host to open SSH connection to
     * @param port remote SSH port
     * @param username SSH connection user name
     * @param password SSH connection password
     * @return instance of SshConnection
     */
    SshConnection getConnection(String host, int port, String username, String password);

    /**
     * Creates instance of SshConnection.
     *
     * @param host remote host to open SSH connection to
     * @param port remote SSH port
     * @param keyFile SSH connection key file location
     * @return instance of SshConnection
     */
    SshConnection getConnection(String host, int port, String keyFile);
}
