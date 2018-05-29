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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromprocess;

import javax.enterprise.context.Dependent;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

@Dependent
@Qualifier(CreateLinesFromProcessHook.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateProductAndAttributes extends CreateLinesFromProcessHook {

  @Override
  public int getOrder() {
    return -50;
  }

  /**
   * Update the product and attribute set to the new invoice line
   */
  @Override
  public void exec() {
    // Update the product
    getInvoiceLine().setProduct(
        (Product) getCopiedFromLine().get(
            isCopiedFromOrderLine() ? OrderLine.PROPERTY_PRODUCT : ShipmentInOutLine.PROPERTY_PRODUCT));
    // Update the attributes
    AttributeSetInstance attributeSetValue = (AttributeSetInstance) getCopiedFromLine().get(
        isCopiedFromOrderLine() ? OrderLine.PROPERTY_ATTRIBUTESETVALUE
            : ShipmentInOutLine.PROPERTY_ATTRIBUTESETVALUE);
    if (attributeSetValue != null) {
      getInvoiceLine().setAttributeSetValue(copyAttributeSetValue(attributeSetValue));
    }
  }

  private AttributeSetInstance copyAttributeSetValue(final AttributeSetInstance attributeSetValue) {
    AttributeSetInstance newAttributeSetInstance = copyAttributeSetInstance(attributeSetValue);
    copyAttributes(attributeSetValue, newAttributeSetInstance);
    return newAttributeSetInstance;
  }

  private AttributeSetInstance copyAttributeSetInstance(final AttributeSetInstance attributeSetValue) {
    AttributeSetInstance newAttributeSetInstance = OBProvider.getInstance().get(
        AttributeSetInstance.class);
    newAttributeSetInstance.setAttributeSet(attributeSetValue.getAttributeSet());
    newAttributeSetInstance.setSerialNo(attributeSetValue.getSerialNo());
    newAttributeSetInstance.setLot(attributeSetValue.getLot());
    newAttributeSetInstance.setExpirationDate(attributeSetValue.getExpirationDate());
    newAttributeSetInstance.setDescription(attributeSetValue.getDescription());
    newAttributeSetInstance.setLotName(attributeSetValue.getLotName());
    newAttributeSetInstance.setLocked(attributeSetValue.isLocked());
    newAttributeSetInstance.setLockDescription(attributeSetValue.getLockDescription());
    OBDal.getInstance().save(newAttributeSetInstance);
    return newAttributeSetInstance;
  }

  private void copyAttributes(final AttributeSetInstance attributeSetValueFrom,
      final AttributeSetInstance attributeSetInstanceTo) {
    for (AttributeInstance attrInstance : attributeSetValueFrom.getAttributeInstanceList()) {
      AttributeInstance newAttributeInstance = OBProvider.getInstance()
          .get(AttributeInstance.class);
      newAttributeInstance.setAttributeSetValue(attributeSetInstanceTo);
      newAttributeInstance.setAttribute(attrInstance.getAttribute());
      attrInstance.setAttributeValue(attrInstance.getAttributeValue());
      attributeSetInstanceTo.getAttributeInstanceList().add(newAttributeInstance);
      OBDal.getInstance().save(newAttributeInstance);
      OBDal.getInstance().save(attributeSetInstanceTo);
    }
  }
}
