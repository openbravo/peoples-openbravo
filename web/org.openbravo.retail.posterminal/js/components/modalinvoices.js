/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalInvoicesHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalInvoicesHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onPopupOpened: 'popupOpened',
    onPopupClosed: 'popupClosed'
  },
  components: [],
  popupOpened: function(inSender, inEvent) {
    this.searchAction();
    return true;
  },
  popupClosed: function(inSender, inEvent) {
    this.clearAction();
    return true;
  },
  searchAction: function() {
    this.filters = {
      pos: OB.MobileApp.model.get('terminal').id,
      client: OB.MobileApp.model.get('terminal').client,
      organization: OB.MobileApp.model.get('terminal').organization,
      orderId: OB.MobileApp.model.receipt.get('id')
    };

    this.doSearchAction({
      filters: this.filters
    });
    return true;
  },
  clearAction: function() {
    this.doClearAction();
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListInvoicesLine',
  kind: 'OB.UI.Button',
  classes: 'obUiListInvoicesLine',
  tap: function() {
    this.inherited(arguments);
    this.model.set('checked', !this.model.get('checked'));
    this.model.trigger('verifyDoneButton', this.model);
  },
  labelComponents: {
    name: 'line',
    classes: 'obUiListInvoicesLine-line',
    components: [
      {
        name: 'topLine',
        classes: 'obUiListInvoicesLine-line-topLine'
      },
      {
        name: 'bottonLine',
        classes: 'obUiListInvoicesLine-line-bottonLine'
      },
      {
        classes: 'obUiListInvoicesLine-line-element1'
      }
    ]
  },
  toggle: function() {
    this.setChecked(!this.getChecked());
  },
  setChecked: function(value) {
    if (value) {
      this.check();
    } else {
      this.unCheck();
    }
  },
  getChecked: function(value) {
    return this.checked;
  },
  check: function() {
    this.addClass('active');
    this.checked = true;
  },
  unCheck: function() {
    this.removeClass('active');
    this.checked = false;
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.label.createComponent(this.labelComponents);
  },
  create: function() {
    this.inherited(arguments);
    this.$.label.$.topLine.setContent(
      this.model.get('documentNo') +
        ' - ' +
        (this.model.get('bp')
          ? this.model.get('bp').get('_identifier')
          : this.model.get('businessPartner'))
    );
    this.$.label.$.bottonLine.setContent(
      this.model.get('totalamount') +
        ' (' +
        OB.I18N.formatDate(new Date(this.model.get('orderDate'))) +
        ') '
    );
    this.setChecked(this.model.get('checked'));
    this.render();
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListInvoices',
  classes: 'obUiListInvoices',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  components: [
    {
      classes: 'obUiListInvoices-container1',
      components: [
        {
          classes: 'obUiListInvoices-container1-container1',
          components: [
            {
              classes: 'obUiListInvoices-container1-container1-container1',
              components: [
                {
                  name: 'invoiceslistitemprinter',
                  kind: 'OB.UI.ScrollableTable',
                  classes:
                    'obUiListInvoices-container1-container1-container1-invoiceslistitemprinter',
                  renderHeader: 'OB.UI.ModalInvoicesHeader',
                  renderLine: 'OB.UI.ListInvoicesLine',
                  renderEmpty: 'OB.UI.RenderEmpty'
                }
              ]
            }
          ]
        },
        {
          kind: 'OB.UI.ModalInvoicesFooter',
          name: 'footer',
          classes: 'obUiListInvoices-container1-footer'
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.invoicesList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    var me = this,
      processHeader = new OB.DS.Process(
        'org.openbravo.retail.posterminal.InvoicesHeader'
      );
    me.filters = inEvent.filters;
    var limit = OB.Model.Order.prototype.dataLimit;
    this.clearAction();
    processHeader.exec(
      {
        filters: me.filters,
        _limit: limit
      },
      function(data) {
        if (data && data.exception) {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_ErrorGettingInvoices') +
              ': ' +
              data.exception.message
          );
        } else if (data) {
          _.each(data, function(iter) {
            iter.orderDate = OB.I18N.parseServerDate(
              iter.orderDate
            ).toISOString();
            me.invoicesList.add(iter);
          });
        } else {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices'));
        }
      }
    );
    return true;
  },
  invoicesList: null,
  init: function(model) {
    var me = this;
    this.model = model;
    this.invoicesList = new Backbone.Collection();
    this.$.invoiceslistitemprinter.setCollection(this.invoicesList);
    this.invoicesList.on('verifyDoneButton', function(item) {
      if (item.get('checked')) {
        me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(
          false
        );
      } else {
        me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(
          true
        );
        _.each(me.invoicesList.models, function(e) {
          if (e.get('checked')) {
            me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(
              false
            );
            return;
          }
        });
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalInvoicesFooter',
  classes: 'obUiModalInvoicesFooter',
  events: {
    onHideThisPopup: '',
    onShowPopup: '',
    onPrintSingleReceipt: ''
  },
  handlers: {
    onPopupOpened: 'popupOpened'
  },
  processesToListen: ['invoicesPrintReceipt', 'invoicesPrintInvoices'],
  buttons: [
    {
      classes: 'obUiModalInvoicesFooter-printReceipt',
      name: 'printReceipt',
      kind: 'OB.UI.ModalDialogButton',
      ontap: 'printReceiptAction',
      i18nContent: 'OBPOS_LblPrintOneReceipt',
      isDefaultAction: true,
      position: 30
    },
    {
      classes: 'obUiModalInvoicesFooter-printInvoices',
      name: 'printInvoices',
      kind: 'OB.UI.ModalDialogButton',
      ontap: 'printInvoiceAction',
      i18nContent: 'OBPOS_LblPrintInvoices',
      position: 20
    }
  ],
  components: [
    {
      classes: 'obUiModal-footer-mainButtons',
      name: 'modalInvoicesFooter__buttonsContainer'
    }
  ],
  initComponents: function() {
    var strProto = '__proto__';
    var cancelButton = {
      classes: 'obUiModalInvoicesFooter-cancelButton',
      name: 'cancelButton',
      kind: 'OB.UI.ModalDialogButton',
      ontap: 'cancelAction',
      i18nContent: 'OBMOBC_LblCancel',
      position: 10
    };
    // Sort buttons by positions
    if (this.buttons && _.isArray(this.buttons)) {
      // Add cancel button
      var cancel = _.find(this.buttons, function(b) {
        return b.name === 'cancelButton';
      });
      if (!cancel) {
        this.buttons.push(cancelButton);
      }
      this.buttons.sort(function(a, b) {
        return a.position - b.position;
      });
    }
    // Create components
    if (
      this[strProto].kindComponents &&
      _.isArray(this[strProto].kindComponents) &&
      this[strProto].kindComponents.length > 0
    ) {
      this[strProto].kindComponents[0].components = [];
      _.each(
        this.buttons,
        function(btnToAdd, index) {
          this[strProto].kindComponents[0].components.push(btnToAdd);
          if (OB.UTIL.isNullOrUndefined(btnToAdd.ontap)) {
            var ontapFunctionName = btnToAdd.name + '_ontap';
            btnToAdd.ontap = ontapFunctionName;
            this[strProto][ontapFunctionName] = btnToAdd.buttonPressedFunction;
          }
        },
        this
      );
    }
    // Build the component
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  popupOpened: function(inSender, inEvent) {
    var me = this;
    this.disablePrintInvoicesButton(true);
    _.each(this.$.modalInvoicesFooter__buttonsContainer.children, function(
      btnComponent
    ) {
      if (btnComponent.popupOpenedFunction) {
        var bindOpenedFunction = btnComponent.popupOpenedFunction.bind(me);
        bindOpenedFunction(inEvent.model);
      }
    });
    return true;
  },
  processStarted: function(process, execution, processesInExec) {
    if (
      processesInExec.models.length === 1 &&
      process.get('executions').models.length === 1
    ) {
      this.disableButtons();
    }
  },
  processFinished: function(process, execution, processesInExec) {
    if (processesInExec.models.length === 0) {
      this.enableButtons();
    }
  },
  disableButtons: function() {
    this.$.printReceipt.setDisabled(true);
    this.$.printInvoices.setDisabled(true);
    this.$.cancelButton.setDisabled(true);
  },
  enableButtons: function() {
    var checkedInvoices = _.compact(
      this.parent.parent.parent.parent.$.body.$.listInvoices.invoicesList.map(
        function(e) {
          if (e.get('checked')) {
            return e;
          }
        }
      )
    );
    this.$.printReceipt.setDisabled(false);
    this.$.printInvoices.setDisabled(checkedInvoices.length === 0);
    this.$.cancelButton.setDisabled(false);
  },
  printReceiptAction: function() {
    var execution = OB.UTIL.ProcessController.start('invoicesPrintReceipt');
    this.owner.model.get('order').set('forceReceiptTemplate', true);
    this.doPrintSingleReceipt({
      callback: function() {
        OB.UTIL.ProcessController.finish('invoicesPrintReceipt', execution);
      }
    });
  },
  disablePrintInvoicesButton: function(value) {
    this.$.printInvoices.setDisabled(value);
  },
  printInvoiceAction: function() {
    var me = this,
      process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.OpenInvoices'
      ),
      checkedInvoices = _.compact(
        this.parent.parent.parent.parent.$.body.$.listInvoices.invoicesList.map(
          function(e) {
            if (e.get('checked')) {
              return e;
            }
          }
        )
      );
    if (checkedInvoices.length === 0) {
      return true;
    }
    var execution = OB.UTIL.ProcessController.start('invoicesPrintInvoices');

    process.exec(
      {
        invoices: checkedInvoices
      },
      function(data) {
        if (data && data.exception) {
          OB.error('Error getting invoices: ' + data.exception.message);
          OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
        } else if (data) {
          var printInvoice,
            finishPrintInvoices = _.after(data.length, function() {
              OB.UTIL.ProcessController.finish(
                'invoicesPrintInvoices',
                execution
              );
            });

          printInvoice = function(indx) {
            if (indx >= data.length) {
              return;
            }
            if (
              OB.UTIL.isNullOrUndefined(data[indx].receiptLines) ||
              data[indx].receiptLines.length === 0
            ) {
              finishPrintInvoices();
              printInvoice(++indx);
            } else {
              OB.UTIL.TicketListUtils.newPaidReceipt(data[indx], function(
                invoice
              ) {
                invoice.set('loadedFromServer', true);
                invoice.set('checked', true);
                invoice.set('belongsToMultiOrder', true);
                invoice.set('isInvoice', true);
                invoice.calculateGrossAndSave(false, function() {
                  try {
                    OB.UTIL.HookManager.executeHooks(
                      'OBPOS_PrePrintPaidReceipt',
                      {
                        context: this.model,
                        receipt: invoice
                      },
                      function(args) {
                        if (
                          args &&
                          args.cancelOperation &&
                          args.cancelOperation === true
                        ) {
                          finishPrintInvoices();
                          printInvoice(++indx);
                          return;
                        }
                        me.model.printReceipt.print(invoice);
                        finishPrintInvoices();
                        printInvoice(++indx);
                      }
                    );
                  } catch (e) {
                    OB.error('Error printing the receipt:' + e);
                    finishPrintInvoices();
                    printInvoice(++indx);
                  }
                });
              });
            }
          };

          printInvoice(0);
        } else {
          //error or offline
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices'));
          OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
        }
      },
      function(err) {
        //error or offline
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices'));
        OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
      }
    );
  },
  cancelAction: function() {
    this.doHideThisPopup();
  },
  init: function(model) {
    this.model = model;
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalInvoices',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalInvoices',
  executeOnHide: function() {
    this.waterfall('onPopupClosed');
  },
  executeOnShow: function() {
    this.waterfall('onPopupOpened', {
      model: this.model
    });
  },
  hideCloseButton: true,
  i18nHeader: 'OBPOS_LblInvoices',
  body: {
    kind: 'OB.UI.ListInvoices'
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  init: function(model) {
    this.model = model;
  }
});
