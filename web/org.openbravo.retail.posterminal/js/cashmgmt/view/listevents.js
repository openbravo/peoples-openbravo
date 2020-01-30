/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

//Renders a modal popup with a list of reasons for drops/deposits
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
  kind: 'OB.UI.Modal',
  classes:
    'u-popup-top-separation-large obposObposcashmgmtUiModalDepositEvents',
  body: {
    kind: 'OB.OBPOSCashMgmt.UI.ListEvents',
    classes: 'obposObposcashmgmtUiModalDepositEvents-obObposcashmgmtUiListEvent'
  },
  footer: {
    classes:
      'obUiModal-footer-mainButtons obposObposcashmgmtUiModalDepositEvents-footer',
    components: [
      {
        kind: 'OB.UI.ModalDepositEventsCancel',
        classes:
          'obposObposcashmgmtUiModalDepositEvents-footer-modalDepositEventsCancelButton',
        name: 'modalDepositEventsCancelButton'
      }
    ]
  }
});

//Popup with the destinations for deposits/drops
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListEvents',
  classes: 'obObposcashmgmtUiListEvent row-fluid',
  components: [
    {
      classes: 'obObposcashmgmtUiListEvent-container1 span12',
      components: [
        {
          classes: 'obObposcashmgmtUiListEvent-container1-element1'
        }
      ]
    }
  ],

  init: function(model) {
    this.createComponent({
      name: this.owner.owner.type,
      kind: 'OB.UI.Table',
      classes: 'obObposcashmgmtUiListEvent-obUiTable',
      renderLine: 'OB.OBPOSCashMgmt.UI.ListEventLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    });
    this.model = model;
    this.$[this.owner.owner.type].setCollection(
      this.model.get(this.owner.owner.type)
    );
  }
});

//Renders each of the deposit/drops destinations
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListEventLine',
  kind: 'OB.UI.SelectButton',
  classes: 'obObposcashmgmtUiListEventLine',
  events: {
    onHideThisPopup: ''
  },
  components: [
    {
      name: 'line',
      classes: 'obObposcashmgmtUiListEventLine-line'
    }
  ],
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
  },

  create: function() {
    this.inherited(arguments);
    this.$.line.setContent(this.model.get('name'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalDepositEventsCancel',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obUiModalDepositEventsCancel',
  tap: function() {
    this.doHideThisPopup();
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});
