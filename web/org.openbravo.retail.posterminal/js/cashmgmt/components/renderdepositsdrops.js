/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDepositsDrops = OB.COMP.CustomView.extend({
    render: function() {
      if(this.model.get('drop')!==0){
        this.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 69%'}, content: [
              'A '+this.model.get('name')+' drop to: '+this.model.get('description')
            ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: right; width: 20%'}, content: [
              '-'+this.model.get('drop').toString()
            ]}
           ]}
        ]}).$el);
        this.me.total= OB.DEC.sub(this.me.total,this.model.get('drop'));
      }else{
        this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 69%'}, content: [
                'A '+this.model.get('name')+' deposit from: '+this.model.get('description')
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'text-align:right; padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: right; width: 20%'}, content: [
                this.model.get('deposit').toString()
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
