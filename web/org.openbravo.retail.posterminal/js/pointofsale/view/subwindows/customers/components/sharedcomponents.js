/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
  name: 'OB.UI.CustomerComboProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onSaveChange: 'saveChange'
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
    onSaveCustomer: 'saveCustomer'
  },
  events: {},
  components: [{
    name: 'bodyheader'
  }, {
    name: 'customerAttributes'
  }],
  setCustomer: function (inSender, inEvent) {
    this.customer = inEvent.customer;
    this.waterfall('onLoadValue', {
      customer: this.customer
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
      this.model.get('customer').saveCustomer();
      goToViewWindow(sw, {
        customer: this.model.get('customer')
      });
    } else {
      this.model.get('customer').loadById(this.customer.get('id'), function (customer) {
        getCustomerValues({
          customer: customer
        });
        customer.saveCustomer();
        goToViewWindow(sw, {
          customer: customer
        });
      });
    }
  },
  initComponents: function () {
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
        this.$.customerAttributes.createComponent({
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