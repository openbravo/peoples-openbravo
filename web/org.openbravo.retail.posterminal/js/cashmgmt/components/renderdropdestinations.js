/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDropDestinations =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'padding: 10px; border: 1px solid #cccccc;'}, content: [
             this.model.get('name')
          ]}
        ]}
      ).$el);
      return this;
    }
  });
}());