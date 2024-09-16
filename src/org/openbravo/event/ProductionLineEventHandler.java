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
 * All portions are Copyright (C) 2013-2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.enterprise.event.Observes;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductBOM;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.service.db.DalConnectionProvider;

class ProductionLineEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductionLine.ENTITY_NAME) };
  private static final String BOM_PRODUCTION = "321";
  private static final BigDecimal ZERO = new BigDecimal("0");

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    VariablesSecureApp vars = null;
    try {
      vars = RequestContext.get().getVariablesSecureApp();
    } catch (Exception e) {
      throw new OBException("Error: " + e.getMessage());
    }
    String currentTabId = vars.getStringParameter("tabId");
    if (BOM_PRODUCTION.equals(currentTabId)) {
      final Entity productionLineEntity = ModelProvider.getInstance()
          .getEntity(ProductionLine.ENTITY_NAME);
      final Property productionPlanProperty = productionLineEntity
          .getProperty(ProductionLine.PROPERTY_PRODUCTIONPLAN);
      final Property productProperty = productionLineEntity
          .getProperty(ProductionLine.PROPERTY_PRODUCT);
      final ProductionPlan productionPlan = (ProductionPlan) event
          .getCurrentState(productionPlanProperty);
      final Product currentProduct = (Product) event.getCurrentState(productProperty);

      // Product validation only for Bundle and Unbundle type
      if (productionPlan.getProduction().getProductionbomType() != null
          && !productionPlan.getProduction()
              .getProductionbomType()
              .getType()
              .equalsIgnoreCase("T")) {
        boolean allowProductChange = productionPlan.getProduction()
            .getProductionbomType()
            .isAllowproductchange();
        // Product change not allowed for the selected type
        if (!allowProductChange) {
          // Product Exists in Production Product
          if (!productionPlan.getProduct().getId().equals(currentProduct.getId())) {
            // Product Exists in Bom Product - Stocked
            if (!productExistsInBomProductList(productionPlan, currentProduct)) {
              List<ProductBOM> nonStockedBomProductList = getNonStockedBomProductList(
                  productionPlan.getProduct());
              if (nonStockedBomProductList.size() > 0) {
                // Product Exists in Bom Product - Non Stocked
                boolean productExists = false;
                for (ProductBOM nonStockProdBom : nonStockedBomProductList) {
                  if (productExistsInNonStockedBomProductHierarchy(nonStockProdBom.getBOMProduct(),
                      currentProduct)) {
                    productExists = true;
                    break;
                  }
                }
                if (!productExists) {
                  String language = OBContext.getOBContext().getLanguage().getLanguage();
                  ConnectionProvider conn = new DalConnectionProvider(false);
                  throw new OBException(
                      Utility.messageBD(conn, "@OnlyBoMProductsAllowed@", language));
                }
              } else {
                String language = OBContext.getOBContext().getLanguage().getLanguage();
                ConnectionProvider conn = new DalConnectionProvider(false);
                throw new OBException(
                    Utility.messageBD(conn, "@OnlyBoMProductsAllowed@", language));
              }
            }
          }
        }
      }
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    VariablesSecureApp vars = null;
    try {
      vars = RequestContext.get().getVariablesSecureApp();
    } catch (Exception e) {
      throw new OBException("Error: " + e.getMessage());
    }
    String currentTabId = vars.getStringParameter("tabId");
    if (BOM_PRODUCTION.equals(currentTabId)) {
      final Entity productionLineEntity = ModelProvider.getInstance()
          .getEntity(ProductionLine.ENTITY_NAME);
      final Property productionPlanProperty = productionLineEntity
          .getProperty(ProductionLine.PROPERTY_PRODUCTIONPLAN);
      final Property productProperty = productionLineEntity
          .getProperty(ProductionLine.PROPERTY_PRODUCT);
      final Property movementQtyProperty = productionLineEntity
          .getProperty(ProductionLine.PROPERTY_MOVEMENTQUANTITY);
      final ProductionPlan productionPlan = (ProductionPlan) event
          .getCurrentState(productionPlanProperty);
      final Product currentProduct = (Product) event.getCurrentState(productProperty);
      final Product previousProduct = (Product) event.getPreviousState(productProperty);
      final BigDecimal currentMovementQty = (BigDecimal) event.getCurrentState(movementQtyProperty);
      final BigDecimal previousMovementQty = (BigDecimal) event
          .getPreviousState(movementQtyProperty);

      if (!previousProduct.getId().equals(currentProduct.getId())) {
        // Product validation only for Bundle and Unbundle type
        if (productionPlan.getProduction().getProductionbomType() != null
            && !productionPlan.getProduction()
                .getProductionbomType()
                .getType()
                .equalsIgnoreCase("T")) {
          boolean allowProductChange = productionPlan.getProduction()
              .getProductionbomType()
              .isAllowproductchange();
          // Product change not allowed for the selected type
          if (!allowProductChange) {
            // Product Exists in Production Product
            if (!productionPlan.getProduct().getId().equals(currentProduct.getId())) {
              // Product Exists in Bom Product - Stocked
              if (!productExistsInBomProductList(productionPlan, currentProduct)) {
                List<ProductBOM> nonStockedBomProductList = getNonStockedBomProductList(
                    productionPlan.getProduct());
                if (nonStockedBomProductList.size() > 0) {
                  // Product Exists in Bom Product - Non Stocked
                  boolean productExists = false;
                  for (ProductBOM nonStockProdBom : nonStockedBomProductList) {
                    if (productExistsInNonStockedBomProductHierarchy(
                        nonStockProdBom.getBOMProduct(), currentProduct)) {
                      productExists = true;
                      break;
                    }
                  }
                  if (!productExists) {
                    String language = OBContext.getOBContext().getLanguage().getLanguage();
                    ConnectionProvider conn = new DalConnectionProvider(false);
                    throw new OBException(
                        Utility.messageBD(conn, "@OnlyBoMProductsAllowed@", language));
                  }
                } else {
                  String language = OBContext.getOBContext().getLanguage().getLanguage();
                  ConnectionProvider conn = new DalConnectionProvider(false);
                  throw new OBException(
                      Utility.messageBD(conn, "@OnlyBoMProductsAllowed@", language));
                }
              }
            }
          }
        }
      }
      // Quantity validation only for without production type
      if (productionPlan.getProduction().getProductionbomType() == null) {
        OBCriteria<ProductionLine> productionLineCriteria = OBDal.getInstance()
            .createCriteria(ProductionLine.class);
        productionLineCriteria
            .add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONPLAN, productionPlan));
        productionLineCriteria.add(Restrictions.gt(ProductionLine.PROPERTY_MOVEMENTQUANTITY, ZERO));
        if (productionLineCriteria.count() > 0 && previousMovementQty != currentMovementQty) {
          if (currentMovementQty.compareTo(ZERO) == 1 && previousMovementQty.compareTo(ZERO) != 1) {
            String language = OBContext.getOBContext().getLanguage().getLanguage();
            ConnectionProvider conn = new DalConnectionProvider(false);
            throw new OBException(
                Utility.messageBD(conn, "@ConsumedProductWithPostiveQty@", language));
          } else if (currentMovementQty.compareTo(ZERO) == -1
              && previousMovementQty.compareTo(ZERO) != -1 && productionLineCriteria.count() == 1) {
            String language = OBContext.getOBContext().getLanguage().getLanguage();
            ConnectionProvider conn = new DalConnectionProvider(false);
            throw new OBException(
                Utility.messageBD(conn, "@ProducedProductWithNegativeQty@", language));
          }
        }
      }
    }
  }

  private boolean productExistsInBomProductList(ProductionPlan productionPlan,
      Product lineProduct) {
    // @formatter:off
    final String hql = "select count(pl)"
    +" from MaterialMgmtProductionPlan pl"
    +" where pl.id=:planId" 
    +" and exists (select 1 from ProductBOM bom"
    +" where bom.product.id=pl.product.id"
    +" and bom.bOMProduct.id=:lineProductId)";
    // @on
    
    final Query<Long> qry = OBDal.getInstance().getSession().createQuery(hql, Long.class);
    qry.setParameter("planId", productionPlan.getId());
    qry.setParameter("lineProductId", lineProduct.getId());
    return qry.list().get(0) > Long.parseLong("0");
  }
  
  
  private List<ProductBOM> getNonStockedBomProductList(Product productionProduct) {
    final String hql = "as bom"
    +" join bom.bOMProduct p"
    +" where bom.product.id=:productionProductId"
    +" and p.stocked='N'"
    +" and p.billOfMaterials='Y'";

    return OBDal.getInstance()
        .createQuery(ProductBOM.class, hql)
        .setNamedParameter("productionProductId", productionProduct.getId())
        .list();
  }
  

  @SuppressWarnings("unchecked")
  private boolean productExistsInNonStockedBomProductHierarchy(Product nonStockedProduct, Product lineProduct) {
   // @formatter:off
   final String sql ="WITH RECURSIVE bom_product_hierarchy AS ("
      + "          SELECT p.m_product_id, p.name, p.isstocked, p.isBom, CAST('1' as NUMERIC) as qty"
      + "          FROM m_product p"
      + "          WHERE p.m_product_id = :nonStockedProductId"
      + "          UNION ALL"
      + "          SELECT p.m_product_id, p.name, p.isstocked, p.isBom, pb.bomqty*CAST(ph.qty as NUMERIC) as qty"
      + "          FROM m_product p"
      + "          JOIN M_PRODUCT_BOM pb ON p.m_product_id = pb.M_PRODUCTBOM_id"
      + "          JOIN bom_product_hierarchy ph ON pb.m_product_id = ph.m_product_id "
      + "          where (ph.isstocked='N' and ph.isBom='Y')"
      + "  )"
      + "  SELECT count(ph) FROM bom_product_hierarchy ph where ph.m_product_id=:lineProductId";
     // @formatter:on
    @SuppressWarnings("rawtypes")
    final NativeQuery qry = OBDal.getInstance()
        .getSession()
        .createNativeQuery(sql)
        .setParameter("nonStockedProductId", nonStockedProduct.getId())
        .setParameter("lineProductId", lineProduct.getId());
    List<BigInteger> ls = qry.list();
    return (Long) ls.get(0).longValue() > Long.parseLong("0");
  }
}
