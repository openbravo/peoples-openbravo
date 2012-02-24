(function (OBPOS) {


  
  // Order list
  OBPOS.Sales.Order = function (container) {
    this.changeListeners = [];
    this.selectListeners = [];
    this.clickListeners = [];
    
    var me = this;
    this.changeListeners.push(function () {
      me.totalnet.text(me.receipt.printNet());
      me.totalgross.text(me.receipt.printNet());
    });
    
    this.renderHeader = function () {
      return [
        DOM(NODE('th', {'style': 'width:40%;'}, ['Product'])),                                                                                                                                  
        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Units'])),                                                                                                                                  
        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Price'])),                                                                                                                                  
        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Net']))    
      ];
    };

    this.renderLine = function (l) {
      return [
        DOM(NODE('td', {'style': 'width:40%;'}, [l.productidentifier])),                                                                                                                                  
        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printQty()])),                                                                                                                                  
        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printPrice()])),                                                                                                                                  
        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printNet()]))    
      ];      
    };
    
    this.totalgross = $(DOM(NODE('h3', {}, [])));
    this.totalnet = $(DOM(NODE('strong', {}, [])));                                                                  
    this.tbody = $(DOM(NODE('tbody', {}, [])));
    this.trheader = $(DOM(NODE('tr', {}, [])));
    this.trheader.append(this.renderHeader());
    
    this.divscroll = $(DOM(
      NODE('div', {'style': 'overflow:auto; height: 300px; margin-bottom:30px;'}, [
        NODE('table', {'class': 'table table-bordered'}, [
          NODE('thead', {}, [
            this.trheader                                                                      
          ]),                                                               
          this.tbody
        ])
      ])
    ));
    
    container.append($(DOM(
      NODE('div', {}, [
        NODE('table', {'class': 'table table-bordered'}, [
          NODE('tbody', {}, [
            NODE('tr', {}, [
              NODE('td', {}, ['10:15 - <9332>']),                                                                  
              NODE('td', {'style': 'text-align:right;'}, [ 
                this.totalgross
              ]),                                                                  
            ])                                                                      
          ])                                                                                                   
        ]),
        this.divscroll,
        NODE('table', {'class': 'table table-bordered'}, [
          NODE('tbody', {}, [
            NODE('tr', {}, [
              NODE('td', {}, ['Taxes']),                                                                  
              NODE('td', {'style': 'text-align:right;'}, [
                NODE('strong', {}, [])                                                                  
              ])                                                                 
            ]),     
            NODE('tr', {}, [
              NODE('td', {}, ['Net']),                                                                  
              NODE('td', {'style': 'text-align:right;'}, [
                this.totalnet
              ])                                                                 
            ])               
          ])                                                                                                   
        ])             
      ])
    )));    
  }

  OBPOS.Sales.Order.prototype.createRow = function (line) {
    var me = this;
    var tr = $('<tr/>');
    tr.append(this.renderLine(line));
    tr.click(function () {
      me.setSelected((me.tbody.children().index(tr)), true);
    });
    return tr;
  }

  OBPOS.Sales.Order.prototype.clear = function () {
    this.receipt = new OBPOS.Sales.Receipt();
    
    this.lineselected = -1;

    this.tbody.empty();
    this.fireChangeEvent();
    this.fireSelectEvent(-1, null);
  }

  OBPOS.Sales.Order.prototype.getReceipt = function () {
    return this.receipt;
  }
  
  OBPOS.Sales.Order.prototype.setSelected = function (n, clicked) {
    var children = this.tbody.children();
    if (this.lineselected > -1) {
      children.eq(this.lineselected).css('background-color', '').css('color', '');
    }
    this.lineselected = n;
    if (this.receipt.lines[this.lineselected]) {
      var elemselected = children.eq(this.lineselected);
      elemselected.css('background-color', '#049cdb').css('color', '#fff');
      OBPOS.Sales.makeElemVisible(this.divscroll, elemselected);
      this.fireSelectEvent(this.lineselected, this.receipt.lines[this.lineselected]);
      if (clicked) {
        this.fireClickEvent(this.lineselected, this.receipt.lines[this.lineselected]);
      }
    } else {
      this.lineselected = -1;
      this.fireSelectEvent(-1, null);
    }
  }

  OBPOS.Sales.Order.prototype.addUnit = function (qty) {
    if (this.lineselected > -1) {
      
      qty = isNaN(qty) ? 1 : qty;
      
      var line = this.receipt.lines[this.lineselected];
      
      line.qty += qty;
      var tr = this.tbody.children().eq(this.lineselected).empty();
      tr.append(this.renderLine(line));
      this.fireChangeEvent();
      this.fireSelectEvent(this.lineselected, line);
    }
  }

  OBPOS.Sales.Order.prototype.setUnit = function (qty) {
    if (this.lineselected > -1) {
      
      qty = isNaN(qty) ? this.receipt.lines[this.lineselected].qty : qty;
      
      var line = this.receipt.lines[this.lineselected];
      
      line.qty = qty;
      if (line.qty <= 0) {
        this.removeLine();
      } else {      
        var tr = this.tbody.children().eq(this.lineselected).empty();
        tr.append(this.renderLine(line));
        this.fireChangeEvent();
        this.fireSelectEvent(this.lineselected, line);
      }
    }
  }
  
  OBPOS.Sales.Order.prototype.removeUnit = function (qty) {
    if (this.lineselected > -1) {
      
      qty = isNaN(qty) ? 1 : Math.abs(qty);
      
      var line = this.receipt.lines[this.lineselected];
      
      line.qty -= qty;
      if (line.qty <= 0) {
        this.removeLine();
      } else {
        var tr = this.tbody.children().eq(this.lineselected).empty();
        tr.append(this.renderLine(line));
        this.fireChangeEvent();
        this.fireSelectEvent(this.lineselected, line);
      }
    }
  }

  OBPOS.Sales.Order.prototype.removeLine = function () {
    var l = arguments[0] ? arguments[0] : this.lineselected;
    if (l > -1) {
      this.receipt.lines.splice(l, 1);
      this.tbody.children().eq(l).remove();
      this.fireChangeEvent();

      if (l >= this.receipt.lines.length) {
        this.setSelected(this.receipt.lines.length - 1);
      } else {
        this.setSelected(l);
      }
    }
  }

  OBPOS.Sales.Order.prototype.addLine = function (l) {
    this.receipt.lines.push(l);
    this.tbody.append(this.createRow(l));
    this.setSelected(this.receipt.lines.length - 1);
    this.fireChangeEvent();
  }

  OBPOS.Sales.Order.prototype.addProduct = function (p) {
    if (this.lineselected > -1 &&
        this.receipt.lines[this.lineselected].productid === p.product.id) {
      // add 1 unit to the current line.
      this.addUnit();
    } else {
      // a new line
      var l = new OBPOS.Sales.ReceiptLine({
        productid: p.product.id,
        productidentifier: p.product._identifier,
        qty: 1,
        price: p.price.listPrice
      });
      this.addLine(l);
    }
  }
  
  OBPOS.Sales.Order.prototype.addChangeListener = function (l) {
    this.changeListeners.push(l);
  }

  OBPOS.Sales.Order.prototype.fireChangeEvent = function () {
    for (var i = 0, max = this.changeListeners.length; i < max; i++) {
      this.changeListeners[i](this.receipt);
    }
  }

  OBPOS.Sales.Order.prototype.addSelectListener = function (l) {
    this.selectListeners.push(l);
  }

  OBPOS.Sales.Order.prototype.fireSelectEvent = function (l, line) {
    for (var i = 0, max = this.selectListeners.length; i < max; i++) {
      this.selectListeners[i](l, line);
    }
  }

  OBPOS.Sales.Order.prototype.addClickListener = function (l) {
    this.clickListeners.push(l);
  }

  OBPOS.Sales.Order.prototype.fireClickEvent = function (l, line) {
    for (var i = 0, max = this.clickListeners.length; i < max; i++) {
      this.clickListeners[i](l, line);
    }
  }  
  
  
  // Total
  OBPOS.Sales.Total = function (elem) {
    this.elem = elem;

    this.render = function (receipt) {
      var net = receipt.printNet();
      $('#totalnet').text(net);
      $('#totalgross').text(net);
    }
  }

  OBPOS.Sales.Total.prototype.calculate = function (receipt) {
    this.render(receipt);
  }
  

}(window.OBPOS));    