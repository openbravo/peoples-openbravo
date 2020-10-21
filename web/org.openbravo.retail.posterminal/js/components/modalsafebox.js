/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ModalSafeBox_buttonCheck',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obUiModalSafeBoxButtonCheck',
  i18nContent: 'OBPOS_LblSafeBoxCheckButton',
  isDefaultAction: true,
  events: {
    onCheckSafeBox: ''
  },
  tap: function() {
    this.doCheckSafeBox();
  }
});

enyo.kind({
  name: 'OB.UI.ModalSafeBox_buttonSkip',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obUiModalSafeBoxButtonSkip',
  i18nContent: 'OBPOS_LblSafeBoxSkipButton',
  events: {
    onSkipSafeBox: ''
  },
  tap: function() {
    this.doSkipSafeBox();
  }
});

enyo.kind({
  name: 'OB.UI.ModalSafeBox',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalSafeBox',
  i18nHeader: 'OBPOS_ModalSafeBox',
  bodyClass: 'obUiModalSafeBox-body',
  footerClass: 'obUiModalSafeBox-footer',
  hideCloseButton: true,
  autoDismiss: false,
  handlers: {
    onCheckSafeBox: 'doCheckSafeBox',
    onSkipSafeBox: 'doSkipSafeBox'
  },
  body: {
    components: [
      {
        name: 'safeBoxInput',
        kind: 'enyo.Input',
        classes: 'obUiModalSafeBox-body-safeBoxInput',
        type: 'text'
      }
    ]
  },
  footer: {
    components: [
      {
        name: 'checkButton',
        kind: 'OB.UI.ModalSafeBox_buttonCheck',
        classes: 'obUiModalSafeBox-footer-checkButton'
      },
      {
        name: 'skipButton',
        kind: 'OB.UI.ModalSafeBox_buttonSkip',
        classes: 'obUiModalSafeBox-footer-skipButton'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  doCheckSafeBox: function() {
    if (
      !OB.UTIL.localStorage.getItem('safeBoxes') ||
      JSON.parse(OB.UTIL.localStorage.getItem('safeBoxes')).length <= 0
    ) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NoSafeBoxInformation'));
      this.doSkipSafeBox();
      return;
    }

    const searchKey = this.$.body.$.safeBoxInput.getValue();
    if (_.isEmpty(searchKey)) {
      this.doSkipSafeBox();
    } else {
      const safeboxList = JSON.parse(OB.UTIL.localStorage.getItem('safeBoxes'));
      const validSafeBox = safeboxList.find(safebox => {
        return searchKey === safebox.searchKey;
      });
      // If search key is not found, raise a warning an do nothing
      if (OB.UTIL.isNullOrUndefined(validSafeBox)) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidSafeBoxSearchKey'));
        return;
      }

      // In case the safe box has a user asigned, check if it is the same as logged
      // or it is a safe box manager
      if (
        !OB.UTIL.isNullOrUndefined(validSafeBox.userId) &&
        validSafeBox.userId !== OB.MobileApp.model.usermodel.get('id') &&
        !OB.MobileApp.model.hasPermission(
          'OBPOS_approval.manager.safebox',
          true
        )
      ) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_SafeBoxAssignedToOtherUser')
        );
        return;
      }

      let safeBoxConfigured = true;
      validSafeBox.paymentMethods.forEach(function(paymentMethod) {
        for (let key in OB.MobileApp.model.paymentnames) {
          const payment = OB.MobileApp.model.paymentnames[key];
          if (
            safeBoxConfigured &&
            payment.paymentMethod.issafebox &&
            payment.paymentMethod.paymentMethod ===
              paymentMethod.paymentMethodId &&
            payment.paymentMethod.currency === paymentMethod.currency &&
            payment.payment.financialAccount ===
              paymentMethod.financialAccountId
          ) {
            safeBoxConfigured = false;
          }
        }
      });
      if (!safeBoxConfigured) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_PaymentMethodSafeBoxNotConfigured')
        );
        return;
      }

      // At this point everything is ok, continue flow
      OB.UTIL.localStorage.setItem(
        'currentSafeBox',
        JSON.stringify(validSafeBox)
      );
      // Open drawer to insert the Safe Box
      OB.POS.hwserver.openDrawer(
        false,
        OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
      );

      this.hide();
      this.args.callback();
    }
  },
  doSkipSafeBox: function() {
    OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_SafeBoxPaymentsDeactivated'));
    this.hide();
    this.args.callback();
  },
  executeOnShow: function() {
    this.$.body.$.safeBoxInput.attributes.placeholder = OB.I18N.getLabel(
      'OBPOS_SafeBoxSearchKey'
    );
  }
});
