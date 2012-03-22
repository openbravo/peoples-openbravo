define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchProduct = function (context) {
    var me = this;

    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');   
    
    this.productname = OB.UTIL.EL(
      {tag: 'input', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}}           
    );
    
    this.productcategory = OB.UTIL.EL(
      {tag: 'select', attr: {}}           
    );    
    
    this.products = new OB.MODEL.Collection(context.get('DataProduct'));    
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
    this.productsview.setModel(this.products);  
    this.productsview.stack.on('click', function (model,index) {
      this.receipt.addProduct(this.stack.get('selected'), model);
    }, this);
    
    this.categories = new OB.MODEL.Collection(context.get('DataCategory'));
    this.categoriesview = new OB.COMP.ListView({
      $: this.productcategory,
      renderHeader: function (model) {
        return OB.UTIL.EL(
          {tag: 'option', attr: {value: ''}, content: [
             '(All categories)'                                                                                
          ]}
        );                  
      },      
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'option', attr: {value: model.get('category').id}, content: [
              model.get('category')._identifier                                                                                
          ]}
        );                  
      }      
    });    
    this.categoriesview.setModel(this.categories);  
    
    this.receipt.on('reset', function() {
      this.products.reset();                   
    }, this);    
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: white; color: black; height: 500px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'class': 'row-fluid'}, content: [
            {tag: 'div', attr: {'class': 'span2'}, content: [   
               '_'
            ]},                                  
            {tag: 'div', attr: {'class': 'span10', 'style': 'height: 500px; overflow: auto;'}, content: [    
              {tag: 'div', attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
                {tag: 'div', attr: {'class': 'span9'}, content: [    
                  {tag: 'div', attr: {'style': 'padding: 10px'},  content: [    
                    {tag: 'div', content: [    
                      this.productname 
                    ]},
                    {tag: 'div', content: [    
                      this.productcategory 
                    ]}                   
                  ]}                   
                ]},                                                               
                {tag: 'div', attr: {'class': 'span3'}, content: [ 
                  {tag: 'div', attr: {'style': 'padding: 10px'}, content: [    
                    {tag: 'button', attr: {'style': 'width: 100%'}, content: [
                      {tag:'i', attr: {'class': 'icon-search'}}, ' Search'                       
                    ], init: function () {
                      this.click(function () {
                        var filter = {};
                        if (me.productname.val() && me.productname.val() !== '') {
                          filter.product = filter.product || {};
                          filter.product._identifier = '%i' + OB.UTIL.escapeRegExp(me.productname.val());
                        }
                        if (me.productcategory.val() && me.productcategory.val() !== '') {
                          filter.product = filter.product || {};
                          filter.product.productCategory = me.productcategory.val();
                        }
                        
                        // this.products.exec({ product: { 'productCategory': this.categories.at(selected).get('category').id } });
                        me.products.exec(filter);                        
                      });
  
                    }}                                                                   
                  ]}                                                                   
                ]}                    
              ]},
              
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span12'}, content: [    
                  {tag: 'div', content: [ 
                    this.productsview.div
                  ]}                   
                ]}                   
              ]}                                                             
            ]}                                                                   
          ]}                      
        ]}        
      ]}
    );
    
    this.categories.exec({});    
  };
  
}); 