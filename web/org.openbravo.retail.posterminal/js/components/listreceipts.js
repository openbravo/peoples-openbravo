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
          {kind: B.KindJQuery('div'), content: [ 
            {kind: OB.COMP.TableView, attr: {
              collection: this.receiptlist,
              renderEmpty: function () {
                return B(
                  {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                    OB.I18N.getLabel('OBPOS_SearchNoResults')
                  ]}
                );        
              },
              renderLine: function (model) {
                return B(
                  {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [                                                                                                                                                                        
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
                       OB.I18N.formatHour(model.get('orderdate'))
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
                      model.get('documentno')
                    ]}, 
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left;width: 50%;'}, content: [ 
                      model.get('bp').get('_identifier')
                    ]}, 
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                      {kind: B.KindJQuery('strong'), content: [ 
                         model.printNet()                                                                                                                             
                      ]}                                                                                                                                                                                                                                 
                    ]},              
                    {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                  ]}
                );
              }
            }}
          ]}                   
        ]}                   
      ]}
    );
    this.$ = this.component.$;
  };
  
}); 