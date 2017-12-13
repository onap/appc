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

package org.onap.appc.cache;

import org.junit.Assert;
import org.junit.Test;

public class CacheStrategiesTest {
    private CacheStrategies cacheStrategies = CacheStrategies.LRU;

    @Test
    public void testName() throws Exception {
        Assert.assertEquals("Should have name LRU", "LRU", cacheStrategies.name());
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals("Should return String LRU", "LRU", cacheStrategies.toString());
    }

    @Test
    public void testEquals() throws Exception {
        Assert.assertTrue(cacheStrategies.equals(CacheStrategies.LRU));
        Assert.assertFalse(cacheStrategies.equals(null));
    }
}
