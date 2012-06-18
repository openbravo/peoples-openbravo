/*global define, setInterval */

define(['builder', 'utilities', 'i18n', 'components/clock'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CloseInfo = function (context) {
    var me = this;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'position: relative; background: darkgray; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: OB.COMP.Clock, attr: {'class': 'pos-clock'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 10px;'}, content: [
              {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange', 'style': 'float:right;'}, content: [
                OB.I18N.getLabel('OBPOS_LblCancel')
              ], init: function () {
                this.$el.click(function(e) {
                  e.preventDefault();
                  ////
                });
              }},
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblEndDayProcess')]} ,
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 20px;'}, content: [{kind: OB.COMP.ButtonPrev},{kind: OB.COMP.ButtonNext}]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblSteps')]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep1')]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep2')]} ,
              //{kind: B.KindJQuery('div'), content: ['3) Prepare report']} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep3')]}
            ]}
          ]}
        ]}
      ]}
    , context);
    this.$el = this.component.$el;
    context.closeprevbutton.$el.attr('disabled','disabled');
    context.countcash.$el.hide();
    context.postprintclose.$el.hide();


  };

});