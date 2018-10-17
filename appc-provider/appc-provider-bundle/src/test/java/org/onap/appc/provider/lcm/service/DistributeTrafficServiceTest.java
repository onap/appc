/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Orange
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

package org.onap.appc.provider.lcm.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(MockitoJUnitRunner.class)
public class DistributeTrafficServiceTest {

    private final Action myAction = Action.DistributeTraffic;
    private final String rpcName = "distribute-traffic";
    private  String PAYLOAD_STRING = "{\"ConfigFileName\":\"test\"}";

    private final DistributeTrafficInput mockInput = mock(DistributeTrafficInput.class);
    private final CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private final ActionIdentifiers mockActionIdentifiers = mock(ActionIdentifiers.class);
    private final Payload mockPayload = mock(Payload.class);

    private DistributeTrafficService distributeTrafficService;

    @Before
    public void setUp() throws Exception {
        distributeTrafficService = spy(new DistributeTrafficService());
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals("Should have proper ACTION", myAction,
                (Action) Whitebox.getInternalState(distributeTrafficService, "expectedAction"));
        assertEquals("Should have action-status RPC name", rpcName,
                Whitebox.getInternalState(distributeTrafficService, "rpcName").toString());
    }

    private void helpInitializeRequestParameters() {
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(myAction).when(mockInput).getAction();
        Mockito.doReturn(mockActionIdentifiers).when(mockInput).getActionIdentifiers();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
        Mockito.doReturn("vnfId").when(mockActionIdentifiers).getVnfId();
    }

    @Test
    public void testProcess() throws Exception {

        helpInitializeRequestParameters();

        // test processAction return without error
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);

        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());

        ResponseContext mockResponseContext = mock(ResponseContext.class);
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();

        Mockito.when(distributeTrafficService.executeAction(any())).thenReturn(mockOutput);

        DistributeTrafficOutputBuilder  outputBuilder = distributeTrafficService.process(mockInput);

        Mockito.verify(distributeTrafficService, times(1)).proceedAction(mockInput);
        assertNotNull("Should have commonHeader", outputBuilder.getCommonHeader());
    }

    @Test
    public void testValidateMissingParameters() throws Exception {
        DistributeTrafficOutputBuilder outputBuilder = distributeTrafficService.process(mockInput);
        Mockito.verify(distributeTrafficService, times(0)).proceedAction(any());
        assertNull("Should not have commonHeader as we did not mock it", outputBuilder.getCommonHeader());
        assertEquals("should return missing parameter status",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                outputBuilder.getStatus().getCode());
    }

    @Test
    public void testValidateForMissingOrInvalidAction() throws Exception {
        helpInitializeRequestParameters();

        // check missing Action
        Mockito.doReturn(null).when(mockInput).getAction();
        distributeTrafficService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficService, "status");
        assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // check invalid Action
        Mockito.doReturn(Action.Migrate).when(mockInput).getAction();

        distributeTrafficService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(distributeTrafficService, "status");
        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateForMissingActionIdentifiers() throws Exception {

        helpInitializeRequestParameters();
        Mockito.doReturn(null).when(mockInput).getActionIdentifiers();

        // test missing ActionIdentifiers
        distributeTrafficService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficService, "status");
        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateEmptyOrMissingPayload() throws Exception {
        helpInitializeRequestParameters();

        // validate empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        distributeTrafficService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficService, "status");
        assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // validate missing payload
        Mockito.doReturn(null).when(mockInput).getPayload();
        distributeTrafficService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(distributeTrafficService, "status");

        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

    }

}
