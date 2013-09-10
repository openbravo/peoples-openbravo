/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var BPLocation = OB.Data.ExtensibleModel.extend({
    modelName: 'BPLocation',
    tableName: 'c_bpartner_location',
    entityName: 'BPLocation',
    source: 'org.openbravo.retail.posterminal.master.BPLocation',
    dataLimit: 300,
    local: false,
    saveCustomerAddr: function (silent) {
      var nameLength, newSk;
      if (!this.get("name")) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NameReqForBPAddress'));
        return false;
      }

      if (!this.get("postalCode")) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_PostalReqForBPAddress'));
        return false;
      }
      if (!this.get("cityName")) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CityReqForBPAddress'));
        return false;
      }

      this.set('_identifier', this.get('name'));
      this.trigger('customerAddrSaved');
      return true;
      //datacustomeraddrsave will catch this event and save this locally with changed = 'Y'
      //Then it will try to send to the backend
    },
    loadById: function (CusAddrId, userCallback) {
      //search data in local DB and load it to this
      var me = this,
          criteria = {
          id: CusAddrId
          };
      OB.Dal.find(OB.Model.BPLocation, criteria, function (customerAddr) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customerAddr || customerAddr.length === 0) {
          me.clearModelWith(null);
          userCallback(me);
        } else {
          me.clearModelWith(customerAddr.at(0));
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
        this.set('id', null);
        this.set('bpartner', null);
        this.set('name', null);
        this.set('postalCode', null);
        this.set('cityName', null);
        this.set('country', OB.POS.modelterminal.get('terminal').defaultbp_bpcountry);
        this.set('client', OB.POS.modelterminal.get('terminal').client);
        this.set('organization', OB.POS.modelterminal.get('terminal').defaultbp_bporg);
        this.set('_identifier', null);
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
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(BPLocation);
}());