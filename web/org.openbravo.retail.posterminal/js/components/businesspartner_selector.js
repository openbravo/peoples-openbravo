/*
 ************************************************************************************
 * Copyright (C) 2016-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB, _ */

OB.UTIL.BusinessPartnerSelector = {
  cloneAndPush: function (list, value) {
    var result = _.clone(list || []);
    result.push(value);
    return result;
  },
  cloneAndPop: function (list) {
    var result = _.clone(list || []);
    result.pop();
    return result;
  }
};

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartnerSelector',
  classes: 'btnlink-gray flex-customer-buttons-item flex-customer-buttons-customer',
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
          presetCustomerId: OB.MobileApp.model.receipt.get('bp').id,
          target: 'order',
          clean: true,
          navigationPath: []
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
  name: 'OB.UI.NewCustomerButton',
  events: {
    onShowPopup: '',
    onHideThisPopup: ''
  },
  disabled: false,
  style: 'width: 170px; margin: 0px 9px 8px 0px;',
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
    var modalDlg = this.owner.owner.owner.owner.owner.owner,
        navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(modalDlg.args.navigationPath, 'modalcustomer');
    this.doShowPopup({
      popup: 'customerCreateAndEdit',
      args: {
        businessPartner: null,
        target: modalDlg.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(navigationPath, 'customerView'),
        cancelNavigationPath: navigationPath
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
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OB.UI.AdvancedFilterButton',
  style: 'width: 170px; margin: 0px 0px 8px 9px;',
  classes: 'btnlink-yellow btnlink btnlink-small ',
  dialog: 'modalAdvancedFilterBP',
  i18nLabel: 'OBPOS_LblAdvancedFilter',
  disabled: false,
  handlers: {
    onNewBPDisabled: 'doDisableNewBP'
  },
  doDisableNewBP: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
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
  name: 'OB.UI.ModalBpSelectorScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  components: [{
    style: 'padding: 10px;',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.BPartnerFilter.getProperties()
  }, {
    style: 'padding: 7px;',
    showing: true,
    handlers: {
      onSetShow: 'setShow'
    },
    setShow: function (inSender, inEvent) {
      this.setShowing(inEvent.visibility);
      return true;
    },
    components: [{
      style: 'display: table; width: 100%',
      components: [{
        style: 'display: table-cell; text-align: right;',
        components: [{
          kind: 'OB.UI.NewCustomerButton',
          name: 'newAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.AdvancedFilterButton'
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
    var dialog = this.owner.owner.dialog;
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      dialog.bubble('onShowPopup', {
        popup: 'customerView',
        args: {
          businessPartner: bp,
          target: dialog.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(dialog.owner.owner.args.navigationPath, 'modalcustomer')
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

    var dialog = this.owner.owner.dialog,
        navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(dialog.owner.owner.args.navigationPath, 'modalcustomer');

    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      dialog.bubble('onShowPopup', {
        popup: 'customerCreateAndEdit',
        args: {
          businessPartner: bp,
          target: dialog.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(navigationPath, 'customerView'),
          cancelNavigationPath: navigationPath
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
  selectItem: function (bpartner) {
    var dialog = this.owner.owner.dialog;
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function (bp) {
      OB.MobileApp.view.$.containerWindow.getRoot().bubble('onShowPopup', {
        popup: 'modalcustomeraddress',
        args: {
          target: 'modal_selector_business_partners',
          businessPartner: bp,
          manageAddress: true,
          clean: true,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(dialog.owner.owner.args.navigationPath, 'modalcustomer')
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
      permission: 'OBPOS_retail.editCustomerButton'
    }, {
      kind: 'OB.UI.BPAddressContextMenuItem',
      permission: 'OBPOS_retail.assignToReceiptAddress'
    });

    menuOptions = menuOptions.concat(extraOptions);
    this.$.menu.setItems(menuOptions);
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsSelectorLine',
  kind: 'OB.UI.ListSelectorLine',
  components: [{
    name: 'line',
    style: 'line-height: 23px; width: 100%',
    components: [{
      name: 'textInfo',
      style: 'float: left; width: calc(100% - 50px); padding: 8px 0px; display: table; ',
      components: [{
        style: 'display: table-cell;',
        components: [{
          tag: 'span',
          name: 'identifier'
        }, {
          tag: 'span',
          style: 'color: #888888;',
          name: 'filter'
        }, {
          tag: 'span',
          style: 'font-weight: bold; color: red;',
          name: 'onHold'
        }]
      }, {
        style: 'vertical-align: top; ',
        name: 'bottomShipIcon',
        classes: 'addresshipitems fix-bgposition-y'
      }, {
        style: 'vertical-align: top; ',
        name: 'bottomBillIcon',
        classes: 'addressbillitems fix-bgposition-y; '
      }, {
        style: 'clear: both;'
      }]
    }, {
      style: 'float: right; width: 30px; margin-right: 13px;',
      components: [{
        kind: 'OB.UI.BusinessPartnerContextMenu',
        name: 'btnContextMenu'
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.filter.setContent(this.model.get('filter'));
    if (this.model.get('customerBlocking') && this.model.get('salesOrderBlocking')) {
      this.$.onHold.setContent(' (' + OB.I18N.getLabel('OBPOS_OnHold') + ')');
    }
    this.$.bottomShipIcon.show();
    this.$.bottomBillIcon.show();
    if (this.model.get('isBillTo') && this.model.get('isShipTo')) {
      this.$.bottomShipIcon.applyStyle('visibility', 'visible');
      this.$.bottomBillIcon.applyStyle('visibility', 'visible');
    } else if (this.model.get('isBillTo')) {
      this.$.bottomShipIcon.applyStyle('visibility', 'hidden');
      this.$.bottomBillIcon.applyStyle('visibility', 'visible');
    } else if (this.model.get('isShipTo')) {
      this.$.bottomShipIcon.applyStyle('visibility', 'visible');
      this.$.bottomBillIcon.applyStyle('visibility', 'hidden');
    } else {
      this.$.bottomShipIcon.hide();
      this.$.bottomBillIcon.hide();
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
  name: 'OB.UI.ListBpsSelector',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearFilterSelector: 'clearAction',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget'
  },
  events: {
    onChangeBusinessPartner: '',
    onChangeFilterSelector: '',
    onHideSelector: '',
    onShowSelector: ''
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
          classes: 'bp-scroller',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalBpSelectorScrollableHeader',
          renderLine: 'OB.UI.ListBpsSelectorLine',
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
  loadPresetCustomer: function (bpartnerId) {
    var me = this;
    OB.Dal.get(OB.Model.BusinessPartner, bpartnerId, function (bp) {
      bp.set('bpartnerId', bpartnerId, {
        silent: true
      });
      me.bpsList.reset([bp]);
      me.$.stBPAssignToReceipt.$.tbody.show();
    });
  },
  clearAction: function (inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this;

    if (OB.MobileApp.model.hasPermission('OBPOS_retail.createCustomerButton', true)) {
      this.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.newAction.setDisabled(false);
    }

    this.$.stBPAssignToReceipt.$.tempty.hide();
    this.$.stBPAssignToReceipt.$.tbody.hide();
    this.$.stBPAssignToReceipt.$.tlimit.hide();
    this.$.renderLoading.show();

    function hasLocationInFilter() {
      if (OB.MobileApp.model.hasPermission('OBPOS_FilterAlwaysBPByAddress', true)) {
        return true;
      }
      return _.some(inEvent.filters, function (flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function (col) {
          return col.column === flt.column;
        });
        return column && column.location;
      });
    }

    function errorCallback(tx, error) {
      me.$.renderLoading.hide();
      me.$.stBPAssignToReceipt.$.tempty.show();
      me.doHideSelector();
      var i, message, tokens;

      function getProperty(property) {
        return OB.Model.BPartnerFilter.getProperties().find(function (prop) {
          return prop.name === property;
        });
      }

      // Generate a generic message if error is not defined
      if (OB.UTIL.isNullOrUndefined(error) || OB.UTIL.isNullOrUndefined(error.message)) {
        error = {
          message: OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable')
        };
      }

      if (error.message.startsWith('###')) {
        tokens = error.message.split('###');
        message = [];
        for (i = 0; i < tokens.length; i++) {
          if (tokens[i] !== '') {
            if (tokens[i] === 'OBMOBC_FilteringNotAllowed' || tokens[i] === 'OBMOBC_SortingNotAllowed') {
              message.push({
                content: OB.I18N.getLabel(tokens[i]),
                style: 'text-align: left; padding-left: 8px;'
              });
            } else {
              var property = getProperty(tokens[i]);
              if (property) {
                message.push({
                  content: OB.I18N.getLabel(property.caption),
                  style: 'text-align: left; padding-left: 8px;',
                  tag: 'li'
                });
              }
            }
          }
        }
      } else {
        message = error.message;
      }

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), message, null, {
        onHideFunction: function () {
          me.doShowSelector();
        }
      });
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
            operator: OB.MobileApp.model.hasPermission('OBPOS_remote.customer_usesContains', true) ? OB.Dal.CONTAINS : OB.Dal.STARTSWITH,
            value: flt.value,
            location: column.location
          });
        }
      });
      if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
        criteria._limit = OB.DEC.abs(OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true));
      }
      if (inEvent.orderby) {
        criteria._orderByProperties = [{
          property: inEvent.orderby.name,
          sorting: inEvent.orderby.direction
        }];
      } else {
        criteria._orderByProperties = [{
          property: 'bpName',
          sorting: 'asc'
        }];
      }
      OB.Dal.find(OB.Model.BPartnerFilter, criteria, successCallbackBPs, errorCallback, this);
    } else {
      var limit, index = 0,
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
            params.push('%' + flt.value + '%');
            if (!inEvent.advanced && flt.orderby) {
              orderby = ' order by ' + flt.column;
            }
          });
        }
      } else if (inEvent.filters.length > 0) {
        select += 'where bp._filter like ? or loc._filter like ?';
        var text = OB.UTIL.unAccent(inEvent.filters[0].value);
        params.push('%' + text + '%');
        params.push('%' + text + '%');
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
        limit = OB.DEC.abs(OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true));
      }
      OB.Dal.query(OB.Model.BPartnerFilter, select + orderby, params, successCallbackBPs, errorCallback, null, null, limit);
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
      var bp = new OB.Model.BusinessPartner({
        id: bpartner.get('bpartnerId')
      });
      bp.loadBPLocations(shipping, billing, function (shipping, billing, locations) {
        me.setBPLocation(bpartner, shipping, billing, locations);
      });
    }
  },
  setBPLocation: function (bpartner, shipping, billing, locations) {
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
      bp.setBPLocations(shipping, billing, OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true));
      bp.set('locations', locations);
      if (me.target.startsWith('filterSelectorButton_')) {
        me.doChangeFilterSelector({
          selector: {
            name: me.target.substring('filterSelectorButton_'.length),
            value: bp.get('id'),
            text: bp.get('_identifier'),
            businessPartner: bp
          }
        });
      } else {
        me.doChangeBusinessPartner({
          businessPartner: bp,
          target: me.target
        });
      }
    });
  }
});

/*Modal definition*/
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OB.UI.ModalSelectorBusinessPartners',
  topPosition: '75px',
  i18nHeader: 'OBPOS_LblAssignCustomer',
  body: {
    kind: 'OB.UI.ListBpsSelector'
  },
  executeOnShow: function () {
    if (!this.isInitialized()) {
      this.inherited(arguments);
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
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.createCustomerButton', true));
      if (!OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.hideFilterCombo();
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_retail.disableNewBPButton', true)) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.newAction.setDisabled(true);
      }
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.clearFilter();
      if (this.args.businessPartner) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.searchAction();
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.bPartner = this.args.businessPartner;
      } else {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.bPartner = null;
      }
      if (this.args.presetCustomerId) {
        this.$.body.$.listBpsSelector.loadPresetCustomer(this.args.presetCustomerId);
      }
    } else if (this.args.makeSearch) {
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.searchAction();
    }
    return true;
  },
  getScrollableTable: function () {
    return this.$.body.$.listBpsSelector.$.stBPAssignToReceipt;
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function () {
    return this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.advancedFilterWindowButton;
  },
  getAdvancedFilterDialog: function () {
    return 'modalAdvancedFilterBP';
  },
  init: function (model) {
    this.inherited(arguments);
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.$.entityFilterText.skipAutoFilterPref = true;
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterBP',
  model: OB.Model.BPartnerFilter,
  initComponents: function () {
    this.inherited(arguments);
    _.each(this.model.getProperties(), function (prop) {
      // Set filter options for bpCategory and taxID
      if (prop.name === 'bpCategory') {
        prop.filter = OB.MobileApp.model.get('terminal').bp_showcategoryselector;
      }
      if (prop.name === 'taxID') {
        prop.filter = OB.MobileApp.model.get('terminal').bp_showtaxid;
      }
    }, this);
    this.setFilters(this.model.getProperties());
  }
});