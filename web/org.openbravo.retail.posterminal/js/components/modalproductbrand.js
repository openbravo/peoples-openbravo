/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalProductBrandHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'filterText',
          style: 'width: 100%'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    this.doSearchAction({
      valueName: this.$.filterText.getValue()
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBrandsLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'border-bottom: 1px solid #cccccc;text-align: left; padding-left: 70px;',
  events: {
    onHideThisPopup: '',
    onSelectBrand: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doSelectBrand({
      value: this.model
    });
    this.doHideThisPopup();
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(this.model.get('name'));
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBrands',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'brandslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalProductBrandHeader',
          renderLine: 'OB.UI.ListBrandsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.brandsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        i, j, criteria = {},
        filter = inEvent.valueName;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBrands(dataBrands) {
      if (dataBrands && dataBrands.length > 0) {
        for (i = 0; i < dataBrands.length; i++) {
          for (j = 0; j < me.parent.parent.model.get('brandFilter').length; j++) {
            if (dataBrands.models[i].get('id') === me.parent.parent.model.get('brandFilter')[j].id) {
              dataBrands.models[i].set('checked', true);
            }
          }
        }
        me.brandsList.reset(dataBrands.models);
      } else {
        me.brandsList.reset();
      }
    }
    criteria._filter = {
      operator: OB.Dal.CONTAINS,
      value: filter
    };
    OB.Dal.find(OB.Model.Brand, criteria, successCallbackBrands, errorCallback);
    return true;
  },
  brandsList: null,
  init: function (model) {
    this.brandsList = new Backbone.Collection();
    this.$.brandslistitemprinter.setCollection(this.brandsList);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductBrand',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null
  },
  executeOnHide: function () {
    this.$.body.$.listBrands.$.brandslistitemprinter.$.theader.$.modalProductBrandHeader.clearAction();
  },
  executeOnShow: function () {
    var i, j;
    this.$.header.setContent(OB.I18N.getLabel('OBMOBC_LblBrand'));
    this.waterfall('onSearchAction', {
      valueName: this.$.body.$.listBrands.$.brandslistitemprinter.$.theader.$.modalProductBrandHeader.$.filterText.getValue()
    });
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListBrands'
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});