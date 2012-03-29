

define(['utilities', 'i18n', 'model/order', 'model/productprice', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Catalog = function (context) {

    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');      
    
    this.categories = new OB.MODEL.Collection(context.get('DataCategory'));
    this.categoriesview = new OB.COMP.TableView({
      style: 'list',
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [
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
    this.categoriesview.setModel(this.categories);    
    this.categoriesview.stack.on('change:selected', function () {
      var selected = this.categoriesview.stack.get('selected')
      if (selected >= 0) {
        this.products.exec({ product: { 'productCategory': this.categories.at(selected).get('category').id } });
        this.titleProd.text(this.categories.at(selected).get('category')._identifier);
      }
    }, this);
        
    
    // this.products = new OB.MODEL.Collection(context.get('DataProduct'));
    this.products = new OB.MODEL.ProductPrice(context.get('modelterminal').get('pricelistversion').id, context.get('DataProduct'), context.get('DataProductPrice'));
    this.productsview = new OB.COMP.TableView({ 
      renderLine: function (model) {
        return OB.UTIL.EL(         
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [
            {tag: 'div', attr: {style: 'float: left; width: 20%'}, content: [ 
              OB.UTIL.getThumbnail(model.get('img'))
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 60%;'}, content: [ 
              model.get('product')._identifier
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
              {tag: 'strong', content: [ 
                OB.I18N.formatCurrency(model.get('price').listPrice)                                                                                                                                         
              ]}                                                                                                                                                                                                                                 
            ]},                                                                                      
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                    
      }      
    });   
    this.productsview.setModel(this.products);
    this.productsview.stack.on('click', function (model,index) {
      this.receipt.addProduct(this.stack.get('selected'), model);
    }, this);    
    
    this.receipt.on('reset', function () {
      if (this.categories.length > 0){
        this.categoriesview.stack.set('selected', 0);
      }
    }, this);  
    
    this.titleProd = OB.UTIL.EL({tag: 'h3'});
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', attr: {'class': 'row-fluid'}, content: [                                                              
        {tag: 'div', attr: {'class': 'span6'}, content: [   
          {tag: 'div', attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
            {tag: 'div', attr: {style: 'overflow:auto; height: 500px'}, content: [ 
              {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
                this.titleProd
              ]},
              this.productsview.div
            ]}                   
          ]}                  
        ]},         
        {tag: 'div', attr: {'class': 'span6'}, content: [    
          {tag: 'div', attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
            {tag: 'div', attr: {style: 'overflow:auto; height: 500px'}, content: [ 
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

    // Exec
    this.categories.exec();
  };
  
  OB.COMP.Catalog.prototype.attr = function (attr, value) {
  };
  OB.COMP.Catalog.prototype.append = function append(child) {
  };   
});