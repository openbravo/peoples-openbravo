/*
 ************************************************************************************
 * Copyright (C) 2013-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockThisStore',
  kind: 'OB.UI.Button',
  classes:
    'obObposPointOfSaleUiProductDetailsViewButtonStockThisStore obUiActionButton',
  events: {
    onOpenLocalStockModal: '',
    onOpenLocalStockClickableModal: ''
  },
  tap: function() {
    if (
      !OB.MobileApp.model.get('permissions').OBPOS_warehouseselectionforline ||
      !this.model.get('order').get('isEditable')
    ) {
      this.doOpenLocalStockModal();
    } else {
      this.doOpenLocalStockClickableModal();
    }
  },
  init: function(model) {
    this.model = model;
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockOtherStore',
  kind: 'OB.UI.Button',
  classes:
    'obObposPointOfSaleUiProductDetailsViewButtonStockOtherStore obUiActionButton',
  events: {
    onOpenOtherStoresStockModal: ''
  },
  tap: function() {
    var me = this,
      leftSubWindow = me.parent.leftSubWindow,
      organization = leftSubWindow.organization || {
        id: OB.MobileApp.model.get('terminal').organization,
        name: OB.I18N.getLabel('OBPOS_LblThisStore', [
          OB.MobileApp.model.get('terminal').organization$_identifier
        ]),
        country: OB.MobileApp.model.get('terminal').organizationCountryId,
        region: OB.MobileApp.model.get('terminal').organizationRegionId
      };
    if (
      OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) &&
      !OB.UTIL.isCrossStoreOrganization(organization)
    ) {
      var serverCallStoreDetailedStock = new OB.DS.Process(
        'org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'
      );
      leftSubWindow.bodyComponent.$.stockOthers.setContent(
        OB.I18N.getLabel('OBPOS_loadingStock')
      );
      serverCallStoreDetailedStock.exec(
        {
          organization: OB.MobileApp.model.get('terminal').organization,
          product: leftSubWindow.product.get('id')
        },
        function(data) {
          if (data && data.exception) {
            leftSubWindow.bodyComponent.$.stockOthers.setContent(
              OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved')
            );
            leftSubWindow.bodyComponent.$.stockOthers.addClass(
              'obObposPointOfSaleUiProductDetailsViewButtonStockOtherStore_error'
            );
          } else if (
            data.product === leftSubWindow.product.get('id') &&
            leftSubWindow.showing &&
            (data.qty || data.qty === 0)
          ) {
            data.product = leftSubWindow.product;
            leftSubWindow.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(
              data
            );
            me.doOpenOtherStoresStockModal({
              isSelectStore: true
            });
            leftSubWindow.bodyComponent.$.stockOthers.removeClass(
              'obObposPointOfSaleUiProductDetailsViewButtonStockOtherStore_error'
            );
            leftSubWindow.bodyComponent.$.stockOthers.setContent(
              OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty
            );
          }
        }
      );
    } else {
      this.doOpenOtherStoresStockModal({
        isSelectStore: true
      });
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonAddToTicket',
  kind: 'OB.UI.Button',
  classes: 'obObposPointOfSaleUiProductDetailsViewButtonAddToTicket',
  i18nLabel: 'OBPOS_addToTicket',
  events: {
    onAddProduct: '',
    onSetLineProperty: '',
    onCloseLeftSubWindow: ''
  },
  setProperLabel: function() {
    if (this.leftSubWindow && this.leftSubWindow.line) {
      this.setLabel(OB.I18N.getLabel('OBMOBC_LblApply'));
    } else {
      this.setLabel(OB.I18N.getLabel('OBPOS_addToTicket'));
    }
  },
  tap: function() {
    var product = this.leftSubWindow.product,
      me = this;

    if (product) {
      var line = null;
      if (me.leftSubWindow && me.leftSubWindow.line) {
        line = me.leftSubWindow.line;
      }
      var attrs =
        me.leftSubWindow.inEvent && me.leftSubWindow.inEvent.attrs
          ? me.leftSubWindow.inEvent.attrs
          : {};
      attrs.organization = me.leftSubWindow.organization || {
        id: OB.MobileApp.model.get('terminal').organization,
        name: OB.I18N.getLabel('OBPOS_LblThisStore', [
          OB.MobileApp.model.get('terminal').organization$_identifier
        ]),
        country: OB.MobileApp.model.get('terminal').organizationCountryId,
        region: OB.MobileApp.model.get('terminal').organizationRegionId
      };
      me.leftSubWindow.organization = null;
      attrs.warehouse = {
        id: me.leftSubWindow.warehouse.warehouseid
          ? this.leftSubWindow.warehouse.warehouseid
          : this.leftSubWindow.warehouse.id,
        warehousename: me.leftSubWindow.warehouse.warehousename,
        warehouseqty: me.leftSubWindow.warehouse.warehouseqty
      };
      if (me.leftSubWindow.documentType) {
        attrs.documentType = me.leftSubWindow.documentType;
        attrs.quotationDocumentType = me.leftSubWindow.quotationDocumentType;
      }
      me.leftSubWindow.documentType = null;
      me.leftSubWindow.quotationDocumentType = null;
      if (
        line &&
        line.get('obrdmDeliveryMode') !== product.get('obrdmDeliveryMode') &&
        product.get('productType') !== 'S' &&
        OB.UTIL.isCrossStoreOrganization(attrs.organization)
      ) {
        attrs.obrdmDeliveryMode = product.get('obrdmDeliveryMode');
        attrs.obrdmDeliveryDate = product.get('obrdmDeliveryDate');
        attrs.obrdmDeliveryTime = product.get('obrdmDeliveryTime');
        attrs.nameDelivery = _.find(
          OB.MobileApp.model.get('deliveryModes'),
          function(mode) {
            return mode.id === attrs.obrdmDeliveryMode;
          }
        ).name;
      }

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreTapStockAddReceipt',
        {
          context: me,
          params: attrs,
          line: line
        },
        function(args) {
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
            if (attrs.obrdmDeliveryMode) {
              me.doSetLineProperty({
                line: line,
                property: 'obrdmDeliveryMode',
                value: attrs.obrdmDeliveryMode
              });
              me.doSetLineProperty({
                line: line,
                property: 'nameDelivery',
                value: attrs.nameDelivery
              });
              if (attrs.obrdmDeliveryDate) {
                me.doSetLineProperty({
                  line: line,
                  property: 'obrdmDeliveryDate',
                  value: attrs.obrdmDeliveryDate
                });
              }
              if (attrs.obrdmDeliveryTime) {
                me.doSetLineProperty({
                  line: line,
                  property: 'obrdmDeliveryTime',
                  value: attrs.obrdmDeliveryTime
                });
              }
            }
            if (OB.UTIL.isCrossStoreEnabled()) {
              line.set(
                {
                  priceList: product.get('currentPrice').price,
                  price: product.get('currentPrice').price
                },
                {
                  silent: true
                }
              );
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
        }
      );
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose',
  kind: 'OB.UI.ModalCloseButton',
  classes: 'obObposPointOfSaleUiProductDetailsViewButtonClose',
  tap: function() {
    if (this.getDisabled()) {
      return;
    }
    this.leftSubWindow.doCloseLeftSubWindow();
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_header',
  classes: 'obObposPointOfSaleUiProductDetailsViewHeader',
  components: [
    {
      name: 'productName',
      classes: 'obObposPointOfSaleUiProductDetailsViewHeader-productName'
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonClose',
      name: 'buttonClose',
      classes: 'obObposPointOfSaleUiProductDetailsViewHeader-buttonClose'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_body',
  classes: 'obObposPointOfSaleUiProductDetailsViewBody',
  handlers: {
    onOpenLocalStockModal: 'openLocalStockModal',
    onOpenLocalStockClickableModal: 'openLocalStockClickableModal',
    onOpenOtherStoresStockModal: 'openOtherStoresStockModal'
  },
  events: {
    onShowPopup: ''
  },
  openLocalStockModal: function() {
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
  openLocalStockClickableModal: function() {
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
  openOtherStoresStockModal: function(inSender, inEvent) {
    if (OB.UTIL.isCrossStoreEnabled()) {
      var me = this;
      var selectedStoreCallBack = function(data) {
        var warehouse = data.warehouse
            ? data.warehouse
            : {
                warehouseid: data.warehouseid,
                warehousename: data.warehousename,
                warehouseqty: data.stock
              },
          organization = data.organization
            ? data.organization
            : {
                id: data.orgId,
                name: data.orgName,
                country: data.countryId,
                region: data.regionId
              };
        me.$.stockHere.removeClass('error');
        me.$.stockHere.setContent(
          OB.I18N.getLabel('OBPOS_storeStock_NotCalculated')
        );
        me.$.productPrice.setContent(
          OB.I18N.getLabel('OBPOS_priceInfo') +
            '<b>' +
            OB.I18N.formatCurrency(data.currentPrice.price) +
            '</b>'
        );
        me.$.productAddToReceipt.setProperLabel();
        me.$.productAddToReceipt.setDisabled(false);
        me.leftSubWindow.documentType = data.documentType;
        me.leftSubWindow.quotationDocumentType = data.quotationDocumentType;
        me.leftSubWindow.organization = organization;
        if (OB.UTIL.isCrossStoreOrganization(organization)) {
          me.leftSubWindow.setDefaultDeliveryMode();
        }
        me.leftSubWindow.bodyComponent.$.productDeliveryModes.setShowing(
          me.leftSubWindow.product.get('productType') !== 'S' &&
            OB.UTIL.isCrossStoreOrganization(organization)
        );
        me.leftSubWindow.changeWarehouseInfo(null, warehouse);
        me.leftSubWindow.product.set('listPrice', data.currentPrice.price);
        me.leftSubWindow.product.set('standardPrice', data.currentPrice.price);
        me.leftSubWindow.product.set('organization', organization);
        if (data.productPrices) {
          me.leftSubWindow.product.set('productPrices', data.productPrices);
        } else {
          me.leftSubWindow.product.set('currentPrice', data.currentPrice);
        }
      };
      if (
        !this.leftSubWindow.forceSelectStore &&
        !inEvent.isSelectStore &&
        (this.leftSubWindow.line ||
          !OB.UTIL.isCrossStoreProduct(this.leftSubWindow.product))
      ) {
        var data = null;
        if (this.leftSubWindow.line) {
          data = {
            stock: this.leftSubWindow.line.get('warehouse').warehouseqty,
            warehouse: this.leftSubWindow.line.get('warehouse'),
            organization: this.leftSubWindow.line.get('organization')
          };
          if (
            OB.UTIL.isCrossStoreOrganization(
              this.leftSubWindow.line.get('organization')
            ) ||
            OB.UTIL.isCrossStoreProduct(this.leftSubWindow.line.get('product'))
          ) {
            data.currentPrice = this.leftSubWindow.line
              .get('product')
              .get('currentPrice');
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
              name: OB.I18N.getLabel('OBPOS_LblThisStore', [
                OB.MobileApp.model.get('terminal').organization$_identifier
              ]),
              country: OB.MobileApp.model.get('terminal').organizationCountryId,
              region: OB.MobileApp.model.get('terminal').organizationRegionId
            }
          };
          selectedStoreCallBack(data);
        }
      } else {
        if (
          this.leftSubWindow.product.get('productType') === 'I' ||
          this.leftSubWindow.product.get('productType') === 'S'
        ) {
          this.doShowPopup({
            popup: 'OBPOS_modalCrossStoreSelector',
            args: {
              productId: this.leftSubWindow.product.get('id'),
              productType: this.leftSubWindow.product.get('productType'),
              productUOM: this.leftSubWindow.product.get('uOMsymbol'),
              callback: selectedStoreCallBack
            }
          });
        } else {
          data = {
            orgId: this.leftSubWindow.product.get('orgId'),
            orgName: this.leftSubWindow.product.get('organization').name,
            countryId: this.leftSubWindow.product.get('organization').country,
            regionId: this.leftSubWindow.product.get('organization').region,
            warehouseid: this.leftSubWindow.product.get('warehouse').id,
            warehousename: this.leftSubWindow.product.get('warehouse')
              .warehousename,
            stock: 0,
            currentPrice: {
              priceListId: OB.MobileApp.model.get('terminal').priceList,
              price: this.leftSubWindow.product.get('standardPrice')
            }
          };
          selectedStoreCallBack(data);
        }
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
  components: [
    {
      name: 'contextImage',
      classes: 'obObposPointOfSaleUiProductDetailsViewBody-contextImage',
      components: [
        {
          kind: 'OB.UI.Thumbnail',
          name: 'productImage',
          classes:
            'obObposPointOfSaleUiProductDetailsViewBody-contextImage-productImage'
        }
      ]
    },
    {
      classes: 'obObposPointOfSaleUiProductDetailsViewBody-container2',
      components: [
        {
          name: 'warehouseToGet',
          allowHtml: true,
          classes:
            'obObposPointOfSaleUiProductDetailsViewBody-container2-warehouseToGet'
        }
      ]
    },
    {
      classes: 'obObposPointOfSaleUiProductDetailsViewBody-container3',
      components: [
        {
          classes:
            'obObposPointOfSaleUiProductDetailsViewBody-container3-container1',
          components: [
            {
              classes:
                'obObposPointOfSaleUiProductDetailsViewBody-container3-container1-container1',
              components: [
                {
                  kind:
                    'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockThisStore',
                  name: 'stockHere',
                  classes:
                    'obObposPointOfSaleUiProductDetailsViewBody-container3-container1-container1-stockHere'
                }
              ]
            },
            {
              classes:
                'obObposPointOfSaleUiProductDetailsViewBody-container3-container1-container2',
              components: [
                {
                  kind:
                    'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonStockOtherStore',
                  name: 'stockOthers',
                  classes:
                    'obObposPointOfSaleUiProductDetailsViewBody-container3-container1-container2-stockOthers'
                }
              ]
            },
            {
              name: 'productDeliveryModes',
              kind: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryModesButton',
              classes:
                'obObposPointOfSaleUiProductDetailsViewBody-container3-container1-productDeliveryModes'
            }
          ]
        },
        {
          classes:
            'obObposPointOfSaleUiProductDetailsViewBody-container3-container2',
          components: [
            {
              name: 'productPrice',
              allowHtml: true,
              classes:
                'obObposPointOfSaleUiProductDetailsViewBody-container3-container2-productPrice'
            },
            {
              classes:
                'obObposPointOfSaleUiProductDetailsViewBody-container3-container2-container2',
              components: [
                {
                  name: 'productAddToReceipt',
                  kind:
                    'OB.OBPOSPointOfSale.UI.ProductDetailsView_ButtonAddToTicket',
                  classes:
                    'obObposPointOfSaleUiProductDetailsViewBody-container3-container2-container2-productAddToReceipt'
                }
              ]
            }
          ]
        }
      ]
    },
    {
      kind: 'Scroller',
      classes: 'obObposPointOfSaleUiProductDetailsViewBody-scroller',
      components: [
        {
          name: 'descriptionArea',
          classes:
            'obObposPointOfSaleUiProductDetailsViewBody-scroller-descriptionArea'
        }
      ]
    }
  ]
});

enyo.kind({
  kind: 'OB.UI.LeftSubWindow',
  name: 'OB.OBPOSPointOfSale.UI.ProductDetailsView',
  classes: 'obObposPointOfSaleUiProductDetailsView',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onModifyWarehouse: 'changeWarehouseInfo'
  },
  changeWarehouseInfo: function(inSender, inEvent) {
    var me = this;
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_BeforeWarehouseChange',
      {
        oldWarehouse: me.warehouse,
        newWarehouse: inEvent,
        currentLine: me.line
      },
      function(args) {
        if (args && args.cancelOperation) {
          return;
        }
        inEvent.warehouseqty = inEvent.warehouseqty
          ? inEvent.warehouseqty
          : '0';
        me.setWarehouseInfo(inEvent.warehousename, inEvent.warehouseqty);
        me.warehouse = inEvent;
      }
    );
  },
  setWarehouseInfo: function(warehouseName, quantity) {
    this.bodyComponent.$.warehouseToGet.setContent(
      OB.UTIL.isNullOrUndefined(quantity) ||
        this.product.get('productType') === 'S'
        ? warehouseName
        : OB.I18N.getLabel('OBPOS_warehouseSelected', [warehouseName, quantity])
    );
  },
  loadDefaultWarehouseData: function(defaultWarehouse) {
    if (defaultWarehouse) {
      this.setWarehouseInfo(
        defaultWarehouse.get('warehousename'),
        defaultWarehouse.get('warehouseqty')
      );
    } else {
      this.setWarehouseInfo(
        OB.MobileApp.model.get('warehouses')[0].warehousename,
        0
      );
    }
  },
  getStoreStock: function(params) {
    var me = this;
    if (OB.UTIL.isCrossStoreEnabled()) {
      me.bodyComponent.$.stockHere.setShowing(
        this.product.get('productType') !== 'S'
      );
      me.bodyComponent.$.stockHere.setContent(
        OB.I18N.getLabel('OBPOS_storeStock_NotCalculated')
      );
      me.bodyComponent.$.stockHere.setDisabled(
        OB.UTIL.isNullOrUndefined(me.organization) ||
          ((OB.UTIL.isCrossStoreOrganization(me.organization) ||
            OB.UTIL.isCrossStoreProduct(me.product)) &&
            (!me.line || OB.DEC.compare(me.line.get('qty')) > 0))
      );
      if (this.product.get('productType') !== 'S') {
        me.bodyComponent.$.productAddToReceipt.setDisabled(true);
      }
      if (params.checkStockCallback) {
        params.checkStockCallback();
      }
    } else {
      me.bodyComponent.$.stockHere.setContent(
        OB.I18N.getLabel('OBPOS_loadingStock')
      );
      me.bodyComponent.$.stockHere.setDisabled(false);
      me.bodyComponent.$.productAddToReceipt.setDisabled(true);
    }
    if (
      this.product.get('productType') !== 'S' &&
      (OB.UTIL.isNullOrUndefined(me.organization) ||
        !OB.UTIL.isCrossStoreOrganization(me.organization))
    ) {
      OB.UTIL.StockUtils.getReceiptLineStock(
        me.product.get('id'),
        undefined,
        function(data) {
          if (data && data.exception) {
            me.bodyComponent.$.stockHere.setContent(
              OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved')
            );
            me.bodyComponent.$.stockHere.addClass('error');
          } else if (data.product === me.product.get('id')) {
            if (data.qty || data.qty === 0) {
              data.product = me.product;
              var currentWarehouse;
              if (
                !_.find(data.warehouses, function(warehouse) {
                  return (
                    warehouse.warehouseid ===
                    OB.MobileApp.model.get('warehouses')[0].warehouseid
                  );
                })
              ) {
                data.warehouses.unshift({
                  warehouseid: OB.MobileApp.model.get('warehouses')[0]
                    .warehouseid,
                  warehousename: OB.MobileApp.model.get('warehouses')[0]
                    .warehousename,
                  warehouseqty: OB.DEC.Zero
                });
              }
              me.localStockModel = new OB.OBPOSPointOfSale.UsedModels.LocalStock(
                data
              );
              currentWarehouse = me.localStockModel.getWarehouseById(
                me.warehouse.warehouseid || me.warehouse.id
              );
              me.warehouse.warehouseqty = currentWarehouse.get('warehouseqty');
              me.loadDefaultWarehouseData(currentWarehouse);
              me.bodyComponent.$.stockHere.removeClass('error');
              me.bodyComponent.$.stockHere.setContent(
                OB.I18N.getLabel('OBPOS_storeStock') + data.qty
              );
            }
          }
          me.bodyComponent.$.productAddToReceipt.setDisabled(false);
          if (params.checkStockCallback) {
            params.checkStockCallback();
          }
        }
      );
    }
  },
  getOtherStock: function() {
    var serverCallStoreDetailedStock = new OB.DS.Process(
        'org.openbravo.retail.posterminal.stock.OtherStoresDetailedStock'
      ),
      me = this;
    if (OB.UTIL.isCrossStoreEnabled()) {
      me.bodyComponent.$.stockOthers.setContent(
        OB.I18N.getLabel('OBPOS_SelectStore')
      );
      me.bodyComponent.$.stockOthers.setDisabled(
        me.line && OB.DEC.compare(me.line.get('qty')) < 0
      );
      me.bodyComponent.$.stockOthers.doOpenOtherStoresStockModal();
    } else if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      me.bodyComponent.$.stockOthers.setContent(
        OB.I18N.getLabel('OBPOS_otherStoresStock_NotCalculated')
      );
    } else {
      me.bodyComponent.$.stockOthers.setContent(
        OB.I18N.getLabel('OBPOS_loadingStock')
      );
      serverCallStoreDetailedStock.exec(
        {
          organization: OB.MobileApp.model.get('terminal').organization,
          product: me.product.get('id')
        },
        function(data) {
          if (data && data.exception) {
            me.bodyComponent.$.stockOthers.setContent(
              OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved')
            );
            me.bodyComponent.$.stockOthers.addClass(
              'obObposPointOfSaleUiProductDetailsViewBody-stockOthers_error'
            );
          } else if (
            data.product === me.product.get('id') &&
            (data.qty || data.qty === 0)
          ) {
            data.product = me.product;
            me.otherStoresStockModel = new OB.OBPOSPointOfSale.UsedModels.OtherStoresWarehousesStock(
              data
            );
            me.bodyComponent.$.stockOthers.removeClass(
              'obObposPointOfSaleUiProductDetailsViewBody-stockOthers_error'
            );
            me.bodyComponent.$.stockOthers.setContent(
              OB.UTIL.isCrossStoreEnabled()
                ? OB.I18N.getLabel('OBPOS_SelectStore')
                : OB.I18N.getLabel('OBPOS_otherStoresStock') + data.qty
            );
          }
        }
      );
    }
  },
  beforeSetShowing: function(params) {
    if (!params.product || OB.MobileApp.model.get('warehouses').length === 0) {
      this.doShowPopup({
        popup: 'modalConfigurationRequiredForCrossStore'
      });
      return false;
    }
    this.line = params.line || null;
    this.product = params.product;
    this.forceSelectStore = params.forceSelectStore || false;
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.setShowing(
      this.product.get('productType') !== 'S' &&
        (!OB.UTIL.isNullOrUndefined(this.organization) &&
          OB.UTIL.isCrossStoreOrganization(this.organization))
    );
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.setDetailsView(
      this.$.leftSubWindowBody.$.body
    );
    this.$.leftSubWindowBody.leftSubWindow.bodyComponent.$.productDeliveryModes.removeClass(
      'btnlink-orange'
    );
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
    this.headerComponent.$.productName.setContent(
      params.product.get('_identifier') +
        ' (' +
        params.product.get('uOMsymbol') +
        ')'
    );
    if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true)) {
      this.bodyComponent.$.contextImage.hide();
    } else {
      this.bodyComponent.$.contextImage.show();
    }
    if (OB.MobileApp.model.get('permissions')['OBPOS_retail.productImages']) {
      this.bodyComponent.$.productImage.setImgUrl(
        OB.UTIL.getImageURL(params.product.get('id'))
      );
      this.bodyComponent.$.productImage.setAttribute(
        'onerror',
        'if (this.src != "../org.openbravo.mobile.core/assets/img/box.png") this.src = "../org.openbravo.mobile.core/assets/img/box.png"; '
      );
    } else {
      this.bodyComponent.$.productImage.setImg(params.product.get('img'));
    }
    this.setWarehouseInfo(
      OB.UTIL.isCrossStoreProduct(this.product)
        ? OB.I18N.getLabel('OBPOS_loadingFromCrossStoreWarehouses')
        : OB.I18N.getLabel('OBPOS_loadingFromWarehouse', [
            this.warehouse.warehousename
          ])
    );
    this.bodyComponent.$.productPrice.setContent(
      params.product.has('standardPrice')
        ? OB.I18N.getLabel('OBPOS_priceInfo') +
            '<b>' +
            OB.I18N.formatCurrency(params.product.get('standardPrice')) +
            '</b>'
        : OB.I18N.getLabel('OBPOS_priceInfo')
    );
    this.bodyComponent.$.descriptionArea.setContent(
      params.product.get('description')
    );
    this.bodyComponent.$.productAddToReceipt.setProperLabel();
    this.getOtherStock();
    this.getStoreStock(params);
    return true;
  },
  setDefaultDeliveryMode: function() {
    var defaultOrderDeliveryMode = OB.MobileApp.model.receipt.get(
      'obrdmDeliveryModeProperty'
    );
    this.product.set(
      'obrdmDeliveryMode',
      OB.UTIL.isCrossStoreOrganization(this.organization) &&
        defaultOrderDeliveryMode === 'PickAndCarry'
        ? 'PickupInStore'
        : defaultOrderDeliveryMode
    );
    this.product.set(
      'obrdmDeliveryDate',
      OB.MobileApp.model.receipt.get('obrdmDeliveryDateProperty')
    );
    this.product.set(
      'obrdmDeliveryTime',
      OB.MobileApp.model.receipt.get('obrdmDeliveryTimeProperty')
    );
  },
  header: {
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_header',
    name: 'header',
    classes: 'obObposPointOfSaleUiProductDetailsView-header-header'
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView_body',
    name: 'body',
    classes: 'obObposPointOfSaleUiProductDetailsView-body-body'
  }
});
