/*global window, B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.DepositsDropsTicket = OB.COMP.CustomView.extend({
	  _id: 'depositsdropsTicket',
	  initialize: function () {
	        var me = this;
	        this.startingCash= OB.DEC.Zero;
	        this.totalTendered= OB.DEC.Zero;
	        this.total= OB.DEC.Zero;
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
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px;  float: left; width: 70%'}, content: [
                      'Starting Cash'
                    ]},
                    {kind: B.KindJQuery('div'), attr: { 'id': 'startingCashTicket','style': 'text-align:right; padding: 10px 20px 10px 10px; float: right;'}, content: [
                      OB.I18N.formatCurrency(OB.DEC.add(0,this.startingCash))
                    ]}
                   ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px;  float: left; width: 70%'}, content: [
                      'Total tendered'
                    ]},
                    {kind: B.KindJQuery('div'), attr: { 'id': 'totalTenderedTicket', 'style': 'text-align:right; padding: 10px 20px 10px 10px; float: right;'}, content: [
                      OB.I18N.formatCurrency(OB.DEC.add(0,this.totalTendered))
                    ]}
                   ]}
                ]},
	              {kind: OB.UI.TableView, id: 'tableview', attr: {
	                style: 'list',
	                collection: this.options.ListDepositsDrops.dropsdeps,
	                me: me,
                  renderEmpty: OB.COMP.RenderEmpty,
	                renderLine: OB.COMP.RenderDepositsDrops.extend({me:me})
	              }},
	              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc;'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; float: left; width: 70%'}, content: [
                              OB.I18N.getLabel('OBPOS_ReceiptTotal')
                      ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 0px;  float: right; '}, content: [
                   {kind: Backbone.View.extend({
                     tagName: 'span',
                     attributes: {'style': 'float:right;'},
                     initialize: function () {
                          var that = this;
                          this.total = $('<strong/>');
                          this.$el.append(this.total);
                          // Set Model
                          me.options.ListDepositsDrops.dropsdeps.on('add', function(e) {
                          if(me.options.ListDepositsDrops.dropsdeps.length!==0){
//                          $('#startingCashTicket').text(OB.I18N.formatCurrency(me.listdepositsdrops.models[0].get('startingCash')));
                            that.total.text(OB.I18N.formatCurrency(OB.DEC.add(OB.DEC.add(me.options.ListDepositsDrops.startingCash,me.options.ListDepositsDrops.totalTendered),me.options.ListDepositsDrops.total)));
                            $('#totalTenderedTicket').text(OB.I18N.formatCurrency(me.options.ListDepositsDrops.totalTendered));
                          }
                        });
                          me.on('change:total', function() {
                            that.total.text(OB.I18N.formatCurrency(OB.DEC.add(OB.DEC.add(me.options.ListDepositsDrops.startingCash,me.options.ListDepositsDrops.totalTendered),me.options.ListDepositsDrops.total)));
                          if(OB.DEC.compare(OB.DEC.add(0,me.total) )<0){
                             this.$el.css("color","red");//negative value
                          }else{
                             this.$el.css("color","black");
                          }
                          }, this);
                           // Initial total display
                          this.total.text(OB.I18N.formatCurrency(OB.DEC.add(OB.DEC.add(me.options.ListDepositsDrops.startingCash,me.options.ListDepositsDrops.totalTendered),me.options.ListDepositsDrops.total)));
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