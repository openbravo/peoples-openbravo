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
 * portions are Copyright (C) 2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting.printing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.erpCommon.utility.reporting.ToolsData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailData;
import org.openbravo.erpCommon.utility.poc.PocData;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.erpCommon.utility.poc.EmailType;
import org.openbravo.erpCommon.utility.poc.PocException;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.xmlEngine.XmlDocument;

@SuppressWarnings("serial")
public class PrintController extends HttpSecureAppServlet
{
    static Logger log4j = Logger.getLogger( PrintController.class );

    // TODO: When an email is in draft status add the notification that the document can not be emailed
        
	public void init( ServletConfig config )
	{
		super.init(config);
		boolHist = false;
	}

	@SuppressWarnings("unchecked")
	public void doPost( HttpServletRequest request, HttpServletResponse response )
			throws IOException, ServletException
	{
		VariablesSecureApp vars = new VariablesSecureApp(request);
		
		DocumentType	documentType = DocumentType.UNKNOWN;
		String	sessionValuePrefix = null;
		String	strDocumentId = null;
		
		//Determine which process called the print controller
		if (log4j.isDebugEnabled()) log4j.debug( "Servletpath: " + request.getServletPath() );
		if( request.getServletPath().toLowerCase().indexOf( "quotations") != -1 )
		{
			documentType = DocumentType.QUOTATION;
			// The prefix PRINTORDERS is a fixed name based on the KEY of the AD_PROCESS
			sessionValuePrefix = "PRINTQUOTATIONS";
			
			strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcOrderId_R");
			if (strDocumentId.equals(""))
				strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcOrderId");
		}
		if( request.getServletPath().toLowerCase().indexOf( "orders") != -1 )
		{
			documentType = DocumentType.SALESORDER;
			// The prefix PRINTORDERS is a fixed name based on the KEY of the AD_PROCESS
			sessionValuePrefix = "PRINTORDERS";
			
			strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcOrderId_R");
			if (strDocumentId.equals(""))
				strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcOrderId");
		}
		if( request.getServletPath().toLowerCase().indexOf( "invoices") != -1 )
		{
			documentType = DocumentType.SALESINVOICE;
			// The prefix PRINTINVOICES is a fixed name based on the KEY of the AD_PROCESS
			sessionValuePrefix = "PRINTINVOICES";
			
			strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcInvoiceId_R");
			if (strDocumentId.equals(""))
				strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpcInvoiceId");			
		}
		if( request.getServletPath().toLowerCase().indexOf( "shipments") != -1 )
		{
			documentType = DocumentType.SHIPMENT;
			// The prefix PRINTINVOICES is a fixed name based on the KEY of the AD_PROCESS
			sessionValuePrefix = "PRINTSHIPMENTS";
			
			strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpmInoutId_R");
			if (strDocumentId.equals(""))
				strDocumentId = vars.getSessionValue( sessionValuePrefix + ".inpmInoutId");			
		}

		post(request,response,vars,documentType,sessionValuePrefix, strDocumentId);
		
	}

	 protected void post( HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, DocumentType documentType, String  sessionValuePrefix, String  strDocumentId )
	  throws IOException, ServletException
	  {

	    Map<String, Report> reports;    
	    String  documentIds[] = null;   
	    if (log4j.isDebugEnabled()) log4j.debug("strDocumentId: " + strDocumentId);
	    // normalize the string of ids to a comma separated list
	    strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
	    if( strDocumentId.length() == 0 )
	      throw new ServletException( Utility.messageBD(this, "NoDocument", vars.getLanguage()));
	    
	    documentIds = strDocumentId.split( "," );
	    if (log4j.isDebugEnabled()) log4j.debug( "Number of documents selected: " + documentIds.length );     
	    
	    reports = (Map<String, Report>)vars.getSessionObject( sessionValuePrefix + ".Documents" );
	    ReportManager reportManager = new ReportManager( this, globalParameters.strFTPDirectory, strReplaceWithFull, globalParameters.strBaseDesignPath, globalParameters.strDefaultDesignPath, globalParameters.prefix, classInfo);        
	    
	    if (vars.commandIn("PRINT")||vars.commandIn("ARCHIVE")||( !request.getRequestURI().endsWith( ".html" ) ))
	    {
	        String documentId = vars.getStringParameter( "inpDocumentId" );
		    if (strDocumentId!=null) strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
	      Report report = reports.get( strDocumentId );
	      if( report == null )
	        throw new ServletException( Utility.messageBD(this, "NoDataReport", vars.getLanguage()) + documentId );
	      
	      // Check if the document is not in status 'draft'
	      if( !report.isDraft() && !report.isAttached() && vars.commandIn("ARCHIVE"))
	      {
	        // TODO: Move the table Id retrieval into the DocumentType getTableId method!
	        // get the Id of the entities table, this is used to store the file as an OB attachment
	          String tableId = ToolsData.getTableId( this, report.getDocumentType().getTableName() );

	          if (log4j.isDebugEnabled()) log4j.debug( "Table " + report.getDocumentType().getTableName() + " has table id: " + tableId );
	          // Save the report as a attachment because it is being transferred to the user
	          try
	          {
	            reportManager.createAttachmentForReport(this, report, tableId, vars );
	          }
	          catch( ReportingException exception )
	          {
	            throw new ServletException( exception );
	          }
	      }
	      else
	      {
	        if (log4j.isDebugEnabled()) log4j.debug( "Document is not attached." );
	      }

	      // Dump the report to the users browser
	      response.setContentType("application/pdf; charset=UTF-8");
	      if (log4j.isDebugEnabled()) log4j.debug( "Dumping file: " + report.getTargetLocation() );
	          Utility.dumpFile( report.getTargetLocation(), response.getOutputStream() );
	          response.flushBuffer();     
	    }
	    else
	    {
	      if (vars.commandIn("DEFAULT"))
	      {
	        reports = new HashMap<String, Report>();        
	        for( int index = 0; index < documentIds.length; index++ )
	        {
	          String documentId = documentIds[index];
	          if (log4j.isDebugEnabled()) log4j.debug( "Processing document with id: " + documentId );
	          
	          try
	          {
	            Report report = new Report( this, documentType, documentId, vars.getLanguage() );
	            reports.put( documentId, report );
	          
	            //TODO: The creation of the reports can be delayed until the actual report is viewed or emailed. This saves time when creating the window
	            reportManager.processReport( report, vars );
	          }
	          catch( ReportingException exception )
	          {
	            throw new ServletException( exception );
	          }
	        }
	        
	        vars.setSessionObject( sessionValuePrefix + ".Documents", reports );
	        if( request.getServletPath().toLowerCase().indexOf( "print.html") != -1 )
	          createPrintOptionsPage(request, response, vars, documentType, "('" + strDocumentId + "')", reports );
	        else
              createEmailOptionsPage(request, response, vars, documentType, "('" + strDocumentId + "')", reports );	        	
	      }
	      else if (vars.commandIn("EMAIL"))
	      {
	        int nrOfEmailsSend = 0;
	        for( int index = 0; index < documentIds.length; index++ )
	        {
	          String documentId = documentIds[index];
	          if (log4j.isDebugEnabled()) log4j.debug( "Processing document with id: " + documentId );
	          
	          Report report = reports.get( documentId );
	          if( report == null )
	            throw new ServletException( Utility.messageBD(this, "NoDataReport", vars.getLanguage()) + documentId );

	          // Check if the document is not in status 'draft'
	          if( !report.isDraft() )
	          {
	            // Check if the report is already attached
	            if( !report.isAttached() )
	            {
	              // get the Id of the entities table, this is used to store the file as an OB attachment
	                String tableId = ToolsData.getTableId( this, report.getDocumentType().getTableName() );
	                
	                //If the user wants to archive the document
	                if (vars.getStringParameter("inpArchive").equals("Y")){
	                  // Save the report as a attachment because it is being transferred to the user
	                  try
	                  {	                  
	                    reportManager.createAttachmentForReport( this, report, tableId, vars );
	                  }
	                  catch( ReportingException exception )
	                  {
	                    throw new ServletException( exception );
	                  }
	                }
	            }
	            else
	            {
	              if (log4j.isDebugEnabled()) log4j.debug( "Document is not attached." );
	            }
	          
	            sendDocumentEmail( report, vars );
	            nrOfEmailsSend++;
	          }
	        }

	        createPrintStatusPage(response, vars, nrOfEmailsSend );
	      }
	  
	      pageError(response);
	    }
	  }

	protected PocData[] getContactDetails( DocumentType documentType, String strDocumentId ) throws ServletException
	{
		switch( documentType )
		{
			case QUOTATION:
				return PocData.getContactDetailsForOrders( this, strDocumentId );
			case SALESORDER:
				return PocData.getContactDetailsForOrders( this, strDocumentId );
			case SALESINVOICE:
				return PocData.getContactDetailsForInvoices( this, strDocumentId );
			case SHIPMENT:
				return PocData.getContactDetailsForShipments( this, strDocumentId );
			case PURCHASEORDER:
				return PocData.getContactDetailsForOrders( this, strDocumentId );
		}
		return null;
	}
	
	protected void sendDocumentEmail( Report report, VariablesSecureApp vars ) throws IOException, ServletException
	{
		String documentId = report.getDocumentId();
		String attachmentFileLocation = report.getTargetLocation();
		
		String ourReference = vars.getStringParameter( "ourReference-" + documentId );
		String cusReference = vars.getStringParameter( "cusReference-" + documentId );
		if (log4j.isDebugEnabled()) log4j.debug( "our document ref: " + ourReference );
		if (log4j.isDebugEnabled()) log4j.debug( "cus document ref: " + cusReference );
		
		String salesrepName = vars.getStringParameter( "salesrepName-" + documentId );
		String salesrepEmail = vars.getStringParameter( "salesrepEmail-" + documentId );
		String contactName =  vars.getStringParameter( "contactName-" + documentId );
		String contactEmail =  vars.getStringParameter( "contactEmail-" + documentId );
		String emailSubject =  vars.getStringParameter( "emailSubject" );
		String emailBody =  vars.getStringParameter( "emailBody" );

		if (log4j.isDebugEnabled()) log4j.debug( "sales rep name: " + salesrepName );
		if (log4j.isDebugEnabled()) log4j.debug( "sales rep email: " + salesrepEmail );
		if (log4j.isDebugEnabled()) log4j.debug( "recipient name: " + contactName );
		if (log4j.isDebugEnabled()) log4j.debug( "recipient email: " + contactEmail );
		
		// Also send it to the current user
		PocData[] currentUserInfo = PocData.getContactDetailsForUser( this, vars.getUser() );
		String userName = currentUserInfo[0].userName;
		String userEmail = currentUserInfo[0].userEmail;
		if (log4j.isDebugEnabled()) log4j.debug( "user name: " + userName );
		if (log4j.isDebugEnabled()) log4j.debug( "user email: " + userEmail );
		
		// TODO: Move this to the beginning of the print handling and do nothing if these conditions fail!!!)

		if( (salesrepEmail == null && salesrepEmail.length() == 0) )
		{
			throw new ServletException( Utility.messageBD(this, "NoSalesRepEmail", vars.getLanguage()));
		}

		if( (contactEmail == null && contactEmail.length() == 0) )
		{
			throw new ServletException(Utility.messageBD(this, "NoCustomerEmail", vars.getLanguage()));
		}
		
		// Replace special tags
        emailSubject = emailSubject.replaceAll( "@cus_ref@", cusReference);
        emailSubject = emailSubject.replaceAll( "@our_ref@", ourReference);

        emailBody = emailBody.replaceAll( "@cus_ref@", cusReference);
        emailBody = emailBody.replaceAll( "@our_ref@", ourReference);

		try
		{
			Session session = EmailManager.newMailSession( this, vars.getClient() );

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress( salesrepEmail ) );
			message.addRecipient( Message.RecipientType.TO, new InternetAddress( contactEmail ) );
			
			message.addRecipient( Message.RecipientType.BCC, new InternetAddress( salesrepEmail ) );
			
			if( userEmail != null && userEmail.length() > 0 )
				message.addRecipient( Message.RecipientType.BCC, new InternetAddress( userEmail ) );
			
			
			message.setSubject( emailSubject );
			
			// Content consists of 2 parts, the message body and the attachment
			// We therefor use a multipart message
		    Multipart multipart = new MimeMultipart();
			
		    // Create the message part 
		    MimeBodyPart messageBodyPart = new MimeBodyPart();
		    messageBodyPart.setText( emailBody );
		    multipart.addBodyPart(messageBodyPart);
		    
		    // Create the attachment part
		    messageBodyPart = new MimeBodyPart();
		    DataSource source = new FileDataSource( attachmentFileLocation );
		    messageBodyPart.setDataHandler( new DataHandler(source));
		    messageBodyPart.setFileName( attachmentFileLocation.substring(attachmentFileLocation.lastIndexOf("/") + 1 ) );
		    multipart.addBodyPart(messageBodyPart);			
			
			message.setContent( multipart );
			
			// Send the email
			Transport.send(message);
			
			String clientId = vars.getClient();
			String organizationId = vars.getOrg();
			String userId = vars.getUser();
			String from = salesrepEmail;
			String to = contactEmail;
			String cc = "";
			String bcc = salesrepEmail;
			if( userEmail != null && userEmail.length() > 0 )
				bcc = bcc + "; " + userEmail;
			String subject = emailSubject;
			String body = emailBody;
			String dateOfEmail = Utility.formatDate( new Date(), "yyyyMMddHHmmss");
			String bPartnerId = report.getBPartnerId();
			
			// Store the email in the database
		    Connection conn = null;
		    try
		    {
		    	conn = this.getTransactionConnection();		    	
		    	
		    	// First store the email message
		    	String newEmailId = SequenceIdData.getUUID();
		    	if (log4j.isDebugEnabled()) log4j.debug( "New email id: " + newEmailId );

				EmailData.insertEmail( conn, this, newEmailId, clientId, organizationId, userId, 
						EmailType.OUTGOING.getStringValue(), from, to, cc, bcc, dateOfEmail, subject, body, bPartnerId ); 
		    	
		    	releaseCommitConnection(conn);
		    }
		    catch ( NoConnectionAvailableException exception )
		    {
		    	log4j.error( exception );		    	
				throw new ServletException( exception );		    	
		    }
		    catch ( SQLException exception )
		    {
		    	log4j.error( exception );
		    	try
		    	{
		    		releaseRollbackConnection(conn);
		    	}
		    	catch (Exception ignored) {}
		    	
				throw new ServletException( exception );		    	
		    }
			
		}
		catch (PocException exception)
		{
	    	log4j.error( exception );
			throw new ServletException( exception );
		}
		catch (AddressException exception)
		{
	    	log4j.error( exception );
			throw new ServletException( exception );
		}
		catch (MessagingException exception)
		{
	    	log4j.error( exception );
			throw new ServletException( exception );
		}		
	}
	
	void createPrintOptionsPage( HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, DocumentType documentType, String strDocumentId, Map<String, Report> reports) throws IOException,
		ServletException
	{
	XmlDocument xmlDocument=null;
	xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/PrintOptions").createXmlDocument();
	xmlDocument.setParameter("strDocumentId", strDocumentId);
		
		
		// Get additional document information
		String draftDocumentIds = "";
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
	  xmlDocument.setParameter("language", vars.getLanguage());	      
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("description", "");
      xmlDocument.setParameter("help", "");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println(xmlDocument.print());
		out.close();		
	}

    void createEmailOptionsPage( HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars, DocumentType documentType, String strDocumentId, Map<String, Report> reports) throws IOException,
	ServletException
    {
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/EmailOptions").createXmlDocument();
    xmlDocument.setParameter("strDocumentId", strDocumentId);

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("language", vars.getLanguage());	      
    xmlDocument.setParameter("theme", vars.getTheme());
	
    // GET EMAIL TEMPLATE (SUBJECT AND BODY)
    // Temporarily get the email definition from the first report (99% of the time this is the correct one)
    // However, with multiple selected documents this can create wrong results (languages and different documents)
    EmailDefinition emailDefinition;
    try
    {
        emailDefinition = reports.values().iterator().next().getEmailDefinition();
        if (log4j.isDebugEnabled()) log4j.debug( "Crm configuration, template subject: " + emailDefinition.getSubject() );
        if (log4j.isDebugEnabled()) log4j.debug( "Crm configuration, template body: " + emailDefinition.getBody() );
    }
    catch (ReportingException exception)
    {
        throw new ServletException( exception );
    }
	
	// Get additional document information
	String draftDocumentIds = "";
	PocData[] pocData = getContactDetails( documentType, strDocumentId );
	for (PocData documentData : pocData)
	{
		Report report = reports.get( documentData.documentId );
		// All ids of documents in draft are passed to the web client
		if( report.isDraft() )
		{
			if( draftDocumentIds.length() > 0 )
				draftDocumentIds += ",";
			draftDocumentIds += report.getDocumentId(); 
		}
		
		// Fill the report location
		String reportFilename = report.getContextSubFolder() + report.getFilename();
		documentData.reportLocation = request.getContextPath() + "/" + reportFilename + "?documentId=" + documentData.documentId;
		if (log4j.isDebugEnabled()) log4j.debug( " Filling report location with: " + documentData.reportLocation );
	}
	if (log4j.isDebugEnabled()) log4j.debug( "Documents still in draft: " + draftDocumentIds );
	xmlDocument.setParameter( "draftDocumentIds", draftDocumentIds );

    xmlDocument.setParameter( "emailSubject", emailDefinition.getSubject() );
    xmlDocument.setParameter( "emailBody", emailDefinition.getBody() );
	
	xmlDocument.setData( "structure1", pocData);
	response.setContentType("text/html; charset=UTF-8");
	PrintWriter out = response.getWriter();
	out.println(xmlDocument.print());
	out.close();		
}
	
	void createPrintStatusPage( HttpServletResponse response, VariablesSecureApp vars, int nrOfEmailsSend ) throws IOException,
		ServletException
	{		
		XmlDocument xmlDocument=null;
		xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/PrintStatus").createXmlDocument();
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
	    xmlDocument.setParameter("theme", vars.getTheme());	
	    xmlDocument.setParameter("language", vars.getLanguage());		    
		xmlDocument.setParameter( "nrOfEmailsSend", "" + nrOfEmailsSend );

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		out.println(xmlDocument.print());
		out.close();		
	}

	public String getServletInfo()
	{
		return "Servlet that processes the print action";
	} // End of getServletInfo() method
}
