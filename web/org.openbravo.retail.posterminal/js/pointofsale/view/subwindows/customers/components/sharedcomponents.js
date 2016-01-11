/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, $, console */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.ModalConfigurationRequiredForCreateCustomers',
  kind: 'OB.UI.ModalInfo',
  i18nHeader: 'OBPOS_configurationRequired',
  bodyContent: {
    i18nContent: 'OBPOS_configurationNeededToCreateCustomers'
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.cancelEdit',
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
  name: 'OB.UI.CustomerTextProperty',
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
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(this.modelProperty) !== undefined) {
        this.setValue(inEvent.customer.get(this.modelProperty));
      }
    } else {
      this.setValue('');
    }
  },
  saveChange: function (inSender, inEvent) {
    inEvent.customer.set(this.modelProperty, this.getValue());
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
  name: 'OB.UI.CustomerTextPropertyAddr',
  kind: 'OB.UI.CustomerTextProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onHideShow: 'hideShow'
  },
  loadValue: function (inSender, inEvent) {
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(this.modelProperty) !== undefined) {
        if (_.isNull(inEvent.customer.get(this.modelProperty))) {
          this.setValue(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
          this.setAttribute('readonly', 'readonly');
        } else {
          this.setValue(inEvent.customer.get(this.modelProperty));
          if (_.isUndefined(this.readOnly)) {
            this.setAttribute('readonly', '');
          }
        }
      }
    } else {
      this.setValue('');
      this.setAttribute('readonly', '');
    }
  }
});

enyo.kind({
  name: 'OB.UI.SwitchShippingInvoicingAddr',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
  },
  events: {
    onHideShowFields: ''
  },
  components: [{
    name: 'useSameCheck',
    style: 'text-align: right; width: 20%; float: left; padding: 0px 5px 1px 0px',
    components: [{
      name: 'btnUseSameCheck',
      kind: 'OB.UI.CheckboxButton',
      style: 'background-position: 0px 5px; height: 38px; margin-left: 5px; '
    }]
  }, {
    name: 'info',
    style: 'width: 75%; float: left; ',
    components: [{
      name: 'infotext',
      style: 'display: table-cell; vertical-align: middle; height: 30px; padding: 4px; '
    }]
  }, {
    style: 'clear:both'
  }, {
    name: 'shipLbl',
    classes: 'twoAddrLayoutHeader'
  }, {
    name: 'invLbl',
    classes: 'twoAddrLayoutHeader'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.owner.owner.$.labelLine.hide();
    this.$.infotext.setContent(OB.I18N.getLabel('OBPOS_SameAddrInfo'));
    this.$.shipLbl.setContent(OB.I18N.getLabel('OBPOS_LblShipAddr'));
    this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
  },
  rendered: function () {
    this.inherited(arguments);
    this.owner.applyStyle('width', '100%');
  },
  loadValue: function (inSender, inEvent) {
    if (inEvent.customer !== undefined) {
      this.hide();
    } else {
      this.show();
      this.$.btnUseSameCheck.checked = true;
      this.$.btnUseSameCheck.addClass('active');
      this.doHideShowFields({
        checked: this.$.btnUseSameCheck.checked
      });
      this.$.shipLbl.hide();
      this.$.invLbl.hide();
    }
  },
  saveChange: function (inSender, inEvent) {
    inEvent.customer.set('useSameAddrForShipAndInv', this.$.btnUseSameCheck.checked);
  },
  tap: function () {
    this.doHideShowFields({
      checked: this.$.btnUseSameCheck.checked
    });
    this.$.shipLbl.setShowing(!this.$.btnUseSameCheck.checked);
    this.$.invLbl.setShowing(!this.$.btnUseSameCheck.checked);
  }
});

enyo.kind({
  name: 'OB.UI.CustomerComboProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onHideShow: 'hideShow'
  },
  events: {
    onSaveProperty: ''
  },
  components: [{
    kind: 'OB.UI.List',
    name: 'customerCombo',
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
    this.$.customerCombo.setCollection(this.collection);
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
      if (inEvent.customer) {
        //Edit: select actual value
        if (categ.get(this.retrievedPropertyForValue) === inEvent.customer.get(this.modelProperty)) {
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
      this.$.customerCombo.setSelected(index);
    } else {
      this.$.customerCombo.setSelected(0);
    }
  },
  saveChange: function (inSender, inEvent) {
    var selected = this.collection.at(this.$.customerCombo.getSelected());
    inEvent.customer.set(this.modelProperty, selected.get(this.retrievedPropertyForValue));
    if (this.modelPropertyText) {
      inEvent.customer.set(this.modelPropertyText, selected.get(this.retrievedPropertyForText));
    }
  },
  initComponents: function () {
    if (this.collectionName && OB && OB.Collection && OB.Collection[this.collectionName]) {
      this.collection = new OB.Collection[this.collectionName]();
    } else {
      OB.info('OB.UI.CustomerComboProperty: Collection is required');
    }
    this.inherited(arguments);
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  handlers: {
    onSetCustomer: 'setCustomer',
    onSaveCustomer: 'preSaveCustomer',
    onHideShowFields: 'hideShowFields'
  },
  events: {},
  components: [{
    name: 'bodyheader'
  }, {
    name: 'customerAttributes',
    style: 'overflow-x: hidden; overflow-y: auto; max-height: 580px;',
    components: [{
      name: 'customerOnlyFields'
    }, {
      name: 'shippingAddrFields'
    }, {
      name: 'invoicingAddrFields'
    }]
  }],
  hideShowFields: function (inSender, inEvent) {
    this.$.shippingAddrFields.addRemoveClass('twoAddrLayout', !inEvent.checked);
    this.$.invoicingAddrFields.addRemoveClass('twoAddrLayout', !inEvent.checked);
    this.waterfall('onHideShow', {
      checked: inEvent.checked
    });
  },
  setCustomer: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.waterfall('onLoadValue', {
      customer: this.customer
    });
  },
  preSaveCustomer: function (inSender, inEvent) {
    var me = this,
        inSenderOriginal = inSender,
        inEventOriginal = inEvent;
    OB.UTIL.HookManager.executeHooks('OBPOS_PreCustomerSave', {
      inSender: inSenderOriginal,
      inEvent: inEventOriginal,
      passValidation: true,
      error: '',
      meObject: me
    }, function (args) {
      if (args.passValidation) {
        args.meObject.saveCustomer(args.inSender, args.inEvent);
      } else {
        OB.UTIL.showError(args.error);
      }
    });
  },
  saveCustomer: function (inSender, inEvent) {
    var me = this,
        sw = me.subWindow;

    function getCustomerValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer
      });
    }

    function goToViewWindow(sw, params) {
      if (sw.caller === 'mainSubWindow') {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'customerView',
            params: {
              navigateOnClose: 'mainSubWindow',
              businessPartner: params.customer
            }
          }
        });
      } else {
        sw.doChangeSubWindow({
          newWindow: {
            name: 'customerView',
            params: {
              navigateOnClose: 'customerAdvancedSearch',
              businessPartner: params.customer
            }
          }
        });
      }
    }

    if (this.customer === undefined) {
      this.model.get('customer').newCustomer();
      this.waterfall('onSaveChange', {
        customer: this.model.get('customer')
      });
      this.model.get('customer').adjustNames();
      OB.UTIL.HookManager.executeHooks('OBPOS_BeforeCustomerSave', {
        customer: this.model.get('customer'),
        isNew: true
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          return true;
        }
        args.customer.saveCustomer(function () {
          goToViewWindow(sw, {
            customer: OB.UTIL.clone(args.customer)
          });
        });
      });
    } else {
      var that = this;
      this.model.get('customer').loadById(this.customer.get('id'), function (customer) {
        getCustomerValues({
          customer: customer
        });
        customer.adjustNames();
        OB.UTIL.HookManager.executeHooks('OBPOS_BeforeCustomerSave', {
          customer: customer,
          isNew: false
        }, function (args) {
          if (args && args.cancellation && args.cancellation === true) {
            return true;
          }
          args.customer.saveCustomer(function () {
            if (!inEvent.silent) {
              goToViewWindow(sw, {
                customer: args.customer
              });
            }
          });
        });
      });
    }
  },
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    this.$.bodyheader.createComponent({
      kind: this.windowHeader
    });
    this.attributeContainer = this.$.customerAttributes;
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
        this.$.customerOnlyFields.createComponent({
          kind: 'OB.UI.CustomerPropertyLine',
          name: 'line_' + natt.name,
          newAttribute: natt
        }, {
          owner: me.attributeContainer
        });
      }
    }, this);
    enyo.forEach(this.shipAddrAttributes, function (natt) {
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
        this.$.shippingAddrFields.createComponent({
          kind: 'OB.UI.CustomerPropertyLine',
          name: 'line_' + natt.name,
          newAttribute: natt
        }, {
          owner: me.attributeContainer
        });
      }
    }, this);
    enyo.forEach(this.invAddrAttributes, function (natt) {
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
        this.$.invoicingAddrFields.createComponent({
          kind: 'OB.UI.CustomerPropertyLine',
          name: 'line_' + natt.name,
          newAttribute: natt
        }, {
          owner: me.attributeContainer
        });
      }
    }, this);
  },
  init: function (model) {
    this.model = model;
  }
});