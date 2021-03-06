/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.lifecyclemanager.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.lifecyclemanager.LifecycleManager;
import org.onap.appc.lifecyclemanager.objects.LifecycleException;
import org.onap.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.onap.appc.statemachine.StateMachine;
import org.onap.appc.statemachine.StateMetaDataReader;
import org.onap.appc.statemachine.impl.StateMachineFactory;
import org.onap.appc.statemachine.impl.readers.AppcOamMetaDataReader;
import org.onap.appc.statemachine.impl.readers.VnfMetaDataReader;
import org.onap.appc.statemachine.objects.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LifecycleManagerImpl implements LifecycleManager{

    private StateMetaDataReader metadataReader;
    private static Map<String,StateMachine> stateMachineMap = new ConcurrentHashMap<>();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(LifecycleManagerImpl.class);
    private final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();

    @Override
    public String getNextState(String vnfType, String currentState, String event) throws NoTransitionDefinedException,LifecycleException{
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getNextState with vnfType = "+ vnfType +    ", currentState = " + currentState + ", event = " + event);
        }

        State nextState;
        StateMachine machine;
        StateMachineResponse response;
        try {
            machine = this.getStateMachine(vnfType);
            response = machine.handleEvent(new State(currentState),new Event(event));
            if(Response.NO_TRANSITION_DEFINED.equals(response.getResponse())){
                errorLogger.error(EELFResourceManager.format(Msg.VF_ILLEGAL_COMMAND, vnfType,event,currentState));
                throw new NoTransitionDefinedException("No Transition Defined for currentState = " +  currentState + ", event = " + event,currentState,event);
            }
            nextState = response.getNextState();
        } catch (InvalidInputException e) {
            logger.error(e.getMessage());
            throw new LifecycleException(e,currentState,event);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getNextState with (nextState = " + nextState.getStateName() + ")");
        }
        return nextState.getStateName();
    }

    private StateMachine getStateMachine(String vnfType) {

        String vnfType1 = vnfType;

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getNextState with vnfType = "+ vnfType1);
        }
        if(vnfType1 == null){
            vnfType1 = "DEFAULT";
        }
        StateMachine machine = stateMachineMap.get(vnfType1);
        if(machine == null){
            metadataReader = getMetadataReader(vnfType1);
            StateMachineMetadata metadata = metadataReader.readMetadata();
            machine = StateMachineFactory.getStateMachine(metadata);
            stateMachineMap.put(vnfType1,machine);
        }

        logger.trace("Exiting getStateMachine with StateMachine = " + stateMachineMap.get(vnfType1).toString());
        return stateMachineMap.get(vnfType1);
    }

    private StateMetaDataReader getMetadataReader(String vnfType) {
        return vnfType.equals("APPC") ? new AppcOamMetaDataReader() : new VnfMetaDataReader();
    }

}
