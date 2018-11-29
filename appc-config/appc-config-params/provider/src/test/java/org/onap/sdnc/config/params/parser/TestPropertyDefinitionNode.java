/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM
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

package org.onap.sdnc.config.params.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.sdnc.config.params.ParamsHandlerConstant;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import org.onap.sdnc.config.params.transformer.ArtificatTransformer;

public class TestPropertyDefinitionNode {
    private PropertyDefinitionNode propertyDefinitionNode;

    @Before
    public void setup() {
        propertyDefinitionNode = new PropertyDefinitionNode();
    }

    @Test
    public void testProcessMissingParamKeys() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String yamlData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);

        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.processMissingParamKeys(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ParamsHandlerConstant.OUTPUT_PARAM_STATUS),
                ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

    }

    @Test(expected = SvcLogicException.class)
    public void testInProcessMissingParamKeysForEmptyPdContent() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.processMissingParamKeys(inParams, ctx);
    }

    @Test
    public void testProcessExternalSystemParamKeys() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String yamlData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);

        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

        inParams.put(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME, "SOURCE");

        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.processExternalSystemParamKeys(inParams, ctx);

        for (Object key : ctx.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
            if (StringUtils.contains(parmName, "keys")) {
                System.out.println("Key: " + parmName + ",    Value: " + parmValue);
            }
        }
        assertEquals(ctx.getAttribute("test." + ParamsHandlerConstant.OUTPUT_PARAM_STATUS),
                ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);
    }
    
    @Test
    public void testProcessExternalSystemParamKeysForEmptyParamData() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String yamlData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME, "SOURCE");

        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.processExternalSystemParamKeys(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ParamsHandlerConstant.OUTPUT_PARAM_STATUS),
                ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

      
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testProcessExternalSystemParamKeysForEmptySystemName() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String yamlData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);

        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Request Param (systemName) is Missing ..");
        propertyDefinitionNode.processExternalSystemParamKeys(inParams, ctx);

    }

    @Test(expected = SvcLogicException.class)
    public void testProcessExternalSystemParamKeysForEmptyPdContent() throws Exception {

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME, "SOURCE");
        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.processExternalSystemParamKeys(inParams, ctx);
    }

    @Test
    public void mergeJsonData() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

        String mergeJsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/merge-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_MERGE__JSON_DATA, mergeJsonData);

        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.mergeJsonData(inParams, ctx);
        String mergedParams = ctx.getAttribute("test." + ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER);
        assertNotNull(mergedParams);
        assertEquals(ctx.getAttribute("test." + ParamsHandlerConstant.OUTPUT_PARAM_STATUS),
                ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);
    }

    @Test
    public void mergeJsonDataForEmptyParams() throws SvcLogicException, IOException {

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String mergeJsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/merge-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_MERGE__JSON_DATA, mergeJsonData);
        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.mergeJsonData(inParams, ctx);
        String status = ctx.getAttribute("test.status");
        assertEquals(ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS, status);

    }

    @Test(expected = SvcLogicException.class)
    public void testMergeJsonDataCatchPortion() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.mergeJsonData(inParams, ctx);
    }

    @Test
    public void testArtificatTransformer() throws Exception {
        ArtificatTransformer transformer = new ArtificatTransformer();
        String yamlData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());

        PropertyDefinition propertyDefinition = transformer.convertYAMLToPD(yamlData);
        String yaml = transformer.convertPDToYaml(propertyDefinition);
    }

    @Test
    public void testValidationPd() throws Exception {
        Map<String, String> inParams = new HashMap<String, String>();
        SvcLogicContext ctx = new SvcLogicContext();
        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        String mergeJsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, jsonData);
        inParams.put(ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER, mergeJsonData);
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME, "INSTAR");
        propertyDefinitionNode.validateParams(inParams, ctx);
    }
    
    @Test(expected=SvcLogicException.class)
    public void testValidateParamsForEmptyParams() throws Exception
    {
        Map<String, String> inParams = new HashMap<String, String>();
        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.validateParams(inParams, ctx);
    }
    
    @Test
    public void testValidateParamsForEmptyConfigParams() throws Exception
    {
        Map<String, String> inParams = new HashMap<String, String>();
        String jsonData = IOUtils.toString(
                TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"),
                Charset.defaultCharset());
        inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, jsonData);
        SvcLogicContext ctx = new SvcLogicContext();
        propertyDefinitionNode.validateParams(inParams, ctx);
        String status = ctx.getAttribute("status");
        assertEquals(ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS,status);
    }

}
