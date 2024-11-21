/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.businesslogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.retail.config.OBRETCOProlProduct;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.utils.FormatUtilities;

public class CloneProductListActionHandler extends BaseActionHandler {
  private static final Logger log4j = LogManager.getLogger();

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    final DataToJsonConverter jsonConverter = new DataToJsonConverter();
    JSONObject json = new JSONObject();
    try {
      OBContext.setAdminMode(true);
      String productListId = (String) parameters.get("ProductListId");
      OBRETCOProductList objProdList = OBDal.getInstance()
          .get(OBRETCOProductList.class, productListId);

      OBRETCOProductList objCloneProdList = (OBRETCOProductList) DalUtil.copy(objProdList, false);
      String truncName = FormatUtilities
          .truncate(OBMessageUtils.messageBD("OBRETCO_Copyof") + objCloneProdList.getName(), 57);

      // Check if the name exist
      OBCriteria<OBRETCOProductList> obCriteria = OBDal.getInstance()
          .createCriteria(OBRETCOProductList.class);
      obCriteria.add(Restrictions.eq(OBRETCOProductList.PROPERTY_NAME, truncName));
      List<OBRETCOProductList> bpgs = obCriteria.list();
      if (bpgs.size() != 0) {
        // The name of the product list exist
        try {
          json.put("status", 1);
          json.put("error",
              String.format(OBMessageUtils.messageBD("OBRETCO_DuplicatedName"), truncName));
        } catch (JSONException jsone) {
          throw new OBException(jsone);
        }
        return json;

      } else {
        objCloneProdList.setName(truncName);
      }
      // save the cloned order object
      OBDal.getInstance().save(objCloneProdList);

      // Clone product category list
      final OBCriteria<OBRETCOProductcategory> obc = OBDal.getInstance()
          .createCriteria(OBRETCOProductcategory.class);
      obc.add(Restrictions.eq(OBRETCOProductcategory.PROPERTY_OBRETCOPRODUCTLIST, objProdList));
      obc.setFilterOnReadableOrganization(false);
      obc.setFilterOnReadableClients(false);
      List<OBRETCOProductcategory> oBRETCOProductcategoryList = obc.list();
      for (OBRETCOProductcategory oBRETCOProductcategory : oBRETCOProductcategoryList) {
        OBRETCOProductcategory newOBRETCOProductcategory = (OBRETCOProductcategory) DalUtil
            .copy(oBRETCOProductcategory, false);
        newOBRETCOProductcategory.setObretcoProductlist(objCloneProdList);
        OBDal.getInstance().save(newOBRETCOProductcategory);
      }
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().refresh(objCloneProdList);

      // Clone product list
      final Map<String, Object> parameters2 = new HashMap<>(1);
      final String whereClause = " as pllines where pllines.obretcoProductlist.id = :productList";

      parameters2.put("productList", objProdList.getId());
      final OBQuery<OBRETCOProlProduct> obq = OBDal.getInstance()
          .createQuery(OBRETCOProlProduct.class, whereClause, parameters2);
      obq.setFilterOnReadableOrganization(false);

      final ScrollableResults productScroll = obq.scroll(ScrollMode.FORWARD_ONLY);
      int i = 0;
      try {
        while (productScroll.next()) {
          i++;
          final OBRETCOProlProduct productListLine = (OBRETCOProlProduct) productScroll.get()[0];

          OBRETCOProlProduct newProductListLine = (OBRETCOProlProduct) DalUtil.copy(productListLine,
              false);
          newProductListLine.setObretcoProductlist(objCloneProdList);
          OBDal.getInstance().save(newProductListLine);

          if ((i % 100) == 0) {
            OBDal.getInstance().flush();
            OBDal.getInstance().getSession().clear();
            OBDal.getInstance().save(objCloneProdList);
          }
        }
      } finally {
        productScroll.close();
      }
      json = jsonConverter.toJsonObject(objCloneProdList, DataResolvingMode.FULL);
      OBDal.getInstance().commitAndClose();
      return json;
    } catch (Exception e) {
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      log4j.error(message, e);
      OBDal.getInstance().rollbackAndClose();
      try {
        json.put("status", 1);
        json.put("error", message);
      } catch (JSONException jsone) {
        throw new OBException(jsone);
      }
      return json;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
