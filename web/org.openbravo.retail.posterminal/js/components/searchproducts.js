/*global B , Backbone, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchProduct = Backbone.View.extend({
	initialize: function(){
	  var me = this;

    this.receipt = this.options.modelorder;

    this.categories = new OB.MODEL.Collection({ds: null});
    this.products = new OB.MODEL.Collection({ds: null});

    this.products.on('click', function (model) {
      this.receipt.addProduct(model);
    }, this);

    this.receipt.on('clear', function() {
      this.productname.val('');
      this.productcategory.val('');
      //A filter should be set before show products. -> Big data!!
      //this.products.exec({priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: {}});
    }, this);

    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'},  content: [
                {kind: B.KindJQuery('div'), content: [
                  {kind: B.KindJQuery('input'), id: 'productname', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}},
                  {kind: OB.COMP.ClearButton }
                ]},
                {kind: B.KindJQuery('div'), content: [
                  {kind: OB.COMP.ListView('select'), id: 'productcategory', attr: {
                    collection: this.categories,
                    renderHeader: function (model) {
                      return (
                        {kind: B.KindJQuery('option'), attr: {value: ''}, content: [
                           OB.I18N.getLabel('OBPOS_SearchAllCategories')
                        ]}
                      );
                    },
                    renderLine: function (model) {
                      return (
                        {kind: B.KindJQuery('option'), attr: {value: model.get('id')}, content: [
                            model.get('_identifier')
                        ]}
                      );
                    }
                  }}
                ]}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'}, content: [
                {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-gray', 'style': 'float:right;'}, content: [
                  {kind: B.KindJQuery('i'), attr: {'class': 'icon-search btn-icon-left'}}, OB.I18N.getLabel('OBPOS_SearchButtonSearch')
                ], init: function () {
                  var that = me;
                  this.$el.click(function (e) {
                    var criteria = {};

                    function successCallbackPrices(dataPrices, dataProducts) {
                      if(dataPrices){
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
                      that.products.reset(dataProducts.models);
                    }
                    
                    function errorCallback(tx, error) {
                      OB.UTIL.showError("OBDAL error: " + error );
                    }

                    function successCallbackProducts(dataProducts) {
                      if(dataProducts && dataProducts.length > 0){
                        criteria = {'priceListVersion' : OB.POS.modelterminal.get('pricelistversion').id};
                        OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
                      }else{
                        OB.UTIL.showWarning("No products found");
                        that.products.reset();
                      }
                    }
                    
                    e.preventDefault();
                    if (that.productname.val() && that.productname.val() !== '') {
                      criteria._identifier  = {
                        operator: OB.Dal.CONTAINS,
                        value: that.productname.val()
                      };
                    }
                    if (that.productcategory.val() && that.productcategory.val() !== '') {
                      criteria.productCategory  = that.productcategory.val();
                    }
                    OB.Dal.find(OB.Model.Product, criteria , successCallbackProducts, errorCallback);
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
                    return (
                      {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                        OB.I18N.getLabel('OBPOS_SearchNoResults')
                      ]}
                    );
                  },
                  renderLine: OB.COMP.RenderProduct
                }}
              ]}
            ]}
          ]}
        ]}
      ]}
    );
    this.$el = this.component.$el;
    this.productname = this.component.context.productname.$el;
    this.productcategory = this.component.context.productcategory.$el;
    this.tableview = this.component.context.tableview;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackCategories(dataCategories, me) {
      if(dataCategories && dataCategories.length > 0){
        me.categories.reset(dataCategories.models);
      }else{
        me.categories.reset();
      }
    }

    OB.Dal.find(OB.Model.ProductCategory, null , successCallbackCategories, errorCallback, this);
    }
  });
}());