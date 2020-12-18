/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var AbstractKeyboardAction = function(args) {
    OB.Actions.AbstractAction.call(this, args);
    this.action = args.action;
    this.command = function(view) {
      view.waterfall('onVirtualKeyboardCommand', {
        action: this.action
      });
    };
  };

  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-0',
      properties: {
        label: '0'
      },
      action: '0'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-1',
      properties: {
        label: '1'
      },
      action: '1'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-2',
      properties: {
        label: '2'
      },
      action: '2'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-3',
      properties: {
        label: '3'
      },
      action: '3'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-4',
      properties: {
        label: '4'
      },
      action: '4'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-5',
      properties: {
        label: '5'
      },
      action: '5'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-6',
      properties: {
        label: '6'
      },
      action: '6'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-7',
      properties: {
        label: '7'
      },
      action: '7'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-8',
      properties: {
        label: '8'
      },
      action: '8'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-9',
      properties: {
        label: '9'
      },
      action: '9'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-Period',
      properties: {
        label: '.'
      },
      action: '.'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-Backspace',
      properties: {
        i18nLabel: 'OBMOBC_KbBackspace',
        cssClasses: 'btn-icon btn-icon-backspace'
      },
      action: 'del'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-Enter',
      properties: {
        i18nLabel: 'OBMOBC_KbEnter',
        cssClasses: 'btn-icon btn-icon-enter'
      },
      action: 'OK'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-*',
      properties: {
        label: '*'
      },
      action: '*'
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new AbstractKeyboardAction({
      window: 'retail.pointofsale',
      name: 'keyboard-/',
      properties: {
        label: '/'
      },
      action: '/'
    })
  );
})();
