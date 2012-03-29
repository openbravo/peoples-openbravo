define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderView = function (context) {
  
    var me = this;
    this.orderview = new OB.COMP.TableView({
      stack: context.get('stackorder'),
      style: 'edit',

      renderEmpty: function () {
        return function () {
          return OB.UTIL.EL(
            {tag: 'div', attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
               OB.I18N.getLabel('OBPOS_ReceiptNew')
            ]}
          );
        };            
      },
  
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [
            {tag: 'div', attr: {style: 'float: left; width: 40%'}, content: [ 
              model.get('productidentifier')                                                                
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
              model.printQty()                                                                                                                                                          
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
              model.printPrice()                                                             
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
              model.printNet()
            ]},
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );         
      }      
    });

    this.totalnet = OB.UTIL.EL({tag:'strong'});      
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
        {tag: 'div', attr: {style: 'overflow:auto; height: 500px'}, content: [         
          {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [                                                                                        
            {tag: 'strong', attr: {'style': 'color: green;'}, content: [                                                                                        
              '10:15 - <9332> Federal Lounge'
            ]}        
          ]},           
          {tag: 'div', content: [              
            this.orderview.div,          
            
            {tag: 'ul', attr: {'class': 'unstyled'}, content: [                                                                                        
              {tag: 'li', content: [                                                                                        
                {tag: 'div', attr: {style: 'position: relative; padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                  {tag: 'div', attr: {style: 'float: left; width: 80%; color:  #888888'}, content: [ 
                    OB.I18N.getLabel('OBPOS_ReceiptTaxes')
                  ]},                                                                                                                                                                           
                  {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                    ''
                  ]},
                  {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
                ]}
              ]},
              {tag: 'li', content: [                                                                                        
                {tag: 'div', attr: {style: 'position: relative; padding: 10px;'}, content: [
                  {tag: 'div', attr: {style: 'float: left; width: 80%'}, content: [ 
                    OB.I18N.getLabel('OBPOS_ReceiptTotal')
                  ]},                                                                                                                                                                           
                  {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                    this.totalnet
                  ]},
                  {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
                ]}
              ]}               
            ]} 
          ]}                                                          
        ]}                                                              
      ]}         
    );
    
    // Set Model
    this.receipt =  context.get('modelorder');
    var lines = this.receipt.get('lines');
    
    this.orderview.setModel(lines); 
    
    lines.on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printNet());   
    }, this);
  }
  
  OB.COMP.OrderView.prototype.attr = function (attr, value) {
  };
  OB.COMP.OrderView.prototype.append = function append(child) {
  }; 
  
});    