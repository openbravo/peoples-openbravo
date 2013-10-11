/*global OB, $, Backbone, enyo */



enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockThisStore',
  kind: 'OB.UI.SmallButton',
  events: {
    onOpenLocalStockModal: ''
  },
  classes: 'btnlink-green',
  style: 'min-width: 200px; margin: 2px 5px 2px 5px;',
  tap: function () {
    this.doOpenLocalStockModal();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockOtherStore',
  kind: 'OB.UI.SmallButton',
  events: {
    onOpenOtherStoresStockModal: ''
  },
  classes: 'btnlink-green',
  style: 'min-width: 200px; margin: 2px 5px 2px 5px;',
  tap: function () {
    this.doOpenOtherStoresStockModal();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonAddToTicket',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-green',
  style: 'min-width: 70px; margin: 2px 5px 2px 5px;',
  i18nLabel: 'OBPOS_addToTicket',
  events: {
    onAddProduct: ''
  },
  tap: function () {
    if (this.leftSubWindow.product) {
      this.doAddProduct({
        product: this.leftSubWindow.product,
        ignoreStockTab: true
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose',
  style: 'float: right; cursor: pointer; font-size: 150%; font-weight: bold; color: #CCCCCC; width: 40px; height: 40px; margin: -10px; text-align: right; padding: 8px;',
  content: '×',
  tap: function () {
    this.leftSubWindow.doCloseLeftSubWindow();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_header',
  style: 'font-size: 22px; height: 25px; padding: 15px 15px 5px 15px;',
  components: [{
    name: 'productName',
    style: 'float: left;'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose'
  }]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_body',
  handlers: {
    onOpenLocalStockModal: 'openLocalStockModal',
    onOpenOtherStoresStockModal: 'openOtherStoresStockModal'
  },
  events: {
    onShowPopup: ''
  },
  openLocalStockModal: function () {
    if (this.leftSubWindow.localStockModel) {
      this.doShowPopup({
        popup: 'modalLocalStock',
        args: {
          stockInfo: this.leftSubWindow.localStockModel
        }
      });
    }
    return true;
  },
  openOtherStoresStockModal: function () {
    if (this.leftSubWindow.otherStoresStockModel) {
      this.doShowPopup({
        popup: 'modalStockInOtherStores',
        args: {
          stockInfo: this.leftSubWindow.otherStoresStockModel
        }
      });
    }
    return true;
  },
  components: [{
    style: 'height: 160px; padding: 20px;',
    components: [{
      name: 'productImage',
      baseStyle: 'background:#ffffff url(data:image/png;base64,xxImgBinaryDataxx) center center no-repeat;background-size:contain;margin: auto; height: 100%; width: 100%; background-size: contain;',
      style: 'background-color:#ffffff; background-repeat: no-repeat; background-position: center center; background-size: contain; margin: auto; height: 100%; width: 100%;'
    }]
  }, {
    style: 'margin: 5px 15px;',
    components: [{
      name: 'warehouseToGet',
      allowHtml: true,
      style: 'line-height: 20px; font-size: 20px; padding: 10px; color: black;'
    }]
  }, {
    style: 'height: 80px;  padding: 10px;',
    components: [{
      style: 'float: left; width: 50%;',
      components: [{
        style: 'padding: 0px 0px 15px 0px;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockThisStore',
          name: 'stockHere'
        }]
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockOtherStore',
        name: 'stockOthers'
      }]
    }, {
      style: 'float: right;',
      components: [{
        name: 'productPrice',
        allowHtml: true,
        style: 'margin: 5px 0px 12px 0px; text-align: center; font-size: 18px; font-weight: 600;'
      }, {
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonAddToTicket'
        }]
      }]
    }]
  }, {
    kind: 'Scroller',
    maxHeight: '191px',
    style: 'padding: 15px;',
    components: [{
      name: 'descriptionArea'
    }]
  }]
});

enyo.kind({
  kind: 'OB.UI.LeftSubWindow',
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView',
  events: {
    onShowPopup: ''
  },
  loadDefaultWarehouseData: function (defaultWarehouse) {
    if (defaultWarehouse) {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [defaultWarehouse.get('warehousename'), defaultWarehouse.get('warehouseqty')]));
    } else {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [OB.POS.modelterminal.get('warehouses')[0].warehousename, '0']));
    }
  },
  getStoreStock: function () {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.StoreDetailedStock'),
        me = this;
    this.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
    serverCallStoreDetailedStock.exec({
      organization: OB.POS.modelterminal.get('terminal').organization,
      product: this.product.get('id')
    }, function (data) {
      if (data && data.exception) {
        me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
        me.bodyComponent.$.stockHere.addClass("error");
      } else if (data.product === me.product.get('id') && me.showing) {
        if (data.qty || data.qty === 0) {
          data.product = me.product;
          me.localStockModel = new OB.OBPOSPointOfSale.UsedModels.LocalStock(data);
          if (me.localStockModel.get('warehouses').at(0)) {
            me.loadDefaultWarehouseData(me.localStockModel.getWarehouseById(OB.POS.modelterminal.get('warehouses')[0].warehouseid));
          }
          me.bodyComponent.$.stockHere.removeClass("error");
          me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_storeStock') + data.qty);
        }
      }
    });
  },
  getOtherStock: function () {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'),
        me = this;
    this.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
    serverCallStoreDetailedStock.exec({
      organization: OB.POS.modelterminal.get('terminal').organization,
      product: this.product.get('id')
    }, function (data) {
      if (data && data.exception) {
        me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
        me.bodyComponent.$.stockOthers.addClass("error");
      } else if (data.product === me.product.get('id') && me.showing) {
        if (data.qty || data.qty === 0) {
          data.product = me.product;
          me.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(data);
          me.bodyComponent.$.stockOthers.removeClass("error");
          me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty);
        }
      }
    });
  },
  beforeSetShowing: function (params) {
    if (!params.product || OB.POS.modelterminal.get('warehouses').length === 0) {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCrossStore'
      });
      return false;
    }
    this.product = params.product;
    this.localStockModel = null;
    this.otherStoresStockModel = null;
    this.headerComponent.$.productName.setContent(params.product.get('_identifier') + ' (' + params.product.get('uOMsymbol') + ')');
    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      this.bodyComponent.$.productImage.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(params.product.get('id')) + '), url(' + "/openbravo/web/org.openbravo.mobile.core/assets/img/box.png" + ')');
    } else {
      this.bodyComponent.$.productImage.applyStyle('background-image', 'url(data:image/png;base64,' + params.product.get('img') + ')');
    }
    this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_loadingFromWarehouse', [OB.POS.modelterminal.get('warehouses')[0].warehousename]));
    this.bodyComponent.$.productPrice.setContent(OB.I18N.getLabel('OBPOS_priceInfo') + '<b>' + OB.I18N.formatCurrency(params.product.get('standardPrice')) + '</b>');
    this.bodyComponent.$.descriptionArea.setContent(params.product.get('description'));
    this.getOtherStock();
    this.getStoreStock();
    return true;
  },
  header: {
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_header',
    name: 'header'
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_body',
    name: 'body'
  }
});