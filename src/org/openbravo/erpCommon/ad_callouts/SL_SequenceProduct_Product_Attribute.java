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

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Expression;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.common.plm.Product;

public class SL_SequenceProduct_Product_Attribute extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final IsIDFilter idFilter = new IsIDFilter();

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String strmProductId = info.getStringParameter("inpmProductId", idFilter);
    Product product = OBDal.getInstance().get(Product.class, strmProductId);
    OBCriteria AttributeUseCriteria = OBDal.getInstance().createCriteria(AttributeUse.class);
    AttributeUseCriteria.add(Expression.eq(AttributeUse.PROPERTY_ATTRIBUTESET,
        product.getAttributeSet()));
    AttributeUseCriteria.addOrderBy(AttributeUse.PROPERTY_SEQUENCENUMBER, true);
    List<AttributeUse> AttUseList = AttributeUseCriteria.list();

    info.addSelect("inpmAttributeuseId");

    for (AttributeUse AttUse : AttUseList) {
      info.addSelectResult(AttUse.getId(), AttUse.getAttribute().getIdentifier());
    }

    info.endSelect();
  }
}
