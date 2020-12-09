/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 *
 * Author Yogas Karnik
 *
 */
/* global enyo */

enyo.kind({
  name: 'OB.UI.ModalQuotationProductAttributesScroller.QuotationLines',
  classes: 'obUiModalQuotationProductAttributesScrollerQuotationLines',
  components: [
    {
      classes:
        'obUiModalQuotationProductAttributesScrollerQuotationLines-container1',
      components: [
        {
          name: 'productName',
          type: 'text',
          classes:
            'obUiModalQuotationProductAttributesScrollerQuotationLines-container1-productName',
          content: ''
        }
      ]
    },
    {
      classes:
        'obUiModalQuotationProductAttributesScrollerQuotationLines-container2',
      components: [
        {
          name: 'newAttribute',
          classes:
            'obUiModalQuotationProductAttributesScrollerQuotationLines-container2-newAttribute',
          components: [
            {
              kind: 'OB.UI.renderTextProperty',
              name: 'valueAttribute',
              classes:
                'obUiModalQuotationProductAttributesScrollerQuotationLines-newAttribute-valueAttribute',
              maxlength: '70',
              handlers: {
                oninput: 'blur'
              },
              blur: function() {
                this.bubble('onFieldChanged');
              },
              placeholder: 'Scan attribute'
            }
          ]
        }
      ]
    },
    {
      classes:
        'obUiModalQuotationProductAttributesScrollerQuotationLines-container3'
    }
  ]
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalQuotationProductAttributes',
  i18nHeader: 'OBPOS_QuotationProductAttributesDialogTitle',
  classses: 'obUiModalQuotationProductAttributes',
  autoDismiss: false,
  hideCloseButton: true,
  body: {
    kind: 'Scroller',
    classses: 'obUiModalQuotationProductAttributes-scroller',
    thumb: true,
    components: [
      {
        name: 'quotationLinesComponent',
        classses:
          'obUiModalQuotationProductAttributes-scroller-quotationLinesComponent'
      }
    ]
  },
  header: {
    classes: 'obUiModalQuotationProductAttributes-header'
  },
  handlers: {
    onFieldChanged: 'fieldChanged'
  },
  footer: {
    classes: 'obUiModalQuotationProductAttributes-footer',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        classes:
          'obUiModalQuotationProductAttributes-footer-obUiModalDialogButton1',
        i18nContent: 'OBMOBC_LblOk',
        tap: function() {
          this.owner.owner.saveAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes:
          'obUiModalQuotationProductAttributes-footer-obUiModalDialogButton2',
        i18nContent: 'OBPOS_LblClear',
        tap: function() {
          this.owner.owner.clearAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes:
          'obUiModalQuotationProductAttributes-footer-obUiModalDialogButton3',
        i18nContent: 'OBMOBC_LblCancel',
        tap: function() {
          this.owner.owner.cancelAction();
        }
      }
    ]
  },
  clearAction: function() {
    var me = this,
      i,
      line = me.args.lines;
    for (i = 0; i < line.length; i++) {
      me.$.body.$.quotationLinesComponent.$[
        'quotationLine' + i
      ].$.valueAttribute.setValue(null);
      me.$.body.$.quotationLinesComponent.$[
        'quotationLine' + i
      ].$.valueAttribute.addClass(
        'obUiModalQuotationProductAttributes-quotationLinesComponent-quotationLine_clear'
      );
    }
    me.$.footer.$.modalDialogButton.setDisabled(true);
    return;
  },
  cancelAction: function() {
    var me = this,
      lines = me.args.lines,
      order = me.args.quotationProductAttribute;
    order.deleteOrder(lines);
    this.hide();
    return;
  },
  saveAction: function() {
    var me = this,
      lines = me.args.lines,
      order = me.args.quotationProductAttribute,
      lineIndex,
      inpAttribute;
    lineIndex = 0;
    lines.forEach(function(theLine) {
      inpAttribute = me.$.body.$.quotationLinesComponent.$[
        'quotationLine' + lineIndex
      ].$.valueAttribute.getValue();
      if (inpAttribute) {
        theLine.set('attributeValue', inpAttribute);
        order.save();
      }
      lineIndex++;
    });

    order.trigger('orderCreatedFromQuotation');
    this.hide();
    return;
  },
  fieldChanged: function(inSender, inEvent) {
    var me = this,
      lines = me.args.lines,
      enteredAttribute,
      inpAttribute,
      lineIndex,
      focusIndex;
    lineIndex = 0;
    lines.forEach(function(theLine) {
      enteredAttribute = false;
      inpAttribute = me.$.body.$.quotationLinesComponent.$[
        'quotationLine' + lineIndex
      ].$.valueAttribute.getValue();
      if (inpAttribute) {
        enteredAttribute = true;
        focusIndex = lines.length === 0 ? 0 : lineIndex + 1;
        if (focusIndex < lines.length) {
          me.$.body.$.quotationLinesComponent.$[
            'quotationLine' + focusIndex
          ].$.valueAttribute.focus();
        }
      } else {
        enteredAttribute = false;
      }
      lineIndex++;
    });
    if (enteredAttribute) {
      me.$.footer.$.modalDialogButton.setDisabled(false);
    }
    return true;
  },
  executeOnShow: function() {
    var me = this,
      lines = me.args.lines,
      i;
    me.setHeader(OB.I18N.getLabel('OBPOS_QuotationProductAttributeDesc'));
    i = 0;
    me.$.body.$.quotationLinesComponent.destroyComponents();
    lines.forEach(function(theLine) {
      var quotationLine = me.$.body.$.quotationLinesComponent.createComponent({
        kind: 'OB.UI.ModalQuotationProductAttributesScroller.QuotationLines',
        classes:
          'obUiModalQuotationProductAttributes-quotationLinesComponent-quotationLine',
        name: 'quotationLine' + i
      });
      quotationLine.$.valueAttribute.focus();
      quotationLine.$.productName.setContent(
        theLine.get('product').get('_identifier')
      );
      i++;
    });
    me.$.footer.$.modalDialogButton.setDisabled(true);
    me.$.body.render();
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});
