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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.07.31 at 10:30:39 AM EDT
//


package org.onap.appc.flow.controller.interfaceData;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vnfId",
    "vnfName",
    "vnfType",
    "vm"
})
@XmlRootElement(name = "vnf-info")
public class VnfInfo {

    @XmlElement(name = "vnf-id", required = true)
    @JsonProperty("vnf-id")
    protected String vnfId;
    @XmlElement(name = "vnf-name", required = true)
    @JsonProperty("vnf-name")
    protected String vnfName;
    @XmlElement(name = "vnf-type", required = true)
    @JsonProperty("vnf-type")
    protected String vnfType;
    @JsonProperty("vm")
    protected List<Vm> vm;

    /**
     * Gets the value of the vnfId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVnfId() {
        return vnfId;
    }

    /**
     * Sets the value of the vnfId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVnfId(String value) {
        this.vnfId = value;
    }

    /**
     * Gets the value of the vnfName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVnfName() {
        return vnfName;
    }

    /**
     * Sets the value of the vnfName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVnfName(String value) {
        this.vnfName = value;
    }

    /**
     * Gets the value of the vnfType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVnfType() {
        return vnfType;
    }

    /**
     * Sets the value of the vnfType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVnfType(String value) {
        this.vnfType = value;
    }

    /**
     * Gets the value of the vm property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vm property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVm().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Vm }
     *
     *
     */
    public List<Vm> getVm() {
        if (vm == null) {
            vm = new ArrayList<Vm>();
        }
        return this.vm;
    }

    @Override
    public String toString() {
        return "VnfInfo [vnfId=" + vnfId + ", vnfName=" + vnfName
                + ", vnfType=" + vnfType + ", vm=" + vm + "]";
    }

}
