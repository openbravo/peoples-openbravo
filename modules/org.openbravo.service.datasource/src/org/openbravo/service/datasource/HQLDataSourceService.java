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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider;
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
import org.openbravo.service.datasource.hql.HQLInjectionQualifier;
import org.openbravo.service.datasource.hql.HqlInjector;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.json.AdvancedQueryBuilder;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HQLDataSourceService extends ReadOnlyDataSourceService {
  private static final Logger log = LoggerFactory.getLogger(HQLDataSourceService.class);
  private static final String AND = " AND ";
  private static final String WHERE = " WHERE ";
  private static final String ORDERBY = " ORDER BY ";
  private static final String ADDITIONAL_FILTERS = "@additional_filters@";
  private static final String INJECTION_POINT_GENERIC_ID = "@injection_point_#@";
  private static final String INJECTION_POINT_INDEX_PLACEHOLDER = "#";
  private static final String DUMMY_INJECTION_POINT_REPLACEMENT = " 1 = 1 ";
  @Inject
  @Any
  private Instance<HqlInjector> hqlInjectors;
  @Inject
  @Any
  private Instance<HqlQueryTransformer> hqlQueryTransformers;

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
    String tableId = parameters.get("tableId");
    String tabId = parameters.get("tabId");
    Table table = null;
    if (tableId != null) {
      table = OBDal.getInstance().get(Table.class, tableId);
    } else if (tabId != null) {
      Tab tab = null;
      tab = OBDal.getInstance().get(Tab.class, tabId);
      table = tab.getTable();
    }
    boolean justCount = true;
    Query countQuery = getQuery(table, parameters, justCount);
    return ((Number) countQuery.uniqueResult()).intValue();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {

    String tableId = parameters.get("tableId");
    String tabId = parameters.get("tabId");
    Table table = null;
    if (tableId != null) {
      table = OBDal.getInstance().get(Table.class, tableId);
    } else if (tabId != null) {
      Tab tab = null;
      tab = OBDal.getInstance().get(Tab.class, tabId);
      table = tab.getTable();
    }
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tableId);
    OBContext.setAdminMode(true);
    boolean justCount = false;
    Query query = getQuery(table, parameters, justCount);

    if (startRow > 0) {
      query.setFirstResult(startRow);
    }
    if (endRow > startRow) {
      query.setMaxResults(endRow - startRow + 1);
    }

    String distinct = parameters.get(JsonConstants.DISTINCT_PARAMETER);
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
          Property property = entity.getPropertyByColumnName(column.getDBColumnName());
          record.put(property.getName(), properties[i]);
          i++;
        }
      }
      data.add(record);
    }
    OBContext.restorePreviousMode();
    return data;
  }

  /**
   * Returns a hibernate query object based on the hql query, if the justCount parameter is true,
   * the query will just return the number of records that fulfill the criteria. If justCount is
   * false, the query will return all the actual records that fulfill the criteria
   * 
   * @param table
   * @param parameters
   * @param justCount
   * @return
   */
  private Query getQuery(Table table, Map<String, String> parameters, boolean justCount) {
    OBContext.setAdminMode(true);
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
      if (justCount) {
        hqlQuery = "select count(distinct e." + distinct + ") " + formClause;
      } else {
        hqlQuery = "select distinct e." + distinct + " " + formClause;
      }
    }

    // adds the additional filters (client, organization and criteria) to the query
    hqlQuery = addAdditionalFilters(table, hqlQuery, whereClause, parameters);

    // adds the order by clause
    String orderByClause = getSortByClause(parameters);
    if (!orderByClause.isEmpty()) {
      hqlQuery = hqlQuery + orderByClause;
    }

    Map<String, Object> queryNamedParameters = new HashMap<String, Object>();

    // if the is any HQL Query transformer defined for this table, use it to transform the query
    hqlQuery = transFormQuery(hqlQuery, queryNamedParameters, parameters);

    // replaces the injection points with injected code or with dummy comparisons
    // if the injected code includes named parameters for the query, they are stored in the
    // queryNamedParameters parameter
    hqlQuery = fillInInjectionPoints(hqlQuery, queryNamedParameters, parameters);

    if (distinct == null && justCount) {
      final String from = "from ";
      String formClause = hqlQuery.substring(hqlQuery.toLowerCase().indexOf(from));
      hqlQuery = "select count(*) " + formClause;
    }

    Query query = OBDal.getInstance().getSession().createQuery(hqlQuery);

    // sets the parameters of the query
    queryNamedParameters.putAll(queryBuilder.getNamedParameters());
    for (String key : queryNamedParameters.keySet()) {
      if (queryNamedParameters.get(key) instanceof BigDecimal) {
        // TODO: find a better way to avoid the cast exception from BigDecimal to Long
        queryNamedParameters.put(key, ((BigDecimal) queryNamedParameters.get(key)).longValue());
      }
      query.setParameter(key, queryNamedParameters.get(key));
    }

    OBContext.restorePreviousMode();
    return query;
  }

  /**
   * If the hql query has injection points, resolve them using dependency injection. If some
   * injection points are defined in the query but its definition is not injected, replace them with
   * dummy comparisons
   * 
   * @param hqlQuery
   *          hql query that might contain injection points
   * @param queryNamedParameters
   *          array with the named paremeters that will be set to the query. At this point it is
   *          empty, is can be filled in by the injection point implementators
   * @param parameters
   *          parameters of this request
   * @return the updated hql query. Also, hqlParameters can contain the named parameters used in the
   *         injection points
   */
  private String fillInInjectionPoints(String hqlQuery, Map<String, Object> queryNamedParameters,
      Map<String, String> parameters) {
    String updatedHqlQuery = hqlQuery;
    int index = 0;
    while (existsInjectionPoint(hqlQuery, index)) {
      HqlInjector injector = getInjector(index, parameters);
      String injectedCode = null;
      if (injector != null) {
        injectedCode = injector.injectHql(parameters, queryNamedParameters);
      }
      if (injectedCode == null) {
        injectedCode = DUMMY_INJECTION_POINT_REPLACEMENT;
      }
      String injectionPointId = INJECTION_POINT_GENERIC_ID.replace(
          INJECTION_POINT_INDEX_PLACEHOLDER, Integer.toString(index));
      updatedHqlQuery = updatedHqlQuery.replace(injectionPointId, injectedCode);
      index++;
    }
    return updatedHqlQuery;
  }

  /**
   * If there is any HQL Query Transformer defined, uses its transformHqlQuery to transform the
   * query
   * 
   * @param hqlQuery
   *          the original HQL query
   * @param queryNamedParameters
   *          the named parameters that will be used in the query
   * @param parameters
   *          the parameters of the request
   * @return the transformed query
   */
  private String transFormQuery(String hqlQuery, Map<String, Object> queryNamedParameters,
      Map<String, String> parameters) {
    String transformedHqlQuery = hqlQuery;
    HqlQueryTransformer hqlQueryTransformer = getTransformer(parameters);
    if (hqlQueryTransformer != null) {
      transformedHqlQuery = hqlQueryTransformer.transformHqlQuery(transformedHqlQuery, parameters,
          queryNamedParameters);
    }
    return transformedHqlQuery;
  }

  /**
   * Returns, if defined, an HQL Query Transformer for this table. If the are several transformers
   * defined, the one with the lowest priority will be chosen
   * 
   * @param parameters
   *          the parameters of the request
   * @return the HQL Query transformer that will be used to transform the query
   */
  private HqlQueryTransformer getTransformer(Map<String, String> parameters) {
    HqlQueryTransformer transformer = null;
    String tableId = parameters.get("tableId");
    for (HqlQueryTransformer nextTransformer : hqlQueryTransformers
        .select(new ComponentProvider.Selector(tableId))) {
      if (transformer == null) {
        transformer = nextTransformer;
      } else if (nextTransformer.getPriority(parameters) < transformer.getPriority(parameters)) {
        transformer = nextTransformer;
      } else if (nextTransformer.getPriority(parameters) == transformer.getPriority(parameters)) {
        log.warn(
            "Trying to get hql query transformer injector for the table with id {}, there are more than one instance with same priority",
            tableId);
      }
    }
    return transformer;
  }

  /**
   * Returns, if defined, an injector for the injection point with index it
   * 
   * @param index
   *          the index of the injection point
   * @param parameters
   *          the parameters of the request
   * @return the injector with the lowest priority for the injection point @injection_point_<index>@
   */
  private HqlInjector getInjector(int index, Map<String, String> parameters) {
    HqlInjector injector = null;
    String tableId = parameters.get("tableId");
    for (HqlInjector inj : hqlInjectors.select(new HQLInjectionQualifier.Selector(tableId, Integer
        .toString(index)))) {
      if (injector == null) {
        injector = inj;
      } else if (inj.getPriority(parameters) < injector.getPriority(parameters)) {
        injector = inj;
      } else if (inj.getPriority(parameters) == injector.getPriority(parameters)) {
        log.warn(
            "Trying to get hql injector for the injection point {} of the table with id {}, there are more than one instance with same priority",
            INJECTION_POINT_GENERIC_ID.replace(INJECTION_POINT_INDEX_PLACEHOLDER,
                Integer.toString(index)), tableId);
      }
    }
    return injector;
  }

  /**
   * Checks if the injection point with id index exists in the provided hql query
   * 
   * @param hqlQuery
   *          hql query that can contain injection points
   * @param index
   *          index of the injection point
   * @return true if the hql query contains an injection point with the provided index, false
   *         otherwise
   */
  private boolean existsInjectionPoint(String hqlQuery, int index) {
    String injectionPointId = INJECTION_POINT_GENERIC_ID.replace(INJECTION_POINT_INDEX_PLACEHOLDER,
        Integer.toString(index));
    return hqlQuery.contains(injectionPointId);
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

        if (column.isLinkToParentColumn()) {
          propertyNameBefore = property.getName() + "." + JsonConstants.ID;
          propertyNameAfter = column.getEntityAlias() + "." + JsonConstants.ID;
        } else {
          Entity refEntity = property.getReferencedProperty().getEntity();
          String identifierPropertyName = refEntity.getIdentifierProperties().get(0).getName();
          propertyNameBefore = property.getName() + "." + identifierPropertyName;
          propertyNameAfter = column.getEntityAlias() + "." + identifierPropertyName;
        }

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
    OBContext.setAdminMode(true);
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
    OBContext.restorePreviousMode();
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