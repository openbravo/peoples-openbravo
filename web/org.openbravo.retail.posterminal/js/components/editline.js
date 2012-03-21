define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = function (context) {
    var me = this;   
    
    this.editlineimage = OB.UTIL.EL(
      {tag: 'div', attr: {'class': 'span4'}}
    );
    this.editlinename = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlineqty = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlineprice = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlinenet = OB.UTIL.EL(
      {tag: 'strong'}
    );

    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #7da7d9; color: white; height: 250px; margin: 5px; padding: 5px'}, content: [                             
          {tag: 'div', attr: {'style': 'padding: 10px'}, content: [
            {tag: 'button', attr: {'style': 'margin: 5px;' }, content: [
              'Delete'                       
            ], init: function () {
              this.click(function() {
                if (me.line) {
                  me.receipt.deleteLine(me.line);        
                }              
              });
            }}                               
          ]},
          {tag: 'div', attr: {'class': 'row-fluid', 'style': 'padding: 10px'}, content: [
            {tag: 'div', attr: {'class': 'span8'}, content: [
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Description'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  this.editlinename                
                ]}                
              ]},      
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Quantity'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  this.editlineqty 
                ]}                
              ]},                                                             
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Price'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  this.editlineprice
                ]}                
              ]},                                                             
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Line value'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  {tag: 'strong', content: [                                
                  ]}                
                ]}                
              ]},                                                             
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Discount'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  {tag: 'strong', content: [                                
                  ]}                
                ]}                
              ]},                                                             
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span4'}, content: [
                  'Total'                                                                         
                ]},  
                {tag: 'div', attr: {'class': 'span8'}, content: [                                
                  this.editlinenet           
                ]}                
              ]}                                                                    
            ]},                             
            this.editlineimage                                                       
          ]} 
        ]}          
      ]}
    );

    // Set Model
    
    this.products = context.get('modelproducts');
    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');
    this.line = null;
    this.index = -1;
        
    this.stack.on('change:selected', function () {
      
      var index = this.stack.get('selected');
      var lines = this.receipt.get('lines');
      if (index >= 0 && index < lines.length) {  
        this.editLine(index, lines.at(index));     
      } else {
        this.editLine(-1, null);
      }
    }, this);    
  };
  
  OB.COMP.EditLine.prototype.renderLine = function () {
    
    var me = this;  
    if (this.line) {      
      this.products.ds.find({
        product: {id: this.line.get('productid')}
      }, function (data) {
        if (data) {
          me.editlineimage.empty().append(OB.UTIL.getThumbnail(data.img, 128, 164));
          me.editlinename.text(data.product._identifier);
          me.editlineqty.text(me.line.printQty());
          me.editlineprice.text(me.line.printPrice());
          me.editlinenet.text(me.line.printNet());
        }
      });
    } else {
      me.editlineimage.empty();
      me.editlinename.empty();
      me.editlineqty.empty();
      me.editlineprice.empty();
      me.editlinenet.empty();
    }    
  }  
  
  OB.COMP.EditLine.prototype.editLine = function (index, line) {
    
    if (this.line) {
      this.line.off('change', this.renderLine);
    }
    
    this.line = line;
    this.index = index;
    
    if (this.line) {
      this.line.on('change', this.renderLine, this);     
    }
    
    this.renderLine();
  };
  
}); 