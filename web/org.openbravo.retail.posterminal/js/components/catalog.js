

define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Catalog = function (container) {

    
    this.categoriesview = new OB.COMP.TableView({
      style: 'list',
      renderLine: function (model) {
        return [
                OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:20%;'}, [OB.UTIL.getThumbnail(model.get('img'))])),                                                                                                                                  
                OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:80%;'}, [model.get('category')._identifier])),                                                                                                                                  
              ];          
      }      
    });
    
    this.productsview = new OB.COMP.TableView({ 
      renderLine: function (model) {
        return [
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:20%;'}, [OB.UTIL.getThumbnail(model.get('img'))])),                                                                                                                                  
              OB.UTIL.DOM(OB.UTIL.NODE('td', {'style': 'width:80%;'}, [
                OB.UTIL.NODE('div', {}, [model.get('product')._identifier]),                                             
                OB.UTIL.NODE('div', {}, [
                  OB.UTIL.NODE('strong', {}, [
                    OB.UTIL.formatNumber(model.get('price').listPrice, {
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
    
    this.titleProd = $(OB.UTIL.DOM(OB.UTIL.NODE('h3', {id: 'category'}, [' '])));
    this.tbodyProd = $(OB.UTIL.DOM(OB.UTIL.NODE('tbody', {}, [])));
    
    container.append($(OB.UTIL.DOM(
        OB.UTIL.NODE('div', {'class': 'span8'}, [
          OB.UTIL.NODE('div', {'class': 'row'}, [
            OB.UTIL.NODE('div', {'class': 'span4'}, [
               
                                             
              OB.UTIL.NODE('table', {'class': 'table table-bordered'}, [
                OB.UTIL.NODE('tbody', {}, [
                  OB.UTIL.NODE('tr', {}, [
                    OB.UTIL.NODE('td', {}, [
                      OB.UTIL.NODE('h3', {}, ['Categories'])            
                    ])          
                  ])
                ])      
              ]),
              this.categoriesview.div      
            ]),
            OB.UTIL.NODE('div', {'class': 'span4'}, [
              OB.UTIL.NODE('table', {'class': 'table table-bordered'}, [
                OB.UTIL.NODE('tbody', {}, [
                  OB.UTIL.NODE('tr', {}, [
                    OB.UTIL.NODE('td', {}, [ this.titleProd ])          
                  ])
                ])      
              ]),
              this.productsview.div          
            ])        
          ])
        ])        
    )));     
  };
  
  OB.COMP.Catalog.prototype.setModel = function (categories, products, receipt, stack) {
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
    
    this.receipt.on('reset', function () {
      if (this.categories.length > 0){
        this.categoriesview.stack.set('selected', 0);
      }
    }, this);    
    
  };
  
});