(function (OBPOS) {
  
  OBPOS.Sales.Catalog = function (container) {

    
    this.categoriesview = new OBPOS.Sales.TableView({
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
  
}(window.OBPOS));  