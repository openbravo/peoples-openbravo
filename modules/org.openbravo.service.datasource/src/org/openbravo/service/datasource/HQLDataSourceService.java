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
 * All portions are Copyright (C) 2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.json.JsonConstants;

public class HQLDataSourceService extends ReadOnlyDataSourceService {

  private static final String ORDERBY = " order by ";

  @Override
  protected int getCount(Map<String, String> parameters) {
    return 0;
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {

    String tabId = parameters.get("tabId");
    Tab tab = null;
    if (tabId != null) {
      tab = OBDal.getInstance().get(Tab.class, tabId);
    }
    Table table = tab.getTable();

    List<Column> columns = table.getADColumnList();
    String hqlQuery = table.getHqlQuery();
    String orderByClause = getSortByClause(parameters);

    if (!orderByClause.isEmpty()) {
      hqlQuery = hqlQuery + orderByClause;
    }
    Query query = OBDal.getInstance().getSession().createQuery(hqlQuery);
    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    for (Object row : query.list()) {
      Map<String, Object> record = new HashMap<String, Object>();
      Object[] properties = (Object[]) row;
      int i = 0;
      for (Column column : columns) {
        record.put(column.getName(), properties[i]);
        i++;
      }
      data.add(record);
    }
    return data;
  }

  /**
   * Returns a HQL sort by clause based on the parameters sent to the datasource
   * 
   * @param parameters
   *          parameters sent in the request. They can contain useful info like the property being
   *          sorted, its table, etc
   * @return an HQL sort by clause or an empty string if the grid is not being filtered
   */
  private String getSortByClause(Map<String, String> parameters) {
    String orderByClause = "";
    final String sortBy = parameters.get(JsonConstants.SORTBY_PARAMETER);
    if (sortBy != null) {
      orderByClause = sortBy;
    } else if (parameters.get(JsonConstants.ORDERBY_PARAMETER) != null) {
      orderByClause = parameters.get(JsonConstants.ORDERBY_PARAMETER);
    }
    final boolean asc = !orderByClause.startsWith("-");
    String direction = "";
    if (!asc) {
      orderByClause = orderByClause.substring(1);
      direction = " desc ";
    }
    if (!orderByClause.isEmpty()) {
      orderByClause = ORDERBY + orderByClause + direction;
    }
    return orderByClause;
  }
}
