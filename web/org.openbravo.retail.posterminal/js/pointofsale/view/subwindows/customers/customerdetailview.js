/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $*/

enyo.kind({
  kind: 'OB.UI.Subwindow',
  name: 'OB.OBPOSPointOfSale.UI.customers.editcustomer',
  events: {
    onShowPopup: ''
  },
  beforeSetShowing: function (params) {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null && OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {

      this.waterfall('onSetCustomer', {
        customer: params.businessPartner
      });

      return true;
    } else {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCreateNewCustomers'
      });
      return false;
    }
  },
  defaultNavigateOnClose: 'customerAdvancedSearch',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    headermessage: OB.I18N.getLabel('OBPOS_TitleViewCustomer'),
    onTapCloseButton: function () {
      var subWindow = this.subWindow;
      subWindow.doChangeSubWindow({
        newWindow: {
          name: subWindow.navigateOnClose,
          params: {
            navigateOnClose: 'mainSubWindow'
          }
        }
      });
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customers.editcustomers_impl'
  }
});


/**/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customers.assigncustomertoticket',
  style: 'width: 150px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblAssignToTicket'),
  handlers: {
    onSetCustomer: 'setCustomer'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  setCustomer: function (inSender, inEvent) {
    this.customer = inEvent.customer;
  },
  tap: function () {
    var sw = this.subWindow;
    sw.doChangeSubWindow({
      newWindow: {
        name: 'mainSubWindow'
      }
    });
    this.doChangeBusinessPartner({
      businessPartner: this.customer
    });
  },
  init: function (model) {
    this.model = model;
  }
});

/*header of window body*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.EditCustomerWindowHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    style: 'padding: 10px 500px 10px 500px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          handlers: {
            onSetCustomer: 'setCustomer'
          },
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-orange btnlink btnlink-small',
          content: OB.I18N.getLabel('OBPOS_LblEdit'),
          setCustomer: function (inSender, inEvent) {
            this.customer = inEvent.customer;
            if (!OB.UTIL.isWritableOrganization(this.customer.get('bporg'))) {
              this.disabled = true;
              this.setAttribute("disabled", "disabled");
            } else {
              this.disabled = false;
              this.setAttribute("disabled", null);
            }
          },
          tap: function () {
            if (this.disabled === false) {
              var sw = this.subWindow;
              this.model.get('subWindowManager').set('currentWindow', {
                name: 'customerCreateAndEdit',
                params: {
                  businessPartner: this.customer,
                  navigateOnClose: sw.getName()
                }
              });
            }
          },
          init: function (model) {
            this.model = model;
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customers.assigncustomertoticket'
        }]
      }]
    }]
  }],
  searchAction: function () {
    this.doSearchAction({
      bpName: this.$.filterText.getValue()
    });
  }
});


enyo.kind({
  kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  name: 'OB.OBPOSPointOfSale.UI.customers.editcustomers_impl',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customers.EditCustomerWindowHeader',
  newAttributes: [{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerName',
    modelProperty: 'name',
    label: OB.I18N.getLabel('OBPOS_LblName'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerTaxId',
    modelProperty: 'taxID',
    label: OB.I18N.getLabel('OBPOS_LblTaxId'),
    readOnly: true,
    displayLogic: OB.POS.modelterminal.get('terminal').bp_showtaxid
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocName',
    modelProperty: 'locName',
    label: OB.I18N.getLabel('OBPOS_LblAddress'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPostalCode',
    modelProperty: 'postalCode',
    label: OB.I18N.getLabel('OBPOS_LblPostalCode'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerCity',
    modelProperty: 'cityName',
    label: OB.I18N.getLabel('OBPOS_LblCity'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPhone',
    modelProperty: 'phone',
    label: OB.I18N.getLabel('OBPOS_LblPhone'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerEmail',
    modelProperty: 'email',
    label: OB.I18N.getLabel('OBPOS_LblEmail'),
    readOnly: true
  }]
});