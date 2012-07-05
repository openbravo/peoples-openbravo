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
              renderLine: OB.COMP.RenderPendingReceipt.extend({me:me}),
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