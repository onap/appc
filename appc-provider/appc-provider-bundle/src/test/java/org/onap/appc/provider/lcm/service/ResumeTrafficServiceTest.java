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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ResumeTrafficService.class, RequestExecutor.class})
public class ResumeTrafficServiceTest {
    private final Action myAction = Action.ResumeTraffic;

    private ResumeTrafficInput mockInput = mock(ResumeTrafficInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);

    private ResumeTrafficService resumeServiceAction;
    @Before
    public void setUp() throws Exception {
        resumeServiceAction = spy(new ResumeTrafficService());
    }


    @Test
    public void testConstructor() throws Exception {
        Action expectedAction = Action.ResumeTraffic;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
                (Action) org.powermock.reflect.Whitebox.getInternalState(resumeServiceAction, "expectedAction"));
        Assert.assertEquals("Should have resume-traffic RPC name", "resume-traffic",
                (org.powermock.reflect.Whitebox.getInternalState(resumeServiceAction, "rpcName")).toString());
    }

//    @Test
//    public void testProcess() throws Exception {
//        // test error occurs in validation
//        ResumeTrafficOutputBuilder outputBuilder = resumeServiceAction.process(mockInput);
//        Mockito.verify(resumeServiceAction, times(0)).proceedAction(any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//            outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
//
//        // to make validation pass
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(myAction).when(mockInput).getAction();
//        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test processAction return with error
//        outputBuilder = resumeServiceAction.process(mockInput);
//        Mockito.verify(resumeServiceAction, times(1)).proceedAction(any());
//        Assert.assertTrue("Should have commonHeader",outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//            Integer.valueOf(LCMCommandStatus.REJECTED.getResponseCode()),
//            outputBuilder.getStatus().getCode());
//
//        // test processAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = resumeServiceAction.process(mockInput);
//        Assert.assertTrue("Should have commonHeader",outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status", successCode, outputBuilder.getStatus().getCode());
//    }

    @Test
    public void testValidate() throws Exception {
        resumeServiceAction.validate(mockCommonHeader, Action.ResumeTraffic, mockAI);
        Status status = (Status) Whitebox.getInternalState(resumeServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(resumeServiceAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(resumeServiceAction, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test empty action
        resumeServiceAction.validate(mockCommonHeader, Action.ResumeTraffic, mockAI);
        status = (Status) Whitebox.getInternalState(resumeServiceAction, "status");
        Assert.assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty ActionIdentifier
        resumeServiceAction.validate(mockCommonHeader, Action.ResumeTraffic, mockAI);
        status = (Status) Whitebox.getInternalState(resumeServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test Invalid VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        resumeServiceAction.validate(mockCommonHeader, Action.ResumeTraffic, mockAI);
        status = (Status) Whitebox.getInternalState(resumeServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }

}
