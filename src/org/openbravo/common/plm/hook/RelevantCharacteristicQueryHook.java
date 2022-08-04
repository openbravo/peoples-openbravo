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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.plm.hook;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.common.plm.RelevantCharacteristicProperty;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.service.json.AdvancedQueryBuilder;
import org.openbravo.service.json.AdvancedQueryBuilderHook;
import org.openbravo.service.json.JoinDefinition;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * This hooks allows to build the joins, filters and order by clauses for those queries that use a
 * property path that points to a relevant characteristic property.
 */
@Dependent
public class RelevantCharacteristicQueryHook implements AdvancedQueryBuilderHook {
  private static final Logger log = LogManager.getLogger();

  private Map<String, String> joinsWithProductCharacteristicValue = new HashMap<>();

  @Override
  public List<JoinDefinition> getJoinDefinitions(AdvancedQueryBuilder queryBuilder,
      List<JoinDefinition> joinDefinitions) {
    for (RelevantCharacteristicProperty property : getRelevantCharacteristicProperties(
        queryBuilder)) {
      // add the join with M_Product, if needed
      String productAlias;
      if (isProductEntity(queryBuilder.getEntity())) {
        productAlias = queryBuilder.getMainAlias();
      } else {
        JoinDefinition productJoin = getJoinDefinition(joinDefinitions, property.getBasePath());
        if (productJoin == null) {
          productJoin = new JoinDefinition(queryBuilder).setOwnerAlias(queryBuilder.getMainAlias())
              .setFetchJoin(false)
              .setPropertyPath(property.getBasePath());
          joinDefinitions.add(productJoin);
        }
        productAlias = productJoin.getJoinAlias();
      }

      // join with M_Product_Ch_Value
      JoinDefinition relevantCharJoin = new JoinDefinition(queryBuilder).setOwnerAlias(productAlias)
          .setFetchJoin(false)
          .setProperty(ModelProvider.getInstance()
              .getEntity(Product.class)
              .getProperty(Product.PROPERTY_PRODUCTCHARACTERISTICVALUELIST))
          .setJoinWithClause("characteristic.id = '" + property.getCharacteristicId() + "'");
      joinDefinitions.add(relevantCharJoin);

      // need this left join with M_Ch_Value to not lose the records with null value for the
      // characteristic when sorting
      JoinDefinition charValueJoin = new JoinDefinition(queryBuilder)
          .setOwnerAlias(relevantCharJoin.getJoinAlias())
          .setFetchJoin(false)
          .setProperty(ModelProvider.getInstance()
              .getEntity(ProductCharacteristicValue.class)
              .getProperty(ProductCharacteristicValue.PROPERTY_CHARACTERISTICVALUE));
      joinDefinitions.add(charValueJoin);
      joinsWithProductCharacteristicValue.put(property.getName(), charValueJoin.getJoinAlias());
    }
    return joinDefinitions;
  }

  @Override
  public String parseSimpleFilterClause(AdvancedQueryBuilder queryBuilder, String fieldName,
      String operator, Object value) {
    if (!"equals".equals(operator)) {
      log.error("Cannot filter using operator {} the field {} with value {}", operator, fieldName,
          value);
      return null;
    }
    String relevantCharacteristic = getRelevantCharacteristic(queryBuilder, fieldName);
    if (relevantCharacteristic == null) {
      return null;
    }
    String filterProperty = joinsWithProductCharacteristicValue.get(relevantCharacteristic)
        + DalUtil.DOT + CharacteristicValue.PROPERTY_ID;
    return filterProperty + AdvancedQueryBuilder.getHqlOperator(operator) + "'" + value + "'";
  }

  @Override
  public String parseOrderByClausePart(AdvancedQueryBuilder queryBuilder, String orderByPart) {
    boolean desc = orderByPart.startsWith("-");
    String path = desc ? orderByPart.substring(1) : orderByPart;
    String identifierPart = JsonConstants.FIELD_SEPARATOR + JsonConstants.IDENTIFIER;
    if (path.endsWith(identifierPart)) {
      path = path.substring(0, path.length() - identifierPart.length());
    }
    String relevantCharacteristic = getRelevantCharacteristic(queryBuilder, path);
    if (relevantCharacteristic == null) {
      return null;
    }
    return joinsWithProductCharacteristicValue.get(relevantCharacteristic) + DalUtil.DOT
        + CharacteristicValue.PROPERTY_SEQUENCENUMBER + (desc ? " desc " : "");
  }

  private List<RelevantCharacteristicProperty> getRelevantCharacteristicProperties(
      AdvancedQueryBuilder queryBuilder) {
    // For grid requests the relevant characteristic properties are passed as additional properties
    // For requests coming from the grid filters, they are not included as additional properties but
    // as we only need to retrieve them in case the grid is filtered by any of them, in that case we
    // get them from the criteria
    Set<String> properties = queryBuilder.getAdditionalProperties().isEmpty()
        ? getPropertiesFromCriteria(queryBuilder.getCriteria())
        : new HashSet<>(queryBuilder.getAdditionalProperties());

    return properties.stream()
        .map(p -> RelevantCharacteristicProperty.from(queryBuilder.getEntity(), p).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Set<String> getPropertiesFromCriteria(JSONObject criteria) {
    try {
      if (criteria.has("criteria")) {
        Set<String> properties = new HashSet<>();
        JSONArray c = criteria.getJSONArray("criteria");
        for (int i = 0; i < c.length(); i++) {
          properties.addAll(getPropertiesFromCriteria(c.getJSONObject(i)));
        }
        return properties;
      } else if (criteria.has("fieldName")) {
        return Set.of(criteria.getString("fieldName"));
      }
    } catch (JSONException ex) {
      log.error("Error extracting fields from criteria {}", criteria, ex);
    }
    return Collections.emptySet();
  }

  private JoinDefinition getJoinDefinition(List<JoinDefinition> list, String path) {
    return list.stream()
        .filter(join -> path.equals(join.getPropertyPath()))
        .findFirst()
        .orElse(null);
  }

  private boolean isProductEntity(Entity entity) {
    return entity != null && Product.ENTITY_NAME.equals(entity.getName());
  }

  private String getRelevantCharacteristic(AdvancedQueryBuilder queryBuilder, String fieldName) {
    if (isProductEntity(queryBuilder.getEntity())) {
      return RelevantCharacteristicProperty.getRelevantCharateristicProperties()
          .stream()
          .filter(fieldName::equals)
          .findFirst()
          .orElse(null);
    }
    List<Property> properties = JsonUtils.getPropertiesOnPath(queryBuilder.getEntity(), fieldName);
    if (properties.isEmpty()) {
      return null;
    }
    Property property = properties.get(properties.size() - 1);
    if (!isProductEntity(property.getTargetEntity())) {
      return null;
    }
    return RelevantCharacteristicProperty.getRelevantCharateristicProperties()
        .stream()
        .filter(p -> fieldName.endsWith(DalUtil.FIELDSEPARATOR + p))
        .findFirst()
        .orElse(null);
  }
}
