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

package org.onap.appc.adapter.netconf;

import org.onap.appc.adapter.netconf.jsch.NetconfClientJsch;
import org.onap.appc.adapter.netconf.odlconnector.NetconfClientRestconfImpl;


public class NetconfClientFactory {

    public NetconfClient GetNetconfClient(NetconfClientType type){

        if(type==NetconfClientType.RESTCONF) {
            return new NetconfClientRestconfImpl();
        }else if(type == NetconfClientType.SSH){
            return new NetconfClientJsch();
        }

        return null;
    }
}
