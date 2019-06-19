/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  name: 'OB.UI.ModalModulesInDev',
  kind: 'OB.UI.ModalInfo',
  classes: 'obUiModalModulesInDev',
  bodyContent: {
    classes: 'obUiModalModulesInDev-bodyContent',
    components: [
      {
        name: 'message',
        classes: 'obUiModalModulesInDev-bodyContent-message',
        content: ''
      },
      {
        name: 'link',
        classes: 'obUiModalModulesInDev-bodyContent-link',
        tag:
          'a href="http://wiki.openbravo.com/wiki/WebPOS_and_HTTPS" target="_blank"',
        content: '',
        showing: false
      }
    ]
  },
  executeOnShow: function() {
    if (!OB.UTIL.isHTTPSAvailable()) {
      this.$.bodyContent.$.message.setContent(
        OB.I18N.getLabel('OBPOS_NonSecureModalPopup')
      );
      this.$.bodyContent.$.link.setContent(OB.I18N.getLabel('OBPOS_link'));
      this.$.bodyContent.$.link.setShowing(true);
    } else if (OB.UTIL.Debug.isDebug()) {
      var ifInDevelopment = 'OBPOS_modalModulesInDevBody';
      var ifInTestEnvironment = 'OBPOS_modalApplicationInTestEnvironmentBody';
      var i18nLabel = 'OBMOBC_Debug';
      if (OB.UTIL.Debug.getDebugCauses().isInDevelopment) {
        i18nLabel = ifInDevelopment;
      } else if (OB.UTIL.Debug.getDebugCauses().isTestEnvironment) {
        i18nLabel = ifInTestEnvironment;
      }
      this.$.bodyContent.$.message.setContent(OB.I18N.getLabel(i18nLabel));
    }
  }
});
