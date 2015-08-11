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
package org.openbravo.client.application.window;

import java.text.NumberFormat;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.ADFile;
import org.openbravo.portal.PortalAccessible;

/**
 * This ActionHandler implements the DELETE and GETSIZE actions which are used by the ImageBLOB
 * reference components in the OB3 windows.
 * 
 * The DELETE action deletes an image from the database, and its reference from the referencing
 * table
 * 
 * The GETSIZE action gets the size of an image. If the image doesn't yet have size properties, it
 * will create them and save them in the image object in the database. Also, it will compute the
 * MIME type if it hasn't yet been computed
 * 
 */
public class FileActionHandler extends BaseActionHandler implements PortalAccessible {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {

    Object command = parameters.get("command");
    if ("GETFILEINFO".equals(command)) {
      String fileID = (String) parameters.get("inpfileId");
      JSONObject result = new JSONObject();

      try {
        // Check file null or invalid
        ADFile file = OBDal.getInstance().get(ADFile.class, fileID);
        if (file != null) {
          result.put("name", file.getName());
          result.put("ext", getExtension(file.getName()));
          result.put("mimetype", file.getMimetype());
          result.put("size", file.getFilesize());
          result.put("displaysize", formatFileSize(file.getFilesize().intValue()));

        }
      } catch (JSONException e) {
        throw new OBException(e);
      }

      return result;
    } else if ("DELETE".equals(command)) {
      String fileID = (String) parameters.get("inpfileId");
      JSONObject result = new JSONObject();

      try {
        // Check file null or invalid
        ADFile file = OBDal.getInstance().get(ADFile.class, fileID);
        if (file != null) {
          result.put("deleted", file.getName());
          OBDal.getInstance().remove(file);
          OBDal.getInstance().flush();
        }
      } catch (JSONException e) {
        throw new OBException(e);
      }
      return result;
    } else {
      throw new OBException("Command not found : " + command);
    }
  }

  private String getExtension(String filename) {

    if (filename == null) {
      return "";
    }

    int i = filename.lastIndexOf(".");
    if (i < 0) {
      return "";
    }

    return filename.substring(i + 1);
  }

  private String formatFileSize(int size) {

    NumberFormat f = Utility
        .getFormat(RequestContext.get().getVariablesSecureApp(), "amountInform");

    if (size < 1024) {
      return f.format(size) + " B";
    } else if (size < 1048576) {
      return f.format(size / 1024.0) + " KB";
    } else if (size < 1073741824) {
      return f.format(size / 1048576.0) + " MB";
    } else {
      return f.format(size / 1073741824.0) + " GB";
    }
  }
}
