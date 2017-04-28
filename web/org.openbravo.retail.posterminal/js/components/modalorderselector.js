/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  name: 'OBPOS.UI.ReceiptSelector',
  kind: 'OB.UI.ModalSelector',
  topPosition: '70px',
  i18nHeader: 'OBPOS_OpenReceipt',
  handlers: {
    onChangePaidReceipt: 'changePaidReceipt'
  },
  body: {
    kind: 'OB.UI.ReceiptsList'
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.receiptsList.$.receiptslistitemprinter.$.theader.$.modalReceiptsScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function () {
    return this.$.body.$.receiptsList.$.receiptslistitemprinter.$.theader.$.modalReceiptsScrollableHeader.$.advancedFilterWindowButtonReceipts;
  },
  getAdvancedFilterDialog: function () {
    return 'OB_UI_ModalAdvancedFilterReceipts';
  },
  changePaidReceipt: function (inSender, inEvent) {
    this.model.get('orderList').addPaidReceipt(inEvent.newPaidReceipt);
    if (inEvent.newPaidReceipt.get('isLayaway')) {
      this.model.attributes.order.calculateReceipt();
    } else {
      this.model.attributes.order.calculateGrossAndSave(false);
    }
    return true;
  },
  executeOnShow: function () {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
  },
  init: function (model) {
    this.model = model;
  }
});

enyo.kind({
  kind: 'OB.UI.ListSelectorLine',
  name: 'OB.UI.ReceiptSelectorRenderLine',
  components: [{
    name: 'line',
    style: 'width: 100%',
    components: [{
      name: 'lineInfo',
      style: 'float: left; width: 100%; padding: 5px; ',
      components: [{
        style: 'padding-bottom: 3px',
        components: [{
          style: 'float: left; width: 100px;',
          name: 'date'
        }, {
          style: 'float: left; padding-left:5px;',
          name: 'documentNo'
        }, {
          style: 'float: right; padding-right:5px; width: 100px; text-align: right; font-weight: bold;',
          name: 'amount'
        }, {
          style: 'clear: both;'
        }]
      }, {
        components: [{
          style: 'float: left; width: 100px;',
          name: 'time'
        }, {
          style: 'float: left; padding-left:5px;',
          name: 'customer'
        }, {
          style: 'float: right; padding-right:5px; width: 100px; text-align: right; font-weight: bold;',
          name: 'orderType'
        }, {
          style: 'clear: both;'
        }]
      }]
    }]
  }],
  create: function () {
    var orderDate, orderTime, orderType, me = this;
    this.inherited(arguments);

    orderDate = new Date(OB.I18N.normalizeDate(this.model.get('creationDate')));
    orderType = OB.MobileApp.model.get('orderType').find(function (ot) {
      return ot.id === me.model.get('orderType');
    }).name;

    this.$.date.setContent(OB.I18N.formatDate(orderDate));
    this.$.documentNo.setContent(this.model.get('documentNo'));
    this.$.amount.setContent(OB.I18N.formatCurrency(this.model.get('totalamount')));
    this.$.time.setContent(OB.I18N.formatHour(orderDate));
    this.$.customer.setContent(this.model.get('businessPartnerName'));
    this.$.orderType.setContent(orderType);
    switch (this.model.get('orderType')) {
    case 'QT':
      this.$.orderType.applyStyle('color', 'rgb(248, 148, 29)');
      break;
    case 'LAY':
      this.$.orderType.applyStyle('color', 'lightblue');
      break;
    case 'RET':
      this.$.orderType.applyStyle('color', 'rgb(248, 148, 29)');
      break;
    default:
      this.$.orderType.applyStyle('color', 'rgb(108, 179, 63)');
      break;
    }
    this.applyStyle('padding', '5px');
    this.render();
  }
});

enyo.kind({
  name: 'OB.UI.ReceiptsList',
  classes: 'row-fluid',
  handlers: {
    onClearFilterSelector: 'clearAction',
    onSearchAction: 'searchAction'
  },
  events: {
    onShowPopup: '',
    onChangePaidReceipt: '',
    onChangeCurrentOrder: ''
  },
  receiptList: null,
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;',
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'receiptslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '350px',
          renderHeader: 'OB.UI.ModalReceiptsScrollableHeader',
          renderLine: 'OB.UI.ReceiptSelectorRenderLine',
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
  clearAction: function () {
    this.receiptList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this;

    function errorCallback() {
      me.$.renderLoading.hide();
      me.receiptList.reset();
      me.$.receiptslistitemprinter.$.tempty.show();
    }

    function successCallback(data) {
      me.$.renderLoading.hide();
      if (data && data.length > 0) {
        me.receiptList.reset(data.models);
        me.$.receiptslistitemprinter.$.tbody.show();
      } else {
        me.receiptList.reset();
        me.$.receiptslistitemprinter.$.tempty.show();
      }
    }
    this.$.receiptslistitemprinter.$.tempty.hide();
    this.$.receiptslistitemprinter.$.tbody.hide();
    this.$.receiptslistitemprinter.$.tlimit.hide();
    this.$.renderLoading.show();

    var criteria, orderByClause = '';

    if (inEvent.orderby) {
      orderByClause = inEvent.orderby.serverColumn + ' ' + inEvent.orderby.direction;
    } else {
      orderByClause = 'ord.documentNo';
    }

    criteria = {
      forceRemote: true,
      _orderByClause: orderByClause
    };

    if (OB.MobileApp.model.hasPermission("OBPOS_orderLimit", true)) {
      criteria._limit = OB.DEC.abs(OB.MobileApp.model.hasPermission("OBPOS_orderLimit", true));
    }

    criteria.remoteFilters = [];

    inEvent.filters.forEach(function (flt) {
      if (flt.hqlFilter) {
        criteria.remoteFilters.push({
          value: flt.hqlFilter,
          columns: [],
          operator: OB.Dal.FILTER,
          params: [flt.value]
        });
      } else {
        criteria.remoteFilters.push({
          value: flt.value,
          columns: [flt.column],
          operator: flt.operator || OB.Dal.STARTSWITH,
          isId: flt.column === 'orderType'
        });
      }
    });

    OB.Dal.find(OB.Model.OrderFilter, criteria, function (data) {
      if (data) {
        successCallback(data);
      } else {
        errorCallback();
      }
    }, function () {
      errorCallback();
    });

  },
  init: function (model) {
    var me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
    this.model = model;
    this.receiptList = new Backbone.Collection();
    this.$.receiptslistitemprinter.setCollection(this.receiptList);
    this.receiptList.on('click', function (model) {
      function loadOrder(model) {
        var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearch');
        OB.UTIL.showLoading(true);
        process.exec({
          orderid: model.get('id')
        }, function (data) {
          if (data) {
            if (me.model.get('leftColumnViewManager').isMultiOrder()) {
              if (me.model.get('multiorders')) {
                me.model.get('multiorders').resetValues();
              }
              me.model.get('leftColumnViewManager').setOrderMode();
            }
            var searchSynchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearchNewReceipt');
            me.model.get('orderList').newPaidReceipt(data[0], function (order) {
              me.doChangePaidReceipt({
                newPaidReceipt: order
              });
              OB.UTIL.SynchronizationHelper.finished(searchSynchId, 'clickSearchNewReceipt');

            });
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
          }
          OB.UTIL.SynchronizationHelper.finished(synchId, 'clickSearch');
        });
        return true;
      }

      // Check in Current Session
      var orderTypeMsg, i;
      for (i = 0; i < me.model.get('orderList').length; i++) {
        if (me.model.get('orderList').models[i].get('id') === model.get('id')) {
          orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
          if (me.model.get('orderList').models[i].get('isLayaway')) {
            orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
          } else if (me.model.get('orderList').models[i].get('isQuotation')) {
            orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
          }
          me.doShowPopup({
            popup: 'OB_UI_MessageDialog',
            args: {
              message: (enyo.format(OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'), orderTypeMsg, model.get('documentNo')))
            }
          });
          if (OB.MobileApp.model.receipt.get('documentNo') !== model.get('documentNo')) {
            me.doChangeCurrentOrder({
              newCurrentOrder: me.model.get('orderList').models[i]
            });
          }
          return true;
        }
      }

      // Check in Other Session
      OB.Dal.find(OB.Model.Order, {
        'hasbeenpaid': 'N'
      }, function (ordersNotProcessed) {
        if (ordersNotProcessed.length > 0) {
          var existingOrder = _.find(ordersNotProcessed.models, function (order) {
            return order.get('id') === model.get('id') || order.get('oldId') === model.get('id');
          });
          if (existingOrder) {
            var orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
            if (existingOrder.get('isLayaway')) {
              orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
            } else if (existingOrder.get('isQuotation')) {
              orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
            }
            // Getting Other Session User's username
            OB.Dal.find(OB.Model.Session, {
              'id': existingOrder.get('session')
            }, function (sessions) {
              if (sessions.length > 0) {
                OB.Dal.find(OB.Model.User, {
                  'id': sessions.models[0].get('user')
                }, function (users) {
                  if (users.length > 0) {
                    OB.UTIL.showConfirmation.display(enyo.format(OB.I18N.getLabel('OBPOS_ticketAlreadyOpenedInSession'), orderTypeMsg, model.get('documentNo'), users.models[0].get('name')), enyo.format(OB.I18N.getLabel('OBPOS_MsgConfirmSaveInCurrentSession'), users.models[0].get('name')), [{
                      label: OB.I18N.getLabel('OBMOBC_LblOk'),
                      action: function () {
                        OB.Dal.remove(existingOrder, function () {
                          loadOrder(model);
                        }, OB.UTIL.showError);
                      }
                    }, {
                      label: OB.I18N.getLabel('OBMOBC_LblCancel')
                    }], {
                      onHideFunction: function (dialog) {
                        return true;
                      }
                    });
                  }
                });
              }
            });
          } else {
            return loadOrder(model);
          }
        } else {
          return loadOrder(model);
        }
      });
      return true;
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.ModalReceiptsScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    style: 'padding: 10px;',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.OrderFilter.getProperties()
  }, {
    style: 'padding: 10px;',
    components: [{
      style: 'display: table; width: 100%;',
      components: [{
        style: 'display: table-cell; text-align: center; ',
        components: [{
          kind: 'OBPOS.UI.AdvancedFilterWindowButtonReceipts'
        }]
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.filterSelector.$.entityFilterText.skipAutoFilterPref = true;
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterReceipts',
  initComponents: function () {
    this.inherited(arguments);
    this.setFilters(OB.Model.OrderFilter.getProperties());
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBPOS.UI.AdvancedFilterWindowButtonReceipts',
  dialog: 'OB_UI_ModalAdvancedFilterReceipts'
});