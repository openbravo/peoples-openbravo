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
import org.apache.log4j.Logger;
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
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.openbravo.xmlEngine.XmlDocument;

public class TabAttachments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(TabAttachments.class);

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
      vars.setSessionValue("TabAttachments.tabId", strTab);
      final String strWindow = vars.getStringParameter("inpwindowId");
      vars.setSessionValue("TabAttachments.windowId", strWindow);
      final String key = vars.getStringParameter("inpKey");
      vars.setSessionValue("TabAttachments.key", key);
      final String strDataType = vars.getStringParameter("inpadDatatypeId");

      final String strDocumentOrganization = vars.getStringParameter("inpDocumentOrg");
      final String inpName = "inpname";
      final FileItem file = vars.getMultiFile(inpName);
      if (file == null)
        throw new ServletException("Empty file");
      final String tmpFolder = System.getProperty("java.io.tmpdir");
      final File targetDirectory = new File(tmpFolder);
      if (!targetDirectory.exists()) {
        targetDirectory.mkdirs();
      }
      String strName = file.getName();
      // FIXME: Get the directory separator from Java runtime
      int i = strName.lastIndexOf("\\");
      if (i != -1) {
        strName = strName.substring(i + 1);
        // FIXME: Get the directory separator from Java runtime
      } else if ((i = strName.lastIndexOf("/")) != -1) {
        strName = strName.substring(i + 1);
      }
      File tempFile = new File(targetDirectory, strName);
      OBContext.setAdminMode();
      Tab tab = OBDal.getInstance().get(Tab.class, strTab);
      JSONObject obj = AttachmentsAH.getAttachmentJSONObject(tab, key);
      String buttonId = vars.getStringParameter("buttonId");
      try {
        try {
          file.write(tempFile);
        } catch (Exception e) {
          log.error("Error creating temp file");
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
            if (!param.isUserEditable() && param.getPropertyPath() != null) {
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
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write("<HTML><BODY><script type=\"text/javascript\">");
        writer.write("top.OB.Utilities.uploadFinished(\"" + buttonId + "\"," + obj.toString()
            + ");");
        writer.write("</SCRIPT></BODY></HTML>");
      } catch (OBException e) {
        OBDal.getInstance().rollbackAndClose();
        log.error(e.getMessage());

        String viewId = vars.getStringParameter("viewId");
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write("<HTML><BODY><script type=\"text/javascript\">");
        writer.write("top.OB.Utilities.uploadFinished(\"" + buttonId + "\"," + obj.toString()
            + ");");
        writer.write("top.OB.Utilities.writeErrorMessage(\"" + viewId + "\",\"" + e.getMessage()
            + "\");");
        writer.write("</SCRIPT></BODY></HTML>");
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        log.error(e.getMessage(), e);

        String viewId = vars.getStringParameter("viewId");
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write("<HTML><BODY><script type=\"text/javascript\">");
        writer.write("top.OB.Utilities.uploadFinished(\"" + buttonId + "\"," + obj.toString()
            + ");");
        writer.write("top.OB.Utilities.writeErrorMessage(\"" + viewId + "\",\"" + e.getMessage()
            + "\");");
        writer.write("</SCRIPT></BODY></HTML>");
      } finally {
        OBContext.restorePreviousMode();
        if (tempFile.exists()) { // If tempFile still exists in attachments/tmp must be removed
          tempFile.delete();
        }
      }
    } else if (vars.commandIn("DISPLAY_DATA")) {
      final String strFileReference = vars.getStringParameter("inpcFileId");
      ByteArrayOutputStream os = null;
      try {
        OBContext.setAdminMode();
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
        log.error(e.getMessage());
        String viewId = vars.getStringParameter("viewId");
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write("<HTML><BODY><script type=\"text/javascript\">");
        writer.write("top.OB.Utilities.writeErrorMessage(\"" + viewId + "\",\"" + e.getMessage()
            + "\");");
        writer.write("</SCRIPT></BODY></HTML>");

      } finally {
        os.close();
        OBContext.restorePreviousMode();
      }

    } else if (vars.getCommand().contains("GET_MULTIPLE_RECORDS_OB3")) {
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
        log.error(e.getMessage());
        String viewId = vars.getStringParameter("viewId");
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write("<HTML><BODY><script type=\"text/javascript\">");
        writer.write("top.OB.Utilities.writeErrorMessage(\"" + viewId + "\",\"" + e.getMessage()
            + "\");");
        writer.write("</SCRIPT></BODY></HTML>");

      } finally {
        os.close();
      }

    } else if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpTabId", "TabAttachments.tabId");
      vars.getGlobalVariable("inpwindowId", "TabAttachments.windowId");
      vars.getGlobalVariable("inpKey", "TabAttachments.key");
      vars.getGlobalVariable("inpEditable", "TabAttachments.editable");
      printPageFS(response, vars);
      // This command is only for 2.50
    } else if (vars.commandIn("FRAME1", "RELATION")) {
      final String strTab = vars.getGlobalVariable("inpTabId", "TabAttachments.tabId");
      final String strWindow = vars.getGlobalVariable("inpwindowId", "TabAttachments.windowId");
      final String key = vars.getGlobalVariable("inpKey", "TabAttachments.key");
      final boolean editable = vars.getGlobalVariable("inpEditable", "TabAttachments.editable")
          .equals("Y");
      printPage(response, vars, strTab, strWindow, key, editable);
      // This command is only for 2.50
    } else if (vars.commandIn("FRAME2")) {
      whitePage(response);
      // This command is only for 2.50
    } else if (vars.commandIn("CHECK")) {
      final String tabId = vars.getStringParameter("inpTabId");
      final String inpKey = vars.getStringParameter("inpKey");
      printPageCheck(response, vars, tabId, inpKey);
    } else
      pageError(response);
  }

  private void printPageFS(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Attachments relations frame set");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/TabAttachments_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTab,
      String strWindow, String key, boolean editable) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the attachments relations");
    final String[] discard = { "noData", "" };
    if (!editable)
      discard[1] = "editable";
    final TabAttachmentsData[] data = TabAttachmentsData.selectTabInfo(this, strTab);
    String tableId = "";
    if (data == null || data.length == 0)
      throw new ServletException("Tab not found: " + strTab);
    else {
      tableId = data[0].adTableId;
      if (data[0].isreadonly.equals("Y"))
        discard[0] = new String("selReadOnly");
    }

    final TabAttachmentsData[] files = TabAttachmentsData.select(this,
        Utility.getContext(this, vars, "#User_Client", strWindow),
        Utility.getContext(this, vars, "#AccessibleOrgTree", strWindow), tableId, key);

    if ((files == null) || (files.length == 0))
      discard[0] = "widthData";
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/TabAttachments_F1", discard).createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("window", strWindow);
    xmlDocument.setParameter("key", key);
    xmlDocument.setParameter("recordIdentifier",
        TabAttachmentsData.selectRecordIdentifier(this, key, vars.getLanguage(), strTab));

    {
      final OBError myMessage = vars.getMessage("TabAttachments");
      vars.removeMessage("TabAttachments");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setData("structure1", files);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
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
