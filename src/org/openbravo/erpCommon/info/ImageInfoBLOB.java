/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.DataPackage;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.xmlEngine.XmlDocument;

public class ImageInfoBLOB extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String parentObjectId = vars.getStringParameter("parentObjectId");
    if (parentObjectId == null || parentObjectId.equals("")) {
      String keyCol = vars.getStringParameter("inpkeyColumnId");
      parentObjectId = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(keyCol));
    }

    String columnName = vars.getStringParameter("columnName");
    if (columnName == null || columnName.equals(""))
      columnName = vars.getStringParameter("inpColumnName");
    String tableId = vars.getStringParameter("tableId");
    if (tableId == null || tableId.equals(""))
      tableId = vars.getStringParameter("inpTableId");

    String imageID = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(columnName));
    if (imageID == null || imageID.equals("")) {
      imageID = vars.getStringParameter("imageId");
    }

    if (vars.commandIn("DEFAULT")) {

      printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId);
    } else if (vars.commandIn("SAVE")) {
      boolean adminMode = OBContext.getOBContext().isInAdministratorMode();
      OBContext.getOBContext().setInAdministratorMode(true);
      final FileItem fi = vars.getMultiFile("inpFile");
      ArrayList bytes = new ArrayList();
      InputStream in = fi.getInputStream();
      int nextChar;
      while ((nextChar = in.read()) != -1)
        bytes.add(new Byte((byte) nextChar));
      byte[] bytea = new byte[bytes.size()];
      for (int i = 0; i < bytes.size(); i++)
        bytea[i] = ((Byte) bytes.get(i)).byteValue();

      // Using DAL to write the image data to the database
      Image image;
      if (imageID == null || imageID.equals("")) {
        image = OBProvider.getInstance().get(Image.class);
        image.setBindaryData(bytea);
        image.setName("Image");
        OBDal.getInstance().save(image);
        OBDal.getInstance().flush();

      } else {
        image = OBDal.getInstance().get(Image.class, imageID);
        image.setBindaryData(bytea);
        OBDal.getInstance().flush();
      }
      PrintWriter writer = response.getWriter();
      writeRedirect(writer, image.getId(), columnName);

      OBContext.getOBContext().setInAdministratorMode(adminMode);
    } else if (vars.commandIn("DELETE")) {
      if (imageID != null && !imageID.equals("")) {
        boolean adminMode = OBContext.getOBContext().setInAdministratorMode(true);
        try {
          Image image = OBDal.getInstance().get(Image.class, imageID);
          Table table = OBDal.getInstance().get(Table.class, tableId);
          String propertyName = ModelProvider.getInstance().getEntityByTableName(
              table.getDBTableName()).getPropertyByColumnName(columnName).getName();
          DataPackage dpackage = table.getDataPackage();
          try {
            Class tableClass = Class.forName(dpackage.getJavaPackage() + "."
                + table.getJavaClassName());
            Object parentObject = OBDal.getInstance().get(tableClass, parentObjectId);
            Class[] argTypes = new Class[2];
            argTypes[0] = String.class;
            argTypes[1] = Object.class;
            Method setMethod = tableClass.getMethod("set", argTypes);
            Object[] argValues = new Object[2];
            argValues[0] = propertyName;
            argValues[1] = null;
            setMethod.invoke(parentObject, argValues);
            OBDal.getInstance().flush();
            OBDal.getInstance().remove(image);
          } catch (Exception e) {
            e.printStackTrace();
            log4j.error("Class for table not found", e);
          }
        } finally {
          OBContext.getOBContext().setInAdministratorMode(adminMode);
        }
        PrintWriter writer = response.getWriter();
        writeRedirect(writer, "", columnName);
      } else {
        printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId);
      }
    } else {
      pageError(response);
    }
  }

  private void printPageFrame(HttpServletResponse response, VariablesSecureApp vars,
      String imageID, String tableId, String columnName, String parentObjectId) throws IOException,
      ServletException {
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
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void writeRedirect(PrintWriter writer, String imageId, String columnname) {
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    writer
        .println("<script language=\"JavaScript\" src=\"../../../../../web/js/searchs.js\" type=\"text/javascript\"></script>");
    writer.println("<script language=\"JavaScript\" type=\"text/javascript\">");
    writer.println("function onLoadDo(){");
    writer.println("var parentWindow = parent.opener;");
    writer.println("parentWindow.document.getElementById('" + columnname + "').value = \""
        + imageId + "\";");
    writer.println("parentWindow.document.getElementById('" + columnname
        + "_R').src = \"../utility/ShowImage?id=" + imageId + "\";");
    if (imageId.equals("")) {
      writer.println("parentWindow.document.getElementById('" + columnname
          + "_R').className = \"Image_NotAvailable_medium\"");
    } else {
      writer.println("parentWindow.document.getElementById('" + columnname
          + "_R').className = \"dummyClass_\" + parent.opener.document.getElementById('"
          + columnname + "_R').className;");
    }
    writer.println("window.close()");

    writer.println("try { parentWindow.changeToEditingMode('force'); } catch (e) {}");
    writer.println("}");
    writer.println("</script>");
    writer.println("</head>");
    writer.println("<body  onload=\"onLoadDo();\">");
    writer.println("</body>");
    writer.println("</html>");
  }

}