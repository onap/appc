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

package org.onap.appc.oam.processor;

import com.att.eelf.configuration.EELFLogger;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.exceptions.InvalidStateException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.OAMCommandStatus;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.onap.appc.requesthandler.LCMStateManager;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;

import java.util.concurrent.TimeoutException;

/**
 * Processor to handle maintenance mode OAM API.
 */
public class OamMmodeProcessor extends BaseProcessor {
    /**
     * Constructor
     *
     * @param eelfLogger          for logging
     * @param configurationHelper for property reading
     * @param stateHelper         for APP-C OAM state checking
     * @param asyncTaskHelper     for scheduling async task
     * @param operationHelper     for operational helper
     */
    public OamMmodeProcessor(EELFLogger eelfLogger,
                             ConfigurationHelper configurationHelper,
                             StateHelper stateHelper,
                             AsyncTaskHelper asyncTaskHelper,
                             OperationHelper operationHelper) {
        super(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);

        rpc = AppcOam.RPC.maintenance_mode;
        auditMsg = Msg.OAM_OPERATION_ENTERING_MAINTENANCE_MODE;
    }

    @Override
    protected void preProcess(final Object requestInput)
            throws InvalidInputException, InvalidStateException, APPCException, InterruptedException, TimeoutException {
        super.preProcess(requestInput);

        //Close the gate so that no more new LCM request will be excepted.
        LCMStateManager lcmStateManager = operationHelper.getService(LCMStateManager.class);
        lcmStateManager.disableLCMOperations();
    }

    @Override
    protected void scheduleAsyncTask() {
        runnable = new MyRunnable(this);
        super.scheduleAsyncTask();
    }

    /**
     * {@inheritDoc}
     * For maintenance mode we want a longer delay before initial execution of {@link BaseActionRunnable}
     * so that any accepted LCM actions have time to git scheduled in the Dispatcher.
     */
    @Override
    protected long getInitialDelayMillis(){
        //wait ten seconds before
        return 10000L;
    }

    /**
     * This runnable does the async handling for the maintenance mode REST API, and will be scheduled to run
     * until terminating condition reaches.
     *
     * <p>The runnable will continue run if: <br>
     *   - the runnable is not canceled outside <br>
     *   - the in progress LCM request count is not zero<br>
     * <p> When LCM request count reaches to zero, this runnable will: <br>
     *     - post message through operationHelper <br>
     *     - set APP-C OAM state to maintenance mode <br>
     *     - audit log the state <br>
     *     - terminate this runnable itself <br>
     */
    class MyRunnable extends BaseActionRunnable {
        private int inprogressRequestCount;

        MyRunnable(BaseProcessor parent) {
            super(parent);

            actionName = "OAM Maintenance mode";
            auditMsg = Msg.OAM_OPERATION_MAINTENANCE_MODE;
            finalState = AppcOamStates.MaintenanceMode;
        }

        @Override
        boolean doAction() {
            // always return true, so that we can check the LCM request count
            return true;
        }

        @Override
        boolean checkState() {
            logDebug(String.format("Executing %s task", actionName));


            boolean hasError = false;
            try {
                inprogressRequestCount = getInprogressLCMRequestCount();
                if (inprogressRequestCount > 0) {
                    // if there are still LCM request in progress, keep waiting
                    return false;
                }

                setStatus(OAMCommandStatus.SUCCESS);
            } catch (Exception e) {
                setErrorStatus(e);
                hasError = true;
            }

            postAction(hasError ? AppcOamStates.Error : finalState);
            return true;
        }

        /**
         * Get in progress LCM request count through RequestHandler.
         * @return thecount of in progress LCM request
         * @throws APPCException if RequestHandler throws it.
         */
        private int getInprogressLCMRequestCount() throws APPCException {
            RequestHandler requestHandler = operationHelper.getService(RequestHandler.class);

            if (requestHandler == null) {
                return 0;
            }

            return requestHandler.getInprogressRequestCount();
        }

        @Override
        void keepWaiting() {
            logDebug("The application '%s' has '%s' outstanding LCM request to complete" +
                    " before coming to a complete maintenance_mode.",
                configurationHelper.getAppcName(), inprogressRequestCount);
        }
    }
}
