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

package org.onap.appc.listener.LCM.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;
import org.onap.appc.listener.util.Mapper;

/**
 * This class represents a message being sent out to DMaaP by APPC to update listeners on the status of a request
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputBody {

    @JsonProperty("common-header")
    private CommonHeader header;

    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("locked")
    private String locked;

    public OutputBody() {
    }

    public OutputBody(InputBody msg) {
        this.header = new CommonHeader(msg.getCommonHeader());
    }


    public JSONObject toResponse() {
        return Mapper.toJsonObject(this);
    }

    public String getLocked() {
        return locked;
    }

    public String getPayload() {
        return payload;
    }

    public CommonHeader getHeader() {
        return header;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public void setHeader(CommonHeader header) {
        this.header = header;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return String.format("%s - %s(%s)", getHeader().getRequestID(), getStatus().getCode(), getStatus().getValue());
    }
}

