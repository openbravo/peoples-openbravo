/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
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
  brandValueFilterQualifier: 'PBrand_Filter',
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
    var criteria = {
      '_orderBy': [{
        'column': 'name',
        'asc': true
      }]
    };
    var products = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.products,
        productFilterText = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.$.productFilterText.getValue(),
        characteristic = [],
        productCategory, forceRemote = false,
        productCharacteristic = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.parent;
    productCharacteristic.customFilters.forEach(function (hqlFilter) {
      if (!_.isUndefined(hqlFilter.hqlCriteriaBrand) && !_.isUndefined(hqlFilter.forceRemote)) {
        var hqlCriteriaFilter = hqlFilter.hqlCriteriaBrand();
        hqlCriteriaFilter.forEach(function (filter) {
          if (filter !== "" && forceRemote === false) {
            forceRemote = hqlFilter.forceRemote;
          }
        });
      }
    });
    productCategory = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.getProductCategoryFilter(forceRemote);
    if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) && !forceRemote) {
      var BFilterByCH_Filter = "",
          brandValueFilter = "",
          params = [],
          sql = "select distinct(b.m_product_id),b.name,b._identifier,b._filter,b._idx from m_brand b left join m_product p on p.brand=b.m_product_id where 1=1 ",
          sqlCriteriaFilter = "",
          orderby = "order by b._identifier asc";
      if (productFilterText !== "" || productCategory !== "__all__" || productCategory !== "'__all__'") {
        params.push("%" + productFilterText + "%");
        if (productCategory === "OBPOS_bestsellercategory") {
          brandValueFilter += " and p.bestseller = 'true' AND ( Upper(p._filter) LIKE Upper(?)) ";
        } else if ((productCategory === "__all__") || (productCategory === "'__all__'") || (productCategory === "")) {
          brandValueFilter += " and (Upper(p._filter) LIKE Upper(?)) ";
        } else {
          brandValueFilter += " and  (Upper(p._filter) LIKE Upper(?)) AND(p.m_product_category_id IN (" + productCategory + ")) ";
        }
      }
      if (me.parent.parent.model.get('filter').length > 0) {
        for (i = 0; i < me.parent.parent.model.get('filter').length; i++) {
          if (!characteristic.includes(me.parent.parent.model.get('filter')[i].characteristic_id)) {
            characteristic.push(me.parent.parent.model.get('filter')[i].characteristic_id);
          }
        }
        for (i = 0; i < characteristic.length; i++) {
          var characteristicsValuesStr = "",
              num = 0;
          for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
            if (characteristic[i] === me.parent.parent.model.get('filter')[j].characteristic_id) {
              if (num > 0) {
                characteristicsValuesStr += ',';
              }
              characteristicsValuesStr += "'" + me.parent.parent.model.get('filter')[j].id + "'";
              num++;
            }
          }
          BFilterByCH_Filter += " and (exists (select 1 from M_Product_Ch_Value pchv where p.m_product_id= pchv.m_product_id and pchv.m_ch_value_id in (" + characteristicsValuesStr + "))) ";
        }
      }
      productCharacteristic.customFilters.forEach(function (sqlFilter) {
        if (!_.isUndefined(sqlFilter.sqlFilterQueryCharacteristics)) {
          var criteriaFilter = sqlFilter.sqlFilterQueryBrand();
          if (criteriaFilter.query !== null) {
            params = params.concat(criteriaFilter.filters);
            sqlCriteriaFilter += criteriaFilter.query;
          }
        }
      });
      sql = sql + brandValueFilter + BFilterByCH_Filter + sqlCriteriaFilter + orderby;
      OB.Dal.query(OB.Model.Brand, sql, params, successCallbackBrands, errorCallback, this);
    } else {
      var remoteCriteria = [],
          characteristicValue = [],
          brandfilter = {},
          chFilter = {},
          productText;
      criteria = {};
      if (products.collection.length > 0) {
        if (productFilterText !== "" || productCategory.value !== "__all__") {
          var productCat = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.getSelectedCategories(),
              category = productCat.indexOf('OBPOS_bestsellercategory') >= 0 ? 'OBPOS_bestsellercategory' : (productCat.indexOf('__all__') >= 0 ? '__all__' : [productCategory.value]);
          brandfilter.columns = [];
          brandfilter.operator = OB.Dal.FILTER;
          brandfilter.value = this.brandValueFilterQualifier;
          productText = (OB.MobileApp.model.hasPermission('OBPOS_remote.product' + OB.Dal.USESCONTAINS, true) ? '%' : '') + productFilterText + '%';
          brandfilter.params = [productText, productCategory.filter ? productCategory.params[0] : category];
          remoteCriteria.push(brandfilter);
        }
      }
      if (me.parent.parent.model.get('filter').length > 0) {
        for (i = 0; i < me.parent.parent.model.get('filter').length; i++) {
          if (!characteristic.includes(me.parent.parent.model.get('filter')[i].characteristic_id)) {
            characteristic.push(me.parent.parent.model.get('filter')[i].characteristic_id);
          }
        }
        for (i = 0; i < characteristic.length; i++) {
          for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
            if (characteristic[i] === me.parent.parent.model.get('filter')[j].characteristic_id) {
              characteristicValue.push(me.parent.parent.model.get('filter')[j].id);
            }
          }
          if (characteristicValue.length > 0) {
            chFilter = {
              columns: [],
              operator: OB.Dal.FILTER,
              value: 'BFilterByCH_Filter',
              filter: characteristic[i],
              params: [characteristicValue]
            };
            remoteCriteria.push(chFilter);
            characteristicValue = [];
          }
        }
      }
      criteria.hqlCriteria = [];
      productCharacteristic.customFilters.forEach(function (hqlFilter) {
        if (!_.isUndefined(hqlFilter.hqlCriteriaBrand)) {
          var hqlCriteriaFilter = hqlFilter.hqlCriteriaBrand();
          if (!_.isUndefined(hqlCriteriaFilter)) {
            hqlCriteriaFilter.forEach(function (filter) {
              if (filter) {
                remoteCriteria.push(filter);
              }
            });
          }
        }
      });
      criteria.remoteFilters = remoteCriteria;
      criteria.forceRemote = forceRemote;
      OB.Dal.find(OB.Model.Brand, criteria, successCallbackBrands, errorCallback);
    }
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
  topPosition: '170px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null
  },
  executeOnShow: function () {
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