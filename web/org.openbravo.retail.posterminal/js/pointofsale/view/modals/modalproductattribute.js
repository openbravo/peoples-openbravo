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
      ontap: 'saveAttribute'
    }, {
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBPOS_LblClear',
      ontap: 'clearAction'
    }, {
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBMOBC_LblCancel',
      ontap: 'cancelAction'
    }]
  },

  /*
   * - LOT: expression starts with L followed by alphanumeric lot name
   * - SERIAL and LOT: expression starts with L followed by alphanumeric lot name and serial number
   * - EXPIRATION DATE: expression should contain a date format (dd-MM-yyyy)
   * - LOT and SERIAL and EXPIRATION DATE - LJAN17_#6969_28-02-2018
   */
  validateAttribute: function (attribute) {
    var valueAttribute = attribute,
        pattern = "/^L|[0-9a-zA-Z]*#*[0-9_a-zA-Z]*";
    return (valueAttribute.match(pattern)) ? true : false;
  },

  saveAttribute: function (inSender, inEvent) {
    var me = this,
        attributeValue = this.$.bodyContent.$.valueAttribute.getValue(),
        receipt = me.owner.model.get('order'),
        orderline = me.args.line,
        p = orderline.get('product'),
        qty = orderline.get('qty'),
        options = me.args.options,
        newline = true,
        repeteadAttribute = false,
        showErrorSerialNumber = false,
        i, repeteadLine;

    if (attributeValue) {
      for (i = 0; i < me.owner.model.get('order').get('lines').length; i++) {
        if (me.owner.model.get('order').get('lines').models[i].getAttributeValue() === attributeValue) {
          repeteadAttribute = true;
          repeteadLine = me.owner.model.get('order').get('lines').models[i];
        }
      }
      if (this.validateAttribute(attributeValue)) {
        if (!options) {
          if (repeteadAttribute) {
            me.owner.model.get('order').get('lines').remove(orderline);
            if (OB.MobileApp.model.hasPermission('OBPOS_EnableAttrSetSearch', true) && p.get('hasAttributes') && p.get('isSerialNo')) {
              showErrorSerialNumber = true;
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NotSerialNo'), OB.I18N.getLabel('OBPOS_ProductHasSerialNo', null), [{
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                action: function () {
                  return true;
                }
              }, {
                label: OB.I18N.getLabel('OBMOBC_LblCancel')
              }])
            } else {
              receipt.addUnit(repeteadLine, 1);
            }
            receipt.save();
          } else {
            orderline.set('attributeValue', attributeValue);
            me.owner.model.get('order').save();
          }
        } else {
          if (attributeValue != orderline.getAttributeValue()) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NotValidateAttribute'), OB.I18N.getLabel('OBPOS_NotSameAttribute', null), [{
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              action: function () {
                return true;
              }
            }, {
              label: OB.I18N.getLabel('OBMOBC_LblCancel')
            }])
            me.owner.model.get('order').get('lines').remove(orderline);
          }
        }
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NotValidAttribute'));
      }
      if (!showErrorSerialNumber) {
        this.args.callbackPostAddProductToOrder(receipt, p, orderline, qty, null, newline);
      }
    }
    return true;
  },
  clearAction: function () {
    this.$.bodyContent.$.valueAttribute.setValue(null);
    return true;
  },

  cancel: function () {
    var currentLine = this.args.line
    var options = this.args.options
    if (currentLine && !options) {
      this.owner.model.get('order').deleteLine(currentLine);
      this.owner.model.get('order').save();
    }
    this.hide();
    return true;
  },
  executeOnShow: function () {
    var me = this;
    this.$.bodyButtons.saveAttribute = function (inSender, inEvent) {
      me.saveAttribute(inSender, inEvent);
      me.hide();
    };
    this.$.bodyButtons.clearAction = function () {
      me.clearAction();
    };
    this.$.bodyButtons.cancelAction = function () {
      me.cancel();
    };
  },
  executeOnHide: function () {
    var attributeValue = this.$.bodyContent.$.valueAttribute.getValue()
    var options = this.args.options
    if (!attributeValue && !options) {
      this.owner.model.get('order').deleteLine(this.args.line);
      this.owner.model.get('order').save();
    }
    this.$.bodyContent.$.valueAttribute.setValue(null);
  }
});
OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalProductAttributes',
  name: 'OB.UI.ModalProductAttributes'
});