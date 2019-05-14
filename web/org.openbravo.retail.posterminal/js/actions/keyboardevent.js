/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function () {

  var AbstractKeyboardAction = function (args) {
      OB.Actions.AbstractAction.call(this, args);
      this.keyboardEvent = args.keyboardEvent;
      this.command = function (view) {
        view.waterfall('onGlobalKeypress', {
          keyboardEvent: this.keyboardEvent
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
    keyboardEvent: {
      key: '0'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-1',
    properties: {
      label: '1'
    },
    keyboardEvent: {
      key: '1'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-2',
    properties: {
      label: '2'
    },
    keyboardEvent: {
      key: '2'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-3',
    properties: {
      label: '3'
    },
    keyboardEvent: {
      key: '3'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-4',
    properties: {
      label: '4'
    },
    keyboardEvent: {
      key: '4'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-5',
    properties: {
      label: '5'
    },
    keyboardEvent: {
      key: '5'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-6',
    properties: {
      label: '6'
    },
    keyboardEvent: {
      key: '6'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-7',
    properties: {
      label: '7'
    },
    keyboardEvent: {
      key: '7'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-8',
    properties: {
      label: '8'
    },
    keyboardEvent: {
      key: '8'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-9',
    properties: {
      label: '9'
    },
    keyboardEvent: {
      key: '9'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-Period',
    properties: {
      label: '.'
    },
    keyboardEvent: {
      key: '.'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-Backspace',
    properties: {
      label: '\u232B'
    },
    keyboardEvent: {
      key: 'Backspace',
      which: 8
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-Enter',
    properties: {
      label: '\u21B5'
    },
    keyboardEvent: {
      key: 'Enter',
      which: 13
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-*',
    properties: {
      label: '*'
    },
    keyboardEvent: {
      key: '*'
    }
  }));
  OB.MobileApp.actionsRegistry.register(
  new AbstractKeyboardAction({
    window: 'retail.pointofsale',
    name: 'keyboard-/',
    properties: {
      label: '/'
    },
    keyboardEvent: {
      key: '/'
    }
  }));

}());