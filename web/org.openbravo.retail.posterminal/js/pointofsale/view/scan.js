/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.InDevHeader',
  showing: false,
  handlers: {
    onInDevHeaderShow: 'doShowHeader'
  },
  events: {
    onShowPopup: ''
  },
  doShowHeader: function (inSender, inEvent) {
    this.show();
  },

  components: [{
    name: 'innerDiv',
    style: 'text-align: center; font-size: 30px; padding: 5px; padding-top: 0px;',
    components: [{
      name: 'headerText',
      kind: 'OB.UI.SmallButton',
      classes: 'span12 ' + (OB.UTIL.Debug.isDebug() && OB.UTIL.Debug.getDebugCauses().isTestEnvironment ? 'btnlink-orange' : 'btnlink-red'),
      style: '  height: 50px; margin: 5px 5px 0px 0px; font-size: 20px; cursor: pointer; font-weight: bold',
      tap: function () {
        this.owner.doShowPopup({
          popup: 'modalModulesInDev'
        });
      },
      content: '',
      init: function () {
        if (OB.UTIL.Debug.isDebug()) {
          var ifInDevelopment = 'OBPOS_ModulesInDevelopment';
          var ifInTestEnvironment = 'OBPOS_ApplicationInTestEnvironment';
          var i18nLabel = 'OBMOBC_Debug';
          if (OB.UTIL.Debug.getDebugCauses().isInDevelopment) {
            i18nLabel = ifInDevelopment;
          } else if (OB.UTIL.Debug.getDebugCauses().isTestEnvironment) {
            i18nLabel = ifInTestEnvironment;
          }
          this.setContent(OB.I18N.getLabel(i18nLabel));
        }
      }
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Scan',
  published: {
    receipt: null
  },
  components: [{
    style: 'position:relative; background-color: #7da7d9; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    }, {
      components: [{
        name: 'msgwelcome',
        showing: false,
        style: 'padding: 10px;',
        components: [{
          style: 'float:right;',
          name: 'msgwelcomeLbl'
        }]
      }, {
        name: 'msgaction',
        showing: false,
        components: [{
          name: 'txtaction',
          style: 'padding: 10px; float: left; width: 320px; line-height: 23px;'
        }, {
          style: 'float: right;',
          components: [{
            name: 'undobutton',
            kind: 'OB.UI.SmallButton',
            i18nContent: 'OBMOBC_LblUndo',
            classes: 'btnlink-white btnlink-fontblue',
            tap: function () {
              if (this.undoclick) {
                this.undoclick();
              }
            },
            init: function (model) {
              this.model = model;
            }
          }]
        }]
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.InDevHeader',
        style: 'height: 35px;',
        name: 'divInDevHeader'
      }]
    }]
  }],

  receiptChanged: function () {
    this.receipt.on('clear change:undo', function () {
      this.manageUndo();
    }, this);

    this.manageUndo();
  },

  manageUndo: function () {
    var undoaction = this.receipt.get('undo');

    if (undoaction) {
      this.$.msgwelcome.hide();
      this.$.msgaction.show();
      this.$.txtaction.setContent(undoaction.text);
      this.$.undobutton.undoclick = undoaction.undo;
    } else {
      this.$.msgaction.hide();
      this.$.msgwelcome.show();
      delete this.$.undobutton.undoclick;
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.msgwelcomeLbl.setContent(OB.I18N.getLabel('OBPOS_WelcomeMessage'));
  }
});