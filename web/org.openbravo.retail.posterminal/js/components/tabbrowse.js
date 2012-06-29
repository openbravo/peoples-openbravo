/*global window, B, Backbone , $ */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabBrowse = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#catalog',
    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
    shownEvent: function(e) {
      this.options.keyboard.hide();
    }
  });

  OB.COMP.BrowseCategories = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'overflow:auto; margin: 5px;'
    },
    initialize: function() {
      var $child = $('<div/>');
      $child.css({
        'background-color': '#ffffff',
        'color': 'black',
        'padding': '5px'
      });
      this.listCategories = new OB.COMP.ListCategories(this.options);
      $child.append(this.listCategories.$el);
      this.$el.append($child);
    }
  });

  OB.COMP.BrowseProducts = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'overflow:auto; height: 500px; margin: 5px;'
    },
    initialize: function() {
      var $child = $('<div/>');
      $child.css({
        'background-color': '#ffffff',
        'color': 'black',
        'padding': '5px'
      });
      this.listProducts = new OB.COMP.ListProducts(this.options);
      $child.append(this.listProducts.$el);
      this.$el.append($child);
    }
  });

  OB.COMP.TabBrowse = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'id': 'catalog',
      'class': 'tab-pane'
    },
    initialize: function() {
      var $child = $('<div/>');
      $child.addClass('row-fluid');

      var $subChild = $('<div/>');
      $subChild.addClass('span6');
      var browseProd = new OB.COMP.BrowseProducts(this.options);
      $subChild.append(browseProd.$el);

      var $subChild2 = $('<div/>');
      $subChild2.addClass('span6');
      var browseCateg = new OB.COMP.BrowseCategories(this.options);
      $subChild2.append(browseCateg.$el);

      $child.append($subChild);
      $child.append($subChild2);

      this.$el.append($child);

      browseCateg.listCategories.categories.on('selected', function(category) {
        browseProd.listProducts.loadCategory(category);
      }, this);
    }
  });
}());