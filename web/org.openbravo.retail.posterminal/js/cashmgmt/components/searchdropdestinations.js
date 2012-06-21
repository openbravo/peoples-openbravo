/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchDropDestinations = function (context) {
	  var me = this;

	    this._id = 'SearchDropDestinations';

//	    this.destinations = context.DataDestinations.destinations;
	    this.destinations = new Backbone.Collection();
//	    this.destination.on('click', function (model, index) {
//	      this.receiptlist.load(model);
//	    }, this);


	    this.component = B(
	      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
	        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
	          {kind: B.KindJQuery('div'), attr: {'style':  'border-bottom: 1px solid #cccccc;'}},
	          {kind: B.KindJQuery('div'), content: [
	            {kind: OB.COMP.TableView, id: 'tableview', attr: {
	              collection: this.destinations,
	              renderLine: OB.COMP.RenderDropDestinations,
              renderEmpty: function () {
              return (
                {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc;text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
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
	    this.destinations.add({'name':'Envelope 1'});
	    this.destinations.add({'name':'Envelope 2'});
	    this.destinations.add({'name':'Envelope 3'});
  };
}());