/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.sw_editcustomers',
  style: 'background-color: #FFFFFF;',
  showing: false,
  beforeSetShowing: function(params) {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null &&
        OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null &&
        OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {
      
      this.waterfall('onSetCustomer', {
        customer: params.businessPartner,
        callerWindow: params.callerWindow
    });
      
      return true;
    }else{
      $('#modalConfigurationRequiredForCreateNewCustomers').modal("show");
      return false
    }
  },
  components: [{
    name: 'OB.UI.EditCustomerWindowImpl',
    kind: 'OB.UI.NewCustomerWindow',
    windowHeader: 'OB.UI.EditCustomerWindowHeader',
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
  }]
});