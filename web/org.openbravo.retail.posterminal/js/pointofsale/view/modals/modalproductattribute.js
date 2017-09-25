/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB, moment, enyo */
enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalProductAttributes',
  i18nHeader: 'OBPOS_ProductAttributeValueDialogTitle',
  autoDismiss: false,
  bodyContent: {
    components: [{
      initComponents: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_ProductAttributeValueDialogTitleDesc'));
      }
    }, {
      kind: 'enyo.Input',
      type: 'text',
      attributes: {
        maxlength: 70
      },
      style: 'text-align: center;width: 400px; height: 40px;',
      name: 'valueAttribute',
      selectOnFocus: true,
      isFirstFocus: true,
      value: ''
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBMOBC_LblOk',
      tap: function () {
        this.owner.owner.saveAction();
      }
    }, {
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBPOS_LblClear',
      tap: function () {
        this.owner.owner.clearAction();
      }
    }, {
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBMOBC_LblCancel',
      tap: function () {
        this.owner.owner.cancelAction();
      }
    }]
  },
  /**
   * This method should be overriden to implement validation of attributes for specific cases
   */
  validAttribute: function (attribute) {
    return true;
  },
  saveAttribute: function (inSender, inEvent) {
    var me = this,
        inpAttributeValue = this.$.bodyContent.$.valueAttribute.getValue();
    if ((this.validAttribute(inpAttributeValue) && inpAttributeValue)) {
      this.args.callback(inpAttributeValue);
      this.hide();
    } else {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NoAttributeValue'));
    }
  },
  clearAction: function () {
    this.$.bodyContent.$.valueAttribute.setValue(null);
    return;
  },
  cancelAction: function () {
    if (this.args.callback) {
      this.args.callback(null);
    }
    this.hide();
    return;
  },
  saveAction: function () {
    this.saveAttribute();
    return;
  },
  executeOnHide: function () {
    var me = this;
    var inpAttributeValue = this.$.bodyContent.$.valueAttribute.getValue();
    if (!inpAttributeValue && this.args.callback) {
      this.args.callback(null);
    }
    this.$.bodyContent.$.valueAttribute.setValue(null);
  },
  executeOnShow: function () {
    this.$.headerCloseButton.hide();
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});