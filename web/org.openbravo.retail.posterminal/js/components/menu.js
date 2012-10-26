/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ToolbarMenuButton',
  kind: 'OB.UI.ToolbarButton',
  attributes: {
    'data-toggle': 'dropdown'
  }
});

enyo.kind({
  name: 'OB.UI.ToolbarMenu',
  classes: 'dropdown',
  style: 'display: inline-block; width: 100%;',
  components: [{
    kind: 'OB.UI.ToolbarMenuButton',
    components: [{
      name: 'leftIcon'
    }, {
      tag: 'span'
    }, {
      name: 'rightIcon'
    }]
  }, {
    tag: 'ul',
    classes: 'dropdown-menu',
    name: 'menu'
  }],
  initComponents: function () {
    this.inherited(arguments);
    if (this.icon) {
      this.$.leftIcon.addClass(this.icon);
    }
    if (this.iconright) {
      this.$.rightIcon.addClass(this.iconright);
    }

    enyo.forEach(this.menuEntries, function (entry) {
      this.$.menu.createComponent(entry);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.MenuReturn',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_receipt.invoice',
  events: {
    onShowReturnText: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblReturn'),
  tap: function () {
    this.doShowReturnText();
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isEditable', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false) {
          this.setShowing(false);
          return;
        }
      }
      this.setShowing(true);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.MenuProperties',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_receipt.properties',
  events: {
    onShowReceiptProperties: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblProperties'),
  tap: function () {
    this.doShowReceiptProperties();
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isEditable', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false) {
          this.setShowing(false);
          return;
        }
      }
      this.setShowing(true);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.MenuInvoice',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_receipt.invoice',
  events: {
    onReceiptToInvoice: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblInvoice'),
  tap: function () {
    this.doReceiptToInvoice();
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isEditable', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false) {
          this.setShowing(false);
          return;
        }
      }
      this.setShowing(true);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.MenuCustomers',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_receipt.customers',
  events: {
    onChangeSubWindow: ''
  },
  label: 'Customers',
  tap: function () {
    this.doChangeSubWindow({
      newWindow: {
        name: 'customerAdvancedSearch',
        params: {
          navigateOnClose: 'mainSubWindow'
        }
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.MenuPrint',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_print.receipt',
  events: {
    onPrintReceipt: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblPrintReceipt'),
  tap: function () {
    if (OB.POS.modelterminal.hasPermission(this.permission)) {
      this.doPrintReceipt();
    }
  }
});

enyo.kind({
  name: 'OB.UI.MenuSeparator',
  tag: 'li',
  classes: 'divider'
});

enyo.kind({
  name: 'OB.UI.MenuPaidReceipts',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_retail.paidReceipts',
  events: {
    onPaidReceipts: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblPaidReceipts'),
  tap: function () {
    if (!OB.POS.modelterminal.get('connectedToERP')) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
      return;
    }
    if (OB.POS.modelterminal.hasPermission(this.permission)) {
      this.doPaidReceipts();
    }
  }
});

enyo.kind({
  name: 'OB.UI.MenuBackOffice',
  kind: 'OB.UI.MenuAction',
  permission: 'OBPOS_retail.backoffice',
  url: '../..',
  events: {
    onBackOffice: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblOpenbravoWorkspace'),
  tap: function () {
    if (OB.POS.modelterminal.hasPermission(this.permission)) {
      this.doBackOffice({
        url: this.url
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.StandardMenu',
  kind: 'OB.UI.ToolbarMenu',
  icon: 'btn-icon btn-icon-menu',
  initComponents: function () {
    // dynamically generating the menu
    this.menuEntries = [];
    this.menuEntries.push({
      kind: 'OB.UI.MenuReturn'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuProperties'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuInvoice'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuPrint'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuCustomers'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuPaidReceipts'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuBackOffice'
    });

    enyo.forEach(OB.POS.windows.filter(function (window) {
      // show in menu only the ones with menuPosition
      return window.get('menuPosition');
    }), function (window) {
      this.menuEntries.push({
        kind: 'OB.UI.MenuItem',
        label: window.get('menuLabel'),
        route: window.get('route')
      });
    }, this);
    this.inherited(arguments);
  }
});


enyo.kind({
  name: 'OB.UI.MenuSeparator',
  tag: 'li',
  classes: 'divider'
});

enyo.kind({
  name: 'OB.UI.MenuItem',
  tag: 'li',
  components: [{
    tag: 'a',
    name: 'item',
    style: 'padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;',
    attributes: {
      href: '#'
    }
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.item.setContent(this.label);
    if (!OB.POS.modelterminal.hasPermission(this.route)) {
      this.$.item.setStyle('color: #cccccc; padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;');
    }
  },
  tap: function () {
    if (OB.POS.modelterminal.isWindowOnline(this.route) === true) {
      if (!OB.POS.modelterminal.get('connectedToERP')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
        return;
      }
      if (OB.POS.modelterminal.get('loggedOffline') === true) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowOfflineLogin'));
        return;
      }
    }
    if (!OB.POS.modelterminal.hasPermission(this.route)) {
      return;
    }
    if (this.route) {
      OB.POS.navigate(this.route);
    }
    if (this.url) {
      window.open(this.url, '_blank');
    }
  }
});