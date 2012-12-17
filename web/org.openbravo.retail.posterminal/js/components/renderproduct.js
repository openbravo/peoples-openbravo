/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.RenderProduct',

  kind: 'OB.UI.SelectButton',
  components: [{
    style: 'float: left; width: 25%',
    components: [{
      kind: 'OB.UI.Thumbnail',
      name: 'thumbnail'
    }]
  }, {
    style: 'float: left; width: 55%;',
    components: [{
      name: 'identifier',
      style: 'padding-left: 5px;'
    }]
  }, {
    name: 'price',
    style: 'float: left; width: 20%; text-align: right; font-weight:bold;'
  }, {
    style: 'clear:both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    this.$.thumbnail.setImg(this.model.get('img'));
  }
});