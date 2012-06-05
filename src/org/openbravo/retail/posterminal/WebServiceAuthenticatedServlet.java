/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
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
