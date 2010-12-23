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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.querylist;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.client.myob.WidgetInstance;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonUtils;

/**
 * Reads the tabs which the user is allowed to see.
 * 
 * @author gorkaion
 */
public class QueryListDataSource extends ReadOnlyDataSourceService {

  private final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();

  /**
   * Returns the count of objects based on the passed parameters.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @return the total number of objects
   */
  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, -1).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    OBContext.setAdminMode();
    try {
      WidgetInstance widgetInstance = OBDal.getInstance().get(WidgetInstance.class,
          parameters.get("widgetInstanceId"));
      boolean isExport = "true".equals(parameters.get("exportToFile"));
      String viewMode = parameters.get("viewMode");
      WidgetClass widgetClass = widgetInstance.getWidgetClass();

      Query widgetQuery = OBDal.getInstance().getSession().createQuery(
          widgetClass.getOBCQLWidgetQueryList().get(0).getHQL());
      String[] queryAliases = widgetQuery.getReturnAliases();

      if (!isExport && "widget".equals(viewMode)) {
        int rowsNumber = Integer.valueOf((parameters.get("rowsNumber") != null) ? parameters
            .get("rowsNumber") : "10");
        widgetQuery.setMaxResults(rowsNumber);
      } else if (!isExport) {
        if (startRow > 0) {
          widgetQuery.setFirstResult(startRow);
        }
        if (endRow > startRow) {
          widgetQuery.setMaxResults(endRow - startRow + 1);
        }
      }

      String[] params = widgetQuery.getNamedParameters();
      if (params.length > 0) {
        HashMap<String, Object> parameterValues = getParameterValues(parameters, widgetInstance);

        for (int i = 0; i < params.length; i++) {
          String namedParam = params[i];
          boolean isParamSet = false;
          if (parameterValues.containsKey(namedParam)) {
            widgetQuery.setParameter(namedParam, parameterValues.get(namedParam));
            isParamSet = true;
          }
          if (!isParamSet) {
            // TODO: throw an exception
          }
        }
      }

      List<OBCQL_QueryColumn> columns = QueryListUtils.getColumns(widgetClass
          .getOBCQLWidgetQueryList().get(0));

      final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
      for (Object objResult : widgetQuery.list()) {
        final Map<String, Object> data = new HashMap<String, Object>();

        Object[] resultList = new Object[1];
        if (objResult instanceof Object[])
          resultList = (Object[]) objResult;
        else
          resultList[0] = objResult;

        for (OBCQL_QueryColumn column : columns) {
          // TODO: throw an exception if the display expression doesn't match any returned alias.
          for (int i = 0; i < queryAliases.length; i++) {
            if (queryAliases[i].equals(column.getDisplayExpression())
                || (!isExport && queryAliases[i].equals(column.getLinkExpression()))) {
              Object value = resultList[i];
              if (value instanceof Date) {
                value = xmlDateFormat.format(value);
              }
              if (value instanceof Timestamp) {
                value = xmlDateTimeFormat.format(value);
              }
              data.put(queryAliases[i], value);
            }
          }
        }
        result.add(data);
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a HashMap with the values of the parameters included on the given widget instance.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param widgetInstance
   *          the widget instance owner of the parameter values
   * @return a HashMap<String, Object> with the value of each parameter mapped by the DBColumnName
   *         of the parameter.
   */
  private HashMap<String, Object> getParameterValues(Map<String, String> parameters,
      WidgetInstance widgetInstance) {
    HashMap<String, Object> parameterValues = new HashMap<String, Object>();
    for (ParameterValue value : widgetInstance
        .getOBUIAPPParameterValueEMObkmoWidgetInstanceIDList()) {
      parameterValues.put(value.getParameter().getDBColumnName(), ApplicationUtils
          .getParameterValue(value));
    }

    for (Parameter parameter : widgetInstance.getWidgetClass()
        .getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
      if (!parameterValues.containsKey(parameter.getDBColumnName()) && parameter.isFixed()) {
        parameterValues.put(parameter.getDBColumnName(), ApplicationUtils.getParameterFixedValue(
            parameters, parameter));
      }
    }
    return parameterValues;
  }
}
