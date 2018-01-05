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

package org.onap.appc.executionqueue.helper;

import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    private int default_queue_size = 10;
    private int default_threadpool_size = 10;
    private String queue_size_key = "appc.dispatcher.executionqueue.backlog.size";
    private String threadpool_size_key = "appc.dispatcher.executionqueue.threadpool.size";

    private Configuration configuration;

    /**
     * Initialization.
     * <p>Used by blueprint.
     */
    public void init() {
        configuration = ConfigurationFactory.getConfiguration();
    }

    public int getExecutionQueueSize() {
        String sizeStr = configuration.getProperty(queue_size_key, String.valueOf(default_queue_size));

        int size = default_queue_size;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {

        }

        return size;
    }

    public int getThreadPoolSize() {
        String sizeStr = configuration.getProperty(threadpool_size_key, String.valueOf(default_threadpool_size));

        int size = default_threadpool_size;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {

        }

        return size;
    }

    public ThreadFactory getThreadFactory(final boolean isDaemon, final String threadNamePrefix) {
        return new ThreadFactory() {
            private final String THREAD_NAME_PATTERN = "%s-%d";
            private final ThreadFactory factory = Executors.defaultThreadFactory();
            private final AtomicInteger counter = new AtomicInteger();

            public Thread newThread(Runnable r) {
                Thread t = factory.newThread(r);
                t.setDaemon(isDaemon);
                if (threadNamePrefix != null && !threadNamePrefix.isEmpty()) {
                    final String threadName = String.format(THREAD_NAME_PATTERN, threadNamePrefix, counter.incrementAndGet());
                    t.setName(threadName);
                }
                return t;
            }
        };
    }
}
