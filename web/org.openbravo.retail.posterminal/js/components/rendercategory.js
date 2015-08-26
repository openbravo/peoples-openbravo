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
  height: 32,
  events: {
    onCategoryExpandCollapse: ''
  },
  tap: function () {
    this.doCategoryExpandCollapse({
      categoryId: this.owner.model.get('id'),
      expand: true
    });
  }
});

enyo.kind({
  name: 'OB.UI.RenderCategoryCollapse',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconCollapse.png',
  sizing: "cover",
  width: 26,
  height: 26,
  showing: false,
  events: {
    onCategoryExpandCollapse: ''
  },
  tap: function () {
    this.doCategoryExpandCollapse({
      categoryId: this.owner.model.get('id'),
      expand: false
    });
  }
});

enyo.kind({
  name: 'OB.UI.RenderCategoryTree',
  kind: 'OB.UI.listItemButton',
  style: 'height: 41px;',
  components: [{
    style: 'float:left; width: 80%;',
    components: [{
      classes: 'product_category_tree_identifier',
      name: 'identifier'
    }]
  }, {
    style: 'float:left; width: 20%; text-align: right; margin-top: -8px;',
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
        this.bubble('onCategoryExpandCollapse', {
          categoryId: this.owner.model.get('id'),
          expand: this.owner.$.expand.getShowing()
        });
        return true;
      }
    }]
  }],

  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnselect-browse');
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.identifier.setStyle('padding-left: ' + (14 * this.model.get('level')) + 'px;');
    this.$.expandCollapse.setShowing(this.model.get('issummary'));
  }
});