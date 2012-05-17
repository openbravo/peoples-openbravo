/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n', 'components/commonbuttons'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonPrev = OB.COMP.Button.extend({
    icon: 'icon-chevron-left icon-white',
    label: OB.I18N.getLabel('OBPOS_LblPrevStep'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modeldaycash.prevStep();
    }       
  });    

  OB.COMP.ButtonNext = OB.COMP.Button.extend({
    iconright: 'icon-chevron-right icon-white',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modeldaycash.nextStep();
    }       
  });  
  
  OB.COMP.CloseCashSteps = Backbone.View.extend({
    tagName: 'div',
    className: 'btnlink',
    attributes: {'style': 'width: 300px; text-align:center;'},
    initialize: function () {

      this.component = B(
        {kind: B.KindJQuery('div'), content: [
          'Supper trooper'      
        ]}
      );   
      this.$el.append(this.component.$el);
    }    
  });   


});    