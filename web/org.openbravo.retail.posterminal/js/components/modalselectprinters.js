/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _, $ */

enyo.kind({
  name: 'SelectPrintersLine',
  printer: {},
  printerscontainer: null,
  handlers: {
    onSelectLine: 'selectLine'
  },
  components: [{
    style: 'line-height: 23px; width: 100%; padding-left: 15px; border-bottom: 1px solid #ccc;',
    components: [{
      components: [{
        kind: 'OB.UI.RadioButton',
        name: 'selected',
        style: 'float: left; padding-left: 60px; margin: 10px;',
        components: [{
          name: 'printer',
          style: 'padding: 5px 0px 0px 0px; font-size: 125%;'
        }],
        tap: function () {
          this.bubble('onSelectLine');
        }
      }]
    }, {
      style: 'clear: both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.printer.setContent(this.printer._identifier);
  },
  selectLine: function (inSender, inEvent) {
    this.printerscontainer.selectURL(this.printer.hardwareURL);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'SelectPrintersApply',
  i18nLabel: 'OBMOBC_LblApply',
  isDefaultAction: true,
  events: {
    onApplyChanges: ''
  },
  tap: function () {
    if (this.doApplyChanges()) {
      this.doHideThisPopup();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'SelectPrintersCancel',
  i18nLabel: 'OBMOBC_LblCancel',
  events: {
    onCancelChanges: ''
  },
  tap: function () {
    this.doCancelChanges();
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectPrinters',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCancelChanges: 'cancelChanges'
  },
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'printerslist',
      selectURL: function (url) {
        var isalreadychecked = false;

        // check radio of activeurl radio
        _.each(this.$, function (value, key, list) {
          if (!isalreadychecked && value.printer.hardwareURL === url) {
            value.$.selected.activeRadio();
            isalreadychecked = true;
          } else {
            value.$.selected.disableRadio();
          }
        }, this);
      },
      getActiveURL: function () {

        // check radio of activeurl radio
        var selected = _.find(this.$, function (value, key, list) {
          return value.$.selected.checked;
        }, this);
        return selected.printer.hardwareURL;
      }
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'SelectPrintersApply'
    }, {
      kind: 'SelectPrintersCancel'
    }]
  },

  applyChanges: function (inSender, inEvent) {
    OB.POS.hwserver.setActiveURL(this.printerscontainer.getActiveURL());
    this.args.actionExecuted = true;
    if (this.args.onSuccess) {
      this.args.onSuccess();
    }
    return true;
  },

  cancelChanges: function (inSender, inEvent) {
    this.args.actionExecuted = true;
    if (this.args.onCancel) {
      this.args.onCancel();
    }
  },

  initComponents: function () {
    this.inherited(arguments);
    this.printerscontainer = this.$.bodyContent.$.printerslist;
    this.autoDismiss = false;
    this.setHeader(OB.I18N.getLabel('OBPOS_SelectPrintersTitle'));

    // list all printers
    var printers = OB.POS.modelterminal.get('hardwareURL');

    // Add Main URL
    var editline = this.printerscontainer.createComponent({
      kind: 'SelectPrintersLine',
      name: 'printerMain',
      printerscontainer: this.printerscontainer,
      printer: {
        _identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
        hardwareURL: OB.POS.hwserver.mainurl
      }
    }).render();

    // Add the rest of URLs
    _.each(printers, function (printer) {
      if (printer.hasReceiptPrinter) {
        this.printerscontainer.createComponent({
          kind: 'SelectPrintersLine',
          name: 'printerLine' + printer.id,
          printerscontainer: this.printerscontainer,
          printer: printer
        }).render();
      }
    }, this);

    // Select the active URL
    this.printerscontainer.selectURL(OB.POS.hwserver.activeurl);
  },
  executeOnHide: function () {
    if (!this.args.actionExecuted && this.args.onHide) {
      this.args.onHide();
    }
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectPDFPrinters',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCancelChanges: 'cancelChanges'
  },
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'printerslist',
      selectURL: function (url) {
        var isalreadychecked = false;

        // check radio of activeurl radio
        _.each(this.$, function (value, key, list) {
          if (!isalreadychecked && value.printer.hardwareURL === url) {
            value.$.selected.activeRadio();
            isalreadychecked = true;
          } else {
            value.$.selected.disableRadio();
          }
        }, this);
      },
      getActiveURL: function () {

        // check radio of activeurl radio
        var selected = _.find(this.$, function (value, key, list) {
          return value.$.selected.checked;
        }, this);
        return selected.printer.hardwareURL;
      }
    }]
  },
  bodyButtons: {
    components: [{
      name: "SelectPDFPrintersApply",
      kind: 'SelectPrintersApply'
    }, {
      name: "SelectPDFPrintersCancel",
      kind: 'SelectPrintersCancel'
    }]
  },

  applyChanges: function (inSender, inEvent) {
    OB.POS.hwserver.setActivePDFURL(this.printerscontainer.getActiveURL());
    this.args.actionExecuted = true;
    if (this.args.onSuccess) {
      this.args.onSuccess();
    }
    return true;
  },

  cancelChanges: function (inSender, inEvent) {
    this.args.actionExecuted = true;
    if (this.args.onCancel) {
      this.args.onCancel();
    }
  },

  initComponents: function () {
    this.inherited(arguments);
    this.printerscontainer = this.$.bodyContent.$.printerslist;
    this.autoDismiss = false;
    this.setHeader(OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle'));

    // list all printers
    var printers = OB.POS.modelterminal.get('hardwareURL');

    // Add Main URL
    var editline = this.printerscontainer.createComponent({
      kind: 'SelectPrintersLine',
      name: 'PDFprinterMain',
      printerscontainer: this.printerscontainer,
      printer: {
        _identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
        hardwareURL: OB.POS.hwserver.mainurl
      }
    }).render();

    // Add the rest of URLs
    _.each(printers, function (printer) {
      if (printer.hasPDFPrinter) {
        this.printerscontainer.createComponent({
          kind: 'SelectPrintersLine',
          name: 'PDFprinterLine' + printer.id,
          printerscontainer: this.printerscontainer,
          printer: printer
        }).render();
      }
    }, this);

    // Select the active URL
    this.printerscontainer.selectURL(OB.POS.hwserver.activepdfurl);
  },
  executeOnHide: function () {
    if (!this.args.actionExecuted && this.args.onHide) {
      this.args.onHide();
    }
  }
});