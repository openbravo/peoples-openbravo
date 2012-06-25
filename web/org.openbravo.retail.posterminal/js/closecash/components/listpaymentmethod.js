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
	    this.paymentmethods = new OB.MODEL.Collection(this.options.DataCloseCashPaymentMethod);
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
	          collection: this.paymentmethods,
	          me: me,
	          renderEmpty: function () {
	            return (
	              {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc;text-align: center; font-weight:bold; font-size: 150%; color: #cccccc'}, content: [
	                OB.I18N.getLabel('OBPOS_SearchNoResults')
	              ]}
	            );
	          },
	          renderLine: OB.COMP.RenderPayments.extend({me:me})
	        }},
	        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc; float: left;'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding-right: 88px; float: right;' }, content: [
                   {kind: OB.COMP.ButtonOk.extend({
                   me:me,
                   attributes:{'button':'allokbutton'},
                   clickEvent: function (e) {
                    var that = this;
                    $('a[button="okbutton"]').hide();
                    this.$el.css('visibility', 'hidden');
                    this.me.options.modeldaycash.paymentmethods.each(function(elem){
                      $('div[searchKey*="'+elem.get("_id")+'"]').text(elem.get('expected').toString());
                      elem.set('counted',OB.DEC.add(0,elem.get('expected')));
                      that.me.options.modeldaycash.set('totalCounted',OB.DEC.add(that.me.options.modeldaycash.get('totalCounted'),elem.get('counted')));
                  });
                    $('div[button*="countedbutton"]').show();
                  }
                     })
                   }
                 ]}
             ]}
         ]},
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
					              me.options.modeldaycash.on('change:totalExpected', function() {
					              this.total.text(me.options.modeldaycash.get('totalExpected').toString());
					            }, this);
					            // Initial total display
					            this.total.text(me.options.modeldaycash.get('totalExpected').toString());
					    }
					  })}
	                 ]},
	                 {kind: B.KindJQuery('div'), id: 'total', attr: {'style': 'padding: 17px 5px 17px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
	                           {kind: Backbone.View.extend({
					    tagName: 'span',
					    attributes: {'style': 'padding-left: 60px'},
					    initialize: function () {
				            this.total = $('<strong/>');
				            this.$el.append(this.total);
				            // Set Model
				            me.options.modeldaycash.on('change:totalCounted', function() {
				              this.total.text((OB.DEC.sub(me.options.modeldaycash.get('totalCounted'),me.options.modeldaycash.get('totalExpected'))).toString());
				              if(OB.DEC.compare(OB.DEC.add(0,this.total.text()) )<0){
						        this.$el.css("color","red");//negative value
						      }else{
						        this.$el.css("color","black");
						      }
				            }, this);
				            // Initial total display
				            this.total.text((OB.DEC.sub(me.options.modeldaycash.get('totalCounted'),me.options.modeldaycash.get('totalExpected'))).toString());
				            if(OB.DEC.compare(OB.DEC.add(0,this.total.text()) )<0){
						      this.$el.css("color","red");//negative value
						    }else{
						      this.$el.css("color","black");
						    }
					    }
					  })}
	                 ]}
	             ]}
	         ]}
	      ]}
	    );
	    this.$el = this.component.$el;
	    this.tableview = this.component.context.tableview;
	    this.paymentmethods.exec();
    }
  });
}());