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
  initComponents: function() {
    this.inherited(arguments);
    if (this.icon) {
      this.$.leftIcon.addClass(this.icon);
    }
    if (this.iconright) {
      this.$.rightIcon.addClass(this.iconright);
    }

    enyo.forEach(this.menuEntries, function(entry) {
      this.$.menu.createComponent(entry);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.MenuReturn',
  kind: 'OB.UI.MenuAction',
  events: {
    onShowReturnText: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblReturn'),
  tap: function() {
    this.doShowReturnText();
  }
});

enyo.kind({
  name: 'OB.UI.MenuInvoice',
  kind: 'OB.UI.MenuAction',
  events: {
    onReceiptToInvoice: ''
  },
  label: OB.I18N.getLabel('OBPOS_LblInvoice'),
  tap: function() {
    this.doReceiptToInvoice();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.StandardMenu',
  kind: 'OB.UI.ToolbarMenu',
  icon: 'btn-icon btn-icon-menu',
  initComponents: function() {
    // dynamically generating the menu
    this.menuEntries = [];
    this.menuEntries.push({
      kind: 'OB.UI.MenuReturn'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuInvoice'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuItem',
      label: OB.I18N.getLabel('OBPOS_LblOpenbravoWorkspace'),
      url: '../..'
    });

    enyo.forEach(OB.POS.windows.filter(function(window) {
      // show in menu only the ones with menuPosition
      return window.get('menuPosition');
    }), function(window) {
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
  initComponents: function() {
    this.inherited(arguments);
    this.$.item.setContent(this.label);
    if (!OB.POS.modelterminal.hasPermission(this.route)) {
      this.$.item.setStyle('color: #cccccc; padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;');
    }
  },
  tap: function() {
    // TODO: check online for required windows
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

