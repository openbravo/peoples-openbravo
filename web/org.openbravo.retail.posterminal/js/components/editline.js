/*global define */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = function (context) {
    var me = this;   

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #7da7d9; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [                             
          {kind: B.KindJQuery('div'), id: 'msgedit', attr: {'class': 'row-fluid', 'style': 'display: none;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [
              {kind: B.KindJQuery('div'), attr: {style: 'padding: 10px; width:100%'}, content: [                                                                                
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                             
                    {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                      OB.I18N.getLabel('OBPOS_ButtonDelete')
                    ], init: function () {
                      this.$.click(function(e) {
                        e.preventDefault();
                        if (me.line) {
                          me.receipt.deleteLine(me.line);    
                          me.receipt.trigger('scan');
                        }              
                      });
                    }} 
                  ]}
                ]},                
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LineDescription')
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), id: 'editlinename'}
                  ]}                
                ]},      
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LineQuantity')
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), id: 'editlineqty'}
                  ]}                
                ]},                                                             
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LinePrice')                                                                       
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), id: 'editlineprice'}
                  ]}                
                ]},                                                             
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LineValue')
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), content: [                                
                    ]}                
                  ]}                
                ]},                                                             
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LineDiscount')
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), content: [                                
                    ]}                
                  ]}                
                ]},                                                             
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                    OB.I18N.getLabel('OBPOS_LineTotal')
                  ]},  
                  {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [                                
                    {kind: B.KindJQuery('strong'), id: 'editlinenet'}
                  ]}                
                ]}                                                                                                                                      
              ]}                                                                                                                                      
            ]},                             
            {kind: B.KindJQuery('div'), attr: {'class': 'span4;', 'style': ' padding:20px;'}, content: [
              {kind: B.KindJQuery('div'), id: 'editlineimage'}
            ]}                                                      
          ]},
          {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 10px; display: none;'}, content: [
            {kind: B.KindJQuery('div'), id: 'txtaction', attr: {'style': 'float:left;'}}       
          ]}   
        ]}          
      ]}
    );
    
    this.$ = this.component.$;
    this.msgedit = this.component.context.msgedit.$;
    this.msgaction = this.component.context.msgaction.$;
    this.txtaction = this.component.context.txtaction.$;
    this.editlineimage = this.component.context.editlineimage.$;
    this.editlinename = this.component.context.editlinename.$;
    this.editlineqty = this.component.context.editlineqty.$;
    this.editlineprice = this.component.context.editlineprice.$;
    this.editlinenet = this.component.context.editlinenet.$;
    
    // Set Model
    
    this.products = context.DataProduct;
    this.receipt = context.modelorder;
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
          me.editlineimage.empty().append(B(
              {kind: OB.UTIL.Thumbnail, attr: {img: data.img, width: 128, height: 128}}
          ).$);
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