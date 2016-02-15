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
      if (!_.isUndefined(me.customerAddr)) {
        params.customerAddr.set('onlyOneAddress', me.customerAddr.get('onlyOneAddress'));
      }
      if (sw.params.navigateType === 'modal' && !sw.params.navigateOnCloseParent) {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'mainSubWindow'
          }
        });
        sw.doShowPopup({
          popup: sw.params.navigateOnClose,
          args: {
            target: sw.params.target
          }
        });
      } else {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'customerAddressView',
            params: {
              businessPartner: params.customer,
              bPLocation: params.customerAddr,
              navigateOnClose: sw.params.navigateType === 'modal' ? sw.params.navigateOnCloseParent : 'mainSubWindow',
              navigateType: sw.params.navigateType,
              target: sw.params.target
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
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NameReqForBPAddress'));
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
            if (customerAddr.get('id') === me.customer.get("locId") || customerAddr.get('id') === me.customer.get("shipLocId")) {
              if (customerAddr.get('id') === me.customer.get('locId')) {
                me.customer.set('locName', customerAddr.get('name'));
                if (!customerAddr.get('isBillTo')) {
                  me.customer.set('locId', null);
                  me.customer.set('locName', null);
                  me.customer.set('postalCode', null);
                  me.customer.set('cityName', null);
                  me.customer.set('countryName', null);
                } else {
                  me.customer.set('locId', customerAddr.get('id'));
                  me.customer.set('locName', customerAddr.get('name'));
                  me.customer.set('postalCode', customerAddr.get('postalCode'));
                  me.customer.set('cityName', customerAddr.get('cityName'));
                  me.customer.set('countryName', customerAddr.get('countryName'));
                }
              }
              if (customerAddr.get('id') === me.customer.get('shipLocId')) {
                if (!customerAddr.get('isShipTo')) {
                  me.customer.set('shipLocId', null);
                  me.customer.set('shipLocName', null);
                  me.customer.set('shipPostalCode', null);
                  me.customer.set('shipCityName', null);
                  me.customer.set('shipCountryName', null);
                } else {
                  me.customer.set('shipLocId', customerAddr.get('id'));
                  me.customer.set('shipLocName', customerAddr.get('name'));
                  me.customer.set('shipPostalCode', customerAddr.get('postalCode'));
                  me.customer.set('shipCityName', customerAddr.get('cityName'));
                  me.customer.set('shipCountryName', customerAddr.get('countryName'));
                }
              }
              me.customer.set('locationModel', customerAddr);
              OB.Dal.save(me.customer, function success(tx) {
                me.doChangeBusinessPartner({
                  businessPartner: me.customer,
                  target: 'order'
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

        if (OB.MobileApp.model.receipt.get('lines').length > 0 && OB.MobileApp.model.receipt.get('bp').get('shipLocId') === customerAddr.get('id') && !customerAddr.get('isShipTo')) {
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

enyo.kind({
  name: 'OB.UI.CustomerAddrComboProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
  },
  events: {
    onSaveProperty: ''
  },
  components: [{
    kind: 'OB.UI.List',
    name: 'customerAddrCombo',
    classes: 'combo',
    style: 'width: 101%; margin:0;',
    renderLine: enyo.kind({
      kind: 'enyo.Option',
      initComponents: function () {
        this.inherited(arguments);
        this.setValue(this.model.get(this.parent.parent.retrievedPropertyForValue));
        this.setContent(this.model.get(this.parent.parent.retrievedPropertyForText));
      }
    }),
    renderEmpty: 'enyo.Control'
  }],
  loadValue: function (inSender, inEvent) {
    this.$.customerAddrCombo.setCollection(this.collection);
    this.fetchDataFunction(inEvent);
  },
  dataReadyFunction: function (data, inEvent) {
    var index = 0,
        result = null;
    if (this.destroyed) {
      return;
    }
    if (data) {
      this.collection.reset(data.models);
    } else {
      this.collection.reset(null);
      return;
    }

    result = _.find(this.collection.models, function (categ) {
      if (inEvent.customerAddr) {
        //Edit: select actual value
        if (categ.get(this.retrievedPropertyForValue) === inEvent.customerAddr.get(this.modelProperty)) {
          return true;
        }
      } else {
        //New: select default value
        if (categ.get(this.retrievedPropertyForValue) === this.defaultValue()) {
          return true;
        }
      }
      index += 1;
    }, this);
    if (result) {
      this.$.customerAddrCombo.setSelected(index);
    } else {
      this.$.customerAddrCombo.setSelected(0);
    }
  },
  saveChange: function (inSender, inEvent) {
    var selected = this.collection.at(this.$.customerAddrCombo.getSelected());
    inEvent.customerAddr.set(this.modelProperty, selected.get(this.retrievedPropertyForValue));
    if (this.modelPropertyText) {
      inEvent.customerAddr.set(this.modelPropertyText, selected.get(this.retrievedPropertyForText));
    }
  },
  initComponents: function () {
    if (this.collectionName && OB && OB.Collection && OB.Collection[this.collectionName]) {
      this.collection = new OB.Collection[this.collectionName]();
    } else {
      OB.info('OB.UI.CustomerAddrComboProperty: Collection is required');
    }
    this.inherited(arguments);
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});