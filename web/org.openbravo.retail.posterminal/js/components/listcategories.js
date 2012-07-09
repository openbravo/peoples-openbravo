/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListCategories = Backbone.View.extend({
    optionsid: 'ListCategories',
    tag: 'div',
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'h3',
        content: [
        OB.I18N.getLabel('OBPOS_LblCategories')]
      }]
    }, {
      id: 'tableview',
      view: OB.UI.TableView.extend({
        style: 'list',
        renderEmpty: OB.COMP.RenderEmpty,
        renderLine: OB.COMP.RenderCategory
      })
    }],
    initialize: function () {

      this.options[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      this.receipt = this.options.modelorder;
      this.categories = new OB.Collection.ProductCategoryList();
      this.tableview.registerCollection(this.categories);

      this.receipt.on('clear', function () {
        if (this.categories.length > 0) {
          this.categories.at(0).trigger('selected', this.categories.at(0));
        }
      }, this);

      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackCategories(dataCategories, me) {
        if (dataCategories && dataCategories.length > 0) {
          me.categories.reset(dataCategories.models);
        } else {
          me.categories.reset();
        }
      }

      OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
    }
  });
}());