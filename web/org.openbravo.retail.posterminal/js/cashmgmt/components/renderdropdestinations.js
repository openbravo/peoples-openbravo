/*global define, $ */

define(['builder', 'utilities', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDropDestinations =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'padding: 10px; border: 1px solid #cccccc;'}, content: [
             'Envelope 1'
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
          {kind: B.KindJQuery('div'), attr: {style: 'padding: 10px; border: 1px solid #cccccc;'}, content: [
             'Envelope 2'
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
          {kind: B.KindJQuery('div'), attr: {style: 'padding: 10px; border: 1px solid #cccccc;'}, content: [
             'Envelope 3'
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
        ]}
      ).$el);
      return this;
    }
  });
});