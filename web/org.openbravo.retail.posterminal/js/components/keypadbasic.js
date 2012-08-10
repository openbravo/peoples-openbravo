/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
  name: 'OB.UI.PaymentButton',
  style: 'margin: 5px;',
  components: [{
    kind: 'OB.UI.Button',
    classes: 'btnkeyboard',
    name: 'btn'
  }],
  background: '#6cb33f',
  initComponents: function() {
    var btn;
    this.inherited(arguments);

    btn = this.$.btn;
    btn.setContent(this.label || OB.I18N.formatCoins(this.amount));
    btn.applyStyle('background-color', this.background);
    btn.applyStyle('border', '10px solid' + (this.bordercolor || this.background));
  },
  tap: function() {
    var me = this,
        receipt = this.owner.owner.owner.owner.model.get('order');

    receipt.addPayment(new OB.Model.PaymentLine({
      kind: me.paymenttype,
      name: OB.POS.modelterminal.getPaymentName(me.paymenttype),
      amount: OB.DEC.number(me.amount)
    }));

    console.log('add coin', this.amount);
  }

});

enyo.kind({
  name: 'OB.UI.ButtonKey',
  tag: 'div',
  style: 'margin: 5px;',
  classButtonActive: 'btnactive',
  classButton: '',
  command: false,
  permission: null,
  label: null,
  classButtonActive: 'btnactive',
  components: [{
    kind: 'OB.UI.Button',
    name: 'button',
    classes: 'btnkeyboard'
  }],
  initComponents: function() {
    var me = this,
        keyboard = this.owner.keyPressed ? this.owner : this.owner.owner.keyPressed ? this.owner.owner : this.owner.owner.owner.keyPressed ? this.owner.owner.owner : this.owner.owner.owner.owner;

    this.inherited(arguments);

    if (this.command) {
      if (this.definition) {
        keyboard.addCommand(this.command, this.definition);
      }
      if (this.command === '---') {
        // It is the null command
        this.command = false;
      } else if (!this.command.match(/^([0-9]|\.|,|[a-z])$/) && this.command !== 'OK' && this.command !== 'del' && this.command !== String.fromCharCode(13) && !keyboard.commands[this.command]) {
        // is not a key and does not exists the command
        this.command = false;
      } else if (this.permission && !OB.POS.modelterminal.hasPermission(this.permission)) {
        // does not have permissions.
        this.command = false;
      }
    }

    if (this.command) {
      this.$.button.tap = function() {
        keyboard.keyPressed(me.command);
      }
      keyboard.addButton(this.command, this.$.button);
    } else {
      this.$.button.addClass('btnkeyboard-inactive');
    }

    this.$.button.addClass(this.classButton);
    this.$.button.setContent(this.label);
  }
});