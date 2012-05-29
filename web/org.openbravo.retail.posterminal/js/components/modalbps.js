/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/searchbps'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ModalBPs = OB.COMP.Modal.extend({

    id: 'modalcustomer',
    header: OB.I18N.getLabel('OBPOS_LblAssignCustomer'),
    getContentView: function () {
      return (
        {kind: OB.COMP.SearchBP, attr: {
          renderLine: OB.COMP.SelectButton.extend({
            render: function() {
              this.$el.append(B(
                {kind: B.KindJQuery('div'), content: [                                                                                   
                  {kind: B.KindJQuery('div'), content: [ 
                    this.model.get('BusinessPartner')._identifier
                  ]},                                                                                                                                                                     
                  {kind: B.KindJQuery('div'), attr:{'style': 'color: #888888'}, content: [ 
                    this.model.get('BusinessPartnerLocation')._identifier
                  ]},                                                                                                                                                                     
                  {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                ]}
              ).$el);
              return this;
            }
          })               
        }}   
      );
    }    
  });  
  
});  