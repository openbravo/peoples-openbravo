/*
 ************************************************************************************
 * Copyright (C) 2014-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, moment, enyo */


/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalPRScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
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
  disableFilterText: function (value) {
    this.$.filterText.setDisabled(value);
  },
  clearAction: function () {
    if (!this.$.filterText.disabled) {
      this.$.filterText.setValue('');
    }
    this.$.startDate.setValue('');
    this.$.endDate.setValue('');
    this.doClearAction();
  },

  getDateFilters: function () {
    var startDate, endDate, startDateValidated = true,
        endDateValidated = true,
        formattedStartDate = '',
        formattedEndDate = '';
    startDate = this.$.startDate.getValue();
    endDate = this.$.endDate.getValue();

    if (startDate !== '') {
      startDateValidated = OB.Utilities.Date.OBToJS(startDate, OB.Format.date);
      if (startDateValidated) {
        formattedStartDate = OB.Utilities.Date.JSToOB(startDateValidated, 'yyyy-MM-dd');
      }
    }

    if (endDate !== '') {
      endDateValidated = OB.Utilities.Date.OBToJS(endDate, OB.Format.date);
      if (endDateValidated) {
        formattedEndDate = OB.Utilities.Date.JSToOB(endDateValidated, 'yyyy-MM-dd');
      }
    }

    if (startDate !== '' && startDateValidated && endDate !== '' && endDateValidated) {
      if (moment(endDateValidated).diff(moment(startDateValidated)) < 0) {
        endDateValidated = null;
        startDateValidated = null;
      }
    }

    if (startDateValidated === null || endDateValidated === null) {
      this.showValidationErrors(startDateValidated !== null, endDateValidated !== null);
      return false;
    } else {
      this.$.startDate.removeClass('error');
      this.$.endDate.removeClass('error');
    }

    this.filters = _.extend(this.filters, {
      startDate: formattedStartDate,
      endDate: formattedEndDate
    });

    return true;
  },

  searchAction: function () {
    var params = this.parent.parent.parent.parent.parent.parent.parent.parent.params;

    this.filters = {
      documentType: params.isQuotation ? ([OB.MobileApp.model.get('terminal').terminalType.documentTypeForQuotations]) : ([OB.MobileApp.model.get('terminal').terminalType.documentType, OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns]),
      docstatus: params.isQuotation ? 'UE' : null,
      isQuotation: params.isQuotation ? true : false,
      isLayaway: params.isLayaway ? true : false,
      isReturn: params.isReturn ? true : false,
      filterText: this.$.filterText.getValue(),
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
  name: 'OB.UI.ListPRsLine',
  kind: 'OB.UI.listItemButton',
  allowHtml: true,
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      name: 'topLine'
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
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + this.model.get('businessPartner') + returnLabel);
    this.$.bottonLine.setContent(this.model.get('totalamount') + ' (' + this.model.get('orderDate').substring(0, 10) + ') ');

    OB.UTIL.HookManager.executeHooks('OBPOS_RenderPaidReceiptLine', {
      paidReceiptLine: this
    }, function () {
      //All should be done in module side
    });

    this.render();
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListPRs',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangePaidReceipt: '',
    onShowPopup: '',
    onAddProduct: '',
    onHideThisPopup: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;',
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'prslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '350px',
          renderHeader: 'OB.UI.ModalPRScrollableHeader',
          renderLine: 'OB.UI.ListPRsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }, {
          name: 'renderLoading',
          style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.prsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        ordersLoaded = [],
        existentOrders = [],
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceiptsHeader');
    me.filters = inEvent.filters;
    this.clearAction();
    this.$.prslistitemprinter.$.tempty.hide();
    this.$.prslistitemprinter.$.tbody.hide();
    this.$.prslistitemprinter.$.tlimit.hide();
    this.$.renderLoading.show();
    var i;
    for (i = 0; i < this.model.get('orderList').length; i++) {
      // Get the id of each order
      if (this.model.get('orderList').models[i].get('id')) {
        existentOrders.push(this.model.get('orderList').models[i].get('id'));
      }
    }
    var limit = OB.Model.Order.prototype.dataLimit;
    if (OB.MobileApp.model.hasPermission('OBPOS_orderLimit', true)) {
      limit = OB.DEC.abs(OB.MobileApp.model.hasPermission('OBPOS_orderLimit', true));
    }
    process.exec({
      filters: me.filters,
      _limit: limit,
      _dateFormat: OB.Format.date
    }, function (data) {
      if (data) {
        ordersLoaded = [];
        _.each(data, function (iter) {
          me.model.get('orderList').newDynamicOrder(iter, function (order) {
            if (existentOrders.indexOf(order.id) === -1) {
              // Only push the order if not exists in previous receipts
              ordersLoaded.push(order);
            }
          });
        });
        me.prsList.reset(ordersLoaded);
        me.$.renderLoading.hide();
        if (data && data.length > 0) {
          me.$.prslistitemprinter.$.tbody.show();
        } else {
          me.$.prslistitemprinter.$.tempty.show();
        }
        me.$.prslistitemprinter.getScrollArea().scrollToTop();


      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
      }
    }, function (error) {
      if (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
      }
    });
    return true;
  },
  prsList: null,
  init: function (model) {
    var me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
    this.model = model;
    this.prsList = new Backbone.Collection();
    this.$.prslistitemprinter.setCollection(this.prsList);
    this.prsList.on('click', function (model) {
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearch');
      OB.UTIL.showLoading(true);
      process.exec({
        orderid: model.get('id')
      }, function (data) {
        if (data) {
          if (me.model.get('leftColumnViewManager').isMultiOrder()) {
            if (me.model.get('multiorders')) {
              me.model.get('multiorders').resetValues();
            }
            me.model.get('leftColumnViewManager').setOrderMode();
          }
          OB.UTIL.HookManager.executeHooks('OBRETUR_ReturnFromOrig', {
            order: data[0],
            context: me,
            params: me.parent.parent.params
          }, function (args) {
            if (!args.cancelOperation) {
              var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearchNewReceipt');
              me.model.get('orderList').newPaidReceipt(data[0], function (order) {
                me.doChangePaidReceipt({
                  newPaidReceipt: order
                });
                OB.UTIL.SynchronizationHelper.finished(synchId, 'clickSearchNewReceipt');
              });
            }
          });
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
        }
        OB.UTIL.SynchronizationHelper.finished(synchId, 'clickSearch');
      });
      return true;

    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalPaidReceipts',
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  i18nHeader: 'OBPOS_LblPaidReceipts',
  published: {
    params: null
  },
  changedParams: function () {

  },
  body: {
    kind: 'OB.UI.ListPRs'
  },
  handlers: {
    onChangePaidReceipt: 'changePaidReceipt'
  },
  changePaidReceipt: function (inSender, inEvent) {
    this.model.get('orderList').addPaidReceipt(inEvent.newPaidReceipt);
    return true;
  },
  executeOnShow: function () {
    this.$.body.$.listPRs.$.prslistitemprinter.$.theader.$.modalPRScrollableHeader.disableFilterText(false);
    this.$.body.$.listPRs.$.prslistitemprinter.$.theader.$.modalPRScrollableHeader.clearAction();
    if (this.params.isQuotation) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_Quotations'));
    } else if (this.params.isLayaway) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblLayaways'));
    } else {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblPaidReceipts'));
      if (this.params.isReturn && this.params.bpartner) {
        var me = this;
        me.$.body.$.listPRs.$.prslistitemprinter.$.theader.$.modalPRScrollableHeader.$.filterText.setValue(this.params.bpartner.get('name'));
        me.$.body.$.listPRs.$.prslistitemprinter.$.theader.$.modalPRScrollableHeader.searchAction();
        enyo.forEach(this.model.get('orderList').current.get('lines').models, function (l) {
          if (l.get('originalOrderLineId')) {
            me.$.body.$.listPRs.$.prslistitemprinter.$.theader.$.modalPRScrollableHeader.disableFilterText(true);
            return;
          }
        });
      }
    }
  },
  init: function (model) {
    this.model = model;
  }
});