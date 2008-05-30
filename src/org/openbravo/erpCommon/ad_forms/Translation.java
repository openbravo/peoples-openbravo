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





public class Translation extends HttpSecureAppServlet
{
	private static final long serialVersionUID = 1L;
	/**	XML Element Tag			*/
	public static final String	XML_TAG = "compiereTrl";
	/**	XML Attribute Table			*/
	public static final String	XML_ATTRIBUTE_TABLE = "table";
	/** XML Attribute Language		*/
	public static final String	XML_ATTRIBUTE_LANGUAGE = "language";

	/**	XML Row Tag					*/
	public static final String	XML_ROW_TAG = "row";
	/** XML Row Attribute ID		*/
	public static final String	XML_ROW_ATTRIBUTE_ID = "id";
	/** XML Row Attribute Translated	*/
	public static final String	XML_ROW_ATTRIBUTE_TRANSLATED = "trl";

	/**	XML Value Tag				*/
	public static final String	XML_VALUE_TAG = "value";
	/** XML Value Column			*/
	public static final String	XML_VALUE_ATTRIBUTE_COLUMN = "column";
	/** XML Value Original			*/
	public static final String	XML_VALUE_ATTRIBUTE_ORIGINAL = "original";
  
  public static final String CONTRIBUTORS_FILENAME = "CONTRIBUTORS";
  public static final String XML_CONTRIB = "Contributors";

	/**	Table is centrally maintained	*/
	private boolean			m_IsCentrallyMaintained = false;

//  private String m_Directory;

  /*public void init (ServletConfig config) {
    super.init(config);
    m_Directory = config.getServletContext().getInitParameter("AttachmentDirectory");
  }*/


   public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"); //added for JDK1.5
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
    	//vars.setSessionValue("Translation.message", strMessage);
        vars.setMessage("Translation", myMessage);
        response.sendRedirect(strDireccion + request.getServletPath());
      
     }
     else  {
       String strLang = vars.getRequestGlobalVariable("language", "translation.lang");
       String strClient = vars.getRequestGlobalVariable("client", "translation.client");
      if (log4j.isDebugEnabled()) log4j.debug("Lang "+strLang+" Client "+strClient);


      OBError myMessage = importTrl(strLang,strClient, vars);
      if (log4j.isDebugEnabled()) log4j.debug("message:"+myMessage.getMessage());
      //vars.setSessionValue("Translation.message", strMessage);
      vars.setMessage("Translation", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());

     }
  }



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
       //return Utility.messageBD(this, "CannotWriteDirectory", vars.getLanguage())+" "+strFTPDirectory;
     }
     
    (new File(strFTPDirectory+"/lang")).mkdir();
    String directory = strFTPDirectory+"/lang/"+AD_Language+"/";    
    (new File(directory)).mkdir();

    if (log4j.isDebugEnabled()) log4j.debug("directory "+directory);
   
    try{
      TranslationData[] tables = TranslationData.trlTables(this);
      for (int i=0; i<tables.length; i++ )
         exportTrl(directory, AD_Client_ID, AD_Language, tables[i].c);
      exportContibutors(directory, AD_Language);
    }catch (Exception e)
    {
      log4j.error(e.toString());
      myMessage.setType("Error");        
      myMessage.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
      return myMessage;
      //return Utility.messageBD(this, "Error", vars.getLanguage());
    }
    myMessage.setType("Success");        
    myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    return myMessage;
    //return Utility.messageBD(this, "Success", vars.getLanguage());
  }
   
  private OBError importTrl(String strLang, String strClient, VariablesSecureApp vars) {
    String AD_Language = strLang;
    String directory =  globalParameters.strFTPDirectory+"/lang/"+AD_Language+"/";
    OBError myMessage = null;    
        
    myMessage = new OBError();
    myMessage.setTitle("");
    
     if ((new File(directory).exists())&&(new File(directory).canRead())) {
       if (log4j.isDebugEnabled()) log4j.debug("can read "+directory);
     } else {
       log4j.error("Can't read on directory: "+globalParameters.strFTPDirectory);
       myMessage.setType("Error");        
       myMessage.setMessage(Utility.messageBD(this, "CannotReadDirectory", vars.getLanguage())+" "+directory);
       return myMessage;
       //return Utility.messageBD(this, "CannotReadDirectory", vars.getLanguage())+" "+directory;
     }
    
    int AD_Client_ID = Integer.valueOf(strClient);
    try{
      TranslationData[] tables = TranslationData.trlTables(this);
      for (int i=0; i<tables.length; i++ )
         importTrl( directory, AD_Client_ID, AD_Language, tables[i].c);
      importContributors(directory,AD_Language);
    }catch (Exception e)
    {
      log4j.error(e.toString());
      myMessage.setType("Error");        
      myMessage.setMessage(Utility.messageBD(this, "Error", vars.getLanguage()));
      return myMessage;
      //return Utility.messageBD(this, "Error", vars.getLanguage());
    }
    myMessage.setType("Success");        
    myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    return myMessage;
    //return Utility.messageBD(this, "Success", vars.getLanguage());
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
      //	Output
      out.createNewFile();
      StreamResult result = new StreamResult(out);
      //	Transform
      transformer.transform (source, result);
    } catch (Exception e)	{
			log4j.error("exportTrl", e);
    }
  }

  private String exportTrl(String directory, int AD_Client_ID, String AD_Language, String Trl_Table)
	{
		String fileName = directory + File.separator + Trl_Table + "_" + AD_Language + ".xml";
		log4j.info("exportTrl - " + fileName);
		File out = new File(fileName);
    Statement st = null;
    try
		{
		boolean isBaseLanguage = (TranslationData.baseLanguage(this, AD_Language).equals("Y"));
    log4j.info("baseLang "+isBaseLanguage);
		String tableName = Trl_Table;
		int pos = tableName.indexOf("_TRL");
		String Base_Table = Trl_Table.substring(0, pos);
		if (isBaseLanguage)
			tableName =  Base_Table;
     log4j.info("table - " + tableName);
		String keyColumn = Base_Table + "_ID";
		 TranslationData[] trlColumns = getTrlColumns (Base_Table);
		//
		StringBuffer sql = null;
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();


			//	Root
			Element root = document.createElement(XML_TAG);
			root.setAttribute(XML_ATTRIBUTE_LANGUAGE, AD_Language);
			root.setAttribute(XML_ATTRIBUTE_TABLE, Base_Table);
			document.appendChild(root);
      log4j.info("exportTrl - kk ");
			//
			sql = new StringBuffer ("SELECT ");
			if (isBaseLanguage)
				sql.append("'Y',");							//	1
			else
		    sql.append("t.IsTranslated,");
			
      sql.append("t.").append(keyColumn);				//	2
			//
			for (int i = 0; i < trlColumns.length; i++)
				sql.append(", t.").append(trlColumns[i].c)
					.append(",o.").append(trlColumns[i].c).append(" AS ").append(trlColumns[i].c).append("O");
			//
			sql.append(" FROM ").append(tableName).append(" t")
				.append(" INNER JOIN ").append(Base_Table)
				.append(" o ON (t.").append(keyColumn).append("=o.").append(keyColumn).append(")");
			boolean haveWhere = false;
			if (!isBaseLanguage)
			{
				sql.append(" WHERE t.AD_Language='"+AD_Language+"'");
				haveWhere = true;
			}
			if (m_IsCentrallyMaintained)
			{
				sql.append (haveWhere ? " AND " : " WHERE ").append ("o.IsCentrallyMaintained='N'");
				haveWhere = true;
			}
			if (AD_Client_ID >= 0)
				sql.append(haveWhere ? " AND " : " WHERE ").append("o.AD_Client_ID=").append(AD_Client_ID);
			sql.append(" ORDER BY t.").append(keyColumn);
			//

      if (log4j.isDebugEnabled()) log4j.debug("SQL:"+sql.toString());
      st = this.getStatement();
       if (log4j.isDebugEnabled()) log4j.debug("st");
//	PreparedStatement pstmt = DB.prepareStatement(sql.toString());
		/*	if (!isBaseLanguage)
				st.setString(1, AD_Language);*/
			ResultSet rs = st.executeQuery(sql.toString());
      if (log4j.isDebugEnabled()) log4j.debug("rs");
			int rows = 0;
			while (rs.next())
			{
				Element row = document.createElement (XML_ROW_TAG);
				row.setAttribute(XML_ROW_ATTRIBUTE_ID, String.valueOf(rs.getInt(2)));	//	KeyColumn
				row.setAttribute(XML_ROW_ATTRIBUTE_TRANSLATED, rs.getString(1));		//	IsTranslated
				for (int i = 0; i < trlColumns.length; i++)
				{
					Element value = document.createElement (XML_VALUE_TAG);
					value.setAttribute(XML_VALUE_ATTRIBUTE_COLUMN, trlColumns[i].c);
					String origString = rs.getString(trlColumns[i].c + "O");			//	Original Value
					if (origString == null)
						origString = "";
					String valueString = rs.getString(trlColumns[i].c);				//	Value
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
			
      //
			DOMSource source = new DOMSource(document);
			TransformerFactory tFactory = TransformerFactory.newInstance();
      		tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//	Output
			out.createNewFile();
			//	Transform
			//transformer.transform (source, result);
      OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream (out));//, "ISO-8859-1");
      transformer.transform (source, new StreamResult(osw));
      // FIXME: We should be closing the file (out and its related classes) here to make sure that is closed
      // and that is does not really get closed when the GC claims the object (indeterministic)
		}
		catch (Exception e)
		{
			log4j.error("exportTrl", e);
		} finally {
          try {
            if (st != null) releaseStatement(st);
          } catch (Exception ignored) {}
    }

		return "";
	}	//	exportTrl
  
  private String importContributors(String directory, String AD_Language){
    String fileName = directory + File.separator + CONTRIBUTORS_FILENAME+"_" + AD_Language + ".xml";
    File in = new File (fileName);
    if (!in.exists()){
			String msg = "File does not exist: " + fileName;
			log4j.error(msg);
			return msg;
		}
    try {
      TranslationHandler handler = new TranslationHandler(this);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, handler);
      return "";
    } catch (Exception e){
			log4j.error("importContrib", e);
			return e.toString();
		}
  }
  
  private String importTrl(String directory, int AD_Client_ID, String AD_Language, String Trl_Table)
	{
		String fileName = directory + File.separator + Trl_Table + "_" + AD_Language + ".xml";
		log4j.info("importTrl - " + fileName);
		File in = new File (fileName);
		if (!in.exists())
		{
			String msg = "File does not exist: " + fileName;
			log4j.error("importTrl - " + msg);
			return msg;
		}

		try
		{
			TranslationHandler handler = new TranslationHandler(AD_Client_ID, this);
			SAXParserFactory factory = SAXParserFactory.newInstance();
		//	factory.setValidating(true);
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, handler);
			log4j.info("importTrl - Updated=" + handler.getUpdateCount());
			//return Msg.getMsg(Env.getCtx(), "Updated") + "=" + handler.getUpdateCount();
      return "";
		}
		catch (Exception e)
		{
			log4j.error("importTrl", e);
			return e.toString();
		}
	}	//	importTrl

private  TranslationData[] getTrlColumns (String Base_Table)
  {
		try
		{
      m_IsCentrallyMaintained = (TranslationData.centrallyMaintained(this,Base_Table) != "0");
      m_IsCentrallyMaintained = false;  //???
		}
		catch (Exception e)
		{
			log4j.error("getTrlColumns (IsCentrallyMaintained)", e);
		}


    TranslationData[] list = null;

		try
		{
      list = TranslationData.trlColumns(this, Base_Table + "_TRL");
		}
		catch (Exception e)
		{
			log4j.error("getTrlColumns", e);
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
    //String strMessage = vars.getSessionValue("Translation.message");
    if (log4j.isDebugEnabled()) log4j.debug("datasheet message:"+myMessage.getMessage());
    //vars.removeSessionValue("Translation.message");
    //if (strMessage!=null && !strMessage.equals(""))
    //{
    //xmlDocument.setParameter("mensaje", strMessage.equals("")?"":"alert('"+strMessage+"');");
    //}

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramSelLanguage", vars.getSessionValue("translation.lang"));
    xmlDocument.setParameter("paramSystem", vars.getSessionValue("translation.client"));
    xmlDocument.setData("structure1", LanguageComboData.select(this));
 
    xmlDocument.setData("structureClient", ClientComboData.selectAllClients(this));

    out.println(xmlDocument.print());
    out.close();
	}
  }


}	//	Translation
