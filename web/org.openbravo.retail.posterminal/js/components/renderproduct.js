/*global define, $ */

define(['builder', 'utilities', 'utilitiesui', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.RenderProduct =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [ 
            {kind: OB.UTIL.Thumbnail, attr: {img: this.model.get('img')}}
          ]},                                                                                      
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 60%;'}, content: [ 
            this.model.get('product')._identifier
          ]},                                                                                      
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
            {kind: B.KindJQuery('strong'), content: [ 
              OB.I18N.formatCurrency(this.model.get('price').listPrice)                                                                                                                                         
            ]}                                                                                                                                                                                                                                 
          ]},                                                                                      
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
        ]}
      ).$el);
      return this;
    }
  });
});  
  