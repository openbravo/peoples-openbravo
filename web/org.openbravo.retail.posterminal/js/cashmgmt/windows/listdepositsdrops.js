/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.COMP.ListDepositsDrops = Backbone.View.extend({
  tagName: 'div',

  contentView: [{
    tag: 'div',
    attributes: {
      style: 'overflow:auto; height: 500px; margin: 5px'
    },
    content: [{
      tag: 'div',
      attributes: {
        'style': 'background-color: #ffffff; color: black; padding: 5px;'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'row-fluid'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span12',
            'style': 'border-bottom: 1px solid #cccccc;'
          },
          content: [{
            tag: 'div',
            attributes: {
              'style': 'padding: 6px; border-bottom: 1px solid #cccccc;text-align:center; font-weight:bold;'
            },
            content: [OB.I18N.getLabel('OBPOS_LblCashManagement')]
          }, {
            tag: 'div',
            id: 'userName',
            attributes: {
              'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;',
            },
          }, {
            tag: 'div',
            id: 'time',
            attributes: {
              'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'
            },
            //   content: [OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24)]
          }, {
            tag: 'div',
            id: 'store',
            attributes: {
              'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'
            },
            //    content: [OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.POS.modelterminal.get('terminal').organization$_identifier]
          }, {
            tag: 'div',
            id: 'terminal',
            attributes: {
              'style': 'padding: 6px; border-bottom: 1px solid #cccccc; text-align:center;'
            },
            //   content: [OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.POS.modelterminal.get('terminal')._identifier]
          }, {
            id: 'depositDropsList',
            view: OB.UI.TableView.extend({
              style: 'list',
              renderLine: OB.COMP.RenderDepositsDrops,
              renderEmpty: OB.COMP.RenderEmpty
            })
          }]
        }]
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);
    this.depositDropsList.registerCollection(this.options.parent.model.getData('DataDepositsDrops'));
  },
  render: function() {
    this.userName.text(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.POS.modelterminal.get('context').user._identifier);
    this.time.text(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24));
    this.store.text(OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.POS.modelterminal.get('terminal').organization$_identifier);
    this.terminal.text(OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.POS.modelterminal.get('terminal')._identifier);
    return this;
  }

});