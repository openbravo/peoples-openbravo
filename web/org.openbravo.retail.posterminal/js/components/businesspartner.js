/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB, _ */


enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartner',
  classes: 'btnlink-gray',
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
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
    if (!this.isEnabled) {
      this.removeClass('btnlink');
      this.addClass('btnbp');
    } else {
      this.removeClass('btnbp');
      this.addClass('btnlink');
    }
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
        popup: 'modalcustomer',
        args: {
          target: 'order'
        }
      });
    }
  },
  initComponents: function () {
    return this;
  },
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
      this.setDisabled(false);
      this.removeClass('disabled');
      this.disabled = false;
      return;
    }
    this.setDisabled(true);
    this.addClass('disabled');
    this.disabled = true;
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
      this.setDisabled(false);
      this.removeClass('disabled');
      this.disabled = false;
      return;
    }
    this.setDisabled(true);
    this.addClass('disabled');
    this.disabled = true;
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
        style: 'display: table-cell; width: 30%;',
        components: [{
          kind: 'OB.UI.List',
          name: 'customerFilterColumn',
          classes: 'combo',
          style: 'width: 95%',
          handlers: {
            onchange: 'changeColumn'
          },
          renderLine: enyo.kind({
            kind: 'enyo.Option',
            initComponents: function () {
              this.inherited(arguments);
              this.setValue(this.model.get('id'));
              this.setContent(this.model.get('name'));
            }
          }),
          renderEmpty: 'enyo.Control',
          changeColumn: function () {
            this.owner.doClearAction();
          },
          initComponents: function () {
            var columns = [];
            _.each(OB.Model.BPartnerFilter.getProperties(), function (prop) {
              if (prop.filter) {
                columns.push({
                  id: prop.column,
                  name: OB.I18N.getLabel(prop.caption)
                });
              }
            });
            this.setCollection(new Backbone.Collection());
            this.getCollection().reset(columns);
          }
        }]
      }, {
        style: 'display: table-cell; width: 70%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'customerFilterText',
          style: 'width: 100%',
          skipAutoFilterPref: 'OBPOS_remote.customer'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'OB.UI.Bp.Modal.search',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 40px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 40px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }]
  }, {
    style: 'padding: 10px;',
    showing: true,
    handlers: {
      onSetShow: 'setShow'
    },
    setShow: function (inSender, inEvent) {
      this.setShowing(inEvent.visibility);
    },
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
    this.$.customerFilterText.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    this.doSearchAction({
      filters: [{
        column: this.$.customerFilterColumn.getValue(),
        text: this.$.customerFilterText.getValue()
      }]
    });
    return true;
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPDetailsContextMenuItem',
  i18NLabel: 'OBPOS_BPViewDetails',
  selectItem: function (bpartner) {
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      OB.MobileApp.view.$.containerWindow.getRoot().model.attributes.subWindowManager.set('currentWindow', {
        name: 'customerView',
        params: {
          businessPartner: bp,
          navigateOnClose: 'mainSubWindow'
        }
      });
    });
    return true;
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPEditContextMenuItem',
  i18NLabel: 'OBPOS_BPEdit',
  selectItem: function (bpartner) {
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      OB.MobileApp.view.$.containerWindow.getRoot().model.attributes.subWindowManager.set('currentWindow', {
        name: 'customerCreateAndEdit',
        params: {
          businessPartner: bp,
          navigateOnClose: 'mainSubWindow'
        }
      });
    });
    return true;
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenu',
  name: 'OB.UI.BusinessPartnerContextMenu',
  initComponents: function () {
    this.inherited(arguments);
    var menuOptions = [],
        extraOptions = OB.MobileApp.model.get('extraBPContextMenuOptions') || [];

    menuOptions.push({
      kind: 'OB.UI.BPDetailsContextMenuItem',
      permission: 'OBPOS_receipt.customers'
    }, {
      kind: 'OB.UI.BPEditContextMenuItem',
      permission: 'OBPOS_retail.editCustomers'
    });

    menuOptions = menuOptions.concat(extraOptions);
    this.$.menu.setItems(menuOptions);
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLine',
  kind: 'OB.UI.listItemButton',
  components: [{
    name: 'line',
    style: 'line-height: 23px; width: 100%',
    components: [{
      name: 'textInfo',
      style: 'float: left; ',
      components: [{
        style: 'display: inline-block;',
        name: 'identifier'
      }, {
        style: 'display: inline-block; font-weight: bold; color: red; padding-left:5px;',
        name: 'onHold'
      }, {
        style: 'clear: both;'
      }]
    }, {
      kind: 'OB.UI.BusinessPartnerContextMenu',
      name: 'btnContextMenu',
      style: 'float: right'
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
    var bPartner = this.owner.owner.owner.bPartner;
    if (bPartner && bPartner.get('id') === this.model.get('id')) {
      this.applyStyle('background-color', '#fbf6d1');
    }
    // Context menu
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.setModel(this.model);
    }
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
          scrollAreaMaxHeight: '350px',
          renderHeader: 'OB.UI.ModalBpScrollableHeader',
          renderLine: 'OB.UI.ListBpsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }, {
          name: 'renderLoading',
          style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this;

    if (OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers')) {
      this.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.setDisabled(false);
    }

    this.$.stBPAssignToReceipt.$.tempty.hide();
    this.$.stBPAssignToReceipt.$.tbody.hide();
    this.$.stBPAssignToReceipt.$.tlimit.hide();
    this.$.renderLoading.show();

    function hasLocationInFilter() {
      return _.some(inEvent.filters, function (flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
          return col.column === flt.column;
        });
        return column && column.location;
      });
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPs(dataBps) {
      me.$.renderLoading.hide();
      if (dataBps && dataBps.length > 0) {
        _.each(dataBps.models, function (bp) {
          var _identifier = bp.get('bpName'),
              location = hasLocationInFilter();
          if (location) {
            _identifier += ' / ' + bp.get('locName');
          }
          _.each(inEvent.filters, function (flt, index) {
            if (flt.column !== 'bp.name' && flt.column !== 'loc.name') {
              var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
                return col.column === flt.column;
              });
              if (column) {
                _identifier += ' / ' + (bp.get(column.name) ? bp.get(column.name) : '');
              }
            }
          });
          bp.set('_identifier', _identifier);
        });
        me.bpsList.reset(dataBps.models);
        me.$.stBPAssignToReceipt.$.tbody.show();
      } else {
        me.bpsList.reset();
        me.$.stBPAssignToReceipt.$.tempty.show();
      }
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var criteria = {};
      criteria.remoteFilters = [];
      _.each(inEvent.filters, function (flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
          return col.column === flt.column;
        });
        if (column) {
          criteria.remoteFilters.push({
            columns: [column.name],
            operator: OB.Dal.CONTAINS,
            value: OB.UTIL.unAccent(flt.text),
            location: column.location
          });
        }
      });
      if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
        criteria._limit = OB.DEC.abs(OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true));
      }
      OB.Dal.find(OB.Model.BPartnerFilter, criteria, successCallbackBPs, errorCallback, this);
    } else {
      var index = 0,
          params = [],
          location = hasLocationInFilter(),
          select = 'select ',
          orderby = ' order by bp.name';

      _.each(OB.Model.BPartnerFilter.getProperties(), function (prop) {
        if (prop.column !== '_filter' && prop.column !== '_idx' && prop.column !== '_identifier') {
          if (index !== 0) {
            select += ', ';
          }
          if (prop.column === 'id') {
            select += (location ? 'loc.c_bpartner_location_id' : 'bp.c_bpartner_id') + ' as ' + prop.name;
          } else {
            if (!location && prop.location) {
              select += "'' as " + prop.name;
            } else {
              select += prop.column + ' as ' + prop.name;
            }
          }
          index++;
        }
      });
      select += ' from c_bpartner bp ' + (location ? 'left join c_bpartner_location loc on bp.c_bpartner_id = loc.c_bpartner_id ' : '');

      if (inEvent.filters.length > 0) {
        select += 'where ';
        _.each(inEvent.filters, function (flt, index) {
          if (index !== 0) {
            select += ' and ';
          }
          select += flt.column + ' like ? ';
          params.push('%' + OB.UTIL.unAccent(flt.text) + '%');
          if (flt.orderby) {
            orderby = ' order by ' + flt.column;
          }
        });
      }
      OB.Dal.query(OB.Model.BPartnerFilter, select + orderby, params, successCallbackBPs, errorCallback, null, null, OB.Model.BPartnerFilter.prototype.dataLimit);
    }
    return true;
  },
  bpsList: null,
  init: function (model) {
    this.bpsList = new Backbone.Collection();
    this.$.stBPAssignToReceipt.setCollection(this.bpsList);
    this.bpsList.on('click', function (model) {
      if (model.get('customerBlocking') && model.get('salesOrderBlocking')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerOnHold', [model.get('_identifier')]));
      } else if (!model.get('ignoreSetBP')) {
        var me = this;
        OB.Dal.get(OB.Model.BusinessPartner, model.get('bpartnerId'), function (bp) {
          me.doChangeBusinessPartner({
            businessPartner: bp,
            target: me.owner.owner.args.target
          });
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
    if (_.isUndefined(this.args.visibilityButtons)) {
      this.args.visibilityButtons = true;
    }
    this.waterfall('onSetShow', {
      visibility: this.args.visibilityButtons
    });
    this.bubble('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    if (OB.MobileApp.model.hasPermission('OBPOS_retail.disableNewBPButton', true)) {
      this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.setDisabled(true);
    }
    if (this.args.businessPartner) {
      this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.searchAction();
      this.$.body.$.listBps.$.stBPAssignToReceipt.bPartner = this.args.businessPartner;
    } else {
      this.$.body.$.listBps.$.stBPAssignToReceipt.bPartner = null;
    }
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