/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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
    name: 'productImage',
    style: 'max-width: 100%;',
    classes: 'standardFlexContainer',
    components: [{
      style: 'vertical-align: top;  width: 50px; ',
      components: [{
        tag: 'div',
        classes: 'image-wrap',
        contentType: 'image/png',
        style: 'width: 49px; height: 49px;',
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
      name: 'productInfo',
      style: 'width: 100%; flex-wrap: wrap; padding-left: 5px;',
      classes: 'standardFlexContainer',
      components: [{
        style: 'vertical-align: top; width: 100%; word-break: break-word;',
        name: 'identifierContainer',
        components: [{
          name: 'identifier',
          style: 'max-height: 70px; overflow: hidden;'
        }, {
          style: 'color: #888888',
          name: 'filterAttr',
          allowHtml: true
        }]
      }, {
        name: 'icons',
        minWidth: 0,
        style: 'vertical-align: top;',
        components: [{
          name: 'bestseller',
          style: 'height: 16px; width: 16px; padding: 0px 2px; float: left;',
          kind: 'OB.UI.Thumbnail.Bestseller',
          'default': 'img/iconBestsellerSmall.png',
          showing: false
        }]
      }, {
        name: 'priceBox',
        style: 'vertical-align: top;',
        components: [{
          components: [{
            name: 'price',
            style: 'text-align: right; font-weight: bold;'
          }, {
            name: 'priceList',
            style: 'text-align: right; font-weight: bold; color: grey; font-size: 14px;'
          }]
        }]
      }]
    }]
  }, {
    style: 'clear:both;'
  }, {
    name: 'generic',
    style: 'float: right; width: 30%; text-align: right; font-style: italic; color: grey; font-weight: bold;',
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
        searchTab = false,
        filterAttr = this.model.get("filterAttr"),
        maxWidthCalc;
    if (filterAttr && _.isArray(filterAttr) && filterAttr.length > 0) {
      filterAttr.forEach(function (attr) {
        if (filterTxt !== '') {
          filterTxt = filterTxt + attr.separator;
        }
        filterTxt = filterTxt + attr.value;
      });
    }
    if (this.id.indexOf('searchCharacteristic') !== -1) {
      searchTab = true;
    }
    this.$.identifierContainer.addStyles('width: 38%;');
    this.$.identifier.setContent(this.setIdentifierContent());
    this.$.filterAttr.setContent(filterTxt);
    if (this.model.get('showchdesc')) {
      this.$.bottonLine.setContent(this.model.get('characteristicDescription'));
    }

    if (this.model.get('currentStandardPrice') && this.model.get('currentStandardPrice') !== "undefined") {
      if (OB.MobileApp.model.hasPermission('ShowStandardPriceOnSearchAndBrowse', true)) {
        if (OB.I18N.formatCurrency(this.model.get('currentStandardPrice')).length > 11 && !searchTab) {
          this.$.priceList.addStyles('font-size: 11px;');
        }
        this.$.priceList.setContent(OB.I18N.formatCurrency(this.model.get('currentStandardPrice')));
      }
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    } else {
      if (this.model.get('standardPrice')) {
        if (OB.I18N.formatCurrency(this.model.get('standardPrice')).length > 11 && !searchTab) {
          this.$.price.addStyles('font-size: 11px;');
        }
      }
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    }

    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.model.get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
      this.$.thumbnail.hide();
    } else {
      this.$.thumbnail.setImg(this.model.get('img'));
      this.$.icon.parent.hide();
    }

    if (this.owner.owner.owner.owner.owner.name === 'browseProducts') {
      if (enyo.Panels.isScreenNarrow()) {
        maxWidthCalc = parseInt(document.body.clientWidth / 2, 10) - 213;
      } else {
        maxWidthCalc = parseInt(document.body.clientWidth / 4, 10) - 213;
      }
    } else {
      if (enyo.Panels.isScreenNarrow()) {
        maxWidthCalc = parseInt(document.body.clientWidth / 2, 10) - 213;
      } else {
        maxWidthCalc = parseInt(document.body.clientWidth, 10) - 363;
      }
    }
    if (maxWidthCalc < 0) {
      maxWidthCalc = 0;
    }
    maxWidthCalc = Math.floor(maxWidthCalc / 20) * 20 >= 20 ? Math.floor(maxWidthCalc / 20) * 20 : 20;
    this.$.icons.addStyles('max-width: ' + maxWidthCalc + 'px');
    //this.$.icons.addStyles('max-width: 16px');
    if (this.model.get('bestseller') !== true) {
      this.$.icons.applyStyle('min-width', this.$.icons.minWidth + 'px');
      this.$.icons.applyStyle('width', '0px');
      this.$.priceBox.applyStyle('margin-left', 'auto');
      this.$.bestseller.$.image.hide();
    } else {
      this.$.icons.applyStyle('margin-left', 'auto');
      this.$.icons.minWidth += 20;
      this.$.icons.applyStyle('min-width', this.$.icons.minWidth + 'px');
      this.$.bestseller.addStyles('display: block');
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true) || OB.MobileApp.model.hasPermission('OBPOS_HideProductImagesInSearchAndBrowse', true)) {
      this.$.thumbnail.hide();
      this.$.icon.parent.hide();
      this.$.identifierContainer.addStyles('width: 70%;');
    }

    if (this.model.get('isGeneric')) {
      this.$.generic.setContent(OB.I18N.getLabel('OBMOBC_LblGeneric'));
      this.$.generic.show();
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderProduct', {
      model: this
    });
  },
  setIdentifierContent: function () {
    return this.model.get('_identifier');
  }
});

enyo.kind({
  name: 'OB.UI.Thumbnail.Bestseller',
  kind: 'OB.UI.Thumbnail',
  drawImage: function () {
    this.inherited(arguments);
    this.$.image.applyStyle('background-position', '0px 0px');
    this.$.image.applyStyle('background-color', 'transparent');
    this.$.image.applyStyle('background-size', '14px 16px !important');
  },
  initComponents: function () {
    this.inherited(arguments);
    this.removeClass('image-wrap');
  }
});