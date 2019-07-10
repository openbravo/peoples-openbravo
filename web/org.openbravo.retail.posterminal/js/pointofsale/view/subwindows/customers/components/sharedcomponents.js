/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _*/

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.customers.ModalConfigurationRequiredForCreateCustomers',
  kind: 'OB.UI.ModalInfo',
  classes:
    'obObposPointOfSaleUiCustomersModalConfigurationRequiredForCreateCustomers',
  i18nHeader: 'OBPOS_configurationRequired',
  bodyContent: {
    i18nContent: 'OBPOS_configurationNeededToCreateCustomers',
    classes:
      'obObposPointOfSaleUiCustomersModalConfigurationRequiredForCreateCustomers-bodyContent'
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.cancelEdit',
  kind: 'OB.UI.Button',
  classes: 'obObposPointOfSaleUiCustomersCancelEdit',
  i18nContent: 'OBMOBC_LblCancel',
  handlers: {
    onDisableButton: 'disableButton'
  },
  disableButton: function(inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
    if (inEvent.disabled) {
      this.addClass(this.classButtonDisabled);
    } else {
      this.removeClass(this.classButtonDisabled);
    }
  },
  tap: function() {
    this.bubble('onCancelClose');
  }
});

enyo.kind({
  name: 'OB.UI.CustomerPropertyLine',
  kind: 'OB.UI.FormElement',
  classes:
    'obUiFormElement_dataEntry obUiFormElement_dataEntry_icon obUICustomerPropertyLine'
});

enyo.kind({
  name: 'OB.UI.CustomerTextProperty',
  kind: 'OB.UI.FormElement.Input',
  type: 'text',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
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
  valueSet: function(inSender, inEvent) {
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      this.setValue(inEvent.data[this.modelProperty]);
    }
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getValue();
  },
  input: function() {},
  change: function() {},
  loadValue: function(inSender, inEvent) {
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(this.modelProperty) !== undefined) {
        this.setValue(inEvent.customer.get(this.modelProperty));
      }
    } else {
      this.setValue('');
    }
  },
  saveChange: function(inSender, inEvent) {
    inEvent.customer.set(this.modelProperty, this.getValue());
  }
});

enyo.kind({
  name: 'OB.UI.CustomerTextPropertyAddr',
  kind: 'OB.UI.CustomerTextProperty',
  classes: 'obUiCustomerTextPropertyAddr',
  handlers: {
    onLoadValue: 'loadValue',
    onHideShow: 'hideShow'
  },
  loadValue: function(inSender, inEvent) {
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
  kind: 'OB.UI.FormElement.Checkbox',
  classes: 'obUiSwitchShippingInvoicingAddr',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue'
  },
  events: {
    onHideShowFields: ''
  },
  valueSet: function(inSender, inEvent) {
    if (inEvent.data.hasOwnProperty('btnUseSameCheck')) {
      this.doHideShowFields({
        checked: inEvent.data.btnUseSameCheck
      });
      this.setChecked(inEvent.data.btnUseSameCheck);
    }
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent.btnUseSameCheck = this.getChecked();
  },
  loadValue: function(inSender, inEvent) {
    if (inEvent.customer !== undefined) {
      this.formElement.hide();
    } else {
      this.formElement.show();
      this.setChecked(true);
      this.doHideShowFields({
        checked: this.getChecked()
      });
    }
  },
  saveChange: function(inSender, inEvent) {
    inEvent.customer.set('useSameAddrForShipAndInv', this.getChecked());
  },
  tap: function() {
    this.inherited(arguments);
    this.doHideShowFields({
      checked: this.getChecked()
    });
  }
});

enyo.kind({
  name: 'OB.UI.CustomerComboProperty',
  classes: 'obUiCustomerComboProperty',
  kind: 'OB.UI.List',
  renderLine: enyo.kind({
    kind: 'OB.UI.FormElement.Select.Option',
    initComponents: function() {
      this.inherited(arguments);
      this.setValue(this.model.get(this.parent.retrievedPropertyForValue));
      this.setContent(this.model.get(this.parent.retrievedPropertyForText));
    }
  }),
  renderEmpty: 'enyo.Control',
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
  valueSet: function(inSender, inEvent) {
    var i;
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      for (i = 0; i < this.getCollection().length; i++) {
        if (
          this.getCollection().models[i].get('id') ===
          inEvent.data[this.modelProperty]
        ) {
          this.setSelected(i);
          break;
        }
      }
    }
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getValue();
  },
  loadValue: function(inSender, inEvent) {
    this.setCollection(this.collection);
    this.fetchDataFunction(inEvent);
  },
  change: function() {},
  dataReadyFunction: function(data, inEvent) {
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

    result = _.find(
      this.collection.models,
      function(categ) {
        if (inEvent.customer) {
          //Edit: select actual value
          if (
            categ.get(this.retrievedPropertyForValue) ===
            inEvent.customer.get(this.modelProperty)
          ) {
            return true;
          }
        } else {
          //New: select default value
          if (
            categ.get(this.retrievedPropertyForValue) === this.defaultValue()
          ) {
            return true;
          }
        }
        index += 1;
      },
      this
    );
    if (result) {
      this.setSelected(index);
    } else {
      this.setSelected(0);
    }
  },
  saveChange: function(inSender, inEvent) {
    var selected = this.collection.at(this.getSelected());
    if (selected) {
      inEvent.customer.set(
        this.modelProperty,
        selected.get(this.retrievedPropertyForValue)
      );
      if (this.modelPropertyText) {
        inEvent.customer.set(
          this.modelPropertyText,
          selected.get(this.retrievedPropertyForText)
        );
      }
    }
  },
  initComponents: function() {
    if (
      this.collectionName &&
      OB &&
      OB.Collection &&
      OB.Collection[this.collectionName]
    ) {
      this.collection = new OB.Collection[this.collectionName]();
    } else {
      OB.info('OB.UI.CustomerComboProperty: Collection is required');
    }
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  classes: 'obObposPointOfSaleUiCustomersEditCreatecustomers',
  handlers: {
    onSaveCustomer: 'preSaveCustomer',
    onHideShowFields: 'hideShowFields'
  },
  events: {},
  components: [
    {
      name: 'bodyheader',
      classes: 'obObposPointOfSaleUiCustomersEditCreatecustomers-bodyheader'
    },
    {
      name: 'customerAttributes',
      kind: 'Scroller',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes',
      components: [
        {
          name: 'customerOnlyFields',
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-customerOnlyFields'
        },
        {
          name: 'shipAndInvAddress',
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-shipAndInvAddress',
          components: [
            {
              name: 'shipAddress',
              classes:
                'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-shipAndInvAddress-shipAddress',
              components: [
                {
                  name: 'shipLbl',
                  kind: 'OB.UI.FormSection.Label',
                  showing: false,
                  classes:
                    'obObposPointOfSaleUiCustomersEditCreatecustomers-shipAddress-shipLbl'
                },
                {
                  name: 'shippingAddrFields',
                  classes:
                    'obObposPointOfSaleUiCustomersEditCreatecustomers-shipAddress-shippingAddrFields u-clearBoth'
                }
              ]
            },
            {
              name: 'invAddress',
              classes:
                'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-shipAndInvAddress-invAddress',
              components: [
                {
                  name: 'invLbl',
                  kind: 'OB.UI.FormSection.Label',
                  showing: false,
                  classes:
                    'obObposPointOfSaleUiCustomersEditCreatecustomers-invAddress-invLbl'
                },
                {
                  style: 'clear:both',
                  name: 'invoicingAddrFields',
                  classes:
                    'obObposPointOfSaleUiCustomersEditCreatecustomers-invAddress-invoicingAddrFields u-clearBoth'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  hideShowFields: function(inSender, inEvent) {
    this.$.shipAddress.setShowing(!inEvent.checked);
    this.$.shipLbl.setShowing(!inEvent.checked);
    this.$.invLbl.setShowing(!inEvent.checked);
    this.waterfall('onHideShow', {
      checked: inEvent.checked
    });
    return true;
  },
  setCustomer: function(customer) {
    this.customer = customer;
    this.waterfall('onLoadValue', {
      customer: this.customer
    });
  },
  preSaveCustomer: function(inSender, inEvent) {
    var me = this,
      inSenderOriginal = inSender,
      inEventOriginal = inEvent;

    //Validate anonymous customer edit allowed
    if (
      this.customer &&
      OB.MobileApp.model.get('terminal').businessPartner === this.customer.id &&
      OB.MobileApp.model.hasPermission(
        'OBPOS_NotAllowEditAnonymousCustomer',
        true
      )
    ) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotEditAnonymousCustomer'));
      return;
    }

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreCustomerSave',
      {
        inSender: inSenderOriginal,
        inEvent: inEventOriginal,
        passValidation: true,
        error: '',
        meObject: me,
        validations: inEvent.validations
      },
      function(args) {
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
      }
    );
    return true;
  },
  saveCustomer: function(inSender, inEvent) {
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
      _.each(items, function(item) {
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

    function validateSMS(customer) {
      //Validate that sms field is filled if  'Commercial Auth -> sms' is checked
      var commercialAuthViaSms = customer.get('viasms');
      var alternativePhone = customer.get('alternativePhone');
      var phone = customer.get('phone');
      if (commercialAuthViaSms && (phone === '' && alternativePhone === '')) {
        return false;
      } else {
        return true;
      }
    }

    function validateEmail(customer) {
      //Validate that email field is filled if 'Commercial Auth -> email' is checked
      var commercialAuthViaEmail = customer.get('viaemail');
      var email = customer.get('email');
      if (commercialAuthViaEmail && email === '') {
        return false;
      } else {
        return true;
      }
    }

    function validateForm(form) {
      if (inEvent.validations) {
        var customer = form.model.get('customer'),
          errors = checkMandatoryFields(
            form.$.customerOnlyFields.children,
            customer
          );
        if (form.$.invoicingAddrFields.showing) {
          var invoicingErrors = checkMandatoryFields(
            form.$.invoicingAddrFields.children,
            customer
          );
          if (invoicingErrors) {
            if (errors) {
              errors += ', ';
            }
            errors +=
              form.$.invoicingAddrFields
                .getClassAttribute()
                .indexOf('twoAddrLayout') === 0
                ? OB.I18N.getLabel('OBPOS_LblBillAddr') +
                  ' [' +
                  invoicingErrors +
                  ']'
                : invoicingErrors;
          }
        }
        if (
          form.$.shippingAddrFields.showing &&
          form.$.shippingAddrFields
            .getClassAttribute()
            .indexOf('twoAddrLayout') === 0
        ) {
          var shippingErrors = checkMandatoryFields(
            form.$.shippingAddrFields.children,
            customer
          );
          if (shippingErrors) {
            if (errors) {
              errors += ', ';
            }
            errors +=
              OB.I18N.getLabel('OBPOS_LblShipAddr') +
              ' [' +
              shippingErrors +
              ']';
          }
        }
        if (errors) {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_BPartnerRequiredFields', [errors])
          );
          return false;
        }
        if (
          customer.get('firstName').length + customer.get('lastName').length >=
          60
        ) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_TooLongName'));
          return false;
        }
        if (!validateSMS(customer)) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_PhoneRequired'));
          return false;
        }
        if (!validateEmail(customer)) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_EmailRequired'));
          return false;
        }
      }
      return true;
    }

    function beforeCustomerSave(customer, isNew) {
      customer.adjustNames();
      if (customer.get('locationModel')) {
        customer.set(
          'countryId',
          customer.get('locationModel').get('countryId')
        );
        customer.set(
          'shipCountryId',
          customer.get('locationModel').get('shipCountryId')
        );
        customer.set(
          'countryName',
          customer.get('locationModel').get('countryName')
        );
        customer.set(
          'shipCountryName',
          customer.get('locationModel').get('shipCountryName')
        );
      }
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_BeforeCustomerSave',
        {
          customer: customer,
          isNew: isNew,
          validations: inEvent.validations,
          windowComponent: me
        },
        function(args) {
          if (args && args.cancellation && args.cancellation === true) {
            enableButtonsCallback(false);
            return true;
          }
          customerEdited = args.customer;
          args.customer.saveCustomer(function(result) {
            me.waterfall('onDisableButton', {
              disabled: false
            });
            if (result && !inEvent.silent) {
              me.bubble('onCancelClose', {
                customer: customerEdited
              });
            }
          });
        }
      );
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
      this.model.get('customer').loadModel(this.customer, function(customer) {
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
  initComponents: function() {
    var me = this;
    this.inherited(arguments);
    this.$.bodyheader.createComponent({
      kind: this.windowHeader
    });
    this.$.shipLbl.setContent(OB.I18N.getLabel('OBPOS_LblShipAddr'));
    this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
    this.attributeContainer = this.$.customerAttributes;
    //Sort Attributes
    if (OB.MobileApp.model.get('permissions').OBPOS_CustomerCompSortOrder) {
      var prefIndex,
        sortOrder,
        sortedAttr = [],
        prefAttr = [];
      try {
        sortOrder = JSON.parse(
          OB.MobileApp.model
            .get('permissions')
            .OBPOS_CustomerCompSortOrder.replace(/'/g, '"')
        );
        _.each(this.newAttributes, function(attr) {
          prefIndex = sortOrder.indexOf(attr.modelProperty);
          if (prefIndex >= 0) {
            prefAttr[prefIndex] = attr;
          } else {
            sortedAttr.push(attr);
          }
        });
        prefAttr = _.filter(prefAttr, function(attr) {
          return !OB.UTIL.isNullOrUndefined(attr);
        });
        this.newAttributes = prefAttr.concat(sortedAttr);
      } catch (e) {
        // Don't do anything if exception is thrown
      }
    }
    enyo.forEach(
      this.newAttributes,
      function(natt) {
        this.$.customerOnlyFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-customerOnlyFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
              natt.name,
            newAttribute: natt
          },
          {
            owner: me.attributeContainer
          }
        );
      },
      this
    );
    enyo.forEach(
      this.shipAddrAttributes,
      function(natt) {
        this.$.shippingAddrFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-shippingAddrFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
              natt.name,
            newAttribute: natt
          },
          {
            owner: me.attributeContainer
          }
        );
      },
      this
    );
    enyo.forEach(
      this.invAddrAttributes,
      function(natt) {
        this.$.invoicingAddrFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-invoicingAddrFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
              natt.name,
            newAttribute: natt
          },
          {
            owner: me.attributeContainer
          }
        );
      },
      this
    );
  },
  init: function(model) {
    this.model = model;
  }
});

enyo.kind({
  name: 'OB.UI.CustomerConsentCheckProperty',
  kind: 'OB.UI.FormElement.Checkbox',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue'
  },
  events: {
    onSaveProperty: ''
  },
  valueSet: function(inSender, inEvent) {
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      this.setChecked(inEvent.data[this.modelProperty]);
    }
    this.inherited(arguments);
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getChecked();
  },
  loadValue: function(inSender, inEvent) {
    var me = this;
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(me.modelProperty) !== undefined) {
        me.setChecked(inEvent.customer.get(me.modelProperty));
      }
    } else {
      me.setChecked(false);
    }
    this.inherited(arguments);
  },
  saveChange: function(inSender, inEvent) {
    var me = this;
    inEvent.customer.set(me.modelProperty, me.getChecked());
  }
});

enyo.kind({
  name: 'OB.UI.CustomerCheckCommercialAuth',
  kind: 'OB.UI.FormElement.Checkbox',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue'
  },
  events: {
    onSaveProperty: ''
  },
  valueSet: function(inSender, inEvent) {
    if (inEvent.data.hasOwnProperty(this.modelProperty)) {
      this.setChecked(inEvent.data[this.modelProperty]);
    }
    this.inherited(arguments);
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getChecked();
  },
  loadValue: function(inSender, inEvent) {
    var me = this;
    var form, i;
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(me.modelProperty) !== undefined) {
        me.setChecked(inEvent.customer.get(me.modelProperty));
      }
      form = me.formElement.parent;
      for (i = 0; i < form.children.length; i++) {
        if (form.children[i].name === 'line_contactpreferences') {
          var contactpreferences = form.children[i];
          var commercialauth = inEvent.customer.attributes.commercialauth;
          if (commercialauth) {
            contactpreferences.show();
          } else {
            contactpreferences.hide();
          }
        }
      }
    } else {
      me.setChecked(false);
    }
    this.inherited(arguments);
  },
  tap: function() {
    this.inherited(arguments);
    var form, i;
    form = this.formElement.parent;
    for (i = 0; i < form.children.length; i++) {
      if (form.children[i].name === 'line_contactpreferences') {
        var contactpreferences = form.children[i];
        var smsLabelCheck =
          contactpreferences.$.newAttribute.$.contactpreferences.$
            .smsLabelCheck;
        var emailLabelCheck =
          contactpreferences.$.newAttribute.$.contactpreferences.$
            .emailLabelCheck;
        if (this.getChecked()) {
          contactpreferences.show();
        } else {
          contactpreferences.hide();
          //reset saved values when hide contact preferences
          smsLabelCheck.setChecked(false);
          emailLabelCheck.setChecked(false);
        }
      }
    }
  },
  saveChange: function(inSender, inEvent) {
    var me = this;
    var form, i;
    inEvent.customer.set(me.modelProperty, me.getChecked());
    form = me.formElement.parent;
    for (i = 0; i < form.children.length; i++) {
      if (form.children[i].name === 'line_contactpreferences') {
        var contactpreferences = form.children[i];
        inEvent.customer.set(
          'viasms',
          contactpreferences.$.newAttribute.$.contactpreferences.$.smsLabelCheck.getChecked()
        );
        inEvent.customer.set(
          'viaemail',
          contactpreferences.$.newAttribute.$.contactpreferences.$.emailLabelCheck.getChecked()
        );
      }
    }
  }
});

enyo.kind({
  name: 'OB.UI.CustomerCheckOption',
  kind: 'enyo.Button',
  classes: 'obUiCustomerCheckOption',
  checked: false,
  tap: function() {
    if (this.readOnly) {
      return;
    }
    this.setChecked(!this.getChecked());
  },
  toggle: function() {
    this.setChecked(!this.getChecked());
  },
  setChecked: function(value) {
    if (value) {
      this.check();
    } else {
      this.unCheck();
    }
  },
  getChecked: function(value) {
    return this.checked;
  },
  check: function() {
    this.addClass('active');
    this.checked = true;
  },
  unCheck: function() {
    this.removeClass('active');
    this.checked = false;
  }
});

enyo.kind({
  name: 'OB.UI.CustomerCheckComboProperty',
  kind: 'OB.UI.FormElement.Custom',
  classes: 'obUiCustomerCheckComboProperty',
  components: [
    {
      kind: 'OB.UI.CustomerCheckOption',
      name: 'smsLabelCheck'
    },
    {
      kind: 'OB.UI.CustomerCheckOption',
      name: 'emailLabelCheck'
    }
  ],
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
  },
  events: {
    onSaveProperty: ''
  },
  loadValue: function(inSender, inEvent) {
    var me = this;
    var commertialauth, form, i;
    form = me.formElement.parent;
    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get('viasms') !== undefined) {
        me.$.smsLabelCheck.setChecked(inEvent.customer.get('viasms'));
      } else {
        me.$.smsLabelCheck.setChecked(false);
      }
      if (inEvent.customer.get('viaemail') !== undefined) {
        me.$.emailLabelCheck.setChecked(inEvent.customer.get('viaemail'));
      } else {
        me.$.emailLabelCheck.setChecked(false);
      }
      if (me.$.smsLabelCheck.getChecked()) {
        me.$.smsLabelCheck.addClass('active');
      } else {
        me.$.smsLabelCheck.removeClass('active');
      }
      if (me.$.emailLabelCheck.getChecked()) {
        me.$.emailLabelCheck.addClass('active');
      } else {
        me.$.emailLabelCheck.removeClass('active');
      }
      for (i = 0; i < form.children.length; i++) {
        if (form.children[i].name === 'line_commercialauth') {
          commertialauth = form.children[i].$.newAttribute.$.commercialauth;
          if (commertialauth.getChecked()) {
            if (me.$.smsLabelCheck.getChecked()) {
              me.$.smsLabelCheck.addClass('active');
            } else {
              me.$.smsLabelCheck.removeClass('active');
            }
            if (me.$.emailLabelCheck.getChecked()) {
              me.$.emailLabelCheck.addClass('active');
            } else {
              me.$.emailLabelCheck.removeClass('active');
            }
          }
        }
      }
    }
    this.doHandleFormElementStyle();
  },
  saveChange: function(inSender, inEvent) {
    var me = this;
    inEvent.customer.set('viasms', me.$.smsLabelCheck.getChecked());
    inEvent.customer.set('viaemail', me.$.emailLabelCheck.getChecked());
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.smsLabelCheck.setContent(OB.I18N.getLabel('OBPOS_LblSms'));
    this.$.emailLabelCheck.setContent(OB.I18N.getLabel('OBPOS_LblEmail'));
    if (this.readOnly) {
      this.$.smsLabelCheck.setDisabled(true);
      this.$.emailLabelCheck.setDisabled(true);
    }
    this.show();
  }
});
