/*global define */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'model/order', 'model/terminal', 'components/table'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ListPaymentMethods = function (context) {   
    
    this._id = 'ListPaymentMethods';
    
    this.paymethods = new OB.MODEL.Collection(context.DataPaymentMethod);
     
    this.component = B(
      {kind: B.KindJQuery('div'), content: [ 
        
        {kind: OB.COMP.TableView, id: 'tableview', attr: {
          style: 'list',  
          collection: this.paymethods,
          renderEmpty: function () {
            return (
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                OB.I18N.getLabel('OBPOS_SearchNoResults')
              ]}
            );            
          }        
        }}
      ]}         
    );
    this.$el = this.component.$el;
    this.tableview = this.component.context.tableview;       
    this.tableview.renderLine = function (model) {
      return (
        {kind: B.KindJQuery('button'), attr: {'class': 'btnselect'}, content: [                                                                              
          {kind: B.KindJQuery('div'), content: [ 
            model.get('searchKey')._identifier                                                                                                                                               
          ]}                                                                                   
        ]}
      );               
    };     

    // Exec
    this.paymethods.exec();
  };
  
  OB.COMP.ListPaymentMethods.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;      
  };  
});