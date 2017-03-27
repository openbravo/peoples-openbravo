/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB, _ */

(function () {

  OB.DATA = window.OB.DATA || {};

  OB.DATA.CustomerAddrSave = function (model) {
    this.context = model;
    this.customerAddr = model.get('customerAddr');

    // trigger is from previous code, keeping it for backward compat
    this.customerAddr.on('customerAddrSaved', function () {
      OB.DATA.executeCustomerAddressSave(this.customerAddr);
    }, this);

    OB.DATA.updateDefaultCustomerLocations = function (customerAddr) {
      if (customerAddr.get('bpartner') === OB.MobileApp.model.get('businesspartner')) {
        // Update default BP locations cache
        var i, foundAddress = false,
            locations = OB.MobileApp.model.get('businessPartner').get('locations');
        for (i = 0; i < locations.length; i++) {
          if (locations[i].get('id') === customerAddr.get('id')) {
            locations[i] = customerAddr.clone();
            foundAddress = true;
            break;
          }
        }
        if (!foundAddress) {
          locations.push(customerAddr.clone());
        }
      }
    };

    OB.DATA.executeCustomerAddressSave = function (customerAddr, callback) {
      var customerAddrList, customerAddrId = customerAddr.get('id'),
          isNew = false,
          bpLocToSave = new OB.Model.ChangedBPlocation(),
          customerAddrListToChange, updateLocally, me = this;

      bpLocToSave.set('isbeingprocessed', 'N');
      customerAddr.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      bpLocToSave.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (customerAddrId) {
        customerAddr.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        var now = new Date();
        customerAddr.set('timezoneOffset', now.getTimezoneOffset());
        customerAddr.set('loaded', OB.I18N.normalizeDate(new Date(customerAddr.get('loaded'))));
        bpLocToSave.set('json', JSON.stringify(customerAddr.serializeToJSON()));
        bpLocToSave.set('c_bpartner_location_id', customerAddr.get('id'));
      } else {
        isNew = true;
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) { //With high volume we only save address we it is assigned to the order
        if (isNew) {
          me.receipt.get('bp').set('moreaddress', true); // For to show two address buttons in receipt
          customerAddr.set('posTerminal', OB.MobileApp.model.get('terminal').id);
          var uuid = OB.UTIL.get_UUID();
          customerAddr.set('id', uuid);
          customerAddr.id = uuid;
          bpLocToSave.set('json', JSON.stringify(customerAddr.serializeToJSON()));
          bpLocToSave.set('id', customerAddr.get('id'));
        }
        me.receipt.trigger('change:bp', me.receipt);
        bpLocToSave.set('isbeingprocessed', 'Y');
        OB.DATA.updateDefaultCustomerLocations(customerAddr);
        OB.Dal.save(bpLocToSave, function () {
          bpLocToSave.set('json', JSON.stringify(customerAddr.serializeToJSON()));
          var successCallback, errorCallback, List;
          successCallback = function () {
            if (callback) {
              callback();
            }
            // update each order also so that new name is shown
            if (OB.MobileApp.model.orderList) {
              _.forEach(OB.MobileApp.model.orderList.models, function (order) {
                if (order.get('bp').get('locId') === customerAddr.get('id')) {
                  var bp = order.get('bp');
                  bp.set('locId', customerAddr.get('id'));
                  bp.set('locName', customerAddr.get('name'));
                  bp.set('postalCode', customerAddr.get('postalCode'));
                  bp.set('cityName', customerAddr.get('cityName'));
                  bp.set('countryName', customerAddr.get('countryName'));
                  bp.set('locationModel', customerAddr);
                  order.save();
                  // refresh the display
                  if (OB.MobileApp.model.orderList.modelorder && OB.MobileApp.model.orderList.modelorder.get('id') === order.get('id')) {
                    OB.MobileApp.model.orderList.modelorder.setBPandBPLoc(bp, false, true);
                  }
                }
              });
            }
            OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerAddrSaved', [customerAddr.get('_identifier')]));
          };
          customerAddrListToChange = new OB.Collection.ChangedBPlocationList();
          customerAddrListToChange.add(bpLocToSave);
          OB.MobileApp.model.runSyncProcess(successCallback);
        }, function () {
          //error saving BP changes with changes in changedbusinesspartners
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerAddrChn', [customerAddr.get('_identifier')]));
        }, isNew);
      }

      // if the bp is already used in one of the orders then update locally also
      updateLocally = !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true) || (!isNew && OB.MobileApp.model.orderList && _.filter(OB.MobileApp.model.orderList.models, function (order) {
        return order.get('bp').get('locId') === customerAddr.get('id');
      }).length > 0);

      if (updateLocally) {
        //save that the customer address is being processed by server
        customerAddr.set('loaded', OB.I18N.normalizeDate(new Date()));
        OB.Dal.save(customerAddr, function () {
          OB.DATA.updateDefaultCustomerLocations(customerAddr);
          me.receipt.trigger('change:bp', me.receipt);
          if (isNew) {
            customerAddr.set('posTerminal', OB.MobileApp.model.get('terminal').id);
            bpLocToSave.set('json', JSON.stringify(customerAddr.serializeToJSON()));
            bpLocToSave.set('c_bpartner_location_id', customerAddr.get('id'));
          }
          bpLocToSave.set('isbeingprocessed', 'Y');
          OB.Dal.save(bpLocToSave, function () {
            bpLocToSave.set('json', customerAddr.serializeToJSON());
            var successCallback, errorCallback, List;
            successCallback = function () {
              if (callback) {
                callback();
              }
              // update each order also so that new name is shown
              if (OB.MobileApp.model.orderList) {
                _.forEach(OB.MobileApp.model.orderList.models, function (order) {
                  if (order.get('bp').get('locId') === customerAddr.get('id')) {
                    var bp = order.get('bp');
                    bp.set('locId', customerAddr.get('id'));
                    bp.set('locName', customerAddr.get('name'));
                    bp.set('postalCode', customerAddr.get('postalCode'));
                    bp.set('cityName', customerAddr.get('cityName'));
                    bp.set('countryName', customerAddr.get('countryName'));
                    bp.set('locationModel', customerAddr);
                    order.save();
                    // refresh the display
                    if (OB.MobileApp.model.orderList.modelorder && OB.MobileApp.model.orderList.modelorder.get('id') === order.get('id')) {
                      OB.MobileApp.model.orderList.modelorder.setBPandBPLoc(bp, false, true);
                    }
                  }
                });
              }
              OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerAddrSaved', [customerAddr.get('_identifier')]));
            };
            customerAddrListToChange = new OB.Collection.ChangedBPlocationList();
            customerAddrListToChange.add(bpLocToSave);
            if (!OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
              OB.MobileApp.model.runSyncProcess(successCallback);
            }
          }, function () {
            //error saving BP changes with changes in changedbusinesspartners
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerAddrChn', [customerAddr.get('_identifier')]));
          });
        }, function () {
          //error saving BP Location with new values in c_bpartner_location
          OB.error(arguments);
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerAddrLocally', [customerAddr.get('_identifier')]));
        });
      }
    };
  };
}());