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

package org.onap.appc.workflow.impl;

class WorkflowKey {
    private final String name;
    private final String version;
    private final String module;

    WorkflowKey(String name, String version, String module) {
        this.name = name;
        this.version = version;
        this.module = module;
    }

    String name() {
        return name;
    }

    String version() {
        return version;
    }

    String module() {
        return module;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(128);
        buff.append("{");
        buff.append("module = ").append(module);
        buff.append(", ");
        buff.append("name = ").append(name);
        buff.append(", ");
        buff.append("version = ").append(version);
        buff.append("}");
        return buff.toString();
    }
}
