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
        if (OB.MobileApp.model.get('errorInPopup')) {
          OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_ProblemWithPopups'), OB.I18N.getLabel('OBPOS_ProblemWithPopupInfo'), [{
            isConfirmButton: true,
            label: OB.I18N.getLabel('OBMOBC_Reload'),
            action: function () {
              window.location.reload();
            }
          }, {
            label: OB.I18N.getLabel('OBMOBC_Continue')
          }], {
            style: 'background-color: red;'
          });
        } else {
          this.owner.doShowPopup({
            popup: 'modalModulesInDev'
          });
        }
      },
      content: ''
    }]
  }],
  showHeaderContent: function () {
    var i18nLabel = 'OBMOBC_Debug';
    this.$.headerText.removeClass('btnlink-orange');
    this.$.headerText.removeClass('btnlink-red');
    this.$.headerText.addClass((OB.UTIL.Debug.isDebug() && OB.UTIL.Debug.getDebugCauses().isTestEnvironment && !OB.MobileApp.model.get('errorInPopup')) ? 'btnlink-orange' : 'btnlink-red');
    if (OB.MobileApp.model.get('errorInPopup')) {
      i18nLabel = 'OBPOS_NotWorkingPopups';
      this.$.headerImg1.show();
      this.$.headerImg2.show();
      this.show();
      this.$.headerContentContainer.addStyles('width: calc(100% - 110px);');
    } else {
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
    }
    this.$.headerContent.setContent(OB.I18N.getLabel(i18nLabel));
  },
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    OB.MobileApp.model.set('errorInPopup', false);
    this.showHeaderContent();
    OB.MobileApp.model.on('change:errorInPopup', function (model) {
      me.showHeaderContent();
      me.$.headerText.tap();
    });
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
              OB.UTIL.HookManager.executeHooks('OBPOS_PreUndo_' + undoaction, {
                undoBtn: me,
                order: OB.MobileApp.model.receipt
              }, function (args) {
                if (!args.cancellation && me.undoclick) {
                  me.undoclick();
                }
                OB.UTIL.HookManager.executeHooks('OBPOS_PostUndo_' + undoaction, {
                  undoBtn: me,
                  order: OB.MobileApp.model.receipt
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