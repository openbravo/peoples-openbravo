/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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
    this.waterfall('onAddressChanged', {
      address: params.bPLocation
    });
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
    onSetCustomerAddr: 'setCustomerAddr',
    onAddressChanged: 'addressChanged'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  setCustomerAddr: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.customerAddr = inEvent.customerAddr;
    this.model.attributes.customerAddr.set('loaded', OB.I18N.formatDateISO(new Date()));
  },
  tap: function () {
    var me = this;
    if (me.customerAddr.get('isBillTo') && me.customerAddr.get('isShipTo')) {
      me.customer.set('locId', me.customerAddr.get('id'));
      me.customer.set('locName', me.customerAddr.get('name'));
      me.customer.set('locShipId', me.customerAddr.get('id'));
      me.customer.set('locShipName', me.customerAddr.get('name'));
      me.customer.set('postalCode', me.customerAddr.get('postalCode'));
      me.customer.set('cityName', me.customerAddr.get('cityName'));
      me.customer.set('locationModel', me.customerAddr);
    } else if (me.customerAddr.get('isBillTo')) {
      me.customer.set('locId', me.customerAddr.get('id'));
      me.customer.set('locName', me.customerAddr.get('name'));
      if (me.customer.get('locId') === me.customer.get('locShipId')) {
        me.customer.set('locShipId', null);
        me.customer.set('locShipName', null);
      }
    } else if (me.customerAddr.get('isShipTo')) {
      me.customer.set('locShipId', me.customerAddr.get('id'));
      me.customer.set('locShipName', me.customerAddr.get('name'));
      me.customer.set('postalCode', me.customerAddr.get('postalCode'));
      me.customer.set('cityName', me.customerAddr.get('cityName'));
      if (me.customer.get('locShipId') === me.customer.get('locId')) {
        me.customer.set('locId', null);
        me.customer.set('locName', null);
      }
      me.customer.set('locationModel', me.customerAddr);
    }

    me.customer.set('countryName', me.customerAddr.get('countryName'));
    me.model.get('order').trigger('change:bp', me.model.get('order'));
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
    this.inherited(arguments);
    var me = this;
    this.model = model;
    this.model.get('customerAddr').on('customerAddrSaved', function () {
      me.waterfall('onAddressChanged', {
        address: this
      });
    });
  },
  renderAssignToticket: function (newLabel) {
    this.setContent(newLabel);
  },
  addressChanged: function (inSender, inEvent) {
    var customerAddr = inEvent.address;
    if (customerAddr.get('isBillTo') && !customerAddr.get('isShipTo')) {
      this.renderAssignToticket(OB.I18N.getLabel('OBPOS_LblAssignBillAddress'));
    } else if (customerAddr.get('isShipTo') && !customerAddr.get('isBillTo')) {
      this.renderAssignToticket(OB.I18N.getLabel('OBPOS_LblAssignShipAddress'));
    } else {
      this.renderAssignToticket(OB.I18N.getLabel('OBPOS_LblAssignAddress'));
    }
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
            if (!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers')) {
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
    name: 'customerAddrCustomerName',
    modelProperty: 'customerName',
    i18nLabel: 'OBPOS_LblCustomer',
    readOnly: true
  }, {
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
  }, {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrCountry',
    modelProperty: 'countryName',
    i18nLabel: 'OBPOS_LblCountry',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerAddrCheckProperty',
    name: 'customerAddrShip',
    modelProperty: 'isShipTo',
    i18nLabel: 'OBPOS_LblShipAddr',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerAddrCheckProperty',
    name: 'customerAddrBill',
    modelProperty: 'isBillTo',
    i18nLabel: 'OBPOS_LblBillAddr',
    readOnly: true
  }]
});