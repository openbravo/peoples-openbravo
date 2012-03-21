

define(['utilities', 'model/order', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Catalog = function (context) {

    
    this.categoriesview = new OB.COMP.TableView({
      style: 'list',
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'div', attr: {style: 'position: relative; padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
              {tag: 'div', attr: {style: 'float: left; width: 20%'}, content: [ 
                OB.UTIL.getThumbnail(model.get('img'))                                                       
              ]},                                                                                      
              {tag: 'div', attr: {style: 'float: left; width: 80%;'}, content: [ 
                model.get('category')._identifier                                                                                                                                               
              ]},                                                                                      
              {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                  
      }      
    });
    
    this.productsview = new OB.COMP.TableView({ 
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'div', attr: {style: 'position: relative; padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
              {tag: 'div', attr: {style: 'float: left; width: 20%'}, content: [ 
                OB.UTIL.getThumbnail(model.get('img'))
              ]},                                                                                      
              {tag: 'div', attr: {style: 'float: left; width: 60%;'}, content: [ 
                model.get('product')._identifier
              ]},                                                                                      
              {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                {tag: 'strong', content: [ 
                  OB.UTIL.formatNumber(model.get('price').listPrice, {
                    decimals: 2,
                    decimal: '.',
                    group: ',',
                    currency: '$#'
                  })                                                                                                                                            
                ]}                                                                                                                                                                                                                                 
              ]},                                                                                      
              {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                    
      }      
    });   
    
    this.titleProd = OB.UTIL.EL({tag: 'h3'});
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', attr: {'class': 'row-fluid'}, content: [                                                              
        {tag: 'div', attr: {'class': 'span6'}, content: [   
          {tag: 'div', attr: {style: 'overflow:auto; height: 500px'}, content: [ 
            {tag: 'div', attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
              {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                this.titleProd
              ]},
              this.productsview.div
            ]}                   
          ]}                  
        ]},         
        {tag: 'div', attr: {'class': 'span6'}, content: [    
          {tag: 'div', attr: {style: 'overflow:auto; height: 500px'}, content: [ 
            {tag: 'div', attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [ 
              {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                {tag: 'h3', content: [
                  'Categories'
                ]}
              ]},
              this.categoriesview.div
            ]}         
          ]}                   
        ]}         
      ]}         
    );

    
    // Set Model
    this.categories = context.get('modelcategories');;
    this.categoriesview.setModel(this.categories);    
    
    this.products = context.get('modelproducts');
    this.productsview.setModel(this.products);
    
    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');   
    
    this.categoriesview.stack.on('change:selected', function () {
      var selected = this.categoriesview.stack.get('selected')
      if (selected >= 0) {
        this.products.exec({ product: { 'productCategory': this.categories.at(selected).get('category').id } });
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
    
    // Exec
    this.categories.exec();
  };
  
  OB.COMP.Catalog.prototype.attr = function (attr, value) {
  };
  OB.COMP.Catalog.prototype.append = function append(child) {
  };   
});