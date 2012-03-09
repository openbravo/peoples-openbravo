define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = function (B) {
    var me = this;
    
    this.$ = $("<div/>").load('comp_editline.html', function () {
      
      
      me.keyboard = new OB.COMP.Keyboard();
      $('#keyboardcontainer').append(me.keyboard.$);
      
      
      me.keyboard.on('command', function(cmd) {
        if (cmd === '-') {
          if (this.line) {
            this.line.removeUnit(this.keyboard.getNumber());
            if (this.line.get('qty') <= 0) {
              this.receipt.get('lines').remove(this.line);
            }
          }
        } else if (cmd === '+') {
          if (this.line) {
            this.line.addUnit(this.keyboard.getNumber());
            if (this.line.get('qty') <= 0) {
              this.receipt.get('lines').remove(this.line);
            }        
          }
        } else if (cmd === 'qty') {
          if (this.line) {
            this.line.setUnit(this.keyboard.getNumber());
            if (this.line.get('qty') <= 0) {
              this.receipt.get('lines').remove(this.line);
            }            
          }
        } else if (cmd === String.fromCharCode(13)) {
          this.products.ds.find({
            product: {uPCEAN: this.keyboard.getString()}
          }, function (data) {
            if (data) {      
              this.receipt.addProduct(data);
            } else {
              alert('UPC/EAN code not found');
            }
          });
        }         
      }, me);
      
    });

    // Set Model
    
    this.products = B.get('modelproducts');
    this.receipt = B.get('modelorder');
    this.stack = B.get('orderviewstack');
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
    
    if (this.keyboard) {
      this.keyboard.reset();
    }
      
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