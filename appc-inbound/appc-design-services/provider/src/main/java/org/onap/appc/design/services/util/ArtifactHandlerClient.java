/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.design.services.util;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Feature;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;


public class ArtifactHandlerClient {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerClient.class);
    static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
    private Properties props = new Properties();

    public ArtifactHandlerClient() throws IOException {
        String propDir = DesignServiceConstants.getEnvironmentVariable(SDNC_CONFIG_DIR_VAR);
        if (propDir == null) {
            throw new IOException(" Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        }
        String propFile = propDir + "/" + DesignServiceConstants.DESIGN_SERVICE_PROPERTIES;
        InputStream propStream = new FileInputStream(propFile);
        try {
            props.load(propStream);
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

    public String createArtifactData(String payload, String requestID) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);

        ObjectNode json = objectMapper.createObjectNode();
        String artifactName = payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue();
        String artifactVersion = payloadObject.get(DesignServiceConstants.ARTIFACT_VERSOIN).textValue();
        String artifactContents = payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS).textValue();

        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put(DesignServiceConstants.REQUETS_ID, requestID);
        requestInfo.put(DesignServiceConstants.REQUEST_ACTION, "StoreSdcDocumentRequest");
        requestInfo.put(DesignServiceConstants.SOURCE, DesignServiceConstants.DESIGN_TOOL);

        ObjectNode docParams = objectMapper.createObjectNode();
        docParams.put(DesignServiceConstants.ARTIFACT_VERSOIN, artifactVersion);
        docParams.put(DesignServiceConstants.ARTIFACT_NAME, artifactName);
        docParams.put(DesignServiceConstants.ARTIFACT_CONTENTS, artifactContents);

        json.put(DesignServiceConstants.REQUEST_INFORMATION, requestInfo);
        json.put(DesignServiceConstants.DOCUMENT_PARAMETERS, docParams);
        log.info("Final data =" + json.toString());
        return String.format("{\"input\": %s}", json.toString());
    }

    public Map<String, String> execute(String payload, String rpc) throws IOException {
        log.info("Configuring Rest Operation for Payload " + payload + " RPC : " + rpc);
        Map<String, String> outputMessage = new HashMap<>();
        Client client = null;
        WebTarget webResource;
        Response clientResponse = null;
        EncryptionTool et = EncryptionTool.getInstance();
        String responseDataType = MediaType.APPLICATION_JSON;
        String requestDataType = MediaType.APPLICATION_JSON;

        try {
            
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext;
            SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[]{secureRestClientTrustManager}, null);
	
		
            client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(getHostnameVerifier()).build();
            
            String password = et.decrypt(props.getProperty("appc.upload.pass"));
            client.register(HttpAuthenticationFeature.basic(props.getProperty("appc.upload.user"), password));
            webResource = client.target(new URI(props.getProperty("appc.upload.provider.url")));
            webResource.property("Content-Type", "application/json;charset=UTF-8");
            log.info("Starting Rest Operation.....");
            if (HttpMethod.GET.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.request(responseDataType).get(Response.class);
            } else if (HttpMethod.POST.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.request(requestDataType).post(Entity.json(payload),Response.class);
            } else if (HttpMethod.PUT.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.request(requestDataType).put(Entity.json(payload),Response.class);
            } else if (HttpMethod.DELETE.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.request().delete(Response.class);
            }
            validateClientResponse(clientResponse);
            log.info("Completed Rest Operation.....");

        } catch (Exception e) {
            log.debug("failed in RESTCONT Action", e);
            throw new IOException("Error While Sending Rest Request" + e.getMessage(), e);
        } finally {
            // clean up.
            if (client != null) {
                client.close();
            }
        }
        return outputMessage;
    }

    private void validateClientResponse(Response clientResponse) throws ArtifactHandlerInternalException {
        if (clientResponse == null) {
            throw new ArtifactHandlerInternalException("Failed to create client response");
        }
        if (clientResponse.getStatus() != 200) {
            throw new ArtifactHandlerInternalException("HTTP error code : " + clientResponse.getStatus());
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }
}
