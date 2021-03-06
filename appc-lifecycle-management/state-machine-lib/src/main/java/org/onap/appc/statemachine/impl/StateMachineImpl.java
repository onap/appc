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

package org.onap.appc.statemachine.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.statemachine.StateMachine;
import org.onap.appc.statemachine.objects.Event;
import org.onap.appc.statemachine.objects.Response;
import org.onap.appc.statemachine.objects.State;
import org.onap.appc.statemachine.objects.StateMachineMetadata;
import org.onap.appc.statemachine.objects.StateMachineResponse;
import org.onap.appc.statemachine.objects.Transition;

/**
 * Implementation of StateMachine
 */
public class StateMachineImpl implements StateMachine {
    private static final String INVALID_INPUT_FORMAT = "VNF State or incoming event is invalid. State = %s event = %s";
    static final String TO_STRING_FORMAT = "StateMachineImpl{states=%s, events=%s}";

    private final Set<State> states;
    private final Set<Event> events;

    StateMachineImpl(StateMachineMetadata metadata){
        this.states = new HashSet<>();
        this.states.addAll(metadata.getStates());
        this.events = new HashSet<>();
        this.events.addAll(metadata.getEvents());
    }

    @Override
    public StateMachineResponse handleEvent(State inputState, Event event) throws InvalidInputException{

        if(!validateInputs(inputState,event)){
            throw new InvalidInputException(String.format(INVALID_INPUT_FORMAT, inputState, event));
        }

        StateMachineResponse response = new StateMachineResponse();
        State currentState = null;
        State nextState = null;
        for(State stateInSet : states){
            if(stateInSet.equals(inputState)){
                currentState = stateInSet;
                break;
            }
        }
        if (currentState != null) {
            List<Transition> transitions = currentState.getTransitions();
            if (transitions != null) {
            for (Transition transition : currentState.getTransitions()) {
                if (event.equals(transition.getEvent())) {
                    nextState = transition.getNextState();
                }
            }
        }
      }
        if(nextState == null){
            response.setResponse(Response.NO_TRANSITION_DEFINED);
        }
        else if(inputState.equals(nextState)){
            response.setResponse(Response.NO_STATE_CHANGE);
        }
        else{
            response.setResponse(Response.VALID_TRANSITION);
        }
        response.setNextState(nextState);
        return response;
    }

    boolean validateInputs(State state,Event event) {
        return state != null
                && event != null
                && this.states.contains(state)
                && this.events.contains(event);
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, states, events);
    }
}
