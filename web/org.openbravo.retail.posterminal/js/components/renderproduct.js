/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderProduct = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%'
      },
      content: [{
        id: 'viewthumbnail',
        view: OB.UTIL.Thumbnail
      }]
    }, {
      tag: 'div',
      attributes: {
        style: 'float: left; width: 60%;'
      },
      content: [{
        id: 'dividentifier',
        tag: 'div',
        attributes: {
          style: 'padding-left: 5px;'
        }
      }]
    }, {
      id: 'divprice',
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%; text-align: right; font-weight:bold;'
      }
    }, {
      tag: 'div',
      attributes: {
        style: 'clear: both;'
      }
    }],
    render: function () {
      this.viewthumbnail.img = this.model.get('img');
      this.viewthumbnail.render();
      this.dividentifier.text(this.model.get('_identifier'));
      this.divprice.text(OB.I18N.formatCurrency(this.model.get('price').get('listPrice')));
      return this;
    }
  });
}());