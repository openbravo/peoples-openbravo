/*global B */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderView = function (context) {

    // Set Model
    this.receipt =  context.modelorder;
    var lines = this.receipt.get('lines');

    this.receipt.on('change:gross', this.renderTotal, this);
    this.receipt.on('change:orderType', this.renderFooter, this);
    this.receipt.on('change:generateInvoice', this.renderFooter, this);

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: OB.COMP.TableView, id: 'tableview', attr: {
          style: 'edit',
          collection: lines,
          renderEmpty: function () {
            return (
              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc'}, content: [
                 OB.I18N.getLabel('OBPOS_ReceiptNew')
              ]}
            );
          },
          renderLine: OB.COMP.RenderOrderLine
        }},
        {kind: B.KindJQuery('ul'), attr: {'class': 'unstyled'}, content: [
//          {kind: B.KindJQuery('li'), content: [
//            {kind: B.KindJQuery('div'), attr: {style: 'position: relative; padding: 10px; border-bottom: 1px solid #cccccc;'}, content: [
//              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%; color:  #888888'}, content: [
//                OB.I18N.getLabel('OBPOS_ReceiptTaxes')
//              ]},
//              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [
//                OB.I18N.formatCurrency(0)
//              ]},
//              {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
//            ]}
//          ]},

          {kind: B.KindJQuery('li'), content: [
            {kind: B.KindJQuery('div'), attr: {style: 'position: relative; padding: 10px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%'}, content: [
                OB.I18N.getLabel('OBPOS_ReceiptTotal')
              ]},
              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [
                {kind: B.KindJQuery('strong'), id: 'totalgross'}
              ]},
              {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
            ]}
          ]},
          {kind: B.KindJQuery('li'), content: [
            {kind: B.KindJQuery('div'), id: 'footer', attr: {style: 'padding: 10px; border-top: 1px solid #cccccc; text-align: center; font-weight: bold; font-size: 30px; color: #f8941d'}}
          ]}
        ]}
      ]}
    );
    this.$el = this.component.$el;
    this.$footer = this.component.context.footer.$el;
    this.totalgross = this.component.context.totalgross.$el;
    this.tableview = this.component.context.tableview;

    // Initial total display...
    this.renderFooter();
    this.renderTotal();
  };

  OB.COMP.OrderView.prototype.renderFooter = function () {
    var s = [];
    if (this.receipt.get('orderType') === 1) {
      s.push(OB.I18N.getLabel('OBPOS_ToBeReturned'));
    }
    if (this.receipt.get('generateInvoice')) {
      s.push(OB.I18N.getLabel('OBPOS_ToInvoice'));
    }     
    
    if (s.length > 0) {
      this.$footer.text(s.join(' / '));
      this.$footer.show();
    } else {
      this.$footer.hide();
    }
  };

  OB.COMP.OrderView.prototype.renderTotal = function () {
    this.totalgross.text(this.receipt.printTotal());
  };
}());