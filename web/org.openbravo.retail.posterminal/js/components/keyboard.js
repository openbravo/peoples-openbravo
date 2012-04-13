/*global define,$,_,Backbone */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Keyboard = function (context) {    
    var me = this;
    this._id = 'keyboard';
    
    var BtnAction = function (context) {
      var btnme = this;
      this.context = context;
      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'style': 'margin: 5px;'}, content: [
          {kind: B.KindJQuery('a'), id: 'anchor', attr: {'href': '#', 'class': 'btnkeyboard'}, init: function () {           
            this.$.click(function(e) {          
              e.preventDefault();
              me.keyPressed(btnme.command);  
            });                
          }}
        ]}
      );
      this.$ = this.component.$;
      this.anchor = this.component.context.anchor.$;
    };
    BtnAction.prototype.attr = function (attr) {
      this.command = attr.command;
    };
    BtnAction.prototype.append = function (child) {
      if (child.$) {
        this.anchor.append(child.$);
      }
    };
    BtnAction.prototype.inithandler = function (init) {
      if (init) {
        init.call(this);
      }
    };    
    
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span3'}, content: [
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                 
                {kind: BtnAction, attr: {'command': 'paym:cash'}, content: [OB.I18N.getLabel('OBPOS_KbCash')]}
              ]}          
            ]}                
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction, attr: {'command': 'paym:card'}, content: [OB.I18N.getLabel('OBPOS_KbCard')]}
              ]}          
            ]}                
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction, attr: {'command': 'paym:voucher'}, content: [OB.I18N.getLabel('OBPOS_KbVoucher')]}
              ]}          
            ]}                
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction, attr: {'command': '---'}, content: ['---']}
              ]}          
            ]}                
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction, attr: {'command': '---'}, content: ['---']}
              ]}          
            ]}                
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                 
                {kind: BtnAction, attr: {'command': '---'}, content: ['---']}
              ]}          
            ]}                
          ]}          
        ]},    
        {kind: B.KindJQuery('div'), attr: {'class': 'span9'}, content: [ 
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
              {kind: B.KindJQuery('div'), attr: {'style': 'margin:5px'}, content: [ 
                {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width: 100%; height: 40px;'}, content: [ 
                  {kind: B.KindJQuery('pre'), content: [ 
                    ' ', {kind: B.KindJQuery('span'), id: 'editbox'}
                  ]}
                ]}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ 
              {kind: BtnAction, attr: {'command': 'del'}, content: [
                {kind: B.KindJQuery('i'), attr:{'class': 'icon-chevron-left'}}
              ]}
            ]}                        
          ]},                                                                                                                                
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '/'}, content: ['/']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '*'}, content: ['*']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '%'}, content: ['%']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '-'}, content: ['-']}                                                                            
            ]},     
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '+'}, content: ['+']}                                                                            
            ]},   
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '---'}, content: ['---']}                                                                            
            ]}            
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '7'}, content: ['7']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '8'}, content: ['8']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '9'}, content: ['9']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: BtnAction, attr: {'command': 'qty'}, content: [OB.I18N.getLabel('OBPOS_KbQuantity')]}                                                                            
            ]}     
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '4'}, content: ['4']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '5'}, content: ['5']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '6'}, content: ['6']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: BtnAction, attr: {'command': 'price'}, content: [OB.I18N.getLabel('OBPOS_KbPrice')]}                                                                            
            ]}     
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '1'}, content: ['1']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '2'}, content: ['2']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '3'}, content: ['3']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: BtnAction, attr: {'command': 'dto'}, content: [OB.I18N.getLabel('OBPOS_KbDiscount')]}                                                                            
            ]}     
          ]},           
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
              {kind: BtnAction, attr: {'command': '0'}, content: ['0']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
              {kind: BtnAction, attr: {'command': '.'}, content: ['.']}                                                                            
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: BtnAction, attr: {'command': String.fromCharCode(13)}, content: [
                {kind: B.KindJQuery('i'), attr:{'class': 'icon-ok'}}                                                                                      
              ]}                                                                            
            ]}     
          ]}            
        ]}
      ]}                                                                                                                                
    );
    
    this.$ = this.component.$;
    this.editbox =  this.component.context.editbox.$; 
    
    this.products = context.DataProduct;
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

        this.products.ds.find({
          product: {uPCEAN: this.getString()}
        }, function (data) {
          if (data) {      
            me.receipt.addProduct(me.line, new Backbone.Model(data));
            me.receipt.trigger('scan');
          } else {
            alert(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound')); // 'UPC/EAN code not found'
          }
        });
      }         
    }, this);       
    
    $(window).keypress(function(e) {
      me.keyPressed(String.fromCharCode(e.which));
    });     
  };
  
  _.extend(OB.COMP.Keyboard.prototype, Backbone.Events);

  OB.COMP.Keyboard.prototype.clear = function () {
      this.editbox.empty();      
  };
  
  OB.COMP.Keyboard.prototype.show = function () {
      this.clear();
      this.$.show();      
  };
  
  OB.COMP.Keyboard.prototype.hide = function () {
      this.$.hide();    
  };
  
  OB.COMP.Keyboard.prototype.getNumber = function () {
    var i = parseInt(this.editbox.text(), 10);
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