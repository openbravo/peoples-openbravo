/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $ */

enyo.kind({
  name: 'OB.UI.ModalReceiptLinesProperties',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  header: OB.I18N.getLabel('OBPOS_ReceiptLinePropertiesDialogTitle'),
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
      kind: 'OB.UI.ReceiptPropertiesDialogApply'
    }, {
      kind: 'OB.UI.ReceiptPropertiesDialogCancel'
    }]
  },
  loadValue: function (mProperty, component) {
    this.waterfall('onLoadValue', {
      order: this.currentLine,
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
    label: OB.I18N.getLabel('OBPOS_LblDescription')
  }]
});

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OB.UI.ValidateAction',
  header: '',
  isDefaultAction: true,
  bodyContent: {
    name : 'message',
    content: ''
  },
  executeOnShow: function () {
    this.$.header.setContent(this.args.header);
    this.$.bodyContent.$.message.setContent(this.args.message);
  }
});

