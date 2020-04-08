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

package org.onap.appc.requesthandler.impl;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a message being sent out to DMaaP by APPC as async response.
 * note the structure of this class must be adapted to the sync message sent to DMaaP represened in org.onap.appc.listener.LCM.domainmodel.DmaapOutgoingMessage
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmaapOutgoingMessage {

    @JsonProperty("version")
    private String version;

    @JsonProperty("type")
    private String type;

    @JsonProperty("correlation-id")
    private String correlationID;

    private final static String defaultCambriaPartition = "MSO";
    @JsonProperty("cambria.partition")
    private String cambriaPartition = defaultCambriaPartition;

    @JsonProperty("rpc-name")
    private String rpcName;

    @JsonProperty("body")
    private Body body;

    public DmaapOutgoingMessage() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getCambriaPartition() {
        return cambriaPartition;
    }

    public void setCambriaPartition(String cambriaPartition) {
        this.cambriaPartition = cambriaPartition;
    }

    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DmaapOutgoingMessage{" +
                "cambriaPartition='" + cambriaPartition + '\'' +
                ", rpcName='" + rpcName + '\'' +
                ", body=" + body +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        public Body() {
        }

        public Body(Object output) {
            this.output = output;
        }

        @JsonProperty("output")
        private Object output;

        public Object getOutput() {
            return output;
        }

        public void setOutput(Object body) {
            this.output = body;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "output=" + output +
                    '}';
        }
    }
}

