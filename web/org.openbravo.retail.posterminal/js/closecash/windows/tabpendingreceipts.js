/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  var me = this;
  OB.COMP.ListReceipts = function (context) {
    var me = this;

    this._id = 'ListReceipts';

    this.receipt = context.modelorder;
    this.receiptlist = context.modelorderlist;

    this.receiptlist.on('click', function (model, index) {
      this.receiptlist.load(model);
    }, this);


    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style':  'border-bottom: 1px solid #cccccc;'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: OB.COMP.TableView, id: 'tableview', attr: {
              collection: this.receiptlist,

              render: function () {
                return (
                  {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                    'fsdafs'
                  ]}
                );
              },

              renderEmpty: function () {
                return (
                  {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                    OB.I18N.getLabel('OBPOS_SearchNoResults')
                  ]}
                );
              }
            }}
          ]}
        ]}
      ]}
    );
    this.$el = this.component.$el;
    this.tableview = this.component.context.tableview;
    this.tableview.renderLine = function (model) {
      return (
        {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [
          {kind: B.KindJQuery('div'), content: [
            model.get('documentNo')
          ]}
        ]}
      );
    };
  };

  OB.COMP.ListReceipts.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;
  };

  OB.COMP.PendingReceipts = OB.COMP.CustomView.extend({
  _id: 'pendingreceipts',
   createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'pendingreceipts', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [

                     OB.I18N.getLabel('OBPOS_LblStep1of3')

                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div')},
              {kind: OB.COMP.ListReceipts}
            ]}
          ]}
        ]}
      );
    }
  });

}());