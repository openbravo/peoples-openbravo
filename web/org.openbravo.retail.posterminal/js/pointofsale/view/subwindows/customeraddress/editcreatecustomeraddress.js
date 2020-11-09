/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddr',
  classes: 'obObposPointOfSaleUiCustomeraddrNewcustomeraddr',
  events: {
    onShowPopup: '',
    onDisableButton: ''
  },
  handlers: {
    onSetValues: 'setValues',
    onSaveCustomerAddr: 'saveCustomerAddr',
    onRetrieveCustomer: 'retrieveCustomers',
    onCancelClose: 'cancelClose'
  },
  setValues: function(inSender, inEvent) {
    this.waterfall('onSetValue', inEvent);
  },
  retrieveCustomers: function(inSender, inEvent) {
    var retrievedValues = inEvent || {};
    this.waterfall('onRetrieveValues', retrievedValues);
    return retrievedValues;
  },
  cancelClose: function(inSender, inEvent) {
    this.customerAddr = inEvent.customerAddr;
    this.hide();
    return true;
  },
  executeOnShow: function() {
    if (
      OB.MobileApp.model.get('terminal').defaultbp_paymentmethod !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bpcategory !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_paymentterm !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_invoiceterm !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bpcountry !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bporg !== null
    ) {
      this.$.body.$.edit_createcustomers_impl.setCustomerAddr(
        this.args.businessPartner,
        this.args.bPLocation
      );
      this.customerAddr = null;
      if (this.args.bPLocation) {
        this.$.header.setContent(
          OB.I18N.getLabel('OBPOS_TitleEditCustomerAddress')
        );
      } else {
        this.$.header.setContent(
          OB.I18N.getLabel('OBPOS_TitleNewCustomerAddress')
        );
      }
      this.waterfall('onDisableButton', {
        disabled: false
      });
      //show
      return true;
    } else {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCreateNewCustomers'
      });
      //not show
      return false;
    }
  },
  executeOnHide: function() {
    _.each(
      this.$.body.$.edit_createcustomers_impl.$.customerAddrAttributes.$,
      function(attribute) {
        if (attribute.hasClass('obUiFormElement_error')) {
          attribute.removeClass('obUiFormElement_error');
          attribute.setMessage();
        }
      }
    );
    var navigationPath =
      this.customerAddr || !this.args.cancelNavigationPath
        ? this.args.navigationPath
        : this.args.cancelNavigationPath;
    this.doShowPopup({
      popup: navigationPath[navigationPath.length - 1],
      args: {
        businessPartner: this.args.businessPartner,
        bPLocation: this.customerAddr
          ? this.customerAddr
          : this.args.bPLocation,
        target: this.args.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(
          navigationPath
        ),
        cancelNavigationPath: navigationPath,
        makeSearch: this.customerAddr !== undefined
      }
    });
  },
  saveCustomerAddr: function(inSender, inEvent) {
    this.waterfall('onExecuteSaveCustomerAddr', inEvent);
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl',
    classes:
      'obObposPointOfSaleUiCustomeraddrNewcustomeraddr-obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl'
  }
  //'footer' implemented programatically from the body
});

//button of header of the body
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddrsave',
  classes: 'obObposPointOfSaleUiCustomeraddrNewcustomeraddrsave',
  i18nLabel: 'OBPOS_LblSave',
  events: {
    onSaveCustomerAddr: ''
  },
  handlers: {
    onDisableButton: 'disableButton'
  },
  disableButton: function(inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
  },
  tap: function() {
    var me = this;
    this.disableButton(this, {
      disabled: true
    });
    OB.info(
      'Time: ' +
        new Date() +
        '. Customer Addr Save Button Pressed ( Status: ' +
        this.disabled +
        ') '
    );
    if (me.blocked) {
      OB.error(
        'Time: ' +
          new Date() +
          '. Customer Addr Save button has been pressed 2 times and second execution is discarded '
      );
      return;
    } else {
      me.blocked = true;
      setTimeout(function() {
        me.blocked = false;
      }, 500);
    }
    this.doSaveCustomerAddr({ validations: true });
  }
});

//Header of body
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.subwindowNewCustomer_footer',
  classes: 'obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter',
  components: [
    {
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter-container1',
      components: [
        {
          classes:
            'obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter-container1-container1',
          components: [
            {
              kind: 'OB.OBPOSPointOfSale.UI.customeraddr.cancelEdit',
              name: 'addressEditCancel',
              classes:
                'obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter-container1-container1-addressEditCancel',
              handlers: {
                onSetCustomerAddr: 'setCustomerAddr'
              },
              setCustomerAddr: function(inSender, inEvent) {
                this.customer = inEvent.customer;
                this.customerAddr = inEvent.customerAddr;
              },
              tap: function() {
                this.bubble('onCancelClose');
              }
            }
          ]
        },
        {
          classes:
            'obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter-container1-container2',
          components: [
            {
              kind: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddrsave',
              classes:
                'obObposPointOfSaleUiCustomeraddrSubwindowNewCustomerFooter-container1-container2-obObposPointOfSaleUiCustomeraddrNewcustomeraddrsave',
              isDefaultAction: true
            }
          ]
        }
      ]
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl',
  kind: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers',
  classes: 'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl',
  windowFooter:
    'OB.OBPOSPointOfSale.UI.customeraddr.subwindowNewCustomer_footer',
  newAttributes: [
    {
      kind: 'OB.UI.CustomerAddrTextProperty',
      name: 'customerAddrCustomerName',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrCustomerName',
      modelProperty: 'customerName',
      i18nLabel: 'OBPOS_LblCustomer',
      readOnly: true
    },
    {
      kind: 'OB.UI.CustomerAddrComboProperty',
      name: 'customerAddrCountry',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrCountry',
      modelProperty: 'countryId',
      modelPropertyText: 'countryName',
      collectionName: 'CountryList',
      i18nLabel: 'OBPOS_LblCountry',
      defaultValue: function() {
        return OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'id',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: '_identifier',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: async function(args) {
        var me = this;
        try {
          const dataCountry = await OB.App.MasterdataModels.Country.orderedBy(
            '_identifier'
          );
          me.dataReadyFunction(dataCountry, args);
        } catch (err) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingCountries'));
          me.dataReadyFunction(null, args);
        }
      }
    },
    {
      kind: 'OB.UI.CustomerAddrTextProperty',
      name: 'customerAddrPostalCode',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrPostalCode',
      modelProperty: 'postalCode',
      i18nLabel: 'OBPOS_LblPostalCode',
      maxlength: 10
    },
    {
      kind: 'OB.UI.CustomerAddrTextProperty',
      name: 'customerAddrCity',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrCity',
      modelProperty: 'cityName',
      i18nLabel: 'OBPOS_LblCity',
      maxlength: 60
    },
    {
      kind: 'OB.UI.CustomerAddrTextProperty',
      name: 'customerAddrName',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrName',
      modelProperty: 'name',
      i18nLabel: 'OBPOS_LblAddress',
      maxlength: 60,
      mandatory: true
    },
    {
      kind: 'OB.UI.CustomerAddrCheckProperty',
      name: 'customerAddrShip',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrShip',
      modelProperty: 'isShipTo',
      i18nLabel: 'OBPOS_LblShipAddr'
    },
    {
      kind: 'OB.UI.CustomerAddrCheckProperty',
      name: 'customerAddrBill',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomersImpl-newAttributes-customerAddrBill',
      modelProperty: 'isBillTo',
      i18nLabel: 'OBPOS_LblBillAddr'
    }
  ]
});
