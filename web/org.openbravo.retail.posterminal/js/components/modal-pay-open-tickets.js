/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, OB*/

enyo.kind({
  name: 'OB.UI.ReceiptsForPayOpenTicketsList',
  kind: 'OB.UI.GenericReceiptsList',
  classes: 'obUiReceiptsForPayOpenTicketsList',
  initComponents: function() {
    this.inherited(arguments);
    this.setFilterModel(OB.Model.VReturnsFilter);
    this.setNameOfReceiptsListItemPrinter(
      'payOpenTicketsReceiptsListItemPrinter'
    );
    this.$.containerOfReceiptsListItemPrinter.createComponent(
      {
        name: 'payOpenTicketsReceiptsListItemPrinter',
        kind: 'OB.UI.ScrollableTable',
        scrollAreaClasses:
          'obUiReceiptsForPayOpenTicketsList-listItemPrinter-scrollArea',
        renderHeader: null,
        renderLine: 'OB.UI.ListMultiOrdersLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      },
      {
        // needed to fix the owner so it is not containerOfReceiptsListItemPrinter but ReceiptsForVerifiedReturnsList
        // so can be accessed navigating from the parent through the components
        owner: this
      }
    );
    this.$[this.getNameOfReceiptsListItemPrinter()].renderHeader =
      'OB.UI.ModalPayOpenTicketsScrollableHeader';
  },
  init: function(model) {
    this.inherited(arguments);
    this.model = model;
    if (OB.MobileApp.model.get('terminal').terminalType.calculateprepayments) {
      this.setDefaultFilters([
        {
          value: 'payOpenTickets',
          columns: ['orderType']
        }
      ]);
    } else {
      this.setDefaultFilters([
        {
          value: 'LAY',
          columns: ['orderType']
        }
      ]);
    }
  },
  actionPrePrint: function(data, criteria) {
    var totalData = [],
      popedElements = [],
      stringFilterDate,
      filterDate,
      stringOrderDate,
      orderDate;

    totalData = totalData.concat(this.model.get('orderList').models);
    totalData.forEach(function(order) {
      if (
        order.attributes.lines.length === 0 ||
        order.attributes.gross < 0 ||
        order.get('isPaid') ||
        order.get('isQuotation') ||
        order.getOrderType() === 3
      ) {
        popedElements.push(order);
      }
    });
    popedElements.forEach(function(popedElement) {
      totalData.splice(totalData.indexOf(popedElement), 1);
    });
    criteria.remoteFilters.forEach(function(remoteFilters) {
      popedElements = [];

      totalData.forEach(function(order) {
        switch (remoteFilters.columns[0]) {
          case 'documentNo':
            if (!order.attributes.documentNo.includes(remoteFilters.value)) {
              popedElements.push(order);
            }
            break;

          case 'businessPartner':
            if (
              remoteFilters.value !== '' &&
              order.attributes.bp.id !== remoteFilters.value
            ) {
              popedElements.push(order);
            }
            break;

          case 'orderDateFrom':
            stringFilterDate = remoteFilters.params[0];
            filterDate = new Date(stringFilterDate);
            stringOrderDate = order.attributes.orderDate;

            stringOrderDate = stringOrderDate.replace(/T.*/, '');
            orderDate = new Date(stringOrderDate);

            if (filterDate.getTime() > orderDate.getTime()) {
              popedElements.push(order);
            }
            break;

          case 'orderDateTo':
            stringFilterDate = remoteFilters.params[0];
            filterDate = new Date(stringFilterDate);

            stringOrderDate = order.attributes.orderDate;
            stringOrderDate = stringOrderDate.replace(/T.*/, '');
            orderDate = new Date(stringOrderDate);

            if (filterDate.getTime() < orderDate.getTime()) {
              popedElements.push(order);
            }
            break;

          case 'totalamountFrom':
            if (order.attributes.gross < remoteFilters.params[0]) {
              popedElements.push(order);
            }
            break;

          case 'totalamountTo':
            if (order.attributes.gross > remoteFilters.params[0]) {
              popedElements.push(order);
            }
            break;

          default:
            break;
        }
      });
      popedElements.forEach(function(popedElement) {
        totalData.splice(totalData.indexOf(popedElement), 1);
      });
    });
    if (
      !OB.MobileApp.model.hasPermission(
        'OBPOS_SelectCurrentTicketsOnPaidOpen',
        true
      )
    ) {
      totalData.forEach(function(model) {
        model.set('checked', false);
      });
    }
    totalData = totalData.concat(data.models);
    data.models = totalData;
    data.length = totalData.length;
    data.crossStoreInfo = false;

    if (data && data.length > 0) {
      _.each(
        data.models,
        function(model) {
          if (OB.UTIL.isCrossStoreReceipt(model)) {
            data.crossStoreInfo = true;
            return;
          }
        },
        this
      );

      _.each(
        data.models,
        function(model) {
          model.crossStoreInfo = data.crossStoreInfo;
        },
        this
      );
    }
  }
});

enyo.kind({
  name: 'OB.UI.ModalPayOpenTicketsScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalPayOpenTicketsScrollableHeader',
  filterModel: OB.Model.VReturnsFilter,
  events: {
    onSearchAction: ''
  },
  components: [
    {
      kind: 'OB.UI.FilterSelectorTableHeader',
      name: 'filterSelector',
      classes: 'obUiModalPayOpenTicketsScrollableHeader-filterSelector'
    },
    {
      classes: 'obUiModalPayOpenTicketsScrollableHeader-container1',
      components: [
        {
          classes:
            'obUiModalPayOpenTicketsScrollableHeader-container1-container1',
          components: [
            {
              classes:
                'obUiModalPayOpenTicketsScrollableHeader-container1-container1-container1',
              components: [
                {
                  kind: 'OBPOS.UI.AdvancedFilterWindowButtonPayOpenTickets',
                  classes:
                    'obUiModalPayOpenTicketsScrollableHeader-container1-container1-container1-obposUiAdvancedFilterWindowButtonVerifiedReturns'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.filters = this.filterModel.getFilterPropertiesWithSelectorPreference();
    this.inherited(arguments);
    this.$.filterSelector.$.entityFilterText.skipAutoFilterPref = true;
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterVerifiedReturns',
  model: OB.Model.VReturnsFilter,
  initComponents: function() {
    this.inherited(arguments);
    OB.UTIL.hideStoreFilter(OB.Model.VReturnsFilter.getProperties());
    this.setFilters(OB.Model.VReturnsFilter.getProperties());
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBPOS.UI.AdvancedFilterWindowButtonPayOpenTickets',
  classes: 'obposUiAdvancedFilterWindowButtonPayOpenTickets',
  dialog: 'modalAdvancedFilterVerifiedReturns'
});

enyo.kind({
  name: 'OB.UI.ModalMultiOrdersPayOpenTickets',
  kind: 'OB.UI.ModalSelector',
  classes: 'u-popup-top-separation-medium obUiModalMultiOrdersPayOpenTickets',
  i18nHeader: 'OBPOS_LblPaidReceipts',
  published: {
    params: null
  },
  body: {
    kind: 'OB.UI.ReceiptsForPayOpenTicketsList',
    classes:
      'obUiModalMultiOrdersPayOpenTickets-obUiReceiptsForPayOpenTicketsList'
  },
  getFilterSelectorTableHeader: function() {
    return this.$.body.$.receiptsForPayOpenTicketsList.$
      .payOpenTicketsReceiptsListItemPrinter.$.theader.$
      .modalPayOpenTicketsScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function() {
    return this.$.body.$.receiptsForPayOpenTicketsList.$
      .payOpenTicketsReceiptsListItemPrinter.$.theader.$
      .modalPayOpenTicketsScrollableHeader.$
      .advancedFilterWindowButtonVerifiedReturns;
  },
  getAdvancedFilterDialog: function() {
    return 'modalAdvancedFilterVerifiedReturns';
  },
  executeOnShow: function() {
    var me = this;
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
    if (
      !OB.MobileApp.model.get('connectedToERP') ||
      OB.MobileApp.model.hasPermission(
        'OBPOS_SelectCurrentTicketsOnPaidOpen',
        true
      )
    ) {
      _.each(me.model.get('orderList').models, function(iter) {
        if (iter.get('lines') && iter.get('lines').length > 0) {
          if (
            (iter.get('orderType') === 0 || iter.get('orderType') === 2) &&
            !iter.get('isPaid') &&
            !iter.get('isQuotation') &&
            iter.get('gross') >= 0
          ) {
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
  init: function(model) {
    this.model = model;
    this.$.header.createComponent({
      kind: 'OB.UI.ModalMultiOrdersTopHeader',
      classes:
        'obUiModalMultiOrdersPayOpenTickets-header-obUiModalMultiOrdersTopHeader'
    });
  }
});

enyo.kind({
  name: 'OB.UI.ListMultiOrdersLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'obUiListMultiOrdersLine',
  events: {
    onHideThisPopup: ''
  },
  tap: function() {
    this.inherited(arguments);
    if (
      this.model.crossStoreInfo &&
      OB.UTIL.isCrossStoreReceipt(this.model) &&
      !this.model.get('checked')
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_LblCrossStorePayment'),
        OB.I18N.getLabel('OBPOS_LblCrossStoreMessage', [
          this.model.get('documentNo'),
          this.model.get('store')
        ]) +
          '. ' +
          OB.I18N.getLabel('OBPOS_LblCrossStoreDelivery'),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_Continue'),
            isConfirmButton: true,
            args: {
              model: this.model
            },
            action: function() {
              this.args.model.set('checked', !this.args.model.get('checked'));
              this.args.model.trigger('verifyDoneButton', this.args.model);
              return true;
            }
          },
          {
            label: OB.I18N.getLabel('OBMOBC_LblCancel'),
            args: {
              button: this
            },
            action: function() {
              this.args.button.removeClass('active');
              return true;
            }
          }
        ]
      );
    } else {
      this.model.set('checked', !this.model.get('checked'));
      this.model.trigger('verifyDoneButton', this.model);
    }
  },
  components: [
    {
      name: 'line',
      classes: 'obUiListMultiOrdersLine-line',
      components: [
        {
          name: 'store',
          classes: 'obUiListMultiOrdersLine-line-store'
        },
        {
          classes: 'obUiListMultiOrdersLine-line-element1'
        },
        {
          name: 'topLine',
          classes: 'obUiListMultiOrdersLine-line-topLine'
        },
        {
          name: 'isLayaway',
          classes: 'obUiListMultiOrdersLine-line-isLayaway'
        },
        {
          name: 'bottonLine',
          classes: 'obUiListMultiOrdersLine-line-bottonLine'
        },
        {
          classes: 'obUiListMultiOrdersLine-line-element2'
        }
      ]
    }
  ],
  create: function() {
    var returnLabel = '';
    this.inherited(arguments);
    if (this.model.crossStoreInfo) {
      this.$.store.setContent(
        OB.UTIL.isCrossStoreReceipt(this.model)
          ? this.model.get('store')
          : OB.I18N.getLabel('OBPOS_LblThisStore', [
              OB.MobileApp.model.get('terminal').organization$_identifier
            ])
      );
    } else {
      this.$.store.setContent('');
    }
    if (
      this.model.get('documentTypeId') ===
      OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns
    ) {
      this.model.set(
        'totalamount',
        OB.DEC.mul(this.model.get('totalamount'), -1)
      );
      returnLabel = ' (' + OB.I18N.getLabel('OBPOS_ToReturn') + ')';
    }
    this.$.topLine.setContent(
      this.model.get('documentNo') +
        ' - ' +
        (this.model.get('bp')
          ? this.model.get('bp').get('_identifier')
          : this.model.get('businessPartnerName')) +
        returnLabel
    );
    this.$.bottonLine.setContent(
      (this.model.get('totalamount') || this.model.get('totalamount') === 0
        ? this.model.get('totalamount')
        : this.model.getGross()) +
        ' (' +
        OB.I18N.formatDate(new Date(this.model.get('orderDate'))) +
        ') '
    );
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }

    switch (this.model.get('orderType')) {
      case 'ORD':
        this.$.isLayaway.setContent(OB.I18N.getLabel('OBPOS_LblAssignReceipt'));
        this.$.isLayaway.setClasses('payOpenTicketsIsReceipt');
        break;

      case 'LAY':
        this.$.isLayaway.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
        this.$.isLayaway.setClasses('payOpenTicketsIsLayaway');
        break;

      default:
        break;
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
  classes: 'obUiModalMultiOrdersTopHeader',
  events: {
    onHideThisPopup: '',
    onSelectMultiOrders: '',
    onTabChange: '',
    onRightToolDisabled: ''
  },
  components: [
    {
      classes: 'obUiModalMultiOrdersTopHeader-container1',
      components: [
        {
          classes: 'obUiModalMultiOrdersTopHeader-container1-container1',
          components: [
            {
              name: 'doneMultiOrdersButton',
              kind: 'OB.UI.SmallButton',
              classes:
                'obUiModalMultiOrdersTopHeader-container1-container1-doneMultiOrdersButton',
              ontap: 'doneAction'
            }
          ]
        },
        {
          classes: 'obUiModalMultiOrdersTopHeader-container1-container2',
          components: [
            {
              name: 'title',
              classes:
                'obUiModalMultiOrdersTopHeader-container1-container2-title'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.title.setContent(OB.I18N.getLabel('OBPOS_LblMultiOrders'));
    this.$.doneMultiOrdersButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
  },
  disableDoneButton: function(value) {
    this.$.doneMultiOrdersButton.setDisabled(value);
  },
  doneAction: function() {
    var execution = OB.UTIL.ProcessController.start('payOpenTicketsValidation'),
      selectedMultiOrders = [],
      alreadyPaidOrders = [],
      alreadyPaidOrdersDocNo = '',
      me = this,
      process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.PaidReceipts'
      ),
      checkedMultiOrders = _.compact(
        this.parent.parent.parent.$.body.$.receiptsForPayOpenTicketsList.receiptList.map(
          function(e) {
            if (e.get('checked')) {
              return e;
            }
          }
        )
      ),
      addOrdersToOrderList,
      i,
      j,
      wrongOrder,
      firstCheck = true,
      cancellingOrdersToCheck = me.owner.owner.model.get('orderList').models,
      showSomeOrderIsPaidPopup;

    if (checkedMultiOrders.length === 0) {
      OB.UTIL.ProcessController.finish('payOpenTicketsValidation', execution);
      return true;
    }

    showSomeOrderIsPaidPopup = function() {
      OB.UTIL.StockUtils.checkOrderLinesStock(selectedMultiOrders, function(
        hasStock
      ) {
        if (hasStock) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PreMultiOrderHook',
            {
              selectedMultiOrders: selectedMultiOrders
            },
            function(args) {
              if (args && args.cancellation) {
                OB.UTIL.ProcessController.finish(
                  'payOpenTicketsValidation',
                  execution
                );
                return;
              }
              me.doSelectMultiOrders({
                value: selectedMultiOrders,
                callback: function() {
                  OB.UTIL.ProcessController.finish(
                    'payOpenTicketsValidation',
                    execution
                  );
                  me.showPaymentView();
                }
              });
            }
          );
        } else {
          OB.UTIL.ProcessController.finish(
            'payOpenTicketsValidation',
            execution
          );
        }
      });
    };

    addOrdersToOrderList = _.after(checkedMultiOrders.length, function() {
      if (alreadyPaidOrdersDocNo) {
        if (checkedMultiOrders.length === alreadyPaidOrders.length) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_PaidOrderAllPaid', [alreadyPaidOrdersDocNo])
          );
          return;
        } else {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_Warning'),
            OB.I18N.getLabel('OBPOS_PaidOrder', [alreadyPaidOrdersDocNo]),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function() {
                  showSomeOrderIsPaidPopup();
                }
              }
            ],
            {
              onHideFunction: function() {
                OB.UTIL.ProcessController.finish(
                  'payOpenTicketsValidation',
                  execution
                );
              }
            }
          );
        }
      } else {
        showSomeOrderIsPaidPopup();
      }
    });

    OB.UTIL.showLoading(true);
    this.doHideThisPopup();
    // Check if the selected orders are payable by the 'Pay Open Tickets' flow
    for (i = 0; i < checkedMultiOrders.length; i++) {
      var iter = checkedMultiOrders[i];
      if (
        _.indexOf(this.owner.owner.model.get('orderList').models, iter) !== -1
      ) {
        // Check if there's an order with a reverse payment
        if (iter.isNewReversed()) {
          wrongOrder = {
            docNo: iter.get('documentNo'),
            problem: 'reversePayment'
          };
          break;
        }
      } else {
        //Check if there's an order that is being canceled/replaced
        var cancellingOrders = [];
        for (j = 0; j < cancellingOrdersToCheck.length; j++) {
          var order = cancellingOrdersToCheck[j];
          if (firstCheck) {
            if (order.get('canceledorder')) {
              if (order.get('canceledorder').id === iter.id) {
                wrongOrder = {
                  docNo: iter.get('documentNo'),
                  error: 'cancellingOrder'
                };
                break;
              }
              cancellingOrders.push(order);
            }
          } else {
            if (order.get('canceledorder').id === iter.id) {
              wrongOrder = {
                docNo: iter.get('documentNo'),
                error: 'cancellingOrder'
              };
              break;
            }
          }
        }
        if (wrongOrder) {
          break;
        }
        if (firstCheck) {
          firstCheck = false;
          cancellingOrdersToCheck = cancellingOrders;
        }
      }
    }
    // Stop if there's any order that cannot be paid using 'Pay Open Tickets'
    if (wrongOrder) {
      if (wrongOrder.error === 'reversePayment') {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_ReversePaymentPending', [wrongOrder.docNo])
        );
      } else if (wrongOrder.error === 'cancellingOrder') {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_CancellingOrder', [wrongOrder.docNo])
        );
      }
      OB.UTIL.ProcessController.finish('payOpenTicketsValidation', execution);
      OB.UTIL.showLoading(false);
      return;
    }
    this.owner.owner.model.deleteMultiOrderList();
    _.each(checkedMultiOrders, function(iter) {
      var idx = me.owner.owner.model
        .get('orderList')
        .map(function(order) {
          return order.id;
        })
        .indexOf(iter.id);
      if (idx !== -1) {
        var order = me.owner.owner.model.get('orderList').at(idx);
        order.getPrepaymentAmount(function() {
          order.set('checked', true);
          order.save();
          selectedMultiOrders.push(order);
          addOrdersToOrderList();
        });
      } else {
        process.exec(
          {
            orderid: iter.id,
            crossStore: OB.UTIL.isCrossStoreReceipt(iter)
              ? iter.get('organization')
              : null
          },
          function(data) {
            if (data) {
              me.owner.owner.model
                .get('orderList')
                .newPaidReceipt(data[0], function(order) {
                  if (
                    (order.get('isPaid') || order.get('isLayaway')) &&
                    order.getPayment() >= order.getGross()
                  ) {
                    OB.Dal.remove(order);
                    alreadyPaidOrders.push(order);
                    alreadyPaidOrdersDocNo = alreadyPaidOrdersDocNo.concat(
                      ' ' + order.get('documentNo')
                    );
                    addOrdersToOrderList();
                  } else {
                    order.set('loadedFromServer', true);
                    me.owner.owner.model
                      .get('orderList')
                      .addMultiReceipt(order);
                    order.set('checked', iter.get('checked'));
                    OB.DATA.OrderTaxes(order);
                    order.set('belongsToMultiOrder', true);
                    order.getPrepaymentAmount(function() {
                      order.calculateReceipt(function() {
                        selectedMultiOrders.push(order);
                        addOrdersToOrderList();
                      });
                    });
                  }
                });
            } else {
              OB.UTIL.ProcessController.finish(
                'payOpenTicketsValidation',
                execution
              );
              OB.UTIL.showLoading(false);
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
            }
          },
          function(data) {
            OB.UTIL.ProcessController.finish(
              'payOpenTicketsValidation',
              execution
            );
          }
        );
      }
    });
  },
  cancelAction: function() {
    this.doHideThisPopup();
  },
  showPaymentView: function() {
    OB.UTIL.showLoading(false);
    this.doTabChange({
      tabPanel: 'payment',
      keyboard: 'toolbarpayment',
      edit: false
    });
  }
});
