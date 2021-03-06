/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 IBM
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.util;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

public class TimeTest {

    private final Date dateForTests = Date
            .from(LocalDate.of(2004, 2, 9).atStartOfDay(ZoneId.systemDefault()).toInstant());

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

        long msecFromBegin = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 + cal.get(Calendar.MINUTE) * 60 * 1000
                + cal.get(Calendar.SECOND) * 1000 + cal.get(Calendar.MILLISECOND);

        assertEquals(msecFromBegin, 0);

    }

    @Test
    public void testGetCurrentUTCDate() {

        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        Date utcDate = Date.from(utc.toInstant());
        long epochSecs = utc.toEpochSecond();

        long utcSecs = utcDate.getTime() / 1000;

        assertEquals(epochSecs, utcSecs);
    }

    @Test
    public void testEndOfDayLocal() {
        final Date dateNow = new Date();
        assertTrue(Time.endOfDayLocal(dateNow) instanceof Date);
    }

    @Test
    public void testGetDateByLocaleAndTimeZone() {
        final Date dateNow = new Date();
        Locale locale = new Locale("fr");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/France");
        assertNotNull(Time.getDateByLocaleAndTimeZone(dateNow, locale, timeZone));
        assertTrue(Time.getDateByLocaleAndTimeZone(dateNow, locale, timeZone) instanceof String);
    }

    @Test
    public void testUtcFormat() {
        final Date date = new Date();
        assertNotNull(Time.utcFormat(date));
        assertTrue(Time.utcFormat(date) instanceof String);
    }

    // this test succeeds if localTime() does not throw an exception
    @Test
    public void testLocalTime() {
        Time.localTime(1532083631);
    }

    @Test
    public void testSetDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2018);
        cal.set(Calendar.MONTH, 07);
        cal.set(Calendar.DAY_OF_MONTH, 03);
        Calendar cal1 = Time.setDate(cal, 2018, 07, 03);
        assertEquals(cal, cal1);
    }

    @Test
    public void testStartOfDayLocal() {
        assertTrue(Time.startOfDayLocal() instanceof Date);
    }

    @Test
    public void testTimeStamp() {
        assertTrue(Time.timestamp() instanceof XMLGregorianCalendar);
    }

    @Test
    public void testDateToStringConverterMillis() {
        String dateString = Time.dateToStringConverterMillis(dateForTests);
        String expected = "2004-02-09 00:00:00:000";
        assertEquals(expected, dateString);
    }
    
    @Test
    public void testDateToStringConverterMillisForNullDate() {
        assertNull(Time.dateToStringConverterMillis(null));
    }

    @Test
    public void testStringToDateConverterMillis() throws ParseException {
        Date date = Time.stringToDateConverterMillis("2004-02-09 00:00:00:000");
        Date expected = dateForTests;
        assertEquals(expected, date);
    }

    @Test
    public void testTruncateDate() throws ParseException {
        Date date = Time.truncDate(dateForTests);
        Date expected = dateForTests;
        assertEquals(expected, date);
    }

    @Test
    public void testToDate() throws ParseException, DatatypeConfigurationException {
        Date date = dateForTests;
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        Date actual = Time.toDate(calendar);
        Date expected = dateForTests;
        assertEquals(expected, actual);
    }

    @Test
    public void testUtcTime() {
        assertTrue(Time.utcTime()!=0);
    }

    @Test
    public void testCurrentUTCDate() {
        assertTrue(Time.getCurrentUTCDate() instanceof Date);
    }
}
