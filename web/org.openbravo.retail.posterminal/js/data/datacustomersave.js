/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function () {

  OB.DATA = window.OB.DATA || {};

  OB.DATA.CustomerSave = function (model) {
    this.context = model;
    this.customer = model.get('customer');

    // trigger is from previous code, keeping it for backward compat
    this.customer.on('customerSaved', function () {
      OB.DATA.executeCustomerSave(this.customer);
    }, this);

    OB.DATA.executeCustomerSave = function (customer, callback) {
      var customersList, customerId = customer.get('id'),
          isNew = false,
          bpToSave = new OB.Model.ChangedBusinessPartners(),
          bpLocation, bpLocToSave = new OB.Model.BPLocation(),
          bpShipLocToSave = new OB.Model.BPLocation(),
          customersListToChange, updateLocally, finalCallback;

      var setBPLocationProperty = function (location, customer, sucesscallback) {
          if (!OB.UTIL.isNullOrUndefined(location) && !OB.UTIL.isNullOrUndefined(customer)) {
            var locName = customer.get('locId') === location.get('id') ? 'locName' : 'shipLocName',
                cityName = customer.get('locId') === location.get('id') ? 'cityName' : 'shipCityName',
                postalCode = customer.get('locId') === location.get('id') ? 'postalCode' : 'shipPostalCode',
                countryId = customer.get('locId') === location.get('id') ? 'countryId' : 'shipCountryId',
                countryName = customer.get('locId') === location.get('id') ? 'countryName' : 'shipCountryName';
            _.each(OB.Model.BPLocation.getPropertiesForUpdate(), function (property) {
              var key = property.name;
              if (!OB.UTIL.isNullOrUndefined(key)) {
                if (key === '_identifier') {
                  location.set(key, customer.get(locName));
                } else if (key === 'bpartner') {
                  location.set(key, customer.get('id'));
                } else if (key === 'name') {
                  location.set(key, customer.get(locName));
                } else if (key === 'cityName') {
                  location.set(key, customer.get(cityName));
                } else if (key === 'postalCode') {
                  location.set(key, customer.get(postalCode));
                } else if (key === 'countryName') {
                  var countryNameValue = customer.get(countryName) ? customer.get(countryName) : OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name;
                  location.set(key, countryNameValue);
                } else if (key === 'countryId') {
                  var countryIdValue = customer.get(countryId) ? customer.get(countryId) : OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
                  location.set(key, countryIdValue);
                } else if (key === 'isBillTo') {
                  location.set(key, customer.get('locId') === location.get('id'));
                } else if (key === 'isShipTo') {
                  location.set(key, customer.get('shipLocId') === location.get('id'));
                } else {
                  location.set(key, customer.get(key));
                }
              }
            });
            if (sucesscallback) {
              sucesscallback();
            }
          }
          };

      finalCallback = function (result) {
        if (callback) {
          callback(result);
        }
      };

      bpToSave.set('isbeingprocessed', 'N');
      customer.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      bpToSave.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (customerId) {
        customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        var now = new Date();
        customer.set('timezoneOffset', now.getTimezoneOffset());
        if (OB.UTIL.isNullOrUndefined(customer.get('loaded'))) {
          customer.set('loaded', OB.I18N.normalizeDate(new Date()));
        } else {
          customer.set('loaded', OB.I18N.normalizeDate(new Date(customer.get('loaded'))));
        }
        // Set locId only, So it will not create new location 
        bpLocation = customer.get('locationModel');
        customer.set('locId', bpLocation.get('id'));

        bpToSave.set('json', JSON.stringify(customer.serializeEditedToJSON()));
        bpToSave.set('c_bpartner_id', customer.get('id'));
      } else {
        isNew = true;
      }
      // if the bp is already used in one of the orders then update locally also
      updateLocally = !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true) || (!isNew && OB.MobileApp.model.orderList && _.filter(OB.MobileApp.model.orderList.models, function (order) {
        return order.get('bp').get('id') === customerId;
      }).length > 0);

      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) { //With high volume we only save locally when it is assigned to the order
        if (isNew) {
          customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
          var uuid = OB.UTIL.get_UUID();
          customer.set('id', uuid);
          customer.id = uuid;
          bpToSave.set('json', JSON.stringify(customer.serializeToJSON()));
          bpToSave.set('id', customer.get('id'));
        }

        bpToSave.set('isbeingprocessed', 'Y');

        if (!updateLocally) {
          OB.UTIL.HookManager.executeHooks('OBPOS_PostCustomerSave', {
            customer: customer,
            bpToSave: bpToSave
          }, function (args) {
            bpToSave.set('json', JSON.stringify(customer.serializeToJSON()));
            OB.Dal.save(bpToSave, function () {
              var successCallback = function () {
                  OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [customer.get('_identifier')]));
                  finalCallback(true);
                  };
              OB.MobileApp.model.runSyncProcess(successCallback, function () {
                finalCallback(false);
              });
            }, function () {
              //error saving BP changes with changes in changedbusinesspartners
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [customer.get('_identifier')]));
              finalCallback(false);
            }, isNew);
          });
        }
      }

      if (updateLocally) {
        //save that the customer is being processed by server
        customer.set('loaded', OB.I18N.normalizeDate(new Date()));
        OB.Dal.save(customer, function () {
          //OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSavedSuccessfullyLocally',[customer.get('_identifier')]));
          // Saving Customer Address locally
          if (isNew) {
            //create bploc from scratch and set all properties
            bpLocToSave.set('id', customer.get('locId'));
            setBPLocationProperty(bpLocToSave, customer, function () {
              OB.Dal.save(bpLocToSave, function () {
                //check if shipping address is different
                if (customer.get('locId') !== customer.get('shipLocId')) {
                  bpShipLocToSave = new OB.Model.BPLocation();
                  bpLocToSave.set('id', customer.get('shipLocId'));
                  setBPLocationProperty(bpLocToSave, customer, function () {
                    OB.Dal.save(bpLocToSave, function () {}, function () {
                      OB.error(arguments);
                    }, isNew);
                  });
                }
              }, function () {
                OB.error(arguments);
              }, isNew);
            });
          }

          if (isNew) {
            customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
            bpToSave.set('json', JSON.stringify(customer.serializeToJSON()));
            bpToSave.set('c_bpartner_id', customer.get('id'));
          }
          bpToSave.set('isbeingprocessed', 'Y');
          OB.UTIL.HookManager.executeHooks('OBPOS_PostCustomerSave', {
            customer: customer,
            bpToSave: bpToSave
          }, function (args) {
            OB.Dal.save(bpToSave, function () {
              // update each order also so that new name is shown and the bp
              // in the order is the same as what got saved
              if (OB.MobileApp.model.orderList) {
                _.forEach(OB.MobileApp.model.orderList.models, function (order) {
                  if (order.get('bp').get('id') === customerId) {
                    var clonedBP = new OB.Model.BusinessPartner();
                    OB.UTIL.clone(customer, clonedBP);
                    var bp = order.get('bp');
                    if (order.get('bp').get('locId') !== customer.get('locId')) {
                      // if the order has a different address but same BP than the bp
                      // then copy over the address data
                      clonedBP.set('locId', bp.get('locId'));
                      clonedBP.set('locName', bp.get('locName'));
                      clonedBP.set('postalCode', bp.get('postalCode'));
                      clonedBP.set('cityName', bp.get('cityName'));
                      clonedBP.set('countryName', bp.get('countryName'));
                      clonedBP.set('locationModel', bp.get('locationModel'));
                    }
                    if (order.get('bp').get('shipLocId') !== customer.get('shipLocId')) {
                      clonedBP.set('shipLocId', bp.get('shipLocId'));
                      clonedBP.set('shipLocName', bp.get('shipLocName'));
                      clonedBP.set('shipPostalCode', bp.get('shipPostalCode'));
                      clonedBP.set('shipCityName', bp.get('shipCityName'));
                      clonedBP.set('shipCountryName', bp.get('shipCountryName'));
                    }
                    order.set('bp', clonedBP);
                    order.save();
                    if (OB.MobileApp.model.orderList.modelorder && OB.MobileApp.model.orderList.modelorder.get('id') === order.get('id')) {
                      OB.MobileApp.model.orderList.modelorder.setBPandBPLoc(clonedBP, false, true);
                    }
                  }
                });
              }

              var successCallback;
              successCallback = function () {
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [customer.get('_identifier')]));
                finalCallback(true);
              };
              OB.MobileApp.model.runSyncProcess(successCallback, function () {
                finalCallback(false);
              });
            }, function () {
              //error saving BP changes with changes in changedbusinesspartners
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [customer.get('_identifier')]));
              finalCallback(false);
            });
          });
        }, function () {
          //error saving BP with new values in c_bpartner
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerLocally', [customer.get('_identifier')]));
          finalCallback(false);
        });
      }

    };
  };
}());