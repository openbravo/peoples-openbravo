/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */


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
  initComponents: function() {},
  renderCustomer: function(newCustomer) {
    this.setContent(newCustomer);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(this.order.get('bp').get('_identifier'));
    } else {
      this.renderCustomer('');
    }

    this.order.on('change:bp', function(model) {
      if (model.get('bp')) {
        this.renderCustomer(model.get('bp').get('_identifier'));
      } else {
        this.renderCustomer('');
      }
    }, this);
  }
});

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
  init: function() {}
});

enyo.kind({
  name: 'OB.UI.ListBps',
  classes: 'row-fluid',
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
              components: [{kind: 'OB.UI.buttonFilterBps',
                ontap: 'searchUsingBpsFilter'}]
            }, {
              style: 'display: table-cell;',
              components: [{kind: 'OB.UI.buttonClearFilterBps',
              ontap: 'clearBpsFilter'  
              }]
            }]
          }]
        }]
      }]
    }, {
      components: [{
        name: 'bpslistitemprinter',
        kind: 'OB.UI.Table',
        renderLine: 'OB.UI.ListBpsLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],
  clearBpsFilter: function(inSender, inEvent) {
    this.bpsList.reset();
    this.$.filterText.setValue('');
    return true;
  },
  searchUsingBpsFilter: function(inSender, inEvent) {
    var me = this, filter = this.$.filterText.getValue();

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
  init: function() {
    this.bpsList = new Backbone.Collection();
    this.$.bpslistitemprinter.setCollection(this.bpsList);
    this.bpsList.on('click', function(model) {
      this.clickedBp = model;
      this.doChangeBusinessPartner();
    }, this)
  }
});

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
  create: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.address.setContent(this.model.get('locName'));
  }
});

enyo.kind({
  name: 'OB.UI.buttonClearFilterBps',
  kind: 'OB.UI.Button',
  style: 'width: 100px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  components: [{
    classes: 'btn-icon-small btn-icon-clear'
  }, {
    tag: 'span'
  }]
});

enyo.kind({
  name: 'OB.UI.buttonFilterBps',
  kind: 'OB.UI.Button',
  style: 'width: 100px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink-gray btnlink btnlink-small',
  components: [{
    classes: 'btn-icon-small btn-icon-search'
  }, {
    tag: 'span'
  }]
});