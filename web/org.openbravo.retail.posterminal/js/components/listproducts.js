/*global define */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/productprice', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListProducts = function (context) {
    
    this._id = 'ListProducts';      
    this.receipt = context.get('modelorder');
    this.line = null;   
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
    }, this);    
    
    this.products = new OB.MODEL.ProductPrice(OB.POS.modelterminal.get('pricelistversion').id, context.get('DataProduct'), context.get('DataProductPrice'));
  
    this.products.on('click', function (model) {
      this.receipt.addProduct(this.line, model);
    }, this);    
    
    context.get('ListCategories').categories.on('selected', function (category) {
      if (category) {
        this.products.exec({ product: { 'productCategory': category.get('category').id } });    
        this.titleProd.text(category.get('category')._identifier);      
      }
    }, this);      
     
    this.component = B(                                           
      {kind: B.KindJQuery('div'), content: [ 
        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('h3'), id: 'title'}
        ]},
        {kind: OB.COMP.TableView, attr: {
          collection: this.products,
          renderLine: function (model) {
            return B(         
              {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [ 
                  {kind: OB.UTIL.Thumbnail, attr: {img: model.get('img')}}
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 60%;'}, content: [ 
                  model.get('product')._identifier
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                  {tag: 'strong', content: [ 
                    OB.I18N.formatCurrency(model.get('price').listPrice)                                                                                                                                         
                  ]}                                                                                                                                                                                                                                 
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
              ]}
            );                    
          }            
        }}
      ]}                           
    );
    this.$ = this.component.$;
    this.titleProd = this.component.context.get('title').$; 
  };   
});