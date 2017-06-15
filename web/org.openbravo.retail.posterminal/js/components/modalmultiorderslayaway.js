/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalMultiOrdersLayaway',
  executeOnShow: function () {
    this.$.bodyButtons.$.btnModalMultiSearchInput.setValue('');
  },
  bodyContent: {
    i18nContent: 'OBPOS_MultiOrdersLayaway' // TODO: add this as part of the message + '\n' + OB.I18N.getLabel('OBPOS_cannotBeUndone')
  },
  bodyButtons: {
    components: [{
      name: 'btnModalMultiSearchInput',
      kind: 'OB.UI.SearchInput'
    }, {
      kind: 'OB.UI.btnApplyMultiLayaway'
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBPOS_MultiOrdersLayawayHeader');
    this.inherited(arguments);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnApplyMultiLayaway',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function () {
    var amount = OB.DEC.Zero,
        total = OB.DEC.Zero,
        tmp, currentOrder;
    currentOrder = this.model.get('multiOrders').get('multiOrdersList').get(this.owner.owner.args.id);
    if (this.owner.$.btnModalMultiSearchInput.getValue().indexOf('%') !== -1) {
      try {
        tmp = this.owner.$.btnModalMultiSearchInput.getValue().replace('%', '');
        amount = OB.DEC.div(OB.DEC.mul(currentOrder.getPending(), tmp), 100);
      } catch (ex) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_notValidInput_header'), OB.I18N.getLabel('OBPOS_notValidQty'));
        return;
      }
    } else {
      try {
        if (!OB.I18N.isValidNumber(this.owner.$.btnModalMultiSearchInput.getValue())) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_notValidInput_header'), OB.I18N.getLabel('OBPOS_notValidQty'));
          return;
        } else {
          var tmpAmount = this.owner.$.btnModalMultiSearchInput.getValue();
          while (tmpAmount.indexOf(OB.Format.defaultGroupingSymbol) !== -1) {
            tmpAmount = tmpAmount.replace(OB.Format.defaultGroupingSymbol, '');
          }
          amount = OB.I18N.parseNumber(tmpAmount);
        }
      } catch (exc) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_notValidInput_header'), OB.I18N.getLabel('OBPOS_notValidQty'));
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
    amount = OB.DEC.toNumber(OB.DEC.toBigDecimal(amount));
    if (amount === 0) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_notValidInput_header'), OB.I18N.getLabel('OBPOS_amtGreaterThanZero'), [{
        isConfirmButton: true,
        label: OB.I18N.getLabel('OBMOBC_LblOk')
      }]);
      return;
    }
    total = currentOrder.get('gross');
    if ((OB.DEC.compare(OB.DEC.sub(total, OB.DEC.add(amount, currentOrder.get('payment')))) <= 0) || (OB.DEC.compare(OB.DEC.sub(amount, total)) > 0 || OB.DEC.compare(amount) < 0)) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_notValidInput_header'), OB.I18N.getLabel('OBPOS_notValidQty'));
      this.doHideThisPopup();
      return;
    }
    currentOrder.set('amountToLayaway', amount);
    currentOrder.setOrderType(null, 2);
    currentOrder.trigger('amountToLayaway');
    this.doHideThisPopup();
  },
  init: function (model) {
    this.model = model;
  }
});