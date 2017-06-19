/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo , _ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ButtonVoid',
  kind: 'OB.UI.SmallButton',
  classes: 'btn-icon-small btn-icon-clear',
  style: 'background-color: #e2e2e2; margin: 5px 0px 5px 0px;',
  initComponents: function () {
    return this;
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderPendingReceiptLine',
  events: {
    onVoidOrder: ''
  },
  components: [{
    style: 'display: table; height: 42px; width: 100%; border-bottom: 1px solid #cccccc;',
    components: [{
      name: 'orderDate',
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 10%;'
    }, {
      name: 'documentNo',
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 20%;'
    }, {
      name: 'bp',
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 40%;'
    }, {
      name: 'printGross',
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 15%; font-weight: bold; text-align: right;'
    }, {
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 15%;',
      components: [{
        name: 'buttonVoid',
        kind: 'OB.OBPOSCashUp.UI.ButtonVoid',
        ontap: 'voidOrder'
      }]
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.orderDate.setContent(OB.I18N.formatHour(this.model.get('orderDate')));
    this.$.documentNo.setContent(this.model.get('documentNo'));
    this.$.bp.setContent(this.model.get('bp').get('_identifier'));
    this.$.printGross.setContent(this.model.printGross());
  },
  voidOrder: function (inSender, inEvent) {
    var me = this;
    OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ConfirmDeletion'), OB.I18N.getLabel('OBPOS_MsgConfirmDelete'), [{
      label: OB.I18N.getLabel('OBPOS_LblYesDelete'),
      isConfirmButton: true,
      action: function () {
        me.doVoidOrder();
      }
    }, {
      label: OB.I18N.getLabel('OBMOBC_LblCancel')
    }]);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ListPendingReceipts',
  published: {
    collection: null
  },
  handlers: {
    onVoidOrder: 'voidOrder'
  },
  components: [{
    classes: 'tab-pane',
    components: [{
      style: 'overflow:auto; height: 500px; margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              name: 'stepsheader',
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              renderHeader: function (step, count) {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) + " " + OB.I18N.getLabel('OBPOS_LblStepPendingOrders') + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
              }
            }]
          }, {
            style: 'clear: both;'
          }]
        }, {
          name: 'rowDeleteAll',
          classes: 'row-fluid',
          components: [{
            style: 'span12; padding: 2px 5px 2px 5px; border-bottom: 1px solid #cccccc;',
            components: [{
              name: 'btnDeleteAll',
              kind: 'OB.UI.SmallButton',
              classes: 'btnlink-gray',
              style: 'float: right; min-width: 70px; margin: 2px 5px 2px 5px;',
              initComponents: function () {
                this.setContent(OB.I18N.getLabel('OBPOS_DeleteAll'));
              },
              ontap: 'voidAllPendingReceipts'
            }, {
              style: 'clear: both;'
            }]
          }]
        }, {
          classes: 'row-fluid',
          components: [{
            style: 'span12',
            components: [{
              classes: 'row-fluid',
              components: [{
                name: 'pendingReceiptList',
                kind: 'OB.UI.Table',
                renderLine: 'OB.OBPOSCashUp.UI.RenderPendingReceiptLine',
                renderEmpty: 'OB.UI.RenderEmpty',
                listStyle: 'list'
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  init: function (model) {
    this.model = model;
  },
  collectionChanged: function (oldCol) {
    this.$.pendingReceiptList.setCollection(this.collection);

    if (oldCol) {
      oldCol.off('remove add reset', this.receiptsChanged);
    }
    this.collection.on('remove add reset', this.receiptsChanged, this);
  },
  receiptsChanged: function () {
    if (this.collection.length === 0) {
      this.$.rowDeleteAll.hide();
    } else {
      this.$.rowDeleteAll.show();
    }
  },
  voidOrder: function (inSender, inEvent) {
    var me = this,
        model = inEvent.originator.model,
        i;

    if (OB.MobileApp.model.get('isMultiOrderState')) {
      if (OB.MobileApp.model.multiOrders.checkMultiOrderPayment()) {
        return;
      }
    }

    if (model.checkOrderPayment()) {
      return false;
    }

    OB.UTIL.Approval.requestApproval(
    this.model, 'OBPOS_approval.cashupremovereceipts', function (approved, supervisor, approvalType) {
      if (approved) {
        // approved so remove the entry
        var callback = function () {
            me.collection.remove(model);
            };
        model.deleteOrder(me, callback);
      }
    });
  },
  voidAllPendingReceipts: function (inSender, inEvent) {
    var me = this;
    OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ConfirmDeletion'), OB.I18N.getLabel('OBPOS_cannotBeUndone'), [{
      label: OB.I18N.getLabel('OBPOS_LblYesDelete'),
      isConfirmButton: true,
      action: function () {
        me.voidAllOrders();
      }
    }, {
      label: OB.I18N.getLabel('OBMOBC_LblCancel')
    }]);
  },
  voidAllOrders: function (inSender, inEvent) {
    var me = this,
        i;

    if (OB.MobileApp.model.get('isMultiOrderState')) {
      if (OB.MobileApp.model.multiOrders.checkMultiOrderPayment()) {
        return;
      }
    }

    if (this.collection.checkOrderListPayment()) {
      return false;
    }

    function removeOneModel(model, collection) {
      if (collection.length === 0) {
        return;
      }
      var callback = function () {
          collection.remove(model);
          removeOneModel(collection.at(0), collection);
          };
      model.deleteOrder(me, callback);
    }

    OB.UTIL.Approval.requestApproval(
    this.model, 'OBPOS_approval.cashupremovereceipts', function (approved, supervisor, approvalType) {
      if (approved) {
        removeOneModel(me.collection.at(0), me.collection);
      }
    });
  },
  displayStep: function (model) {
    // this function is invoked when displayed.   
    this.$.stepsheader.renderHeader(model.stepNumber('OB.CashUp.StepPendingOrders'), model.stepCount());
  }
});