

define(['utilities'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
  OB.MODEL.Stack = Backbone.Model.extend({
    initialize : function () {
      this.set('selected', -1);
    }
  });
  
});