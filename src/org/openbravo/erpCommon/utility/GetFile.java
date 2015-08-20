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

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.ADFile;

/**
 * 
 * This utility class implements a servlet that download files stored in the database
 * 
 */
public class GetFile extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  protected static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
  protected static final String RESPONSE_NO_CACHE = "no-cache";

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  /**
   * Receiving an id parameter it looks in database for the file with that id and downloads it
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    String fileID = request.getParameter("id");
    if (fileID == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    OBContext.setAdminMode(true);
    try {
      // Check file null or invalid
      ADFile file = OBDal.getInstance().get(ADFile.class, fileID);
      if (file == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      // enforce cache validation/checks every time
      response.addHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
      response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      response.setContentType(file.getMimetype());
      OutputStream out = response.getOutputStream();
      out.write(file.getBindaryData());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
