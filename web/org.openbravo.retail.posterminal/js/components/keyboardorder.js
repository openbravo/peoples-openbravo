/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $, _, Backbone */


enyo.kind({
  name: 'OB.UI.KeyboardOrder',
  kind: 'OB.UI.Keyboard',
  sideBarEnabled: true,


  init: function() {
    this.inherited(arguments);
    //this.products = this.options.root.DataProductPrice;
    this.receipt = this.owner.model.get('order');
    this.line = null;

    this.receipt.get('lines').on('selected', function(line) {
      this.line = line;
      //TODO:do this   this.clear();
    }, this);
  },
  initComponents: function() {
    this.addCommand('line:qty', {
      action: function(txt) {
        if (this.line) {
          this.receipt.setUnit(this.line, OB.I18N.parseNumber(txt));
          this.receipt.trigger('scan');
        }
      }
    });

    this.addCommand('line:price', {
      permission: 'OBPOS_order.changePrice',
      action: function(txt) {
        console.log('price', txt);
        if (this.line) {
          this.receipt.setPrice(this.line, OB.I18N.parseNumber(txt));
          this.receipt.trigger('scan');
        }
      }
    });
    this.addCommand('line:dto', {
      permission: 'OBPOS_order.discount',
      action: function(txt) {
        console.log('dto', txt);
        if (this.line) {
          this.receipt.trigger('discount', this.line, OB.I18N.parseNumber(txt));
        }
      }
    });
    this.addCommand('code', {
      action: function(txt) {
        console.log('code', txt);
        var criteria, me = this;

        function successCallbackPrices(dataPrices, dataProducts) {
          if (dataPrices) {
            _.each(dataPrices.models, function(currentPrice) {
              if (dataProducts.get(currentPrice.get('product'))) {
                dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
              }
            });
            _.each(dataProducts.models, function(currentProd) {
              if (currentProd.get('price') === undefined) {
                var price = new OB.Model.ProductPrice({
                  'listPrice': 0
                });
                dataProducts.get(currentProd.get('id')).set('price', price);
                OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
              }
            });
          } else {
            OB.UTIL.showWarning("OBDAL No prices found for products");
            _.each(dataProducts.models, function(currentProd) {
              var price = new OB.Model.ProductPrice({
                'listPrice': 0
              });
              currentProd.set('price', price);
            });
          }
          me.receipt.addProduct(new Backbone.Model(dataProducts.at(0)));
          me.receipt.trigger('scan');
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
      }
    });
    this.addCommand('+', {
      stateless: true,
      action: function(txt) {
        console.log('+', txt);
        if (this.line) {
          this.receipt.addUnit(this.line, OB.I18N.parseNumber(txt));
          this.receipt.trigger('scan');
        }
      }
    });
    this.addCommand('-', {
      stateless: true,
      action: function(txt) {
        console.log('-', txt);
        if (this.line) {
          this.receipt.removeUnit(this.line, OB.I18N.parseNumber(txt));
          this.receipt.trigger('scan');
        }
      }
    });

    // calling super after setting keyboard properties
    this.inherited(arguments);

    this.addKeypad('OB.UI.KeypadCoins');
    this.addToolbarComponent('OB.UI.ToolbarPayment');
    this.addToolbar(OB.UI.ToolbarScan);
  }
});

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.KeyboardOrder = OB.COMP.Keyboard.extend({
    initialize: function() {
      this.addCommand('line:qty', {
        'action': function(txt) {
          if (this.line) {
            this.receipt.setUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:price', {
        'permission': 'OBPOS_order.changePrice',
        'action': function(txt) {
          if (this.line) {
            this.receipt.setPrice(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('line:dto', {
        'permission': 'OBPOS_order.discount',
        'action': function(txt) {
          if (this.line) {
            this.receipt.trigger('discount', this.line, OB.I18N.parseNumber(txt));
          }
        }
      });
      this.addCommand('code', {
        'action': function(txt) {
          var criteria, me = this;

          function successCallbackPrices(dataPrices, dataProducts) {
            if (dataPrices) {
              _.each(dataPrices.models, function(currentPrice) {
                if (dataProducts.get(currentPrice.get('product'))) {
                  dataProducts.get(currentPrice.get('product')).set('price', currentPrice);
                }
              });
              _.each(dataProducts.models, function(currentProd) {
                if (currentProd.get('price') === undefined) {
                  var price = new OB.Model.ProductPrice({
                    'listPrice': 0
                  });
                  dataProducts.get(currentProd.get('id')).set('price', price);
                  OB.UTIL.showWarning("No price found for product " + currentProd.get('_identifier'));
                }
              });
            } else {
              OB.UTIL.showWarning("OBDAL No prices found for products");
              _.each(dataProducts.models, function(currentProd) {
                var price = new OB.Model.ProductPrice({
                  'listPrice': 0
                });
                currentProd.set('price', price);
              });
            }
            me.receipt.addProduct(new Backbone.Model(dataProducts.at(0)));
            me.receipt.trigger('scan');
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
        }
      });
      this.addCommand('+', {
        'stateless': true,
        'action': function(txt) {
          if (this.line) {
            this.receipt.addUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });
      this.addCommand('-', {
        'stateless': true,
        'action': function(txt) {
          if (this.line) {
            this.receipt.removeUnit(this.line, OB.I18N.parseNumber(txt));
            this.receipt.trigger('scan');
          }
        }
      });

      this.products = this.options.root.DataProductPrice;
      this.receipt = this.options.root.modelorder;
      this.line = null;

      this.receipt.get('lines').on('selected', function(line) {
        this.line = line;
        this.clear();
      }, this);

      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
      // First the keypads
      this.addKeypad(OB.COMP.KeypadCoins);

      // Toolbars at the end...
      this.addToolbarView('toolbarpayment', OB.UI.ToolbarPayment); // after the keypad coins because the toolbar payment depends on the coins keypad.
      this.addToolbarView('toolbarscan', OB.COMP.ToolbarScan);

    }
  });

}());