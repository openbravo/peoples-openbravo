/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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
      var customersList, customerId = this.customer.get('id'),
          isNew = false,
          bpToSave = new OB.Model.ChangedBusinessPartners(),
          bpLocation, bpLocToSave = new OB.Model.BPLocation(),
          bpShipLocToSave = new OB.Model.BPLocation(),
          customersListToChange, updateLocally;

      bpToSave.set('isbeingprocessed', 'N');
      customer.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      bpToSave.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (customerId) {
        customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        var now = new Date();
        customer.set('timezoneOffset', now.getTimezoneOffset());
        customer.set('loaded', OB.I18N.normalizeDate(new Date(customer.get('loaded'))));
        bpToSave.set('json', JSON.stringify(this.customer.serializeEditedToJSON()));
        bpToSave.set('c_bpartner_id', this.customer.get('id'));
      } else {
        isNew = true;
      }

      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) { //With high volume we only save locally when it is assigned to the order
        if (isNew) {
          customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
          var uuid = OB.UTIL.get_UUID();
          customer.set('id', uuid);
          customer.id = uuid;
          bpToSave.set('json', JSON.stringify(customer.serializeToJSON()));
          bpToSave.set('id', customer.get('id'));
        }

        // the location is sent to the server as part of the BP save, but when the BP is 
        // added directly to the ticket it also needs the location object in the BP, in the
        // locationModel
        // This can be done by creating the object as below or requesting it again from the server
        // but this takes another request, that's why it is created here as an object
        if (!bpToSave.get('locationModel')) {
          bpLocation = new OB.Model.BPLocation();
          bpLocation.set('id', customer.get('locId'));
          bpLocation.set('bpartner', customer.get('id'));
          bpLocation.set('name', customer.get('locName'));
          bpLocation.set('postalCode', customer.get('postalCode'));
          bpLocation.set('cityName', customer.get('cityName'));
          bpLocation.set('_identifier', customer.get('locName'));
          bpLocation.set('countryName', customer.get('countryName'));
          bpLocation.set('countryId', customer.get('countryId'));
          bpToSave.set('locationModel', bpLocation);
        }

        bpToSave.set('isbeingprocessed', 'Y');
        OB.UTIL.HookManager.executeHooks('OBPOS_PostCustomerSave', {
          customer: customer,
          bpToSave: bpToSave
        }, function (args) {
          OB.Dal.save(bpToSave, function () {
            bpToSave.set('json', customer.serializeToJSON());
            var successCallback = function () {
                if (callback) {
                  callback();
                }
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [customer.get('_identifier')]));
                };
            OB.MobileApp.model.runSyncProcess(successCallback);
          }, function () {
            //error saving BP changes with changes in changedbusinesspartners
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [customer.get('_identifier')]));
          }, isNew);
        });
      }

      // if the bp is already used in one of the orders then update locally also
      updateLocally = !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true) || (!isNew && OB.MobileApp.model.orderList && _.filter(OB.MobileApp.model.orderList.models, function (order) {
        return order.get('bp').get('id') === customerId;
      }).length > 0);

      if (updateLocally) {
        //save that the customer is being processed by server
        customer.set('loaded', OB.I18N.normalizeDate(new Date()));
        OB.Dal.save(customer, function () {
          //OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSavedSuccessfullyLocally',[customer.get('_identifier')]));
          // Saving Customer Address locally
          if (isNew) {
                  // update each order also so that new name is shown and the bp
                  // in the order is the same as what got saved
                  if (OB.MobileApp.model.orderList) {
                    _.forEach(OB.MobileApp.model.orderList.models, function (order) {
                      if (order.get('bp').get('id') === customerId) {
                        var clonedBP = new OB.Model.BusinessPartner();
                        OB.UTIL.clone(customer, clonedBP);
                        if (order.get('bp').get('locId') !== customer.get('locId')) {
                          // if the order has a different address than the bp
                          // then copy over the address data
                          var bp = order.get('bp');
                          clonedBP.set('locId', bp.get('locId'));
                          clonedBP.set('locName', bp.get('locName'));
                          clonedBP.set('postalCode', bp.get('postalCode'));
                          clonedBP.set('cityName', bp.get('cityName'));
                          clonedBP.set('countryName', bp.get('countryName'));
                          clonedBP.set('locationModel', bp.get('locationModel'));
                        }
                        order.set('bp', clonedBP);
                        order.save();
                        if (OB.MobileApp.model.orderList.modelorder && OB.MobileApp.model.orderList.modelorder.get('id') === order.get('id')) {
                          OB.MobileApp.model.orderList.modelorder.setBPandBPLoc(clonedBP, false, true);
                        }
                      }
                    });
                  }

            //create bploc from scratch
            if (customer.get('useSameAddrForShipAndInv')) {
              bpLocToSave.set('id', customer.get('locId'));
              bpLocToSave.set('bpartner', customer.get('id'));
              bpLocToSave.set('name', customer.get('locName'));
              bpLocToSave.set('isBillTo', true);
              bpLocToSave.set('isShipTo', true);
              bpLocToSave.set('postalCode', customer.get('postalCode'));
              bpLocToSave.set('cityName', customer.get('cityName'));
              bpLocToSave.set('_identifier', customer.get('locName'));
              bpLocToSave.set('countryName', customer.get('countryName'));
              bpLocToSave.set('countryId', customer.get('countryId'));
              OB.Dal.save(bpLocToSave, function () {
                //customer location created successfully. Nothing to do here.
              }, function () {
                OB.error(arguments);
              }, true);
            } else {
              bpLocToSave.set('id', customer.get('locId'));
              bpLocToSave.set('bpartner', customer.get('id'));
              bpLocToSave.set('name', customer.get('locName'));
              bpLocToSave.set('isBillTo', true);
              bpLocToSave.set('isShipTo', false);
              bpLocToSave.set('postalCode', customer.get('postalCode'));
              bpLocToSave.set('cityName', customer.get('cityName'));
              bpLocToSave.set('_identifier', customer.get('locName'));
              bpLocToSave.set('countryName', customer.get('countryName'));
              bpLocToSave.set('countryId', customer.get('countryId'));

              bpShipLocToSave.set('id', customer.get('shipLocId'));
              bpShipLocToSave.set('bpartner', customer.get('id'));
              bpShipLocToSave.set('name', customer.get('shipLocName'));
              bpShipLocToSave.set('isBillTo', false);
              bpShipLocToSave.set('isShipTo', true);
              bpShipLocToSave.set('postalCode', customer.get('shipPostalCode'));
              bpShipLocToSave.set('cityName', customer.get('shipCityName'));
              bpShipLocToSave.set('_identifier', customer.get('shipLocName'));
              bpShipLocToSave.set('countryName', customer.get('shipCountryName'));
              bpShipLocToSave.set('countryId', customer.get('shipCountryId'));

              OB.Dal.save(bpLocToSave, function () {
                //customer billing location created successfully. Nothing to do here.
              }, function () {
                OB.error(arguments);
              }, true);
              OB.Dal.save(bpShipLocToSave, function () {
                //customer shipping location created successfully. Nothing to do here.
              }, function () {
                OB.error(arguments);
              }, true);
            }
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
              bpToSave.set('json', customer.serializeToJSON());
              var successCallback, errorCallback, List;
              successCallback = function () {
                if (callback) {
                  callback();
                }
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [customer.get('_identifier')]));
              };
              OB.MobileApp.model.runSyncProcess(successCallback);
            }, function () {
              //error saving BP changes with changes in changedbusinesspartners
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [customer.get('_identifier')]));
            });
          });
        }, function () {
          //error saving BP with new values in c_bpartner
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerLocally', [customer.get('_identifier')]));
        });
      }

    };
  };
}());