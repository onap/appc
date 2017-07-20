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

package org.openecomp.appc.oam.messageadapter;




import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;import org.openecomp.appc.oam.AppcOam;

public class OAMContext {

    private AppcOam.RPC rpcName;
    private CommonHeader commonHeader;
    private Status status;


    public AppcOam.RPC getRpcName() {
        return rpcName;
    }

    public void setRpcName(AppcOam.RPC rpcName) {
        this.rpcName = rpcName;
    }

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "OAMContext {" +
                "rpcName=" + rpcName +
                ", commonHeader=" + commonHeader +
                ", status=" + status +
                '}';
    }
}
