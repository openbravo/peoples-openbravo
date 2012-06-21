/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPayments = OB.COMP.CustomView.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
                    this.model.get('commercialName')
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                    this.model.get('currentBalance').toString()
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
                       {kind: OB.COMP.ButtonEdit.extend({rowid :this.model.get('id'), searchKey :this.model.get('searchKey'), commercialName :this.model.get('commercialName'), _id :'edit_'+this.model.get('id'), me:this.me})},
                       {kind: OB.COMP.ButtonOk.extend({ rowid :this.model.get('id'), searchKey :this.model.get('searchKey'), _id :'ok_'+this.model.get('id'), me:this.me})},
                       {kind: B.KindJQuery('div'), rowid :this.model.get('id'), id :'counted_'+this.model.get('id'), attr:{'searchKey':this.model.get('searchKey'), 'hidden':'hidden','style':'padding: 17px 110px 17px 0px; float: right; width: 10%'},
                        content:['0']
                       }
                  ]}

               ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      var pay = new OB.MODEL.PaymentMethod();
      pay.set('id',this.model.get('id'));
      pay.set('_id',this.model.get('searchKey'));
      pay.set('name',this.model.get('commercialName'));
      pay.set('expected',OB.DEC.add(0,this.model.get('currentBalance')));
      this.me.options.modeldaycash.paymentmethods.add(pay);
      this.me.options.modeldaycash.set('totalExpected',OB.DEC.add(this.me.options.modeldaycash.get('totalExpected'),this.model.get('currentBalance')));
      this.me.options.modeldaycash.trigger('change:totalCounted');
      return this;
    }
  });
}());
