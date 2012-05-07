/*global define,_*/

define(['utilities', 'datasource'], function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.Container = function (context) {
    this.context = context;
    this.datachildren = [];
  };

  OB.DATA.Container.prototype.append = function (child) {
    this.datachildren.push(child);
  };  

  OB.DATA.Container.prototype.inithandler = function () {  
    var i, max;
    for (i = 0, max = this.datachildren.length; i < max; i++) {
      this.datachildren[i].load();
    }
  };

  OB.DATA.Base = {
    load: function () { 
       this.ds.load(this.loadparams);
    } 
  };

  OB.DATA.BPs = function (context) {
    this._id = 'DataBPs';
    this.context = context;
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
      'select bp as BusinessPartner, loc as BusinessPartnerLocation ' + 
      'from BusinessPartner bp, BusinessPartnerLocation loc ' + 
      'where bp.id = loc.businessPartner.id and bp.customer = true and bp.$readableCriteria'));
    this.loadparams = {};     
  };
  _.extend(OB.DATA.BPs.prototype, OB.DATA.Base);

  OB.DATA.ProductPrice = function (context, id) {
    this._id = 'DataProductPrice';
    this.context = context;
    this.ds = new OB.DS.DataSourceProductPrice(
      new OB.DS.Query(
      'select p as product, img.bindaryData as img ' + 
      'from Product p left outer join p.image img ' + 
      'where p.$readableCriteria and p.obposCatalog = true order by p.obposLine, p.name'),
      new OB.DS.Query(
      'from PricingProductPrice where priceListVersion in ' + 
      '(select plv.id from PricingPriceList as ppl, PricingPriceListVersion as plv ' + 
      'where ppl.organization.id = :org and ppl.salesPriceList = true  and ppl.$readableCriteria and ppl.id = plv.priceList.id  and ' + 
      'plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = ppl.id))')
    );
    this.loadparams = {product: {}, productprice: {'org': OB.POS.modelterminal.get('terminal').organization }};
  };
 _.extend(OB.DATA.ProductPrice.prototype, OB.DATA.Base);
 
  OB.DATA.Category = function (context, id) {
    this._id = 'DataCategory';
    this.context = context;    
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
        'select c as category, img.bindaryData as img ' + 
        'from ProductCategory as c left outer join c.obposImage img ' +
        'where c.$readableCriteria and c.oBPOSIsCatalog = true ' +
        'order by c.oBPOSPOSLine, c.name'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.Category.prototype, OB.DATA.Base);
 
  OB.DATA.TaxRate = function (context, id) {
    this._id = 'DataTaxRate';
    this.context = context;    
    this.ds = new OB.DS.DataSource(new OB.DS.Query(
        'from FinancialMgmtTaxRate where $readableCriteria'));
    this.loadparams = {};
  };
  _.extend(OB.DATA.Category.prototype, OB.DATA.Base);  
});