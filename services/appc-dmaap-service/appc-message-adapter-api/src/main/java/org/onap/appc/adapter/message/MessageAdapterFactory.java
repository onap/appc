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
package org.onap.appc.adapter.message;

import java.util.Collection;
import java.util.Set;

public interface MessageAdapterFactory {
	
	// TODO: how do you configure the MessageService type?
	
	public Producer createProducer(Collection<String> pools, String writeTopic, String apiKey, String apiSecret);
	
	public Producer createProducer(Collection<String> pools, Set<String> writeTopics, String apiKey, String apiSecret);

	public Consumer createConsumer(Collection<String> pool, String readTopic, 
			                              String clientName, String clientId, String filter_json, String apiKey, String apiSecret);
}
