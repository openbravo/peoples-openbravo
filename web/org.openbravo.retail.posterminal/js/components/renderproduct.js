/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderProduct =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%'}, content: [
            {kind: OB.UTIL.Thumbnail, attr: {img: this.model.get('img')}}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 60%;'}, content: [
            {kind: B.KindJQuery('div'), attr: {style: 'padding-left: 5px;'}, content: [
              this.model.get('_identifier')
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [
            {kind: B.KindJQuery('strong'), content: [
              OB.I18N.formatCurrency(this.model.get('price').get('listPrice'))
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      return this;
    }
  });
}());
