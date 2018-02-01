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

package org.onap.appc.rankingframework.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.onap.appc.rankingframework.ConfigurationSet;
import org.onap.appc.rankingframework.RankedAttributesResolver;
import org.onap.appc.rankingframework.RankedAttributesResolverFactory;

public final class DefaultRankedAttributesTreeFactory implements RankedAttributesResolverFactory {

    private final Strategy defaultStrategy = new BacktraceStrategy();

    @Override
    public <R> RankedAttributesResolver<R> create(ConfigurationSet<R> config) {

        CompositeNode<R> root = RankedAttributesTreeBuilder.build(config);
        return new RankedAttributesTree<>(root, toList(config.getRankedAttributeNames()), defaultStrategy);
    }

    private static List<String> toList(Collection<String> col) {
        return Collections.unmodifiableList(new ArrayList<>(col));
    }
}
