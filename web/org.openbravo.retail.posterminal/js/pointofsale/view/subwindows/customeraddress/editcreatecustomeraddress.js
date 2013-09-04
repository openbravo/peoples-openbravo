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

/*global enyo, _, $ */

enyo.kind({
  kind: 'OB.UI.Subwindow',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddr',
  events: {
    onShowPopup: ''
  },
  beforeSetShowing: function (params) {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null && OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {
      this.waterfall('onSetCustomerAddr', {
        customer: params.businessPartner,
        customerAddr: params.bPLocation,
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
  defaultNavigateOnClose: 'customerAddressView',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    name: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomerheader',
    handlers: {
      onSetCustomerAddr: 'setCustomerAddr'
    },
    i18nHeaderMessage: 'OBPOS_TitleEditNewCustomerAddress',
    setCustomerAddr: function (inSender, inEvent) {
      this.customer = inEvent.customer;
      this.customerAddr = inEvent.customerAddr;
    },
    onTapCloseButton: function () {
      var subWindow = this.subWindow;
      var customer, customerAddr;
      if (this.headerContainer) {
        customer = this.headerContainer.customer;
        customerAddr = this.headerContainer.customerAddr;
      } else {
        customer = this.customer;
        customerAddr = this.customerAddr;
      }
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
              navigateOnClose: 'customerAddressSearch',
              businessPartner: customer,
              bPLocation: customerAddr
            }
          }
        });
      }
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl'
  }
});

//button of header of the body
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddrsave',
  style: 'width: 100px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblSave',
  events: {
    onSaveCustomerAddr: ''
  },
  tap: function () {
    this.doSaveCustomerAddr();
  }
});

//Header of body
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.subwindowNewCustomer_bodyheader',
  components: [{
    components: [{
      style: 'display: table; margin: 0 auto;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddrsave'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customeraddr.cancelEdit',
          handlers: {
            onSetCustomerAddr: 'setCustomerAddr'
          },
          setCustomerAddr: function (inSender, inEvent) {
            this.customer = inEvent.customer;
            this.customerAddr = inEvent.customerAddr;
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
                    navigateOnClose: 'customerAddressSearch',
                    businessPartner: this.customer,
                    bPLocation: this.customerAddr
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
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl',
  kind: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customeraddr.subwindowNewCustomer_bodyheader',
  newAttributes: [{
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrName',
    modelProperty: 'name',
    i18nLabel: 'OBPOS_LblAddress',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrPostalCode',
    modelProperty: 'postalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    maxlength: 10
  }, {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60
  }]
});