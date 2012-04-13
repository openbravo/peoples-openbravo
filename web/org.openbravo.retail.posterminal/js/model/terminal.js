/*global define, Backbone */

define(['datasource', 'utilities'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
  OB.MODEL.Collection = Backbone.Collection.extend({
    constructor: function (data) {
      this.ds = data.ds;
      Backbone.Collection.prototype.constructor.call(this);
    },
    inithandler : function (init) { 
       if (init) {
         init.call(this);
       }
    },
    exec : function (filter) {
      var me = this;
      this.ds.exec(filter, function (data) {
        var i;
        me.reset();
        if (data.exception) {
          alert(data.exception.message);
        } else {
          for (i in data) {
            if(data.hasOwnProperty(i)) {
              me.add(data[i]);
            }
          }
        }
      });
    }
  });  
  

  // Terminal model.
  
  OB.MODEL.Terminal = Backbone.Model.extend({
    
    defaults : {
      terminal: null,
      bplocation: null,
      location: null,
      pricelist: null,
      pricelistversion: null
    },
    
    load: function () {
      var me = this;
      var t = OB.UTIL.getParameterByName("terminal") || "POS-1";
  
      new OB.DS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal').exec({
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
      new OB.DS.Query('from BusinessPartnerLocation where id = (select min(id) from BusinessPartnerLocation where businessPartner.id = :bp and $readableCriteria)').exec({
        bp: this.get('terminal').businessPartner
      }, function (data) {
        if (data[0]) {
          me.set('bplocation', data[0]);
        }
      });    
    },
    
    loadLocation: function () {
      var me = this;
      new OB.DS.Query('from Location where id = (select min(locationAddress) from OrganizationInformation where organization.id = :org and $readableCriteria)').exec({
        org: this.get('terminal').organization
      }, function (data) {
        if (data[0]) {
          me.set('location', data[0]);
        }
      });    
    }, 
    
    loadPriceList: function () {
      var me = this;    
      new OB.DS.Query('from PricingPriceList where id =:pricelist and $readableCriteria').exec({
        pricelist: this.get('terminal').priceList
      }, function (data) {
        if (data[0]) {
          me.set('pricelist', data[0]);
        }
      });    
    },
    
    loadPriceListVersion: function () {
      var me = this;
      new OB.DS.Query('select plv.id AS id from PricingPriceListVersion AS plv where plv.$readableCriteria and plv.priceList.id =:pricelist and plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = :pricelist)').exec({
        pricelist: this.get('terminal').priceList
      }, function (data) {
        if (data[0]) {
          me.set('pricelistversion', data[0]);
          me.trigger('ready');
        }
      });    
    }
  });
  
});