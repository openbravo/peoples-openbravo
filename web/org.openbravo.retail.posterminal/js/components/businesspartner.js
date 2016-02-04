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
  name: 'OB.UI.AdvancedFilterWindowButton',
  style: 'margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblAdvancedFilter',
  disabled: false,
  handlers: {
    onNewBPDisabled: 'doDisableNewBP'
  },
  doDisableNewBP: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  events: {
    onShowPopup: '',
    onSearchAction: '',
    onHideBPSelector: '',
    onShowBPSelector: ''
  },
  tap: function () {
    if (this.disabled) {
      return true;
    }
    var me = this;
    this.doHideBPSelector();
    this.doShowPopup({
      popup: 'modalAdvancedFilterBP',
      args: {
        lastFilters: this.owner.$.filterSelector.lastFilters,
        callback: function (result) {
          me.doShowBPSelector();
          if (result) {
            me.doSearchAction({
              filters: result.filters,
              orderby: result.orderby,
              advanced: true
            });
          }
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
  },
  initComponents: function () {
    this.inherited(arguments);
    this.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.customer_advanced_filters', true));
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    style: 'padding: 10px;',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.BPartnerFilter.getProperties()  
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
          kind: 'OB.UI.AdvancedFilterWindowButton'
        }]
      }]
    }]
  }]
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
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPAddressContextMenuItem',
  i18NLabel: 'OBPOS_BPAddress',
  events: {
    onShowPopup: '',
    onHideBPSelector: ''
  },
  selectItem: function (bpartner) {
    var target = this.owner.owner.dialog.target;
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      OB.MobileApp.view.$.containerWindow.$.pointOfSale.bubble('onShowPopup', {
        popup: 'modalcustomeraddress',
        args: {
          target: target,
          businessPartner: bp,
          manageAddress: true
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
    }, {
      kind: 'OB.UI.BPAddressContextMenuItem',
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
      style: 'float: left; width: 91%',
      components: [{
        style: 'float: left; display: inline-block;',
        name: 'identifier'
      }, {
        style: 'float: left; display: inline-block; color: #888888; padding-left:5px;',
        name: 'filter'
      }, {
        style: 'float: left;',
        components: [{
          style: 'float: left;',
          name: 'bottomShipIcon'
        }, {
          style: 'float: left;',
          name: 'bottomBillIcon'
        }]
      }, {
        style: 'display: inline-block; float: left; font-weight: bold; color: red; padding-left:5px;',
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
    this.$.filter.setContent(this.model.get('filter'));
    if (this.model.get('customerBlocking') && this.model.get('salesOrderBlocking')) {
      this.$.onHold.setContent('(' + OB.I18N.getLabel('OBPOS_OnHold') + ')');
    }
    if (this.model.get('isBillTo') && this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('addresshipitems');
      this.$.bottomBillIcon.addClass('addressbillitems');
    } else if (this.model.get('isBillTo')) {
      this.$.bottomBillIcon.addClass('addressbillitems');
    } else if (this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('addresshipitems');
    }
    var bPartner = this.owner.owner.owner.bPartner;
    if (bPartner && bPartner.get('id') === this.model.get('id')) {
      this.applyStyle('background-color', '#fbf6d1');
    }
    // Context menu
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.dialog = this.owner.owner.owner.owner;
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
    onClearAction: 'clearAction',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget'
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
  setBusinessPartnerTarget: function (inSender, inEvent) {
    this.target = inEvent.target;
  },
  clearAction: function (inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this;

    if (OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers')) {
      this.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.setDisabled(false);
    }

    this.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.filterSelector.setAdvancedSearch(inEvent.advanced);
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
          var filter = '';
          if (hasLocationInFilter() || !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            filter = ' / ' + bp.get('locName');
          }
          _.each(inEvent.filters, function (flt, index) {
            if (flt.column !== 'bp.name' && flt.column !== 'loc.name') {
              var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
                return col.column === flt.column;
              });
              if (column) {
                filter += ' / ' + (bp.get(column.name) ? bp.get(column.name) : '');
              }
            }
          });
          bp.set('_identifier', bp.get('bpName'));
          bp.set('filter', filter);
        });
        me.bpsList.reset(dataBps.models);
        me.$.stBPAssignToReceipt.$.tbody.show();
      } else {
        me.bpsList.reset();
        me.$.stBPAssignToReceipt.$.tempty.show();
      }
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var criteria = {
        _orderByClause: ''
      };
      criteria.remoteFilters = [];
      var hasLocation = false;
      _.each(inEvent.filters, function (flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
          return col.column === flt.column;
        });
        if (column) {
          if (column.location) {
            hasLocation = true;
          }
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
      if (inEvent.orderby && ((inEvent.orderby.isLocationFilter && hasLocation) || !inEvent.orderby.isLocationFilter)) {
        criteria._orderByClause = inEvent.orderby.column + ' ' + inEvent.orderby.direction;
      }
      OB.Dal.find(OB.Model.BPartnerFilter, criteria, successCallbackBPs, errorCallback, this);
    } else {
      var index = 0,
          params = [],
          select = 'select ',
          orderby = ' order by ' + (inEvent.advanced && inEvent.orderby ? inEvent.orderby.column + ' ' + inEvent.orderby.direction : 'bp.name');

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
      select += ' from c_bpartner bp left join c_bpartner_location loc on bp.c_bpartner_id = loc.c_bpartner_id ';

      if (inEvent.advanced) {
        if (inEvent.filters.length > 0) {
          select += 'where ';
          _.each(inEvent.filters, function (flt, index) {
            if (index !== 0) {
              select += ' and ';
            }
            select += flt.column + ' like ? ';
            params.push('%' + OB.UTIL.unAccent(flt.text) + '%');
            if (!inEvent.advanced && flt.orderby) {
              orderby = ' order by ' + flt.column;
            }
          });
        }
      } else if (inEvent.filters.length > 0) {
        select += 'where bp._filter like ? or loc._filter like ?';
        var text = OB.UTIL.unAccent(inEvent.filters[0].text);
        params.push('%' + text + '%');
        params.push('%' + text + '%');
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
        if (model.get('bpLocactionId')) {
          OB.Dal.get(OB.Model.BPLocation, model.get('bpLocactionId'), function (loc) {
            var shipping = null,
                billing = null;
            if (loc) {
              if (!billing && loc.get('isBillTo')) {
                billing = loc;
              }
              if (!shipping && loc.get('isShipTo')) {
                shipping = loc;
              }
            }
            me.loadBPLocations(model, shipping, billing);
          });
        } else {
          me.loadBPLocations(model, null, null);
        }
      }
    }, this);
  },
  loadBPLocations: function (bpartner, shipping, billing) {
    var me = this;
    if (shipping && billing) {
      this.setBPLocation(bpartner, shipping, billing);
    } else {
      var criteria = {
        bpartner: {
          operator: OB.Dal.EQ,
          value: bpartner.get('bpartnerId')
        }
      };
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        var filterBpartnerId = {
          columns: ['bpartner'],
          operator: OB.Dal.EQ,
          value: bpartner.get('bpartnerId')
        };
        criteria.remoteFilters = [filterBpartnerId];
      }
      OB.Dal.find(OB.Model.BPLocation, criteria, function (collection) {
        if (!billing) {
          billing = _.find(collection.models, function (loc) {
            return loc.get('isBillTo');
          });
        }
        if (!shipping) {
          shipping = _.find(collection.models, function (loc) {
            return loc.get('isShipTo');
          });
        }
        me.setBPLocation(bpartner, shipping, billing);
      });
    }
  },
  setBPLocation: function (bpartner, shipping, billing) {
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      if (!shipping) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerNoShippingAddress', [bpartner.get('_identifier')]));
        return;
      }
      if (!billing) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BPartnerNoInvoiceAddress', [bpartner.get('_identifier')]));
        return;
      }
    }
    var me = this;
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      if (shipping) {
        bp.set('shipLocId', shipping.get('id'));
        bp.set('shipLocName', shipping.get('name'));
        bp.set('shipCityName', shipping.get('cityName'));
        bp.set('shipPostalCode', shipping.get('postalCode'));
      } else {
        bp.set('shipLocId', null);
        bp.set('shipLocName', null);
        bp.set('shipCityName', null);
        bp.set('shipPostalCode', null);
      }
      if (billing) {
        bp.set("locId", billing.get("id"));
        bp.set("locName", billing.get("name"));
      } else {
        bp.set("locId", null);
        bp.set("locName", null);
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        bp.set('locationModel', shipping);
      }
      me.doChangeBusinessPartner({
        businessPartner: bp,
        target: me.target
      });
    });
  }
});

/*Modal definition*/
enyo.kind({
  name: 'OB.UI.ModalBusinessPartners',
  topPosition: '100px',
  kind: 'OB.UI.Modal',
  handlers: {
    onHideBPSelector: 'hideBPSelector',
    onShowBPSelector: 'showBPSelector'
  },
  hideBPSelector: function () {
    this.hide();
  },
  showBPSelector: function () {
    this.show();
  },
  executeOnShow: function () {
    if (_.isUndefined(this.args.visibilityButtons)) {
      this.args.visibilityButtons = true;
    }
    if (_.isUndefined(this.args.target)) {
      this.args.target = 'order';
    }
    this.waterfall('onSetShow', {
      visibility: this.args.visibilityButtons
    });
    this.bubble('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    this.waterfall('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    if (!OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.filterSelector.hideFilterCombo();
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_retail.disableNewBPButton', true)) {
      this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.setDisabled(true);
    }
    if (this.args.businessPartner) {
      this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.filterSelector.searchAction();
      this.$.body.$.listBps.$.stBPAssignToReceipt.bPartner = this.args.businessPartner;
    } else {
      this.$.body.$.listBps.$.stBPAssignToReceipt.bPartner = null;
    }
    return true;
  },

  executeOnHide: function () {
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.filterSelector.clearFilter();
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

enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterBP',
  initComponents: function () {
    this.inherited(arguments);
    _.each(OB.Model.BPartnerFilter.getProperties(), function (prop) {
      // Set filter options for bpCategory and taxID
      if (prop.name === 'bpCategory') {
        prop.filter = OB.MobileApp.model.get('terminal').bp_showcategoryselector;
      }
      if (prop.name === 'taxID') {
        prop.filter = OB.MobileApp.model.get('terminal').bp_showtaxid;
      }
      if (prop.filter) {
        this.$.body.$.filters.addFilter(prop);
      }
    }, this);
  }
});