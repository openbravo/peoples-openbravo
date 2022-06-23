/*
 ************************************************************************************
 * Copyright (C) 2012-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

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
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obObposPointOfSaleUiCustomersCancelEdit',
  i18nContent: 'OBMOBC_LblCancel',
  handlers: {
    onDisableButton: 'disableButton'
  },
  disableButton: function(inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
  },
  tap: function() {
    this.bubble('onCancelClose');
  }
});

enyo.kind({
  name: 'OB.UI.CustomerPropertyLine',
  kind: 'OB.UI.FormElement',
  classes: 'obUiFormElement_dataEntry obUICustomerPropertyLine'
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
    onRetrieveValues: 'retrieveValue',
    onkeydown: 'keydown'
  },
  events: {
    onSaveProperty: '',
    onRetrieveCustomer: '',
    onSetValues: ''
  },
  valueSet: function(inSender, inEvent) {
    if (
      Object.prototype.hasOwnProperty.call(inEvent.data, this.modelProperty)
    ) {
      this.setValue(inEvent.data[this.modelProperty]);
    }
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getValue();
  },
  input: function(inSender, inEvent) {
    this.inherited(arguments);
    let customer = this.formElement.owner.owner.customer;
    let provider = OB.DQMController.getProviderForField(
      this.modelProperty,
      OB.DQMController.Suggest
    );
    if (provider) {
      let me = this,
        value = this.getValue(),
        oldCustomer = '';
      if (customer !== undefined) {
        oldCustomer = customer.toJSON();
      }
      if (value.length >= 3) {
        provider.suggest(
          oldCustomer,
          this.modelProperty,
          value,
          function(result) {
            me.lastSuggestionList = result;
            me.formElement.$.scrim.show();
            me.formElement.$.suggestionList.createSuggestionList(result, value);
          },
          'customerForm'
        );
      } else {
        me.formElement.$.suggestionList.$.suggestionListtbody.destroyComponents();
        me.formElement.$.suggestionList.addClass('u-hideFromUI');
        me.lastSuggestionList = null;
      }
    }
  },
  focus: function(inSender, inEvent) {
    this.onFocusValue = this.getValue();
  },
  blur: function(inSender, inEvent) {
    this.inherited(arguments);
    if (this.onFocusValue === this.getValue()) {
      return;
    }
    setTimeout(() => {
      this.validateValue(inSender, inEvent);
    }, 50);
  },
  validateValue: function(inSender, inEvent) {
    const value = this.getValue();
    if (
      this.lastSuggestionList &&
      this.lastSuggestionList.length > 0 &&
      this.lastSuggestionList.find(data => {
        if (data instanceof Object) {
          return value === data.displayedInfo;
        }
        return value === data;
      })
    ) {
      return;
    }
    let customer = this.formElement.owner.owner.customer;
    let provider = OB.DQMController.getProviderForField(
      this.modelProperty,
      OB.DQMController.Validate
    );
    if (provider) {
      let me = this,
        oldCustomer = '';
      if (customer !== undefined) {
        oldCustomer = customer.toJSON();
      }
      provider.validate(
        oldCustomer,
        me.modelProperty,
        me.getValue(),
        function(result) {
          if (result && result.status) {
            me.formElement.setMessage();
          } else {
            me.formElement.setMessage(result.message, true);
          }
        },
        'customerForm'
      );
    }
  },
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
  },
  keydown: function(inSender, inEvent) {
    this.inherited(arguments);
    let index,
      suggestionList = this.formElement.$.suggestionList;
    if (suggestionList.$.suggestionListtbody.children.length > 0) {
      index = suggestionList.getActiveElement();
      //ArrowDown
      if (inEvent.keyCode === 40) {
        suggestionList.setActiveElement(index + 1);
        suggestionList.setInactiveElement(index);
      }
      //ArrowUp
      if (inEvent.keyCode === 38) {
        if (index > 0) {
          suggestionList.setActiveElement(index - 1);
          suggestionList.setInactiveElement(index);
        }
      }
      //Enter and Tab
      if (inEvent.keyCode === 13 || inEvent.keyCode === 9) {
        let activeElement =
          suggestionList.$.suggestionListtbody.children[
            suggestionList.getActiveElement()
          ];
        if (activeElement) {
          let value =
            activeElement.value.start +
            activeElement.value.bold +
            activeElement.value.end;
          suggestionList.owner.$.coreElementContainer.children[0].setValue(
            value
          );
          suggestionList.addClass('u-hideFromUI');
          suggestionList.owner.$.scrim.hide();
        }
        return true;
      }
    }
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
  name: 'OB.UI.CustomerStatisticsTextProperty',
  kind: 'OB.UI.FormElement.Input',
  class: 'obUiCustomerStatisticsTextProperty',
  type: 'text',
  handlers: {
    onSetStatisticValue: 'setStatisticsValue'
  },
  setStatisticsValue: function(inSender, inEvent) {
    if (inEvent[this.textProperty]) {
      this.setValue(inEvent[this.textProperty]);
      this.formElement.show();
      this.formElement.parent.parent.show();
    } else {
      this.formElement.hide();
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
    if (Object.prototype.hasOwnProperty.call(inEvent.data, 'btnUseSameCheck')) {
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
    if (
      Object.prototype.hasOwnProperty.call(inEvent.data, this.modelProperty)
    ) {
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
    if (this.destroyed) {
      return;
    }
    if (data) {
      data.models
        ? this.collection.reset(data.models)
        : this.collection.reset(data);
    } else {
      this.collection.reset(null);
      return;
    }

    var i;
    for (i = 0; i < this.collection.models.length; i++) {
      var categ = this.collection.models[i];
      //Edit: select actual value
      if (
        inEvent.customer &&
        categ.get(this.retrievedPropertyForValue) ===
          inEvent.customer.get(this.modelProperty)
      ) {
        this.setSelected(i);
        break;
      } else if (
        //New: select default value
        categ.get(this.retrievedPropertyForValue) === this.defaultValue()
      ) {
        this.setSelected(i);
      }
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
    if (this.collectionName) {
      OB && OB.Collection && OB.Collection[this.collectionName]
        ? (this.collection = new OB.Collection[this.collectionName]())
        : (this.collection = new Backbone.Collection());
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
    onExecuteSaveCustomer: 'preSaveCustomer',
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
        },
        {
          name: 'statistics',
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-statistics',
          components: [
            {
              name: 'statisticsLbl',
              kind: 'OB.UI.FormSection.Label',
              classes:
                'obObposPointOfSaleUiCustomersEditCreatecustomers-statistics-statisticsLbl'
            },
            {
              name: 'statisticsFields',
              classes:
                'obObposPointOfSaleUiCustomersEditCreatecustomers-statistics-statisticsFields u-clearBoth'
            }
          ]
        }
      ]
    }
  ],
  hideShowFields: function(inSender, inEvent) {
    if (inEvent.checked) {
      this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblAddress'));
    } else {
      this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
    }
    this.$.shipAddress.setShowing(!inEvent.checked);
    this.$.shipLbl.setShowing(!inEvent.checked);
    this.$.invLbl.setShowing(true);
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
    const me = this;
    const inSenderOriginal = inSender;
    const inEventOriginal = inEvent;
    const errorCallback = function() {
      me.parent.parent.waterfall('onDisableButton', {
        disabled: false
      });
    };

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
      errorCallback();
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
          errorCallback();
          return;
        }
        if (args.passValidation) {
          args.meObject.saveCustomer(args.inSender, args.inEvent);
        } else {
          OB.UTIL.showError(args.error);
          errorCallback();
        }
      }
    );
    return true;
  },
  saveCustomer: function(inSender, inEvent) {
    var me = this,
      customerEdited;

    function enableButtonsCallback(disable) {
      me.parent.parent.waterfall('onDisableButton', {
        disabled: disable
      });
    }

    function getCustomerValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer
      });
    }

    function checkFields(items, customer) {
      let errors = '';
      _.each(items, function(item) {
        if (item.coreElement && item.coreElement.mandatory) {
          var value = customer.get(item.coreElement.modelProperty);
          if (!value) {
            item.setMessage(OB.I18N.getLabel('OBMOBC_LblMandatoryField'), true);
            if (errors) {
              errors += ', ';
            }
            errors += OB.I18N.getLabel(item.coreElement.i18nLabel);
          } else {
            item.setMessage();
          }
        }
        if (
          item.getError() &&
          !OB.UTIL.isNullOrUndefined(item.$.msg.content) &&
          item.$.msg.content !== '' &&
          item.$.msg.content !== OB.I18N.getLabel('OBMOBC_LblMandatoryField')
        ) {
          if (errors) {
            errors += ', ';
          }
          errors += OB.I18N.getLabel(item.coreElement.i18nLabel);
        }
      });
      return errors;
    }

    function requiredSMS(customer, form) {
      //Validate that sms field is filled if  'Commercial Auth -> sms' is checked
      var commercialAuthViaSms = customer.get('viasms');
      var alternativePhone = customer.get('alternativePhone');
      var phone = customer.get('phone');
      var phoneField = form.attributeComponents.customerPhone;
      var altPhoneField = form.attributeComponents.alternativePhone;
      if (commercialAuthViaSms && (phone === '' && alternativePhone === '')) {
        phoneField.setMessage(
          OB.I18N.getLabel('OBMOBC_LblMandatoryField'),
          true
        );
        altPhoneField.setMessage(
          OB.I18N.getLabel('OBMOBC_LblMandatoryField'),
          true
        );
        return false;
      } else {
        phoneField.setMessage();
        altPhoneField.setMessage();
        return true;
      }
    }

    function requitedEmail(customer, form) {
      //Validate that email field is filled if 'Commercial Auth -> email' is checked
      var commercialAuthViaEmail = customer.get('viaemail');
      var email = customer.get('email');
      var emailField = form.attributeComponents.customerEmail;
      if (commercialAuthViaEmail && email === '') {
        emailField.setMessage(
          OB.I18N.getLabel('OBMOBC_LblMandatoryField'),
          true
        );
        return false;
      } else {
        emailField.setMessage();
        return true;
      }
    }

    function validateForm(form) {
      if (inEvent.validations) {
        var customer = form.model.get('customer'),
          errors = '';
        var contactInfoErrors = checkFields(
          form.$.customerOnlyFields.$.contactInfo.$.contactInfoFields.children,
          customer
        );
        if (contactInfoErrors) {
          errors += contactInfoErrors;
        }
        var personalInfoErrors = checkFields(
          form.$.customerOnlyFields.$.personalInfo.$.personalInfoFields
            .children,
          customer
        );
        if (personalInfoErrors) {
          if (errors) {
            errors += ', ';
          }
          errors += personalInfoErrors;
        }
        var otherInfoErrors = checkFields(
          form.$.customerOnlyFields.$.otherInfo.$.otherInfoFields.children,
          customer
        );
        if (otherInfoErrors) {
          if (errors) {
            errors += ', ';
          }
          errors += otherInfoErrors + ', ';
        }
        if (form.$.invoicingAddrFields.showing) {
          var invoicingErrors = checkFields(
            form.$.invoicingAddrFields.children,
            customer
          );
          if (invoicingErrors) {
            if (errors) {
              errors += ', ';
            }
            errors += form.$.shipAddress.showing
              ? OB.I18N.getLabel('OBPOS_LblBillAddr') +
                ' [' +
                invoicingErrors +
                ']'
              : invoicingErrors;
          }
        }
        if (form.$.shippingAddrFields.showing && form.$.shipAddress.showing) {
          var shippingErrors = checkFields(
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
        if (!requiredSMS(customer, form)) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_PhoneRequired'));

          return false;
        }
        if (!requitedEmail(customer, form)) {
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
            enableButtonsCallback(false);

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
    if (this.windowFooter) {
      this.owner.owner.$.footer.createComponent({
        kind: this.windowFooter
      });
      this.owner.owner.$.footer.show();
    }
    this.$.shipLbl.setContent(OB.I18N.getLabel('OBPOS_LblShipAddr'));
    this.$.invLbl.setContent(OB.I18N.getLabel('OBPOS_LblBillAddr'));
    this.$.statisticsLbl.setContent(OB.I18N.getLabel('OBPOS_LblStatistics'));
    this.attributeContainer = this.$.customerAttributes;

    this.attributeComponents = {};

    //Create Field Group along with Fields
    enyo.forEach(
      this.fieldGroups,
      function(fg) {
        //Section
        this.$.customerOnlyFields.createComponent({
          name: fg.sectionName,
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-customerOnlyFields-' +
            fg.sectionName
        });
        //Label
        this.$.customerOnlyFields.$[fg.sectionName].createComponent({
          name: fg.sectionLableName,
          kind: 'OB.UI.FormSection.Label',
          content: OB.I18N.getLabel(fg.title),
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-customerOnlyFields-' +
            fg.sectionName +
            '-' +
            fg.sectionLableName
        });
        //FieldSection
        this.$.customerOnlyFields.$[fg.sectionName].createComponent({
          name: fg.sectionFieldsName,
          classes:
            'obObposPointOfSaleUiCustomersEditCreatecustomers-customerAttributes-customerOnlyFields-' +
            fg.sectionName +
            '-' +
            fg.sectionFieldsName +
            ' u-clearBoth'
        });
        //FieldLine
        enyo.forEach(
          this.newAttributes,
          function(natt) {
            if (natt.fgSection === undefined) {
              natt.fgSection = 'OBPOS_FG_OthersInformation';
            }
            if (natt.fgSection === fg.groupName) {
              this.attributeComponents[natt.name] = this.$.customerOnlyFields.$[
                fg.sectionName
              ].$[fg.sectionFieldsName].createComponent(
                {
                  kind: 'OB.UI.CustomerPropertyLine',
                  name: 'line_' + natt.name,
                  classes:
                    'obObposPointOfSaleUiCustomersEditCreatecustomers-customerOnlyFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
                    natt.name,
                  coreElement: natt
                },
                {
                  owner: me.attributeContainer
                }
              );
            }
          },
          this
        );
      },
      this
    );
    enyo.forEach(
      this.sameAddrCheckAttributes,
      function(natt) {
        this.attributeComponents[
          natt.name
        ] = this.$.customerOnlyFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            coreElement: natt
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
        this.attributeComponents[
          natt.name
        ] = this.$.shippingAddrFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-shippingAddrFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
              natt.name,
            coreElement: natt
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
        this.attributeComponents[
          natt.name
        ] = this.$.invoicingAddrFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-invoicingAddrFields-obUiCustomerPropertyLine obUiCustomerPropertyLine_' +
              natt.name,
            coreElement: natt
          },
          {
            owner: me.attributeContainer
          }
        );
      },
      this
    );
    enyo.forEach(
      this.statisticsAttributes,
      function(natt) {
        this.attributeComponents[
          natt.name
        ] = this.$.statisticsFields.createComponent(
          {
            kind: 'OB.UI.CustomerPropertyLine',
            name: 'line_' + natt.name,
            classes:
              'obObposPointOfSaleUiCustomersEditCreatecustomers-statisticsFields-obUiCustomerPropertyLine',
            coreElement: natt
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
  name: 'OB.UI.CustomerCheckProperty',
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
    if (
      Object.prototype.hasOwnProperty.call(inEvent.data, this.modelProperty)
    ) {
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
    if (
      Object.prototype.hasOwnProperty.call(inEvent.data, this.modelProperty)
    ) {
      this.setChecked(inEvent.data[this.modelProperty]);
    }
    this.inherited(arguments);
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.getChecked();
  },
  loadValue: function(inSender, inEvent) {
    var me = this;
    var form, i, contactpreferences;
    form = me.formElement.parent;

    //Get Contact Preference Element
    for (i = 0; i < form.children.length; i++) {
      if (form.children[i].name === 'line_contactpreferences') {
        contactpreferences = form.children[i];
      }
    }

    if (inEvent.customer !== undefined) {
      if (inEvent.customer.get(me.modelProperty) !== undefined) {
        me.setChecked(inEvent.customer.get(me.modelProperty));
      }
      var commercialauth = inEvent.customer.attributes.commercialauth;
      if (commercialauth) {
        contactpreferences.show();
      } else {
        contactpreferences.hide();
      }
    } else {
      me.setChecked(false);
      contactpreferences.hide();
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
        var smsLabelCheck = contactpreferences.coreElement.$.smsLabelCheck;
        var emailLabelCheck = contactpreferences.coreElement.$.emailLabelCheck;
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
          contactpreferences.coreElement.$.smsLabelCheck.getChecked()
        );
        inEvent.customer.set(
          'viaemail',
          contactpreferences.coreElement.$.emailLabelCheck.getChecked()
        );
      }
    }
  }
});

enyo.kind({
  name: 'OB.UI.CustomerCheckOption',
  kind: 'OB.UI.Button',
  classes: 'obUiCustomerCheckOption',
  checked: false,
  tap: function() {
    if (this.readOnly || this.getDisabled()) {
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
  getCanNullify: function() {
    return false;
  },
  getDisplayedValue: function() {
    this.inherited(arguments);
    return true;
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
          commertialauth = form.children[i].coreElement;
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
    } else {
      me.$.smsLabelCheck.setChecked(false);
      me.$.emailLabelCheck.setChecked(false);
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
    this.$.smsLabelCheck.setLabel(OB.I18N.getLabel('OBPOS_LblSms'));
    this.$.emailLabelCheck.setLabel(OB.I18N.getLabel('OBPOS_LblEmail'));
    if (this.readOnly) {
      this.$.smsLabelCheck.setDisabled(true);
      this.$.emailLabelCheck.setDisabled(true);
    }
    this.show();
  }
});
