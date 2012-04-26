/*global define */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'model/order', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListCategories = function (context) {   
    
    this._id = 'ListCategories';
    
    this.receipt = context.modelorder;        
    this.categories = new OB.MODEL.Collection(context.DataCategory);
        
    this.receipt.on('clear', function () {
      if (this.categories.length > 0){
        this.categories.at(0).trigger('selected', this.categories.at(0));
      }
    }, this);  
     
    this.component = B(
      {kind: B.KindJQuery('div'), content: [ 
        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('h3'), content: [
            OB.I18N.getLabel('OBPOS_LblCategories')
          ]}
        ]},
        {kind: OB.COMP.TableView, attr: {
          style: 'list',  
          collection: this.categories,
          renderEmpty: function () {
            return B(
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                OB.I18N.getLabel('OBPOS_SearchNoResults')
              ]}
            );            
          },                
          renderLine: function (model) {
            return B(
              {kind: B.KindJQuery('a'), attr: {'href': '#', 'class': 'btnselect'}, content: [
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
    );
    this.$ = this.component.$;

    // Exec
    this.categories.exec();
  };
});