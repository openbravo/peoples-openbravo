/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
        tag: 'div',
        content: [{
          id: 'divreturn',
          tag: 'div',
          attributes: {
            style: 'float: left; width: 25%; color: #f8941d; font-weight:bold;'
          }
        }, {
          id: 'divinvoice',
          tag: 'div',
          attributes: {
            style: 'float: left; width: 25%; font-weight:bold;'
          }
        }, {
          id: 'divgross',
          tag: 'div',
          attributes: {
            style: 'float: right; width: 25%; text-align: right; font-weight:bold;'
          }
        }, {
          tag: 'div',
          attributes: {
            style: 'clear: both;'
          }
        }]
      }]
    }],
    render: function () {
      this.divdate.text(OB.I18N.formatHour(this.model.get('orderDate')));
      this.divdocumentno.text(this.model.get('documentNo'));
      this.divbp.text(this.model.get('bp').get('_identifier'));
      this.divreturn.text(this.model.get('orderType') === 1 ? OB.I18N.getLabel('OBPOS_ToReturn') : '');
      this.divinvoice.text(this.model.get('generateInvoice') ? OB.I18N.getLabel('OBPOS_ToInvoice') : '');
      this.divgross.text(this.model.printGross());

      return this;
    }
  });
}());