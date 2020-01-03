/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 Orange
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.domainmodel.lcm.*;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.conv.Converter;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;


public class ConverterTest {
    private String expectedJsonBodyStr ="{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}}";
    private String expectedDmaapOutgoingMessageJsonStringTest ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"test\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringRollback ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"rollback\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringSnapshot ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"snapshot\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringAudit ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"audit\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringHealthCheck ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"health-check\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringLiveUpgrade ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"live-upgrade\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringLock ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"lock\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringModifyConfig ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"config-modify\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringSoftwareUpload ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"software-upload\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringStop ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"stop\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringSync ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"sync\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringTerminate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"terminate\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUnlock ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"unlock\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUpgradePreCheck ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"upgrade-pre-check\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUpgradePostCheck ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"upgrade-post-check\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUpgradeSoftware ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"upgradesoftware\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringDownloadNeSw ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"download-ne-sw\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringActivateNeSw ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"activate-ne-sw\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUpgradeBackout ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"upgradebackout\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringUpgradeBackup ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"upgradebackup\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringReboot ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"reboot\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPreConfigure ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"pre-configure\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringGetConfig ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"get-config\",\"type\":\"response\"}";
    private String expectedJsonBodyStrwithPayload ="{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}}";
    private String expectedDmaapOutgoingMessageJsonStringDistributeTraffic = "{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"distribute-traffic\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringDistributeTrafficCheck = "{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"distribute-traffic-check\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPreRebuild ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"pre-rebuild\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPostRebuild ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"post-rebuild\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPreEvacuate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"pre-evacuate\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPreMigrate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"pre-migrate\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPostMigrate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"post-migrate\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringPostEvacuate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"post-evacuate\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringLicenseManagement ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"license-management\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringStartTraffic ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"start-traffic\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringStatusTraffic ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"status-traffic\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringStopTraffic ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"stop-traffic\",\"type\":\"response\"}";
    private String expectedDmaapOutgoingMessageJsonStringProvisioning ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"provisioning\",\"type\":\"response\"}";
 
    @Test
    public void convDateToZuluStringTest(){
        String dateToZuluString = Converter.convDateToZuluString(new Date(0L));
        Assert.assertEquals("1970-01-01T00:00:00.000Z", dateToZuluString);
    }

    @Test
    public void convAsyncResponseToBuilderTestTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Test;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringTestTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Test;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringTest, jsonStr);
    }

    @Test
    public void convPayloadObjectToJsonStringTest() throws JsonProcessingException, ParseException {
        String jsonString = Converter.convPayloadObjectToJsonString("any valid JSON string value");
        Assert.assertEquals("any valid JSON string value", jsonString);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("key","value");
        jsonString = Converter.convPayloadObjectToJsonString(hashMap);
        Assert.assertEquals("{\"key\":\"value\"}", jsonString);
    }

    @Test
    public void convAsyncResponseToBuilderRollbackTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Rollback;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringRollbackTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Rollback;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringRollback, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderSnapshotTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Snapshot;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringSnapshotTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Snapshot;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSnapshot, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderAuditTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Audit;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringAuditTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Audit;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringAudit, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderHealthCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.HealthCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringHealthCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.HealthCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringHealthCheck, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderLiveUpgradeTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.LiveUpgrade;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringLiveUpgradeTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.LiveUpgrade;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringLiveUpgrade, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderLockTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Lock;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringLockTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Lock;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringLock, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderModifyConfigTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.ConfigModify;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringModifyConfigTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.ConfigModify;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringModifyConfig, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderSoftwareUploadTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.SoftwareUpload;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringSoftwareUploadTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.SoftwareUpload;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSoftwareUpload, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderStopTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Stop;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringStopTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Stop;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringStop, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderSync() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Sync;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringSync() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Sync;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSync, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderTerminateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Sync;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringTerminateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Terminate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringTerminate, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderUnlockTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Unlock;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUnlockTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Unlock;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUnlock, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderRebootTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Reboot;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringRebootTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.Reboot;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringReboot, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderUpgradePreCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradePreCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUpgradePreCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradePreCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUpgradePreCheck, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderUpgradePostCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradePostCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUpgradePostCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradePostCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUpgradePostCheck, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderUpgradeSoftwareTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeSoftware;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUpgradeSoftwareTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeSoftware;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUpgradeSoftware, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderDownloadNeSwTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.DownloadNeSw;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringDownloadNeSwTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.DownloadNeSw;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringDownloadNeSw, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderActivateNeSwTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.ActivateNeSw;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringActivateNeSwTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.ActivateNeSw;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringActivateNeSw, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderUpgradeBackoutTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeBackout;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUpgradeBackoutTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeBackout;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUpgradeBackout, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderUpgradeBackupTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeBackup;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringUpgradeBackupTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.UpgradeBackup;
        String rpcName = action.name().toLowerCase();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUpgradeBackup, jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderPreConfigureTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreConfigure;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPreConfigureTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreConfigure;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPreConfigure, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderGetConfigTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.GetConfig;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringGetConfigTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.GetConfig;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringGetConfig, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderDistributeTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.DistributeTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringDistributeTrafficTest()
            throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.DistributeTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringDistributeTraffic, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderDistributeTrafficCheckTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.DistributeTrafficCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringDistributeTrafficCheckTest()
            throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponse();
        VNFOperation action = VNFOperation.DistributeTrafficCheck;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringDistributeTrafficCheck, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPreRebuildTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreRebuild;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPreRebuildTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreRebuild;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPreRebuild, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPostRebuildTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostRebuild;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPostRebuildTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostRebuild;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPostRebuild, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPreEvacuateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreEvacuate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPreEvacuateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreEvacuate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPreEvacuate, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPreMigrateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreMigrate;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPreMigrateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PreMigrate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPreMigrate, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPostMigrateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostMigrate;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPostMigrateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostMigrate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPostMigrate, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderPostEvacuateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostEvacuate;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringPostEvacuateTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.PostEvacuate;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringPostEvacuate, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderStartTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StartTraffic;
        String rpcName = convertActionNameToUrl(action.name());

        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringStartTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StartTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringStartTraffic, jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderStatusTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StatusTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringStatusTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StatusTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringStatusTraffic, jsonStr);
    }
  
    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringLicenseManagementTest()
            throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.LicenseManagement;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringLicenseManagement,jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderLicenseManagementTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.LicenseManagement;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
    }
   @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringProvisioningTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Provisioning;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringProvisioning,jsonStr);
    }

    @Test
    public void convAsyncResponseToBuilderProvisioningTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.Provisioning;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
    }
    @Test
    public void convAsyncResponseToBuilderStopTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StopTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
        Assert.assertEquals(expectedJsonBodyStrwithPayload, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringStopTrafficTest() throws JsonProcessingException {
        ResponseContext asyncResponse = buildAsyncResponsewithPayload();
        VNFOperation action = VNFOperation.StopTraffic;
        String rpcName = convertActionNameToUrl(action.name());
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringStopTraffic, jsonStr);
    }

    /*@Test
    public void convAsyncResponseToBuilderTest() throws JsonProcessingException {
        AsyncResponse asyncResponse = buildAsyncResponse();
        String jsonStr = Converter.convAsyncResponseToJsonStringBody(asyncResponse);
        Assert.assertEquals(expectedJsonBodyStr, jsonStr);
    }

    @Test
    public void convAsyncResponseToDmaapOutgoingMessageJsonStringTest() throws JsonProcessingException {
        AsyncResponse asyncResponse = buildAsyncResponse();
        String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(asyncResponse);
        System.out.println("jsonStr = " + jsonStr);
        Assert.assertEquals(expectedDmaapOutgoingMessageJsonString, jsonStr);
    }*/


    private ResponseContext buildAsyncResponse() {
        ResponseContext asyncResponse = createResponseContextWithSubObjects();
        asyncResponse.getStatus().setCode(LCMCommandStatus.SUCCESS.getResponseCode());
        asyncResponse.getStatus().setMessage(LCMCommandStatus.SUCCESS.getResponseMessage());
        asyncResponse.getCommonHeader().setOriginatorId("oid");
        asyncResponse.getCommonHeader().setApiVer("2.0.0");
        asyncResponse.getCommonHeader().setRequestId("reqid");
        asyncResponse.getCommonHeader().setTimestamp(new Date(1000L));
        asyncResponse.setPayload(
                "any valid JSON string value. Json escape characters need to be added to make it a valid json string value");
        return asyncResponse;
    }

    private ResponseContext buildAsyncResponsewithPayload() {
        ResponseContext asyncResponse = createResponseContextWithSubObjects();
        asyncResponse.getStatus().setCode(LCMCommandStatus.SUCCESS.getResponseCode());
        asyncResponse.getStatus().setMessage(LCMCommandStatus.SUCCESS.getResponseMessage());
        asyncResponse.getCommonHeader().setOriginatorId("oid");
        asyncResponse.getCommonHeader().setApiVer("2.0.0");
        asyncResponse.getCommonHeader().setRequestId("reqid");
        asyncResponse.getCommonHeader().setTimestamp(new Date(1000L));
        asyncResponse.setPayload("{}");
        return asyncResponse;
    }

    private ResponseContext createResponseContextWithSubObjects() {

        ResponseContext responseContext = new ResponseContext();
        CommonHeader commonHeader = new CommonHeader();
        Flags flags = new Flags();
        Status status = new Status();
        responseContext.setCommonHeader(commonHeader);
        responseContext.setStatus(status);
        commonHeader.setFlags(flags);
        return responseContext;
    }

    private String convertActionNameToUrl(String action) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return action.replaceAll(regex, replacement)
                .toLowerCase();
    }


}
