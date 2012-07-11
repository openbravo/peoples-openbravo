/*global $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var BtnSide = Backbone.View.extend({
    tagName: 'div',
    attributes: {'style': 'display:table; width:100%'},
    initialize: function () {
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
    initialize: function () {

      this.options[this.optionsid] = this;
      OB.UTIL.initContentView(this);

      var me = this;

      this.toolbars = {};
      
      this.keypads = {};
      this.keypad = '';
      this.addKeypad(OB.COMP.KeypadBasic); // index
      this.showKeypad();     

      this.on('command', function (cmd) {
        var txt;
        var me = this;
        if (this.editbox.text() && cmd === String.fromCharCode(13)) {
          // Barcode read using an scanner or typed in the keyboard...
          this.execCommand(this.commands.code, this.getString());
        } else if (cmd === 'OK') {

          // Accepting a command
          txt = this.getString();

          if (txt && this.status === '') {
            // It is a barcode
            this.execCommand(this.commands.code, txt);
          } else if (txt && this.status !== '') {
            this.execCommand(this.commands[this.status], txt);
            this.setStatus('');
          }
        } else if ((cmd.substring(0, 5) !== 'line:' || this.line) && this.commands[cmd]) {
          txt = this.getString();
          if (this.commands[cmd].stateless) {
            // Stateless commands: add, subs, ...
            this.execStatelessCommand(this.commands[cmd], txt);
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
        }
      }, this);

      //Special case to manage the dot (.) pressing in the numeric keypad (only can be managed using keydown)
      $(window).keydown(function (e) {
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

      $(window).keypress(function (e) {
        if (window.fixFocus()) {
          if (e.which !== 46 || OB.Format.defaultDecimalSymbol === '.') { //Any keypress except any kind of dot (.)
            me.keyPressed(String.fromCharCode(e.which));
          }
        }
      });
    },

    setStatus: function (newstatus) {
      var btn = this.buttons[this.status];
      if (btn) {
        btn.$el.removeClass(btn.classButtonActive);
      }
      this.status = newstatus;
      btn = this.buttons[this.status];
      if (btn) {
        btn.$el.addClass(btn.classButtonActive);
      }
    },

    execCommand: function (cmddefinition, txt) {
      if (!cmddefinition.permissions || OB.POS.modelterminal.hasPermission(cmddefinition.permissions)) {
        cmddefinition.action.call(this, txt);
      }
    },

    execStatelessCommand: function (cmddefinition, txt) {
      cmddefinition.action.call(this, txt);
    },

    addCommand: function (cmd, definition) {
      this.commands[cmd] = definition;
    },

    addButton: function (cmd, btn) {
      if (this.buttons[cmd]) {
        this.buttons[cmd] = this.buttons[cmd].add(btn);
      } else {
        this.buttons[cmd] = btn;
      }
    },

    clear: function () {
      this.editbox.empty();
      this.setStatus('');
    },

    show: function (toolbar, keypad) {
      var t;
      this.clear();
      this.showKeypad(keypad);
      if (toolbar) {
        for (t in this.toolbars) {
          if (this.toolbars.hasOwnProperty(t)) {
            this.toolbars[t].$el.hide();
          }
        }
        this.toolbars[toolbar].$el.show();
      }
      this.$el.show();
    },

    hide: function () {
      this.$el.hide();
    },

    getNumber: function () {
      var i = OB.I18N.parseNumber(this.editbox.text());
      this.editbox.empty();
      return i;
    },

    getString: function () {
      var s = this.editbox.text();
      this.editbox.empty();
      return s;
    },

    keyPressed: function (key) {

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

    addToolbar: function (name, value) {
      var i, max;

      var Toolbar = Backbone.View.extend({
        tag: 'div',
        attributes: {'style': 'display:none'}
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
    
    addToolbarView: function (name, component) {
      this.toolbars[name] = new component({parent: this}).render();
      this.toolbarcontainer.append(this.toolbars[name].$el);
    },
    
    addKeypad: function (component) {
      // Initialize keypad...
      var inst = new component({parent: this}).render();
      this.keypads[inst.name] = inst;
      this.keypadcontainer.append(inst.$el);
    },
    
    showKeypad: function (padname) {
      var t;
      for (t in this.keypads) {
        if (this.keypads.hasOwnProperty(t)) {
          this.keypads[t].$el.hide();
        }
      }
      this.keypad = this.keypads[padname || 'index'];
      this.keypad.$el.show();
    },

    attr: function (attrs) {
      var attr;
      for (attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
          this.addToolbar(attr, attrs[attr]);
        }
      }
    }
  });


  OB.COMP.KeyboardOrder = OB.COMP.Keyboard.extend({
    initialize: function () {
      this.addCommand('line:qty', {
        'action': function (txt) {
          if (this.line) {
            this.receipt.setUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:price', {
        'permission': 'OBPOS_order.changePrice',
        'action': function (txt) {
          if (this.line) {
            this.receipt.setPrice(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:dto', {
        'permission': 'OBPOS_order.discount',
        'action': function (txt) {
          if (this.line) {
            this.receipt.trigger('discount', this.line, OB.I18N.parseNumber(txt));
          }
        }
      });
      this.addCommand('code', {
        'action': function (txt) {
          var criteria, me = this;

          function successCallbackPrices(dataPrices, dataProducts) {
            if (dataPrices) {
              _.each(dataPrices.models, function (currentPrice) {
                if (dataProducts.get(currentPrice.get('product'))) {
                  dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
                }
              });
              _.each(dataProducts.models, function (currentProd) {
                if (currentProd.get('price') === undefined) {
                  var price = new OB.Model.ProductPrice({
                    'listPrice': 0
                  });
                  dataProducts.get(currentProd.get('id')).set('price', price);
                  OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
                }
              });
            } else {
              OB.UTIL.showWarning("OBDAL No prices found for products");
              _.each(dataProducts.models, function (currentProd) {
                var price = new OB.Model.ProductPrice({
                  'listPrice': 0
                });
                currentProd.set('price', price);
              });
            }
            me.receipt.addProduct(new Backbone.Model(dataProducts.at(0)));
            me.receipt.trigger('scan');
          }

          function errorCallback(tx, error) {
            OB.UTIL.showError("OBDAL error: " + error);
          }

          function successCallbackProducts(dataProducts) {
            if (dataProducts && dataProducts.length > 0) {
              criteria = {
                'priceListVersion': OB.POS.modelterminal.get('pricelistversion').id
              };
              OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
            } else {
              // 'UPC/EAN code not found'
              OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [txt]));
            }
          }

          criteria = {
            'uPCEAN': txt
          };
          OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
        }
      });
      this.addCommand('+', {
        'stateless': true,
        'action': function (txt) {
          if (this.line) {
            this.receipt.addUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('-', {
        'stateless': true,
        'action': function (txt) {
          if (this.line) {
            this.receipt.removeUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });

      this.products = this.options.DataProductPrice;
      this.receipt = this.options.modelorder;
      this.line = null;

      this.receipt.get('lines').on('selected', function (line) {
        this.line = line;
        this.clear();
      }, this);

      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
      
      // Toolbars at the end...
      this.addToolbar('toolbarpayment', new OB.UI.ToolbarPayment(this.options).toolbar);
      this.addToolbar('toolbarscan', new OB.COMP.ToolbarScan(this.options).toolbar);
    }
  });

  OB.COMP.KeyboardCash = OB.COMP.Keyboard.extend({
    initialize: function () {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
    }
  });

}());