/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

/* items of collection */
enyo.kind({
  name: 'OBRDM.UI.ListWarehouseLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obrdmUiListWarehouseLine',
  components: [
    {
      name: 'line',
      classes: 'obrdmUiListWarehouseLine-line obrdm-listwarehouseline-line',
      components: [
        {
          classes: 'obrdm-listwarehouseline-organization',
          name: 'organization'
        },
        {
          classes: 'obrdm-listwarehouseline-quantity',
          name: 'quantity'
        },
        {
          classes: 'obrdmUiListWarehouseLine-line-element1'
        }
      ]
    }
  ],
  events: {
    onSelectWarehouse: ''
  },
  tap: function() {
    this.doSelectWarehouse({
      warehouseId: this.model.get('warehouseid'),
      warehouseName: this.model.get('warehousename')
    });
  },
  create: function() {
    this.inherited(arguments);
    this.$.organization.setContent(this.model.get('warehousename'));
    this.$.quantity.setContent(this.model.get('warehouseqty'));
  }
});

/* Scrollable table (body of modal) */
enyo.kind({
  name: 'OBRDM.UI.ListWarehouse',
  classes: 'obrdmUiListWarehouse row-fluid',
  components: [
    {
      classes: 'obrdmUiListWarehouse-container1',
      components: [
        {
          classes: 'obrdmUiListWarehouse-container1-container1 row-fluid',
          components: [
            {
              classes: 'obrdmUiListWarehouse-container1-container1-container1',
              components: [
                {
                  name: 'stWarehouseSelector',
                  kind: 'OB.UI.ScrollableTable',
                  classes:
                    'obrdmUiListWarehouse-container1-container1-container1-stWarehouseSelector',
                  renderLine: 'OBRDM.UI.ListWarehouseLine',
                  renderEmpty: 'OB.UI.RenderEmpty'
                },
                {
                  name: 'renderLoading',
                  classes:
                    'obrdmUiListWarehouse-container1-container1-container1-renderLoading',
                  showing: false,
                  initComponents: function() {
                    this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
                  }
                },
                {
                  name: 'renderError',
                  classes:
                    'obrdmUiListWarehouse-container1-container1-container1-renderError',
                  showing: false,
                  initComponents: function() {
                    this.setContent(
                      OB.I18N.getLabel('OBPOS_stockCannotBeRetrieved')
                    );
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ],

  search: function(product) {
    this.$.stWarehouseSelector.$.tempty.hide();
    this.$.stWarehouseSelector.$.tbody.hide();
    this.$.stWarehouseSelector.$.tlimit.hide();
    this.$.renderError.hide();
    this.$.renderLoading.show();

    var me = this;
    OB.UTIL.StockUtils.getReceiptLineStock(product, undefined, function(data) {
      me.$.renderLoading.hide();
      if (data && data.exception) {
        me.$.renderError.show();
      } else {
        me.$.stWarehouseSelector.getCollection().reset(data.warehouses);
      }
    });
  },

  warehouseList: null,

  init: function(model) {
    this.warehouseList = new Backbone.Collection();
    this.$.stWarehouseSelector.setCollection(this.warehouseList);
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OBRDM.UI.ModalWarehouseSelector',
  classes: 'obrdmUiModalWarehouseSelector',
  i18nHeader: 'OBRDM_LblSelectWarehouse',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    onSelectWarehouse: 'selectWarehouse'
  },
  body: {
    kind: 'OBRDM.UI.ListWarehouse'
  },
  selectWarehouse: function(inSender, inEvent) {
    this.warehouseId = inEvent.warehouseId;
    this.warehouseName = inEvent.warehouseName;
    this.doHideThisPopup();
    return true;
  },
  executeOnShow: function() {
    this.$.body.$.listWarehouse.search(this.args.product);
    this.warehouseId = null;
    this.warehouseName = null;
    return true;
  },
  executeOnHide: function() {
    if (this.args.callback) {
      this.args.callback(
        this.args.context,
        this.warehouseId,
        this.warehouseName
      );
    }
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalWarehouseSelector',
  name: 'OBRDM_ModalWarehouseSelector'
});
