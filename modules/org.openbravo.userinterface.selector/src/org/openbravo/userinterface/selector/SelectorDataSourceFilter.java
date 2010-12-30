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
package org.openbravo.userinterface.selector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.OBBindings;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.DataSourceFilter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.json.QueryBuilder.TextMatching;

/**
 * Implements the a datasource filter request for the selectors. Used to generates Hibernate where
 * clauses based on dynamic expressions (JavaScript)
 * 
 * @author iperdomo
 */
public class SelectorDataSourceFilter implements DataSourceFilter {

  private static Logger log = Logger.getLogger(SelectorDataSourceFilter.class);
  private String dateFormat = null;
  private DateFormat systemDateFormat = null;
  private TextMatching textMatching = TextMatching.exact;

  public SelectorDataSourceFilter() {
  }

  @Override
  public void doFilter(Map<String, String> parameters, HttpServletRequest request) {
    final long t1 = System.currentTimeMillis();

    try {

      OBContext.setAdminMode();

      String selectorId = parameters.get(SelectorConstants.DS_REQUEST_SELECTOR_ID_PARAMETER);
      String requestType = parameters.get(SelectorConstants.DS_REQUEST_TYPE_PARAMETER);

      if (selectorId == null || selectorId.equals("")) {
        return;
      }

      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);

      OBCriteria<SelectorField> sfc = OBDal.getInstance().createCriteria(SelectorField.class);
      sfc.add(Expression.isNotNull(SelectorField.PROPERTY_DEFAULTEXPRESSION));
      sfc.add(Expression.eq(SelectorField.PROPERTY_OBUISELSELECTOR, sel));

      if ((sel.getFilterExpression() == null || sel.getFilterExpression().equals(""))
          && sfc.count() == 0) { // Nothing to filter
        return;
      }

      final ScriptEngineManager manager = new ScriptEngineManager();
      final ScriptEngine engine = manager.getEngineByName("js");

      // Initializing the OB JavaScript object
      engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters, request.getSession()));

      // Applying filter expression
      applyFilterExpression(sel, engine, parameters);

      // Applying default expression for selector fields when is not a selector window request
      if (!"Window".equals(requestType)) {
        applyDefaultExpressions(sel, engine, parameters, sfc, request);
      }

    } catch (Exception e) {
      log.error("Error executing filter: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
      log.debug("doFilter took: " + (System.currentTimeMillis() - t1) + "ms");
    }
  }

  /**
   * Evaluates the Selector filter expression and modifies the parameters map for data filtering
   */
  private void applyFilterExpression(Selector sel, ScriptEngine engine,
      Map<String, String> parameters) {

    if (sel.getFilterExpression() == null) {
      return;
    }

    Object result = null;
    String dynamicWhere = "";

    try {
      result = engine.eval(sel.getFilterExpression());
      if (result != null && !result.toString().equals("")) {
        dynamicWhere = result.toString();
      }
    } catch (Exception e) {
      log.error("Error evaluating filter expression: " + e.getMessage(), e);
    }

    if (!dynamicWhere.equals("")) {
      log.debug("Adding to where clause (based on filter expression): " + dynamicWhere);

      String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);

      if (currentWhere == null || currentWhere.equals("null") || currentWhere.equals("")) {
        parameters.put(JsonConstants.WHERE_PARAMETER, dynamicWhere);
      } else {
        parameters.put(JsonConstants.WHERE_PARAMETER, currentWhere + " and " + dynamicWhere);
      }
    }
  }

  /**
   * Evaluates the default expressions and modifies the parameters map for data filtering
   */
  private void applyDefaultExpressions(Selector sel, ScriptEngine engine,
      Map<String, String> parameters, OBCriteria<SelectorField> sfc, HttpServletRequest request) {

    if (sfc.count() == 0) {
      return;
    }

    Object result = null;
    StringBuffer sb = new StringBuffer();
    String textMatchingName = null;

    if (parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE)) {
      textMatchingName = parameters.get(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE);
    } else {
      textMatchingName = parameters.get(JsonConstants.TEXTMATCH_PARAMETER);
    }

    if (textMatchingName != null) {
      for (TextMatching txtMatching : TextMatching.values()) {
        if (txtMatching.name().equals(textMatchingName)) {
          textMatching = txtMatching;
          break;
        }
      }
    }

    Entity entity = ModelProvider.getInstance().getEntityByTableId(sel.getTable().getId());

    for (SelectorField sf : sfc.list()) {
      // Skip values from the request
      if (parameters.get(sf.getProperty()) != null) {
        log.debug("Skipping the default value evaluation for property: " + sf.getProperty()
            + " - value from request: " + parameters.get(sf.getProperty()));
        continue;
      }

      final List<Property> properties = JsonUtils.getPropertiesOnPath(entity, sf.getProperty());

      if (properties.isEmpty()) {
        continue;
      }

      final Property property = properties.get(properties.size() - 1);

      try {
        result = engine.eval(sf.getDefaultExpression());

        if (result == null || result.toString().equals("")) {
          continue;
        }

        if (sb.length() > 0) {
          sb.append(" and ");
        }

        // Code duplicated from org.openbravo.service.json.QueryBuilder
        // Used to identify the type of property and modify the _where parameter
        // If the this code change, make sure you check the getWhereClause method of the
        // QueryBuilder. Check issue https://issues.openbravo.com/view.php?id=14239

        if (String.class == property.getPrimitiveObjectType()) {
          if (textMatching == TextMatching.exact) {
            sb.append(sf.getProperty() + " = '" + result.toString() + "'");
          } else if (textMatching == TextMatching.startsWith) {
            sb.append("upper(" + sf.getProperty() + ") like '" + result.toString().toUpperCase()
                + "%'");
          } else {
            sb.append("upper(" + sf.getProperty() + ") like '%"
                + result.toString().toUpperCase().replaceAll(" ", "%") + "%'");
          }
        } else if (Boolean.class == property.getPrimitiveObjectType() || property.isNumericType()) {
          sb.append(sf.getProperty() + " = " + result.toString());
        } else if (Date.class.isAssignableFrom(property.getPrimitiveObjectType())) {

          if (dateFormat == null || systemDateFormat == null) {
            dateFormat = (String) request.getSession(false).getAttribute("#AD_JAVADATEFORMAT");
            systemDateFormat = new SimpleDateFormat(dateFormat);
          }

          try {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(systemDateFormat.parse(result.toString()));
            sb.append("(day(" + sf.getProperty() + ") = " + cal.get(Calendar.DATE) + " and month("
                + sf.getProperty() + ") = " + (cal.get(Calendar.MONTH) + 1) + " and year("
                + sf.getProperty() + ") = " + cal.get(Calendar.YEAR) + ")");
          } catch (Exception e) {
            log.error("Error trying to parse date for property " + sf.getProperty(), e);
          }

        }
      } catch (Exception e) {
        log.error("Error evaluating filter expression: " + sf.getDefaultExpression(), e);
      }
    }

    if (sb.length() == 0) {
      return;
    }

    log.debug("Adding to where clause (based on fields default expression): " + sb.toString());

    String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);

    if (currentWhere == null || currentWhere.equals("null") || currentWhere.equals("")) {
      parameters.put(JsonConstants.WHERE_PARAMETER, sb.toString());
    } else {
      parameters.put(JsonConstants.WHERE_PARAMETER, currentWhere + " and " + sb.toString());
    }
  }
}
