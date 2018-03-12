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

package org.onap.appc.ccadaptor;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class EncryptionToolTest {

    private static final String PLAIN_TEXT = "encrypt";
    private static final String ENCRYPTED_TEXT = "enc:JjEZHlg7VQ==";
    private static final String FAKE_ENCRYPTED_TEXT = "jsaASGou!na=e";

    private static EncryptionTool encryptionTool;

    @BeforeClass
    public static void setup() {
        encryptionTool = EncryptionTool.getInstance();
    }

    @Test
    public void should_return_null_when_given_null_to_encrypt() {
        assertEquals(null, encryptionTool.encrypt(null));
    }

    @Test
    public void should_encrypt_given_string() {
        assertEquals(ENCRYPTED_TEXT, encryptionTool.encrypt(PLAIN_TEXT));
    }

    @Test
    public void should_return_null_when_given_null_to_decrypt() {
        assertEquals(null, encryptionTool.decrypt(null));
    }

    @Test
    public void should_return_same_string_when_given_unencrypted_string() {
        assertEquals(FAKE_ENCRYPTED_TEXT, encryptionTool.decrypt(FAKE_ENCRYPTED_TEXT));
    }

    @Test
    public void should_decrypt_given_string() {
        assertEquals(PLAIN_TEXT, encryptionTool.decrypt(ENCRYPTED_TEXT));
    }
}