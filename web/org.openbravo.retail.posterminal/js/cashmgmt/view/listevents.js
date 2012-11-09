/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo */

//Renders a modal popup with a list of reasons for drops/deposits
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  body: {
    kind: 'OB.OBPOSCashMgmt.UI.ListEvents'
  },

  init: function () {
    this.$.body.$.listEvents.init();
  }
});

//Popup with the destinations for deposits/drops
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListEvents',
  classes: 'row-fluid',
  components: [{
    classes: 'span12',
    components: [{
      components: [{
        //tableview
        name: 'eventList',
        kind: 'OB.UI.Table',
        style: 'overflow: auto; max-height: 600px',
        renderLine: 'OB.OBPOSCashMgmt.UI.ListEventLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],

  init: function () {
    this.$.eventList.setCollection(this.owner.owner.owner.model.getData(this.owner.owner.type));
  }
});

//Renders each of the deposit/drops destinations
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListEventLine',
  kind: 'OB.UI.SelectButton',
  style: 'height: 60px; background-color: #dddddd; border: 1px solid #ffffff;',
  events: {
    onHideThisPopup: ''
  },
  components: [{
    name: 'line',
    style: 'padding: 1px 0px 1px 5px;'

  }],
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },

  create: function () {
    this.inherited(arguments);
    this.$.line.setContent(this.model.get('name'));
  }
});