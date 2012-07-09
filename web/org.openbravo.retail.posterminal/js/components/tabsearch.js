/*global window, B, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};


  OB.UI.ButtonTabSearch = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#search',
    label: OB.I18N.getLabel('OBPOS_LblSearch'),
    shownEvent: function(e) {
      this.options.keyboard.hide();
    }
  });
  OB.UI.TabSearch = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'id': 'search',
      'class': 'tab-pane'
    },
    initialize: function() {
      var $container, $subContainer, searchProd;
      $container = $('<div/>');
      $container.css({
        'overflow': "auto",
        'margin': '5px'
      });
      $subContainer = $('<div/>');
      $subContainer.css({
        'background-color': "#ffffff",
        'color': 'black',
        'padding': '5px'
      });
      searchProd = new OB.COMP.SearchProduct(this.options);
      $subContainer.append(searchProd.$el);
      $container.append($subContainer);
      this.$el.append($container);
    }
  });

}());