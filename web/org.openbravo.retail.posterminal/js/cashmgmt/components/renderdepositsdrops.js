/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDepositsDrops = OB.COMP.CustomView.extend({
    render: function() {
      var time = new Date(this.model.get('time'));
      if(this.model.get('timeOffset')){
        time.setMinutes(time.getMinutes()+ this.model.get('timeOffset') + time.getTimezoneOffset());
      }
      if(this.model.get('drop')!==0){
        this.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px;  float: left; width: 40%'}, content: [
              OB.I18N.getLabel('OBPOS_LblWithdrawal')+': '+this.model.get('description')
            ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: left;  width: 15%'}, content: [
              this.model.get('user')
            ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: left;  width: 10%'}, content: [
              time.toString().substring(16,21)
            ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: right;'}, content: [
              OB.I18N.formatCurrency(OB.DEC.sub(0,this.model.get('drop')))
            ]}
           ]}
        ]}).$el);
        this.me.total= OB.DEC.sub(this.me.total,this.model.get('drop'));
      }else{
        this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; float: left; width: 40%'}, content: [
                OB.I18N.getLabel('OBPOS_LblDeposit')+': '+this.model.get('description')
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: left;  width: 15%'}, content: [
                 this.model.get('user')
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: left;  width: 10%'}, content: [
                 time.toString().substring(16,21)
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; float: right;'}, content: [
                OB.I18N.formatCurrency(OB.DEC.add(0,this.model.get('deposit')))
              ]}
             ]}
          ]}).$el);
        this.me.total= OB.DEC.add(this.me.total,this.model.get('deposit'));
      }
      this.me.trigger('change:total');
      return this;
    }
  });
}());
