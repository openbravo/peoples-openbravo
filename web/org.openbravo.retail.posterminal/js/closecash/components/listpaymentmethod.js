/*global define */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'model/order', 'model/terminal', 'components/table', 'closecash/components/renderpayments'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListPaymentMethods = function (context) {

    this._id = 'ListPaymentMethods';
    this.paymethods = new OB.MODEL.Collection(context.DataPaymentMethod);

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
       {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
             OB.I18N.getLabel('OBPOS_LblPaymentMethod')
          ]},
        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
               OB.I18N.getLabel('OBPOS_LblExpected')
          ]},
          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 0px 10px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
               OB.I18N.getLabel('OBPOS_LblCounted')
          ]}

        ]}
      ]},
        {kind: OB.COMP.TableView, id: 'tableview', attr: {
          style: 'list',
          collection: this.paymethods,
          renderEmpty: function () {
            return (
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc;text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
                OB.I18N.getLabel('OBPOS_SearchNoResults')
              ]}
            );
          },
          renderLine: OB.COMP.RenderPayments
        }},
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
                         'TOTAL'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                          '8.450,50'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 5px 17px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
                           '0,00'
                 ]}
             ]}
         ]}
      ]}
    );
    this.$el = this.component.$el;
    this.tableview = this.component.context.tableview;
//    this.tableview.renderLine = function (model) {
//      return (
//        {kind: OB.COMP.RenderPayments}
//       );
//    }
  // Exec
    this.paymethods.exec();
  };

  OB.COMP.ListPaymentMethods.prototype.attr = function (attrs) {
    this.tableview.renderLine = attrs.renderLine || this.tableview.renderLine;
  };
});