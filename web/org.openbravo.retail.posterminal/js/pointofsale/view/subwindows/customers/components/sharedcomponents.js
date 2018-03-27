/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
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
  i18nContent: 'OBMOBC_LblCancel',
  handlers: {
    onDisableButton: 'disableButton'
  },
  disableButton: function (inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
    if (inEvent.disabled) {
      this.addClass(this.classButtonDisabled);
    } else {
      this.removeClass(this.classButtonDisabled);
    }
  },
  tap: function () {
    this.bubble('onCancelClose');
  }
});

enyo.kind({
  name: 'OB.UI.CustomerPropertyLine',
  components: [{
    classes: 'customer-property-label',
    name: 'labelLine'
  }, {
    classes: 'customer-property-text',
    name: 'newAttribute'
  }, {
    style: 'clear: both'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.newAttribute.createComponent(this.newAttribute);
    this.$.labelLine.content = this.newAttribute.i18nLabel ? OB.I18N.getLabel(this.newAttribute.i18nLabel) : this.newAttribute.label;
    if (this.newAttribute.mandatory) {
      this.$.labelLine.content = this.$.labelLine.content + ' *';
    }
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
    onSaveChange: 'saveChange',
    onblur: 'blur',
    onchange: 'change',
    oninput: 'input',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue'
  },
  events: {
    onSaveProperty: '',
    onRetrieveCustomer: '',
    onSetValues: ''
  },
  valueSet: function (inSender, inEvent) {
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      this.setValue(inEvent.data[this.modelProperty]);
    }
  },
  retrieveValue: function (inSender, inEvent) {
    inEvent[this.modelProperty] = this.getValue();
  },
  blur: function () {},
  input: function () {},
  change: function () {},
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
    onSaveChange: 'saveChange',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue'
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
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.owner.owner.$.labelLine.hide();
    this.$.infotext.setContent(OB.I18N.getLabel('OBPOS_SameAddrInfo'));
  },
  valueSet: function (inSender, inEvent) {
    if (inEvent.data.hasOwnProperty('btnUseSameCheck')) {
      this.doHideShowFields({
        checked: inEvent.data.btnUseSameCheck
      });
      if (inEvent.data.btnUseSameCheck) {
        this.$.btnUseSameCheck.check();
      } else {
        this.$.btnUseSameCheck.unCheck();
      }
    }
  },
  retrieveValue: function (inSender, inEvent) {
    inEvent.btnUseSameCheck = this.$.btnUseSameCheck.checked;
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
    }
  },
  saveChange: function (inSender, inEvent) {
    inEvent.customer.set('useSameAddrForShipAndInv', this.$.btnUseSameCheck.checked);
  },
  tap: function () {
    this.doHideShowFields({
      checked: this.$.btnUseSameCheck.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.CustomerComboProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onHideShow: 'hideShow',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue',
    onchange: 'change'
  },
  events: {
    onSaveProperty: '',
    onSetValues: ''
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
  valueSet: function (inSender, inEvent) {
    var i;
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      for (i = 0; i < this.$.customerCombo.getCollection().length; i++) {
        if (this.$.customerCombo.getCollection().models[i].get('id') === inEvent.data[this.modelProperty]) {
          this.$.customerCombo.setSelected(i);
          break;
        }
      }
    }
  },
  retrieveValue: function (inSender, inEvent) {
    inEvent[this.modelProperty] = this.$.customerCombo.getValue();
  },
  loadValue: function (inSender, inEvent) {
    this.$.customerCombo.setCollection(this.collection);
    this.fetchDataFunction(inEvent);
  },
  change: function () {},
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
    if (selected) {
      inEvent.customer.set(this.modelProperty, selected.get(this.retrievedPropertyForValue));
      if (this.modelPropertyText) {
        inEvent.customer.set(this.modelPropertyText, selected.get(this.retrievedPropertyForText));
      }
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
    onSaveCustomer: 'preSaveCustomer',
    onHideShowFields: 'hideShowFields'
  },
  events: {},
  components: [{
    name: 'bodyheader'
  }, {
    name: 'customerAttributes',
    kind: 'Scroller',
    maxHeight: '500px',
    horizontal: 'hidden',
    components: [{
      name: 'customerOnlyFields'
    }, {
      name: 'shipAddress',
      components: [{
        name: 'shipLbl',
        showing: false,
        classes: 'twoAddrLayoutHeader'
      }, {
        style: 'clear:both',
        name: 'shippingAddrFields'
      }]
    }, {
      name: 'invAddress',
      components: [{
        name: 'invLbl',
        showing: false,
        classes: 'twoAddrLayoutHeader'
      }, {
        style: 'clear:both',
        name: 'invoicingAddrFields'
      }]
    }]
  }],
  hideShowFields: function (inSender, inEvent) {
    this.$.shipLbl.setShowing(!inEvent.checked);
    this.$.invLbl.setShowing(!inEvent.checked);
    this.$.shipAddress.addRemoveClass('twoAddrLayout', !inEvent.checked);
    this.$.invAddress.addRemoveClass('twoAddrLayout', !inEvent.checked);
    this.waterfall('onHideShow', {
      checked: inEvent.checked
    });
    return true;
  },
  setCustomer: function (customer) {
    this.customer = customer;
    this.waterfall('onLoadValue', {
      customer: this.customer
    });
  },
  preSaveCustomer: function (inSender, inEvent) {
    var me = this,
        inSenderOriginal = inSender,
        inEventOriginal = inEvent;

    //Validate anonymous customer edit allowed
    if (this.customer && OB.MobileApp.model.get('terminal').businessPartner === this.customer.id && OB.MobileApp.model.hasPermission('OBPOS_NotAllowEditAnonymousCustomer', true)) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotEditAnonymousCustomer'));
      return;
    }

    OB.UTIL.HookManager.executeHooks('OBPOS_PreCustomerSave', {
      inSender: inSenderOriginal,
      inEvent: inEventOriginal,
      passValidation: true,
      error: '',
      meObject: me,
      validations: inEvent.validations
    }, function (args) {
      if (args.cancellation) {
        return;
      }
      if (args.passValidation) {
        args.meObject.saveCustomer(args.inSender, args.inEvent);
      } else {
        OB.UTIL.showError(args.error);
        me.waterfall('onDisableButton', {
          disabled: false
        });
      }
    });
    return true;
  },
  saveCustomer: function (inSender, inEvent) {
    var me = this,
        customerEdited;

    function enableButtonsCallback(disable) {
      me.waterfall('onDisableButton', {
        disabled: disable
      });
    }

    function getCustomerValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer
      });
    }

    function checkMandatoryFields(items, customer) {
      var errors = '';
      _.each(items, function (item) {
        if (item.newAttribute.mandatory) {
          var value = customer.get(item.newAttribute.modelProperty);
          if (!value) {
            if (errors) {
              errors += ', ';
            }
            errors += OB.I18N.getLabel(item.newAttribute.i18nLabel);
          }
        }
      });
      return errors;
    }

    function validateForm(form) {
      if (inEvent.validations) {
        var customer = form.model.get('customer'),
            errors = checkMandatoryFields(form.$.customerOnlyFields.children, customer);
        if (form.$.invoicingAddrFields.showing) {
          var invoicingErrors = checkMandatoryFields(form.$.invoicingAddrFields.children, customer);
          if (invoicingErrors) {
            if (errors) {
              errors += ', ';
            }
            errors += (form.$.invoicingAddrFields.getClassAttribute().indexOf("twoAddrLayout") === 0 ? OB.I18N.getLabel('OBPOS_LblBillAddr') + ' [' + invoicingErrors + ']' : invoicingErrors);
          }
        }
        if (form.$.shippingAddrFields.showing && form.$.shippingAddrFields.getClassAttribute().indexOf("twoAddrLayout") === 0) {
          var shippingErrors = checkMandatoryFields(form.$.shippingAddrFields.children, customer);
          if (shippingErrors) {
            if (errors) {
              errors += ', ';
            }
            errors += OB.I18N.getLabel('OBPOS_LblShipAddr') + ' [' + shippingErrors + ']';
          }
        }
        if (errors) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerRequiredFields', [errors]));
          return false;
        }
        if (customer.get('firstName').length + customer.get('lastName').length >= 60) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_TooLongName'));
          return false;
        }
      }
      return true;
    }

    function beforeCustomerSave(customer, isNew) {
      customer.adjustNames();
      OB.UTIL.HookManager.executeHooks('OBPOS_BeforeCustomerSave', {
        customer: customer,
        isNew: isNew,
        validations: inEvent.validations,
        windowComponent: me
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          enableButtonsCallback(false);
          return true;
        }
        customerEdited = args.customer;
        args.customer.saveCustomer(function (result) {
          me.waterfall('onDisableButton', {
            disabled: false
          });
          if (result && !inEvent.silent) {
            me.bubble('onCancelClose', {
              customer: customerEdited
            });
          }
        });
      });
    }

    if (this.customer === undefined) {
      this.model.get('customer').newCustomer();
      this.waterfall('onSaveChange', {
        customer: this.model.get('customer')
      });
      if (validateForm(this)) {
        beforeCustomerSave(this.model.get('customer'), true);
      } else {
        enableButtonsCallback(false);
      }
    } else {
      this.model.get('customer').loadModel(this.customer, function (customer) {
        getCustomerValues({
          customer: customer
        });
        if (validateForm(me)) {
          beforeCustomerSave(customer, false);
        } else {
          enableButtonsCallback(false);
        }
      });
    }
  },
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    this.$.bodyheader.createComponent({
      kind: this.windowHeader
    });
    this.$.shipLbl.setContent(OB.I18N.getLabel('OBPOS_LblShipAddr'));
    this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
    this.attributeContainer = this.$.customerAttributes;
    enyo.forEach(this.newAttributes, function (natt) {
      this.$.customerOnlyFields.createComponent({
        kind: 'OB.UI.CustomerPropertyLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      }, {
        owner: me.attributeContainer
      });
    }, this);
    enyo.forEach(this.shipAddrAttributes, function (natt) {
      this.$.shippingAddrFields.createComponent({
        kind: 'OB.UI.CustomerPropertyLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      }, {
        owner: me.attributeContainer
      });
    }, this);
    enyo.forEach(this.invAddrAttributes, function (natt) {
      this.$.invoicingAddrFields.createComponent({
        kind: 'OB.UI.CustomerPropertyLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      }, {
        owner: me.attributeContainer
      });
    }, this);
  },
  init: function (model) {
    this.model = model;
  }
});