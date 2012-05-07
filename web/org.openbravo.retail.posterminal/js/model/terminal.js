/*global define, $, Backbone */

define(['datasource', 'utilities', 'utilitiesui'], function () {
  
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
      this.ds.exec(filter, function (data, info) {
        var i;
        me.reset();
        me.trigger('info', info);
        if (data.exception) {
          OB.UTIL.showError(data.exception.message);
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
    
    load: function (user, password) {
      
      // reset all application state.
      $(window).off('keypress');
      this.set('terminal', null);
      this.set('bplocation', null);
      this.set('location', null);
      this.set('pricelist', null);
      this.set('pricelistversion', null);
      
      // Starting app
      var me = this;
      var params = {
          terminal:OB.UTIL.getParameterByName("terminal") || "POS-1"
      };
  
      new OB.DS.Query('from OBPOS_Applications where $readableCriteria and searchKey = :terminal').exec(
        params,
        function (data) {
          if (data.exception) {
            me.triggerFail(data.exception);
          } else if (data[0]) {
            me.set('terminal', data[0]);
            me.loadBP();
            me.loadBPLocation();
            me.loadLocation();
            me.loadPriceList();
            me.loadPriceListVersion();
            me.loadCurrency();
          } else {
            OB.UTIL.showError("Terminal does not exists: " + params.terminal);
          }
        },
        user,
        password
      );        
    },
    
    loadBP: function () {
      var me = this;
      new OB.DS.Query('from BusinessPartner where id = :bp and $readableCriteria)').exec({
        bp: this.get('terminal').businessPartner
      }, function (data) {
        if (data[0]) {
          me.set('businesspartner', data[0]);
          me.triggerReady();
        }
      });    
    },
    
    loadBPLocation: function () {
      var me = this;
      new OB.DS.Query('from BusinessPartnerLocation where id = :bploc and $readableCriteria)').exec({
        bploc: this.get('terminal').partnerAddress
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
          me.triggerReady();
        }
      });    
    },
    
    loadCurrency: function () {
      var me = this;    
      new OB.DS.Query('from Currency where id =:currency and $readableCriteria').exec({
        currency: this.get('terminal').currency
      }, function (data) {
        if (data[0]) {
          me.set('currency', data[0]);
          me.triggerReady();
        }
      });    
    },   
    
    triggerReady: function () {
      if (this.get('pricelistversion') && this.get('currency') && this.get('businesspartner')) {
        this.trigger('ready');
      }     
    },
    
    triggerFail: function (exception) {
      this.trigger('fail', exception);
    }
  });
  
});