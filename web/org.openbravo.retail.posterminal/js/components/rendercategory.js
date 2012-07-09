/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderCategory = OB.COMP.SelectButton.extend({
    className: 'btnselect btnselect-browse',
    contentView: [{
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%'
      },
      content: [{
        id: 'modelthumbnail',
        view: OB.UTIL.Thumbnail
      }]
    }, {
      tag: 'div',
      attributes: {
        style: 'float: left; width: 80%;'
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
      this.modelthumbnail.img = this.model.get('img');
      this.modelthumbnail.render();
      this.modelidentifier.text(this.model.get('_identifier'));
      return this;
    }
  });
}());