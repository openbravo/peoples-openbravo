/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.TerminalWindowModel.extend({
  models: [],

  loadUnpaidOrders: function(loadUnpaidOrdersCallback) {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
      model = this,
      reCalculateReceipt = false,
      me = this;

    // Get pending tickets ignoring those created in other users session
    const session = OB.MobileApp.model.get('session');
    const ordersNotPaid = OB.App.State.TicketList.Utils.getSessionTickets(
      session
    ).filter(ticket => ticket.hasbeenpaid === 'N');

    let currentOrder = {},
      loadOrderStr;

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreLoadUnpaidOrdersHook',
      {
        ordersNotPaid: ordersNotPaid,
        model: model
      },
      function(args) {
        OB.MobileApp.model.on(
          'window:ready',
          function() {
            OB.MobileApp.model.off('window:ready', null, model);
            const addNewOrderCallback = () => {
              if (!args.ordersNotPaid || args.ordersNotPaid.length === 0) {
                // If there are no pending orders, add an initial empty order
                OB.App.State.Global.createEmptyTicket(
                  OB.UTIL.TicketUtils.addTicketCreationDataToPayload()
                ).then(async () => {
                  OB.MobileApp.model.receipt.setIsCalculateGrossLockState(
                    false
                  );
                  OB.MobileApp.model.receipt.setIsCalculateReceiptLockState(
                    false
                  );
                  if (me.get('leftColumnViewManager').isMultiOrder()) {
                    me.loadCheckedMultiorders();
                  }
                  OB.UTIL.TicketUtils.loadAndSyncTicketFromState();
                  if (
                    OB.MobileApp.model.hasPermission(
                      'OBPOS_remote.customer',
                      true
                    )
                  ) {
                    const bp = OB.MobileApp.model.receipt.get('bp');
                    await OB.App.State.Global.saveBusinessPartner(
                      bp.serializeToJSON()
                    );
                    await OB.App.State.Global.saveBusinessPartnerLocation(
                      bp.get('locationModel').serializeToJSON()
                    );
                  }

                  OB.UTIL.HookManager.executeHooks('OBPOS_NewReceipt', {
                    newOrder: OB.App.StateBackwardCompatibility.getInstance(
                      'Ticket'
                    ).toBackboneObject(OB.App.State.getState().Ticket)
                  });
                });
              } else {
                model
                  .updateCurrentTicketIfNeeded(args.ordersNotPaid)
                  .then(() => {
                    if (me.get('leftColumnViewManager').isMultiOrder()) {
                      me.loadCheckedMultiorders();
                    }
                    OB.UTIL.TicketUtils.loadAndSyncTicketFromState();
                    // The order object is stored in the json property of the row fetched from the database
                    orderlist = new Backbone.Collection(
                      args.ordersNotPaid.map(ticket =>
                        OB.App.StateBackwardCompatibility.getInstance(
                          'Ticket'
                        ).toBackboneObject(ticket)
                      )
                    );
                    // current order is the one synchronized from the state
                    currentOrder = OB.MobileApp.model.receipt;
                    //removing Orders lines without mandatory fields filled
                    OB.UTIL.HookManager.executeHooks(
                      'OBPOS_CheckReceiptMandatoryFields',
                      {
                        orders: orderlist.models // local backbone array, not OrderList instance
                      },
                      function(args) {
                        reCalculateReceipt = args.reCalculateReceipt;
                        OB.UTIL.TicketListUtils.loadLocalTicket(
                          currentOrder.id
                        ).then(() => {
                          if (reCalculateReceipt) {
                            OB.MobileApp.model.receipt.calculateGrossAndSave();
                          }

                          if (currentOrder.documentNo) {
                            loadOrderStr =
                              OB.I18N.getLabel('OBPOS_Order') +
                              currentOrder.documentNo +
                              OB.I18N.getLabel('OBPOS_Loaded');
                            OB.UTIL.showAlert.display(
                              loadOrderStr,
                              OB.I18N.getLabel('OBPOS_Info')
                            );
                          }
                        });
                      }
                    );
                  });
              }
            };
            if (
              OB.MobileApp.model.get('terminal').terminalType.safebox &&
              OB.UTIL.isNullOrUndefined(
                OB.UTIL.localStorage.getItem('currentSafeBox')
              )
            ) {
              OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                popup: 'OBPOS_modalSafeBox',
                args: {
                  callback: addNewOrderCallback
                }
              });
            } else {
              addNewOrderCallback();
            }
          },
          model
        );
        loadUnpaidOrdersCallback();
      }
    );
  },

  updateCurrentTicketIfNeeded: async function(unPaidTickets) {
    const stateTicket = OB.App.State.getState().Ticket;
    if (stateTicket.session !== OB.App.TerminalProperty.get('session')) {
      await OB.App.State.Global.loadLocalTicket({
        id: unPaidTickets[0].id
      });
    }
  },

  loadCheckedMultiorders: function() {
    if (!this.get('leftColumnViewManager').isMultiOrder()) {
      return false;
    }
    var checkedMultiOrders,
      multiOrders = this.get('multiOrders'),
      multiOrderList = multiOrders.get('multiOrdersList'),
      me = this;

    const session = OB.MobileApp.model.get('session');
    const possibleMultiOrder = OB.App.State.TicketList.Utils.getSessionTickets(
      session
    ).filter(ticket => ticket.hasbeenpaid === 'N');

    //OB.Dal.find success
    if (possibleMultiOrder && possibleMultiOrder.length > 0) {
      checkedMultiOrders = _.compact(
        possibleMultiOrder.map(function(e) {
          if (e.checked) {
            return OB.App.StateBackwardCompatibility.getInstance(
              'Ticket'
            ).toBackboneObject(e);
          }
        })
      );

      multiOrderList.reset(checkedMultiOrders);

      // MultiOrder payments
      var multiOrderAddPayments = function() {
        var payments = JSON.parse(
          OB.UTIL.localStorage.getItem('multiOrdersPayment')
        );
        _.each(payments, function(payment) {
          multiOrders.addPayment(new OB.Model.PaymentLine(payment));
        });
      };
      multiOrderList.trigger('loadedMultiOrder', multiOrderAddPayments);
    } else if (me.isValidMultiOrderState()) {
      multiOrders.resetValues();
      me.get('leftColumnViewManager').setOrderMode();
    }
  },
  isValidMultiOrderState: function() {
    if (this.get('leftColumnViewManager') && this.get('multiOrders')) {
      return (
        this.get('leftColumnViewManager').isMultiOrder() &&
        this.get('multiOrders').hasDataInList()
      );
    }
    return false;
  },
  getPending: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPending();
    } else {
      return this.get('multiOrders').getPending();
    }
  },
  getChange: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getChange();
    } else {
      return this.get('multiOrders').getChange();
    }
  },
  getTotal: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getTotal();
    } else {
      return this.get('multiOrders').getTotal();
    }
  },
  getPrepaymentAmount: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').get('obposPrepaymentamt');
    } else {
      return this.get('multiOrders').get('obposPrepaymentamt');
    }
  },
  getPayment: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPayment();
    } else {
      return this.get('multiOrders').getPayment();
    }
  },
  addPayment: function(payment, callback) {
    var modelToIncludePayment;

    if (this.get('leftColumnViewManager').isOrder()) {
      modelToIncludePayment = this.get('order');
    } else {
      modelToIncludePayment = this.get('multiOrders');
    }

    modelToIncludePayment.addPayment(payment, callback);
  },
  deleteMultiOrderList: function() {
    _.each(
      this.get('multiOrders').get('multiOrdersList').models,
      function(order) {
        if (order.get('originalOrderType') !== order.get('orderType')) {
          order.setOrderType(null, order.get('originalOrderType'));
        }
        order.unset('amountToLayaway');
        order.unset('originalOrderType');
        order.unset('belongsToMultiOrder');
        if (order.get('loadedFromServer')) {
          order.deleteOrder();
        }
      },
      this
    );
    return true;
  },
  init: function() {
    OB.error(
      'This init method should never be called for this model. Call initModels and loadModels instead'
    );
    this.initModels(function() {});
    this.loadModels(function() {});
  },

  initModels: function(callback) {
    var me = this;

    // create and expose the receipt
    var receipt = new OB.Model.Order();
    // fire events if the receipt model is the target of the OB.UTIL.clone method
    receipt.triggerEventsIfTargetOfSourceWhenCloning = function() {
      return true;
    };
    OB.MobileApp.model.receipt = receipt;

    // create the multiOrders and expose it
    var multiOrders = new OB.Model.MultiOrders();
    OB.MobileApp.model.multiOrders = multiOrders;

    const session = OB.MobileApp.model.get('session');
    var ticketList = new Backbone.Collection(
      OB.App.State.TicketList.Utils.getSessionTickets(session).map(ticket => {
        return OB.App.StateBackwardCompatibility.getInstance(
          'Ticket'
        ).toBackboneObject(ticket);
      })
    );
    OB.MobileApp.model.orderList = ticketList;

    // changing this initialization order may break the loading
    this.set('order', receipt);
    this.set('orderList', ticketList); // Kept because this is used in many other places
    this.set('customer', new OB.Model.BusinessPartner());
    this.set('customerAddr', new OB.Model.BPLocation());
    this.set('multiOrders', multiOrders);
    OB.DATA.CustomerSave(this);
    OB.DATA.CustomerAddrSave(this);
    OB.DATA.OrderTaxes(receipt);

    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    var ViewManager = Backbone.Model.extend({
      defaults: {
        currentWindow: {
          name: 'mainSubWindow',
          params: []
        }
      },
      initialize: function() {}
    });

    var LeftColumnViewManager = Backbone.Model.extend({
      defaults: {
        currentView: {}
      },
      initialize: function() {
        this.on(
          'change:currentView',
          function(changedModel) {
            OB.UTIL.localStorage.setItem(
              'leftColumnCurrentView',
              JSON.stringify(changedModel.get('currentView'))
            );
            this.trigger(changedModel.get('currentView').name);
            OB.MobileApp.model.set(
              'isMultiOrderState',
              changedModel.get('currentView').name === 'order' ? false : true
            );
          },
          this
        );
      },
      setOrderMode: function(parameters) {
        this.set('currentView', {
          name: 'order',
          params: parameters
        });
        OB.UTIL.localStorage.setItem(
          'leftColumnCurrentView',
          JSON.stringify(this.get('currentView'))
        );
      },
      isOrder: function() {
        if (this.get('currentView').name === 'order') {
          return true;
        }
        return false;
      },
      setMultiOrderMode: function(parameters) {
        this.set('currentView', {
          name: 'multiorder',
          params: parameters
        });
      },
      isMultiOrder: function() {
        if (this.get('currentView').name === 'multiorder') {
          return true;
        }
        return false;
      }
    });

    this.set('leftColumnViewManager', new LeftColumnViewManager());
    this.set('subWindowManager', new ViewManager());

    OB.MobileApp.model.runSyncProcess(
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        me.loadCheckedMultiorders();
      },
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        me.loadCheckedMultiorders();
      }
    );

    this.checkOpenDrawer = function({ openDrawer, label } = {}) {
      if (openDrawer) {
        OB.POS.hwserver.openDrawer(
          {
            openFirst: true,
            label
          },
          OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales
        );
      }
    };

    var isSlowDevice =
      OB.UTIL.localStorage.getItem('benchmarkScore') &&
      parseInt(OB.UTIL.localStorage.getItem('benchmarkScore'), 10) < 1000;

    // If the device is too slow and the preference allows it, or the terminal type is configured, a block screen is shown if the calculation of the receipt is taking more than 1 sec
    if (
      (OB.MobileApp.model.get('terminal') &&
        OB.MobileApp.model.get('terminal').terminalType &&
        OB.MobileApp.model.get('terminal').terminalType
          .processingblockscreen) ||
      (isSlowDevice &&
        OB.MobileApp.model.hasPermission(
          'OBPOS_processingBlockScreenOnSlowDevices',
          true
        ))
    ) {
      var execution;
      receipt.on('calculatingReceipt', function() {
        setTimeout(function() {
          if (receipt.calculatingReceipt === true) {
            execution = OB.UTIL.ProcessController.start('slowCalculateReceipt');
          }
        }, 1000);
      });

      receipt.on('calculatedReceipt', function() {
        if (!OB.UTIL.isNullOrUndefined(execution)) {
          OB.UTIL.ProcessController.finish('slowCalculateReceipt', execution);
        }
      });
    }

    receipt.on('checkOpenDrawer', function({ openDrawer, label } = {}) {
      me.checkOpenDrawer({
        openDrawer,
        label
      });
    });

    this.get('multiOrders').on('checkOpenDrawer', function({
      openDrawer,
      label
    } = {}) {
      me.checkOpenDrawer({
        openDrawer,
        label
      });
    });

    // Listening events that cause a discount recalculation
    receipt.get('lines').on(
      'add change:qty change:price',
      function(line) {
        var terminalOrganization = {
            id: OB.MobileApp.model.get('terminal').organization,
            name: OB.I18N.getLabel('OBPOS_LblThisStore', [
              OB.MobileApp.model.get('terminal').organization$_identifier
            ]),
            country: OB.MobileApp.model.get('terminal').organizationCountryId,
            region: OB.MobileApp.model.get('terminal').organizationRegionId
          },
          terminalWarehouse = {
            id: OB.MobileApp.model.get('warehouses')[0].warehouseid,
            warehousename: OB.MobileApp.model.get('warehouses')[0].warehousename
          };
        // Do not calculate the receipt if the ticket is not editable or is being cloned
        if (
          !receipt.get('isEditable') ||
          receipt.get('skipApplyPromotions') ||
          receipt.get('cloningReceipt')
        ) {
          return;
        }
        if (
          line.get('qty') < 0 &&
          OB.UTIL.isNullOrUndefined(line.get('canceledLine')) &&
          OB.UTIL.isNullOrUndefined(line.get('isVerifiedReturn')) &&
          OB.UTIL.isCrossStoreLine(line)
        ) {
          line.set('organization', terminalOrganization);
          line.set('warehouse', terminalWarehouse);
        }
        // Calculate the receipt
        receipt.calculateReceipt(null, line);
      },
      this
    );

    receipt.get('lines').on('remove', function() {
      if (!receipt.get('isEditable') || receipt.get('deleting')) {
        return;
      }
      // Calculate the receipt
      receipt.calculateReceipt();
    });

    receipt.on(
      'change:bp change:externalBusinessPartner',
      function() {
        if (!receipt.get('isEditable') || receipt.get('lines').length === 0) {
          return;
        }
        receipt.get('lines').forEach(function(l) {
          l.unset('noDiscountCandidates', {
            silent: true
          });
        });
        if (!OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
          // Calculate the receipt only if it's not multipricelist
          receipt.calculateReceipt();
        }
      },
      this
    );

    callback();
  },

  loadModels: function(loadModelsCallback) {
    var me = this;

    this.set('filter', []);
    this.set('brandFilter', []);

    async function searchCurrentBP(callback) {
      var errorCallback = function() {
        OB.error(
          OB.I18N.getLabel('OBPOS_BPInfoErrorTitle') +
            '. Message: ' +
            OB.I18N.getLabel('OBPOS_BPInfoErrorMessage')
        );
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_BPInfoErrorTitle'),
          OB.I18N.getLabel('OBPOS_BPInfoErrorMessage'),
          [
            {
              label: OB.I18N.getLabel('OBPOS_Reload')
            }
          ],
          {
            onShowFunction: function(popup) {
              popup.$.headerCloseButton.hide();
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
            },
            onHideFunction: function() {
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
              window.location.reload();
            },
            autoDismiss: false
          }
        );
      };

      function successCallbackBPs(dataBps) {
        if (dataBps) {
          var partnerAddressId = OB.MobileApp.model.get('terminal')
            .partnerAddress;
          dataBps.loadBPLocations(null, null, async function(
            shipping,
            billing,
            locations
          ) {
            var defaultAddress = _.find(locations, function(loc) {
              return loc.id === partnerAddressId;
            });
            if (defaultAddress) {
              if (defaultAddress.get('isShipTo')) {
                shipping = defaultAddress;
              }
              if (defaultAddress.get('isBillTo')) {
                billing = defaultAddress;
              }
            }
            dataBps.setBPLocations(shipping, billing, true);
            dataBps.set('locations', locations);
            OB.MobileApp.model.set('businessPartner', dataBps);
            me.loadUnpaidOrders(function() {
              OB.Taxes.Pos.initCache(function() {
                OB.Discounts.Pos.initCache(function() {
                  me.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(me);

                  initHardwareManagerServer();

                  const ticketPrinter = new OB.App.Class.TicketPrinter();
                  ticketPrinter.setLegacyPrinter(me.printReceipt);
                  OB.App.SynchronizationBuffer.addMessageSynchronization(
                    'HardwareManager',
                    'displayTotal',
                    async message => {
                      await ticketPrinter.displayTotal(message);
                    }
                  );
                  OB.App.SynchronizationBuffer.addMessageSynchronization(
                    'HardwareManager',
                    'printTicket',
                    async message => {
                      await ticketPrinter.printTicket(message);
                    }
                  );
                  OB.App.SynchronizationBuffer.addMessageSynchronization(
                    'HardwareManager',
                    'printTicketLine',
                    async message => {
                      await ticketPrinter.printTicketLine(message);
                    }
                  );

                  // Now, get the hardware manager status
                  OB.POS.hwserver.status(function(data) {
                    if (data && data.exception) {
                      OB.UTIL.showError(data.exception.message);
                      callback();
                    } else {
                      // Save hardware manager information
                      if (data && data.version) {
                        // Max database string size: 10
                        var hwmVersion =
                          data.version.length > 10
                            ? data.version.substring(0, 9)
                            : data.version;
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerVersion',
                          hwmVersion
                        );
                      }
                      if (data && data.revision) {
                        // Max database string size: 15
                        var hwmRevision =
                          data.revision.length > 15
                            ? data.version.substring(0, 14)
                            : data.revision;
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerRevision',
                          hwmRevision
                        );
                      }
                      if (data && data.javaInfo) {
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerJavaInfo',
                          data.javaInfo
                        );
                      }
                      // Now that templates has been initialized, print welcome message
                      OB.POS.hwserver.print(
                        me.printReceipt.templatewelcome,
                        {},
                        function(data) {
                          if (data && data.exception) {
                            OB.UTIL.showError(
                              OB.I18N.getLabel(
                                'OBPOS_MsgHardwareServerNotAvailable'
                              )
                            );
                            callback();
                          } else {
                            callback();
                          }
                        },
                        OB.DS.HWServer.DISPLAY
                      );
                    }
                  });
                });
              });
            });
          });
        }
      }

      async function initHardwareManagerServer() {
        const hardwareManagerEndpoint = new OB.App.Class.HardwareManagerEndpoint();
        OB.App.SynchronizationBuffer.registerEndpoint(hardwareManagerEndpoint);

        let isOnline = true;
        try {
          const data = await hardwareManagerEndpoint.controller.getHardwareManagerStatus();
          isOnline = data && !data.notConfigured;
        } catch (error) {
          isOnline = false;
        }
        const hardwareManagerServer = OB.App.RemoteServerController.getRemoteServer(
          'HardwareManagerServer'
        );
        if (isOnline) {
          hardwareManagerServer.setOnline();
        } else {
          hardwareManagerServer.setOffline();
        }
      }

      let checkBPInLocal;
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        checkBPInLocal = function() {
          OB.Dal.get(
            OB.Model.BusinessPartner,
            OB.MobileApp.model.get('businesspartner'),
            successCallbackBPs,
            errorCallback,
            errorCallback,
            null,
            true
          );
        };
        OB.Dal.get(
          OB.Model.BusinessPartner,
          OB.MobileApp.model.get('businesspartner'),
          successCallbackBPs,
          checkBPInLocal,
          errorCallback
        );
      } else {
        checkBPInLocal = async function() {
          try {
            let businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
              OB.MobileApp.model.get('businesspartner')
            );
            successCallbackBPs(
              OB.Dal.transform(OB.Model.BusinessPartner, businessPartner)
            );
          } catch (error) {
            errorCallback(error);
          }
        };
        try {
          let businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
            OB.MobileApp.model.get('businesspartner')
          );
          if (businessPartner !== undefined) {
            successCallbackBPs(
              OB.Dal.transform(OB.Model.BusinessPartner, businessPartner)
            );
          } else {
            checkBPInLocal();
          }
        } catch (error) {
          errorCallback(error);
        }
      }
    }
    OB.MobileApp.model.runSyncProcess(
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        //Because in terminal we've the BP id and we want to have the BP model.
        //In this moment we can ensure data is already loaded in the local database
        searchCurrentBP(loadModelsCallback);
      },
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        searchCurrentBP(loadModelsCallback);
      }
    );
  },

  /**
   * Approval final stage. Where approvalChecked event is triggered, with approved
   * property set to true or false regarding if approval was finally granted. In
   * case of granted approval, the approval is added to the order so it can be saved
   * in backend for audit purposes.
   */
  approvedRequest: function(approved, supervisor, approvalType, callback) {
    var newApprovals,
      approvals,
      approval,
      i,
      callbackFunc,
      hasPermission = false,
      saveApproval,
      executeHook,
      request,
      me = this;

    saveApproval = function(order, silent) {
      newApprovals = [];

      approvals = order.get('approvals') || [];
      if (!Array.isArray(approvalType)) {
        approvalType = [approvalType];
      }

      _.each(approvals, function(appr) {
        var results;
        results = _.find(approvalType, function(apprType) {
          return apprType === appr.approvalType;
        });

        if (_.isUndefined(results)) {
          newApprovals.push(appr);
        }
      });

      for (i = 0; i < approvalType.length; i++) {
        approval = {
          approvalType: approvalType[i],
          userContact: supervisor.get('id'),
          created: new Date().getTime()
        };
        newApprovals.push(approval);
      }
      order.set('approvals', newApprovals, {
        silent: silent
      });
    };

    callbackFunc = function() {
      if (enyo.isFunction(callback)) {
        callback(approved, supervisor, approvalType);
      }
    };

    executeHook = function(approvalType, finalCallback) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PostRequestApproval_' + approvalType,
        {
          approved: approved,
          supervisor: supervisor,
          approvalType: approvalType,
          callbackApproval: callback,
          context: me
        },
        function(args) {
          finalCallback(args);
        }
      );
    };

    request = function(args) {
      if (_.isArray(approvalType)) {
        hasPermission = _.every(approvalType, function(a) {
          return OB.MobileApp.model.hasPermission(a, true);
        });
      } else if (!OB.UTIL.isNullOrUndefined(approvalType)) {
        hasPermission = OB.MobileApp.model.hasPermission(approvalType, true);
      } else {
        callbackFunc();
        return;
      }
      if (hasPermission) {
        callbackFunc();
        return;
      }

      if (approved) {
        if (me.get('leftColumnViewManager').isOrder()) {
          saveApproval(me.get('order'));
        } else {
          me.get('multiOrders')
            .get('multiOrdersList')
            .forEach(function(order) {
              saveApproval(order, true);
            });
        }
      }

      me.trigger('approvalChecked', {
        approved: approved
      });
      callbackFunc();
    };

    if (_.isArray(approvalType)) {
      var afterExecuteHook = _.after(approvalType.length, function(args) {
        request(args);
      });
      _.each(approvalType, function(type) {
        executeHook(type.approval, function(args) {
          afterExecuteHook(args);
        });
      });
    } else {
      executeHook(approvalType, function(args) {
        request(args);
      });
    }
  }
});
