/*global B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchDropDepDestinations = function (context) {
	    var ctx = context;
	    this._id = 'SearchDropDestinations';
//	    if(context.type==='drop'){
//	     this.destinations = context.DataDropDestinations.destinations;
//	    }else{
//      this.destinations = context.DataDepositDestinations.destinations;
//	    }
	    this.destinations = context.destinations;
//	    this.destinations.on('click', function (model, index) {
//          this.proc.exec({
////        terminalId: OB.POS.modelterminal.get('terminal').id,
//            name: model.get('name'),
//            amount: ctx.amountToDrop,
//            key: ctx.destinationKey,
//            type: ctx.type
//          }, function (data, message) {
//          if (data && data.exception) {
//            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgFinishCloseError'));
//          } else {
//            $('#modalFinishClose').modal('show');
//          }
//         });
//       }, this);
//       this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt');


	    this.component = B(
	      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
	        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
	          {kind: B.KindJQuery('div'), content: [
	            {kind: OB.COMP.TableView, id: 'tableview', attr: {
	              collection: this.destinations,
	              renderLine: OB.COMP.RenderDropDepDestinations,
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
	    //********TEMP*******************************
	    this.destinations.add({'name':'Envelope 1'});
	    this.destinations.add({'name':'Envelope 2'});
	    this.destinations.add({'name':'Envelope 3'});
	    //*******************************************
  };
}());