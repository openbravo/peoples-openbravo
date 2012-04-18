/*global define */

define(['builder', 'utilities', 'model/order', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderView = function (context) {
  
    // Set Model
    this.receipt =  context.modelorder;
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printTotal());   
    }, this);    

    this.component = B(
      {kind: B.KindJQuery('div'), content: [                                                
        {kind: OB.COMP.TableView, attr: {
          style: 'edit',
          collection: lines,
          renderEmpty: function () {
            return B(
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                 OB.I18N.getLabel('OBPOS_ReceiptNew')
              ]}
            );          
          },
          renderLine: function (model) {
            return B(
              {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%'}, content: [ 
                  model.get('productidentifier')                                                                
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                  model.printQty()                                                                                                                                                          
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                  model.printPrice()                                                             
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                  model.printNet()
                ]},
                {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
              ]}
            );         
          }        
        }},                                                
        {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled'}, content: [                                                                                        
          {kind: B.KindJQuery('li'), content: [                                                                                        
            {kind: B.KindJQuery('div'), attr: {style: 'position: relative; padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%; color:  #888888'}, content: [ 
                OB.I18N.getLabel('OBPOS_ReceiptTaxes')
              ]},                                                                                                                                                                           
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                ''
              ]},
              {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
            ]}
          ]},
          {kind: B.KindJQuery('li'), content: [                                                                                        
            {kind: B.KindJQuery('div'), attr: {style: 'position: relative; padding: 10px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%'}, content: [ 
                OB.I18N.getLabel('OBPOS_ReceiptTotal')
              ]},                                                                                                                                                                           
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                {kind: B.KindJQuery('strong'), id: 'totalnet'}                                                                                     
              ]},
              {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                   
            ]}
          ]}               
        ]} 
      ]}                                                               
    );
    this.$ = this.component.$;
    this.totalnet = this.component.context.totalnet.$;
    
  };
});    