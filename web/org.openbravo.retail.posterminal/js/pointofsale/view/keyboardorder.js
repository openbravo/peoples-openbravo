/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _, Audio, Backbone */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
  kind: 'OB.UI.Keyboard',
  keypads: [],
  published: {
    receipt: null
  },
  events: {
    onShowPopup: '',
    onAddProduct: '',
    onSetDiscountQty: '',
    onDiscountsMode: '',
    onSetMultiSelectionItems: '',
    onDeleteLine: ''
  },
  discountsMode: false,
  handlers: {
    onSetMultiSelected: 'setMultiSelected',
    onKeyboardOnDiscountsMode: 'keyboardOnDiscountsMode'
  },
  setMultiSelected: function (inSender, inEvent) {
    if (inEvent.models && inEvent.models.length > 0 && !(inEvent.models[0] instanceof OB.Model.OrderLine)) {
      return;
    }
    this.selectedModels = inEvent.models;
    this.selectedModelsSameQty = true;
    if (this.selectedModels.length > 1) {
      var i, qty = this.selectedModels[0].get('qty');
      for (i = 1; i < this.selectedModels.length; i++) {
        if (qty !== this.selectedModels[i].get('qty')) {
          this.selectedModelsSameQty = false;
          break;
        }
      }
    }
  },
  keyboardOnDiscountsMode: function (inSender, inEvent) {
    if (inEvent.status) {
      this.showSidepad('ticketDiscountsToolbar');
    } else {
      this.showSidepad('sideenabled');
    }
    if (!inEvent.status) {
      //exit from discounts
      this.discountsMode = false;
      if (this.prevdefaultcommand) {
        this.defaultcommand = this.prevdefaultcommand;
      }
      if (this.buttons['ticket:discount']) {
        this.buttons['ticket:discount'].removeClass('btnactive');
      }

      this.keyboardDisabled(inSender, {
        status: false
      });
    } else {
      this.discountsMode = true;
      this.prevdefaultcommand = this.defaultcommand;
      this.clearInput();
      this.defaultcommand = 'ticket:discount';
      if (inEvent.writable) {
        //enable keyboard
        this.keyboardDisabled(inSender, {
          status: false
        });
        //button as active
        if (this.buttons['ticket:discount']) {
          this.buttons['ticket:discount'].addClass('btnactive');
        }
      } else {
        if (this.buttons['ticket:discount']) {
          this.buttons['ticket:discount'].removeClass('btnactive');
        }

        this.keyboardDisabled(inSender, {
          status: true
        });
        return true;
      }
    }
  },
  sideBarEnabled: true,

  receiptChanged: function () {
    this.$.toolbarcontainer.$.toolbarPayment.setReceipt(this.receipt);

    this.line = null;

    this.receipt.get('lines').on('selected', function (line) {
      this.line = line;
      this.clearEditBox();
    }, this);
  },
  validateQuantity: function (keyboard, value) {
    if (!isFinite(value)) {
      return true;
    }
    var valueBigDecimal = OB.DEC.toBigDecimal(value);
    if (valueBigDecimal.scale() > keyboard.line.get('product').get('uOMstandardPrecision')) {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_StdPrecisionLimitError', [keyboard.line.get('product').get('uOMstandardPrecision')]));
      return false;
    }
    return true;
  },
  validateReceipt: function (keyboard, validateLine) {
    if (keyboard.receipt.get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return false;
    }
    if (validateLine) {
      if (keyboard.line && keyboard.line.get('product').get('isEditableQty') === false) {
        this.doShowPopup({
          popup: 'modalNotEditableLine'
        });
        return false;
      } else if (!keyboard.line) {
        return false;
      }
    }
    return true;
  },
  initComponents: function () {
    var me = this;

    var actionAddProduct = function (keyboard, value) {
        if (keyboard.line.get('relatedLines')) {
          return;
        }
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        if (keyboard.line) {
          if (_.isNaN(value)) {
            return true;
          } else {
            me.doAddProduct({
              product: keyboard.line.get('product'),
              qty: value,
              options: {
                line: keyboard.line,
                blockAddProduct: true
              }
            });
            keyboard.receipt.trigger('scan');
          }
        }
        };

    var actionAddMultiProduct = function (keyboard, qty) {
        var cancelQtyChange = false,
            cancelQtyChangeReturn = false;
        if (me.selectedModelsSameQty) {

          // Check if is trying to remove delivered units or to modify negative lines in a cancel and replace ticket.
          // In that case stop the flow and show an error popup.
          if (keyboard.receipt.get('replacedorder')) {
            _.each(me.selectedModels, function (l) {
              var oldqty = l.get('qty'),
                  newqty = oldqty + qty;

              if (oldqty > 0 && newqty < l.get('remainingQuantity')) {
                cancelQtyChange = true;
              } else if (oldqty < 0 && l.get('remainingQuantity')) {
                cancelQtyChangeReturn = true;
              }
            });
          }
          if (cancelQtyChange) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceQtyEdit'));
            return;
          } else if (cancelQtyChangeReturn) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn'));
            return;
          }

          keyboard.receipt.set('undo', null);
          keyboard.receipt.set('multipleUndo', true);
          var selection = [];
          _.each(me.selectedModels, function (model) {
            selection.push(model);
            keyboard.line = model;
            actionAddProduct(keyboard, qty);
          });
          keyboard.receipt.set('multipleUndo', null);
          me.doSetMultiSelectionItems({
            selection: selection
          });
        }
        };

    var actionRemoveProduct = function (keyboard, value) {
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        if (keyboard.line) {
          keyboard.receipt.removeUnit(keyboard.line, value);
          keyboard.receipt.trigger('scan');
        }
        };

    // action bindable to a command that completely deletes a product from the order list
    var actionDeleteLine = function (keyboard) {
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        if (keyboard.model.get('leftColumnViewManager') && !keyboard.model.get('leftColumnViewManager').isMultiOrder()) {
          if (keyboard.line) {
            keyboard.doDeleteLine({
              selectedModels: keyboard.selectedModels
            });
          }
        } else {
          return true;
        }

        };

    this.addCommand('line:qty', {
      action: function (keyboard, txt) {
        var value = OB.I18N.parseNumber(txt),
            toadd;

        if (!keyboard.line) {
          return true;
        }
        if (value || value === 0) {
          if (!me.validateQuantity(keyboard, value)) {
            return true;
          }
          keyboard.receipt.set('undo', null);
          var selection = [];
          if (me.selectedModels && me.selectedModels.length > 1) {
            keyboard.receipt.set('multipleUndo', true);
          }
          _.each(me.selectedModels, function (model) {
            selection.push(model);
            keyboard.line = model;
            if (keyboard.receipt.get('orderType') === 1) {
              toadd = value - (-keyboard.line.get('qty'));
            } else {
              toadd = value - keyboard.line.get('qty');
            }
            if (toadd !== 0) {
              if (value === 0) { // If final quantity will be 0 then request approval
                keyboard.doDeleteLine({
                  selectedModels: keyboard.selectedModels
                });
              } else {
                actionAddProduct(keyboard, toadd);
              }
            } else {
              keyboard.clearInput();
            }
          });
          keyboard.receipt.set('multipleUndo', null);
          me.doSetMultiSelectionItems({
            selection: selection
          });
        }
      }
    });

    this.addCommand('line:price', {
      permission: 'OBPOS_order.changePrice',
      action: function (keyboard, txt) {
        if (!me.validateReceipt(keyboard, false)) {
          return true;
        }
        if (!keyboard.line) {
          return true;
        }
        if (keyboard.line.get('product').get('isEditablePrice') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableLine'
          });
          return true;
        }
        if (keyboard.line) {
          OB.UTIL.Approval.requestApproval(
          me.model, 'OBPOS_approval.setPrice', function (approved, supervisor, approvalType) {
            if (approved) {
              var price = OB.I18N.parseNumber(txt),
                  cancelChange = false;
              if (me.selectedModels.length > 1) {
                keyboard.receipt.set('undo', null);
                keyboard.receipt.set('multipleUndo', true);

                _.each(me.selectedModels, function (model) {
                  if (model.get('replacedorderline') && model.get('qty') < 0) {
                    cancelChange = true;
                  }
                });

                if (cancelChange) {
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceReturnPriceChange'));
                  return;
                }

                _.each(me.selectedModels, function (model) {
                  keyboard.receipt.setPrice(model, price);
                });
                keyboard.receipt.set('multipleUndo', null);
              } else {
                keyboard.receipt.setPrice(keyboard.line, price);
              }
              keyboard.receipt.calculateReceipt();
              keyboard.receipt.trigger('scan');
            }
          });
        }
      }
    });

    this.addCommand('line:dto', {
      permission: 'OBPOS_order.discount',
      action: function (keyboard, txt) {
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        if (OB.MobileApp.model.get('permissions')["OBPOS_retail.discountkeyboard"] === true || keyboard.line.getQty() < 0) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_LineCanNotBeSelected'));
          return true;
        }
        keyboard.receipt.set('undo', null);
        keyboard.receipt.set('multipleUndo', true);
        var discount = OB.I18N.parseNumber(txt);
        _.each(me.selectedModels, function (model) {
          keyboard.receipt.trigger('discount', model, discount);
        });
        keyboard.receipt.set('multipleUndo', null);
        keyboard.lastStatus = '';
        keyboard.setStatus('');
      }
    });

    this.addCommand('screen:dto', {
      stateless: true,
      permission: 'OBPOS_order.discount',
      action: function (keyboard, txt) {
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        me.doDiscountsMode({
          tabPanel: 'edit',
          keyboard: 'toolbardiscounts',
          edit: false,
          options: {
            discounts: true
          }
        });
      }
    });

    //To be used in the discounts side bar
    this.addCommand('ticket:discount', {
      permission: 'OBPOS_retail.advDiscounts',
      action: function (keyboard, txt) {
        if (keyboard.discountsMode) {
          me.doSetDiscountQty({
            qty: OB.I18N.parseNumber(txt)
          });
          return true;
        }
      }
    });

    this.addCommand('code', new OB.UI.BarcodeActionHandler());

    this.addCommand('+', {
      stateless: true,
      action: function (keyboard, txt) {
        var qty = 1;
        if (!me.selectedModels || !keyboard.line) {
          return;
        }

        if ((!_.isNull(txt) || !_.isUndefined(txt)) && !_.isNaN(OB.I18N.parseNumber(txt))) {
          qty = OB.I18N.parseNumber(txt);
          if (!me.validateQuantity(keyboard, qty)) {
            return true;
          }
        }
        if (me.selectedModels.length > 1) {
          actionAddMultiProduct(keyboard, qty);
        } else {
          keyboard.receipt.set('multipleUndo', null);
          actionAddProduct(keyboard, qty);
        }
      }
    });

    this.addCommand('-', {
      stateless: true,
      action: function (keyboard, txt) {
        var qty = 1,
            value;
        if (!me.selectedModels || !keyboard.line) {
          return;
        }
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }

        if ((!_.isNull(txt) || !_.isUndefined(txt)) && !_.isNaN(OB.I18N.parseNumber(txt))) {
          qty = OB.I18N.parseNumber(txt);
          if (!me.validateQuantity(keyboard, qty)) {
            return true;
          }
        }
        if (me.selectedModels.length > 0) {
          if (keyboard.receipt.get('orderType') === 1) {
            value = me.selectedModels[0].get('qty') + qty;
          } else {
            value = me.selectedModels[0].get('qty') - qty;
          }
        } else if (!_.isUndefined(keyboard.line)) {
          value = keyboard.line.get('qty') - qty;
        }
        if (value === 0) { // If final quantity will be 0 then request approval
          keyboard.doDeleteLine({
            selectedModels: keyboard.selectedModels
          });
        } else {
          var actionAddProducts;
          actionAddProducts = function (doNotReturnServices) {
            if (keyboard.receipt.validateAllowSalesWithReturn(value, false, me.selectedModels)) {
              return;
            }
            keyboard.receipt.set('undo', null);
            if (me.selectedModels.length > 1) {
              actionAddMultiProduct(keyboard, -qty);
            } else {
              keyboard.receipt.set('multipleUndo', null);
              actionAddProduct(keyboard, -qty);
            }
          };

          if (value > 0) {
            actionAddProducts();
          } else {
            me.getReceipt().checkReturnableProducts(me.selectedModels, me.model, function () {
              actionAddProducts();
            });
          }
        }
      }
    });

    // add a command that will handle the DELETE keyboard key
    this.addCommand('line:delete', {
      stateless: true,
      action: function (keyboard) {
        if (keyboard.line) {
          actionDeleteLine(keyboard);
        }
      }
    });

    // calling super after setting keyboard properties
    this.inherited(arguments);

    this.addToolbarComponent('OB.OBPOSPointOfSale.UI.ToolbarPayment');
    this.addToolbar(OB.OBPOSPointOfSale.UI.ToolbarScan);
    this.addToolbar(OB.OBPOSPointOfSale.UI.ToolbarDiscounts);
  },


  init: function (model) {
    this.model = model;
    // Add the keypads for each payment method
    this.initCurrencyKeypads();

    _.each(this.keypads, function (keypadname) {
      this.addKeypad(keypadname);
    }, this);
  },
  initCurrencyKeypads: function () {
    var me = this;
    var currenciesManaged = {};

    _.each(OB.MobileApp.model.get('payments'), function (payment) {
      // Is cash method if is checked as iscash or is the legacy hardcoded cash method for euros.
      if ((payment.paymentMethod.iscash && payment.paymentMethod.showkeypad) && !currenciesManaged[payment.paymentMethod.currency]) {
        // register that is already built
        currenciesManaged[payment.paymentMethod.currency] = true;

        // Build the panel
        OB.Dal.find(OB.Model.CurrencyPanel, {
          'currency': payment.paymentMethod.currency,
          _orderByClause: 'line'
        }, function (datacurrency) {
          if (datacurrency.length > 0) {
            me.buildCoinsAndNotesPanel(payment, payment.symbol, datacurrency);
          } else if (payment.payment.searchKey === 'OBPOS_payment.cash' && payment.paymentMethod.currency === '102') {
            // Add the legacy keypad if is the legacy hardcoded cash method for euros.
            me.addKeypad('OB.UI.KeypadCoinsLegacy');
          }
        }, function (tx, error) {
          OB.UTIL.showError("OBDAL error: " + error);
        });
      }
    }, this);
  },

  buildCoinsAndNotesButton: function (paymentkey, coin) {
    if (coin) {
      return {
        kind: 'OB.UI.PaymentButton',
        paymenttype: paymentkey,
        amount: coin.get('amount'),
        background: coin.get('backcolor') || '#f3bc9e',
        bordercolor: coin.get('bordercolor') || coin.get('backcolor') || '#f3bc9e'
      };
    } else {
      return {
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '',
        command: 'dummy'
      };
    }
  },

  buildCoinsAndNotesPanel: function (payment, symbol, datacurrency) {

    enyo.kind({
      name: 'OB.UI.Keypad' + payment.payment.searchKey,
      label: _.template('<%= symbol %>,<%= symbol %>,<%= symbol %>,...', {
        symbol: symbol
      }),
      padName: 'Coins-' + payment.paymentMethod.currency,
      padPayment: payment.payment.searchKey,
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span4',
          components: [{
            kind: 'OB.UI.ButtonKey',
            classButton: 'btnkeyboard-num',
            label: '/',
            command: '/'
          }]
        }, {
          classes: 'span4',
          components: [{
            kind: 'OB.UI.ButtonKey',
            classButton: 'btnkeyboard-num',
            label: '*',
            command: '*'
          }]
        }, {
          classes: 'span4',
          components: [{
            kind: 'OB.UI.ButtonKey',
            classButton: 'btnkeyboard-num',
            label: '%',
            command: '%'
          }]
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(9))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(10))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(11))]
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(6))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(7))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(8))]
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(3))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(4))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(5))]
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(0))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(1))]
        }, {
          classes: 'span4',
          components: [this.buildCoinsAndNotesButton(payment.payment.searchKey, datacurrency.at(2))]
        }]
      }]
    });
    this.addKeypad('OB.UI.Keypad' + payment.payment.searchKey);
  }
});

enyo.kind({
  // Overwrite this component to customize the BarcodeActionHandler
  name: 'OB.UI.BarcodeActionHandler',
  kind: 'OB.UI.AbstractBarcodeActionHandler',

  errorCallback: function (tx, error) {
    OB.UTIL.showError('OBDAL error: ' + error);
  },

  findProductByBarcode: function (code, callback, keyboard, attrs) {
    if (attrs.receipt && attrs.receipt.get('isEditable') === false) {
      //Checking preference to search Receipt using Scanner
      attrs.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var me = this;
    OB.debug('BarcodeActionHandler - id: ' + code);
    if (code.length > 0) {
      OB.UTIL.HookManager.executeHooks('OBPOS_BarcodeScan', {
        context: me,
        code: code.replace(/\\-/g, '-').replace(/\\+/g, '+'),
        callback: callback,
        attrs: attrs
      }, function (args) {
        if (args.cancellation) {
          return;
        }
        me.searchProduct(args.code, args.callback, args.attrs);
      });
    }
  },

  searchProduct: function (code, callback, attrs) {
    var me = this,
        criteria = {
        uPCEAN: code
        };
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      var uPCEAN = {
        columns: ['uPCEAN'],
        operator: 'equals',
        value: code
      };
      var remoteCriteria = [uPCEAN];
      criteria.remoteFilters = remoteCriteria;
    }

    OB.Dal.findUsingCache('productSearch', OB.Model.Product, criteria, function (data) {
      me.searchProductCallback(data, code, callback, attrs);
    }, me.errorCallback, {
      modelsAffectedByCache: ['Product']
    });
  },

  searchProductCallback: function (data, code, callback, attrs) {
    this.successCallbackProducts(data, code, callback, attrs);
  },

  successCallbackProducts: function (dataProducts, code, callback, attrs) {
    OB.UTIL.HookManager.executeHooks('OBPOS_BarcodeSearch', {
      dataProducts: dataProducts,
      code: code,
      callback: callback,
      attrs: attrs
    }, function (args) {
      if (args.cancellation) {
        return;
      }
      var reproduceErrorSound = function () {
          //scanMode is disable, raise an error sound only if the preference allows it.
          if (OB.MobileApp.model.hasPermission('OBMOBC_ReproduceErrorSoundOnFailedScan', true)) {
            var error_sound = new Audio('../org.openbravo.mobile.core/sounds/Computer_Error.mp3');
            error_sound.play();
          }
          };
      if (args.dataProducts && args.dataProducts.length > 0) {
        OB.debug('productfound');
        args.callback(args.dataProducts.at(0), args.attrs);
      } else {
        // If rfid has been used remove code from buffer
        if (args.attrs && args.attrs.obposEpccode) {
          OB.UTIL.RfidController.removeEpc(args.attrs.obposEpccode);
        }
        OB.UTIL.HookManager.executeHooks('OBPOS_PostBarcodeAction', {
          keyboard: this,
          code: code
        }, function (args) {
          if (args.cancellation) {
            return;
          }
          // If the preference to show that the 'UPC/EAN code has not been found is enabled'
          if (OB.MobileApp.model.hasPermission('OBPOS_showPopupEANNotFound', true)) {
            reproduceErrorSound();
            OB.MobileApp.model.set('reproduceErrorSound', true);
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [args.code]), undefined, [{
              isConfirmButton: true,
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              action: function () {
                OB.MobileApp.model.set('reproduceErrorSound', false);
              }
            }], {
              defaultAction: false,
              onHideFunction: function () {
                OB.MobileApp.model.set('reproduceErrorSound', false);
              }
            });
          } else {
            reproduceErrorSound();
            OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [args.code]));
          }
        });
      }
    });
  },

  addProductToReceipt: function (keyboard, product, attrs) {
    keyboard.doAddProduct({
      product: product,
      qty: attrs.unitsToAdd,
      ignoreStockTab: true,
      options: {
        blockAddProduct: true
      },
      attrs: attrs
    });
    keyboard.receipt.trigger('scan');
  }
});