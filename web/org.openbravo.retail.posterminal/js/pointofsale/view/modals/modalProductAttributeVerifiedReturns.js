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
  style: 'width: 900px;',
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
  validAttribute: function (attribute) {
    var valueAttribute = attribute,
        pattern = "/^L|[0-9a-zA-Z]*#*[0-9_a-zA-Z]*";
    return (valueAttribute.match(pattern)) ? true : false;
  },
  clearAction: function () {
    var i, line = this.args.line;
    for (i = 0; i < line.length; i++) {
      this.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].setValue(null);
    }
    return true;
  },
  cancelAction: function () {
    this.owner.model.get('order').get('lines').remove(this.args.line);
    this.hide();
    return true;
  },
  validateAction: function () {
    this.validateAttributeWithOrderlines();
    this.hide();
    return true;
  },
  validateAttributeWithOrderlines: function (inSender, inEvent) {
    var me = this,
        currentlineProduct, orderlineProduct, currentlineAttribute, orderlineAttr, i, line = me.args.line,
        orderline = me.owner.model.get('order').get('lines'),
        notValidAttribute = false;
    for (i = 0; i < orderline.length; i++) {
      orderlineProduct = orderline.models[i].attributes.product.id;
      orderlineAttr = orderline.models[i].getAttributeValue();
      currentlineProduct = line[i].id;
      currentlineAttribute = me.$.bodyContent.$.verifiedReturns.$['valueAttribute' + i].getValue();
      if ((orderlineAttr !== currentlineAttribute) && (orderlineProduct === currentlineProduct)) {
        notValidAttribute = true;
        break;
      }
    }
    if (notValidAttribute) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NotValidateAttribute'), OB.I18N.getLabel('OBPOS_NotSameAttribute'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          return false;
        }
      }]);
    }
  },
  executeOnShow: function () {
    var me = this,
        line = me.args.line,
        i, noOfLines = line.length;
    this.$.bodyContent.$.verifiedReturns.destroyComponents();
    this.$.bodyContent.$.verifiedReturns.createComponent({
      initComponents: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_ProductAttributeValueDialogTitleDesc'));
      }
    });
    for (i = 0; i < noOfLines; i++) {
      this.$.bodyContent.$.verifiedReturns.createComponent({
        components: [{
          kind: 'enyo.Input',
          type: 'text',
          attributes: {
            maxlength: 70
          },
          style: 'text-align: center;width: 400px; height: 40px;',
          name: 'valueAttribute' + i,
          selectOnFocus: true,
          isFirstFocus: true,
          value: ''
        }, {
          name: 'productName' + i,
          style: 'text-align: left;width: 400px; height: 40px;'
        }]
      });
      this.$.bodyContent.$.verifiedReturns.$['productName' + i].setContent(line[i].name);
    }
    this.$.bodyContent.$.verifiedReturns.render();
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalProductAttributeVerifiedReturns',
  name: 'OB.UI.ModalProductAttributeVerifiedReturns'
});