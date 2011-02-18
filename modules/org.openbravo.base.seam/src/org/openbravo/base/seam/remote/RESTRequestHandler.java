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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles web service requests received in the {@link OBSeamWebService}.
 * 
 * @author mtaal
 */
public interface RESTRequestHandler {

  /**
   * Execute a delete action.
   * 
   * @param path
   *          the path divided in segments, only the part after the seam request handler name is
   *          passed.
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @throws Exception
   */
  public void doDelete(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Execute a get action.
   * 
   * @param path
   *          the path divided in segments, only the part after the seam request handler name is
   *          passed.
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @throws Exception
   */
  public void doGet(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Execute a post action.
   * 
   * @param path
   *          the path divided in segments, only the part after the seam request handler name is
   *          passed.
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @throws Exception
   */
  public void doPost(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  /**
   * Execute a put action.
   * 
   * @param path
   *          the path divided in segments, only the part after the seam request handler name is
   *          passed.
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @throws Exception
   */
  public void doPut(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception;
}
