/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.appc.cache.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class LRUCacheTest {

    private LRUCache cache;

    @Before
    public void setUp() {
        cache = new LRUCache(20);
    }

    @Test
    public void testConstructor() throws Exception {
        Map internalMap = Whitebox.getInternalState(cache, "map");
        Assert.assertTrue(internalMap != null);
        Assert.assertTrue(internalMap.size() == 0);
    }

    @Test
    public void testGetAndPutObject() throws Exception {
        String key = "testing key";
        Assert.assertTrue(cache.getObject(key) == null);

        String value = "testing value";
        cache.putObject(key, value);
        Map internalMap = Whitebox.getInternalState(cache, "map");
        Assert.assertTrue(internalMap.containsKey(key));
        Assert.assertTrue(internalMap.containsValue(value));
        Assert.assertTrue(internalMap.size() == 1);

        Assert.assertEquals(value, cache.getObject(key));
    }

}
