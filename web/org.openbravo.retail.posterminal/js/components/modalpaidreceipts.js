/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, moment, $, _, enyo */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalPRScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onClearActionByKey: 'clearAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'enyo.Input',
          type: 'text',
          style: 'width:100%',
          classes: 'input',
          name: 'filterText',
          //          onchange: 'searchUsingBpsFilter',
          attributes: {
            'x-webkit-speech': 'x-webkit-speech'
          }
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          classes: 'btnlink-gray btnlink btnlink-small',
          components: [{
            classes: 'btn-icon-small btn-icon-search'
          }, {
            tag: 'span'
          }],
          ontap: 'searchAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.Button',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          classes: 'btnlink-yellow btnlink btnlink-small',
          attributes: {
            'onEnterTap': 'onClearActionByKey'
          },
          components: [{
            classes: 'btn-icon-small btn-icon-clear'
          }, {
            tag: 'span'
          }],
          ontap: 'clearAction'
        }]
      }]
    }, {
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          content: 'Start Date:',
          style: 'width: 200px;  margin: 0px 0px 2px 5px;'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          content: 'End Date:',
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
          style: 'width: 100px;  margin: 0px 0px 8px 5px;'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          content: 'yyyy-mm-dd',
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }, {
        kind: 'enyo.Input',
        name: 'endDate',
        size: '10',
        type: 'text',
        style: 'width: 100px;  margin: 0px 0px 8px 50px;'
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          content: 'yyyy-mm-dd',
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
      documentNo: this.$.filterText.getValue(),
      startDate: this.$.startDate.getValue(),
      endDate: this.$.endDate.getValue(),
      pos: OB.POS.modelterminal.get('terminal').id,
      client: OB.POS.modelterminal.get('terminal').client,
      organization: OB.POS.modelterminal.get('terminal').organization
    };
    this.doSearchAction({
      filters: this.filters
    });
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListPRsLine',
  kind: 'OB.UI.SelectButton',
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
    this.inherited(arguments);
    this.$.topLine.setContent(this.model.get('documentNo') + ' - ' + this.model.get('bp').get('_identifier'));
    this.$.bottonLine.setContent(this.model.get('gross'));
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
    onChangePaidReceipt: ''
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
        process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
    this.clearAction();
    process.exec({
      filters: inEvent.filters
    }, function (data) {
      if (data) {
        _.each(data, function (iter) {
          me.prsList.add(me.model.get('orderList').newPaidReceipt(iter));
        });
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
      }
    });
    return true;
  },
  prsList: null,
  init: function (model) {
    this.model = model;
    this.prsList = new Backbone.Collection();
    this.$.prslistitemprinter.setCollection(this.prsList);
    this.prsList.on('click', function (model) {
      this.doChangePaidReceipt({
        newPaidReceipt: model
      });
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalPaidReceipts',
  kind: 'OB.UI.Modal',
  onEnterTap: function (args, action) {
    if (action) {
      this.waterfall(action);
      return true;
    } else {
      this.waterfall("onSearchActionByKey");
      return true;
    }
  },
  topPosition: '125px',
  modalClass: 'modal',
  headerClass: 'modal-header',
  bodyClass: 'modal-header',
  header: OB.I18N.getLabel('OBPOS_LblPaidReceipts'),
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
  init: function (model) {
    this.model = model;
  }
});