/*global $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var BtnSide = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    initialize: function () {
      var inst = new this.options.btn({
        parent: this.options.parent
      });
      inst.render();
      this.$el.append($('<div/>').attr('class', 'span12').append(inst.$el));
    }
  });

  var ButtonDummy = Backbone.View.extend({
    tag: 'div',
    initialize: function () {
      this.$el.attr('class', this.options.className);
    }
  });

  var BtnAction = Backbone.View.extend({
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
          className: 'btnkeyboard ' + this.classButton
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
              view: BtnAction.extend({
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
            tag: 'div',
            attributes: {
              'class': 'span8'
            },
            content: [{
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
                    command: OB.Format.defaultDecimalSymbol,
                    classButton: 'btnkeyboard-num',
                    contentViewButton: [OB.Format.defaultDecimalSymbol]
                  })
                }]
              }]
            }]
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
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
                  view: BtnAction.extend({
                    command: 'line:dto',
                    permission: 'OBPOS_order.discount',
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
                  view: BtnAction.extend({
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

    show: function (toolbar) {
      var t;
      this.clear();
      if (toolbar) {
        for (t in this.toolbars) {
          if (this.toolbars.hasOwnProperty(t)) {
            this.toolbars[t].hide();
          }
        }
        this.toolbars[toolbar].show();
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

      this.toolbars[name] = $('<div/>').attr('style', 'display:none');

      for (i = 0, max = value.length; i < max; i++) {
        // add the command if provided
        if (value[i].definition) {
          this.addCommand(value[i].command, value[i].definition);
        }
        // add the button   
        this.toolbars[name].append(new BtnSide({
          parent: this,
          btn: BtnAction.extend({
            command: value[i].command,
            classButtonActive: value[i].classButtonActive || 'btnactive-green',
            permission: (value[i].definition ? value[i].definition.permission : null),
            contentViewButton: [value[i].label]
          })
        }).render().$el);
      }
      while (i < 6) {
        this.toolbars[name].append(new BtnSide({
          parent: this,
          btn: BtnAction
        }).render().$el);
        i++;
      }

      this.toolbarcontainer.append(this.toolbars[name]);
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