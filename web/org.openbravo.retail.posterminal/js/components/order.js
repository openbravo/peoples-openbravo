define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderView = function (container) {
  
    var me = this;
    this.orderview = new OB.COMP.TableView({
    style: 'edit',
    renderHeader: function () {
      return [
              OB.UTIL.DOM(OB.UTIL.NODE('th', {'style': 'width:40%;'}, ['Product'])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('th', {'style': 'width:20%;text-align:right;'}, ['Units'])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('th', {'style': 'width:20%;text-align:right;'}, ['Price'])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('th', {'style': 'width:20%;text-align:right;'}, ['Net']))                                                                                                                                      
            ];          
    }, 

    renderLine: function (model) {
      return [
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:40%;'}, [model.get('productidentifier')])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printQty()])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printPrice()])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printNet()]))                                                                                                                                  
            ];          
      }      
    });
    
    this.totalgross = $(OB.UTIL.DOM(OB.UTIL.NODE('h3', {}, [])));
    this.totalnet = $(OB.UTIL.DOM(OB.UTIL.NODE('strong', {}, [])));                                                                  
    
    container.append($(OB.UTIL.DOM(
      OB.UTIL.NODE('div', {}, [
        OB.UTIL.NODE('table', {'class': 'table table-bordered'}, [
          OB.UTIL.NODE('tbody', {}, [
            OB.UTIL.NODE('tr', {}, [
              OB.UTIL.NODE('td', {}, ['10:15 - <9332>']),                                                                  
              OB.UTIL.NODE('td', {'style': 'text-align:right;'}, [ 
                this.totalgross
              ]),                                                                  
            ])                                                                      
          ])                                                                                                   
        ]),
        this.orderview.div,
        OB.UTIL.NODE('table', {'class': 'table table-bordered'}, [
          OB.UTIL.NODE('tbody', {}, [
            OB.UTIL.NODE('tr', {}, [
              OB.UTIL.NODE('td', {}, ['Taxes']),                                                                  
              OB.UTIL.NODE('td', {'style': 'text-align:right;'}, [
                OB.UTIL.NODE('strong', {}, [])                                                                  
              ])                                                                 
            ]),     
            OB.UTIL.NODE('tr', {}, [
              OB.UTIL.NODE('td', {}, ['Net']),                                                                  
              OB.UTIL.NODE('td', {'style': 'text-align:right;'}, [
                this.totalnet
              ])                                                                 
            ])               
          ])                                                                                                   
        ])             
      ])
    )));    
  }

  OB.COMP.OrderView.prototype.setModel = function (receipt) {
    this.receipt = receipt;
    var lines = this.receipt.get('lines');
    
    this.orderview.setModel(lines); 
    
    lines.on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printNet());
      this.totalgross.text(this.receipt.printNet());      
    }, this);
  }

});    