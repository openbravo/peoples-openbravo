/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $,  _, Backbone */


enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
  kind: 'OB.UI.Keyboard',
  keypads: ['OB.UI.KeypadCoins'],
  published: {
    receipt: null
  },
  events: {
    onShowPopup: '',
    onAddProduct: '',
    onSetDiscountQty: ''
  },
  discountsMode: false,
  handlers: {
    onKeyboardOnDiscountsMode: 'keyboardOnDiscountsMode'
  },

  keyboardOnDiscountsMode: function (inSender, inEvent) {
    if (!inEvent.status) {
      //exit from discounts
      this.discountsMode = false;
      if (this.buttons['line:dto']) {
        this.buttons['line:dto'].removeClass('btnactive');
      }

      this.keyboardDisabled(inSender, {
        status: false
      });
    } else {
      this.discountsMode = true;
      if (inEvent.writable) {
        //enable keyboard
        this.keyboardDisabled(inSender, {
          status: false
        });
        //disable commands except line:dto
        if (this.buttons['+']) {
          this.buttons['+'].setDisabled(inEvent.status);
        }

        if (this.buttons['-']) {
          this.buttons['-'].setDisabled(inEvent.status);
        }

        if (this.buttons['line:price']) {
          this.buttons['line:price'].setDisabled(inEvent.status);
        }
        if (this.buttons['line:qty']) {
          this.buttons['line:qty'].setDisabled(inEvent.status);
        }

        //css
        if (this.buttons['+']) {
          this.buttons['+'].addClass('btnkeyboard-inactive');
        }

        if (this.buttons['-']) {
          this.buttons['-'].addClass('btnkeyboard-inactive');
        }

        if (this.buttons['line:price']) {
          this.buttons['line:price'].addClass('btnkeyboard-inactive');
        }
        if (this.buttons['line:qty']) {
          this.buttons['line:qty'].addClass('btnkeyboard-inactive');
        }

        //button as active
        if (this.buttons['line:dto']) {
          this.buttons['line:dto'].addClass('btnactive');
        }
      } else {
        if (this.buttons['line:dto']) {
          this.buttons['line:dto'].removeClass('btnactive');
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
      this.clear();
    }, this);
  },
  initComponents: function () {
    var me = this;

    var actionAddProduct = function (keyboard, value) {
        if (keyboard.receipt.get('isEditable') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return true;
        }
        if (keyboard.line.get('product').get('isEditableQty') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableLine'
          });
          return true;
        }
        if (keyboard.line) {
          if (keyboard.line.get('product').get('groupProduct') === false) {
            me.doShowPopup({
              popup: 'modalProductCannotBeGroup'
            });
            return true;
          }
          me.doAddProduct({
            product: keyboard.line.get('product'),
            qty: value
          });
          keyboard.receipt.trigger('scan');
        }
        };

    var actionRemoveProduct = function (keyboard, value) {
        if (keyboard.receipt.get('isEditable') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return true;
        }
        if (keyboard.line.get('product').get('isEditableQty') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableLine'
          });
          return true;
        }
        if (keyboard.line) {
          keyboard.receipt.removeUnit(keyboard.line, value);
          keyboard.receipt.trigger('scan');
        }
        };

    this.addCommand('line:qty', {
      action: function (keyboard, txt) {
        var value = OB.I18N.parseNumber(txt);
        if (value || value === 0) {
          value = value - keyboard.line.get('qty');
          if (value > 0) {
            actionAddProduct(keyboard, value);
          } else if (value < 0) {
            actionRemoveProduct(keyboard, -value);
          } else {
            me.doDeleteLine({
              line: keyboard.line
            });
          }
        }
      }
    });

    this.addCommand('line:price', {
      permission: 'OBPOS_order.changePrice',
      action: function (keyboard, txt) {
        if (keyboard.receipt.get('isEditable') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return true;
        }
        if (keyboard.line.get('product').get('isEditablePrice') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableLine'
          });
          return true;
        }
        if (keyboard.line) {
          keyboard.receipt.setPrice(keyboard.line, OB.I18N.parseNumber(txt));
          keyboard.receipt.trigger('scan');
        }
      }
    });

    this.addCommand('line:dto', {
      permission: 'OBPOS_order.discount',
      action: function (keyboard, txt) {
        if (keyboard.discountsMode) {
          me.doSetDiscountQty({
            qty: OB.I18N.parseNumber(txt)
          });
          return true;
        }
        if (keyboard.receipt.get('isEditable') === false) {
          me.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return true;
        }
        if (keyboard.line) {
          keyboard.receipt.trigger('discount', keyboard.line, OB.I18N.parseNumber(txt));
        }
      }
    });
    this.addCommand('code', new OB.UI.BarcodeActionHandler());
    this.addCommand('+', {
      stateless: true,
      action: function (keyboard, txt) {
        actionAddProduct(keyboard, OB.I18N.parseNumber(txt));
      }
    });
    this.addCommand('-', {
      stateless: true,
      action: function (keyboard, txt) {
        actionRemoveProduct(keyboard, OB.I18N.parseNumber(txt));
      }
    });

    // calling super after setting keyboard properties
    this.inherited(arguments);

    _.each(this.keypads, function (keypadname) {
      this.addKeypad(keypadname);
    }, this);

    this.addToolbarComponent('OB.OBPOSPointOfSale.UI.ToolbarPayment');
    this.addToolbar(OB.OBPOSPointOfSale.UI.ToolbarScan);
    this.addToolbar(OB.OBPOSPointOfSale.UI.ToolbarDiscounts);
  }
});


enyo.kind({
  name: 'OB.UI.AbstractBarcodeActionHandler',
  kind: enyo.Object,
  action: function (keyboard, txt) {
    if (keyboard.receipt.get('isEditable') === false) {
      keyboard.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var me = this;

    this.findProductByBarcode(txt, function (product) {
      me.addProductToReceipt(keyboard, product);
    });
  },

  findProductByBarcode: function (txt, callback) {
    var criteria;

    function successCallbackPrices(dataPrices, dataProducts) {
      if (dataPrices && dataPrices.length !== 0) {
        _.each(dataPrices.models, function (currentPrice) {
          if (dataProducts.get(currentPrice.get('product'))) {
            dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
          }
        });
        _.each(dataProducts.models, function (currentProd) {
          if (currentProd.get('price') === undefined) {
            var price = new OB.Model.ProductPrice({
              'standardPrice': 0
            });
            dataProducts.get(currentProd.get('id')).set('price', price);
            OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
          }
        });
      } else {
        OB.UTIL.showWarning("OBDAL No prices found for products");
        _.each(dataProducts.models, function (currentProd) {
          var price = new OB.Model.ProductPrice({
            'standardPrice': 0
          });
          currentProd.set('price', price);
        });
      }
      callback(new Backbone.Model(dataProducts.at(0)));
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        criteria = {
          'priceListVersion': OB.POS.modelterminal.get('pricelistversion').id
        };
        OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, errorCallback, dataProducts);
      } else {
        // 'UPC/EAN code not found'
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_KbUPCEANCodeNotFound', [txt]));
      }
    }

    criteria = {
      'uPCEAN': txt
    };
    OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
  },

  addProductToReceipt: function (keyboard, product) {
    keyboard.doAddProduct({
      product: product
    });
    keyboard.receipt.trigger('scan');
  }
});

enyo.kind({
  // Overwrite this component to customize the BarcodeActionHandler
  name: 'OB.UI.BarcodeActionHandler',
  kind: 'OB.UI.AbstractBarcodeActionHandler'
});