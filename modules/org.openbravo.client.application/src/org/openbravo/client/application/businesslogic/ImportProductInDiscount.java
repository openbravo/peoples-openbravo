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
 * All portions are Copyright (C) 2019-2023 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):
 ************************************************************************
 */

package org.openbravo.client.application.businesslogic;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.model.pricing.priceadjustment.Product;

public class ImportProductInDiscount extends ProcessUploadedFile {
  private static final long serialVersionUID = 1L;

  @Override
  protected void clearBeforeImport(String ownerId, JSONObject paramValues) {
    @SuppressWarnings("unchecked")
    NativeQuery<String> qry = OBDal.getInstance()
        .getSession()
        .createNativeQuery(
            "update m_offer_product set updated=now(), updatedby=:userId, isactive='N' where m_offer_id = :m_offer_id");
    qry.setParameter("userId", OBContext.getOBContext().getUser().getId());
    qry.setParameter("m_offer_id", ownerId);
    qry.executeUpdate();
  }

  @Override
  protected UploadResult doProcessFile(JSONObject paramValues, File file) throws Exception {
    final UploadResult uploadResult = new UploadResult();
    final String discountId = paramValues.getString("inpOwnerId");
    final PriceAdjustment discount = OBDal.getInstance().get(PriceAdjustment.class, discountId);
    final String errorMsgProductNotFound = OBMessageUtils.getI18NMessage("OBUIAPP_ProductNotFound",
        new String[0]);

    try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getAbsolutePath()))) {
      String line;
      while ((line = br.readLine()) != null) {
        final String productKey = line.trim();

        // ignore spaces
        if (productKey.length() == 0) {
          continue;
        }

        uploadResult.incTotalCount();

        final List<String> productIds = getProductIds(discount.getClient().getId(),
            discount.getOrganization().getId(), productKey);
        if (productIds.isEmpty()) {
          uploadResult.incErrorCount();
          uploadResult.addErrorMessage(productKey + " --> " + errorMsgProductNotFound + "\n");
        } else {
          for (String productId : productIds) {
            // check if the line already exists
            final OBQuery<Product> productDiscountQry = OBDal.getInstance()
                .createQuery(Product.class,
                    "m_offer_id=:m_offer_id and m_product_id=:m_product_id");
            productDiscountQry.setNamedParameter("m_offer_id", discountId);
            productDiscountQry.setNamedParameter("m_product_id", productId);
            productDiscountQry.setFilterOnReadableOrganization(false);
            productDiscountQry.setFilterOnActive(false);
            productDiscountQry.setMaxResult(1);
            Product productDiscount = productDiscountQry.uniqueResult();
            if (productDiscount == null) {
              // create a new one
              productDiscount = OBProvider.getInstance().get(Product.class);
              productDiscount.setClient(discount.getClient());
              productDiscount.setOrganization(discount.getOrganization());
              productDiscount.setPriceAdjustment(discount);
              productDiscount.setProduct(
                  OBDal.getInstance().get(org.openbravo.model.common.plm.Product.class, productId));
            }
            productDiscount.setActive(true);
            OBDal.getInstance().save(productDiscount);
          }
        }
      }
    }
    OBDal.getInstance().flush();
    return uploadResult;
  }

  @SuppressWarnings("unchecked")
  protected List<String> getProductIds(String clientId, String orgId, String productKey) {
    String sql = "SELECT p.m_product_id from m_product p "
        + "where p.ad_client_id=:clientId and p.value=:value and "
        + "((ad_isorgincluded(:orgId, p.ad_org_id, :clientId) <> -1) or "
        + "(ad_isorgincluded( p.ad_org_id,:orgId, :clientId) <> -1))";
    Session session = OBDal.getInstance().getSession();
    @SuppressWarnings("rawtypes")
    NativeQuery qry = session.createNativeQuery(sql);
    qry.setParameter("clientId", clientId);
    qry.setParameter("orgId", orgId);
    qry.setParameter("value", productKey);
    return (List<String>) qry.list();
  }
}
