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
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.materialmgmt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductAUM;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * Utility class for methods related to Unit of Measure functionality
 * 
 * @author Nono Carballo
 *
 */
public class UOMUtil {

  private static final Logger log4j = Logger.getLogger(UOMUtil.class);
  private static final String UOM_PROPERTY = "UomManagement";
  private static final String UOM_NOT_APPLICABLE = "NA";

  public static final String UOM_PRIMARY = "P";
  public static final String FIELD_PROVIDER_ID = "id";
  public static final String FIELD_PROVIDER_NAME = "name";

  /**
   * Get default AUM for a product in a given document
   * 
   * @param mProductId
   *          The product Id
   * @param documentTypeId
   *          The document type id if the parent document
   * @return The default AUM for the product for the given document
   */

  public static String getDefaultAUMForDocument(String mProductId, String documentTypeId) {
    OBContext.setAdminMode();
    if (mProductId == null || documentTypeId == null) {
      return null;
    }
    DocumentType docType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    return getDefaultAUMForFlow(mProductId, docType.isSalesTransaction());
  }

  /**
   * Get default AUM for a product in a Sales Flow. Used when document does not have set document
   * type.
   * 
   * @param mProductId
   *          The product Id
   * @return The default AUM for the product for the Sales Flow
   */
  public static String getDefaultAUMForSales(String mProductId) {
    return getDefaultAUMForFlow(mProductId, true);
  }

  /**
   * Get default AUM for a product in a Purchase Flow. Used when document does not have set document
   * type.
   * 
   * @param mProductId
   *          The product Id
   * @return The default AUM for the product for the Purchase Flow
   */
  public static String getDefaultAUMForPurchase(String mProductId) {
    return getDefaultAUMForFlow(mProductId, false);
  }

  private static String getDefaultAUMForFlow(String mProductId, boolean isSoTrx) {
    if (mProductId == null) {
      return null;
    }
    OBContext.setAdminMode();
    OBCriteria<ProductAUM> pAUMCriteria = OBDal.getInstance().createCriteria(ProductAUM.class);
    pAUMCriteria.add(Restrictions.and(Restrictions.eq("product.id", mProductId), Restrictions.eq(
        isSoTrx ? ProductAUM.PROPERTY_SALES : ProductAUM.PROPERTY_PURCHASE, UOM_PRIMARY)));
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    String finalAUM = product.getUOM().getId();
    ProductAUM primaryAum = (ProductAUM) pAUMCriteria.uniqueResult();
    if (primaryAum != null) {
      finalAUM = primaryAum.getUOM().getId();
    }
    OBContext.restorePreviousMode();
    return finalAUM;
  }

  /**
   * Get all the available UOM for a product for a given document
   * 
   * @param mProductId
   *          The product id
   * @param documentTypeId
   *          The document type id if the parent document
   * @return List of the available UOM
   */
  public static List<UOM> getAvailableUOMsForDocument(String mProductId, String docTypeId) {
    OBContext.setAdminMode();
    List<UOM> lUom = new ArrayList<UOM>();
    if (mProductId == null || docTypeId == null) {
      return lUom;
    }
    DocumentType docType = OBDal.getInstance().get(DocumentType.class, docTypeId);
    OBCriteria<ProductAUM> pAUMCriteria = OBDal.getInstance().createCriteria(ProductAUM.class);
    pAUMCriteria.add(Restrictions.and(Restrictions.eq("product.id", mProductId), Restrictions.ne(
        docType.isSalesTransaction() ? ProductAUM.PROPERTY_SALES : ProductAUM.PROPERTY_PURCHASE,
        UOM_NOT_APPLICABLE)));
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    List<ProductAUM> pAUMList = pAUMCriteria.list();
    for (ProductAUM pAUM : pAUMList) {
      lUom.add(pAUM.getUOM());
    }
    lUom.add(product.getUOM());
    OBContext.restorePreviousMode();
    return lUom;
  }

  /**
   * Performs conversion for quantity
   * 
   * @param mProductId
   *          The product
   * @param qty
   *          The quantity
   * @param toUOM
   *          The UOM
   * @param reverse
   *          true if reverse, false otherwise
   * @return
   * @throws OBException
   */

  private static BigDecimal getConvertedQty(String mProductId, BigDecimal qty, String toUOMId,
      boolean reverse) throws OBException {

    OBContext.setAdminMode();
    BigDecimal strQty = qty;
    Product product = OBDal.getInstance().get(Product.class, mProductId);

    if (product == null || toUOMId == null || toUOMId == ""
        || toUOMId.equals(product.getUOM().getId())) {
      return strQty;
    }

    if (qty != null) {

      OBCriteria<ProductAUM> productAUMConversionCriteria = OBDal.getInstance().createCriteria(
          ProductAUM.class);
      productAUMConversionCriteria.add(Restrictions.and(Restrictions.eq("product.id", mProductId),
          Restrictions.eq("uOM.id", toUOMId)));

      try {
        ProductAUM conversion = (ProductAUM) productAUMConversionCriteria.uniqueResult();
        if (conversion == null) {
          OBContext.restorePreviousMode();
          throw new OBException(OBMessageUtils.messageBD(new DalConnectionProvider(),
              "NoAUMDefined", OBContext.getOBContext().getLanguage().getLanguage()));
        }
        BigDecimal rate = conversion.getConversionRate();
        UOM uom = OBDal.getInstance().get(UOM.class, conversion.getUOM().getId());
        if (reverse) {
          strQty = qty.divide(rate, uom.getStandardPrecision().intValue(), RoundingMode.HALF_UP);
        } else {
          strQty = rate.multiply(qty).setScale(uom.getStandardPrecision().intValue(),
              RoundingMode.HALF_UP);
        }
      } catch (NonUniqueResultException e) {
        OBContext.restorePreviousMode();
        throw new OBException(OBMessageUtils.messageBD(new DalConnectionProvider(), "DuplicateAUM",
            OBContext.getOBContext().getLanguage().getLanguage()));
      }
    }
    OBContext.restorePreviousMode();
    return strQty;
  }

  /**
   * Computes base quantity based on alternative quantity
   * 
   * @param mProductId
   *          The product
   * @param qty
   *          The alternative quantity
   * @param toUOM
   *          The UOM
   * @return
   * @throws OBException
   */

  public static BigDecimal getConvertedQty(String mProductId, BigDecimal qty, String toUOM)
      throws OBException {

    BigDecimal strQty = getConvertedQty(mProductId, qty, toUOM, false);

    return strQty;
  }

  /**
   * Computes alternative quantity based on base quantity
   * 
   * @param mProductId
   *          The product
   * @param qty
   *          The base quantity
   * @param toUOM
   *          The UOM
   * @return
   * @throws OBException
   */
  public static BigDecimal getConvertedAumQty(String mProductId, BigDecimal qty, String toUOM)
      throws OBException {

    BigDecimal strQty = getConvertedQty(mProductId, qty, toUOM, true);

    return strQty;
  }

  /**
   * Returns if the UomManagement preference is enabled
   * 
   * @return true if enabled, false otherwise
   */
  public static boolean isUomManagementEnabled() {
    OBContext.setAdminMode();
    String propertyValue = "N";
    try {
      Client systemClient = OBDal.getInstance().get(Client.class, "0");
      propertyValue = Preferences.getPreferenceValue(UOM_PROPERTY, true, systemClient, null, null,
          null, null);
    } catch (PropertyException e) {
      log4j.debug("Preference UomManagement not found", e);
    }
    OBContext.restorePreviousMode();
    return propertyValue.equals("Y");
  }

  /**
   * Returns a FieldProvider array containing the default AUM for a product in a given flow
   * 
   * @param productId
   *          The product Id
   * @param docTypeId
   *          The document type Id
   * @return
   */
  public static FieldProvider[] selectDefaultAUM(String productId, String docTypeId) {
    FieldProvider[] finalResult = new FieldProvider[1];
    List<Map<String, String>> result = new ArrayList<>();
    Map<String, String> resultMap = new HashMap<>();
    if (productId == null || productId.isEmpty() || docTypeId == null || docTypeId.isEmpty()) {
      return FieldProviderFactory.getFieldProviderArray(result);
    }
    OBContext.setAdminMode();
    String id = getDefaultAUMForDocument(productId, docTypeId);
    UOM uom = OBDal.getInstance().get(UOM.class, id);
    resultMap.put(FIELD_PROVIDER_ID, id);
    resultMap.put(FIELD_PROVIDER_NAME, uom.getName());
    result.add(resultMap);
    finalResult = FieldProviderFactory.getFieldProviderArray(result);
    OBContext.restorePreviousMode();
    return finalResult;
  }

  /**
   * Returns a FieldProvider array containing the availables UOM for a product in a given flow
   * 
   * @param productId
   *          The product Id
   * @param docTypeId
   *          The document type Id
   * @return
   */
  public static FieldProvider[] selectAUM(String productId, String docTypeId) {
    FieldProvider[] finalResult = new FieldProvider[1];
    Map<String, String> resultMap = new HashMap<>();
    List<Map<String, String>> result = new ArrayList<>();
    if (productId == null || productId.isEmpty() || docTypeId == null || docTypeId.isEmpty()) {
      return FieldProviderFactory.getFieldProviderArray(result);
    }
    OBContext.setAdminMode();
    List<UOM> availableUOM = getAvailableUOMsForDocument(productId, docTypeId);
    for (UOM uom : availableUOM) {
      resultMap = new HashMap<>();
      resultMap.put(FIELD_PROVIDER_ID, uom.getId());
      resultMap.put(FIELD_PROVIDER_NAME, uom.getName());
      result.add(resultMap);
    }
    finalResult = FieldProviderFactory.getFieldProviderArray(result);
    OBContext.restorePreviousMode();
    return finalResult;
  }

  public static FieldProvider[] selectUOM(String productId) {
    FieldProvider[] finalResult = new FieldProvider[1];
    List<Map<String, String>> result = new ArrayList<>();
    Map<String, String> resultMap = new HashMap<>();
    if (productId == null || productId.isEmpty()) {
      return FieldProviderFactory.getFieldProviderArray(result);
    }
    OBContext.setAdminMode();
    OBCriteria<ProductUOM> pUomCriteria = OBDal.getInstance().createCriteria(ProductUOM.class);
    pUomCriteria.add(Restrictions.eq("product.id", productId));
    List<ProductUOM> pUomList = pUomCriteria.list();
    for (ProductUOM pUom : pUomList) {
      resultMap = new HashMap<>();
      resultMap.put(FIELD_PROVIDER_ID, pUom.getUOM().getId());
      resultMap.put(FIELD_PROVIDER_NAME, pUom.getUOM().getName());
      result.add(resultMap);
    }
    finalResult = FieldProviderFactory.getFieldProviderArray(result);
    OBContext.restorePreviousMode();
    return finalResult;
  }

}
