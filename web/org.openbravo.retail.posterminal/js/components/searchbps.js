/*global define, Backbone */

define(['builder', 'utilities', 'i18n', 'components/commonbuttons', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchBP = function (context) {
    var me = this;
    
    this._id = 'SearchBPs';

    this.receipt = context.modelorder;
    this.bps = new OB.MODEL.Collection(context.DataBPs);    
    
    this.bps.on('click', function (model) {
      this.receipt.setBPandBPLoc(new Backbone.Model(model.get('BusinessPartner')), new Backbone.Model(model.get('BusinessPartnerLocation')));
    }, this);
    
    this.receipt.on('clear', function() {
      me.bpname.val('');
      this.bps.exec({});
    }, this);  
 
   this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [    
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span8'}, content: [    
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'},  content: [    
                {kind: B.KindJQuery('div'), content: [    
                  {kind: B.KindJQuery('input'), id: 'bpname', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech'}},
                  {kind: OB.COMP.ClearButton}
                ]}                  
              ]}                   
            ]},                                                               
            {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [ 
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'}, content: [    
                {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-gray', 'style': 'float:right;'}, content: [
                  {kind: B.KindJQuery('i'), attr: {'class': 'icon-search'}}, OB.I18N.getLabel('OBPOS_SearchButtonSearch')
                ], init: function () {
                  this.$el.click(function (e) {
                    e.preventDefault();
                    var filter = {};
                    if (me.bpname.val() && me.bpname.val() !== '') {
                      filter = {
                          BusinessPartner :{
                            _identifier : '%i' + OB.UTIL.escapeRegExp(me.bpname.val())
                          }
                      };
                    }
                    me.bps.exec(filter);
                  });
                }}                                                                   
              ]}                                                                   
            ]}                    
          ]},          
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [    
              {kind: B.KindJQuery('div'), content: [ 
                {kind: OB.COMP.TableView, id: 'tableview', attr: {
                  collection: this.bps,
                  renderEmpty: function () {
                    return (
                      {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                        OB.I18N.getLabel('OBPOS_SearchNoResults')
                      ]}
                    );            
                  }            
                }}
              ]}                   
            ]}                   
          ]}                                                             
        ]}                                                                   
      ]}                      
    );
    this.$el = this.component.$el;
    this.bpname = this.component.context.bpname.$el;
    this.tableview = this.component.context.tableview;       
    this.tableview.renderLine = function (model) {
      return (
        {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [                                                                                   
          {kind: B.KindJQuery('div'), content: [ 
            model.get('BusinessPartner')._identifier
          ]}
        ]}
      );                    
    };           
  };
  
  OB.COMP.SearchBP.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;      
  };  
  
}); 