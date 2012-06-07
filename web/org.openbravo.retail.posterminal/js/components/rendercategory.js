/*global define, $ */

define(['builder', 'utilities', 'utilitiesui', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderCategory = OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [
            {kind: OB.UTIL.Thumbnail, attr: {img: this.model.get('img')}}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 80%;'}, content: [
            this.model.get('category')._identifier
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      return this;
    }
  });
});
