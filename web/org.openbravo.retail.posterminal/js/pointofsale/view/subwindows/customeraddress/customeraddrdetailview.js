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

/*global enyo, $*/

enyo.kind({
  kind: 'OB.UI.Subwindow',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.editcustomeraddr',
  events: {
    onShowPopup: ''
  },
  beforeSetShowing: function (params) {
    this.waterfall('onSetCustomerAddr', {
      customer: params.businessPartner,
      customerAddr: params.bPLocation
    });

    return true;
  },
  defaultNavigateOnClose: 'customerAddressSearch',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    i18nHeaderMessage: 'OBPOS_TitleViewCustomerAddress',
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
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.editcustomers_impl'
  }
});


/**/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.assigncustomeraddrtoticket',
  style: 'margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  handlers: {
    onSetCustomerAddr: 'setCustomerAddr'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  setCustomerAddr: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.customerAddr = inEvent.customerAddr;
  },
  tap: function () {
    var me = this;
    me.customer.set('locId', me.customerAddr.get('id'));
    me.customer.set('locName', me.customerAddr.get('name'));
    me.doChangeBusinessPartner({
      businessPartner: me.customer
    });
    var sw = me.subWindow;
    sw.doChangeSubWindow({
      newWindow: {
        name: 'mainSubWindow'
      }
    });
  },
  init: function (model) {
    this.model = model;
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_LblAssignAddress'));
  }
});

/*header of window body*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.EditCustomerWindowHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    components: [{
      style: 'display: table; margin: 0 auto;',
      components: [{
        components: [{
          kind: 'OB.UI.Button',
          handlers: {
            onSetCustomerAddr: 'setCustomerAddr'
          },
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-orange btnlink btnlink-small',
          setCustomerAddr: function (inSender, inEvent) {
            this.customer = inEvent.customer;
            this.customerAddr = inEvent.customerAddr;
            if (!OB.UTIL.isWritableOrganization(this.customer.get('organization')) || !OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers')) {
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
                name: 'customerAddrCreateAndEdit',
                params: {
                  businessPartner: this.customer,
                  bPLocation: this.customerAddr,
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
          kind: 'OB.OBPOSPointOfSale.UI.customeraddr.assigncustomeraddrtoticket'
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
  kind: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers',
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.editcustomers_impl',
  style: 'padding: 9px 15px;',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customeraddr.EditCustomerWindowHeader',
  newAttributes: [{
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrName',
    modelProperty: 'name',
    i18nLabel: 'OBPOS_LblAddress',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrPostalCode',
    modelProperty: 'postalCode',
    i18nLabel: 'OBPOS_LblPostalCode',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    readOnly: true
  }]
});