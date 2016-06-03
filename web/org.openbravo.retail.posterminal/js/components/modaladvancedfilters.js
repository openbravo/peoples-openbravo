/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB, _ */

/* Advanced Filter Modal definition */
enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.AdvancedFilterClear',
  classes: 'btnlink-gray btnlink btnlink-small',
  style: 'width: 146px; ',
  events: {
    onClearAll: ''
  },
  tap: function () {
    this.doClearAll();
  },
  initComponents: function () {
    this.setContent(OB.I18N.getLabel('OBPOS_ClearAll'));
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.AdvancedFilterApply',
  classes: 'btnlink-yellow btnlink btnlink-small',
  style: 'width: 146px; ',
  events: {
    onApplyFilters: ''
  },
  tap: function () {
    this.doApplyFilters();
  },
  isDefaultAction: true,
  initComponents: function () {
    this.setContent(OB.I18N.getLabel('OBPOS_applyFilters'));
  }
});

enyo.kind({
  name: 'OB.UI.AdvancedFilterTable',
  kind: 'Scroller',
  maxHeight: '400px',
  style: 'width: 100%; background-color: #fff; ',
  horizontal: 'hidden',
  initComponents: function () {
    this.inherited(arguments);
    this.filters = [];
  },

  setSortNone: function () {
    _.each(this.filters, function (flt) {
      var button = flt.owner.$['order' + flt.filter.name],
          buttonClasses = button.getClassAttribute().split(' ');
      button.removeClass(buttonClasses[buttonClasses.length - 1]);
      button.addClass('iconSortNone');
    });
  },

  addFilter: function (filter) {
    var filterEditor;
    if (filter.isList) {
      filterEditor = {
        kind: 'OB.UI.FilterSelectorList',
        name: 'input' + filter.name,
        style: 'float: left; width: calc(100% - 120px); padding: 4px; height: 40px;'
      };
    } else if (filter.isAmount) {
      filterEditor = {
        kind: 'OB.UI.FilterSelectorAmount',
        name: 'input' + filter.name,
        style: 'float: left; width: calc(100% - 120px); padding: 0; height: 40px;'
      };
    } else if (filter.isSelector) {
      filterEditor = {
        kind: 'OB.UI.FilterSelectorButton',
        name: 'input' + filter.name,
        selectorPopup: filter.selectorPopup,
        filterName: filter.name,
        style: 'float: left; width: calc(100% - 120px); padding: 0; height: 40px;'
      };
    } else {
      filterEditor = {
        kind: 'OB.UI.FilterSelectorText',
        name: 'input' + filter.name,
        style: 'float: left; width: calc(100% - 130px); padding: 4px;'
      };
    }
    var filterLine = this.createComponent({
      filter: filter,
      style: 'width: 100%; clear:both; background-color: #fff; height: 40px; padding-top: 2px; padding-left: 13px; overflow: hidden;',
      components: [{
        style: 'float: left; width: 112px;  background-color: #e2e2e2; height: 29px; padding-top: 11px; padding-right: 5px; text-align: right;font-size: 16px; color: black; text-overflow: ellipsis; white-space: nowrap; overflow: hidden;',
        name: 'label' + filter.name,
        content: OB.I18N.getLabel(filter.caption)
      }, {
        style: 'float: left; text-align: left; padding-left: 2px; width: calc(100% - 125px);',
        name: 'filterEditor' + filter.name,
        components: [
        filterEditor,
        {
          name: 'deleteSpacer' + filter.name,
          style: 'width: 30px; height: 30px; float: left; margin: 5px 0px 5px 8px;'
        }, {
          kind: 'OB.UI.SmallButton',
          name: 'delete' + filter.name,
          classes: 'btnlink-white iconRemove',
          style: 'float: left; margin: 5px 0px 5px 8px;',
          showing: false,
          tap: function () {
            this.owner.$['input' + filter.name].setValue('');
            if (this.owner.$['input' + filter.name].kind === 'OB.UI.FilterSelectorText') {
              this.owner.$['filterEditor' + filter.name].hideRemove();
            }
          }
        }, {
          kind: 'OB.UI.SmallButton',
          name: 'order' + filter.name,
          classes: 'btnlink-white iconSortNone',
          style: 'float: left; margin: 5px 8px 5px 8px;',
          tap: function () {
            var buttonClasses = this.getClassAttribute().split(' '),
                buttonClass = buttonClasses[buttonClasses.length - 1];
            this.owner.setSortNone();
            this.addClass(buttonClass === 'iconSortAsc' ? 'iconSortDesc' : (buttonClass === 'iconSortDesc' ? 'iconSortNone' : 'iconSortAsc'));
          }
        }],
        hideRemove: function () {
          this.owner.$['delete' + filter.name].hide();
          this.owner.$['deleteSpacer' + filter.name].show();
        },
        showRemove: function () {
          this.owner.$['deleteSpacer' + filter.name].hide();
          this.owner.$['delete' + filter.name].show();
        }
      }]
    }, {
      owner: this
    });
    filterLine.render();
    if (filter.isList) {
      filterLine.owner.$['input' + filter.name].changeColumn(filter);
    }
    this.filters.push(filterLine);
  },

  clearAll: function () {
    var me = this;
    _.each(this.filters, function (flt) {
      if (flt.filter.filter) {
        if (flt.filter.preset) {
          flt.owner.$['input' + flt.filter.name].setPresetValue(flt.filter.preset);
        } else {
          flt.owner.$['input' + flt.filter.name].setValue('');
          flt.owner.$['filterEditor' + flt.filter.name].hideRemove();
        }
        flt.owner.$['input' + flt.filter.name].removeClass('error');
      }
    });
    this.owner.$.dateFormatError.hide();
  },

  applyFilters: function () {
    var me = this,
        filterError = false,
        result = {
        filters: [],
        orderby: null
        };

    me.owner.$.dateFormatError.hide();
    _.each(this.filters, function (flt) {
      var text = flt.owner.$['input' + flt.filter.name].getValue(),
          orderClasses = flt.owner.$['order' + flt.filter.name].getClassAttribute().split(' '),
          orderClass = orderClasses[orderClasses.length - 1],
          dateValidated;

      flt.owner.$['input' + flt.filter.name].removeClass('error');

      text = text ? text.trim() : '';
      if (text) {
        if (flt.filter.isAmount) {
          flt.filter.operator = flt.owner.$['input' + flt.filter.name].getOperator();
        }
        if (flt.filter.isDate) {
          dateValidated = OB.Utilities.Date.OBToJS(text, OB.Format.date) || OB.Utilities.Date.OBToJS(text, 'yyyy-MM-dd');
          if (dateValidated) {
            text = OB.Utilities.Date.JSToOB(dateValidated, 'yyyy-MM-dd');
            me.owner.$.dateFormatError.hide();
            flt.owner.$['input' + flt.filter.name].removeClass('error');
          } else {
            me.owner.$.dateFormatError.show();
            flt.owner.$['input' + flt.filter.name].addClass('error');
            filterError = true;
          }
        }
        result.filters.push({
          column: flt.filter.column,
          text: text
        });
      }
      if (orderClass !== 'iconSortNone') {
        result.orderby = {
          name: flt.filter.name,
          column: OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true) ? flt.filter.serverColumn : flt.filter.column,
          direction: orderClass === 'iconSortAsc' ? 'asc' : 'desc',
          isLocationFilter: flt.filter.location,
          isDate: flt.filter.isDate
        };
      }
    });
    if (filterError) {
      return false;
    } else {
      return result;
    }
  }

});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalAdvancedFilters',
  i18nHeader: 'OBPOS_LblAdvancedFilters',
  style: 'height: 600px',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    onChangeFilterSelector: 'changeFilterSelector',
    onClearAll: 'clearAll',
    onApplyFilters: 'applyFilters'
  },
  body: {
    components: [{
      style: 'height: 32px; color: black; font-size: 16px;',
      components: [{
        style: 'width: 50%; float: left; ',
        components: [{
          style: 'float: right; ',
          kind: 'OB.UI.AdvancedFilterClear',
          name: 'btnClear'
        }]
      }, {
        style: 'width: 50%; float: left;',
        components: [{
          kind: 'OB.UI.AdvancedFilterApply',
          name: 'btnApply'
        }]
      }]
    }, {
      style: 'height: 15px;'
    }, {
      style: 'width: 100%; float: left; text-align:center; ',
      name: 'dateFormatError',
      showing: false,
      initComponents: function () {
        this.setContent(enyo.format(OB.I18N.getLabel('OBPOS_DateFormatError'), OB.I18N.getDateFormatLabel()));
      }
    }, {
      kind: 'OB.UI.AdvancedFilterTable',
      name: 'filters'
    }]
  },

  changeFilterSelector: function (inSender, inEvent) {
    this.show();
    var selector = _.find(this.$.body.$.filters.filters, function (flt) {
      return flt.filter.name === inEvent.selector.name;
    }, this);
    selector.owner.$['input' + selector.filter.name].setSelectorValue(inEvent.selector.id, inEvent.selector.text);
  },

  clearAll: function () {
    this.$.body.$.filters.clearAll();
  },

  applyFilters: function () {
    var tmpFilters = this.$.body.$.filters.applyFilters();
    if (tmpFilters) {
      this.filtersToApply = tmpFilters;
      this.doHideThisPopup();
    }
  },

  executeOnShow: function () {
    if (this.args.callback) {
      this.callback = this.args.callback;
      _.each(this.$.body.$.filters.filters, function (filter) {
        if (filter.filter.preset) {
          filter.owner.$['input' + filter.filter.name].setPresetValue(filter.filter.preset);
        }
      }, this);
      if (this.args.lastFilters) {
        _.each(this.$.body.$.filters.filters, function (filter) {
          var lastFlt = _.find(this.args.lastFilters, function (last) {
            return last.column === filter.filter.column;
          });
          filter.owner.$['input' + filter.filter.name].setValue(lastFlt ? lastFlt.text : '');
        }, this);
      }
      this.filtersToApply = null;
    }
    return true;
  },

  executeOnHide: function () {
    if (!this.$.body.showSelector) {
      if (this.callback) {
        this.callback(this.filtersToApply);
      }
    } else {
      this.$.body.showSelector = false;
    }
  }

});