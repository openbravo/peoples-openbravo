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
 * All portions are Copyright (C) 2022-2023 Openbravo SLU
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
import static org.mockito.Mockito.verify;
import static org.openbravo.test.base.TestConstants.Orgs.MAIN;
import static org.openbravo.test.matchers.json.JSONMatchers.equal;
import static org.openbravo.test.matchers.json.JSONMatchers.matchesObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.StringEnumerateDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Protocol;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.service.externalsystem.ExternalSystem;
import org.openbravo.service.externalsystem.ExternalSystem.Operation;
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
@Ignore("Fails with JDK 22")
public class HttpExternalSystemCommunicationTest extends WeldBaseTest {

  @Inject
  private ExternalSystemProvider externalSystemProvider;

  private ExternalSystemData externalSystemData;
  private HttpExternalSystemData httpExternalSystemData;
  ArgumentCaptor<Supplier<HttpRequest>> requestCaptor;

  @Before
  @SuppressWarnings("unchecked")
  public void init() {
    setTestAdminContext();
    createTestData();

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

    requestCaptor = ArgumentCaptor.forClass(Supplier.class);
  }

  private void createTestData() {
    Country newCountry = OBProvider.getInstance().get(Country.class);
    newCountry.setName("Wonderland");
    newCountry.setISOCountryCode("WL");
    newCountry.setAddressPrintFormat("-");
    OBDal.getInstance().save(newCountry);
    OBDal.getInstance().commitAndClose();
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
    deleteTestData();
  }

  private void deleteTestData() {
    OBDal.getInstance()
        .getSession()
        .createQuery("DELETE FROM Country WHERE iSOCountryCode IN ('WL', 'TE')")
        .executeUpdate();
    OBDal.getInstance().commitAndClose();
  }

  @Test
  public void sendWithBasicAuth() throws JSONException, ServletException {
    ExternalSystemResponse response = sendWithBasicCredentials("BASIC");

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    assertThat("Expected Response Data", (JSONObject) response.getData(),
        equal(getExpectedResponseData()));
  }

  @Test
  @Issue("49159")
  public void sendWithBasicAuthAlwaysInHeader() throws JSONException, ServletException {
    ExternalSystemResponse response = sendWithBasicCredentials("BASIC_ALWAYS_HEADER");

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    assertThat("Expected Response Data", (JSONObject) response.getData(),
        equal(getExpectedResponseData()));
  }

  private ExternalSystemResponse sendWithBasicCredentials(String authorizationType)
      throws JSONException, ServletException {
    return getExternalSystem(authorizationType).send(getRequestDataSupplier()).join();
  }

  @Test
  public void sendUnauthorized() throws JSONException {
    httpExternalSystemData.setAuthorizationType("NOAUTH");

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();
    ExternalSystemResponse response = externalSystem.send(getRequestDataSupplier()).join();

    assertResponse(response, ExternalSystemResponse.Type.ERROR,
        HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void cannotSendWithUnsupportedRequestMethod() {
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

    assertResponse(response, ExternalSystemResponse.Type.ERROR, 0);
    assertThat("Expected Response Error", response.getError().toString(),
        startsWith("java.net.ConnectException"));
  }

  @Test
  @Issue("53077")
  public void sendWithDeleteOperation() throws ServletException {
    HttpExternalSystem externalSystem = getExternalSystem();
    String path = getCountry("WL").getId();

    ExternalSystemResponse response = externalSystem.send(Operation.DELETE, path).join();

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    verifyRequestMethod(externalSystem, "DELETE");
  }

  @Test
  @Issue("53077")
  public void sendWithReadOperation() throws ServletException {
    HttpExternalSystem externalSystem = getExternalSystem();
    String path = getCountry("WL").getId();

    ExternalSystemResponse response = externalSystem.send(Operation.READ, path).join();

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    verifyRequestMethod(externalSystem, "GET");
    assertThat("Expected Response Data", (JSONObject) response.getData(),
        matchesObject(new JSONObject(Map.of("iSOCountryCode", "WL"))));
  }

  @Test
  @Issue("53077")
  public void sendWithQueryParams() throws ServletException, JSONException {
    HttpExternalSystem externalSystem = getExternalSystem();

    ExternalSystemResponse response = externalSystem
        .send(Operation.READ, Map.of("queryParameters", Map.of("_where", "iSOCountryCode='WL'")))
        .join();

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    verifyRequestMethod(externalSystem, "GET");
    JSONArray data = ((JSONObject) response.getData()).getJSONObject("response")
        .getJSONArray("data");
    assertThat("Expected Response Data", data.getJSONObject(0),
        matchesObject(new JSONObject(Map.of("iSOCountryCode", "WL"))));
  }

  @Test
  @Issue("53077")
  public void sendWithCreateOperation() throws ServletException, JSONException {
    HttpExternalSystem externalSystem = getExternalSystem();
    JSONObject requestData = new JSONObject();
    requestData.put("data",
        new JSONObject(Map.of("name", "Test", "iSOCountryCode", "TE", "addressPrintFormat", "-")));

    ExternalSystemResponse response = externalSystem
        .send(Operation.CREATE, getRequestDataSupplier(requestData))
        .join();

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    verifyRequestMethod(externalSystem, "POST");
  }

  @Test
  @Issue("53077")
  public void sendWithUpdateOperation() throws ServletException, JSONException {
    HttpExternalSystem externalSystem = getExternalSystem();
    JSONObject requestData = new JSONObject();
    requestData.put("data",
        new JSONObject(Map.of("id", getCountry("WL").getId(), "description", "hi")));

    ExternalSystemResponse response = externalSystem
        .send(Operation.UPDATE, getRequestDataSupplier(requestData))
        .join();

    assertResponse(response, ExternalSystemResponse.Type.SUCCESS, HttpServletResponse.SC_OK);
    verifyRequestMethod(externalSystem, "PUT");
  }

  @Test
  @Issue("53077")
  public void cannotSendPayloadInGetRequests() {
    OBException exceptionRule = assertThrows(OBException.class,
        () -> getExternalSystem().send(Operation.READ, getRequestDataSupplier(new JSONObject()))
            .join());

    assertThat(exceptionRule.getMessage(), equalTo("GET requests do not accept a payload"));
  }

  @Test
  @Issue("53077")
  public void cannotSendPayloadInDeleteRequests() {
    OBException exceptionRule = assertThrows(OBException.class,
        () -> getExternalSystem().send(Operation.DELETE, getRequestDataSupplier(new JSONObject()))
            .join());

    assertThat(exceptionRule.getMessage(), equalTo("DELETE requests do not accept a payload"));
  }

  private HttpExternalSystem getExternalSystem() throws ServletException {
    return getExternalSystem("BASIC");
  }

  private HttpExternalSystem getExternalSystem(String authorizationType) throws ServletException {
    httpExternalSystemData.setAuthorizationType(authorizationType);
    httpExternalSystemData.setUsername("Openbravo");
    httpExternalSystemData.setPassword(FormatUtilities.encryptDecrypt("openbravo", true));

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();
    return Mockito.spy((HttpExternalSystem) externalSystem);
  }

  private Country getCountry(String isoCode) {
    return (Country) OBDal.getInstance()
        .createCriteria(Country.class)
        .add(Restrictions.eq(Country.PROPERTY_ISOCOUNTRYCODE, isoCode))
        .setFilterOnActive(false)
        .setMaxResults(1)
        .uniqueResult();
  }

  private Supplier<InputStream> getRequestDataSupplier() throws JSONException {
    JSONObject requestData = new JSONObject();
    requestData.put("data", new JSONArray());
    return getRequestDataSupplier(requestData);
  }

  private Supplier<InputStream> getRequestDataSupplier(JSONObject requestData) {
    return () -> new ByteArrayInputStream(requestData.toString().getBytes());
  }

  private void assertResponse(ExternalSystemResponse response, ExternalSystemResponse.Type type,
      int statusCode) {
    assertThat("Has Expected Response Type", response.getType(), equalTo(type));
    assertThat("Expected Response Status Code", response.getStatusCode(), equalTo(statusCode));
  }

  private void verifyRequestMethod(HttpExternalSystem externalSystemSpy, String method) {
    verify(externalSystemSpy, Mockito.times(1)).sendRequest(requestCaptor.capture());
    HttpRequest request = requestCaptor.getValue().get();
    assertThat("Expected Request Method", request.method(), equalTo(method));
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
