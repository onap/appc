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

package org.onap.appc.instar.interfaceImpl;

import java.util.HashMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.instar.dme2client.Dme2Client;

public class TestInstarRestClientImpl {

	@Test(expected=Exception.class)
	public void testSendRequest() throws Exception{
		HashMap<String,String> map = new HashMap<>();
		map.put("key", "value");
		Dme2Client client = Mockito.mock(Dme2Client.class);
		Mockito.when(client.send()).thenReturn("test");
		InstarRestClientImpl imp = new InstarRestClientImpl(map);
		imp.sendRequest("getIpAddressByVnf");				
	}
}
