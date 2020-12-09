/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
  classes: 'obObposPointOfSaleUiCustomersNewcustomer',
  hideCloseButton: true,
  autoDismiss: false,
  closeOnEscKey: false,
  events: {
    onShowPopup: '',
    onDisableButton: ''
  },
  handlers: {
    onCancelClose: 'cancelClose',
    onSetValues: 'setValues',
    onSaveCustomer: 'saveCustomer',
    onRetrieveCustomer: 'retrieveCustomers'
  },
  cancelClose: function(inSender, inEvent) {
    this.customer = inEvent.customer;
    this.hide();
    return true;
  },
  setValues: function(inSender, inEvent) {
    this.waterfall('onSetValue', inEvent);
  },
  retrieveCustomers: function(inSender, inEvent) {
    var retrievedValues = inEvent || {};
    this.waterfall('onRetrieveValues', retrievedValues);
    return retrievedValues;
  },
  executeOnShow: function() {
    var me = this;

    if (
      OB.MobileApp.model.get('terminal').defaultbp_paymentmethod !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bpcategory !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_paymentterm !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_invoiceterm !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bpcountry !== null &&
      OB.MobileApp.model.get('terminal').defaultbp_bporg !== null
    ) {
      // Hide components depending on its displayLogic function
      _.each(
        this.$.body.$.edit_createcustomers_impl.$.customerAttributes.$,
        function(attribute) {
          if (attribute.name !== 'strategy') {
            if (
              attribute.coreElement.displayLogic &&
              !attribute.coreElement.displayLogic()
            ) {
              attribute.hide();
            }
          }
        }
      );

      //hide address fields while editing customers
      if (this.args.businessPartner) {
        this.customer = this.args.businessPartner;
        this.$.body.$.edit_createcustomers_impl.setCustomer(
          this.args.businessPartner
        );
        this.$.body.$.edit_createcustomers_impl.$.invoicingAddrFields.hide();
        this.$.body.$.edit_createcustomers_impl.$.shippingAddrFields.hide();
        this.$.header.setContent(OB.I18N.getLabel('OBPOS_TitleEditCustomer'));

        //Statistics
        me.$.body.$.edit_createcustomers_impl.$.statistics.setShowing(false);
        var anonymousCustomer = OB.MobileApp.model.get('businessPartner').id;
        if (
          OB.MobileApp.model.get('connectedToERP') &&
          this.args.businessPartner.id !== anonymousCustomer
        ) {
          var process = new OB.DS.Process(
            'org.openbravo.retail.posterminal.process.CustomerStatistics'
          );
          process.exec(
            {
              organization: OB.MobileApp.model.get('terminal').organization,
              bpId: this.args.businessPartner.id
            },
            function(data, message) {
              if (data && data.exception) {
                OB.UTIL.showError(
                  OB.I18N.getLabel('OBPOS_GetStatistics_Error')
                );
              } else if (data) {
                me.waterfall('onSetStatisticValue', data);
              } else {
                OB.UTIL.showError(
                  OB.I18N.getLabel('OBPOS_GetStatistics_Error')
                );
              }
            }
          );
        }
      } else {
        this.$.body.$.edit_createcustomers_impl.setCustomer(undefined);
        this.$.body.$.edit_createcustomers_impl.$.invoicingAddrFields.show();
        this.$.body.$.edit_createcustomers_impl.$.shippingAddrFields.show();
        this.$.header.setContent(OB.I18N.getLabel('OBPOS_TitleNewCustomer'));
        this.$.body.$.edit_createcustomers_impl.$.statistics.setShowing(false);
      }
      this.waterfall('onDisableButton', {
        disabled: false
      });
      if (this.args.focusError) {
        _.each(
          this.$.body.$.edit_createcustomers_impl.$.customerAttributes.$,
          function(attribute) {
            _.each(this.args.focusError, function(field, indx) {
              if (attribute.name === 'line_' + field) {
                var attr = attribute.$.coreElementContainer.$[field];
                attr.addClass(
                  'obObposPointOfSaleUiCustomersNewcustomer-newAttribute_error'
                );
                if (indx === 0) {
                  window.setTimeout(function() {
                    attr.focus();
                  }, 100);
                }
              }
            });
          },
          this
        );
      }
      //show
      return true;
    } else {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCreateNewCustomers'
      });
      //not show
      return false;
    }
  },
  executeOnHide: function() {
    if (this.args.focusError) {
      _.each(
        this.$.body.$.edit_createcustomers_impl.$.customerAttributes.$,
        function(attribute) {
          _.each(this.args.focusError, function(field) {
            if (attribute.name === 'line_' + field) {
              var attr = attribute.$.coreElementContainer.$[field];
              attr.removeClass(
                'obObposPointOfSaleUiCustomersNewcustomer-newAttribute_error'
              );
              attribute.$.labelLine.addClass(
                'obObposPointOfSaleUiCustomersNewcustomer-labelLine_black'
              );
            }
          });
        },
        this
      );
    }
    _.each(
      this.$.body.$.edit_createcustomers_impl.$.customerAttributes.$,
      function(attribute) {
        if (attribute.hasClass('obUiFormElement_error')) {
          attribute.removeClass('obUiFormElement_error');
          attribute.setMessage();
        }
      }
    );
    var navigationPath =
      this.customer || !this.args.cancelNavigationPath
        ? this.args.navigationPath
        : this.args.cancelNavigationPath;
    if (navigationPath) {
      this.doShowPopup({
        popup: navigationPath[navigationPath.length - 1],
        args: {
          businessPartner: this.customer
            ? this.customer
            : this.args.businessPartner,
          target: this.args.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(
            navigationPath
          ),
          makeSearch: this.customer !== undefined
        }
      });
    }
  },
  showingChanged: function() {
    this.inherited(arguments);
    if (!this.showing) {
      this.$.body.$.edit_createcustomers_impl.$.invLbl.setShowing(false);
      this.$.body.$.edit_createcustomers_impl.$.shipLbl.setShowing(false);
    }
  },
  saveCustomer: function(inSender, inEvent) {
    this.waterfall('onExecuteSaveCustomer', inEvent);
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl',
    classes:
      'obObposPointOfSaleUiCustomersNewcustomer-body-obObposPointOfSaleUiCustomersEditCreatecustomersImpl'
  }
  //'footer' implemented programatically from the body
});

//button of header of the body
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.customers.newcustomersave',
  classes: 'obObposPointOfSaleUiCustomersNewcustomersave',
  i18nLabel: 'OBPOS_LblSave',
  events: {
    onSaveCustomer: ''
  },
  handlers: {
    onDisableButton: 'disableButton'
  },
  tap: function() {
    var me = this;
    OB.info(
      'Time: ' +
        new Date() +
        '. Customer Save Button Pressed ( Status: ' +
        this.disabled +
        ') '
    );
    if (me.blocked) {
      OB.error(
        'Time: ' +
          new Date() +
          '. Customer Save button has been pressed 2 times and second execution is discarded '
      );
      return;
    } else {
      me.blocked = true;
      this.disableButton(this, {
        disabled: true
      });
      setTimeout(function() {
        me.blocked = false;
      }, 500);
    }
    this.doSaveCustomer({
      validations: true
    });
  },
  disableButton: function(inSender, inEvent) {
    this.setDisabled(inEvent.disabled);
  }
});

//Header of body
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.NewCustomer_footer',
  classes: 'obObposPointOfSaleUiCustomersNewCustomerFooter',
  components: [
    {
      classes: 'obObposPointOfSaleUiCustomersNewCustomerFooter-container1',
      components: [
        {
          classes:
            'obObposPointOfSaleUiCustomersNewCustomerFooter-container1-container1',
          components: [
            {
              kind: 'OB.OBPOSPointOfSale.UI.customers.cancelEdit',
              classes:
                'obObposPointOfSaleUiCustomersNewCustomerFooter-container1-container1-obObposPointOfSaleUiCustomersCancelEdit'
            }
          ]
        },
        {
          classes:
            'obObposPointOfSaleUiCustomersNewCustomerFooter-container1-container2',
          components: [
            {
              kind: 'OB.OBPOSPointOfSale.UI.customers.newcustomersave',
              isDefaultAction: true,
              classes:
                'obObposPointOfSaleUiCustomersNewCustomerFooter-container1-container2-obObposPointOfSaleUiCustomersNewcustomersave'
            }
          ]
        }
      ]
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl',
  kind: 'OB.OBPOSPointOfSale.UI.customers.edit_createcustomers',
  classes: 'obObposPointOfSaleUiCustomersEditCreatecustomersImpl',
  windowHeader: 'OB.OBPOSPointOfSale.UI.customers.NewCustomer_bodyheader',
  fieldGroups: [
    {
      groupName: 'OBPOS_FG_PersonalInformation',
      title: 'OBMOBC_Personal_Info',
      sectionName: 'personalInfo',
      sectionLableName: 'personalInfoLbl',
      sectionFieldsName: 'personalInfoFields',
      sectionFieldsLineName: 'personalInfoFieldsLine'
    },
    {
      groupName: 'OBPOS_FG_ContactInformation',
      title: 'OBPOS_Contact_Info',
      sectionName: 'contactInfo',
      sectionLableName: 'contactInfoLbl',
      sectionFieldsName: 'contactInfoFields',
      sectionFieldsLineName: 'contactInfoFieldsLine'
    },
    {
      groupName: 'OBPOS_FG_OthersInformation',
      title: 'OBPOS_Other_Info',
      sectionName: 'otherInfo',
      sectionLableName: 'otherInfoLbl',
      sectionFieldsName: 'otherInfoFields',
      sectionFieldsLineName: 'otherInfoFieldsLine'
    }
  ],
  windowFooter: 'OB.OBPOSPointOfSale.UI.customers.NewCustomer_footer',
  newAttributes: [
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'greeting',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-greeting',
      modelProperty: 'greetingId',
      modelPropertyText: 'greetingName',
      collectionName: 'greetingsList',
      //Default value for new lines
      defaultValue: function() {
        return undefined;
      },
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: 'name',
      //function to retrieve the data
      fetchDataFunction: function(args) {
        var me = this,
          data = new Backbone.Collection();
        _.each(
          OB.MobileApp.model.get('greetings'),
          function(greeting) {
            var greetingToAdd = new Backbone.Model({
              _identifier: greeting.name,
              name: greeting.name,
              id: greeting.id
            });
            data.push(greetingToAdd);
          },
          args
        );
        //When dataReadyFunction is executed the HTML component is not already rendered
        //adding timeout, the popup is shown and rendered so when data ready is executed
        //the element to be selected is ready and will work.
        //without setTimeout me.hashNode() is false
        //with setTimeout me.hashNode() is a DOM element
        setTimeout(function() {
          me.dataReadyFunction(data, args);
        }, 0);
      },
      i18nLabel: 'OBPOS_LblGreetings',
      fgSection: 'OBPOS_FG_PersonalInformation',
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowGreetings',
          true
        );
      }
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'firstName',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-firstName',
      modelProperty: 'firstName',
      isFirstFocus: true,
      i18nLabel: 'OBPOS_LblName',
      fgSection: 'OBPOS_FG_PersonalInformation',
      maxlength: 60,
      mandatory: true
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'lastName',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-lastName',
      modelProperty: 'lastName',
      isFirstFocus: true,
      i18nLabel: 'OBPOS_LblLastName',
      fgSection: 'OBPOS_FG_PersonalInformation',
      maxlength: 60
    },
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'customerCategory',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerCategory',
      modelProperty: 'businessPartnerCategory',
      //Required: property where the selected value will be get and where the value will be saved
      modelPropertyText: 'businessPartnerCategory_name',
      //optional: When saving, the property which will store the selected text
      collectionName: 'BPCategoryList',
      defaultValue: function() {
        return OB.MobileApp.model.get('terminal').defaultbp_bpcategory;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'id',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: '_identifier',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: async function(args) {
        var me = this;

        try {
          const dataBPCategory = await OB.App.MasterdataModels.BPCategory.orderedBy(
            'name'
          );
          me.dataReadyFunction(dataBPCategory, args);
        } catch (err) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPCategories'));
          me.dataReadyFunction(null, args);
        }
      },
      i18nLabel: 'OBPOS_BPCategory',
      fgSection: 'OBPOS_FG_OthersInformation',
      mandatory: true,
      displayLogic: function() {
        return OB.MobileApp.model.get('terminal').bp_showcategoryselector;
      }
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'customerTaxId',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerTaxId',
      modelProperty: 'taxID',
      i18nLabel: 'OBPOS_LblTaxId',
      fgSection: 'OBPOS_FG_PersonalInformation',
      displayLogic: function() {
        return OB.MobileApp.model.get('terminal').bp_showtaxid;
      },
      maxlength: 20
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'customerPhone',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerPhone',
      modelProperty: 'phone',
      i18nLabel: 'OBPOS_LblPhone',
      fgSection: 'OBPOS_FG_ContactInformation',
      maxlength: 40
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'alternativePhone',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-alternativePhone',
      modelProperty: 'alternativePhone',
      i18nLabel: 'OBPOS_LblAlternativePhone',
      fgSection: 'OBPOS_FG_ContactInformation',
      maxlength: 40
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'customerEmail',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerEmail',
      modelProperty: 'email',
      i18nLabel: 'OBPOS_LblEmail',
      fgSection: 'OBPOS_FG_ContactInformation',
      maxlength: 255
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'birthPlace',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-birthPlace',
      modelProperty: 'birthPlace',
      i18nLabel: 'OBPOS_LblBirthplace',
      fgSection: 'OBPOS_FG_PersonalInformation',
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowBirthplace',
          true
        );
      }
    },
    {
      kind: 'OB.UI.DatePicker',
      name: 'birthDay',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-birthDay',
      modelProperty: 'birthDay',
      i18nLabel: 'OBPOS_LblBirthdate',
      fgSection: 'OBPOS_FG_PersonalInformation',
      handlers: {
        onLoadValue: 'loadValue',
        onSaveChange: 'saveChange',
        onSetValue: 'valueSet',
        onRetrieveValues: 'retrieveValue'
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
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowBirthdate',
          true
        );
      },
      loadValue: function(inSender, inEvent) {
        if (inEvent.customer && inEvent.customer.get(this.modelProperty)) {
          this.setValue(new Date(inEvent.customer.get(this.modelProperty)));
        } else {
          this.setValue('');
        }
        if (this.formElement) {
          this.formElement.handleFormElementStyle();
        }
      },
      saveChange: function(inSender, inEvent) {
        var value = this.getValue();
        if (value) {
          inEvent.customer.set(this.modelProperty, value);
        } else {
          inEvent.customer.set(this.modelProperty, '');
        }
      }
    },
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'customerLanguage',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerLanguage',
      modelProperty: 'language',
      modelPropertyText: 'language_name',
      collectionName: 'languageList',
      defaultValue: function() {
        return OB.MobileApp.model.get('terminal').language_string;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'language',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: 'name',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: function(args) {
        var me = this,
          data = new Backbone.Collection();
        //This function must be called when the data is readyargs = "language";
        _.each(
          OB.MobileApp.model.get('language'),
          function(lg) {
            var languageToAdd = new Backbone.Model({
              _identifier: lg.language,
              language: lg.language,
              name: lg.name,
              id: lg.id
            });
            data.push(languageToAdd);
          },
          args
        );
        //When dataReadyFunction is executed the HTML component is not already rendered
        //adding timeout, the popup is shown and rendered so when data ready is executed
        //the element to be selected is ready and will work.
        //without setTimeout me.hashNode() is false
        //with setTimeout me.hashNode() is a DOM element
        setTimeout(function() {
          me.dataReadyFunction(data, args);
        }, 0);
      },
      i18nLabel: 'OBPOS_LblLanguage',
      fgSection: 'OBPOS_FG_PersonalInformation',
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowLanguage',
          true
        );
      }
    },
    {
      kind: 'OB.UI.CustomerTextProperty',
      name: 'comments',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-comments',
      modelProperty: 'comments',
      i18nLabel: 'OBPOS_LblComments',
      fgSection: 'OBPOS_FG_OthersInformation',
      maxlength: 40,
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowComments',
          true
        );
      }
    },
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'customerPriceList',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-customerPriceList',
      modelProperty: 'priceList',
      //Required: property where the selected value will be get and where the value will be saved
      modelPropertyText: 'priceListName',
      //optional: When saving, the property which will store the selected text
      collectionName: 'PriceListList',
      defaultValue: function() {
        return OB.MobileApp.model.get('pricelist').id;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'id',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: 'name',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: function(args) {
        var me = this,
          getPriceList;

        getPriceList = async function(callback) {
          let priceListData = new Backbone.Collection();
          try {
            const criteria = new OB.App.Class.Criteria().orderBy('name', 'asc');
            const priceList = await OB.App.MasterdataModels.PriceList.find(
              criteria.build()
            );
            if (priceList && priceList.length > 0) {
              priceList.forEach(function(p) {
                priceListData.add(OB.Dal.transform(OB.Model.PriceList, p));
              });
            }
            callback(priceListData);
          } catch (error) {
            OB.UTIL.showError(error);
            callback(priceListData);
          }
        };

        getPriceList(function(data) {
          data.add(
            OB.Dal.transform(OB.Model.PriceList, {
              id: OB.MobileApp.model.get('pricelist').id,
              name: OB.MobileApp.model.get('pricelist').name,
              priceIncludesTax: OB.MobileApp.model.get('pricelist')
                .priceIncludesTax,
              c_currency_id: OB.MobileApp.model.get('pricelist').currency
            }),
            {
              at: 0
            }
          );
          me.dataReadyFunction(data, args);
        });
      },
      i18nLabel: 'OBPOS_PriceList',
      fgSection: 'OBPOS_FG_OthersInformation',
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission('EnableMultiPriceList', true);
      }
    },
    {
      kind: 'OB.UI.CustomerCheckProperty',
      name: 'isCustomerConsent',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-isCustomerConsent',
      modelProperty: 'isCustomerConsent',
      fgSection: 'OBPOS_FG_OthersInformation',
      i18nLabel: 'OBPOS_CustomerConsent'
    },
    {
      kind: 'OB.UI.CustomerCheckCommercialAuth',
      name: 'commercialauth',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-commercialauth',
      modelProperty: 'commercialauth',
      i18nLabel: 'OBPOS_CommercialAuth',
      fgSection: 'OBPOS_FG_ContactInformation',
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowCommercialAuth',
          true
        );
      }
    },
    {
      kind: 'OB.UI.CustomerCheckComboProperty',
      name: 'contactpreferences',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-newAttributes-contactpreferences',
      modelProperty: 'contactpreferences',
      i18nLabel: 'OBPOS_ContactPreferences',
      fgSection: 'OBPOS_FG_ContactInformation',
      setEditedProperties: function(oldBp, editedBp) {
        editedBp.set('viasms', oldBp.get('viasms'));
        editedBp.set('viaemail', oldBp.get('viaemail'));
      },
      displayLogic: function() {
        return OB.MobileApp.model.hasPermission(
          'OBPOS_Cus360ShowContactPreferences',
          true
        );
      }
    }
  ],
  sameAddrCheckAttributes: [
    {
      kind: 'OB.UI.SwitchShippingInvoicingAddr',
      name: 'useSameAddrCheck',
      i18nLabel: 'OBPOS_SameAddrInfo',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-sameAddrCheckAttributes-useSameAddrCheck'
    }
  ],
  shipAddrAttributes: [
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'customerShipCountry',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-shipAddrAttributes-customerShipCountry',
      modelProperty: 'shipCountryId',
      modelPropertyText: 'shipCountryName',
      collectionName: 'CountryList',
      i18nLabel: 'OBPOS_LblCountry',
      mandatory: true,
      defaultValue: function() {
        return OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'id',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: '_identifier',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: async function(args) {
        var me = this;
        try {
          const dataCountry = await OB.App.MasterdataModels.Country.orderedBy(
            '_identifier'
          );
          me.dataReadyFunction(dataCountry, args);
        } catch (err) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingCountries'));
          me.dataReadyFunction(null, args);
        }
      },
      hideShow: function(inSender, inEvent) {
        if (inEvent.checked) {
          this.owner.owner.hide();
        } else {
          this.owner.owner.show();
        }
      }
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerPostalCode',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-shipAddrAttributes-customerPostalCode',
      modelProperty: 'shipPostalCode',
      i18nLabel: 'OBPOS_LblPostalCode',
      maxlength: 10,
      hideShow: function(inSender, inEvent) {
        if (inEvent.checked) {
          this.owner.owner.hide();
        } else {
          this.owner.owner.show();
        }
      }
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerCity',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-shipAddrAttributes-customerCity',
      modelProperty: 'shipCityName',
      i18nLabel: 'OBPOS_LblCity',
      maxlength: 60,
      hideShow: function(inSender, inEvent) {
        if (inEvent.checked) {
          this.owner.owner.hide();
        } else {
          this.owner.owner.show();
        }
      }
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerLocName',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-shipAddrAttributes-customerLocName',
      modelProperty: 'shipLocName',
      i18nLabel: 'OBPOS_LblAddress',
      hasAddrIcons: true,
      maxlength: 60,
      mandatory: true,
      hideShow: function(inSender, inEvent) {
        if (inEvent.checked) {
          this.owner.owner.hide();
        } else {
          this.owner.owner.show();
        }
      }
    }
  ],
  invAddrAttributes: [
    {
      kind: 'OB.UI.CustomerComboProperty',
      name: 'customerCountry',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-invAddrAttributes-customerCountry',
      modelProperty: 'countryId',
      modelPropertyText: 'countryName',
      collectionName: 'CountryList',
      i18nLabel: 'OBPOS_LblCountry',
      mandatory: true,
      defaultValue: function() {
        return OB.MobileApp.model.get('terminal').defaultbp_bpcountry;
      },
      //Default value for new lines
      retrievedPropertyForValue: 'id',
      //property of the retrieved model to get the value of the combo item
      retrievedPropertyForText: '_identifier',
      //property of the retrieved model to get the text of the combo item
      //function to retrieve the data
      fetchDataFunction: async function(args) {
        var me = this;
        try {
          const dataCountry = await OB.App.MasterdataModels.Country.orderedBy(
            '_identifier'
          );
          me.dataReadyFunction(dataCountry, args);
        } catch (err) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingCountries'));
          me.dataReadyFunction(null, args);
        }
      }
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerInvPostalCode',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-invAddrAttributes-customerInvPostalCode',
      modelProperty: 'postalCode',
      i18nLabel: 'OBPOS_LblPostalCode',
      maxlength: 10
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerInvCity',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-invAddrAttributes-customerInvCity',
      modelProperty: 'cityName',
      i18nLabel: 'OBPOS_LblCity',
      maxlength: 60
    },
    {
      kind: 'OB.UI.CustomerTextPropertyAddr',
      name: 'customerInvLocName',
      classes:
        'obObposPointOfSaleUiCustomersEditCreatecustomersImpl-invAddrAttributes-customerInvLocName',
      modelProperty: 'locName',
      i18nLabel: 'OBPOS_LblAddress',
      maxlength: 60,
      mandatory: true,
      hasAddrIcons: true
    }
  ],
  statisticsAttributes: [
    {
      kind: 'OB.UI.CustomerStatisticsTextProperty',
      name: 'recency',
      classes: 'obObPosPointOfSaleUiCustomersEditCustomersImpl-recency',
      i18nLabel: 'OBPOS_LblRecency',
      textProperty: 'recencyMsg',
      readOnly: true
    },
    {
      kind: 'OB.UI.CustomerStatisticsTextProperty',
      name: 'frequency',
      classes: 'obObPosPointOfSaleUiCustomersEditCustomersImpl-frequency',
      i18nLabel: 'OBPOS_LblFrequency',
      textProperty: 'frequencyMsg',
      readOnly: true
    },
    {
      kind: 'OB.UI.CustomerStatisticsTextProperty',
      name: 'monetaryValue',
      classes: 'obObPosPointOfSaleUiCustomersEditCustomersImpl-monetaryValue',
      i18nLabel: 'OBPOS_LblMonetaryVal',
      textProperty: 'monetaryValMsg',
      readOnly: true
    },
    {
      kind: 'OB.UI.CustomerStatisticsTextProperty',
      name: 'averageCart',
      classes: 'obObPosPointOfSaleUiCustomersEditCustomersImpl-averageCart',
      i18nLabel: 'OBPOS_LblAvgCart',
      textProperty: 'averageBasketMsg',
      readOnly: true
    }
  ]
});
