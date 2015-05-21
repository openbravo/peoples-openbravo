/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  name: 'OB.UI.RenderProduct',
  kind: 'OB.UI.listItemButton',
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
    style: 'float: left;',
    name: 'identifierContainer',
    components: [{
      name: 'identifier'
    }, {
      style: 'color: #888888',
      name: 'filterAttr',
      allowHtml: true
    }]
  }, {
    style: 'float: right; width: 27%;',
    components: [{
      style: 'width: 100%; text-align: right; font-weight:bold;',
      name: 'price'
    }, {
      name: 'priceList',
      style: 'width: 100%; text-align: right; font-weight:bold; color: grey; font-size: 14px;',
    }]
  }, {
    style: 'clear:both;'
  }, {
    name: 'generic',
    style: 'float: right; width: 20%; text-align: right; font-style: italic; color: grey; padding: 15px; font-weight: bold;',
    showing: false
  }, {
    style: 'clear:both;'
  }, {
    style: 'color: #888888; float: left; width: 100%; text-align: left; font-style: italic; color: grey; font-size: 13px; padding-top: 10px;',
    name: 'bottonLine'
  }],
  initComponents: function () {
    this.inherited(arguments);
    // Build filter info from filter attributes
    var filterTxt = '',
        filterAttr = this.model.get("filterAttr");
    if (filterAttr && _.isArray(filterAttr) && filterAttr.length > 0) {
      filterAttr.forEach(function (attr) {
        if (filterTxt !== '') {
          filterTxt = filterTxt + attr.separator;
        }
        filterTxt = filterTxt + attr.value;
      });
    }
    this.$.identifierContainer.addStyles('width: 38%;');
    this.$.identifier.setContent(this.setIdentifierContent());
    this.$.filterAttr.setContent(filterTxt);
    if (this.model.get('showchdesc')) {
      this.$.bottonLine.setContent(this.model.get('characteristicDescription'));
    }
    if (this.model.get('currentStandardPrice') && this.model.get('currentStandardPrice') !== "undefined") {
      if (OB.MobileApp.model.hasPermission('ShowStandardPriceOnSearchAndBrowse', true)) {
        this.$.priceList.setContent(OB.I18N.formatCurrency(this.model.get('currentStandardPrice')));
      }
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    } else {
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    }

    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.model.get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
      this.$.thumbnail.hide();
    } else {
      this.$.thumbnail.setImg(this.model.get('img'));
      this.$.icon.parent.hide();
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImagesInSearchAndBrowse', true)) {
      this.$.thumbnail.hide();
      this.$.icon.parent.hide();
      this.$.identifierContainer.addStyles('width: 70%;');
    }

    if (this.model.get('isGeneric')) {
      this.$.generic.setContent(OB.I18N.getLabel('OBMOBC_LblGeneric'));
      this.$.generic.show();
    }
  },
  setIdentifierContent: function () {
    return this.model.get('_identifier');
  }
});