/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTabPendingReceipts = OB.COMP.ButtonTab.extend({
    tabpanel: '#pendingreceipts',
    label: OB.I18N.getLabel('OBPOS_LblPendingReceipts')   
  });    

  OB.COMP.TabPendingReceipts = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'pendingreceipts', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [                                                                           
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                             
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [   
  
                     'Pending Receipts'                                                                                                        
                                                                                                                             
                  ]}
                ]}                                                              
              ]},
              {kind: B.KindJQuery('div')}                    
            ]}                                                              
          ]} 
        ]} 
      );
    }   
  }); 
  
});  