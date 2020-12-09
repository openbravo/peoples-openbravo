/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.UI.ModalModulesInDev',
  kind: 'OB.UI.ModalInfo',
  classes: 'obUiModalModulesInDev',
  body: {
    classes: 'obUiModalModulesInDev-body',
    components: [
      {
        name: 'message',
        classes: 'obUiModalModulesInDev-body-message',
        content: ''
      },
      {
        name: 'link',
        classes: 'obUiModalModulesInDev-body-link',
        tag:
          'a href="http://wiki.openbravo.com/wiki/WebPOS_and_HTTPS" target="_blank"',
        content: '',
        showing: false
      }
    ]
  },
  executeOnShow: function() {
    var i18nBodyMessage, i18nHeader;
    if (!OB.UTIL.isHTTPSAvailable()) {
      i18nBodyMessage = 'OBPOS_NonSecureModalPopup';
      i18nHeader = 'OBPOS_NonSecureConnection';
      this.$.body.$.link.setContent(OB.I18N.getLabel('OBPOS_link'));
      this.$.body.$.link.setShowing(true);
    } else if (OB.UTIL.Debug.isDebug()) {
      this.$.body.$.link.setShowing(false);
      if (OB.UTIL.Debug.getDebugCauses().isInDevelopment) {
        i18nBodyMessage = 'OBPOS_modalModulesInDevBody';
        i18nHeader = 'OBPOS_ModulesInDevelopment';
      } else if (OB.UTIL.Debug.getDebugCauses().isTestEnvironment) {
        i18nBodyMessage = 'OBPOS_modalApplicationInTestEnvironmentBody';
        i18nHeader = 'OBPOS_ApplicationInTestEnvironment';
      } else {
        i18nBodyMessage = 'OBMOBC_Debug';
        i18nHeader = 'OBMOBC_Debug';
      }
    }
    this.$.body.$.message.setContent(OB.I18N.getLabel(i18nBodyMessage));
    this.setHeader(OB.I18N.getLabel(i18nHeader));
  }
});
