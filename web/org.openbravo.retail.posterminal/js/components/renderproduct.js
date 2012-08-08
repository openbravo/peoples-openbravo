/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  enyo.kind({
    name: 'OB.UI.RenderProduct',

    kind: 'OB.UI.SelectButton',
    components: [{
      style: 'float: left; width: 25%',
      components: [{
        kind: 'OB.UI.Thumbnail'
      }]
    }, {
      style: 'float: left; width: 55%;',
      components: [{
        name: 'identifier',
        style: 'padding-left: 5px;'
      }]
    },{
    	name: 'price',
    	style: 'float: left; width: 20%; text-align: right; font-weight:bold;'
    },{
    	style:'clear:both;'
    }],
    initComponents: function() {
      this.inherited(arguments);
      this.$.identifier.setContent(this.model.get('_identifier'));
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('price').get('listPrice')));
    }
  });

  OB.COMP.RenderProduct = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        style: 'float: left; width: 25%'
      },
      content: [{
        id: 'viewthumbnail',
        view: OB.UTIL.Thumbnail
      }]
    }, {
      tag: 'div',
      attributes: {
        style: 'float: left; width: 55%;'
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
    render: function() {
      this.viewthumbnail.img = this.model.get('img');
      this.viewthumbnail.render();
      this.dividentifier.text(this.model.get('_identifier'));
      this.divprice.text(OB.I18N.formatCurrency(this.model.get('price').get('listPrice')));
      return this;
    }
  });
}());