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

package org.onap.appc.requesthandler;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.adapter.factory.DmaapMessageAdapterFactoryImpl;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.VNFContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.executor.CommandExecutor;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.lifecyclemanager.LifecycleManager;
import org.onap.appc.lifecyclemanager.objects.LifecycleException;
import org.onap.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.messageadapter.MessageAdapter;
import org.onap.appc.messageadapter.impl.MessageAdapterImpl;
import org.onap.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.MissingVNFDataInAAIException;
import org.onap.appc.requesthandler.exceptions.RequestExpiredException;
import org.onap.appc.requesthandler.exceptions.VNFNotFoundException;
import org.onap.appc.requesthandler.exceptions.WorkflowNotFoundException;
import org.onap.appc.requesthandler.impl.RequestHandlerImpl;
import org.onap.appc.requesthandler.impl.RequestValidatorImpl;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {FrameworkUtil.class, TransactionRecorder.class, RequestHandlerImpl.class,RequestValidatorImpl.class, TransactionRecorder.class, MessageAdapterImpl.class})
public class RequestHandlerTest {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerTest.class);

    private RequestHandlerImpl requestHandler;
    private RequestValidatorImpl requestValidator;
    private WorkFlowManager workflowManager;
    private LockManager lockManager;
    private Configuration configuration;

    private final BundleContext bundleContext=Mockito.mock(BundleContext.class);
    private final Bundle bundleService=Mockito.mock(Bundle.class);
    private final ServiceReference sref=Mockito.mock(ServiceReference.class);
    private MessageAdapterFactory factory = new DmaapMessageAdapterFactoryImpl();

    
    @Before
    public void init() throws Exception {
        configuration = ConfigurationFactory.getConfiguration();
                
        configuration.setProperty("appc.LCM.topic.write" , "TEST");
        configuration.setProperty("appc.LCM.client.key" , "TEST");
        configuration.setProperty("appc.LCM.client.secret" , "TEST");
        
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(MessageAdapterImpl.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(MessageAdapterFactory.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(factory);
        
    
        requestHandler = new RequestHandlerImpl();
        LifecycleManager lifecyclemanager= mock(LifecycleManager.class);
        workflowManager= mock(WorkFlowManager.class);

        CommandExecutor commandExecutor= mock(CommandExecutor.class);
        MessageAdapter messageAdapter = mock(MessageAdapter.class);
        lockManager = mock(LockManager.class);
        TransactionRecorder transactionRecorder= mock(TransactionRecorder.class);

        requestHandler.setMessageAdapter(messageAdapter);
        requestValidator = mock(RequestValidatorImpl.class);
        requestValidator.setWorkflowManager(workflowManager);
        requestHandler.setCommandExecutor(commandExecutor);
        requestHandler.setRequestValidator(requestValidator);
        requestHandler.setLockManager(lockManager);
        requestHandler.setTransactionRecorder(transactionRecorder);

        doNothing().when(transactionRecorder).store(anyObject());
//        Mockito.when(commandExecutor.executeCommand((CommandExecutorInput)anyObject())).thenReturn(true);
    }

    private void threadSleep(){
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //TODO needs to be fixed
    /*@Test
    public void testNegativeFlowWithRequestingUsedVnfId() throws Exception {
        logger.debug("=====================testNegativeFlowWithRequestingUsedVnfId=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input1 = this.getRequestHandlerInput("131", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);
        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        PowerMockito.doThrow(new LockException(" ")).when(lockManager).acquireLock(Matchers.anyString(), Matchers.anyString(), Matchers.anyByte());
        RequestHandlerOutput output1 = requestHandler.handleRequest(input1);
        threadSleep ();
        Assert.assertEquals(LCMCommandStatus.LOCKING_FAILURE.getResponseCode(), output1.getResponseContext().getStatus().getCode());
        logger.debug("testNegativeFlowWithRequestingUsedVnfId");
        logger.debug("=====================testNegativeFlowWithRequestingUsedVnfId=============================");
    }

    @Test
    public void testInvalidVNFExceptionRequest() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException,  DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure, 0,false,originatorID, requestID, subRequestID,new Date());
        ///PowerMockito.doThrow(new VNFNotFoundException(" ")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.VNF_NOT_FOUND.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testLifecycleException() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException,  DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.INVALID_VNF_STATE.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }


    @Test
    public void testRequestExpiredException() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.EXPIRED_REQUEST.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testMissingVNFdata() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException,  DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        *//*PowerMockito.doThrow(new MissingVNFDataInAAIException("vnf-type")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));*//*
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.MISSING_VNF_DATA_IN_AAI.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testWorkflowNotFoundException() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        //PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        //PowerMockito.doThrow(new WorkflowNotFoundException("Unable to find the DG","VNF-2.0.0.0", "Test")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.WORKFLOW_NOT_FOUND.getResponseCode(), output.getResponseContext().getStatus().getCode());}

    @Test
    public void testDGWorkflowNotFoundException() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure, 0, false, originatorID, requestID, subRequestID, new Date());
        PowerMockito.doThrow(new DGWorkflowNotFoundException("Unable to find the DG", "VNF-2.0.0.0", "temp", "Test","Test","Test")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.DG_WORKFLOW_NOT_FOUND.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testInvalidInputException() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID1 = UUID.randomUUID().toString();
        String requestID1 = UUID.randomUUID().toString();
        String subRequestID1 = UUID.randomUUID().toString();
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input1 = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID1, requestID1, subRequestID1,new Date());
        //PowerMockito.doThrow(new InvalidInputException(" ")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output1 = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode(), output1.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testNoTransitionDefinedException() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        //PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3010", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        ///PowerMockito.doThrow(new NoTransitionDefinedException("Invalid VNF State","Unstable","Test event")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.NO_TRANSITION_DEFINE.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void rejectInvalidRequest() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        ///PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("3009", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        ///PowerMockito.doThrow(new VNFNotFoundException(" ")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.VNF_NOT_FOUND.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testUnstableWorkingState() throws Exception {
        logger.debug("=====================testUnstableWorkingState=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("37", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        mockRuntimeContextAndVnfContext(input);
        RequestHandlerOutput output = requestHandler.handleRequest(input);

        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());

        RequestHandlerInput input1 = this.getRequestHandlerInput("37", VNFOperation.Configure,1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        PowerMockito.doThrow(new LockException(" ")).when(lockManager).acquireLock(Matchers.anyString(), Matchers.anyString(), Matchers.anyByte());
        mockRuntimeContextAndVnfContext(input1);
        RequestHandlerOutput output1 = requestHandler.handleRequest(input1);

        Assert.assertEquals(LCMCommandStatus.LOCKING_FAILURE.getResponseCode(), output1.getResponseContext().getStatus().getCode());
        logger.debug("=====================testUnstableWorkingState=============================");
    }

    @Test
    public void testOnRequestExecutionEndSuccessForWorkingState() throws Exception {
        logger.debug("=====================testOnRequestExecutionEndSuccessForWorkingState=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input1 = this.getRequestHandlerInput("137", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));


        RequestHandlerOutput output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        threadSleep();

        requestHandler.onRequestExecutionEnd(this.getAsyncResponse(true,LCMCommandStatus.SUCCESS,"137", "", "", ""));

        input1 = this.getRequestHandlerInput("137", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);
        output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        logger.debug("=====================testOnRequestExecutionEndSuccessForWorkingState=============================");
    }

    private void mockRuntimeContextAndVnfContext(RequestHandlerInput input1) throws Exception {
        RuntimeContext runtimeContext = PowerMockito.mock(RuntimeContext.class);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setType("SCP");
        vnfContext.setId("137");
        when(runtimeContext.getVnfContext()).thenReturn(vnfContext);
        when(runtimeContext.getRequestContext()).thenReturn(input1.getRequestContext());
        when(runtimeContext.getRpcName()).thenReturn(input1.getRpcName());
        Date startTime = new Date();
        when(runtimeContext.getTimeStart()).thenReturn(startTime.toInstant());


        ResponseContext responseContext = new ResponseContext();
        responseContext.setStatus(new Status());
        responseContext.setAdditionalContext(new HashMap<>(4));
        responseContext.setCommonHeader(input1.getRequestContext().getCommonHeader());
        runtimeContext.setResponseContext(responseContext);
        when(runtimeContext.getResponseContext()).thenReturn(responseContext);
        responseContext.setStatus(new Status());
        runtimeContext.setResponseContext(responseContext);
        PowerMockito.whenNew(RuntimeContext.class).withAnyArguments().thenReturn(runtimeContext);

    }

    @Test
    public void testOnRequestExecutionEndFailureForWorkingState() throws Exception {
        logger.debug("=====================testOnRequestExecutionEndFailureForWorkingState=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));

        RuntimeContext noneMockRuntimeContext = this.getAsyncResponse(false,LCMCommandStatus.NO_TRANSITION_DEFINE,"38", "", "", "");

        RequestHandlerInput input1 = this.getRequestHandlerInput("38", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);
        RequestHandlerOutput output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        threadSleep();
        requestHandler.onRequestExecutionEnd(noneMockRuntimeContext);

        input1 = this.getRequestHandlerInput("38", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);
        output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.UNSTABLE_VNF.getResponseCode(),output.getResponseContext().getStatus().getCode());

        logger.debug("=====================testOnRequestExecutionEndFailureForWorkingState=============================");
    }

    @Test
    public void testOnRequestExecutionEndTTLExpiredForWorkingState() throws Exception {
        logger.debug("=====================testOnRequestExecutionEndFailureForWorkingState=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));

        RequestHandlerInput input1 = this.getRequestHandlerInput("39", VNFOperation.Configure, 1,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        mockRuntimeContextAndVnfContext(input1);

        RequestHandlerOutput output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        threadSleep();
        input1 = this.getRequestHandlerInput("39", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        PowerMockito.doThrow(new LockException(" ")).when(lockManager).acquireLock(Matchers.anyString(), Matchers.anyString(), Matchers.anyByte());
        output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.LOCKING_FAILURE.getResponseCode(),output.getResponseContext().getStatus().getCode());
        logger.debug("=====================testOnRequestExecutionEndFailureForWorkingState=============================");
    }

    @Test
    public void testOnRequestTTLEndForWorkingState() throws Exception {
        logger.debug("=====================testOnRequestTTLEndForWorkingState=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));

        RequestHandlerInput input1 = this.getRequestHandlerInput("40", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);
        RequestHandlerOutput output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        threadSleep();
        RuntimeContext response = this.getAsyncResponse(false,LCMCommandStatus.EXPIRED_REQUEST_FAILURE,"40", "", "", "");
        requestHandler.onRequestTTLEnd(response);
        input1 = this.getRequestHandlerInput("40", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.UNSTABLE_VNF.getResponseCode(),output.getResponseContext().getStatus().getCode());
        logger.debug("=====================testOnRequestTTLEndForWorkingState=============================");
    }

    @Test
    public void testForceCommandExecution() throws Exception {
        logger.debug("=====================testForceCommandExecution=============================");
        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input1 = this.getRequestHandlerInput("138", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        mockRuntimeContextAndVnfContext(input1);

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        RequestHandlerOutput output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        threadSleep();
        RuntimeContext response = this.getAsyncResponse(false,LCMCommandStatus.ACCEPTED,"138", "", "", "");
        requestHandler.onRequestTTLEnd(response);
        input1 = this.getRequestHandlerInput("138", VNFOperation.Configure, 1200,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),new Date());
        input1.getRequestContext().getCommonHeader().getFlags().setForce(true);
        mockRuntimeContextAndVnfContext(input1);
        output = requestHandler.handleRequest(input1);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(),output.getResponseContext().getStatus().getCode());
        logger.debug("=====================testForceCommandExecution=============================");
    }

    @Test
    public void testOnRequestExecutionEndSuccess() throws VNFNotFoundException {
        logger.debug("=====================Positive TEST - On Request Execution End SUCCESS- Starts =============================");
        requestHandler.onRequestExecutionEnd(this.getAsyncResponse(true,LCMCommandStatus.SUCCESS,"201", "", "", ""));
        logger.debug("=====================Positive TEST - On Request Execution End SUCCESS- Ends =============================");
    }

    @Test
    public void testOnRequestExecutionEndFailure() throws  VNFNotFoundException {
        logger.debug("=====================Positive TEST - On Request Execution End FAILURE- Starts =============================");
        requestHandler.onRequestExecutionEnd(this.getAsyncResponse(false,LCMCommandStatus.DG_FAILURE,"202", "", "", ""));
        logger.debug("=====================Positive TEST - On Request Execution End FAILURE- Ends =============================");
    }

    private RequestHandlerInput getRequestHandlerInput(String vnfID, VNFOperation action, int ttl, boolean force,String originatorId, String requestId, String subRequestId,Date timeStamp){
        String API_VERSION= "2.0.0";
        RequestHandlerInput input = new RequestHandlerInput();
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        RequestContext requestContext = runtimeContext.getRequestContext();
        input.setRequestContext(requestContext);
        requestContext.getActionIdentifiers().setVnfId(vnfID);
        requestContext.setAction(action);
        input.setRpcName(convertActionNameToUrl(action.name()));
        requestContext.getCommonHeader().setRequestId(requestId);
        requestContext.getCommonHeader().setSubRequestId(subRequestId);
        requestContext.getCommonHeader().setOriginatorId(originatorId);
        requestContext.getCommonHeader().getFlags().setTtl(ttl);
        requestContext.getCommonHeader().getFlags().setForce(force);
        requestContext.getCommonHeader().setTimestamp(timeStamp);
        requestContext.getCommonHeader().setApiVer(API_VERSION);
        return input;
    }

    private RuntimeContext getAsyncResponse(boolean wfStatus, LCMCommandStatus commandStatus, String vnfId, String originatorId, String requestId, String subRequestId)
    {
        RuntimeContext output = createRuntimeContextWithSubObjects();


        output.getRequestContext().getActionIdentifiers().setVnfId(vnfId);
        output.getVnfContext().setId(vnfId);
        output.getResponseContext().getCommonHeader().setApiVer("2.0.0");
        output.getResponseContext().getCommonHeader().setTimestamp(new Date());
        output.getResponseContext().getStatus().setCode(LCMCommandStatus.SUCCESS.getResponseCode());
        output.setTimeStart(new Date().toInstant());
        output.getResponseContext().getCommonHeader().setOriginatorId(originatorId);
        output.getResponseContext().getCommonHeader().setRequestId(requestId);
        output.getResponseContext().getCommonHeader().setSubRequestId(subRequestId);

        output.getVnfContext().setType("FIREWALL");
        output.getRequestContext().setAction(VNFOperation.Configure);
        output.setRpcName("configure");
        output.getResponseContext().setPayload("");
        return output;
    }

    @Test
    public void rejectDuplicateRequest() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));

        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("301", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        mockRuntimeContextAndVnfContext(input);

        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());

        input = this.getRequestHandlerInput("309", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());

        PowerMockito.doThrow(new DuplicateRequestException(" ")).when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));
        output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.DUPLICATE_REQUEST.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void removeRequestFromRegistryOnRequestCompletion() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));

        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("302", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        mockRuntimeContextAndVnfContext(input);

        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());

        RuntimeContext asyncResponse = this.getAsyncResponse(true,LCMCommandStatus.SUCCESS,"302",originatorID,requestID,subRequestID);
        requestHandler.onRequestExecutionEnd(asyncResponse);

        input = this.getRequestHandlerInput("310", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        mockRuntimeContextAndVnfContext(input);
        output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void removeRequestFromRegistryOnTTLExpiration() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        PowerMockito.doNothing().when(requestValidator).validateRequest(Matchers.any(RuntimeContext.class));

        Mockito.when(workflowManager.workflowExists(anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("303", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID,new Date());
        mockRuntimeContextAndVnfContext(input);
        RequestHandlerOutput output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());

        RuntimeContext asyncResponse = this.getAsyncResponse(true,LCMCommandStatus.ACCEPTED,"303",originatorID,requestID,subRequestID);
        requestHandler.onRequestTTLEnd(asyncResponse);

        output = requestHandler.handleRequest(input);
        Assert.assertEquals(LCMCommandStatus.ACCEPTED.getResponseCode(), output.getResponseContext().getStatus().getCode());
    }*/

    /*@Test
    public void getMetricserviceTest() throws Exception{
        Method method = RequestHandlerImpl.class.getDeclaredMethod("getMetricservice", null);
        method.setAccessible(true);
        method.invoke(null, null);

    }*/
    @Test
    public void onRequestExecutionStartTest() throws  Exception{
                requestHandler.onRequestExecutionStart("303",false,  true);
        Assert.assertNotNull(requestHandler);
    }


    private RuntimeContext createRuntimeContextWithSubObjects() {
        RuntimeContext runtimeContext = new RuntimeContext();
        RequestContext requestContext = new RequestContext();
        runtimeContext.setRequestContext(requestContext);
        ResponseContext responseContext = createResponseContextWithSuObjects();
        runtimeContext.setResponseContext(responseContext);
        CommonHeader commonHeader = new CommonHeader();
        requestContext.setCommonHeader(commonHeader);
        Flags flags = new Flags();
        commonHeader.setFlags(flags);
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        requestContext.setActionIdentifiers(actionIdentifiers);
        VNFContext vnfContext = new VNFContext();
        runtimeContext.setVnfContext(vnfContext);
        return runtimeContext;

    }

    private ResponseContext createResponseContextWithSuObjects(){
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

