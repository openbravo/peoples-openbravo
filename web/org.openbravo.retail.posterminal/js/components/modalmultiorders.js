/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _, moment */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalMultiOrdersHeader',
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
          style: 'width: 100%',
          isFirstFocus: true
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
            this.setContent(OB.I18N.getLabel('OBPOS_LblDateFormat'));
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
            this.setContent(OB.I18N.getLabel('OBPOS_LblDateFormat'));
          },
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }]
    }]
  }],
  showValidationErrors: function (stDate, endDate) {
    var me = this;
    if (stDate === false) {
      this.$.startDate.addClass('error');
      setTimeout(function () {
        me.$.startDate.removeClass('error');
      }, 5000);
    }
    if (endDate === false) {
      this.$.endDate.addClass('error');
      setTimeout(function () {
        me.$.endDate.removeClass('error');
      }, 5000);
    }
  },
  clearAction: function () {
    this.$.filterText.setValue('');
    this.$.startDate.setValue('');
    this.$.endDate.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    var startDate, endDate, startDateValidated = true,
        endDateValidated = true;
    startDate = this.$.startDate.getValue();
    endDate = this.$.endDate.getValue();

    if (startDate !== '') {
      startDateValidated = false;
      startDateValidated = moment(startDate, "YYYY-MM-DD").isValid();
    }

    if (endDate !== '') {
      endDateValidated = false;
      endDateValidated = moment(endDate, "YYYY-MM-DD").isValid();
    }

    if (startDate !== '' && startDateValidated && endDate !== '' && endDateValidated) {
      if (moment(endDate, "YYYY-MM-DD").diff(moment(startDate, "YYYY-MM-DD")) < 0) {
        endDateValidated = false;
        startDateValidated = false;
      }
    }

    if (startDateValidated === false || endDateValidated === false) {
      this.showValidationErrors(startDateValidated, endDateValidated);
      return true;
    } else {
      this.$.startDate.removeClass("error");
      this.$.endDate.removeClass("error");
    }
    this.filters = {
      documentType: [OB.POS.modelterminal.get('terminal').terminalType.documentType, OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns],
      docstatus: null,
      isQuotation: false,
      isLayaway: true,
      filterText: this.$.filterText.getValue(),
      startDate: this.$.startDate.getValue(),
      endDate: this.$.endDate.getValue(),
      pos: OB.POS.modelterminal.get('terminal').id,
      client: OB.POS.modelterminal.get('terminal').client,
      organization: OB.POS.modelterminal.get('terminal').organization
    };
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
    if (this.model.get('documentTypeId') === OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns) {
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
  searchAction: function (inSender, inEvent) {
    var me = this,
        toMatch = 0,
        re, actualDate, i, processHeader = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceiptsHeader');
    me.filters = inEvent.filters;
    this.clearAction();
    processHeader.exec({
      filters: me.filters,
      _limit: OB.Model.Order.prototype.dataLimit
    }, function (data) {
      if (data) {
        _.each(me.model.get('orderList').models, function (iter) {
          if (iter.get('lines') && iter.get('lines').length > 0) {
            re = new RegExp(me.filters.filterText, 'gi');
            toMatch = iter.get('documentNo').match(re) + iter.get('bp').get('_identifier').match(re);
            if ((me.filters.filterText === "" || toMatch !== 0) && (iter.get('orderType') === 0 || iter.get('orderType') === 2) && !iter.get('isPaid') && !iter.get('isQuotation')) {
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

      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
      }
    });
    return true;
  },
  multiOrdersList: null,
  init: function (model) {
    this.model = model;
    this.multiOrdersList = new Backbone.Collection();
    this.$.multiorderslistitemprinter.setCollection(this.multiOrdersList);
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
        classes: 'btnlink-gray',
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
    _.each(checkedMultiOrders, function (iter) {
      if (_.indexOf(me.owner.owner.model.get('orderList').models, iter) !== -1) {
        iter.save();
        selectedMultiOrders.push(iter);
      } else {
        process.exec({
          orderid: iter.id
        }, function (data) {
          var taxes;
          OB.UTIL.showLoading(false);
          if (data) {
            me.owner.owner.model.get('orderList').newPaidReceipt(data[0], function (order) {
              order.set('loadedFromServer', true);
              order.set('checked', iter.get('checked'))
              taxes = OB.DATA.OrderTaxes(order);
              order.save();
              selectedMultiOrders.push(order);
              if (selectedMultiOrders.length === checkedMultiOrders.length) {
                me.doSelectMultiOrders({
                  value: selectedMultiOrders
                });
              }
            });
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
          }
        });
      }
      if (selectedMultiOrders.length === checkedMultiOrders.length) {
        me.doSelectMultiOrders({
          value: selectedMultiOrders
        });
      }
    });

    this.doTabChange({
      tabPanel: 'payment',
      keyboard: 'toolbarpayment',
      edit: false
    });
    this.doHideThisPopup();
  },
  cancelAction: function () {
    this.doHideThisPopup();
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
    var i, j;
    this.$.header.$.modalMultiOrdersTopHeader.$.title.setContent(OB.I18N.getLabel('OBPOS_LblMultiOrders'));
    this.$.body.$.listMultiOrders.cleanFilter = true;
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