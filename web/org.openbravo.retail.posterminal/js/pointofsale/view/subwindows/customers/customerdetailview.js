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
  name: 'OB.OBPOSPointOfSale.UI.customers.editcustomer',
  events: {
    onShowPopup: ''
  },
  beforeSetShowing: function (params) {
    this.params = params;
    this.waterfall('onSetCustomer', {
      customer: params.businessPartner
    });

    // Hide components depending on its displayLogic function
    _.each(this.$.subWindowBody.$.editcustomers_impl.$.customerAttributes.$, function (attribute) {
      _.each(attribute.$.newAttribute.$, function (attrObject) {
        if (attrObject.displayLogic && !attrObject.displayLogic()) {
          this.hide();
        }
      }, attribute);
    });

    return true;
  },
  defaultNavigateOnClose: 'mainSubWindow',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    i18nHeaderMessage: 'OBPOS_TitleViewCustomer',
    onTapCloseButton: function () {
      var subWindow = this.subWindow,
          params = this.owner.owner.owner.params;
      if (_.isUndefined(params)) { //For the flow when press the ESC Key. The 'this' changed because is relative. Dependent if press 'X' button or ESC key for closed the Popup.
        params = this.owner.owner.params;
      }
      if (params.navigateType === 'modal') {
        subWindow.doChangeSubWindow({
          newWindow: {
            name: 'mainSubWindow'
          }
        });
        this.owner.owner.owner.doShowPopup({
          popup: params.navigateOnClose,
          args: {
            target: params.target
          }
        });
      } else {
        subWindow.doChangeSubWindow({
          newWindow: {
            name: subWindow.navigateOnClose,
            params: {
              navigateOnClose: 'mainSubWindow'
            }
          }
        });
      }
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customers.editcustomers_impl'
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customers.assigncustomertoticket',
  style: 'margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  handlers: {
    onSetCustomer: 'setCustomer',
    onSetBPartnerTarget: 'setBPartnerTarget'
  },
  events: {
    onHideThisPopup: '',
    onShowPopup: '',
    onChangeBusinessPartner: ''
  },
  setCustomer: function (inSender, inEvent) {
    this.customer = inEvent.customer;
  },
  setBPartnerTarget: function (inSender, inEvent) {
    this.target = inEvent.target;
  },
  tap: function () {
    var sw = this.subWindow;
    sw.doChangeSubWindow({
      newWindow: {
        name: 'mainSubWindow'
      }
    });
    this.doChangeBusinessPartner({
      businessPartner: this.customer,
      target: this.target
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

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.customers.editnewaddress',
  style: 'margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  handlers: {
    onSetCustomer: 'setCustomer'
  },
  events: {
    onHideThisPopup: ''
  },
  setCustomer: function (inSender, inEvent) {
    this.customer = inEvent.customer;
  },
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerAddressSearch',
      params: {
        caller: 'mainSubWindow',
        bPartner: this.customer.get('id'),
        showShipAndInv: true
      }
    });
  },
  init: function (model) {
    this.model = model;
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_TitleEditNewAddress'));
  }
});

/*header of window body*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.EditCustomerWindowHeader',
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
            onSetCustomer: 'setCustomer'
          },
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-orange btnlink btnlink-small',
          setCustomer: function (inSender, inEvent) {
            this.customer = inEvent.customer;
            if (!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomerButton', true)) {
              this.disabled = true;
              this.setAttribute("disabled", "disabled");
            } else {
              this.disabled = false;
              this.setAttribute("disabled", null);
            }
          },
          tap: function () {
            if (this.disabled === false) {
              var sw = this.subWindow,
                  params = this.owner.owner.owner.owner.owner.params;
              this.model.get('subWindowManager').set('currentWindow', {
                name: 'customerCreateAndEdit',
                params: {
                  businessPartner: this.customer,
                  navigateOnClose: sw.getName(),
                  navigateOnCloseParent: params.navigateOnClose,
                  navigateType: params.navigateType,
                  target: params.target
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
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.customers.editnewaddress'
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
    modelProperty: 'firstName',
    i18nLabel: 'OBPOS_LblName',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'lastName',
    modelProperty: 'lastName',
    i18nLabel: 'OBPOS_LblLastName',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerBpCat',
    modelProperty: 'businessPartnerCategory_name',
    i18nLabel: 'OBPOS_BPCategory',
    readOnly: true,
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showcategoryselector;
    }
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerTaxId',
    modelProperty: 'taxID',
    i18nLabel: 'OBPOS_LblTaxId',
    readOnly: true,
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showtaxid;
    }
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
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'birthPlace',
    modelProperty: 'birthPlace',
    i18nLabel: 'OBPOS_LblBirthplace',
    readOnly: true,
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('OBPOS_ShowBusinessPartnerBirthInfo', true);
    }
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'birthDay',
    modelProperty: 'birthDay',
    i18nLabel: 'OBPOS_LblBirthday',
    readOnly: true,
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('OBPOS_ShowBusinessPartnerBirthInfo', true);
    }
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerPriceList',
    modelProperty: 'priceList',
    i18nLabel: 'OBPOS_PriceList',
    readOnly: true,
    loadValue: function (inSender, inEvent) {
      if (inEvent.customer !== undefined) {
        if (inEvent.customer.get(this.modelProperty) !== undefined) {
          var me = this;
          OB.UTIL.getPriceListName(inEvent.customer.get(this.modelProperty), function (name) {
            me.setValue(name);
          });
        }
      } else {
        this.setValue('');
      }
    },
    displayLogic: function () {
      return OB.MobileApp.model.hasPermission('EnableMultiPriceList', true);
    }
  }]
});