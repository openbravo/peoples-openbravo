/*global B , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchProduct = Backbone.View.extend({
	  initialize: function(){
	  var me = this;

    //this._id = 'SearchProducts';

    this.receipt = this.options.modelorder;

    this.categories = new OB.MODEL.Collection(this.options.DataCategory);
    this.products = new OB.MODEL.Collection(this.options.DataProductPrice);

    this.products.on('click', function (model) {
      this.receipt.addProduct(model);
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
                        {kind: B.KindJQuery('option'), attr: {value: model.get('category').id}, content: [
                            model.get('category')._identifier
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
                  this.$el.click(function (e) {
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
    this.categories.exec({});
    }
  });
}());