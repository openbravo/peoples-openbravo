/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _, $ */

enyo.kind({
  name: 'OB.UI.ModalReceiptLinesProperties',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  executeOnShow: function () {
    if (this.currentLine) {
      var diff = this.propertycomponents;
      var att, receiptLineDescription, receiptLineDescriptionControl, receiptLineDescriptionNewAttribute;
      for (att in diff) {
        if (diff.hasOwnProperty(att)) {
          this.loadValue(att, diff[att]);
          if (diff[att].owner.$.receiptLineDescription) {
            receiptLineDescription = diff[att].owner.$.receiptLineDescription;
            receiptLineDescriptionControl = diff[att].owner.owner.$.control.id;
            receiptLineDescriptionNewAttribute = diff[att].owner.owner.$.newAttribute.id;
          }
        }
      }
      setTimeout(function () {
        receiptLineDescription.focus();
        document.getElementById(receiptLineDescriptionControl).style.cssText = 'border: 1px solid #F0F0F0; float: left; width: 60%;';
        document.getElementById(receiptLineDescriptionNewAttribute).style.cssText = 'width: 100%';
      }, 200);
    }
    this.autoDismiss = true;
    if (this && this.args && this.args.autoDismiss === false) {
      this.autoDismiss = false;
    }
  },
  executeOnHide: function () {
    if (this.args && this.args.requiredFiedls && this.args.requiredFieldNotPresentFunction) {
      var smthgPending = _.find(this.args.requiredFiedls, function (fieldName) {
        return OB.UTIL.isNullOrUndefined(this.currentLine.get(fieldName));
      }, this);
      if (smthgPending) {
        this.args.requiredFieldNotPresentFunction(this.currentLine, smthgPending);
      }
    }
  },
  i18nHeader: 'OBPOS_ReceiptLinePropertiesDialogTitle',
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'attributes'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ReceiptPropertiesDialogApply',
      name: 'receiptLinePropertiesApplyBtn'
    }, {
      kind: 'OB.UI.ReceiptPropertiesDialogCancel',
      name: 'receiptLinePropertiesCancelBtn'
    }]
  },
  loadValue: function (mProperty, component) {
    this.waterfall('onLoadValue', {
      model: this.currentLine,
      modelProperty: mProperty
    });
    // Make it visible or not...
    if (component.showProperty) {
      component.showProperty(this.currentLine, function (value) {
        component.owner.owner.setShowing(value);
      });
    } // else make it visible...
  },
  applyChanges: function (inSender, inEvent) {
    var diff, att, result = true;
    diff = this.propertycomponents;
    for (att in diff) {
      if (diff.hasOwnProperty(att)) {
        if (diff[att].owner.owner.getShowing()) {
          result = result && diff[att].applyValue(this.currentLine);
        }
      }
    }
    return result;
  },
  validationMessage: function (args) {
    this.owner.doShowPopup({
      popup: 'modalValidateAction',
      args: args
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
    this.setHeader(OB.I18N.getLabel(this.i18nHeader));

    this.propertycomponents = {};

    enyo.forEach(this.newAttributes, function (natt) {
      var editline = this.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.PropertyEditLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      });
      this.propertycomponents[natt.modelProperty] = editline.propertycomponent;
      this.propertycomponents[natt.modelProperty].propertiesDialog = this;
    }, this);
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').get('lines').on('selected', function (lineSelected) {
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
    }, this);
  }
});


enyo.kind({
  name: 'OB.UI.ModalReceiptLinesPropertiesImpl',
  kind: 'OB.UI.ModalReceiptLinesProperties',
  newAttributes: [{
    kind: 'OB.UI.renderTextProperty',
    name: 'receiptLineDescription',
    modelProperty: 'description',
    i18nLabel: 'OBPOS_LblDescription',
    maxLength: 255
  }, {
    kind: 'OB.UI.renderComboProperty',
    name: 'priceReason',
    modelProperty: 'oBPOSPriceModificationReason',
    i18nLabel: 'OBPOS_PriceModification',
    retrievedPropertyForValue: 'id',
    retrievedPropertyForText: '_identifier',
    init: function (model) {
      this.model = model;
      this.collection = new Backbone.Collection();
      this.$.renderCombo.setCollection(this.collection);
      var i = 0;
      for (i; i < OB.MobileApp.model.get('priceModificationReasons').length; i++) {
        model = new Backbone.Model(OB.MobileApp.model.get('priceModificationReasons')[i]);
        this.collection.add(model);
      }
    },
    loadValue: function (inSender, inEvent) {
      if (inEvent.modelProperty === this.modelProperty) {
        if (inEvent.model.get('oBPOSPriceModificationReason')) {
          var i;
          for (i = 0; i < OB.MobileApp.model.get('priceModificationReasons').length; i++) {
            if (inEvent.model.get('oBPOSPriceModificationReason') === OB.MobileApp.model.get('priceModificationReasons')[i].id) {
              this.$.renderCombo.setSelected(i);
              break;
            }
          }
        } else {
          this.$.renderCombo.setSelected(0);
        }
      }
    },
    applyValue: function (inSender, inEvent) {
      inSender.set(this.modelProperty, this.$.renderCombo.getValue());
      return true;
    },
    showProperty: function (orderline, callback) {
      if (orderline.get('oBPOSPriceModificationReason') && OB.MobileApp.model.get('priceModificationReasons').length > 0) {
        callback(true);
      } else {
        callback(false);
      }
    }
  }]
});

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OB.UI.ValidateAction',
  header: '',
  isDefaultAction: true,
  bodyContent: {
    name: 'message',
    content: ''
  },
  executeOnShow: function () {
    this.$.header.setContent(this.args.header);
    this.$.bodyContent.$.message.setContent(this.args.message);
  }
});