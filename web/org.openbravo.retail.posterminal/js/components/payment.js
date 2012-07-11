/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  var DoneButton = OB.COMP.RegularButton.extend({
    'label': OB.I18N.getLabel('OBPOS_LblDone'),
    'clickEvent': function() {
      var parent = this.options.parent;
      parent.receipt.calculateTaxes(function () {
        parent.receipt.trigger('closed');
        parent.modelorderlist.deleteCurrent();
      });
    }                    
  });
  
  var RemovePayment = Backbone.View.extend({
    tag: 'a',
    attributes: {'href': '#'},    
    contentView: [{tag: 'i', attributes: {'class': 'icon-remove icon-white'}}],
    initialize: function () {
      OB.UTIL.initContentView(this);
      var parent = this.options.parent;
      this.$el.click(function(e) {
        e.preventDefault();
        parent.options.parent.options.parent.receipt.removePayment(parent.options.model);
      });  
    }
  });
   
  var RenderPaymentLine = OB.COMP.SelectPanel.extend({
    contentView : [
        {tag: 'div', attributes: {'style': 'color:white;'}, content: [
          {tag: 'div', id: 'divname', attributes: {style: 'float: left; width: 40%'}},
          {tag: 'div', id: 'divamount', attributes: {style: 'float: left; width: 40%; text-align:right;'}},
          {tag: 'div', attributes: {style: 'float: left; width: 20%; text-align:right;'}, content: [
            {view: RemovePayment}
          ]},
          {tag: 'div', attributes: {style: 'clear: both;'}}
        ]}                                          
    ],
    render: function () {
      this.divname.text(OB.POS.modelterminal.getPaymentName(this.model.get('kind')));
      this.divamount.text(this.model.printAmount());
      return this;
    }
  });

  OB.COMP.Payment = Backbone.View.extend({
    tag: 'div',
    contentView: [
                  
          {tag: 'div', attributes: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
            {tag: 'div', attributes: {'class': 'row-fluid'}, content: [
              {tag: 'div', attributes: {'class': 'span12'}, content: [
              ]}
            ]},
            {tag: 'div', attributes: {'class': 'row-fluid'}, content: [
              {tag: 'div', attributes: {'class': 'span7'}, content: [
                {tag: 'div', attributes: {'style': 'padding: 10px 0px 0px 10px;'}, content: [
                  {tag: 'span', id: 'totalpending', attributes: {style: 'font-size: 24px; font-weight: bold;'}},
                  {tag: 'span', id: 'totalpendinglbl', content: [OB.I18N.getLabel('OBPOS_PaymentsRemaining')]},
                  {tag: 'span', id: 'change', attributes: {style: 'font-size: 24px; font-weight: bold;'}},
                  {tag: 'span', id: 'changelbl', content: [OB.I18N.getLabel('OBPOS_PaymentsChange')]},
                  {tag: 'span', id: 'overpayment', attributes: {style: 'font-size: 24px; font-weight: bold;'}},
                  {tag: 'span', id: 'overpaymentlbl', content: [OB.I18N.getLabel('OBPOS_PaymentsOverpayment')]}
                ]},
                {tag: 'div', attributes: {style: 'overflow:auto; width: 100%;'}, content: [
                  {tag: 'div', attributes: {'style': 'padding: 5px'}, content: [
                    {tag: 'div', attributes: {'style': 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'}, content: [
                    ]},
                    {id: 'tableview', view: OB.UI.TableView.extend({
                      renderEmpty: Backbone.View.extend({
                        tagName: 'div',
                        attributes: {'style': 'height: 36px'}
                      }),                      
                      renderLine: RenderPaymentLine
                    })}
                  ]}
                ]}
              ]},
              {tag: 'div', attributes: {'class': 'span5'}, content: [
                {tag: 'div', attributes: {'style': 'float: right;'}, id: 'doneaction', content: [
                  {view: DoneButton}
                ]}
              ]}
            ]}
          ]}                  
    ],
    initialize : function () {
      
      OB.UTIL.initContentView(this);
      
      var i, max;
      var me = this;

      this.modelorderlist = this.options.modelorderlist;
      this.receipt = this.options.modelorder;
      var payments = this.receipt.get('payments');
      var lines = this.receipt.get('lines');
      
      this.tableview.registerCollection(payments);

      this.receipt.on('change:payment change:change change:gross', function() {
        this.updatePending();
      }, this);
      this.updatePending();
    },
    updatePending : function () {
      var paymentstatus = this.receipt.getPaymentStatus();
      if (paymentstatus.change) {
        this.change.text(paymentstatus.change);
        this.change.show();
        this.changelbl.show();
      } else {
        this.change.hide();
        this.changelbl.hide();
      }
      if (paymentstatus.overpayment) {
        this.overpayment.text(paymentstatus.overpayment);
        this.overpayment.show();
        this.overpaymentlbl.show();
      } else {
        this.overpayment.hide();
        this.overpaymentlbl.hide();
      }
      if (paymentstatus.done) {
        this.totalpending.hide();
        this.totalpendinglbl.hide();        
        this.doneaction.show();
      } else {
        this.totalpending.text(paymentstatus.pending);
        this.totalpending.show();
        this.totalpendinglbl.show();
        this.doneaction.hide();
      }
    }

  });
}());