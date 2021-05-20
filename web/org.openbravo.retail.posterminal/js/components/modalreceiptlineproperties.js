/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.UI.ModalReceiptLinesProperties',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalReceiptLinesProperties',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  executeOnShow: function() {
    if (this.currentLine || this.args.forceLoad) {
      let att,
        diff = this.propertycomponents;
      for (att in diff) {
        if (Object.prototype.hasOwnProperty.call(diff, att)) {
          this.loadValue(att, diff[att]);
        }
      }
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
    this.formData = {};
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
      model: this.args && this.args.forceLoad ? undefined : this.currentLine,
      modelProperty: mProperty,
      extraParams: this.args
    });
    // Make it visible or not...
    if (component.showProperty) {
      component.showProperty(
        this.currentLine,
        function(value) {
          component.setShowing(value);
          component.owner.owner.setShowing(value);
        },
        this.args
      );
    } else {
      component.setShowing(true);
      component.owner.owner.setShowing(true);
    }
  },
  applyChanges: function(inSender, inEvent) {
    var diff,
      att,
      result = true;
    diff = this.propertycomponents;
    this.formData = {};
    for (att in diff) {
      if (Object.prototype.hasOwnProperty.call(diff, att)) {
        if (diff[att].owner.owner.getShowing()) {
          if (this.args.callback) {
            this.formData[diff[att].modelProperty] = diff[att].getValue
              ? diff[att].getValue()
              : diff[att].value;
            this.formData[diff[att].modelPropertyText] = diff[att].getContent
              ? diff[att].getContent()
              : diff[att].content;
            if (
              diff[att].additionalProperties ||
              diff[att].container.additionalProperties
            ) {
              this.formData[
                diff[att].modelProperty + 'AdditionalProperties'
              ] = diff[att].additionalProperties
                ? diff[att].additionalProperties
                : diff[att].container.additionalProperties;
            }
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
              if (Object.prototype.hasOwnProperty.call(diff, att)) {
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
      maxlength: 255
    },
    {
      kind: 'OB.UI.renderComboProperty',
      name: 'priceReason',
      classes: 'obUiModalReceiptLinesPropertiesImpl-newAttributes-priceReason',
      modelProperty: 'oBPOSPriceModificationReason',
      i18nLabel: 'OBPOS_PriceModification',
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: '_identifier',
      collection: new Backbone.Collection(),
      init: function() {
        OB.MobileApp.model.get('priceModificationReasons').forEach(reason => {
          this.collection.add(new Backbone.Model(reason));
        }, this);
      },
      fetchDataFunction: function(args) {
        if (
          args &&
          args.model &&
          args.model.get('oBPOSPriceModificationReason')
        ) {
          this.dataReadyFunction(this.collection, args);
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
