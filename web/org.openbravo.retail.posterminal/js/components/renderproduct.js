/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */
enyo.kind({
  name: 'OB.UI.RenderProduct',
  kind: 'OB.UI.listItemButton',
  resizeHandler: function () {
    if (!this.model) {
      return true;
    }
    if (!this.debounceRedraw) {
      this.debounceRedraw = _.debounce(this.drawPriceBasedOnSize, 500);
    }
    this.inherited(arguments);
    this.debounceRedraw();
    return true;
  },
  classes: 'productLine standardFlexContainer flexColumn',
  components: [{
    classes: 'standardFlexContainer flexAllWidth',
    components: [{
      name: 'productImgContainer',
      classes: 'productImgContainer',
      components: [{
        name: 'productImage',
        style: 'max-width: 100%;',
        classes: 'standardFlexContainer',
        components: [{
          style: 'vertical-align: top; width: 50px; ',
          components: [{
            tag: 'div',
            classes: 'flex-image-wrap',
            contentType: 'image/png',
            style: 'width: 49px; height: 49px;',
            components: [{
              tag: 'img',
              name: 'icon',
              style: 'margin: auto; height: 100%; width: 100%; background-size: contain; background-repeat:no-repeat; background-position:center;'
            }]
          }]
        }]
      }, {
        kind: 'OB.UI.Thumbnail',
        name: 'thumbnail',
        cssClass: 'flex-image-wrap'
      }]
    }, {
      classes: 'flexAllWidth',
      components: [{
        classes: 'standardFlexContainer',
        components: [{
          name: 'identifierContainer',
          classes: 'standardFlexContainer flexColumn',
          components: [{
            name: 'identifier',
            classes: 'productIdentifier'
          }, {
            style: 'color: #888888',
            name: 'filterAttr',
            allowHtml: true
          }]
        }, {
          classes: 'standardFlexContainer flexColumn',
          style: 'width: 38px;',
          components: [{
            kind: 'OB.UI.ProductContextMenu',
            name: 'btnProductContextMenu'
          }]
        }]
      }, {
        classes: 'standardFlexContainer flexwrap flexend',
        style: 'padding-right: 6px',
        components: [{
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
              style: 'text-align: right; font-weight: bold; color: grey;'
            }]
          }]
        }]
      }]
    }]
  }, {
    name: 'generic',
    style: 'text-align: right; font-style: italic; color: grey; font-weight: bold; padding-right: 6px;',
    showing: false
  }, {
    style: 'color: #888888; text-align: left; font-style: italic; color: grey; font-size: 13px; padding-top: 10px; padding-right: 6px;',
    name: 'bottonLine'
  }],
  drawPriceBasedOnSize: function () {
    var shouldResizeWork = ((enyo.Panels.isScreenNarrow() && document.body.clientWidth <= 466) || (!enyo.Panels.isScreenNarrow() && document.body.clientWidth <= 925));
    var hideProductImages = (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true) || OB.MobileApp.model.hasPermission('OBPOS_HideProductImagesInSearchAndBrowse', true));
    var searchTab = false;

    function getFontSize(price) {
      var fontSize = '16px;';
      if (price.length === 9) {
        fontSize = '15px;';
        if (enyo.Panels.isScreenNarrow() && document.body.clientWidth <= 410) {
          fontSize = '14px;';
        }
      } else if (price.length === 10) {
        fontSize = '14px;';
        if (enyo.Panels.isScreenNarrow() && document.body.clientWidth <= 410) {
          fontSize = '13px;';
        }
      } else if (price.length === 11) {
        fontSize = '13px;';
        if (enyo.Panels.isScreenNarrow() && document.body.clientWidth <= 410) {
          fontSize = '12px;';
        }
      } else if (price.length > 11) {
        fontSize = '12px;';
        if (!enyo.Panels.isScreenNarrow()) {
          if (document.body.clientWidth >= 840 && document.body.clientWidth <= 925) {
            fontSize = '13px;';
          } else if (document.body.clientWidth < 840) {
            fontSize = '11px;';
          }
        } else {
          if (document.body.clientWidth <= 425 && document.body.clientWidth > 400) {
            fontSize = '11px;';
          } else if (document.body.clientWidth <= 400) {
            fontSize = '10px;';
          }
        }
      }
      return fontSize;
    }
    if (_.isUndefined(this.$.price) || _.isUndefined(this.$.priceList) || _.isUndefined(this.model)) {
      //Probably this event was raised during destroy and we want to ignore it.
      return true;
    }
    if (this.id.indexOf('searchCharacteristic') !== -1) {
      searchTab = true;
    }
    if (this.model.get('currentStandardPrice') && this.model.get('currentStandardPrice') !== "undefined") {
      this.$.priceList.addStyles('font-size: 16px;');
      if (OB.MobileApp.model.hasPermission('ShowStandardPriceOnSearchAndBrowse', true)) {
        if (OB.I18N.formatCurrency(this.model.get('currentStandardPrice')).length > 11 && !searchTab && !hideProductImages && shouldResizeWork) {
          this.$.price.addStyles('font-size: ' + getFontSize(OB.I18N.formatCurrency(this.model.get('currentStandardPrice'))));
        }
        this.$.priceList.setContent(OB.I18N.formatCurrency(this.model.get('currentStandardPrice')));
      }
      if (this.model.get('standardPrice')) {
        this.$.price.addStyles('font-size: 16px;');
        if (!searchTab && !hideProductImages && shouldResizeWork) {
          this.$.price.addStyles('font-size: ' + getFontSize(OB.I18N.formatCurrency(this.model.get('standardPrice'))));
        }
      }
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    } else {
      if (this.model.get('standardPrice')) {
        this.$.price.addStyles('font-size: 16px;');
        if (!searchTab && !hideProductImages && shouldResizeWork) {
          this.$.price.addStyles('font-size: ' + getFontSize(OB.I18N.formatCurrency(this.model.get('standardPrice'))));
        }
      }
      this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    }
    // Context menu
    if (this.model.get('productType') !== 'I' || this.$.btnProductContextMenu.$.menu.itemsCount === 0) {
      this.$.btnProductContextMenu.hide();
      this.$.identifierContainer.setStyle("");
    } else {
      this.$.btnProductContextMenu.setModel(this.model);
      this.$.identifierContainer.setStyle("width: calc(100% - 38px)");
      if (this.model.get('showchdesc') && !this.model.get('characteristicDescription')) {
        this.setStyle('padding: 8px 10px 0px 10px');
      }
    }
  },
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
    this.$.identifier.setContent(this.setIdentifierContent());
    this.$.filterAttr.setContent(filterTxt);
    if (this.model.get('showchdesc')) {
      this.$.bottonLine.setContent(this.model.get('characteristicDescription'));
    }
    this.drawPriceBasedOnSize(searchTab);

    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      if (this.model.get('imgId')) {
        this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.model.get('id')) + '_min), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
      } else {
        this.$.icon.applyStyle('background-image', 'url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
      }
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
    if (this.model.get('bestseller') !== true) {
      this.$.icons.applyStyle('min-width', this.$.icons.minWidth + 'px');
      this.$.icons.applyStyle('width', '0px');
      this.$.bestseller.$.image.hide();
    } else {
      this.$.icons.minWidth += 20;
      this.$.icons.applyStyle('min-width', this.$.icons.minWidth + 'px');
      this.$.bestseller.addStyles('display: block');
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true) || OB.MobileApp.model.hasPermission('OBPOS_HideProductImagesInSearchAndBrowse', true)) {
      this.$.productImgContainer.hide();
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

enyo.kind({
  kind: 'OB.UI.ListContextMenu',
  name: 'OB.UI.ProductContextMenu',
  initComponents: function () {
    this.inherited(arguments);
    this.$.menu.setItems(OB.MobileApp.model.get('productContextMenuOptions'));
  }
});