/*global B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchDepositEvents = function (context) {
    var ctx = context;
    this._id = 'SearchDepositEvents';
    var me = this;
    this.destinations = new OB.MODEL.Collection(context.DataDepositEvents);
    context.DataDepositEvents.ds.on('ready', function(){
      me.destinations.reset(this.cache);
    });
    this.destinations.exec();
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), content: [
            {kind: OB.UI.TableView, id: 'tableview', attr: {
              collection: this.destinations,
              renderLine: OB.COMP.RenderDropDepDestinations,
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