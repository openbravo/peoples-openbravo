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
      if (
        !OB.UTIL.isNullOrUndefined(validSafeBox.userId) &&
        validSafeBox.userId !== OB.MobileApp.model.usermodel.get('id')
      ) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_SafeBoxAssignedToOtherUser')
        );
        return;
      }

      // At this point everything is ok, continue flow
      OB.UTIL.localStorage.setItem(
        'currentSafeBox',
        JSON.stringify(validSafeBox)
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
