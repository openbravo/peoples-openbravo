/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderBusinessPartner = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'line-height: 23px;'
      },
      content: [{
        id: 'dividentifier',
        tag: 'div'
      }, {
        id: 'divlocation',
        tag: 'div',
        attributes: {
          style: 'color: #888888;'
        }
      }, {
        tag: 'div',
        attributes: {
          style: 'clear: both;'
        }
      }]
    }],
    render: function () {
      this.dividentifier.text(this.model.get('_identifier') + "  dd");
      this.divlocation.text(this.model.get('locName'));
      return this;
    }
  });
}());