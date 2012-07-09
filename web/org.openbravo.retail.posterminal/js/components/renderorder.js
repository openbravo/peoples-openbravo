/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderOrder = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'line-height: 23px;'
      },
      content: [{
        tag: 'div',
        content: [{
          id: 'divdate',
          tag: 'div',
          attributes: {
            style: 'float: left; width: 15%'
          }
        }, {
          id: 'divdocumentno',
          tag: 'div',
          attributes: {
            style: 'float: left; width: 25%;'
          }
        }, {
          id: 'divbp',
          tag: 'div',
          attributes: {
            style: 'float: left; width: 60%;'
          }

        }, {
          tag: 'div',
          attributes: {
            style: 'clear: both;'
          }
        }]
      }, {
        id: 'divgross',
        tag: 'div',
        attributes: {
          style: 'text-align: right; font-weight:bold;'
        }
      }]
    }],
    render: function () {
      this.divdate.text(OB.I18N.formatHour(this.model.get('orderDate')));
      this.divdocumentno.text(this.model.get('documentNo'));
      this.divbp.text(this.model.get('bp').get('_identifier'));
      this.divgross.text(this.model.printGross());

      return this;
    }
  });
}());