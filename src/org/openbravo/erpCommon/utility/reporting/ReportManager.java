/*
 * ************************************************************************ The
 * The contents of this file are subject to the Openbravo Public License Version 1.0
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL.
 * Portions created by Business Momentum b.v. (http://www.businessmomentum.eu)
 * are Copyright (C) 2007-2008 by Business Momentum b.v. All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.ClassInfoData;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachmentsData;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.utils.Replace;

public class ReportManager
{
  private static Logger log4j = Logger.getLogger( ReportManager.class );
  private static final String TEMP_REPORT_DIR = "tmp";

  private ConnectionProvider  _connectionProvider;
  private String        _strBaseDesignPath;
  private String        _strDefaultDesignPath;
  private String        _strBaseWeb;    //BASE WEB!!!!!!
  private ClassInfoData   _classInfo;
  private String        _prefix;
  private String        _strAttachmentPath;

  public ReportManager( ConnectionProvider connectionProvider, String ftpDirectory, String replaceWithFull, String baseDesignPath, String defaultDesignPath, String prefix, ClassInfoData classInfo)
  {
    _connectionProvider = connectionProvider;
    _strBaseWeb = replaceWithFull;
    _strBaseDesignPath = baseDesignPath;
    _strDefaultDesignPath = defaultDesignPath;
    _strAttachmentPath = ftpDirectory;
    _classInfo = classInfo;
    _prefix =prefix;
    
    //Strip of ending slash character
    if( _strBaseDesignPath.endsWith("/") )
      _strBaseDesignPath = _strBaseDesignPath.substring( 0, _strBaseDesignPath.length() - 1 );
    if( _strDefaultDesignPath.endsWith("/") )
      _strDefaultDesignPath = _strDefaultDesignPath.substring( 0, _strDefaultDesignPath.length() - 1 );   
  }

  private String getBaseDesignPath( String language )
  {
    String designPath = _strDefaultDesignPath;
    if( !language.equals( "" ) && !language.equals("en_US") ) designPath = language;
    designPath = _prefix + "/" + _strBaseDesignPath + "/" + designPath;

    return designPath;
  }
  
  public void processReport( Report report, VariablesSecureApp variables ) throws ReportingException
  {
    String language = variables.getLanguage();
    String baseDesignPath = getBaseDesignPath( language );
    Locale locale = new Locale( language.substring(0,2), language.substring(3,5) );

    String templateLocation = report.getTemplateInfo().getTemplateLocation();
    templateLocation = Replace.replace(Replace.replace(templateLocation,"@basedesign@", baseDesignPath ),"@baseattach@", _strAttachmentPath);
    String templateFile = templateLocation + report.getTemplateInfo().getTemplateFilename();

    HashMap<String, Object> designParameters = new HashMap<String, Object>();

    designParameters.put("DOCUMENT_ID", report.getDocumentId() );
    designParameters.put("TEMPLATE_LOCATION", templateLocation );
    designParameters.put("BASE_ATTACH", _strAttachmentPath);  // TODO: Rename parameter to BASE_ATTACH_PATH
    designParameters.put("BASE_WEB", _strBaseWeb );       // TODO: Do not use Base web, this is an url and generates web traffic, a local path reference should be used
    try
    {
      JasperDesign jasperDesign= JRXmlLoader.load( templateFile );
      JasperReport jasperReport= JasperCompileManager.compileReport( jasperDesign );
      if (designParameters == null) designParameters = new HashMap<String, Object>();

      designParameters.put("IS_IGNORE_PAGINATION",  false );
      designParameters.put("USER_CLIENT", Utility.getContext( _connectionProvider, variables, "#User_Client", "" ) );
      designParameters.put("USER_ORG", Utility.getContext( _connectionProvider, variables, "#User_Org", "" ) );
      designParameters.put("LANGUAGE", language);
      designParameters.put("LOCALE", locale);

      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
      dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
      DecimalFormat NumberFormat = new DecimalFormat(variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
      designParameters.put("NUMBERFORMAT", NumberFormat);

      if (log4j.isDebugEnabled()) log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
      JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

      JasperPrint jasperPrint;

      Connection con = null;
      try
      {
        con = _connectionProvider.getTransactionConnection();
        jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
      }
      catch (Exception e)
      {
        throw new ReportingException(e.getMessage());
      }
      finally
      {
        _connectionProvider.releaseRollbackConnection(con);
      }

      // Create target directory if it does not exist
          File targetDirectory = new File( _strAttachmentPath + "/" + TEMP_REPORT_DIR );
          if( !targetDirectory.exists() ) targetDirectory.mkdirs();
      
      String target = _strAttachmentPath + "/" + TEMP_REPORT_DIR + "/" + report.getFilename();
      JasperExportManager.exportReportToPdfFile( jasperPrint, target );

      report.setTargetDirectory( targetDirectory );
    }
    catch ( JRException exception )
    {
      throw new ReportingException( exception );
    }
    catch ( Exception exception)
    {
      throw new ReportingException( exception );
    }
  }
  
  public File createAttachmentForReport( ConnectionProvider connectionProvider, Report report, String tableId, VariablesSecureApp vars ) throws ReportingException, IOException
  {
    if( report.isAttached() )
      throw new ReportingException( Utility.messageBD(connectionProvider, "AttachmentExists", vars.getLanguage()) );
    
    String destination = tableId + "-" + report.getDocumentId();
    
    // First move the file to the correct destination
        File destinationFolder = new File( _strAttachmentPath + "/" + destination );
        if( !destinationFolder.exists() )
        {
          destinationFolder.mkdirs();
        }
        
        File sourceFile = new File( report.getTargetLocation() );
        File destinationFile = new File( destinationFolder, sourceFile.getName() );
        log4j.debug( "Destination file before renaming: " + destinationFile );
        if( !sourceFile.renameTo( destinationFile ) )
          throw new ReportingException(Utility.messageBD(connectionProvider, "UnreachableDestination", vars.getLanguage()) + destinationFolder );
        
    report.setTargetDirectory( destinationFolder );
        
      // Attach them to the order in OB
      Connection conn = null;
      try
      {
        conn = _connectionProvider.getTransactionConnection();

        String newFileId = SequenceIdData.getUUID();
        log4j.debug( "New file id: " + newFileId );
        // The 103 in the following insert specifies the document type: in this case PDF
        TabAttachmentsData.insert(conn, _connectionProvider, newFileId, vars.getClient(), vars.getOrg(), vars.getUser(), tableId, report.getDocumentId(), "103", "Generated by printing ", destinationFile.getName() );

        _connectionProvider.releaseCommitConnection(conn);
      }
      catch (Exception exception)
      {
        try
        {
          _connectionProvider.releaseRollbackConnection(conn);
        }
        catch (Exception ignored)
        {
        }
        
        throw new ReportingException( exception );
      }
      
      report.setAttached( true );
      
      return destinationFile;
  }
  
}
