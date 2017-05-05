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
  name: 'OB.UI.ModalProductAttributeVerifiedReturns',
  i18nHeader: 'OBPOS_ModalProductAttributeVerifiedReturnsTitle',
  style: 'width: 700px;',
  autoDismiss: false,
  bodyContent: {
    name: 'verifiedReturns'
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBMOBC_LblOk',
      isDefaultAction: true,
      tap: function () {
        this.owner.owner.validateAction();
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
  clearAction: function () {
    var me = this,
        i, line = me.args.line;
    for (i = 0; i < line.length; i++) {
      me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].setValue(null);
      me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].addStyles('background-color: none');
    }
    me.$.bodyButtons.$.modalDialogButton.setDisabled(true);
    this.blur();
    return;
  },
  cancelAction: function () {
    this.hide();
    return;
  },
  validateAction: function () {
    var me = this;
    me.args.returnLinesPopup.callbackExecutor();
    this.hide();
    return;
  },
  blur: function (inSender, inEvent) {
    var me = this,
        line = me.args.line,
        validAttribute, orderlineAttribute, orderlineProduct, inpProduct, inpAttribute, i;
    for (i = 0; i < line.length; i++) {
      validAttribute = false;
      orderlineProduct = line[i].id;
      orderlineAttribute = line[i].attributeValue;
      inpProduct = me.$.bodyContent.$.verifiedReturns.$['productId' + i].getContent();
      inpAttribute = me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].getValue();
      if (inpAttribute) {
        if ((orderlineAttribute !== inpAttribute) && (orderlineProduct === inpProduct)) {
          me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].addStyles('background-color: red');
          validAttribute = false;
        } else {
          me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].addStyles('background-color: #6cb33f');
          validAttribute = true;
        }
      }
    }
    if (validAttribute) {
      me.$.bodyButtons.$.modalDialogButton.setDisabled(false);
    }
    return true;
  },
  executeOnShow: function () {
    var me = this,
        line = me.args.line,
        documentno = me.args.documentno,
        i;
    me.$.bodyContent.$.verifiedReturns.destroyComponents();
    me.$.bodyContent.$.verifiedReturns.createComponent({
      style: 'border-bottom: 1px solid #cccccc;',
      components: [{
        name: 'headerTitle',
        type: 'text'
      }, {
        name: 'documentno',
        type: 'text',
        classes: 'span4',
        style: 'line-height: 50px; font-size: 17px;text-align: center;width: 700px;font-weight: bold;border-bottom: 1px solid #cccccc;'
      }]
    });
    me.$.bodyContent.$.verifiedReturns.$.headerTitle.setContent(OB.I18N.getLabel('OBPOS_ProductAttributeValueVerifiedReturnsDesc'));
    me.$.bodyContent.$.verifiedReturns.$.documentno.setContent(documentno);
    for (i = 0; i < line.length; i++) {
      me.$.bodyContent.$.verifiedReturns.createComponent({
        components: [{
          name: 'productName' + i,
          type: 'text',
          classes: 'span4',
          style: 'line-height: 35px; font-size: 17px;text-align: left;width: 275px; padding-top: 10px;padding-left: 5px;font-weight: bold'
        }, {
          kind: 'enyo.Input',
          type: 'text',
          maxlength: '70',
          classes: 'span4',
          style: 'line-height: 35px; font-size: 17px;text-align: center;width: 400px; padding-top: 10px;',
          name: 'valueAttribute' + i,
          isFirstFocus: true,
          handlers: {
            onblur: 'blur'
          },
          placeholder: 'Scan attribute'
        }, {
          name: 'productId' + i,
          type: 'text',
          classes: 'span4',
          style: 'line-height: 50px;width: 700px;'
        }]
      });

      me.$.bodyContent.$.verifiedReturns.$['productName' + i].setContent(line[i].name);
      me.$.bodyContent.$.verifiedReturns.$['productId' + i].setContent(line[i].id);
      me.$.bodyContent.$.verifiedReturns.$['productId' + i].hide();
    }
    me.$.bodyButtons.$.modalDialogButton.setDisabled(true);
    me.$.bodyContent.$.verifiedReturns.render();
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalProductAttributeVerifiedReturns',
  name: 'OB.UI.ModalProductAttributeVerifiedReturns'
});