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

package org.onap.appc.statemachine.objects;

/**
 * Transition Object
 */
public class Transition {
    private Event event;
    private State nextState;

    /**
     * Constructor
     * @param event which triggers the transition
     * @param nextState of State which indicates the transition targeted state
     */
    Transition(Event event, State nextState) {
        this.event = event;
        this.nextState = nextState;
    }

    public Event getEvent() {
        return event;
    }

    public State getNextState() {
        return nextState;
    }
}
