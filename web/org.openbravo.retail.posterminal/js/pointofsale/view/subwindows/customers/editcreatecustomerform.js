/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, $ */

enyo.kind({
  kind: 'OB.UI.Subwindow',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
  events: {
    onShowPopup: ''
  },
  beforeSetShowing: function (params) {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null && OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {

      this.waterfall('onSetCustomer', {
        customer: params.businessPartner
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
  defaultNavigateOnClose: 'customerView',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    name: 'OB.OBPOSPointOfSale.UI.customers.newcustomerheader',
    handlers: {
      onSetCustomer: 'setCustomer'
    },
    i18nHeaderMessage: 'OBPOS_TitleEditNewCustomer',
    setCustomer: function (inSender, inEvent) {
      this.customer = inEvent.customer;
    },
    onTapCloseButton: function () {
      var subWindow = this.subWindow;
      if (subWindow.caller === 'mainSubWindow') {
        subWindow.doChangeSubWindow({
          newWindow: {
            name: subWindow.caller
          }
        });
      } else {
        subWindow.doChangeSubWindow({
          newWindow: {
            name: subWindow.caller,
            params: {
              navigateOnClose: 'customerAdvancedSearch',
              businessPartner: (this.headerContainer && this.headerContainer.customer) ? this.headerContainer.customer : (this.customer ? this.customer : null)
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
    this.doSaveCustomer();
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
            var subWindow = this.subWindow;
            if (subWindow.caller === 'mainSubWindow') {
              subWindow.doChangeSubWindow({
                newWindow: {
                  name: subWindow.caller
                }
              });
            } else {
              subWindow.doChangeSubWindow({
                newWindow: {
                  name: subWindow.caller,
                  params: {
                    navigateOnClose: 'customerAdvancedSearch',
                    businessPartner: this.customer
                  }
                }
              });
            }
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
    name: 'customerName',
    modelProperty: 'name',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblName',
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
    name: 'customerLocName',
    modelProperty: 'locName',
    i18nLabel: 'OBPOS_LblAddress',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPostalCode',
    modelProperty: 'postalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    maxlength: 10
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60
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
  }]
});