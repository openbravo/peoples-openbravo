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
    this.waterfall('onSetCustomer', {
      customer: params.businessPartner
    });

    return true;
  },
  defaultNavigateOnClose: 'customerAdvancedSearch',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    i18nHeaderMessage: 'OBPOS_TitleViewCustomer',
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
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_LblAssignToTicket'));
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
          setCustomer: function (inSender, inEvent) {
            this.customer = inEvent.customer;
            if (!OB.UTIL.isWritableOrganization(this.customer.get('organization'))) {
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
          },
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblEdit'));
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
    i18nLabel: 'OBPOS_LblName',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerBpCat',
    modelProperty: 'businessPartnerCategory_name',
    i18nLabel: 'OBPOS_BPCategory',
    readOnly: true
    //displayLogic: OB.POS.modelterminal.get('terminal').bp_showcategoryselector
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerTaxId',
    modelProperty: 'taxID',
    i18nLabel: 'OBPOS_LblTaxId',
    readOnly: true
    //  displayLogic: OB.POS.modelterminal.get('terminal').bp_showtaxid
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocName',
    modelProperty: 'locName',
    i18nLabel: 'OBPOS_LblAddress',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPostalCode',
    modelProperty: 'postalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPhone',
    modelProperty: 'phone',
    i18nLabel: 'OBPOS_LblPhone',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerEmail',
    modelProperty: 'email',
    i18nLabel: 'OBPOS_LblEmail',
    readOnly: true
  }]
});