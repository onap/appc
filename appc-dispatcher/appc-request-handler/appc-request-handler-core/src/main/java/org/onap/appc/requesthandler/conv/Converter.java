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

package org.onap.appc.requesthandler.conv;

import org.apache.commons.lang3.StringUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.FlagsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.StatusBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.requesthandler.impl.AbstractRequestHandlerImpl;
import org.onap.appc.requesthandler.impl.DmaapOutgoingMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class Converter {
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final static String DMaaP_ROOT_VALUE = "output";
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
    private final static EELFLogger logger = EELFManager.getInstance().getLogger(Converter.class);
    static {
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Builder<?> convAsyncResponseToBuilder(
            VNFOperation vnfOperation, String rpcName, ResponseContext response) {
        Builder<?> outObj = null;
        if (response == null) {
            throw new IllegalArgumentException("empty asyncResponse");
        }
        if (vnfOperation == null) {
            throw new IllegalArgumentException("empty asyncResponse.action");
        }
        logger.debug("Entered Converter.convAsyncResponseToBuilder(): Operation Name " + vnfOperation.name());
        org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action action =
                org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action.valueOf(vnfOperation.name());
        logger.debug("After resolving action");
        CommonHeader commonHeader = convAsyncResponseTorev160108CommonHeader(response);
        Status status = convAsyncResponseTorev160108Status(response);
        Payload payload = convAsyncResponseTorev160108Payload(response);
        logger.debug("Extracted action, status, payload");
        switch (action) {
            case Rollback:
                outObj = new RollbackOutputBuilder();
                ((RollbackOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RollbackOutputBuilder)outObj).setStatus(status);
                ((RollbackOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case Snapshot:
                outObj = new SnapshotOutputBuilder();
                ((SnapshotOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SnapshotOutputBuilder)outObj).setStatus(status);
                try {
                    ((SnapshotOutputBuilder) outObj)
                            .setSnapshotId(response.getAdditionalContext().get("output.snapshot-id"));
                } catch (NullPointerException ignored) {
                    // in case of negative response, snapshotID is not populated, so just ignore NPL
                }
                return outObj;
            case Audit:
                outObj = new AuditOutputBuilder();
                ((AuditOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((AuditOutputBuilder)outObj).setStatus(status);
                ((AuditOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case HealthCheck:
                outObj = new HealthCheckOutputBuilder();
                ((HealthCheckOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((HealthCheckOutputBuilder)outObj).setStatus(status);
                ((HealthCheckOutputBuilder)outObj).setPayload(payload);
                logger.debug("In HealthCheck case: created outObj, returning");
                return outObj;
            case LiveUpgrade:
                outObj = new LiveUpgradeOutputBuilder();
                ((LiveUpgradeOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((LiveUpgradeOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Lock:
                outObj = new LockOutputBuilder();
                ((LockOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((LockOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Configure:
                outObj = new ConfigureOutputBuilder();
                ((ConfigureOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigureOutputBuilder)outObj).setStatus(status);
                ((ConfigureOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigModify:
                outObj = new ConfigModifyOutputBuilder();
                ((ConfigModifyOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigModifyOutputBuilder)outObj).setStatus(status);
                ((ConfigModifyOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigScaleOut:
                outObj = new ConfigScaleOutOutputBuilder();
                ((ConfigScaleOutOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigScaleOutOutputBuilder)outObj).setStatus(status);
                ((ConfigScaleOutOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigRestore:
                outObj = new ConfigRestoreOutputBuilder();
                ((ConfigRestoreOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigRestoreOutputBuilder)outObj).setStatus(status);
                ((ConfigRestoreOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case SoftwareUpload:
                outObj = new SoftwareUploadOutputBuilder();
                ((SoftwareUploadOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SoftwareUploadOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Stop:
                outObj = new StopOutputBuilder();
                ((StopOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StopOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Sync:
                outObj = new SyncOutputBuilder();
                ((SyncOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SyncOutputBuilder)outObj).setStatus(status);
                ((SyncOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case Terminate:
                outObj = new TerminateOutputBuilder();
                ((TerminateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((TerminateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Test:
                outObj = new TestOutputBuilder();
                ((TestOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((TestOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Unlock:
                outObj = new UnlockOutputBuilder();
                ((UnlockOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UnlockOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Restart:
                outObj = new RestartOutputBuilder();
                ((RestartOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RestartOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Rebuild:
                outObj = new RebuildOutputBuilder();
                ((RebuildOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RebuildOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Reboot:
                outObj = new RebootOutputBuilder();
                ((RebootOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RebootOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Migrate:
                outObj = new MigrateOutputBuilder();
                ((MigrateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((MigrateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Evacuate:
                outObj = new EvacuateOutputBuilder();
                ((EvacuateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((EvacuateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case ConfigBackup:
                outObj = new ConfigBackupOutputBuilder();
                ((ConfigBackupOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigBackupOutputBuilder)outObj).setStatus(status);
                ((ConfigBackupOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigBackupDelete:
                outObj = new ConfigBackupDeleteOutputBuilder();
                ((ConfigBackupDeleteOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigBackupDeleteOutputBuilder)outObj).setStatus(status);
                ((ConfigBackupDeleteOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigExport:
                outObj = new ConfigExportOutputBuilder();
                ((ConfigExportOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigExportOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Start:
                outObj = new StartOutputBuilder();
                ((StartOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StartOutputBuilder)outObj).setStatus(status);
                return outObj;
            case StopApplication:
                outObj = new StopApplicationOutputBuilder();
                ((StopApplicationOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StopApplicationOutputBuilder)outObj).setStatus(status);
                ((StopApplicationOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case StartApplication:
                outObj = new StartApplicationOutputBuilder();
                ((StartApplicationOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StartApplicationOutputBuilder)outObj).setStatus(status);
                ((StartApplicationOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case QuiesceTraffic:
                outObj = new QuiesceTrafficOutputBuilder();
                ((QuiesceTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((QuiesceTrafficOutputBuilder)outObj).setStatus(status);
                ((QuiesceTrafficOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ResumeTraffic:
                outObj = new ResumeTrafficOutputBuilder();
                ((ResumeTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ResumeTrafficOutputBuilder)outObj).setStatus(status);
                ((ResumeTrafficOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case UpgradeSoftware:
                outObj = new UpgradeSoftwareOutputBuilder();
                ((UpgradeSoftwareOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UpgradeSoftwareOutputBuilder)outObj).setStatus(status);
                ((UpgradeSoftwareOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case UpgradePostCheck:
                outObj = new UpgradePostCheckOutputBuilder();
                ((UpgradePostCheckOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UpgradePostCheckOutputBuilder)outObj).setStatus(status);
                ((UpgradePostCheckOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case UpgradeBackup:
                outObj = new UpgradeBackupOutputBuilder();
                ((UpgradeBackupOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UpgradeBackupOutputBuilder)outObj).setStatus(status);
                ((UpgradeBackupOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case UpgradeBackout:
                outObj = new UpgradeBackoutOutputBuilder();
                ((UpgradeBackoutOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UpgradeBackoutOutputBuilder)outObj).setStatus(status);
                ((UpgradeBackoutOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case UpgradePreCheck:
                outObj = new UpgradePreCheckOutputBuilder();
                ((UpgradePreCheckOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UpgradePreCheckOutputBuilder)outObj).setStatus(status);
                ((UpgradePreCheckOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case AttachVolume:
                outObj = new AttachVolumeOutputBuilder();
                ((AttachVolumeOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((AttachVolumeOutputBuilder)outObj).setStatus(status);
                return outObj;
            case DetachVolume:
                outObj = new DetachVolumeOutputBuilder();
                ((DetachVolumeOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((DetachVolumeOutputBuilder)outObj).setStatus(status);
                return outObj;
            case DistributeTraffic:
                outObj = new DistributeTrafficOutputBuilder();
                ((DistributeTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((DistributeTrafficOutputBuilder)outObj).setStatus(status);
                return outObj;
            case DistributeTrafficCheck:
                outObj = new DistributeTrafficCheckOutputBuilder();
                ((DistributeTrafficCheckOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((DistributeTrafficCheckOutputBuilder)outObj).setStatus(status);
                return outObj;
            case PreConfigure:
                outObj = new PreConfigureOutputBuilder();
                ((PreConfigureOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PreConfigureOutputBuilder)outObj).setStatus(status);
                ((PreConfigureOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case GetConfig:
                outObj = new GetConfigOutputBuilder();
                ((GetConfigOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((GetConfigOutputBuilder)outObj).setStatus(status);
                ((GetConfigOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PostEvacuate:
                outObj = new PostEvacuateOutputBuilder();
                ((PostEvacuateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PostEvacuateOutputBuilder)outObj).setStatus(status);
                ((PostEvacuateOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PreMigrate:
                outObj = new PreMigrateOutputBuilder();
                ((PreMigrateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PreMigrateOutputBuilder)outObj).setStatus(status);
                ((PreMigrateOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PostMigrate:
                outObj = new PostMigrateOutputBuilder();
                ((PostMigrateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PostMigrateOutputBuilder)outObj).setStatus(status);
                ((PostMigrateOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PreRebuild:
                outObj = new PreRebuildOutputBuilder();
                ((PreRebuildOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PreRebuildOutputBuilder)outObj).setStatus(status);
                ((PreRebuildOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case Provisioning:
                outObj = new ProvisioningOutputBuilder();
                ((ProvisioningOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ProvisioningOutputBuilder)outObj).setStatus(status);
                ((ProvisioningOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PostRebuild:
                outObj = new PostRebuildOutputBuilder();
                ((PostRebuildOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PostRebuildOutputBuilder)outObj).setStatus(status);
                ((PostRebuildOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case PreEvacuate:
                outObj = new PreEvacuateOutputBuilder();
                ((PreEvacuateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((PreEvacuateOutputBuilder)outObj).setStatus(status);
                ((PreEvacuateOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case StartTraffic:
                outObj = new StartTrafficOutputBuilder();
                ((StartTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StartTrafficOutputBuilder)outObj).setStatus(status);
                ((StartTrafficOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case StatusTraffic:
                outObj = new StatusTrafficOutputBuilder();
                ((StatusTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StatusTrafficOutputBuilder)outObj).setStatus(status);
                ((StatusTrafficOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case StopTraffic:
                outObj = new StopTrafficOutputBuilder();
                ((StopTrafficOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StopTrafficOutputBuilder)outObj).setStatus(status);
                ((StopTrafficOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case LicenseManagement:
                outObj = new LicenseManagementOutputBuilder();
                ((LicenseManagementOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((LicenseManagementOutputBuilder)outObj).setStatus(status);
                ((LicenseManagementOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case DownloadNESw:
                outObj = new DownloadNESwOutputBuilder();
                ((DownloadNESwOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((DownloadNESwOutputBuilder)outObj).setStatus(status);
                ((DownloadNESwOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ActivateNESw:
                outObj = new ActivateNESwOutputBuilder();
                ((ActivateNESwOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ActivateNESwOutputBuilder)outObj).setStatus(status);
                ((ActivateNESwOutputBuilder)outObj).setPayload(payload);
                return outObj;
            default:
                throw new IllegalArgumentException(action+" action is not supported");
        }
    }

    public static Payload convAsyncResponseTorev160108Payload(ResponseContext inObj) {
        logger.debug("Entering convAsyncResponseTorev160108Payload");
        Payload payload = null;
        if (inObj.getPayload() != null) {
            payload = new Payload(inObj.getPayload());
        }
        logger.debug("Exiting convAsyncResponseTorev160108Payload");
        return payload;
    }

    public static String convPayloadObjectToJsonString(Object inObj) throws ParseException {
        String payloadAsString = null;
        if (inObj != null) {

            if (inObj instanceof String) {
                payloadAsString = (String)inObj;
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    payloadAsString = objectMapper.writeValueAsString(inObj);
                } catch (JsonProcessingException e) {
                    String errMsg = "Error serialize payload json to string";
                    throw new ParseException(errMsg + "-" + e.toString(), 0);
                }
            }
        }
        return payloadAsString;
    }

    public static Status convAsyncResponseTorev160108Status(ResponseContext inObj) {
        logger.debug("Entering convAsyncResponseTorev160108Status");
        StatusBuilder statusBuilder = new StatusBuilder();
        statusBuilder.setCode(inObj.getStatus().getCode());
        statusBuilder.setMessage(inObj.getStatus().getMessage());
        logger.debug("Exiting convAsyncResponseTorev160108Status");
        return statusBuilder.build();
    }

    public static CommonHeader convAsyncResponseTorev160108CommonHeader(ResponseContext inObj) {
        logger.debug("Entered into convAsyncResponseTorev160108CommonHeader");
        CommonHeader outObj = null;
        if (inObj == null) {
            throw new IllegalArgumentException("empty asyncResponse");
        }

        CommonHeaderBuilder commonHeaderBuilder = new CommonHeaderBuilder();
        org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags commonHeaderFlags;
        if (inObj.getCommonHeader().getFlags() != null) {
            commonHeaderFlags = Converter.convFlagsMapTorev160108Flags(inObj.getCommonHeader().getFlags());
            commonHeaderBuilder.setFlags(commonHeaderFlags);
        }

        logger.debug("Before setApiVer");
        commonHeaderBuilder.setApiVer(inObj.getCommonHeader().getApiVer());
        commonHeaderBuilder.setRequestId(inObj.getCommonHeader().getRequestId());
        if (inObj.getCommonHeader().getSubRequestId() != null) {
            commonHeaderBuilder.setSubRequestId(inObj.getCommonHeader().getSubRequestId());
        }
        logger.debug("Before getOriginatorId");
        if (inObj.getCommonHeader().getOriginatorId() != null) {
            commonHeaderBuilder.setOriginatorId(inObj.getCommonHeader().getOriginatorId());
        }
        logger.debug("Before getTimeStamp");
        if (inObj.getCommonHeader().getTimeStamp() != null) {
            String zuluTimestampStr = Converter.convDateToZuluString(inObj.getCommonHeader().getTimeStamp());
            logger.debug("After invoking convDateToZuluString()");
            ZULU zuluTimestamp = new ZULU(zuluTimestampStr);
            logger.debug("After ZULU()");
            commonHeaderBuilder.setTimestamp(zuluTimestamp);
        }
        outObj = commonHeaderBuilder.build();
        logger.debug("Exiting from convAsyncResponseTorev160108CommonHeader: Returning outObj: " + outObj.toString());
        return outObj;

    }

    public static String convDateToZuluString(Date timeStamp) {
        return isoFormatter.format(timeStamp);
    }

    public static org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags
            convFlagsMapTorev160108Flags(org.onap.appc.domainmodel.lcm.Flags flags) {
        Flags rev160108flags;
        boolean anyFlag = false;
        FlagsBuilder flagsBuilder = new FlagsBuilder();
        /*
         * TODO: The below flags are related to APP-C request and should not be sent back -
         * uncomment when response flags are introduced.
         */
        /*
        if (flags.containsKey(FORCE_FLAG)) {
            org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags.Force force =
                    org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header
                        .Flags.Force.valueOf(flags.get(FORCE_FLAG).toString());
            flagsBuilder.setForce(force);
            anyFlag = true;
        }
        if (flags.containsKey(MODE_FLAG)) {
            org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags.Mode mode =
                    org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header
                        .Flags.Mode.valueOf(flags.get(MODE_FLAG).toString());
            flagsBuilder.setMode(mode);
            anyFlag = true;
        }
        if (flags.containsKey(TTL_FLAG)) {
            flagsBuilder.setTtl(Integer.valueOf(flags.get(TTL_FLAG).toString()));
            anyFlag = true;
        }
        if (anyFlag) {
            rev160108flags = flagsBuilder.build();
        }
         */

        rev160108flags = flagsBuilder.build();
        return rev160108flags;
    }

    public static String convAsyncResponseToJsonStringBody(
            VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse)
            throws JsonProcessingException {
        Builder<?> builder = Converter.convAsyncResponseToBuilder(vnfOperation, rpcName, asyncResponse);
        Object message = builder.build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(message.getClass(), MixInFlagsMessage.class);
        objectMapper.addMixIn(CommonHeader.class, MixInCommonHeader.class);
        objectMapper.addMixIn(Flags.class, MixIn.class);
        objectMapper.addMixIn(Status.class, MixIn.class);
        objectMapper.addMixIn(Payload.class, MixIn.class);
        objectMapper.addMixIn(ZULU.class, MixIn.class);

//                .configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true)
        ObjectWriter writer = objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .writer(SerializationFeature.WRAP_ROOT_VALUE)
                .withRootName(DMaaP_ROOT_VALUE)
                .withoutFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return writer.writeValueAsString(message);
    }

    public static String convAsyncResponseToDmaapOutgoingMessageJsonString(
            VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse)
            throws JsonProcessingException {
        logger.debug("Entered Converter.convAsyncResponseToDmaapOutgoingMessageJsonString()");
        DmaapOutgoingMessage dmaapOutgoingMessage =
                convAsyncResponseToDmaapOutgoingMessage(vnfOperation, rpcName, asyncResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(dmaapOutgoingMessage.getBody().getOutput().getClass(), MixInFlagsMessage.class);
        objectMapper.addMixIn(CommonHeader.class, MixInCommonHeader.class);
        objectMapper.addMixIn(Flags.class, MixIn.class);
        objectMapper.addMixIn(Status.class, MixIn.class);
        objectMapper.addMixIn(Payload.class, MixIn.class);
        objectMapper.addMixIn(ZULU.class, MixIn.class);

//                .configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true)
        ObjectWriter writer = objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .writer();
        logger.debug("Exiting Converter.convAsyncResponseToDmaapOutgoingMessageJsonString()");
        return writer.writeValueAsString(dmaapOutgoingMessage);
    }

    public static DmaapOutgoingMessage convAsyncResponseToDmaapOutgoingMessage(
            VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse)
            throws JsonProcessingException {
        logger.debug("Entered Converter.convAsyncResponseToDmaapOutgoingMessage()");
        DmaapOutgoingMessage outObj = new DmaapOutgoingMessage();
        String correlationID = getCorrelationID(asyncResponse);
        outObj.setCorrelationID(correlationID);
        outObj.setType("response");
        outObj.setRpcName(rpcName);
        logger.debug("In Converter.convAsyncResponseToDmaapOutgoingMessage()"
                + " before invoking convAsyncResponseToBuilder");
        Builder<?> builder = Converter.convAsyncResponseToBuilder(vnfOperation, rpcName, asyncResponse);
        Object messageBody = builder.build();
        DmaapOutgoingMessage.Body body = new DmaapOutgoingMessage.Body(messageBody);
        outObj.setBody(body);
        logger.debug("Exiting Converter.convAsyncResponseToDmaapOutgoingMessage(): messageBody is: " + body.toString());
        return outObj;
    }

    private static String getCorrelationID(ResponseContext context) {
        return context.getCommonHeader().getRequestId()
                + (context.getCommonHeader().getSubRequestId() == null ?
                    "" : "-" + context.getCommonHeader().getSubRequestId());
    }

    public static String getRpcName(String action) {
        String regex = "([a-zA-Z])(?=[A-Z])";
        String replacement = "$1-";
        return action.replaceAll(regex, replacement).toLowerCase();
    }

    abstract class MixIn {
        @JsonIgnore
        abstract Class<? extends DataContainer> getImplementedInterface(); // to be removed during serialization

        @JsonValue
        abstract java.lang.String getValue();
    }
    abstract class MixInCommonHeader extends MixIn {
        @JsonProperty("api-ver")
        abstract java.lang.String getApiVer();
        @JsonProperty("originator-id")
        abstract java.lang.String getOriginatorId();
        @JsonProperty("request-id")
        abstract java.lang.String getRequestId();
        @JsonProperty("sub-request-id")
        abstract java.lang.String getSubRequestId();

    }
    abstract class MixInFlagsMessage extends MixIn {
        @JsonProperty("common-header")
        abstract CommonHeader getCommonHeader();
    }
}
