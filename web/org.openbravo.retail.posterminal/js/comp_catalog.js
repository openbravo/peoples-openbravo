(function (OBPOS) {
  
  OBPOS.Sales.Catalog = function (container) {

    
    this.categoriesview = new OBPOS.Sales.TableView({
//      renderHeader: function (model) {
//        return [
//                DOM(NODE('td', {'style': 'width:20%;'}, [OBPOS.Sales.getThumbnail(model.get('img'))])),                                                                                                                                  
//                DOM(NODE('td', {'style': 'width:80%;'}, [model.get('category')._identifier])),                                                                                                                                  
//              ];          
//      }, 
      style: 'list',
      renderLine: function (model) {
        return [
                DOM(NODE('td', {'style': 'width:20%;'}, [OBPOS.Sales.getThumbnail(model.get('img'))])),                                                                                                                                  
                DOM(NODE('td', {'style': 'width:80%;'}, [model.get('category')._identifier])),                                                                                                                                  
              ];          
      }      
    });
    
    this.productsview = new OBPOS.Sales.TableView({ 
      renderLine: function (model) {
        return [
              DOM(NODE('td', {'style': 'width:20%;'}, [OBPOS.Sales.getThumbnail(model.get('img'))])),                                                                                                                                  
              DOM(NODE('td', {'style': 'width:80%;'}, [
                NODE('div', {}, [model.get('product')._identifier]),                                             
                NODE('div', {}, [
                  NODE('strong', {}, [
                    OBPOS.Format.formatNumber(model.get('price').listPrice, {
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
    });   
    
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
              this.categoriesview.div      
            ]),
            NODE('div', {'class': 'span4'}, [
              NODE('table', {'class': 'table table-rounded'}, [
                NODE('tbody', {}, [
                  NODE('tr', {}, [
                    NODE('td', {}, [ this.titleProd ])          
                  ])
                ])      
              ]),
              this.productsview.div          
            ])        
          ])
        ])        
    )));     
  };
  
  OBPOS.Sales.Catalog.prototype.setModel = function (categories, products, receipt, stack) {
    this.categories = categories;
    this.categoriesview.setModel(this.categories);    
    
    this.products = products;
    this.productsview.setModel(this.products);
    
    this.receipt = receipt;
    this.stack = stack;   
    
    this.categoriesview.stack.on('change:selected', function () {
      var selected = this.categoriesview.stack.get('selected')
      if (selected >= 0) {
        this.products.load({ product: { 'productCategory': this.categories.at(selected).get('category').id } });
        this.titleProd.text(this.categories.at(selected).get('category')._identifier);
      }
    }, this);
    
    this.productsview.stack.on('click', function (model,index) {
      this.receipt.addProduct(this.stack.get('selected'), model);
    }, this);
    
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
          var tr = $('<tr/>').attr("id", "catrow-" + data[i].category.id);
          tr.append(me.renderCategory(data[i]));
          tr.click((function (id, name) {
            return function () {
              me.reloadProducts(id, name);
            };
          }(data[i].category.id, data[i].category._identifier)));
          table.append(tr);
        }

        // reload first product
        if (data.length > 0) {
          me.reloadProducts(data[0].category.id, data[0].category._identifier);
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
              me.receipt.addProduct(me.stack.get('selected'), p);
            };
          }(data[i])));
          table.append(tr);

        }
        $('#productscroll').scrollTop(0);
      }
    });
  }
  
}(window.OBPOS));  