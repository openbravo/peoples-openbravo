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
package org.openbravo.plm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.service.json.AdvancedQueryBuilder;
import org.openbravo.service.json.AdvancedQueryBuilderHook;
import org.openbravo.service.json.JoinDefinition;
import org.openbravo.service.json.JsonUtils;

/**
 * This hooks allows to build the joins, filters and order by clauses for those queries that use a
 * property path that points to a relevant characteristic property.
 */
@Dependent
public class RelevantCharacteristicQueryHook implements AdvancedQueryBuilderHook {

  private Map<String, String> joinsWithProductCharacteristicValue = new HashMap<>();

  @Override
  public List<JoinDefinition> getJoinDefinitions(AdvancedQueryBuilder queryBuilder,
      List<JoinDefinition> joinDefinitions) {
    for (String propertyPath : queryBuilder.getAdditionalProperties()) {
      RelevantCharacteristicProperty property = RelevantCharacteristicProperty
          .from(queryBuilder.getEntity(), propertyPath)
          .orElse(null);

      if (property == null) {
        // not a relevant characteristic property
        continue;
      }

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
    if (isProductEntity(queryBuilder.getEntity())) {
      // TODO
      return null;
    }
    String relevantCharacteristic = getRelevantCharacteristic(queryBuilder, fieldName);
    if (relevantCharacteristic == null) {
      return null;
    }
    return joinsWithProductCharacteristicValue.get(relevantCharacteristic) + ".id = '" + value
        + "'";
  }

  @Override
  public String parseOrderByClausePart(AdvancedQueryBuilder queryBuilder, String orderByPart) {
    if (orderByPart.contains("product$productCategory$_identifier")) {
      // TODO: remove
      orderByPart = "-product$oBPFColor";
    }
    if (isProductEntity(queryBuilder.getEntity())) {
      // TODO
      return null;
    }
    boolean desc = orderByPart.startsWith("-");
    String path = desc ? orderByPart.substring(1) : orderByPart;
    String relevantCharacteristic = getRelevantCharacteristic(queryBuilder, path);
    if (relevantCharacteristic == null) {
      return null;
    }
    return joinsWithProductCharacteristicValue.get(relevantCharacteristic) + ".code"
        + (desc ? " desc " : "");
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
