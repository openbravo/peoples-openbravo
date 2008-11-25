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

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.xml.XMLUtil;
import org.openbravo.test.base.BaseTest;

/**
 * Base class for webservice tests, mainly provides utility methods.
 * 
 * @author mtaal
 */

public class BaseWSTest extends BaseTest {

    private static final String OB_URL = "http://localhost:8080/openbravo";
    private static final String LOGIN = "Openbravo";
    private static final String PWD = "openbravo";

    protected void doDirectDeleteRequest(String wsPart, int expectedResponse) {
        try {
            setErrorOccured(true);
            final HttpURLConnection hc = createConnection(wsPart, "DELETE");
            hc.connect();
            assertEquals(expectedResponse, hc.getResponseCode());
            setErrorOccured(false);
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

    protected String doContentRequest(String wsPart, String content,
            int expectedResponse, String expectedContent, String method) {
        try {
            setErrorOccured(true);
            final HttpURLConnection hc = createConnection(wsPart, method);
            final OutputStream os = hc.getOutputStream();
            os.write(content.getBytes("UTF-8"));
            os.flush();
            os.close();
            hc.connect();

            final SAXReader sr = new SAXReader();
            final InputStream is = hc.getInputStream();
            final Document doc = sr.read(is);
            final String retContent = XMLUtil.getInstance().toString(doc);
            if (retContent.indexOf(expectedContent) == -1) {
                System.err.println(retContent);
                fail();
            }
            assertEquals(expectedResponse, hc.getResponseCode());
            setErrorOccured(false);
            return retContent;
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

    protected String getTagValue(String content, String tag) {
        final int index1 = content.indexOf("<" + tag + ">")
                + ("<" + tag + ">").length();
        if (index1 == -1) {
            return "";
        }
        final int index2 = content.indexOf("</" + tag + ">");
        if (index2 == -1) {
            return "";
        }
        return content.substring(index1, index2);
    }

    protected String doTestGetRequest(String wsPart, String testContent,
            int responseCode) {
        try {
            setErrorOccured(true);
            final HttpURLConnection hc = createConnection(wsPart, "GET");
            hc.connect();
            final SAXReader sr = new SAXReader();
            final InputStream is = hc.getInputStream();
            final Document doc = sr.read(is);
            final String content = XMLUtil.getInstance().toString(doc);
            if (testContent != null && content.indexOf(testContent) == -1) {
                System.err.println(content);
                fail();
            }
            assertEquals(responseCode, hc.getResponseCode());
            is.close();
            setErrorOccured(false);
            return content;
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

    protected HttpURLConnection createConnection(String wsPart, String method)
            throws Exception {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(LOGIN, PWD.toCharArray());
            }
        });
        System.err.println(method + ": " + OB_URL + wsPart);
        final URL url = new URL(OB_URL + wsPart);
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

}