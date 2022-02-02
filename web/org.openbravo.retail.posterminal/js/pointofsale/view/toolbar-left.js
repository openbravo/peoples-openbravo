/*
 ************************************************************************************
 * Copyright (C) 2013-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

/*left toolbar*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
  classes: 'obObposPointOfSaleUiLeftToolbarButton',
  components: [
    {
      name: 'theButton',
      classes: 'obObposPointOfSaleUiLeftToolbarButton-theButton',
      attributes: {}
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  classes: 'obObposPointOfSaleUiLeftToolbar',
  components: [
    {
      tag: 'ul',
      classes: 'obObposPointOfSaleUiLeftToolbar-toolbar row-fluid',
      name: 'toolbar'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(
      this.buttons,
      function(btn) {
        this.$.toolbar.createComponent({
          kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
          classes:
            'obObposPointOfSaleUiLeftToolbar-toolbar-obObposPointOfSaleUiLeftToolbarButton',
          button: btn
        });
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.ButtonNew',
  kind: 'OB.UI.ToolbarButton',
  i18nContent: 'OBMOBC_NewReceipt',
  classes: 'obUiButtonNew',
  events: {
    onAddNewOrder: ''
  },
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  processesToListen: [
    'calculateReceipt',
    'addProduct',
    'servicePriceCalculation'
  ],
  disabled: false,
  isLocked: false,
  lastDisabledStatus: false,
  disableButton: function() {
    this.isLocked = true;
    this.setDisabledIfSynchronized();
  },
  enableButton: function() {
    this.isLocked = false;
    this.setDisabledIfSynchronized();
  },
  setButtonDisabled: function(value) {
    this.lastDisabledStatus = value;
    this.setDisabledIfSynchronized();
  },
  setDisabledIfSynchronized: function() {
    var value = this.lastDisabledStatus || this.isLocked || false;
    if (this.isLocked) {
      value = true;
    }
    if (
      OB.UTIL.ProcessController.getProcessesInExecByOBj(this).length > 0 &&
      !value
    ) {
      return true;
    }
    this.disabled = value;
    this.setDisabled(value);
  },
  disabledButton: function(inSender, inEvent) {
    this.updateDisabled(inEvent.disableButtonNew || inEvent.status);
  },
  updateDisabled: function(isDisabled) {
    this.setButtonDisabled(isDisabled);
    if (isDisabled) {
      this.addClass('empty');
    } else {
      this.removeClass('empty');
    }
  },
  init: function(model) {
    this.model = model;
  },
  tap: function() {
    var me = this;
    if (this.disabled) {
      return true;
    }
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreCreateNewReceipt',
      {
        model: this.model,
        context: this
      },
      function(args) {
        if (!args.cancelOperation) {
          if (me.model.get('leftColumnViewManager').isMultiOrder()) {
            me.model.deleteMultiOrderList();
            me.model.get('multiOrders').resetValues();
            me.model.get('leftColumnViewManager').setOrderMode();
          } else {
            if (
              OB.MobileApp.model.get('permissions')['OBPOS_print.suspended'] &&
              me.model.get('order').get('lines').length !== 0
            ) {
              me.model.get('order').trigger('print');
            }
          }
          me.doAddNewOrder();
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostAddNewReceipt',
            {
              model: me.model,
              context: me
            },
            function() {
              //Nothing to do
            }
          );
          return true;
        }
      }
    );
  }
});

enyo.kind({
  name: 'OB.UI.ButtonDelete',
  kind: 'OB.UI.ToolbarButton',
  i18nContent: 'OBMOBC_DeleteReceipt',
  i18nContentPaidTicket: 'OBMOBC_CloseReceipt',
  i18nContentMultiOrder: 'OBMOBC_Close',
  classes: 'obUiButtonDelete',
  events: {
    onShowPopup: '',
    onDeleteOrder: '',
    onRemoveMultiOrders: ''
  },
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  processesToListen: [
    'calculateReceipt',
    'addProduct',
    'tapTotalButton',
    'completeQuotation',
    'servicePriceCalculation'
  ],
  disabled: false,
  isLocked: false,
  lastDisabledStatus: false,
  disableButton: function() {
    this.isLocked = true;
    this.setDisabledIfSynchronized();
  },
  enableButton: function() {
    this.isLocked = false;
    this.setDisabledIfSynchronized();
  },
  setButtonDisabled: function(value) {
    this.lastDisabledStatus = value;
    this.setDisabledIfSynchronized();
  },
  setDisabledIfSynchronized: function() {
    var value = this.lastDisabledStatus || this.isLocked || false;
    if (this.isLocked) {
      value = true;
    }
    if (
      OB.UTIL.ProcessController.getProcessesInExecByOBj(this).length > 0 &&
      !value
    ) {
      return true;
    }
    this.disabled = value;
    this.setDisabled(value);
  },
  disabledButton: function(inSender, inEvent) {
    this.updateDisabled(inEvent.status);
  },
  updateDisabled: function(isDisabled) {
    this.setButtonDisabled(isDisabled);
    if (isDisabled) {
      this.addClass('empty');
    } else {
      this.removeClass('empty');
    }
  },
  tap: function() {
    var me = this,
      isMultiOrders = this.model.isValidMultiOrderState();

    if (this.disabled) {
      return true;
    }

    if (isMultiOrders) {
      this.doRemoveMultiOrders();
      return true;
    }

    // validate payments
    if (this.model.get('order').checkOrderPayment()) {
      this.setDisabled(false);
      return false;
    }

    // deletion without warning is allowed if the ticket has been processed
    if (this.model.get('order').isProcessedTicket()) {
      this.doDeleteOrder();
    } else {
      if (
        OB.MobileApp.model.hasPermission('OBPOS_approval.removereceipts', true)
      ) {
        //Show the pop up to delete or not
        this.doShowPopup({
          popup: 'modalConfirmReceiptDelete'
        });
      } else {
        OB.UTIL.Approval.requestApproval(
          this.model,
          'OBPOS_approval.removereceipts',
          function(approved) {
            if (approved) {
              //Delete the order without the popup
              me.doDeleteOrder({
                notSavedOrder: true
              });
            }
          }
        );
      }
    }
  },
  addPaidTicketClass: function() {
    this.addClass('paidticket');
    this.setLabel(OB.I18N.getLabel(this.i18nContentPaidTicket));
  },
  removePaidTicketClass: function() {
    this.removeClass('paidticket');
    this.setLabel(OB.I18N.getLabel(this.i18nContent));
  },
  addMultiOrderStatus: function() {
    this.addClass('paidticket');
    this.setLabel(OB.I18N.getLabel(this.i18nContentMultiOrder));
  },
  init: function(model) {
    this.model = model;
    this.model.get('leftColumnViewManager').on(
      'multiorder',
      function() {
        this.addMultiOrderStatus();
        return true;
      },
      this
    );
    this.model.get('leftColumnViewManager').on(
      'order',
      function() {
        this.removePaidTicketClass();
        if (this.model.get('order').isProcessedTicket()) {
          this.addPaidTicketClass();
        }
        this.bubble('onChangeTotal', {
          newTotal: this.model.get('order').getTotal()
        });
      },
      this
    );

    //    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
    //      if (model.get('isMultiOrders')) {
    //        this.addClass('paidticket');
    //      } else {
    //        this.removeClass('paidticket');
    //      }
    //      return true;
    //    }, this);
    this.model.get('order').on(
      'change:isPaid change:isQuotation change:isLayaway change:hasbeenpaid change:isModified change:isEditable',
      function(changedModel) {
        if (changedModel.isProcessedTicket()) {
          this.addPaidTicketClass();
          return;
        }
        this.removePaidTicketClass();
      },
      this
    );
    this.model.get('order').on(
      'showDiscount',
      function(model) {
        this.updateDisabled(true);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObposPointOfSaleUiButtonTabPayment',
  tabPanel: 'payment',
  i18nContent: 'OBMOBC_LblCheckout',
  handlers: {
    onChangedTotal: 'renderTotal',
    onRightToolbarDisabled: 'disabledButton'
  },
  processesToListen: [
    'calculateReceipt',
    'completeQuotation',
    'clearWith',
    'addProduct',
    'deleteLine',
    'servicePriceCalculation',
    'totalAmountValidation'
  ],
  isEnabled: true,
  disabledButton: function(inSender, inEvent) {
    if (inEvent.exceptionPanel === this.tabPanel) {
      return true;
    }
    this.isEnabled = !inEvent.status;
    this.disabledChanged(inEvent.status);
  },
  disableButton: function() {
    this.setDisabled(true);
    this.disabledChanged(true);
  },
  enableButton: function() {
    this.setDisabled(false);
    this.disabledChanged(false);
  },
  disabledChanged: function(isDisabled) {
    // logic decide if the button will be allowed to be enabled
    // the decision to enable the button is made based on several requirements that must be met
    var requirements,
      me = this,
      hasBeenPaid;
    if (
      OB.UTIL.ProcessController.getProcessesInExecByOBj(this).length > 0 &&
      !isDisabled
    ) {
      return true;
    }

    function requirementsAreMet(model) {
      // This function is in charge of managing all the requirements of the pay button to be enabled and disabled
      // Any attribute or parameter used to change the state of the button MUST be managed here
      requirements = {
        isModel: undefined,
        isReceipt: undefined,
        isMultiOrder: undefined,
        receiptId: undefined,
        isReceiptBp: undefined,
        receiptBpId: undefined,
        isReceiptLines: undefined,
        isReceiptLinesLengthGreaterThanZero: undefined,
        isReceiptHasbeenpaidEqualToN: undefined,
        isToolbarEnabled: undefined,
        isDisabledRequest: undefined,
        isLocallyGeneratedPayments: undefined
      };

      // If any requirement is not met, return false
      // Checks are grouped as objects are known to exist
      requirements.isDisabledRequest = isDisabled;
      if (requirements.isDisabledRequest) {
        return false;
      }
      requirements.isToolbarEnabled = me.isEnabled;
      if (!requirements.isToolbarEnabled) {
        return false;
      }
      requirements.isModel = !OB.UTIL.isNullOrUndefined(model);
      if (!requirements.isModel) {
        return false;
      }
      var receipt = model.get('order');
      requirements.isReceipt = !OB.UTIL.isNullOrUndefined(receipt);
      if (!requirements.isReceipt) {
        return false;
      }
      requirements.isMultiOrder = model
        .get('leftColumnViewManager')
        .isMultiOrder();
      if (requirements.isMultiOrder) {
        return true;
      }
      requirements.receiptId = receipt.get('id');
      requirements.isReceiptBp = !OB.UTIL.isNullOrUndefined(receipt.get('bp'));
      requirements.isReceiptLines = !OB.UTIL.isNullOrUndefined(
        receipt.get('lines')
      );
      if (
        OB.UTIL.isNullOrUndefined(requirements.receiptId) ||
        !requirements.isReceiptBp ||
        !requirements.isReceiptLines
      ) {
        return false;
      }
      requirements.receiptBpId = receipt.get('bp').get('id');
      requirements.isReceiptLinesLengthGreaterThanZero =
        receipt.get('lines').length > 0;
      requirements.isReceiptHasbeenpaidEqualToN =
        receipt.get('hasbeenpaid') === 'N';
      hasBeenPaid =
        receipt.get('isPaid') &&
        ((receipt.isNegative() &&
          receipt.getPrePaymentQty() <= receipt.getTotal()) ||
          (!receipt.isNegative() &&
            receipt.getPrePaymentQty() >= receipt.getTotal()));
      requirements.isLocallyGeneratedPayments = !OB.UTIL.isNullOrUndefined(
        receipt.get('payments').find(function(payment) {
          return !payment.get('isPrePayment');
        })
      );
      if (
        OB.UTIL.isNullOrUndefined(requirements.receiptBpId) ||
        (!requirements.isReceiptLinesLengthGreaterThanZero &&
          !requirements.isLocallyGeneratedPayments) ||
        !requirements.isReceiptHasbeenpaidEqualToN
      ) {
        return false;
      }
      // All requirements are met
      return true;
    }

    var newIsDisabledState;
    // [TODO] Must discuss if there is a better way to show discretionary discount pane
    var discountEdit = this.owner.owner.owner.owner.$.rightPanel.$.toolbarpane
      ? this.owner.owner.owner.owner.$.rightPanel.$.toolbarpane.$.edit.$
          .editTabContent.$.discountsEdit.showing
      : false;
    if (requirementsAreMet(this.model)) {
      newIsDisabledState = false;
      this.totalPrinter.show();
      if (!hasBeenPaid) {
        this.totalPrinter.removeClass(
          'obObposPointOfSaleUiButtonTabPayment-totalPrinter_black'
        );
        this.totalPrinter.addClass(
          'obObposPointOfSaleUiButtonTabPayment-totalPrinter_white '
        );
      }
    } else {
      newIsDisabledState = true;
      if (discountEdit) {
        this.totalPrinter.hide();
      } else if (OB.MobileApp.model.get('serviceSearchMode')) {
        this.totalPrinter.removeClass(
          'obObposPointOfSaleUiButtonTabPayment-totalPrinter_white '
        );
        this.totalPrinter.addClass(
          'obObposPointOfSaleUiButtonTabPayment-totalPrinter_black'
        );
      }
    }

    OB.UTIL.Debug.execute(function() {
      if (!requirements) {
        throw "The 'requirementsAreMet' function must have been called before this point";
      }
    });

    // Log the status and requirements of the pay button state
    // This log is used to keep control on the requests to enable and disable the button, and to have a quick
    // view of which requirements haven't been met if the button is disabled.
    // The enabling/disabling flow MUST go through this point to ensure that all requests are logged
    var msg = enyo.format(
      'Pay button is %s',
      newIsDisabledState ? 'disabled' : 'enabled'
    );
    if (
      newIsDisabledState === true &&
      requirements.isReceiptLinesLengthGreaterThanZero &&
      requirements.isReceiptHasbeenpaidEqualToN
    ) {
      msg += ' and should be enabled';
      OB.error(msg, requirements);
      OB.UTIL.Debug.execute(function() {
        throw msg;
      });
    } else {
      OB.debug(msg, requirements); // tweak this log level if the previous line is not enough
    }

    this.disabled = newIsDisabledState; // for getDisabled() to return the correct value
    this.setAttribute('disabled', newIsDisabledState); // to effectively turn the button enabled or disabled
    if (newIsDisabledState) {
      this.addClass('disabled');
    } else {
      this.removeClass('disabled');
    }
    if (hasBeenPaid && !newIsDisabledState) {
      this.totalPrinter.removeClass(
        'obObposPointOfSaleUiButtonTabPayment-totalPrinter_white'
      );
      this.addClass('obObposPointOfSaleUiButtonTabPayment_disabled');
    } else {
      this.removeClass('obObposPointOfSaleUiButtonTabPayment_disabled');
    }
  },
  events: {
    onTabChange: '',
    onClearUserInput: '',
    onShowPopup: ''
  },
  showPaymentTab: function() {
    var receipt = this.model.get('order'),
      me = this,
      roundedPayment,
      paymentStatus;
    if (receipt.get('isQuotation')) {
      OB.MobileApp.model.receipt.runCompleteTicket(
        OB.App.State.Global.completeQuotation,
        'completeQuotation'
      );
      return;
    }
    if (
      !this.model.get('order').get('isEditable') &&
      !this.model.get('order').get('isLayaway') &&
      !this.model.get('order').get('isPaid') &&
      this.model.get('order').get('orderType') !== 3
    ) {
      return true;
    }
    receipt.trigger('updatePending', true);
    if (this.model.get('order').get('orderType') === 3) {
      me.doTabChange({
        tabPanel: me.tabPanel,
        keyboard: 'toolbarpayment',
        edit: false
      });
    }

    OB.App.State.Global.displayTotal({
      ticket: this.model.get('leftColumnViewManager').isMultiOrder()
        ? OB.UTIL.TicketUtils.toMultiTicket(this.model.get('multiOrders'))
        : null
    });

    me.doTabChange({
      tabPanel: me.tabPanel,
      keyboard: 'toolbarpayment',
      edit: false
    });
    me.bubble('onShowColumn', {
      colNum: 1
    });

    OB.MobileApp.view.scanningFocus(true);
    if (OB.UTIL.RfidController.isRfidConfigured()) {
      OB.UTIL.RfidController.disconnectRFIDDevice();
    }

    if (receipt.get('payments').length > 0) {
      roundedPayment = _.find(receipt.get('payments').models, function(
        payment
      ) {
        return payment.has('paymentRoundingLine') && !payment.get('isPaid');
      });

      roundedPayment = OB.UTIL.isNullOrUndefined(roundedPayment)
        ? receipt.get('payments').models[
            receipt.get('payments').models.length - 1
          ]
        : roundedPayment;

      if (
        !OB.UTIL.isNullOrUndefined(roundedPayment) &&
        !roundedPayment.get('isPaid')
      ) {
        paymentStatus = receipt.getPaymentStatus();
        if (paymentStatus.pendingAmt > 0 || receipt.get('change') !== 0) {
          if (roundedPayment.has('paymentRoundingLine')) {
            receipt.removePayment(
              new OB.Model.PaymentLine(
                roundedPayment.get('paymentRoundingLine')
              ),
              null,
              async function() {
                roundedPayment.set('paymentRoundingLine', null);
                await OB.App.State.Ticket.addPaymentRounding({
                  payments: OB.MobileApp.model.get('payments'),
                  terminal: OB.MobileApp.model.get('terminal'),
                  payment: JSON.parse(JSON.stringify(roundedPayment))
                });
              }
            );
          }
        }
      }
    }
  },
  tap: function() {
    var me = this,
      criteria = {},
      paymentModels = OB.MobileApp.model.get('payments');
    if (this.disabled === false) {
      var execution = OB.UTIL.ProcessController.start('totalAmountValidation'),
        receipt = me.model.get('order'),
        receiptLines = receipt.get('lines').models,
        i;
      if (
        receipt.get('isQuotation') &&
        receipt.get('bp').id ===
          OB.MobileApp.model.get('terminal').businessPartner &&
        !OB.MobileApp.model.get('terminal').quotation_anonymouscustomer
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_quotationsOrdersWithAnonimousCust')
        );
        OB.UTIL.ProcessController.finish('totalAmountValidation', execution);
        return;
      }
      if (
        !OB.MobileApp.model.get('isMultiOrderState') &&
        receipt.isNegative()
      ) {
        var hasNoRefundablePayment =
          _.filter(paymentModels, function(payment) {
            return !payment.paymentMethod.refundable;
          }).length === paymentModels.length;
        if (
          hasNoRefundablePayment &&
          !OB.MobileApp.model.get('terminal').allowpayoncredit
        ) {
          OB.UTIL.showConfirmation.display(
            '',
            OB.I18N.getLabel('OBPOS_LblNoRefundablePayments'),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk')
              }
            ]
          );
          OB.UTIL.ProcessController.finish('totalAmountValidation', execution);
          return;
        }
      }

      for (i = 1; i < receipt.get('lines').models.length; i++) {
        if (
          receiptLines[0].get('organization').id !==
          receiptLines[i].get('organization').id
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_ReceiptLinesSameStore')
          );
          OB.UTIL.ProcessController.finish('totalAmountValidation', execution);
          return;
        }
      }

      if (receiptLines.length > 0) {
        if (!receiptLines[0].get('isVerifiedReturn')) {
          if (
            OB.UTIL.isCrossStoreReceipt(receipt) &&
            !OB.UTIL.isCrossStoreLine(receiptLines[0])
          ) {
            receipt.set(
              'warehouse',
              OB.MobileApp.model.get('warehouses')[0].warehouseid
            );
            receipt.set('priceList', OB.MobileApp.model.get('pricelist'));
          }
          receipt.set('organization', receiptLines[0].get('organization').id);
        }
        if (OB.UTIL.isCrossStoreReceipt(receipt)) {
          receipt.set('warehouse', receiptLines[0].get('warehouse').id);
          if (receiptLines[0].get('product').get('currentPrice')) {
            receipt.set(
              'priceList',
              receiptLines[0].get('product').get('currentPrice').priceListId
            );
          }
          if (receiptLines[0].has('documentType')) {
            if (receipt.get('isQuotation')) {
              receipt.set(
                'documentType',
                receiptLines[0].get('quotationDocumentType')
              );
            } else {
              receipt.set('documentType', receiptLines[0].get('documentType'));
            }
          }
        }
      }

      if (receipt.get('orderType') === 3) {
        OB.UTIL.ProcessController.finish('totalAmountValidation', execution);
        this.showPaymentTab();
        return;
      }
      OB.UTIL.StockUtils.checkOrderLinesStock([receipt], async function(
        hasStock
      ) {
        const completePayment = function() {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PrePaymentHook',
            {
              context: me.model,
              caller: me
            },
            function(args) {
              if (args && args.cancellation) {
                OB.UTIL.ProcessController.finish(
                  'totalAmountValidation',
                  execution
                );
                return;
              }
              OB.UTIL.HookManager.executeHooks(
                'OBPOS_PrePaymentApproval',
                {
                  context: me.model,
                  caller: me
                },
                function(args2) {
                  OB.UTIL.HookManager.executeHooks(
                    'OBPOS_CheckPaymentApproval',
                    {
                      approvals: [],
                      context: me.model,
                      caller: me
                    },
                    function(args3) {
                      function showPaymentTab() {
                        if (
                          !_.isUndefined(args3.approved) ? args3.approved : true
                        ) {
                          me.model.get('order').getPrepaymentAmount(function() {
                            OB.UTIL.ProcessController.finish(
                              'totalAmountValidation',
                              execution
                            );
                            me.showPaymentTab();
                          }, true);
                        } else {
                          OB.UTIL.ProcessController.finish(
                            'totalAmountValidation',
                            execution
                          );
                        }
                      }

                      if (
                        me.model.get('order').getGross() < 0 &&
                        !OB.MobileApp.model.get('permissions')[
                          'OBPOS_approval.returns'
                        ]
                      ) {
                        args3.approvals.push('OBPOS_approval.returns');
                      }
                      if (
                        OB.MobileApp.model.hasPermission(
                          'OBPOS_EnableLossSales',
                          true
                        )
                      ) {
                        var onHideModalLossSale = function() {
                            OB.UTIL.ProcessController.finish(
                              'totalAmountValidation',
                              execution
                            );
                          },
                          requestApprovalForLossSale = function(callback) {
                            var lines = OB.UTIL.LossSaleUtils.getLossSaleLines(
                              me.model.get('order')
                            );
                            if (lines.length) {
                              me.doShowPopup({
                                popup: 'OBPOS_modalLossSale',
                                args: {
                                  lossSaleLines: lines,
                                  onhide: onHideModalLossSale,
                                  callback: callback
                                }
                              });
                            } else {
                              callback({ requestApproval: false });
                            }
                          };

                        if (me.model.get('order').get('isEditable')) {
                          requestApprovalForLossSale(data => {
                            if (data.requestApproval) {
                              args3.approvals.push('OBPOS_approval.lossSales');
                              if (data.adjustPrice) {
                                args3.adjustPrice = true;
                                args3.lines = data.lines;
                              }
                            }
                            if (
                              args3.approvals.length > 0 &&
                              !receipt.get('isLayaway') &&
                              !receipt.get('isPaid')
                            ) {
                              OB.UTIL.Approval.requestApproval(
                                me.model,
                                args3.approvals,
                                function(approved) {
                                  if (approved) {
                                    if (args3.adjustPrice) {
                                      OB.UTIL.LossSaleUtils.adjustPriceOnLossSaleLines(
                                        args3.lines
                                      );
                                    }
                                    showPaymentTab();
                                  } else {
                                    OB.UTIL.ProcessController.finish(
                                      'totalAmountValidation',
                                      execution
                                    );
                                  }
                                }
                              );
                            } else if (!data.lossSaleNotValidated) {
                              showPaymentTab();
                            }
                          });
                        }
                      }
                    }
                  );
                }
              );
            }
          );
        };

        function successCallback(data) {
          if (
            data &&
            data.length > 0 &&
            !receipt.get('isPaid') &&
            !receipt.get('isLayaway')
          ) {
            OB.UTIL.ProcessController.finish(
              'totalAmountValidation',
              execution
            );
            receipt.trigger('showProductList', null, 'final', function() {
              execution = OB.UTIL.ProcessController.start(
                'totalAmountValidation'
              );
              completePayment();
              me.doClearUserInput();
            });
          } else {
            completePayment();
            me.doClearUserInput();
          }
        }

        function errorCallback(trx, error) {
          completePayment();
          me.doClearUserInput();
        }

        if (hasStock) {
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
            criteria.remoteFilters = [];
            criteria.remoteFilters.push({
              columns: [],
              operator: OB.Dal.FILTER,
              value: 'Final_Services',
              params: []
            });
            criteria.remoteFilters.push({
              columns: ['ispack'],
              operator: 'equals',
              value: false,
              fieldType: 'forceString'
            });
            OB.Dal.find(
              OB.Model.Product,
              criteria,
              function(data) {
                successCallback(data);
              },
              errorCallback
            );
          } else {
            criteria = new OB.App.Class.Criteria()
              .criterion('productType', 'S')
              .criterion('proposalType', 'FMA')
              .build();
            try {
              const products = await OB.App.MasterdataModels.Product.find(
                criteria
              );
              let data = [];
              for (let i = 0; i < products.length; i++) {
                data.push(OB.Dal.transform(OB.Model.Product, products[i]));
              }
              successCallback(data);
            } catch (error) {
              errorCallback(error);
            }
          }
        } else {
          OB.UTIL.ProcessController.finish('totalAmountValidation', execution);
        }
      });
    }
  },
  attributes: {},
  customComponents: [
    {
      kind: 'OB.UI.FitText',
      name: 'totalButtonDiv',
      minFontSize: 12,
      maxFontSize: 26,
      maxHeight: 57,
      classes: 'obObposPointOfSaleUiButtonTabPayment-totalButtonDiv buttonText',
      components: [
        {
          tag: 'span',
          name: 'totalPrinter',
          classes:
            'obObposPointOfSaleUiButtonTabPayment-totalButtonDiv-totalPrinter',
          renderTotal: function(total) {
            this.setContent(OB.I18N.formatCurrency(total));
            //It needs an small asynch to be rendered and then we can adaptFontSize
            setTimeout(
              function(me) {
                me.parent.rendered();
              },
              0,
              this
            );
          }
        }
      ]
    }
  ],
  getLabel: function() {
    return this.totalPrinter.getContent();
  },
  initComponents: function() {
    this.inherited(arguments);
    //FIXME: handle properly the css classes to show the required background depending on the status
    this.$.icon.createComponents(this.customComponents);
    this.totalPrinter = this.$.icon.$.totalPrinter;
    this.removeClass('btnlink-gray');
  },
  destroyComponents: function() {
    this.inherited(arguments);
  },
  renderTotal: function(inSender, inEvent) {
    this.totalPrinter.renderTotal(inEvent.newTotal);
    this.disabledChanged(false);
  },
  init: function(model) {
    this.model = model;
    this.model.get('order').on(
      'change:isEditable change:isLayaway change:orderType',
      function(newValue) {
        if (newValue) {
          if (
            !newValue.get('isEditable') &&
            !newValue.get('isLayaway') &&
            !newValue.get('isPaid') &&
            newValue.get('orderType') !== 3
          ) {
            this.tabPanel = null;
            this.disabledChanged(true);
            return;
          }
        }
        this.tabPanel = 'payment';
        this.disabledChanged(false);
      },
      this
    );
    this.model.get('order').on(
      'change:id',
      function() {
        this.disabledChanged(false);
      },
      this
    );
    // the button state must be set only once, in the initialization
    this.setDisabled(true);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
  kind: 'OB.OBPOSPointOfSale.UI.ToolbarImpl',
  classes: 'obObposPointOfSaleUiLeftToolbarImpl',
  buttons: [
    {
      kind: 'OB.UI.ButtonNew',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-obUiButtonNew'
    },
    {
      kind: 'OB.UI.ButtonDelete',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-ButtonDelete'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-ButtonTabPayment',
      name: 'btnTotalToPay'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
  }
});
