/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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
      classes: 'span12',
      style: 'height: 50px; margin: 5px 5px 0px 0px; padding: 0px; font-size: 20px; cursor: pointer; font-weight: bold;',
      components: [{
        name: 'headerImg1',
        style: 'float: left; margin: 8px 10px;',
        components: [{
          style: 'height: 32px; width: 35px; background:url(../org.openbravo.mobile.core/assets/img/Warning.png) no-repeat top left;'
        }]
      }, {
        name: 'headerContentContainer',
        style: 'height: 50px; display: inline-flex; align-items: center;',
        components: [{
          name: 'headerContent',
          style: 'flex: 1;'
        }]
      }, {
        name: 'headerImg2',
        style: 'float: right; margin: 8px 10px;',
        components: [{
          style: 'height: 32px; width: 35px; background:url(../org.openbravo.mobile.core/assets/img/Warning.png) no-repeat top left;'
        }]
      }],
      tap: function () {
        this.owner.doShowPopup({
          popup: 'modalModulesInDev'
        });
      },
      content: ''
    }]
  }],
  showHeaderContent: function () {
    var i18nLabel = 'OBMOBC_Debug';
    this.$.headerText.removeClass('btnlink-orange');
    this.$.headerText.removeClass('btnlink-red');
    this.$.headerText.addClass((OB.UTIL.Debug.isDebug() && OB.UTIL.Debug.getDebugCauses().isTestEnvironment) ? 'btnlink-orange' : 'btnlink-red');

    this.$.headerImg1.hide();
    this.$.headerImg2.hide();
    this.$.headerContentContainer.addStyles('width: calc(100% - 40px);');
    if (!OB.UTIL.isHTTPSAvailable()) {
      i18nLabel = 'OBPOS_NonSecureConnection';
    } else if (OB.UTIL.Debug.isDebug()) {
      if (OB.UTIL.Debug.getDebugCauses().isInDevelopment) {
        i18nLabel = 'OBPOS_ModulesInDevelopment';
      } else if (OB.UTIL.Debug.getDebugCauses().isTestEnvironment) {
        i18nLabel = 'OBPOS_ApplicationInTestEnvironment';
      }
    }
    this.$.headerContent.setContent(OB.I18N.getLabel(i18nLabel));
  },
  initComponents: function () {
    this.inherited(arguments);
    this.showHeaderContent();
  }
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
        style: 'padding: 6px;',
        components: [{
          style: 'float:right;',
          name: 'msgwelcomeLbl',
          classes: 'msgwelcomeLbl'
        }]
      }, {
        name: 'msgaction',
        showing: false,
        components: [{
          name: 'txtaction',
          style: 'overflow-x:hidden; overflow-y:auto; max-height:134px; padding: 10px; float: left; width: 400px; line-height: 23px',
          classes: 'enyo-scroller span7'
        }, {
          style: 'float: right;',
          components: [{
            name: 'undobutton',
            kind: 'OB.UI.SmallButton',
            i18nContent: 'OBMOBC_LblUndo',
            classes: 'btnlink-white btnlink-fontblue',
            tap: function () {
              var me = this,
                  undoaction = this.undoaction;
              this.setDisabled(true);
              OB.UTIL.HookManager.executeHooks('OBPOS_PreUndo_' + undoaction, {
                undoBtn: me,
                order: OB.MobileApp.model.receipt,
                selectedLines: OB.MobileApp.model.receipt.get('undo').lines
              }, function (args) {
                if (!args.cancellation && me.undoclick) {
                  me.undoclick();
                } else {
                  me.setDisabled(false);
                }
                OB.UTIL.HookManager.executeHooks('OBPOS_PostUndo_' + undoaction, {
                  undoBtn: me,
                  order: OB.MobileApp.model.receipt,
                  selectedLines: args.selectedLines
                });
              });
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
      this.$.undobutton.undoaction = undoaction.action;
      this.$.undobutton.undoclick = undoaction.undo;
      this.$.undobutton.setDisabled(false);
    } else {
      this.$.msgaction.hide();
      this.$.msgwelcome.show();
      this.$.undobutton.undoaction = null;
      delete this.$.undobutton.undoclick;
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.msgwelcomeLbl.setContent(OB.I18N.getLabel('OBPOS_WelcomeMessage'));
  }
});