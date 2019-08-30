/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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

package org.onap.appc.instar.dme2client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Feature;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class Dme2Client {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(Dme2Client.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
    private Properties properties = new Properties();
    private String operationName;
    private String appendContext;
    private String mask;
    private String ipAddress;

    public Dme2Client(String optName, String subCtxt, Map<String, String> data) throws IOException {
        log.info("Setting Properties for DME2 Client for INSTAR connection");
        this.operationName = optName;
        this.appendContext = data.get(subCtxt);
        if ("getVnfbyIpadress".equals(optName)) {
            this.ipAddress = data.get("ipAddress");
            this.mask = data.get("mask");
        }
        String propDir = InstarClientConstant.getEnvironmentVariable(SDNC_CONFIG_DIR_VAR);
        if (propDir == null) {
            throw new IOException("Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        }
        String propFile = propDir + InstarClientConstant.OUTBOUND_PROPERTIES;
        InputStream propStream = InstarClientConstant.getInputStream(propFile);
        try {
            properties.load(propStream);
        } catch (Exception e) {
            throw new IOException("Could not load properties file " + propFile, e);
        } finally {
            try {
                propStream.close();
            } catch (Exception e) {
                log.warn("Could not close FileInputStream", e);
            }
        }
    }

    private Response sendToInstar() throws SvcLogicException {

        log.info("Called Send with operation Name=" + this.operationName + "and = " +
            properties.getProperty(operationName + InstarClientConstant.BASE_URL));

        String resourceUri = buildResourceUri();

        log.info("DME Endpoint URI:" + resourceUri);

        Client client = null;
        WebTarget webResource;
        Response clientResponse = null;
        String authorization = properties.getProperty("authorization");
        String requestDataType = "application/json";
        String responseDataType = MediaType.APPLICATION_JSON;
        String methodType = properties.getProperty("getIpAddressByVnf_method");
        String request = "";
        String userId = properties.getProperty("MechID");
        String password = properties.getProperty("MechPass");

        log.info("authorization = " + authorization + "methodType= " + methodType);

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext;
            SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[]{secureRestClientTrustManager}, null);


            client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(getHostnameVerifier()).build();

            client.register(HttpAuthenticationFeature.basic(userId, password));
            webResource = client.target(new URI(resourceUri));
            webResource.property("Content-Type", "application/json;charset=UTF-8");

            if (HttpMethod.GET.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.request(responseDataType).get(Response.class);
            } else if (HttpMethod.POST.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.request(requestDataType).post( Entity.json(request),Response.class);
            } else if (HttpMethod.PUT.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.request(requestDataType).put( Entity.json(request),Response.class);
            } else if (HttpMethod.DELETE.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.request().delete(Response.class);
            }
            return clientResponse;

        } catch (Exception e) {
            log.info(
                "failed in RESTCONT Action (" + methodType + ") for the resource " + resourceUri + ", falut message :"
                    + e.getMessage());
            throw new SvcLogicException("Error While gettting Data from INSTAR", e);

        } finally {
            // clean up.
            if (client != null) {
                client.close();
            }
        }
    }

    private String buildResourceUri() {
        String resourceUri = properties.getProperty(operationName + InstarClientConstant.BASE_URL) +
            properties.getProperty(operationName + InstarClientConstant.URL_SUFFIX);

        if (ipAddress != null && mask == null) {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYIPADDRESS) + ipAddress;
        } else if (mask != null) {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYIPADDRESS)
                + ipAddress + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYMASK) + mask;
        } else {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT) + appendContext;
        }
        return resourceUri;
    }

    public String send() {
        String response = null;
        try {
            if (validateProperties()) {
                return IOUtils.toString(Dme2Client.class.getClassLoader().getResourceAsStream("/tmp/sampleResponse"),
                    Charset.defaultCharset());
            }
            Response clientResponse = sendToInstar();
            if (clientResponse != null) {
                response = clientResponse.readEntity(String.class);
                log.info(clientResponse.getStatus() + " Status, Response :" + response);
            }
        } catch (Exception e) {
            log.error("Failed to send response", e);
        }
        return response;
    }

    private boolean validateProperties() {
        return properties != null
            && properties.getProperty(InstarClientConstant.MOCK_INSTAR) != null
            && "true".equalsIgnoreCase(properties.getProperty(InstarClientConstant.MOCK_INSTAR));
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }
}
