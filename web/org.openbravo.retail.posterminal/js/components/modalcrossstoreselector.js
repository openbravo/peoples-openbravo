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
  topPosition: '70px',
  i18nHeader: 'OBPOS_SelectStore',
  style: 'width: 725px',
  body: {
    kind: 'OBPOS.UI.ListCrossStoreProducts'
  },
  executeOnShow: function () {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
  },
  executeOnHide: function () {
    this.inherited(arguments);
    OB.MobileApp.view.scanningFocus(true);
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.listCrossStoreProducts.$.csProductsSelector.$.theader.$.modalCrossStoreProductScrollableHeader.$.filterSelector;
  }
});


enyo.kind({
  name: 'OBPOS.UI.ModalCrossStoreProductScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  components: [{
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector',
    filters: OB.Model.CrossStoreFilter.getProperties()
  }]
});

/* Scrollable table (body of modal) */
enyo.kind({
  name: 'OBPOS.UI.ListCrossStoreProducts',
  classes: 'row-fluid',
  handlers: {
    onClearFilterSelector: 'clearAction',
    onSearchAction: 'searchAction',
    onSelectAll: 'selectAll',
    onPrepareSelected: 'prepareSelected',
    onChangeLine: 'changeLine',
    onChangeAllLines: 'changeAllLines'
  },
  events: {
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'csProductsSelector',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '420px',
          renderHeader: 'OBPOS.UI.ModalCrossStoreProductScrollableHeader',
          renderLine: 'OB.UI.RenderEmpty',
          renderEmpty: 'OB.UI.RenderEmpty'
        }, {
          name: 'renderLoading',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }]
      }]
    }]
  }],

  clearAction: function () {
    this.productsList.reset();
    return true;
  },

  searchAction: function (inSender, inEvent) {},

  productsList: null,

  init: function (model) {
    var me = this,
        terminal = OB.POS.modelterminal.get('terminal');
    this.productsList = new Backbone.Collection();
    this.$.csProductsSelector.setCollection(this.productsList);
  }
});