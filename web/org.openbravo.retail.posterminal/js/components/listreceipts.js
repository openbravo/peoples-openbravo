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
              collection: this.receiptlist,
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
    this.$ = this.component.$;
    this.tableview = this.component.context.tableview;       
    this.tableview.renderLine = function (model) {
      return (
        {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [                                                                                   
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
}); 