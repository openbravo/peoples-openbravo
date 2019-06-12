/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.GridKeyboard',
  kind: 'OB.OBPOS.UI.GridKeyboard',
  classes: 'obObposPointOfSaleUiGridKeyboard',
  components: [{
    kind: 'OB.UI.ActionEditBox',
    name: 'keyboardEditBox',
    statename: 'editbox',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionEditBox-generic obObposPointOfSaleUiGridKeyboard-keyboardEditBox'
  }, {
    kind: 'OB.UI.ActionButton',
    name: 'addqty',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-addqty',
    action: {
      window: 'retail.pointofsale',
      name: 'addQuantity'
    }
  }, {
    kind: 'OB.UI.ActionButton',
    name: 'removeqty',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-removeqty',
    action: {
      window: 'retail.pointofsale',
      name: 'removeQuantity'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'backspace',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-backspace',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-Backspace'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey0',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey0',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-0'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey1',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey1',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-1'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey2',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey2',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-2'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey3',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey3',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-3'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey4',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey4',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-4'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey5',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey5',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-5'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey6',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey6',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-6'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey7',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey7',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-7'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey8',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey8',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-8'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkey9',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkey9',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-9'
    }
  }, {
    kind: 'OB.UI.ActionButtonKey',
    name: 'keyboardkeyPeriod',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkeyPeriod',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-Period'
    }
  }, {
    kind: 'OB.UI.ActionButton',
    name: 'keyboardkeyEnter',
    classes: 'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic obObposPointOfSaleUiGridKeyboard-keyboardkeyEnter',
    action: {
      window: 'retail.pointofsale',
      name: 'keyboard-Enter'
    }
  }]
});