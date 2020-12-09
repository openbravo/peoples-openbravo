/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* global enyo */
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
        name: 'label',
        content: '',
        classes: 'obUiModalProductAttributes-body-label'
      },
      {
        kind: 'OB.UI.FormElement',
        name: 'formElementValueAttribute',
        classes:
          'obUiFormElement_dataFilter obUiModalProductAttributes-body-formElementValueAttribute',
        coreElement: {
          name: 'valueAttribute',
          kind: 'OB.UI.FormElement.Input',
          attributes: {
            //Allowed, it is not a style attribute
            maxlength: 190
          },
          selectOnFocus: true,
          isFirstFocus: true,
          i18nLabel: 'OBMOBC_LblValue',
          classes: 'obUiModalProductAttributes-body-valueAttribute'
        }
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons obUiModalProductAttributes-footer',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-clearButton',
        i18nContent: 'OBPOS_LblClear',
        tap: function() {
          this.owner.owner.clearAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-cancelButton',
        i18nContent: 'OBMOBC_LblCancel',
        tap: function() {
          this.owner.owner.cancelAction();
        }
      },
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalProductAttributes-footer-okButton',
        i18nContent: 'OBMOBC_LblOk',
        isDefaultAction: true,
        tap: function() {
          this.owner.owner.saveAction();
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
    var inpAttributeValue = this.$.body.$.formElementValueAttribute.coreElement.getValue();
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
    this.$.body.$.formElementValueAttribute.coreElement.setValue(null);
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
    this.$.body.$.formElementValueAttribute.coreElement.setValue(null);
  },
  executeOnShow: function() {
    if (this.args.options.attSetInstanceDesc) {
      this.$.body.$.formElementValueAttribute.coreElement.setValue(
        this.args.options.attSetInstanceDesc
      );
    } else if (this.args.options.attributeValue) {
      this.$.body.$.formElementValueAttribute.coreElement.setValue(
        this.args.options.attributeValue
      );
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.body.$.label.setContent(
      OB.I18N.getLabel('OBPOS_ProductAttributeValueDialogTitleDesc')
    );
  }
});
