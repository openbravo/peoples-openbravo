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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Image;

/**
 * 
 * This utility class implements a servlet that shows an image stored in database
 * 
 */
public class ShowImageLogo extends HttpBaseServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Receiving an logo parameter and organization id, it looks in database for the corresponding
   * logo and displays it
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);
    String logo = vars.getStringParameter("logo");
    String org = vars.getStringParameter("orgId");

    // read the image data
    byte[] img;
    String mimeType = null;
    Image image = Utility.getImageLogoObject(logo, org);
    if (image != null) {
      img = image.getBindaryData();
      mimeType = image.getMimetype();
    } else {
      img = Utility.getImageLogo(logo, org);
    }
    // write the mimetype
    if (mimeType == null) {
      mimeType = MimeTypeUtil.getInstance().getMimeTypeName(img);
      if (image != null) {
        // If there is an OBContext, we attempt to save the MIME type of the image
        updateMimeType(image.getId(), mimeType);
      }
    }
    if (!mimeType.equals("")) {
      response.setContentType(mimeType);
    }

    // write the image
    OutputStream out = response.getOutputStream();
    response.setContentLength(img.length);
    out.write(img);
    out.close();
  }

  /**
   * This method updates the MIME type of an image, using SQL. DAL cannot be used because there is
   * no OBContext in the Login page
   */
  private void updateMimeType(String id, String mimeType) {
    PreparedStatement ps = null;
    try {
      ps = OBDal.getInstance().getConnection(true)
          .prepareStatement("UPDATE ad_image SET mimetype=? WHERE ad_image_id=?");
      ps.setString(1, mimeType);
      ps.setString(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      log4j.error("Couldn't update mime information of image", e);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (SQLException e) {
        // ignore
      }
    }
  }
}
