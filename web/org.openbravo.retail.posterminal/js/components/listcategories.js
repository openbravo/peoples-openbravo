/*global B */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListCategories = function (context) {

    this._id = 'ListCategories';

    this.receipt = context.modelorder;
    this.categories = new OB.Collection.ProductCategoryList();

    this.receipt.on('clear', function () {
      if (this.categories.length > 0){
        this.categories.at(0).trigger('selected', this.categories.at(0));
      }
    }, this);

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('h3'), content: [
            OB.I18N.getLabel('OBPOS_LblCategories')
          ]}
        ]},
        {kind: OB.COMP.TableView, id: 'tableview', attr: {
          style: 'list',
          collection: this.categories,
          renderEmpty: function () {
            return (
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                OB.I18N.getLabel('OBPOS_SearchNoResults')
              ]}
            );
          },
          renderLine: OB.COMP.RenderCategory
        }}
      ]}
    );
    this.$el = this.component.$el;
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
  };
}());