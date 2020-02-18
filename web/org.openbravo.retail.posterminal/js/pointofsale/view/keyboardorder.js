/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _, Audio*/

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
    onDeleteLine: '',
    onWriteState: ''
  },
  discountsMode: false,
  handlers: {
    onSetMultiSelected: 'setMultiSelected',
    onKeyboardOnDiscountsMode: 'keyboardOnDiscountsMode'
  },
  setMultiSelected: function(inSender, inEvent) {
    if (
      inEvent.models &&
      inEvent.models.length > 0 &&
      !(inEvent.models[0] instanceof OB.Model.OrderLine)
    ) {
      return;
    }
    this.selectedModels = inEvent.models;
    this.doWriteState({
      name: 'selectedReceiptLines',
      value: inEvent.models
    });
    this.selectedEditPrice = OB.MobileApp.model.hasPermission(
      'OBPOS_order.changePrice',
      false
    );
    if (this.selectedEditPrice) {
      var i;
      for (i = 0; i < this.selectedModels.length; i++) {
        if (!this.selectedModels[i].get('product').get('obposEditablePrice')) {
          this.selectedEditPrice = false;
          break;
        }
      }
    }
    this.$.btnPrice.waterfall('onDisableButton', {
      disabled: !this.selectedEditPrice
    });
  },
  keyboardOnDiscountsMode: function(inSender, inEvent) {
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
    OB.MobileApp.view.setFocusOnFocusKeeper();
  },
  sideBarEnabled: true,

  receiptChanged: function() {
    this.$.toolbarcontainer.$.toolbarPayment.setReceipt(this.receipt);

    this.line = null;
    this.doWriteState({
      name: 'selectedReceiptLine',
      value: null
    });

    this.receipt.get('lines').on(
      'selected',
      function(line) {
        this.line = line;
        this.clearEditBox();
        this.doWriteState({
          name: 'selectedReceiptLine',
          value: line
        });
      },
      this
    );
  },
  validatePrice: function(keyboard, price, callback) {
    var me = this;
    if (
      OB.MobileApp.model.hasPermission('OBPOS_maxPriceUsingKeyboard', true) &&
      price >=
        OB.I18N.parseNumber(
          OB.MobileApp.model.hasPermission('OBPOS_maxPriceUsingKeyboard', true)
        )
    ) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardHeader'),
        OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardBody', [price]),
        [
          {
            isConfirmButton: true,
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function() {
              callback(me, keyboard, price);
            }
          },
          {
            label: OB.I18N.getLabel('OBMOBC_LblCancel')
          }
        ]
      );
      return false;
    } else {
      return true;
    }
  },
  validateReceipt: function(keyboard, validateLine) {
    if (keyboard.receipt.get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return false;
    }
    if (validateLine) {
      if (
        keyboard.line &&
        keyboard.line.get('product').get('isEditableQty') === false
      ) {
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
  initComponents: function() {
    var me = this;

    // action bindable to a command that completely deletes a product from the order list
    var actionDeleteLine = function(keyboard) {
      if (!me.validateReceipt(keyboard, true)) {
        return true;
      }
      if (
        keyboard.model.get('leftColumnViewManager') &&
        !keyboard.model.get('leftColumnViewManager').isMultiOrder()
      ) {
        if (keyboard.line) {
          keyboard.doDeleteLine({
            selectedReceiptLines: keyboard.selectedModels
          });
        }
      } else {
        return true;
      }
    };

    this.addCommand('line:qty', {
      action: function(keyboard, txt) {
        OB.MobileApp.actionsRegistry.execute({
          window: 'retail.pointofsale',
          name: 'changeQuantity'
        });
      }
    });

    this.addCommand('line:price', {
      permission: 'OBPOS_order.changePrice',
      action: function(keyboard, txt) {
        OB.MobileApp.actionsRegistry.execute({
          window: 'retail.pointofsale',
          name: 'changePrice'
        });
      }
    });

    this.addCommand('line:dto', {
      permission: 'OBPOS_order.discount',
      action: function(keyboard, txt) {
        if (!me.validateReceipt(keyboard, true)) {
          return true;
        }
        if (
          OB.MobileApp.model.get('permissions')[
            'OBPOS_retail.discountkeyboard'
          ] === true ||
          keyboard.line.getQty() < 0
        ) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_LineCanNotBeSelected'));
          return true;
        }
        keyboard.receipt.set('undo', null);
        keyboard.receipt.set('multipleUndo', true);
        var discount = OB.I18N.parseNumber(txt);
        _.each(me.selectedModels, function(model) {
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
      action: function(keyboard, txt) {
        OB.MobileApp.actionsRegistry.execute({
          window: 'retail.pointofsale',
          name: 'discount'
        });
      }
    });

    //To be used in the discounts side bar
    this.addCommand('ticket:discount', {
      permission: 'OBPOS_retail.advDiscounts',
      action: function(keyboard, txt) {
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
      action: function(keyboard, txt) {
        OB.MobileApp.actionsRegistry.execute({
          window: 'retail.pointofsale',
          name: 'addQuantity'
        });
      }
    });

    this.addCommand('-', {
      stateless: true,
      action: function(keyboard, txt) {
        OB.MobileApp.actionsRegistry.execute({
          window: 'retail.pointofsale',
          name: 'removeQuantity'
        });
      }
    });

    // add a command that will handle the DELETE keyboard key
    this.addCommand('line:delete', {
      stateless: true,
      action: function(keyboard) {
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

  init: function(model) {
    this.model = model;
    // Add the keypads for each payment method
    this.initCurrencyKeypads();

    _.each(
      this.keypads,
      function(keypadname) {
        this.addKeypad(keypadname);
      },
      this
    );
  },
  initCurrencyKeypads: function() {
    var me = this;
    var currenciesManaged = {};

    _.each(
      OB.MobileApp.model.get('payments'),
      function(payment) {
        // Is cash method if is checked as iscash or is the legacy hardcoded cash method for euros.
        if (
          payment.paymentMethod.iscash &&
          payment.paymentMethod.showkeypad &&
          OB.MobileApp.model.hasPermission(payment.payment.searchKey) &&
          !currenciesManaged[payment.paymentMethod.currency]
        ) {
          // register that is already built
          currenciesManaged[payment.paymentMethod.currency] = true;

          // Build the panel
          OB.Dal.find(
            OB.Model.CurrencyPanel,
            {
              currency: payment.paymentMethod.currency,
              _orderByClause: 'line'
            },
            function(datacurrency) {
              if (datacurrency.length > 0) {
                me.buildCoinsAndNotesPanel(
                  payment,
                  payment.symbol,
                  datacurrency
                );
              } else if (
                payment.payment.searchKey === 'OBPOS_payment.cash' &&
                payment.paymentMethod.currency === '102'
              ) {
                // Add the legacy keypad if is the legacy hardcoded cash method for euros.
                me.addKeypad('OB.UI.KeypadCoinsLegacy');
              }
            },
            function(tx, error) {
              OB.UTIL.showError(error);
            }
          );
        }
      },
      this
    );
  },

  buildCoinsAndNotesButton: function(paymentkey, coin) {
    if (coin) {
      return {
        kind: 'OB.UI.PaymentButton',
        paymenttype: paymentkey,
        amount: coin.get('amount'),
        background: coin.get('backcolor') || '#f3bc9e',
        bordercolor:
          coin.get('bordercolor') || coin.get('backcolor') || '#f3bc9e'
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

  buildCoinsAndNotesPanel: function(payment, symbol, datacurrency) {
    enyo.kind({
      name: 'OB.UI.Keypad' + payment.payment.searchKey,
      label: _.template('<%= symbol %>,<%= symbol %>,<%= symbol %>,...', {
        symbol: symbol
      }),
      padName: 'Coins-' + payment.paymentMethod.currency,
      padPayment: payment.payment.searchKey,
      classes: 'obUiKeypadCoinsLegacy',
      components: [
        {
          classes: 'obUiKeypadCoinsLegacy-container1',
          components: [
            {
              classes: 'obUiKeypadCoinsLegacy-container1-container1',
              components: [
                {
                  kind: 'OB.UI.ButtonKey',
                  classButton:
                    'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
                  label: '/',
                  command: '/'
                }
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container1-container2',
              components: [
                {
                  kind: 'OB.UI.ButtonKey',
                  classButton:
                    'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
                  label: '*',
                  command: '*'
                }
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container1-containe3',
              components: [
                {
                  kind: 'OB.UI.ButtonKey',
                  classButton:
                    'obObposPointOfSaleUiGridKeyboard-obUiActionButton-generic',
                  label: '%',
                  command: '%'
                }
              ]
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container2',
          components: [
            {
              classes: 'obUiKeypadCoinsLegacy-container2-container1',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(9)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container2-container2',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(10)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container2-container3',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(11)
                )
              ]
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container3',
          components: [
            {
              classes: 'obUiKeypadCoinsLegacy-container3-container1',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(6)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container3-container2',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(7)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container3-container3',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(8)
                )
              ]
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container4',
          components: [
            {
              classes: 'obUiKeypadCoinsLegacy-container4-container1',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(3)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container4-container2',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(4)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container4-container3',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(5)
                )
              ]
            }
          ]
        },
        {
          classes: 'obUiKeypadCoinsLegacy-container5',
          components: [
            {
              classes: 'obUiKeypadCoinsLegacy-container5-container1',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(0)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container5-container2',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(1)
                )
              ]
            },
            {
              classes: 'obUiKeypadCoinsLegacy-container5-container3',
              components: [
                this.buildCoinsAndNotesButton(
                  payment.payment.searchKey,
                  datacurrency.at(2)
                )
              ]
            }
          ]
        }
      ]
    });
    this.addKeypad('OB.UI.Keypad' + payment.payment.searchKey);
  }
});

enyo.kind({
  // Overwrite this component to customize the BarcodeActionHandler
  name: 'OB.UI.BarcodeActionHandler',
  kind: 'OB.UI.AbstractBarcodeActionHandler',

  errorCallback: function(tx, error) {
    OB.UTIL.showError(error);
  },

  findProductByBarcode: function(code, callback, keyboard, attrs) {
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
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_BarcodeScan',
        {
          context: me,
          code: code.replace(/\\-/g, '-').replace(/\\+/g, '+'),
          callback: callback,
          attrs: attrs
        },
        function(args) {
          if (args.cancellation) {
            return;
          }

          if (me.selectPrinter(args.code)) {
            return;
          }

          args.attrs = args.attrs || {};
          args.attrs.isScanning = true;
          me.searchProduct(args.code, args.callback, args.attrs);
        }
      );
    }
  },

  selectPrinter: function(code) {
    var hardwareUrls = OB.MobileApp.model.get('hardwareURL');
    var foundPrinter = hardwareUrls.find(function(hwurl) {
      return hwurl.barcode === code;
    });

    if (foundPrinter) {
      OB.POS.hwserver.setActiveURL(foundPrinter.id);
      OB.UTIL.showSuccess(
        OB.I18N.getLabel('OBPOS_PrinterFound', [foundPrinter._identifier])
      );

      return true;
    }

    return false;
  },

  searchProduct: async function(code, callback, attrs) {
    let me = this,
      criteria;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      criteria = {};
      let uPCEAN = {
        columns: ['uPCEAN'],
        operator: 'equals',
        fieldType: 'forceString',
        value: code
      };
      let remoteCriteria = [uPCEAN];
      criteria.remoteFilters = remoteCriteria;

      OB.Dal.findUsingCache(
        'productSearch',
        OB.Model.Product,
        criteria,
        function(data) {
          me.searchProductCallback(data, code, callback, attrs);
        },
        me.errorCallback,
        {
          modelsAffectedByCache: ['Product']
        }
      );
    } else {
      criteria = new OB.App.Class.Criteria()
        .criterion('uPCEAN', code.toUpperCase())
        .build();
      try {
        const products = await OB.App.MasterdataModels.Product.find(criteria);
        let data = [];
        for (let i = 0; i < products.length; i++) {
          data.push(OB.Dal.transform(OB.Model.Product, products[i]));
        }
        me.searchProductCallback(data, code, callback, attrs);
      } catch (error) {
        me.errorCallback;
      }
    }
  },

  searchProductCallback: function(data, code, callback, attrs) {
    this.successCallbackProducts(data, code, callback, attrs);
  },

  successCallbackProducts: function(dataProducts, code, callback, attrs) {
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_BarcodeSearch',
      {
        dataProducts: dataProducts,
        code: code,
        callback: callback,
        attrs: attrs
      },
      function(args) {
        if (args.cancellation) {
          return;
        }
        var reproduceErrorSound = function() {
          //scanMode is disable, raise an error sound only if the preference allows it.
          if (
            OB.MobileApp.model.hasPermission(
              'OBMOBC_ReproduceErrorSoundOnFailedScan',
              true
            )
          ) {
            var error_sound = new Audio(
              '../org.openbravo.mobile.core/sounds/Computer_Error.mp3'
            );
            error_sound.play();
          }
        };
        if (args.dataProducts && args.dataProducts.length > 0) {
          OB.debug('productfound');
          let data;
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
            data = args.dataProducts.at(0);
          } else {
            data = args.dataProducts[0];
          }
          args.callback(data, args.attrs);
        } else {
          // If rfid has been used remove code from buffer
          if (
            OB.UTIL.RfidController.isRfidConfigured() &&
            args.attrs &&
            args.attrs.obposEpccode
          ) {
            OB.UTIL.RfidController.removeEpc(args.attrs.obposEpccode);
          }
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostBarcodeAction',
            {
              keyboard: this,
              code: code
            },
            function(args) {
              if (args.cancellation) {
                return;
              }
              // If the preference to show that the 'UPC/EAN code has not been found is enabled'
              if (
                OB.MobileApp.model.hasPermission(
                  'OBPOS_showPopupEANNotFound',
                  true
                )
              ) {
                reproduceErrorSound();
                OB.MobileApp.model.set('reproduceErrorSound', true);
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [args.code]),
                  undefined,
                  [
                    {
                      isConfirmButton: true,
                      label: OB.I18N.getLabel('OBMOBC_LblOk'),
                      action: function() {
                        OB.MobileApp.model.set('reproduceErrorSound', false);
                      }
                    }
                  ],
                  {
                    defaultAction: false,
                    onHideFunction: function() {
                      OB.MobileApp.model.set('reproduceErrorSound', false);
                    }
                  }
                );
              } else {
                reproduceErrorSound();
                OB.UTIL.showWarning(
                  OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [args.code])
                );
              }
            }
          );
        }
      }
    );
  },

  addProductToReceipt: function(keyboard, product, attrs) {
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
