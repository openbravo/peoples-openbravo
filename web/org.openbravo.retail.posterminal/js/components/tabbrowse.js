/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabBrowse = OB.COMP.ButtonTab.extend({
    tabpanel: '#catalog',
    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
    shownEvent: function (e) {
      this.options.keyboard.hide();
    }
  });

  OB.COMP.BrowseCategories = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [
            {kind: OB.COMP.ListCategories}
          ]}
        ]}
      );
    }
  });

  OB.COMP.BrowseProducts = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [
            {kind: OB.COMP.ListProducts}
          ]}
        ]}
      );
    }
  });

  OB.COMP.TabBrowse = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'catalog', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: OB.COMP.BrowseProducts}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: OB.COMP.BrowseCategories}
            ]}
          ]}
        ], init: function () {
          this.context.ListCategories.categories.on('selected', function (category) {
            this.context.ListProducts.loadCategory(category);
          }, this);
        }}
      );
    }
  });
}());