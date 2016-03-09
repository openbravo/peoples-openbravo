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
  kind: 'OB.UI.List',
  name: 'OB.UI.FilterSelectorList',
  classes: 'combo',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('name'));
    }
  }),
  renderEmpty: 'enyo.Control',
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
        addModel = value !== undefined;
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
  initComponents: function () {
    this.setCollection(new Backbone.Collection());
    this.getCollection().reset([]);
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
    onChangeColumn: 'changeColumn'
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
            style: 'display: table-cell; width: 35%;',
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
            }]
          }, {
            style: 'display: table-cell; width: 65%;',
            name: 'entitySearchContainer',
            components: [{
              kind: 'OB.UI.SearchInputAutoFilter',
              name: 'entityFilterText',
              style: 'width: 100%; margin-bottom: 0px;'
            }, {
              kind: 'OB.UI.FilterSelectorList',
              name: 'entityFilterList',
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
  changeColumn: function (inSender, inEvent) {
    var column = _.find(this.filters, function (flt) {
      return flt.column === inEvent.value;
    }, this);
    if (column) {
      this.$.entityFilterText.setShowing(!column.isList);
      this.$.entityFilterList.setShowing(column.isList);
      if (column.isList) {
        this.$.entityFilterList.changeColumn(column);
      }
    }
  },
  searchAction: function () {
    var me = this,
        text = this.$.entityFilterText.getValue();
    if (this.showFields) {
      var value = this.$.entityFilterColumn.getValue(),
          column = _.find(this.filters, function (flt) {
          return flt.column === value;
        }, this);
      if (text !== '' && column && column.isDate) {
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
    this.$.entityFilterList.setSelected(0);
    this.$.advancedFilterInfo.setShowing(false);
    this.$.dateFormatError.hide();
    this.$.filterInputs.setShowing(true);
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
  initComponents: function () {
    this.inherited(arguments);
    if (this.$.entityFilterColumn.collection.length > 0) {
      this.bubble('onChangeColumn', {
        value: this.$.entityFilterColumn.collection.at(0).id
      });
    }
  }
});