/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalDiscountNeedQty',
  kind: 'OB.UI.ModalInfo',
  classes: 'obObposPointOfSaleUiModalsModalDiscountNeedQty',
  i18nHeader: 'OBPOS_discountNeedsQty_header',
  bodyContent: {
    classes: 'obObposPointOfSaleUiModalsModalDiscountNeedQty-element1',
    i18nContent: 'OBPOS_discountNeedsQty_body'
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalNotValidValueForDiscount',
  kind: 'OB.UI.ModalInfo',
  classes: 'obObposPointOfSaleUiModalsModalNotValidValueForDiscount',
  i18nHeader: 'OBPOS_modalNotValidValueForDiscount_header',
  bodyContent: {
    classes: 'obObposPointOfSaleUiModalsModalNotValidValueForDiscount-element1',
    i18nContent: 'OBPOS_modalNotValidValueForDiscount_body'
  }
});
