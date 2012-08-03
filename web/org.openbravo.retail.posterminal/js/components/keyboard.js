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

  var BtnSide = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'display:table; width:100%'
    },
    initialize: function() {
      var inst = new this.options.btn({
        parent: this.options.parent
      });
      inst.render();
      this.$el.append(inst.$el);
    }
  });

  OB.COMP.Keyboard = Backbone.View.extend({

    optionsid: 'keyboard',
    status: '',
    commands: {},
    buttons: {},
    contentView: [{
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        id: 'toolbarcontainer',
        tag: 'div',
        attributes: {
          'class': 'span3'
        }
      }, {
        tag: 'div',
        attributes: {
          'class': 'span9'
        },
        content: [{
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
              tag: 'div',
              attributes: {
                'style': 'margin:5px'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'style': 'text-align: right; width: 100%; height: 40px;'
                },
                content: [{
                  tag: 'pre',
                  attributes: {
                    'style': 'font-size: 35px; height: 33px; padding: 22px 5px 0px 0px;'
                  },
                  content: [' ',
                  {
                    id: 'editbox',
                    tag: 'span',
                    attributes: {
                      'style': 'margin-left: -10px;'
                    }
                  }]
                }]
              }]
            }]
          }, {
            tag: 'div',
            attributes: {
              'class': 'span4'
            },
            content: [{
              view: OB.COMP.ButtonKey.extend({
                command: 'del',
                contentViewButton: [{
                  tag: 'div',
                  attributes: {
                    'class': 'btn-icon btn-icon-backspace'
                  }
                }]
              })
            }]
          }]
        }, {
          tag: 'div',
          attributes: {
            'class': 'row-fluid'
          },
          content: [{
            id: 'keypadcontainer',
            tag: 'div',
            attributes: {
              'class': 'span8'
            }
          }, {
            tag: 'div',
            attributes: {
              'class': 'span4'
            },
            content: [{
              id: 'sideenabled',
              attributes: {
                'style': 'display:none'
              },
              tag: 'div',
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'row-fluid'
                },
                content: [{
                  tag: 'div',
                  attributes: {
                    'class': 'span6'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey.extend({
                      command: '-',
                      classButton: 'btnkeyboard-num btnkeyboard-minus',
                      contentViewButton: ['-']
                    })
                  }]
                }, {
                  tag: 'div',
                  attributes: {
                    'class': 'span6'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey.extend({
                      command: '+',
                      classButton: 'btnkeyboard-num btnkeyboard-plus',
                      contentViewButton: ['+']
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey.extend({
                      command: 'line:qty',
                      contentViewButton: [OB.I18N.getLabel('OBPOS_KbQuantity')]
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey.extend({
                      command: 'line:price',
                      permission: 'OBPOS_order.changePrice',
                      contentViewButton: [OB.I18N.getLabel('OBPOS_KbPrice')]
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey.extend({
                      command: 'line:dto',
                      permission: 'OBPOS_order.discount',
                      contentViewButton: [OB.I18N.getLabel('OBPOS_KbDiscount')]
                    })
                  }]
                }]
              }]
            }, {
              id: 'sidedisabled',
              attributes: {
                'style': 'display:none'
              },
              tag: 'div',
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'row-fluid'
                },
                content: [{
                  tag: 'div',
                  attributes: {
                    'class': 'span6'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey
                  }]
                }, {
                  tag: 'div',
                  attributes: {
                    'class': 'span6'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey
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
                    'class': 'span12'
                  },
                  content: [{
                    view: OB.COMP.ButtonKey
                  }]
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
                  'class': 'span12'
                },
                content: [{
                  view: OB.COMP.ButtonKey.extend({
                    command: 'OK',
                    contentViewButton: [{
                      tag: 'div',
                      attributes: {
                        'class': 'btn-icon btn-icon-enter'
                      }
                    }]
                  })
                }]
              }]
            }]
          }]
        }]
      }]
    }],



    initialize: function() {

      this.options.root[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      var me = this;

      this.toolbars = {};

      this.keypads = {};
      this.keypad = '';
      this.addKeypad(OB.COMP.KeypadBasic); // index
      this.showKeypad();
      this.showSidepad('sidedisabled');

      this.on('command', function(cmd) {
        var txt;
        var me = this;
        if (this.editbox.text() && cmd === String.fromCharCode(13)) {
          txt = this.getString();

          if (this.defaultcommand) {
            this.execCommand(this.commands[this.defaultcommand], txt);
          } else {
            OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoDefaultActionDefined'));
          }
        } else if (cmd === 'OK') {
          txt = this.getString();

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
            } else if (this.status === cmd) { // Reset status
              this.setStatus('');
            } else {
              this.setStatus(cmd);
            }
          }
        } else {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoActionDefined'));
        }
      }, this);

      //Special case to manage the dot (.) pressing in the numeric keypad (only can be managed using keydown)
      $(window).keydown(function(e) {
        if (window.fixFocus()) {
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
        }
        return true;
      });

      $(window).keypress(function(e) {
        if (window.fixFocus()) {
          if (e.which !== 46 || OB.Format.defaultDecimalSymbol === '.') { //Any keypress except any kind of dot (.)
            me.keyPressed(String.fromCharCode(e.which));
          }
        }
      });
    },

    setStatus: function(newstatus) {
      var btn = this.buttons[this.status];
      if (btn) {
        btn.$el.removeClass(btn.classButtonActive);
      }
      this.status = newstatus;
      this.trigger('status', this.status);
      btn = this.buttons[this.status];
      if (btn) {
        btn.$el.addClass(btn.classButtonActive);
      }
    },

    execCommand: function(cmddefinition, txt) {
      if (!cmddefinition.permissions || OB.POS.modelterminal.hasPermission(cmddefinition.permissions)) {
        cmddefinition.action.call(this, txt);
      }
    },

    execStatelessCommand: function(cmd, txt) {
      this.commands[cmd].action.call(this, txt);
    },

    addCommand: function(cmd, definition) {
      this.commands[cmd] = definition;
    },

    addButton: function(cmd, btn) {
      if (this.buttons[cmd]) {
        if (this.buttons[cmd].add) this.buttons[cmd] = this.buttons[cmd].add(btn);
      } else {
        this.buttons[cmd] = btn;
      }
    },

    clear: function() {
      this.editbox.empty();
      this.setStatus('');
    },

    show: function(toolbar) {
      var t;
      var mytoolbar;

      this.clear();

      if (toolbar) {
        for (t in this.toolbars) {
          if (this.toolbars.hasOwnProperty(t)) {
            this.toolbars[t].$el.hide();
          }
        }
        mytoolbar = this.toolbars[toolbar];
        mytoolbar.$el.show();
        if (mytoolbar.shown) {
          mytoolbar.shown();
        }
      }
      this.$el.show();
    },

    hide: function() {
      this.$el.hide();
    },

    getNumber: function() {
      var i = OB.I18N.parseNumber(this.editbox.text());
      this.editbox.empty();
      return i;
    },

    getString: function() {
      var s = this.editbox.text();
      this.editbox.empty();
      return s;
    },

    keyPressed: function(key) {

      var t;
      if (key.match(/^([0-9]|\.|,|[a-z])$/)) {
        t = this.editbox.text();
        this.editbox.text(t + key);
      } else if (key === 'del') {
        t = this.editbox.text();
        if (t.length > 0) {
          this.editbox.text(t.substring(0, t.length - 1));
        }
      } else {
        this.trigger('command', key);
      }
    },

    addToolbar: function(name, value) {
      var i, max;

      var Toolbar = Backbone.View.extend({
        tag: 'div',
        attributes: {
          'style': 'display:none'
        }
      });

      this.toolbars[name] = new Toolbar();

      for (i = 0, max = value.length; i < max; i++) {
        // add the command if provided
        if (value[i].definition) {
          this.addCommand(value[i].command, value[i].definition);
        }
        // add the button
        this.toolbars[name].$el.append(new BtnSide({
          parent: this,
          btn: OB.COMP.ButtonKey.extend({
            command: value[i].command,
            classButtonActive: value[i].classButtonActive || 'btnactive-green',
            permission: (value[i].definition ? value[i].definition.permission : null),
            contentViewButton: [value[i].label]
          })
        }).render().$el);
      }
      while (i < 6) {
        this.toolbars[name].$el.append(new BtnSide({
          parent: this,
          btn: OB.COMP.ButtonKey
        }).render().$el);
        i++;
      }

      this.toolbarcontainer.append(this.toolbars[name].$el);
    },

    addToolbarView: function(name, component) {
      this.toolbars[name] = new component({
        parent: this
      }).render();
      this.toolbarcontainer.append(this.toolbars[name].$el);
    },

    addKeypad: function(component) {
      // Initialize keypad...
      var inst = new component({
        parent: this
      }).render();
      this.keypads[inst.name] = inst;
      this.keypadcontainer.append(inst.$el);
    },

    showKeypad: function(padname) {
      var t;
      for (t in this.keypads) {
        if (this.keypads.hasOwnProperty(t)) {
          this.keypads[t].$el.hide();
        }
      }
      this.keypad = this.keypads[padname || 'index'];
      this.keypad.$el.show();
      this.trigger('keypad', this.keypad.name);
    },

    showSidepad: function(sidepadname) {
      this.sideenabled.hide();
      this.sidedisabled.hide();
      this[sidepadname].show();
    },

    attr: function(attrs) {
      var attr;
      for (attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
          this.addToolbar(attr, attrs[attr]);
        }
      }
    }
  });

  OB.COMP.KeyboardCash = OB.COMP.Keyboard.extend({
    initialize: function() {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
    }
  });

}());


enyo.kind({
  name: 'OB.UI.Keyboard',

  commands: {},
  buttons: {},
  status: '',

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
              style: 'font-size: 35px; height: 33px; padding: 22px 5px 0px 0px;',
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
          components: [{
            kind: 'OB.UI.KeypadBasic'
          }]
        }, {
          tag: 'div',
          classes: 'span4',
          components: [{
            //sideenabled
            tag: 'div',
            // style: 'display:none',
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
                  command: 'line:dto'
                }]
              }]
            }]
          }, {
            //sidedisabled
            tag: 'div',
            style: 'display:none',
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
    onCommandFired: ''
  },

  handlers: {
    onCommandFired: 'commandHandler'
  },

  setStatus: function(newstatus) {
    var btn = this.buttons[this.status];
    if (btn) {
      //TODO: btn.$el.removeClass(btn.classButtonActive);
    }
    this.status = newstatus;
    //TODO: used in coins this.trigger('status', this.status);
    btn = this.buttons[this.status];
    if (btn) {
      // TODO:btn.$el.addClass(btn.classButtonActive);
    }
  },

  execCommand: function(cmddefinition, txt) {
	  console.log('execCommand', arguments);
    if (!cmddefinition.permissions || OB.POS.modelterminal.hasPermission(cmddefinition.permissions)) {
      cmddefinition.action.call(this, txt);
    }
  },

  execStatelessCommand: function(cmd, txt) {
    this.commands[cmd].action.call(this, txt);
  },

  getNumber: function() {
    return OB.I18N.parseNumber(this.getString());
  },

  getString: function() {
    var s = this.$.editbox.getContent();
    this.$.editbox.setContent('');
    return s;
  },

  commandHandler: function(snender, event) {
    console.log('command fired', arguments);
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

  initComponents: function() {
    var me = this;

    this.inherited(arguments);

    //Special case to manage the dot (.) pressing in the numeric keypad (only can be managed using keydown)
    $(window).keydown(function(e) {
      if (window.fixFocus()) {
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
      }
      return true;
    });

    $(window).keypress(function(e) {
      if (window.fixFocus()) {
        if (e.which !== 46 || OB.Format.defaultDecimalSymbol === '.') { //Any keypress except any kind of dot (.)
          me.keyPressed(String.fromCharCode(e.which));
        }
      }
    });
  },

  keyPressed: function(key) {
    console.log('key pressed', key);
    var t;
    if (key.match(/^([0-9]|\.|,|[a-z])$/)) {
      t = this.$.editbox.getContent();
      this.$.editbox.setContent(t + key);
    } else if (key === 'del') {
      t = this.$.editbox.getContent();
      if (t.length > 0) {
        this.$.editbox.setContent(t.substring(0, t.length - 1));
      }
    } else {
      //TODO: this.trigger('command', key);
      this.doCommandFired({
        key: key
      });
    }
  },

  addToolbar: function(buttons) {
    var emptyBtn = {
      kind: 'OB.UI.BtnSide',
      btn: {}
    },
        i = 0;

    enyo.forEach(buttons, function(btnDef) {
      this.$.toolbarcontainer.createComponent({
        kind: 'OB.UI.BtnSide',
        btn: btnDef
      });
      i++;
    }, this);

    // populate toolbar up to 6 empty buttons
    for (; i < 6; i++) {
      this.$.toolbarcontainer.createComponent(emptyBtn);
    }
  },

  addCommand: function(cmd, definition) {
    this.commands[cmd] = definition;
  },

  addButton: function(cmd, btn) {
    if (this.buttons[cmd]) {
      if (this.buttons[cmd].add) {
        this.buttons[cmd] = this.buttons[cmd].add(btn);
      }
    } else {
      this.buttons[cmd] = btn;
    }
  }
});

enyo.kind({
  name: 'OB.UI.BtnSide',
  tag: 'div',
  style: 'display:table; width:100%',
  initComponents: function() {
    console.log('a');
    this.createComponent({
      kind: 'OB.UI.ButtonKey',
      label: this.btn.label,
      command: this.btn.command,
      definition: this.btn.definition
    });
  }
});