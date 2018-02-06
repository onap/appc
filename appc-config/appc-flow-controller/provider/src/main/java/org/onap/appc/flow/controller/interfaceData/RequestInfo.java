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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.07.31 at 10:30:39 AM EDT
//


package org.onap.appc.flow.controller.interfaceData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "action",
    "actionLevel",
    "actionIdentifier",
    "payload"
})
@XmlRootElement(name = "request-info")
public class RequestInfo {

    @XmlElement(required = true)
    protected String action;
    @XmlElement(name = "action-level", required = true)
    @JsonProperty("action-level")
    protected String actionLevel;
    @XmlElement(name = "action-identifier", required = true)
    @JsonProperty("action-identifier")
    protected ActionIdentifier actionIdentifier;
    @XmlElement(required = true)
    protected String payload;

    /**
     * Gets the value of the action property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the actionLevel property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getActionLevel() {
        return actionLevel;
    }

    /**
     * Sets the value of the actionLevel property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setActionLevel(String value) {
        this.actionLevel = value;
    }

    /**
     * Gets the value of the actionIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link ActionIdentifier }
     *
     */
    public ActionIdentifier getActionIdentifier() {
        return actionIdentifier;
    }

    /**
     * Sets the value of the actionIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link ActionIdentifier }
     *
     */
    public void setActionIdentifier(ActionIdentifier value) {
        this.actionIdentifier = value;
    }

    /**
     * Gets the value of the payload property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the value of the payload property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayload(String value) {
        this.payload = value;
    }

    @Override
    public String toString() {
        return "RequestInfo [action=" + action + ", actionLevel=" + actionLevel
                + ", actionIdentifier=" + actionIdentifier + ", payload="
                + payload + "]";
    }

}
