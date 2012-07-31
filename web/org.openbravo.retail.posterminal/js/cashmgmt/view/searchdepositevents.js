/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ , Backbone */



//  OB.COMP.SearchDepositEvents = function (context) {
//    var ctx = context;
//    this._id = 'SearchDepositEvents';
//    var me = this;
//    this.destinations = new OB.Model.Collection(context.DataDepositEvents);
//    context.DataDepositEvents.ds.on('ready', function(){
//      me.destinations.reset(this.cache);
//    });
//    this.destinations.exec();
//    this.component = B(
//      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//          {kind: B.KindJQuery('div'), content: [
//            {kind: OB.UI.TableView, id: 'tableview', attr: {
//              collection: this.destinations,
//              renderLine: OB.COMP.RenderDropDepDestinations,
//              renderEmpty: OB.COMP.RenderEmpty
//            }}
//          ]}
//        ]}
//      ]}
//    );
//    this.$el = this.component.$el;
//    this.tableview = this.component.context.tableview;
//  };
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