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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.OBContext;

/**
 * A base JSON web service This servlet just verifies if the user is authenticated
 * 
 * @author adrianromero
 */
public abstract class WebServiceAuthenticatedServlet extends WebServiceAbstractServlet {

  private static final long serialVersionUID = 1L;

  protected final void authenticated(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (OBContext.getOBContext() == null) {
      throw new OBSecurityException();
    }
  }
}
