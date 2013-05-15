/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBrandsLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'border-bottom: 1px solid #cccccc;text-align: left; padding-left: 70px;',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.model.set('checked', !this.model.get('checked'));
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
        i, j;

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
    OB.Dal.find(OB.Model.Brand, null, successCallbackBrands, errorCallback);
    return true;
  },
  brandsList: null,
  init: function (model) {
    this.brandsList = new Backbone.Collection();
    this.$.brandslistitemprinter.setCollection(this.brandsList);
  }
});

enyo.kind({
  name: 'OB.UI.ModalProductBrandTopHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onHideThisPopup: '',
    onSelectBrand: '',
    onSearchAction: ''
  },
  components: [{
    style: 'display: table;',
    components: [{
      style: 'display: table-cell; width: 100%;',
      components: [{
        name: 'title',
        style: 'text-align: center; vertical-align: middle'
      }]
    }, {
      style: 'display: table-cell;',
      components: [{
        name: 'doneBrandButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'doneAction'
      }]
    }, {
      style: 'display: table-cell;',
      components: [{
        classes: 'btnlink-gray',
        name: 'cancelBrandButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'cancelAction'
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.doneBrandButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
    this.$.cancelBrandButton.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  },
  doneAction: function () {
    var selectedBrands = _.compact(this.parent.parent.parent.$.body.$.listBrands.brandsList.map(function (e) {
      return e;
    }));
    this.doSelectBrand({
      value: selectedBrands
    });
    this.doHideThisPopup();
  },
  cancelAction: function () {
    this.doHideThisPopup();
  }
}); /*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductBrand',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null
  },
  executeOnShow: function () {
    var i, j;
    this.$.header.parent.addStyles('padding: 0px; border-bottom: 1px solid #cccccc');
    this.$.header.$.modalProductBrandTopHeader.$.title.setContent(OB.I18N.getLabel('OBMOBC_LblBrand'));
    this.waterfall('onSearchAction');
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListBrands'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
    this.$.header.createComponent({
      kind: 'OB.UI.ModalProductBrandTopHeader',
      style: 'border-bottom: 0px'
    });
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});