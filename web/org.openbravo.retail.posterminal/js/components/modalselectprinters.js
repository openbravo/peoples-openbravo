/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
    name: 'line',
    style: 'line-height: 23px; width: 100%; border-bottom: 1px solid #ccc;',
    components: [{
      style: 'float: left; width: 15%;',
      components: [{
        kind: 'OB.UI.RadioButton',
        name: 'selected',
        tap: function () {
          this.bubble('onSelectLine');
        }
      }]
    }, {
      style: 'float: left; width: 85%;  padding: 14px 0px  14px 0px;  color: #000; text-align: left;',
      name: 'printer'
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
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectPrinters',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges'
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
    return true;
  },

  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
    this.setHeader(OB.I18N.getLabel('OBPOS_SelectPrintersTitle'));

    // list all printers
    var printers = OB.POS.modelterminal.get('hardwareURL');
    this.printerscontainer = this.$.bodyContent.$.printerslist;

    //    _identifier: "The other printer"
    //      active: true
    //      client: "39363B0921BB4293B48383844325E84C"
    //      client$_identifier: "The White Valley Group"
    //      creationDate: "2015-09-17T17:38:34+02:00"
    //      hardwareURL: "http://localhost:8091"
    //      hasPDFPrinter: true
    //      hasReceiptPrinter: true
    //      id: "8AE1BD1A859F451DB184C116901BE219"
    //      name: "The other printer"
    //      organization: "D270A5AC50874F8BA67A88EE977F8E3B"
    //      organization$_identifier: "Vall Blanca Store"
    //      pOSTerminalType: "BD39916225594B32A88983899CF05F72"
    //      pOSTerminalType$_identifier: "VBS POS Terminal Type"
    //      __proto__: Object
    //      , 
    // Add Main URL
    var editline = this.printerscontainer.createComponent({
      kind: 'SelectPrintersLine',
      name: 'printerMain',
      printerscontainer: this.printerscontainer,
      printer: {
        _identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
        hardwareURL: OB.POS.hwserver.mainurl
      }
    });

    // Add the rest of URLs
    _.each(printers, function (printer) {
      this.printerscontainer.createComponent({
        kind: 'SelectPrintersLine',
        name: 'printerLine' + printer.id,
        printerscontainer: this.printerscontainer,
        printer: printer
      });
    }, this);
  },
  executeOnShow: function () {
    // Select the active URL
    this.printerscontainer.selectURL(OB.POS.hwserver.activeurl);
  },
  executeOnHide: function () {}
});