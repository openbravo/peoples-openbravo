/*global $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var ButtonDummy = Backbone.View.extend({
    tagName: 'button',
    initialize: function () {
      this.$el.attr('class', this.options.className);
    }
  });

  OB.COMP.KeyboardComponent = Backbone.View.extend({
    initialize: function () {

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
    initialize: function (attr) {

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
        this.button.clickEvent = function (e) {
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
    append: function (child) {
      if (child.$el) {
        this.button.$el.append(child.$el);
      }
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
        }), id: 'button'
      }]
    }],
    background: '#6cb33f',
    initialize: function () {
      OB.UTIL.initContentView(this);
      var me = this;
      this.button.$el.css({'background-color': this.background, 'border': '10px solid' + (this.bordercolor || this.background)});
      this.button.$el.text(this.label || OB.I18N.formatCurrency(this.amount));
      this.button.clickEvent = function (e) {
        me.options.parent.receipt.addPayment(new OB.Model.PaymentLine({'kind': me.paymenttype, 'name': OB.POS.modelterminal.getPaymentName(me.paymenttype), 'amount': OB.DEC.number(me.amount)}));
      };
    }
  });

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