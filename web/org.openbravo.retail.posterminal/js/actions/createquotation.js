/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'createQuotation',
      permission: 'OBPOS_receipt.quotation',
      properties: {
        i18nContent: 'OBPOS_CreateQuotation'
      },
      isActive: function(view) {
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });
        return !isQuotation;
      },
      command: function(view) {
        if (
          OB.MobileApp.model.get('terminal').terminalType
            .documentTypeForQuotations
        ) {
          if (OB.MobileApp.model.hasPermission(this.permission)) {
            if (view.model.get('leftColumnViewManager').isMultiOrder()) {
              if (view.model.get('multiorders')) {
                view.model.get('multiorders').resetValues();
              }
              view.model.get('leftColumnViewManager').setOrderMode();
            }
            view.createQuotation();
          }
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationNoDocType'));
        }
      }
    })
  );
})();
