/*global define, $ */

define(['builder', 'i18n',
        'data/datamaster', 'data/dataordersave', 'data/dataordertaxes', 'data/dataorderdiscount',
        'model/terminal', 'model/order',
        'components/commonbuttons', 'components/hwmanager', 
        'windows/posbuttons',
        'components/modalreceipts', 'components/modalbps',
        'components/tabscan', 'components/tabbrowse', 'components/tabsearch', 'components/tabeditline', 'components/tabpayment',
        'components/order', 'components/orderdetails', 'components/businesspartner', 'components/listreceiptscounter', 'components/keyboard'
        ], function (B) {  
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.PointOfSale = OB.COMP.CustomView.extend({
    createView: function () {

      return (
        {kind: B.KindJQuery('section'), content: [
          
          {kind: OB.MODEL.Order},
          {kind: OB.MODEL.OrderList}, 
          
          {kind: OB.DATA.Container, content: [
            {kind: OB.DATA.BPs},
            {kind: OB.DATA.ProductPrice},
            {kind: OB.DATA.Category},      
            {kind: OB.DATA.TaxRate},               
            {kind: OB.COMP.HWManager, attr: { 'templateline': 'res/printline.xml', 'templatereceipt': 'res/printreceipt.xml'}}
          ]},    
                 
          {kind: OB.DATA.OrderDiscount},
          {kind: OB.DATA.OrderTaxes},
          {kind: OB.DATA.OrderSave},
          
          {kind: OB.COMP.ModalBPs},
          {kind: OB.COMP.ModalReceipts},      
  
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                              
              {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos'}, content: [     
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonNew}                                                        
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonDelete}                                                        
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonPrint}                                                        
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.MenuButton.extend({icon: 'icon-th-large icon-white', label: OB.I18N.getLabel('OBPOS_LblMenu')}), content: [
                    {kind: OB.COMP.MenuItem.extend({href:'../..', label:OB.I18N.getLabel('OBPOS_LblOpenbravoWorkspace')})}, 
                    {kind: OB.COMP.MenuItem.extend({href: OB.POS.hrefWindow('org.openbravo.retail.posterminal/js/windows/closecash'), label: OB.I18N.getLabel('OBPOS_LblCloseCash')})}
                  ]}                                                        
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonTabPayment}                                                        
                ]},
                {kind: B.KindJQuery('li'), content: [                                                  
                  {kind: OB.COMP.ButtonTabBrowse}                              
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonTabSearch}
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonTabScan}      
                ]},
                {kind: B.KindJQuery('li'), content: [
                  {kind: OB.COMP.ButtonTabEditLine }    
                ]}            
              ]}                                                                                  
            ]}
          ]},
  
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                                                                                                                                    
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                                                                            
              {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [ 
                {kind: B.KindJQuery('div'), attr: {'style': 'position: relative;background-color: #ffffff; color: black;'}, content: [                                                                                                                                       
                  {kind: OB.COMP.ReceiptsCounter},                       
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px;'}, content: [  
                    {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                             
                        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [                                                                                                                                              
                            {kind: OB.COMP.OrderDetails},    
                            {kind: OB.COMP.BusinessPartner},        
                            {kind: B.KindJQuery('div'), attr: {'style': 'clear:both;'}} 
                        ]}
                      ]}                                                              
                    ]},
                    {kind: OB.COMP.OrderView, attr: {
                      renderLine: function (model) {
                        return (
                          {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [
                            {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%'}, content: [ 
                              model.get('product').get('product')._identifier                                                                
                            ]},                                                                                      
                            {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                              model.printQty()                                                                                                                                                          
                            ]},                                                                                      
                            {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                              model.printPrice()                                                             
                            ]},                                                                                      
                            {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                              model.printNet()
                            ]},
                            {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                          ]}
                        );         
                      }                 
                    }}                    
                  ]}                                                              
                ]}                                                              
              ]}  
            ]},          
  
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'tab-content'}, content: [
                {kind: OB.COMP.TabScan },               
                {kind: OB.COMP.TabBrowse },                
                {kind: OB.COMP.TabSearch },  
                {kind: OB.COMP.TabEditLine },     
                {kind: OB.COMP.TabPayment }                
              ]},
              {kind: OB.COMP.KeyboardOrder}
            ]}        
          ]}
          
        ], init: function () {
          this.context.on('domready', function () {
            this.context.modelorderlist.addNewOrder();
          }, this);
        }}
      );           
    }   
  });
  
  return OB.COMP.PointOfSale;
});