/*global define, B , $ , Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ListPaymentMethods = Backbone.View.extend({
	tagName: 'div',
	attributes: {'style': 'position: absolute; top:0px; right: 0px;'},
    initialize: function () {
        var me = this;
	    this._id = 'ListPaymentMethods';
	    this.daycash =  this.options.modeldaycash;
//	    this.options.DataCloseCashPaymentMethod.ds.load();
//	    this.daycash.paymethods = new OB.MODEL.Collection(this.options.DataCloseCashPaymentMethod);
//	    this.daycash.paymethods.ds.load();
	    //***************TEMP*****************************
	    var pay1 = new OB.MODEL.PaymentMethod();
	    pay1.set('id', '1');
	    pay1.set('name', 'Card');
	    pay1.set('financialaccount', '11');
	    pay1.set('expected', -123.62);
	    pay1.set('counted', OB.DEC.Zero);
	    var pay2 = new OB.MODEL.PaymentMethod();
	    pay2.set('id', '2');
	    pay2.set('name', 'Cash');
	    pay2.set('financialaccount', '22');
	    pay2.set('expected', 150.55);
	    pay2.set('counted', OB.DEC.Zero);
	    var pay3 = new OB.MODEL.PaymentMethod();
	    pay3.set('id', '3');
	    pay3.set('name', 'Voucher');
	    pay3.set('financialaccount', '33');
	    pay3.set('expected', 220.54);
	    pay3.set('counted', OB.DEC.Zero);
	    var paycollection = new Backbone.Collection();
	    paycollection.add(pay1);
	    paycollection.add(pay2);
	    paycollection.add(pay3);
	    this.daycash.paymentmethods=paycollection;
	    //************************************************
	    this.daycash.paymentmethods.each(function(payment){
	      me.daycash.set('totalExpected',parseFloat(me.daycash.get('totalExpected'), 10)+parseFloat(payment.get('expected'), 10));
	    });
	    this.daycash.set('totalDifference',parseFloat(this.daycash.get('totalCounted'), 10)-parseFloat(this.daycash.get('totalExpected'), 10));

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
	          collection: this.daycash.paymentmethods,
	          me: me,
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
	                         OB.I18N.getLabel('OBPOS_ReceiptTotal')
	                 ]},
	                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
					{kind: Backbone.View.extend({
					    tagName: 'span',
					    initialize: function () {
					            this.total = $('<strong/>');
					            this.$el.append(this.total);
					            // Set Model
					            this.dayCash = me.options.modeldaycash;
					            this.dayCash.on('change:totalExpected', function() {
					              this.total.text(this.dayCash.get('totalExpected').toString());
					            }, this);
					            // Initial total display
					            this.total.text(this.dayCash.get('totalExpected').toString());
					    }
					  })}
	                 ]},
	                 {kind: B.KindJQuery('div'), id: 'total', attr: {'style': 'padding: 17px 5px 17px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
	                           {kind: Backbone.View.extend({
					    tagName: 'span',
					    initialize: function () {
					            this.total = $('<strong/>');
					            this.$el.append(this.total);
					            // Set Model
					            this.dayCash = me.options.modeldaycash;
					            this.dayCash.on('change:totalDifference', function() {
					              this.total.text(this.dayCash.get('totalDifference').toString());
					            }, this);
					            // Initial total display
					            this.total.text(this.dayCash.get('totalDifference').toString());
					    }
					  })}
	                 ]}
	             ]}
	         ]}
	      ]}
	    );
	    this.$el = this.component.$el;
	    this.tableview = this.component.context.tableview;
    }
  });
}());