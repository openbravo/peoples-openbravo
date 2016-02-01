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
  style: 'width: 100%; background-color: #fff; overflow: auto',

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
    var filterLine = this.createComponent({
      filter: filter,
      style: 'width: 100%; clear:both; background-color: #fff; height: 32px; padding-top: 2px; overflow: hidden;',
      components: [{
        style: 'float: left; width: 30%;  background-color: #e2e2e2; height: 25px; padding-top: 6px; padding-right: 5px; text-align: right;font-size: 16px; color: black',
        name: 'label' + filter.name,
        content: OB.I18N.getLabel(filter.caption)
      }, {
        style: 'float: left; width: 65%; text-align: left; padding-left: 5px;',
        components: [{
          kind: 'enyo.Input',
          type: 'text',
          classes: 'input',
          name: 'input' + filter.name,
          style: 'float: left; width: 69%; padding: 0px;'
        }, {
          kind: 'OB.UI.SmallButton',
          name: 'order' + filter.name,
          classes: 'btnlink-white iconSortNone',
          style: 'float: left; margin-top: 1px',
          tap: function () {
            var buttonClasses = this.getClassAttribute().split(' '),
                buttonClass = buttonClasses[buttonClasses.length - 1];
            this.owner.setSortNone();
            this.addClass(buttonClass === 'iconSortAsc' ? 'iconSortDesc' : (buttonClass === 'iconSortDesc' ? 'iconSortNone' : 'iconSortAsc'));
          }
        }]
      }]
    });
    filterLine.render();
    this.filters.push(filterLine);
  },

  clearAll: function () {
    _.each(this.filters, function (flt) {
      flt.owner.$['input' + flt.filter.name].setValue('');
    });
  },

  applyFilters: function () {
    var result = {
      filters: [],
      orderby: null
    };
    _.each(this.filters, function (flt) {
      var text = flt.owner.$['input' + flt.filter.name].getValue(),
          orderClasses = flt.owner.$['order' + flt.filter.name].getClassAttribute().split(' '),
          orderClass = orderClasses[orderClasses.length - 1];
      text = text ? text.trim() : '';
      if (text) {
        result.filters.push({
          column: flt.filter.column,
          text: text
        });
      }
      if (orderClass !== 'iconSortNone') {
        result.orderby = {
          name: flt.filter.name,
          column: flt.filter.serverColumn,
          direction: orderClass === 'iconSortAsc' ? 'asc' : 'desc',
          isLocationFilter: flt.filter.location
        };
      }
    });
    return result;
  }

});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalAdvancedFilters',
  topPosition: '125px',
  i18nHeader: 'OBPOS_LblAdvancedFilters',
  style: 'width: 400px',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
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
      kind: 'OB.UI.AdvancedFilterTable',
      name: 'filters'
    }]
  },

  clearAll: function () {
    this.$.body.$.filters.clearAll();
  },

  applyFilters: function () {
    this.filtersToApply = this.$.body.$.filters.applyFilters();
    this.doHideThisPopup();
  },

  executeOnShow: function () {
    if (this.args.lastFilters) {
      _.each(this.$.body.$.filters.filters, function (filter) {
        var lastFlt = _.find(this.args.lastFilters, function (last) {
          return last.column === filter.filter.column;
        });
        filter.owner.$['input' + filter.filter.name].setValue(lastFlt ? lastFlt.text : '');
      }, this);
    }
    this.filtersToApply = null;
    return true;
  },

  executeOnHide: function () {
    if (this.args.callback) {
      this.args.callback(this.filtersToApply);
    }
  }

});