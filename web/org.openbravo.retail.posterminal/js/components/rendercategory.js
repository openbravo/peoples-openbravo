/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderCategory = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        style: 'float: left; width: 25%'
      },
      content: [{
        id: 'modelthumbnail',
        view: OB.UTIL.Thumbnail
      }]
    }, {
      tag: 'div',
      attributes: {
        style: 'float: left; width: 75%;'
      },
      content: [{
        id: 'modelidentifier',
        tag: 'div',
        attributes: {
          style: 'padding-left: 5px;'
        }
      }, {
        tag: 'div',
        attributes: {
          style: 'clear: both;'
        }
      }]
    }],
    render: function () {
      this.$el.addClass('btnselect-browse');
      this.modelthumbnail.img = this.model.get('img');
      this.modelthumbnail.render();
      this.modelidentifier.text(this.model.get('_identifier'));
      return this;
    }
  });
}());