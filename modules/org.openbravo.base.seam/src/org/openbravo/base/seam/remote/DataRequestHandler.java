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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.CheckException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.ResourceNotFoundException;

/**
 * An {@link OBSeamRequestHandler} which can handle requests for data by queries. It expects json
 * data content and generates json strings as a result.
 * 
 * @author mtaal
 */
@Name("dataRequestHandler")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@AutoCreate
public class DataRequestHandler implements RESTRequestHandler {

  @In
  private DataToJsonConverter dataToJsonConverter;

  @In
  private RequestUtils requestUtils;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.seam.remote.OBSeamRequestHandler#doDelete(java.lang.String[],
   * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doDelete(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * Supports retrieval of business objects from Openbravo and returns them as json strings in the
   * content of the response.
   * 
   * @see org.openbravo.base.seam.remote.OBSeamRequestHandler#doGet(java.lang.String[],
   *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doGet(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    if (path.length == 0) {
      throw new ResourceNotFoundException("Can not handle a request without path info "
          + request.getRequestURI());
    }

    final String entityName;
    if (path[0].indexOf("[") != -1) {
      entityName = path[0].substring(0, path[0].indexOf("["));
    } else {
      entityName = path[0];
    }

    try {
      ModelProvider.getInstance().getEntity(entityName);
    } catch (final CheckException ce) {
      throw new ResourceNotFoundException("Resource " + entityName + " not found", ce);
    }

    // now check the second segment and see if an operation is required
    String id = null;
    boolean countOperation = false;
    if (path.length == 2) {
      if (path[1].equals("count")) {
        countOperation = true;
      } else {
        id = path[1];
      }
    }

    // for (Enumeration<?> enumeration = request.getParameterNames();
    // enumeration.hasMoreElements();) {
    // final String name = (String) enumeration.nextElement();
    // System.err.println(name + " --> " + request.getParameter(name));
    // }

    if (id == null) {
      // show all of type entityname

      // check if there is a whereClause
      final String where = request.getParameter("where");
      final String orderBy = request.getParameter("orderBy");

      // System.err.println("Range: " + request.getHeader("Range"));

      final String firstResult;
      final String maxResult;
      if (request.getHeader("Range") != null) {
        final String range = request.getHeader("Range");
        final int from = parseFromRange(range);
        final int to = parseToRange(range);
        if (from != -1 && to != -1) {
          firstResult = "" + from;
          maxResult = "" + (1 + to - from);
        } else {
          // print a warning or so?
          firstResult = null;
          maxResult = null;
        }
      } else {
        firstResult = request.getParameter("firstResult");
        maxResult = request.getParameter("maxResult");
      }

      String whereOrderByClause = "";
      if (where != null) {
        whereOrderByClause += where;
      }

      final OBQuery<BaseOBObject> obqCount = OBDal.getInstance().createQuery(entityName,
          whereOrderByClause);
      final Integer count = obqCount.count();
      if (countOperation) {
        final String jsonResult = dataToJsonConverter.convertToJsonString(count,
            DataResolvingMode.SHORT);
        requestUtils.writeJsonResult(response, jsonResult);
        return;
      }

      if (orderBy != null) {
        whereOrderByClause += " order by " + orderBy;
      }

      final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(entityName,
          whereOrderByClause);

      if (firstResult != null) {
        try {
          obq.setFirstResult(Integer.parseInt(firstResult));
        } catch (NumberFormatException e) {
          throw new InvalidRequestException("Value of firstResult parameter is not an integer: "
              + firstResult);
        }
      }
      if (maxResult != null) {
        try {
          obq.setMaxResult(Integer.parseInt(maxResult));
        } catch (NumberFormatException e) {
          throw new InvalidRequestException("Value of maxResult parameter is not an integer: "
              + firstResult);
        }
      }

      final List<BaseOBObject> bobs = obq.list();
      final String jsonResult = dataToJsonConverter.convertToJsonString(bobs,
          DataResolvingMode.FULL);
      requestUtils.writeJsonResult(response, jsonResult);

      final int start;
      if (firstResult == null) {
        start = 0;
      } else {
        start = Integer.parseInt(firstResult);
      }
      response.addHeader("Content-Range", start + "-" + (start + bobs.size() - 1) + "/" + count);
      return;
    } else {
      final BaseOBObject bob = OBDal.getInstance().get(entityName, id);
      if (bob == null) {
        throw new ResourceNotFoundException("No resource found for entity " + entityName
            + " using id " + id);
      }
      final String jsonResult = dataToJsonConverter
          .convertToJsonString(bob, DataResolvingMode.FULL);
      requestUtils.writeJsonResult(response, jsonResult);
      return;
    }
  }

  protected int getFirstResult(HttpServletRequest request) {
    return 0;
  }

  private int parseFromRange(String range) {
    final int isIndex = range.indexOf("=");
    final int dashIndex = range.indexOf("-");
    if (isIndex == -1 || dashIndex == -1) {
      return -1;
    }
    try {
      return Integer.parseInt(range.substring(isIndex + 1, dashIndex));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private int parseToRange(String range) {
    final int dashIndex = range.indexOf("-");
    if (dashIndex == -1) {
      return -1;
    }
    try {
      return Integer.parseInt(range.substring(dashIndex + 1));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.seam.remote.OBSeamRequestHandler#doPost(java.lang.String[],
   * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doPost(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.seam.remote.OBSeamRequestHandler#doPut(java.lang.String[],
   * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doPut(String[] path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
