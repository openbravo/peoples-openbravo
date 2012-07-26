/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone , $ */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.ButtonTabBrowse = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#catalog',
    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
    shownEvent: function(e) {
      this.options.root.keyboard.hide();
    }
  });

  OB.UI.BrowseCategories = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'overflow:auto; height: 612px; margin: 5px;'
    },
    contentView: [
      {tag: 'div', attributes: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [
        {view: OB.COMP.ListCategories, id: 'listCategories'}
      ]}
    ],
    initialize: function() {
      OB.UTIL.initContentView(this);
    }
  });

  OB.UI.BrowseProducts = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'overflow:auto; height: 612px; margin: 5px;'
    },
    contentView: [
      {tag: 'div', attributes: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [
        {view: OB.COMP.ListProducts, id: 'listProducts'}
      ]}
    ],    
    initialize: function() {
      OB.UTIL.initContentView(this);
    }
  });

  OB.UI.TabBrowse = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'id': 'catalog',
      'class': 'tab-pane'
    },
    contentView: [
      {tag: 'div', attributes: {'class': 'row-fluid'}, content: [
        {tag: 'div', attributes: {'class': 'span6'}, content: [
          {view: OB.UI.BrowseProducts, id: 'browseProducts'}
        ]},
        {tag: 'div', attributes: {'class': 'span6'}, content: [
          {view: OB.UI.BrowseCategories, id: 'browseCategories'}
        ]}
      ]}
    ],     
    initialize: function() {
      OB.UTIL.initContentView(this);
      
//      this.options.root.ListCategories.categories.on('selected', function(category) { // Another option
      this.browseCategories.listCategories.categories.on('selected', function(category) {     
        this.browseProducts.listProducts.loadCategory(category);
      }, this);
    }
  });
}());