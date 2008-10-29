/*
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.0
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL All
 * portions are Copyright (C) 2001-2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.TemplateData;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class TemplateInfo
{
	private static Logger log4j = Logger.getLogger( TemplateInfo.class );
	
	private String _TemplateLocation;
	private String _TemplateFilename;
	private String _ReportFilename;
	private EmailDefinition _DefaultEmailDefinition;
	private Map<String, EmailDefinition>  _EmailDefinitions;
	  
	  public class EmailDefinition
	  {
	    private String _Subject;
	    private String _Body;
	    private String _Language;
	    private boolean _IsDefault;
	    
	    public EmailDefinition( EmailDefinitionData emailDefinitionData )
	    {
	      _Subject = emailDefinitionData.getField("subject");
	      _Body = emailDefinitionData.getField("body");
	      _Language = emailDefinitionData.getField("ad_language");
	      if( emailDefinitionData.getField("isdefault") != null )
	      {
	        _IsDefault = emailDefinitionData.getField("isdefault") == "Y" ? true : false;
	      }
	    }

	    public String getSubject()
	    {
	      return _Subject;
	    }

	    public String getBody()
	    {
	      return _Body;
	    }

	    public String getLanguage()
	    {
	      return _Language;
	    }

	    public boolean isDefault()
	    {
	      return _IsDefault;
	    }
	  }
	  
  public TemplateInfo( ConnectionProvider connectionProvider, String docTypeId, String orgId, String strLanguage ) throws ServletException, ReportingException
  {
    TemplateData[] templates = TemplateData.getDocumentTemplates( connectionProvider, docTypeId, orgId );
    if( templates.length > 0 )
    {
      setTemplateLocation( templates[0].getField("template_location") );
      
      _TemplateFilename = templates[0].getField("template_filename");
      _ReportFilename = templates[0].getField("report_filename");
      
      //READ EMAIL DEFINITIONS!!!!
      _EmailDefinitions = new HashMap<String, EmailDefinition>();
      EmailDefinitionData[] emailDefinitionsData = EmailDefinitionData.getEmailDefinitions( connectionProvider, orgId, templates[0].cPocDoctypeTemplateId );
      if( emailDefinitionsData.length > 0 )
      {
        for( EmailDefinitionData emailDefinitionData : emailDefinitionsData )
        {
          EmailDefinition emailDefinition = new EmailDefinition( emailDefinitionData );
          _EmailDefinitions.put( emailDefinition.getLanguage(), emailDefinition );
          
          if( emailDefinition.isDefault() )
            _DefaultEmailDefinition = emailDefinition;
        }
        if( _DefaultEmailDefinition == null && !_EmailDefinitions.isEmpty() )
        {
          _DefaultEmailDefinition = _EmailDefinitions.values().iterator().next();
        }
      }
      else
        throw new ReportingException( Utility.messageBD(connectionProvider, "NoEmailDefinitions", strLanguage) + templates[0].cPocDoctypeTemplateId);
    }
    else
      throw new ServletException( Utility.messageBD(connectionProvider, "NoDocumentTypeTemplate", strLanguage) + docTypeId );
  }
	
	public String getTemplate()
	{
		return _TemplateLocation + "/" + _TemplateFilename;
	}
	
	public void setTemplateLocation( String templateLocation )
	{
		_TemplateLocation = templateLocation;
		// Make sure the location always ends with a / character
		if( !_TemplateLocation.endsWith( "/" ) )
			_TemplateLocation = _TemplateLocation + "/";
		if (log4j.isDebugEnabled()) log4j.debug( "Template location is set to: " + _TemplateLocation );
	}

	public String getTemplateLocation()
	{
		return _TemplateLocation;
	}

	public String getTemplateFilename()
	{
		return _TemplateFilename;
	}

	public String getReportFilename()
	{
		return _ReportFilename;
	}

	public EmailDefinition getEmailDefinition( String language ) throws ReportingException
	{
	    EmailDefinition emailDefinition = _EmailDefinitions.get( language );
	    if( emailDefinition == null )
	    {
	      log4j.info( "No email definition found for language " + language + ". Using default email definition" );
	      emailDefinition = _DefaultEmailDefinition;
	    }
	    
	    if( emailDefinition == null )
	      throw new ReportingException( "No email definition available." );
	    return emailDefinition;
	}
}
