/*global define */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

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
              collection: this.receiptlist
            }}
          ]}                   
        ]}                   
      ]}
    );
    this.$el = this.component.$el;
    this.tableview = this.component.context.tableview;       
    this.tableview.renderLine = OB.COMP.SelectButton.extend({
      render: function() {
        this.$el.append(B(                                                                               
          this.model.get('documentNo')                                                                         
        ).$el);
        return this;
      }
    });   
  };
  
  OB.COMP.ListReceipts.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;      
  };   
}); 