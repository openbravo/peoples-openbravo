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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.reference.EnumUIDefinition;
import org.openbravo.client.kernel.reference.ForeignKeyUIDefinition;
import org.openbravo.client.kernel.reference.IDUIDefinition;
import org.openbravo.client.kernel.reference.NumberUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.kernel.reference.YesNoUIDefinition;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.json.AdvancedQueryBuilder;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class HQLDataSourceService extends ReadOnlyDataSourceService {

  private static final String AND = " AND ";
  private static final String WHERE = " WHERE ";
  private static final String ORDERBY = " ORDER BY ";
  private static final String ADDITIONAL_FILTERS = "@additional_filters@";

  @Override
  // Returns the datasource properties, based on the columns of the table that is going to use the
  // datasource
  // This is needed to support client side filtering
  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    List<DataSourceProperty> dataSourceProperties = new ArrayList<DataSourceProperty>();
    String tableId = (String) parameters.get("tableId");
    if (tableId != null) {
      Table table = OBDal.getInstance().get(Table.class, tableId);
      for (Column column : table.getADColumnList()) {
        final DataSourceProperty dsProperty = new DataSourceProperty();
        dsProperty.setName(column.getName());
        dsProperty.setMandatory(column.isMandatory());
        dsProperty.setUpdatable(column.isUpdatable());
        Reference reference = column.getReference();
        final UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
            reference);
        if (uiDefinition instanceof IDUIDefinition) {
          dsProperty.setId(true);
        } else {
          dsProperty.setId(false);
        }
        dsProperty.setBoolean(uiDefinition instanceof YesNoUIDefinition);
        dsProperty.setPrimitive(!(uiDefinition instanceof ForeignKeyUIDefinition));
        dsProperty.setUIDefinition(uiDefinition);
        if (dsProperty.isPrimitive()) {
          dsProperty.setPrimitiveObjectType(((PrimitiveDomainType) uiDefinition.getDomainType())
              .getPrimitiveType());
          dsProperty.setNumericType(uiDefinition instanceof NumberUIDefinition);
          if (uiDefinition instanceof EnumUIDefinition) {
            Set<String> allowedValues = DataSourceProperty.getAllowedValues(column
                .getReferenceSearchKey());
            dsProperty.setAllowedValues(allowedValues);
            dsProperty.setValueMap(DataSourceProperty.createValueMap(allowedValues, column
                .getReferenceSearchKey().getId()));
          }
        }
        dataSourceProperties.add(dsProperty);
      }
    }
    return dataSourceProperties;
  }

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

    String hqlQuery = table.getHqlQuery();

    // obtains the where clause from the criteria, using the AdvancedQueryBuilder
    JSONObject criteria = JsonUtils.buildCriteria(parameters);
    AdvancedQueryBuilder queryBuilder = new AdvancedQueryBuilder();
    queryBuilder.setEntity(ModelProvider.getInstance().getEntityByTableId(table.getId()));
    queryBuilder.setCriteria(criteria);
    String whereClause = queryBuilder.getWhereClause();

    // replace the property names with the column alias
    whereClause = replaceParametersWithAlias(table, whereClause);

    String distinct = parameters.get(JsonConstants.DISTINCT_PARAMETER);
    if (distinct != null) {
      final String from = "from ";
      String formClause = hqlQuery.substring(hqlQuery.toLowerCase().indexOf(from));
      // TODO: Improve distinct query like this: https://issues.openbravo.com/view.php?id=25182
      hqlQuery = "select distinct e." + distinct + " " + formClause;
    }

    // adds the additional filters (client, organization and criteria) to the query
    hqlQuery = addAdditionalFilters(table, hqlQuery, whereClause, parameters);

    // adds the order by clause
    String orderByClause = getSortByClause(parameters);
    if (!orderByClause.isEmpty()) {
      hqlQuery = hqlQuery + orderByClause;
    }

    Query query = OBDal.getInstance().getSession().createQuery(hqlQuery);

    // sets the parameters of the query
    Map<String, Object> hqlParameters = queryBuilder.getNamedParameters();
    for (String key : hqlParameters.keySet()) {
      if (hqlParameters.get(key) instanceof BigDecimal) {
        // TODO: find a better way to avoid the cast exception from BigDecimal to Long
        hqlParameters.put(key, ((BigDecimal) hqlParameters.get(key)).longValue());
      }
      query.setParameter(key, hqlParameters.get(key));
    }

    if (startRow > 0) {
      query.setFirstResult(startRow);
    }
    if (endRow > startRow) {
      query.setMaxResults(endRow - startRow + 1);
    }

    List<Column> columns = table.getADColumnList();
    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    for (Object row : query.list()) {
      Map<String, Object> record = new HashMap<String, Object>();
      int i = 0;
      if (distinct != null) {
        BaseOBObject bob = (BaseOBObject) row;
        record.put(JsonConstants.ID, bob.getId());
        record.put(JsonConstants.IDENTIFIER, bob.getIdentifier());
      } else {
        Object[] properties = (Object[]) row;
        for (Column column : columns) {
          record.put(column.getName(), properties[i]);
          i++;
        }
      }
      data.add(record);
    }
    return data;
  }

  /**
   * This method replace the column names with their alias
   * 
   * @param table
   *          the table being filtered
   * @param whereClause
   *          the filter criteria
   * @return an updated filter criteria that uses the alias of the columns instead of their names
   */
  private String replaceParametersWithAlias(Table table, String whereClause) {
    if (whereClause.trim().isEmpty()) {
      return whereClause;
    }
    String updatedWhereClause = whereClause.toString();
    Entity entity = ModelProvider.getInstance().getEntityByTableId(table.getId());
    for (Column column : table.getADColumnList()) {
      // look for the property name, replace it with the column alias
      Property property = entity.getPropertyByColumnName(column.getDBColumnName());
      Map<String, String> replacementMap = new HashMap<String, String>();
      String propertyNameBefore = null;
      String propertyNameAfter = null;
      if (property.isPrimitive()) {
        // if the property is a primitive, just replace the property name with the column alias
        propertyNameBefore = property.getName();
        propertyNameAfter = column.getEntityAlias();
      } else {
        // if the property is a FK, then the name of the identifier property of the referenced
        // entity has to be appended
        Entity refEntity = property.getReferencedProperty().getEntity();
        String identifierPropertyName = refEntity.getIdentifierProperties().get(0).getName();
        propertyNameBefore = property.getName() + "." + identifierPropertyName;
        propertyNameAfter = column.getEntityAlias() + "." + identifierPropertyName;
      }
      replacementMap.put(" " + propertyNameBefore + " ", " " + propertyNameAfter + " ");
      replacementMap.put("(" + propertyNameBefore + ")", "(" + propertyNameAfter + ")");
      for (String toBeReplaced : replacementMap.keySet()) {
        if (updatedWhereClause.contains(toBeReplaced)) {
          updatedWhereClause = updatedWhereClause.replace(toBeReplaced,
              replacementMap.get(toBeReplaced));
        }
      }
    }
    return updatedWhereClause;
  }

  /**
   * Adds the additional filters to the hql query. The additional filters include the client filter,
   * the organization filter and the filter created from the grid criteria
   * 
   * @param table
   *          table being fetched
   * @param hqlQuery
   *          hql query without the additional filters
   * @param filterWhereClause
   *          filter created from the grid criteria
   * @param parameters
   *          parameters used for this request
   * @return
   */
  private String addAdditionalFilters(Table table, String hqlQuery, String filterWhereClause,
      Map<String, String> parameters) {
    StringBuffer additionalFilter = new StringBuffer();
    final String entityAlias = table.getEntityAlias();

    // replace the carriage returns and the tabulations with blanks
    String hqlQueryWithFilters = hqlQuery.replace("\n", " ").replace("\r", " ");

    // client filter
    additionalFilter.append(entityAlias + ".client.id in ('0', '")
        .append(OBContext.getOBContext().getCurrentClient().getId()).append("')");

    // organization filter
    final String orgs = DataSourceUtils.getOrgs(parameters.get(JsonConstants.ORG_PARAMETER));
    if (StringUtils.isNotEmpty(orgs)) {
      additionalFilter.append(AND);
      additionalFilter.append(entityAlias + ".organization in (" + orgs + ")");
    }

    if (!filterWhereClause.trim().isEmpty()) {
      // if the filter where clause contains the string 'where', get rid of it
      String whereClause = filterWhereClause.replaceAll("(?i)" + WHERE, " ");
      additionalFilter.append(AND + whereClause);
    }

    // the _where parameter contains the filter clause and the where clause defined at tab level
    String whereClauseParameter = parameters.get(JsonConstants.WHERE_PARAMETER);
    if (whereClauseParameter != null && !whereClauseParameter.trim().isEmpty()
        && !"null".equals(whereClauseParameter)) {
      additionalFilter.append(AND + whereClauseParameter);
    }

    if (hqlQueryWithFilters.contains(ADDITIONAL_FILTERS)) {
      // replace @additional_filters@ with the actual hql filters
      hqlQueryWithFilters = hqlQueryWithFilters.replace(ADDITIONAL_FILTERS,
          additionalFilter.toString());
    } else {
      // adds the hql filters in the proper place at the end of the query
      String separator = null;
      // TODO: only the WHERE of the outer query should
      if (StringUtils.containsIgnoreCase(hqlQueryWithFilters, WHERE)) {
        // if there is already a where clause, append with 'AND'
        separator = AND;
      } else {
        // otherwise, append with 'where'
        separator = WHERE;
      }
      hqlQueryWithFilters = hqlQueryWithFilters + separator + additionalFilter.toString();
    }
    return hqlQueryWithFilters;
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
      orderByClause = ORDERBY + "e." + orderByClause + direction + ", e.id";
    }
    return orderByClause;
  }
}