/*global define */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'model/order', 'model/terminal', 'components/table',
        'components/renderproduct'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListProducts = function (context) {
    var me = this;
    
    this._id = 'ListProducts';   
    this.receipt = context.modelorder;
    this.line = null;   
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
    }, this);    
    
    this.products = new OB.MODEL.Collection(context.DataProductPrice);
  
    this.products.on('click', function (model) {
      this.receipt.addProduct(this.line, model);
    }, this);      
     
    this.component = B(                                           
      {kind: B.KindJQuery('div'), content: [ 
        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('h3'), id: 'title'}
        ]},
        {kind: OB.COMP.TableView, id: 'tableview', attr: {
          collection: this.products,
          renderEmpty: function () {
            return (
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                OB.I18N.getLabel('OBPOS_SearchNoResults')
              ]}
            );            
          },
          renderLine: OB.COMP.RenderProduct
        }}
      ]}                           
    );
    this.$el = this.component.$el;
    this.titleProd = this.component.context.title.$el; 
    this.tableview = this.component.context.tableview;    
  };    
  
  OB.COMP.ListProducts.prototype.loadCategory = function (category) {
    if (category) {
      this.products.exec({ priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: {product: { 'productCategory': category.get('category').id }}});    
      this.titleProd.text(category.get('category')._identifier);
    } else {
      this.products.reset();
      this.titleProd.text(OB.I18N.getLabel('OBPOS_LblNoCategory'));
    }
  };
});