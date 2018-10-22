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
  name: 'OB.OBPOSCashUp.UI.ButtonBring',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-gray',
  style: 'background-color: #e2e2e2; margin: 5px 0px 5px 0px;',
  initComponents: function () {
    this.setContent(OB.I18N.getLabel('OBPOS_BringOrder'));
    return this;
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderPendingReceiptLine',
  events: {
    onVoidOrder: '',
    onBringOrder: ''
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
      name: 'sessionUser',
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 15%; color: #d3d3d3; text-align: right;'
    }, {
      style: 'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px; width: 15%;',
      components: [{
        name: 'buttonBring',
        kind: 'OB.OBPOSCashUp.UI.ButtonBring',
        ontap: 'bringOrder'
      }]
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
    if (this.model.get('session') === OB.MobileApp.model.get('session')) {
      this.$.buttonBring.hide();
    } else {
      var me = this;
      OB.Dal.find(OB.Model.User, {
        'id': this.model.get('updatedBy')
      }, function (user) {
        if (user.models.length > 0 && !_.isUndefined(me.$.sessionUser)) {
          me.$.sessionUser.setContent(user.models[0].get('name'));
        }
      });
    }
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
  },
  bringOrder: function (inSender, inEvent) {
    var me = this,
        jsonOrder;
    OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ConfirmBringOrder'), OB.I18N.getLabel('OBPOS_MsgConfirmBringOrder'), [{
      label: OB.I18N.getLabel('OBPOS_LblYesBring'),
      isConfirmButton: true,
      action: function () {
        jsonOrder = JSON.parse(me.model.get('json'));
        jsonOrder.session = OB.MobileApp.model.get('session');
        jsonOrder.createdBy = OB.MobileApp.model.usermodel.id;
        jsonOrder.updatedBy = OB.MobileApp.model.usermodel.id;
        me.model.set('json', JSON.stringify(jsonOrder));
        me.model.set('session', OB.MobileApp.model.get('session'));
        OB.Dal.save(me.model, null, null, false);
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
              name: 'btnBringAll',
              kind: 'OB.UI.SmallButton',
              classes: 'btnlink-gray',
              style: 'float: right; min-width: 70px; margin: 2px 5px 2px 5px;',
              initComponents: function () {
                this.setContent(OB.I18N.getLabel('OBPOS_BringAll'));
              },
              ontap: 'bringAllPendingReceipts'
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
    var showBringBtn = false;
    if (this.collection.length === 0) {
      this.$.rowDeleteAll.hide();
    } else {
      this.$.rowDeleteAll.show();
      showBringBtn = _.find(this.collection.models, function (model) {
        return model.get('session') !== OB.MobileApp.model.get('session');
      });
      if (OB.UTIL.isNullOrUndefined(showBringBtn) || !showBringBtn) {
        this.$.btnBringAll.hide();
      }
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
  bringAllPendingReceipts: function (inSender, inEvent) {
    var me = this,
        jsonOrder;
    OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ConfirmBringOrder'), OB.I18N.getLabel('OBPOS_cannotBeUndone'), [{
      label: OB.I18N.getLabel('OBPOS_LblYesBring'),
      isConfirmButton: true,
      action: function () {
        _.each(me.collection.models, function (model) {
          if (model.get('session') !== OB.MobileApp.model.get('session')) {
            jsonOrder = JSON.parse(model.get('json'));
            jsonOrder.session = OB.MobileApp.model.get('session');
            jsonOrder.createdBy = OB.MobileApp.model.usermodel.id;
            jsonOrder.updatedBy = OB.MobileApp.model.usermodel.id;
            model.set('json', JSON.stringify(jsonOrder));
            model.set('session', OB.MobileApp.model.get('session'));
            OB.Dal.save(model, null, null, false);
          }
        });
        me.$.btnBringAll.hide();
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