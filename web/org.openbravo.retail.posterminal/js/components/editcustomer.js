/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  name: 'OB.UI.CustomerPropertyLine',
  components: [{
    name: 'labelLine',
    style: 'font-size: 15px; color: black; text-align: right; border: 1px solid #FFFFFF; background-color: #E2E2E2; width: 10%; height: 28px; padding: 12px 5px 1px 0; float: left;'
  }, {
    style: 'border: 1px solid #FFFFFF; float: left; width: 88%;',
    name: 'newAttribute'
  }, {
    style: 'clear: both'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.newAttribute.createComponent(this.newAttribute);
    this.$.labelLine.content = this.newAttribute.label;
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
  loadValue: function (sender, event) {
    if (event.customer !== undefined) {
      if (event.customer.get(this.modelProperty) !== undefined) {
        this.setValue(event.customer.get(this.modelProperty));
      }
    } else {
      this.setValue('');
    }
  },
  saveChange: function (sender, event) {
    event.customer.set(this.modelProperty, this.getValue());
  },
  initComponents: function () {
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.NewCustomerWindowHeaderSave',
  style: 'width: 100px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblSave'),
  events: {
    onSaveCustomer: ''
  },
  tap: function () {
    this.doSaveCustomer();
  }
});

enyo.kind({
  name: 'OB.UI.NewCustomerWindowHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    style: 'padding: 10px 500px 10px 500px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.NewCustomerWindowHeaderSave'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          classes: 'btnlink-gray btnlink btnlink-small',
          content: OB.I18N.getLabel('OBPOS_LblCancel'),
          handlers: {
            onSetCustomer: 'setCustomer'
          },
          setCustomer: function (sender, event) {
            this.customer = event.customer;
            this.callerWindow = event.callerWindow;
          },
          tap: function () {
            this.model.get('subWindowManager').set('currentWindow', {
              name: this.callerWindow,
              params: {
                businessPartner: this.customer
              }
            });
          },
          init: function (model) {
            this.model = model;
          }
        }]
      }]
    }]
  }],
  searchAction: function () {
    this.doSearchAction({
      bpName: this.$.filterText.getValue()
    });
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.EditCustomerWindowHeaderAssign',
  style: 'width: 150px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblAssignToTicket'),
  handlers: {
    onSetCustomer: 'setCustomer'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  setCustomer: function (sender, event) {
    this.customer = event.customer;
  },
  tap: function () {
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'mainSubWindow',
      params: []
    });
    this.doChangeBusinessPartner({
      businessPartner: this.customer
    });
  },
  init: function (model) {
    this.model = model;
  }
});

enyo.kind({
  name: 'OB.UI.EditCustomerWindowHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    tag: 'div',
    style: 'padding: 9px 15px;',
    components: [{
      tag: 'a',
      classes: 'close',
      components: [{
        tag: 'span',
        style: 'font-size: 150%',
        allowHtml: true,
        content: '&times;'
      }],
      tap: function () {
        this.model.get('subWindowManager').set('currentWindow', {
          name: 'subWindow_customers',
          params: []
        });
      },
      init: function (model) {
        this.model = model;
      }
    }]
  }, {
    style: 'padding: 10px 500px 10px 500px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          handlers: {
            onSetCustomer: 'setCustomer'
          },
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-orange btnlink btnlink-small',
          content: OB.I18N.getLabel('OBPOS_LblEdit'),
          setCustomer: function (sender, event) {
            this.customer = event.customer;
          },
          tap: function () {
            this.model.get('subWindowManager').set('currentWindow', {
              name: 'subWindow_new_customer',
              params: {
                businessPartner: this.customer,
                callerWindow: 'subWindow_edit_customer'
              }
            });
          },
          init: function (model) {
            this.model = model;
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.EditCustomerWindowHeaderAssign'
        }]
      }]
    }]
  }],
  searchAction: function () {
    this.doSearchAction({
      bpName: this.$.filterText.getValue()
    });
  }
});

/*New Customer Window*/
enyo.kind({
  name: 'OB.UI.NewCustomerWindow',
  handlers: {
    onSetCustomer: 'setCustomer',
    onSaveCustomer: 'saveCustomer'
  },
  events: {},
  components: [{
    name: 'headerButtons'
  }, {
    name: 'customerAttributes'
  }],
  setCustomer: function (sender, event) {
    this.customer = event.customer;
    this.callerWindow = event.callerWindow;
    this.waterfall('onLoadValue', {
      customer: this.customer
    });
  },
  saveCustomer: function (sender, event) {
    var me = this,
        subWindowManager = this.model.get('subWindowManager');

    function getCustomerValues(params) {
      me.waterfall('onSaveChange', {
        customer: params.customer
      });
    }

    function goToEditWindow(params) {
      subWindowManager.set('currentWindow', {
        name: 'subWindow_edit_customer',
        params: {
          businessPartner: params.customer
        }
      });
    }

    if (this.customer === undefined) {
      this.model.get('customer').newCustomer();
      this.waterfall('onSaveChange', {
        customer: this.model.get('customer')
      });
      this.model.get('customer').saveCustomer();
      goToEditWindow({
        customer: this.model.get('customer')
      });
    } else {
      this.model.get('customer').loadById(this.customer.get('id'), function (customer) {
        getCustomerValues({
          customer: customer
        });
        customer.saveCustomer();
        goToEditWindow({
          customer: customer
        });
      });
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.headerButtons.createComponent({
      kind: this.windowHeader
    });
    this.attributeContainer = this.$.customerAttributes;
    enyo.forEach(this.newAttributes, function (natt) {
      this.$.customerAttributes.createComponent({
        kind: 'OB.UI.CustomerPropertyLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      });
    }, this);
  },
  init: function (model) {
    this.model = model;
  }
});