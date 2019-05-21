/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  name: 'OB.UI.ModalReceiptPropertiesImpl',
  kind: 'OB.UI.ModalReceiptProperties',
  classes: 'obUiModalReceiptPropertiesImpl',
  handlers: {
    onCloseCancelSelector: 'closeCancelSelector',
    onUpdateFilterSelector: 'updateFilterSelector',
    onMoveScrollDown: 'moveScrollDown'
  },
  newAttributes: [{
    kind: 'OB.UI.renderTextProperty',
    name: 'receiptDescription',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-receiptDescription',
    modelProperty: 'description',
    i18nLabel: 'OBPOS_LblDescription',
    maxLength: 255
  }, {
    kind: 'OB.UI.renderBooleanProperty',
    name: 'printBox',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-printBox obUiModalReceiptPropertiesImpl-newAttributes-printBox_active',
    checked: true,
    modelProperty: 'print',
    i18nLabel: 'OBPOS_Lbl_RP_Print'
  }, {
    kind: 'OB.UI.renderComboProperty',
    name: 'salesRepresentativeBox',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-salesRepresentativeBox',
    modelProperty: 'salesRepresentative',
    modelPropertyText: 'salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
    i18nLabel: 'OBPOS_SalesRepresentative',
    permission: 'OBPOS_salesRepresentative.receipt',
    permissionOption: 'OBPOS_SR.comboOrModal',
    retrievedPropertyForValue: 'id',
    retrievedPropertyForText: '_identifier',
    init: function (model) {
      this.collection = new OB.Collection.SalesRepresentativeList();
      this.model = model;
      this.doLoadValueNeeded = true;
      if (!OB.MobileApp.model.hasPermission(this.permission)) {
        this.doLoadValueNeeded = false;
        this.parent.parent.parent.hide();
      } else {
        if (OB.MobileApp.model.hasPermission(this.permissionOption, true)) {
          this.doLoadValueNeeded = false;
          this.parent.parent.parent.hide();
        }
      }
    },

    // override to not load things upfront when not needed
    loadValue: function () {
      if (this.doLoadValueNeeded) {
        // call the super implementation in the prototype directly
        OB.UI.renderComboProperty.prototype.loadValue.apply(this, arguments);
      }
    },

    fetchDataFunction: function (args) {
      var me = this,
          actualUser;

      if (this.collection.length === 0) {
        OB.Dal.find(OB.Model.SalesRepresentative, null, function (data) {
          if (me.destroyed) {
            return;
          }
          if (data.length > 0) {
            data.unshift({
              id: null,
              _identifier: null
            });
            me.dataReadyFunction(data, args);
          } else {
            actualUser = new OB.Model.SalesRepresentative();
            actualUser.set('_identifier', me.model.get('order').get('salesRepresentative$_identifier'));
            actualUser.set('id', me.model.get('order').get('salesRepresentative'));
            data.models = [actualUser];
            me.dataReadyFunction(data, args);
          }

        }, function () {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingSalesRepresentative'));
          me.dataReadyFunction(null, args);
        }, args);
      } else {
        me.dataReadyFunction(this.collection, args);
      }
    }
  }, {
    kind: 'OB.UI.SalesRepresentative',
    name: 'salesrepresentativebutton',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-salesRepresentativeBox',
    i18nLabel: 'OBPOS_SalesRepresentative',
    permission: 'OBPOS_salesRepresentative.receipt',
    permissionOption: 'OBPOS_SR.comboOrModal'
  }, {
    kind: 'OB.UI.Customer',
    target: 'filterSelectorButton_receiptProperties',
    popup: 'receiptPropertiesDialog',
    name: 'customerbutton',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-customerbutton',
    i18nLabel: 'OBPOS_LblCustomer'
  }, {
    kind: 'OB.UI.ShipTo',
    target: 'filterSelectorButton_receiptProperties',
    popup: 'receiptPropertiesDialog',
    name: 'addressshipbutton',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-addressshipbutton',
    i18nLabel: 'OBPOS_LblShipAddr'
  }, {
    kind: 'OB.UI.BillTo',
    target: 'filterSelectorButton_receiptProperties',
    popup: 'receiptPropertiesDialog',
    name: 'addressbillbutton',
    classes: 'obUiModalReceiptPropertiesImpl-newAttributes-addressbillbutton',
    i18nLabel: 'OBPOS_LblBillAddr'
  }],

  closeCancelSelector: function (inSender, inEvent) {
    if (inEvent.target === 'filterSelectorButton_receiptProperties') {
      this.show();
    }
  },
  moveScrollDown: function (inSender, inEvent) {
    if (inEvent.target === 'filterSelectorButton_receiptProperties') {
      this.$.bodyContent.$.scroller.setScrollTop(this.$.bodyContent.$.scroller.getScrollTop() + inEvent.lineHeight);
      return true;
    }
  },
  updateFilterSelector: function (inSender, inEvent) {
    if (inEvent.selector.name === 'receiptProperties') {
      this.bubble('onChangeBusinessPartner', {
        businessPartner: inEvent.selector.businessPartner,
        target: 'order'
      });
      this.show();
    }
  },
  resetProperties: function () {
    var p, att;
    // reset all properties
    for (p in this.newAttributes) {
      if (this.newAttributes.hasOwnProperty(p)) {
        att = this.$.bodyContent.$.attributes.$['line_' + this.newAttributes[p].name].$.newAttribute.$[this.newAttributes[p].name];
        if (att && att.setValue) {
          att.setValue('');
        }
      }
    }
  },
  executeOnShow: function (isSender, inEvent) {
    var bp = this.model.get('bp'),
        p;
    this.waterfall('onSetModel', {
      model: this.model
    });
    if (bp && bp.get('locId') === bp.get('shipLocId')) {
      this.$.bodyContent.$.attributes.$.line_addressshipbutton.hide();
      this.$.bodyContent.$.attributes.$.line_addressbillbutton.$.labelLine.setContent(OB.I18N.getLabel('OBPOS_LblAddress'));
    } else {
      this.$.bodyContent.$.attributes.$.line_addressshipbutton.show();
      this.$.bodyContent.$.attributes.$.line_addressbillbutton.$.labelLine.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
    }
    for (p in this.newAttributes) {
      if (this.newAttributes.hasOwnProperty(p)) {
        this.loadValue(this.newAttributes[p].modelProperty);
      }
    }
  },
  init: function (model) {
    this.setHeader(OB.I18N.getLabel('OBPOS_ReceiptPropertiesDialogTitle'));

    this.model = model.get('order');
    this.model.on('change', function () {
      var diff = this.model.changedAttributes(),
          att;
      for (att in diff) {
        if (diff.hasOwnProperty(att)) {
          this.loadValue(att);
        }
      }
    }, this);

    this.model.on('paymentAccepted', function () {
      this.resetProperties();
    }, this);
  }
});