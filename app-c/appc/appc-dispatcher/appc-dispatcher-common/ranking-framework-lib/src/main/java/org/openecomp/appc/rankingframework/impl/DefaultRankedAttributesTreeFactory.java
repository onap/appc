/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.rankingframework.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openecomp.appc.rankingframework.ConfigurationSet;
import org.openecomp.appc.rankingframework.RankedAttributesResolver;
import org.openecomp.appc.rankingframework.RankedAttributesResolverFactory;

public final class DefaultRankedAttributesTreeFactory implements RankedAttributesResolverFactory {

    private final Strategy DEFAULT_STRATEGY = new BacktraceStrategy();

    @Override
    public <R> RankedAttributesResolver<R> create(ConfigurationSet<R> config) {

        CompositeNode<R> root = RankedAttributesTreeBuilder.build(config);
        RankedAttributesResolver<R> tree = new RankedAttributesTree<R>(root, toList(config.getRankedAttributeNames()),
                DEFAULT_STRATEGY);
        return tree;
    }

    private static List<String> toList(Collection<String> col) {
        return Collections.unmodifiableList(new ArrayList<>(col));
    }
}
