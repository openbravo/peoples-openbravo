/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

(function() {
  enyo.kind({
    name: 'OBRDM.UI.MenuIssueSO',
    kind: 'OB.UI.MenuAction',
    classes: 'obrdmUiMenuIssueSO',
    permission: 'OBRDM_IssueSalesOrder',
    i18nLabel: 'OBRDM_LblIssueSalesOrder',
    events: {
      onShowPopup: ''
    },
    tap: function() {
      this.inherited(arguments);
      if (!OB.MobileApp.model.get('connectedToERP')) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline')
        );
        return;
      }
      OB.debug('Menu > Issue Sales Order');
      this.doShowPopup({
        popup: 'OBRDM_ModalOrderSelector'
      });
    },
    displayLogic: function() {
      if (
        OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true) &&
        OB.MobileApp.model.hasPermission('OBRDM_IssueSalesOrder', true)
      ) {
        this.show();
      } else {
        this.hide();
        return;
      }
    },
    init: function(model) {
      this.displayLogic();
    }
  });

  OB.OBPOSPointOfSale.UI.LeftToolbarImpl.prototype.menuEntries.push({
    kind: 'OBRDM.UI.MenuIssueSO'
  });
})();
