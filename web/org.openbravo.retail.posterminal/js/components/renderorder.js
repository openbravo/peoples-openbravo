/*global define, $ */

define(['builder', 'utilities', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.RenderOrder =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [                                                                                                                                                                        
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
             OB.I18N.formatHour(this.model.get('orderDate'))
          ]},                                                                                      
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
            this.model.get('documentNo')
          ]}, 
          {kind: B.KindJQuery('div'), attr: {style: 'float: left;width: 50%;'}, content: [ 
            this.model.get('bp').get('_identifier')
          ]}, 
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
            {kind: B.KindJQuery('strong'), content: [ 
               this.model.printGross()                                                                                                                             
            ]}                                                                                                                                                                                                                                 
          ]},              
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
        ]}
      ).$el);
      return this;
    }
  });
});