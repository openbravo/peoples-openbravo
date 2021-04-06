/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

// Toolbar container
// ----------------------------------------------------------------------------
//enyo.kind({
//  name: 'OB.OBPOSPointOfSale.UI.RightToolbar',
//  handlers: {
//    onTabButtonTap: 'tabButtonTapHandler'
//  },
//  components: [{
//    tag: 'ul',
//    classes: 'unstyled nav-pos row-fluid',
//    name: 'toolbar'
//  }],
//  tabButtonTapHandler: function (inSender, inEvent) {
//    if (inEvent.tabPanel) {
//      this.setTabButtonSelected(inEvent.tabPanel);
//    }
//  },
//  setTabButtonSelected: function (tabName) {
//    var buttonContainerArray = this.$.toolbar.getComponents(),
//        i;
//
//    for (i = 0; i < buttonContainerArray.length; i++) {
//      buttonContainerArray[i].removeClass('selected');
//      if (buttonContainerArray[i].getComponents()[0].getComponents()[0].name === tabName) {
//        buttonContainerArray[i].addClass('selected');
//      }
//    }
//  },
//  manualTap: function (tabName, options) {
//    var tab;
//
//    function getButtonByName(name, me) {
//      var componentArray = me.$.toolbar.getComponents(),
//          i;
//      for (i = 0; i < componentArray.length; i++) {
//        if (componentArray[i].$.theButton.getComponents()[0].name === name) {
//          return componentArray[i].$.theButton.getComponents()[0];
//        }
//      }
//      return null;
//    }
//
//    tab = getButtonByName(tabName, this);
//    if (tab) {
//      tab.tap(options);
//    }
//  },
//  initComponents: function () {
//    this.inherited(arguments);
//    enyo.forEach(this.buttons, function (btn) {
//      this.$.toolbar.createComponent({
//        kind: 'OB.OBPOSPointOfSale.UI.RightToolbarButton',
//        button: btn
//      });
//    }, this);
//  }
//});
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
  kind: 'OB.OBPOSPointOfSale.UI.ToolbarImpl',
  classes: 'obObposPointOfSaleUiRightToolbarImpl',
  published: {
    receipt: null
  },
  events: {
    onShowMultiSelection: ''
  },
  handlers: {
    onTabButtonTap: 'tabButtonTapHandler'
  },
  lastSelectedTabPanel: '',
  lastButtonTab: '',
  tabButtonTapHandler: function(inSender, inEvent) {
    if (inEvent.tabPanel) {
      this.setTabButtonSelected(inEvent.tabPanel);
      if (this.lastSelectedTabPanel !== inEvent.tabPanel) {
        this.lastSelectedTabPanel = inEvent.tabPanel;
        if (inEvent.tabPanel === 'edit') {
          this.doShowMultiSelection({
            show: true
          });
        } else {
          this.doShowMultiSelection({
            show: false
          });
        }
      }
    }
  },
  setTabButtonSelected: function(tabName) {
    var buttonContainerArray = this.$.toolbar.getComponents(),
      i;

    for (i = 0; i < buttonContainerArray.length; i++) {
      buttonContainerArray[i].removeClass('selected');
      if (buttonContainerArray[i].setSelected) {
        buttonContainerArray[i].setSelected(false);
      }
      if (
        buttonContainerArray[i].tabToOpen === tabName &&
        buttonContainerArray[i].setSelected
      ) {
        buttonContainerArray[i].addClass('selected');
        buttonContainerArray[i].setSelected(true);
      }
    }
  },
  manualTap: function(tabName, options) {
    var tab, defaultTab;

    function getButtonByName(name, me) {
      var componentArray = me.$.toolbar.getComponents(),
        i;
      for (i = 0; i < componentArray.length; i++) {
        if (componentArray[i].tabToOpen === name && componentArray[i].showing) {
          return componentArray[i];
        }
      }
      return null;
    }

    tab = getButtonByName(tabName, this);
    if (options) {
      options.isManual = true;
    } else {
      options = {
        isManual: true
      };
    }

    if (tab) {
      tab.tap(options);
    } else {
      defaultTab = _.find(this.$.toolbar.getComponents(), function(component) {
        if (component.button.defaultTab) {
          return component;
        }
      }).$.theButton.getComponents()[0];
      defaultTab.tap(options);
    }
  },
  buttons: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabScan',
      name: 'toolbarBtnScan',
      classes: 'obObposPointOfSaleUiRightToolbarImpl-buttons-toolbarBtnScan',
      tabToOpen: 'scan',
      defaultTab: true
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabBrowse',
      name: 'toolbarBtnCatalog',
      classes: 'obObposPointOfSaleUiRightToolbarImpl-buttons-toolbarBtnCatalog',
      tabToOpen: 'catalog'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabSearchCharacteristic',
      name: 'toolbarBtnSearchCharacteristic',
      classes:
        'obObposPointOfSaleUiRightToolbarImpl-buttons-toolbarBtnSearchCharacteristic',
      tabToOpen: 'searchCharacteristic'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabEditLine',
      name: 'toolbarBtnEdit',
      classes: 'obObposPointOfSaleUiRightToolbarImpl-buttons-toolbarBtnEdit',
      tabToOpen: 'edit'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ButtonTabCart',
      name: 'toolbarBtnCart',
      classes: 'obObposPointOfSaleUiRightToolbarImpl-buttons-toolbarBtnCart',
      tabToOpen: 'receipt'
    }
  ],

  receiptChanged: function() {
    if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
      return;
    }
    this.receipt.on(
      'clear',
      function() {
        if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
          return;
        }
        this.waterfall('onChangeTotal', {
          newTotal: this.receipt.getTotal()
        });
        if (this.receipt.get('isEditable') === false) {
          this.manualTap('edit');
        } else {
          if (!OB.UI.MultiColumn.isSingleColumn()) {
            this.manualTap(OB.MobileApp.model.get('terminal').defaultwebpostab);
          }
        }
      },
      this
    );

    this.receipt.on(
      'scan',
      function(params) {
        if (OB.MobileApp.model.get('lastPaneShown') !== 'scan') {
          this.manualTap('scan', params);
        } else {
          this.owner.owner.$.rightPanel.$.keyboard.lastStatus = '';
          this.owner.owner.$.rightPanel.$.keyboard.setStatus('');
        }
      },
      this
    );

    this.receipt.get('lines').on(
      'click',
      function() {
        this.manualTap('edit');
      },
      this
    );

    //some button will draw the total
    this.bubble('onChangeTotal', {
      newTotal: this.receipt.getTotal()
    });
    this.receipt.on(
      'change:gross forceChangeTotal',
      function(model) {
        this.bubble('onChangeTotal', {
          newTotal: this.receipt.getTotal(),
          normalOrder: true
        });
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarButton',
  classes: 'obObposPointOfSaleUiRightToolbarButton',
  tag: 'li',
  components: [
    {
      name: 'theButton',
      classes: 'obObposPointOfSaleUiRightToolbarButton-theButton',
      attributes: {}
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    if (this.button.containerCssClass) {
      this.setClassAttribute(this.button.containerCssClass);
    }
    this.$.theButton.createComponent(this.button);
  }
});

// Toolbar buttons
// ----------------------------------------------------------------------------
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabCart',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObposPointOfSaleUiButtonTabCart',
  events: {
    onTabChange: '',
    onRightToolbarDisabled: ''
  },
  handlers: {
    // on single column, left always enable the cart button
    onRightToolbarDisabled: ''
  },
  init: function(model) {
    this.model = model;
  },
  disabledButton: function(inSender, inEvent) {
    var isDisabled = inEvent.status;
    this.isEnabled = !inEvent.status;
    this.setDisabled(isDisabled);
    this.setDisabled(inEvent.status);
    //    if (!this.isEnabled) {
    //      this.$.lbl.hide();
    //    } else {
    //      this.$.lbl.show();
    //    }
  },
  i18nLabel: 'OBMOBC_LblCart',
  tabPanel: 'receipt',
  tap: function(options) {
    if (!this.disabled) {
      this.doTabChange({
        tabPanel: this.tabPanel,
        keyboard: 'toolbarscan',
        edit: false,
        options: options,
        status: ''
      });
      OB.POS.terminal.$.containerWindow
        .getRoot()
        .$.multiColumn.$.panels.addClass('obUiMultiColumn-panels-showReceipt');
    }
    OB.MobileApp.view.scanningFocus(true);
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabScan',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObposPointOfSaleUiButtonTabScan',
  tabPanel: 'scan',
  i18nLabel: 'OBMOBC_LblScan',

  events: {
    onTabChange: '',
    onRightToolbarDisabled: ''
  },
  handlers: {
    onRightToolbarDisabled: 'disabledButton',
    onPointOfSaleLoad: 'pointOfSaleLoad'
  },
  rfidOnIcon: 'obObposPointOfSaleUiButtonTabScan-status-rfidIcon_rfidOn',
  rfidOffIcon: 'obObposPointOfSaleUiButtonTabScan-status-rfidIcon_rfidOff',
  rfidOfflineIcon:
    'obObposPointOfSaleUiButtonTabScan-status-rfidIcon_rfidOffline',
  // components: [{
  //   name: 'status',
  //   classes: 'obObposPointOfSaleUiButtonTabScan-status',
  //   components: [{
  //     name: 'rfidIcon',
  //     showing: false,
  //     classes: 'obObposPointOfSaleUiButtonTabScan-status-rfidIcon'
  //   }]
  // }],
  init: function(model) {
    this.model = model;
    // [TODO] Fix or discuss where ad how RFID icon will be shown
    //    this.$.lbl.addClass('obObposPointOfSaleUiButtonTabScan-lbl');
    // if (OB.UTIL.RfidController.isRfidConfigured()) {
    //   this.$.rfidIcon.show();
    // }
    // OB.UTIL.RfidController.on(
    //   'change:connected change:connectionLost',
    //   function(model) {
    //     if (this.$.rfidIcon) {
    //       if (OB.UTIL.RfidController.get('connectionLost')) {
    //         this.$.rfidIcon.removeClass(this.rfidOnIcon);
    //         this.$.rfidIcon.removeClass(this.rfidOffIcon);
    //         this.$.rfidIcon.addClass(this.rfidOfflineIcon);
    //       } else {
    //         this.$.rfidIcon.removeClass(this.rfidOfflineIcon);
    //         if (
    //           OB.UTIL.RfidController.get('isRFIDEnabled') &&
    //           OB.UTIL.RfidController.get('connected')
    //         ) {
    //           this.$.rfidIcon.removeClass(this.rfidOffIcon);
    //           this.$.rfidIcon.addClass(this.rfidOnIcon);
    //         } else {
    //           this.$.rfidIcon.removeClass(this.rfidOnIcon);
    //           this.$.rfidIcon.addClass(this.rfidOffIcon);
    //         }
    //       }
    //     }
    //   },
    //   this
    // );
  },
  disabledButton: function(inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
    if (!this.isEnabled) {
      this.addClass('empty');
    } else {
      this.removeClass('empty');
    }
  },
  tap: function(options) {
    if (!this.disabled) {
      this.doTabChange({
        tabPanel: this.tabPanel,
        keyboard: 'toolbarscan',
        edit: false,
        options: options,
        status: ''
      });
    }
    OB.MobileApp.view.scanningFocus(true);

    return true;
  }
  //  pointOfSaleLoad: function (inSender, inEvent) {
  //    if (OB.UTIL.RfidController.get('connectionLost')) {
  //      this.$.rfidIcon.addClass(this.rfidOfflineIcon);
  //    } else {
  //      this.$.rfidIcon.removeClass(this.rfidOfflineIcon);
  //    }
  //    if (!OB.UTIL.RfidController.get('isRFIDEnabled') || !OB.UTIL.RfidController.get('reconnectOnScanningFocus')) {
  //      this.$.rfidIcon.addClass(this.rfidOffIcon);
  //      this.$.rfidIcon.removeClass(this.rfidOnIcon);
  //    } else {
  //      this.$.rfidIcon.addClass(this.rfidOnIcon);
  //      this.$.rfidIcon.removeClass(this.rfidOffIcon);
  //    }
  //  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabBrowse',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObPosPointOfSaleUiButtonTabBrowse',
  events: {
    onTabChange: '',
    onRightToolbarDisabled: ''
  },
  handlers: {
    onRightToolbarDisabled: 'disabledButton'
  },
  init: function(model) {
    this.model = model;
    //    var me = this;
    //    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
    //      me.doRightToolbarDisabled({
    //        status: model.get('isMultiOrders')
    //      });
    //    }, this);
  },
  disabledButton: function(inSender, inEvent) {
    var isDisabled = inEvent.status;
    this.isEnabled = !inEvent.status;
    if (OB.MobileApp.model.hasPermission('OBPOS_disableBrowseTab', true)) {
      isDisabled = true;
    }
    this.setDisabled(isDisabled);
    if (!this.isEnabled) {
      this.addClass('empty');
    } else {
      this.removeClass('empty');
    }
  },
  tabPanel: 'catalog',
  i18nLabel: 'OBMOBC_LblBrowse',
  tap: function() {
    if (!this.disabled) {
      this.doTabChange({
        tabPanel: this.tabPanel,
        keyboard: false,
        edit: false
      });
    }
    OB.MobileApp.view.scanningFocus(true);
  },
  initComponents: function() {
    this.inherited(arguments);
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      this.hide();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabSearchCharacteristic',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObPosPointOfSaleUiButtonTabSearchCharacteristic',
  tabPanel: 'searchCharacteristic',
  i18nLabel: 'OBPOS_LblSearch',
  events: {
    onTabChange: '',
    onRightToolbarDisabled: ''
  },
  handlers: {
    onRightToolbarDisabled: 'disabledButton'
  },
  init: function(model) {
    this.model = model;
    //    var me = this;
    //    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
    //      me.doRightToolbarDisabled({
    //        status: model.get('isMultiOrders')
    //      });
    //    }, this);
  },
  disabledButton: function(inSender, inEvent) {
    var isDisabled = inEvent.status;
    this.isEnabled = !inEvent.status;
    if (OB.MobileApp.model.hasPermission('OBPOS_disableSearchTab', true)) {
      isDisabled = true;
    }
    this.setDisabled(isDisabled);
    if (!this.isEnabled && !OB.MobileApp.model.get('serviceSearchMode')) {
      this.addClass('empty');
    } else {
      this.removeClass('empty');
    }
  },
  tap: function() {
    if (this.disabled === false) {
      OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
      this.doTabChange({
        tabPanel: this.tabPanel,
        keyboard: false,
        edit: false
      });
      OB.MobileApp.view.scanningFocus(true);
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      this.owner.addClass(
        'obUiMultiColumnToolbar-standardToolbar-toolbar_noBrowse'
      );
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabEditLine',
  kind: 'OB.UI.ToolbarButtonTab',
  classes: 'obObPosPointOfSaleUiButtonTabEditLine',
  continueClass: 'obObPosPointOfSaleUiButtonTabEditLine_continue',
  published: {
    ticketLines: null
  },
  tabPanel: 'edit',
  i18nLabel: 'OBPOS_LblEdit',
  events: {
    onTabChange: '',
    onRightToolbarDisabled: '',
    onDisableUserInterface: '',
    onEnableUserInterface: '',
    onFinishServiceProposal: '',
    onToggleLineSelection: '',
    onShowActionIcons: ''
  },
  handlers: {
    onRightToolbarDisabled: 'disableButton',
    onManageServiceProposal: 'manageServiceProposal'
  },
  init: function(model) {
    this.model = model;
    this.model
      .get('order')
      .get('lines')
      .on(
        'selected',
        function(lineSelected) {
          if (
            this.model.get('leftColumnViewManager').isOrder() &&
            this.model.get('order').get('lines').length > 0
          ) {
            this.currentLine = lineSelected;
            if (
              OB.MobileApp.model.hasPermission('OBPOS_disableEditTab', true)
            ) {
              this.setDisabled(true);
            } else {
              this.setDisabled(false);
            }
          } else {
            this.setDisabled(true);
          }
        },
        this
      );
    //    var me = this;
    //    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
    //      me.doRightToolbarDisabled({
    //        status: model.get('isMultiOrders')
    //      });
    //    }, this);
  },
  disableButton: function(inSender, inEvent) {
    inEvent.subtab ? (this.subtab = inEvent.subtab) : (this.subtab = null);
    this.disabledButton(inSender, inEvent);
  },
  disabledButton: function(inSender, inEvent) {
    var isDisabled = inEvent.status;
    if (inEvent.tab === this.tabToOpen) {
      isDisabled = false;
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_disableEditTab', true)) {
      isDisabled = true;
    }
    this.setDisabled(isDisabled);
  },
  manageServiceProposal: function(inSender, inEvent) {
    OB.MobileApp.model.set('serviceSearchMode', inEvent.proposalType);
    this.previousStatus = inEvent.previousStatus;
    this.setLabel(OB.I18N.getLabel('OBPOS_LblContinue'));
    this.addClass(this.continueClass);
    this.doDisableUserInterface();
    OB.MobileApp.view.scanningFocus(false);
    this.setDisabled(false);
    this.doShowActionIcons({
      show: false
    });
  },
  tap: function(options) {
    if (this.subtab) {
      const receiptClass = 'obUiMultiColumn-panels-showReceipt',
        panels = OB.POS.terminal.$.containerWindow.getRoot().$.multiColumn.$
          .panels,
        editToolbarPane = OB.POS.terminal.$.containerWindow.getRoot().$
          .multiColumn.$.rightPanel.$.toolbarpane.$.edit;
      if (panels.hasClass(receiptClass)) {
        panels.removeClass(receiptClass);
        this.addClass('selected');
        this.parent.$.toolbarBtnCart.removeClass('selected');
        editToolbarPane.addClass('selected');
      } else {
        this.setSelected(true);
      }
      return;
    }
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      this.setLabel(OB.I18N.getLabel('OBPOS_LblEdit'));
      this.removeClass(this.continueClass);
      this.doEnableUserInterface();
      OB.MobileApp.view.scanningFocus(true);
      this.doShowActionIcons({
        show: true
      });
      this.doToggleLineSelection({
        status: false
      });
      if (OB.MobileApp.model.get('serviceSearchMode') === 'mandatory') {
        this.restoreStatus();
      } else if (OB.MobileApp.model.get('serviceSearchMode') === 'final') {
        this.previousStatus.callback();
      }
      OB.MobileApp.model.unset('serviceSearchMode');
    } else {
      var lines = this.model.get('order').get('lines');
      if (!options.isManual) {
        // The tap was not manual. So consider the last line added
        var lastLine;
        if (lines && lines.length > 0) {
          lastLine = lines.models[lines.length - 1];
        }
        if (this.currentLine) {
          this.currentLine.trigger('selected', this.currentLine);
        } else if (lastLine) {
          lastLine.trigger('selected', lastLine);
        }
      }
      if (!this.disabled && lines.length > 0) {
        //If the button is not disabled and we have, at least, one line,
        //the view is changed to edit view
        this.doTabChange({
          tabPanel: this.tabPanel,
          keyboard: 'toolbarscan',
          status: '',
          edit: true
        });
      }
      OB.MobileApp.view.scanningFocus(true);
    }
  },
  restoreStatus: function() {
    if (
      this.previousStatus.tab === 'scan' ||
      this.previousStatus.tab === 'edit'
    ) {
      this.doTabChange({
        tabPanel: this.previousStatus.tab,
        keyboard: 'toolbarscan'
      });
    } else if (this.previousStatus.tab === 'catalog') {
      this.doTabChange({
        tabPanel: this.previousStatus.tab
      });
    } else {
      this.doTabChange({
        tabPanel: this.previousStatus.tab
      });
      this.doFinishServiceProposal({
        status: this.previousStatus
      });
    }
  }
});

// Toolbar panes
//----------------------------------------------------------------------------
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
  published: {
    model: null
  },
  classes: 'obObpodPointOfSaleUiRightToolbarPane',
  handlers: {
    onTabButtonTap: 'tabButtonTapHandler'
  },
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.TabScan',
      name: 'scan',
      classes: 'obObpodPointOfSaleUiRightToolbarPane-scan'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.TabBrowse',
      name: 'catalog',
      classes: 'obObpodPointOfSaleUiRightToolbarPane-catalog'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.TabSearchCharacteristic',
      name: 'searchCharacteristic',
      classes: 'obObpodPointOfSaleUiRightToolbarPane-searchCharacteristic'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.TabPayment',
      name: 'payment',
      classes: 'obObpodPointOfSaleUiRightToolbarPane-payment'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.TabEditLine',
      name: 'edit',
      classes: 'obObpodPointOfSaleUiRightToolbarPane-edit'
    }
  ],
  tabButtonTapHandler: function(inSender, inEvent) {
    if (inEvent.tabPanel) {
      this.showPane(inEvent.tabPanel, inEvent.options);
    }
  },
  showPane: function(tabName, options) {
    var me = this;
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreShowPane',
      {
        context: me,
        options: options,
        tabName: tabName
      },
      function(args) {
        if (args && args.cancelOperation && args.cancelOperation === true) {
          return;
        }
        OB.MobileApp.model.set('lastPaneShown', 'unknown');
        var paneArray = me.getComponents(),
          i;
        for (i = 0; i < paneArray.length; i++) {
          paneArray[i].removeClass('selected');
          if (paneArray[i].name === args.tabName) {
            if (paneArray[i].executeOnShow) {
              paneArray[i].executeOnShow(options);
            }
            paneArray[i].addClass('selected');
            OB.MobileApp.model.set('lastPaneShown', args.tabName);
          }
        }
      }
    );
  },
  modelChanged: function() {
    var receipt = this.model.get('order');
    this.$.scan.setReceipt(receipt);
    this.$.searchCharacteristic.setReceipt(receipt);
    this.$.payment.setReceipt(receipt);
    this.$.edit.setReceipt(receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabSearchCharacteristic',
  kind: 'OB.UI.TabPane',
  published: {
    receipt: null
  },
  classes: 'obObposPointOfSaleUiTabSearchCharacteristic',
  components: [
    {
      kind: 'OB.UI.SearchProductCharacteristic',
      name: 'searchCharacteristicTabContent',
      classes:
        'obObposPointOfSaleUiTabSearchCharacteristic-searchCharacteristicTabContent'
    }
  ],
  receiptChanged: function() {
    this.$.searchCharacteristicTabContent.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabBrowse',
  kind: 'OB.UI.TabPane',
  classes: 'obObposPointOfSaleUiTabBrowse',
  components: [
    {
      kind: 'OB.UI.ProductBrowser',
      name: 'catalogTabContent',
      classes: 'obObposPointOfSaleUiTabBrowse-catalogTabContent'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabScan',
  kind: 'OB.UI.TabPane',
  classes: 'obObposPointOfSaleUiTabScan',
  published: {
    receipt: null
  },
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.Scan',
      name: 'scanTabContent',
      classes: 'obObposPointOfSaleUiTabScan-scanTabContent'
    }
  ],
  receiptChanged: function() {
    this.$.scanTabContent.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabEditLine',
  kind: 'OB.UI.TabPane',
  classes: 'obObposPointOfSaleUiTabEditLine',
  published: {
    receipt: null
  },
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.EditLine',
      name: 'editTabContent',
      classes: 'obObposPointOfSaleUiTabEditLine-editTabContent'
    }
  ],
  receiptChanged: function() {
    this.$.editTabContent.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabPayment',
  kind: 'OB.UI.TabPane',
  classes: 'obObposPointOfSaleUiTabPayment',
  published: {
    receipt: null
  },
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.Payment',
      name: 'paymentTabContent',
      classes: 'obObposPointOfSaleUiTabPayment-paymentTabContent'
    }
  ],
  receiptChanged: function() {
    this.$.paymentTabContent.setReceipt(this.receipt);
  },
  executeOnShow: function(options) {}
});
