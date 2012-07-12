/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabPayment = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#payment',
    initialize: function () {
      OB.COMP.ToolbarButtonTab.prototype.initialize.call(this); // super.initialize();
      this.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'style': 'text-align: center; font-size: 30px;'}, content: [
          {kind: B.KindJQuery('span'), attr: {'style': 'font-weight: bold; margin: 0px 5px 0px 0px;'}, content: [
            {kind: OB.COMP.Total}
          ]},
          {kind: B.KindJQuery('span'), content: [
            //OB.I18N.getLabel('OBPOS_LblPay')
          ]}
        ]}
      , this.options).$el);
    },
    render: function () {
      OB.COMP.ToolbarButtonTab.prototype.render.call(this); // super.initialize();
      this.$el.removeClass('btnlink-gray');
      return this;
    },
    shownEvent: function(e) {
      this.options.keyboard.show('toolbarpayment');
    }
  });

  OB.COMP.TabPayment = Backbone.View.extend({
    tagName: 'div',
    attributes: {'id': 'payment', 'class': 'tab-pane'},
    initialize: function () {
      var paymentCoins = new OB.COMP.Payment(this.options);
      this.$el.append(paymentCoins.$el);
    }
  });

}());