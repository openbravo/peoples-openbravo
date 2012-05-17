/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTabCountCash= OB.COMP.ButtonTab.extend({
    tabpanel: '#countcash',
    label: OB.I18N.getLabel('OBPOS_LblCountCash')      
  });    

  OB.COMP.TabCountCash = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [          
          {kind: B.KindJQuery('div'), id:'countcash', 'class': 'tab-pane', attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [                                                                           
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                             
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [   
  
                     'Count Cash'                                                                                                        
                                                                                                                             
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