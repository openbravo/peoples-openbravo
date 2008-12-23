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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailData;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.erpCommon.utility.poc.EmailType;
import org.openbravo.erpCommon.utility.poc.PocConfigurationData;
import org.openbravo.erpCommon.utility.poc.PocData;
import org.openbravo.erpCommon.utility.poc.PocException;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateData;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo;
import org.openbravo.erpCommon.utility.reporting.ToolsData;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.xmlEngine.XmlDocument;

@SuppressWarnings("serial")
public class PrintController extends HttpSecureAppServlet {
    static Logger log4j = Logger.getLogger(PrintController.class);
    final Map differentDocTypes = new HashMap<String, TemplateData[]>();
    private PocData[] pocData;

    // TODO: When an email is in draft status add the notification that the
    // document can not be emailed

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final VariablesSecureApp vars = new VariablesSecureApp(request);

        DocumentType documentType = DocumentType.UNKNOWN;
        String sessionValuePrefix = null;
        String strDocumentId = null;

        // Determine which process called the print controller
        if (log4j.isDebugEnabled())
            log4j.debug("Servletpath: " + request.getServletPath());
        if (request.getServletPath().toLowerCase().indexOf("quotations") != -1) {
            documentType = DocumentType.QUOTATION;
            // The prefix PRINTORDERS is a fixed name based on the KEY of the
            // AD_PROCESS
            sessionValuePrefix = "PRINTQUOTATIONS";

            strDocumentId = vars.getSessionValue(sessionValuePrefix
                    + ".inpcOrderId_R");
            if (strDocumentId.equals(""))
                strDocumentId = vars.getSessionValue(sessionValuePrefix
                        + ".inpcOrderId");
        }
        if (request.getServletPath().toLowerCase().indexOf("orders") != -1) {
            documentType = DocumentType.SALESORDER;
            // The prefix PRINTORDERS is a fixed name based on the KEY of the
            // AD_PROCESS
            sessionValuePrefix = "PRINTORDERS";

            strDocumentId = vars.getSessionValue(sessionValuePrefix
                    + ".inpcOrderId_R");
            if (strDocumentId.equals(""))
                strDocumentId = vars.getSessionValue(sessionValuePrefix
                        + ".inpcOrderId");
        }
        if (request.getServletPath().toLowerCase().indexOf("invoices") != -1) {
            documentType = DocumentType.SALESINVOICE;
            // The prefix PRINTINVOICES is a fixed name based on the KEY of the
            // AD_PROCESS
            sessionValuePrefix = "PRINTINVOICES";

            strDocumentId = vars.getSessionValue(sessionValuePrefix
                    + ".inpcInvoiceId_R");
            if (strDocumentId.equals(""))
                strDocumentId = vars.getSessionValue(sessionValuePrefix
                        + ".inpcInvoiceId");
        }
        if (request.getServletPath().toLowerCase().indexOf("shipments") != -1) {
            documentType = DocumentType.SHIPMENT;
            // The prefix PRINTINVOICES is a fixed name based on the KEY of the
            // AD_PROCESS
            sessionValuePrefix = "PRINTSHIPMENTS";

            strDocumentId = vars.getSessionValue(sessionValuePrefix
                    + ".inpmInoutId_R");
            if (strDocumentId.equals(""))
                strDocumentId = vars.getSessionValue(sessionValuePrefix
                        + ".inpmInoutId");
        }

        post(request, response, vars, documentType, sessionValuePrefix,
                strDocumentId);

    }

    protected void post(HttpServletRequest request,
            HttpServletResponse response, VariablesSecureApp vars,
            DocumentType documentType, String sessionValuePrefix,
            String strDocumentId) throws IOException, ServletException {

        Map<String, Report> reports;
        String documentIds[] = null;
        if (log4j.isDebugEnabled())
            log4j.debug("strDocumentId: " + strDocumentId);
        // normalize the string of ids to a comma separated list
        strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
        if (strDocumentId.length() == 0)
            throw new ServletException(Utility.messageBD(this, "NoDocument",
                    vars.getLanguage()));

        documentIds = strDocumentId.split(",");

        if (log4j.isDebugEnabled())
            log4j.debug("Number of documents selected: " + documentIds.length);

        reports = (Map<String, Report>) vars
                .getSessionObject(sessionValuePrefix + ".Documents");
        final ReportManager reportManager = new ReportManager(this,
                globalParameters.strFTPDirectory, strReplaceWithFull,
                globalParameters.strBaseDesignPath,
                globalParameters.strDefaultDesignPath, globalParameters.prefix,
                classInfo);

        if (vars.commandIn("PRINT") || vars.commandIn("ARCHIVE")
                || (!request.getRequestURI().endsWith(".html"))) {
            final String documentId = vars.getStringParameter("inpDocumentId");
            if (strDocumentId != null)
                strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
            final Report report = reports.get(strDocumentId);
            if (report == null)
                throw new ServletException(Utility.messageBD(this,
                        "NoDataReport", vars.getLanguage())
                        + documentId);

            // Check if the document is not in status 'draft'
            if (!report.isDraft() && !report.isAttached()
                    && vars.commandIn("ARCHIVE")) {
                // TODO: Move the table Id retrieval into the DocumentType
                // getTableId method!
                // get the Id of the entities table, this is used to store the
                // file as an OB attachment
                final String tableId = ToolsData.getTableId(this, report
                        .getDocumentType().getTableName());

                if (log4j.isDebugEnabled())
                    log4j.debug("Table "
                            + report.getDocumentType().getTableName()
                            + " has table id: " + tableId);
                // Save the report as a attachment because it is being
                // transferred to the user
                try {
                    reportManager.createAttachmentForReport(this, report,
                            tableId, vars);
                } catch (final ReportingException exception) {
                    throw new ServletException(exception);
                }
            } else {
                if (log4j.isDebugEnabled())
                    log4j.debug("Document is not attached.");
            }

            // Dump the report to the users browser
            response.setContentType("application/pdf; charset=UTF-8");
            if (log4j.isDebugEnabled())
                log4j.debug("Dumping file: " + report.getTargetLocation());
            Utility.dumpFile(report.getTargetLocation(), response
                    .getOutputStream());
            response.flushBuffer();
        } else {
            if (vars.commandIn("DEFAULT")) {

                reports = new HashMap<String, Report>();
                for (int index = 0; index < documentIds.length; index++) {
                    final String documentId = documentIds[index];
                    if (log4j.isDebugEnabled())
                        log4j.debug("Processing document with id: "
                                + documentId);

                    try {
                        final Report report = new Report(this, documentType,
                                documentId, vars.getLanguage(), "default");
                        reports.put(documentId, report);

                        // TODO: The creation of the reports can be delayed
                        // until the actual report is viewed or emailed. This
                        // saves time when creating the window
                        reportManager.processReport(report, vars);

                        // check the different doc typeId's if all the selected
                        // doc's
                        // has the same doc typeId the template selector should
                        // appear
                        if (!differentDocTypes.containsKey(report
                                .getDocTypeId())) {
                            differentDocTypes.put(report.getDocTypeId(), report
                                    .getTemplate());
                        }
                    } catch (final ReportingException exception) {
                        throw new ServletException(exception);
                    }

                }

                vars.setSessionObject(sessionValuePrefix + ".Documents",
                        reports);
                // try {
                if (request.getServletPath().toLowerCase()
                        .indexOf("print.html") != -1)
                    createPrintOptionsPage(request, response, vars,
                            documentType, getComaSeparatedString(documentIds),
                            reports);
                else
                    createEmailOptionsPage(request, response, vars,
                            documentType, getComaSeparatedString(documentIds),
                            reports);
                // } catch (final ServletException e) {
                // TODO does not work
                // vars.getRequestGlobalVariable("inpTabId", "inpTabId");
                // final OBError on = new OBError();
                // on.setMessage("sdasdasdasdadasd");
                // on.setTitle("dsadasd");
                // on.setType("Error");
                // vars.setMessage("290", on);
                // vars.getRequestGlobalVariable("inpTabId",
                // "AttributeSetInstance.tabId");
                // printPageClosePopUpAndRefresh(response, vars);
                // }
            } else if (vars.commandIn("ADD")) {
                if (request.getServletPath().toLowerCase()
                        .indexOf("print.html") != -1)
                    createPrintOptionsPage(request, response, vars,
                            documentType, getComaSeparatedString(documentIds),
                            reports);
                else {
                    final boolean showList = true;
                    createEmailOptionsPage(request, response, vars,
                            documentType, getComaSeparatedString(documentIds),
                            reports);
                }

            } else if (vars.commandIn("DEL")) {
                final String documentToDelete = vars
                        .getStringParameter("idToDelete");
                final Vector<java.lang.Object> vector = (Vector<java.lang.Object>) request
                        .getSession().getAttribute("files");
                request.getSession().setAttribute("files", vector);

                seekAndDestroy(vector, documentToDelete);
                createEmailOptionsPage(request, response, vars, documentType,
                        getComaSeparatedString(documentIds), reports);

            } else if (vars.commandIn("EMAIL")) {
                int nrOfEmailsSend = 0;
                for (final PocData documentData : pocData) {
                    final String documentId = documentData.documentId;
                    if (log4j.isDebugEnabled())
                        log4j.debug("Processing document with id: "
                                + documentId);

                    final Report report = reports.get(documentId);

                    // if there is only one document type id the user should be
                    // able to choose between different templates
                    if (differentDocTypes.size() == 1) {
                        final String templateId = vars
                                .getRequestGlobalVariable("templates",
                                        "templates");
                        try {
                            final TemplateInfo usedTemplateInfo = new TemplateInfo(
                                    this, report.getDocTypeId(), report
                                            .getOrgId(), vars.getLanguage(),
                                    templateId);
                            report.setTemplateInfo(usedTemplateInfo);
                        } catch (final ReportingException e) {
                            throw new ServletException(
                                    "Error trying to get template information",
                                    e);
                        }
                    }
                    if (report == null)
                        throw new ServletException(Utility.messageBD(this,
                                "NoDataReport", vars.getLanguage())
                                + documentId);
                    // Check if the document is not in status 'draft'
                    if (!report.isDraft()) {
                        // Check if the report is already attached
                        if (!report.isAttached()) {
                            // get the Id of the entities table, this is used to
                            // store the file as an OB attachment
                            final String tableId = ToolsData.getTableId(this,
                                    report.getDocumentType().getTableName());

                            // If the user wants to archive the document
                            if (vars.getStringParameter("inpArchive").equals(
                                    "Y")) {
                                // Save the report as a attachment because it is
                                // being transferred to the user
                                try {
                                    reportManager.createAttachmentForReport(
                                            this, report, tableId, vars);
                                } catch (final ReportingException exception) {
                                    throw new ServletException(exception);
                                }
                            }
                        } else {
                            if (log4j.isDebugEnabled())
                                log4j.debug("Document is not attached.");
                        }

                        sendDocumentEmail(report, vars, request.getSession()
                                .getAttribute("files"), documentData);
                        nrOfEmailsSend++;
                    }
                }
                request.getSession().removeAttribute("files");
                createPrintStatusPage(response, vars, nrOfEmailsSend);
            }

            pageError(response);
        }
    }

    /**
     * 
     * @param vector
     * @param documentToDelete
     */
    private void seekAndDestroy(Vector<Object> vector, String documentToDelete) {
        for (int i = 0; i < vector.size(); i++) {
            final AttachContent content = (AttachContent) vector.get(i);
            if (content.id.equals(documentToDelete)) {
                vector.remove(i);
                break;
            }
        }

    }

    protected PocData[] getContactDetails(DocumentType documentType,
            String strDocumentId) throws ServletException {
        switch (documentType) {
        case QUOTATION:
            return PocData.getContactDetailsForOrders(this, strDocumentId);
        case SALESORDER:
            return PocData.getContactDetailsForOrders(this, strDocumentId);
        case SALESINVOICE:
            return PocData.getContactDetailsForInvoices(this, strDocumentId);
        case SHIPMENT:
            return PocData.getContactDetailsForShipments(this, strDocumentId);
        case PURCHASEORDER:
            return PocData.getContactDetailsForOrders(this, strDocumentId);
        }
        return null;
    }

    protected void sendDocumentEmail(Report report, VariablesSecureApp vars,
            Object object, PocData documentData) throws IOException,
            ServletException {
        final String documentId = report.getDocumentId();
        final String attachmentFileLocation = report.getTargetLocation();

        final String ourReference = report.getOurReference();
        final String cusReference = report.getCusReference();
        if (log4j.isDebugEnabled())
            log4j.debug("our document ref: " + ourReference);
        if (log4j.isDebugEnabled())
            log4j.debug("cus document ref: " + cusReference);
        // Also send it to the current user
        final PocData[] currentUserInfo = PocData.getContactDetailsForUser(
                this, vars.getUser());
        final String userName = currentUserInfo[0].userName;
        final String userEmail = currentUserInfo[0].userEmail;
        if (log4j.isDebugEnabled())
            log4j.debug("user name: " + userName);
        if (log4j.isDebugEnabled())
            log4j.debug("user email: " + userEmail);
        final String salesrepName = documentData.salesrepName;
        final String salesrepEmail = documentData.salesrepEmail;
        final String contactName = documentData.contactName;
        final String contactEmail = documentData.contactEmail;
        String emailSubject = vars.getStringParameter("emailSubject");
        String emailBody = vars.getStringParameter("emailBody");

        if (log4j.isDebugEnabled())
            log4j.debug("sales rep name: " + salesrepName);
        if (log4j.isDebugEnabled())
            log4j.debug("sales rep email: " + salesrepEmail);
        if (log4j.isDebugEnabled())
            log4j.debug("recipient name: " + contactName);
        if (log4j.isDebugEnabled())
            log4j.debug("recipient email: " + contactEmail);

        // TODO: Move this to the beginning of the print handling and do nothing
        // if these conditions fail!!!)

        if ((salesrepEmail == null || salesrepEmail.length() == 0)) {
            throw new ServletException(Utility.messageBD(this,
                    "NoSalesRepEmail", vars.getLanguage()));
        }

        if ((contactEmail == null || contactEmail.length() == 0)) {
            throw new ServletException(Utility.messageBD(this,
                    "NoCustomerEmail", vars.getLanguage()));
        }

        // Replace special tags

        emailSubject = emailSubject.replaceAll("@cus_ref@", cusReference);
        emailSubject = emailSubject.replaceAll("@our_ref@", ourReference);
        emailSubject = emailSubject.replaceAll("@cus_nam@", contactName);
        emailSubject = emailSubject.replaceAll("@sal_nam@", salesrepName);

        emailBody = emailBody.replaceAll("@cus_ref@", cusReference);
        emailBody = emailBody.replaceAll("@our_ref@", ourReference);
        emailBody = emailBody.replaceAll("@cus_nam@", contactName);
        emailBody = emailBody.replaceAll("@sal_nam@", salesrepName);

        try {
            final Session session = EmailManager.newMailSession(this, vars
                    .getClient());

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(PocConfigurationData
                    .getSenderAddress(this, vars.getClient())));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                    contactEmail));

            message.addRecipient(Message.RecipientType.BCC,
                    new InternetAddress(salesrepEmail));

            if (userEmail != null && userEmail.length() > 0)
                message.addRecipient(Message.RecipientType.BCC,
                        new InternetAddress(userEmail));

            message.setSubject(emailSubject);

            // Content consists of 2 parts, the message body and the attachment
            // We therefor use a multipart message
            final Multipart multipart = new MimeMultipart();

            // Create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(emailBody);
            multipart.addBodyPart(messageBodyPart);

            // Create the attachment part
            messageBodyPart = new MimeBodyPart();
            final DataSource source = new FileDataSource(attachmentFileLocation);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentFileLocation
                    .substring(attachmentFileLocation.lastIndexOf("/") + 1));
            multipart.addBodyPart(messageBodyPart);

            // Add aditional attached documents
            if (object != null) {
                final Vector<java.lang.Object> vector = (Vector<java.lang.Object>) object;
                for (int i = 0; i < vector.size(); i++) {
                    final AttachContent content = (AttachContent) vector.get(i);
                    final File file = prepareFile(content);
                    messageBodyPart = new MimeBodyPart();
                    messageBodyPart.attachFile(file);
                    multipart.addBodyPart(messageBodyPart);
                }
            }

            message.setContent(multipart);

            // Send the email
            Transport.send(message);

            final String clientId = vars.getClient();
            final String organizationId = vars.getOrg();
            final String userId = vars.getUser();
            final String from = salesrepEmail;
            final String to = contactEmail;
            final String cc = "";
            String bcc = salesrepEmail;
            if (userEmail != null && userEmail.length() > 0)
                bcc = bcc + "; " + userEmail;
            final String subject = emailSubject;
            final String body = emailBody;
            final String dateOfEmail = Utility.formatDate(new Date(),
                    "yyyyMMddHHmmss");
            final String bPartnerId = report.getBPartnerId();

            // Store the email in the database
            Connection conn = null;
            try {
                conn = this.getTransactionConnection();

                // First store the email message
                final String newEmailId = SequenceIdData.getUUID();
                if (log4j.isDebugEnabled())
                    log4j.debug("New email id: " + newEmailId);

                EmailData.insertEmail(conn, this, newEmailId, clientId,
                        organizationId, userId, EmailType.OUTGOING
                                .getStringValue(), from, to, cc, bcc,
                        dateOfEmail, subject, body, bPartnerId);

                releaseCommitConnection(conn);
            } catch (final NoConnectionAvailableException exception) {
                log4j.error(exception);
                throw new ServletException(exception);
            } catch (final SQLException exception) {
                log4j.error(exception);
                try {
                    releaseRollbackConnection(conn);
                } catch (final Exception ignored) {
                }

                throw new ServletException(exception);
            }

        } catch (final PocException exception) {
            log4j.error(exception);
            throw new ServletException(exception);
        } catch (final AddressException exception) {
            log4j.error(exception);
            throw new ServletException(exception);
        } catch (final MessagingException exception) {
            log4j.error(exception);
            throw new ServletException(exception);
        }
    }

    void createPrintOptionsPage(HttpServletRequest request,
            HttpServletResponse response, VariablesSecureApp vars,
            DocumentType documentType, String strDocumentId,
            Map<String, Report> reports) throws IOException, ServletException {
        XmlDocument xmlDocument = null;
        xmlDocument = xmlEngine
                .readXmlTemplate(
                        "org/openbravo/erpCommon/utility/reporting/printing/PrintOptions")
                .createXmlDocument();
        xmlDocument.setParameter("strDocumentId", strDocumentId);

        // Get additional document information
        final String draftDocumentIds = "";
        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\r\n");
        xmlDocument.setParameter("language", vars.getLanguage());
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("description", "");
        xmlDocument.setParameter("help", "");
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    void createEmailOptionsPage(HttpServletRequest request,
            HttpServletResponse response, VariablesSecureApp vars,
            DocumentType documentType, String strDocumentId,
            Map<String, Report> reports) throws IOException, ServletException {
        XmlDocument xmlDocument = null;
        pocData = getContactDetails(documentType, strDocumentId);
        Vector<java.lang.Object> vector = (Vector<java.lang.Object>) request
                .getSession().getAttribute("files");

        final String[] hiddenTags = getHiddenTags(pocData, vector, vars);
        if (hiddenTags != null) {
            xmlDocument = xmlEngine
                    .readXmlTemplate(
                            "org/openbravo/erpCommon/utility/reporting/printing/EmailOptions",
                            hiddenTags).createXmlDocument();
        } else {
            xmlDocument = xmlEngine
                    .readXmlTemplate(
                            "org/openbravo/erpCommon/utility/reporting/printing/EmailOptions")
                    .createXmlDocument();
        }

        xmlDocument.setParameter("strDocumentId", strDocumentId);

        boolean isTheFirstEntry = false;
        if (vector == null) {
            vector = new Vector<java.lang.Object>(0);
            isTheFirstEntry = new Boolean(true);
        }

        final AttachContent file = new AttachContent();
        if (vars.getMultiFile("inpFile") != null
                && !vars.getMultiFile("inpFile").getName().equals("")) {
            final AttachContent content = new AttachContent();
            final FileItem file1 = vars.getMultiFile("inpFile");
            content.setFileName(file1.getName());
            content.setFileItem(file1);
            content.setId(file1.getName());
            content.visible = "hidden";
            if (vars.getStringParameter("inpArchive") == "Y") {
                content.setSelected("true");
            }
            vector.addElement(content);
            request.getSession().setAttribute("files", vector);

        }

        if ("yes".equals(vars.getStringParameter("closed"))) {
            xmlDocument.setParameter("closed", "yes");
            request.getSession().removeAttribute("files");
        }

        // TODO aki
        // new TemplateInfo(this, documentType., (String) request.getSession()
        // .getAttribute("AD_ORG_ID"), vars.getLanguage(), vars
        // .getRequestGlobalVariable("templates", "templates"));
        // xmlDocument.setData("reportEmail", "liststructure", report
        // .getTemplate());

        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\r\n");
        xmlDocument.setParameter("language", vars.getLanguage());
        xmlDocument.setParameter("theme", vars.getTheme());

        // GET EMAIL TEMPLATE (SUBJECT AND BODY)
        // Temporarily get the email definition from the first report (99% of
        // the time this is the correct one)
        // However, with multiple selected documents this can create wrong
        // results (languages and different documents)
        EmailDefinition emailDefinition;
        try {
            emailDefinition = reports.values().iterator().next()
                    .getEmailDefinition();
            if (log4j.isDebugEnabled())
                log4j.debug("Crm configuration, template subject: "
                        + emailDefinition.getSubject());
            if (log4j.isDebugEnabled())
                log4j.debug("Crm configuration, template body: "
                        + emailDefinition.getBody());
        } catch (final ReportingException exception) {
            throw new ServletException(exception);
        }

        // Get additional document information
        String draftDocumentIds = "";
        final AttachContent attachedContent = new AttachContent();
        final boolean onlyOneAttachedDoc = onlyOneAttachedDocs(reports);
        final Map<String, PocData> customerMap = new HashMap<String, PocData>();
        final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
        final Vector<Object> cloneVector = new Vector<Object>();
        for (final PocData documentData : pocData) {
            // Map used to count the different users

            final String customer = documentData.contactName;
            if (customer == null || customer.length() == 0)
                throw new ServletException(Utility.messageBD(this,
                        "There is at least one document with no contact", vars
                                .getLanguage()));
            if (!customerMap.containsKey(customer)) {
                customerMap.put(customer, documentData);
            }

            final String salesRep = documentData.salesrepName;
            if (salesRep == null || salesRep.length() == 0)
                throw new ServletException(
                        Utility
                                .messageBD(
                                        this,
                                        "There is at least one document with no sales representive",
                                        vars.getLanguage()));
            if (!salesRepMap.containsKey(salesRep)) {
                salesRepMap.put(salesRep, documentData);
            }

            final Report report = reports.get(documentData.documentId);
            // All ids of documents in draft are passed to the web client
            if (report.isDraft()) {
                if (draftDocumentIds.length() > 0)
                    draftDocumentIds += ",";
                draftDocumentIds += report.getDocumentId();
            }

            // Fill the report location
            final String reportFilename = report.getContextSubFolder()
                    + report.getFilename();
            documentData.reportLocation = request.getContextPath() + "/"
                    + reportFilename + "?documentId=" + documentData.documentId;
            if (log4j.isDebugEnabled())
                log4j.debug(" Filling report location with: "
                        + documentData.reportLocation);

            if (onlyOneAttachedDoc) {
                attachedContent.setDocName(report.getFilename());
                attachedContent.setVisible("checkbox");
                cloneVector.add(attachedContent);
            }

        }

        final int numberOfCustomers = customerMap.size();
        final int numberOfSalesReps = salesRepMap.size();

        if (!onlyOneAttachedDoc && isTheFirstEntry) {
            if (numberOfCustomers > 1) {
                attachedContent.setDocName(String.valueOf(reports.size()
                        + " Documents to " + String.valueOf(numberOfCustomers)
                        + " Customers"));
                attachedContent.setVisible("checkbox");

            } else {
                attachedContent.setDocName(String.valueOf(reports.size()
                        + " Documents"));
                attachedContent.setVisible("checkbox");

            }
            cloneVector.add(attachedContent);
        }

        final AttachContent[] data = new AttachContent[vector.size()];
        final AttachContent[] data2 = new AttachContent[cloneVector.size()];
        if (cloneVector.size() >= 1) { // Has more than 1 element
            vector.copyInto(data);
            cloneVector.copyInto(data2);
            xmlDocument.setData("structure2", data2);
            xmlDocument.setData("structure1", data);
        }
        if (pocData.length >= 1) {
            xmlDocument.setData("reportEmail", "liststructure", reports.get(
                    (pocData[0].documentId)).getTemplate());
        }

        if (log4j.isDebugEnabled())
            log4j.debug("Documents still in draft: " + draftDocumentIds);
        xmlDocument.setParameter("draftDocumentIds", draftDocumentIds);

        if (vars.commandIn("ADD") || vars.commandIn("DEL")) {
            final String emailSubject = vars.getStringParameter("emailSubject");
            final String emailBody = vars.getStringParameter("emailBody");
            xmlDocument.setParameter("emailSubject", emailSubject);
            xmlDocument.setParameter("emailBody", emailBody);
            xmlDocument.setParameter("contactEmail", vars
                    .getStringParameter("contactEmail"));
            xmlDocument.setParameter("salesrepEmail", vars
                    .getStringParameter("salesrepEmail"));
        } else {
            xmlDocument.setParameter("emailSubject", emailDefinition
                    .getSubject());
            xmlDocument.setParameter("contactEmail", pocData[0].contactEmail);
            xmlDocument.setParameter("salesrepEmail", pocData[0].salesrepEmail);
            xmlDocument.setParameter("emailBody", emailDefinition.getBody());
        }

        xmlDocument.setParameter("inpArchive", vars
                .getStringParameter("inpArchive"));
        xmlDocument.setParameter("contactName", pocData[0].contactName);
        xmlDocument.setParameter("salesrepName", pocData[0].salesrepName);
        xmlDocument.setParameter("inpArchive", vars
                .getStringParameter("inpArchive"));
        xmlDocument.setParameter("multCusCount", String
                .valueOf(numberOfCustomers));
        xmlDocument.setParameter("multSalesRepCount", String
                .valueOf(numberOfSalesReps));
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    /**
     * @author gmauleon
     * @param pocData
     * @param vars
     * @param vector
     * @return
     */
    private String[] getHiddenTags(PocData[] pocData, Vector<Object> vector,
            VariablesSecureApp vars) {
        String[] discard;
        final Map<String, PocData> customerMap = new HashMap<String, PocData>();
        final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
        for (final PocData documentData : pocData) {
            // Map used to count the different users

            final String customer = documentData.contactName;
            final String salesRep = documentData.salesrepName;
            if (!customerMap.containsKey(customer)) {
                customerMap.put(customer, documentData);
            }
            if (!salesRepMap.containsKey(salesRep)) {
                salesRepMap.put(salesRep, documentData);
            }
        }
        final boolean moreThanOneCustomer = (customerMap.size() > 1);
        final boolean moreThanOnesalesRep = (salesRepMap.size() > 1);

        // check the number of customer and the number of
        // sales Rep. to choose one of the 3 possibilities
        // 1.- n customer n sales rep (hide both inputs)
        // 2.- n customers 1 sales rep (hide only first input)
        // 3.- Otherwise show both
        if (moreThanOneCustomer && moreThanOnesalesRep) {
            discard = new String[] { "customer", "salesRep" };
        } else if (moreThanOneCustomer) {
            discard = new String[] { "customer", "multSalesRep",
                    "multSalesRepCount" };
        } else {
            discard = new String[] { "multipleCustomer" };
        }

        // check the templates
        if (differentDocTypes.size() > 1) { // the templates selector shouldn't
            // appear
            if (discard == null) { // Its the only think to hide
                discard = new String[] { "discardSelect" };
            } else {
                final String[] discardAux = new String[discard.length + 1];
                for (int i = 0; i < discard.length; i++) {
                    discardAux[0] = discard[0];
                }
                discardAux[discard.length] = "discardSelect";
                return discardAux;
            }
        }
        if (vector == null && vars.getMultiFile("inpFile") == null) {
            if (discard == null) {
                discard = new String[] { "view" };
            } else {
                final String[] discardAux = new String[discard.length + 1];
                for (int i = 0; i < discard.length; i++) {
                    discardAux[i] = discard[i];
                }
                discardAux[discard.length] = "view";
                return discardAux;
            }
        }
        return discard;
    }

    private boolean onlyOneAttachedDocs(Map<String, Report> reports) {
        if (reports.size() == 1) {
            return true;
        } else {
            return false;
        }

    }

    void createPrintStatusPage(HttpServletResponse response,
            VariablesSecureApp vars, int nrOfEmailsSend) throws IOException,
            ServletException {
        XmlDocument xmlDocument = null;
        xmlDocument = xmlEngine
                .readXmlTemplate(
                        "org/openbravo/erpCommon/utility/reporting/printing/PrintStatus")
                .createXmlDocument();
        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\r\n");
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("language", vars.getLanguage());
        xmlDocument.setParameter("nrOfEmailsSend", "" + nrOfEmailsSend);

        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();

        out.println(xmlDocument.print());
        out.close();
    }

    /**
     * 
     * @param documentIds
     * @return returns a comma separated and quoted string of documents id's.
     *         useful to sql querys
     */
    private String getComaSeparatedString(String[] documentIds) {
        String result = new String("(");
        for (int index = 0; index < documentIds.length; index++) {
            final String documentId = documentIds[index];
            if (index + 1 == documentIds.length) {
                result = result + "'" + documentId + "')";
            } else {
                result = result + "'" + documentId + "',";
            }

        }
        return result;
    }

    /**
     * @author gmauleon
     * @param content
     * @return
     * @throws ServletException
     */
    private File prepareFile(AttachContent content) throws ServletException {
        try {
            final File f = new File(content.getFileName());
            final InputStream inputStream = content.getFileItem()
                    .getInputStream();
            final OutputStream out = new FileOutputStream(f);
            final byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();
            inputStream.close();
            return f;
        } catch (final Exception e) {
            throw new ServletException("Error trying to get the attached file",
                    e);
        }

    }

    @Override
    public String getServletInfo() {
        return "Servlet that processes the print action";
    } // End of getServletInfo() method
}
