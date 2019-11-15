/*
 ************************************************************************************
 * Copyright (C) 2014-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.UI.ModalVerifiedReturns',
  kind: 'OB.UI.ModalSelector',
  classes: 'obUiModalVerifiedReturns',
  i18nHeader: 'OBPOS_LblPaidReceipts',
  published: {
    params: null
  },
  body: {
    kind: 'OB.UI.ReceiptsForVerifiedReturnsList',
    classes: 'obUiModalVerifiedReturns-body-obUiReceiptsForVerifiedReturnsList'
  },
  footer: {
    kind: 'OB.UI.ModalVerifiedReturnsFooter'
  },
  getFilterSelectorTableHeader: function() {
    return this.$.body.$.receiptsForVerifiedReturnsList.$
      .verifiedReturnsReceiptsListItemPrinter.$.theader.$
      .modalVerifiedReturnsScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function() {
    return this.$.body.$.receiptsForVerifiedReturnsList.$
      .verifiedReturnsReceiptsListItemPrinter.$.theader.$
      .modalVerifiedReturnsScrollableHeader.$
      .advancedFilterWindowButtonVerifiedReturns;
  },
  getAdvancedFilterDialog: function() {
    return 'modalAdvancedFilterVerifiedReturns';
  },
  executeOnShow: function() {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
  },
  init: function(model) {
    this.model = model;
  }
});
