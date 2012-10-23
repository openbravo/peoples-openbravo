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
  kind: 'OB.UI.subwindow',
  name: 'OB.OBPOSPointOfSale.customers.UI.editcustomer',
  beforeSetShowing: function(params) {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null && OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null && OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {

      this.waterfall('onSetCustomer', {
        customer: params.businessPartner
      });

      return true;
    } else {
      $('#modalConfigurationRequiredForCreateNewCustomers').modal("show");
      return false;
    }
  },
  header: {
    kind: 'OB.UI.subwindowheader',
    headermessage: 'Customer details',
    onTapCloseButton: function() {
      var subWindow = this.subWindow;
      subWindow.doChangeSubWindow({
        newWindow: {
          name: subWindow.caller
        }
      });
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.customers.UI.editcustomers_impl'
  }
});


/**/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.customers.UI.assigncustomertoticket',
  style: 'width: 150px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblAssignToTicket'),
  handlers: {
    onSetCustomer: 'setCustomer'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  setCustomer: function(sender, event) {
    this.customer = event.customer;
  },
  tap: function() {
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
  init: function(model) {
    this.model = model;
  }
});

/*header of window body*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.customers.UI.EditCustomerWindowHeader',
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
          setCustomer: function(sender, event) {
            this.customer = event.customer;
          },
          tap: function() {
            this.model.get('subWindowManager').set('currentWindow', {
              name: 'customerCreateAndEdit',
              params: {
                businessPartner: this.customer,
                caller: 'customerView'
              }
            });
          },
          init: function(model) {
            this.model = model;
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.customers.UI.assigncustomertoticket'
        }]
      }]
    }]
  }],
  searchAction: function() {
    this.doSearchAction({
      bpName: this.$.filterText.getValue()
    });
  }
});


enyo.kind({
  kind: 'OB.OBPOSPointOfSale.customers.UI.edit_createcustomers',
  name: 'OB.OBPOSPointOfSale.customers.UI.editcustomers_impl',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.customers.UI.EditCustomerWindowHeader',
  newAttributes: [{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerName',
    modelProperty: 'name',
    label: OB.I18N.getLabel('OBPOS_LblName'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocName',
    modelProperty: 'locName',
    label: OB.I18N.getLabel('OBPOS_LblAddress'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPostalCode',
    modelProperty: 'postalcode',
    label: OB.I18N.getLabel('OBPOS_LblPostalCode'),
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerCity',
    modelProperty: 'city',
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