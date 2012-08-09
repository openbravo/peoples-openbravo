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

  enyo.kind({
    name: 'OB.UI.ButtonTabBrowse',
    kind: 'OB.UI.ToolbarButtonTab',
    tabPanel: '#catalog',
    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
    tap: function() {
      this.inherited(arguments);
      this.owner.owner.owner.owner.owner.$.keyboard.hide();
    },
    initComponents: function() {
      this.inherited(arguments);
    }
  });

  //Refacorized using enyo -> OB.UI.ButtonTabBrowse
  //  OB.UI.ButtonTabBrowse = OB.COMP.ToolbarButtonTab.extend({
  //    tabpanel: '#catalog',
  //    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
  //    shownEvent: function(e) {
  //      this.options.root.keyboard.hide();
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.BrowseCategories',
    style: 'overflow:auto; height: 612px; margin: 5px;',
    components: [{
      style: 'background-color: #ffffff; color: black; padding: 5px',
      components: [{
        kind: 'OB.UI.ListCategories',
        name: 'listCategories'
      }]
    }]
  });

  enyo.kind({
    name: 'OB.UI.BrowseProducts',
    style: 'overflow:auto; height: 612px; margin: 5px;',
    components: [{
      style: 'background-color: #ffffff; color: black; padding: 5px',
      components: [{
        kind: 'OB.UI.ListProducts',
        name: 'listProducts'
      }]
    }]
  });

  //  OB.UI.BrowseCategories = Backbone.View.extend({
  //    tagName: 'div',
  //    attributes: {
  //      'style': 'overflow:auto; height: 612px; margin: 5px;'
  //    },
  //    contentView: [{
  //      tag: 'div',
  //      attributes: {
  //        'style': 'background-color: #ffffff; color: black; padding: 5px'
  //      },
  //      content: [{
  //        view: OB.COMP.ListCategories,
  //        id: 'listCategories'
  //      }]
  //    }],
  //    initialize: function() {
  //      OB.UTIL.initContentView(this);
  //    }
  //  });
  //  OB.UI.BrowseProducts = Backbone.View.extend({
  //    tagName: 'div',
  //    attributes: {
  //      'style': 'overflow:auto; height: 612px; margin: 5px;'
  //    },
  //    contentView: [{
  //      tag: 'div',
  //      attributes: {
  //        'style': 'background-color: #ffffff; color: black; padding: 5px'
  //      },
  //      content: [{
  //        view: OB.COMP.ListProducts,
  //        id: 'listProducts'
  //      }]
  //    }],
  //    initialize: function() {
  //      OB.UTIL.initContentView(this);
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.TabBrowse',
    classes: 'tab-pane',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span6',
        components: [{
          kind: 'OB.UI.BrowseProducts',
          name: 'browseProducts'
        }]
      }, {
        classes: 'span6',
        components: [{
          kind: 'OB.UI.BrowseCategories',
          name: 'browseCategories'
        }]
      }]
    }],

    makeId: function() {
      return 'catalog';
    },

    init: function() {
      console.log('init tab ')
      this.$.browseCategories.$.listCategories.categories.on('selected', function(category) {
        console.log('selected')
        this.$.browseProducts.$.listProducts.loadCategory(category);
      }, this);
    }


  });

  //  OB.UI.TabBrowse = Backbone.View.extend({
  //    tagName: 'div',
  //    attributes: {
  //      'id': 'catalog',
  //      'class': 'tab-pane'
  //    },
  //    contentView: [{
  //      tag: 'div',
  //      attributes: {
  //        'class': 'row-fluid'
  //      },
  //      content: [{
  //        tag: 'div',
  //        attributes: {
  //          'class': 'span6'
  //        },
  //        content: [{
  //          view: OB.UI.BrowseProducts,
  //          id: 'browseProducts'
  //        }]
  //      }, {
  //        tag: 'div',
  //        attributes: {
  //          'class': 'span6'
  //        },
  //        content: [{
  //          view: OB.UI.BrowseCategories,
  //          id: 'browseCategories'
  //        }]
  //      }]
  //    }],
  //    initialize: function() {
  //      OB.UTIL.initContentView(this);
  //
  //      //      this.options.root.ListCategories.categories.on('selected', function(category) { // Another option
  //      this.browseCategories.listCategories.categories.on('selected', function(category) {
  //        this.browseProducts.listProducts.loadCategory(category);
  //      }, this);
  //    }
  //  });
}());