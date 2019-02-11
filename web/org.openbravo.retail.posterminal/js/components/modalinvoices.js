/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _  */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalInvoicesHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onPopupOpened: 'popupOpened',
    onPopupClosed: 'popupClosed'
  },
  components: [],
  popupOpened: function (inSender, inEvent) {
    this.searchAction();
    return true;
  },
  popupClosed: function (inSender, inEvent) {
    this.clearAction();
    return true;
  },
  searchAction: function () {
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
  clearAction: function () {
    this.doClearAction();
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListInvoicesLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check modal-invoices-list-invoices-line',
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
      style: 'color: #888888',
      name: 'bottonLine'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + (this.model.get('bp') ? this.model.get('bp').get('_identifier') : this.model.get('businessPartner')));
    this.$.bottonLine.setContent(this.model.get('totalamount') + ' (' + OB.I18N.formatDate(new Date(this.model.get('orderDate'))) + ') ');
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
    this.render();
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListInvoices',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'invoiceslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '300px',
          renderHeader: 'OB.UI.ModalInvoicesHeader',
          renderLine: 'OB.UI.ListInvoicesLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }, {
      kind: 'OB.UI.ModalInvoicesFooter',
      name: 'footer'
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.invoicesList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        processHeader = new OB.DS.Process('org.openbravo.retail.posterminal.InvoicesHeader');
    me.filters = inEvent.filters;
    var limit = OB.Model.Order.prototype.dataLimit;
    this.clearAction();
    processHeader.exec({
      filters: me.filters,
      _limit: limit
    }, function (data) {
      if (data && data.exception) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices') + ': ' + data.exception.message);
      } else if (data) {
        _.each(data, function (iter) {
          me.invoicesList.add(iter);
        });
      } else {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices'));
      }
    });
    return true;
  },
  invoicesList: null,
  init: function (model) {
    var me = this;
    this.model = model;
    this.invoicesList = new Backbone.Collection();
    this.$.invoiceslistitemprinter.setCollection(this.invoicesList);
    this.invoicesList.on('verifyDoneButton', function (item) {
      if (item.get('checked')) {
        me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(false);
      } else {
        me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(true);
        _.each(me.invoicesList.models, function (e) {
          if (e.get('checked')) {
            me.parent.parent.$.body.$.listInvoices.$.footer.disablePrintInvoicesButton(false);
            return;
          }
        });
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalInvoicesFooter',
  events: {
    onHideThisPopup: '',
    onShowPopup: '',
    onPrintReceipt: ''
  },
  handlers: {
    onPopupOpened: 'popupOpened'
  },
  processesToListen: ['invoicesPrintReceipt', 'invoicesPrintInvoices'],
  buttons: [{
    classes: 'modalInvoices__footer--button',
    name: 'printReceipt',
    kind: 'OB.UI.SmallButton',
    ontap: 'printReceiptAction',
    i18nContent: 'OBPOS_LblPrintOneReceipt',
    position: 10
  }, {
    classes: 'modalInvoices__footer--button',
    name: 'printInvoices',
    kind: 'OB.UI.SmallButton',
    ontap: 'printInvoiceAction',
    i18nContent: 'OBPOS_LblPrintInvoices',
    position: 20
  }],
  components: [{
    classes: 'modalInvoices__footer',
    name: 'modalInvoicesFooter__buttonsContainer'
  }],
  initComponents: function () {
    var strProto = '__proto__';
    var cancelButton = {
      classes: 'modalInvoices__footer--button modalInvoices__footer--buttoncancel',
      name: 'cancelButton',
      kind: 'OB.UI.SmallButton',
      ontap: 'cancelAction',
      i18nContent: 'OBMOBC_LblCancel'
    };
    // Sort buttons by possitions
    if (this.buttons && _.isArray(this.buttons)) {
      this.buttons.sort(function (a, b) {
        return a.position - b.position;
      });
      // Add cancel button
      var cancel = _.find(this.buttons, function (b) {
        return b.name === 'cancelButton';
      });
      if (!cancel) {
        this.buttons.push(cancelButton);
      }
    }
    // Create components
    if (this[strProto].kindComponents && _.isArray(this[strProto].kindComponents) && this[strProto].kindComponents.length > 0) {
      this[strProto].kindComponents[0].components = [];
      _.each(this.buttons, function (btnToAdd, index) {
        this[strProto].kindComponents[0].components.push(btnToAdd);
        if (OB.UTIL.isNullOrUndefined(btnToAdd.ontap)) {
          var ontapFunctionName = btnToAdd.name + '_ontap';
          btnToAdd.ontap = ontapFunctionName;
          this[strProto][ontapFunctionName] = btnToAdd.buttonPressedFunction;
        }
      }, this);
    }
    // Build the component
    this.inherited(arguments);
    // Calculate number of buttons
    if (this.buttons && _.isArray(this.buttons) && this.buttons.length > 1) {
      // Apply CSS class based on number of buttons
      this.$.modalInvoicesFooter__buttonsContainer.addClass('modalInvoices__footer--' + this.buttons.length);
    } else {
      OB.warn('OB.UI.ModalInvoicesFooter component requires at least one button');
    }
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function () {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  popupOpened: function (inSender, inEvent) {
    var me = this;
    this.disablePrintInvoicesButton(true);
    _.each(this.$.modalInvoicesFooter__buttonsContainer.children, function (btnComponent) {
      if (btnComponent.popupOpenedFunction) {
        var bindOpenedFunction = btnComponent.popupOpenedFunction.bind(me);
        bindOpenedFunction(inEvent.model);
      }
    });
    return true;
  },
  processStarted: function (process, execution, processesInExec) {
    if (processesInExec.models.length === 1 && process.get('executions').models.length === 1) {
      this.disableButtons();
    }
  },
  processFinished: function (process, execution, processesInExec) {
    if (processesInExec.models.length === 0) {
      this.enableButtons();
    }
  },
  disableButtons: function () {
    this.$.printReceipt.setDisabled(true);
    this.$.printInvoices.setDisabled(true);
    this.$.cancelButton.setDisabled(true);
  },
  enableButtons: function () {
    var checkedInvoices = _.compact(this.parent.parent.parent.parent.$.body.$.listInvoices.invoicesList.map(function (e) {
      if (e.get('checked')) {
        return e;
      }
    }));
    this.$.printReceipt.setDisabled(false);
    this.$.printInvoices.setDisabled(checkedInvoices.length === 0);
    this.$.cancelButton.setDisabled(false);
  },
  printReceiptAction: function () {
    var execution = OB.UTIL.ProcessController.start('invoicesPrintReceipt');
    this.owner.model.get('order').set('forceReceiptTemplate', true);
    this.doPrintReceipt({
      callback: function () {
        OB.UTIL.ProcessController.finish('invoicesPrintReceipt', execution);
      }
    });
  },
  disablePrintInvoicesButton: function (value) {
    this.$.printInvoices.setDisabled(value);
  },
  printInvoiceAction: function () {
    var me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.OpenInvoices'),
        checkedInvoices = _.compact(this.parent.parent.parent.parent.$.body.$.listInvoices.invoicesList.map(function (e) {
        if (e.get('checked')) {
          return e;
        }
      }));
    if (checkedInvoices.length === 0) {
      return true;
    }
    var execution = OB.UTIL.ProcessController.start('invoicesPrintInvoices');

    process.exec({
      invoices: checkedInvoices,
      originServer: undefined
    }, function (data) {
      if (data && data.exception) {
        OB.error('Error getting invoices: ' + data.exception.message);
        OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
      } else if (data) {

        var printInvoice, finishPrintInvoices = _.after(data.length, function () {
          OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
        });

        printInvoice = function (indx) {
          if (data.length === indx) {
            return;
          }
          if (data[indx].receiptLines.length === 0) {
            finishPrintInvoices();
            printInvoice(++indx);
          } else {
            me.owner.model.get('orderList').newPaidReceipt(data[indx], function (invoice) {
              invoice.set('loadedFromServer', true);
              invoice.set('checked', true);
              invoice.set('belongsToMultiOrder', true);
              invoice.set('isInvoice', true);
              invoice.calculateGrossAndSave(false, function () {
                try {
                  OB.UTIL.HookManager.executeHooks('OBPOS_PrePrintPaidReceipt', {
                    context: this.model,
                    receipt: invoice
                  }, function (args) {
                    if (args && args.cancelOperation && args.cancelOperation === true) {
                      finishPrintInvoices();
                      printInvoice(++indx);
                      return;
                    }
                    me.model.printReceipt.print(invoice, {
                      callback: function () {
                        finishPrintInvoices();
                        printInvoice(++indx);
                      }
                    });
                  });
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
    }, function (err) {
      //error or offline
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingInvoices'));
      OB.UTIL.ProcessController.finish('invoicesPrintInvoices', execution);
    });
  },
  cancelAction: function () {
    this.doHideThisPopup();
  },
  init: function (model) {
    this.model = model;
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalInvoices',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnHide: function () {
    this.waterfall('onPopupClosed');
  },
  executeOnShow: function () {
    this.waterfall('onPopupOpened', {
      model: this.model
    });
  },
  i18nHeader: 'OBPOS_LblInvoices',
  body: {
    kind: 'OB.UI.ListInvoices'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
  },
  init: function (model) {
    this.model = model;
  }
});