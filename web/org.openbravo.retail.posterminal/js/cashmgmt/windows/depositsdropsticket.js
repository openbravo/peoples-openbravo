/*global window, B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.DepositsDropsTicket = OB.COMP.CustomView.extend({
	  _id: 'depositsdropsTicket',
	  initialize: function () {
	        var me = this;
	        this.total= OB.DEC.Zero;
	        this.listdepositsdrops = this.options.ListDepositsDrops.listdepositsdrops;
	        this.component = B(
	        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
	          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
	            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
	              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
	                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
	                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [
	                     OB.I18N.getLabel('OBPOS_LblDepositsDrops')
	                  ]}
	                ]}
	              ]},
	              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [
                       OB.I18N.getLabel('OBPOS_LblCashMgmtHeader')
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                         OB.I18N.getLabel('OBPOS_LblUser')+': '+OB.POS.modelterminal.get('context').user._identifier
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                        OB.I18N.getLabel('OBPOS_LblTime')+': '+ new Date().toString().substring(3,24)
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                        OB.I18N.getLabel('OBPOS_LblStore')+': '+ OB.POS.modelterminal.get('terminal').organization$_identifier
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                        OB.I18N.getLabel('OBPOS_LblTerminal')+': '+ OB.POS.modelterminal.get('terminal')._identifier
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                    ]}
                  ]}
                ]},
	              {kind: OB.UI.TableView, id: 'tableview', attr: {
	                style: 'list',
	                collection: this.listdepositsdrops,
	                me: me,
                  renderEmpty: OB.COMP.RenderEmpty,
	                renderLine: OB.COMP.RenderDepositsDrops.extend({me:me})
	              }},
	              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc;'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; float: left; width: 70%'}, content: [
                              OB.I18N.getLabel('OBPOS_ReceiptTotal')
                      ]},
                  {kind: B.KindJQuery('div'), id: 'total', attr: {'style': 'padding: 17px 20px 17px 0px;  float: right; '}, content: [
                   {kind: Backbone.View.extend({
                     tagName: 'span',
                     attributes: {'style': 'float:right;'},
                     initialize: function () {
                          this.total = $('<strong/>');
                          this.$el.append(this.total);
                          // Set Model
                          me.on('change:total', function() {
                          this.total.text(OB.I18N.formatCurrency(me.total));
                          if(OB.DEC.compare(OB.DEC.add(0,me.total) )<0){
                             this.$el.css("color","red");//negative value
                          }else{
                             this.$el.css("color","black");
                          }
                          }, this);
                           // Initial total display
                          this.total.text(OB.I18N.formatCurrency(me.total));
                         if(OB.DEC.compare(OB.DEC.add(0,me.total) )<0){
                             this.$el.css("color","red");//negative value
                         }else{
                             this.$el.css("color","black");
                         }
                        }
                      })}
                    ]}
              ]}
              ]}
          ]}
	            ]}
	          ]}
	        );
	       this.$el = this.component.$el;
	       this.tableview = this.component.context.tableview;
	    }
	  });
}());