/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, moment, enyo */


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
    var params = this.parent.parent.parent.parent.parent.parent.parent.parent.params;
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
      documentType: params.isQuotation ? ([OB.POS.modelterminal.get('terminal').terminalType.documentTypeForQuotations]) : ([OB.POS.modelterminal.get('terminal').terminalType.documentType, OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns]),
      docstatus: params.isQuotation ? 'UE' : null,
      isQuotation: params.isQuotation ? true : false,
      isLayaway: params.isLayaway ? true : false,
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
  name: 'OB.UI.ListPRsLine',
  kind: 'OB.UI.SelectButton',
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
    if (this.model.get('documentTypeId') === OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns) {
      this.model.set('totalamount', OB.DEC.mul(this.model.get('totalamount'), -1));
      returnLabel = ' (' + OB.I18N.getLabel('OBPOS_ToReturn') + ')';
    }
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + this.model.get('businessPartner') + returnLabel);
    this.$.bottonLine.setContent(this.model.get('totalamount') + ' (' + this.model.get('orderDate').substring(0, 10) + ') ');
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
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalPRScrollableHeader',
          renderLine: 'OB.UI.ListPRsLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.prsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceiptsHeader');
    this.clearAction();
    process.exec({
      filters: inEvent.filters,
      _limit: OB.Model.Order.prototype.dataLimit
    }, function (data) {
      if (data) {
        _.each(data, function (iter) {
          me.model.get('orderList').newDynamicOrder(iter, function (order) {
            me.prsList.add(order);
          });
        });
        if (me.prsList.length === 1) {
          me.prsList.trigger('click', me.prsList.at(0));
          me.doHideThisPopup();
        }
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
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
      OB.UTIL.showLoading(true);
      process.exec({
        orderid: model.get('id')
      }, function (data) {
        OB.UTIL.showLoading(false);
        if (data) {
          if (me.model.get('leftColumnViewManager').isMultiOrder()) {
            if (me.model.get('multiorders')) {
              me.model.get('multiorders').resetValues();
            }
            me.model.get('leftColumnViewManager').setOrderMode();
          }
          OB.MobileApp.model.hookManager.executeHooks('OBRETUR_ReturnFromOrig', {
            order: data[0],
            context: me,
            params: me.parent.parent.params
          }, function (args) {
            if (!args.cancelOperation) {
              me.model.get('orderList').newPaidReceipt(data[0], function (order) {
                me.doChangePaidReceipt({
                  newPaidReceipt: order
                });
              });
            }
          });
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
        }
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
  changedParams: function (value) {

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
    if (this.params.isQuotation) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_Quotations'));
    } else if (this.params.isLayaway) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblLayaways'));
    } else {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblPaidReceipts'));
    }
  },
  init: function (model) {
    this.model = model;
  }
});