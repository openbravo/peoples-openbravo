/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDepositsDrops = OB.COMP.CustomView.extend({
    render: function() {
      var me = this;
      this.total= OB.DEC.add(this.model.get('startingCash'),this.model.get('totalTendered'));
      if(!this.model.get('total')){
        this.model.set('total', this.total);
      }
      this.dropsdeps = this.model.get('listdepositsdrops');
      if(this.dropsdeps.length!==0){
        this.model.set('total', this.total);
      }

      this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px;  float: left;'}, content: [
                 {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
              ]}
             ]}
          ]}).$el);
      this.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px 20px 6px 10px;  float: left; width: 70%'}, content: [
            OB.I18N.getLabel('OBPOS_LblStarting')+' '+this.model.get('payName')
          ]},
          {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: right;'}, content: [
            OB.I18N.formatCurrency(OB.DEC.add(0,this.model.get('startingCash')))
          ]}
         ]}
      ]}).$el);
      this.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px 20px 6px 10px;  float: left; width: 70%'}, content: [
            OB.I18N.getLabel('OBPOS_LblTotalTendered')+' '+this.model.get('payName')
          ]},
          {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: right;'}, content: [
            OB.I18N.formatCurrency(OB.DEC.add(0,this.model.get('totalTendered')))
          ]}
         ]}
      ]}).$el);

      for(var i=0; i< this.dropsdeps.length; i++) {
        var time = new Date(this.dropsdeps[i].time);
        if(this.dropsdeps[i].timeOffset){
          time.setMinutes(time.getMinutes()+ this.dropsdeps[i].timeOffset + time.getTimezoneOffset());
        }
        if(this.dropsdeps[i].drop!==0){
          this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px 20px 6px 10px;  float: left; width: 40%'}, content: [
                OB.I18N.getLabel('OBPOS_LblWithdrawal')+': '+this.dropsdeps[i].description
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 15%'}, content: [
                this.dropsdeps[i].user
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 10%'}, content: [
                time.toString().substring(16,21)
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: right;'}, content: [
                OB.I18N.formatCurrency(OB.DEC.sub(0,this.dropsdeps[i].drop))
              ]}
             ]}
          ]}).$el);
          me.total= OB.DEC.sub(me.total,this.dropsdeps[i].drop);
        }else{
          this.$el.append(B(
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12','style': 'border-bottom: 1px solid #cccccc;'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 6px 20px 6px 10px; float: left; width: 40%'}, content: [
                  OB.I18N.getLabel('OBPOS_LblDeposit')+': '+this.dropsdeps[i].description
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 15%'}, content: [
                    this.dropsdeps[i].user
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 10%'}, content: [
                   time.toString().substring(16,21)
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 6px 20px 6px 10px; float: right;'}, content: [
                  OB.I18N.formatCurrency(OB.DEC.add(0,this.dropsdeps[i].deposit))
                ]}
               ]}
            ]}).$el);
          me.total= OB.DEC.add(me.total,this.dropsdeps[i].deposit);
        }
        me.trigger('change:total');
      }
      this.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12', 'style': 'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; float: left; width: 70%; font-weight:bold;'}, content: [
                    OB.I18N.getLabel('OBPOS_LblNewAvailableIn')+' '+this.model.get('payName')
            ]},
        {kind: B.KindJQuery('div'), id: 'total', attr: {'style': 'padding: 10px 20px 10px 0px;  float: right; '}, content: [
         {kind: Backbone.View.extend({
           tagName: 'span',
           attributes: {'style': 'float:right;'},
           initialize: function () {
                this.total = $('<strong/>');
                this.$el.append(this.total);
                this.total.text(OB.I18N.formatCurrency(me.total));
                me.on('change:total', function() {
                  this.total.text(OB.I18N.formatCurrency(me.total));
                if(OB.DEC.compare(me.total) < 0){
                   this.$el.css("color","red");//negative value
                }else{
                   this.$el.css("color","black");
                }
                }, this);
                 // Initial total display
                this.total.text(OB.I18N.formatCurrency(me.total));
               if(OB.DEC.compare(me.total) < 0){
                   this.$el.css("color","red");//negative value
               }else{
                   this.$el.css("color","black");
               }
              }
            })}
          ]}
    ]}
    ]}).$el);
      return this;
    }
  });
}());
