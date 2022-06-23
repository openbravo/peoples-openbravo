/*
 ************************************************************************************
 * Copyright (C) 2012-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.customeraddr.ModalConfigurationRequiredForCreateCustomers',
  kind: 'OB.UI.ModalInfo',
  classes:
    'obObposPointOfSaleUiCustomeraddrModalConfigurationRequiredForCreateCustomers',
  i18nHeader: 'OBPOS_configurationRequired',
  bodyContent: {
    i18nContent: 'OBPOS_configurationNeededToCreateCustomers'
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.cancelEdit',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obObposPointOfSaleUiCustomeraddrCancelEdit',
  i18nContent: 'OBMOBC_LblCancel',
  handlers: {
    onDisableButton: 'disableButton'
  },
  disableButton: function(inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
  }
});

enyo.kind({
  name: 'OB.UI.CustomerAddrTextProperty',
  kind: 'OB.UI.FormElement.Input',
  type: 'text',
  classes: 'obUiCustomerAddrTextProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onblur: 'blur',
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
    let provider = OB.DQMController.getProviderForField(
      this.modelProperty,
      OB.DQMController.Validate
    );
    if (provider) {
      let me = this;
      provider.validate(
        null,
        me.modelProperty,
        me.getValue(),
        function(result) {
          if (result && result.status) {
            me.formElement.setMessage();
          } else {
            me.formElement.setMessage(result.message, true);
          }
        },
        'addressForm'
      );
    }
  },
  input: function(inSender, inEvent) {
    this.inherited(arguments);
    let provider = OB.DQMController.getProviderForField(
      this.modelProperty,
      OB.DQMController.Suggest
    );
    if (provider) {
      let me = this,
        value = this.getValue();
      if (value.length >= 3) {
        provider.suggest(
          null,
          this.modelProperty,
          value,
          function(result) {
            me.lastSuggestionList = result;
            me.formElement.$.scrim.show();
            me.formElement.$.suggestionList.createSuggestionList(result, value);
          },
          'addressForm'
        );
      } else {
        me.formElement.$.suggestionList.$.suggestionListtbody.destroyComponents();
        me.formElement.$.suggestionList.addClass('u-hideFromUI');
        me.lastSuggestionList = null;
      }
    }
  },
  change: function() {},
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
  loadValue: function(inSender, inEvent) {
    if (inEvent.customerAddr !== undefined) {
      if (inEvent.customerAddr.get(this.modelProperty) !== undefined) {
        this.setValue(inEvent.customerAddr.get(this.modelProperty));
      }
    } else {
      this.setValue('');
    }
    if (
      this.modelProperty === 'customerName' &&
      inEvent.customer !== undefined &&
      inEvent.customer.get('name') !== undefined
    ) {
      this.setValue(inEvent.customer.get('name'));
    }
  },
  saveChange: function(inSender, inEvent) {
    inEvent.customerAddr.set(this.modelProperty, this.getValue());
  },
  initComponents: function() {
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
    if (this.maxlength) {
      this.setAttribute('maxlength', this.maxlength);
    }
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
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers',
  classes: 'obObposPointOfSaleUiCustomeraddrEditCreatecustomers',
  handlers: {
    onExecuteSaveCustomerAddr: 'preSaveCustomerAddr'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [
    {
      name: 'bodyheader',
      classes: 'obObposPointOfSaleUiCustomeraddrEditCreatecustomers-bodyheader'
    },
    {
      name: 'customerAddrAttributes',
      classes:
        'obObposPointOfSaleUiCustomeraddrEditCreatecustomers-customerAddrAttributes'
    }
  ],
  setCustomerAddr: function(customer, customerAddr) {
    this.customer = customer;
    this.customerAddr = customerAddr;
    this.waterfall('onLoadValue', {
      customer: this.customer,
      customerAddr: this.customerAddr
    });
  },
  preSaveCustomerAddr: function(inSender, inEvent) {
    const me = this;
    const inSenderOriginal = inSender;
    const inEventOriginal = inEvent;
    const errorCallback = function() {
      me.parent.parent.waterfall('onDisableButton', {
        disabled: false
      });
    };

    function checkFields(items) {
      let errors = '';
      _.each(items, function(item) {
        if (item.coreElement && item.coreElement.mandatory) {
          var value = item.coreElement.getValue();
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

    function validateForm(form) {
      if (inEvent.validations) {
        let items = form.$.customerAddrAttributes.children,
          errors = checkFields(items);
        if (errors) {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_BPartnerRequiredFields', [errors])
          );
          return false;
        }
      }
      return true;
    }

    if (!validateForm(this)) {
      errorCallback();
      return;
    }

    //Validate anonymous customer Address edit allowed
    if (
      this.customer &&
      OB.MobileApp.model.get('terminal').businessPartner === this.customer.id &&
      OB.MobileApp.model.hasPermission(
        'OBPOS_NotAllowEditAnonymousCustomer',
        true
      )
    ) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotEditAnonymousCustAddr'));
      errorCallback();
      return;
    }

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreCustomerAddrSave',
      {
        inSender: inSenderOriginal,
        inEvent: inEventOriginal,
        passValidation: true,
        error: '',
        meObject: me
      },
      function(args) {
        if (args.cancellation) {
          errorCallback();
          return;
        }
        if (args.passValidation) {
          args.meObject.saveCustomerAddr(args.inSender, args.inEvent);
        } else {
          OB.UTIL.showWarning(args.error);
          errorCallback();
        }
      }
    );
  },

  saveCustomerAddr: async function(inSender, inEvent) {
    var me = this;

    function enableButtonsCallback() {
      me.parent.parent.waterfall('onDisableButton', {
        disabled: false
      });
    }

    function getCustomerAddrValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer,
        customerAddr: params.customerAddr
      });
    }

    function goToViewWindow(params) {
      if (!_.isUndefined(me.customerAddr)) {
        params.customerAddr.set(
          'onlyOneAddress',
          me.customerAddr.get('onlyOneAddress')
        );
      }
      me.bubble('onCancelClose', {
        customerAddr: params.customerAddr,
        makeSearch: true
      });
    }

    if (
      me.$.customerAddrAttributes.$.line_customerAddrShip &&
      !me.$.customerAddrAttributes.$.line_customerAddrShip.coreElement.checked
    ) {
      try {
        const criteria = new OB.App.Class.Criteria();
        criteria.criterion('bpartner', inSender.args.businessPartner.get('id'));
        criteria.criterion('isShipTo', true);
        let bPLocations = await OB.App.MasterdataModels.BusinessPartnerLocation.find(
          criteria.build()
        );
        if (bPLocations && bPLocations.length === 1) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_BPartnerNoShippingAddressSave')
          );
          enableButtonsCallback();
          return;
        }
      } catch (error) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_BPartnerNoShippingAddressSave')
        );
      }
    }

    if (this.customerAddr === undefined) {
      this.model.get('customerAddr').newCustomerAddr();
      this.model.get('customerAddr').set('bpartner', this.customer.get('id'));
      this.waterfall('onSaveChange', {
        customerAddr: this.model.get('customerAddr')
      });
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_BeforeCustomerAddrSave',
        {
          customerAddr: this.model.get('customerAddr'),
          isNew: true,
          windowComponent: me
        },
        function(args) {
          if (args && args.cancellation && args.cancellation === true) {
            enableButtonsCallback();
            return true;
          }
          var callback = function() {
            enableButtonsCallback();
            goToViewWindow({
              customer: OB.UTIL.clone(me.customer),
              customerAddr: OB.UTIL.clone(me.model.get('customerAddr'))
            });
          };
          me.model
            .get('customerAddr')
            .saveCustomerAddr(callback, enableButtonsCallback);
        }
      );
    } else {
      this.model
        .get('customerAddr')
        .loadModel(this.customerAddr, function(customerAddr) {
          me.customer.loadBPLocations(
            null,
            null,
            function(ships, bills, locs) {
              var callback = function() {
                var i;
                goToViewWindow({
                  customer: me.customer,
                  customerAddr: customerAddr
                });
                if (
                  customerAddr.get('id') === me.customer.get('locId') ||
                  customerAddr.get('id') === me.customer.get('shipLocId')
                ) {
                  if (!customerAddr.get('isBillTo')) {
                    me.customer.set('locId', null);
                    me.customer.set('locName', null);
                    me.customer.set('postalCode', null);
                    me.customer.set('cityName', null);
                    me.customer.set('countryName', null);
                    me.customer.set('regionId', null);
                    me.customer.set('regionName', null);
                  } else {
                    me.customer.set('locId', customerAddr.get('id'));
                    me.customer.set('locName', customerAddr.get('name'));
                    me.customer.set(
                      'postalCode',
                      customerAddr.get('postalCode')
                    );
                    me.customer.set('cityName', customerAddr.get('cityName'));
                    me.customer.set(
                      'countryName',
                      customerAddr.get('countryName')
                    );
                    me.customer.set('regionId', customerAddr.get('regionId'));
                    me.customer.set(
                      'regionName',
                      customerAddr.get('regionName')
                    );
                  }
                  if (!customerAddr.get('isShipTo')) {
                    me.customer.set('shipLocId', null);
                    me.customer.set('shipLocName', null);
                    me.customer.set('shipPostalCode', null);
                    me.customer.set('shipCityName', null);
                    me.customer.set('shipRegionId', null);
                    me.customer.set('shipRegionName', null);
                    me.customer.set('shipCountryId', null);
                    me.customer.set('shipCountryName', null);
                  } else {
                    me.customer.set('shipLocId', customerAddr.get('id'));
                    me.customer.set('shipLocName', customerAddr.get('name'));
                    me.customer.set(
                      'shipPostalCode',
                      customerAddr.get('postalCode')
                    );
                    me.customer.set(
                      'shipCityName',
                      customerAddr.get('cityName')
                    );
                    me.customer.set(
                      'shipRegionId',
                      customerAddr.get('regionId')
                    );
                    me.customer.set(
                      'shipRegionName',
                      customerAddr.get('regionName')
                    );
                    me.customer.set(
                      'shipCountryId',
                      customerAddr.get('countryId')
                    );
                    me.customer.set(
                      'shipCountryName',
                      customerAddr.get('countryName')
                    );
                  }
                  me.customer.set('locationModel', customerAddr);
                  //If it an js object, convert in a BPLocation
                  if (
                    me.customer.get('locationBillModel') &&
                    !me.customer.get('locationBillModel').get
                  ) {
                    me.customer.set(
                      'locationBillModel',
                      new OB.Model.BPLocation(
                        me.customer.get('locationBillModel')
                      )
                    );
                  }
                  if (me.model.get('orderList').length > 1) {
                    for (i = 0; i < me.model.get('orderList').length; i++) {
                      if (
                        me.model
                          .get('orderList')
                          .models[i].get('bp')
                          .get('id') === me.customer.get('id')
                      ) {
                        me.model
                          .get('orderList')
                          .models[i].set('bp', me.customer);
                      }
                    }
                  }
                  me.doChangeBusinessPartner({
                    businessPartner: me.customer,
                    target: 'order'
                  });
                  enableButtonsCallback();
                } else {
                  enableButtonsCallback();
                }
              };

              getCustomerAddrValues({
                customer: me.customer,
                customerAddr: customerAddr
              });

              var billingLocs = _.filter(locs, function(loc) {
                return (
                  loc.get('isBillTo') &&
                  loc.get('id') !== me.model.get('customerAddr').get('id')
                );
              });

              if (
                !me.model.get('customerAddr').get('isBillTo') &&
                billingLocs.length === 0
              ) {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBPOS_NoBillingAddrHeader'),
                  OB.I18N.getLabel('OBPOS_NoBillingAddrBody', [
                    me.model.get('customerAddr').get('customerName'),
                    me.model.get('customerAddr').get('name')
                  ])
                );
                enableButtonsCallback();
              } else {
                if (
                  OB.MobileApp.model.receipt.get('lines').length > 0 &&
                  OB.MobileApp.model.receipt.get('bp').get('shipLocId') ===
                    customerAddr.get('id') &&
                  !customerAddr.get('isShipTo')
                ) {
                  OB.UTIL.showConfirmation.display(
                    OB.I18N.getLabel('OBPOS_InformationTitle'),
                    OB.I18N.getLabel('OBPOS_UncheckShipToText'),
                    [
                      {
                        label: OB.I18N.getLabel('OBPOS_LblOk'),
                        isConfirmButton: true,
                        action: function() {
                          OB.UTIL.HookManager.executeHooks(
                            'OBPOS_BeforeCustomerAddrSave',
                            {
                              customerAddr: me.model.get('customerAddr'),
                              isNew: false,
                              windowComponent: me
                            },
                            function(args) {
                              var receipt = OB.MobileApp.model.receipt,
                                orderlines = [];
                              if (
                                args &&
                                args.cancellation &&
                                args.cancellation === true
                              ) {
                                enableButtonsCallback();
                                return true;
                              }
                              _.each(receipt.get('lines').models, function(
                                line
                              ) {
                                orderlines.push(line);
                              });
                              receipt.deleteLinesFromOrder(
                                orderlines,
                                function() {
                                  args.customerAddr.saveCustomerAddr(
                                    callback,
                                    enableButtonsCallback
                                  );
                                }
                              );
                            }
                          );
                        }
                      },
                      {
                        label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                        action: function() {
                          enableButtonsCallback();
                        }
                      }
                    ],
                    {
                      autoDismiss: false,
                      onHideFunction: function() {
                        return;
                      }
                    }
                  );
                } else {
                  OB.UTIL.HookManager.executeHooks(
                    'OBPOS_BeforeCustomerAddrSave',
                    {
                      customerAddr: me.model.get('customerAddr'),
                      isNew: false,
                      windowComponent: me
                    },
                    function(args) {
                      if (
                        args &&
                        args.cancellation &&
                        args.cancellation === true
                      ) {
                        enableButtonsCallback();
                        return true;
                      }
                      args.customerAddr.saveCustomerAddr(
                        callback,
                        enableButtonsCallback
                      );
                    }
                  );
                }
              }
            },
            me.customer.get('id')
          );
        });
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    if (this.windowFooter) {
      this.owner.owner.$.footer.createComponent({
        kind: this.windowFooter
      });
      this.owner.owner.$.footer.show();
    }
    this.attributeContainer = this.$.customerAddrAttributes;
    enyo.forEach(
      this.newAttributes,
      function(natt) {
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
            classes:
              'obObposPointOfSaleUiCustomeraddrEditCreatecustomers-customerAddrAttributes-obUiCustomerPropertyLine ' +
              (natt.classes ? natt.classes : ''),
            name: 'line_' + natt.name,
            coreElement: natt
          });
        }
      },
      this
    );
  },
  init: function(model) {
    this.model = model;
  }
});

enyo.kind({
  name: 'OB.UI.CustomerAddrCheckProperty',
  kind: 'OB.UI.FormElement.Checkbox',
  classes: 'obUiCustomerAddrCheckProperty',
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
      if (inEvent.data[this.modelProperty]) {
        this.check();
      } else {
        this.unCheck();
      }
    }
  },
  retrieveValue: function(inSender, inEvent) {
    inEvent[this.modelProperty] = this.checked;
  },
  loadValue: function(inSender, inEvent) {
    var me = this;
    if (inEvent.customerAddr !== undefined) {
      if (inEvent.customerAddr.get(me.modelProperty) !== undefined) {
        me.setChecked(inEvent.customerAddr.get(me.modelProperty));
      }
    } else {
      me.setChecked(true);
    }
  },
  saveChange: function(inSender, inEvent) {
    var me = this;
    inEvent.customerAddr.set(me.modelProperty, me.checked);
  },
  initComponents: function() {
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
  classes: 'obUiCustomerAddrComboProperty',
  kind: 'OB.UI.List',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange',
    onSetValue: 'valueSet',
    onRetrieveValues: 'retrieveValue',
    onchange: 'change'
  },
  events: {
    onSaveProperty: '',
    onSetValues: ''
  },
  renderLine: enyo.kind({
    kind: 'OB.UI.FormElement.Select.Option',
    classes:
      'obUiCustomerAddrComboProperty-customerAddrCombo-renderLine-enyoOption',
    initComponents: function() {
      this.inherited(arguments);
      this.setValue(this.model.get(this.parent.retrievedPropertyForValue));
      this.setContent(this.model.get(this.parent.retrievedPropertyForText));
    }
  }),
  renderEmpty: 'enyo.Control',
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
    var index = 0,
      result = null;
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

    result = _.find(
      this.collection.models,
      function(categ) {
        if (inEvent.customerAddr) {
          //Edit: select actual value
          if (
            categ.get(this.retrievedPropertyForValue) ===
            inEvent.customerAddr.get(this.modelProperty)
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
    inEvent.customerAddr.set(
      this.modelProperty,
      selected.get(this.retrievedPropertyForValue)
    );
    if (this.modelPropertyText) {
      inEvent.customerAddr.set(
        this.modelPropertyText,
        selected.get(this.retrievedPropertyForText)
      );
    }
  },
  initComponents: function() {
    this.collection = new Backbone.Collection();
    this.inherited(arguments);
  }
});
