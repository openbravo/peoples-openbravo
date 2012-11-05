/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.KeypadBasic',
  label: OB.I18N.getLabel('OBPOS_KeypadBasic'),
  padName: 'basic',
  components: [{
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '/',
        command: '/'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '*',
        command: '*'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '%',
        command: '%'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '7',
        command: '7'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '8',
        command: '8'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '9',
        command: '9'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '4',
        command: '4'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '5',
        command: '5'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '6',
        command: '6'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '1',
        command: '1'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '2',
        command: '2'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '3',
        command: '3'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span8',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '0',
        command: '0'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: OB.Format.defaultDecimalSymbol,
        command: OB.Format.defaultDecimalSymbol
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.UI.ButtonKey',
  events: {
    onAddCommand: '',
    onAddButton: '',
    onKeyCommandPressed: '',
    onRegisterButton: ''

  },
  tag: 'div',
  style: 'margin: 5px;',
  classButtonActive: 'btnactive',
  classButton: '',
  command: false,
  permission: null,
  label: null,
  components: [{
    kind: 'OB.UI.Button',
    name: 'button',
    classes: 'btnkeyboard'
  }],
  initComponents: function () {
    var me = this;

    this.inherited(arguments);

    this.doRegisterButton();

    this.$.button.addClass(this.classButton);
    this.$.button.setContent(this.label);
  }
});