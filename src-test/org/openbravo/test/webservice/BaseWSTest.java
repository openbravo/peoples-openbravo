/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.webservice;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.xml.XMLUtil;
import org.openbravo.test.base.BaseTest;

/**
 * Base class for webservice tests. Provides several methods to do HTTP REST requests.
 * 
 * @author mtaal
 */

public class BaseWSTest extends BaseTest {

  private static final Logger log = Logger.getLogger(BaseWSTest.class);

  private static final String OB_URL = "http://localhost:8080/openbravo";
  private static final String LOGIN = "Openbravo";
  private static final String PWD = "openbravo";

  /**
   * Executes a DELETE HTTP request, the wsPart is appended to the {@link #getOpenbravoURL()}.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param expectedResponse
   *          the expected HTTP response code
   */
  protected void doDirectDeleteRequest(String wsPart, int expectedResponse) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, "DELETE");
      hc.connect();
      assertEquals(expectedResponse, hc.getResponseCode());
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Execute a REST webservice HTTP request which posts/puts content and returns a XML result.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param content
   *          the content (XML) to post or put
   * @param expectedResponse
   *          the expected HTTP response code
   * @param expectedContent
   *          the system check that the returned content contains this expectedContent
   * @param method
   *          POST or PUT
   * @return
   */
  protected String doContentRequest(String wsPart, String content, int expectedResponse,
      String expectedContent, String method) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, method);
      final OutputStream os = hc.getOutputStream();
      os.write(content.getBytes("UTF-8"));
      os.flush();
      os.close();
      hc.connect();

      assertEquals(expectedResponse, hc.getResponseCode());

      if (expectedResponse == 500) {
        // no content available anyway
        return "";
      }
      final SAXReader sr = new SAXReader();
      final InputStream is = hc.getInputStream();
      final Document doc = sr.read(is);
      final String retContent = XMLUtil.getInstance().toString(doc);
      if (retContent.indexOf(expectedContent) == -1) {
        log.debug(retContent);
        fail();
      }
      return retContent;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Convenience method to get a value of a specific XML element without parsing the whole xml
   * 
   * @param content
   *          the xml
   * @param tag
   *          the element name
   * @return the value
   */
  protected String getTagValue(String content, String tag) {
    final int index1 = content.indexOf("<" + tag + ">") + ("<" + tag + ">").length();
    if (index1 == -1) {
      return "";
    }
    final int index2 = content.indexOf("</" + tag + ">");
    if (index2 == -1) {
      return "";
    }
    return content.substring(index1, index2);
  }

  /**
   * Executes a GET request.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param testContent
   *          the system check that the returned content contains this testContent. if null is
   *          passed for this parameter then this check is not done.
   * @param responseCode
   *          the expected HTTP response code
   * @return the content returned from the GET request
   */
  protected String doTestGetRequest(String wsPart, String testContent, int responseCode) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, "GET");
      hc.connect();
      final SAXReader sr = new SAXReader();
      final InputStream is = hc.getInputStream();
      final Document doc = sr.read(is);
      final String content = XMLUtil.getInstance().toString(doc);
      if (testContent != null && content.indexOf(testContent) == -1) {
        log.debug(content);
        fail();
      }
      assertEquals(responseCode, hc.getResponseCode());
      is.close();
      return content;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Creates a HTTP connection.
   * 
   * @param wsPart
   * @param method
   *          POST, PUT, GET or DELETE
   * @return the created connection
   * @throws Exception
   */
  protected HttpURLConnection createConnection(String wsPart, String method) throws Exception {
    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(LOGIN, PWD.toCharArray());
      }
    });
    log.debug(method + ": " + getOpenbravoURL() + wsPart);
    final URL url = new URL(getOpenbravoURL() + wsPart);
    final HttpURLConnection hc = (HttpURLConnection) url.openConnection();
    hc.setRequestMethod(method);
    hc.setAllowUserInteraction(false);
    hc.setDefaultUseCaches(false);
    hc.setDoOutput(true);
    hc.setDoInput(true);
    hc.setInstanceFollowRedirects(true);
    hc.setUseCaches(false);
    hc.setRequestProperty("Content-Type", "text/xml");
    return hc;
  }

  /**
   * Returns the url of the Openbravo instance. The default value is: {@link #OB_URL}
   * 
   * @return the url of the Openbravo instance.
   */
  protected String getOpenbravoURL() {
    return OB_URL;
  }

  /**
   * Returns the login used to login for the webservice. The default value is {@link #LOGIN}.
   * 
   * @return the login name used to login for the webservice
   */
  protected String getLogin() {
    return LOGIN;
  }

  /**
   * Returns the password used to login into the webservice server. The default value is
   * {@link #PWD}.
   * 
   * @return the password used to login into the webservice, the default is {@link #PWD}
   */
  protected String getPassword() {
    return PWD;
  }
}