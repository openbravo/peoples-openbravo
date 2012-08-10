/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $, _, Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // No need to refactor
  var ButtonDummy = Backbone.View.extend({
    tagName: 'button',
    initialize: function() {
      this.$el.attr('class', this.options.className);
    }
  });

  // Is this needed to be refactored?
  OB.COMP.KeyboardComponent = Backbone.View.extend({
    initialize: function() {

      // bind Keyboard properties to parent that is the real Keyboard...
      this.receipt = this.options.parent.receipt;
      this.commands = this.options.parent.commands;
      this.addCommand = _.bind(this.options.parent.addCommand, this.options.parent);
      this.addButton = _.bind(this.options.parent.addButton, this.options.parent);
      this.keyPressed = _.bind(this.options.parent.keyPressed, this.options.parent);
      //      this.showKeypad =  _.bind(this.options.parent.showKeypad, this.options.parent);
      OB.UTIL.initContentView(this);
    }
  });

  // Refactored with enyo: OB.UI.ButtonKey
  OB.COMP.ButtonKey = Backbone.View.extend({
    classButton: '',
    command: false,
    permission: null,
    label: null,
    classButtonActive: 'btnactive',
    tagName: 'div',
    attributes: {
      'style': 'margin: 5px;'
    },
    initialize: function(attr) {

      this.kb = this.options.parent;
      var me = this;

      if (this.command) {
        if (this.definition) {
          this.kb.addCommand(this.command, this.definition);
        }
        if (this.command === '---') {
          // It is the null command
          this.command = false;
        } else if (!this.command.match(/^([0-9]|\.|,|[a-z])$/) && this.command !== 'OK' && this.command !== 'del' && this.command !== String.fromCharCode(13) && !this.kb.commands[this.command]) {
          // is not a key and does not exists the command
          this.command = false;
        } else if (this.permission && !OB.POS.modelterminal.hasPermission(this.permission)) {
          // does not have permissions.
          this.command = false;
        }
      }

      if (this.command) {
        this.button = new OB.COMP.Button();
        this.button.$el.attr('class', 'btnkeyboard ' + this.classButton);
        this.button.clickEvent = function(e) {
          me.kb.keyPressed(me.command);
        };
        this.kb.addButton(this.command, this.button);
      } else {
        this.button = new ButtonDummy({
          className: 'btnkeyboard ' + 'btnkeyboard-inactive ' + this.classButton
        });
      }

      // Initialize the interface of the button
      this.button.contentView = this.contentViewButton;
      this.button.classButtonActive = this.classButtonActive;
      OB.UTIL.initContentView(this.button);

      this.$el.append(this.button.$el);
    },
    append: function(child) {
      if (child.$el) {
        this.button.$el.append(child.$el);
      }
    }
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

  OB.COMP.PaymentButton = Backbone.View.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'margin: 5px;'
      },
      content: [{
        view: OB.COMP.Button.extend({
          className: 'btnkeyboard'
        }),
        id: 'button'
      }]
    }],
    background: '#6cb33f',
    initialize: function() {
      OB.UTIL.initContentView(this);
      var me = this;
      this.button.$el.css({
        'background-color': this.background,
        'border': '10px solid' + (this.bordercolor || this.background)
      });
      this.button.$el.text(this.label || OB.I18N.formatCoins(this.amount));
      this.button.clickEvent = function(e) {
        me.options.parent.receipt.addPayment(new OB.Model.PaymentLine({
          'kind': me.paymenttype,
          'name': OB.POS.modelterminal.getPaymentName(me.paymenttype),
          'amount': OB.DEC.number(me.amount)
        }));
      };
    }
  });

  //Refactored with enyo: OB.UI.KeypadBasic
  OB.COMP.KeypadBasic = OB.COMP.KeyboardComponent.extend({
    tag: 'div',
    name: 'index',
    label: OB.I18N.getLabel('OBPOS_KeypadBasic'),
    attributes: {
      'style': 'display:none'
    },
    contentView: [{
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '/',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['/']
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '*',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['*']
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '%',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['%']
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '7',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['7']
          }),
          content: ['7']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '8',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['8']
          }),
          content: ['8']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '9',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['9']
          }),
          content: ['9']
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '4',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['4']
          }),
          content: ['4']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            kb: this,
            command: '5',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['5']
          }),
          content: ['5']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            kb: this,
            command: '6',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['6']
          }),
          content: ['6']
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '1',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['1']
          }),
          content: ['1']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '2',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['2']
          }),
          content: ['2']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '3',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['3']
          }),
          content: ['3']
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span8'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '0',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['0']
          }),
          content: ['0']
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: OB.Format.defaultDecimalSymbol,
            classButton: 'btnkeyboard-num',
            contentViewButton: [OB.Format.defaultDecimalSymbol]
          })
        }]
      }]
    }]
  });

}());

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