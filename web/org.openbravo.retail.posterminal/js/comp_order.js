(function (OBPOS) {


  
  // Order list
  OBPOS.Sales.OrderView = function (container) {
    this.changeListeners = [];
    this.selectListeners = [];
    this.clickListeners = [];
    
    var me = this;
    
//    this.changeListeners.push(function () {
//      me.totalnet.text(me.receipt.printNet());
//      me.totalgross.text(me.receipt.printNet());
//    });
    
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
        DOM(NODE('td', {'style': 'width:40%;'}, [l.get('productidentifier')])),                                                                                                                                  
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

  OBPOS.Sales.OrderView.prototype.createRow = function (line) {
    var me = this;
    var tr = $('<tr/>');
    tr.append(this.renderLine(line));
    tr.click(function () {
      me.setSelected((me.tbody.children().index(tr)), true);
    });
    return tr;
  }

  OBPOS.Sales.OrderView.prototype.clear = function () {
    
    // Init the receipt
    this.receipt = new OBPOS.Model.Order();
    
    this.receipt.get('lines').on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printNet());
      this.totalgross.text(this.receipt.printNet());      
    }, this);

    this.receipt.get('lines').reset();
    
    // Init the view
    this.lineselected = -1;

    this.tbody.empty();
    this.fireChangeEvent();
    this.fireSelectEvent(-1, null);
  }

  OBPOS.Sales.OrderView.prototype.getReceipt = function () {
    return this.receipt;
  }
  
  OBPOS.Sales.OrderView.prototype.setSelected = function (n, clicked) {
    var children = this.tbody.children();
    if (this.lineselected > -1) {
      children.eq(this.lineselected).css('background-color', '').css('color', '');
    }
    this.lineselected = n;
    
    var line = this.receipt.get('lines').at(this.lineselected);
    
    if (line) {
      var elemselected = children.eq(this.lineselected);
      elemselected.css('background-color', '#049cdb').css('color', '#fff');
      OBPOS.Sales.makeElemVisible(this.divscroll, elemselected);
      this.fireSelectEvent(this.lineselected, line);
      if (clicked) {
        this.fireClickEvent(this.lineselected, line);
      }
    } else {
      this.lineselected = -1;
      this.fireSelectEvent(-1, null);
    }
  }

  OBPOS.Sales.OrderView.prototype.addUnit = function (qty) {
    if (this.lineselected > -1) {
  
      var line = this.receipt.get('lines').at(this.lineselected);
      
      qty = isNaN(qty) ? 1 : qty;
      
      line.set('qty', line.get('qty') + qty);
      var tr = this.tbody.children().eq(this.lineselected).empty();
      tr.append(this.renderLine(line));
      this.fireChangeEvent();
      this.fireSelectEvent(this.lineselected, line);
    }
  }

  OBPOS.Sales.OrderView.prototype.setUnit = function (qty) {
    if (this.lineselected > -1) {
      
      var line = this.receipt.get('lines').at(this.lineselected);
      
      qty = isNaN(qty) ? line.get('qty') : qty;
      
      line.set('qty', qty);
      if (line.get('qty') <= 0) {
        this.removeLine();
      } else {      
        var tr = this.tbody.children().eq(this.lineselected).empty();
        tr.append(this.renderLine(line));
        this.fireChangeEvent();
        this.fireSelectEvent(this.lineselected, line);
      }
    }
  }
  
  OBPOS.Sales.OrderView.prototype.removeUnit = function (qty) {
    if (this.lineselected > -1) {
      
      qty = isNaN(qty) ? 1 : Math.abs(qty);
      
      var line = this.receipt.get('lines').at(this.lineselected);
      
      line.set('qty', line.get('qty') - qty);
      if (line.get('qty') <= 0) {
        this.removeLine();
      } else {
        var tr = this.tbody.children().eq(this.lineselected).empty();
        tr.append(this.renderLine(line));
        this.fireChangeEvent();
        this.fireSelectEvent(this.lineselected, line);
      }
    }
  }

  OBPOS.Sales.OrderView.prototype.removeLine = function () {
    var l = arguments[0] ? arguments[0] : this.lineselected;
    if (l > -1) {
      
      var line = this.receipt.get('lines').at(this.lineselected);
      
      this.receipt.get('lines').remove(line);
      this.tbody.children().eq(l).remove();
      this.fireChangeEvent();

      if (l >= this.receipt.get('lines').length) {
        this.setSelected(this.receipt.get('lines').length - 1);
      } else {
        this.setSelected(l);
      }
    }
  }

  OBPOS.Sales.OrderView.prototype.addLine = function (l) {
    this.receipt.get('lines').add(l);
    this.tbody.append(this.createRow(l));
    this.setSelected(this.receipt.get('lines').length - 1);
    this.fireChangeEvent();
  }

  OBPOS.Sales.OrderView.prototype.addProduct = function (p) {
    if (this.lineselected > -1 &&
        this.receipt.get('lines').at(this.lineselected).get('productid') === p.product.id) {
      // add 1 unit to the current line.
      this.addUnit();
    } else {
      // a new line
      var l = new OBPOS.Model.OrderLine({
        productid: p.product.id,
        productidentifier: p.product._identifier,
        qty: 1,
        price: p.price.listPrice
      });
      this.addLine(l);
    }
  }
  
  OBPOS.Sales.OrderView.prototype.addChangeListener = function (l) {
    this.changeListeners.push(l);
  }

  OBPOS.Sales.OrderView.prototype.fireChangeEvent = function () {
    for (var i = 0, max = this.changeListeners.length; i < max; i++) {
      this.changeListeners[i](this.receipt);
    }
  }

  OBPOS.Sales.OrderView.prototype.addSelectListener = function (l) {
    this.selectListeners.push(l);
  }

  OBPOS.Sales.OrderView.prototype.fireSelectEvent = function (l, line) {
    for (var i = 0, max = this.selectListeners.length; i < max; i++) {
      this.selectListeners[i](l, line);
    }
  }

  OBPOS.Sales.OrderView.prototype.addClickListener = function (l) {
    this.clickListeners.push(l);
  }

  OBPOS.Sales.OrderView.prototype.fireClickEvent = function (l, line) {
    for (var i = 0, max = this.clickListeners.length; i < max; i++) {
      this.clickListeners[i](l, line);
    }
  }  
 

}(window.OBPOS));    