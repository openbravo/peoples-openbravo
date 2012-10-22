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
  name: 'OB.OBPOSPointOfSale.UI.sw_newcustomers',
  style: 'background-color: #FFFFFF;',
  showing: false,
  beforeSetShowing: function(params) {

    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {

      this.callerWindow = params.callerWindow;
      
      this.waterfall('onSetCustomer', {
        customer: params.businessPartner,
        callerWindow: params.callerWindow
      });
      //show
      return true;
      } else {
        $('#modalConfigurationRequiredForCreateNewCustomers').modal("show");
        //not show
        return false;
      }
    }, components: [{
      tag: 'div',
      style: 'padding: 9px 15px;',
      components: [{
        tag: 'a',
        classes: 'close',
        components: [{
          tag: 'span',
          style: 'font-size: 150%',
          allowHtml: true,
          content: '&times;'
        }],
        tap: function() {
          this.model.get('subWindowManager').set('currentWindow', {
            name: this.parent.parent.callerWindow,
            params: []
          });
        },
        init: function(model) {
          this.model = model;
        }
      }, {
        tag: 'h3',
        name: 'divheaderCustomerEditNew',
        content: OB.I18N.getLabel('OBPOS_TitleEditNewCustomer')
      }]
    }, {
      name: 'OB.UI.NewCustomerWindowImpl',
      kind: 'OB.UI.NewCustomerWindow',
      windowHeader: 'OB.UI.NewCustomerWindowHeader',
      newAttributes: [{
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerName',
        modelProperty: 'name',
        label: OB.I18N.getLabel('OBPOS_LblName')
      }, {
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerLocName',
        modelProperty: 'locName',
        label: OB.I18N.getLabel('OBPOS_LblAddress')
      }, {
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerPostalCode',
        modelProperty: 'postalcode',
        label: OB.I18N.getLabel('OBPOS_LblPostalCode')
      }, {
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerCity',
        modelProperty: 'city',
        label: OB.I18N.getLabel('OBPOS_LblCity')
      }, {
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerPhone',
        modelProperty: 'phone',
        label: OB.I18N.getLabel('OBPOS_LblPhone')
      }, {
        kind: 'OB.UI.CustomerTextProperty',
        name: 'customerEmail',
        modelProperty: 'email',
        label: OB.I18N.getLabel('OBPOS_LblEmail')
      }]
    }]
  });