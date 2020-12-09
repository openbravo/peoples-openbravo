/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalMultiOrdersLayaway',
  classes: 'obUiModalMultiOrdersLayaway',
  executeOnShow: function() {
    this.$.body.$.formElementBtnModalMultiSearchInput.coreElement.setValue('');
  },
  i18nHeader: 'OBPOS_MultiOrdersLayawayHeader',
  body: {
    classes: 'obUiModalMultiOrdersLayaway-body',
    components: [
      {
        name: 'label',
        content: '',
        classes: 'obUiModalMultiOrdersLayaway-body-label'
      },
      {
        kind: 'OB.UI.FormElement',
        name: 'formElementBtnModalMultiSearchInput',
        classes:
          'obUiFormElement_dataFilter obUiModalMultiOrdersLayaway-body-formElementBtnModalMultiSearchInput',
        coreElement: {
          name: 'btnModalMultiSearchInput',
          kind: 'OB.UI.SearchInput',
          i18nLabel: 'OBPOS_AmountOfCash',
          classes: 'obUiModalMultiOrdersLayaway-body-btnModalMultiSearchInput'
        }
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons obUiModalMultiOrdersLayaway-footer',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        classes: 'obUiModalMultiOrdersLayaway-footer-cancel',
        i18nLabel: 'OBMOBC_LblCancel',
        tap: function() {
          if (this.disabled === false) {
            this.doHideThisPopup();
          }
        }
      },
      {
        kind: 'OB.UI.btnApplyMultiLayaway',
        isDefaultAction: true,
        classes: 'obUiModalMultiOrdersLayaway-footer-obUiBtnApplyMultiLayaway'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.body.$.label.setContent(
      OB.I18N.getLabel('OBPOS_MultiOrdersLayaway') +
        ' (' +
        OB.I18N.getLabel('OBPOS_cannotBeUndone') +
        ')'
    );
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnApplyMultiLayaway',
  classes: 'obUibtnApplyMultiLayaway',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
  },
  tap: function() {
    var me = this,
      amount = OB.DEC.Zero,
      total = OB.DEC.Zero,
      tmp,
      currentOrder;
    currentOrder = this.model
      .get('multiOrders')
      .get('multiOrdersList')
      .get(this.owner.owner.args.id);
    if (
      this.popup.$.body.$.formElementBtnModalMultiSearchInput.coreElement
        .getValue()
        .indexOf('%') !== -1
    ) {
      try {
        tmp = this.popup.$.body.$.formElementBtnModalMultiSearchInput.coreElement
          .getValue()
          .replace('%', '');
        amount = OB.DEC.div(OB.DEC.mul(currentOrder.getPending(), tmp), 100);
      } catch (ex) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_notValidInput_header'),
          OB.I18N.getLabel('OBPOS_notValidQty')
        );
        return;
      }
    } else {
      try {
        if (
          !OB.I18N.isValidNumber(
            this.popup.$.body.$.formElementBtnModalMultiSearchInput.coreElement.getValue()
          )
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_notValidInput_header'),
            OB.I18N.getLabel('OBPOS_notValidQty')
          );
          return;
        } else {
          var tmpAmount = this.popup.$.body.$.formElementBtnModalMultiSearchInput.coreElement.getValue();
          while (tmpAmount.indexOf(OB.Format.defaultGroupingSymbol) !== -1) {
            tmpAmount = tmpAmount.replace(OB.Format.defaultGroupingSymbol, '');
          }
          amount = OB.I18N.parseNumber(tmpAmount);
        }
      } catch (exc) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_notValidInput_header'),
          OB.I18N.getLabel('OBPOS_notValidQty')
        );
        return;
      }
    }
    if (_.isNaN(amount)) {
      currentOrder.setOrderType(null, 0);
      currentOrder.unset('amountToLayaway');
      currentOrder.trigger('amountToLayaway');
      this.doHideThisPopup();
      return;
    }
    if (amount === 0) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_notValidInput_header'),
        OB.I18N.getLabel('OBPOS_amtGreaterThanZero'),
        [
          {
            isConfirmButton: true,
            label: OB.I18N.getLabel('OBMOBC_LblOk')
          }
        ]
      );
      return;
    }
    var decimalAmount = OB.DEC.toBigDecimal(amount);
    if (decimalAmount.scale() > OB.DEC.getScale()) {
      OB.UTIL.showWarning(
        OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [amount])
      );
      OB.warn(
        'Amount to layaway ' +
          OB.I18N.getLabel('OBPOS_NotValidCurrencyAmount', [amount])
      );
      return;
    } else {
      amount = OB.DEC.toNumber(decimalAmount);
    }
    total = currentOrder.get('gross');
    if (
      OB.DEC.compare(
        OB.DEC.sub(total, OB.DEC.add(amount, currentOrder.get('payment')))
      ) < 0 ||
      (OB.DEC.compare(OB.DEC.sub(amount, total)) > 0 ||
        OB.DEC.compare(amount) < 0)
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_notValidInput_header'),
        OB.I18N.getLabel('OBPOS_notValidQty')
      );
      OB.warn(
        enyo.format(
          'Amount to layaway: Amount %s is greater than receipt amount',
          amount
        )
      );
      this.doHideThisPopup();
      return;
    }
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreMultiOrderLayaway',
      {
        amount: amount,
        order: currentOrder,
        context: this
      },
      function(args) {
        if (args.cancellation) {
          me.doHideThisPopup();
          return;
        }
        currentOrder.set('amountToLayaway', args.amount);
        currentOrder.setOrderType(null, 2);
        currentOrder.trigger('amountToLayaway');
        me.doHideThisPopup();
      }
    );
  },
  init: function(model) {
    this.model = model;
  }
});
