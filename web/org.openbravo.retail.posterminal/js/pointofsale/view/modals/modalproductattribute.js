/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
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
      isDefaultAction: true,
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
  /*
   * - LOT: expression starts with L followed by alphanumeric lot name
   * - SERIAL and LOT: expression starts with L followed by alphanumeric lot name and serial number
   * - EXPIRATION DATE: expression should contain a date format (dd-MM-yyyy)
   * - LOT and SERIAL and EXPIRATION DATE - LJAN17_#6969_28-02-2018
   */
  validAttribute: function (attribute) {
    var valueAttribute = attribute,
        pattern = "/^L|[0-9a-zA-Z]*#*[0-9_a-zA-Z]*";
    return (valueAttribute.match(pattern)) ? true : false;
  },
  saveAttribute: function (inSender, inEvent) {
    var me = this,
        inpAttributeValue = this.$.bodyContent.$.valueAttribute.getValue();
    if ((this.validAttribute(inpAttributeValue) && inpAttributeValue)) {
      this.args.callback(inpAttributeValue);
    } else {
      this.args.callback(null);
    }
  },
  clearAction: function () {
    this.$.bodyContent.$.valueAttribute.setValue(null);
    return true;
  },
  cancelAction: function () {
    this.deleteOrderline(this.args.line);
    this.hide();
    return true;
  },
  saveAction: function () {
    this.saveAttribute();
    this.hide();
    return true;
  },
  executeOnHide: function () {
    var me = this;
    var inpAttributeValue = this.$.bodyContent.$.valueAttribute.getValue();
    if (!inpAttributeValue) {
      this.deleteOrderline(this.args.line);
      if (me.args.finalCallback) {
        me.args.finalCallback(false, null);
      }
    }
    this.$.bodyContent.$.valueAttribute.setValue(null);
  }
});
OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalProductAttributes',
  name: 'OB.UI.ModalProductAttributes'
});