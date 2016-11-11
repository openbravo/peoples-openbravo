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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductAUM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * @author Nono Carballo
 *
 */
public class CentralBroker {

  private static CentralBroker _instance = null;
  private final Logger log4j = Logger.getLogger(CentralBroker.class);

  private String[] salesDocuments = { "SOO", // Sales Order
      "MMS", // Material Delivery
      "ARI" // AR Invoice
  };
  private String[] purchaseDocuments = { "POO", // Purchase Order
      "MMR", // Material Receipt
      "API", // AP Invoice
      "POR" // Purchase Requisition
  };

  private CentralBroker() {
    super();
  }

  public static CentralBroker getInstance() {
    if (_instance == null) {
      _instance = new CentralBroker();
    }
    return _instance;
  }

  /**
   * Get default AUM for a product in a given document
   * 
   * @param mProductId
   *          The product Id
   * @param documentTypeId
   *          The document type id if the parent document
   * @return The default AUM for the product for the given document
   */

  public String getDefaultAUMForDocument(String mProductId, String documentTypeId) {
    DocumentType docType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    String docBaseType = docType.getDocumentCategory();
    OBCriteria<ProductAUM> pAUMCriteria = OBDal.getInstance().createCriteria(ProductAUM.class);
    pAUMCriteria.add(Restrictions.eq("product.id", mProductId));
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    String finalAUM = product.getUOM().getId();
    for (ProductAUM pAUM : pAUMCriteria.list()) {
      if (isSalesDocument(docBaseType)) {
        if (pAUM.getSales().equals("P")) {
          finalAUM = pAUM.getUOM().getId();
          break;
        }
      } else if (isPurchaseDocument(docBaseType)) {
        if (pAUM.getPurchase().equals("P")) {
          finalAUM = pAUM.getUOM().getId();
          break;
        }
      }
    }
    return finalAUM;
  }

  private boolean isSalesDocument(String docBaseType) {
    for (String docBase : salesDocuments) {
      if (docBase.equals(docBaseType)) {
        return true;
      }
    }
    return false;
  }

  private boolean isPurchaseDocument(String docBaseType) {
    for (String docBase : purchaseDocuments) {
      if (docBase.equals(docBaseType)) {
        return true;
      }
    }
    return false;
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
  public List<UOM> getAvailableUOMsForDocument(String mProductId, String documentTypeId) {
    List<UOM> lUom = new ArrayList<UOM>();
    DocumentType docType = OBDal.getInstance().get(DocumentType.class, documentTypeId);
    String docBaseType = docType.getDocumentCategory();
    OBCriteria<ProductAUM> pAUMCriteria = OBDal.getInstance().createCriteria(ProductAUM.class);
    pAUMCriteria.add(Restrictions.eq("product.id", mProductId));
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    lUom.add(product.getUOM());
    for (ProductAUM pAUM : pAUMCriteria.list()) {
      if (isSalesDocument(docBaseType)) {
        if (!pAUM.getSales().equals("NA")) {
          lUom.add(pAUM.getUOM());
        }
      } else if (isPurchaseDocument(docBaseType)) {
        if (!pAUM.getPurchase().equals("NA")) {
          lUom.add(pAUM.getUOM());
        }
      }
    }
    return lUom;
  }

  /**
   * Performs reverse conversion for quantity
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

  public BigDecimal getConvertedQty(String mProductId, BigDecimal qty, String toUOM, boolean reverse)
      throws OBException {

    BigDecimal strQty = qty;
    Product product = OBDal.getInstance().get(Product.class, mProductId);

    if (toUOM.equals(product.getUOM().getId())) {
      return strQty;
    }

    if (qty != null) {

      OBCriteria<ProductAUM> productAUMConversionCriteria = OBDal.getInstance().createCriteria(
          ProductAUM.class);
      productAUMConversionCriteria.add(Restrictions.and(Restrictions.eq("product.id", mProductId),
          Restrictions.eq("uOM.id", toUOM)));

      List<ProductAUM> uOmConversionList = productAUMConversionCriteria.list();
      if (uOmConversionList.size() > 0) {
        if (uOmConversionList.size() == 1) {
          ProductAUM conversion = uOmConversionList.get(0);
          BigDecimal rate = conversion.getConversionRate();
          UOM uom = OBDal.getInstance().get(UOM.class, conversion.getUOM().getId());
          if (reverse) {
            strQty = qty.divide(rate, uom.getStandardPrecision().intValue(), RoundingMode.HALF_UP);
          } else {
            strQty = rate.multiply(qty).setScale(uom.getStandardPrecision().intValue(),
                RoundingMode.HALF_UP);
          }
        } else {
          throw new OBException(Utility.messageBD(new DalConnectionProvider(), "DuplicateAUM",
              OBContext.getOBContext().getLanguage().getLanguage()));
        }
      } else {
        throw new OBException(Utility.messageBD(new DalConnectionProvider(), "NoAUMDefined",
            OBContext.getOBContext().getLanguage().getLanguage()));
      }
    }

    return strQty;
  }

  /**
   * Performs direct conversion for quantity
   * 
   * @param mProductId
   *          The product
   * @param qty
   *          The quantity
   * @param toUOM
   *          The UOM
   * @return
   * @throws OBException
   */

  public BigDecimal getConvertedQty(String mProductId, BigDecimal qty, String toUOM)
      throws OBException {

    BigDecimal strQty = getConvertedQty(mProductId, qty, toUOM, false);

    return strQty;
  }

  /**
   * Returns if the UomManagement preference is enabled
   * 
   * @return 'Y'/ 'N'
   */
  public String isUomManagementEnabled() {
    String propertyValue = "N";
    try {
      propertyValue = Preferences.getPreferenceValue("UomManagement", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null);

    } catch (PropertyException e) {
      log4j.debug("Preference UomManagement not found", e);
    }
    return propertyValue;
  }

}
