/*global B, $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var parseNumber = function (s) {
    if (OB.Format.defaultDecimalSymbol !== '.') {
      s = s.toString();
      while (s.indexOf(OB.Format.defaultDecimalSymbol) !== -1) {
        s = s.replace(OB.Format.defaultDecimalSymbol, '.');
      }
    }
    return OB.DEC.number(parseFloat(s, 10));
  };

  var BtnAction = function (kb) {

    return Backbone.View.extend({
      tagName: 'div',
      attributes: {'style': 'margin: 5px;'},
      attr: function (attr) {
        var me = this;
        var cmd = attr.command;
        if (attr.command === '---') {
          // It is the null command
          this.command = false;
        } else if (!cmd.match(/^([0-9]|\.|,|[a-z])$/) && cmd !== 'OK' && cmd !== 'del' && cmd !== String.fromCharCode(13) && !kb.commands[cmd]) {
          // is not a key and does not exists the command
          this.command = false;
        } else if (attr.permission && !OB.POS.modelterminal.hasPermission(attr.permission)) {
          // does not have permissions.
          this.command = false;
        } else {
          this.command = attr.command;
        }

        if (this.command) {
          this.button = new OB.COMP.Button();
          this.button.$el.addClass('btnkeyboard');
          this.button.clickEvent = function (e) {
            kb.keyPressed(me.command);
          };
          kb.addButton(this.command, this.button.$el);
        } else {
          this.button = B({kind: B.KindJQuery('div'), id: 'button', attr: {'class': 'btnkeyboard'}});
        }
        this.$el.append(this.button.$el);
      },
      append: function (child) {
        if (child.$el) {
          this.button.$el.append(child.$el);
        }
      }
    });
  };

  OB.COMP.Keyboard = Backbone.View.extend({

    _id: 'keyboard',
    status: '',
    commands: {},
    buttons: {},
    initialize: function () {

      var me = this;

      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), id: 'toolbarcontainer', attr: {'class': 'span4'}},
          {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'margin:5px'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width: 100%; height: 40px;'}, content: [
                    {kind: B.KindJQuery('pre'), attr: {'style': 'font-size:150%;'}, content: [
                      ' ', {kind: B.KindJQuery('span'), id: 'editbox'}
                    ]}
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                {kind: BtnAction(this), attr: {'command': 'del'}, content: [
                  {kind: B.KindJQuery('i'), attr:{'class': 'icon-chevron-left'}}
                ]}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '/'}, content: ['/']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '*'}, content: ['*']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '%'}, content: ['%']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '7'}, content: ['7']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '8'}, content: ['8']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '9'}, content: ['9']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '4'}, content: ['4']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '5'}, content: ['5']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '6'}, content: ['6']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '1'}, content: ['1']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '2'}, content: ['2']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': '3'}, content: ['3']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
                    {kind: BtnAction(this), attr: {'command': '0'}, content: ['0']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    {kind: BtnAction(this), attr: {'command': OB.Format.defaultDecimalSymbol}, content: [OB.Format.defaultDecimalSymbol]}
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                    {kind: BtnAction(this), attr: {'command': '-'}, content: ['-']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                    {kind: BtnAction(this), attr: {'command': '+'}, content: ['+']}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction(this), attr: {'command': 'line:qty'}, content: [OB.I18N.getLabel('OBPOS_KbQuantity')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction(this), attr: {'command': 'line:price', 'permission': 'OBPOS_order.changePrice'}, content: [OB.I18N.getLabel('OBPOS_KbPrice')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction(this), attr: {'command': 'line:dto', 'permission': 'OBPOS_order.discount'}, content: [OB.I18N.getLabel('OBPOS_KbDiscount')]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: BtnAction(this), attr: {'command': 'OK'}, content: [
                      {kind: B.KindJQuery('i'), attr:{'class': 'icon-arrow-left'}}
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
        if (window.fixFocus() && OB.Format.defaultDecimalSymbol !== '.') {
          if (e.keyCode === 110) { //Numeric keypad dot (.)
            me.keyPressed(OB.Format.defaultDecimalSymbol);
          } else if (e.keyCode === 190) { //Character keyboard dot (.)
            me.keyPressed('.');
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
      var i = parseNumber(this.editbox.text());
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
      var i, max, content;
      content = [];
      for (i = 0, max = value.length; i < max; i++) {
        // add the command if provided
        if (value[i].definition) {
          this.addCommand(value[i].command, value[i].definition);
        }
        // add the button
        content.push({kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
            {kind: BtnAction(this), attr: {'command': value[i].command, 'permission': (value[i].definition ? value[i].definition.permission : null)}, content: [value[i].label]}
          ]}
        ]});
      }
      while (i < 6) {
        content.push({kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
            {kind: BtnAction(this), attr: {'command': '---'}, content: [ {kind: B.KindHTML('<span>&nbsp;</span>')} ]}
          ]}
        ]});
        // content.push({command:'---', label: {kind: B.KindHTML('<span>&nbsp;</span>')}});
        i++;
      }

      this.toolbars[name] = B({kind: B.KindJQuery('div'), attr:{'style': 'display:none;'}, content: content}).$el;
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
            this.receipt.setUnit(this.line, parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:price', {
        'permission': 'OBPOS_order.changePrice',
        'action': function (txt) {
          if (this.line) {
            this.receipt.setPrice(this.line, parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:dto', {
        'permission': 'OBPOS_order.discount',
        'action': function (txt) {
          if (this.line) {
             this.receipt.trigger('discount', this.line, parseNumber(txt));
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
            this.receipt.addUnit(this.line, parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('-', {
        'stateless': true,
        'action': function (txt) {
          if (this.line) {
            this.receipt.removeUnit(this.line, parseNumber(txt));
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
      this.addToolbar('toolbarpayment', new OB.COMP.ToolbarPayment(this.options).toolbar);
      this.addToolbar('toolbarscan', new OB.COMP.ToolbarScan(this.options).toolbar);
    }
  });

  OB.COMP.KeyboardCash = OB.COMP.Keyboard.extend({
    initialize: function () {
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();

    }
  });


}());