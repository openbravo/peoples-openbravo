/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  name: 'OB.UI.ModalReceiptLinesProperties',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalReceiptLinesProperties',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  executeOnShow: function() {
    if (this.currentLine || this.args.forceLoad) {
      var diff = this.propertycomponents;
      var att,
        receiptLineDescription,
        receiptLineDescriptionControl,
        receiptLineDescriptionCoreElement;
      for (att in diff) {
        if (diff.hasOwnProperty(att)) {
          this.loadValue(att, diff[att]);
          if (diff[att].owner.$.receiptLineDescription) {
            receiptLineDescription = diff[att].owner.$.receiptLineDescription;
            receiptLineDescriptionControl = diff[att].owner.owner.$.control.id;
            receiptLineDescriptionCoreElement =
              diff[att].owner.owner.$.coreElementContainer.id;
          }
        }
      }
      setTimeout(function() {
        receiptLineDescription.focus();
        document
          .getElementById(receiptLineDescriptionControl)
          .classList.add(
            'obUiModalReceiptLinesPropertiesImpl-receiptLineDescription-control'
          );
        document
          .getElementById(receiptLineDescriptionCoreElement)
          .classList.add(
            'obUiModalReceiptLinesPropertiesImpl-receiptLineDescription-coreElement'
          );
      }, 200);
    }
    this.autoDismiss = true;
    if (this && this.args && this.args.autoDismiss === false) {
      this.autoDismiss = false;
    }
  },
  executeOnHide: function() {
    if (this.args.callback) {
      this.args.callback({ ...this.formData });
    }
    if (
      this.args &&
      this.args.requiredFiedls &&
      this.args.requiredFieldNotPresentFunction
    ) {
      var smthgPending = _.find(
        this.args.requiredFiedls,
        function(fieldName) {
          return OB.UTIL.isNullOrUndefined(this.currentLine.get(fieldName));
        },
        this
      );
      if (smthgPending) {
        this.args.requiredFieldNotPresentFunction(
          this.currentLine,
          smthgPending
        );
      }
    }
  },
  i18nHeader: 'OBPOS_ReceiptLinePropertiesDialogTitle',
  body: {
    kind: 'Scroller',
    classes: 'obUiModalReceiptLinesProperties-body-scroller',
    thumb: true,
    components: [
      {
        name: 'attributes',
        classes: 'obUiModalReceiptLinesProperties-scroller-attributes'
      }
    ]
  },
  footer: {
    classes: 'obUiModalReceiptLinesProperties-footer',
    components: [
      {
        kind: 'OB.UI.ReceiptPropertiesDialogCancel',
        name: 'receiptLinePropertiesCancelBtn',
        classes:
          'obUiModalReceiptLinesProperties-footer-receiptLinePropertiesCancelBtn'
      },
      {
        kind: 'OB.UI.ReceiptPropertiesDialogApply',
        name: 'receiptLinePropertiesApplyBtn',
        classes:
          'obUiModalReceiptLinesProperties-footer-receiptLinePropertiesApplyBtn'
      }
    ]
  },
  loadValue: function(mProperty, component) {
    this.waterfall('onLoadValue', {
      model: this.currentLine,
      modelProperty: mProperty,
      extraParams: this.args
    });
    // Make it visible or not...
    if (component.showProperty) {
      component.showProperty(
        this.currentLine,
        function(value) {
          component.owner.owner.setShowing(value);
        },
        this.args
      );
    } // else make it visible...
  },
  applyChanges: function(inSender, inEvent) {
    var diff,
      att,
      result = true;
    diff = this.propertycomponents;
    this.formData = {};
    for (att in diff) {
      if (diff.hasOwnProperty(att)) {
        if (diff[att].owner.owner.getShowing()) {
          if (this.args.callback) {
            this.formData[diff[att].modelProperty] = diff[att].value;
            this.formData[diff[att].modelPropertyText] = diff[att].content;
          } else {
            result = result && diff[att].applyValue(this.currentLine);
          }
        }
      }
    }
    return result;
  },
  validationMessage: function(args) {
    this.owner.doShowPopup({
      popup: 'modalValidateAction',
      args: args
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.body.$.attributes;

    this.propertycomponents = {};

    enyo.forEach(
      this.newAttributes,
      function(natt) {
        var editline = this.$.body.$.attributes.createComponent({
          kind: 'OB.UI.PropertyEditLine',
          name: 'line_' + natt.name,
          classes:
            'obUiModalReceiptLinesProperties-scroller-attributes-obUiPropertyEditLine',
          coreElement: natt
        });
        this.propertycomponents[natt.modelProperty] = editline.coreElement;
        this.propertycomponents[natt.modelProperty].propertiesDialog = this;
      },
      this
    );
  },
  init: function(model) {
    this.model = model;
    this.model
      .get('order')
      .get('lines')
      .on(
        'selected',
        function(lineSelected) {
          var diff, att;
          this.currentLine = lineSelected;
          if (lineSelected) {
            diff = this.propertycomponents;
            for (att in diff) {
              if (diff.hasOwnProperty(att)) {
                this.loadValue(att, diff[att]);
              }
            }
          }
        },
        this
      );
  }
});

enyo.kind({
  name: 'OB.UI.ModalReceiptLinesPropertiesImpl',
  kind: 'OB.UI.ModalReceiptLinesProperties',
  classes: 'obUiModalReceiptLinesPropertiesImpl',
  newAttributes: [
    {
      kind: 'OB.UI.renderTextProperty',
      name: 'receiptLineDescription',
      classes:
        'obUiModalReceiptLinesPropertiesImpl-newAttributes-receiptLineDescription',
      modelProperty: 'description',
      i18nLabel: 'OBPOS_LblDescription',
      maxLength: 255
    },
    {
      kind: 'OB.UI.renderComboProperty',
      name: 'priceReason',
      classes: 'obUiModalReceiptLinesPropertiesImpl-newAttributes-priceReason',
      modelProperty: 'oBPOSPriceModificationReason',
      i18nLabel: 'OBPOS_PriceModification',
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: '_identifier',
      init: function(model) {
        this.model = model;
        this.collection = new Backbone.Collection();
        this.setCollection(this.collection);
        var i = 0;
        for (
          i;
          i < OB.MobileApp.model.get('priceModificationReasons').length;
          i++
        ) {
          model = new Backbone.Model(
            OB.MobileApp.model.get('priceModificationReasons')[i]
          );
          this.collection.add(model);
        }
      },
      loadValue: function(inSender, inEvent) {
        if (inEvent.modelProperty === this.modelProperty && inEvent.model) {
          if (inEvent.model.get('oBPOSPriceModificationReason')) {
            var i;
            for (
              i = 0;
              i < OB.MobileApp.model.get('priceModificationReasons').length;
              i++
            ) {
              if (
                inEvent.model.get('oBPOSPriceModificationReason') ===
                OB.MobileApp.model.get('priceModificationReasons')[i].id
              ) {
                this.setSelected(i);
                break;
              }
            }
          } else {
            this.setSelected(0);
          }
        }
      },
      applyValue: function(inSender, inEvent) {
        inSender.set(this.modelProperty, this.getValue());
        return true;
      },
      showProperty: function(orderline, callback) {
        if (
          orderline &&
          orderline.get('oBPOSPriceModificationReason') &&
          OB.MobileApp.model.get('priceModificationReasons').length > 0
        ) {
          callback(true);
        } else {
          callback(false);
        }
      }
    }
  ]
});

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OB.UI.ValidateAction',
  header: '',
  classes: 'obUiValidateAction',
  isDefaultAction: true,
  i18nBody: 'message',
  executeOnShow: function() {
    this.setHeader(this.args.header);
    this.$.body.$.message.setContent(this.args.message);
  }
});
