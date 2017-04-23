/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package odata.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

public class Util {
  public static String sf_learning_key = "Basic amlheGluZzpjNjdmODBlODJlMWFkOGIzZjc0OGU1ODQ2YWQ5ODQ1Mzc2ZGU5NjU0ODNjNjM5NTAzMDZiMTAwYjdlMDhkMzFi";
  public static String type_post = "POST";
  public static String type_get = "GET";  
  public static Entity findEntity(EdmEntityType edmEntityType, EntityCollection entitySet,
                                  List<UriParameter> keyParams) throws ODataApplicationException {

    List<Entity> entityList = entitySet.getEntities();

    // loop over all entities in order to find that one that matches all keys in request
    // e.g. contacts(ContactID=1, CompanyID=1)
    for (Entity entity: entityList) {
      boolean foundEntity = entityMatchesAllKeys(edmEntityType, entity, keyParams);
      if (foundEntity) {
        return entity;
      }
    }

    return null;
  }

  public static boolean entityMatchesAllKeys(EdmEntityType edmEntityType, Entity entity, List<UriParameter> keyParams)
          throws ODataApplicationException {

    // loop over all keys
    for (final UriParameter key : keyParams) {
      // key
      String keyName = key.getName();
      String keyText = key.getText();

      // Edm: we need this info for the comparison below
      EdmProperty edmKeyProperty = (EdmProperty) edmEntityType.getProperty(keyName);
      Boolean isNullable = edmKeyProperty.isNullable();
      Integer maxLength = edmKeyProperty.getMaxLength();
      Integer precision = edmKeyProperty.getPrecision();
      Boolean isUnicode = edmKeyProperty.isUnicode();
      Integer scale = edmKeyProperty.getScale();
      // get the EdmType in order to compare
      EdmType edmType = edmKeyProperty.getType();
      EdmPrimitiveType edmPrimitiveType = (EdmPrimitiveType) edmType;

      // Runtime data: the value of the current entity
      // don't need to check for null, this is done in olingo library
      Object valueObject = entity.getProperty(keyName).getValue();

      // now need to compare the valueObject with the keyText String
      // this is done using the type.valueToString
      String valueAsString;
      try {
        valueAsString = edmPrimitiveType.valueToString(valueObject, isNullable, maxLength, precision, scale, isUnicode);
      } catch (EdmPrimitiveTypeException e) {
        throw new ODataApplicationException("Failed to retrieve String value", HttpStatusCode.INTERNAL_SERVER_ERROR
                .getStatusCode(), Locale.ENGLISH, e);
      }

      if (valueAsString == null) {
        return false;
      }

      boolean matches = valueAsString.equals(keyText);
      if (!matches) {
        // if any of the key properties is not found in the entity, we don't need to search further
        return false;
      }
    }

    return true;
  }
  
  public static String getURLResponse(String urlString, String auth, String payload, String type ) {
	    //Logger logger = LoggerFactory.getLogger(this.getClass());
	    String result = null;
	    OutputStream output = null;
	    BufferedReader br = null;
	    InputStreamReader isr = null;
	    InputStream is = null;
	    try {
	      URL url = new URL(urlString);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      //if (conn instanceof HttpsURLConnection) {
	      //  disableSSLVerification((HttpsURLConnection) conn);
	      //}
          if (type == type_post){
    	      conn.setRequestMethod(HttpMethod.POST);
          }else if(type == type_get){
        	  conn.setRequestMethod(HttpMethod.GET);
          }
	      //conn.setRequestMethod(HttpMethod.POST);
	      conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
	      conn.setRequestProperty(HttpHeaders.AUTHORIZATION, auth);

	      if (payload != null){	     
	    	  conn.setDoOutput(true);
	          output = conn.getOutputStream();
		      output.write(payload.getBytes(StandardCharsets.UTF_8));  
		      output.flush();
	      }

	      int statusCode = conn.getResponseCode();
	      if (400 <= statusCode || statusCode >= 599) {
	       // logger.error("get post response failed, connection response code: " + statusCode + ", message: "
	       //     + conn.getResponseMessage());
	        return null;
	      }

	      is = conn.getInputStream();
	      isr = new InputStreamReader(is);
	      br = new BufferedReader(isr);
	      String inputLine;
	      StringBuilder response = new StringBuilder();
	      while ((inputLine = br.readLine()) != null) {
	        response.append(inputLine);
	      }
	      result = response.toString();
	    } catch (IOException e) {
	      //logger.error("get post response failed, connection response code: " + e.getMessage());
	    } finally {
	      IOUtils.closeQuietly(output);
	      IOUtils.closeQuietly(br);
	      IOUtils.closeQuietly(isr);
	      IOUtils.closeQuietly(is);
	    }
	    return result;
	  }
  
  private static void disableSSLVerification(HttpsURLConnection connection) {
	    connection.setHostnameVerifier(new HostnameVerifier() {
	      public boolean verify(String hostname, SSLSession session) {
	        return true;
	      }
	    });
	  }
  
  public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    JSONObject param = new JSONObject();
	    JSONObject scope = new JSONObject();

	    param.put("grant_type", "client_credentials");
	    scope.put("userId", "Eddiy");
	    scope.put("companyId", "jiaxing");
	    scope.put("userType", "user");
	    scope.put("resourceType", "learning_public_api");
	    param.put("scope", scope);
	    String jsonString = getURLResponse("https://jiaxing-stage.lms.sapsf.cn/learning/oauth-api/rest/v1/token",sf_learning_key, param.toString(), type_post);

	    
  }

}
