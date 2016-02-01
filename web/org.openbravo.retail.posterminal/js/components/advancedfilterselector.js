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
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          style: 'width: 100%;',
          name: 'advancedFilterInfo',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_AdvancedFiltersApplied'));
          }
        }, {
          style: 'width: 100%;',
          name: 'filterInputs',
          components: [{
            style: 'display: table-cell; width: 150px;',
            name: 'customerFilterColumnContainer',
            components: [{
              kind: 'OB.UI.List',
              name: 'customerFilterColumn',
              classes: 'combo',
              style: 'width: 95%',
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
                this.owner.doClearAction();
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
            style: 'display: table-cell; width: 275px;',
            name: 'customerSearchContainer',
            components: [{
              kind: 'OB.UI.SearchInputAutoFilter',
              name: 'customerFilterText',
              style: 'width: 100%'
            }]
          }]
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 40px; margin: 0px 5px 8px 19px;',
          tap: function () {
            this.owner.clearFilter();
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'customerSearchBtn',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 40px; margin: 0px 0px 8px 5px;',
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
  searchAction: function () {
    var text = this.$.customerFilterText.getValue();
    if (this.showFields) {
      var value = this.$.customerFilterColumn.getValue(),
          column = _.find(this.filters, function (flt) {
          return flt.column === value;
        }, this);
      if (column && column.isDate) {
        var dateValidated = OB.Utilities.Date.OBToJS(text, OB.Format.date) || OB.Utilities.Date.OBToJS(text, 'yyyy-MM-dd');
        if (dateValidated) {
          text = OB.Utilities.Date.JSToOB(dateValidated, 'yyyy-MM-dd');
          this.$.customerFilterText.removeClass('error');
        } else {
          OB.UTIL.showError(enyo.format(OB.I18N.getLabel('OBPOS_DateFormatError'), OB.Format.date));
          this.$.customerFilterText.addClass('error');
          return;
        }
      }
    }
    var filters = [{
      column: this.showFields ? this.$.customerFilterColumn.getValue() : '_filter',
      text: text
    }];
    this.lastFilters = filters;
    this.doSearchAction({
      filters: filters,
      advanced: false
    });
  },
  clearFilter: function () {
    this.$.customerFilterText.setValue('');
    this.$.customerFilterText.removeClass('error');
    this.$.advancedFilterInfo.setShowing(false);
    this.$.filterInputs.setShowing(true);
    this.$.customerSearchBtn.putDisabled(false);
    this.doClearAction();
  },
  setAdvancedSearch: function (isAdvanced) {
    this.$.advancedFilterInfo.setShowing(isAdvanced);
    this.$.filterInputs.setShowing(!isAdvanced);
    this.$.customerSearchBtn.putDisabled(isAdvanced);
    if (isAdvanced) {
      this.lastFilters = null;
    }
  },
  hideFilterCombo: function () {
    this.showFields = false;
    this.$.customerFilterColumnContainer.setStyle('display: none');
    this.$.customerSearchContainer.setStyle('display: table-cell; width: 425px;');
  }
});