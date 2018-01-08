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

package org.onap.appc.requesthandler.exceptions;

/**
 * This exception is thrown when more than one record are retrieved from DB
 */
public class MultipleRecordsRetrievedException extends Exception {
    /**
     * Constructs a new exception with null as its detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to initCause.
     */
    public MultipleRecordsRetrievedException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to initCause.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     */
    public MultipleRecordsRetrievedException(String message) {
        super(message);
    }
}
