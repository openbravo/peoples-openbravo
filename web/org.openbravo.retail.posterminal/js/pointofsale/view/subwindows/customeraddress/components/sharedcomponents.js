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

/*global OB, enyo, _ */

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
      if (this.modelProperty === 'countryName') {
        this.setValue(OB.MobileApp.model.get('terminal').defaultbp_bpcountry_name);
      }
    }
    if (this.modelProperty === 'customerName' && inEvent.customer !== undefined && inEvent.customer.get('name') !== undefined) {
      this.setValue(inEvent.customer.get('name'));
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
    onSaveCustomerAddr: 'preSaveCustomerAddr'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [{
    name: 'bodyheader'
  }, {
    name: 'customerAddrAttributes',
    style: 'overflow-x:hidden; overflow-y:auto; max-height:622px;'
  }],
  setCustomerAddr: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.customerAddr = inEvent.customerAddr;
    this.waterfall('onLoadValue', {
      customer: this.customer,
      customerAddr: this.customerAddr
    });
  },
  preSaveCustomerAddr: function (inSender, inEvent) {
    var me = this,
        inSenderOriginal = inSender,
        inEventOriginal = inEvent;
    OB.UTIL.HookManager.executeHooks('OBPOS_PreCustomerAddrSave', {
      inSender: inSenderOriginal,
      inEvent: inEventOriginal,
      passValidation: true,
      error: '',
      meObject: me
    }, function (args) {
      if (args.passValidation) {
        args.meObject.saveCustomerAddr(args.inSender, args.inEvent);
      } else {
        OB.UTIL.showWarning(args.error);
      }
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
      if (this.model.get('customerAddr').get('name') === '') {
    	OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NameReqForBPAddress'));
        return false;
      } else {
        var callback = function () {
            goToViewWindow(sw, {
              customer: OB.UTIL.clone(me.customer),
              customerAddr: OB.UTIL.clone(me.model.get('customerAddr'))
            });
        };
        this.model.get('customerAddr').saveCustomerAddr(callback);
      }
    } else {
      this.model.get('customerAddr').loadById(this.customerAddr.get('id'), function (customerAddr) {

          function continueSaving() {
        	  customerAddr.saveCustomerAddr(function () {
                goToViewWindow(sw, {
                  customer: me.customer,
                  customerAddr: customerAddr
                });
                if (customerAddr.get('id') === me.customer.get("locId") || customerAddr.get('id') === me.customer.get("locShipId")) {
                  if (!customerAddr.get('isBillTo')) {
                    me.customer.set('locId', null);
                    me.customer.set('locName', null);
                  }
                  if (!customerAddr.get('isShipTo')) {
                    me.customer.set('locShipId', null);
                    me.customer.set('locShipName', null);
                    me.customer.set('postalCode', null);
                    me.customer.set('cityName', null);
                  }
                  me.customer.set('locationModel', customerAddr);
                  OB.Dal.save(me.customer, function success(tx) {
                    me.doChangeBusinessPartner({
                      businessPartner: me.customer
                    });
                  }, function error(tx) {
                    OB.error(tx);
                  });
                }
        	  }); 	  
          }
          
          getCustomerAddrValues({
            customerAddr: customerAddr
          });
          
          if (OB.MobileApp.model.receipt.get('lines').length > 0 && OB.MobileApp.model.receipt.get('bp').get('locShipId') === customerAddr.get('id') && !customerAddr.get('isShipTo')) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_UncheckShipToText'), [{
              label: OB.I18N.getLabel('OBPOS_LblOk'),
              isConfirmButton: true,
              action: function () {
                continueSaving();
              }
            }, {
              label: OB.I18N.getLabel('OBMOBC_LblCancel')
            }], {
              autoDismiss: false,
              onHideFunction: function () {
                return;
              }
            });
          } else {
              continueSaving();
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

enyo.kind({
  name: 'OB.UI.CustomerAddrCheckProperty',
  kind: 'OB.UI.CheckboxButton',
  style: 'margin: 5px 0px 0px 5px;',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
  },
  events: {
    onSaveProperty: ''
  },
  loadValue: function (inSender, inEvent) {
    var me = this;
    if (inEvent.customerAddr !== undefined) {
      if (inEvent.customerAddr.get(me.modelProperty) !== undefined) {
        me.checked = inEvent.customerAddr.get(me.modelProperty);
      }
      if (me.checked) {
        me.addClass('active');
      } else {
        me.removeClass('active');
      }
    } else {
      me.checked = true;
      me.addClass('active');
    }
  },
  saveChange: function (inSender, inEvent) {
    var me = this;
    inEvent.customerAddr.set(me.modelProperty, me.checked);
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