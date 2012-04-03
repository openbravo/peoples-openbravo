

define(['utilities', 'i18n', 'model/order', 'model/productprice', 'model/terminal', 'components/table'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListCategories = function (context, id) {   
    
    context.set(id || 'ListCategories', this);
    
    this.receipt = context.get('modelorder');
        
    this.categories = new OB.MODEL.Collection(context.get('DataCategory'));
    this.categoriesview = new OB.COMP.CollectionView({
      style: 'list',
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'a', attr: {'href': '#', 'class': 'btnselect'}, content: [
            {tag: 'div', attr: {style: 'float: left; width: 20%'}, content: [ 
              OB.UTIL.getThumbnail(model.get('img'))                                                       
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 80%;'}, content: [ 
              model.get('category')._identifier                                                                                                                                               
            ]},                                                                                      
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );                  
      }      
    });
    this.categoriesview.setModel(this.categories);    
        
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
        this.categoriesview.div
      ]}         
    );

    // Exec
    this.categories.exec();
  };
  
  OB.COMP.ListCategories.prototype.attr = function (attr, value) {
  };
  OB.COMP.ListCategories.prototype.append = function append(child) {
  };   
});