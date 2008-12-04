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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;

public class Report {
    private static Logger log4j = Logger.getLogger(Report.class);

    private DocumentType _DocumentType;
    private String _DocumentId; // Order Id, invoice id, etc.
    private String _DocumentStatus;
    private String _OurReference;
    private String _CusReference;
    private String _BPartnerId;
    private String _BPartnerLanguage;
    private String _Filename;
    private File _targetDirectory;
    private boolean _isAttached;
    private String docTypeId;

    public String getDocTypeId() {
        return docTypeId;
    }

    public void setDocTypeId(String docTypeId) {
        this.docTypeId = docTypeId;
    }

    private TemplateInfo _TemplateInfo;

    public Report(ConnectionProvider connectionProvider,
            DocumentType documentType, String documentId, String strLanguage,
            String templateId) throws ReportingException, ServletException {
        _DocumentType = documentType;
        _DocumentId = documentId;
        ReportData[] reportData = null;

        switch (_DocumentType) {
        case QUOTATION: // Retrieve quotation information
            reportData = ReportData
                    .getOrderInfo(connectionProvider, documentId);
            break;
        case SALESORDER: // Retrieve order information
            reportData = ReportData
                    .getOrderInfo(connectionProvider, documentId);
            break;

        case SALESINVOICE: // Retrieve invoice information
            reportData = ReportData.getInvoiceInfo(connectionProvider,
                    documentId);
            break;

        case SHIPMENT: // Retrieve shipment information
            reportData = ReportData.getShipmentInfo(connectionProvider,
                    documentId);
            break;

        default:
            throw new ReportingException(Utility.messageBD(connectionProvider,
                    "UnknownDocumentType", strLanguage)
                    + _DocumentType);
        }

        if (reportData.length == 1) {
            final String orgId = reportData[0].getField("ad_Org_Id");
            docTypeId = reportData[0].getField("docTypeTargetId");

            _OurReference = reportData[0].getField("ourreference");
            _CusReference = reportData[0].getField("cusreference");
            _BPartnerId = reportData[0].getField("bpartner_id");
            _BPartnerLanguage = reportData[0].getField("bpartner_language");
            _DocumentStatus = reportData[0].getField("docstatus");
            _TemplateInfo = new TemplateInfo(connectionProvider, docTypeId,
                    orgId, strLanguage, templateId);

            _Filename = generateReportFileName();
            _targetDirectory = null;
        } else
            throw new ReportingException(Utility.messageBD(connectionProvider,
                    "NoDataReport", strLanguage)
                    + documentId);

    }

    private String generateReportFileName() {
        // Generate the target report filename
        final String dateStamp = Utility.formatDate(new Date(),
                "yyyyMMdd-HHmmss");

        String reportFilename = _TemplateInfo.getReportFilename();
        reportFilename = reportFilename.replaceAll("@our_ref@", _OurReference);
        reportFilename = reportFilename.replaceAll("@cus_ref@", _CusReference);
        reportFilename = reportFilename + "." + dateStamp + ".pdf";
        if (log4j.isDebugEnabled())
            log4j.debug("target report filename: " + reportFilename);

        return reportFilename;
    }

    public String getContextSubFolder() throws ServletException {
        return _DocumentType.getContextSubFolder();
    }

    public DocumentType getDocumentType() {
        return _DocumentType;
    }

    public String getDocumentId() {
        return _DocumentId;
    }

    public TemplateInfo getTemplateInfo() {
        return _TemplateInfo;
    }

    public EmailDefinition getEmailDefinition() throws ReportingException {
        return _TemplateInfo.getEmailDefinition(_BPartnerLanguage);
    }

    public String getOurReference() {
        return _OurReference;
    }

    public String getCusReference() {
        return _CusReference;
    }

    public String getDocumentStatus() {
        return _DocumentStatus;
    }

    public String getBPartnerId() {
        return _BPartnerId;
    }

    public boolean isDraft() {
        return _DocumentStatus.equals("DR");
    }

    // public boolean isCompleted()
    // {
    // return _DocumentStatus.equals( "CO" );
    // }

    public String getFilename() {
        return _Filename;
    }

    public File getTargetDirectory() {
        return _targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        _targetDirectory = targetDirectory;
    }

    public String getTargetLocation() throws IOException {
        return _targetDirectory.getCanonicalPath() + "/" + _Filename;
    }

    public boolean isAttached() {
        return _isAttached;
    }

    public void setAttached(boolean attached) {
        _isAttached = attached;
    }

    public TemplateData[] getTemplate() {
        if (_TemplateInfo.getTemplates() != null) {
            return _TemplateInfo.getTemplates();
        }
        return null;
    }
}
