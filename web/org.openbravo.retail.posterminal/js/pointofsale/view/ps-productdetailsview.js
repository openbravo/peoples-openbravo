/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockThisStore',
  kind: 'OB.UI.SmallButton',
  events: {
    onOpenLocalStockModal: '',
    onOpenLocalStockClickableModal: ''
  },
  classes: 'btnlink-green',
  style: 'min-width: 200px; margin: 2px 5px 2px 5px;',
  tap: function () {
    if (!OB.MobileApp.model.get('permissions').OBPOS_warehouseselectionforline || !this.model.get('order').get('isEditable')) {
      this.doOpenLocalStockModal();
    } else {
      this.doOpenLocalStockClickableModal();
    }
  },
  init: function (model) {
    this.model = model;
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
    var me = this;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'),
          leftSubWindow = this.parent.leftSubWindow;
      leftSubWindow.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
      serverCallStoreDetailedStock.exec({
        organization: OB.MobileApp.model.get('terminal').organization,
        product: leftSubWindow.product.get('id')
      }, function (data) {
        if (data && data.exception) {
          leftSubWindow.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
          leftSubWindow.bodyComponent.$.stockOthers.addClass('error');
        } else if (data.product === leftSubWindow.product.get('id') && leftSubWindow.showing && (data.qty || data.qty === 0)) {
          data.product = leftSubWindow.product;
          leftSubWindow.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(data);
          me.doOpenOtherStoresStockModal();
          leftSubWindow.bodyComponent.$.stockOthers.removeClass('error');
          leftSubWindow.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty);
        }
      });
    } else {
      this.doOpenOtherStoresStockModal();
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonAddToTicket',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-green',
  style: 'min-width: 70px; margin: 2px 5px 2px 5px;',
  i18nLabel: 'OBPOS_addToTicket',
  events: {
    onAddProduct: '',
    onSetLineProperty: '',
    onCloseLeftSubWindow: ''
  },
  setLabel: function () {
    if (this.leftSubWindow && this.leftSubWindow.line) {
      this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_addToTicket'));
    }
  },
  tap: function () {
    var product = this.leftSubWindow.product,
        me = this;

    function addLine(line, product, attrs, args) {
      if (line) {
        if (attrs.warehouse.id !== line.get('warehouse').id) {
          me.doSetLineProperty({
            line: line,
            property: 'warehouse',
            value: attrs.warehouse
          });
        }
        me.doCloseLeftSubWindow();
      } else {
        me.doAddProduct({
          attrs: attrs,
          options: {
            line: line,
            blockAddProduct: true
          },
          product: product,
          qty: args.qty ? args.qty : OB.DEC.One,
          ignoreStockTab: true
        });
      }
    }

    if (product) {
      var line = null,
          lines = OB.MobileApp.model.receipt.get('lines'),
          allLinesQty = 0;
      if (me.leftSubWindow && me.leftSubWindow.line) {
        line = me.leftSubWindow.line;
      }
      var attrs = (me.leftSubWindow.inEvent && me.leftSubWindow.inEvent.attrs) ? me.leftSubWindow.inEvent.attrs : {};
      attrs.warehouse = {
        id: me.leftSubWindow.warehouse.warehouseid ? this.leftSubWindow.warehouse.warehouseid : this.leftSubWindow.warehouse.id,
        warehousename: me.leftSubWindow.warehouse.warehousename,
        warehouseqty: me.leftSubWindow.warehouse.warehouseqty
      };
      OB.UTIL.HookManager.executeHooks('OBPOS_PreTapStockAddReceipt', {
        context: me,
        params: attrs,
        line: line
      }, function (args) {
        if (args && args.cancelOperation && args.cancelOperation === true) {
          return;
        }
        _.forEach(lines.models, function (li) {
          if ((li.get('product').get('id') === product.get('id') && li.get('warehouse').id === attrs.warehouse.id) || (line && li.get('id') === line.get('id'))) {
            allLinesQty += li.get('qty');
          }
        });
        if (!line) {
          allLinesQty += args.qty ? args.qty : OB.DEC.One;
        } else {
          allLinesQty += args.qty ? args.qty : OB.DEC.Zero;
        }
        if ((product.get('isdiscontinued') || product.get('issalediscontinued'))) {
          if (OB.MobileApp.model.hasPermission('OBPOS_AvoidProductDiscontinuedStockCheck', true)) {
            if (allLinesQty > attrs.warehouse.warehouseqty) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_ErrorProductDiscontinued', [product.get('_identifier'), allLinesQty, attrs.warehouse.warehouseqty, attrs.warehouse.warehousename]));
            } else {
              addLine(line, product, attrs, args);
            }
          } else {
            if (line && allLinesQty > attrs.warehouse.warehouseqty) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_ErrorProductDiscontinued', [product.get('_identifier'), allLinesQty, attrs.warehouse.warehouseqty, attrs.warehouse.warehousename]));
            } else {
              addLine(line, product, attrs, args);
            }
          }
        } else {
          addLine(line, product, attrs, args);
        }
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose',
  style: 'float: right; cursor: pointer; font-size: 150%; font-weight: bold; color: #CCCCCC; width: 40px; height: 40px; margin: -10px; text-align: right; padding: 8px;',
  init: function () {
    this.setContent(OB.I18N.getLabel('OBMOBC_Character')[2]);
  },
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
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose',
    name: 'buttonClose'
  }]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_body',
  handlers: {
    onOpenLocalStockModal: 'openLocalStockModal',
    onOpenLocalStockClickableModal: 'openLocalStockClickableModal',
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
  openLocalStockClickableModal: function () {
    if (this.leftSubWindow.localStockModel) {
      this.doShowPopup({
        popup: 'modalLocalStockClickable',
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
    name: 'contextImage',
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
          name: 'productAddToReceipt',
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
  handlers: {
    onModifyWarehouse: 'changeWarehouseInfo'
  },
  changeWarehouseInfo: function (inSender, inEvent) {
    var me = this;
    OB.UTIL.HookManager.executeHooks('OBPOS_BeforeWarehouseChange', {
      oldWarehouse: me.warehouse,
      newWarehouse: inEvent,
      currentLine: me.line
    }, function (args) {
      if (args && args.cancelOperation) {
        return;
      }
      me.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [inEvent.warehousename, inEvent.warehouseqty]));
      me.warehouse = inEvent;
    });
  },
  loadDefaultWarehouseData: function (defaultWarehouse) {
    if (defaultWarehouse) {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [defaultWarehouse.get('warehousename'), defaultWarehouse.get('warehouseqty')]));
    } else {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [OB.MobileApp.model.get('warehouses')[0].warehousename, '0']));
    }
  },
  getStoreStock: function (params) {
    var me = this;
    this.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
    this.bodyComponent.$.productAddToReceipt.setDisabled(true);
    OB.UTIL.StockUtils.getReceiptLineStock(me.product.get('id'), undefined, function (data) {
      if (data && data.exception) {
        me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
        me.bodyComponent.$.stockHere.addClass('error');
      } else if (data.product === me.product.get('id')) {
        if (data.qty || data.qty === 0) {
          data.product = me.product;
          me.localStockModel = new OB.OBPOSPointOfSale.UsedModels.LocalStock(data);
          if (me.localStockModel.get('warehouses').at(0)) {
            if (me.warehouse.warehouseid) {
              me.loadDefaultWarehouseData(me.localStockModel.getWarehouseById(me.warehouse.warehouseid));
            } else {
              me.loadDefaultWarehouseData(me.localStockModel.getWarehouseById(me.warehouse.id));
            }
          }
          me.bodyComponent.$.stockHere.removeClass('error');
          me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_storeStock') + data.qty);
        }
        me.bodyComponent.$.productAddToReceipt.setDisabled(false);
      }
      if (params.checkStockCallback) {
        params.checkStockCallback();
      }
    });
  },
  getOtherStock: function () {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'),
        me = this;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      this.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_otherStoresStock_NotCalculated'));
    } else {
      this.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
      serverCallStoreDetailedStock.exec({
        organization: OB.MobileApp.model.get('terminal').organization,
        product: this.product.get('id')
      }, function (data) {
        if (data && data.exception) {
          me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
          me.bodyComponent.$.stockOthers.addClass('error');
        } else if (data.product === me.product.get('id') && (data.qty || data.qty === 0)) {
          data.product = me.product;
          me.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(data);
          me.bodyComponent.$.stockOthers.removeClass('error');
          me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty);
        }
      });
    }
  },
  beforeSetShowing: function (params) {
    if (!params.product || OB.MobileApp.model.get('warehouses').length === 0) {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCrossStore'
      });
      return false;
    }
    this.line = params.line || null;
    this.product = params.product;
    this.localStockModel = null;
    this.otherStoresStockModel = null;
    if (params.warehouse) {
      this.warehouse = params.warehouse;
      if (this.warehouse && this.warehouse.id) {
        this.warehouse.warehouseid = this.warehouse.id;
      }
    } else {
      this.warehouse = OB.MobileApp.model.get('warehouses')[0];
    }
    this.headerComponent.$.productName.setContent(params.product.get('_identifier') + ' (' + params.product.get('uOMsymbol') + ')');
    if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true)) {
      this.bodyComponent.$.contextImage.hide();
    } else {
      this.bodyComponent.$.contextImage.show();
    }
    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      this.bodyComponent.$.productImage.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(params.product.get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
    } else {
      this.bodyComponent.$.productImage.applyStyle('background-image', 'url(data:image/png;base64,' + params.product.get('img') + ')');
    }
    this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_loadingFromWarehouse', [this.warehouse.warehousename]));
    this.bodyComponent.$.productPrice.setContent(OB.I18N.getLabel('OBPOS_priceInfo') + '<b>' + OB.I18N.formatCurrency(params.product.get('standardPrice')) + '</b>');
    this.bodyComponent.$.descriptionArea.setContent(params.product.get('description'));
    this.bodyComponent.$.productAddToReceipt.setLabel();
    this.getOtherStock();
    this.getStoreStock(params);
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