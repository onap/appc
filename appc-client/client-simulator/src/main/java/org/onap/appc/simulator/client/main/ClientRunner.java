/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
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

package org.onap.appc.simulator.client.main;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.onap.appc.simulator.client.RequestHandler;
import org.onap.appc.simulator.client.impl.JsonRequestHandler;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ClientRunner {
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(ClientRunner.class);
    public static void main(String ... args) throws Exception {
        String folder = args[0];
        if (folder == null) {
            folder = System.getProperty("user.dir");
        }
        LOG.info("== THR#" +Thread.currentThread().getId()+ " Reading files under the folder : " + folder);

        String inputType = args[1];
        if (inputType != null && !inputType.matches("JSON")) {
            throw new RuntimeException("### ERROR ### - Unsupported file type <" + inputType + "> was provided");
        }

        Properties properties = getProperties(folder);
        RequestHandler reqHandler = new JsonRequestHandler(properties);
        List<File> sources = getJsonFiles(folder);
        File log = new File(folder + "/output.txt");
        int filesNum = 0;
        for (File source: sources) {
            reqHandler.proceedFile(source, log);
            LOG.info("== THR#" +Thread.currentThread().getId()+ " File <" + source.getName() + "> processed.");
            ++filesNum;
        }
        LOG.info("DONE with " + filesNum + " files under the folder : " + folder);
        Thread.sleep(30);
        LOG.info("Shutdown ...");
        reqHandler.shutdown(Boolean.parseBoolean(properties.getProperty("client.force.shutdown")));
//        System.exit(0);
    }

     private static Properties getProperties(String folder) {
        Properties prop = new Properties();

        InputStream conf = null;
        try {
            conf = new FileInputStream(folder + "client-simulator.properties");
        } catch (FileNotFoundException e) {

        }
        if (conf != null) {
            try {
                prop.load(conf);
            } catch (IOException e) {
                LOG.error("Error occured while reading the property file", e);
            }
        } else {
            try {
                prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("client-simulator.properties"));
            } catch (Exception e) {
                throw new RuntimeException("### ERROR ### - Could not load properties to test");
            }
        }
        return prop;
    }

     private static List<File> getJsonFiles(String folder) throws FileNotFoundException {
        Path dir = Paths.get(folder);
        FileFilter fileFilter = new WildcardFileFilter("*.json");
        return new ArrayList<File>(Arrays.asList(dir.toFile().listFiles(fileFilter)));
    }
}
