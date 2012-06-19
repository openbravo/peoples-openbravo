/*global window, define, $, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/searchproducts'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ButtonTabSearch = OB.COMP.ButtonTab.extend({
    tabpanel: '#search',
    label: OB.I18N.getLabel('OBPOS_LblSearch'),
    shownEvent: function (e) {
      this.options.keyboard.hide();
    }
  });
  OB.COMP.TabSearch = Backbone.View.extend({
    tagName: 'div',
    attributes: {'id': 'search', 'class': 'tab-pane'},
    initialize: function () {
      var $child = $('<div/>');
      $child.css({'overflow': "auto", 'height': '500px', 'margin': '5px'});
      var $subChild = $('<div/>');
      $subChild.css({'background-color': "#ffffff", 'color': 'black', 'padding': '5px'});
      var searchProd = new OB.COMP.SearchProduct(this.options);
      $subChild.append(searchProd.$el);
      $child.append($subChild);
      this.$el.append($child);
    }
  });

});