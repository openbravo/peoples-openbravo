/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


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
        view: OB.OBPOSCasgMgmt.UI.ButtonNextCashMgmt
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);
  }
});