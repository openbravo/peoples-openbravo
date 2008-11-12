 /******************************************************************************
  * The contents of this file are subject to the   Compiere License  Version 1.1
  * ("License"); You may not use this file except in compliance with the License
  * You may obtain a copy of the License at http://www.compiere.org/license.html
  * Software distributed under the License is distributed on an  "AS IS"  basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.
  * The Original Code is                  Compiere  ERP & CRM  Business Solution
  * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
  * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
  * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
  * Contributor(s): Openbravo SL
  * Contributions are Copyright (C) 2001-2008 Openbravo S.L.
  ******************************************************************************/
package org.openbravo.erpCommon.ad_forms;

import java.io.*;
import java.sql.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import javax.servlet.*;
import javax.servlet.http.*;
//import org.apache.log4j.Category;

import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.ad_combos.LanguageComboData;
import org.openbravo.erpCommon.ad_combos.ClientComboData;




/**
 * Class for import/export languages.
 * 
 * The tree of languages is:
 * 
 * {attachmentsDir}
 *   {laguageFolder}
 *      {moduleFolder}
 *      
 * Example:
 *  /opt/attachments/
 *    en_US/
 *      <trl tables from core>
 *      module1/
 *        <trl tables from module1>
 * 
 */
public class Translation extends HttpSecureAppServlet
{
  private static final long serialVersionUID = 1L;
  /**  XML Element Tag      */
  public static final String  XML_TAG = "compiereTrl";
  /**  XML Attribute Table      */
  public static final String  XML_ATTRIBUTE_TABLE = "table";
  /** XML Attribute Language    */
  public static final String  XML_ATTRIBUTE_LANGUAGE = "language";
  /** XML row attribute original language*/
  public static final String  XML_ATTRIBUTE_BASE_LANGUAGE = "baseLanguage";
  /** XML Attribute Version   */
  public static final String  XML_ATTRIBUTE_VERSION = "version";
  /**  XML Row Tag          */
  
  public static final String  XML_ROW_TAG = "row";
  /** XML Row Attribute ID    */
  public static final String  XML_ROW_ATTRIBUTE_ID = "id";
  /** XML Row Attribute Translated  */
  public static final String  XML_ROW_ATTRIBUTE_TRANSLATED = "trl";


  /**  XML Value Tag        */
  public static final String  XML_VALUE_TAG = "value";
  /** XML Value Column      */
  public static final String  XML_VALUE_ATTRIBUTE_COLUMN = "column";
  /** XML Value Original      */
  public static final String  XML_VALUE_ATTRIBUTE_ORIGINAL = "original";
  
  public static final String CONTRIBUTORS_FILENAME = "CONTRIBUTORS";
  public static final String XML_CONTRIB = "Contributors";

  /**  Table is centrally maintained  */
  private boolean      m_IsCentrallyMaintained = false;
  
  static Logger translationlog4j;
  static ConnectionProvider cp;


  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"); //added for JDK1.5
    setLog4j(log4j);
    setConnectionProvicer(this);
    if (vars.commandIn("DEFAULT"))
    {
       printPageDataSheet(response, vars);
    }
    else if (vars.commandIn("EXPORT"))  {
      
      String strLang = vars.getRequestGlobalVariable("language", "translation.lang");
      String strClient = vars.getRequestGlobalVariable("client", "translation.client");
      if (log4j.isDebugEnabled()) log4j.debug("Lang "+strLang+" Client "+strClient);
     
      //New message system
      OBError myMessage = exportTrl(strLang,strClient, vars);
      
      if (log4j.isDebugEnabled()) log4j.debug("message:"+myMessage.getMessage());
        vars.setMessage("Translation", myMessage);
        response.sendRedirect(strDireccion + request.getServletPath());
      
     }
     else  {
       String strLang = vars.getRequestGlobalVariable("language", "translation.lang");
       String strClient = vars.getRequestGlobalVariable("client", "translation.client");
      if (log4j.isDebugEnabled()) log4j.debug("Lang "+strLang+" Client "+strClient);

      String directory = globalParameters.strFTPDirectory+"/lang/"+strLang+"/";
      OBError myMessage = importTrlDirectory(directory, strLang, strClient, vars);
      if (log4j.isDebugEnabled()) log4j.debug("message:"+myMessage.getMessage());
      vars.setMessage("Translation", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());

     }
  }


  public static void setConnectionProvicer(ConnectionProvider conn) {
    cp = conn;
  }


  public static void setLog4j(Logger logger) {
    translationlog4j = logger;
  }


  /**
   * Export all the trl tables that refers to tables with ad_module_id column
   * or trl tables that refers to tables with a parent table with ad_module_id column
   * 
   * For example: If a record from ad_process is in module "core", the records from 
   * ad_process_trl and ad_process_para_trl are exported in "core" module
   * 
   * @param strLang Language to export.
   * @param strClient Client to export.
   * @param vars Handler for the session info.
   * @return Message with the error or with the success
   */
  private OBError exportTrl(String strLang, String strClient, VariablesSecureApp vars) {
    String AD_Language = strLang;
    OBError myMessage = null;    
    
    myMessage = new OBError();
    myMessage.setTitle("");
     int AD_Client_ID = Integer.valueOf(strClient);

      String strFTPDirectory = globalParameters.strFTPDirectory;

     if (new File(strFTPDirectory).canWrite()) {
       if (log4j.isDebugEnabled()) log4j.debug("can write...");
     } else {
       log4j.error("Can't write on directory: "+strFTPDirectory);
       myMessage.setType("Error");        
       myMessage.setMessage(Utility.messageBD(this, "CannotWriteDirectory", vars.getLanguage())+" "+strFTPDirectory);
       return myMessage; 
     }
     
    (new File(strFTPDirectory+"/lang")).mkdir();
    String rootDirectory = strFTPDirectory+"/lang/";
    String directory = strFTPDirectory+"/lang/"+AD_Language+"/";    
    (new File(directory)).mkdir();

    if (log4j.isDebugEnabled()) log4j.debug("directory "+directory);
   
    try{
      TranslationData[] modulesTables = TranslationData.trlModulesTables(this);
      for (int i=0; i<modulesTables.length; i++ ){        
        exportModulesTrl(rootDirectory, AD_Client_ID, AD_Language, modulesTables[i].c);
      }
      exportContibutors(directory, AD_Language);
    }catch (Exception e)
    {
      log4j.error(e.toString());
      myMessage.setType("Error");        
      myMessage.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
      return myMessage;
    }
    myMessage.setType("Success");        
    myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    return myMessage;
  }
  
  /**
   * 
   * The import process insert in database all the translations found in the folder
   *  of the defined language RECURSIVELY.
   * It don't take into account if a module is marked o no as isInDevelopment.
   * Only search for trl's xml files corresponding with trl's tables in database. 
   *  
   * 
   * @param directory Directory for trl's xml files
   * @param strLang Language to import
   * @param strClient Client to import
   * @param vars Handler for the session info.
   * @return Message with the error or with the success
   */
  public static OBError importTrlDirectory(String directory, String strLang, String strClient, VariablesSecureApp vars) {
    String AD_Language = strLang;

    OBError myMessage = null;    
    myMessage = new OBError();
    myMessage.setTitle("");
    
    String UILanguage = vars==null?"en_US":vars.getLanguage();
       
    if ((new File(directory).exists())&&(new File(directory).canRead())) {
      if (translationlog4j.isDebugEnabled()) translationlog4j.debug("can read "+directory);
    } else {
      translationlog4j.error("Can't read on directory: "+directory);
      myMessage.setType("Error");        
      myMessage.setMessage(Utility.messageBD(cp, "CannotReadDirectory", UILanguage)+" "+directory);
      return myMessage;
    }
    
    int AD_Client_ID = Integer.valueOf(strClient);
    try{
      TranslationData[] tables = TranslationData.trlTables(cp);
      for (int i=0; i<tables.length; i++ )
        importTrlFile( directory, AD_Client_ID, AD_Language, tables[i].c);
      importContributors(directory,AD_Language);
    }catch (Exception e)
    {
      translationlog4j.error(e.toString());
      myMessage.setType("Error");        
      myMessage.setMessage(Utility.messageBD(cp, "Error", UILanguage));
      return myMessage;
    }
    
    File file = new File(directory);
    File [] list = file.listFiles();
    for(int f=0; f<list.length; f++){
      if(list[f].isDirectory()){
        OBError subDirError = importTrlDirectory(list[f].toString()+"/", strLang, strClient, vars) ;
        if( !"Success".equals(subDirError.getType()) )
          return subDirError; 
      }
    }

    myMessage.setType("Success");        
    myMessage.setMessage(Utility.messageBD(cp, "Success", UILanguage));
    return myMessage;
  }
 
  private void exportContibutors(String directory, String AD_Language) {
    File out = new File(directory, CONTRIBUTORS_FILENAME+"_" + AD_Language + ".xml");
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Element root = document.createElement(XML_CONTRIB);
      root.setAttribute(XML_ATTRIBUTE_LANGUAGE, AD_Language);
      document.appendChild(root);
      root.appendChild(document.createTextNode(TranslationData.selectContributors(this, AD_Language)));
      DOMSource source = new DOMSource(document);
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();
      //  Output
      out.createNewFile();
      StreamResult result = new StreamResult(out);
      //  Transform
      transformer.transform (source, result);
    } catch (Exception e)  {
      log4j.error("exportTrl", e);
    }
  }

  private String exportModulesTrl(String rootDirectory, int AD_Client_ID, String AD_Language, String Trl_Table) {
    String directory = "";
    try {
      TranslationData[] modules = TranslationData.modules(this);
      for (int mod=0; mod<modules.length; mod++ ){
        Statement st = null;
        try {
          String moduleLanguage = TranslationData.selectModuleLang(this, modules[mod].adModuleId);
          if (moduleLanguage != null && !moduleLanguage.equals("") && !moduleLanguage.equals(AD_Language)) { //Translate only languages different than the modules's one
            String ad_module_id = modules[mod].adModuleId;
            String ad_module_value = modules[mod].value;
            
            String strParentTable = null;
            TranslationData[] parentTable = TranslationData.parentTable(this, Trl_Table);
            if(parentTable.length > 0){
              strParentTable = parentTable[0].tablename;
            }
            
            /**
             *  core    ->  /lang/en_GB/
             *  modules ->  /lang/en_GB/modValue/ 
             */
            if (ad_module_id.equals("0")) directory = rootDirectory + AD_Language + "/";
            else directory = rootDirectory + AD_Language + "/" + ad_module_value + "/";    
            (new File(directory)).mkdir();
      
            String fileName = directory + Trl_Table + "_" + AD_Language + ".xml";
            log4j.info("exportTrl - " + fileName);
            File out = new File(fileName);
            
            String tableName = Trl_Table;
            int pos = tableName.indexOf("_TRL");
            String Base_Table = Trl_Table.substring(0, pos);
            log4j.info("table - " + tableName);
            String keyColumn = Base_Table + "_ID";
            TranslationData[] trlColumns = getTrlColumns (Base_Table);
            //
            StringBuffer sql = null;
          
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
  
            //  Root
            Element root = document.createElement(XML_TAG);
            root.setAttribute(XML_ATTRIBUTE_LANGUAGE, AD_Language);
            root.setAttribute(XML_ATTRIBUTE_TABLE, Base_Table);
            root.setAttribute(XML_ATTRIBUTE_BASE_LANGUAGE, moduleLanguage);
            root.setAttribute(XML_ATTRIBUTE_VERSION, TranslationData.version (this));
            document.appendChild(root);
            log4j.info("exportTrl - kk ");
            //
            sql = new StringBuffer ("SELECT ");
            
            sql.append("t.IsTranslated,");
            
            sql.append("t.").append(keyColumn);       //  2
            //
            for (int i = 0; i < trlColumns.length; i++)
              sql.append(", t.").append(trlColumns[i].c)
                .append(",o.").append(trlColumns[i].c).append(" AS ").append(trlColumns[i].c).append("O");
            //
            sql.append(" FROM ").append(tableName).append(" t")
              .append(" INNER JOIN ").append(Base_Table)
              .append(" o ON (t.").append(keyColumn).append("=o.").append(keyColumn).append(")");
            boolean haveWhere = false;
            
            sql.append(" WHERE t.AD_Language='"+AD_Language+"'");
            haveWhere = true;
            
            if (m_IsCentrallyMaintained)
            {
              sql.append (haveWhere ? " AND " : " WHERE ").append ("o.IsCentrallyMaintained='N'");
              haveWhere = true;
            }
            if (AD_Client_ID >= 0){
              sql.append(haveWhere ? " AND " : " WHERE ").append("o.AD_Client_ID='").append(AD_Client_ID).append("'");
              haveWhere = true;
            }
            
            if(strParentTable == null){
              sql.append(haveWhere ? " AND " : " WHERE ").append(" o.ad_module_id='").append(ad_module_id).append("'");
            } else {
              /** Search for ad_module_id in the parent table  */
              sql.append(haveWhere ? " AND " : " WHERE ");
              sql.append(" exists ( select 1 from ").append(strParentTable).append(" p ");
              sql.append("   where p.").append(strParentTable+"_ID").append("=").append("o."+strParentTable+"_ID");
              sql.append("   and p.ad_module_id='").append(ad_module_id).append("')");
            }
            sql.append(" ORDER BY t.").append(keyColumn);
            //
  
            if (log4j.isDebugEnabled()) log4j.debug("SQL:"+sql.toString());
            st = this.getStatement();
            if (log4j.isDebugEnabled()) log4j.debug("st");
  
            ResultSet rs = st.executeQuery(sql.toString());
            if (log4j.isDebugEnabled()) log4j.debug("rs");
            int rows = 0;
            while (rs.next())
            {
              Element row = document.createElement (XML_ROW_TAG);
              row.setAttribute(XML_ROW_ATTRIBUTE_ID, String.valueOf(rs.getString(2)));  //  KeyColumn
              row.setAttribute(XML_ROW_ATTRIBUTE_TRANSLATED, rs.getString(1));    //  IsTranslated
              for (int i = 0; i < trlColumns.length; i++)
              {
                Element value = document.createElement (XML_VALUE_TAG);
                value.setAttribute(XML_VALUE_ATTRIBUTE_COLUMN, trlColumns[i].c);
                String origString = rs.getString(trlColumns[i].c + "O");      //  Original Value
                if (origString == null)
                  origString = "";
                String valueString = rs.getString(trlColumns[i].c);       //  Value
                if (valueString == null)
                  valueString = "";
                value.setAttribute(XML_VALUE_ATTRIBUTE_ORIGINAL, origString);
                value.appendChild(document.createTextNode(valueString));
                row.appendChild(value);
              }
              root.appendChild(row);
              rows++;
            }
            rs.close();
            releaseStatement(st);
      
            log4j.info("exportTrl - Records=" + rows + ", DTD=" + document.getDoctype());
  
            DOMSource source = new DOMSource(document);
            TransformerFactory tFactory = TransformerFactory.newInstance();
                tFactory.setAttribute("indent-number", new Integer(2));
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //  Output
            out.createNewFile();
            //  Transform
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream (out));//, "ISO-8859-1");
            transformer.transform (source, new StreamResult(osw));
            // FIXME: We should be closing the file (out and its related classes) here to make sure that is closed
            // and that is does not really get closed when the GC claims the object (indeterministic)
            
          } // translate or not (if)
        } catch (Exception e) {
          log4j.error("exportTrl", e);
        } finally {
              try {
                if (st != null) releaseStatement(st);
              } catch (Exception ignored) {}
        }
      }
    } catch (Exception e) {
      log4j.error("exportTrl", e);
    }
    return "";
  } //  exportModulesTrl

  
  private static String importContributors(String directory, String AD_Language){
    String fileName = directory + File.separator + CONTRIBUTORS_FILENAME+"_" + AD_Language + ".xml";
    File in = new File (fileName);
    if (!in.exists()){
      String msg = "File does not exist: " + fileName;
      translationlog4j.debug(msg);
      return msg;
    }
    try {
      TranslationHandler handler = new TranslationHandler(cp);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(in, handler);
      return "";
    } catch (Exception e){
      translationlog4j.error("importContrib", e);
      return e.toString();
    }
  }
  
  private static String importTrlFile(String directory, int AD_Client_ID, String AD_Language, String Trl_Table)
  {
    String fileName = directory + File.separator + Trl_Table + "_" + AD_Language + ".xml";
    translationlog4j.debug("importTrl - " + fileName);
    File in = new File (fileName);
    if (!in.exists())
    {
      String msg = "File does not exist: " + fileName;
      translationlog4j.debug("importTrl - " + msg);
      return msg;
    }

    try
    {
      TranslationHandler handler = new TranslationHandler(AD_Client_ID, cp);
      SAXParserFactory factory = SAXParserFactory.newInstance();
    //  factory.setValidating(true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(in, handler);
      translationlog4j.info("importTrl - Updated=" + handler.getUpdateCount()+" - from file "+fileName);
      //return Msg.getMsg(Env.getCtx(), "Updated") + "=" + handler.getUpdateCount();
      return "";
    }
    catch (Exception e)
    {
      translationlog4j.error("importTrl", e);
      return e.toString();
    }
  }  //  importTrl

private  TranslationData[] getTrlColumns (String Base_Table)
  {
    try
    {
      m_IsCentrallyMaintained = (TranslationData.centrallyMaintained(cp,Base_Table) != "0");
      m_IsCentrallyMaintained = false;  //???
    }
    catch (Exception e)
    {
      translationlog4j.error("getTrlColumns (IsCentrallyMaintained)", e);
    }


    TranslationData[] list = null;

    try
    {
      list = TranslationData.trlColumns(cp, Base_Table + "_TRL");
    }
    catch (Exception e)
    {
      translationlog4j.error("getTrlColumns", e);
    }
    return list;
}


 void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Translation").createXmlDocument();
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "Translation", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
  try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.Translation");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "Translation.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "Translation.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("Translation");
      vars.removeMessage("Translation");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

    if (log4j.isDebugEnabled()) log4j.debug("datasheet message:"+myMessage.getMessage());

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramSelLanguage", vars.getSessionValue("translation.lang"));
    xmlDocument.setParameter("paramSystem", vars.getSessionValue("translation.client"));
    xmlDocument.setData("structure1", LanguageComboData.select(this));
 
    xmlDocument.setData("structureClient", ClientComboData.selectAllClients(this));

    out.println(xmlDocument.print());
    out.close();
  }
  }
}  //  Translation
