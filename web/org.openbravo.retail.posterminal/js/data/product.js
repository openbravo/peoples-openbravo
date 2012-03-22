

define(['utilities', 'datasource'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  
  OB.DATA.Base = {
    init: function (context, id, defaultid) {
      this.context = context;
      context.set(id || defaultid, this);      
    },
    inithandler: function (init) { 
       if (init) {
         init.call(this);
       }
       // load datasource
       this.ds.load(this.loadparams);
    },
    attr: function (attr, value) {
    },
    append: function (child) {
    }   
  };
  
  OB.DATA.Product = function (context, id) {
    this.init(context, id, 'DataProduct');
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
      'select p as product, pp as price, img.bindaryData as img ' +
      'from PricingProductPrice as pp inner join pp.product p left outer join p.image img ' + 
      'where p.$readableCriteria and p.obposCatalog = true and pp.priceListVersion.id = :priceListVersion ' + 
      'order by p.obposLine, p.name'));
    this.loadparams = {'priceListVersion': this.context.get('modelterminal').get('pricelistversion').id }; 
  };
  _.extend(OB.DATA.Product.prototype, OB.DATA.Base);
  
  OB.DATA.Category = function (context, id) {
    this.init(context, id, 'DataCategory');
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
        'select c as category, img.bindaryData as img ' + 
        'from ProductCategory as c left outer join c.obposImage img ' +
        'where c.$readableCriteria and c.oBPOSIsCatalog = true ' +
        'order by c.oBPOSPOSLine, c.name'));
    this.loadparams = {};     
  };
  _.extend(OB.DATA.Category.prototype, OB.DATA.Base);
 
    
});