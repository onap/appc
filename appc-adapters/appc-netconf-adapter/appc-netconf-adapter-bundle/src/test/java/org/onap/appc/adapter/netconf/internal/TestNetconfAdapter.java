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

package org.onap.appc.adapter.netconf.internal;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class TestNetconfAdapter {

	private static final String EOM = "]]>]]>";

	@Test
	public void testReceiveMessage() throws IOException {
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(pos);

		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream os = new PipedOutputStream(pis);

		NetconfAdapter netconfAdapter = new NetconfAdapter(is, os);

		String request = "Hello, netconf!";
		pos.write(request.getBytes());
		pos.write(EOM.getBytes());
		String response = netconfAdapter.receiveMessage();
		Assert.assertNotNull(response);
		Assert.assertEquals(request, response.trim());
	}

	@Test
	public void testSendMessage() throws IOException {
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(pos);

		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream os = new PipedOutputStream(pis);

		NetconfAdapter netconfAdapter = new NetconfAdapter(is, os);

		String request = "Hello, netconf!";
		netconfAdapter.sendMessage(request);
		byte[] bytes = new byte[request.length()+EOM.length()+2];
		int count = pis.read(bytes);
		String response = new String(bytes, 0, count);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.endsWith(EOM));
		response = response.substring(0, response.length() - EOM.length()).trim();
		Assert.assertEquals(request, response);
	}

	@Test
	public void testSendReceive() throws IOException {
		PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os);

		NetconfAdapter netconfAdapter = new NetconfAdapter(is, os);

		String request = "Hello, netconf!";
		netconfAdapter.sendMessage(request);
		String response = netconfAdapter.receiveMessage();
		Assert.assertNotNull(response);
		Assert.assertEquals(request, response.trim());
	}
}
