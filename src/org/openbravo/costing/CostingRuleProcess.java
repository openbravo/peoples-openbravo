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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class CostingRuleProcess implements Process {
  private ProcessLogger logger;
  private static Logger log4j = Logger.getLogger(CostingBackground.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    try {
      final String recordID = (String) bundle.getParams().get("M_Costing_Rule_ID");
      final CostingRule rule = OBDal.getInstance().get(CostingRule.class, recordID);

      // Step 1. Check and create physical inventories.
      List<String> productIds = new ArrayList<String>();
      List<Product> productList = new ArrayList<Product>();
      if (rule.getProduct() != null) {
        productList.add(rule.getProduct());
      } else if (rule.getProductCategory() != null) {
        productList = rule.getProductCategory().getProductList();
      } else {
        productList = getProductList(rule, productIds);
      }

      while (!productList.isEmpty()) {
        for (Product product : productList) {
          CostingRule oldRule = getPreviousRule(product);
          if (oldRule != null) {
            // Check all productTrx are calculated.
            checkPreviousTrx(product);
            createPhysicalInventories(product);
          } else {
            if (product.getCostType() != null) {
              setOldEngineTrxCost(product);
            }
          }
          productIds.add(product.getId());
        }
        if (rule.getProduct() != null || rule.getProductCategory() != null) {
          break;
        }
        productList = getProductList(rule, productIds);
      }

      // Step 2. Process closing physical inventories.

      // Step 3. Set valid from date

      // Step 4. Process starting physical inventories.

      rule.setValidated(true);
      OBDal.getInstance().save(rule);
    } catch (final OBException e) {
      logger.log(e.getMessage());
      log4j.error(e.getMessage(), e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(e.getMessage());
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();

    } catch (final Exception e) {
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      logger.log(message);
      log4j.error(message, e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }
  }

  private void createPhysicalInventories(Product product) {
    // TODO Auto-generated method stub

  }

  private void checkPreviousTrx(Product product) {
    OBCriteria<MaterialTransaction> trxCrit = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    trxCrit.add(Restrictions.eq(MaterialTransaction.PROPERTY_PRODUCT, product));
    trxCrit.add(Restrictions.isNull(MaterialTransaction.PROPERTY_TRANSACTIONCOST));

  }

  private void setOldEngineTrxCost(Product product) {
    // Set cost in all product transactions.

  }

  private CostingRule getPreviousRule(Product product) {
    // Search product specific rule
    OBCriteria<CostingRule> ruleCrit = OBDal.getInstance().createCriteria(CostingRule.class);
    ruleCrit.add(Restrictions.eq(CostingRule.PROPERTY_PRODUCT, product));
    ruleCrit.add(Restrictions.eq(CostingRule.PROPERTY_VALIDATED, true));
    ruleCrit.addOrderBy(CostingRule.PROPERTY_STARTINGDATE, false);
    CostingRule rule = (CostingRule) ruleCrit.uniqueResult();
    if (rule != null) {
      return rule;
    }
    // Search product category rule
    ruleCrit = OBDal.getInstance().createCriteria(CostingRule.class);
    ruleCrit
        .add(Restrictions.eq(CostingRule.PROPERTY_PRODUCTCATEGORY, product.getProductCategory()));
    ruleCrit.add(Restrictions.eq(CostingRule.PROPERTY_VALIDATED, true));
    ruleCrit.addOrderBy(CostingRule.PROPERTY_STARTINGDATE, false);
    rule = (CostingRule) ruleCrit.uniqueResult();
    if (rule != null) {
      return rule;
    }
    // Search generic rule.
    ruleCrit = OBDal.getInstance().createCriteria(CostingRule.class);
    ruleCrit.add(Restrictions.eq(CostingRule.PROPERTY_VALIDATED, true));
    ruleCrit.addOrderBy(CostingRule.PROPERTY_STARTINGDATE, false);
    return (CostingRule) ruleCrit.uniqueResult();

  }

  private void processProduct(CostingRule rule, Product product) {
    // TODO Auto-generated method stub

  }

  private List<Product> getProductList(CostingRule rule, List<String> productIds) {
    OBCriteria<Product> productCrit = OBDal.getInstance().createCriteria(Product.class);
    productCrit.setFilterOnReadableOrganization(false);
    productCrit.add(Restrictions.not(Restrictions.in("id", productIds)));
    // FIXME: filter by flag to set product's cost is calculated
    // productCrit.add(Restrictions.eq("new_flag", true));
    productCrit.setMaxResults(1000);

    return productCrit.list();
  }
}
