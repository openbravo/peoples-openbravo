/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2001-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.window.AttachImplementationManager;
import org.openbravo.client.application.window.AttachmentUtils;
import org.openbravo.client.application.window.AttachmentsAH;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabAttachments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static Logger log = LoggerFactory.getLogger(TabAttachments.class);

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    final VariablesSecureApp vars = new VariablesSecureApp(request);
    post(vars, request, response);
  }

  public void post(VariablesSecureApp vars, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    AttachImplementationManager aim = WeldUtils
        .getInstanceFromStaticBeanManager(AttachImplementationManager.class);
    if (vars.getCommand().startsWith("SAVE_NEW")) {

      final String strTab = vars.getStringParameter("inpTabId");
      final String key = vars.getStringParameter("inpKey");
      final String strDataType = vars.getStringParameter("inpadDatatypeId");
      final String strDocumentOrganization = vars.getStringParameter("inpDocumentOrg");
      final FileItem file = vars.getMultiFile("inpname");
      if (file == null) {
        throw new ServletException("Empty file");
      }
      final String tmpFolder = System.getProperty("java.io.tmpdir");

      String strName = file.getName();
      int i = strName.lastIndexOf(File.separator);
      if (i != -1) {
        strName = strName.substring(i + 1);
      }
      File tempFile = new File(tmpFolder, strName);
      OBContext.setAdminMode(true);
      Tab tab = null;
      JSONObject obj = new JSONObject();
      String strMessage = "";

      try {
        tab = OBDal.getInstance().get(Tab.class, strTab);
        obj = AttachmentsAH.getAttachmentJSONObject(tab, key);
        try {
          file.write(tempFile);
        } catch (Exception e) {
          log.error("Error creating temp file", e);
          throw new OBException(OBMessageUtils.messageBD("ErrorUploadingFile"), e);
        }
        //
        Map<String, String> metadata = new HashMap<String, String>();
        AttachmentConfig attConfig = AttachmentUtils.getAttachmentConfig();
        AttachmentMethod attachMethod;
        if (attConfig == null) {
          attachMethod = AttachmentUtils.getDefaultAttachmentMethod();
        } else {
          attachMethod = attConfig.getAttachmentMethod();
        }
        Map<String, String> fixedParameters = ParameterUtils.fixRequestMap(request);
        for (Parameter param : AttachmentUtils.getMethodMetadataParameters(attachMethod, tab)) {
          String value;
          if (param.isFixed()) {
            if (param.getPropertyPath() != null) {
              // not relevant value
              value = "Property Path";
            } else if (param.isEvaluateFixedValue()) {
              value = ParameterUtils.getParameterFixedValue(fixedParameters, param).toString();
            } else {
              value = param.getFixedValue();
            }
          } else {
            value = vars.getStringParameter(param.getDBColumnName()).toString();
          }
          metadata.put(param.getId(), value);
        }

        aim.upload(strTab, key, strDataType, strDocumentOrganization, metadata, tempFile);
        obj = AttachmentsAH.getAttachmentJSONObject(tab, key);
      } catch (OBException e) {
        OBDal.getInstance().rollbackAndClose();
        log.error("Error uploading the file", e);
        strMessage = e.getMessage();
      } finally {
        OBContext.restorePreviousMode();
        if (tempFile.exists()) { // If tempFile still exists in attachments/tmp must be removed
          tempFile.delete();
        }
      }
      printResponse(response, vars, obj, strMessage);

    } else if (vars.commandIn("DOWNLOAD_FILE")) {
      final String strFileReference = vars.getStringParameter("attachmentId");
      ByteArrayOutputStream os = null;
      try {
        OBContext.setAdminMode(true);
        os = new ByteArrayOutputStream();
        aim.download(strFileReference, os);
        Attachment attachment = OBDal.getInstance().get(Attachment.class, strFileReference);

        if (attachment.getDataType().equals("")) {
          response.setContentType("application/txt");
        } else {
          response.setContentType(attachment.getDataType());
        }

        response.setCharacterEncoding("UTF-8");
        String userAgent = request.getHeader("user-agent");
        if (userAgent.contains("MSIE")) {
          response.setHeader(
              "Content-Disposition",
              "attachment; filename=\""
                  + URLEncoder.encode(attachment.getName().replace("\"", "\\\""), "utf-8") + "\"");
        } else {
          response.setHeader(
              "Content-Disposition",
              "attachment; filename=\""
                  + MimeUtility
                      .encodeWord(attachment.getName().replace("\"", "\\\""), "utf-8", "Q") + "\"");
        }

        response.getOutputStream().write(os.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();

      } catch (OBException e) {
        log.error("Error downloading file.", e);
        printResponse(response, vars, null, e.getMessage());

      } finally {
        if (os != null) {
          os.close();
        }
        OBContext.restorePreviousMode();
      }

    } else if (vars.getCommand().contains("DOWNLOAD_ALL")) {
      String tabId = vars.getStringParameter("tabId");
      String recordIds = vars.getStringParameter("recordIds");
      ByteArrayOutputStream os = null;
      try {
        os = new ByteArrayOutputStream();
        aim.dowloadAll(tabId, recordIds, os);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=attachments.zip");
        response.getOutputStream().write(os.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();
      } catch (OBException e) {
        log.error("Error downloading all files.", e);
        printResponse(response, vars, null, e.getMessage());

      } finally {
        if (os != null) {
          os.close();
        }
      }

      // This command is only for 2.50
    } else if (vars.commandIn("CHECK")) {
      final String tabId = vars.getStringParameter("inpTabId");
      final String inpKey = vars.getStringParameter("inpKey");
      printPageCheck(response, vars, tabId, inpKey);
    } else
      pageError(response);
  }

  private void printResponse(HttpServletResponse response, VariablesSecureApp vars, JSONObject obj,
      String strMessage) throws IOException {
    response.setContentType("text/html; charset=UTF-8");
    Writer writer = response.getWriter();
    writer.write("<HTML><BODY><script type=\"text/javascript\">");
    if (obj != null) {
      final String buttonId = vars.getStringParameter("buttonId");
      writer.write("top.OB.Utilities.uploadFinished(\"" + buttonId + "\"," + obj.toString() + ");");
    }
    if (StringUtils.isNotBlank(strMessage)) {
      final String viewId = vars.getStringParameter("viewId");
      writer
          .write("top.OB.Utilities.writeErrorMessage(\"" + viewId + "\",\"" + strMessage + "\");");
    }
    writer.write("</SCRIPT></BODY></HTML>");

  }

  private void printPageCheck(HttpServletResponse response, VariablesSecureApp vars, String strTab,
      String recordId) throws IOException, ServletException {
    response.setContentType("text/plain; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.print(Utility.hasTabAttachments(this, vars, strTab, recordId));
    out.close();
  }

  /**
   * Provides the directory in which the attachment has to be stored. For example for tableId "259",
   * recordId "0F3A10E019754BACA5844387FB37B0D5", the file directory returned is
   * "259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5". In case 'SaveAttachmentsOldWay' preference is
   * enabled then the file directory returned is "259-0F3A10E019754BACA5844387FB37B0D5"
   * 
   * @param tableID
   *          UUID of the table
   * 
   * @param recordID
   *          UUID of the record
   * 
   * @return file directory to save the attachment
   */
  public static String getAttachmentDirectoryForNewAttachments(String tableID, String recordID) {
    String fileDir = tableID + "-" + recordID;
    String saveAttachmentsOldWay = null;
    try {
      saveAttachmentsOldWay = Preferences.getPreferenceValue("SaveAttachmentsOldWay", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null);
    } catch (PropertyException e) {
      // if property not found, save attachments the new way
      saveAttachmentsOldWay = "N";
    }

    if ("Y".equals(saveAttachmentsOldWay)) {
      return fileDir;
    } else {
      fileDir = tableID + "/" + splitPath(recordID);
    }
    return fileDir;
  }

  /**
   * Provides the directory in which the attachment is stored. For example for tableId "259",
   * recordId "0F3A10E019754BACA5844387FB37B0D5", and fileName "test.txt" the file directory
   * returned is "259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5". In case 'SaveAttachmentsOldWay'
   * preference is enabled then the file directory returned is
   * "259-0F3A10E019754BACA5844387FB37B0D5"
   * 
   * @param tableID
   *          UUID of the table
   * 
   * @param recordID
   *          UUID of the record
   * 
   * @param fileName
   *          Name of the file
   * 
   * @return file directory in which the attachment is stored
   */
  public static String getAttachmentDirectory(String tableID, String recordID, String fileName) {
    String fileDir = tableID + "-" + recordID;
    Table attachmentTable = null;
    try {
      OBContext.setAdminMode();
      attachmentTable = OBDal.getInstance().get(Table.class, tableID);
      OBCriteria<Attachment> attachmentCriteria = OBDal.getInstance().createCriteria(
          Attachment.class);
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordID));
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_TABLE, attachmentTable));
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_NAME, fileName));

      attachmentCriteria.setFilterOnReadableOrganization(false);
      if (attachmentCriteria.count() > 0) {
        Attachment attachment = attachmentCriteria.list().get(0);
        if (attachment.getPath() != null) {
          fileDir = attachment.getPath();
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return fileDir;
  }

  /**
   * Provides the value to be saved in path field in c_file. The path field is used to get the
   * location of the attachment. For example 259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5. This
   * path is relative to the attachments folder
   * 
   * @param fileDirectory
   *          the directory that is retrieved from getFileDirectory()
   * 
   * @return value to be saved in path in c_file
   */
  public static String getPath(String fileDirectory) {
    if (fileDirectory != null && fileDirectory.contains("-")) {
      return null;
    } else {
      return fileDirectory;
    }
  }

  /**
   * Splits the path name component so that the resulting path name is 3 characters long sub
   * directories. For example 12345 is splitted to 123/45
   * 
   * @param origname
   *          Original name
   * @return splitted name.
   */
  public static String splitPath(final String origname) {
    String newname = "";
    for (int i = 0; i < origname.length(); i += 3) {
      if (i != 0) {
        newname += "/";
      }
      newname += origname.substring(i, Math.min(i + 3, origname.length()));
    }
    return newname;
  }

  @Override
  public String getServletInfo() {
    return "Servlet that presents the attachments";
  } // end of getServletInfo() method
}
