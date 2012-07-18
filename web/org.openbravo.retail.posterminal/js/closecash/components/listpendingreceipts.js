/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global define, B , $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListPendingReceipts = function (context) {
    var me = this;
    this._id = 'ListPendingReceipts';
    this.receiptlist = context.modelorderlist;

    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style':  'border-bottom: 1px solid #cccccc;'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: OB.UI.TableView, id: 'tableview', attr: {
              collection: this.receiptlist,
              renderLine: OB.COMP.RenderPendingReceipt.extend({me:me, ctx: context}),
              renderEmpty: OB.COMP.RenderEmpty
            }}
          ]}
        ]}
      ]}
    );
    this.$el = this.component.$el;
    this.tableview = this.component.context.tableview;
  };
}());