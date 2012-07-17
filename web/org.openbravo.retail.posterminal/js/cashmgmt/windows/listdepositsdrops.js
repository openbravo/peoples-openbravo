/*global window, B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListDepositsDrops = OB.COMP.CustomView.extend({
	  _id: 'listdepositsdrops',
	  initialize: function () {
	        var me = this;
	        this._id = 'ListDepositsDrops';
	        this.listdepositsdrops = new OB.Model.Collection(this.options.DataDepositsDrops);
	        this.component = B(
	          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
	           {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc;'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px; border-bottom: 1px solid #cccccc;text-align:center; font-weight:bold;'}, content: [
                   OB.I18N.getLabel('OBPOS_LblCashManagement')
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                     OB.I18N.getLabel('OBPOS_LblUser')+': '+OB.POS.modelterminal.get('context').user._identifier
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                    OB.I18N.getLabel('OBPOS_LblTime')+': '+ new Date().toString().substring(3,24)
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                    OB.I18N.getLabel('OBPOS_LblStore')+': '+ OB.POS.modelterminal.get('terminal').organization$_identifier
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                    OB.I18N.getLabel('OBPOS_LblTerminal')+': '+ OB.POS.modelterminal.get('terminal')._identifier
                ]},
                {kind: OB.UI.TableView, id: 'tableview', attr: {
                  style: 'list',
                  collection: this.listdepositsdrops,
                  renderEmpty: OB.COMP.RenderEmpty,
                  renderLine: OB.COMP.RenderDepositsDrops
                }}
               ]}
	            ]}
             ]}
	          ]}
	        );
	       this.$el = this.component.$el;
	       this.tableview = this.component.context.tableview;
	       this.listdepositsdrops.exec();
	    }
	  });
}());