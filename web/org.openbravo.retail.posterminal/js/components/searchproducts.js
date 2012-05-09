/*global define */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchProduct = function (context) {
    var me = this;
    
    this._id = 'SearchProducts';    

    this.receipt = context.modelorder;
    
    this.line = null;  
    
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
    }, this);    
    
    this.categories = new OB.MODEL.Collection(context.DataCategory);   
    this.products = new OB.MODEL.Collection(context.DataProductPrice);

    this.products.on('click', function (model) {
      this.receipt.addProduct(this.line, model);
    }, this);
    
    this.receipt.on('clear', function() {
      this.productname.val('');
      this.productcategory.val('');
      this.products.exec({priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: {}});
    }, this);    
    
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [  
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [    
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span9'}, content: [    
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'},  content: [    
                {kind: B.KindJQuery('div'), content: [    
                  {kind: B.KindJQuery('input'), id: 'productname', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}}           
                ]},
                {kind: B.KindJQuery('div'), content: [    
                  {kind: OB.COMP.ListView('select'), id: 'productcategory', attr: {
                    collection: this.categories,
                    renderHeader: function (model) {
                      return B(
                        {kind: B.KindJQuery('option'), attr: {value: ''}, content: [
                           OB.I18N.getLabel('OBPOS_SearchAllCategories')
                        ]}
                      );                  
                    },    
                    renderLine: function (model) {
                      return B(
                        {kind: B.KindJQuery('option'), attr: {value: model.get('category').id}, content: [
                            model.get('category')._identifier                                                                                
                        ]}
                      );                  
                    }              
                  }}   
                ]}                   
              ]}                   
            ]},                                                               
            {kind: B.KindJQuery('div'), attr: {'class': 'span3'}, content: [ 
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'}, content: [    
                {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-gray', 'style': 'float:right;'}, content: [
                  {kind: B.KindJQuery('i'), attr: {'class': 'icon-search'}}, OB.I18N.getLabel('OBPOS_SearchButtonSearch')
                ], init: function () {
                  this.$.click(function (e) {
                    e.preventDefault();
                    var filter = {};
                    if (me.productname.val() && me.productname.val() !== '') {
                      filter.product = filter.product || {};
                      filter.product._identifier = '%i' + OB.UTIL.escapeRegExp(me.productname.val());
                    }
                    if (me.productcategory.val() && me.productcategory.val() !== '') {
                      filter.product = filter.product || {};
                      filter.product.productCategory = me.productcategory.val();
                    }                   
                    me.products.exec({priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: filter});                        
                  });
                }}                                                                   
              ]}                                                                   
            ]}                    
          ]},
          
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [    
              {kind: B.KindJQuery('div'), content: [ 
                {kind: OB.COMP.TableView, id: 'tableview', attr: {
                  collection: this.products,
                  renderEmpty: function () {
                    return B(
                      {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                        OB.I18N.getLabel('OBPOS_SearchNoResults')
                      ]}
                    );            
                  }       
                }}
              ]}                   
            ]}                   
          ]}                                                             
        ]}                                                                   
      ]}                      
    );
    this.$ = this.component.$;
    this.productname = this.component.context.productname.$;
    this.productcategory = this.component.context.productcategory.$;
    this.tableview = this.component.context.tableview;       
    this.tableview.renderLine = function (model) {
      return B(
        {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [                                                                                   
          {kind: B.KindJQuery('div'), content: [ 
            model.get('product')._identifier
          ]}                                                                      
        ]}
      );                    
    };
 
    this.categories.exec({});    
  };
  
  OB.COMP.SearchProduct.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;      
  };
}); 