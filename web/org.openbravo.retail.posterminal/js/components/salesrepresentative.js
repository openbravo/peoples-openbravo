/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */


enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.SalesRepresentative',
  classes: 'btnlink btnlink-small btnlink-gray',
  style: 'float:left; margin:7px; height:27px; padding: 4px 15px 7px 15px;',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalsalesrepresentative'
      });
    }
  },
  init: function (model) {
    if (!OB.POS.modelterminal.hasPermission(this.permission)) {
      this.parent.parent.parent.hide();
    } else {
      if (!OB.POS.modelterminal.hasPermission(this.permissionOption)) {
        this.parent.parent.parent.hide();
      }
    }
    this.setOrder(model.get('order'));
  },
  renderSalesRepresentative: function (newSalesRepresentative) {
    this.setContent(newSalesRepresentative);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('salesRepresentative')) {
      this.renderSalesRepresentative(this.order.get('salesRepresentative'));
    } else {
      this.renderSalesRepresentative('');
    }

    this.order.on('change:salesRepresentative$_identifier change:salesRepresentative', function (model) {
      if (!_.isUndefined(model.get('salesRepresentative$_identifier')) && !_.isNull(model.get('salesRepresentative$_identifier'))) {
        this.renderSalesRepresentative(model.get('salesRepresentative$_identifier'));
      } else {
        this.renderSalesRepresentative('');
      }
    }, this);
  }
});

/*Modal*/

enyo.kind({
  name: 'OB.UI.ModalSrScrollableHeader',
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
      srName: this.$.filterText.getValue()
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListSrsLine',
  kind: 'OB.UI.SelectButton',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      name: 'name'
    }, {
      style: 'clear: both;'
    }]
  }],
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListSrs',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeSalesRepresentative: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;',
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'srslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalSrScrollableHeader',
          renderLine: 'OB.UI.ListSrsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.srsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        filter = inEvent.srName;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPs(dataSrs) {
      if (dataSrs && dataSrs.length > 0) {
        me.srsList.reset(dataSrs.models);
      } else {
        me.srsList.reset();
      }
    }

    var criteria = {};
    if (filter && filter !== '') {
      criteria._identifier = {
        operator: OB.Dal.CONTAINS,
        value: filter
      };
    }

    OB.Dal.find(OB.Model.SalesRepresentative, criteria, successCallbackBPs, errorCallback);
    return true;
  },
  srsList: null,
  init: function (model) {
    this.srsList = new Backbone.Collection();
    this.$.srslistitemprinter.setCollection(this.srsList);
    this.srsList.on('click', function (model) {
      this.doChangeSalesRepresentative({
        salesRepresentative: model
      });
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalSalesRepresentative',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnHide: function () {
    this.$.body.$.listSrs.$.srslistitemprinter.$.theader.$.modalSrScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignSalesRepresentative',
  body: {
    kind: 'OB.UI.ListSrs'
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});