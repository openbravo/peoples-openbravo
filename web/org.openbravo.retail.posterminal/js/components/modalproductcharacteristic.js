/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListValuesLineCheck',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'width: 86%; text-align: left; padding-left: 70px;',
  events: {
    onAddToSelected: ''
  },
  tap: function () {
    this.inherited(arguments);
    var me = this;
    if (!this.parent.model.get('childrenSelected')) {
      this.removeClass('half-active');
    }
    this.doAddToSelected({
      value: me.parent.model,
      checked: !this.parent.model.get('checked'),
      selected: !this.parent.model.get('selected')
    });
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(this.parent.model.get('name'));
    if (this.parent.model.get('selected')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
      if (this.parent.model.get('childrenSelected')) {
        this.addClass('half-active');
      }
    }
  }
});
enyo.kind({
  name: 'OB.UI.ListValuesLineChildren',
  kind: 'OB.UI.Button',
  classes: 'btn-icon-inspectTree',
  style: 'width: 14%; margin: 0px; ',
  showing: false,
  childrenArray: [],
  events: {
    onSetCollection: ''
  },
  create: function () {
    this.inherited(arguments);
    if (this.parent.model.get('childrenList') && this.parent.model.get('childrenList').length > 0 && this.parent.model.get('showChildren')) {
      this.childrenArray = this.parent.model.get('childrenList');
      this.show();
    }
  },
  tap: function () {
    this.doSetCollection({
      value: this.childrenArray,
      parentValue: this.parent.model.get('id')
    });
  }
});
enyo.kind({
  name: 'OB.UI.ListValuesLine',
  style: 'border-bottom: 1px solid #cccccc',
  components: [{
    kind: 'OB.UI.ListValuesLineCheck'
  }, {
    kind: 'OB.UI.ListValuesLineChildren'
  }]
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListValues',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction',
    onSetCollection: 'setCollection'
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'valueslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderLine: 'OB.UI.ListValuesLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  productCharacteristicValueFilterQualifier: 'ProductCH_Filter',
  clearAction: function (inSender, inEvent) {
    this.valuesList.reset();
    this.initialValuesList = null;
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        i, j, whereClause = '',
        params = [],
        products = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.products,
        productCharacteristic = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.parent,
        forceRemote = false,
        theEvent = inEvent;
    var productFilterText, productCategory, productCharacteristicModel, resetValueList, characteristic = [];

    productFilterText = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.$.productFilterText.getValue();
    productCharacteristicModel = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.parent.model;

    productCharacteristic.customFilters.forEach(function (hqlFilter) {
      if (!_.isUndefined(hqlFilter.hqlCriteriaCharacteristicsValue) && !_.isUndefined(hqlFilter.forceRemote)) {
        var hqlCriteriaFilter = hqlFilter.hqlCriteriaCharacteristicsValue();
        if (!_.isUndefined(hqlCriteriaFilter)) {
          hqlCriteriaFilter.forEach(function (filter) {
            if (filter && forceRemote === false) {
              forceRemote = hqlFilter.forceRemote;
            }
          });
        }
      }
    });

    resetValueList = function (dataValues) {
      if (dataValues && dataValues.length > 0) {
        var modelsList, initialModelsList = dataValues.models;
        // Remove Characteristic Parent with No Child
        me.validateChildrenTree(initialModelsList, '0');
        initialModelsList = _.filter(initialModelsList, function (model) {
          return model.get('summaryLevel') ? model.get('hasChildren') : true;
        });
        // Set Children List
        _.each(initialModelsList, function (model) {
          if (model.get('summaryLevel')) {
            model.set('childrenList', _.filter(initialModelsList, function (childModel) {
              return childModel.get('parent') === model.get('id');
            }));
            model.set('showChildren', true);
          }
          model.unset('hasChildren');
        }, this);
        me.initialValuesList = initialModelsList;
        me.updateListSelection(initialModelsList);

        // Get First Parent
        modelsList = _.filter(initialModelsList, function (model) {
          return model.get('parent') === '0';
        });
        me.hasSelectedChildrenTree(modelsList);
        me.valuesList.reset(modelsList);
      } else {
        me.valuesList.reset();
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) || forceRemote) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NoCharacteriticValue', [me.parent.parent.characteristic.get('_identifier')]));
        }
      }
    };

    productCategory = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.getProductCategoryFilter(forceRemote);
    if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) && !forceRemote) {
      var sql, productsIdsList, chFilterQuery = "",
          num, brandStr;
      sql = "select distinct(id), name, characteristic_id, parent, summaryLevel from m_ch_value chv where  chv.characteristic_id = ?";
      sql += " and ((chv.summaryLevel = 'false' and (exists (select 1 from M_Product_Ch_Value mpchv, M_Product p where mpchv.M_Product_ID = p.M_Product_ID and chv.id = mpchv.m_ch_value_id";
      params.push(this.parent.parent.characteristic.get('id'));
      // brand filter
      if (productCharacteristicModel.get('brandFilter').length > 0) {
        num = 0;
        brandStr = "";
        for (i = 0; i < productCharacteristicModel.get('brandFilter').length; i++) {
          if (num >= 1) {
            brandStr += ',';
          }
          brandStr += "'" + productCharacteristicModel.get('brandFilter')[i].id + "'";
          num++;
        }
        sql += " and (p.brand in (" + brandStr + "))";
      }
      // product name and category filter
      if (productFilterText !== undefined && productCategory !== undefined) {
        if (productFilterText !== "" || productCategory !== "__all__" || productCategory !== "'__all__'") {
          params.push("%" + productFilterText + "%");
          if (productCategory === "OBPOS_bestsellercategory") {
            sql += " AND p.bestseller = 'true' AND ( Upper(p._filter) LIKE Upper(?) )";
          } else if ((productCategory === "__all__") || productCategory === "'__all__'" || (productCategory === "")) {
            sql += " AND (Upper(p._filter) LIKE Upper(?))";
          } else {
            sql += " AND (Upper(p._filter) LIKE Upper(?)) AND(p.m_product_category_id IN (" + productCategory + "))";
          }
        }
      }
      // characteristics filter
      if (me.parent.parent.model.get('filter').length > 0) {
        for (i = 0; i < me.parent.parent.model.get('filter').length; i++) {
          if (!characteristic.includes(me.parent.parent.model.get('filter')[i].characteristic_id)) {
            characteristic.push(me.parent.parent.model.get('filter')[i].characteristic_id);
          }
        }

        for (i = 0; i < characteristic.length; i++) {
          num = 0;
          var characteristicsValuesStr = "";
          for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
            if (characteristic[i] === me.parent.parent.model.get('filter')[j].characteristic_id) {
              if (num > 0) {
                characteristicsValuesStr += ',';
              }
              characteristicsValuesStr += "'" + me.parent.parent.model.get('filter')[j].id + "'";
              num++;
            }
          }
          sql += " and (exists (select 1 from M_Product_Ch_Value mpcharv where mpcharv.M_Product_ID = mpchv.M_Product_ID and mpcharv.m_ch_value_id in (" + characteristicsValuesStr + "))) ";
        }
      }
      sql += "))) or chv.summaryLevel = 'true')";
      //external modules filter
      var sqlCriteriaFilter = "";
      productCharacteristic.customFilters.forEach(function (sqlFilter) {
        if (!_.isUndefined(sqlFilter.sqlFilterQueryCharacteristics)) {
          var criteriaFilter = sqlFilter.sqlFilterQueryCharacteristicsValue();
          if (criteriaFilter.query !== null) {
            params = params.concat(criteriaFilter.filters);
            sqlCriteriaFilter += criteriaFilter.query;
          }
        }
      });
      sql = sql + sqlCriteriaFilter;
      sql = sql + ' order by UPPER(name) asc';
      OB.Dal.query(OB.Model.CharacteristicValue, sql, params, function (dataValues, me) {
        resetValueList(dataValues);
      }, function (tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }, this);
      return true;

    } else {

      var remoteCriteria = [],
          characteristicParams = "",
          brandparams = [],
          characteristicValue = [];
      var productFilter = {},
          criteria = {},
          brandfilter = {},
          chFilter = {},
          productText, characteristicfilter = {
          columns: ['characteristic_id'],
          operator: 'equals',
          value: this.parent.parent.characteristic.get('id'),
          isId: true
          };
      // brand filter
      if (productCharacteristicModel.get('brandFilter').length > 0) {
        for (i = 0; i < productCharacteristicModel.get('brandFilter').length; i++) {
          brandparams.push(productCharacteristicModel.get('brandFilter')[i].id);
        }
        if (brandparams.length > 0) {
          brandfilter = {
            columns: [],
            operator: OB.Dal.FILTER,
            value: 'BChV_Filter',
            params: [brandparams]
          };
          remoteCriteria.push(brandfilter);
        }
      }
      // product name and category filter
      if (productFilterText !== undefined || productCategory !== undefined) {
        // characteristic filter
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
            if (i > 0) {
              characteristicParams += ";";
            }
            characteristicParams += characteristicValue;
            characteristicValue = [];
          }
        }
        var productCat = inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.$.searchProductCharacteristicHeader.getSelectedCategories(),
            category = productCat.indexOf('OBPOS_bestsellercategory') >= 0 ? 'OBPOS_bestsellercategory' : (productCat.indexOf('__all__') >= 0 ? '__all__' : [productCategory.value]);
        productFilter.columns = [];
        productFilter.operator = OB.Dal.FILTER;
        productFilter.value = this.productCharacteristicValueFilterQualifier;
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          productText = (OB.MobileApp.model.hasPermission('OBPOS_remote.product' + OB.Dal.USESCONTAINS, true) ? '%' : '') + productFilterText + '%';
        } else {
          productText = '%' + productFilterText + '%';
        }
        productFilter.params = [productText, productCategory.filter ? productCategory.params[0] : category, characteristicParams, brandparams.join(',')];
        remoteCriteria.push(productFilter);
      }
      // external modules filter
      criteria.hqlCriteria = [];
      productCharacteristic.customFilters.forEach(function (hqlFilter) {
        if (!_.isUndefined(hqlFilter.hqlCriteriaCharacteristicsValue)) {
          var hqlCriteriaFilter = hqlFilter.hqlCriteriaCharacteristicsValue();
          if (!_.isUndefined(hqlCriteriaFilter)) {
            hqlCriteriaFilter.forEach(function (filter) {
              if (filter) {
                remoteCriteria.push(filter);
              }
            });
          }
        }
      });
      remoteCriteria.push(characteristicfilter);
      criteria.remoteFilters = remoteCriteria;
      criteria.forceRemote = forceRemote;
      OB.Dal.find(OB.Model.CharacteristicValue, criteria, function (dataValues) {
        resetValueList(dataValues);
      }, function (tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }, this);
      return true;
    }
  },
  parentValue: '0',
  setCollection: function (inSender, inEvent) {
    if (inEvent.parentValue !== '0') {
      this.parent.parent.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: visible');
    }
    this.parentValue = inEvent.parentValue;
    this.updateListSelection(inEvent.value);
    this.hasSelectedChildrenTree(inEvent.value);
    this.valuesList.reset(inEvent.value);
  },
  updateListSelection: function (modelsList) {
    var i, j, filterList = this.parent.parent.model.get('filter'),
        selectedList = this.parent.parent.selected;
    for (i = 0; i < modelsList.length; i++) {
      for (j = 0; j < filterList.length; j++) {
        if (modelsList[i].get('id') === filterList[j].id) {
          modelsList[i].set('checked', true);
          modelsList[i].set('selected', filterList[j].selected);
          if (modelsList[i].get('summaryLevel')) {
            modelsList[i].set('showChildren', false);
          }
          break;
        } else {
          modelsList[i].set('checked', false);
        }
      }
      for (j = 0; j < selectedList.length; j++) {
        if (modelsList[i].get('id') === selectedList[j].id) {
          modelsList[i].set('checked', selectedList[j].get('checked'));
          modelsList[i].set('selected', selectedList[j].get('selected'));
        }
      }
      modelsList[i].set('childrenSelected', null);
    }
  },
  hasSelectedChildrenTree: function (modelsList) {
    var i;
    if (!modelsList || modelsList.length === 0) {
      return false;
    }
    for (i = 0; i < modelsList.length; i++) {
      if (modelsList[i].get('summaryLevel')) {
        if (modelsList[i].get('selected') && modelsList[i].get('parent') !== this.parentValue) {
          return true;
        }
        if (this.hasSelectedChildrenTree(modelsList[i].get('childrenList'))) {
          modelsList[i].set('childrenSelected', true);
          return true;
        }
      } else if (modelsList[i].get('selected') && modelsList[i].get('parent') !== this.parentValue) {
        return true;
      }
    }
    return false;
  },
  validateChildrenTree: function (initialModelsList, parentValue) {
    var i, modelsList, exists = false;

    modelsList = _.filter(initialModelsList, function (model) {
      return model.get('parent') === parentValue;
    });
    if (!modelsList || modelsList.length === 0) {
      return false;
    }
    for (i = 0; i < modelsList.length; i++) {
      if (modelsList[i].get('summaryLevel')) {
        if (this.validateChildrenTree(initialModelsList, modelsList[i].get('id'))) {
          modelsList[i].set('hasChildren', true);
          exists = true;
        }
      } else {
        exists = true;
      }
    }
    return exists;
  },
  valuesList: null,
  init: function (model) {
    this.valuesList = new Backbone.Collection();
    this.$.valueslistitemprinter.setCollection(this.valuesList);
  }
});

enyo.kind({
  name: 'OB.UI.ModalProductChTopHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  style: '',
  events: {
    onHideThisPopup: '',
    onSelectCharacteristicValue: '',
    onGetPrevCollection: ''
  },
  components: [{
    style: 'display: table;  width: 100%;',
    components: [{
      style: 'display: table-cell; ',
      components: [{
        classes: 'btnlink-gray',
        name: 'backChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'backAction'
      }]
    }, {
      style: 'display: table-cell; width: 55%;',
      components: [{
        name: 'title',
        style: 'text-align: center; vertical-align: middle;'
      }]
    }, {
      style: 'display: table-cell; ',
      components: [{
        name: 'doneChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'doneAction'
      }]
    }, {
      style: 'display: table-cell;',
      components: [{
        classes: 'btnlink-gray',
        name: 'cancelChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'cancelAction'
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.backChButton.setContent(OB.I18N.getLabel('OBMOBC_LblBack'));
    this.$.backChButton.addStyles('visibility: hidden');
    this.$.doneChButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
    this.$.cancelChButton.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    this.selectedToSend = [];
    this.parent.parent.parent.selected = [];
  },
  backAction: function () {
    this.doGetPrevCollection();
  },
  doneAction: function () {
    var me = this;
    this.countingValues = this.countingValues + me.parent.parent.parent.selected.length;
    if (me.parent.parent.parent.selected.length > 0) {
      OB.UTIL.showLoading(true);
      this.inspectTree(me.parent.parent.parent.selected);
    } else {
      this.doHideThisPopup();
    }
  },
  cancelAction: function () {
    this.parent.parent.parent.selected = [];
    this.parent.parent.parent.countedValues = 0;
    this.doHideThisPopup();
  },
  checkFinished: function () {
    var me = this;
    if (this.parent.parent.parent.countedValues === this.countingValues) {
      this.doSelectCharacteristicValue({
        value: me.selectedToSend
      });
      this.parent.parent.parent.selected = [];
      this.selectedToSend = [];
      this.parent.parent.parent.countedValues = 0;
      this.countingValues = 0;
      this.doHideThisPopup();
      OB.UTIL.showLoading(false);
    }
  },
  countingValues: 0,
  inspectTree: function (selected, checkedParent) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.getChildren(selected, aux, checkedParent, this);
    }
  },
  getChildren: function (selected, aux, checkedParent, me) {
    var index, dataValues = selected[aux].get('childrenList');
    if (dataValues && dataValues.length > 0) {
      if (!_.isUndefined(checkedParent)) {
        selected[aux].set('checked', checkedParent);
      }
      index = me.selectedToSend.map(function (e) {
        return e.id;
      }).indexOf(selected[aux].id);
      if (index === -1) {
        me.selectedToSend.push(selected[aux]);
      } else if (!_.isNull(selected[aux].get('selected')) && !_.isUndefined(selected[aux].get('selected'))) {
        me.selectedToSend[index] = selected[aux];
      }
      if (!_.isUndefined(checkedParent)) {
        me.inspectTree(dataValues, checkedParent);
      } else {
        me.inspectTree(dataValues, selected[aux].get('checked'));
      }
    } else {
      if (!_.isUndefined(checkedParent)) {
        selected[aux].set('checked', checkedParent);
      }
      index = me.selectedToSend.map(function (e) {
        return e.id;
      }).indexOf(selected[aux].id);
      if (index === -1) {
        me.selectedToSend.push(selected[aux]);
      } else if (!_.isNull(selected[aux].get('selected')) && !_.isUndefined(selected[aux].get('selected'))) {
        me.selectedToSend[index] = selected[aux];
      }
      me.countingValues++;
      me.checkFinished();
    }
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductCharacteristic',
  topPosition: '170px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null,
    selected: []
  },
  events: {
    onAddToSelected: ''
  },
  handlers: {
    onAddToSelected: 'addToSelected',
    onGetPrevCollection: 'getPrevCollection'
  },
  executeOnShow: function () {
    this.$.body.$.listValues.parentValue = '0';
    this.$.header.parent.addStyles('padding: 0px; border-bottom: 1px solid #cccccc');
    this.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: hidden');
    this.characteristic = this.args.model;
    this.$.header.$.modalProductChTopHeader.$.title.setContent(this.args.model.get('_identifier'));
    this.waterfall('onSearchAction');
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListValues'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
    this.$.header.createComponent({
      kind: 'OB.UI.ModalProductChTopHeader',
      style: 'border-bottom: 0px'
    });
  },
  addToSelected: function (inSender, inEvent) {
    var index = this.selected.map(function (e) {
      return e.get('id');
    }).indexOf(inEvent.value.get('id'));
    if (!inEvent.checked) {
      inEvent.value.set('childrenSelected', false);
      this.inspectDeselectTree([inEvent.value], inEvent.value);
    }
    if (index !== -1) {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.selected);
      this.selected[index].set('checked', inEvent.checked);
      this.selected[index].set('selected', inEvent.selected);
    } else {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.selected);
      this.selected.push(inEvent.value);
      this.inspectCountTree([inEvent.value]);
      this.countedValues++;
    }

  },
  getPrevCollection: function (inSender, inEvent) {
    var me = this,
        dataValues;
    dataValues = _.filter(this.$.body.$.listValues.initialValuesList, function (model) {
      return model.get('parent') === _.find(me.$.body.$.listValues.initialValuesList, function (l) {
        return l.get('id') === me.$.body.$.listValues.parentValue;
      }).get('parent');
    }, this);

    if (dataValues && dataValues.length > 0) {
      //We take the first to know the parent
      this.$.body.$.listValues.parentValue = dataValues[0].get('parent');
      this.$.body.$.listValues.updateListSelection(dataValues);
      this.$.body.$.listValues.hasSelectedChildrenTree(dataValues);
      this.$.body.$.listValues.valuesList.reset(dataValues);
    } else {
      this.$.body.$.listValues.parentValue = '0';
      this.$.body.$.listValues.valuesList.reset();
    }
    if (this.$.body.$.listValues.parentValue === '0') { //root
      this.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: hidden');
    }
  },
  countedValues: 0,
  inspectCountTree: function (selected) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.countChildren(selected, aux, this);
    }
  },
  countChildren: function (selected, aux, me) {
    var dataValues = selected[aux].get('childrenList');
    if (dataValues && dataValues.length > 0) {
      me.inspectCountTree(dataValues);
    } else {
      me.countedValues++;
    }
  },
  inspectDeselectTree: function (selected, rootObject) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.deselectChildren(selected, aux, rootObject, this);
    }
  },
  deselectChildren: function (selected, aux, rootObject, me) {
    var index, dataValues = selected[aux].get('childrenList');
    index = me.selected.map(function (e) {
      return e.id;
    }).indexOf(selected[aux].id);
    if (!rootObject.get('selected') && rootObject.get('id') !== selected[aux].get('id')) {
      selected[aux].set('selected', rootObject.get('selected'));
      if (index === -1) {
        me.doAddToSelected({
          value: selected[aux],
          checked: selected[aux].get('checked'),
          selected: selected[aux].get('selected')
        });
      } else {
        me.selected[index] = selected[aux];
      }
    }
    if (dataValues && dataValues.length > 0) {
      me.inspectDeselectTree(dataValues, rootObject);
    }
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});