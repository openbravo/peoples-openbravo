/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'scanCode',
      properties: {
        i18nContent: 'OBMOBC_KbCode'
      },
      command: function(view) {
        var editboxvalue = view.state.readState({
          name: 'editbox'
        });

        if (!editboxvalue) {
          return;
        }

        view.waterfall('onVirtualKeyboardCommand', {
          action: 'code'
        });
      }
    })
  );
})();
