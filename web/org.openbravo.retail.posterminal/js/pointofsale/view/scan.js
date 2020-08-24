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
  doShowHeader: function(inSender, inEvent) {
    this.show();
  },
  components: [
    {
      name: 'innerDiv',
      classes: 'obObposPointOfSaleUiInDevHeader-innerDiv',
      components: [
        {
          name: 'headerText',
          kind: 'OB.UI.Button',
          classes: 'obObposPointOfSaleUiInDevHeader-innerDiv-headerText',
          tap: function() {
            this.owner.doShowPopup({
              popup: 'modalModulesInDev'
            });
          },
          content: ''
        }
      ]
    }
  ],
  showHeaderContent: function() {
    var i18nLabel = 'OBMOBC_Debug';
    this.$.headerText.addClass(
      OB.UTIL.Debug.isDebug() &&
        OB.UTIL.Debug.getDebugCauses().isTestEnvironment
        ? 'obObposPointOfSaleUiInDevHeader-innerDiv-headerText_warn'
        : 'obObposPointOfSaleUiInDevHeader-innerDiv-headerText_error'
    );

    if (!OB.UTIL.isHTTPSAvailable()) {
      i18nLabel = 'OBPOS_NonSecureConnection';
    } else if (OB.UTIL.Debug.isDebug()) {
      if (OB.UTIL.Debug.getDebugCauses().isInDevelopment) {
        i18nLabel = 'OBPOS_ModulesInDevelopment';
      } else if (OB.UTIL.Debug.getDebugCauses().isTestEnvironment) {
        i18nLabel = 'OBPOS_ApplicationInTestEnvironment';
      }
    }
    this.$.headerText.setLabel(OB.I18N.getLabel(i18nLabel));
  },
  initComponents: function() {
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
  components: [
    {
      kind: 'OB.UI.Clock',
      classes: 'obObposPointOfSaleUiScan-obUiClock'
    },
    {
      name: 'mainPanel',
      classes: 'obObposPointOfSaleUiScan-mainPanel',
      components: [
        {
          name: 'msgwelcome',
          classes: 'obObposPointOfSaleUiScan-mainPanel-msgwelcome',
          showing: false,
          components: [
            {
              name: 'msgwelcomeLbl',
              classes: 'obObposPointOfSaleUiScan-msgwelcome-msgwelcomeLbl'
            },
            {
              name: 'msgwelcomeLogo',
              classes: 'obObposPointOfSaleUiScan-msgwelcome-msgwelcomeLogo'
            }
          ]
        },
        {
          name: 'msgaction',
          classes: 'obObposPointOfSaleUiScan-mainPanel-msgaction',
          showing: false,
          components: [
            {
              name: 'txtaction',
              classes: 'obObposPointOfSaleUiScan-msgaction-txtaction'
            },
            {
              name: 'undobutton',
              kind: 'OB.UI.Button',
              i18nContent: 'OBMOBC_LblUndo',
              classes: 'obObposPointOfSaleUiScan-msgaction-undobutton',
              tap: async function() {
                if (OB.App.State.Ticket.isUndoAvailable()) {
                  await OB.App.State.Ticket.undo();
                } else {
                  var me = this,
                    undoaction = this.undoaction;
                  this.setDisabled(true);
                  OB.UTIL.HookManager.executeHooks(
                    'OBPOS_PreUndo_' + undoaction,
                    {
                      undoBtn: me,
                      order: OB.MobileApp.model.receipt,
                      selectedLines: OB.MobileApp.model.receipt.get('undo')
                        .lines
                    },
                    function(args) {
                      if (!args.cancellation && me.undoclick) {
                        me.undoclick();
                      } else {
                        me.setDisabled(false);
                      }
                      OB.UTIL.HookManager.executeHooks(
                        'OBPOS_PostUndo_' + undoaction,
                        {
                          undoBtn: me,
                          order: OB.MobileApp.model.receipt,
                          selectedLines: args.selectedLines
                        }
                      );
                    }
                  );
                }
              },
              init: function(model) {
                this.model = model;
              }
            }
          ]
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.InDevHeader',
          name: 'divInDevHeader',
          classes: 'obObposPointOfSaleUiScan-mainPanel-divInDevHeader'
        }
      ]
    }
  ],

  receiptChanged: function() {
    this.receipt.on(
      'clear change:undo',
      function() {
        this.manageUndo();
      },
      this
    );

    this.manageUndo();
  },

  manageUndo: function() {
    var undoaction = this.receipt.get('undo');

    if (undoaction || OB.App.State.Ticket.isUndoAvailable()) {
      this.$.msgwelcome.hide();
      this.$.msgaction.show();
      if (undoaction) {
        this.$.txtaction.setContent(undoaction.text);
        // TODO: Add undo action here
        this.$.undobutton.undoaction = undoaction.action;
        this.$.undobutton.undoclick = undoaction.undo;
      }
      this.$.undobutton.setDisabled(false);
    } else {
      this.$.msgaction.hide();
      this.$.msgwelcome.show();
      this.$.undobutton.undoaction = null;
      delete this.$.undobutton.undoclick;
    }
  },
  initComponents: function() {
    var self = this;
    this.inherited(arguments);
    this.additionalComponents.forEach(function(component) {
      self.$.mainPanel.createComponent(component);
    });
    this.$.msgwelcomeLbl.setContent(OB.I18N.getLabel('OBPOS_WelcomeMessage'));
  }
});
