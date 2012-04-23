/*global define */


define(['builder', 'i18n',
        'data/datamaster', 'data/dataorder',
        'model/terminal', 'model/order', 'model/productprice',
        'components/hwmanager', 
        'components/searchproducts', 'components/searchbps', 'components/listreceipts', 'components/scan', 'components/editline', 'components/order', 
        'components/total', 'components/businesspartner', 'components/listreceiptscounter', 'components/payment', 'components/keyboard',
        'components/listcategories', 'components/listproducts'
        ], function (B) {
  

  return function () {
    return ( 

      {kind: B.KindJQuery('section'), content: [
        {kind: OB.DATA.Container, content: [
          {kind: OB.DATA.BPs},
          {kind: OB.DATA.Product},
          {kind: OB.DATA.ProductPrice},
          {kind: OB.DATA.Category},      
          {kind: OB.DATA.Order}
        ]},
        
        {kind: OB.MODEL.Order},
        {kind: OB.MODEL.OrderList}, 
        
        {kind: OB.COMP.HWManager, attr: { 'templateline': 'res/printline.xml', 'templatereceipt': 'res/printreceipt.xml'}},     
        
        {kind: B.KindJQuery('div'), attr: {'id': 'modalcustomer', 'class': 'modal hide fade', 'style': 'display: none;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [ 
              {kind: B.KindHTML('<span>&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), content: [OB.I18N.getLabel('OBPOS_LblAssignCustomer')]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-body'}, content: [
            {kind: OB.COMP.SearchBP } 
          ]}      
        ], init: function () {
          
          this.context.SearchBPs.bps.on('click', function (model, index) {
            this.$.modal('hide');
          }, this);
        }},
        {kind: B.KindJQuery('div'), attr: {'id': 'modalreceipts', 'class': 'modal hide fade', 'style': 'display: none;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [ 
              {kind: B.KindHTML('<span>&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), content: [OB.I18N.getLabel('OBPOS_LblAssignReceipt')]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-body'}, content: [
            {kind: OB.COMP.ListReceipts } 
          ]}      
        ], init: function () {
          var context = this.context;
            this.$.on('show', function () {
              context.modelorderlist.saveCurrent();
            });  
            this.context.ListReceipts.receiptlist.on('click', function (model, index) {
              this.$.modal('hide');
            }, this);            
        }},        

        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [

            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-asterisk  icon-white'}}, OB.I18N.getLabel('OBPOS_LblNew')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                  me.context.modelorderlist.createNew();
                });
            }},
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-trash  icon-white'}}, OB.I18N.getLabel('OBPOS_LblDelete')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                  if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
                    me.context.modelorderlist.deleteCurrent();
                  }
                });
            }},            
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-print  icon-white'}}, OB.I18N.getLabel('OBPOS_LblPrint')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                  me.context.modelorder.trigger('print');
                });  
            }},                                                                                         
                            
            {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos'}, content: [     
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'paylink', 'class': 'btnlink btnlink-gray', 'data-toggle': 'tab', 'href': '#payment'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width:100px;'}, content: [
                    {kind: B.KindJQuery('span'), attr: {'style': 'font-weight: bold'}, content: [
                      {kind: OB.COMP.Total}
                    ]},                    
                    {kind: B.KindJQuery('span'), content: [
                      OB.I18N.getLabel('OBPOS_LblPay')
                    ]}
                  ]}
                ], init: function () {
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.keyboard.show();
                  });
                }}        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'cataloglink', 'class': 'btnlink btnlink-gray', 'data-toggle': 'tab', 'href': '#catalog'}, content: [
                  OB.I18N.getLabel('OBPOS_LblBrowse')
                ], init: function () { 
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.keyboard.hide();
                  });                            
                }} 
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'searchlink', 'class': 'btnlink btnlink-gray', 'data-toggle': 'tab', 'href': '#search'}, content: [
                  OB.I18N.getLabel('OBPOS_LblSearch')
                ], init: function () {  
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.keyboard.hide();
                  });
                }}        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'scanlink', 'class': 'btnlink btnlink-gray', 'data-toggle': 'tab', 'href': '#scan'}, content: [
                  OB.I18N.getLabel('OBPOS_LblScan')
                ], init: function () {
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.keyboard.show();
                  });
                  this.context.modelorder.on('clear scan', function() {
                    this.$.tab('show');                         
                  }, this);   
                  this.context.SearchBPs.bps.on('click', function (model, index) {
                    this.$.tab('show');
                  }, this);                  
                }}        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'editionlink', 'class': 'btnlink btnlink-gray', 'data-toggle': 'tab', 'href': '#edition', 'style': 'text-shadow:none;'}, content: [
                  OB.I18N.getLabel('OBPOS_LblEdit')
                ], init: function () {
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.keyboard.show();
                  });
                  
                  this.context.modelorder.get('lines').on('click', function () {
                    this.$.tab('show');
                  }, this);                        
                }}
              ]}            
            ]}                                                                                  
          ]}
        ]},

        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                                                                                                                                  
          {kind: B.KindJQuery('div'), attr: {'class': 'span5'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [                                                                           
              {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                             
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [   
                        {kind: B.KindJQuery('a'), attr: {'class': 'btnlink btnlink-small btnlink-gray', 'href': '#modalcustomer', 'data-toggle': 'modal'}, content: [                                                                                                                                
                          {kind: OB.COMP.BusinessPartner}
                        ]},
                        {kind: B.KindJQuery('div'), attr: {'style': 'float:right'}, content: [                                                                                                                                
                          {kind: B.KindJQuery('a'), attr: {'class': 'btnlink btnlink-small btnlink-gray', 'href': '#modalreceipts', 'data-toggle': 'modal'}, content: [                                                                                                                                
                            {kind: OB.COMP.ReceiptsCounter}
                          ]}
                        ]},                        
                        {kind: B.KindJQuery('div'), attr: {'style': 'clear:both;'}} 
                    ]}
                  ]}                                                              
                ]},
                {kind: OB.COMP.OrderView}                    
              ]}                                                              
            ]}  
          ]},          
          
          {kind: B.KindJQuery('div'), attr: {'class': 'span7'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'tab-content'}, content: [
              {kind: B.KindJQuery('div'), attr: {'id': 'scan', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.Scan }                                                                      
              ]}, 
              {kind: B.KindJQuery('div'), attr: {'id': 'catalog', 'class': 'tab-pane'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
                    {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [                                                                                      
                      {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [                                                                        
                        {kind: OB.COMP.ListProducts }  // Must be defined after ListCategories...
                      ]}        
                    ]}        
                  ]},                                                                                    
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
                    {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [                                                                                      
                      {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [                                                                             
                        {kind: OB.COMP.ListCategories }  
                      ]}        
                    ]}        
                  ]}
                ]}                                                                   
              ], init: function () {
                this.context.ListCategories.categories.on('selected', function (category) {
                  this.context.ListProducts.loadCategory(category);
                }, this);                   
              }},  
              {kind: B.KindJQuery('div'), attr: {'id': 'search', 'class': 'tab-pane'}, content: [
                {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [                                                                                      
                  {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [                                                                                                       
                    {kind: OB.COMP.SearchProduct } 
                  ]}        
                ]}                                                                      
              ]},  
              {kind: B.KindJQuery('div'), attr: {'id': 'edition', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.EditLine }                                                                      
              ]},       
              {kind: B.KindJQuery('div'), attr: {'id': 'payment', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.Payment, attr: {'cashcoins': [50, 20, 10, 5, 2, 1, 0.50, 0.20, 0.10, 0.05, 0.01] }}                                                                      
              ]}             
            ]},
            {kind: OB.COMP.Keyboard }
          ]}        
        ]}

        
      ], init: function () {
        OB.POS.modelterminal.on('domready', function () {
          this.context.modelorderlist.createNew();
        }, this);
      }}
      
    );           
  };
});