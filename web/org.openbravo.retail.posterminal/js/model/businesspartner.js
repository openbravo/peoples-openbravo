/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var BusinessPartner = OB.Data.ExtensibleModel.extend({
    modelName: 'BusinessPartner',
    tableName: 'c_bpartner',
    entityName: 'BusinessPartner',
    source: 'org.openbravo.retail.posterminal.master.BusinessPartner',
    dataLimit: 300,
    saveCustomer: function (silent) {
      var nameLength, newSk;

      if (!this.get("name")) {
        OB.UTIL.showWarning('Name is required for BPartner');
        return;
      }

      if (!this.get("locId")) {
        this.set('locId', OB.UTIL.get_UUID());
      }

      if (!this.get("contactId")) {
        this.set('contactId', OB.UTIL.get_UUID());
      }

      if (!this.get('searchKey')) {
        nameLength = this.get('name').toString().length;
        newSk = this.get('name');
        if (nameLength > 30) {
          newSk = this.get('name').substring(0, 30);
        }
        this.set('searchKey', newSk);
      }

      this.set('_identifier', this.get('name'));

      this.trigger('customerSaved');
      //datacustomersave will catch this event and save this locally with changed = 'Y'
      //Then it will try to send to the backend
    },
    loadById: function (CusId, userCallback) {
      //search data in local DB and load it to this
      var me = this,
          criteria = {
          id: CusId
          };
      OB.Dal.find(OB.Model.BusinessPartner, criteria, function (customerCol) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customerCol || customerCol.length === 0) {
          me.clearModelWith(null);
          userCallback(me);
        } else {
          me.clearModelWith(customerCol.at(0));
          userCallback(me);
        }
      });
    },
    loadByModel: function (cusToLoad) {
      //copy data from model to this
    },
    newCustomer: function () {
      //set values of new attrs in bp model 
      //this values will be copied to the created one
      //in the next instruction
      this.trigger('beforeChangeCustomerForNewOne', this);
      this.clearModelWith(null);
    },
    clearModelWith: function (cusToLoad) {
      var me = this,
          undf;
      if (cusToLoad === null) {
        this.set('id', null);
        this.set('searchKey', null);
        this.set('name', null);
        this.set('description', null);
        this.set('taxID', null);
        this.set('taxCategory', null);
        this.set('paymentMethod', OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod);
        this.set('businessPartnerCategory', OB.POS.modelterminal.get('terminal').defaultbp_bpcategory);
        this.set('businessPartnerCategory_name', OB.POS.modelterminal.get('terminal').defaultbp_bpcategory_name);
        this.set('paymentTerms', OB.POS.modelterminal.get('terminal').defaultbp_paymentterm);
        this.set('invoiceTerms', OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm);
        this.set('priceList', OB.POS.modelterminal.get('pricelist').id);
        this.set('country', OB.POS.modelterminal.get('terminal').defaultbp_bpcountry);
        this.set('client', OB.POS.modelterminal.get('terminal').client);
        this.set('organization', OB.POS.modelterminal.get('terminal').defaultbp_bporg);
        this.set('locId', null);
        this.set('locName', null);
        this.set('creditLimit', OB.DEC.Zero);
        this.set('creditUsed', OB.DEC.Zero);
        this.set('_identifier', null);
        this.set('postalCode', null);
        this.set('contactId', null);
        this.set('cityName', null);
        this.set('phone', null);
        this.set('email', null);
      } else {
        _.each(_.keys(cusToLoad.attributes), function (key) {
          if (cusToLoad.get(key) !== undf) {
            if (cusToLoad.get(key) === null) {
              me.set(key, null);
            } else if (cusToLoad.get(key).at) {
              //collection
              me.get(key).reset();
              cusToLoad.get(key).forEach(function (elem) {
                me.get(key).add(elem);
              });
            } else {
              //property
              me.set(key, cusToLoad.get(key));
            }
          }
        });
      }
    },
    loadByJSON: function (obj) {
      var me = this,
          undf;
      _.each(_.keys(me.attributes), function (key) {
        if (obj[key] !== undf) {
          if (obj[key] === null) {
            me.set(key, null);
          } else {
            me.set(key, obj[key]);
          }
        }
      });
    },
    serializeToJSON: function () {
      return JSON.parse(JSON.stringify(this.toJSON()));
    }
  });

  BusinessPartner.addProperties([{
    name: 'id',
    column: 'c_bpartner_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'organization',
    column: 'ad_org_id',
    type: 'TEXT'
  }, {
    name: 'searchKey',
    column: 'value',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: true,
    type: 'NUMERIC'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'description',
    column: 'description',
    type: 'TEXT'
  }, {
    name: 'taxID',
    column: 'taxID',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'taxCategory',
    column: 'so_bp_taxcategory_id',
    type: 'TEXT'
  }, {
    name: 'paymentMethod',
    column: 'FIN_Paymentmethod_ID',
    type: 'TEXT'
  }, {
    name: 'paymentTerms',
    column: 'c_paymentterm_id',
    type: 'TEXT'
  }, {
    name: 'priceList ',
    column: 'm_pricelist_id',
    type: 'TEXT '
  }, {
    name: 'invoiceTerms',
    column: 'invoicerule',
    type: 'TEXT'
  }, {
    name: 'locId',
    column: 'c_bpartnerlocation_id',
    type: 'TEXT'
  }, {
    name: 'locName',
    column: 'c_bpartnerlocation_name',
    type: 'TEXT'
  }, {
    name: 'postalCode',
    column: 'postalCode',
    type: 'TEXT'
  }, {
    name: 'cityName',
    column: 'cityName',
    type: 'TEXT'
  }, {
    name: 'contactId',
    column: 'ad_user_id',
    type: 'TEXT'
  }, {
    name: 'phone',
    column: 'phone',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'email',
    column: 'email',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'businessPartnerCategory',
    column: 'c_bp_group_id',
    type: 'TEXT'
  }, {
    name: 'businessPartnerCategory_name',
    column: 'c_bp_group_name',
    type: 'TEXT'
  }, {
    name: 'creditLimit',
    column: 'creditLimit',
    type: 'NUMERIC'
  }, {
    name: 'creditUsed',
    column: 'creditUsed',
    type: 'NUMERIC'
  }]);



  OB.Data.Registry.registerModel(BusinessPartner);
}());