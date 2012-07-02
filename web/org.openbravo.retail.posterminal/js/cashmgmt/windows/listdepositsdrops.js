/*global window, B, $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListDepositsDrops = OB.COMP.CustomView.extend({
	  _id: 'listdepositsdrops',
	  initialize: function () {
	        var me = this;
	        this._id = 'ListDepositsDrops';
	        this.total= OB.DEC.Zero;
	        this.listdepositsdrops = new OB.MODEL.Collection(this.options.DataDepositsDrops);
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
	              {kind: OB.COMP.TableView, id: 'tableview', attr: {
	                style: 'list',
	                collection: this.listdepositsdrops,
	                me: me,
	                renderEmpty: function () {
	                  return (
	                    {kind: B.KindJQuery('div'), attr: {'style': ' padding: 20px; border-bottom: 1px solid #cccccc; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
	                      OB.I18N.getLabel('OBPOS_SearchNoResults')
	                    ]}
	                  );
	                },
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
                          this.total.text(me.total.toString());
                          if(OB.DEC.compare(OB.DEC.add(0,this.total.text()) )<0){
                             this.$el.css("color","red");//negative value
                          }else{
                             this.$el.css("color","black");
                          }
                          }, this);
                           // Initial total display
                          this.total.text(me.total.toString());
                         if(OB.DEC.compare(OB.DEC.add(0,this.total.text()) )<0){
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
	       this.listdepositsdrops.exec();
	    }
	  });
}());