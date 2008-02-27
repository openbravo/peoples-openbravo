/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base;

import org.openbravo.xmlEngine.XmlEngine;
import org.openbravo.data.FieldProvider;

import java.sql.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import org.xml.sax.*;
import org.apache.fop.messaging.*;
import org.apache.fop.apps.Driver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.rmi.*;
import rmi.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.database.*;
import org.openbravo.exception.*;
import org.apache.commons.pool.ObjectPool;

import org.apache.avalon.framework.logger.Log4JLogger;

import javax.net.ssl.*;

public class HttpBaseServlet extends HttpServlet implements ConnectionProvider
{
  private static final long serialVersionUID = 1L;
  protected ConnectionProvider myPool;
  public String strDireccion;
  public String strReplaceWith;
  public String strReplaceWithFull;
  protected String strDefaultServlet;
  public XmlEngine xmlEngine=null;
  public Logger log4j = Logger.getLogger(this.getClass());
  protected String PoolFileName;

  protected ConfigParameters globalParameters;

  public void init (ServletConfig config) {
    try {
      super.init(config);
      globalParameters = ConfigParameters.retrieveFrom(config.getServletContext());
      strDefaultServlet = globalParameters.strDefaultServlet;
      xmlEngine = new XmlEngine();
      xmlEngine.fileBaseLocation = new File(globalParameters.getBaseDesignPath());
      xmlEngine.strReplaceWhat = globalParameters.strReplaceWhat;
      xmlEngine.strReplaceWith = globalParameters.strLocalReplaceWith;
      log4j.debug("Replace attribute value: \"" + xmlEngine.strReplaceWhat + "\" with: \"" + xmlEngine.strReplaceWith + "\".");
      xmlEngine.strTextDividedByZero = globalParameters.strTextDividedByZero;
      xmlEngine.fileXmlEngineFormat = new File (globalParameters.getXmlEngineFileFormatPath());
      xmlEngine.initialize();

      log4j.debug("Text of divided by zero: " + XmlEngine.strTextDividedByZero);

      myPool = ConnectionProviderContextListener.getPool(config.getServletContext());
    } catch (ServletException e) {
      e.printStackTrace();
    }
  }

  /*
   * Iniatilizes base variables
   */
  public void initialize(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    strDireccion = HttpBaseUtils.getLocalAddress(request);
    String strActualUrl = HttpBaseUtils.getLocalHostAddress(request);
    if(log4j.isDebugEnabled()) log4j.debug("Server name: " + strActualUrl);
    HttpSession session = request.getSession(true);
    String strLanguage = "";
    try {
      strLanguage = (String) session.getAttribute("#AD_LANGUAGE");
      if (strLanguage==null || strLanguage.trim().equals("")) strLanguage = "";
    }
    catch (Exception e) {
      strLanguage = "";
    }
    xmlEngine.fileBaseLocation = new File(getBaseDesignPath(strLanguage));
    strReplaceWith = globalParameters.strLocalReplaceWith.replace("@actual_url@", strActualUrl).replace("@actual_url_context@", strDireccion);
    strReplaceWithFull = strReplaceWith;
    strReplaceWith = HttpBaseUtils.getRelativeUrl(request, strReplaceWith);
    if(log4j.isDebugEnabled()) log4j.debug("xmlEngine.strReplaceWith: " + strReplaceWith);
    xmlEngine.strReplaceWith = strReplaceWith;

  }
  /*
   * calls super.service. It is used after calling initialize.
   */
  public void serviceInitialized(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    super.service(request, response);
  }

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    initialize(request, response);
    if(log4j.isDebugEnabled()) log4j.debug("Call to HttpServlet.service");
    super.service(request,response);
  }

  protected String getBaseDesignPath(String language) {
    log4j.info("*********************Base path: " + globalParameters.strBaseDesignPath);
    String strNewAddBase = globalParameters.strDefaultDesignPath;
    String strFinal = globalParameters.strBaseDesignPath;
    if (!language.equals("") && !language.equals("en_US")) {
      strNewAddBase = language;
    }
    if (!strFinal.endsWith("/" + strNewAddBase)) {
      strFinal += "/" + strNewAddBase;
    }
    log4j.info("*********************Base path: " + strFinal);
    return globalParameters.prefix + strFinal;
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    doPost(request,response);
  }

  public void doGetCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
    doPostCall(request, response);
  }

  public void doPostCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return;
  }



  public Connection getConnection() throws NoConnectionAvailableException {
    return (myPool.getConnection());
  }

  public String getRDBMS() {
    return (myPool.getRDBMS());
  }

  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    return myPool.getTransactionConnection();
  }

  public void releaseCommitConnection(Connection conn) throws SQLException {
    myPool.releaseCommitConnection(conn);
  }

  public void releaseRollbackConnection(Connection conn) throws SQLException {
    myPool.releaseRollbackConnection(conn);
  }

  public PreparedStatement getPreparedStatement(String poolName, String strSql) throws Exception {
    return (myPool.getPreparedStatement(poolName, strSql));
  }

  public PreparedStatement getPreparedStatement(String strSql) throws Exception {
    return (myPool.getPreparedStatement(strSql));
  }

  public PreparedStatement getPreparedStatement(Connection conn, String strSql) throws SQLException {
    return (myPool.getPreparedStatement(conn, strSql));
  }

  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    try {
      myPool.releasePreparedStatement(preparedStatement);
    } catch (Exception ex) {}
  }

  public Statement getStatement(String poolName) throws Exception {
    return (myPool.getStatement(poolName));
  }

  public Statement getStatement() throws Exception {
    return (myPool.getStatement());
  }

  public Statement getStatement(Connection conn) throws SQLException {
    return (myPool.getStatement(conn));
  }

  public void releaseStatement(Statement statement) throws SQLException {
    myPool.releaseStatement(statement);
  }

  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    myPool.releaseTransactionalStatement(statement);
  }

  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    myPool.releaseTransactionalPreparedStatement(preparedStatement);
  }

  public CallableStatement getCallableStatement(String poolName, String strSql) throws Exception {
    return (myPool.getCallableStatement(poolName, strSql));
  }

  public CallableStatement getCallableStatement(String strSql) throws Exception {
    return (myPool.getCallableStatement(strSql));
  }

  public CallableStatement getCallableStatement(Connection conn, String strSql) throws SQLException {
    return (myPool.getCallableStatement(conn, strSql));
  }

  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    myPool.releaseCallableStatement(callableStatement);
  }


  public String getPoolStatus() {
    if( myPool instanceof ConnectionProviderImpl ) {
        return ((ConnectionProviderImpl)myPool).getStatus();
    } else {
        return "Status unavailable";
    }

  }

  /**
   * renders an FO inputsource into a PDF file which is rendered
   * directly to the response object's OutputStream
   */
  public void renderFO(String strFo, HttpServletResponse response) throws ServletException {
    // Check validity of the certificate
    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        @SuppressWarnings("unused") //external implementation
        public boolean isServerTrusted(java.security.cert.X509Certificate[] cert) {return true;}
        @SuppressWarnings("unused") //external implementation
        public boolean isClientTrusted(java.security.cert.X509Certificate[] cert) {return true;}
      }
    };
    // Install the all-trusting trust manager
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
    }

    try {
      if(log4j.isDebugEnabled()) log4j.debug("Beginning of renderFO");
      if (globalParameters.haveFopConfig()) {
        File fopFile = new File(globalParameters.getFopConfigPath());
        if (fopFile.exists()) {
          @SuppressWarnings("unused") //external implementation
          org.apache.fop.apps.Options options = new org.apache.fop.apps.Options(fopFile);
        }
      }
      strFo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + strFo;

      if ((globalParameters.strServidorRenderFo==null) || (globalParameters.strServidorRenderFo.equals(""))) {
        if (log4j.isDebugEnabled()) log4j.debug(strFo);
        StringReader sr = new StringReader(strFo);
        if (log4j.isDebugEnabled()) log4j.debug(sr.toString());
        InputSource inputFO = new InputSource(sr);

        //log4j.info("Beginning of ByteArrayOutputStream");
        if(log4j.isDebugEnabled()) log4j.debug("Beginning of response.setContentType");
        response.setContentType("application/pdf; charset=UTF-8");
        if(log4j.isDebugEnabled()) log4j.debug("Beginning of driver");
        Driver driver = new Driver();
        driver.setLogger(globalParameters.getFopLogger());
        driver.setRenderer(Driver.RENDER_PDF);
        driver.setInputSource(inputFO);

        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        driver.setOutputStream(response.getOutputStream());

        if(log4j.isDebugEnabled()) log4j.debug("driver.run()");
        driver.run();
        /*log4j.info("Beginning of out.toByteArray()");
          byte[] content = out.toByteArray();
          log4j.info("Beginning of response.setContentLength");
          response.setContentLength(content.length);
          log4j.info("Beginning of response.getOutputStream().write(content)");*/
        /*int incr = 1000;
          for (int i=0;i<content.length;i+=incr) {
          int end = ((content.length<(i+incr))?content.length-i:incr);
          response.getOutputStream().write(content, i, end);
          response.getOutputStream().flush();
          }*/
        /*response.getOutputStream().write(content);
          log4j.info("Beginning of response.getOutputStream().flush()");
          response.getOutputStream().flush();*/
        if(log4j.isDebugEnabled()) log4j.debug("End of renderFO");
        response.getOutputStream().flush();
        response.getOutputStream().close();
        sr.close();
        driver.reset();
        driver = null;
      } else {
        response.setContentType("application/pdf; charset=UTF-8");
        RenderFoI render = (RenderFoI) Naming.lookup("rmi://"+ globalParameters.strServidorRenderFo+"/RenderFo");

        byte[] content = render.computeRenderFo(strFo);
        response.setContentLength(content.length);
        /*int incr = 1000;
          for (int i=0;i<content.length;i+=incr) {
          int end = ((content.length<(i+incr))?content.length-i:incr);
          response.getOutputStream().write(content, i, end);
          response.getOutputStream().flush();
          }*/
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
      }
    } catch (java.lang.IllegalStateException il) {
      return;
    } catch (Exception ex) {
      try { response.getOutputStream().flush(); } catch (Exception ignored) {}
      throw new ServletException(ex);
    }
  }

  static XMLReader createParser() throws ServletException {
    String parserClassName = System.getProperty("org.xml.sax.parser");
    if (parserClassName == null) {
      parserClassName = "org.apache.xerces.parsers.SAXParser";
    }
    try {
      return (XMLReader) Class.forName(
          parserClassName).newInstance();
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  public String arrayDobleEntrada(String strNombreArray, FieldProvider[] data) {
    String strArray = "var " + strNombreArray + " = ";
    if (data.length==0) {
      strArray = strArray + "null";
      return strArray;
    }
    strArray = strArray + "new Array(";
    for (int i=0;i<data.length;i++) {
      strArray = strArray + "\nnew Array(\"" + data[i].getField("padre") + "\", \"" +  data[i].getField("id") + "\", \"" +  FormatUtilities.replaceJS(data[i].getField("name")) + "\")";
      if (i<data.length-1) strArray = strArray + ", ";
    }
    strArray = strArray + ");";
    return strArray;
  }

  public String arrayEntradaSimple(String strNombreArray, FieldProvider[] data) {
    String strArray = "var " + strNombreArray + " = ";
    if (data.length==0) {
      strArray = strArray + "null";
      return strArray;
    }
    strArray = strArray + "new Array(";
    for (int i=0;i<data.length;i++) {
      strArray = strArray + "\nnew Array(\"" + data[i].getField("id") + "\", \"" +  FormatUtilities.replaceJS(data[i].getField("name")) + "\")";
      if (i<data.length-1) strArray = strArray + ", ";
    }
    strArray = strArray + ");";
    return strArray;
  }

  public String getServletInfo() {
    return "This servlet add some functions (connection to data base, xmlEngine, loging) over HttpServlet";
  }
}
