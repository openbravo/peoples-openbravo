

define(['utilities', 'i18n', 'model/order', 'model/productprice', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListProducts = function (context, id) {
    
    context.set(id || 'ListProducts', this);      

    this.receipt = context.get('modelorder');
    this.line = null;   
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
    }, this);    
    
    this.products = new OB.MODEL.ProductPrice(context.get('modelterminal').get('pricelistversion').id, context.get('DataProduct'), context.get('DataProductPrice'));
    this.productsview = new OB.COMP.CollectionView({ 
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
    this.products.on('click', function (model) {
      this.receipt.addProduct(this.line, model);
    }, this);    
  
    this.titleProd = OB.UTIL.EL({tag: 'h3'});
    
    this.$ = OB.UTIL.EL(                                           
      {tag: 'div', content: [ 
        {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          this.titleProd
        ]},
        this.productsview.div
      ]}                           
    );
    
    context.get('ListCategories').categories.on('selected', function (category) {
      if (category) {
        this.products.exec({ product: { 'productCategory': category.get('category').id } });    
        this.titleProd.text(category.get('category')._identifier);      
      }
    }, this);   
  };
  
  OB.COMP.ListProducts.prototype.attr = function (attr, value) {
  };
  OB.COMP.ListProducts.prototype.append = function append(child) {
  };   
});