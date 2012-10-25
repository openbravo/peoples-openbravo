/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone */


enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartner',
  classes: 'btnlink btnlink-small btnlink-gray',
  published: {
    order: null
  },
  attributes: {
    'data-toggle': 'modal',
    'href': '#modalcustomer'
  },
  initComponents: function () {},
  renderCustomer: function (newCustomer) {
    this.setContent(newCustomer);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(this.order.get('bp').get('_identifier'));
    } else {
      this.renderCustomer('');
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderCustomer(model.get('bp').get('_identifier'));
      } else {
        this.renderCustomer('');
      }
    }, this);
  }
});

/*Modal*/


/*header of scrollable table*/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.NewCustomerWindowButton',
  events: {
    onChangeSubWindow: ''
  },
  style: 'width: 150px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblNewCustomer'),
  attributes: {
    'data-dismiss': 'modal'
  },
  handlers: {
    onSetModel: 'setModel'
  },
  setModel: function (sender, event) {
    this.model = event.model;
  },
  tap: function (model) {
    this.doChangeSubWindow({
      newWindow: {
        name: 'customerCreateAndEdit'
      }
    });
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.AdvancedSearchCustomerWindowButton',
  style: 'width: 170px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  content: OB.I18N.getLabel('OBPOS_LblAdvancedSearch'),
  attributes: {
    'data-dismiss': 'modal'
  },
  handlers: {
    onSetModel: 'setModel'
  },
  setModel: function (sender, event) {
    this.model = event.model;
  },
  tap: function () {
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerAdvancedSearch',
      params: {
        caller: 'mainSubWindow'
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'enyo.Input',
          type: 'text',
          style: 'width:100%',
          classes: 'input',
          name: 'filterText',
          onchange: 'searchUsingBpsFilter',
          attributes: {
            'x-webkit-speech': 'x-webkit-speech'
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-gray btnlink btnlink-small',
          components: [{
            classes: 'btn-icon-small btn-icon-search'
          }, {
            tag: 'span'
          }],
          ontap: 'searchAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          classes: 'btnlink-yellow btnlink btnlink-small',
          components: [{
            classes: 'btn-icon-small btn-icon-clear'
          }, {
            tag: 'span'
          }],
          ontap: 'clearAction'
        }]
      }]
    }]
  }, {
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.NewCustomerWindowButton'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.AdvancedSearchCustomerWindowButton'
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    this.doSearchAction({
      bpName: this.$.filterText.getValue()
    });
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLine',
  kind: 'OB.UI.SelectButton',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      name: 'identifier'
    }, {
      style: 'color: #888888',
      name: 'address'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.address.setContent(this.model.get('locName'));
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBps',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;',
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'bpslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalBpScrollableHeader',
          renderLine: 'OB.UI.ListBpsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        filter = inEvent.bpName;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPs(dataBps) {
      if (dataBps && dataBps.length > 0) {
        me.bpsList.reset(dataBps.models);
      } else {
        me.bps.reset();
      }
    }

    var criteria = {};
    if (filter && filter !== '') {
      criteria._identifier = {
        operator: OB.Dal.CONTAINS,
        value: filter
      };
    }

    OB.Dal.find(OB.Model.BusinessPartner, criteria, successCallbackBPs, errorCallback);
    return true;
  },
  bpsList: null,
  init: function (model) {
    this.bpsList = new Backbone.Collection();
    this.$.bpslistitemprinter.setCollection(this.bpsList);
    this.bpsList.on('click', function (model) {
      this.doChangeBusinessPartner({
        businessPartner: model
      });
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBusinessPartners',
  myId: 'modalcustomer',
  kind: 'OB.UI.Modal',
  modalClass: 'modal',
  headerClass: 'modal-header',
  bodyClass: 'modal-header',
  header: OB.I18N.getLabel('OBPOS_LblAssignCustomer'),
  body: {
    kind: 'OB.UI.ListBps'
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});