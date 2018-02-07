/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.data;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

    @JsonProperty("response-code")
    private String responseCode;

    @JsonProperty("response-message")
    private String responseMessage;

    @JsonProperty("response-action")
    private ResponseAction responseAction;

    private String responseActionHanlder;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }


    public String getResponseActionHanlder() {
        return responseActionHanlder;
    }

    public void setResponseActionHanlder(String responseActionHanlder) {
        this.responseActionHanlder = responseActionHanlder;
    }

    public ResponseAction getResponseAction() {
        return responseAction;
    }

    public void setResponseAction(ResponseAction responseAction) {
        this.responseAction = responseAction;
    }

    @Override
    public String toString() {
        return "Response [responseCode=" + responseCode + ", responseMessage=" + responseMessage + ", responseAction="
                + responseAction + ", responseActionHanlder=" + responseActionHanlder + "]";
    }

}
