/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * 
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

package org.onap.appc.adapter.netconf;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestConnectionDetails {

	@Test
	public void testGetSetMethods() {
		ConnectionDetails connectionDetails = new ConnectionDetails();
		connectionDetails.setHost("host1");
		assertEquals("host1", connectionDetails.getHost());

		connectionDetails.setPort(123);
		assertEquals(123, connectionDetails.getPort());

		connectionDetails.setUsername("myname");
		assertEquals("myname", connectionDetails.getUsername());

		connectionDetails.setPassword("mypassword");
		assertEquals("mypassword", connectionDetails.getPassword());
	}

}
