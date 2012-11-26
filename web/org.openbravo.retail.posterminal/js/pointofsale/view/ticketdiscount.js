/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts',
  handlers: {
    onApllyDiscounts: 'applyDiscounts',
    onDiscountsClose: 'closingDiscounts',
    onDiscountQtyChanged: 'discountQtyChanged'
  },
  events: {
    onDiscountsModeFinished: '',
    onDisableKeyboard: '',
    onDiscountsModeKeyboard: ''
  },
  disableKeyboard: function () {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: false
    });
  },
  discountsMode: function () {
    this.doDiscountsModeKeyboard({
      status: false
    });
  },
  enableKeyboard: function () {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: true
    });
  },
  discountQtyChanged: function (inSender, inEvent) {
    this.$.qtyToDiscount.setContent(inEvent.qty);
  },
  style: 'position:relative; background-color: orange; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px',
  content: 'discounts',
  components: [{
    name: 'qtyToDiscount',
  }, {
    content: 'disc mode',
    ontap: 'discountsMode'
  }, {
    content: 'disable',
    ontap: 'disableKeyboard'
  }, {
    content: 'enable',
    ontap: 'enableKeyboard'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
    name: 'checkSelectAll'
  }, {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
    }]
  }],
  closingDiscounts: function (inSender, inEvent) {
    this.$.checkSelectAll.unCheck();
    this.doDiscountsModeFinished({
      tabPanel: 'scan',
      keyboard: 'toolbarscan',
      edit: false,
      options: {
        discounts: false
      }
    });
  },
  applyDiscounts: function (inSender, inEvent) {
    //get discount
    //apply to order
    this.closingDiscounts();
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
  content: 'apply',
  events: {
    onApplyDiscounts: ''
  },
  tap: function () {
    this.doApplyDiscounts();
  }
});

enyo.kind({
  kind: 'OB.UI.CheckboxButton',
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
  events: {
    onCheckAllTicketLines: ''
  },
  checked: false,
  tap: function () {
    this.inherited(arguments);
    this.doCheckAllTicketLines({
      status: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
  events: {
    onDiscountsClose: ''
  },
  kind: 'OB.UI.Button',
  content: 'cancel',
  tap: function () {
    this.doDiscountsClose();
  }
});