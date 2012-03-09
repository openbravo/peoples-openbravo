
define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Keyboard = function () {
    
    me = this;
    _.extend(this, Backbone.Events);
    
    var createbtn = function (command, label) {
      return  $(OB.UTIL.DOM(
        OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, [ label ])                 
      )).click(function () {
        me.keyPressed(command);
      });
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
    
    me.editbox =  $(OB.UTIL.DOM(
      OB.UTIL.NODE('span', {}, [])                 
    ));
    
//    // register keys
//    $(window).keypress(function(e) {
//      me.keyPressed([String.fromCharCode(e.which), me]);
//    }); 
      
      
    this.$ = $(OB.UTIL.DOM(
        OB.UTIL.NODE('div', {'class': 'row-fluid'}, [
          OB.UTIL.NODE('div', {'class': 'span4'}, [
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['---'])    
              ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['---'])    
              ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['---'])    
              ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['---'])    
              ])                                                           
            ]),    
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span12'}, [
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['---'])    
              ])                                                           
            ])    
            
          ]),
          OB.UTIL.NODE('div', {'class': 'span8'}, [
            OB.UTIL.NODE('div', {'class': 'row-fluid'}, [  
              OB.UTIL.NODE('div', {'class': 'span6', 'style': 'text-align: right;'}, [
                OB.UTIL.NODE('pre', {}, [' ', me.editbox])    
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
                OB.UTIL.NODE('button', {'class': 'btn btn-large', 'style': 'width:100%'}, ['@'])    
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
  };
  
  OB.COMP.Keyboard.prototype.attr = function (attr, value) {
  };
  OB.COMP.Keyboard.prototype.append = function append(child) {
  }; 
   
  OB.COMP.Keyboard.prototype.reset = function () {
      this.editbox.empty();      
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