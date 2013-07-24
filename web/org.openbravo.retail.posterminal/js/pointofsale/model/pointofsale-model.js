/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ Backbone enyo _ */

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.WindowModel.extend({
  models: [{
    generatedModel: true,
    modelName: 'TaxRate'
  },
  OB.Model.Product, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.BPCategory, OB.Model.Order, OB.Model.DocumentSequence, OB.Model.ChangedBusinessPartners,
  {
    generatedModel: true,
    modelName: 'Discount'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterBusinessPartner'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterBusinessPartnerGroup'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterProduct'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterProductCategory'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterRole'
  },
  OB.Model.CurrencyPanel, OB.Model.SalesRepresentative],

  loadUnpaidOrders: function () {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
        criteria = {
        'hasbeenpaid': 'N',
        'session': OB.POS.modelterminal.get('session')
        };
    OB.Dal.find(OB.Model.Order, criteria, function (ordersNotPaid) { //OB.Dal.find success
      var currentOrder = {},
          loadOrderStr;
      if (!ordersNotPaid || ordersNotPaid.length === 0) {
        // If there are no pending orders,
        //  add an initial empty order
        orderlist.addFirstOrder();
      } else {
        // The order object is stored in the json property of the row fetched from the database
        orderlist.reset(ordersNotPaid.models);
        // At this point it is sure that there exists at least one order
        currentOrder = ordersNotPaid.models[0];
        orderlist.load(currentOrder);
        loadOrderStr = OB.I18N.getLabel('OBPOS_Order') + currentOrder.get('documentNo') + OB.I18N.getLabel('OBPOS_Loaded');
        OB.UTIL.showAlert.display(loadOrderStr, OB.I18N.getLabel('OBPOS_Info'));
      }
    }, function () { //OB.Dal.find error
      // If there is an error fetching the pending orders,
      // add an initial empty order
      orderlist.addFirstOrder();
    });
  },

  loadCheckedMultiorders: function () {
    // Shows a modal window with the orders pending to be paid
    var checkedMultiOrders, multiOrderList = this.get('multiOrders').get('multiOrdersList'),
        criteria = {
        'hasbeenpaid': 'N',
        'session': OB.POS.modelterminal.get('session')
        };
    OB.Dal.find(OB.Model.Order, criteria, function (possibleMultiOrder) { //OB.Dal.find success
      if (!possibleMultiOrder || possibleMultiOrder.length === 0) {
        //nothing
      } else {
        checkedMultiOrders = _.compact(possibleMultiOrder.map(function (e) {
          if (e.get('checked')) {
            return e;
          }
        }));
        //The order object is stored in the json property of the row fetched from the database
        multiOrderList.reset(checkedMultiOrders);
      }
    }, function () {
      // If there is an error fetching the checked orders of multiorders,
      //OB.Dal.find error
    });
  },
  processChangedCustomers: function () {
    // Processes the customers who has been changed
    var me = this;

    if (OB.POS.modelterminal.get('connectedToERP')) {
      OB.Dal.find(OB.Model.ChangedBusinessPartners, null, function (customersChangedNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customersChangedNotProcessed || customersChangedNotProcessed.length === 0) {
          OB.UTIL.processPaidOrders(me);
          me.loadUnpaidOrders();
          me.loadCheckedMultiorders();
          return;
        }
        successCallback = function () {
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_pendigDataOfCustomersProcessed'));

          OB.UTIL.processPaidOrders(me);
          me.loadUnpaidOrders();
          me.loadCheckedMultiorders();
        };
        errorCallback = function () {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorProcessingCustomersPendingData'));
          // we will not process pending orders in case there was an order while syncing customers
          me.loadUnpaidOrders();
          me.loadCheckedMultiorders();
        };
        customersChangedNotProcessed.each(function (cus) {
          cus.set('json', enyo.json.parse(cus.get('json')));
        });
        OB.UTIL.processCustomers(customersChangedNotProcessed, successCallback, errorCallback);
      });
    } else {
      //We are offline. We continue the normal flow
      me.loadUnpaidOrders();
      me.loadCheckedMultiorders();
    }
  },
  isValidMultiOrderState: function () {
    if (this.get('leftColumnViewManager') && this.get('multiOrders')) {
      return this.get('leftColumnViewManager').isMultiOrder() && this.get('multiOrders').hasDataInList();
    }
    return false;
  },
  getPending: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPending();
    } else {
      return this.get('multiOrders').getPending();
    }
  },
  getChange: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getChange();
    } else {
      return this.get('multiOrders').getChange();
    }
  },
  getTotal: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getTotal();
    } else {
      return this.get('multiOrders').getTotal();
    }
  },
  getPayment: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPayment();
    } else {
      return this.get('multiOrders').getPayment();
    }
  },
  addPayment: function (payment) {
    var modelToIncludePayment;

    if (this.get('leftColumnViewManager').isOrder()) {
      modelToIncludePayment = this.get('order');
    } else {
      modelToIncludePayment = this.get('multiOrders');
    }

    modelToIncludePayment.addPayment(payment);
  },
  init: function () {
    var receipt = new OB.Model.Order(),
        i, j, k, amtAux, amountToPay, ordersLength, multiOrders = new OB.Model.MultiOrders(),
        me = this,
        iter, isNew = false,
        discounts, ordersave, customersave, taxes, orderList, hwManager, ViewManager, LeftColumnViewManager, LeftColumnCurrentView;

    function success() {
      return true;
    }

    function error() {
      OB.UTIL.showError('Error removing');
    }

    function searchCurrentBP() {
      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error while getting BP info: " + error);
      }

      function successCallbackBPs(dataBps) {
        if (dataBps) {
          OB.POS.modelterminal.set('businessPartner', dataBps);
        }
      }
      OB.Dal.get(OB.Model.BusinessPartner, OB.POS.modelterminal.get('businesspartner'), successCallbackBPs, errorCallback);
    }

    //Because in terminal we've the BP id and we want to have the BP model.
    //In this moment we can ensure data is already loaded in the local database
    searchCurrentBP();

    ViewManager = Backbone.Model.extend({
      defaults: {
        currentWindow: {
          name: 'mainSubWindow',
          params: []
        }
      },
      initialize: function () {}
    });
    LeftColumnViewManager = Backbone.Model.extend({
      defaults: {
        currentView: {}
      },
      initialize: function () {
        this.on('change:currentView', function (changedModel) {
          localStorage.setItem('leftColumnCurrentView', JSON.stringify(changedModel.get('currentView')));
          this.trigger(changedModel.get('currentView').name);
        }, this);
      },
      setOrderMode: function (parameters) {
        this.set('currentView', {
          name: 'order',
          params: parameters
        });
        localStorage.setItem('leftColumnCurrentView', JSON.stringify(this.get('currentView')));
      },
      isOrder: function () {
        if (this.get('currentView').name === 'order') {
          return true;
        }
        return false;
      },
      setMultiOrderMode: function (parameters) {
        this.set('currentView', {
          name: 'multiorder',
          params: parameters
        });
      },
      isMultiOrder: function () {
        if (this.get('currentView').name === 'multiorder') {
          return true;
        }
        return false;
      }
    });
    this.set('order', receipt);
    orderList = new OB.Collection.OrderList(receipt);
    this.set('orderList', orderList);
    this.set('customer', new OB.Model.BusinessPartner());
    this.set('multiOrders', multiOrders);

    this.get('multiOrders').on('paymentAccepted', function () {
      OB.UTIL.showLoading(true);
      ordersLength = this.get('multiOrders').get('multiOrdersList').length;
      for (j = 0; j < ordersLength; j++) {
        //Create the negative payment for change
        iter = this.get('multiOrders').get('multiOrdersList').at(j);
        amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
        while ((iter.get('amountToLayaway') !== 0 && iter.get('gross') > iter.get('payment')) || (iter.get('amountToLayaway') > 0)) {
          for (i = 0; i < this.get('multiOrders').get('payments').length; i++) {
            var payment = this.get('multiOrders').get('payments').at(i),
                paymentMethod = OB.POS.terminal.terminal.paymentnames[payment.get('kind')];
            //FIXME:Change is always given back in store currency
            if (this.get('multiOrders').get('change') > 0 && paymentMethod.paymentMethod.iscash) {
              payment.set('origAmount', OB.DEC.sub(payment.get('origAmount'), this.get('multiOrders').get('change')));
              this.get('multiOrders').set('change', OB.DEC.Zero);
            }
            if (payment.get('origAmount') <= amountToPay) {
              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': OB.DEC.mul(payment.get('origAmount'), paymentMethod.mulrate),
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'openDrawer': payment.get('openDrawer')
              }));
              if (!_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway'))) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), payment.get('origAmount')));
              }
              this.get('multiOrders').get('payments').remove(this.get('multiOrders').get('payments').at(i));
              amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
            } else {
              if (j === this.get('multiOrders').get('multiOrdersList').length - 1 && !paymentMethod.paymentMethod.iscash) {
                amtAux = payment.get('origAmount');
                this.get('multiOrders').get('payments').at(i).set('origAmount', OB.DEC.sub(this.get('multiOrders').get('payments').at(i).get('origAmount'), payment.get('origAmount')));
              } else {
                amtAux = OB.DEC.mul(amountToPay, paymentMethod.mulrate);
                this.get('multiOrders').get('payments').at(i).set('origAmount', OB.DEC.sub(this.get('multiOrders').get('payments').at(i).get('origAmount'), amountToPay));
              }

              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': amtAux,
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'openDrawer': payment.get('openDrawer')
              }));
              if (!_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway'))) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), amtAux));
              }
              amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
              break;
            }
          }
        }
        this.get('multiOrders').trigger('closed', iter);
        this.get('multiOrders').trigger('print', iter); // to guaranty execution order
      }
      this.get('leftColumnViewManager').setOrderMode();
    }, this);

    customersave = new OB.DATA.CustomerSave(this);

    this.set('leftColumnViewManager', new LeftColumnViewManager());
    this.set('subWindowManager', new ViewManager());
    discounts = new OB.DATA.OrderDiscount(receipt);
    ordersave = new OB.DATA.OrderSave(this);
    taxes = new OB.DATA.OrderTaxes(receipt);

    OB.POS.modelterminal.saveDocumentSequenceInDB();
    this.processChangedCustomers();

    receipt.on('paymentAccepted', function () {
      receipt.prepareToSend(function () {
        //Create the negative payment for change
        var oldChange = receipt.get('change');
        var clonedCollection = new Backbone.Collection();
        if (!_.isUndefined(receipt.selectedPayment) && receipt.getChange() > 0) {
          var payment = OB.POS.terminal.terminal.paymentnames[receipt.selectedPayment];
          receipt.get('payments').each(function (model) {
            clonedCollection.add(new Backbone.Model(model.toJSON()));
          });
          if (!payment.paymentMethod.iscash) {
            payment = OB.POS.terminal.terminal.paymentnames[OB.POS.modelterminal.get('paymentcash')];
          }
          if (receipt.get('payment') >= receipt.get('gross')) {
            receipt.addPayment(new OB.Model.PaymentLine({
              'kind': payment.payment.searchKey,
              'name': payment.payment.commercialName,
              'amount': OB.DEC.sub(0, OB.DEC.mul(receipt.getChange(), payment.mulrate)),
              'rate': payment.rate,
              'mulrate': payment.mulrate,
              'isocode': payment.isocode,
              'openDrawer': payment.paymentMethod.openDrawer
            }));
          }
          receipt.set('change', oldChange);
          receipt.trigger('closed');
          receipt.get('payments').reset();
          clonedCollection.each(function (model) {
            receipt.get('payments').add(new Backbone.Model(model.toJSON()), {
              silent: true
            });
          });
        } else {
          receipt.trigger('closed');
        }

        receipt.trigger('print'); // to guaranty execution order
        orderList.deleteCurrent();
      });
    }, this);

    receipt.on('paymentDone', function () {

      if (receipt.overpaymentExists()) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            receipt.trigger('paymentAccepted');
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        receipt.trigger('paymentAccepted');
      }
    }, this);

    this.get('multiOrders').on('paymentDone', function () {
      var me = this,
          paymentstatus = this.get('multiOrders');
      if (OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) > 0) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            me.get('multiOrders').trigger('paymentAccepted');
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        this.get('multiOrders').trigger('paymentAccepted');
      }
    }, this);
    receipt.on('openDrawer', function () {
      receipt.trigger('popenDrawer');
    }, this);

    this.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(this);
    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    // Listening events that cause a discount recalculation
    receipt.get('lines').on('add change:qty change:gross change:net', function (line) {
      if (!receipt.get('isEditable')) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt, line);
    }, this);

    receipt.get('lines').on('remove', function () {
      if (!receipt.get('isEditable')) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt);
    });

    receipt.on('change:bp', function (line) {
      if (!receipt.get('isEditable') || receipt.get('lines').length === 0) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt);
    }, this);
    receipt.on('voidLayaway', function () {
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessVoidLayaway');
      process.exec({
        order: receipt
      }, function (data, message) {
        if (data && data.exception) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorVoidLayaway'));
        } else {
          OB.Dal.remove(receipt, null, function (tx, err) {
            OB.UTIL.showError(err);
          });
          receipt.trigger('print');
          if (receipt.get('layawayGross')) {
            receipt.set('layawayGross', null);
          }
          orderList.deleteCurrent();
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessVoidLayaway'));
        }
      });

    }, this);
  },

  /**
   * Hooks for OBPOS_CheckPaymentApproval can modify args.approved to check if 
   * payment is approved. In case value is true the process will continue, if not
   * it is aborted
   */
  checkPaymentApproval: function () {
    var me = this;
    OB.MobileApp.model.hookManager.executeHooks('OBPOS_CheckPaymentApproval', {
      approved: true,
      context: this
    }, function (args) {
      me.trigger('approvalChecked', {
        approved: args.approved
      });
    });
  },

  /**
   * Generic approval checker. It validates user/password can approve the approvalType.
   * It can work online in case that user has done at least once the same approvalType
   * in this same browser. Data regarding privileged users is stored in supervisor table 
   */
  checkApproval: function (approvalType, username, password) {
    OB.Dal.initCache(OB.Model.Supervisor, [], null, null);
    if (OB.MobileApp.model.get('connectedToERP')) {
      new OB.DS.Process('org.openbravo.retail.posterminal.utility.CheckApproval').exec({
        u: username,
        p: password,
        approvalType: approvalType
      }, enyo.bind(this, function (response, message) {
        var approved = false;
        if (response.exception) {
          OB.UTIL.showError(response.exception.message);
          this.approvedTicket(false);
        } else {
          approved = response.canApprove;
          if (!approved) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_UserCannotApprove'));
          }

          // saving supervisor in local so next time it is possible to approve offline
          OB.Dal.find(OB.Model.Supervisor, {
            'id': response.userId
          }, enyo.bind(this, function (users) {
            var supervisor, date, permissions = [];
            if (users.models.length === 0) {
              // new user
              if (response.canApprove) {
                // insert in local db only in case it is supervisor for current type
                date = new Date().toString();
                supervisor = new OB.Model.Supervisor();

                supervisor.set('id', response.userId);
                supervisor.set('name', username);
                supervisor.set('password', OB.MobileApp.model.generate_sha1(password + date));
                supervisor.set('created', date);
                supervisor.set('permissions', JSON.stringify([approvalType]));
                OB.Dal.save(supervisor, null, null, true);
              }
            } else {
              // update existent user granting or revoking permission
              supervisor = users.models[0];

              supervisor.set('password', OB.MobileApp.model.generate_sha1(password + supervisor.get('created')));
              if (supervisor.get('permissions')) {
                permissions = JSON.parse(supervisor.get('permissions'));
              }

              if (response.canApprove) {
                // grant permission if it does not exist
                if (!_.contains(permissions, approvalType)) {
                  permissions.push(approvalType);
                }
              } else {
                // revoke permission if it exists
                if (_.contains(permissions, approvalType)) {
                  permissions = _.without(permissions, approvalType);
                }
              }
              supervisor.set('permissions', JSON.stringify(permissions));

              OB.Dal.save(supervisor);
            }
            this.approvedTicket(approved, supervisor, approvalType);
          }));
        }
      }));
    } else { // offline
      OB.Dal.find(OB.Model.Supervisor, {
        'name': username
      }, enyo.bind(this, function (users) {
        var supervisor, approved = false;
        if (users.models.length === 0) {
          alert(OB.I18N.getLabel('OBPOS_OfflineSupervisorNotRegistered'));
        } else {
          supervisor = users.models[0];
          if (supervisor.get('password') === OB.MobileApp.model.generate_sha1(password + supervisor.get('created'))) {
            if (_.contains(JSON.parse(supervisor.get('permissions')), approvalType)) {
              approved = true;
            } else {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_UserCannotApprove'));
            }
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_InvalidUserPassword'));
          }
        }
        this.approvedTicket(approved, supervisor, approvalType);
      }), function () {});
    }
  },

  /**
   * Approval final stage. Where approvalChecked event is triggered, with approved
   * property set to true or false regarding if approval was finally granted. In 
   * case of granted approval, the approval is added to the order so it can be saved
   * in backend for audit purposes.
   */
  approvedTicket: function (approved, supervisor, approvalType) {
    var order = this.get('order'),
        newApprovals = [],
        approvals, approval, i;


    approvals = order.get('approvals') || [];

    for (i = 0; i < approvals.length; i++) {
      // reset approvals
      if (approvals[i].approvalType !== approvalType) {
        newApprovals.push(approvals[i]);
      }
    }

    if (approved) {
      date = new Date();
      date = date.getTime();
      approval = {
        approvalType: approvalType,
        user: supervisor.get('id'),
        created: (new Date()).getTime()
      }
      newApprovals.push(approval);
      order.set('approvals', newApprovals);
    }


    this.trigger('approvalChecked', {
      approved: approved
    });
  }
});