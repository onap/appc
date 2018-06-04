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

package org.onap.appc.dg.common.impl;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Map;
import org.onap.appc.dg.common.OutputMessagePlugin;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class OutputMessagePluginImpl implements OutputMessagePlugin {

    @Override
    public void outputMessageBuilder(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String errorDescription;
        String eventDescription;

        //making output.status.message
        errorDescription = params.get(Constants.ATTRIBUTE_ERROR_MESSAGE);
        eventDescription = params.get(Constants.EVENT_MESSAGE);

        addToContextIfNotContains(errorDescription, eventDescription, ctx);

        //making event-message

        if (!isEmpty(eventDescription)) {
            ctx.setAttribute(Constants.EVENT_MESSAGE, eventDescription);
        } else {
            ctx.setAttribute(Constants.EVENT_MESSAGE, ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }
    }

    private static void addToContextIfNotContains(String errorDescription, String eventDescription,
        SvcLogicContext ctx) {
        if (!isEmpty(errorDescription)) {
            updateContext(errorDescription, ctx);
        }
        if (!isEmpty(eventDescription)) {
            updateContext(eventDescription, ctx);
        }
    }

    private static void updateContext(String eventDescription, SvcLogicContext ctx) {
        if (isEmpty(ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE))) {
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, eventDescription);
        } else if (!ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE).contains(eventDescription)) {
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE,
                ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE) + " | " + eventDescription);
        }
    }
}
