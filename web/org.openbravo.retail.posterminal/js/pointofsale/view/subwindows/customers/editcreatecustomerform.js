/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  kind: 'OB.UI.Subwindow',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onCancelClose: 'cancelClose'
  },
  beforeSetShowing: function (params) {
    if (OB.MobileApp.model.get('terminal').defaultbp_paymentmethod !== null && OB.MobileApp.model.get('terminal').defaultbp_bpcategory !== null && OB.MobileApp.model.get('terminal').defaultbp_paymentterm !== null && OB.MobileApp.model.get('terminal').defaultbp_invoiceterm !== null && OB.MobileApp.model.get('terminal').defaultbp_bpcountry !== null && OB.MobileApp.model.get('terminal').defaultbp_bporg !== null) {
      this.params = params;
      this.waterfall('onSetCustomer', {
        customer: params.businessPartner
      });

      // Hide components depending on its displayLogic function
      _.each(this.$.subWindowBody.$.edit_createcustomers_impl.$.customerAttributes.$, function (attribute) {
        _.each(attribute.$.newAttribute.$, function (attrObject) {
          if (attrObject.displayLogic && !attrObject.displayLogic()) {
            this.hide();
          }
        }, attribute);
      });

      //hide address fields while editing customers
      if (params.businessPartner) {
        this.$.subWindowBody.$.edit_createcustomers_impl.$.invoicingAddrFields.hide();
        this.$.subWindowBody.$.edit_createcustomers_impl.$.shippingAddrFields.hide();
        this.$.subWindowHeader.$['OB.OBPOSPointOfSale.UI.customers.newcustomerheader'].$.headermessage.setContent(OB.I18N.getLabel('OBPOS_TitleEditCustomer'));
      } else {
        this.$.subWindowBody.$.edit_createcustomers_impl.$.invoicingAddrFields.show();
        this.$.subWindowBody.$.edit_createcustomers_impl.$.shippingAddrFields.show();
        this.$.subWindowHeader.$['OB.OBPOSPointOfSale.UI.customers.newcustomerheader'].$.headermessage.setContent(OB.I18N.getLabel('OBPOS_TitleNewCustomer'));
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
  defaultNavigateOnClose: 'customerView',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    name: 'OB.OBPOSPointOfSale.UI.customers.newcustomerheader',
    handlers: {
      onSetCustomer: 'setCustomer'
    },
    events: {
      onShowPopup: ''
    },
    setCustomer: function (inSender, inEvent) {
      this.customer = inEvent.customer;
    },
    onTapCloseButton: function () {
      this.bubble('onCancelClose');
    }
  },
  cancelClose: function () {
    if (this.params.navigateType === 'modal' && !this.params.navigateOnCloseParent) {
      this.doChangeSubWindow({
        newWindow: {
          name: 'mainSubWindow'
        }
      });
      this.doShowPopup({
        popup: this.params.navigateOnClose,
        args: {
          target: this.params.target
        }
      });
    } else {
      if (this.caller === 'mainSubWindow') {
        this.doChangeSubWindow({
          newWindow: {
            name: this.caller
          }
        });
      } else {
        this.doChangeSubWindow({
          newWindow: {
            name: this.caller,
            params: {
              navigateOnClose: this.params.navigateType === 'modal' ? this.params.navigateOnCloseParent : 'mainSubWindow',
              businessPartner: this.params.businessPartner,
              navigateType: this.params.navigateType,
              target: this.params.target
            }
          }
        });
      }
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
  classes: 'btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblSave',
  events: {
    onSaveCustomer: ''
  },
  tap: function () {
    this.doSaveCustomer({
      validations: true
    });
  }
});

//Header of body
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.subwindowNewCustomer_bodyheader',
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
          kind: 'OB.OBPOSPointOfSale.UI.customers.cancelEdit',
          handlers: {
            onSetCustomer: 'setCustomer'
          },
          setCustomer: function (inSender, inEvent) {
            this.customer = inEvent.customer;
          },
          tap: function () {
            this.bubble('onCancelClose');
          }
        }]
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl',
  kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customers.subwindowNewCustomer_bodyheader',
  newAttributes: [{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'firstName',
    modelProperty: 'firstName',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblName',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'lastName',
    modelProperty: 'lastName',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblLastName',
    maxlength: 60,
    mandatory: true
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
    i18nLabel: 'OBPOS_LblBirthday',
    handlers: {
      onLoadValue: 'loadValue',
      onSaveChange: 'saveChange'
    },
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('OBPOS_ShowBusinessPartnerBirthInfo', true);
    },
    loadValue: function (inSender, inEvent) {
      this.setLocale(OB.Application.language_string);
      if (inEvent.customer && inEvent.customer.get(this.modelProperty)) {
        this.setValue(new Date(inEvent.customer.get(this.modelProperty)));
      } else {
        this.setValue(null);
      }
    },
    saveChange: function (inSender, inEvent) {
      var value = this.getValue();
      var fragments;
      if (value) {
        fragments = [value.getFullYear(), value.getMonth() + 1, value.getDate()];
        if (fragments[1] < 10) {
          fragments[1] = '0' + fragments[1];
        }
        if (fragments[2] < 10) {
          fragments[2] = '0' + fragments[2];
        }
        inEvent.customer.set(this.modelProperty, fragments.join('-'));
      } else {
        inEvent.customer.set(this.modelProperty, null);
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