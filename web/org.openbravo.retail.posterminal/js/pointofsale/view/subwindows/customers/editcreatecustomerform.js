/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
  classes: 'receipt-customer-selector-editor',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onCancelClose: 'cancelClose',
    onSetValues: 'setValues',
    onRetrieveCustomer: 'retrieveCustomers'
  },
  cancelClose: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.hide();
    return true;
  },
  setValues: function (inSender, inEvent) {
    this.waterfall('onSetValue', inEvent);
  },
  retrieveCustomers: function (inSender, inEvent) {
    var retrievedValues = inEvent || {};
    this.waterfall('onRetrieveValues', retrievedValues);
    return retrievedValues;
  },
  executeOnShow: function () {
    if (OB.MobileApp.model.get('terminal').defaultbp_paymentmethod !== null && OB.MobileApp.model.get('terminal').defaultbp_bpcategory !== null && OB.MobileApp.model.get('terminal').defaultbp_paymentterm !== null && OB.MobileApp.model.get('terminal').defaultbp_invoiceterm !== null && OB.MobileApp.model.get('terminal').defaultbp_bpcountry !== null && OB.MobileApp.model.get('terminal').defaultbp_bporg !== null) {
      // Hide components depending on its displayLogic function
      _.each(this.$.body.$.edit_createcustomers_impl.$.customerAttributes.$, function (attribute) {
        if (attribute.name !== 'strategy') {
          _.each(attribute.$.newAttribute.$, function (attrObject) {
            if (attrObject.displayLogic && !attrObject.displayLogic()) {
              this.hide();
            }
          }, attribute);
        }
      });

      //hide address fields while editing customers
      if (this.args.businessPartner) {
        this.$.body.$.edit_createcustomers_impl.setCustomer(this.args.businessPartner);
        this.$.body.$.edit_createcustomers_impl.$.invoicingAddrFields.hide();
        this.$.body.$.edit_createcustomers_impl.$.shippingAddrFields.hide();
        this.$.header.setContent(OB.I18N.getLabel('OBPOS_TitleEditCustomer'));
      } else {
        this.$.body.$.edit_createcustomers_impl.setCustomer(undefined);
        this.$.body.$.edit_createcustomers_impl.$.invoicingAddrFields.show();
        this.$.body.$.edit_createcustomers_impl.$.shippingAddrFields.show();
        this.$.header.setContent(OB.I18N.getLabel('OBPOS_TitleNewCustomer'));
      }
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
  executeOnHide: function () {
    var navigationPath = this.customer || !this.args.cancelNavigationPath ? this.args.navigationPath : this.args.cancelNavigationPath;
    this.doShowPopup({
      popup: navigationPath[navigationPath.length - 1],
      args: {
        businessPartner: this.customer ? this.customer : this.args.businessPartner,
        target: this.args.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(navigationPath),
        makeSearch: this.customer !== undefined
      }
    });
  },
  showingChanged: function () {
    this.inherited(arguments);
    if (!this.showing) {
      this.$.body.$.edit_createcustomers_impl.$.invLbl.setShowing(false);
      this.$.body.$.edit_createcustomers_impl.$.shipLbl.setShowing(false);
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl'
  }
});

//button of header of the body
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomersave',
  style: 'width: 100px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblSave',
  events: {
    onSaveCustomer: ''
  },
  handlers: {
    onDisableButton: 'disableButton'
  },
  tap: function () {
    var me = this;
    this.disableButton(this, {
      disabled: true
    });
    OB.info('Time: ' + new Date() + '. Customer Save Button Pressed ( Status: ' + this.disabled + ') ');
    if (me.blocked) {
      OB.error('Time: ' + new Date() + '. Customer Save button has been pressed 2 times and second execution is discarded ');
      return;
    } else {
      me.blocked = true;
      setTimeout(function () {
        me.blocked = false;
      }, 500);
    }
    this.doSaveCustomer({
      validations: true
    });
  },
  disableButton: function (inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
    if (inEvent.disabled) {
      this.addClass(this.classButtonDisabled);
    } else {
      this.removeClass(this.classButtonDisabled);
    }
  }
});

//Header of body
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.NewCustomer_bodyheader',
  components: [{
    components: [{
      style: 'display: table; margin: 0 auto;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customers.newcustomersave'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customers.cancelEdit'
        }]
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl',
  kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customers.NewCustomer_bodyheader',
  newAttributes: [{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'firstName',
    modelProperty: 'firstName',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblName',
    maxlength: 60,
    mandatory: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'lastName',
    modelProperty: 'lastName',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblLastName',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerCategory',
    modelProperty: 'businessPartnerCategory',
    //Required: property where the selected value will be get and where the value will be saved
    modelPropertyText: 'businessPartnerCategory_name',
    //optional: When saving, the property which will store the selected text
    collectionName: 'BPCategoryList',
    defaultValue: function () {
      return OB.MobileApp.model.get('terminal').defaultbp_bpcategory;
    },
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this,
          criteria;
      criteria = {
        _orderByClause: '_identifier asc'
      };
      OB.Dal.find(OB.Model.BPCategory, criteria, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPCategories'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    i18nLabel: 'OBPOS_BPCategory',
    mandatory: true,
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showcategoryselector;
    }
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerTaxId',
    modelProperty: 'taxID',
    i18nLabel: 'OBPOS_LblTaxId',
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showtaxid;
    },
    maxlength: 20
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPhone',
    modelProperty: 'phone',
    i18nLabel: 'OBPOS_LblPhone',
    maxlength: 40
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerEmail',
    modelProperty: 'email',
    i18nLabel: 'OBPOS_LblEmail',
    maxlength: 255
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'birthPlace',
    modelProperty: 'birthPlace',
    i18nLabel: 'OBPOS_LblBirthplace',
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('OBPOS_ShowBusinessPartnerBirthInfo', true);
    }
  }, {
    kind: 'OB.UI.DatePicker',
    name: 'birthDay',
    modelProperty: 'birthDay',
    i18nLabel: 'OBPOS_LblBirthdate',
    handlers: {
      onLoadValue: 'loadValue',
      onSaveChange: 'saveChange',
      onSetValue: 'valueSet',
      onRetrieveValues: 'retrieveValue'
    },
    valueSet: function (inSender, inEvent) {
      if (inEvent.data.hasOwnProperty(this.modelProperty)) {
        this.setValue(inEvent.data[this.modelProperty]);
      }
    },
    retrieveValue: function (inSender, inEvent) {
      inEvent[this.modelProperty] = this.getValue();
    },
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('OBPOS_ShowBusinessPartnerBirthInfo', true);
    },
    loadValue: function (inSender, inEvent) {
      this.setLocale(OB.MobileApp.model.get('terminal').language_string);
      if (inEvent.customer && inEvent.customer.get(this.modelProperty)) {
        this.setValue(new Date(inEvent.customer.get(this.modelProperty)));
      } else {
        this.setValue('');
      }
    },
    saveChange: function (inSender, inEvent) {
      var value = this.getValue();
      if (value) {
        inEvent.customer.set(this.modelProperty, value);
      } else {
        inEvent.customer.set(this.modelProperty, '');
      }
    }
  }, {
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerPriceList',
    modelProperty: 'priceList',
    //Required: property where the selected value will be get and where the value will be saved
    modelPropertyText: 'priceList_name',
    //optional: When saving, the property which will store the selected text
    collectionName: 'PriceListList',
    defaultValue: function () {
      return OB.MobileApp.model.get('pricelist').id;
    },
    //Default value for new lines
    retrievedPropertyForValue: 'm_pricelist_id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: 'name',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this,
          criteria;
      criteria = {
        _orderByClause: 'name asc'
      };
      OB.Dal.find(OB.Model.PriceList, criteria, function (data, args) {
        //This function must be called when the data is ready
        data.add([{
          m_pricelist_id: OB.MobileApp.model.get('pricelist').id,
          name: OB.MobileApp.model.get('pricelist').name
        }], {
          at: 0
        });
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPPriceList'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    i18nLabel: 'OBPOS_PriceList',
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('EnableMultiPriceList', true);
    }
  }, {
    kind: 'OB.UI.SwitchShippingInvoicingAddr',
    name: 'useSameAddrCheck'
  }],
  shipAddrAttributes: [{
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerShipCountry',
    modelProperty: 'shipCountryId',
    modelPropertyText: 'shipCountryName',
    collectionName: 'CountryList',
    i18nLabel: 'OBPOS_LblCountry',
    defaultValue: function () {
      return OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
    },
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this,
          criteria;
      criteria = {
        _orderByClause: '_identifier asc'
      };
      OB.Dal.find(OB.Model.Country, criteria, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingCountries'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
        this.owner.owner.hide();
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
        this.owner.owner.show();
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerLocName',
    modelProperty: 'shipLocName',
    i18nLabel: 'OBPOS_LblAddress',
    hasAddrIcons: true,
    maxlength: 60,
    mandatory: true,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
        this.owner.owner.hide();
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
        this.owner.owner.show();
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerPostalCode',
    modelProperty: 'shipPostalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    maxlength: 10,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
        this.owner.owner.hide();
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
        this.owner.owner.show();
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerCity',
    modelProperty: 'shipCityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
        this.owner.owner.hide();
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
        this.owner.owner.show();
      }
    }
  }],
  invAddrAttributes: [{
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerCountry',
    modelProperty: 'countryId',
    modelPropertyText: 'countryName',
    collectionName: 'CountryList',
    i18nLabel: 'OBPOS_LblCountry',
    defaultValue: function () {
      return OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
    },
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this,
          criteria;
      criteria = {
        _orderByClause: '_identifier asc'
      };
      OB.Dal.find(OB.Model.Country, criteria, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingCountries'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    mandatory: true,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerInvLocName',
    modelProperty: 'locName',
    i18nLabel: 'OBPOS_LblAddress',
    maxlength: 60,
    mandatory: true,
    hasAddrIcons: true,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerInvPostalCode',
    modelProperty: 'postalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    maxlength: 10,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
      }
    }
  }, {
    kind: 'OB.UI.CustomerTextPropertyAddr',
    name: 'customerInvCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60,
    hideShow: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.owner.removeClass('width52');
        this.owner.owner.$.labelLine.removeClass('width40');
      } else {
        this.owner.addClass('width52');
        this.owner.owner.$.labelLine.addClass('width40');
      }
    }
  }]
});