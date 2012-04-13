/*global define */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/productprice', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListCategories = function (context, id) {   
    
    this.id = 'ListCategories';
    
    this.receipt = context.get('modelorder');
        
    this.categories = new OB.MODEL.Collection(context.get('DataCategory'));
    this.categoriesview = new B(
      {kind: OB.COMP.TableView, attr: {
        style: 'list',  
        collection: this.categories,
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
    ); 
        
    this.receipt.on('clear', function () {
      if (this.categories.length > 0){
        this.categories.at(0).trigger('selected', this.categories.at(0));
      }
    }, this);  
     
    this.$ = OB.UTIL.EL(
                                                        
      {tag: 'div', content: [ 
        {tag: 'div', attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
          {tag: 'h3', content: [
            'Categories'
          ]}
        ]},
        this.categoriesview.$
      ]}         
    );

    // Exec
    this.categories.exec();
  };
});