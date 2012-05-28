/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons','components/listcategories', 'components/listproducts'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTabBrowse = OB.COMP.ButtonTab.extend({
    tabpanel: '#catalog',
    label: OB.I18N.getLabel('OBPOS_LblBrowse'),
    shownEvent: function (e) {      
      this.options.keyboard.hide();
    }       
  });
  
  OB.COMP.BrowseCategories = OB.COMP.CustomView.extend({
    createView: function () {
      return ( 
        {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [                                                                                      
          {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [                                                                             
            {kind: OB.COMP.ListCategories, attr:{
              renderLine:function (model) {
                return (
                  {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [ 
                      {kind: OB.UTIL.Thumbnail, attr: {img: model.get('img')}}
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%;'}, content: [ 
                      model.get('category')._identifier                                                                                                                                               
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                  ]}
                );               
              }
            }}
          ]}        
        ]}                      
      );
    }   
  });  
  
  OB.COMP.BrowseProducts = OB.COMP.CustomView.extend({
    createView: function () {
      return (  
        {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [                                                                                      
          {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [                                                                        
            {kind: OB.COMP.ListProducts, attr: {
              renderLine: function (model) {
                return (
                  {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [ 
                      {kind: OB.UTIL.Thumbnail, attr: {img: model.get('img')}}
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 60%;'}, content: [ 
                      model.get('product')._identifier
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [ 
                      {kind: B.KindJQuery('strong'), content: [ 
                        OB.I18N.formatCurrency(model.get('price').listPrice)                                                                                                                                         
                      ]}                                                                                                                                                                                                                                 
                    ]},                                                                                      
                    {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                  ]}
                );                    
              }                          
            }}
          ]}        
        ]}              
      );
    }   
  });   
  
  OB.COMP.TabBrowse = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'catalog', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
              {kind: OB.COMP.BrowseProducts}
            ]},                                                                                    
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
              {kind: OB.COMP.BrowseCategories}                                                               
            ]}
          ]}                                                                   
        ], init: function () {
          this.context.ListCategories.categories.on('selected', function (category) {
            this.context.ListProducts.loadCategory(category);
          }, this);                   
        }}          
      );
    }   
  }); 
});