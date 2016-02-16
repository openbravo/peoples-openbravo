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
import org.openbravo.model.common.enterprise.Organization;

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

    if (vars.getCommand().startsWith("SAVE_OB3")) {
      OBContext.setAdminMode(true);
      try {
        final FileItem fi = vars.getMultiFile("inpFile");
        byte[] bytea = fi.get();
        String fileName = fi.getName();
        String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
        int size = bytea.length;

        String fileAction = null;

        // Check file constraints.
        if (!validateExtension(fileName, vars.getStringParameter("fileExtensions"))) {
          fileAction = "WRONG_EXTENSION";
        } else if (!validateSize(size, vars.getStringParameter("fileMaxSize"),
            vars.getStringParameter("fileMaxSizeUnit"))) {
          fileAction = "WRONG_SIZE";
        } else {
          fileAction = "SUCCESS";
        }

        // Now save the file
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
        writeRedirectOB3(writer, selectorId, fileid, fileAction, fileName);
      } catch (Throwable t) {
        log4j.error("Error uploading file", t);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String selectorId = orgId = vars.getStringParameter("inpSelectorId");
        writeRedirectOB3(writer, selectorId, "", "ERROR_UPLOADING", t.getMessage());
      } finally {
        OBContext.restorePreviousMode();
      }
    } else {
      pageError(response);
    }
  }

  private void writeRedirectOB3(PrintWriter writer, String selectorId, String fileid,
      String fileAction, String fileName) {
    writer.write("<HTML><BODY><script type=\"text/javascript\">");
    writer.write("top." + selectorId + ".callback('" + fileid + "', '" + fileAction + "', '"
        + fileName + "');");

    // if (StringUtils.isNotEmpty(msg)) {
    // writer.write(", '" + StringEscapeUtils.escapeJavaScript(msg) + "'");
    // }
    //
    // writer.write(");");
    writer.write("</SCRIPT></BODY></HTML>");
  }

  private boolean validateExtension(String filename, String extensions) {
    if (extensions == null || extensions.equals("")) {
      return true; // extensions is not defined, then filename extension is valid
    }

    String filenameupper = filename.toUpperCase();
    String[] extensionslist = extensions.split(",");

    for (int j = 0; j < extensionslist.length; j++) {
      if (filenameupper.endsWith(extensionslist[j].trim().toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean validateSize(int size, String maxSize, String maxSizeUnit) {
    if (maxSize == null || maxSize.equals("")) {
      return true; // Max size is not defined, then size is valid
    }

    double readMaxSize = Double.parseDouble(maxSize);
    double calcMaxSize;
    if ("B".equals(maxSizeUnit)) {
      calcMaxSize = readMaxSize;
    } else if ("KB".equals(maxSizeUnit)) {
      calcMaxSize = readMaxSize * 1024.0;
    } else if ("MB".equals(maxSizeUnit)) {
      calcMaxSize = readMaxSize * 1048576.0;
    } else if ("GB".equals(maxSizeUnit)) {
      calcMaxSize = readMaxSize * 1073741824.0;
    } else {
      calcMaxSize = readMaxSize * 1024.0; // KB by default
    }

    return size <= (int) calcMaxSize;
  }
}
