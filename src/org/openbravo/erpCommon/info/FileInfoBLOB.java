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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.MimeTypeUtil;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.ADFile;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

public class FileInfoBLOB extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String columnName = vars.getStringParameter("columnName");
    if (columnName == null || columnName.equals(""))
      columnName = vars.getStringParameter("inpColumnName");
    String tableId = vars.getStringParameter("tableId");
    if (tableId == null || tableId.equals("")) {
      tableId = vars.getStringParameter("inpTableId");
    }
    if (tableId == null || tableId.equals("")) {
      String tabId = vars.getStringParameter("inpTabId");
      try {
        OBContext.setAdminMode(true);
        Tab tab = OBDal.getInstance().get(Tab.class, tabId);
        tableId = tab.getTable().getId();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    String imageID = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(columnName));
    if (imageID == null || imageID.equals("")) {
      imageID = vars.getStringParameter("imageId");
    }

    String orgId = vars.getStringParameter("inpOrgId");
    if (orgId == null || orgId.equals("")) {
      orgId = vars.getStringParameter("inpadOrgId");
    }
    if (orgId == null || orgId.equals("")) {
      orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    }

    String parentObjectId = vars.getStringParameter("parentObjectId");
    if (parentObjectId == null || parentObjectId.equals("")) {
      OBContext.setAdminMode(true);
      try {
        Table table = OBDal.getInstance().get(Table.class, vars.getStringParameter("inpTableId"));
        if (table != null) {
          List<Column> cols = table.getADColumnList();
          String keyCol = "";
          for (Column col : cols) {
            if (col.isKeyColumn()) {
              keyCol = col.getDBColumnName();
              break;
            }
          }
          parentObjectId = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(keyCol));
        }
      } finally {
        OBContext.restorePreviousMode();
      }

    }
    if (vars.commandIn("DEFAULT")) {

      printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId, orgId);
    } else if (vars.getCommand().startsWith("SAVE_OB3")) {
      OBContext.setAdminMode(true);
      try {
        final FileItem fi = vars.getMultiFile("inpFile");
        byte[] bytea = fi.get();
        String fileName = fi.getName();
        String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
        String fileSizeAction = vars.getStringParameter("imageSizeAction");
        int size = bytea.length;

        // TODO: Check filesize action

        ADFile file = OBProvider.getInstance().get(ADFile.class);
        file.setOrganization(OBDal.getInstance().get(Organization.class, orgId));
        file.setBindaryData(bytea);
        file.setActive(true);
        file.setName(fileName);
        file.setMimetype(mimeType);
        file.setFilesize((long) size);
        OBDal.getInstance().save(file);
        OBDal.getInstance().flush();

        String fileid = file.getId();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String selectorId = orgId = vars.getStringParameter("inpSelectorId");
        writeRedirectOB3(writer, selectorId, fileid, fileSizeAction, fileName, size, null);
      } catch (Throwable t) {
        log4j.error("Error uploading file", t);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String selectorId = orgId = vars.getStringParameter("inpSelectorId");
        writeRedirectOB3(writer, selectorId, "", "ERROR_UPLOADING", null, 0, t.getMessage());
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if (vars.getCommand().startsWith("DELETE_OB3")) {
      if (imageID != null && !imageID.equals("")) {
        OBContext.setAdminMode(true);
        try {
          Image image = OBDal.getInstance().get(Image.class, imageID);
          OBDal.getInstance().flush();
          OBDal.getInstance().remove(image);
        } finally {
          OBContext.restorePreviousMode();
        }
      } else {
        printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId, orgId);
      }
    } else {
      pageError(response);
    }
  }

  private void writeRedirectOB3(PrintWriter writer, String selectorId, String fileid,
      String fileSizeAction, String fileName, int size, String msg) {
    writer.write("<HTML><BODY><script type=\"text/javascript\">");
    writer.write("top." + selectorId + ".callback('" + fileid + "', '" + fileSizeAction + "', '"
        + fileName + "', '" + size + "'");

    if (StringUtils.isNotEmpty(msg)) {
      writer.write(", '" + StringEscapeUtils.escapeJavaScript(msg) + "'");
    }

    writer.write(");");
    writer.write("</SCRIPT></BODY></HTML>");
  }

  private void printPageFrame(HttpServletResponse response, VariablesSecureApp vars,
      String imageID, String tableId, String columnName, String parentObjectId, String orgId)
      throws IOException, ServletException {
    String[] discard;
    if (imageID.equals("")) {
      discard = new String[1];
      discard[0] = "divDelete";
    } else
      discard = new String[0];

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/ImageInfoBLOB", discard).createXmlDocument();

    xmlDocument.setParameter("parentObjectId", parentObjectId);
    xmlDocument.setParameter("imageId", imageID);
    xmlDocument.setParameter("inpColumnName", columnName);
    xmlDocument.setParameter("inpOrgId", orgId);
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
