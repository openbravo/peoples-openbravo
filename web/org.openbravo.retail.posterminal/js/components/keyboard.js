/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, Backbone */

enyo.kind({
  name: 'OB.UI.Keyboard',

  commands: {},
  buttons: {},
  status: '',
  sideBarEnabled: false,
  destroy: function () {
    this.buttons = null;
    this.commands = null;
    $(window).unbind('keypress');
    this.inherited(arguments);
  },
  tag: 'div',
  classes: 'row-fluid',
  components: [{
    name: 'toolbarcontainer',
    tag: 'div',
    classes: 'span3'
  }, {
    tag: 'div',
    classes: 'span9',
    components: [{
      tag: 'div',
      classes: 'row-fluid',
      components: [{
        tag: 'div',
        classes: 'span8',
        components: [{
          tag: 'div',
          style: 'margin:5px',
          components: [{
            tag: 'div',
            style: 'text-align: right; width: 100%; height: 40px;',
            components: [{
              tag: 'pre',
              style: 'margin: 0px 0px 9px 0px; background-color: whiteSmoke; border: 1px solid #CCC; word-wrap: break-word; font-size: 35px; height: 33px; padding: 22px 5px 0px 0px;',
              components: [
              // ' ', XXX:???
              {
                name: 'editbox',
                tag: 'span',
                style: 'margin-left: -10px;'
              }]
            }]
          }]
        }]
      }, {
        tag: 'div',
        classes: 'span4',
        components: [{
          kind: 'OB.UI.ButtonKey',
          classButton: 'btn-icon btn-icon-backspace',
          command: 'del'
        }]
      }, {
        tag: 'div',
        classes: 'row-fluid',
        components: [{ // keypadcontainer
          tag: 'div',
          classes: 'span8',
          name: 'keypadcontainer'
        }, {
          tag: 'div',
          classes: 'span4',
          components: [{
            // rigth toolbar with qty, discount... buttons
            tag: 'div',
            name: 'sideenabled',
            components: [{
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '-',
                  classButton: 'btnkeyboard-num btnkeyboard-minus',
                  command: '-'
                }]
              }, {
                tag: 'div',
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '+',
                  classButton: 'btnkeyboard-num btnkeyboard-plus',
                  command: '+'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: OB.I18N.getLabel('OBPOS_KbQuantity'),
                  command: 'line:qty'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: OB.I18N.getLabel('OBPOS_KbPrice'),
                  permission: 'OBPOS_order.changePrice',
                  command: 'line:price'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: OB.I18N.getLabel('OBPOS_KbDiscount'),
                  permission: 'OBPOS_order.discount',
                  command: 'line:dto'
                }]
              }]
            }]
          }, {
            // empty right toolbar used in case the keyboard
            // shouldn't support these buttons
            tag: 'div',
            name: 'sidedisabled',
            components: [{
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey'
                }]
              }, {
                tag: 'div',
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey'
                }]
              }]
            }, {
              tag: 'div',
              classes: 'row-fluid',
              components: [{
                tag: 'div',
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey'
                }]
              }]
            }]
          }, {
            tag: 'div',
            classes: 'row-fluid',
            components: [{
              tag: 'div',
              classes: 'span12',
              components: [{
                kind: 'OB.UI.ButtonKey',
                classButton: 'btn-icon btn-icon-enter',
                command: 'OK'
              }]
            }]
          }]
        }]
      }]
    }]
  }],

  events: {
    onCommandFired: '',
    onStatusChanged: ''
  },

  handlers: {
    onCommandFired: 'commandHandler',
    onRegisterButton: 'registerButton'
  },

  setStatus: function (newstatus) {
    var btn = this.buttons[this.status];

    if (btn && (btn.classButtonActive || (btn.owner && btn.owner.classButtonActive))) {
      btn.removeClass(btn.classButtonActive || btn.owner.classButtonActive);
    }
    this.status = newstatus;

    // sending the event to the components bellow this one
    this.waterfall('onStatusChanged', {
      status: newstatus
    });

    // set the right keypad by default
    if (this.namedkeypads[this.status]) {
      this.showKeypad(this.namedkeypads[this.status]);
    } else {
      this.showKeypad('basic');
    }

    btn = this.buttons[this.status];
    if (btn && (btn.classButtonActive || (btn.owner && btn.owner.classButtonActive))) {
      btn.addClass(btn.classButtonActive || btn.owner.classButtonActive);
    }
  },

  execCommand: function (cmddefinition, txt) {
    if (!cmddefinition.permission || OB.POS.modelterminal.hasPermission(cmddefinition.permission)) {
      cmddefinition.action(this, txt);
    }
  },

  execStatelessCommand: function (cmd, txt) {
    this.commands[cmd].action(this, txt);
  },

  getNumber: function () {
    return OB.I18N.parseNumber(this.getString());
  },

  getString: function () {
    var s = this.$.editbox.getContent();
    this.$.editbox.setContent('');
    return s;
  },

  clear: function () {
    this.$.editbox.setContent('');
    this.setStatus('');
  },

  commandHandler: function (sender, event) {
    var txt, me = this,
        cmd = event.key;


    if (this.$.editbox.getContent() && cmd === String.fromCharCode(13)) {
      txt = this.getString();

      if (this.defaultcommand) {
        this.execCommand(this.commands[this.defaultcommand], txt);
      } else {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoDefaultActionDefined'));
      }
    } else if (cmd === 'OK') {
      txt = this.getString();

      if (txt === '0' && this.status === '') {
        OB.POS.lock();
      }

      if (txt && this.status === '') {
        if (this.defaultcommand) {
          this.execCommand(this.commands[this.defaultcommand], txt);
        } else {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoDefaultActionDefined'));
        }
      } else if (txt && this.status !== '') {
        this.execCommand(this.commands[this.status], txt);
        this.setStatus('');
      }
    } else if (this.commands[cmd]) {
      txt = this.getString();
      if (this.commands[cmd].stateless) {
        // Stateless commands: add, subs, ...
        this.execStatelessCommand(cmd, txt);
      } else {
        // Statefull commands: quantity, price, discounts, payments ...
        if (txt && this.status === '') { // Short cut: type + action
          this.execCommand(this.commands[cmd], txt);
        } else if (this.status === cmd && txt) {
          this.execCommand(this.commands[cmd], txt);
        } else if (this.status === cmd) { // Reset status
          this.setStatus('');
        } else {
          this.setStatus(cmd);
        }
      }
    } else {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoActionDefined'));
    }
  },

  registerButton: function (sender, event) {
    var me = this,
        button = event.originator;
    if (button.command) {
      if (button.definition) {
        this.addCommand(button.command, button.definition);
      }
      if (button.command === '---') {
        // It is the null command
        button.command = false;
      } else if (!button.command.match(/^([0-9]|\.|,|[a-z])$/) && button.command !== 'OK' && button.command !== 'del' && button.command !== String.fromCharCode(13) && !this.commands[button.command]) {
        // is not a key and does not exists the command
        button.command = false;
      } else if (button.permission && !OB.POS.modelterminal.hasPermission(button.permission)) {
        // does not have permissions.
        button.command = false;
      }
    }

    if (button.command) {
      button.$.button.tap = function () {
        me.keyPressed(button.command);
      };

      this.addButton(button.command, button.$.button);
    } else {
      button.$.button.addClass('btnkeyboard-inactive');
    }
  },

  initComponents: function () {
    var me = this;
    this.buttons = {}; // must be intialized before calling super, not after.
    this.activekeypads = [];
    this.namedkeypads = {};

    this.inherited(arguments);
    this.state = new Backbone.Model();


    this.$.toolbarcontainer.destroyComponents();
    this.$.keypadcontainer.destroyComponents();

    this.showSidepad('sidedisabled');

    if (this.sideBarEnabled) {
      this.$.sideenabled.show();
      this.$.sidedisabled.hide();
    } else {
      this.$.sideenabled.hide();
      this.$.sidedisabled.show();
    }

    this.addKeypad('OB.UI.KeypadBasic');
    this.showKeypad('basic');


    //Special case to manage the dot (.) pressing in the numeric keypad (only can be managed using keydown)
    $(window).off('keydown');
    $(window).keydown(function (e) {
      var handled = false;
      if (OB.UTIL.fixFocus()) {
        if (OB.Format.defaultDecimalSymbol !== '.') {
          if (e.keyCode === 110) { //Numeric keypad dot (.)
            me.keyPressed(OB.Format.defaultDecimalSymbol);
          } else if (e.keyCode === 190) { //Character keyboard dot (.)
            me.keyPressed('.');
          }
        }
        if (e.keyCode === 8) { //del key
          me.keyPressed('del');
        }
        if (e.keyCode === 13) { //intro key
          if ($('.enyo-popup:visible, .subwindow:visible').length > 0) {
            //A modal is opened -> try to call to onEnterTap function of dialog.
            if (OB.UI.UTILS) {
              if ($('.enyo-popup:visible').length > 0) {
                if (OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')]) {
                  if (OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')].enterTap) {
                    handled = OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')].enterTap(e, $('#' + e.srcElement.id).attr('onEnterTap'));
                    if (handled) {
                      return false;
                    }
                  }
                }
              } else if ($('.subwindow:visible').length > 0) {
                //priority of subwindows is less than modals, because a modal can appear on a subwindow
                if (OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')]) {
                  if (OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')].enterTap) {
                    handled = OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')].enterTap(e, $('#' + e.srcElement.id).attr('onEnterTap'));
                    if (handled) {
                      return false;
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        if (e.keyCode === 13) {
          if ($('.enyo-popup:visible, .subwindow:visible').length > 0) {
            //Intro key pressed and modal popup is opned -> try to call onEnterTap of dialog
            //A modal is opened -> try to call to onEnterTap function of dialog.
            if (OB.UI.UTILS) {
              if ($('.enyo-popup:visible').length > 0) {
                if (OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')]) {
                  if (OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')].enterTap) {
                    handled = OB.UI.UTILS.domIdEnyoReference[$('.enyo-popup:visible').attr('id')].enterTap(e, $('#' + e.srcElement.id).attr('onEnterTap'));
                    if (handled) {
                      return false;
                    }
                  }
                }
              } else if ($('.subwindow:visible').length > 0) {
                //priority of subwindows is less than modals, because a modal can appear on a subwindow
                if (OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')]) {
                  if (OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')].enterTap) {
                    handled = OB.UI.UTILS.domIdEnyoReference[$('.subwindow:visible').attr('id')].enterTap(e, $('#' + e.srcElement.id).attr('onEnterTap'));
                    if (handled) {
                      return false;
                    }
                  }
                }
              }
            }
          }
        }
      }
      return true;
    });

    $(window).off('keypress');
    $(window).keypress(function (e) {
      if (OB.UTIL.fixFocus()) {
        if (e.which !== 46 || OB.Format.defaultDecimalSymbol === '.') { //Any keypress except any kind of dot (.)
          me.keyPressed(String.fromCharCode(e.which));
        }
      }
    });
  },

  keyPressed: function (key) {
    var t;
    if (key.match(/^([0-9]|\.|,| |[a-z]|[A-Z])$/)) {
      if ($('.enyo-popup:visible, .subwindow:visible').length === 0) {
        t = this.$.editbox.getContent();
        this.$.editbox.setContent(t + key);
      }
    } else if (key === 'del') {
      if ($('.enyo-popup:visible, .subwindow:visible').length === 0) {
        t = this.$.editbox.getContent();
        if (t.length > 0) {
          this.$.editbox.setContent(t.substring(0, t.length - 1));
        }
      }
    } else {
      this.doCommandFired({
        key: key
      });
    }
  },

  addToolbar: function (newToolbar) {
    var toolbar = this.$.toolbarcontainer.createComponent({
      toolbarName: newToolbar.name,
      shown: newToolbar.shown,
      keboard: this
    });

    var emptyBtn = {
      kind: 'OB.UI.BtnSide',
      btn: {}
    },
        i = 0;

    enyo.forEach(newToolbar.buttons, function (btnDef) {
      if (btnDef.command) {
        toolbar.createComponent({
          kind: 'OB.UI.BtnSide',
          btn: btnDef
        });
      } else {
        toolbar.createComponent(emptyBtn);
      }
      i++;
    }, this);


    // populate toolbar up to 6 empty buttons
    for (; i < 6; i++) {
      toolbar.createComponent(emptyBtn);
    }
  },

  addToolbarComponent: function (newToolbar) {
    this.$.toolbarcontainer.createComponent({
      kind: newToolbar,
      keyboard: this
    });
  },

  showToolbar: function (toolbarName) {
    this.show();
    enyo.forEach(this.$.toolbarcontainer.getComponents(), function (toolbar) {
      if (toolbar.toolbarName === toolbarName) {
        toolbar.show();
        if (toolbar.shown) {
          toolbar.shown();
        }
      } else {
        toolbar.hide();
      }
    }, this);
  },

  addCommand: function (cmd, definition) {
    this.commands[cmd] = definition;
  },

  addButton: function (cmd, btn) {
    if (this.buttons[cmd]) {
      if (this.buttons[cmd].add) {
        this.buttons[cmd] = this.buttons[cmd].add(btn);
      }
    } else {
      this.buttons[cmd] = btn;
    }
  },

  addKeypad: function (keypad) {

    var keypadconstructor = enyo.constructorForKind(keypad);
    this.activekeypads.push(keypadconstructor.prototype.padName);
    if (keypadconstructor.prototype.padPayment) {
      this.namedkeypads[keypadconstructor.prototype.padPayment] = keypadconstructor.prototype.padName;
    }

    this.$.keypadcontainer.createComponent({
      kind: keypad,
      keyboard: this
    }).hide();
  },

  showKeypad: function (keypadName) {
    var firstLabel = null,
        foundLabel = false;
    this.state.set('keypadName', keypadName);
    enyo.forEach(this.$.keypadcontainer.getComponents(), function (pad) {
      if (!firstLabel) {
        firstLabel = pad.label;
      } else if (foundLabel) {
        this.state.set('keypadNextLabel', pad.label);
        foundLabel = false;
      }
      if (pad.padName === keypadName) {
        foundLabel = true;
        pad.show();
        // Set the right payment status. If needed.
        if (pad.padPayment && this.status !== pad.padPayment) {
          this.setStatus(pad.padPayment);
        }
      } else {
        pad.hide();
      }
    }, this);
    if (foundLabel) {
      this.state.set('keypadNextLabel', firstLabel);
    }
  },

  showNextKeypad: function () {
    var i, max, current = this.state.get('keypadName'),
        pad;

    for (i = 0, max = this.activekeypads.length; i < max; i++) {
      if (this.activekeypads[i] === current) {
        this.showKeypad(i < this.activekeypads.length - 1 ? this.activekeypads[i + 1] : this.activekeypads[0]);
        break;
      }
    }
  },

  showSidepad: function (sidepadname) {
    this.$.sideenabled.hide();
    this.$.sidedisabled.hide();
    this.$[sidepadname].show();
  }
});

enyo.kind({
  name: 'OB.UI.BtnSide',
  style: 'display:table; width:100%',
  initComponents: function () {
    this.createComponent({
      kind: 'OB.UI.ButtonKey',
      label: this.btn.label,
      command: this.btn.command,
      permission: this.btn.permission,
      definition: this.btn.definition,
      classButtonActive: this.btn.classButtonActive || 'btnactive-green'
    });
  }
});