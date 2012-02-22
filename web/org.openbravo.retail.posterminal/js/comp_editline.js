(function (OBPOS) {

  OBPOS.Sales.EditLine = function (container) {
    var me = this;
    this.container = container;
    this.clickListeners = [];


    container.load('comp_editline.html', function () {
      $('#btnplus').click(function () {
        me.fireClickEvent('+');
      });
      $('#btnminus').click(function () {
        me.fireClickEvent('-');
      });
      $('#btnmultiply').click(function () {
        me.fireClickEvent('*');
      });
      $('#btnremove').click(function () {
        me.fireClickEvent('x');
      });
      $('#btn0').click(function () {
        me.fireClickEvent('0');
      });      
      $('#btn1').click(function () {
        me.fireClickEvent('1');
      });    
      $('#btn2').click(function () {
        me.fireClickEvent('2');
      }); 
      $('#btn3').click(function () {
        me.fireClickEvent('3');
      }); 
      $('#btn4').click(function () {
        me.fireClickEvent('4');
      }); 
      $('#btn5').click(function () {
        me.fireClickEvent('5');
      }); 
      $('#btn6').click(function () {
        me.fireClickEvent('6');
      }); 
      $('#btn7').click(function () {
        me.fireClickEvent('7');
      }); 
      $('#btn8').click(function () {
        me.fireClickEvent('8');
      }); 
      $('#btn9').click(function () {
        me.fireClickEvent('9');
      }); 
      $('#btndot').click(function () {
        me.fireClickEvent('.');
      }); 
      
      $('#btnce').click(function () {
        me.editbox.empty();
      });       
      
      me.editbox = $('#editbox');
    });
  }

  OBPOS.Sales.EditLine.prototype.cleanLine = function () {
    this.editLine(-1, null)
  }
  
  OBPOS.Sales.EditLine.prototype.editLine = function (l, line) {

    this.editbox.empty();
    
    if (l >= 0) {
      OBPOS.Sales.DSProduct.find({
        id: line.productid
      }, function (data) {
        if (data) {
          $('#editlineimage').empty().append(OBPOS.Sales.getThumbnail(data.binaryData, 128, 164));
          $('#editlinename').text(data._identifier);
          $('#editlineqty').text(line.printQty());
          $('#editlineprice').text(line.printPrice());
          $('#editlinenet').text(line.printNet());
        }
      });
    } else {
      $('#editlineimage').empty();
      $('#editlinename').empty();
      $('#editlineqty').empty();
      $('#editlineprice').empty();
      $('#editlinenet').empty();
    }
  };
  
  OBPOS.Sales.EditLine.prototype.typeKey = function (key) {
    var t = this.editbox.text();
    this.editbox.text(t + key);
  }
  
  OBPOS.Sales.EditLine.prototype.getNumber = function () {
    var i = parseInt(this.editbox.text());
    this.editbox.empty();
    return i;
  }
    
  OBPOS.Sales.EditLine.prototype.getNumber = function () {
    var i = parseInt(this.editbox.text());
    this.editbox.empty();
    return i;
  }
  
  OBPOS.Sales.EditLine.prototype.getString = function () {
    var s = this.editbox.text();
    this.editbox.empty();
    return s;
  }  
  
  OBPOS.Sales.EditLine.prototype.addClickListener = function (l) {
    this.clickListeners.push(l);
  };

  OBPOS.Sales.EditLine.prototype.fireClickEvent = function (key) {
    for (var i = 0, max = this.clickListeners.length; i < max; i++) {
      this.clickListeners[i](key);
    }
  };    

}(window.OBPOS));    