/*global OB, Backbone, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.OBPOSCasgMgmt.UI.DoneButton = OB.COMP.RegularButton.extend({
  _id: 'cashmgmtnextbutton',
  label: OB.I18N.getLabel('OBPOS_LblDone'),
  attributes: {
    'style': 'min-width: 115px;'
  },
  className: 'btnlink-white btnlink-fontgray',
  clickEvent: function(e) {
    //TODO: make some util to do this cleaner
    this.options.parent.options.parent.model.depsdropstosend.trigger('makeDeposits');
  }
});



// Top-right panel with clock and buttons
OB.OBPOSCasgMgmt.UI.CashMgmtInfo = Backbone.View.extend({
  tagName: 'div',
  contentView: [{
    tag: 'div',
    attributes: {
      style: 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px'
    },
    content: [{
      view: OB.COMP.Clock.extend({
        className: 'pos-clock'
      })
    }, {
      tag: 'div',
      content: [{
        tag: 'div',
        id: 'msginfo',
        attributes: {
          style: 'padding: 10px; float: left; width: 320px; line-height: 23px;'
        },
        content: [OB.I18N.getLabel('OBPOS_LblDepositsWithdrawalsMsg')]
      }, {
        tag: 'div',
        id: 'msgaction',
        attributes: {
          style: 'padding: 5px; float: right;'
        },
        content: [{
          view: OB.COMP.SmallButton.extend({
            attributes: {
              href: '#modalCancel',
              'data-toggle': 'modal'
            },
            className: 'btnlink-white btnlink-fontgrey',
            label: OB.I18N.getLabel('OBPOS_LblCancel')
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'align': 'center',
        'style': 'width: 100%; float: left;'
      },
      content: [{
        view: OB.OBPOSCasgMgmt.UI.DoneButton
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);
  }
});

enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.CashMgmtInfo',
  components: [{
    style: 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{ //clock here
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    }, {
      // process info
      style: 'padding: 10px; float: left; width: 320px; line-height: 23px;',
      content: OB.I18N.getLabel('OBPOS_LblDepositsWithdrawalsMsg')
    }, {
      style: 'padding: 5px; float: right;',
      components: [{
        kind: 'OB.OBPOSCasgMgmt.UI.CancelButton'
      }]
    }, {
      // done button
      style: 'width: 100%; float: left;',
      attributes: {
        align: 'center'
      },
      components: [{
        kind: 'OB.OBPOSCasgMgmt.UI.DoneButton'
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.DoneButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-white btnlink-fontgray',
  style: 'min-width: 115px;',
  content: OB.I18N.getLabel('OBPOS_LblDone'),
  tap: function() {
    this.owner.owner.model.depsdropstosend.trigger('makeDeposits');
  }
});

enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.CancelButton',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-white btnlink-fontgray',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  attributes: {
    href: '#modalCancel',
    'data-toggle': 'modal'
  }
});