/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _ */

(function () {

  var BusinessPartner = OB.Data.ExtensibleModel.extend({
    modelName: 'BusinessPartner',
    tableName: 'c_bpartner',
    entityName: 'BusinessPartner',
    source: 'org.openbravo.retail.posterminal.master.BusinessPartner',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
    remote: 'OBPOS_remote.customer',
    saveCustomer: function (callback) {
      var nameLength, newSk;

      if (!this.get('name')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerNameRequired'));
        return false;
      }

      if (!this.get('id')) {
        if (this.get('useSameAddrForShipAndInv')) {
          //Create 1 address for shipping and invoicing
          if (!this.get('locName')) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerAddressRequired'));
            return false;
          }
          this.set('locId', OB.UTIL.get_UUID());
          this.set('shipLocId', this.get('locId'));
          this.set('shipLocName', this.get('locName'));
          this.set('shipPostalCode', this.get('postalCode'));
          this.set('shipCityName', this.get('cityName'));
          this.set('shipCountryName', this.get('countryName'));
          this.set('shipCountryId', this.get('countryId'));
        } else {
          //Create 1 address for shipping and 1 for invoicing
          if (!this.get('locName') || !this.get('shipLocName')) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerAddressRequired'));
            return false;
          }
          this.set('locId', OB.UTIL.get_UUID());
          this.set('shipLocId', OB.UTIL.get_UUID());
        }
      }

      if (!this.get('contactId')) {
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

      if (this.get('birthDay') && typeof this.get('birthDay') !== 'object') {
        return;
      }

      if (this.get('birthDay') && !OB.UTIL.isInThePast(OB.I18N.formatDate(this.get('birthDay')))) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerBirthDayIncorrect'));
        return false;
      }

      this.set('_identifier', this.get('name'));

      // in case of synchronized then directly call customer save with the callback
      if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
        OB.DATA.executeCustomerSave(this, callback);
      } else {
        this.trigger('customerSaved');
        callback();
      }
      return true;
    },
    loadById: function (CusId, userCallback) {
      //search data in local DB and load it to this
      var me = this;
      OB.Dal.get(OB.Model.BusinessPartner, CusId, function (customerCol) { //OB.Dal.get success
        if (!customerCol || customerCol.length === 0) {
          me.clearModelWith(null);
          userCallback(me);
        } else if (!_.isNull(customerCol.get('shipLocId'))) {
          OB.Dal.get(OB.Model.BPLocation, customerCol.get('shipLocId'), function (location) { //OB.Dal.find success
            customerCol.set('locationModel', location);
            me.clearModelWith(customerCol);
            userCallback(me);
          });
        } else {
          me.clearModelWith(customerCol);
          userCallback(me);
        }
      });
    },
    loadModel: function (customerCol, userCallback) {
      //search data in local DB and load it to this
      var me = this;
      if (!customerCol || customerCol.length === 0) {
        me.clearModelWith(null);
        userCallback(me);
      } else {
        OB.Dal.get(OB.Model.BPLocation, customerCol.get('locId'), function (location) { //OB.Dal.find success
          customerCol.set('locationModel', location);
          me.clearModelWith(customerCol);
          userCallback(me);
        });
      }
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

        OB.UTIL.clone(new OB.Model.BusinessPartner(), this);

        this.set('paymentMethod', OB.MobileApp.model.get('terminal').defaultbp_paymentmethod);
        this.set('businessPartnerCategory', OB.MobileApp.model.get('terminal').defaultbp_bpcategory);
        this.set('businessPartnerCategory_name', OB.MobileApp.model.get('terminal').defaultbp_bpcategory_name);
        this.set('paymentTerms', OB.MobileApp.model.get('terminal').defaultbp_paymentterm);
        this.set('invoiceTerms', OB.MobileApp.model.get('terminal').defaultbp_invoiceterm);
        this.set('priceList', OB.MobileApp.model.get('pricelist').id);
        this.set('client', OB.MobileApp.model.get('terminal').client);
        this.set('organization', OB.MobileApp.model.get('terminal').defaultbp_bporg);
        this.set('creditLimit', OB.DEC.Zero);
        this.set('creditUsed', OB.DEC.Zero);
      } else {
        OB.UTIL.clone(cusToLoad, this);
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
    adjustNames: function () {
      var firstName = this.get('firstName'),
          lastName = this.get('lastName'),
          fullName;
      if (firstName) {
        firstName = firstName.trim();
      }
      if (lastName) {
        lastName = lastName.trim();
      }
      this.set('firstName', firstName);
      this.set('lastName', lastName);

      fullName = firstName + (lastName ? ' ' + lastName : '');
      if (fullName.length > 60) {
        fullName = fullName.substring(0, 60);
      }
      this.set('name', fullName);
    },
    serializeEditedToJSON: function () {
      var me = this,
          editedBp = new OB.Model.BusinessPartner();
      //Set entities ids: BusinessPartner, Location and User
      editedBp.set('id', this.get('id'));
      editedBp.set('locId', this.get('locId'));
      editedBp.set('contactId', this.get('contactId'));
      editedBp.set('timezoneOffset', this.get('timezoneOffset'));
      editedBp.set('loaded', this.get('loaded'));
      editedBp.set('posTerminal', this.get('posTerminal'));
      //Set only form attributes
      _.each(OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl.prototype.newAttributes, function (model) {
        if (model.setEditedProperties) {
          model.setEditedProperties(me, editedBp);
        } else {
          editedBp.set(model.modelProperty, me.get(model.modelProperty));
        }
      });
      editedBp.adjustNames();
      return JSON.parse(JSON.stringify(editedBp.toJSON()));
    },
    serializeToJSON: function () {
      return JSON.parse(JSON.stringify(this.toJSON()));
    },
    loadBPLocations: function (shipping, billing, callback) {
      var criteria = {
        bpartner: {
          operator: OB.Dal.EQ,
          value: this.get('id')
        }
      };
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        var filterBpartnerId = {
          columns: ['bpartner'],
          operator: OB.Dal.EQ,
          value: this.get('id'),
          isId: true
        };
        criteria.remoteFilters = [filterBpartnerId];
      }
      OB.Dal.find(OB.Model.BPLocation, criteria, function (collection) {
        if (!billing) {
          billing = _.find(collection.models, function (loc) {
            return loc.get('isBillTo');
          });
        }
        if (!shipping) {
          shipping = _.find(collection.models, function (loc) {
            return loc.get('isShipTo');
          });
        }
        callback(shipping, billing, collection.models);
      });
    },
    setBPLocations: function (shipping, billing, locationModel) {
      if (shipping) {
        this.set('shipLocId', shipping.get('id'));
        this.set('shipLocName', shipping.get('name'));
        this.set('shipCityName', shipping.get('cityName'));
        this.set('shipPostalCode', shipping.get('postalCode'));
      } else {
        this.set('shipLocId', null);
        this.set('shipLocName', null);
        this.set('shipCityName', null);
        this.set('shipPostalCode', null);
      }
      if (billing) {
        this.set("locId", billing.get("id"));
        this.set("locName", billing.get("name"));
        this.set('cityName', billing.get('cityName'));
        this.set('postalCode', billing.get('postalCode'));
        this.set('countryName', billing.get('countryName'));
      } else {
        this.set("locId", null);
        this.set("locName", null);
        this.set('cityName', null);
        this.set('postalCode', null);
        this.set('countryName', null);
      }
      if (locationModel) {
        this.set('locationModel', shipping);
        if (shipping !== null) {
          // Change these information if it's null or undefined. Otherwise, the data is correctly set
          if (OB.UTIL.isNullOrUndefined(this.get('cityName'))) {
            this.set('cityName', shipping.get('cityName'));
          }
          if (OB.UTIL.isNullOrUndefined(this.get('countryName'))) {
            this.set('countryName', shipping.get('countryName'));
          }
          if (OB.UTIL.isNullOrUndefined(this.get('postalCode'))) {
            this.set('postalCode', shipping.get('postalCode'));
          }
        }
      }
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
    filter: true,
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
    name: 'firstName',
    column: 'first_name',
    type: 'TEXT'
  }, {
    name: 'lastName',
    column: 'last_name',
    type: 'TEXT'
  }, {
    name: 'description',
    column: 'description',
    type: 'TEXT'
  }, {
    name: 'taxID',
    column: 'taxID',
    filter: true,
    skipremote: true,
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
    name: 'priceList',
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
    filter: true,
    skipremote: true,
    type: 'TEXT'
  }, {
    name: 'postalCode',
    column: 'postalCode',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'cityName',
    column: 'cityName',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'regionId',
    column: 'regionId',
    type: 'TEXT'
  }, {
    name: 'countryId',
    column: 'countryId',
    type: 'TEXT'
  }, {
    name: 'countryName',
    column: 'countryName',
    type: 'TEXT'
  }, {
    name: 'shipLocId',
    column: 'shipLocId',
    type: 'TEXT'
  }, {
    name: 'shipLocName',
    column: 'shipLocName',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'shipPostalCode',
    column: 'shipPostalCode',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'shipCityName',
    column: 'shipCityName',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'shipRegionId',
    column: 'shipRegionId',
    type: 'TEXT'
  }, {
    name: 'shipCountryId',
    column: 'shipCountryId',
    type: 'TEXT'
  }, {
    name: 'contactId',
    column: 'ad_user_id',
    type: 'TEXT'
  }, {
    name: 'phone',
    column: 'phone',
    filter: true,
    skipremote: true,
    type: 'TEXT'
  }, {
    name: 'email',
    column: 'email',
    filter: true,
    skipremote: true,
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
  }, {
    name: 'taxExempt',
    column: 'taxExempt',
    type: 'TEXT'
  }, {
    name: 'customerBlocking',
    column: 'customerBlocking',
    type: 'TEXT'
  }, {
    name: 'salesOrderBlocking',
    column: 'salesOrderBlocking',
    type: 'TEXT'
  }, {
    name: 'priceIncludesTax',
    column: 'priceIncludesTax',
    type: 'TEXT'
  }, {
    name: 'priceListName',
    column: 'priceListName',
    type: 'TEXT'
  }, {
    name: 'loaded',
    column: 'loaded',
    type: 'TEXT'
  }, {
    name: 'birthDay',
    column: 'birthDay',
    type: 'TEXT'
  }, {
    name: 'birthPlace',
    column: 'birthPlace',
    type: 'TEXT'
  }]);

  BusinessPartner.addIndex([{
    name: 'bp_filter_idx',
    columns: [{
      name: '_filter',
      sort: 'desc'
    }]
  }, {
    name: 'bp_name_idx',
    columns: [{
      name: 'name',
      sort: 'desc'
    }]
  }]);

  OB.Data.Registry.registerModel(BusinessPartner);
}());