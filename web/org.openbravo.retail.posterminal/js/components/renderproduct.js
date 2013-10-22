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
      tag: 'div',
      classes: 'image-wrap',
      contentType: 'image/png',
      style: 'width: 49px; height: 49px',
      components: [{
        tag: 'img',
        name: 'icon',
        style: 'margin: auto; height: 100%; width: 100%; background-size: contain; background-repeat:no-repeat; background-position:center;'
      }]
    }, {
      kind: 'OB.UI.Thumbnail',
      name: 'thumbnail'
    }]
  }, {
    style: 'float: left; width: 45%; ',
    components: [{
      name: 'topLine'
    }, {
      style: 'color: #888888',
      name: 'bottonLine'
    }]
  }, {
    name: 'price',
    style: 'float: right; width: 20%; text-align: right; font-weight:bold;'
  }, {
    style: 'clear:both;'
  }, {
    name: 'generic',
    style: 'float: right; width: 20%; text-align: right; font-style: italic; color: grey; padding: 15px'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.topLine.setContent(this.model.get('_identifier'));
    if (this.model.get('showchdesc')) {
      this.$.bottonLine.setContent(this.model.get('characteristicDescription'));
    }
    this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));

    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.model.get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
      this.$.thumbnail.hide();
    } else {
      this.$.thumbnail.setImg(this.model.get('img'));
      this.$.icon.parent.hide();
    }

    if (this.model.get('isGeneric')) {
      this.$.generic.setContent(OB.I18N.getLabel('OBMOBC_LblGeneric'));
    }
  }
});