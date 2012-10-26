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
  name: 'OB.UI.ModalConfigurationRequiredForCreateCustomers',
  kind: 'OB.UI.ModalInfo',
  header: OB.I18N.getLabel('OBPOS_configurationRequired'),
  bodyContent: {
    tag: 'div',
    content: OB.I18N.getLabel('OBPOS_configurationNeededToCreateCustomers')
  },
  myId: 'modalConfigurationRequiredForCreateNewCustomers'
});


enyo.kind({
  name: 'OB.UI.ModalCustomerScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  style: 'border-bottom: 10px solid #ff0000;',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  components: [{
    style: 'padding: 10px; 10px; 0px; 10px;',
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
  }],
  clearAction: function() {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function() {
    this.doSearchAction({
      bpName: this.$.filterText.getValue(),
      operator: OB.Dal.CONTAINS
    });
  }
});

/*items of collection Customer*/
enyo.kind({
  name: 'OB.UI.ListCustomersLine',
  kind: 'OB.UI.SelectButton',
  classes: 'btnselect-customer',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      style: 'float: left; font-weight: bold;',
      name: 'identifier'
    }, {
      style: 'float: left;',
      name: 'address'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier') + ' / ');
    this.$.address.setContent(this.model.get('locName'));
  }
});

/*Search Customer Button*/
enyo.kind({
  name: 'OB.UI.SearchCustomerButton',
  kind: 'OB.UI.Button',
  events: {
    onSearchAction: ''
  },
  classes: 'btnlink-left-toolbar',
  searchAction: function(params) {
    this.doSearchAction({
      bpName: params.initial,
      operator: params.operator
    });
  }
});

/*New Customer Button*/
enyo.kind({
  name: 'OB.UI.NewCustomerButton',
  kind: 'OB.UI.Button',
  events: {
    onChangeSubWindow: ''
  },
  classes: 'btnlink-left-toolbar',
  tap: function() {
    if (OB.POS.modelterminal.get('terminal').defaultbp_paymentmethod !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_bpcategory !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_paymentterm !== null && 
        OB.POS.modelterminal.get('terminal').defaultbp_invoiceterm !== null &&
        OB.POS.modelterminal.get('terminal').defaultbp_bpcountry !== null &&
        OB.POS.modelterminal.get('terminal').defaultbp_bporg !== null) {
      this.doChangeSubWindow({
        newWindow: {
          name: 'subWindow_new_customer',
          params: {
            callerWindow: 'subWindow_customers'
          }
        }
      });
    } else {
      $('#modalConfigurationRequiredForCreateNewCustomers').modal("show");
    }
  }
});


/* Buttons Left bar*/
enyo.kind({
  name: 'OB.UI.CustomerLeftBar',
  components: [{
    kind: 'OB.UI.NewCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblNew')
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblAC'),
    tap: function() {
      this.searchAction({
        initial: 'A,B,C',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblDF'),
    tap: function() {
      this.searchAction({
        initial: 'D,E,F',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblGI'),
    tap: function() {
      this.searchAction({
        initial: 'G,H,I',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblJL'),
    tap: function() {
      this.searchAction({
        initial: 'J,K,L',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblMO'),
    tap: function() {
      this.searchAction({
        initial: 'M,N,O',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblPR'),
    tap: function() {
      this.searchAction({
        initial: 'P,Q,R',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblSV'),
    tap: function() {
      this.searchAction({
        initial: 'S,T,U,V',
        operator: OB.Dal.STARTSWITH
      });
    }
  }, {
    kind: 'OB.UI.SearchCustomerButton',
    content: OB.I18N.getLabel('OBPOS_LblWZ'),
    tap: function() {
      this.searchAction({
        initial: 'W,X,Y,Z',
        operator: OB.Dal.STARTSWITH
      });
    }
  }]
});

/*scrollable table (body of customer)*/
enyo.kind({
  name: 'OB.UI.ListCustomers',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeBusinessPartner: '',
    onChangeSubWindow: ''
  },
  components: [{
    style: 'width: 10%; float: left; text-align: center; padding-top: 10px',
    components: [{
      kind: 'OB.UI.CustomerLeftBar'
    }]
  }, {
    style: 'width: 90%; float: left;',
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'bpslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '800px',
          renderHeader: 'OB.UI.ModalCustomerScrollableHeader',
          renderLine: 'OB.UI.ListCustomersLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }, {
    style: 'clear: both'
  }],
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    var me = this,
        filter = inEvent.bpName,
        splitFilter = filter.split(","),
        splitFilterLength = splitFilter.length,
        _operator = inEvent.operator,
        i,
        criteria = {};

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPs(dataBps, args) {
      if (args === 0) {
        me.bpsList.reset();
      }
      if (dataBps && dataBps.length > 0) {
        me.bpsList.add(dataBps.models);
      }
    }


    if (filter && filter !== '') {
      for (i = 0; i < splitFilter.length; i++) {
        criteria._identifier = {
          operator: _operator,
          value: splitFilter[i]
        };
        OB.Dal.find(OB.Model.BusinessPartner, criteria, successCallbackBPs, errorCallback, i);
      }
    } else {
      OB.Dal.find(OB.Model.BusinessPartner, criteria, successCallbackBPs, errorCallback, 0);
    }


    return true;
  },
  bpsList: null,
  init: function() {
    this.bpsList = new Backbone.Collection();
    this.$.bpslistitemprinter.setCollection(this.bpsList);
    this.bpsList.on('click', function(model) {
      this.doChangeSubWindow({
        newWindow: {
          name: 'subWindow_edit_customer',
          params: {
            businessPartner: model
          }
        }
      });
    }, this);
  }
});