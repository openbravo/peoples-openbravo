/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB */


enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartner',
  classes: 'btnlink btnlink-small btnlink-gray',
  style: 'float: left; text-overflow:ellipsis; white-space: nowrap; overflow: hidden;',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  handlers: {
    onBPSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function (inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  tap: function () {
    var qty = 0;
    enyo.forEach(this.order.get('lines').models, function (l) {
      if (l.get('originalOrderLineId')) {
        qty = qty + 1;
        return;
      }
    });
    if (qty !== 0) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_Cannot_Change_BPartner'));
      return;
    }
    
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomer'
      });
    }
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
        if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
          if (OB.MobileApp.model.hasPermission('OBPOS_retail.restricttaxidinvoice', true)) {
            if (!model.get('bp').get('taxID')) {
              if (OB.MobileApp.model.get('terminal').terminalType.generateInvoice) {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
              } else {
                OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
              }
              model.set('generateInvoice', false);
            } else {
              model.set('generateInvoice', OB.MobileApp.model.get('terminal').terminalType.generateInvoice);
            }
          }
        } else {
          model.set('generateInvoice', false);
        }
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
    onChangeSubWindow: '',
    onHideThisPopup: ''
  },
  disabled: false,
  style: 'width: 170px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblNewCustomer',
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function (inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  tap: function (model) {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.doChangeSubWindow({
      newWindow: {
        name: 'customerCreateAndEdit',
        params: {
          navigateOnClose: 'mainSubWindow'
        }
      }
    });
  },
  putDisabled: function (status) {
    if (status === false) {
      this.disabled = false;
      this.setDisabled(false);
      this.removeClass('disabled');
      return;
    } else {
      this.disabled = true;
      this.setDisabled();
      this.addClass('disabled');
    }
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.AdvancedSearchCustomerWindowButton',
  style: 'margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblAdvancedSearch',
  disabled: false,
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function (inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerAdvancedSearch',
      params: {
        caller: 'mainSubWindow'
      }
    });
  },
  putDisabled: function (status) {
    if (status === false) {
      this.disabled = false;
      this.setDisabled(false);
      this.removeClass('disabled');
      return;
    } else {
      this.disabled = true;
      this.setDisabled();
      this.addClass('disabled');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_receipt.customers'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'filterText',
          style: 'width: 100%'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'OB.UI.Bp.Modal.search',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
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
          kind: 'OB.UI.NewCustomerWindowButton',
          name: 'newAction'
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
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLine',
  kind: 'OB.UI.listItemButton',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      style: 'display: inline-block;',
      name: 'identifier'
    }, {
      style: 'display: inline-block; font-weight: bold; color: red; padding-left:5px;',
      name: 'onHold'
    }, {
      style: 'clear: left; color: #888888',
      name: 'address'
    }, {
      style: 'clear: both;'
    }]
  }],
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  create: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    if (this.model.get('customerBlocking') && this.model.get('salesOrderBlocking')) {
      this.$.onHold.setContent('(' + OB.I18N.getLabel('OBPOS_OnHold') + ')');
    }
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
          name: 'stBPAssignToReceipt',
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
        me.bpsList.reset();
      }
    }

    var criteria = {};
    if (filter && filter !== '') {
      criteria._filter = {
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
    this.$.stBPAssignToReceipt.setCollection(this.bpsList);
    this.bpsList.on('click', function (model) {
      if (model.get('customerBlocking') && model.get('salesOrderBlocking')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerOnHold', [model.get('_identifier')]));
      } else {
        this.doChangeBusinessPartner({
          businessPartner: model
        });
      }
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBusinessPartners',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnShow: function () {
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    return true;
  },

  executeOnHide: function () {
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignCustomer',
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