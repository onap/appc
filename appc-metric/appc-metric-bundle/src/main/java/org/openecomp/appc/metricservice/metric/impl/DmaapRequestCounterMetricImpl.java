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

package org.onap.appc.metricservice.metric.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.DmaapRequestCounterMetric;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class DmaapRequestCounterMetricImpl implements DmaapRequestCounterMetric {

    private String name;
    private MetricType metricType;
    private AtomicLong recievedMessage = new AtomicLong();
    private AtomicLong publishedMessage = new AtomicLong();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("YYYY-MM-dd:HH:mm:ss");

    private String lastResetTime = dateTimeFormat.format(Calendar.getInstance().getTime());
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DmaapRequestCounterMetricImpl.class);

    public DmaapRequestCounterMetricImpl(String name, MetricType metricType, long recievedMessage,
                                       long publishedMessage) {
        this.name = name;
        this.metricType = metricType;
        this.recievedMessage.set(recievedMessage);
        this.publishedMessage.set(publishedMessage);
    }

    @Override
    public void incrementRecievedMessage() {
        this.recievedMessage.incrementAndGet();
    }

    @Override
    public void incrementPublishedMessage() {
        this.publishedMessage.incrementAndGet();
    }

    @Override
    public String value() {
        logger.debug("Value is getting calculated for metric :" + this.name);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = dateFormat.format(cal.getTime());
            String value = date + "[" + recievedMessage.get() + "],[" + publishedMessage.get() + "]";
            logger.debug("Current value of the metric " + this.name + " :" + value);
            return value;
        } catch (Exception e) {
            logger.debug("Cant format the date.",e);
        }
        return null;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void reset() {
        this.recievedMessage.set(0);
        this.publishedMessage.set(0);
        Calendar cal = Calendar.getInstance();
        lastResetTime = dateTimeFormat.format(cal.getTime());
    }

    @Override
    public MetricType type() {
        return this.metricType;
    }

    @Override
    public HashMap<String, String> getMetricsOutput() {
        HashMap<String, String> dmaapMetricResult = new HashMap<>();
        dmaapMetricResult.put("Total Received messages", Long.toString(recievedMessage.get()));
        dmaapMetricResult.put("Total Published messages", Long.toString(publishedMessage.get()));
        return dmaapMetricResult;
    }

    @Override
    public String toString() {
        return this.value();
    }

    @Override
    public String getLastModified() {
        return lastResetTime;
    }
}
