/*global Backbone, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListProducts = Backbone.View.extend({
    optionsid: 'ListProducts',
    tag: 'div',
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'
      },
      content: [{
        id: 'title',
        tag: 'h3'
      }]
    }, {
      id: 'tableview',
      view: OB.UI.TableView.extend({
        renderEmpty: OB.COMP.RenderEmpty,
        renderLine: OB.COMP.RenderProduct
      })
    }],
    initialize: function () {

      this.options[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      var me = this;
      this.receipt = this.options.modelorder;
      this.products = new OB.Collection.ProductList();
      this.tableview.registerCollection(this.products);

      this.products.on('click', function (model) {
        this.receipt.addProduct(model);
      }, this);
    },
    loadCategory: function (category) {
      var criteria, me = this;

      function successCallbackPrices(dataPrices, dataProducts) {
        if (dataPrices && dataPrices.length > 0) {
          _.each(dataPrices.models, function (currentPrice) {
            if (dataProducts.get(currentPrice.get('product'))) {
              dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
            }
          });
          _.each(dataProducts.models, function (currentProd) {
            if (currentProd.get('price') === undefined) {
              var price = new OB.Model.ProductPrice({
                'listPrice': 0
              });
              dataProducts.get(currentProd.get('id')).set('price', price);
              OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
            }
          });
        } else {
          OB.UTIL.showWarning("OBDAL No prices found for products");
          _.each(dataProducts.models, function (currentProd) {
            var price = new OB.Model.ProductPrice({
              'listPrice': 0
            });
            currentProd.set('price', price);
          });
        }
        me.products.reset(dataProducts.models);
      }

      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackProducts(dataProducts) {
        if (dataProducts && dataProducts.length > 0) {
          criteria = {
            'priceListVersion': OB.POS.modelterminal.get('pricelistversion').id
          };
          OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
        } else {
          me.products.reset();
        }
        me.title.text(category.get('_identifier'));
      }

      if (category) {
        criteria = {
          'productCategory': category.get('id')
        };
        OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
      } else {
        this.products.reset();
        this.title.text(OB.I18N.getLabel('OBPOS_LblNoCategory'));
      }
    }
  });
}());