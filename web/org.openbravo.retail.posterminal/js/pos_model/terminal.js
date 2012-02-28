

OBPOS.Model.Category = Backbone.Model.extend({});

OBPOS.Model.CategoryCol = Backbone.Collection.extend({
  model: OBPOS.Model.Category
});


OBPOS.Model.Product = Backbone.Model.extend({});

OBPOS.Model.ProductCol = Backbone.Collection.extend({
  model: OBPOS.Model.Product
});

OBPOS.Model.Terminal = Backbone.Model.extend({
  
  defaults : {
    terminal: null,
    bplocation: null,
    location: null,
    pricelist: null,
    pricelistversion: null,
    products: new OBPOS.Model.ProductCol(),
    categories: new OBPOS.Model.CategoryCol()
  },
  
  load: function () {
    var me = this;
    var t = OBPOS.getParameterByName("terminal") || "POS-1";

    new OBPOS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal').exec({
      terminal: t
    }, function (data) {
      if (data.exception) {
        window.location = '../org.openbravo.client.application.mobile/login.jsp';
      } else if (data[0]) {
        me.set('terminal', data[0]);
        me.loadBPLocation();
        me.loadLocation();
        me.loadPriceList();
        me.loadPriceListVersion();
      } else {
        alert("Terminal does not exists: " + t);
      }
    });        
  },
  
  loadBPLocation: function () {
    var me = this;
    new OBPOS.Query('from BusinessPartnerLocation where id = (select min(id) from BusinessPartnerLocation where businessPartner.id = :bp and $readableCriteria)').exec({
      bp: this.get('terminal').businessPartner
    }, function (data) {
      if (data[0]) {
        me.set('bplocation', data[0]);
      }
    });    
  },
  
  loadLocation: function () {
    var me = this;
    new OBPOS.Query('from Location where id = (select min(locationAddress) from OrganizationInformation where organization.id = :org and $readableCriteria)').exec({
      org: this.get('terminal').organization
    }, function (data) {
      if (data[0]) {
        me.set('location', data[0]);
      }
    });    
  }, 
  
  loadPriceList: function () {
    var me = this;    
    new OBPOS.Query('from PricingPriceList where id =:pricelist and $readableCriteria').exec({
      pricelist: this.get('terminal').priceList
    }, function (data) {
      if (data[0]) {
        me.set('pricelist', data[0]);
      }
    });    
  },
  
  loadPriceListVersion: function () {
    var me = this;
    new OBPOS.Query('select plv.id AS id from PricingPriceListVersion AS plv where plv.$readableCriteria and plv.priceList.id =:pricelist and plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = :pricelist)').exec({
      pricelist: this.get('terminal').priceList
    }, function (data) {
      if (data[0]) {
        me.set('pricelistversion', data[0]);
        me.initDS();
      }
    });    
  },
  
  initDS : function () {
    
/*    
    // DataSources...
    this.dsproduct = new OBPOS.DataSource(
    new OBPOS.Query(
        'select p as product, pp as price, img.bindaryData as img ' +
        'from PricingProductPrice as pp inner join pp.product p left outer join p.image img ' + 
        'where p.$readableCriteria and p.obposCatalog = true and pp.priceListVersion.id = :priceListVersion ' + 
        'order by p.obposLine, p.name'), {
      'priceListVersion': this.get('pricelistversion').id });

    this.dscategories = new OBPOS.DataSource(
    new OBPOS.Query(
        'select c as category, img.bindaryData as img ' + 
        'from ProductCategory as c left outer join c.obposImage img ' +
        'where c.$readableCriteria and c.oBPOSIsCatalog = true ' +
        'order by c.oBPOSPOSLine, c.name'));
*/    
    
    
    // DataSources...
    OBPOS.Sales.DSProduct = new OBPOS.DataSource(
    new OBPOS.Query(
        'select p as product, pp as price, img.bindaryData as img ' +
        'from PricingProductPrice as pp inner join pp.product p left outer join p.image img ' + 
        'where p.$readableCriteria and p.obposCatalog = true and pp.priceListVersion.id = :priceListVersion ' + 
        'order by p.obposLine, p.name'), {
      'priceListVersion': this.get('pricelistversion').id 
    });

    OBPOS.Sales.DSCategories = new OBPOS.DataSource(
    new OBPOS.Query(
        'select c as category, img.bindaryData as img ' + 
        'from ProductCategory as c left outer join c.obposImage img ' +
        'where c.$readableCriteria and c.oBPOSIsCatalog = true ' +
        'order by c.oBPOSPOSLine, c.name'));    
  }  
});