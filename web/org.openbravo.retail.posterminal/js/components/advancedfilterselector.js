/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB, _ */

enyo.kind({
  name: 'OB.UI.FilterSelectorAmount',
  components: [{
    kind: 'OB.UI.List',
    name: 'filterCondition',
    classes: 'combo',
    style: 'float: left; width: calc(50% - 8px); padding: 4px; margin-right: 16px; margin-bottom: 0px;',
    renderEmpty: 'enyo.Control',
    renderLine: enyo.kind({
      kind: 'enyo.Option',
      initComponents: function () {
        this.inherited(arguments);
        this.setValue(this.model.get('id'));
        this.setContent(this.model.get('name'));
      }
    })
  }, {
    kind: 'enyo.Input',
    type: 'text',
    classes: 'input narrow-input',
    name: 'filterInput',
    style: 'float: left; width: calc(50% - 18px); padding: 4px; margin-bottom: 0px;',
    handlers: {
      onkeyup: 'keyup'
    },
    keyup: function (inSender, inEvent) {
      if (this.hasRemoveButton) {
        var value = this.getValue();
        if (value === '') {
          this.owner.parent.hideRemove();
        } else {
          this.owner.parent.showRemove();
        }
      }
    }
  }],
  getValue: function () {
    return this.$.filterInput.getValue();
  },
  setValue: function (value) {
    this.$.filterInput.setValue(value);
    if (this.hasRemoveButton) {
      if (value === '') {
        this.parent.hideRemove();
      } else {
        this.parent.showRemove();
      }
    }
  },
  setPresetValue: function (preset) {
    this.$.filterCondition.setSelectedValue(preset.id, 'id');
    this.setValue(preset.name);
  },
  getOperator: function () {
    return this.$.filterCondition.getValue();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.filterCondition.setCollection(new Backbone.Collection());
    this.$.filterCondition.getCollection().reset([{
      id: 'greaterThan',
      name: OB.I18N.getLabel('OBMOBC_FilterQueryBuilderMoreThan')
    }, {
      id: 'lessThan',
      name: OB.I18N.getLabel('OBMOBC_FilterQueryBuilderLessThan')
    }, {
      id: 'equals',
      name: OB.I18N.getLabel('OBMOBC_FilterQueryBuilderEquals')
    }, {
      id: 'notEquals',
      name: OB.I18N.getLabel('OBMOBC_FilterQueryBuilderNotEquals')
    }]);
  }

});

enyo.kind({
  name: 'OB.UI.FilterSelectorButton',
  components: [{
    kind: 'OB.UI.SmallButton',
    name: 'filterButton',
    classes: 'btnlink-gray obrcifp-btn',
    tap: function () {
      this.owner.owner.owner.showSelector = true;
      this.owner.showSelector = true;
      this.bubble('onHideThisPopup');
      this.bubble('onShowPopup', {
        popup: this.owner.selectorPopup,
        args: {
          target: 'filterSelectorButton_' + this.owner.filterName,
          filterName: this.owner.filterName
        }
      });
    }
  }],
  getValue: function () {
    return this.id;
  },
  setValue: function (value) {
    this.id = value;
    if (value === '') {
      this.$.filterButton.setContent(' --- ');
    }
    if (this.hasRemoveButton) {
      if (value === '') {
        this.parent.hideRemove();
      } else {
        this.parent.showRemove();
      }
    }
  },
  setSelectorValue: function (id, text) {
    this.setValue(id);
    this.$.filterButton.setContent(text);
  },
  setPresetValue: function (preset) {
    this.setSelectorValue(preset.id, preset.name);
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.filterButton.setContent(' --- ');
  }
});

enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.FilterSelectorList',
  classes: 'combo',
  handlers: {
    onchange: 'change'
  },
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('name'));
    }
  }),
  renderEmpty: 'enyo.Control',
  change: function () {
    var value = this.getValue();
    if (this.hasRemoveButton) {
      if (value === '') {
        this.parent.hideRemove();
      } else {
        this.parent.showRemove();
      }
    }
  },
  changeColumn: function (column) {
    if (column.idList && !OB.MobileApp.model.get(column.termProperty)) {
      var me = this;
      new OB.DS.Request('org.openbravo.retail.posterminal.term.ListReference').exec({
        language: OB.Application.language_string,
        reference: column.idList
      }, function (data) {
        // data is ready. Save it
        OB.MobileApp.model.set(column.termProperty, data);
        me.loadList(column);
      });
    } else {
      this.loadList(column);
    }
  },
  loadList: function (column) {
    var models = OB.MobileApp.model.get(column.termProperty),
        columns = [];
    columns.push({
      id: '',
      name: ''
    });
    _.each(models, function (model) {
      var addModel = true;
      if (column.showValues && column.showValues.length > 0) {
        var value = _.find(column.showValues, function (val) {
          return model[column.propertyId] === val;
        });
        addModel = addModel && value === undefined;
      }
      if (addModel) {
        columns.push({
          id: model[column.propertyId],
          name: model[column.propertyName]
        });
      }
    });
    this.getCollection().reset(columns);
  },
  setValue: function (value) {
    this.setSelectedValue(value, 'id');
    if (this.hasRemoveButton) {
      if (value === '') {
        this.parent.hideRemove();
      } else {
        this.parent.showRemove();
      }
    }
  },
  setPresetValue: function (preset) {
    this.setValue(preset.id);
  },
  initComponents: function () {
    this.setCollection(new Backbone.Collection());
    this.getCollection().reset([]);
  }
});

enyo.kind({
  name: 'OB.UI.FilterSelectorText',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input narrow-input',
  handlers: {
    onkeyup: 'keyup'
  },
  setPresetValue: function (preset) {
    this.setValue(preset.name);
    this.keyup();
  },
  keyup: function (inSender, inEvent) {
    var value = this.getValue();
    if (this.hasRemoveButton) {
      if (value === '') {
        this.parent.hideRemove();
      } else {
        this.parent.showRemove();
      }
    }
  }
});

enyo.kind({
  name: 'OB.UI.FilterSelectorTableHeader',
  published: {
    filters: null,
    showFields: true
  },
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction',
    onChangeColumn: 'changeColumn',
    onChangeFilterSelector: 'changeFilterSelector'
  },
  components: [{
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%; vertical-align: middle; ',
        components: [{
          style: 'width: 100%;',
          name: 'advancedFilterInfo',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_AdvancedFiltersApplied'));
          }
        }, {
          style: 'display: table; width: 100%;',
          name: 'filterInputs',
          components: [{
            style: 'display: table-cell; width: 35%; vertical-align: top',
            name: 'entityFilterColumnContainer',
            components: [{
              kind: 'OB.UI.List',
              name: 'entityFilterColumn',
              classes: 'combo',
              style: 'width: 95%; white-space: nowrap; margin-bottom: 0px',
              handlers: {
                onchange: 'changeColumn'
              },
              renderLine: enyo.kind({
                kind: 'enyo.Option',
                initComponents: function () {
                  this.inherited(arguments);
                  this.setValue(this.model.get('id'));
                  this.setContent(this.model.get('name'));
                }
              }),
              renderEmpty: 'enyo.Control',
              changeColumn: function () {
                this.owner.$.entityFilterText.removeClass('error');
                this.owner.$.dateFormatError.hide();
                this.owner.$.entityFilterText.setValue('');
                this.owner.doClearAction();
                this.bubble('onChangeColumn', {
                  value: this.getValue()
                });
              },
              initComponents: function () {
                var columns = [];
                _.each(this.owner.filters, function (prop) {
                  if (prop.filter) {
                    columns.push({
                      id: prop.column,
                      name: OB.I18N.getLabel(prop.caption)
                    });
                  }
                });
                this.setCollection(new Backbone.Collection());
                this.getCollection().reset(columns);
              }
            }, {
              name: 'entityFilterLabel',
              style: 'text-align: right; padding: 10px 10px;',
              showing: false
            }]
          }, {
            style: 'display: table-cell; width: 65%; vertical-align: top',
            name: 'entitySearchContainer',
            components: [{
              kind: 'OB.UI.SearchInputAutoFilter',
              name: 'entityFilterText',
              hasRemoveButton: false,
              style: 'width: 100%; margin-bottom: 0px;'
            }, {
              kind: 'OB.UI.FilterSelectorList',
              name: 'entityFilterList',
              hasRemoveButton: false,
              style: 'width: 100%; margin-bottom: 0px'
            }, {
              kind: 'OB.UI.FilterSelectorAmount',
              name: 'entityFilterAmount',
              hasRemoveButton: false,
              style: 'width: 100%; margin-bottom: 0px'
            }, {
              kind: 'OB.UI.FilterSelectorButton',
              name: 'entityFilterButton',
              hasRemoveButton: false,
              style: 'width: 100%; margin-bottom: 0px'
            }]
          }]
        }, {
          style: 'padding-left: 10px',
          name: 'dateFormatError',
          showing: false,
          initComponents: function () {
            this.setContent(enyo.format(OB.I18N.getLabel('OBPOS_DateFormatError'), OB.I18N.getDateFormatLabel()));
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 50px; margin: 4px 5px 4px 27px;',
          tap: function () {
            this.owner.clearFilter();
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'entitySearchBtn',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 50px; margin: 4px 0px 4px 5px;',
          tap: function () {
            this.owner.searchAction();
          },
          putDisabled: function (status) {
            if (status === false) {
              this.setDisabled(false);
              this.removeClass('disabled');
              this.disabled = false;
              return;
            } else {
              this.setDisabled(true);
              this.addClass('disabled');
              this.disabled = true;
            }
          }
        }]
      }]
    }]
  }],
  changeFilterSelector: function (inSender, inEvent) {
    if (this.$.entityFilterButton.showSelector) {
      this.$.entityFilterButton.showSelector = false;
      this.$.entityFilterButton.setSelectorValue(inEvent.selector.value, inEvent.selector.text);
      this.owner.owner.owner.owner.owner.owner.show();
    }
  },
  changeColumn: function (inSender, inEvent) {
    var column = _.find(this.filters, function (flt) {
      return flt.column === inEvent.value;
    }, this);
    if (column) {
      this.$.entityFilterText.setShowing(!column.isList && !column.isAmount && !column.isSelector);
      this.$.entityFilterList.setShowing(column.isList === true);
      this.$.entityFilterAmount.setShowing(column.isAmount === true);
      this.$.entityFilterButton.setShowing(column.isSelector === true);
      if (column.isList) {
        this.$.entityFilterList.changeColumn(column);
      }
      if (column.isSelector) {
        this.$.entityFilterButton.selectorPopup = column.selectorPopup;
        this.$.entityFilterButton.filterName = column.name;
      }
    }
  },
  searchAction: function () {
    var me = this,
        text, value = this.$.entityFilterColumn.getValue(),
        column = _.find(this.filters, function (flt) {
        return flt.column === value;
      }, this);

    if (column.isList) {
      text = this.$.entityFilterList.getValue();
    } else if (column.isAmount) {
      column.operator = this.$.entityFilterAmount.getOperator();
      text = this.$.entityFilterAmount.getValue();
    } else if (column.isSelector) {
      text = this.$.entityFilterButton.getValue();
    } else {
      text = this.$.entityFilterText.getValue();
    }

    if (this.showFields && text !== '' && column && column.isDate) {
      var dateValidated = OB.Utilities.Date.OBToJS(text, OB.Format.date) || OB.Utilities.Date.OBToJS(text, 'yyyy-MM-dd');
      if (dateValidated) {
        text = OB.Utilities.Date.JSToOB(dateValidated, 'yyyy-MM-dd');
        me.$.dateFormatError.hide();
        this.$.entityFilterText.removeClass('error');
      } else {
        me.$.dateFormatError.show();
        this.$.entityFilterText.addClass('error');
        return;
      }
    }
    var filters = [{
      column: this.showFields ? this.$.entityFilterColumn.getValue() : '_filter',
      text: text
    }];
    this.lastFilters = filters;
    this.doSearchAction({
      filters: filters,
      advanced: false
    });
  },
  clearFilter: function () {
    this.$.entityFilterText.setValue('');
    this.$.entityFilterText.removeClass('error');
    this.$.entityFilterList.setValue('');
    this.$.entityFilterAmount.setValue('');
    this.$.entityFilterButton.setValue('');
    this.$.advancedFilterInfo.setShowing(false);
    this.$.dateFormatError.hide();
    this.$.filterInputs.setShowing(this.showFields);
    this.$.entitySearchBtn.putDisabled(false);
    this.doClearAction();
  },
  setAdvancedSearch: function (isAdvanced) {
    this.$.advancedFilterInfo.setShowing(isAdvanced);
    this.$.filterInputs.setShowing(!isAdvanced);
    this.$.entitySearchBtn.putDisabled(isAdvanced);
    if (isAdvanced) {
      this.lastFilters = null;
    }
  },
  hideFilterCombo: function () {
    this.showFields = false;
    this.$.entityFilterColumnContainer.setStyle('display: none');
    this.$.entitySearchContainer.setStyle('display: table-cell; width: 425px;');
  },
  fixColumn: function (column) {
    this.$.entityFilterColumn.hide();
    this.$.entityFilterLabel.show();
    this.$.entityFilterLabel.setContent(OB.I18N.getLabel(column.caption));
    this.changeColumn(null, {
      value: column.column
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    var fixed = _.find(this.filters, function (filter) {
      return filter.isFixed;
    });
    if (fixed) {
      this.fixColumn(fixed);
    } else if (this.$.entityFilterColumn.collection.length > 0) {
      this.bubble('onChangeColumn', {
        value: this.$.entityFilterColumn.collection.at(0).id
      });
    }
  }
});