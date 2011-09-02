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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.hibernate.criterion.Expression;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.manufacturing.processplan.OperationProduct;

public class SL_SequenceProduct_Product_Attribute extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final IsIDFilter idFilter = new IsIDFilter();
  private static final String specialAttListId = "FF808181322476640132249E3417002F";
  private static final String LotSearchKey = "LOT";
  private static final String SerialNoSearchKey = "SNO";
  private static final String ExpirationDateearchKey = "EXD";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // String strLastFieldChanged = info.getLastFieldChanged();
    String strmProductId = info.getStringParameter("inpmProductId", idFilter);
    String strmProductSequenceId = info.getStringParameter("inpmaSequenceproductId", idFilter);

    // Fill Normal Attributes
    Product product = OBDal.getInstance().get(Product.class, strmProductId);
    OperationProduct opProduct = OBDal.getInstance().get(OperationProduct.class,
        strmProductSequenceId);

    OBCriteria AttributeUseCriteria = OBDal.getInstance().createCriteria(AttributeUse.class);
    AttributeUseCriteria.add(Expression.eq(AttributeUse.PROPERTY_ATTRIBUTESET,
        product.getAttributeSet()));
    AttributeUseCriteria.addOrderBy(AttributeUse.PROPERTY_SEQUENCENUMBER, true);
    java.util.List<AttributeUse> AttUseList = AttributeUseCriteria.list();

    info.addSelect("inpmAttributeuseId");
    for (AttributeUse AttUse : AttUseList) {
      info.addSelectResult(AttUse.getId(), AttUse.getAttribute().getIdentifier());
    }
    info.endSelect();

    // Fill Special Attributes

    info.addSelect("inpspecialatt");
    // Lot
    if (product.getAttributeSet().isLot() && opProduct.getProduct().getAttributeSet().isLot()) {
      org.openbravo.model.ad.domain.List Lot = SpecialAttListValue(LotSearchKey);
      if (Lot != null)
        info.addSelectResult(Lot.getSearchKey(), Lot.getName());
    }

    // Serial No.
    if (product.getAttributeSet().isSerialNo()
        && opProduct.getProduct().getAttributeSet().isSerialNo()) {
      org.openbravo.model.ad.domain.List Lot = SpecialAttListValue(SerialNoSearchKey);
      if (Lot != null)
        info.addSelectResult(Lot.getSearchKey(), Lot.getName());
    }

    // ExpirationDate
    if (product.getAttributeSet().isExpirationDate()
        && opProduct.getProduct().getAttributeSet().isExpirationDate()) {
      org.openbravo.model.ad.domain.List Lot = SpecialAttListValue(ExpirationDateearchKey);
      if (Lot != null)
        info.addSelectResult(Lot.getSearchKey(), Lot.getName());
    }
    info.endSelect();

  }

  private org.openbravo.model.ad.domain.List SpecialAttListValue(String Value)
      throws ServletException {
    Reference SpecialAttList = OBDal.getInstance().get(Reference.class, specialAttListId);
    OBCriteria SpecialAttListValuesCriteria = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    SpecialAttListValuesCriteria.add(Expression.eq(
        org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE, SpecialAttList));
    SpecialAttListValuesCriteria.add(Expression.eq(
        org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, Value));
    java.util.List<org.openbravo.model.ad.domain.List> SpecialAttListValues = (java.util.List) SpecialAttListValuesCriteria
        .list();
    if (SpecialAttListValues.isEmpty()) {
      return null;
    } else {
      return SpecialAttListValues.get(0);
    }
  }
}
