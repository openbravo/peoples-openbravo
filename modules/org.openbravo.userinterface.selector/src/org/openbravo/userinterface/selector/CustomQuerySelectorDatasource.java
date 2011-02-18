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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.client.application.OBBindings;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class CustomQuerySelectorDatasource extends ReadOnlyDataSourceService {

  private static Logger log = Logger.getLogger(SelectorDataSourceFilter.class);
  private static final String ADDITIONAL_FILTERS = "@additional_filters@";
  private static final String NEW_FILTER_CLAUSE = "\n AND ";
  private static final String NEW_OR_FILTER_CLAUSE = "\n OR ";

  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, -1).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    // creation of formats is done here because they are not thread safe
    final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
    final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
    final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    String selectorId = parameters.get(SelectorConstants.DS_REQUEST_SELECTOR_ID_PARAMETER);

    if (StringUtils.isEmpty(selectorId)) {
      return result;
    }

    OBContext.setAdminMode();
    try {

      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);
      List<SelectorField> fields = sel.getOBUISELSelectorFieldList();

      String HQL = sel.getHQL();
      // Parse the HQL in case that optional filters are required
      HQL = parseOptionalFilters(HQL, parameters, sel, xmlDateFormat);

      String sortBy = parameters.get("_sortBy");
      HQL += getSortClause(sortBy, sel);

      Query selQuery = OBDal.getInstance().getSession().createQuery(HQL);
      String[] queryAliases = selQuery.getReturnAliases();

      if (startRow > 0) {
        selQuery.setFirstResult(startRow);
      }
      if (endRow > startRow) {
        selQuery.setMaxResults(endRow - startRow + 1);
      }

      for (Object objResult : selQuery.list()) {
        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        Object[] resultList = new Object[1];
        if (objResult instanceof Object[]) {
          resultList = (Object[]) objResult;
        } else {
          resultList[0] = objResult;
        }

        for (SelectorField field : fields) {
          // TODO: throw an exception if the display expression doesn't match any returned alias.
          for (int i = 0; i < queryAliases.length; i++) {
            if (queryAliases[i].equals(field.getDisplayColumnAlias())) {
              Object value = resultList[i];
              if (value instanceof Date) {
                value = xmlDateFormat.format(value);
              }
              if (value instanceof Timestamp) {
                value = xmlDateTimeFormat.format(value);
                value = JsonUtils.convertToCorrectXSDFormat((String) value);
              }
              data.put(queryAliases[i], value);
            }
          }
        }
        result.add(data);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  private String parseOptionalFilters(String _HQL, Map<String, String> parameters, Selector sel,
      SimpleDateFormat xmlDateFormat) {
    if (!_HQL.contains(ADDITIONAL_FILTERS)) {
      return _HQL;
    }
    final String requestType = parameters.get(SelectorConstants.DS_REQUEST_TYPE_PARAMETER);
    String HQL = _HQL;
    StringBuffer additionalFilter = new StringBuffer();
    final String entityAlias = sel.getEntityAlias();
    // Client filter
    additionalFilter.append(entityAlias + ".client.id ='").append(
        OBContext.getOBContext().getCurrentClient().getId()).append("'");

    // Organization filter: parameters _org=90A1F59849E84AFABD04814B3D15A691
    final String orgs = getOrgs(parameters.get(JsonConstants.ORG_PARAMETER));
    if (StringUtils.isNotEmpty(orgs)) {
      additionalFilter.append(NEW_FILTER_CLAUSE);
      additionalFilter.append(entityAlias + ".organization in (" + orgs + ")");
    }
    additionalFilter.append(getDefaultFilterExpression(sel, parameters));

    boolean hasFilter = false;
    for (SelectorField field : sel.getOBUISELSelectorFieldList()) {
      String value = parameters.get(field.getDisplayColumnAlias());
      if (field.getDefaultExpression() != null && !"Window".equals(requestType)) {
        try {
          String defaultValue = evaluateExpression(field.getDefaultExpression(), parameters)
              .toString();
          if (StringUtils.isNotEmpty(defaultValue)) {
            additionalFilter.append(NEW_FILTER_CLAUSE);
            additionalFilter.append(getWhereClause(defaultValue, field, xmlDateFormat));
          }
        } catch (Exception e) {
          log.error("Error evaluating filter expression: " + e.getMessage(), e);
        }
      }
      if ((field.isFilterable() || field.getDefaultExpression() != null)
          && field.getClauseLeftPart() != null && StringUtils.isNotEmpty(value)) {
        String whereClause = getWhereClause(value, field, xmlDateFormat);
        if (!hasFilter) {
          additionalFilter.append(NEW_FILTER_CLAUSE);
          additionalFilter.append(" (");
          hasFilter = true;
        } else {
          if ("Window".equals(requestType)) {
            additionalFilter.append(NEW_FILTER_CLAUSE);
          } else {
            additionalFilter.append(NEW_OR_FILTER_CLAUSE);
          }
        }
        additionalFilter.append(whereClause);
      }
    }
    if (hasFilter) {
      additionalFilter.append(")");
    }
    HQL = HQL.replace(ADDITIONAL_FILTERS, additionalFilter.toString());
    return HQL;
  }

  private String getOrgs(String orgId) {
    StringBuffer orgPart = new StringBuffer();
    if (StringUtils.isNotEmpty(orgId)) {
      final Set<String> orgSet = OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(orgId);
      if (orgSet.size() > 0) {
        boolean addComma = false;
        for (String org : orgSet) {
          if (addComma) {
            orgPart.append(",");
          }
          orgPart.append("'" + org + "'");
          addComma = true;
        }
      }
    }
    if (orgPart.length() == 0) {
      String[] orgs = OBContext.getOBContext().getReadableOrganizations();
      boolean addComma = false;
      for (int i = 0; i < orgs.length; i++) {
        if (addComma) {
          orgPart.append(",");
        }
        orgPart.append("'" + orgs[i] + "'");
        addComma = true;
      }
    }
    return orgPart.toString();
  }

  private String getWhereClause(String value, SelectorField field, SimpleDateFormat xmlDateFormat) {
    String whereClause = "";
    DomainType domainType = ModelProvider.getInstance().getReference(field.getReference().getId())
        .getDomainType();
    if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
        || domainType.getClass().equals(LongDomainType.class)) {
      whereClause = field.getClauseLeftPart() + " = " + value;
    } else if (domainType.getClass().equals(DateDomainType.class)) {
      try {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(xmlDateFormat.parse(value));
        whereClause = " (day(" + field.getClauseLeftPart() + ") = " + cal.get(Calendar.DATE);
        whereClause += "\n and month(" + field.getClauseLeftPart() + ") = "
            + (cal.get(Calendar.MONTH) + 1);
        whereClause += "\n and year(" + field.getClauseLeftPart() + ") = " + cal.get(Calendar.YEAR)
            + ") ";
      } catch (Exception e) {
        // ignore these errors, just don't filter then
        // add a dummy whereclause to make the query format correct
        whereClause = "1 = 1";
      }
    } else if (domainType instanceof BooleanDomainType) {
      whereClause = field.getClauseLeftPart() + " = " + value;
    } else {
      whereClause = "C_IGNORE_ACCENT(" + field.getClauseLeftPart() + ")";
      whereClause += " LIKE C_IGNORE_ACCENT(";
      whereClause += "'%" + value.toUpperCase().replaceAll(" ", "%") + "%')";
    }
    return whereClause;
  }

  private String getSortClause(String sortBy, Selector sel) {
    StringBuffer sortByClause = new StringBuffer();
    // If grid is manually filtered sortBy is not empty
    if (StringUtils.isNotEmpty(sortBy)) {
      if (sortBy.contains(JsonConstants.IN_PARAMETER_SEPARATOR)) {
        final String[] fieldNames = sortBy.split(JsonConstants.IN_PARAMETER_SEPARATOR);
        for (String fieldName : fieldNames) {
          int fieldSortIndex = getFieldSortIndex(fieldName, sel);
          if (fieldSortIndex > 0) {
            if (sortByClause.length() > 0) {
              sortByClause.append(", ");
            }
            sortByClause.append(fieldSortIndex);
          }
        }
      } else {
        int fieldSortIndex = getFieldSortIndex(sortBy, sel);
        if (fieldSortIndex > 0) {
          sortByClause.append(fieldSortIndex);
        }
      }
    }

    // If sortByClause is empty set default sort options. Order by first shown column.
    if (sortByClause.length() == 0) {
      String fieldName = "";
      Long sortNumber = Long.MAX_VALUE;
      for (SelectorField selField : sel.getOBUISELSelectorFieldList()) {
        if (selField.isShowingrid() && selField.getSortno() < sortNumber) {
          sortNumber = selField.getSortno();
          fieldName = selField.getDisplayColumnAlias();
        }
      }
      int fieldSortIndex = getFieldSortIndex(fieldName, sel);
      if (fieldSortIndex > 0) {
        sortByClause.append(fieldSortIndex);
      }
    }
    String result = "";
    if (sortByClause.length() > 0) {
      result = " ORDER BY " + sortByClause.toString();
    }

    return result;
  }

  /**
   * Given a Selector object and the request parameters it evaluates the Filter Expression in case
   * that it is defined and returns the result.
   * 
   * @param sel
   *          The Selector that it is being used.
   * @param parameters
   *          parameters used for this request.
   * @return a String with the evaluated JavaScript filter expression in case it is defined.
   */
  private String getDefaultFilterExpression(Selector sel, Map<String, String> parameters) {
    if ((sel.getFilterExpression() == null || sel.getFilterExpression().equals(""))) {
      // Nothing to filter
      return "";
    }

    Object result = evaluateExpression(sel.getFilterExpression(), parameters);
    if (result != null && !result.toString().equals("")) {
      return NEW_FILTER_CLAUSE + "(" + result.toString() + ")";
    }

    return "";
  }

  private Object evaluateExpression(String expression, Map<String, String> parameters) {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByName("js");
    // Initializing the OB JavaScript object
    engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters, RequestContext.get()
        .getSession()));

    // Applying filter expression
    try {
      return engine.eval(expression);
    } catch (Exception e) {
      log.error("Error evaluating filter expression: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Based on the given field name it gets the HQL query column related to it and returns its index.
   * 
   * @param fieldName
   *          Grid's field name or display alias of the related selector field it is desired to
   *          order by.
   * @param sel
   *          The Selector that it is being used.
   * @return The index of the query column related to the field.
   */
  private int getFieldSortIndex(String fieldName, Selector sel) {
    final String[] queryAliases = OBDal.getInstance().getSession().createQuery(
        sel.getHQL().replace(ADDITIONAL_FILTERS, "1=1")).getReturnAliases();
    for (int i = 0; i < queryAliases.length; i++) {
      if (queryAliases[i].equals(fieldName)) {
        return i + 1;
      }
    }
    return 0;
  }
}
