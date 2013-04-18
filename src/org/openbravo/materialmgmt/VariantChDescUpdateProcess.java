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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.QueryTimeoutException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class VariantChDescUpdateProcess extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(VariantChDescUpdateProcess.class);
  private String strProductId;
  private String strChValueId;
  private String strChValueName;

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
      // retrieve standard params
      strProductId = (String) bundle.getParams().get("mProductId");
      strChValueId = (String) bundle.getParams().get("mChValueId");
      strChValueName = (String) bundle.getParams().get("chValueName");

      update();

      bundle.setResult(msg);

      // Postgres wraps the exception into a GenericJDBCException
    } catch (GenericJDBCException ge) {
      log4j.error("Exception processing variant generation", ge);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((GenericJDBCException) ge).getSQLException().getMessage());
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
      // Oracle wraps the exception into a QueryTimeoutException
    } catch (QueryTimeoutException qte) {
      log4j.error("Exception processing variant generation", qte);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((QueryTimeoutException) qte).getSQLException().getMessage().split("\n")[0]);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } catch (final Exception e) {
      log4j.error("Exception processing variant generation", e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }

  }

  public void init(String _strProductId, String _strChValueId, String _strChValueName) {
    this.strProductId = _strProductId;
    this.strChValueId = _strChValueId;
    this.strChValueName = _strChValueName;
  }
  /**
   * Method to update the Characteristics Description.
   * 
   * @param strProductId
   *          Optional parameter, when given updates only the description of this product.
   * @param strChValueId
   *          Optional parameter, when given updates only products with this characteristic value
   *          assigned.
   * @param strChValueName
   *          Optional parameter, when a characteristic value id is given it is possible to pass
   *          here it's new name, used
   */
  public void update() {
    OBContext.setAdminMode(false);
    try {
      StringBuffer where = new StringBuffer();
      where.append(" as p");
      where.append(" where p." + Product.PROPERTY_PRODUCTCHARACTERISTICLIST + " is not empty");
      if (StringUtils.isNotBlank(strProductId)) {
        where.append(" and p.id = :productId");
      }
      if (StringUtils.isNotBlank(strChValueId)) {
        where.append(" and exists (select 1 from p."
            + Product.PROPERTY_PRODUCTCHARACTERISTICVALUELIST + " as chv");
        where.append("    where chv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTICVALUE
            + ".id = :chvid)");
      }
      OBQuery<Product> productQuery = OBDal.getInstance().createQuery(Product.class,
          where.toString());
      if (StringUtils.isNotBlank(strProductId)) {
        productQuery.setNamedParameter("productId", strProductId);
      }
      if (StringUtils.isNotBlank(strChValueId)) {
        productQuery.setNamedParameter("chvid", strChValueId);
      }
      productQuery.setFetchSize(1000);
      productQuery.setFilterOnReadableOrganization(false);
      productQuery.setFilterOnActive(false);

      ScrollableResults products = productQuery.scroll(ScrollMode.FORWARD_ONLY);
      int i = 0;
      while (products.next()) {
        Product product = (Product) products.get(0);
        String strChDesc = "";
        where = new StringBuffer();
        where.append(" as pch");
        where.append(" where pch." + ProductCharacteristic.PROPERTY_PRODUCT + " = :product");
        where.append(" order by pch." + ProductCharacteristic.PROPERTY_SEQUENCENUMBER);
        OBQuery<ProductCharacteristic> pchQuery = OBDal.getInstance().createQuery(
            ProductCharacteristic.class, where.toString());
        pchQuery.setFilterOnActive(false);
        pchQuery.setFilterOnReadableOrganization(false);
        pchQuery.setNamedParameter("product", product);
        for (ProductCharacteristic pch : pchQuery.list()) {
          // Reload pch to avoid errors after session clear.
          OBDal.getInstance().refresh(pch);
          if (StringUtils.isNotBlank(strChDesc)) {
            strChDesc += ", ";
          }
          strChDesc += pch.getCharacteristic().getName() + ":";
          where = new StringBuffer();
          where.append(" as pchv");
          where.append(" where pchv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTIC
              + ".id = :ch");
          where.append("   and pchv." + ProductCharacteristicValue.PROPERTY_PRODUCT
              + ".id = :product");
          OBQuery<ProductCharacteristicValue> pchvQuery = OBDal.getInstance().createQuery(
              ProductCharacteristicValue.class, where.toString());
          pchvQuery.setFilterOnActive(false);
          pchvQuery.setFilterOnReadableOrganization(false);
          pchvQuery.setNamedParameter("ch", pch.getCharacteristic().getId());
          pchvQuery.setNamedParameter("product", product.getId());
          for (ProductCharacteristicValue pchv : pchvQuery.list()) {
            // Reload pchv to avoid errors after session clear.
            OBDal.getInstance().refresh(pchv);
            String strChName = pchv.getCharacteristicValue().getName();
            if (StringUtils.isNotBlank(strChValueId) && StringUtils.isNotBlank(strChValueName)
                && pchv.getCharacteristicValue().getId().equals(strChValueId)) {
              strChName = strChValueName;
            }
            strChDesc += " " + strChName;
          }
        }
        product.setCharacteristicDescription(strChDesc);
        OBDal.getInstance().save(product);

        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
        i++;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}