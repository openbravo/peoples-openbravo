/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB, Backbone, _ */

(function () {

  var BPLocation = OB.Data.ExtensibleModel.extend({
    modelName: 'BPLocation',
    tableName: 'c_bpartner_location',
    entityName: 'BPLocation',
    source: 'org.openbravo.retail.posterminal.master.BPLocation',
    dataLimit: OB.Dal.DATALIMIT,
    local: false,
    remote: 'OBPOS_remote.customer',
    saveCustomerAddr: function (callback) {
      var nameLength, newSk;
      if (!this.get('isBillTo') && !this.get('isShipTo')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerAddrChn', [this.get('_identifier')]));
        return;
      }
      this.set('_identifier', this.get('name'));

      if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
        OB.DATA.executeCustomerAddressSave(this, callback);
      } else {
        this.trigger('customerAddrSaved');
        callback();
      }

      return true;
    },
    loadById: function (CusAddrId, userCallback) {
      //search data in local DB and load it to this
      var me = this;
      OB.Dal.get(OB.Model.BPLocation, CusAddrId, function (customerAddr) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customerAddr || customerAddr.length === 0) {
          me.clearModelWith(null);
          userCallback(me);
        } else {
          me.clearModelWith(customerAddr);
          userCallback(me);
        }
      });
    },
    newCustomerAddr: function () {
      //set values of new attrs in bplocation model
      //this values will be copied to the created one
      //in the next instruction
      this.trigger('beforeChangeCustomerAddrForNewOne', this);
      this.clearModelWith(null);
    },
    clearModelWith: function (cusToLoad) {
      var me = this,
          undf;
      if (cusToLoad === null) {

        OB.UTIL.clone(new OB.Model.BPLocation(), this);

        this.set('countryId', OB.MobileApp.model.get('terminal').defaultbp_bpcountry);
        this.set('countryName', OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name);
        this.set('client', OB.MobileApp.model.get('terminal').client);
        this.set('organization', OB.MobileApp.model.get('terminal').defaultbp_bporg);
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
    serializeToJSON: function () {
      return JSON.parse(JSON.stringify(this.toJSON()));
    }
  });

  BPLocation.addProperties([{
    name: 'id',
    column: 'c_bpartner_location_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'bpartner',
    column: 'c_bpartner_id',
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
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
    name: 'countryName',
    column: 'countryName',
    type: 'TEXT'
  }, {
    name: 'countryId',
    column: 'countryId',
    type: 'TEXT'
  }, {
    name: 'regionName',
    column: 'regionName',
    type: 'TEXT'
  }, {
    name: 'regionId',
    column: 'regionId',
    type: 'TEXT'
  }, {
    name: 'isBillTo',
    column: 'isBillTo',
    primaryKey: false,
    filter: false,
    type: 'TEXT'
  }, {
    name: 'isShipTo',
    column: 'isShipTo',
    primaryKey: false,
    filter: false,
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'loaded',
    column: 'loaded',
    type: 'TEXT'
  }]);

  BPLocation.addIndex([{
    name: 'bploc_name_idx',
    columns: [{
      name: 'name',
      sort: 'desc'
    }]
  }]);

  OB.Data.Registry.registerModel(BPLocation);
}());