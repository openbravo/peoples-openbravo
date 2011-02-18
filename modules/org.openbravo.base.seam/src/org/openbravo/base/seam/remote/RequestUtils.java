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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.remote;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Provides convenience utility methods.
 * 
 * @author mtaal
 */
@Name("requestUtils")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Install(precedence = Install.FRAMEWORK)
public class RequestUtils {

  /**
   * Writes the json string to the response outputstream/writer. Sets the content type to
   * text/x-json.
   * 
   * @param response
   *          the response object to write the output to
   * @param json
   *          a json formatted string
   */
  public void writeJsonResult(HttpServletResponse response, String json) {
    try {
      response.setContentType("text/x-json");
      response.setCharacterEncoding("utf-8");
      response.setHeader("Content-Encoding", "UTF-8");
      final Writer w = response.getWriter();
      w.write(json);
      w.close();
    } catch (IOException e) {
      throw new IllegalStateException("Error while writing the following to a response object \n"
          + json, e);
    }
  }
}
