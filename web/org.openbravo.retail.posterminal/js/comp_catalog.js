(function (OBPOS) {
  
  OBPOS.Sales.Catalog = function (container) {

    this.selectedCategory = null;
    this.selectListeners = [];
    
    this.tbodyCat = $(DOM(NODE('tbody', {}, [])));
    
    this.titleProd = $(DOM(NODE('h3', {id: 'category'}, [' '])));
    this.tbodyProd = $(DOM(NODE('tbody', {}, [])));
    
    container.append($(DOM(
        NODE('div', {'class': 'span8'}, [
          NODE('div', {'class': 'row'}, [
            NODE('div', {'class': 'span4'}, [
              NODE('table', {'class': 'table table-rounded'}, [
                NODE('tbody', {}, [
                  NODE('tr', {}, [
                    NODE('td', {}, [
                      NODE('h3', {}, ['Categories'])            
                    ])          
                  ])
                ])      
              ]),
              NODE('div', {id: 'categoryscroll', style: 'overflow: auto; height: 450px'}, [
                NODE('table', {'class': 'table table-rounded'}, [ this.tbodyCat ])      
              ])          
            ]),
            NODE('div', {'class': 'span4'}, [
              NODE('table', {'class': 'table table-rounded'}, [
                NODE('tbody', {}, [
                  NODE('tr', {}, [
                    NODE('td', {}, [ this.titleProd ])          
                  ])
                ])      
              ]),
              NODE('div', {id: 'productscroll', style: 'overflow: auto; height: 450px'}, [
                NODE('table', {'class': 'table table-rounded'}, [ this.tbodyProd ])      
              ])          
            ])        
          ])
        ])        
    )));   
    
    this.renderCategory = function (cat) {
      return [
        DOM(NODE('td', {'style': 'width:20%;'}, [OBPOS.Sales.getThumbnail(cat.img)])),                                                                                                                                  
        DOM(NODE('td', {'style': 'width:80%;'}, [cat.category._identifier])),                                                                                                                                  
      ];        
    }
    this.renderProduct = function (prod) {
      return [
        DOM(NODE('td', {'style': 'width:20%;'}, [OBPOS.Sales.getThumbnail(prod.img)])),                                                                                                                                  
        DOM(NODE('td', {'style': 'width:80%;'}, [
          NODE('div', {}, [prod.product._identifier]),                                             
          NODE('div', {}, [
            NODE('strong', {}, [
              OBPOS.Format.formatNumber(prod.price.listPrice, {
                decimals: 2,
                decimal: '.',
                group: ',',
                currency: '$#'
              })
            ])  
          ])  
        ])),                                                                                                                                  
      ];        
    }    
  };

  OBPOS.Sales.Catalog.prototype.reloadCategories = function () {
    var me = this;
    OBPOS.Sales.DSCategories.exec({}, function (data) {
      if (data.exception) {
        alert(data.exception.message);
      } else {
        var table = me.tbodyCat.empty();
        me.selectedCategory = null;
        for (var i in data) {
          var tr = $('<tr/>').attr("id", "catrow-" + data[i].id);
          tr.append(me.renderCategory(data[i]));
          tr.click((function (id, name) {
            return function () {
              me.reloadProducts(id, name);
            };
          }(data[i].id, data[i]._identifier)));
          table.append(tr);
        }

        // reload first product
        if (data.length > 0) {
          me.reloadProducts(data[0].id, data[0]._identifier);
        } else {
          me.tbodyProd.empty();
        }
      }
    });
  }

  OBPOS.Sales.Catalog.prototype.reloadProducts = function (category, categoryName) { // 
    var me = this;
    OBPOS.Sales.DSProduct.exec({
      'product': { 'productCategory': category}
    }, function (data) {
      if (data.exception) {
        alert(data.exception.message);
      } else {

        // disable previous category
        if (me.selectedCategory) {
          $('#catrow-' + me.selectedCategory).css('background-color', '').css('color', '');
        }
        me.selectedCategory = category;
        // enable current category
        $('#catrow-' + me.selectedCategory).css('background-color', '#049cdb').css('color', '#fff');

        me.titleProd.text(categoryName);
        var table = me.tbodyProd.empty();
        for (var i in data) {
          var tr = $('<tr/>');
          tr.append(me.renderProduct(data[i]));
          tr.click((function (p) {
            return function () {
              // Fire Select Listener event
              for (var i = 0, max = me.selectListeners.length; i < max; i++) {
                me.selectListeners[i](me, p);
              }
            };
          }(data[i])));
          table.append(tr);

        }
        $('#productscroll').scrollTop(0);
      }
    });
  }

  OBPOS.Sales.Catalog.prototype.addSelectListener = function (l) {
    this.selectListeners.push(l);
  }
  
}(window.OBPOS));  