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

package org.onap.sdnc.config.audit.node;


import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.util.Nodes;
import org.xmlunit.builder.DiffBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class CompareXmlData implements CompareDataInterface
{
    private static final EELFLogger log = EELFManager.getInstance().getLogger(CompareXmlData.class);


    String controlXml;
    String testXml;

    Document doc;

    public CompareXmlData(String controlXml, String testXml) {
        super();
        this.controlXml = controlXml;
        this.testXml = testXml;
    }

    @Override
    public boolean compare() throws Exception
    {

        log.debug("controlXml : " + controlXml);
        log.debug("testXml : " + testXml);
        controlXml = controlXml.replace("junos:", "");
        testXml = testXml.replace("junos:", "");
        //doSetup();

        try
        {
             //Diff diff = new Diff(getCompareDoc(controlXml), getCompareDoc(testXml));
            final org.xmlunit.diff.Diff documentDiff = DiffBuilder
                    .compare(controlXml)
                    .withTest(testXml)
                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                    .checkForSimilar()
                    .withDifferenceEvaluator((comparison, outcome) -> {
                            if (outcome != ComparisonResult.EQUAL && comparison.getType() == ComparisonType.ATTR_VALUE) {
                                Attr a = (Attr) comparison.getControlDetails().getTarget();
                                if ("commit-seconds".equals(Nodes.getQName(a).getLocalPart()) || "commit-localtime".equals(Nodes.getQName(a).getLocalPart())
                                    && "configuration".equals(Nodes.getQName(a.getOwnerElement()).getLocalPart())) {
                                    return ComparisonResult.EQUAL;
                                }
                            }
                            else
                                return ComparisonResult.SIMILAR;

                            return outcome;
                        })
                    .ignoreComments()
                    .ignoreWhitespace()
                    .build();
            if(documentDiff.hasDifferences())
                return false;
            else
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    /*private void doSetup() throws ParserConfigurationException, SAXException, IOException
    {

        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }*/


    public Document getCompareDoc(String inXml) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        StringReader reader = new StringReader(inXml);
        InputSource inputSource = new InputSource(reader);
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        return doc;
    }
}
