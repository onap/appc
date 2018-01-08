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

package org.onap.appc.lockmanager.impl.sql;

import java.sql.Connection;


import org.onap.appc.dao.util.api.JdbcConnectionFactory;
import org.onap.appc.lockmanager.api.LockManager;

public abstract class JdbcLockManager implements LockManager {

    private static final String DEF_TABLE_LOCK_MANAGEMENT = "LOCK_MANAGEMENT";

    private JdbcConnectionFactory connectionFactory;
    protected String tableName = DEF_TABLE_LOCK_MANAGEMENT;

    public void setConnectionFactory(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    protected Connection openDbConnection() {
        return connectionFactory.openDbConnection();
    }

    protected void closeDbConnection(Connection connection) {
        connectionFactory.closeDbConnection(connection);
    }
}
