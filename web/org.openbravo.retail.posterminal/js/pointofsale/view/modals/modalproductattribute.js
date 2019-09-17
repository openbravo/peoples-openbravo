/*
 ************************************************************************************
 * Copyright (C) 2017-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB, enyo */
enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalProductAttributes',
  classes: 'obUiModalProductAttributes',
  i18nHeader: 'OBPOS_ProductAttributeValueDialogTitle',
  autoDismiss: false,
  hideCloseButton: true,
  body: {
    classes: 'obUiModalProductAttributes-body',
    components: [
      {
        initComponents: function() {
          this.setContent(
            OB.I18N.getLabel('OBPOS_ProductAttributeValueDialogTitleDesc')
          );
        }
      },
      {
        kind: 'enyo.Input',
        type: 'text',
        attributes: {
          //Allowed, it is not a style attribute
          maxlength: 190
        },
        name: 'valueAttribute',
        classes: 'obUiModalProductAttributes-body-valueAttribute',
        selectOnFocus: true,
        isFirstFocus: true
      }
    ]
  },
  footer: {
    classes: 'obUiModalProductAttributes-footer',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-obUiModalDialogButton1',
        i18nContent: 'OBMOBC_LblOk',
        tap: function() {
          this.owner.owner.saveAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-obUiModalDialogButton2',
        i18nContent: 'OBPOS_LblClear',
        tap: function() {
          this.owner.owner.clearAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-obUiModalDialogButton3',
        i18nContent: 'OBMOBC_LblCancel',
        tap: function() {
          this.owner.owner.cancelAction();
        }
      }
    ]
  },
  /**
   * This method should be overriden to implement validation of attributes for specific cases
   */
  validAttribute: function(attribute) {
    return true;
  },
  saveAttribute: function(inSender, inEvent) {
    var inpAttributeValue = this.$.body.$.valueAttribute.getValue();
    inpAttributeValue = inpAttributeValue.replace(/\s+/, '');
    if (
      (this.validAttribute(inpAttributeValue) && inpAttributeValue) ||
      this.owner.model.get('order').get('orderType') === 2 ||
      this.owner.model.get('order').get('isLayaway')
    ) {
      this.args.callback(inpAttributeValue);
      this.hide();
    } else {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_NoAttributeValue')
      );
    }
  },
  clearAction: function() {
    this.$.body.$.valueAttribute.setValue(null);
    return;
  },
  cancelAction: function() {
    if (this.args.callback) {
      this.args.callback(null, true);
    }
    this.hide();
    return;
  },
  saveAction: function() {
    this.saveAttribute();
    return;
  },
  executeOnHide: function() {
    this.$.body.$.valueAttribute.setValue(null);
  },
  executeOnShow: function() {
    if (this.args.options.attSetInstanceDesc) {
      this.$.body.$.valueAttribute.setValue(
        this.args.options.attSetInstanceDesc
      );
    } else if (this.args.options.attributeValue) {
      this.$.body.$.valueAttribute.setValue(this.args.options.attributeValue);
    }
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});
