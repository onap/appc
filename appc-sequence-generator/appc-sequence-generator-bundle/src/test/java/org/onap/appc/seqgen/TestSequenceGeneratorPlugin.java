/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.seqgen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.onap.appc.seqgen.dgplugin.SequenceGeneratorPlugin;
import org.onap.appc.seqgen.dgplugin.impl.SequenceGeneratorPluginImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestSequenceGeneratorPlugin {
    @Rule
    public final TestName thisTestName = new TestName();

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestSequenceGeneratorPlugin.class);
    private SequenceGeneratorPlugin seqImpl;

    @Test
    public void testGenerateSequenceStart() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/start.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        seqImpl = new SequenceGeneratorPluginImpl();
        seqImpl.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/Start2.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceWODependencyInfo()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/start-withoutDependency.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-withoutDependency.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceSingleVM()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/start-singleVM-.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-singleVM-.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceNoStrategy() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/no-strategy.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/Start2.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStop() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/stop.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/Output-stop.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceWrongNumber() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/wrongnumber.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.info("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Invalid Number for Wait Time 6a");
    }


    @Test
    public void testGenerateSequenceCyclic() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/cyclic.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.info("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence There seems to be no Root/Independent node for Vnfc dependencies");
    }

    @Test
    public void testGenerateSequenceWrongAction() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/wrongaction.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.info("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Invalid Action start");
    }


    @Test
    public void testGenerateSequenceMissingRequestInfo() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/missingrequestinfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Request info is not provided in the input");
    }

    @Test
    public void testGenerateSequenceStopSingleVM() throws URISyntaxException, IOException{
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/stop-singleVM.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-singleVM.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStopSingleVmPerVnfc() throws URISyntaxException, IOException{
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/stop-singleVmPerVnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-singleVmPerVnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartNoDep() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/restartNodep.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/restart-NoDep.json");
        outputJSON.trim();
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartNoDepSingleVM() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/NoDep-SingleVM.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/restart-Nodep-SingleVM.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStartSingleVmPerVnfc() throws URISyntaxException, IOException{
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/start-singleVmPerVnfc-.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-singleVmPerVnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceVnfcNotPresentInInventory() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/CheckVNfcInInventory.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);
        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/CheckVnfcInInventory.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceCheckMandatoryVnfc() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/CheckMandatoryVnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence VMs missing for the mandatory VNFC : [smp]");
    }

    @Test
    public void testGenerateSequenceCheckMissingDependencyInfo() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/MissingDependencyInfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Dependency model is missing following vnfc type(s): [smp]");
    }

    @Test
    public void testGenerateSequenceExtraVnfcInDependency() throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/WrongDependencyModel.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Dependency model missing vnfc type SMP");
    }

    @Test
    public void testGenerateSequenceStartWithVmStartCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StartWithVmStartCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StartWithVmStartCaps.json");
        Assert.assertEquals(outputJSON.trim(), actualOutput.trim());
    }
    
    @Test
    public void testGenerateSequenceRestartWithVmRestartCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/RestartWithVmRestartCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/RestartWithVmRestartCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }
    
    @Test
    public void testGenerateSequenceStopWithVmStopCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StopWithVmStopCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StopWithVmStopCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }
    
    @Test
    public void testGenerateSequenceStartWithoutAnyCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StartWithoutAnyCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StartWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(), actualOutput.trim());
    }
    
    @Test
    public void testGenerateSequenceRestartWithoutAnyCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/RestartWithoutAnyCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/RestartWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }
    
    @Test
    public void testGenerateSequenceStopWithoutAnyCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StopWithoutAnyCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StopWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStartWithoutVmStartCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StartWithoutVmStartCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        Assert.assertEquals(null, actualOutput);
    }
    
    @Test
    public void testGenerateSequenceRestartWithoutVmRestartCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/RestartWithoutVmRestartCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        Assert.assertEquals(null, actualOutput);
    }
    
    @Test
    public void testGenerateSequenceStopWithoutVmStopCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StopWithoutVmStopCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        Assert.assertEquals(null, actualOutput);
    }

    @Test
    public void testGenerateSequenceStartWithEmptyVmCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StartWithEmptyVmCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StartWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartWithEmptyVmCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/RestartWithEmptyVmCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/RestartWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStopWithEmptyVmCaps()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StopWithEmptyVmCaps.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StopWithoutAnyCaps.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStartWithVmStartCapsAndMissingVnfcInfo()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StartWithVmStartCapsAndMissingVnfcInfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StartWithVmStartCapsAndMissingVnfcInfo.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStopWithVmStopCapsAndMissingVnfcInfo()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/StopWithVmStopCapsAndMissingVnfcInfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/StopWithVmStopCapsAndMissingVnfcInfo.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartWithVmRestartCapsAndMissingVnfcInfo()throws URISyntaxException, IOException {
        logger.info("****** Running test:  " + thisTestName.getMethodName() + " ******");

        String inputJSON = readInput("/input/RestartWithVmRestartCapsAndMissingVnfcInfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String actualOutput = context.getAttribute("output");
        String outputJSON = readOutput("/output/RestartWithVmRestartCapsAndMissingVnfcInfo.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    private String readInput(String inputFile) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(this.getClass().getResource(inputFile).toURI())), "UTF-8");
    }

    private String readOutput(String outputFile) throws IOException,URISyntaxException {
        String output = new String(Files.readAllBytes(Paths.get(this.getClass().getResource(outputFile).toURI())), "UTF-8");
        int start=output.indexOf("[");
        return output.substring(start,output.length());
    }
}
