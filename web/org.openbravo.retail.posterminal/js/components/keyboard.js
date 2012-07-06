/*global B, $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
        
  var BtnSide = Backbone.View.extend({
    tagName: 'div',
    attributes: {'class': 'row-fluid'},
    initialize: function () {
      var inst = new this.options();
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
    kb: null,
    classButton: '',
    command: false,
    permission: null,
    label: null,
    
    tagName: 'div',
    attributes: {'style': 'margin: 5px;'},
    initialize: function (attr) {
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
        this.kb.addButton(this.command, this.button.$el);
      } else {
        this.button = new ButtonDummy({className: 'btnkeyboard ' + this.classButton});
      }
      
      if (this.label) {
        this.button.$el.text(this.label);
      } else {
        this.button.$el.append();
      }
      
      if (this.label) {
        this.button.$el.text(this.label);
      }
      
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
    initialize: function () {
      
      this.options[this.optionsid] = this;

      var me = this;

      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), id: 'toolbarcontainer', attr: {'class': 'span3'}},
          {kind: B.KindJQuery('div'), attr: {'class': 'span9'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'margin:5px'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width: 100%; height: 40px;'}, content: [
                    {kind: B.KindJQuery('pre'), attr: {'style': 'font-size: 35px; height: 33px; padding: 22px 5px 0px 0px;'}, content: [
                      ' ', {kind: B.KindJQuery('span'), attr: {'style': 'margin-left: -10px;'} , id: 'editbox'}
                    ]}
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                {kind: BtnAction.extend({kb: this, command: 'del'}), content: [
                  {kind: B.KindJQuery('div'), attr:{'class': 'btn-icon btn-icon-backspace'}}
                ]}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '/', classButton: 'btnkeyboard-num'}), content: ['/']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '*', classButton: 'btnkeyboard-num'}), content: ['*']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '%', classButton: 'btnkeyboard-num'}), content: ['%']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '7', classButton: 'btnkeyboard-num'}), content: ['7']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '8', classButton: 'btnkeyboard-num'}), content: ['8']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '9', classButton: 'btnkeyboard-num'}), content: ['9']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '4', classButton: 'btnkeyboard-num'}), content: ['4']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '5', classButton: 'btnkeyboard-num'}), content: ['5']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '6', classButton: 'btnkeyboard-num'}), content: ['6']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '1', classButton: 'btnkeyboard-num'}), content: ['1']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '2', classButton: 'btnkeyboard-num'}), content: ['2']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '3', classButton: 'btnkeyboard-num'}), content: ['3']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '0', classButton: 'btnkeyboard-num'}), content: ['0']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction.extend({kb: this, command: OB.Format.defaultDecimalSymbol, classButton: 'btnkeyboard-num'}), content: [OB.Format.defaultDecimalSymbol]}
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '-', classButton: 'btnkeyboard-num btnkeyboard-minus'}), content: ['-']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                    {kind: BtnAction.extend({kb: this, command: '+', classButton: 'btnkeyboard-num btnkeyboard-plus'}), content: ['+']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction.extend({kb: this, command: 'line:qty'}), content: [OB.I18N.getLabel('OBPOS_KbQuantity')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction.extend({kb: this, command: 'line:price', permission: 'OBPOS_order.changePrice'}), content: [OB.I18N.getLabel('OBPOS_KbPrice')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction.extend({kb: this, command: 'line:dto', permission: 'OBPOS_order.discount'}), content: [OB.I18N.getLabel('OBPOS_KbDiscount')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction.extend({kb: this, command: 'OK'}), content: [
                      {kind: B.KindJQuery('div'), attr:{'class': 'btn-icon btn-icon-enter'}}
                    ]}
                  ]}
                ]}
              ]}
            ]}
          ]}
        ]}
      );
      this.setElement(this.component.$el);

      this.editbox =  this.component.context.editbox.$el;
      this.toolbarcontainer = this.component.context.toolbarcontainer.$el;
      this.toolbars = {};

      this.on('command', function(cmd) {
        var txt;
        var me = this;
        if (this.editbox.text() && cmd === String.fromCharCode(13) ) {
          // Barcode read using an scanner or typed in the keyboard...
          this.execCommand(this.commands.code, this.getString());
        } else if (cmd === 'OK') {

          // Accepting a command
          txt = this.getString();

          if (txt && this.status === '') {
            // It is a barcode
            this.execCommand(this.commands.code, txt);
          } else if (txt && this.status !=='') {
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
      $(window).keydown(function(e) {
        if (window.fixFocus()){
          if (OB.Format.defaultDecimalSymbol !== '.') {
            if (e.keyCode === 110) { //Numeric keypad dot (.)
              me.keyPressed(OB.Format.defaultDecimalSymbol);
            } else if (e.keyCode === 190) { //Character keyboard dot (.)
              me.keyPressed('.');
            }
          }
          if(e.keyCode === 8) { //del key
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

    setStatus: function (newstatus) {
      if (this.buttons[this.status]) {
        this.buttons[this.status].removeClass('btnactive');
      }
      this.status = newstatus;
      if (this.buttons[this.status]) {
        this.buttons[this.status].addClass('btnactive');
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

    addCommand: function(cmd, definition) {
      this.commands[cmd] = definition;
    },

    addButton: function(cmd, btn) {
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
        this.toolbars[name].append(new BtnSide(BtnAction.extend({kb: this, command: value[i].command, permission: (value[i].definition ? value[i].definition.permission : null), label: value[i].label})).render().$el);        
      }
      while (i < 6) {
        this.toolbars[name].append(new BtnSide(BtnAction.extend({kb: this})).render().$el);        
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
            if(dataPrices){
              _.each(dataPrices.models, function(currentPrice){
                if(dataProducts.get(currentPrice.get('product'))){
                  dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
                }
              });
              _.each(dataProducts.models, function(currentProd){
                if(currentProd.get('price')===undefined){
                  var price = new OB.Model.ProductPrice({'listPrice': 0});
                  dataProducts.get(currentProd.get('id')).set('price', price);
                  OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
                }
              });
            }else{
              OB.UTIL.showWarning("OBDAL No prices found for products");
              _.each(dataProducts.models, function(currentProd){
                var price = new OB.Model.ProductPrice({'listPrice': 0});
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
            if(dataProducts && dataProducts.length > 0){
              criteria = {'priceListVersion' : OB.POS.modelterminal.get('pricelistversion').id};
              OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
            }else{
              // 'UPC/EAN code not found'
              OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [txt]));
            }
          }

          criteria={
            'uPCEAN' : txt
          };
          OB.Dal.find(OB.Model.Product, criteria , successCallbackProducts, errorCallback);
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