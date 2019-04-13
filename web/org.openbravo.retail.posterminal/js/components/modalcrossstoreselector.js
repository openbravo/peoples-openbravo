/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  name: 'OBPOS.UI.CrossStoreSelector',
  kind: 'OB.UI.ModalSelector',
  classes: 'obpos-modal-store-selector',
  topPosition: '70px',
  i18nHeader: 'OBPOS_SelectStore',
  body: {
    kind: 'OBPOS.UI.CrossStoreList'
  },
  productId: null,
  executeOnShow: function () {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
      this.productId = this.args.productId;
      this.$.body.$.crossStoreList.searchAction(null, {
        filters: []
      });
    }
  },
  executeOnHide: function () {
    this.inherited(arguments);
    OB.MobileApp.view.scanningFocus(true);
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.crossStoreList.$.csStoreSelector.$.theader.$.modalCrossStoreProductScrollableHeader.$.filterSelector;
  }
});


enyo.kind({
  name: 'OBPOS.UI.ModalCrossStoreProductScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  components: [{
    classes: 'obpos-filter-selector',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.CrossStoreFilter.getProperties()
  }]
});

/* Scrollable table (body of modal) */
enyo.kind({
  name: 'OBPOS.UI.CrossStoreList',
  classes: 'row-fluid',
  handlers: {
    onClearFilterSelector: 'searchAction',
    onSearchAction: 'searchAction'
  },
  events: {
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid obpos-list-store',
      components: [{
        classes: 'span12',
        components: [{
          name: 'csStoreSelector',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '420px',
          renderHeader: 'OBPOS.UI.ModalCrossStoreProductScrollableHeader',
          renderLine: 'OBPOS.UI.CrossStoreLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }, {
          name: 'renderLoading',
          classes: 'obpos-list-orders obpos-list-orders-renderloading',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }]
      }]
    }]
  }],
  searchAction: function (inSender, inEvent) {
    var me = this,
        remoteFilters = [],
        params = {},
        currentDate = new Date();
    params.remoteModel = true;
    params.terminalTime = currentDate;
    params.terminalTimeOffset = {
      value: currentDate.getTimezoneOffset(),
      type: 'long'
    };

    _.each(inEvent.filters, function (flt) {

      var column = _.find(OB.Model.CrossStoreFilter.getProperties(), function (col) {
        return col.column === flt.column;
      });
      if (flt.value && column) {
        remoteFilters.push({
          columns: [column.name],
          value: flt.value,
          operator: flt.operator || OB.Dal.STARTSWITH
        });
      }
    });

    function successCallBack(data) {
      if (data && !data.exception) {
        me.$.csStoreSelector.collection.reset(data);
        me.$.renderLoading.hide();
      } else {
        OB.UTIL.showError(OB.I18N.getLabel(data.exception.message));
        me.$.csStoreSelector.collection.reset();
        me.$.renderLoading.hide();
        me.$.csStoreSelector.$.tempty.show();
      }
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError(error);
    }

    var process = new OB.DS.Process(OB.Model.CrossStoreFilter.prototype.source);

    process.exec({
      _limit: OB.Model.CrossStoreFilter.prototype.dataLimit,
      remoteFilters: remoteFilters,
      product: this.owner.owner.productId,
      parameters: params
    }, successCallBack, errorCallback);
  },

  productsList: null,

  init: function (model) {
    var me = this,
        terminal = OB.POS.modelterminal.get('terminal');
    this.productsList = new Backbone.Collection();
    this.$.csStoreSelector.setCollection(this.productsList);
  }
});

/* items of collection */
enyo.kind({
  name: 'OBPOS.UI.CrossStoreLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obpos-listitembutton',
  components: [{
    classes: 'obpos-store-information',
    name: 'iconStore',
    tap: function () {
      this.bubble('onShowPopup', {
        popup: 'storeInformation',
        args: {
          context: this,
          orgId: this.owner.model.get('id'),
          orgName: this.owner.model.get('name')
        }
      });
    }
  }, {
    classes: 'obpos-row-store-name',
    name: 'storeName'
  }, {
    classes: 'obpos-row-store-price',
    name: 'price'
  }, {
    classes: 'obpos-row-store-stock',
    name: 'stock'
  }, {
    classes: '.changedialog-properties-end'
  }],
  create: function () {
    this.inherited(arguments);
    this.$.storeName.setContent(this.model.get('name'));
    this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('price')));
    this.$.stock.setContent(this.model.get('stock') + ' Ud');
  }
});