/*global define,Backbone,localStorage */

define(['utilities', 'utilitiesui', 'arithmetic', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
  // Sales.OrderLine Model
  OB.MODEL.DayCash = Backbone.Model.extend({
    _id: 'modeldaycash',
    defaults : {
      step: 0
    },
    nextStep: function(){
    	
    }
    
  }); 

});    