(function (OBPOS) {

  // Order list
  OBPOS.Sales.OrderView = function (container) {
  
    var me = this;
    
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

  OBPOS.Sales.OrderView.prototype.setModel = function (receipt, stack) {
    this.receipt = receipt;
    var lines = this.receipt.get('lines');
    this.stack = stack;
    this.selected = -1;   
    
    lines.on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printNet());
      this.totalgross.text(this.receipt.printNet());      
    }, this);
    
    lines.on('change', function(model, prop) {          
      var index = lines.indexOf(model);
      this.tbody.children().eq(index)
        .empty()
        .append(this.renderLine(model));      
    }, this);
    
    lines.on('add', function(model, prop, options) {     
      var index = options.index;
      var me = this;
      var tr = $('<tr/>');
      tr.append(this.renderLine(model));
      tr.click(function () {
        me.stack.set('selected', me.receipt.get('lines').indexOf(model));
        me.stack.trigger('gotoedit');
      });
      if (index === lines.length - 1) {
        this.tbody.append(tr);
      } else {
        this.tbody.children().eq(index).before(tr);
      }
      
      this.stack.set('selected', index);     
    }, this);
    
    lines.on('remove', function (model, prop, options) {        
      var index = options.index;
      this.tbody.children().eq(index).remove();

      if (index >= lines.length) {
        this.stack.set('selected', lines.length - 1);
      } else {
        this.stack.trigger('change:selected'); // we need to force the change event.
        // this.stack.set('selected', index);
      }            
    }, this);
    
    lines.on('reset', function() {
      this.tbody.empty();  
      this.stack.set('selected', -1);
    }, this);    
        
    this.stack.on('change:selected', function () {
      var children = this.tbody.children();
      if (this.selected > -1) {
        children.eq(this.selected).css('background-color', '').css('color', '');
      }         
      this.selected = this.stack.get('selected');
      if (this.selected > -1) {
        var elemselected = children.eq(this.selected);      
        elemselected.css('background-color', '#049cdb').css('color', '#fff');
        OBPOS.Sales.makeElemVisible(this.divscroll, elemselected);
      }      
    }, this);
  }

}(window.OBPOS));    