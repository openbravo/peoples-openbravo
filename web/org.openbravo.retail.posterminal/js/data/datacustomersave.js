/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function () {

  OB.DATA = window.OB.DATA || {};

  OB.DATA.CustomerSave = function (model) {
    this.context = model;
    this.customer = model.get('customer');


    this.customer.on('customerSaved', function () {
      var me = this,
          customersList, customerId = this.customer.get('id'),
          isNew = false,
          bpToSave = new OB.Model.ChangedBusinessPartners(),
          bpLocation, bpLocToSave = new OB.Model.BPLocation(),
          customersListToChange;

      bpToSave.set('isbeingprocessed', 'N');
      this.customer.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      bpToSave.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (customerId) {
        this.customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        bpToSave.set('json', JSON.stringify(this.customer.serializeToJSON()));
        bpToSave.set('c_bpartner_id', this.customer.get('id'));
      } else {
        isNew = true;
      }

      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) { //With high volume we only save localy when it is assigned to the order
        if (isNew) {
          me.customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
          var uuid = OB.UTIL.get_UUID();
          me.customer.set('id', uuid);
          me.customer.id = uuid;
          bpToSave.set('json', JSON.stringify(me.customer.serializeToJSON()));
          bpToSave.set('id', me.customer.get('id'));
        }

        // the location is sent to the server as part of the BP save, but when the BP is 
        // added directly to the ticket it also needs the location object in the BP, in the
        // locationModel
        // This can be done by creating the object as below or requesting it again from the server
        // but this takes another request, that's why it is created here as an object
        if (!bpToSave.get('locationModel')) {
          bpLocation = new OB.Model.BPLocation();
          bpLocation.set('id', me.customer.get('locId'));
          bpLocation.set('bpartner', me.customer.get('id'));
          bpLocation.set('name', me.customer.get('locName'));
          bpLocation.set('postalCode', me.customer.get('postalCode'));
          bpLocation.set('cityName', me.customer.get('cityName'));
          bpLocation.set('_identifier', me.customer.get('locName'));
          bpLocation.set('countryName', OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name);
          bpLocation.set('countryId', OB.MobileApp.model.get('terminal').defaultbp_bpcountry);
          bpToSave.set('locationModel', bpLocation);
        }

        bpToSave.set('isbeingprocessed', 'Y');
        OB.UTIL.HookManager.executeHooks('OBPOS_PostCustomerSave', {
          customer: me.customer,
          bpToSave: bpToSave
        }, function (args) {
          OB.Dal.save(bpToSave, function () {
            bpToSave.set('json', me.customer.serializeToJSON());
            var successCallback = function () {
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [me.customer.get('_identifier')]));
                // the location is sent to the server as part of the BP save, but when the BP is 
                // added directly to the ticket it also needs the location object in the BP, in the
                // locationModel
                // This can be done by creating the object as below or requesting it again from the server
                // but this takes another request, that's why it is created here as an object
                if (!bpToSave.get('locationModel')) {
                  bpLocation = new OB.Model.BPLocation();
                  bpLocation.set('id', me.customer.get('locId'));
                  bpLocation.set('bpartner', me.customer.get('id'));
                  bpLocation.set('name', me.customer.get('locName'));
                  bpLocation.set('postalCode', me.customer.get('postalCode'));
                  bpLocation.set('cityName', me.customer.get('cityName'));
                  bpLocation.set('_identifier', me.customer.get('locName'));
                  bpLocation.set('countryName', OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name);
                  bpLocation.set('countryId', OB.MobileApp.model.get('terminal').defaultbp_bpcountry);
                  bpToSave.set('locationModel', bpLocation);
                }
                };
            OB.MobileApp.model.runSyncProcess(successCallback);
          }, function () {
            //error saving BP changes with changes in changedbusinesspartners
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [me.customer.get('_identifier')]));
          }, isNew);
        });
      } else {
        //save that the customer is being processed by server
        OB.Dal.save(this.customer, function () {
          //OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSavedSuccessfullyLocally',[me.customer.get('_identifier')]));
          // Saving Customer Address locally
          if (!isNew) {
            //load the BPlocation and then update it
            OB.Dal.get(OB.Model.BPLocation, me.customer.get('locId'), function (bpLocToUpdate) {
              if (bpLocToUpdate) {
                bpLocToUpdate.set('name', me.customer.get('locName'));
                bpLocToUpdate.set('postalCode', me.customer.get('postalCode'));
                bpLocToUpdate.set('cityName', me.customer.get('cityName'));
                bpLocToUpdate.set('_identifier', me.customer.get('locName'));
                OB.Dal.save(bpLocToUpdate, function () {
                  //customer location updated successfully. Nothing to do here.
                }, function () {
                  OB.error(arguments);
                }, isNew);
              } else {
                OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_errorSavingBPLoc_header'), OB.I18N.getLabel('OBPOS_errorSavingBPLoc_body'));
              }
            }, function () {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_errorSavingBPLoc_header'), OB.I18N.getLabel('OBPOS_errorSavingBPLoc_body'));
            });
          } else {
            //create bploc from scratch
            bpLocToSave.set('id', me.customer.get('locId'));
            bpLocToSave.set('bpartner', me.customer.get('id'));
            bpLocToSave.set('name', me.customer.get('locName'));
            bpLocToSave.set('postalCode', me.customer.get('postalCode'));
            bpLocToSave.set('cityName', me.customer.get('cityName'));
            bpLocToSave.set('_identifier', me.customer.get('locName'));
            bpLocToSave.set('countryName', OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name);
            bpLocToSave.set('countryId', OB.MobileApp.model.get('terminal').defaultbp_bpcountry);
            OB.Dal.save(bpLocToSave, function () {
              //customer location created successfully. Nothing to do here.
            }, function () {
              OB.error(arguments);
            }, isNew);
          }

          if (isNew) {
            me.customer.set('posTerminal', OB.MobileApp.model.get('terminal').id);
            bpToSave.set('json', JSON.stringify(me.customer.serializeToJSON()));
            bpToSave.set('c_bpartner_id', me.customer.get('id'));
          }
          bpToSave.set('isbeingprocessed', 'Y');
          OB.UTIL.HookManager.executeHooks('OBPOS_PostCustomerSave', {
            customer: me.customer,
            bpToSave: bpToSave
          }, function (args) {
            OB.Dal.save(bpToSave, function () {
              bpToSave.set('json', me.customer.serializeToJSON());
              var successCallback, errorCallback, List;
              successCallback = function () {
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_customerSaved', [me.customer.get('_identifier')]));
              };
              OB.MobileApp.model.runSyncProcess(successCallback);
            }, function () {
              //error saving BP changes with changes in changedbusinesspartners
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerChanges', [me.customer.get('_identifier')]));
            });
          });
        }, function () {
          //error saving BP with new values in c_bpartner
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorSavingCustomerLocally', [me.customer.get('_identifier')]));
        });
      }

    }, this);
  };
}());