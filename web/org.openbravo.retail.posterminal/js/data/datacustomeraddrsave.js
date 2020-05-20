/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB*/

(function() {
  OB.DATA = window.OB.DATA || {};

  OB.DATA.CustomerAddrSave = function(model) {
    this.context = model;
    this.customerAddr = model.get('customerAddr');

    // trigger is from previous code, keeping it for backward compat
    this.customerAddr.on(
      'customerAddrSaved',
      function() {
        OB.DATA.executeCustomerAddressSave(this.customerAddr);
      },
      this
    );

    OB.DATA.updateDefaultCustomerLocations = function(customerAddr) {
      var i, foundAddress, locations, addToLocations;
      addToLocations = function(bp) {
        foundAddress = false;
        locations = bp.get('locations');
        if (locations && locations.length > 0) {
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
      // Check with Default Customer
      if (
        OB.MobileApp.model.get('businessPartner') &&
        customerAddr.get('bpartner') ===
          OB.MobileApp.model.get('businessPartner').get('id')
      ) {
        addToLocations(OB.MobileApp.model.get('businessPartner'));
      }
    };

    OB.DATA.executeCustomerAddressSave = async function(
      customerAddr,
      callback,
      callbackError
    ) {
      let customerAddrId = customerAddr.get('id'),
        isNew = false,
        me = this;
      customerAddr.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (customerAddrId) {
        customerAddr.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        var now = new Date();
        customerAddr.set('timezoneOffset', now.getTimezoneOffset());
        if (OB.UTIL.isNullOrUndefined(customerAddr.get('loaded'))) {
          customerAddr.set('loaded', OB.I18N.normalizeDate(new Date()));
        } else {
          customerAddr.set(
            'loaded',
            OB.I18N.normalizeDate(new Date(customerAddr.get('loaded')))
          );
        }
      } else {
        isNew = true;
      }

      // if the bp is already used in one of the orders then update locally also
      let updateLocally =
        !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true) ||
        (!isNew &&
          OB.MobileApp.model.orderList &&
          _.filter(OB.MobileApp.model.orderList.models, function(order) {
            return order.get('bp').get('locId') === customerAddr.get('id');
          }).length > 0);

      //save that the customer address is being processed by server
      if (updateLocally) {
        customerAddr.set('loaded', OB.I18N.normalizeDate(new Date()));
      }
      if (isNew) {
        customerAddr.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        var uuid = OB.UTIL.get_UUID();
        customerAddr.set('id', uuid);
        customerAddr.id = uuid;
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
          me.receipt.get('bp').set('moreaddress', true); // For to show two address buttons in receipt
        }
      }
      customerAddr.unset('creationDate');
      try {
        await OB.App.State.Global.synchronizeBusinessPartnerLocation(
          customerAddr.attributes
        );
        if (callback) {
          callback();
        }
        // update each order also so that new name is shown
        OB.DATA.updateDefaultCustomerLocations(customerAddr);
        OB.UTIL.showSuccess(
          OB.I18N.getLabel('OBPOS_customerAddrSaved', [
            customerAddr.get('_identifier')
          ])
        );
      } catch (error) {
        //error saving BP Location with new values in c_bpartner_location
        OB.error(arguments);
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_errorSavingCustomerAddrLocally', [
            customerAddr.get('_identifier')
          ])
        );
        callbackError();
      }
    };
  };
})();
