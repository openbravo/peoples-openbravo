/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _, OB */

enyo.kind({
  name: 'OB.UI.ReceiptsForPayOpenTicketsList',
  kind: 'OB.UI.GenericReceiptsList',
  initComponents: function () {
    this.inherited(arguments);
    this.setFilterModel(OB.Model.VReturnsFilter);
    this.setNameOfReceiptsListItemPrinter('payOpenTicketsReceiptsListItemPrinter');
    this.$.containerOfReceiptsListItemPrinter.createComponent({
      name: 'payOpenTicketsReceiptsListItemPrinter',
      kind: 'OB.UI.ScrollableTable',
      scrollAreaMaxHeight: '350px',
      renderHeader: null,
      renderLine: 'OB.UI.ListMultiOrdersLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    }, {
      // needed to fix the owner so it is not containerOfReceiptsListItemPrinter but ReceiptsForVerifiedReturnsList
      // so can be accessed navigating from the parent through the components
      owner: this
    });
    this.$[this.getNameOfReceiptsListItemPrinter()].renderHeader = 'OB.UI.ModalPayOpenTicketsScrollableHeader';
  },
  init: function (model) {
    var me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
    this.inherited(arguments);
    this.model = model;
    this.setDefaultFilters([{
      value: 'LAY',
      columns: ['orderType']
    }]);
  },
  actionPrePrint: function (data, criteria) {
    var totalData = [],
        popedElements = [],
        stringFilterDate, filterDate, stringOrderDate, orderDate;

    totalData = totalData.concat(this.model.get('orderList').models);
    totalData.forEach(function (order) {
      if (order.attributes.lines.length === 0) {
        popedElements.push(order);
      }
    });
    popedElements.forEach(function (popedElement) {
      totalData.splice(totalData.indexOf(popedElement), 1);
    });
    criteria.remoteFilters.forEach(function (remoteFilters) {
      popedElements = [];

      totalData.forEach(function (order) {
        switch (remoteFilters.columns[0]) {
        case "documentNo":
          if (!order.attributes.documentNo.includes(remoteFilters.value)) {
            popedElements.push(order);
          }
          break;

        case "businessPartner":
          if (order.attributes.client !== remoteFilters.value) {
            popedElements.push(order);
          }
          break;

        case "orderDateFrom":
          stringFilterDate = remoteFilters.params[0];
          filterDate = new Date(stringFilterDate);
          stringOrderDate = order.attributes.orderDate;

          stringOrderDate = stringOrderDate.replace(/T.*/, '');
          orderDate = new Date(stringOrderDate);

          if (filterDate.getTime() < orderDate.getTime()) {
            popedElements.push(order);
          }
          break;

        case "orderDateTo":
          stringFilterDate = remoteFilters.params[0];
          filterDate = new Date(stringFilterDate);

          stringOrderDate = order.attributes.orderDate;
          stringOrderDate = stringOrderDate.replace(/T.*/, '');
          orderDate = new Date(stringOrderDate);

          if (filterDate.getTime() > orderDate.getTime()) {
            popedElements.push(order);
          }
          break;

        case "totalamountFrom":
          if (order.attributes.gross < remoteFilters.params[0]) {
            popedElements.push(order);
          }
          break;

        case "totalamountTo":
          if (order.attributes.gross > remoteFilters.params[0]) {
            popedElements.push(order);
          }
          break;

        default:
          break;
        }
      });
      popedElements.forEach(function (popedElement) {
        totalData.splice(totalData.indexOf(popedElement), 1);
      });
    });
    totalData = totalData.concat(data.models);
    data.models = totalData;
    data.length = totalData.length;
  }
});

enyo.kind({
  name: 'OB.UI.ModalPayOpenTicketsScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: ''
  },
  components: [{
    style: 'padding: 10px;',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.VReturnsFilter.getProperties()
  }, {
    style: 'padding: 10px;',
    components: [{
      style: 'display: table; width: 100%;',
      components: [{
        style: 'display: table-cell; text-align: center; ',
        components: [{
          kind: 'OBPOS.UI.AdvancedFilterWindowButtonVerifiedReturns'
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
  name: 'OB.UI.ModalAdvancedFilterVerifiedReturns',
  model: OB.Model.VReturnsFilter,
  initComponents: function () {
    this.inherited(arguments);
    this.setFilters(OB.Model.VReturnsFilter.getProperties());
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBPOS.UI.AdvancedFilterWindowButtonVerifiedReturns',
  dialog: 'modalAdvancedFilterVerifiedReturns'
});


enyo.kind({
  name: 'OB.UI.ModalMultiOrdersPayOpenTickets',
  kind: 'OB.UI.ModalSelector',
  topPosition: '70px',
  i18nHeader: 'OBPOS_LblPaidReceipts',
  published: {
    params: null
  },
  body: {
    kind: 'OB.UI.ReceiptsForPayOpenTicketsList'
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.receiptsForPayOpenTicketsList.$.payOpenTicketsReceiptsListItemPrinter.$.theader.$.modalPayOpenTicketsScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function () {
    return this.$.body.$.receiptsForPayOpenTicketsList.$.payOpenTicketsReceiptsListItemPrinter.$.theader.$.modalPayOpenTicketsScrollableHeader.$.advancedFilterWindowButtonVerifiedReturns;
  },
  getAdvancedFilterDialog: function () {
    return 'modalAdvancedFilterVerifiedReturns';
  },
  executeOnShow: function () {
    var me = this;
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_SelectCurrentTicketsOnPaidOpen', true)) {
      _.each(me.model.get('orderList').models, function (iter) {
        if (iter.get('lines') && iter.get('lines').length > 0) {
          if ((iter.get('orderType') === 0 || iter.get('orderType') === 2) && !iter.get('isPaid') && !iter.get('isQuotation') && iter.get('gross') >= 0) {
            if (!_.isNull(iter.id) && !_.isUndefined(iter.id)) {
              iter.set('checked', true);
              me.$.body.$.receiptsForPayOpenTicketsList.receiptList.add(iter);
            }
          }
        }
      });
    }
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
  },
  init: function (model) {
    this.model = model;
    this.$.header.createComponent({
      kind: 'OB.UI.ModalMultiOrdersTopHeader'
    });
  }
});

enyo.kind({
  name: 'OB.UI.ListMultiOrdersLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'border-bottom: 1px solid #cccccc;text-align: left; padding-left: 70px; height: 58px;',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.model.set('checked', !this.model.get('checked'));
    this.model.trigger('verifyDoneButton', this.model);
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px; display: inline',
    components: [{
      style: 'display: inline',
      name: 'topLine'
    }, {
      style: 'font-weight: bold; color: lightblue; float: right; text-align:right; ',
      name: 'isLayaway'
    }, {
      style: 'color: #888888;',
      name: 'bottonLine'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    var returnLabel = '';
    this.inherited(arguments);
    if (this.model.get('documentTypeId') === OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns) {
      this.model.set('totalamount', OB.DEC.mul(this.model.get('totalamount'), -1));
      returnLabel = ' (' + OB.I18N.getLabel('OBPOS_ToReturn') + ')';
    }
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + (this.model.get('bp') ? this.model.get('bp').get('_identifier') : this.model.get('businessPartnerName')) + returnLabel);
    this.$.bottonLine.setContent(((this.model.get('totalamount') || this.model.get('totalamount') === 0) ? this.model.get('totalamount') : this.model.getGross()) + ' (' + OB.I18N.formatDate(new Date(this.model.get('orderDate'))) + ') ');
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
    if (this.model.get('orderType') === 'LAY') {
      this.$.isLayaway.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
    }
    this.render();
  }
});

enyo.kind({
  name: 'OB.UI.ModalMultiOrdersTopHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onHideThisPopup: '',
    onSelectMultiOrders: '',
    onTabChange: '',
    onRightToolDisabled: ''
  },
  components: [{
    style: 'display: table;',
    components: [{
      style: 'display: table-cell; float:left',
      components: [{
        name: 'doneMultiOrdersButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'doneAction'
      }]
    }, {
      style: 'display: table-cell; vertical-align: middle; width: 100%;',
      components: [{
        name: 'title',
        style: 'text-align: center;'
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.title.setContent(OB.I18N.getLabel('OBPOS_LblMultiOrders'));
    this.$.doneMultiOrdersButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
  },
  disableDoneButton: function (value) {
    this.$.doneMultiOrdersButton.setDisabled(value);
  },
  doneAction: function () {
    var selectedMultiOrders = [],
        me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts'),
        checkedMultiOrders = _.compact(this.parent.parent.parent.$.body.$.receiptsForPayOpenTicketsList.receiptList.map(function (e) {
        if (e.get('checked')) {
          return e;
        }
      })),
        addOrdersToOrderList;

    if (checkedMultiOrders.length === 0) {
      return true;
    }

    function newReversalOrder() {
      var i;
      for (i = 0; i < selectedMultiOrders.length; i++) {
        if (selectedMultiOrders[i].isNewReversed()) {
          return selectedMultiOrders[i].get('documentNo');
        }
      }
      return false;
    }

    addOrdersToOrderList = _.after(checkedMultiOrders.length, function () {
      var reversalOrder = newReversalOrder();
      if (reversalOrder) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_ReversePaymentPending', [reversalOrder]));
        return;
      }
      OB.UTIL.StockUtils.checkOrderLinesStock(selectedMultiOrders, function (hasStock) {
        if (hasStock) {
          OB.UTIL.HookManager.executeHooks('OBPOS_PreMultiOrderHook', {
            selectedMultiOrders: selectedMultiOrders
          }, function (args) {
            if (args && args.cancellation) {
              return;
            }
            me.doSelectMultiOrders({
              value: selectedMultiOrders,
              callback: function () {
                me.showPaymentView();
              }
            });
          });
        }
      });
    });

    OB.UTIL.showLoading(true);
    me.owner.owner.model.deleteMultiOrderList();
    _.each(checkedMultiOrders, function (iter) {
      if (_.indexOf(me.owner.owner.model.get('orderList').models, iter) !== -1) {
        iter.set('checked', true);
        iter.save();
        selectedMultiOrders.push(iter);
        addOrdersToOrderList();
      } else {
        process.exec({
          orderid: iter.id
        }, function (data) {
          if (data) {
            me.owner.owner.model.get('orderList').newPaidReceipt(data[0], function (order) {
              order.set('loadedFromServer', true);
              me.owner.owner.model.get('orderList').addMultiReceipt(order);
              order.set('checked', iter.get('checked'));
              OB.DATA.OrderTaxes(order);
              order.set('belongsToMultiOrder', true);
              order.calculateReceipt(function () {
                selectedMultiOrders.push(order);
                addOrdersToOrderList();
              });
            });
          } else {
            OB.UTIL.showLoading(false);
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
          }
        });
      }
    });
    me.doHideThisPopup();
  },
  cancelAction: function () {
    this.doHideThisPopup();
  },
  showPaymentView: function () {
    OB.UTIL.showLoading(false);
    this.doTabChange({
      tabPanel: 'payment',
      keyboard: 'toolbarpayment',
      edit: false
    });
  }
});