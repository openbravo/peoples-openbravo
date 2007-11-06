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
  protected static PeriodicGarbageCollector myGc;
  protected static ConnectionProviderImpl myPool;
  protected static int isDefinedBasePath = 0;
  public String strBaseConfigPath;
  public String strBaseDesignPath;
  public static boolean isFullPathBaseDesignPath = false;
  public String strDefaultDesignPath;
  public String strDireccion;
  public String strReplaceWith;
  public String strReplaceWithFull;
  private String strLocalReplaceWith;
  public String strFopConfig;
  public String strGarbageCollectionTime;
  protected static String strBBDD;
  public String strVersion;
  public String strParentVersion;
  protected static String prefix = null;
  protected static String strContext = null;
  protected static String strSystemLanguage;
  protected static String strFileProperties;
  protected static String strFileSeparator;
  protected static String strDefaultServlet;
  public XmlEngine xmlEngine=null;
  public Logger log4j = Logger.getLogger(this.getClass());
  protected static Log4JLogger logger;
  String strServidorRenderFo = "";
  protected static String stcFileProperties = null;
  protected String PoolFileName;

  public void init (ServletConfig config) {
    try {
      super.init(config);
      strBaseConfigPath = config.getServletContext().getInitParameter("BaseConfigPath");
      if (prefix == null) {
        prefix =  config.getServletContext().getRealPath("/");
        if (log4j.isDebugEnabled()) log4j.debug("************************prefix: " + prefix);
        if (strContext==null || strContext.equals("")) {
          String path = "/";
          int secondPath = -1;
          int firstPath = prefix.lastIndexOf(path);
          if (firstPath==-1) {
            path = "\\";
            firstPath = prefix.lastIndexOf(path);
          }
          if (firstPath!=-1) {
            secondPath = prefix.lastIndexOf(path, firstPath-1);
            strContext = prefix.substring(secondPath+1, firstPath);
          }
        }
        if (log4j.isDebugEnabled()) log4j.debug("context: " + strContext);
        String file = config.getServletContext().getInitParameter("log4j-init-file");
        if (log4j.isDebugEnabled()) log4j.debug("Log file: " + file);
        // if the log4j-init-file is not set, then no point in trying
        if(file != null) {
          //PropertyConfigurator.configure(prefix+file);
          PropertyConfigurator.configure(prefix + "/" + strBaseConfigPath + "/" + file);
        }
      }
      stcFileProperties = prefix + "/" + strBaseConfigPath + "/" + "Openbravo.properties";
      logger = new Log4JLogger(log4j);
      MessageHandler.setQuiet(true);
      MessageHandler.setScreenLogger(logger);
      String strFileFormat = config.getServletContext().getInitParameter("FormatFile");
      strFopConfig = config.getServletContext().getInitParameter("FOPConfig");
      strBaseDesignPath = config.getServletContext().getInitParameter("BaseDesignPath");
      strDefaultDesignPath = config.getServletContext().getInitParameter("DefaultDesignPath");
      strDefaultServlet = config.getServletContext().getInitParameter("DefaultServlet");
      strGarbageCollectionTime = config.getServletContext().getInitParameter("GarbageCollectionTime");
      log4j.info("BaseConfigPath: " + strBaseConfigPath);
      log4j.info("BaseDesignPath: " + strBaseDesignPath);
      strVersion = config.getServletContext().getInitParameter("Version");
      strParentVersion = config.getServletContext().getInitParameter("Parent_Version");
      try {
        strSystemLanguage = System.getProperty("user.language") + "_" + System.getProperty("user.country");
      } catch (java.security.AccessControlException err) {
        log4j.warn(err.getMessage());
        strSystemLanguage = "en_US";
      }
      try {
        strFileSeparator = System.getProperty("file.separator");
      } catch (java.security.AccessControlException err) {
        log4j.warn(err.getMessage());
        strFileSeparator = "/";
      }
      try {
        strFileProperties = System.getProperty("user.home") + strFileSeparator + "TAD.properties";
      } catch (java.security.AccessControlException err) {
        log4j.warn(err.getMessage());
        strFileProperties = "";
      }
      xmlEngine = new XmlEngine();
      if (isDefinedBasePath==0) {
        try {
          File testPrefix = new File(strBaseDesignPath);
          if (!testPrefix.exists()) isFullPathBaseDesignPath = false;
          else isFullPathBaseDesignPath = true;
          testPrefix = null;
        } catch (Exception e) {
      	  isFullPathBaseDesignPath = false;
        }
        isDefinedBasePath = 1;
      }
      
      xmlEngine.fileBaseLocation = new File(isFullPathBaseDesignPath?strBaseDesignPath:(prefix + "/" + strBaseDesignPath));
      xmlEngine.strReplaceWhat = config.getServletContext().getInitParameter("ReplaceWhat");
      strLocalReplaceWith = config.getServletContext().getInitParameter("ReplaceWith");
      xmlEngine.strReplaceWith = strLocalReplaceWith;
      log4j.info("Replace attribute value: \"" + xmlEngine.strReplaceWhat + "\" with: \"" + xmlEngine.strReplaceWith + "\".");
      XmlEngine.strTextDividedByZero = config.getServletContext().getInitParameter("TextDividedByZero");
      xmlEngine.fileXmlEngineFormat = new File (prefix + "/" + strBaseConfigPath + "/" + strFileFormat);
      xmlEngine.initialize();
      strServidorRenderFo = config.getServletContext().getInitParameter("ServidorRenderFo");

      log4j.info("Text of divided by zero: " + XmlEngine.strTextDividedByZero);

      if(myPool == null) {
        try {
          PoolFileName = config.getServletContext().getInitParameter("PoolFile");
          makeConnection();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if((myGc == null)&&(strGarbageCollectionTime!=null && !strGarbageCollectionTime.equals(""))) { // Only created by first servlet to call
        try {
          String garbageCollectionTime  = strGarbageCollectionTime;
          log4j.info("garbageCollectionTime: " + garbageCollectionTime);

          PeriodicGarbageCollector myLocalGc = new PeriodicGarbageCollector(Long.parseLong(garbageCollectionTime));
          if (myLocalGc==null)
            log4j.error("Could not start the garbage collector: ");
          myGc = myLocalGc;
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
  }

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    strDireccion = HttpBaseUtils.getLocalAddress(request);
    String strActualUrl = HttpBaseUtils.getLocalHostAddress(request);
    log4j.info("Server name: " + strActualUrl);
    HttpSession session = request.getSession(true);
    String strLanguage = "";
    try {
      strLanguage = (String) session.getAttribute("#AD_LANGUAGE");
      if (strLanguage==null || strLanguage.trim().equals("")) strLanguage = "";
    }
    catch (Exception e) {
      strLanguage = "";
    }
    if (strBaseDesignPath.endsWith("/")) strDefaultDesignPath = strDefaultDesignPath.substring(0, strDefaultDesignPath.length()-1);
    log4j.info("*********************Base path: " + strBaseDesignPath);
    String strNewAddBase = strDefaultDesignPath;
    String strFinal = strBaseDesignPath;
    if (!strLanguage.equals("") && !strLanguage.equals("en_US")) strNewAddBase = strLanguage;
    if (!strFinal.endsWith("/" + strNewAddBase)) strFinal += "/" + strNewAddBase;
    log4j.info("*********************Base path: " + strFinal);
    xmlEngine.fileBaseLocation = new File(isFullPathBaseDesignPath?strFinal:(prefix + "/" + strFinal));
    strReplaceWith = strLocalReplaceWith.replace("@actual_url_context@", strDireccion);
    strReplaceWith = strReplaceWith.replace("@actual_url@", strActualUrl);
    strReplaceWithFull = strReplaceWith;
    strReplaceWith = HttpBaseUtils.getRelativeUrl(request, strReplaceWith);
    log4j.info("xmlEngine.strReplaceWith: " + strReplaceWith);
    xmlEngine.strReplaceWith = strReplaceWith;

    log4j.info("Call to HttpServlet.service");
    super.service(request,response);
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

  public static String getJavaDateTimeFormat() {
    String javaDateTimeFormat = "dd-MM-yyyy HH:mm:ss";
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(stcFileProperties));
      System.out.println("***************"+stcFileProperties);
      javaDateTimeFormat = properties.getProperty("dateTimeFormat.java");
    } catch (IOException e) { 
      // catch possible io errors from readLine()
      System.out.println("Uh oh, got an IOException error!");
      e.printStackTrace();
    }
    return javaDateTimeFormat;
  }


  /* Database access utilities
  */
  public static ConnectionProviderImpl getPoolWS() throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException("Default pool not found");
    else
      return myPool;
  }
  @SuppressWarnings("unused") // It will be used to implement the Pool status service
  private ObjectPool getPool(String poolName) throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException("Default pool not found");
    else
      return myPool.getPool(poolName);
  }

  @SuppressWarnings("unused") // It will be used to implement the Pool status service
  private ObjectPool getPool() throws PoolNotFoundException {
    if (myPool == null)
      throw new PoolNotFoundException("Default pool not found");
    else
      return myPool.getPool();
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
    myPool.releasePreparedStatement(preparedStatement);
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
    return myPool.getStatus();
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
      log4j.info("Beginning of renderFO");
      if (strBaseDesignPath!=null && strFopConfig!=null) {
        File fopFile = new File(prefix + "/" + strBaseConfigPath + "/" + strFopConfig);
        if (fopFile.exists()) {
          @SuppressWarnings("unused") //external implementation
		org.apache.fop.apps.Options options = new org.apache.fop.apps.Options(new File(prefix + "/" + strBaseConfigPath + "/" + strFopConfig));
        }
      }
      strFo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + strFo;

      if ((strServidorRenderFo==null) || (strServidorRenderFo.equals(""))) {
        if (log4j.isDebugEnabled()) log4j.debug(strFo);
        StringReader sr = new StringReader(strFo);
        if (log4j.isDebugEnabled()) log4j.debug(sr.toString());
        InputSource inputFO = new InputSource(sr);

        //log4j.info("Beginning of ByteArrayOutputStream");
        log4j.info("Beginning of response.setContentType");
        response.setContentType("application/pdf; charset=UTF-8");
        log4j.info("Beginning of driver");
        Driver driver = new Driver();
        driver.setLogger(logger);
        driver.setRenderer(Driver.RENDER_PDF);
        driver.setInputSource(inputFO);

        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        driver.setOutputStream(response.getOutputStream());

        log4j.info("driver.run()");
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
        log4j.info("End of renderFO");
        response.getOutputStream().flush();
        response.getOutputStream().close();
        sr.close();
        driver.reset();
        driver = null;
      } else {
        response.setContentType("application/pdf; charset=UTF-8");
        RenderFoI render = (RenderFoI) Naming.lookup("rmi://"+strServidorRenderFo+"/RenderFo");

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

  public void makeConnection() throws PoolNotFoundException {
    if (myPool != null) {
      try {
        myPool.destroy();
      } catch (Exception ignored) {}
      myPool = null;
    }
    try {
      String strPoolFile = prefix + "/" + strBaseConfigPath + "/" + PoolFileName;
      myPool = new ConnectionProviderImpl(strPoolFile, (!strPoolFile.startsWith("/") && !strPoolFile.substring(1,1).equals(":")), strContext);
    } catch (Exception e) {
      throw new PoolNotFoundException(e.getMessage());
    }
  }

  public String getServletInfo() {
    return "This servlet add some functions (connection to data base, xmlEngine, loging) over HttpServlet";
  }
}
