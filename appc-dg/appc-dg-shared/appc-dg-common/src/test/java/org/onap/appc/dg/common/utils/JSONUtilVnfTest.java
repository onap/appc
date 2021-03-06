/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 AT&T
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.dg.common.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "vnfId", "vnfType" })

public class JSONUtilVnfTest {

        @JsonProperty("vnfId")
        private String vnfId;

        @JsonProperty("vnfType")
        private String vnfType;

        public JSONUtilVnfTest()
        {
           super();
        }

        public JSONUtilVnfTest(String vnfId, String vnfType)
        {
            this.vnfId = vnfId;
            this.vnfType = vnfType;
        }

        public String getVnfId()
        {
            return vnfId;
        }

        public void setVnfId(String vnfId)
        {
            this.vnfId = vnfId;
        }

        public String getVnfType()
        {
            return vnfType;
        }

        public void setVnfType(String vnfType)
        {
            this.vnfType = vnfType;
        }

}
