/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  enyo.kind({
    name: 'OB.UI.ButtonTabSearch',
    kind: 'OB.UI.ToolbarButtonTab',
    tabPanel: '#search',
    label: OB.I18N.getLabel('OBPOS_LblSearch'),
    events: {
      onTabChange: ''
    },
    tap: function() {
      this.doTabChange({
        keyboard: false,
        edit: false
      });
    },
    initComponents: function() {
      this.inherited(arguments);
    }
  });

  //Refacorized using enyo -> OB.UI.ButtonTabSearch
  //  OB.UI.ButtonTabSearch = OB.COMP.ToolbarButtonTab.extend({
  //    tabpanel: '#search',
  //    label: OB.I18N.getLabel('OBPOS_LblSearch'),
  //    shownEvent: function(e) {
  //      this.options.root.keyboard.hide();
  //    }
  //  });
  enyo.kind({
    name: 'OB.UI.TabSearch',

    classes: 'tab-pane',
    components: [{
      style: 'overflow: auto; margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px',
        components: [{
          kind: 'OB.UI.SearchProduct'
        }]
      }]
    }],
    makeId: function() {
      return 'search';
    }

  });

  //    OB.UI.TabSearch = Backbone.View.extend({
  //      tagName: 'div',
  //      attributes: {
  //        'id': 'search',
  //        'class': 'tab-pane'
  //      },
  //      initialize: function() {
  //        var $container, $subContainer, searchProd;
  //        $container = $('<div/>');
  //        $container.css({
  //          'overflow': "auto",
  //          'margin': '5px'
  //        });
  //        $subContainer = $('<div/>');
  //        $subContainer.css({
  //          'background-color': "#ffffff",
  //          'color': 'black',
  //          'padding': '5px'
  //        });
  //        searchProd = new OB.COMP.SearchProduct(this.options);
  //        $subContainer.append(searchProd.$el);
  //        $container.append($subContainer);
  //        this.$el.append($container);
  //      }
  //    });
}());