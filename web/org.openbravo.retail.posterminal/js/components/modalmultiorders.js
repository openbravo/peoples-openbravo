/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, moment */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalMultiOrdersHeader',
  kind: 'OB.UI.ModalPRScrollableHeader',
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
          style: 'width: 100%',
          skipAutoFilterPref: 'OBPOS_remote.order',
          isFirstFocus: true
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'clearButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'searchButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }, {
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblStartDate'));
          },
          style: 'width: 200px;  margin: 0px 0px 2px 5px;'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblEndDate'));
          },
          style: 'width 200px; margin: 0px 0px 2px 65px;'
        }]
      }]
    }, {
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'enyo.Input',
          name: 'startDate',
          size: '10',
          type: 'text',
          style: 'width: 100px;  margin: 0px 0px 8px 5px;',
          onchange: 'searchAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getDateFormatLabel());
          },
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }, {
        kind: 'enyo.Input',
        name: 'endDate',
        size: '10',
        type: 'text',
        style: 'width: 100px;  margin: 0px 0px 8px 50px;',
        onchange: 'searchAction'
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getDateFormatLabel());
          },
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }]
    }]
  }],

  disableFilterButtons: function (value) {
    this.$.searchButton.setDisabled(value);
    this.$.clearButton.setDisabled(value);
  },

  searchAction: function () {
    this.filters = {
      documentType: [OB.MobileApp.model.get('terminal').terminalType.documentType, OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns],
      docstatus: null,
      isQuotation: false,
      isLayaway: true,
      filterText: this.$.filterText.getValue(),
      startDate: this.$.startDate.getValue(),
      endDate: this.$.endDate.getValue(),
      pos: OB.MobileApp.model.get('terminal').id,
      client: OB.MobileApp.model.get('terminal').client,
      organization: OB.MobileApp.model.get('terminal').organization
    };

    if (!this.getDateFilters()) {
      return true;
    }

    this.doSearchAction({
      filters: this.filters
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListMultiOrdersLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'border-bottom: 1px solid #cccccc;text-align: left; padding-left: 70px; height: 58px;',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.model.set('checked', !this.model.get('checked'));
    this.model.trigger('verifyDoneButton', this.model);
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px; display: inline',
    components: [{
      style: 'display: inline',
      name: 'topLine'
    }, {
      style: 'font-weight: bold; color: lightblue; float: right; text-align:right; ',
      name: 'isLayaway'
    }, {
      style: 'color: #888888',
      name: 'bottonLine'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    var returnLabel = '';
    this.inherited(arguments);
    if (this.model.get('documentTypeId') === OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns) {
      this.model.set('totalamount', OB.DEC.mul(this.model.get('totalamount'), -1));
      returnLabel = ' (' + OB.I18N.getLabel('OBPOS_ToReturn') + ')';
    }
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + (this.model.get('bp') ? this.model.get('bp').get('_identifier') : this.model.get('businessPartner')) + returnLabel);
    this.$.bottonLine.setContent(((this.model.get('totalamount') || this.model.get('totalamount') === 0) ? this.model.get('totalamount') : this.model.getPending()) + ' (' + OB.I18N.formatDate(new Date(this.model.get('orderDate'))) + ') ');
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
    if (this.model.get('isLayaway')) {
      this.$.isLayaway.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
    }
    this.render();
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListMultiOrders',
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
          name: 'multiorderslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '300px',
          renderHeader: 'OB.UI.ModalMultiOrdersHeader',
          renderLine: 'OB.UI.ListMultiOrdersLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  cleanFilter: false,
  clearAction: function (inSender, inEvent) {
    this.multiOrdersList.reset();
    return true;
  },
  disableFilters: function (value) {
    this.$.multiorderslistitemprinter.$.theader.$.modalMultiOrdersHeader.disableFilterButtons(value);
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        toMatch = 0,
        re, actualDate, i, processHeader = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceiptsHeader');
    me.filters = inEvent.filters;
    var limit;
    if (!OB.MobileApp.model.hasPermission('OBPOS_remote.order', true)) {
      limit = OB.Model.Order.prototype.dataLimit;
    } else {
      limit = OB.Model.Order.prototype.remoteDataLimit ? OB.Model.Order.prototype.remoteDataLimit : OB.Model.Order.prototype.dataLimit;
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_orderLimit', true)) {
      limit = OB.DEC.abs(OB.MobileApp.model.hasPermission('OBPOS_orderLimit', true));
    }
    this.clearAction();
    // Disable the filters button
    me.disableFilters(true);
    processHeader.exec({
      filters: me.filters,
      _limit: limit
    }, function (data) {
      if (data) {
        _.each(me.model.get('orderList').models, function (iter) {
          if (iter.get('lines') && iter.get('lines').length > 0) {
            re = new RegExp(me.filters.filterText, 'gi');
            toMatch = iter.get('documentNo').match(re) + iter.get('bp').get('_identifier').match(re);
            if ((me.filters.filterText === "" || toMatch !== 0) && (iter.get('orderType') === 0 || iter.get('orderType') === 2) && !iter.get('isPaid') && !iter.get('isQuotation') && iter.get('gross') >= 0) {
              actualDate = new Date().setHours(0, 0, 0, 0);
              if (me.filters.endDate === "" || new Date(me.filters.endDate) >= actualDate) {
                for (i = 0; i < me.filters.documentType.length; i++) {
                  if (me.filters.documentType[i] === iter.get('documentType')) {
                    if (!_.isNull(iter.id) && !_.isUndefined(iter.id)) {
                      if (me.cleanFilter) {
                        iter.unset("checked");
                      }
                      me.multiOrdersList.add(iter);
                      break;
                    }
                  }
                }
              }
            }
          }
        });
        if (me.cleanFilter) {
          me.cleanFilter = false;
        }
        _.each(data, function (iter) {
          me.multiOrdersList.add(iter);
        });

        me.disableFilters(false);
      } else {
        me.disableFilters(false);
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
      }
    });
    return true;
  },
  multiOrdersList: null,
  init: function (model) {
    var me = this;
    this.model = model;
    this.multiOrdersList = new Backbone.Collection();
    this.$.multiorderslistitemprinter.setCollection(this.multiOrdersList);
    this.multiOrdersList.on('verifyDoneButton', function (item) {
      if (item.get('checked')) {
        me.parent.parent.$.header.$.modalMultiOrdersTopHeader.disableDoneButton(false);
      } else {
        me.parent.parent.$.header.$.modalMultiOrdersTopHeader.disableDoneButton(true);
        _.each(me.multiOrdersList.models, function (e) {
          if (e.get('checked')) {
            me.parent.parent.$.header.$.modalMultiOrdersTopHeader.disableDoneButton(false);
            return;
          }
        });
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalMultiOrdersTopHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onHideThisPopup: '',
    onSelectMultiOrders: '',
    onTabChange: '',
    onRightToolDisabled: ''
  },
  components: [{
    style: 'display: table;',
    components: [{
      style: 'display: table-cell; float:left',
      components: [{
        name: 'doneMultiOrdersButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'doneAction'
      }]
    }, {
      style: 'display: table-cell; vertical-align: middle; width: 100%;',
      components: [{
        name: 'title',
        style: 'text-align: center;'
      }]
    }, {
      style: 'display: table-cell; float:right',
      components: [{
        classes: 'btnlink-green',
        name: 'cancelMultiOrdersButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'cancelAction'
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.doneMultiOrdersButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
    this.$.cancelMultiOrdersButton.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  },
  disableDoneButton: function (value) {
    this.$.doneMultiOrdersButton.setDisabled(value);
  },
  doneAction: function () {
    var selectedMultiOrders = [],
        me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts'),
        checkedMultiOrders = _.compact(this.parent.parent.parent.$.body.$.listMultiOrders.multiOrdersList.map(function (e) {
        if (e.get('checked')) {
          return e;
        }
      }));
    if (checkedMultiOrders.length === 0) {
      return true;
    }
    OB.UTIL.showLoading(true);
    me.owner.owner.model.deleteMultiOrderList();
    _.each(checkedMultiOrders, function (iter) {
      if (_.indexOf(me.owner.owner.model.get('orderList').models, iter) !== -1) {
        iter.save();
        selectedMultiOrders.push(iter);
      } else {
        process.exec({
          orderid: iter.id
        }, function (data) {
          if (data) {
            me.owner.owner.model.get('orderList').newPaidReceipt(data[0], function (order) {
              order.set('loadedFromServer', true);
              order.set('checked', iter.get('checked'));
              OB.DATA.OrderTaxes(order);
              order.set('belongsToMultiOrder', true);
              order.calculateReceipt(function () {
                selectedMultiOrders.push(order);
                order.save();
                if (selectedMultiOrders.length === checkedMultiOrders.length) {
                  me.doSelectMultiOrders({
                    value: selectedMultiOrders
                  });
                  me.showPaymentView();
                }
              });
            });
          } else {
            OB.UTIL.showLoading(false);
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
          }
        });
      }
      if (selectedMultiOrders.length === checkedMultiOrders.length) {
        me.doSelectMultiOrders({
          value: selectedMultiOrders
        });
        me.showPaymentView();
      }
    });

    this.doHideThisPopup();
  },
  cancelAction: function () {
    this.doHideThisPopup();
  },
  showPaymentView: function () {
    OB.UTIL.showLoading(false);
    this.doTabChange({
      tabPanel: 'payment',
      keyboard: 'toolbarpayment',
      edit: false
    });
  }
}); /*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalMultiOrders',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnHide: function () {
    this.$.body.$.listMultiOrders.$.multiorderslistitemprinter.$.theader.$.modalMultiOrdersHeader.clearAction();
  },
  executeOnShow: function () {
    this.$.header.$.modalMultiOrdersTopHeader.$.title.setContent(OB.I18N.getLabel('OBPOS_LblMultiOrders'));
    this.$.body.$.listMultiOrders.cleanFilter = true;
    this.$.header.$.modalMultiOrdersTopHeader.disableDoneButton(true);
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListMultiOrders'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
    this.$.header.createComponent({
      kind: 'OB.UI.ModalMultiOrdersTopHeader'
    });
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});