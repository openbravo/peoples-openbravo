/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.openbravo.test.base.TestConstants.Orgs.MAIN;
import static org.openbravo.test.matchers.json.JSONMatchers.equal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.StringEnumerateDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Protocol;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.externalsystem.ExternalSystem;
import org.openbravo.service.externalsystem.ExternalSystemData;
import org.openbravo.service.externalsystem.ExternalSystemProvider;
import org.openbravo.service.externalsystem.ExternalSystemResponse;
import org.openbravo.service.externalsystem.HttpExternalSystemData;
import org.openbravo.test.base.Issue;
import org.openbravo.test.base.TestConstants;
import org.openbravo.utils.FormatUtilities;

/**
 * Tests to cover the sending of data with {@link HttpExternalSystem}. Note: these tests expect to
 * have the server running as they execute HTTP requests and evaluate the responses.
 */
public class HttpExternalSystemCommunicationTest extends WeldBaseTest {

  @Inject
  private ExternalSystemProvider externalSystemProvider;

  private ExternalSystemData externalSystemData;
  private HttpExternalSystemData httpExternalSystemData;

  @Before
  public void init() {
    setTestAdminContext();

    externalSystemData = OBProvider.getInstance().get(ExternalSystemData.class);
    externalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, MAIN));
    externalSystemData.setName("Test");
    Protocol httpProtocol = OBDal.getInstance()
        .getProxy(Protocol.class, TestConstants.Protocols.HTTP);
    externalSystemData.setProtocol(httpProtocol);
    OBDal.getInstance().save(externalSystemData);
    httpExternalSystemData = OBProvider.getInstance().get(HttpExternalSystemData.class);
    httpExternalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, MAIN));
    httpExternalSystemData.setURL(getURL());
    httpExternalSystemData.setExternalSystem(externalSystemData);
    httpExternalSystemData.setActive(true);
    externalSystemData.getExternalSystemHttpList().add(httpExternalSystemData);
    OBDal.getInstance().save(httpExternalSystemData);

    // Add a new request method into the list reference, to avoid failing when checking if the
    // property value is valid after setting the "requestMethod" property value with it
    StringEnumerateDomainType domainType = (StringEnumerateDomainType) httpExternalSystemData
        .getEntity()
        .getProperty("requestMethod")
        .getDomainType();
    domainType.addEnumerateValue("TEST");
  }

  private String getURL() {
    Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    String obURL = props.getProperty("context.url");
    if (StringUtils.isEmpty(obURL)) {
      throw new OBException("context.url is not set in Openbravo.properties");
    }
    return obURL + "/org.openbravo.service.json.jsonrest/Country";
  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }

  @Test
  public void sendWithBasicAuth() throws JSONException, ServletException {
    ExternalSystemResponse response = sendWithBasicCredentials("BASIC");

    assertThat("Is Successful Response", response.getType(),
        equalTo(ExternalSystemResponse.Type.SUCCESS));
    assertThat("Expected Response Status Code", response.getStatusCode(),
        equalTo(HttpServletResponse.SC_OK));
    assertThat("Expected Response Data", (JSONObject) response.getData(),
        equal(getExpectedResponseData()));
  }

  @Test
  @Issue("49159")
  public void sendWithBasicAuthAlwaysInHeader() throws JSONException, ServletException {
    ExternalSystemResponse response = sendWithBasicCredentials("BASIC_ALWAYS_HEADER");

    assertThat("Is Successful Response", response.getType(),
        equalTo(ExternalSystemResponse.Type.SUCCESS));
    assertThat("Expected Response Status Code", response.getStatusCode(),
        equalTo(HttpServletResponse.SC_OK));
    assertThat("Expected Response Data", (JSONObject) response.getData(),
        equal(getExpectedResponseData()));
  }

  private ExternalSystemResponse sendWithBasicCredentials(String authorizationType)
      throws JSONException, ServletException {
    httpExternalSystemData.setAuthorizationType(authorizationType);
    httpExternalSystemData.setUsername("Openbravo");
    httpExternalSystemData.setPassword(FormatUtilities.encryptDecrypt("openbravo", true));

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();

    return externalSystem.send(getRequestDataSupplier()).join();
  }

  @Test
  public void sendUnauthorized() throws JSONException {
    httpExternalSystemData.setAuthorizationType("NOAUTH");

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();
    ExternalSystemResponse response = externalSystem.send(getRequestDataSupplier()).join();

    assertThat("Is Erroneous Response", response.getType(),
        equalTo(ExternalSystemResponse.Type.ERROR));
    assertThat("Expected Response Status Code", response.getStatusCode(),
        equalTo(HttpServletResponse.SC_UNAUTHORIZED));
  }

  @Test
  public void cannotSendWithUnsupportedRequestMethod() throws JSONException {
    OBException exceptionRule = assertThrows(OBException.class, () -> {
      httpExternalSystemData.setAuthorizationType("NOAUTH");
      httpExternalSystemData.setRequestMethod("TEST");
      ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
          .orElseThrow();
      externalSystem.send(getRequestDataSupplier()).join();
    });

    assertThat(exceptionRule.getMessage(), containsString("Unsupported HTTP request method TEST"));
  }

  @Test
  public void sendRequestToUnknownResource() throws JSONException {
    httpExternalSystemData.setURL("http://localhost:8000/dummy");
    httpExternalSystemData.setAuthorizationType("NOAUTH");

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();
    ExternalSystemResponse response = externalSystem.send(getRequestDataSupplier()).join();

    assertThat("Is Erroneous Response", response.getType(),
        equalTo(ExternalSystemResponse.Type.ERROR));
    assertThat("Expected Response Status Code", response.getStatusCode(), equalTo(0));
    assertThat("Expected Response Error", response.getError().toString(),
        startsWith("java.net.ConnectException"));
  }

  private Supplier<InputStream> getRequestDataSupplier() throws JSONException {
    JSONObject requestData = new JSONObject();
    requestData.put("data", new JSONArray());
    return () -> new ByteArrayInputStream(requestData.toString().getBytes());
  }

  private JSONObject getExpectedResponseData() throws JSONException {
    JSONObject expectedResponse = new JSONObject();
    JSONObject response = new JSONObject();
    response.put("status", 0);
    response.put("data", new JSONArray());
    expectedResponse.put("response", response);
    return expectedResponse;
  }
}
