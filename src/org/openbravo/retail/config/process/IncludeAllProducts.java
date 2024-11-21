/*
 ************************************************************************************
 * Copyright (C) 2012-2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProlProduct;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class IncludeAllProducts implements org.openbravo.scheduling.Process {
  private ProcessLogger logger;
  private static final Logger log4j = LogManager.getLogger();

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
      OBContext.setAdminMode(true);
      final String recordID = (String) bundle.getParams().get("Obretco_Productlist_ID");
      OBRETCOProductList productList = OBDal.getInstance().get(OBRETCOProductList.class, recordID);

      String query = "as p where p.sale = 'Y' and p.isGeneric = 'N' and "
          + "p.organization.id in (:orgList) and not exists (select 1 from OBRETCO_Prol_Product as retpro "
          + "where retpro.product.id = p.id and retpro.obretcoProductlist.id = :productList)";

      final OBQuery<Product> obq = OBDal.getInstance().createQuery(Product.class, query);
      obq.setFilterOnReadableOrganization(false);
      obq.setNamedParameter("productList", productList.getId());
      obq.setNamedParameter("orgList",
          OBContext.getOBContext()
              .getOrganizationStructureProvider()
              .getNaturalTree(productList.getOrganization().getId()));

      final ScrollableResults productScroll = obq.scroll(ScrollMode.FORWARD_ONLY);
      try {
        int counter = 1;
        while (productScroll.next()) {
          final Product product = (Product) productScroll.get()[0];

          OBRETCOProlProduct newProductLine = OBProvider.getInstance()
              .get(OBRETCOProlProduct.class);
          newProductLine.setOrganization(productList.getOrganization());
          newProductLine.setProduct(product);
          newProductLine.setObretcoProductlist(productList);
          OBDal.getInstance().save(newProductLine);
          if (counter % 1000 == 0) {
            OBDal.getInstance().flush();
          }
          counter++;
        }
        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      } finally {
        productScroll.close();
      }

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      logger.log(message);
      log4j.error(message, e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
