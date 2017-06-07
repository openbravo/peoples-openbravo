/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB, moment, enyo */

enyo.kind({
  name: 'OB.UI.ModalQuotationProductAttributesScroller.QuotationLines',
  components: [{
    classes: 'properties-label',
    name: 'productName',
    type: 'text',
    style: 'font-size: 17px;margin-top:4px'
  }, {
    kind: 'OB.UI.renderTextProperty',
    name: 'valueAttribute',
    maxlength: '70',
    style: 'width: 55%',
    handlers: {
      oninput: 'blur'
    },
    blur: function () {
      this.bubble('onFieldChanged');
    },
    placeholder: 'Scan attribute'
  }, {
    style: 'clear: both'
  }]
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalQuotationProductAttributes',
  i18nHeader: 'OBPOS_QuotationProductAttributesDialogTitle',
  style: 'width: 700px;',
  autoDismiss: false,
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'quotationLinesComponent'
    }]
  },
  header: {

  },
  handlers: {
    onFieldChanged: 'fieldChanged'
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      i18nContent: 'OBMOBC_LblOk',
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
  clearAction: function () {
    var me = this,
        i, line = me.args.lines;
    for (i = 0; i < line.length; i++) {
      me.$.bodyContent.$.quotationLinesComponent.$['quotationLine' + i].$.valueAttribute.setValue(null);
      me.$.bodyContent.$.quotationLinesComponent.$['quotationLine' + i].$.valueAttribute.addStyles('background-color: none; width: 55%');
    }
    me.$.bodyButtons.$.modalDialogButton.setDisabled(true);
    return;
  },
  cancelAction: function () {
    this.hide();
    return;
  },
  saveAction: function () {
    var me = this,
        lines = me.args.lines,
        order = me.args.quotationProductAttribute,
        lineIndex, inpAttribute;
    lineIndex = 0;
    lines.forEach(function (theLine) {
      inpAttribute = me.$.bodyContent.$.quotationLinesComponent.$['quotationLine' + lineIndex].$.valueAttribute.getValue();
      if (inpAttribute) {
        theLine.set('attributeValue', inpAttribute);
        order.save();
      }
      lineIndex++;
    });

    me.args.quotationProductAttribute.trigger('orderCreatedFromQuotation');
    this.hide();
    return;
  },
  fieldChanged: function (inSender, inEvent) {
    var me = this,
        lines = me.args.lines,
        order = me.args.quotationProductAttribute,
        enteredAttribute, inpAttribute, lineIndex, focusIndex;
    lineIndex = 0;
    lines.forEach(function (theLine) {
      enteredAttribute = false;
      inpAttribute = me.$.bodyContent.$.quotationLinesComponent.$['quotationLine' + lineIndex].$.valueAttribute.getValue();
      if (inpAttribute) {
        enteredAttribute = true;
        focusIndex = lines.length === 0 ? 0 : lineIndex + 1;
        if (focusIndex < lines.length) {
          me.$.bodyContent.$.quotationLinesComponent.$['quotationLine' + focusIndex].$.valueAttribute.focus();
        }
      } else {
        enteredAttribute = false;
      }
      lineIndex++;
    });
    if (enteredAttribute) {
      me.$.bodyButtons.$.modalDialogButton.setDisabled(false);
    }
    return true;
  },
  executeOnShow: function () {
    var me = this,
        lines = me.args.lines,
        i;
    me.$.header.$.headerTitle.setContent(OB.I18N.getLabel('OBPOS_QuotationProductAttributeDesc'));
    me.$.header.$.headerTitle.addStyles('font-size: 24px');
    i = 0;
    me.$.bodyContent.$.quotationLinesComponent.destroyComponents();
    lines.forEach(function (theLine) {
      var quotationLine = me.$.bodyContent.$.quotationLinesComponent.createComponent({
        kind: 'OB.UI.ModalQuotationProductAttributesScroller.QuotationLines',
        name: 'quotationLine' + i
      });
      quotationLine.$.valueAttribute.focus();
      quotationLine.$.productName.setContent(theLine.attributes.product.attributes._identifier);
      i++;
    });
    me.$.bodyButtons.$.modalDialogButton.setDisabled(true);
    this.$.headerCloseButton.hide();
    me.$.bodyContent.render();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.header.createComponent({
      components: [{
        name: 'headerTitle',
        type: 'text'
      }, {
        name: 'documentno',
        type: 'text'
      }]
    });
  }
});
OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OB.UI.ModalQuotationProductAttributes',
  name: 'modalQuotationProductAttributes'
});