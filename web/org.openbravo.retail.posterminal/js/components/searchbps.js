define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchBP = function (context, id) {
    var me = this;
    
    context.set(id || 'SearchBPs', this);

    this.receipt = context.get('modelorder');
    
    this.bpname = OB.UTIL.EL(
      {tag: 'input', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}}           
    );
    
    this.bps = new OB.MODEL.Collection(context.get('DataBPs'));    
    this.bpsview = new OB.COMP.CollectionView({ 
      renderEmpty: function () {
        return function () {
          return OB.UTIL.EL(
            {tag: 'div', attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
              OB.I18N.getLabel('OBPOS_SearchNoResults')
            ]}
          );
        };            
      },      
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [                                                                                   
            {tag: 'div', content: [ 
              model.get('_identifier')
            ]},                                                                                                                                                                     
            {tag: 'div', attr:{'style': 'color: #888888'}, content: [ 
              model.get('description')
            ]},                                                                                                                                                                     
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                    
      }      
    });
    this.bpsview.setModel(this.bps);  
    this.bps.on('click', function (model) {
      this.receipt.setBP(model);
    }, this);
    
    this.receipt.on('clear', function() {
      this.bps.reset();                   
    }, this);    
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: white; height: 300px; color: black; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'class': 'row-fluid'}, content: [
//            {tag: 'div', attr: {'class': 'span2'}, content: [   
//            ]},                                  
//            {tag: 'div', attr: {'class': 'span10', 'style': 'height: 500px; overflow: auto;'}, content: [    
            {tag: 'div', attr: {'class': 'span12', 'style': 'overflow: auto;'}, content: [    
              {tag: 'div', attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
                {tag: 'div', attr: {'class': 'span9'}, content: [    
                  {tag: 'div', attr: {'style': 'padding: 10px'},  content: [    
                    {tag: 'div', content: [    
                      this.bpname 
                    ]}                  
                  ]}                   
                ]},                                                               
                {tag: 'div', attr: {'class': 'span3'}, content: [ 
                  {tag: 'div', attr: {'style': 'padding: 10px'}, content: [    
                    {tag: 'button', attr: {'style': 'width: 100%'}, content: [
                      {tag:'i', attr: {'class': 'icon-search'}}, OB.I18N.getLabel('OBPOS_SearchButtonSearch')
                    ], init: function () {
                      this.click(function () {
                        var filter = {};
                        if (me.bpname.val() && me.bpname.val() !== '') {
                          filter._identifier = '%i' + OB.UTIL.escapeRegExp(me.bpname.val());
                        }
                        // this.products.exec({ product: { 'productCategory': this.categories.at(selected).get('category').id } });
                        me.bps.exec(filter);                        
                      });
  
                    }}                                                                   
                  ]}                                                                   
                ]}                    
              ]},
              
              {tag: 'div', attr: {'class': 'row-fluid'}, content: [
                {tag: 'div', attr: {'class': 'span12'}, content: [    
                  {tag: 'div', content: [ 
                    this.bpsview.div
                  ]}                   
                ]}                   
              ]}                                                             
            ]}                                                                   
          ]}                      
        ]}        
      ]}
    );
  };
  
}); 