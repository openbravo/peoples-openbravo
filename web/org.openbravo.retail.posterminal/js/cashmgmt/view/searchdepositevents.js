/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


// Renders each of the deposit/drops destinations
OB.OBPOSCasgMgmt.UI.RenderDropDepDestinations = OB.COMP.SelectButton.extend({
  attributes: {
    'style': 'background-color:#dddddd;  border: 1px solid #ffffff;'
  },
  contentView: [{
    tag: 'div',
    id: 'divcontent',
    attributes: {
      style: 'padding: 1px 0px 1px 5px;'
    }
  }],

  render: function() {
    this.divcontent.text(this.model.get('name'));
    return this;
  }
});


// Popup with the destinations for deposits/drops
OB.OBPOSCasgMgmt.UI.SearchDepositEvents = Backbone.View.extend({
  tagName: 'div',
  className: 'row-fluid',

  contentView: [{
    tag: 'div',
    attributes: {
      'class': 'span12'
    },
    content: [{
      tag: 'div',
      content: [{
        id: 'tableview',
        view: OB.UI.TableView.extend({
          renderLine: OB.OBPOSCasgMgmt.UI.RenderDropDepDestinations,
          renderEmpty: OB.COMP.RenderEmpty
        })
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);

    this.tableview.registerCollection(this.options.parent.model.getData(this.type));
  }
});