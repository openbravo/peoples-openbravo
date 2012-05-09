/*global define,$,_,Backbone */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'model/order', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  var BtnAction = function (kb) {    
    var Btn = function (context) {
      this.context = context;
      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'style': 'margin: 5px;'}, content: [
        ]}
      );
      this.$ = this.component.$;
    };
    
    Btn.prototype.attr = function (attr) {
      var me = this;
      var cmd = attr.command;      
      if (attr.command === '---') {
        this.command = false;
      } else if (cmd.substring(0, 5) === 'paym:' && !OB.POS.modelterminal.hasPermission(cmd.substring(5))) {
        this.command = false;
      } else { 
        this.command = attr.command;
      }
      
      if (this.command) {
        this.button = B({kind: B.KindJQuery('a'), id: 'button', attr: {'href': '#', 'class': 'btnkeyboard'}, init: function () {           
            this.$.click(function(e) {          
              e.preventDefault();
              kb.keyPressed(me.command);  
            });                
          }}).$;             
      } else {
        this.button = B({kind: B.KindJQuery('div'), id: 'button', attr: {'class': 'btnkeyboard'}}).$;        
      }
      this.$.append(this.button);   
    };
    
    Btn.prototype.append = function (child) {
      if (child.$el) {
        this.button.append(child.$el);
      } else if (child.$) {
        this.button.append(child.$);
      }
    };
    
    Btn.prototype.inithandler = function (init) {
      if (init) {
        init.call(this);
      }
    };     
    return Btn;
  };

  OB.COMP.Keyboard = function (context) {    
    var me = this;
    this._id = 'keyboard';  
    
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), id: 'toolbarcontainer', attr: {'class': 'span5'}, content: [          
          {kind: B.KindJQuery('div'), id: 'toolbarempty', attr: {'style': 'display:block;'}, content: [                                                                            
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction(this), attr: {'command': '---'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]}          
            ]}               
          ]}          
        ]},   
        
        {kind: B.KindJQuery('div'), attr: {'class': 'span7'}, content: [ 
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [ 
              {kind: B.KindJQuery('div'), attr: {'style': 'margin:5px'}, content: [ 
                {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width: 100%; height: 40px;'}, content: [ 
                  {kind: B.KindJQuery('pre'), content: [ 
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
                  {kind: BtnAction(this), attr: {'command': '.'}, content: ['.']}                                                                            
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
                  {kind: BtnAction(this), attr: {'command': 'qty'}, content: [OB.I18N.getLabel('OBPOS_KbQuantity')]}                                                                            
                ]}     
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: BtnAction(this), attr: {'command': 'price'}, content: [OB.I18N.getLabel('OBPOS_KbPrice')]}                                                                            
                ]}     
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: BtnAction(this), attr: {'command': 'dto'}, content: [OB.I18N.getLabel('OBPOS_KbDiscount')]}                                                                            
                ]}     
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: BtnAction(this), attr: {'command': String.fromCharCode(13)}, content: [
                    {kind: B.KindJQuery('i'), attr:{'class': 'icon-ok'}}                                                                                      
                  ]}                                                                            
                ]}     
              ]}                    
            ]}                
          ]}             
        ]}       
      ]}                                                                                                                               
    );
    
    this.$ = this.component.$;
    this.editbox =  this.component.context.editbox.$; 
    this.toolbarcontainer = this.component.context.toolbarcontainer.$;
    this.toolbars = {
        toolbarempty : this.component.context.toolbarempty.$
    };
    
    this.products = context.DataProductPrice;
    this.receipt = context.modelorder;
    this.line = null;
    
    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
      this.clear();
    }, this);  
    
    this.on('command', function(cmd) {
      var me = this;      
      if (cmd === '-') {
        if (this.line) {
          this.receipt.removeUnit(this.line, this.getNumber());     
          this.receipt.trigger('scan');
        }
      } else if (cmd === '+') {
        if (this.line) {
          this.receipt.addUnit(this.line, this.getNumber());    
          this.receipt.trigger('scan');
        }
      } else if (cmd === 'qty') {
        if (this.line) {
          this.receipt.setUnit(this.line, this.getNumber()); 
          this.receipt.trigger('scan');
        }
      } else if (cmd.substring(0, 5) === 'paym:') {
        // payment
        me.receipt.addPayment(new OB.MODEL.PaymentLine(
          {
            'kind': cmd.substring(5), 
            'amount': this.getNumber()
          }
        ));
      } else if (cmd === String.fromCharCode(13)) {
        var code = this.getString();
        this.products.ds.find({
          priceListVersion: OB.POS.modelterminal.get('pricelistversion').id,
          product: { product: {uPCEAN: code}}
        }, function (data) {
          if (data) {      
            me.receipt.addProduct(me.line, new Backbone.Model(data));
            me.receipt.trigger('scan');
          } else {
            alert(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [code])); // 'UPC/EAN code not found'
          }
        });
      }         
    }, this);       

    $(window).keypress(function(e) {
      me.keyPressed(String.fromCharCode(e.which));
    });     
  }; 
  _.extend(OB.COMP.Keyboard.prototype, Backbone.Events);
  
  OB.COMP.Keyboard.prototype.attr = function (attrs) { 
    var attr,i, max, value, content;
    
    for (attr in attrs) {
      if (attrs.hasOwnProperty(attr)) {
        value = attrs[attr];
        content = [];
        for (i = 0, max = value.length; i < max; i++) {
          content.push({kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
              {kind: BtnAction(this), attr: {'command': value[i].command}, content: [value[i].label]}
            ]}          
          ]});
        }

        this.toolbars[attr] = B({kind: B.KindJQuery('div'), attr:{'style': 'display:none;'},content: content}).$;        
        this.toolbarcontainer.append(this.toolbars[attr]);
      }
    }          
  };

  OB.COMP.Keyboard.prototype.clear = function () {
      this.editbox.empty();      
  };
  
  OB.COMP.Keyboard.prototype.show = function (toolbar) {
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
    this.$.show();      
  };
  
  OB.COMP.Keyboard.prototype.hide = function () {
      this.$.hide();    
  };
  
  OB.COMP.Keyboard.prototype.getNumber = function () {
    var i = OB.DEC.number(parseFloat(this.editbox.text(), 10));
    this.editbox.empty();
    return i;
  };
  
  OB.COMP.Keyboard.prototype.getString = function () {
    var s = this.editbox.text();
    this.editbox.empty();
    return s;
  };  
  
  OB.COMP.Keyboard.prototype.keyPressed = function (key) {

    var t;
    if (key.match(/^([0-9]|\.|[a-z])$/)) {
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
  }; 
    
});