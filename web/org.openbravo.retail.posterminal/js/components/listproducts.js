/*global B, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListProducts = function (context) {
    var me = this;

    this._id = 'ListProducts';
    this.receipt = context.modelorder;
    this.products = new OB.MODEL.Collection({ds: null});

    this.products.on('click', function (model) {
      this.receipt.addProduct(model);
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
    var criteria, me = this;

    function successCallbackPrices(dataPrices, dataProducts) {
      if(dataPrices && dataPrices.length > 0){
        _.each(dataPrices.models, function(currentPrice){
          if(dataProducts.get(currentPrice.get('product'))){
            dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
          }
        });
        _.each(dataProducts.models, function(currentProd){
          if(currentProd.get('price')===undefined){
            var price = new OB.Model.ProductPrice({'listPrice': 0});
            dataProducts.get(currentProd.get('id')).set('price', price);
            OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
          }
        });
      }else{
        OB.UTIL.showWarning("OBDAL No prices found for products");
        _.each(dataProducts.models, function(currentProd){
          var price = new OB.Model.ProductPrice({'listPrice': 0});
          currentProd.set('price', price);
        });
      }
      me.products.reset(dataProducts.models);
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackProducts(dataProducts) {
      if(dataProducts && dataProducts.length > 0){
        criteria = {'priceListVersion' : OB.POS.modelterminal.get('pricelistversion').id};
        OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
      }else{
        me.products.reset();
      }
      me.titleProd.text(category.get('_identifier'));
    }

    if (category) {
      criteria = {'productCategory': category.get('id')};
      OB.Dal.find(OB.Model.Product, criteria , successCallbackProducts, errorCallback);
    } else {
      this.products.reset();
      this.titleProd.text(OB.I18N.getLabel('OBPOS_LblNoCategory'));
    }
  };
}());