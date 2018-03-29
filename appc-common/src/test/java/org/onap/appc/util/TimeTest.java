/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018
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


package org.onap.appc.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;


public class TimeTest {

    @Test
    public void testAddTime() {

        final Date dateNow = new Date();
        long dateNowMSec = dateNow.getTime();
        Date dateSecLater = Time.addTime(dateNow, 0, 0, 0, 0, 1);
        long dateSecLaterMSec = dateSecLater.getTime();
        assertEquals(dateNowMSec + 1000, dateSecLaterMSec);

    }

    @Test
    public void testDateOnly() {

        final Date dateNow = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateNow);

        Time.dateOnly(cal);

        long msecFromBegin = cal.get(Calendar.HOUR_OF_DAY)*60*60*1000 +
            cal.get(Calendar.MINUTE)*60*1000 +
            cal.get(Calendar.SECOND)*1000 +
            cal.get(Calendar.MILLISECOND);

        assertEquals( msecFromBegin, 0);

    }

    @Test
    public void testGetCurrentUTCDate() {

        Date utcDate  = Time.getCurrentUTCDate();

        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);

        long epochSecs = utc.toEpochSecond();

        long utcSecs = utcDate.getTime() / 1000;

        assertEquals(epochSecs, utcSecs);
    }

}
