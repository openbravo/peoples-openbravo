/*global define, Backbone */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Generic Button
  OB.COMP.Button = Backbone.View.extend({
    tagName: 'a',
    className: 'btnlink',
    attributes: {'href': '#'},
    initialize: function () {
      this.$el.append(this.$icon);
      this.$el.append(this.$label);
      
    },
    $icon: [],
    $label: [],
    events: {
      'click': 'clickEvent', // attach the click event as part of the element
    },
    clickEvent: function (e) {
      e.preventDefault();
    }       
  });
  
  OB.COMP.ButtonNew = OB.COMP.Button.extend({
    $icon: $('<i class=\"icon-asterisk icon-white\"></i>'),
    $label: $('<span>' + OB.I18N.getLabel('OBPOS_LblNew') + '</span>'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modelorderlist.addNewOrder()
    }       
  });  

});    