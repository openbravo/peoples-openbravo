/*global define, B , $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchRetailTransactions = Backbone.View.extend({
    tagName: 'div',
    attributes: {'style': 'position: absolute; top:0px; right: 0px;'},
    initialize: function () {
      var me = this;
      this._id = 'searchretailtransactions';
      this.transactions = new OB.Model.Collection(this.options.DataCashCloseReport);
      this.options.DataCashCloseReport.ds.on('ready', function(){
        me.transactions.reset(this.cache);
      });
      this.component = B(
        {kind: B.KindJQuery('div'), content: [
           {kind: OB.UI.TableView, id: 'tableview', attr: {
             style: 'list',
             collection: this.transactions,
             me: me,
             renderLine: OB.COMP.RenderRetailTransactions.extend({me:me})
           }}
         ]}
      );
      this.$el = this.component.$el;
      this.tableview = this.component.context.tableview;
      this.transactions.exec();
    }
  });
}());