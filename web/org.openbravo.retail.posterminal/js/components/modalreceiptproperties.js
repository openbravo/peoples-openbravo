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
  i18nHeader: 'OBPOS_ReceiptPropertiesDialogTitle',
  handlers: {
    onCloseCancelSelector: 'closeCancelSelector',
    onUpdateFilterSelector: 'updateFilterSelector',
    onMoveScrollDown: 'moveScrollDown'
  },
  newAttributes: [
    {
      kind: 'OB.UI.renderTextProperty',
      name: 'receiptDescription',
      classes:
        'obUiModalReceiptPropertiesImpl-newAttributes-receiptDescription',
      modelProperty: 'description',
      i18nLabel: 'OBPOS_LblDescription',
      maxLength: 255
    },
    {
      kind: 'OB.UI.renderBooleanProperty',
      name: 'printBox',
      classes:
        'obUiModalReceiptPropertiesImpl-newAttributes-printBox obUiModalReceiptPropertiesImpl-newAttributes-printBox_active',
      checked: true,
      modelProperty: 'print',
      i18nLabel: 'OBPOS_Lbl_RP_Print'
    },
    {
      kind: 'OB.UI.renderComboProperty',
      name: 'salesRepresentativeBox',
      classes:
        'obUiModalReceiptPropertiesImpl-newAttributes-salesRepresentativeBox',
      modelProperty: 'salesRepresentative',
      modelPropertyText:
        'salesRepresentative' +
        OB.Constants.FIELDSEPARATOR +
        OB.Constants.IDENTIFIER,
      i18nLabel: 'OBPOS_SalesRepresentative',
      permission: 'OBPOS_salesRepresentative.receipt',
      permissionOption: 'OBPOS_SR.comboOrModal',
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: '_identifier',
      init: function(model) {
        this.model = model;
        this.doLoadValueNeeded = true;
        if (!OB.MobileApp.model.hasPermission(this.permission)) {
          this.doLoadValueNeeded = false;
          this.formElement.hide();
        } else {
          if (OB.MobileApp.model.hasPermission(this.permissionOption, true)) {
            this.doLoadValueNeeded = false;
            this.formElement.hide();
          }
        }
      },

      initComponents: function() {
        this.collection = new Backbone.Collection();
      },

      // override to not load things upfront when not needed
      loadValue: function() {
        if (this.doLoadValueNeeded) {
          // call the super implementation in the prototype directly
          OB.UI.renderComboProperty.prototype.loadValue.apply(this, arguments);
        }
      },

      fetchDataFunction: async function(args) {
        var me = this,
          actualUser;

        if (this.collection.length === 0) {
          try {
            const dataSalesRepresentative = await OB.App.MasterdataModels.SalesRepresentative.orderedBy(
              '_identifier'
            );

            if (me.destroyed) {
              return;
            }
            if (dataSalesRepresentative && dataSalesRepresentative.length > 0) {
              dataSalesRepresentative.unshift({
                id: null,
                _identifier: null
              });
              me.dataReadyFunction(dataSalesRepresentative, args);
            } else {
              actualUser = new Backbone.Model();
              actualUser.set(
                '_identifier',
                me.model.get('order').get('salesRepresentative$_identifier')
              );
              actualUser.set(
                'id',
                me.model.get('order').get('salesRepresentative')
              );
              dataSalesRepresentative.models = [actualUser];
              me.dataReadyFunction(dataSalesRepresentative, args);
            }
          } catch (err) {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingSalesRepresentative')
            );
            me.dataReadyFunction(null, args);
          }
        } else {
          me.dataReadyFunction(this.collection, args);
        }
      }
    },
    {
      kind: 'OB.UI.SalesRepresentative',
      name: 'salesrepresentativebutton',
      classes:
        'obUiModalReceiptPropertiesImpl-newAttributes-salesrepresentativebutton',
      i18nLabel: 'OBPOS_SalesRepresentative',
      permission: 'OBPOS_salesRepresentative.receipt',
      permissionOption: 'OBPOS_SR.comboOrModal',
      hideNullifyButton: true
    },
    {
      kind: 'OB.UI.Customer',
      target: 'filterSelectorButton_receiptProperties',
      popup: 'receiptPropertiesDialog',
      name: 'customerbutton',
      classes: 'obUiModalReceiptPropertiesImpl-newAttributes-customerbutton',
      hideNullifyButton: true,
      i18nLabel: 'OBPOS_LblCustomer'
    },
    {
      kind: 'OB.UI.ShipTo',
      target: 'filterSelectorButton_receiptProperties',
      popup: 'receiptPropertiesDialog',
      name: 'addressshipbutton',
      classes: 'obUiModalReceiptPropertiesImpl-newAttributes-addressshipbutton',
      hideNullifyButton: true,
      i18nLabel: 'OBPOS_LblShipAddr'
    },
    {
      kind: 'OB.UI.BillTo',
      target: 'filterSelectorButton_receiptProperties',
      popup: 'receiptPropertiesDialog',
      name: 'addressbillbutton',
      classes: 'obUiModalReceiptPropertiesImpl-newAttributes-addressbillbutton',
      hideNullifyButton: true,
      i18nLabel: 'OBPOS_LblBillAddr'
    },
    {
      kind: 'OB.UI.renderComboProperty',
      name: 'invoiceTermsBox',
      classes: 'obUiModalReceiptPropertiesImpl-newAttributes-invoiceTermsBox',
      modelProperty: 'invoiceTerms',
      i18nLabel: 'OBPOS_InvoiceTerms',
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: 'name',
      initComponents: function() {
        this.collection = new Backbone.Collection();
        this.setCollection(this.collection);
        _.each(
          OB.MobileApp.model.get('invoiceTerms'),
          function(invoiceTerm) {
            this.collection.add(new Backbone.Model(invoiceTerm));
          },
          this
        );
      },
      fetchDataFunction: function(args) {
        var me = this;
        setTimeout(function() {
          me.dataReadyFunction(me.collection, args);
        }, 0);
      }
    }
  ],

  closeCancelSelector: function(inSender, inEvent) {
    if (inEvent.target === 'filterSelectorButton_receiptProperties') {
      this.show();
    }
  },
  moveScrollDown: function(inSender, inEvent) {
    if (inEvent.target === 'filterSelectorButton_receiptProperties') {
      this.$.body.$.scroller.setScrollTop(
        this.$.body.$.scroller.getScrollTop() + inEvent.lineHeight
      );
      return true;
    }
  },
  updateFilterSelector: function(inSender, inEvent) {
    if (inEvent.selector.name === 'receiptProperties') {
      this.bubble('onChangeBusinessPartner', {
        businessPartner: inEvent.selector.businessPartner,
        target: 'order'
      });
      this.show();
    }
  },
  resetProperties: function() {
    var p, att;
    // reset all properties
    for (p in this.newAttributes) {
      if (this.newAttributes.hasOwnProperty(p)) {
        att = this.$.body.$.attributes.$['line_' + this.newAttributes[p].name].$
          .coreElementContainer.$[this.newAttributes[p].name];
        if (att && att.setValue) {
          att.setValue('');
        }
      }
    }
  },
  executeOnShow: function(isSender, inEvent) {
    var bp = this.model.get('bp'),
      p;
    this.waterfall('onSetModel', {
      model: this.model
    });
    if (bp && bp.get('locId') === bp.get('shipLocId')) {
      this.$.body.$.attributes.$.line_addressshipbutton.hide();
      this.$.body.$.attributes.$.line_addressbillbutton.$.labelLine.setContent(
        OB.I18N.getLabel('OBPOS_LblAddress')
      );
    } else {
      this.$.body.$.attributes.$.line_addressshipbutton.show();
      this.$.body.$.attributes.$.line_addressbillbutton.$.labelLine.setContent(
        OB.I18N.getLabel('OBPOS_LblBillAddr')
      );
    }
    if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
      this.$.body.$.attributes.$.line_ReceiptDeliveryMode.show();
      this.$.body.$.attributes.$.line_ReceiptDeliveryDate.show();
      this.$.body.$.attributes.$.line_ReceiptDeliveryTime.show();
    } else {
      this.$.body.$.attributes.$.line_ReceiptDeliveryMode.hide();
      this.$.body.$.attributes.$.line_ReceiptDeliveryDate.hide();
      this.$.body.$.attributes.$.line_ReceiptDeliveryTime.hide();
    }
    for (p in this.newAttributes) {
      if (this.newAttributes.hasOwnProperty(p)) {
        this.loadValue(this.newAttributes[p].modelProperty);
      }
    }
  },
  init: function(model) {
    this.model = model.get('order');
    this.model.on(
      'change',
      function() {
        var diff = this.model.changedAttributes(),
          att;
        for (att in diff) {
          if (diff.hasOwnProperty(att)) {
            this.loadValue(att);
          }
        }
      },
      this
    );

    this.model.on(
      'paymentAccepted',
      function() {
        this.resetProperties();
      },
      this
    );
  }
});
