/*global define */

define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = function (context) {
    var me = this;   
    
    this.editlineimage = OB.UTIL.EL(
      {tag: 'div'}
    );
    this.editlinename = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlineqty = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlineprice = OB.UTIL.EL(
      {tag: 'strong'}
    );
    this.editlinenet = OB.UTIL.EL(
      {tag: 'strong'}
    );
    
    this.msgedit = OB.UTIL.EL(
      {tag: 'div', attr: {'class': 'row-fluid', 'style': 'display: none;'}, content: [
        {tag: 'div', attr: {'class': 'span8'}, content: [
          {tag: 'div', attr: {style: 'padding: 10px; width:100%'}, content: [
                                                                             
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span12'}, content: [                                                                             
                {tag: 'a', attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                  OB.I18N.getLabel('OBPOS_ButtonDelete')
                ], init: function () {
                  this.click(function(e) {
                    e.preventDefault();
                    if (me.line) {
                      me.receipt.deleteLine(me.line);    
                      me.receipt.trigger('scan');
                    }              
                  });
                }} 
              ]}
            ]}, 
            
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LineDescription')
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                this.editlinename                
              ]}                
            ]},      
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LineQuantity')
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                this.editlineqty 
              ]}                
            ]},                                                             
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LinePrice')                                                                       
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                this.editlineprice
              ]}                
            ]},                                                             
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LineValue')
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                {tag: 'strong', content: [                                
                ]}                
              ]}                
            ]},                                                             
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LineDiscount')
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                {tag: 'strong', content: [                                
                ]}                
              ]}                
            ]},                                                             
            {tag: 'div', attr: {'class': 'row-fluid'}, content: [
              {tag: 'div', attr: {'class': 'span4'}, content: [
                OB.I18N.getLabel('OBPOS_LineTotal')
              ]},  
              {tag: 'div', attr: {'class': 'span8'}, content: [                                
                this.editlinenet           
              ]}                
            ]}                                                                                                                                      
          ]}                                                                                                                                      
        ]},                             
        {tag: 'div', attr: {'class': 'span4;', 'style': ' padding:20px;'}, content: [
          this.editlineimage
        ]}                                                      
      ]}                              
    );
    
    this.txtaction = OB.UTIL.EL(
      {tag: 'div', attr: {'style': 'float:left;'}}       
    );    
    
    this.msgaction = OB.UTIL.EL(
      {tag: 'div', attr: {'style': 'padding: 10px; display: none;'}, content: [
        this.txtaction                
      ]}          
    );

    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #7da7d9; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [                             
          this.msgedit,
          this.msgaction
        ]}          
      ]}
    );

    // Set Model
    
    this.products = context.get('DataProduct');
    this.receipt = context.get('modelorder');
    this.line = null;
    
    this.receipt.get('lines').on('selected', function (line) {
      if (this.line) {
        this.line.off('change', this.renderLine);
      }    
      this.line = line;
      if (this.line) {
        this.line.on('change', this.renderLine, this);     
      }      
      this.renderLine();
    }, this);   
    
    this.renderLine();
  };
  
  OB.COMP.EditLine.prototype.renderLine = function () {
    
    var me = this;  
    if (this.line) {      
      this.products.ds.find({
        product: {id: this.line.get('productid')}
      }, function (data) {
        if (data) {
          me.msgaction.hide();
          me.msgedit.show();
          me.editlineimage.empty().append(OB.UTIL.getThumbnail(data.img, 128, 128));
          me.editlinename.text(data.product._identifier);
          me.editlineqty.text(me.line.printQty());
          me.editlineprice.text(me.line.printPrice());
          me.editlinenet.text(me.line.printNet());
        }
      });
    } else {
      me.txtaction.text(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      me.msgedit.hide();
      me.msgaction.show();
      me.editlineimage.empty();
      me.editlinename.empty();
      me.editlineqty.empty();
      me.editlineprice.empty();
      me.editlinenet.empty();
    }    
  };
}); 