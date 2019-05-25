/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

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
    var me = this,
        leftSubWindow = me.parent.leftSubWindow;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) && !OB.UTIL.isCrossStoreProduct(leftSubWindow.product)) {
      var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock');
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
    if (product) {
      var line = null;
      if (me.leftSubWindow && me.leftSubWindow.line) {
        line = me.leftSubWindow.line;
      }
      var attrs = (me.leftSubWindow.inEvent && me.leftSubWindow.inEvent.attrs) ? me.leftSubWindow.inEvent.attrs : {};
      attrs.organization = me.leftSubWindow.organization;
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
        if (line) {
          if (attrs.warehouse.id !== line.get('warehouse').id) {
            me.doSetLineProperty({
              line: line,
              property: 'warehouse',
              value: attrs.warehouse
            });
          }
          if (attrs.organization.id !== line.get('organization').id) {
            me.doSetLineProperty({
              line: line,
              property: 'organization',
              value: attrs.organization
            });
          }
          if (OB.UTIL.isCrossStoreEnabled()) {
            if (me.leftSubWindow.organization && me.leftSubWindow.organization.id !== OB.MobileApp.model.get('terminal').organization) {
              product.set('crossStore', true);
            } else {
              product.set('crossStore', false);
            }
            line.set({
              priceList: product.get('currentPrice').price,
              price: product.get('currentPrice').price
            }, {
              silent: true
            });
            OB.MobileApp.model.receipt.calculateReceipt();
          }
          me.doCloseLeftSubWindow();
        } else {
          me.doAddProduct({
            attrs: attrs,
            options: {
              line: line,
              blockAddProduct: true,
              stockScreen: true
            },
            product: product,
            qty: args.qty ? args.qty : OB.DEC.One,
            ignoreStockTab: true
          });
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
    if (OB.UTIL.isCrossStoreEnabled()) {
      var me = this;
      var selectedStoreCallBack = function (data) {
          var warehouse = data.warehouse ? data.warehouse : {
            warehouseid: data.warehouseid,
            warehousename: data.warehousename,
            warehouseqty: data.stock
          },
              organization = data.organization ? data.organization : {
              id: data.orgId,
              name: data.orgName
              };
          me.$.stockHere.removeClass('error');
          me.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_storeStock_NotCalculated'));
          me.$.productPrice.setContent(OB.I18N.getLabel('OBPOS_priceInfo') + '<b>' + OB.I18N.formatCurrency(data.currentPrice.price) + '</b>');
          me.$.productAddToReceipt.setLabel();
          me.$.productAddToReceipt.setDisabled(false);
          me.leftSubWindow.organization = organization;
          me.leftSubWindow.changeWarehouseInfo(null, warehouse);
          me.leftSubWindow.product.set('listPrice', data.currentPrice.price);
          me.leftSubWindow.product.set('standardPrice', data.currentPrice.price);
          if (data.productPrices) {
            me.leftSubWindow.product.set('productPrices', data.productPrices);
          } else {
            me.leftSubWindow.product.set('currentPrice', data.currentPrice);
          }
          };
      if (event && event.target.getAttribute('id') !== 'terminal_containerWindow_pointOfSale_multiColumn_leftPanel_productdetailsview_leftSubWindowBody_body_stockOthers' && (this.leftSubWindow.line || !OB.UTIL.isCrossStoreProduct(this.leftSubWindow.product))) {
        var data = null;
        if (this.leftSubWindow.line) {
          data = {
            stock: this.leftSubWindow.line.get('warehouse').warehouseqty,
            warehouse: this.leftSubWindow.line.get('warehouse'),
            organization: this.leftSubWindow.line.get('organization')
          };
          if (OB.UTIL.isCrossStoreProduct(this.leftSubWindow.line.get('product'))) {
            data.currentPrice = this.leftSubWindow.line.get('product').get('currentPrice');
          } else {
            data.currentPrice = {
              priceListId: OB.MobileApp.model.get('terminal').priceList,
              price: this.leftSubWindow.line.get('product').get('standardPrice')
            };
          }
          selectedStoreCallBack(data);
        } else {
          data = {
            warehouse: this.leftSubWindow.warehouse,
            currentPrice: {
              priceListId: OB.MobileApp.model.get('terminal').priceList,
              price: this.leftSubWindow.product.get('standardPrice')
            },
            organization: {
              id: OB.MobileApp.model.get('terminal').organization,
              name: OB.I18N.getLabel('OBPOS_LblThisStore', [OB.MobileApp.model.get('terminal').organization$_identifier])
            }
          };
          selectedStoreCallBack(data);
        }
      } else {
        this.doShowPopup({
          popup: 'OBPOS_modalCrossStoreSelector',
          args: {
            productId: this.leftSubWindow.product.get('id'),
            productUOM: this.leftSubWindow.product.get('uOMsymbol'),
            callback: selectedStoreCallBack
          }
        });
      }
    } else {
      if (this.leftSubWindow.otherStoresStockModel) {
        this.doShowPopup({
          popup: 'modalStockInOtherStores',
          args: {
            stockInfo: this.leftSubWindow.otherStoresStockModel
          }
        });
      }
    }
    return true;
  },
  components: [{
    style: 'height: 160px; padding: 20px;',
    name: 'contextImage',
    components: [{
      kind: 'OB.UI.Thumbnail',
      name: 'productImage',
      width: '100%',
      height: '100%',
      classes: 'image-wrap image-editline'
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
        style: 'padding: 0px 0px 15px 0px;',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockOtherStore',
          name: 'stockOthers'
        }]
      }, {
        name: 'productDeliveryModes',
        kind: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryModesButton',
        style: 'min-width: 200px; margin: 2px 5px 2px 5px;'
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
      inEvent.warehouseqty = inEvent.warehouseqty ? inEvent.warehouseqty : '0';
      me.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [inEvent.warehousename, inEvent.warehouseqty]));
      me.warehouse = inEvent;
    });
  },
  loadDefaultWarehouseData: function (defaultWarehouse) {
    if (defaultWarehouse) {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [defaultWarehouse.get('warehousename'), defaultWarehouse.get('warehouseqty') ? defaultWarehouse.get('warehouseqty') : '0']));
    } else {
      this.bodyComponent.$.warehouseToGet.setContent(OB.I18N.getLabel('OBPOS_warehouseSelected', [OB.MobileApp.model.get('warehouses')[0].warehousename, '0']));
    }
  },
  getStoreStock: function (params) {
    var me = this;
    if (OB.UTIL.isCrossStoreEnabled()) {
      me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_storeStock_NotCalculated'));
      me.bodyComponent.$.stockHere.setDisabled(OB.UTIL.isCrossStoreProduct(me.product) && (!me.line || OB.DEC.compare(me.line.get('qty')) > 0));
      me.bodyComponent.$.productAddToReceipt.setDisabled(true);
      if (params.checkStockCallback) {
        params.checkStockCallback();
      }
    } else {
      me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
      me.bodyComponent.$.stockHere.setDisabled(false);
      me.bodyComponent.$.productAddToReceipt.setDisabled(true);
    }
    if (!OB.UTIL.isCrossStoreProduct(me.product)) {
      OB.UTIL.StockUtils.getReceiptLineStock(me.product.get('id'), undefined, function (data) {
        if (data && data.exception) {
          me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
          me.bodyComponent.$.stockHere.addClass('error');
        } else if (data.product === me.product.get('id')) {
          if (data.qty || data.qty === 0) {
            data.product = me.product;
            var currentWarehouse;
            if (!_.find(data.warehouses, function (warehouse) {
              return warehouse.warehouseid === OB.MobileApp.model.get('warehouses')[0].warehouseid;
            })) {
              data.warehouses.unshift({
                warehouseid: OB.MobileApp.model.get('warehouses')[0].warehouseid,
                warehousename: OB.MobileApp.model.get('warehouses')[0].warehousename,
                warehouseqty: OB.DEC.Zero
              });
            }
            me.localStockModel = new OB.OBPOSPointOfSale.UsedModels.LocalStock(data);
            currentWarehouse = me.localStockModel.getWarehouseById(me.warehouse.warehouseid || me.warehouse.id);
            me.warehouse.warehouseqty = currentWarehouse.get('warehouseqty');
            me.loadDefaultWarehouseData(currentWarehouse);
            me.bodyComponent.$.stockHere.removeClass('error');
            me.bodyComponent.$.stockHere.setContent(OB.I18N.getLabel('OBPOS_storeStock') + data.qty);
          }
          me.bodyComponent.$.productAddToReceipt.setDisabled(false);
        }
        if (params.checkStockCallback) {
          params.checkStockCallback();
        }
      });
    }
  },
  getOtherStock: function () {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'),
        me = this;
    if (OB.UTIL.isCrossStoreEnabled()) {
      me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_SelectStore'));
      me.bodyComponent.$.stockOthers.setDisabled(me.line && OB.DEC.compare(me.line.get('qty')) < 0);
      me.bodyComponent.$.stockOthers.doOpenOtherStoresStockModal();
    } else if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_otherStoresStock_NotCalculated'));
    } else {
      me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_loadingStock'));
      serverCallStoreDetailedStock.exec({
        organization: OB.MobileApp.model.get('terminal').organization,
        product: me.product.get('id')
      }, function (data) {
        if (data && data.exception) {
          me.bodyComponent.$.stockOthers.setContent(OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved'));
          me.bodyComponent.$.stockOthers.addClass('error');
        } else if (data.product === me.product.get('id') && (data.qty || data.qty === 0)) {
          data.product = me.product;
          me.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(data);
          me.bodyComponent.$.stockOthers.removeClass('error');
          me.bodyComponent.$.stockOthers.setContent(OB.UTIL.isCrossStoreEnabled() ? OB.I18N.getLabel('OBPOS_SelectStore') : OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty);
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
    this.product.set('nameDelivery', OB.UTIL.isCrossStoreProduct(this.product) ? 'Pickup in store' : 'Pick and carry');
    this.product.set('obrdmDeliveryMode', OB.UTIL.isCrossStoreProduct(this.product) ? 'PickupInStore' : 'PickAndCarry');
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.setShowing(OB.UTIL.isNullOrUndefined(this.line));
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.setDetailsView(this.$.leftSubWindowBody.$.body);
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.removeClass('btnlink-orange');
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
      this.bodyComponent.$.productImage.setSrc(OB.UTIL.getImageURL(params.product.get('id')));
      this.bodyComponent.$.productImage.setAttribute('onerror', 'if (this.src != "../org.openbravo.mobile.core/assets/img/box.png") this.src = "../org.openbravo.mobile.core/assets/img/box.png"; ');
    } else {
      this.bodyComponent.$.productImage.setImg(params.product.get('img'));
    }
    this.bodyComponent.$.warehouseToGet.setContent(OB.UTIL.isCrossStoreProduct(this.product) ? OB.I18N.getLabel('OBPOS_loadingFromCrossStoreWarehouses') : OB.I18N.getLabel('OBPOS_loadingFromWarehouse', [this.warehouse.warehousename]));
    this.bodyComponent.$.productPrice.setContent(params.product.has('standardPrice') ? OB.I18N.getLabel('OBPOS_priceInfo') + '<b>' + OB.I18N.formatCurrency(params.product.get('standardPrice')) + '</b>' : OB.I18N.getLabel('OBPOS_priceInfo'));
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