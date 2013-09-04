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
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.ModalConfigurationRequiredForCreateCustomers',
  kind: 'OB.UI.ModalInfo',
  i18nHeader: 'OBPOS_configurationRequired',
  bodyContent: {
    i18nContent: 'OBPOS_configurationNeededToCreateCustomers'
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.cancelEdit',
  kind: 'OB.UI.Button',
  style: 'width: 100px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink-gray btnlink btnlink-small',
  i18nContent: 'OBMOBC_LblCancel'
});

enyo.kind({
  name: 'OB.UI.CustomerPropertyLine',
  components: [{
    name: 'labelLine',
    style: 'font-size: 15px; color: black; text-align: right; border: 1px solid #FFFFFF; background-color: #E2E2E2; width: 20%; height: 28px; padding: 12px 5px 1px 0; float: left;'
  }, {
    style: 'border: 1px solid #FFFFFF; float: left; width: 75%;',
    name: 'newAttribute'
  }, {
    style: 'clear: both'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.newAttribute.createComponent(this.newAttribute);
    this.$.labelLine.content = this.newAttribute.i18nLabel ? OB.I18N.getLabel(this.newAttribute.i18nLabel) : this.newAttribute.label;
  }
});

enyo.kind({
  name: 'OB.UI.CustomerAddrTextProperty',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input',
  style: 'width: 100%; height: 30px; margin:0;',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
  },
  events: {
    onSaveProperty: ''
  },
  loadValue: function (inSender, inEvent) {
    if (inEvent.customerAddr !== undefined) {
      if (inEvent.customerAddr.get(this.modelProperty) !== undefined) {
        this.setValue(inEvent.customerAddr.get(this.modelProperty));
      }
    } else {
      this.setValue('');
    }
  },
  saveChange: function (inSender, inEvent) {
    inEvent.customerAddr.set(this.modelProperty, this.getValue());
  },
  initComponents: function () {
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
    if (this.maxlength) {
      this.setAttribute('maxlength', this.maxlength);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers',
  handlers: {
    onSetCustomerAddr: 'setCustomerAddr',
    onSaveCustomerAddr: 'saveCustomerAddr'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [{
    name: 'bodyheader'
  }, {
    name: 'customerAddrAttributes'
  }],
  setCustomerAddr: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.customerAddr = inEvent.customerAddr;
    this.waterfall('onLoadValue', {
      customer: this.customer,
      customerAddr: this.customerAddr
    });
  },
  saveCustomerAddr: function (inSender, inEvent) {
    var me = this,
        sw = me.subWindow;

    function getCustomerAddrValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer,
        customerAddr: params.customerAddr
      });
    }

    function goToViewWindow(sw, params) {
      if (sw.caller === 'mainSubWindow') {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'customerAddressView',
            params: {
              navigateOnClose: 'mainSubWindow',
              businessPartner: params.customer,
              bPLocation: params.customerAddr
            }
          }
        });
      } else {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'customerAddressView',
            params: {
              navigateOnClose: 'customerAddressSearch',
              businessPartner: params.customer,
              bPLocation: params.customerAddr
            }
          }
        });
      }
    }

    if (this.customerAddr === undefined) {
      this.model.get('customerAddr').newCustomerAddr();
      this.model.get('customerAddr').set('bpartner', this.customer.get('id'));
      this.waterfall('onSaveChange', {
        customerAddr: this.model.get('customerAddr')
      });
      if (this.model.get('customerAddr').saveCustomerAddr()) {
        goToViewWindow(sw, {
          customer: this.customer,
          customerAddr: this.model.get('customerAddr')
        });
      }
    } else {
      this.model.get('customerAddr').loadById(this.customerAddr.get('id'), function (customerAddr) {
        getCustomerAddrValues({
          customerAddr: customerAddr
        });
        if (customerAddr.saveCustomerAddr()) {
          goToViewWindow(sw, {
            customer: me.customer,
            customerAddr: customerAddr
          });
          if (customerAddr.get('id') === me.customer.get("locId")) {

            me.customer.set('locId', customerAddr.get('id'));
            me.customer.set('locName', customerAddr.get('name'));
            OB.Dal.save(me.customer, function success(tx) {
              me.doChangeBusinessPartner({
                businessPartner: me.customer
              });
            }, function error(tx) {
              window.console.error(tx);
            });
          }
        }
      });
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyheader.createComponent({
      kind: this.windowHeader
    });
    this.attributeContainer = this.$.customerAddrAttributes;
    enyo.forEach(this.newAttributes, function (natt) {
      var resultDisplay = true,
          undf;
      if (natt.displayLogic !== undf && natt.displayLogic !== null) {
        if (enyo.isFunction(natt.displayLogic)) {
          resultDisplay = natt.displayLogic(this);
        } else {
          resultDisplay = natt.displayLogic;
        }
      }
      if (resultDisplay) {
        this.$.customerAddrAttributes.createComponent({
          kind: 'OB.UI.CustomerPropertyLine',
          name: 'line_' + natt.name,
          newAttribute: natt
        });
      }
    }, this);
  },
  init: function (model) {
    this.model = model;
  }
});