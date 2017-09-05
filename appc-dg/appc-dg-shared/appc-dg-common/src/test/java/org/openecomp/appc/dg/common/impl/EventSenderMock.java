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

package org.openecomp.appc.dg.common.impl;

import java.util.Map;

import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


public class EventSenderMock implements EventSender {
    EventMessage msg;
    MessageDestination destination;

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg) {
        if (destination != null && msg != null){
            this.msg = msg;
            this.destination = destination;
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg, String eventTopicName) {
        return false;
    }

    @Override
    public boolean sendEvent(MessageDestination destination, Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        return false;
    }

    public EventMessage getMsg() {
        return msg;
    }

    public MessageDestination getDestination() {
        return destination;
    }
}
