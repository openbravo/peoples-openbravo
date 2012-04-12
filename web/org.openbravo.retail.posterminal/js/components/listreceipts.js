/*global define */

define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListReceipts = function (context) {
    var me = this;
    
    this.id = 'ListReceipts';

    this.receipt = context.get('modelorder');
    this.receiptlist = context.get('modelorderlist');
 
    this.receiptsview = new OB.COMP.TableView({ 
      renderEmpty: function () {
        return function () {
          return OB.UTIL.EL(
            {tag: 'div', attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
              OB.I18N.getLabel('OBPOS_SearchNoResults')
            ]}
          );
        };            
      },      
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [                                                                                                                                                                        
            {tag: 'div', attr: {style: 'float: left; width: 80%;'}, content: [ 
              OB.I18N.formatHour(model.get('date')) + ' - <9332> ', model.get('bp') ? model.get('bp').get('_identifier') : ''
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
              {tag: 'strong', content: [ 
                 model.printNet()                                                                                                                             
              ]}                                                                                                                                                                                                                                 
            ]},              
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                    
      }      
    });
    this.receiptsview.setModel(this.receiptlist);  
    this.receiptlist.on('click', function (model, index) {
      this.receiptlist.load(model);
    }, this);
   
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: white; color: black; height: 500px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [  
          ]},
          {tag: 'div', attr: {'class': 'row-fluid'}, content: [
            {tag: 'div', attr: {'class': 'span12', 'style': 'height: 500px; overflow: auto;'}, content: [    
             
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span12'}, content: [    
                  {tag: 'div', content: [ 
                    this.receiptsview.div
                  ]}                   
                ]}                   
              ]}                                                             
            ]}                                                                   
          ]}                      
        ]}        
      ]}
    );
  };
  
}); 