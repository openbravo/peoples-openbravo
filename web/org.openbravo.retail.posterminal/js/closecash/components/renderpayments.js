/*global define, $ */

define(['builder', 'utilities', 'utilitiesui', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPayments = OB.COMP.CustomView.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
                    this.model.get('_identifier')
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                    '6.195'
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
                       {kind: OB.COMP.ButtonEdit}, {kind: OB.COMP.ButtonOk}
//                       ,{kind: OB.COMP.CustomView.extend({
//                       _id :'countedcash',
//                       createView: function () {
//                          return ({kind: B.KindJQuery('div'),attr:{'style':'padding: 17px 110px 17px 0px; float: right; width: 10%'},
//                        content:['6.195']});
//                       }
//                       })
//                       }
                  ]}

               ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      return this;
    }
  });
});
