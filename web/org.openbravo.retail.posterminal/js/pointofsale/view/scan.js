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
  classes: 'obObposPointOfSaleUiInDevHeader',
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
    classes: 'obObposPointOfSaleUiInDevHeader-innerDiv',
    components: [{
      name: 'headerText',
      kind: 'OB.UI.SmallButton',
      classes: 'obObposPointOfSaleUiInDevHeader-innerDiv-headerText span12',
      components: [{
        name: 'headerImg1',
        classes: 'obObposPointOfSaleUiInDevHeader-headerText-headerImg1',
        components: [{
          classes: 'obObposPointOfSaleUiInDevHeader-headerImg1-element1'
        }]
      }, {
        name: 'headerContentContainer',
        classes: 'obObposPointOfSaleUiInDevHeader-headerText-headerContentContainer',
        components: [{
          name: 'headerContent',
          classes: 'obObposPointOfSaleUiInDevHeader-headerContentContainer-headerContent'
        }]
      }, {
        name: 'headerImg2',
        classes: 'obObposPointOfSaleUiInDevHeader-headerText-headerImg2',
        components: [{
          classes: 'obObposPointOfSaleUiInDevHeader-headerImg2-element1'
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
    this.$.headerText.removeClass('obObposPointOfSaleUiInDevHeader-headerText_orange');
    this.$.headerText.removeClass('obObposPointOfSaleUiInDevHeader-headerText_red');
    this.$.headerText.addClass((OB.UTIL.Debug.isDebug() && OB.UTIL.Debug.getDebugCauses().isTestEnvironment) ? 'obObposPointOfSaleUiInDevHeader-headerText_orange' : 'obObposPointOfSaleUiInDevHeader-headerText_red');

    this.$.headerImg1.hide();
    this.$.headerImg2.hide();
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
  classes: 'obObposPointOfSaleUiScan',
  published: {
    receipt: null
  },
  additionalComponents: [],
  components: [{
    classes: 'obObposPointOfSaleUiScan-container1',
    components: [{
      kind: 'OB.UI.Clock',
      classes: 'obObposPointOfSaleUiScan-container1-obUiClock'
    }, {
      name: 'mainPanel',
      classes: 'obObposPointOfSaleUiScan-container1-mainPanel',
      components: [{
        name: 'msgwelcome',
        classes: 'obObposPointOfSaleUiScan-mainPanel-msgwelcome',
        showing: false,
        components: [{
          name: 'msgwelcomeLbl',
          classes: 'obObposPointOfSaleUiScan-msgwelcome-msgwelcomeLbl'
        }]
      }, {
        name: 'msgaction',
        classes: 'obObposPointOfSaleUiScan-mainPanel-msgaction',
        showing: false,
        components: [{
          name: 'txtaction',
          classes: 'obObposPointOfSaleUiScan-msgaction-txtaction span7'
        }, {
          classes: 'obObposPointOfSaleUiScan-msgaction-container2',
          components: [{
            name: 'undobutton',
            kind: 'OB.UI.SmallButton',
            i18nContent: 'OBMOBC_LblUndo',
            classes: 'obObposPointOfSaleUiScan-msgaction-container2-undobutton',
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
        name: 'divInDevHeader',
        classes: 'obObposPointOfSaleUiScan-mainPanel-divInDevHeader'
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
    var self = this;
    this.inherited(arguments);
    this.additionalComponents.forEach(function (component) {
      self.$.mainPanel.createComponent(component);
    });
    this.$.msgwelcomeLbl.setContent(OB.I18N.getLabel('OBPOS_WelcomeMessage'));
  }
});