/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPayments = OB.COMP.CustomView.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc;'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; float: left; width: 20%'}, content: [
                    this.model.get('name')
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; float: left; width: 20%'}, content: [
                    OB.I18N.formatCurrency(OB.DEC.add(0,this.model.get('expected')))
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'float: left;'}, content: [
                       {kind: OB.COMP.ButtonEdit.extend({rowid :this.model.get('id'), searchKey :this.model.get('_id'), commercialName :this.model.get('name'), _id :'edit_'+this.model.get('id'), me:this.me, attributes:{'button':'editbutton'}})}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'float: left;'}, content: [
                       {kind: OB.COMP.ButtonOk.extend({ rowid :this.model.get('id'), searchKey :this.model.get('_id'), _id :'ok_'+this.model.get('id'), me:this.me, attributes:{'button':'okbutton', 'key':this.model.get('_id')}})}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'float: left; padding: 10px 0px 10px 10px;'}, content: [
                       {kind: B.KindJQuery('div'), rowid :this.model.get('id'), id :'counted_'+this.model.get('id'), attr:{'searchKey':this.model.get('_id'), 'button':'countedbutton', 'hidden':'hidden'},
                        content:[OB.I18N.formatCurrency(0)]
                       }
                  ]}

               ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      var pay = new OB.MODEL.PaymentMethod();
      pay.set('id',this.model.get('id'));
      pay.set('_id',this.model.get('_id'));
      pay.set('name',this.model.get('name'));
      pay.set('expected',OB.DEC.add(0,this.model.get('expected')));
      pay.set('paymentMethod',this.model.get('paymentMethod'));
      this.me.options.modeldaycash.paymentmethods.add(pay);
      this.me.options.modeldaycash.set('totalExpected',OB.DEC.add(this.me.options.modeldaycash.get('totalExpected'),this.model.get('expected')));
      this.me.options.modeldaycash.trigger('change:totalCounted');
      return this;
    }
  });
}());
