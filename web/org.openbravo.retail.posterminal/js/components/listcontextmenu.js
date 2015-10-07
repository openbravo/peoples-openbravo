/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  name: 'OB.UI.ListContextMenuItem',
  kind: 'onyx.MenuItem',
  selectItem: function (product) {

  }
});

enyo.kind({
  name: 'OB.UI.ListButtonContextMenu',
  style: 'float:right; width: 40px; height: 40px;',
  classes: 'listcontextmenu-icon',
  active: true,
  tap: function () {
    this.addClass('listcontextmenu-icon', false);
    this.addClass('listcontextmenu-icon-pressed', true);
    this.bubble("onActivate");
    var me = this;
    setTimeout(function () {
      me.addClass('listcontextmenu-icon-pressed', false);
      me.addClass('listcontextmenu-icon', true);
      me.render();
    }, 1000);
    return true;
  },
  setActive: function (act) {
    this.active = true;
  }
});

enyo.kind({
  name: 'OB.UI.ListContextDynamicMenu',
  kind: 'onyx.Menu',
  classes: 'dropdown',
  maxHeight: 600,
  scrolling: false,
  floating: true,
  itemsCount: 0,
  setItems: function (menuItems) {
    // If you want to remove old items, you'll need to get the child
    // components and remove everything but the scroller
    var allowedItems = [];
    _.each(menuItems, function (item) {
      if (!item.permission || OB.MobileApp.model.hasPermission(item.permission, true)) {
        allowedItems.push(item);
      }
    }, this);
    this.itemsCount = allowedItems.length;
    this.createComponents(allowedItems, {
      owner: this
    });
    this.render();
  }
});

enyo.kind({
  name: 'OB.UI.ListContextMenu',
  handlers: {
    onSelect: 'itemSelected'
  },
  published: {
    product: null
  },
  components: [{
    kind: 'onyx.MenuDecorator',
    name: 'btnListContextMenu',
    components: [{
      kind: 'OB.UI.ListButtonContextMenu',
      name: 'listButton'
    }, {
      kind: 'OB.UI.ListContextDynamicMenu',
      name: 'menu'
    }]
  }],
  itemSelected: function (sender, event) {
    event.originator.selectItem(this.product);
    return true;
  }
});