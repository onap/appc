/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018-2019 IBM
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * ================================================================================
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

package org.onap.appc.artifact.handler.dbservices;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DBServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testSaveArtifacts() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        int internalVersion = 1;
        dbService.saveArtifacts(ctx, internalVersion);
    }

    @Test
    public void testLogData() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String prefix = "test";
        assertEquals(QueryStatus.SUCCESS, dbService.logData(ctx, prefix));
    }

    @Test
    public void testProcessConfigActionDg() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        dbService.processConfigActionDg(ctx, isUpdate);
    }


    @Test
    public void testGetModelDataInformationbyArtifactName() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactName = "test";
        assertNotNull(dbService.getModelDataInformationbyArtifactName(artifactName));
    }

    @Test
    public void testUpdateYangContents() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactId = "TestArtifact";
        String yangContents = "TestYangContents";
        dbService.updateYangContents(ctx, artifactId, yangContents);
    }

    @Test
    public void testInsertProtocolReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        dbService.insertProtocolReference(ctx, vnfType, protocol, action, actionLevel, template);
    }


    @Test
    public void testInsertProtocolReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        dbService.insertProtocolReference(ctx, vnfType, protocol, action, actionLevel, template);
    }

    @Test
    public void testProcessDownloadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDownloadDgReference(ctx, isUpdate);
    }

    @Test
    public void testProcessVnfcReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        dbService.processVnfcReference(ctx, isUpdate);
    }

    @Test
    public void testProcessDeviceAuthentication() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute("url", "");
        String expectedKey ="update DEVICE_AUTHENTICATION set USER_NAME = $user-name , PORT_NUMBER = $port-number, URL = $url  where VNF_TYPE = $vnf-type  AND PROTOCOL = $device-protocol AND  ACTION = $action";
        boolean isUpdate = true;
        dbService.processDeviceAuthentication(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test
    public void testProcessDeviceAuthenticationforFalse() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute("url", "");
        boolean isUpdate = false;
        dbService.processDeviceAuthentication(ctx, isUpdate);
        assertEquals(true,ctx.getAttribute("keys").contains("DEVICE_AUTHENTICATION"));
    }

    @Test
    public void testProcessDeviceInterfaceProtocol() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDeviceInterfaceProtocol(ctx, isUpdate);
    }

    @Test
    public void testProcessDeviceInterfaceProtocolForFalse() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        dbService.processDeviceInterfaceProtocol(ctx, isUpdate);
        assertEquals(true,ctx.getAttribute("keys").contains("DEVICE_INTERFACE_PROTOCOL"));
    }

    @Test
    public void testProcessSdcReferences() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "testCategory");
        boolean isUpdate = true;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Test
    public void testIsArtifactUpdateRequired() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.isArtifactUpdateRequired(ctx, db);
    }

    @Test
    public void testgetArtifactID() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.getArtifactID(ctx, db);
    }

    @Test
    public void testGetDownLoadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, "CLI");
        assertEquals("TestDG", dbService.getDownLoadDGReference(ctx));
    }

    @Test
    public void testInitialise() {
        DBService dbService = DBService.initialise();
        assertNotNull(dbService);
    }

    @Test
    public void testGetInternalVersionNumberException() throws SvcLogicException {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - getting internal Artifact Number");
        dbService.getInternalVersionNumber(ctx, "artifactName", "prefix");
    }

    @Test
    public void testGetArtifactIDException() throws SvcLogicException {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - getting  Artifact ID from database");
        dbService.getArtifactID(ctx, "artifactName");
    }

    @Test
    public void testSaveArtifactsException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing storing Artifact: null");
        dbService.saveArtifacts(ctx, -1);
    }

    @Test
    public void testLogDataException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error while logging data");
        dbService.logData(ctx, null);
    }

    @Test
    public void testProcessSdcReferencesException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("file-category", "test");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing sdc_reference table ");
        dbService.processSdcReferences(ctx, false);
    }

    @Test
    public void testIsArtifactUpdateRequiredException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(DBException.class);
        expectedEx.expectMessage("An error occurred while checking for artifact update");
        dbService.isArtifactUpdateRequired(ctx, "db");
    }

    @Test
    public void testProcessDeviceInterfaceProtocolException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing DEVICE_INTERFACE_PROTOCOL table ");
        dbService.processDeviceInterfaceProtocol(ctx, false);
    }

    @Test
    public void testProcessDeviceAuthenticationException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        boolean isUpdate = true;
        expectedEx.expect(DBException.class);
        expectedEx.expectMessage("An error occurred when processing device authentication");
        dbService.processDeviceAuthentication(ctx, isUpdate);
    }

    @Test
    public void testProcessVnfcReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing VNFC_REFERENCE table ");
        dbService.processVnfcReference(ctx, true);
    }

    @Test
    public void testProcessDownloadDGReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing DOWNLOAD_DG_REFERENCE table ");
        dbService.processDownloadDgReference(ctx, false);
    }

    @Test
    public void testProcessConfigActionDgException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing Configure DG Action table ");
        dbService.processConfigActionDg(ctx, true);
    }

    @Test
    public void testGetModelDataInformationbyArtifactNameException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing is ArtifactUpdateRequiredforPD table ");
        dbService.getModelDataInformationbyArtifactName("test");
    }

    @Test
    public void testUpdateYangContentsException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactId = "TestArtifact";
        String yangContents = "TestYangContents";
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing Configure DG Action table ");
        dbService.updateYangContents(ctx, artifactId, yangContents);
    }

    @Test
    public void testGetDownLoadDGReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, "CLI");
        expectedEx.expect(DBException.class);
        expectedEx.expectMessage("An error occurred when getting DG reference");
        assertEquals("TestDG", dbService.getDownLoadDGReference(ctx));
    }
    
    @Test
    public void testcreateQueryListForTemplateIds() {
        MockDBService dbService = MockDBService.initialise(true);
        String queryPart = dbService.createQueryListForTemplateIds("modelId");
        String expected = " AND ARTIFACT_NAME like '%_modelId.%'";
        assertEquals(expected, queryPart);
    }
    
    @Test
    public void testisProtocolReferenceUpdateRequired() throws SvcLogicException {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        boolean result = dbService.isProtocolReferenceUpdateRequired(ctx, vnfType, protocol, action, actionLevel, template);
        assertTrue(result);
    }
    
    @Test
    public void testProcessConfigureActionDg() throws SvcLogicException {
        MockDBService dbService = MockDBService.initialise(true);
        SvcLogicContext ctx = new SvcLogicContext();
        dbService.processConfigureActionDg(ctx, true);
    }
    
    @Test
    public void testUpdateProtocolReference() throws SvcLogicException {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        dbService.updateProtocolReference(ctx, vnfType, protocol, action, actionLevel, template);
       
    }
    
}

