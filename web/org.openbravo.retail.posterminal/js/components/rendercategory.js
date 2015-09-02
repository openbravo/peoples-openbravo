/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.RenderCategory',
  kind: 'OB.UI.listItemButton',
  components: [{
    style: 'float: left; width: 25%',
    components: [{
      kind: 'OB.UI.Thumbnail',
      name: 'thumbnail'
    }]
  }, {
    style: 'float: left; width: 75%;',
    components: [{
      name: 'identifier',
      style: 'padding-left: 5px;'
    }, {
      style: 'clear:both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnselect-browse');
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.thumbnail.setImg(this.model.get('img'));
  }
});

enyo.kind({
  name: 'OB.UI.RenderCategoryExpand',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconExpand.png',
  sizing: "constrain",
  position: 'center',
  width: 32,
  height: 32
});

enyo.kind({
  name: 'OB.UI.RenderCategoryCollapse',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconCollapse.png',
  sizing: "cover",
  width: 26,
  height: 26,
  showing: false
});

enyo.kind({
  name: 'OB.UI.RenderCategoryTree',
  kind: 'OB.UI.listItemButton',
  style: 'height: 41px;',
  handlers: {
    onkeydown: 'keydownHandler'
  },
  keydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13) { // Handle ENTER key in list item
      this.tap();
      return true;
    }
    if (keyCode === 32) { // Handle SPACE key in list item
      if (this.model.get('issummary')) {
        this.categoryExpandCollapse();
      } else {
        this.tap();
      }
      return true;
    }
    return false;
  },
  categoryExpandCollapse: function () {
    this.bubble('onCategoryExpandCollapse', {
      categoryId: this.model.get('id'),
      expand: this.$.expand.getShowing()
    });
  },
  components: [{
    style: 'float:left; width: calc(100% - 40px);',
    components: [{
      classes: 'product_category_tree_identifier',
      name: 'identifier'
    }]
  }, {
    style: 'float:left; width: 40px; text-align: right; margin-top: -8px;',
    components: [{
      name: 'expandCollapse',
      style: 'height: 41px; padding-right: 7px; padding-top: 7px;',
      components: [{
        kind: 'OB.UI.RenderCategoryExpand',
        name: 'expand'
      }, {
        kind: 'OB.UI.RenderCategoryCollapse',
        name: 'collapse'
      }],
      tap: function () {
        this.owner.categoryExpandCollapse();
        return true;
      }
    }]
  }],

  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnselect-browse');
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.identifier.setStyle('padding-left: ' + (14 * this.model.get('level')) + 'px; ' + (this.model.id === '__all__' ? 'font-weight: bold; ' : ''));
    this.$.expandCollapse.setShowing(this.model.get('issummary'));
  }
});