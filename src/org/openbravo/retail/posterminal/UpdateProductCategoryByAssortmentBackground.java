/*
 ************************************************************************************
 * Copyright (C) 2015-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateProductCategoryByAssortmentBackground extends DalBaseProcess {
  private static final Logger log = LoggerFactory
      .getLogger(UpdateProductCategoryByAssortmentBackground.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    ProcessLogger bgLogger = bundle.getLogger();
    VariablesSecureApp vars = bundle.getContext().toVars();
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (isRemote) {
      Client client = OBDal.getInstance().get(Client.class, vars.getClient());
      Organization org = OBDal.getInstance().get(Organization.class, vars.getOrg());

      Set<String> orgtree = OBContext.getOBContext()
          .getOrganizationStructureProvider(client.getId()).getChildTree(org.getId(), true);

      OBContext.setAdminMode(true);
      try {
        OBCriteria<OBRETCOProductList> assortmentList = OBDal.getInstance().createCriteria(
            OBRETCOProductList.class);
        assortmentList.add(Restrictions.eq(OBRETCOProductList.PROPERTY_CLIENT, client));
        if (!org.getId().equals("0")) {
          assortmentList.add(Restrictions.in(OBRETCOProductList.PROPERTY_ORGANIZATION + ".id",
              orgtree));
        }
        for (OBRETCOProductList assortment : assortmentList.list()) {
          assortment.getOBRETCOProductcategoryList().clear();
          OBDal.getInstance().save(assortment);
          String logMsg = "Product category by assortment list cleared: " + assortment.getName();
          bgLogger.log(logMsg + "\n\n");
          log.debug(logMsg);
        }
        OBDal.getInstance().flush();

        for (OBRETCOProductList assortment : assortmentList.list()) {
          List<OBRETCOProductcategory> productCategoryElementList = new ArrayList<OBRETCOProductcategory>();

          final StringBuilder hql = new StringBuilder();
          hql.append("select distinct(mpc.id) ");
          hql.append(" from OBRETCO_ProductList obpl ");
          hql.append(" left join obpl.oBRETCOProlProductList obpp  ");
          hql.append(" left join obpp.product mp  ");
          hql.append(" left join mp.productCategory mpc ");
          hql.append(" where obpl.id= :assortmentid ");
          hql.append(" and mp.active='Y'");

          final Session session = OBDal.getInstance().getSession();
          final Query<String> query = session.createQuery(hql.toString(), String.class);
          query.setParameter("assortmentid", assortment.getId());
          ScrollableResults scroll = query.scroll(ScrollMode.SCROLL_SENSITIVE);
          try {
            int i = 0;
            while (scroll.next()) {
              final String productCategoryId = (String) scroll.get()[0];
              final ProductCategory productCategory = OBDal.getInstance().get(
                  ProductCategory.class, productCategoryId);

              final OBRETCOProductcategory productCategoryElement = OBProvider.getInstance().get(
                  OBRETCOProductcategory.class);
              productCategoryElement.setClient(assortment.getClient());
              productCategoryElement.setOrganization(assortment.getOrganization());
              productCategoryElement.setProductCategory(productCategory);
              productCategoryElement.setObretcoProductlist(assortment);
              OBDal.getInstance().save(productCategoryElement);
              productCategoryElementList.add(productCategoryElement);
              assortment.getOBRETCOProductcategoryList().add(productCategoryElement);

              String logMsg = "Product category: " + productCategory.getName()
                  + " created for assortment: " + assortment.getName();
              bgLogger.log(logMsg + "\n\n");
              log.debug(logMsg);
              if ((i++) % 1000 == 0) {
                session.flush();
                session.clear();
              }
            }
          } finally {
            scroll.close();
          }
        }
      } catch (Exception e) {
        String logMsg = "Error executing product category background ";
        bgLogger.log(logMsg);
        log.error(logMsg, e);
      } finally {
        OBContext.restorePreviousMode();
      }
    } else {
      String logMsg = "Remote product preference is not set";
      bgLogger.log(logMsg);
      log.debug(logMsg);
    }
  }
}
