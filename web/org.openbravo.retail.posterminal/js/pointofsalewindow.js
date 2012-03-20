


define(['builder',
        'model/terminal', 'model/order', 'model/stack',
        'components/hwmanager', 
        'components/catalog', 'components/scan', 'components/editline', 'components/order', 'components/payment', 'components/keyboard'
        ], function (B) {
  

  return function () {
    return ( 
        
      {kind: B.KindJQuery('section'), content: [
        {kind: OB.MODEL.CategoryCol, id: 'modelcategories'},                                      
        {kind: OB.MODEL.ProductCol, id: 'modelproducts', init: function () {
          this.loadparams = {'priceListVersion': this.context.get('modelterminal').get('pricelistversion').id };
        }},                                      
        {kind: OB.MODEL.Order, id: 'modelorder'},                                      
        {kind: OB.MODEL.StackOrder, id: 'stackorder'},                                      
        {kind: OB.COMP.HWManager, attr: { 'templateline': 'res/printline.xml', 'templatereceipt': 'res/printreceipt.xml'}},           
        
        {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'navbar'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'navbar-inner'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'container'}, content: [
                {kind: B.KindJQuery('ul'), attr: {'class': 'nav'}, content: [
                  {kind: B.KindJQuery('li'), attr: {'class': 'divider-vertical'}},        
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'href': '#', 'style': 'text-shadow:none;'}, content: [
                      {kind: B.KindJQuery('i'), attr: {'class': 'icon-asterisk'}}, ' New'      
                    ], init: function () {
                      var me = this;
                      me.$.click(function (e) {
                        e.preventDefault();
                        me.context.get('modelorder').reset();
                      });
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), attr: {'class': 'divider-vertical'}},        
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'href': '#', 'style': 'text-shadow:none;'}, content: [
                      {kind: B.KindJQuery('i'), attr: {'class': 'icon-print'}}, ' Print'      
                    ], init: function () {
                      var me = this;
                      me.$.click(function (e) {
                        e.preventDefault();
                        me.context.get('modelorder').trigger('print');
                      });                        
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), attr: {'class': 'divider-vertical'}}
                ]},      
                {kind: B.KindJQuery('ul'), attr: {'class': 'nav nav-pills'}, content: [     
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'paylink', 'data-toggle': 'tab', 'href': '#payment', 'style': 'text-shadow:none;'}, content: [
                      'Pay'      
                    ], init: function () {   
                      var context = this.context;
                      this.$.on('shown', function () {
                        context.get('keyboard').show();
                      });
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'cataloglink', 'data-toggle': 'tab', 'href': '#catalog', 'style': 'text-shadow:none;'}, content: [
                      'Browse'      
                    ], init: function () {  
                      var context = this.context;
                      this.$.on('shown', function () {
                        context.get('keyboard').hide();
                      });                                            
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'searchlink', 'data-toggle': 'tab', 'href': '#search', 'style': 'text-shadow:none;'}, content: [
                      'Search'      
                    ], init: function () {  
                      var context = this.context;                      
                      this.$.on('shown', function () {
                        context.get('keyboard').hide();
                      });
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'scanlink', 'data-toggle': 'tab', 'href': '#scan', 'style': 'text-shadow:none;'}, content: [
                      'Scan'      
                    ], init: function () { 
                      var context = this.context;                      
                      this.$.on('shown', function () {
                        context.get('keyboard').show();
                      });
                      context.get('modelorder').on('reset', function() {
                        this.$.tab('show');                         
                      }, this);                        
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'editionlink', 'data-toggle': 'tab', 'href': '#edition', 'style': 'text-shadow:none;'}, content: [
                      'Edit'      
                    ], init: function () {   
                      var context = this.context;                      
                      this.$.on('shown', function () {
                        context.get('keyboard').show();
                      });
                      
                      context.get('stackorder').on('click', function () {
                        this.$.tab('show');
                      }, this);                        
                    }},        
                  ]},
                  {kind: B.KindJQuery('li'), attr: {'class': 'divider-vertical'}}
                ]}      
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
                {kind: OB.COMP.Catalog }                                                                      
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
          this.context.get('modelorder').reset();  
        }, this);
      }}
      
    );           
  };
});