/*global define, $ */

define(['builder', 'i18n',
        'components/commonbuttons', 'components/hwmanager', 
        'model/daycash',
        'windows/closebuttons',
        'windows/closeinfo',
        'windows/closekeyboard',
        'windows/tabpendingreceipts', 'windows/tabcountcash'
        ], function (B) {  
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTest = OB.COMP.ToolbarButton.extend({
    icon: 'icon-leaf icon-white',
    label: ' **Leaf**',
    clickEvent: function (e) {
      alert('pressed');
    }       
  });    
  
  
  OB.COMP.CloseCash = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [
    
          {kind: OB.MODEL.DayCash},
          
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [ 
              
              {kind: OB.COMP.ButtonPrev},
              
              {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos'}, content: [     
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonTabPendingReceipts}
                ]},
                {kind: B.KindJQuery('li'), content: [                                                  
                  {kind: OB.COMP.ButtonTabCountCash} 
                ]}   
              ]},         
              {kind: OB.COMP.CloseCashSteps},             
              {kind: OB.COMP.ButtonNext}
            ]}
          ]},
  
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [                                                                                                                                   
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'tab-content'}, content: [
                {kind: OB.COMP.TabPendingReceipts},
                {kind: OB.COMP.TabCountCash}
              ]}
            ]},          
  
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: OB.COMP.CloseInfo }
              ]},
              {kind: OB.COMP.CloseKeyboard }
            ]}        
          ]}
          
        ], init: function () {
          this.context.on('domready', function () {
             $('a[href="#pendingreceipts"]').tab('show');
          }, this);
        }}
      );           
    }   
  });
  
  return OB.COMP.CloseCash;
});