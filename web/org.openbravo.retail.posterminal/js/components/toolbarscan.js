/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ToolbarScan = OB.COMP.KeyboardComponent.extend({
    tagName: 'div',
    attributes: {
      'style': 'display:none'
    },
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey.extend({
          command: 'code',
          classButtonActive: 'btnactive-blue',
          contentViewButton: [OB.I18N.getLabel('OBPOS_KbCode')]
        })
      }]
    }, {
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey
      }]
    }, {
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey
      }]
    }, {
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey
      }]
    }, {
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey
      }]
    }, {
      tag: 'div',
      attributes: {
        'style': 'display:table; width:100%'
      },
      content: [{
        view: OB.COMP.ButtonKey
      }]
    }],
    shown: function() {
      this.options.parent.showKeypad('index');
      this.options.parent.showSidepad('sideenabled');
      this.options.parent.defaultcommand = 'code';
    }
  });

  OB.UI.ToolbarScan = {
    name: 'toolbarscan',
    buttons: [{
      command: 'code',
      label: OB.I18N.getLabel('OBPOS_KbCode'),
      classButtonActive: 'btnactive-blue'
    }],
    shown: function() {
      var keyboard = this.owner.owner;
      keyboard.showKeypad('basic')
      keyboard.showSidepad('sideenabled');
      keyboard.defaultcommand = 'code';
    }
  };


}());