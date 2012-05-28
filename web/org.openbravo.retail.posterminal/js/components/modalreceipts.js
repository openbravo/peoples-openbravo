/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/listreceipts'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ModalReceipts = OB.COMP.Modal.extend({

    id: 'modalreceipts',
    header: OB.I18N.getLabel('OBPOS_LblAssignReceipt'),
    getContentView: function () {
      return (
        {kind: OB.COMP.ListReceipts, attr: {
          renderLine: function (model) {
            return (
              {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [                                                                                                                                                                        
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
                   OB.I18N.formatHour(model.get('orderDate'))
                ]},                                                                                      
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 15%;'}, content: [ 
                  model.get('documentNo')
                ]}, 
                {kind: B.KindJQuery('div'), attr: {style: 'float: left;width: 50%;'}, content: [ 
                  model.get('bp').get('_identifier')
                ]}, 
                {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                  {kind: B.KindJQuery('strong'), content: [ 
                     model.printNet()                                                                                                                             
                  ]}                                                                                                                                                                                                                                 
                ]},              
                {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
              ]}
            );
          }
        }}      
      );
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
      this.options.modelorderlist.saveCurrent();
    }      
  });  

  
});  