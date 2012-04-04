define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchProduct = function (context, id) {
    var me = this;
    
    context.set(id || 'SearchProducts', this);    

    this.receipt = context.get('modelorder');
    this.line = null;   
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
    }, this);    
    
    this.productname = OB.UTIL.EL(
      {tag: 'input', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}}           
    );
    
    this.productcategory = OB.UTIL.EL(
      {tag: 'select', attr: {}}           
    );    
    
    this.products = new OB.MODEL.ProductPrice(context.get('modelterminal').get('pricelistversion').id, context.get('DataProduct'), context.get('DataProductPrice'));    
    this.productsview = new OB.COMP.TableView({ 
      renderEmpty: function () {
        return function () {
          return OB.UTIL.EL(
            {tag: 'div', attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
              OB.I18N.getLabel('OBPOS_SearchNoResults')
            ]}
          );
        };            
      },      
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
                 model.get('price') ?                       
                OB.I18N.formatCurrency(model.get('price').listPrice)                : ''                                                                                                                             
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
    
    this.categories = new OB.MODEL.Collection(context.get('DataCategory'));
    this.categoriesview = new OB.COMP.ListView({
      $: this.productcategory,
      renderHeader: function (model) {
        return OB.UTIL.EL(
          {tag: 'option', attr: {value: ''}, content: [
             OB.I18N.getLabel('OBPOS_SearchAllCategories')                                                                  
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
    
    this.receipt.on('clear', function() {
      this.products.reset();                   
    }, this);    
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: white; color: black; height: 500px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'class': 'row-fluid'}, content: [  
            {tag: 'div', attr: {'class': 'span12', 'style': 'height: 500px; overflow: auto;'}, content: [    
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
                      {tag:'i', attr: {'class': 'icon-search'}}, OB.I18N.getLabel('OBPOS_SearchButtonSearch')
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