
define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Keyboard = function (context, id) {
    
    context.set(id || 'keyboard', this);
    
    me = this;
    
    var createbtn = function (command, label) {
      
      return OB.UTIL.EL(
        {tag: 'div', attr: {'style': 'margin:5px' }, content: [
          {tag: 'button', attr: {'style': 'width: 100%; height: 40px;' }, content: [
            label
          ], init: function () {
            this.click(function () {
              me.keyPressed(command);  
            });
          }}   
        ]}           
      );
//      return  $(OB.UTIL.DOM(
//        OB.UTIL.NODE('button', { 'style': 'width:100%;'}, [ label ])                 
//      )).click(function () {
//        me.keyPressed(command);
//      });
    }
    
    var btndel = createbtn('del', OB.UTIL.NODE('i', {'class': 'icon-chevron-left'}, []));
    
    var btndiv = createbtn('/', '/');
    var btnmultiply = createbtn('*', '*');
    var btnpercentage = createbtn('%', '%');
    var btnminus = createbtn('-', '-');    
    var btnplus = createbtn('+', '+');
    
    var btnqty = createbtn('qty', 'Quantity');
    var btnprice = createbtn('price', 'Price');
    var btndto = createbtn('dto', 'Discount');
    var btnreturn = createbtn(String.fromCharCode(13), OB.UTIL.NODE('i', {'class': 'icon-ok'}, []));

    var btn0 = createbtn('0', '0');
    var btn1 = createbtn('1', '1');
    var btn2 = createbtn('2', '2');
    var btn3 = createbtn('3', '3');
    var btn4 = createbtn('4', '4');
    var btn5 = createbtn('5', '5');
    var btn6 = createbtn('6', '6');
    var btn7 = createbtn('7', '7');
    var btn8 = createbtn('8', '8');
    var btn9 = createbtn('9', '9');
    var btndot = createbtn('.', '.');
    
    var btnaction1 = createbtn('---', '---');
    var btnaction2 = createbtn('---', '---');
    var btnaction3 = createbtn('---', '---');
    var btnaction4 = createbtn('---', '---');
    var btnaction5 = createbtn('---', '---');
    var btnaction6 = createbtn('---', '---');
    
    me.editbox =  $(OB.UTIL.DOM(
      OB.UTIL.NODE('span', {}, [])                 
    ));
    
//    // register keys
//    $(window).keypress(function(e) {
//      me.keyPressed([String.fromCharCode(e.which), me]);
//    }); 

//    this.$ = OB.UTIL.EL(
//      {tag: 'div', attr: {'style': 'width:100%' }, content: [
//        {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//          {tag: 'div', content: [
//            {tag: 'button', attr: { 'style': 'width:100%;'}, content: [ '---']}                               
//          ]},
//          {tag: 'div', content: [
//            {tag: 'button', attr: { 'style': 'width:100%;'}, content: [ '---']}                               
//          ]},
//          {tag: 'div', content: [
//            {tag: 'button', attr: { 'style': 'width:100%;'}, content: [ '---']}                               
//          ]}                    
//        ]},
//        {tag: 'div', attr: {'style': 'float:left; width: 80%' }, content: [
//          {tag: 'div', attr: {'style': 'position:relative; width: 100%' }, content: [
//            {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//              {tag: 'div', attr: {'style': 'margin:5px' }, content: [
//                btndiv
//              ]}                                  
//            ]},                                  
//            {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//              btnmultiply
//            ]},                                  
//            {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//              btnpercentage
//            ]},                                  
//            {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//              btnminus
//            ]},                                  
//            {tag: 'div', attr: {'style': 'float:left; width: 20%' }, content: [
//              btnplus
//            ]}                                  
//                                 
//          ]}                         
//        ]}
//                                            
//                             
//      ]}
//    );      
    this.$ = $(OB.UTIL.DOM(
        OB.UTIL.NODE('div', {'class': 'row-fluid'}, [
          OB.UTIL.NODE('div', {'class': 'span3'}, [
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction1 ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction2 ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction3 ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction4 ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction5 ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [ btnaction6 ])                                                           
            ])    
            
          ]),
          OB.UTIL.NODE('div', {'class': 'span9'}, [
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span6'}, [  
                OB.UTIL.NODE('div', {'style': 'margin:5px'}, [
                  OB.UTIL.NODE('div', {'style': 'text-align: right; width: 100%; height: 40px;'}, [
                    OB.UTIL.NODE('pre', {'style': '' }, [' ', me.editbox])    
                  ])
                ])
              ]), 
              OB.UTIL.NODE('div', {'class': 'span6'}, [ btndel ])               
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btndiv]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btnmultiply ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btnpercentage ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btnminus ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btnplus ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [
              ])                                                          
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn7 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn8 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn9 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span6'}, [ btnqty ])                                                          
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn4 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn5 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn6 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span6'}, [ btnprice ])                                                                
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn1 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn2 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btn3 ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span6'}, [ btndto])                                                                
            ]), 
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span4'}, [ btn0 ]),                                                                                                                     
              OB.UTIL.NODE('div', {'class': 'span2'}, [ btndot ]),                                                           
              OB.UTIL.NODE('div', {'class': 'span6'}, [ btnreturn ])                                                              
            ])       
          ])                  
        ])        
    ));       
    
    
    this.products = context.get('modelproducts');
    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');
    this.line = null;
    this.index = -1;
        
    this.stack.on('change:selected', function () {
      
      var index = this.stack.get('selected');
      var lines = this.receipt.get('lines');
 
      if (index >= 0 && index < lines.length) {  
        this.line = lines.at(index);
        this.index = index;      
      } else {
        this.line = null;
        this.index = -1;    
      }
      this.reset();
      
    }, this);  
    
    
    this.on('command', function(cmd) {
      var me = this;      
      if (cmd === '-') {
        if (this.line) {
          this.receipt.removeUnit(this.line, this.getNumber());      
        }
      } else if (cmd === '+') {
        if (this.line) {
          this.receipt.addUnit(this.line, this.getNumber());      
        }
      } else if (cmd === 'qty') {
        if (this.line) {
          this.receipt.setUnit(this.line, this.getNumber());           
        }
      } else if (cmd === String.fromCharCode(13)) {

        this.products.ds.find({
          product: {uPCEAN: this.getString()}
        }, function (data) {
          if (data) {      
            me.receipt.addProduct(me.index, new OB.MODEL.Product(data));
          } else {
            alert('UPC/EAN code not found');
          }
        });
      }         
    }, this);       
    
    
  };
  
  _.extend(OB.COMP.Keyboard.prototype, Backbone.Events);
  
  OB.COMP.Keyboard.prototype.attr = function (attr, value) {
  };
  OB.COMP.Keyboard.prototype.append = function append(child) {
  }; 
   
  OB.COMP.Keyboard.prototype.reset = function () {
      this.editbox.empty();      
  };
  
  OB.COMP.Keyboard.prototype.show = function () {
      this.reset();
      this.$.show();      
  };
  
  OB.COMP.Keyboard.prototype.hide = function () {
      this.$.hide();    
  };
  
  OB.COMP.Keyboard.prototype.getNumber = function () {
    var i = parseInt(this.editbox.text());
    this.editbox.empty();
    return i;
  };
  
  OB.COMP.Keyboard.prototype.getString = function () {
    var s = this.editbox.text();
    this.editbox.empty();
    return s;
  };  
  
  OB.COMP.Keyboard.prototype.keyPressed = function (key) {
    if (key.match(/^([0-9]|\.|[a-z])$/)) {
      var t = this.editbox.text();
      this.editbox.text(t + key);
    } else if (key === 'del') {
      var t = this.editbox.text();
      if (t.length > 0) {
        this.editbox.text(t.substring(0, t.length - 1));
      }
    } else {
      this.trigger('command', key);
    }
  }; 
    
});