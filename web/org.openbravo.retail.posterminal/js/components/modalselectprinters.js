/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'SelectPrintersLine',
  printer: {},
  classes: 'selectPrinterLine',
  printerscontainer: null,
  handlers: {
    onSelectLine: 'selectLine'
  },
  components: [
    {
      classes: 'selectPrinterLine-container1',
      components: [
        {
          classes: 'selectPrinterLine-container1-container1',
          components: [
            {
              kind: 'OB.UI.RadioButton',
              name: 'selected',
              classes: 'selectPrinterLine-container1-container1-selected',
              components: [
                {
                  name: 'printer',
                  classes: 'selectPrinterLine-selected-printer'
                }
              ],
              tap: function() {
                this.bubble('onSelectLine');
              }
            }
          ]
        },
        {
          classes: 'selectPrinterLine-container1-container2'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.printer.setContent(this.printer._identifier);
  },
  selectLine: function(inSender, inEvent) {
    this.printerscontainer.selectURL(this.printer.id);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'SelectPrintersApply',
  classes: 'selectPrintersApply',
  i18nLabel: 'OBMOBC_LblApply',
  isDefaultAction: true,
  events: {
    onApplyChanges: ''
  },
  tap: function() {
    if (this.doApplyChanges()) {
      this.doHideThisPopup();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'SelectPrintersCancel',
  classes: 'selectPrintersCancel',
  i18nLabel: 'OBMOBC_LblCancel',
  events: {
    onCancelChanges: ''
  },
  tap: function() {
    this.doCancelChanges();
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectPrinters',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalSelectPrinters',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCancelChanges: 'cancelChanges'
  },
  i18nHeader: 'OBPOS_SelectPrintersTitle',
  body: {
    kind: 'Scroller',
    thumb: true,
    classes: 'obUiModalSelectPrinters-body',
    components: [
      {
        name: 'printerslist',
        classes: 'obUiModalSelectPrinters-body-printerslist',
        selectURL: function(hardwareId) {
          var isalreadychecked = false;

          // check radio of activeurl radio
          _.each(
            this.$,
            function(value, key, list) {
              if (!isalreadychecked && value.printer.id === hardwareId) {
                value.$.selected.activeRadio();
                isalreadychecked = true;
              } else {
                value.$.selected.disableRadio();
              }
            },
            this
          );

          if (!isalreadychecked && Object.keys(this.$).length > 0) {
            this.$[Object.keys(this.$)[0]].$.selected.activeRadio();
          }
        },
        getActiveURL: function() {
          // check radio of activeurl radio
          var selected = _.find(
            this.$,
            function(value, key, list) {
              return value.$.selected.checked;
            },
            this
          );
          return selected.printer.id;
        }
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons obUiModalSelectPrinters-footer',
    components: [
      {
        classes: 'obUiModalSelectPrinters-footer-selectPrinterCancel',
        kind: 'SelectPrintersCancel'
      },
      {
        classes: 'obUiModalSelectPrinters-footer-selectPrinterApply',
        kind: 'SelectPrintersApply'
      }
    ]
  },

  applyChanges: function(inSender, inEvent) {
    OB.POS.hwserver.setActiveURL(this.printerscontainer.getActiveURL());
    this.args.actionExecuted = true;
    while (this.successCallbackArray.length !== 0) {
      this.successCallbackArray.pop()();
    }
    return true;
  },

  cancelChanges: function(inSender, inEvent) {
    this.args.actionExecuted = true;
    while (this.cancellCallbackArray.length !== 0) {
      this.cancellCallbackArray.pop()();
    }
  },

  initComponents: function() {
    this.inherited(arguments);
    this.printerscontainer = this.$.body.$.printerslist;
    this.autoDismiss = false;
    this.successCallbackArray = [];
    this.cancellCallbackArray = [];
    this.hideCallbackArray = [];

    // list all printers
    var printers = OB.POS.modelterminal.get('hardwareURL');

    // Add Main URL
    if (
      !_.find(printers, function(printer) {
        return (
          printer.hasReceiptPrinter &&
          printer.hardwareURL === OB.POS.hwserver.mainurl
        );
      })
    ) {
      this.printerscontainer
        .createComponent({
          kind: 'SelectPrintersLine',
          name: 'printerMain',
          printerscontainer: this.printerscontainer,
          printer: {
            _identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
            id: OB.MobileApp.model.get('terminal').id,
            hardwareURL: OB.POS.hwserver.mainurl
          }
        })
        .render();
    }

    // Add the rest of URLs
    _.each(
      printers,
      function(printer) {
        if (printer.hasReceiptPrinter) {
          this.printerscontainer
            .createComponent({
              kind: 'SelectPrintersLine',
              name: 'printerLine' + printer.id,
              printerscontainer: this.printerscontainer,
              printer: printer
            })
            .render();
        }
      },
      this
    );

    // Select the active URL
    this.printerscontainer.selectURL(OB.POS.hwserver.activeurl_id);
  },

  executeOnHide: function() {
    while (!this.args.actionExecuted && this.hideCallbackArray.length !== 0) {
      this.hideCallbackArray.pop()();
    }
    this.cleanBuffers();
  },

  executeOnShow: function() {
    if (this.args.onHide) {
      this.hideCallbackArray.push(this.args.onHide);
    }
    if (this.args.onCancel) {
      this.cancellCallbackArray.push(this.args.onCancel);
    }
    if (this.args.onSuccess) {
      this.successCallbackArray.push(this.args.onSuccess);
    }
    if (
      OB.MobileApp.model.get('terminal').terminalType.selectprinteralways &&
      !this.args.isRetry
    ) {
      this.closeOnEscKey = false;
      this.autoDismiss = false;
      this.$.footer.$.selectPrintersCancel.hide();
      this.$.closebutton.hide();
    } else {
      this.closeOnEscKey = true;
      this.autoDismiss = true;
      this.$.footer.$.selectPrintersCancel.show();
      this.$.closebutton.show();
    }

    if (OB.POS.hwserver.activeurl) {
      this.printerscontainer.selectURL(OB.POS.hwserver.activeurl_id);
    }
  },

  cleanBuffers: function() {
    this.hideCallbackArray.length = 0;
    this.cancellCallbackArray.length = 0;
    this.successCallbackArray.length = 0;
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectPDFPrinters',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalSelectPDFPrinters',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCancelChanges: 'cancelChanges'
  },
  i18nHeader: 'OBPOS_SelectPDFPrintersTitle',
  body: {
    classes: 'obUiModalSelectPDFPrinters-body',
    kind: 'Scroller',
    thumb: true,
    components: [
      {
        name: 'printerslist',
        classes: 'obUiModalSelectPDFPrinters-body-printerslist',
        selectURL: function(hardwareId) {
          var isalreadychecked = false;

          // check radio of activeurl radio
          _.each(
            this.$,
            function(value, key, list) {
              if (!isalreadychecked && value.printer.id === hardwareId) {
                value.$.selected.activeRadio();
                isalreadychecked = true;
              } else {
                value.$.selected.disableRadio();
              }
            },
            this
          );

          if (!isalreadychecked && Object.keys(this.$).length > 0) {
            this.$[Object.keys(this.$)[0]].$.selected.activeRadio();
          }
        },
        getActiveURL: function() {
          // check radio of activeurl radio
          var selected = _.find(
            this.$,
            function(value, key, list) {
              return value.$.selected.checked;
            },
            this
          );
          return selected.printer.id;
        }
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons obUiModalSelectPDFPrinters-footer',
    components: [
      {
        classes: 'obUiModalSelectPDFPrinters-footer-selectPDFPrintersCancel',
        name: 'SelectPDFPrintersCancel',
        kind: 'SelectPrintersCancel'
      },
      {
        classes: 'obUiModalSelectPDFPrinters-footer-selectPDFPrintersApply',
        name: 'SelectPDFPrintersApply',
        kind: 'SelectPrintersApply'
      }
    ]
  },

  applyChanges: function(inSender, inEvent) {
    OB.POS.hwserver.setActivePDFURL(this.printerscontainer.getActiveURL());
    this.args.actionExecuted = true;
    while (this.successCallbackArray.length !== 0) {
      this.successCallbackArray.pop()();
    }
    return true;
  },

  cancelChanges: function(inSender, inEvent) {
    this.args.actionExecuted = true;
    while (this.cancellCallbackArray.length !== 0) {
      this.cancellCallbackArray.pop()();
    }
  },

  initComponents: function() {
    this.inherited(arguments);
    this.printerscontainer = this.$.body.$.printerslist;
    this.autoDismiss = false;
    this.successCallbackArray = [];
    this.cancellCallbackArray = [];
    this.hideCallbackArray = [];

    // list all printers
    var printers = OB.POS.modelterminal.get('hardwareURL');

    // Add Main URL
    if (
      !_.find(printers, function(printer) {
        return (
          printer.hasPDFPrinter &&
          printer.hardwareURL === OB.POS.hwserver.mainurl
        );
      })
    ) {
      this.printerscontainer
        .createComponent({
          kind: 'SelectPrintersLine',
          name: 'PDFprinterMain',
          printerscontainer: this.printerscontainer,
          printer: {
            _identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
            id: OB.MobileApp.model.get('terminal').id,
            hardwareURL: OB.POS.hwserver.mainurl
          }
        })
        .render();
    }

    // Add the rest of URLs
    _.each(
      printers,
      function(printer) {
        if (printer.hasPDFPrinter) {
          this.printerscontainer
            .createComponent({
              kind: 'SelectPrintersLine',
              name: 'PDFprinterLine' + printer.id,
              printerscontainer: this.printerscontainer,
              printer: printer
            })
            .render();
        }
      },
      this
    );

    // Select the active URL
    this.printerscontainer.selectURL(OB.POS.hwserver.activepdfurl_id);
  },

  executeOnHide: function() {
    while (!this.args.actionExecuted && this.hideCallbackArray.length !== 0) {
      this.hideCallbackArray.pop()();
    }
    this.cleanBuffers();
  },

  executeOnShow: function() {
    if (this.args.onHide) {
      this.hideCallbackArray.push(this.args.onHide);
    }
    if (this.args.onCancel) {
      this.cancellCallbackArray.push(this.args.onCancel);
    }
    if (this.args.onSuccess) {
      this.successCallbackArray.push(this.args.onSuccess);
    }
    if (
      OB.MobileApp.model.get('terminal').terminalType.selectprinteralways &&
      !this.args.isRetry
    ) {
      this.closeOnEscKey = false;
      this.autoDismiss = false;
      this.$.footer.$.SelectPDFPrintersCancel.hide();
      this.$.closebutton.hide();
    } else {
      this.closeOnEscKey = true;
      this.autoDismiss = true;
      this.$.footer.$.SelectPDFPrintersCancel.show();
      this.$.closebutton.show();
    }
  },

  cleanBuffers: function() {
    this.hideCallbackArray.length = 0;
    this.cancellCallbackArray.length = 0;
    this.successCallbackArray.length = 0;
  }
});
