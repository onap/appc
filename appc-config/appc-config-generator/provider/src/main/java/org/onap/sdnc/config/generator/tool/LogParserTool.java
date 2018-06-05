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

package org.onap.sdnc.config.generator.tool;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogParserTool {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(JSONTool.class);

    private String[] singleLines;
    private List<String> recentErrors = new ArrayList<>();
    private Date todaysDate = new Date();
    private SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final int MIN_MILLI = 60000;
    private static final int IN_TIME = 0;
    private static final int NOT_IN_TIME = 1;
    private static final int NO_DATE = 2;

    public String parseErrorLog(String data) {
        singleLines = data.split("\\r?\\n");
        try {
            getNearestDates();

        } catch (Exception e) {
            log.error("Failed to get nearest dates", e);
        }
        if (recentErrors.isEmpty()) {
            recentErrors.clear();
            return "Did not find the string 'Starting orchestration of file backed up to /var/opt/MetaSwitch/orch/orch_conf.json' in the log file with timestamp within the last 5 minutes";
        } else if (recentErrors.size() == 1) {
            recentErrors.clear();
            return "Did not find the string ?Error parsing orchestration file? in the log file with timestamp within the last 5 minutes";
        } else {
            String error = recentErrors.get(0);
            recentErrors.clear();
            return "Error: "
                + error.substring(error.indexOf("Error parsing orchestration file:") + 34);
        }
    }

    private void getNearestDates() throws ParseException {
        int result;
        for (int i = singleLines.length - 1; i >= 0; i--) {
            if (singleLines[i].contains("Starting orchestration of file backed up to")
                || singleLines[i].contains("Error parsing orchestration file:")) {
                result = checkDateTime(singleLines[i]);
                if (result == IN_TIME) {
                    recentErrors.add(singleLines[i]);
                } else if (result == NOT_IN_TIME) {
                    return;
                }
            }
        }
    }

    private int checkDateTime(String line) {
        Date newDate;
        try {
            newDate = dFormat.parse(line.substring(0, 19));
            if ((todaysDate.getTime() - newDate.getTime()) <= 5 * MIN_MILLI) {
                return IN_TIME;
            } else {
                return NOT_IN_TIME;
            }
        } catch (ParseException e) {
            log.error("Failed to parse date", e);
            return NO_DATE;
        }
    }
}
