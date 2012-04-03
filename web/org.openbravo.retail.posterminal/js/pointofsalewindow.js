


define(['builder', 'i18n',
        'data/product',
        'model/terminal', 'model/order', 'model/stack', 'model/productprice',
        'components/hwmanager', 
        'components/searchproducts', 'components/searchbps', 'components/listreceipts', 'components/scan', 'components/editline', 'components/order', 
        'components/total', 'components/payment', 'components/keyboard',
        'components/listcategories', 'components/listproducts'
        ], function (B) {
  

  return function () {
    return ( 
        
      {kind: B.KindJQuery('section'), content: [
        {kind: OB.DATA.BPs},
        {kind: OB.DATA.Product},
        {kind: OB.DATA.ProductPrice},
        {kind: OB.DATA.Category},      
        
        {kind: OB.MODEL.OrderList, id: 'modelorderlist', content: [                                                                  
          {kind: OB.MODEL.Order, id: 'modelorder'}
        ]}, 
        // {kind: OB.MODEL.Order, id: 'modelorder'},
        {kind: OB.MODEL.StackOrder, id: 'stackorder'},        
        
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
          
          this.context.get('SearchBPs').bps.on('click', function (model, index) {
            this.$.modal('hide');
          }, this);
        }},
        
        {kind: B.KindJQuery('div'), attr: {'id': 'modalreceipts', 'class': 'modal hide fade', 'style': 'display: none;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [ 
              {kind: B.KindHTML('<span>&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), content: [OB.I18N.getLabel('OBPOS_LblReceipts')]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-body'}, content: [
            {kind: OB.COMP.ListReceipts } 
          ]}      
        ], init: function () {
            var context = this.context;
            this.$.on('show', function () {
              context.get('modelorderlist').saveCurrent();
            });                 
        }},        

        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                                                                           

            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-asterisk  icon-white'}}, OB.I18N.getLabel('OBPOS_LblNew')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                  // me.context.get('modelorder').clear();
                  me.context.get('modelorderlist').createNew();
                });
            }},
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-trash  icon-white'}}, OB.I18N.getLabel('OBPOS_LblDelete')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                });
            }},            
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#'}, content: [
              {kind: B.KindJQuery('i'), attr: {'class': 'icon-print  icon-white'}}, OB.I18N.getLabel('OBPOS_LblPrint')
            ], init: function () {
                var me = this;
                me.$.click(function (e) {
                  e.preventDefault();
                  me.context.get('modelorder').trigger('print');
                });  
            }},                                                                           
                
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#modalcustomer', 'data-toggle': 'modal'}, content: [
              '** Assign customer **'
            ]},
            {kind: B.KindJQuery('a'), attr: {'class': 'btnlink', 'href': '#modalreceipts', 'data-toggle': 'modal'}, content: [
              '** Receipts **'
            ]},                  
                            
            
            {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled nav-pos'}, content: [     
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'paylink', 'class': 'btnlink btnlink-nav', 'data-toggle': 'tab', 'href': '#payment'}, content: [
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
                    context.get('keyboard').show();
                  });
                }},        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'cataloglink', 'class': 'btnlink btnlink-nav', 'data-toggle': 'tab', 'href': '#catalog'}, content: [
                  OB.I18N.getLabel('OBPOS_LblBrowse')
                ], init: function () {  
                  var context = this.context;
                  this.$.on('shown', function () {
                    context.get('keyboard').hide();
                  });                            
                }},        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'searchlink', 'class': 'btnlink btnlink-nav', 'data-toggle': 'tab', 'href': '#search'}, content: [
                  OB.I18N.getLabel('OBPOS_LblSearch')
                ], init: function () {  
                  var context = this.context;                      
                  this.$.on('shown', function () {
                    context.get('keyboard').hide();
                  });
                }},        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'scanlink', 'class': 'btnlink btnlink-nav', 'data-toggle': 'tab', 'href': '#scan'}, content: [
                  OB.I18N.getLabel('OBPOS_LblScan')
                ], init: function () { 
                  var context = this.context;                      
                  this.$.on('shown', function () {
                    context.get('keyboard').show();
                  });
                  context.get('modelorder').on('clear', function() {
                    this.$.tab('show');                         
                  }, this);   
                  context.get('stackorder').on('scan', function () {
                    this.$.tab('show');
                  }, this);
                  context.get('SearchBPs').bps.on('click', function (model, index) {
                    this.$.tab('show');
                  }, this);                  
                  
                  
                }},        
              ]},
              {kind: B.KindJQuery('li'), content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'editionlink', 'class': 'btnlink btnlink-nav', 'data-toggle': 'tab', 'href': '#edition', 'style': 'text-shadow:none;'}, content: [
                  OB.I18N.getLabel('OBPOS_LblEdit')
                ], init: function () {   
                  var context = this.context;                      
                  this.$.on('shown', function () {
                    context.get('keyboard').show();
                  });
                  
                  context.get('stackorder').on('click', function () {
                    this.$.tab('show');
                  }, this);                        
                }},        
              ]}            
            ]}                                                                              

            
            
          ]}
        ]},

        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span5'}, content: [
            {kind: OB.COMP.OrderView}  
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'span7'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'tab-content'}, content: [
              {kind: B.KindJQuery('div'), attr: {'id': 'scan', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.Scan }                                                                      
              ]}, 
              {kind: B.KindJQuery('div'), attr: {'id': 'catalog', 'class': 'tab-pane'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
                    {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
                      {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px'}, content: [                                                                           
                        {kind: OB.COMP.ListCategories }  
                      ]}        
                    ]}        
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
                    {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; margin: 5px; padding: 5px'}, content: [                                                          
                      {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px'}, content: [                                                                           
                        {kind: OB.COMP.ListProducts }  // Must be defined after ListCategories...
                      ]}        
                    ]}        
                  ]}              
                ]}                                                                   
              ]},  
              {kind: B.KindJQuery('div'), attr: {'id': 'search', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.SearchProduct }                                                                      
              ]},  
              {kind: B.KindJQuery('div'), attr: {'id': 'edition', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.EditLine }                                                                      
              ]},       
              {kind: B.KindJQuery('div'), attr: {'id': 'payment', 'class': 'tab-pane'}, content: [
                {kind: OB.COMP.Payment }                                                                      
              ]}             
            ]},
            {kind: OB.COMP.Keyboard }
          ]}        
        ]}

        
      ], init: function () {
        this.context.on('ready', function () {
          // this.context.get('modelorder').clear();
          this.context.get('modelorderlist').createNew();
        }, this);
      }}
      
    );           
  };
});