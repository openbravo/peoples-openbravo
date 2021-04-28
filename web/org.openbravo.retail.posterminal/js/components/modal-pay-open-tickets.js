/*
 ************************************************************************************
 * Copyright (C) 2018-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.UI.ReceiptsForPayOpenTicketsList',
  kind: 'OB.UI.GenericReceiptsList',
  classes: 'obUiReceiptsForPayOpenTicketsList',
  initComponents: function() {
    this.inherited(arguments);
    this.setFilterModel(OB.Model.PayOrderFilter);
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
        renderLine: 'OB.UI.ReceiptSelectorRenderLine',
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
    this.model = model;
    this.inherited(arguments);
    this.receiptList.on(
      'click',
      function(model) {
        if (this.readOnly) {
          return;
        }

        if (
          model.crossStoreInfo &&
          OB.UTIL.isCrossStoreReceipt(model) &&
          !model.get('receiptSelected')
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_LblCrossStorePayment'),
            OB.I18N.getLabel('OBPOS_LblCrossStoreMessage', [
              model.get('documentNo'),
              model.get('store')
            ]) +
              '. ' +
              OB.I18N.getLabel('OBPOS_LblCrossStoreDelivery'),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_Continue'),
                isConfirmButton: true,
                args: {
                  button: this,
                  model: model
                },
                action: function() {
                  this.args.button.waterfall('onChangeCheck', {
                    id: this.args.model.get('id')
                  });
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
                  return true;
                }
              }
            ]
          );
        } else {
          this.waterfall('onChangeCheck', {
            id: model.get('id')
          });
          model.trigger('verifyDoneButton', model);
        }
      },
      this
    );
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
      openedOrders = [],
      stringFilterDate,
      filterDate,
      stringOrderDate,
      orderDate,
      me = this;

    totalData = totalData.concat(OB.App.State.TicketList.Utils.getAllTickets());

    totalData.forEach(function(order) {
      if (
        order.lines.length === 0 ||
        order.grossAmount < 0 ||
        order.isPaid ||
        order.isQuotation ||
        order.orderType === 3
      ) {
        popedElements.push(order);
      } else {
        if (
          !OB.MobileApp.model.get('connectedToERP') ||
          OB.MobileApp.model.hasPermission(
            'OBPOS_SelectCurrentTicketsOnPaidOpen',
            true
          )
        ) {
          order.receiptSelected = true;
        }
        popedElements.push(order);
        openedOrders.push(me.createOpenedOrder(order));
      }
    });
    popedElements.forEach(function(popedElement) {
      totalData.splice(totalData.indexOf(popedElement), 1);
    });
    if (openedOrders.length > 0) {
      totalData = totalData.concat(openedOrders);
    }
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
              order.attributes.businessPartner !== remoteFilters.value
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
            if (order.attributes.totalamountFrom < remoteFilters.params[0]) {
              popedElements.push(order);
            }
            break;

          case 'totalamountTo':
            if (order.attributes.totalamountTo > remoteFilters.params[0]) {
              popedElements.push(order);
            }
            break;
          case 'orderType':
            if (
              remoteFilters.isId &&
              order.attributes.orderType !== remoteFilters.value
            ) {
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
    totalData = totalData.concat(data.models);
    if (
      !OB.MobileApp.model.hasPermission(
        'OBPOS_SelectCurrentTicketsOnPaidOpen',
        true
      )
    ) {
      totalData.forEach(function(model) {
        model.set('receiptSelected', false);
      });
    }

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
          model.set('multiselect', true);
        },
        this
      );
    }
  },
  createOpenedOrder: function(order) {
    return new Backbone.Model({
      id: order.id,
      documentTypeId: order.documentType,
      documentStatus: 'DR',
      orderDate: order.orderDate,
      creationDate: order.orderDate,
      totalamount: order.grossAmount || order.gross,
      businessPartnerName: order.businessPartner
        ? order.businessPartner.name
        : order.bp.name,
      organization: order.organization,
      documentNo: order.documentNo,
      businessPartner: order.businessPartner
        ? order.businessPartner.id
        : order.bp.id,
      externalBusinessPartner: order.externalBusinessPartner
        ? order.externalBusinessPartner
        : null,
      externalBusinessPartnerReference: order.externalBusinessPartnerReference
        ? order.externalBusinessPartnerReference
        : null,
      orderDateFrom: order.orderDate,
      orderDateTo: order.orderDate,
      totalamountFrom: order.grossAmount || order.gross,
      totalamountTo: order.grossAmount || order.gross,
      orderType: order.isLayaway ? 'LAY' : 'DR',
      iscancelled: false,
      store:
        order.organization === OB.MobileApp.model.get('terminal').organization
          ? OB.I18N.getLabel('OBPOS_LblThisStore', [
              OB.MobileApp.model.get('terminal').organization$_identifier
            ])
          : OB.MobileApp.model.get('terminal').organization$_identifier,
      receiptSelected: order.receiptSelected ? order.receiptSelected : false,
      multiselect: true
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalPayOpenTicketsScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalPayOpenTicketsScrollableHeader',
  filterModel: OB.Model.PayOrderFilter,
  events: {
    onSearchAction: ''
  },
  components: [
    {
      kind: 'OB.UI.FilterSelectorTableHeader',
      name: 'filterSelector',
      classes: 'obUiModalPayOpenTicketsScrollableHeader-filterSelector'
    }
  ],
  initComponents: function() {
    this.filters = this.filterModel.getFilterPropertiesWithSelectorPreference();
    OB.UTIL.hideStoreFilter(this.filters);
    this.inherited(arguments);
    this.$.filterSelector.$.formElementEntityFilterText.coreElement.skipAutoFilterPref = true;
  }
});

enyo.kind({
  name: 'OB.UI.ModalPayOpenTicketsFooter',
  classes: 'obUiModalPayOpenTicketsFooter',
  events: {
    onHideThisPopup: '',
    onSelectMultiOrders: '',
    onTabChange: '',
    onRightToolDisabled: ''
  },
  components: [
    {
      classes: 'obUiModalPayOpenTicketsFooter-container1',
      components: [
        {
          classes:
            'obUiModal-footer-secondaryButtons obUiModalPayOpenTicketsFooter-container1-container1',
          components: [
            {
              kind: 'OBPOS.UI.AdvancedFilterWindowButtonPayOpenTickets',
              classes:
                'obUiModalPayOpenTicketsFooter-container1-container1-obposUiAdvancedFilterWindowButtonVerifiedReturns'
            }
          ]
        },
        {
          classes:
            'obUiModal-footer-mainButtons obUiModalPayOpenTicketsFooter-container1-container2',
          components: [
            {
              kind: 'OB.UI.ModalDialogButton',
              classes: 'obUiModalAdvancedFilters-footer-container1-cancel',
              i18nLabel: 'OBMOBC_LblCancel',
              tap: function() {
                if (this.disabled === false) {
                  this.doHideThisPopup();
                }
              }
            },
            {
              name: 'doneMultiOrdersButton',
              kind: 'OB.UI.ModalDialogButton',
              i18nLabel: 'OBMOBC_LblDone',
              classes:
                'obUiModalPayOpenTicketsFooter-container1-container1-doneMultiOrdersButton',
              isDefaultAction: true,
              ontap: 'doneAction'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
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
        this.parent.parent.$.body.$.receiptsForPayOpenTicketsList.receiptList.map(
          function(e) {
            if (e.get('receiptSelected')) {
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
      cancellingOrdersToCheck = OB.App.State.TicketList.Utils.getAllTickets(),
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
                  OB.POS.terminal.$.containerWindow
                    .getRoot()
                    .$.multiColumn.$.panels.addClass(
                      'obUiMultiColumn-panels-showReceipt'
                    );
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
            OB.I18N.getLabel('OBPOS_PaidOrderAllPaid', [
              alreadyPaidOrdersDocNo
            ]),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function() {
                  OB.UTIL.ProcessController.finish(
                    'payOpenTicketsValidation',
                    execution
                  );
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

    this.doHideThisPopup();
    // Check if the selected orders are payable by the 'Pay Open Tickets' flow
    for (i = 0; i < checkedMultiOrders.length; i++) {
      var iter = checkedMultiOrders[i];
      iter.set('checked', true, { silent: true });
      if (
        _.indexOf(OB.App.State.TicketList.Utils.getAllTickets(), iter) !== -1
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
            if (order.canceledorder) {
              if (order.canceledorder.id === iter.id) {
                wrongOrder = {
                  docNo: iter.get('documentNo'),
                  error: 'cancellingOrder'
                };
                break;
              }
              cancellingOrders.push(order);
            }
          } else {
            if (order.canceledorder.id === iter.id) {
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
      return;
    }
    this.owner.owner.model.deleteMultiOrderList();
    _.each(checkedMultiOrders, async function(iter) {
      var idx = OB.App.State.TicketList.Utils.getAllTickets()
        .map(function(order) {
          return order.id;
        })
        .indexOf(iter.id);
      if (idx !== -1) {
        var orderState = OB.App.State.TicketList.Utils.getAllTickets()[idx];
        var order = OB.App.StateBackwardCompatibility.getInstance(
          'Ticket'
        ).toBackboneObject(orderState);

        order.set('checked', true);
        await OB.App.State.Global.checkTicketForPayOpenTickets({
          ticketId: orderState.id,
          checked: true
        });
        // order.save();
        selectedMultiOrders.unshift(order);
        addOrdersToOrderList();
      } else {
        process.exec(
          {
            orderid: iter.id,
            crossStore: OB.UTIL.isCrossStoreReceipt(iter)
              ? iter.get('organization')
              : null
          },
          function(data) {
            if (data && data.exception) {
              OB.UTIL.ProcessController.finish(
                'payOpenTicketsValidation',
                execution
              );
              OB.UTIL.showLoading(false);
              OB.UTIL.showConfirmation.display('', data.exception.message);
            } else if (data) {
              if (data[0].recordInImportEntry) {
                OB.UTIL.ProcessController.finish(
                  'payOpenTicketsValidation',
                  execution
                );
                OB.UTIL.showLoading(false);
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBMOBC_Error'),
                  OB.I18N.getLabel('OBPOS_ReceiptNotSynced', [
                    data[0].documentNo
                  ])
                );
                return;
              }
              OB.UTIL.TicketListUtils.newPaidReceipt(data[0], async function(
                order
              ) {
                if (
                  (order.get('isPaid') || order.get('isLayaway')) &&
                  order.getPayment() >= order.getGross()
                ) {
                  alreadyPaidOrders.push(order);
                  alreadyPaidOrdersDocNo = alreadyPaidOrdersDocNo.concat(
                    ' ' + order.get('documentNo')
                  );
                  addOrdersToOrderList();
                } else {
                  order.set('loadedFromServer', true);
                  order.set('checked', iter.get('checked'));
                  await OB.UTIL.TicketListUtils.addPaidReceipt(order);
                  OB.DATA.OrderTaxes(order);
                  order.set('belongsToMultiOrder', true);
                  order.calculateReceipt(function() {
                    selectedMultiOrders.unshift(order);
                    addOrdersToOrderList();
                  });
                }
              });
            } else {
              OB.UTIL.ProcessController.finish(
                'payOpenTicketsValidation',
                execution
              );
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
    this.doTabChange({
      tabPanel: 'payment',
      keyboard: 'toolbarpayment',
      edit: false
    });
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBPOS.UI.AdvancedFilterWindowButtonPayOpenTickets',
  classes: 'obposUiAdvancedFilterWindowButtonPayOpenTickets',
  dialog: 'OB_UI_ModalAdvancedFilterPayOpenTickets'
});

enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterPayOpenTickets',
  classes: 'obUiModalAdvancedFilterReceipts',
  model: OB.Model.PayOrderFilter,
  initComponents: function() {
    this.inherited(arguments);
    var filters = OB.Model.PayOrderFilter.getFilterPropertiesWithSelectorPreference();
    OB.UTIL.hideStoreFilter(filters);
    this.setFilters(filters);
  }
});

enyo.kind({
  name: 'OB.UI.ModalMultiOrdersPayOpenTickets',
  kind: 'OB.UI.ModalSelector',
  classes: 'obUiModalMultiOrdersPayOpenTickets',
  published: {
    params: null
  },
  i18nHeader: 'OBPOS_LblMultiOrders',
  body: {
    kind: 'OB.UI.ReceiptsForPayOpenTicketsList',
    classes:
      'obUiModalMultiOrdersPayOpenTickets-obUiReceiptsForPayOpenTicketsList'
  },
  footer: {
    kind: 'OB.UI.ModalPayOpenTicketsFooter'
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
    return 'OB_UI_ModalAdvancedFilterPayOpenTickets';
  },
  executeOnShow: function() {
    var me = this,
      isPaid,
      openOrder;
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
          isPaid =
            iter.get('payment') < iter.get('gross') &&
            OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
              ? false
              : iter.get('payment') >= iter.get('gross');
          if (
            (iter.get('orderType') === 0 || iter.get('orderType') === 2) &&
            !isPaid &&
            !iter.get('isQuotation') &&
            iter.get('gross') >= 0
          ) {
            if (!_.isNull(iter.id) && !_.isUndefined(iter.id)) {
              iter.set('receiptSelected', true);
              openOrder = me.$.body.$.receiptsForPayOpenTicketsList.createOpenedOrder(
                JSON.parse(JSON.stringify(iter))
              );
              me.$.body.$.receiptsForPayOpenTicketsList.receiptList.add(
                openOrder
              );
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
  }
});
