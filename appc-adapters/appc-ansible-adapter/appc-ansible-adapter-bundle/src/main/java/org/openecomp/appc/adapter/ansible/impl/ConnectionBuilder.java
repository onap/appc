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

package org.openecomp.appc.adapter.ansible.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.AuthScope;
import org.apache.http.entity.StringEntity;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import java.io.FileInputStream;
import java.io.IOException;


import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;


import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.adapter.ansible.model.AnsibleResult;
import org.openecomp.appc.adapter.ansible.model.AnsibleResultCodes;


/** 
     * Returns custom http client 
     - based on options
     - can create one with ssl using an X509 certificate that does NOT  have a known CA
     - create one which trusts ALL SSL certificates
     - return default httpclient (which only trusts known CAs from default cacerts file for process) -- this is the default option
     
**/


public class ConnectionBuilder {



    private   CloseableHttpClient http_client = null;
    private HttpClientContext http_context  = new HttpClientContext();




    // Various constructors depending on how we want to instantiate the http ConnectionBuilder instance


    /**
     * Constructor that initializes an http client based on certificate
     **/
    public ConnectionBuilder(String CertFile) throws KeyStoreException, CertificateException, IOException, KeyManagementException, NoSuchAlgorithmException, APPCException{
	
	
	/* Point to the certificate */
	FileInputStream fs = new FileInputStream(CertFile);
	
	/* Generate a certificate from the X509 */
	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	X509Certificate cert = (X509Certificate)cf.generateCertificate(fs);
	
	/* Create a keystore object and load the certificate there */
	KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	keystore.load(null, null);
	keystore.setCertificateEntry("cacert", cert);
	
	
	SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).build();
	SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	
	http_client  = HttpClients.custom().setSSLSocketFactory(factory).build();
    };


    /**
     * Constructor which trusts all certificates in a specific java keystore file (assumes a JKS file)
     **/
    public ConnectionBuilder(String trustStoreFile, char[] trustStorePasswd) throws KeyStoreException, IOException, KeyManagementException, NoSuchAlgorithmException, CertificateException {
	
	
	/* Load the specified trustStore */
	KeyStore keystore = KeyStore.getInstance("JKS");
	FileInputStream readStream = new FileInputStream(trustStoreFile);
	keystore.load(readStream,trustStorePasswd);
	
	SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).build();
	SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	
	http_client  = HttpClients.custom().setSSLSocketFactory(factory).build();
    };

    /**
     * Constructor that trusts ALL SSl certificates  (NOTE : ONLY FOR DEV TESTING) if Mode == 1
     or Default if Mode == 0
    */
    public ConnectionBuilder(int Mode) throws SSLException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
	if (Mode == 1){
	    SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null,  new TrustSelfSignedStrategy()).build();
	    SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	    
	    http_client = HttpClients.custom().setSSLSocketFactory(factory).build();       	    
	}

	else{
	    http_client = HttpClients.createDefault();
	}
	
    };

   
    // Use to create an http context with auth headers
    public  void  setHttpContext(String User, String MyPassword){
	
	// Are credential provided ? If so, set the context to be used 
	if (User != null && ! User.isEmpty() && MyPassword != null && ! MyPassword.isEmpty()){	
	    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(User, MyPassword);
	    AuthScope authscope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
	    BasicCredentialsProvider credsprovider = new BasicCredentialsProvider();
	    credsprovider.setCredentials(authscope, credentials);
	    http_context.setCredentialsProvider(credsprovider);
	}

	
    };


    // Method posts to the ansible server and writes out response to 
    // Ansible result object 
    public AnsibleResult Post(String AgentUrl, String Payload){

	AnsibleResult result = new AnsibleResult();
	try{
	    
	    HttpPost postObj = new HttpPost(AgentUrl);
	    StringEntity bodyParams = new StringEntity(Payload, "UTF-8");
	    postObj.setEntity(bodyParams);
	    postObj.addHeader("Content-type", "application/json");
	    
	    HttpResponse response = http_client.execute(postObj, http_context);
	    
	    HttpEntity entity = response.getEntity();
	    String responseOutput =  entity != null ? EntityUtils.toString(entity) : null;
	    int responseCode = response.getStatusLine().getStatusCode();
	    result.setStatusCode(responseCode);
	    result.setStatusMessage(responseOutput);
	}
	
	catch(IOException io){
	    result.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
	    result.setStatusMessage(io.getMessage());
	}
	
	

	return result;

    }
    
    // Method gets information from an Ansible server and writes out response to
    // Ansible result object

    public AnsibleResult Get(String AgentUrl){

	AnsibleResult result = new AnsibleResult();

	try{
	    HttpGet getObj = new HttpGet(AgentUrl );
	    HttpResponse response =  http_client.execute(getObj, http_context);
	    
	    
	    HttpEntity entity = response.getEntity();
	    String responseOutput =  entity != null ? EntityUtils.toString(entity) : null;
	    int responseCode = response.getStatusLine().getStatusCode();
	    result.setStatusCode(responseCode);
	    result.setStatusMessage(responseOutput);

	}
	catch(IOException io){
	    result.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
	    result.setStatusMessage(io.getMessage());
	}
	
	return result;
    };

}
