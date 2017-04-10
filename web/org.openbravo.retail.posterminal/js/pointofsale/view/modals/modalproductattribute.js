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
        inpattributeValue = this.$.bodyContent.$.valueAttribute.getValue(),
        receipt = me.owner.model.get('order'),
        orderline = me.owner.model.get('order').get('lines'),
        currentline = me.args.line,
        currentlineProduct = currentline.get('product'),
        currentlineqty = currentline.get('qty'),
        options = me.args.options,
        newline = true,
        existingAttribute = false,
        finalCallbackStatus = false;
    if (this.validAttribute(inpattributeValue) && inpattributeValue) {
      if (typeof options === 'undefined' || !options) {
        //NORMAL or BLIND RETURN
        if (this.validateAttributeWithOrderlines(currentlineProduct, orderline, inpattributeValue)) {
          this.deleteOrderline(currentline);
          if (currentlineProduct.get('isSerialNo')) {
            this.showConfirm(OB.I18N.getLabel('OBPOS_NotSerialNo'), OB.I18N.getLabel('OBPOS_ProductHasSerialNo'));
            if (me.args.finalCallback) {
              finalCallbackStatus = false;
            }
          }
          receipt.addUnit(currentline, currentlineqty);
          receipt.save();
          finalCallbackStatus = false;
        } else {
          currentline.set('attributeValue', inpattributeValue);
          receipt.save();
          finalCallbackStatus = true;
        }
        this.args.initialCallback(receipt, currentlineProduct, currentline, currentlineqty, null, newline, me.args.finalCallback(finalCallbackStatus, currentline));
      } else {
        //VERIFIED RETURN
        if (inpattributeValue !== currentline.getAttributeValue() && options.isVerifiedReturn) {
          this.showConfirm(OB.I18N.getLabel('OBPOS_NotValidateAttribute'), OB.I18N.getLabel('OBPOS_NotSameAttribute'));
          this.deleteOrderline(currentline);
          finalCallbackStatus = false;
          this.args.initialCallback(receipt, currentlineProduct, currentline, currentlineqty, null, newline, me.args.finalCallback(finalCallbackStatus, currentline));
        }
      }
    } else {
      this.deleteOrderline(currentline);
      finalCallbackStatus = false;
      this.args.initialCallback(receipt, currentlineProduct, currentline, currentlineqty, null, newline, me.args.finalCallback(finalCallbackStatus, currentline));
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
  validateAttributeWithOrderlines: function (currentlineProduct, orderline, inpAttributeValue) {
    var me = this,
        i, orderlineProduct, ordrerlineAttribute, existingAttribute = false;
    for (i = 0; i < orderline.length; i++) {
      orderlineProduct = orderline.models[i].attributes.product.id;
      ordrerlineAttribute = orderline.models[i].getAttributeValue();
      if ((ordrerlineAttribute === inpAttributeValue) && (orderlineProduct === currentlineProduct.id)) {
        existingAttribute = true;
        break;
      }
    }
    return existingAttribute;
  },
  executeOnShow: function () {},
  executeOnHide: function () {
    var me = this;
    var inpattributeValue = this.$.bodyContent.$.valueAttribute.getValue();
    if (!inpattributeValue) {
      this.deleteOrderline(this.args.line);
      if (me.args.finalCallback) {
        me.args.finalCallback(false, null);
      }
    }
    this.$.bodyContent.$.valueAttribute.setValue(null);
  },
  deleteOrderline: function (currentLine) {
    this.owner.model.get('order').deleteLine(currentLine);
    this.owner.model.get('order').save();
  },
  showConfirm: function (label1, label2) {
    var me = this;
    OB.UTIL.showConfirmation.display(label1, label2, [{
      label: OB.I18N.getLabel('OBMOBC_LblOk'),
      action: function () {
        return false;
      }
    }]);
  }
});
OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalProductAttributes',
  name: 'OB.UI.ModalProductAttributes'
});