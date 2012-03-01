(function (OBPOS) {

  // Order list
  OBPOS.Sales.OrderView = function (container) {
  
    var me = this;
    this.orderview = new OBPOS.Sales.TableView({
    style: 'edit',
    renderHeader: function () {
      return [
              DOM(NODE('th', {'style': 'width:40%;'}, ['Product'])),                                                                                                                                  
              DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Units'])),                                                                                                                                  
              DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Price'])),                                                                                                                                  
              DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Net']))                                                                                                                                      
            ];          
    }, 

    renderLine: function (model) {
      return [
              DOM(NODE('td', {'style': 'width:40%;'}, [model.get('productidentifier')])),                                                                                                                                  
              DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printQty()])),                                                                                                                                  
              DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printPrice()])),                                                                                                                                  
              DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [model.printNet()]))                                                                                                                                  
            ];          
      }      
    });
    
//    this.renderHeader = function () {
//      return [
//        DOM(NODE('th', {'style': 'width:40%;'}, ['Product'])),                                                                                                                                  
//        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Units'])),                                                                                                                                  
//        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Price'])),                                                                                                                                  
//        DOM(NODE('th', {'style': 'width:20%;text-align:right;'}, ['Net']))    
//      ];
//    };
//
//    this.renderLine = function (l) {
//      return [
//        DOM(NODE('td', {'style': 'width:40%;'}, [l.get('productidentifier')])),                                                                                                                                  
//        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printQty()])),                                                                                                                                  
//        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printPrice()])),                                                                                                                                  
//        DOM(NODE('td', {'style': 'width:20%;text-align:right;'}, [l.printNet()]))    
//      ];      
//    };
    
    this.totalgross = $(DOM(NODE('h3', {}, [])));
    this.totalnet = $(DOM(NODE('strong', {}, [])));                                                                  
//    this.tbody = $(DOM(NODE('tbody', {}, [])));
//    this.trheader = $(DOM(NODE('tr', {}, [])));
//    this.trheader.append(this.renderHeader());
    
//    this.divscroll = $(DOM(
//      NODE('div', {'style': 'overflow:auto; height: 300px; margin-bottom:30px;'}, [
//        NODE('table', {'class': 'table table-bordered'}, [
//          NODE('thead', {}, [
//            this.trheader                                                                      
//          ]),                                                               
//          this.tbody
//        ])
//      ])
//    ));
    
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
        this.orderview.div,
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

  OBPOS.Sales.OrderView.prototype.setModel = function (receipt) {
    this.receipt = receipt;
    var lines = this.receipt.get('lines');
    
    this.orderview.setModel(lines); 
    
    lines.on('reset change add remove', function() {
      this.totalnet.text(this.receipt.printNet());
      this.totalgross.text(this.receipt.printNet());      
    }, this);
  }

}(window.OBPOS));    