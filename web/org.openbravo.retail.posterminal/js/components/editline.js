define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = function (container) {
    var me = this;
    this.container = container;

    container.load('comp_editline.html', function () {
      $('#btnplus').click(function () {
        me.keyPressed('+');
      });
      $('#btnminus').click(function () {
        me.keyPressed('-');
      });
      $('#btnmultiply').click(function () {
        me.keyPressed('*');
      });
      $('#btnremove').click(function () {
        me.keyPressed('x');
      });
      $('#btn0').click(function () {
        me.keyPressed('0');
      });      
      $('#btn1').click(function () {
        me.keyPressed('1');
      });    
      $('#btn2').click(function () {
        me.keyPressed('2');
      }); 
      $('#btn3').click(function () {
        me.keyPressed('3');
      }); 
      $('#btn4').click(function () {
        me.keyPressed('4');
      }); 
      $('#btn5').click(function () {
        me.keyPressed('5');
      }); 
      $('#btn6').click(function () {
        me.keyPressed('6');
      }); 
      $('#btn7').click(function () {
        me.keyPressed('7');
      }); 
      $('#btn8').click(function () {
        me.keyPressed('8');
      }); 
      $('#btn9').click(function () {
        me.keyPressed('9');
      }); 
      $('#btndot').click(function () {
        me.keyPressed('.');
      }); 
      
      $('#btnce').click(function () {
        me.editbox.empty();
      });       
      
      me.editbox = $('#editbox');
      
      // register keys
      $(window).keypress(function(e) {
        me.keyPressed(String.fromCharCode(e.which));
      });        
    });
  }
  
  OB.COMP.EditLine.prototype.setModel = function (products, receipt, stack) {
    this.products = products;
    this.receipt = receipt;
    this.stack = stack;
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
    
    if (this.editbox) { // if it is already loaded the window...
      this.editbox.empty();
      
      if (this.line) {
        var me = this;
        this.products.ds.find({
          product: {id: this.line.get('productid')}
        }, function (data) {
          if (data) {
            $('#editlineimage').empty().append(OB.UTIL.getThumbnail(data.img, 128, 164));
            $('#editlinename').text(data.product._identifier);
            $('#editlineqty').text(me.line.printQty());
            $('#editlineprice').text(me.line.printPrice());
            $('#editlinenet').text(me.line.printNet());
          }
        });
      } else {
        $('#editlineimage').empty();
        $('#editlinename').empty();
        $('#editlineqty').empty();
        $('#editlineprice').empty();
        $('#editlinenet').empty();
      }    
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
  
  OB.COMP.EditLine.prototype.keyPressed = function (key) {

    if (key === '-') {
      if (this.line) {
        this.line.removeUnit(this.getNumber());
        if (this.line.get('qty') <= 0) {
          this.receipt.get('lines').remove(this.line);
        }
      }
    } else if (key === '+') {
      if (this.line) {
        this.line.addUnit(this.getNumber());
        if (this.line.get('qty') <= 0) {
          this.receipt.get('lines').remove(this.line);
        }        
      }
    } else if (key === '*') {
      if (this.line) {
        this.line.setUnit(this.getNumber());
        if (this.line.get('qty') <= 0) {
          this.receipt.get('lines').remove(this.line);
        }            
      }
    } else if (key === 'x') {
      if (this.line) {
        this.receipt.get('lines').remove(this.line);
      }
    } else if (key === String.fromCharCode(13)) {
      OB.COMP.DSProduct.find({
        product: {uPCEAN: this.getString()}
      }, function (data) {
        if (data) {      
          this.receipt.addProduct(data);
        } else {
          alert('UPC/EAN code not found');
        }
      });
    } else {
      var t = this.editbox.text();
      this.editbox.text(t + key);
    }

  }  
  
  OB.COMP.EditLine.prototype.getNumber = function () {
    var i = parseInt(this.editbox.text());
    this.editbox.empty();
    return i;
  }
  
  OB.COMP.EditLine.prototype.getString = function () {
    var s = this.editbox.text();
    this.editbox.empty();
    return s;
  }  

}); 