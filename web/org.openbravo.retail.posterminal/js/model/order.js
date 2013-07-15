/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_,moment,Backbone,localStorage, enyo */

(function () {

  // Sales.OrderLine Model
  var OrderLine = Backbone.Model.extend({
    modelName: 'OrderLine',
    defaults: {
      product: null,
      productidentifier: null,
      uOM: null,
      qty: OB.DEC.Zero,
      price: OB.DEC.Zero,
      priceList: OB.DEC.Zero,
      gross: OB.DEC.Zero,
      net: OB.DEC.Zero,
      description: ''
    },

    initialize: function (attributes) {
      if (attributes && attributes.product) {
        this.set('product', new OB.Model.Product(attributes.product));
        this.set('productidentifier', attributes.productidentifier);
        this.set('uOM', attributes.uOM);
        this.set('qty', attributes.qty);
        this.set('price', attributes.price);
        this.set('priceList', attributes.priceList);
        this.set('gross', attributes.gross);
        this.set('net', attributes.net);
        this.set('promotions', attributes.promotions);
        this.set('priceIncludesTax', attributes.priceIncludesTax);
        if (!attributes.grossListPrice && attributes.product && attributes.product.price) {
          this.set('grossListPrice', attributes.product.price.standardPrice);
        }
      }
    },

    getQty: function () {
      return this.get('qty');
    },

    printQty: function () {
      return this.get('qty').toString();
    },

    printPrice: function () {
      return OB.I18N.formatCurrency(this.get('_price') || this.get('nondiscountedprice') || this.get('price'));
    },

    printDiscount: function () {
      var d = OB.DEC.sub(this.get('priceList'), this.get('price'));
      if (OB.DEC.compare(d) === 0) {
        return '';
      } else {
        return OB.I18N.formatCurrency(d);
      }
    },

    calculateGross: function () {
      if (this.get('priceIncludesTax')) {
        this.set('gross', OB.DEC.mul(this.get('qty'), this.get('price')));
      } else {
        this.set('net', OB.DEC.mul(this.get('qty'), this.get('price')));
      }
    },

    getGross: function () {
      return this.get('gross');
    },

    getNet: function () {
      return this.get('net');
    },

    printGross: function () {
      return OB.I18N.formatCurrency(this.get('_gross') || this.getGross());
    },

    printNet: function () {
      return OB.I18N.formatCurrency(this.get('nondiscountednet') || this.getNet());
    },

    isAffectedByPack: function () {
      return _.find(this.get('promotions'), function (promotion) {
        if (promotion.pack) {
          return true;
        }
      }, this);
    },

    stopApplyingPromotions: function () {
      var promotions = this.get('promotions'),
          i;
      if (this.get('promotions')) {
        for (i = 0; i < promotions.length; i++) {
          if (!promotions[i].applyNext) {
            return true;
          }
        }
      }
      return false;
    }
  });

  // Sales.OrderLineCol Model.
  var OrderLineList = Backbone.Collection.extend({
    model: OrderLine,
    isProductPresent: function (product) {
      var result = null;
      if (this.length > 0) {
        result = _.find(this.models, function (line) {
          if (line.get('product').get('id') === product.get('id')) {
            return true;
          }
        }, this);
        if (_.isUndefined(result) || _.isNull(result)) {
          return false;
        } else {
          return true;
        }
      } else {
        return false;
      }
    }
  });

  // Sales.Payment Model
  var PaymentLine = Backbone.Model.extend({
    modelName: 'PaymentLine',
    defaults: {
      'amount': OB.DEC.Zero,
      'origAmount': OB.DEC.Zero,
      'paid': OB.DEC.Zero,
      // amount - change...
      'date': null
    },
    printAmount: function () {
      if (this.get('rate')) {
        return OB.I18N.formatCurrency(OB.DEC.mul(this.get('amount'), this.get('rate')));
      } else {
        return OB.I18N.formatCurrency(this.get('amount'));
      }
    },
    printForeignAmount: function () {
      return '(' + OB.I18N.formatCurrency(this.get('amount')) + ' ' + this.get('isocode') + ')';
    }
  });

  // Sales.OrderLineCol Model.
  var PaymentLineList = Backbone.Collection.extend({
    model: PaymentLine
  });

  // Sales.Order Model.
  var Order = Backbone.Model.extend({
    modelName: 'Order',
    tableName: 'c_order',
    entityName: 'Order',
    source: '',
    dataLimit: 300,
    properties: ['id', 'json', 'session', 'hasbeenpaid', 'isbeingprocessed'],
    propertyMap: {
      'id': 'c_order_id',
      'json': 'json',
      'session': 'ad_session_id',
      'hasbeenpaid': 'hasbeenpaid',
      'isbeingprocessed': 'isbeingprocessed'
    },

    defaults: {
      hasbeenpaid: 'N',
      isbeingprocessed: 'N'
    },

    createStatement: 'CREATE TABLE IF NOT EXISTS c_order (c_order_id TEXT PRIMARY KEY, json CLOB, ad_session_id TEXT, hasbeenpaid TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS c_order',
    insertStatement: 'INSERT INTO c_order(c_order_id, json, ad_session_id, hasbeenpaid, isbeingprocessed) VALUES (?,?,?,?,?)',
    local: true,
    _id: 'modelorder',
    initialize: function (attributes) {
      var orderId;
      if (attributes && attributes.id && attributes.json) {
        // The attributes of the order are stored in attributes.json
        // Makes sure that the id is copied
        orderId = attributes.id;
        attributes = JSON.parse(attributes.json);
        attributes.id = orderId;
      }

      if (attributes && attributes.documentNo) {
        this.set('id', attributes.id);
        this.set('client', attributes.client);
        this.set('organization', attributes.organization);
        this.set('documentType', attributes.documentType);
        this.set('createdBy', attributes.createdBy);
        this.set('updatedBy', attributes.updatedBy);
        this.set('orderType', attributes.orderType); // 0: Sales order, 1: Return order
        this.set('generateInvoice', attributes.generateInvoice);
        this.set('isQuotation', attributes.isQuotation);
        this.set('oldId', attributes.oldId);
        this.set('priceList', attributes.priceList);
        this.set('priceIncludesTax', attributes.priceIncludesTax);
        this.set('currency', attributes.currency);
        this.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('session', attributes.session);
        this.set('warehouse', attributes.warehouse);
        this.set('salesRepresentative', attributes.salesRepresentative);
        this.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('posTerminal', attributes.posTerminal);
        this.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('orderDate', new Date(attributes.orderDate));
        this.set('documentNo', attributes.documentNo);
        this.set('undo', attributes.undo);
        this.set('bp', new Backbone.Model(attributes.bp));
        this.set('lines', new OrderLineList().reset(attributes.lines));
        this.set('payments', new PaymentLineList().reset(attributes.payments));
        this.set('payment', attributes.payment);
        this.set('change', attributes.change);
        this.set('qty', attributes.qty);
        this.set('gross', attributes.gross);
        this.trigger('calculategross');
        this.set('net', attributes.net);
        this.set('taxes', attributes.taxes);
        this.set('hasbeenpaid', attributes.hasbeenpaid);
        this.set('isbeingprocessed', attributes.isbeingprocessed);
        this.set('description', attributes.description);
        this.set('print', attributes.print);
        this.set('sendEmail', attributes.sendEmail);
        this.set('isPaid', attributes.isPaid);
        this.set('isLayaway', attributes.isLayaway);
        this.set('isEditable', attributes.isEditable);
        this.set('openDrawer', attributes.openDrawer);
        _.each(_.keys(attributes), function (key) {
          if (!this.has(key)) {
            this.set(key, attributes[key]);
          }
        }, this);
      } else {
        this.clearOrderAttributes();
      }
    },

    save: function () {
      var undoCopy;
      if (this.attributes.json) {
        delete this.attributes.json; // Needed to avoid recursive inclusions of itself !!!
      }
      undoCopy = this.get('undo');
      this.unset('undo');
      this.set('json', JSON.stringify(this.toJSON()));
      OB.Dal.save(this, function () {}, function () {
        window.console.error(arguments);
      });
      this.set('undo', undoCopy);
    },

    calculateTaxes: function (callback) {
      if (callback) {
        callback();
      }
      this.save();
    },

    prepareToSend: function (callback) {
      var me = this;
      this.calculateTaxes(function () {
        me.adjustPrices();
        callback();
      });
    },

    adjustPrices: function () {
      // Apply calculated discounts and promotions to price and gross prices
      // so ERP saves them in the proper place
      this.get('lines').each(function (line) {
        var price = line.get('price'),
            gross = line.get('gross'),
            totalDiscount = 0,
            grossListPrice, grossUnitPrice, discountPercentage, base;

        // Calculate inline discount: discount applied before promotions
        if (line.get('priceList') !== price) {
          grossListPrice = line.get('priceList');
          grossUnitPrice = new BigDecimal(price.toString());
          if (OB.DEC.compare(grossListPrice) === 0) {
            discountPercentage = OB.DEC.Zero;
          } else {
            discountPercentage = OB.DEC.toBigDecimal(grossListPrice).subtract(grossUnitPrice).multiply(new BigDecimal('100')).divide(OB.DEC.toBigDecimal(grossListPrice), 2, BigDecimal.prototype.ROUND_HALF_UP);
            discountPercentage = parseFloat(discountPercentage.setScale(2, BigDecimal.prototype.ROUND_HALF_UP).toString(), 10);
          }
        } else {
          discountPercentage = OB.DEC.Zero;
        }
        line.set({
          discountPercentage: discountPercentage
        }, {
          silent: true
        });

        // Calculate prices after promotions
        base = line.get('price');
        _.forEach(line.get('promotions') || [], function (discount) {
          var discountAmt = discount.actualAmt || discount.amt || 0;
          discount.basePrice = base;
          discount.unitDiscount = OB.DEC.div(discountAmt, line.get('qty'));
          totalDiscount = OB.DEC.add(totalDiscount, discountAmt);
          base = OB.DEC.sub(base, totalDiscount);
        }, this);

        gross = OB.DEC.sub(gross, totalDiscount);
        price = OB.DEC.div(gross, line.get('qty'));

        if (this.get('priceIncludesTax')) {
          line.set({
            net: OB.DEC.toNumber(line.get('discountedNet')) || OB.DEC.toNumber(line.get('net')),
            netfull: line.get('discountedNetfull') || OB.DEC.div(gross, line.get('linerate')),
            pricenet: line.get('discountedNet') ? OB.DEC.div(line.get('discountedNet'), line.get('qty')) : OB.DEC.div(OB.DEC.div(gross, line.get('linerate')), line.get('qty')),
            grossListPrice: grossListPrice || price,
            grossUnitPrice: price,
            lineGrossAmount: gross
          }, {
            silent: true
          });
        } else {
          line.set({
            nondiscountedprice: line.get('price'),
            nondiscountednet: line.get('net'),
            net: OB.DEC.toNumber(line.get('discountedNet')),
            pricenet: OB.DEC.toNumber(line.get('discountedNetPrice')),
            listPrice: line.get('priceList'),
            price: 0,
            grossListPrice: 0,
            lineGrossAmount: 0
          }, {
            silent: true
          });
        }
      }, this);

      var totalnet = this.get('lines').reduce(function (memo, e) {
        var netLine = e.get('discountedNet');
        if (e.get('netfull')) {
          return memo.add(new BigDecimal(String(e.get('netfull'))));
        } else {
          return memo.add(new BigDecimal(String(e.get('net'))));
        }
      }, new BigDecimal(String(OB.DEC.Zero)));
      totalnet = OB.DEC.toNumber(totalnet);

      this.set('net', totalnet);
    },
    getTotal: function () {
      return this.getGross();
    },
    getNet: function () {
      return this.get('net');
    },

    printTotal: function () {
      return OB.I18N.formatCurrency(this.getTotal());
    },

    calculateGross: function () {
      var me = this;
      if (this.get('priceIncludesTax')) {
        this.calculateTaxes(function () {
          var gross = me.get('lines').reduce(function (memo, e) {
            var grossLine = e.getGross();
            if (e.get('promotions')) {
              grossLine = e.get('promotions').reduce(function (memo, e) {
                return OB.DEC.sub(memo, e.actualAmt || e.amt || 0);
              }, grossLine);
            }
            return OB.DEC.add(memo, grossLine);
          }, OB.DEC.Zero);
          me.set('gross', gross);
          me.adjustPayment();
          me.trigger('calculategross');
          me.trigger('saveCurrent');
        });
      } else {
        this.calculateTaxes(function () {
          //If the price doesn't include tax, the discounted gross has already been calculated
          var gross = me.get('lines').reduce(function (memo, e) {
            var grossLine = e.get('fulldiscountedGross');
            if (grossLine) {
              return memo.add(grossLine);
            } else {
              return memo;
            }
          }, new BigDecimal("0"));
          me.set('gross', OB.DEC.toNumber(gross));
          var net = me.get('lines').reduce(function (memo, e) {
            var netLine = e.get('discountedNet');
            if (netLine) {
              return OB.DEC.add(memo, netLine);
            } else {
              return memo;
            }
          }, OB.DEC.Zero);
          me.set('net', net);
          me.adjustPayment();
          me.trigger('calculategross');
          me.trigger('saveCurrent');
        });
      }
      //total qty
      var qty = this.get('lines').reduce(function (memo, e) {
        var qtyLine = e.getQty();
        return OB.DEC.add(memo, qtyLine);
      }, OB.DEC.Zero);
      this.set('qty', qty);
    },

    getQty: function () {
      return this.get('qty');
    },

    getGross: function () {
      return this.get('gross');
    },

    printGross: function () {
      return OB.I18N.formatCurrency(this.getGross());
    },

    getPayment: function () {
      return this.get('payment');
    },

    getChange: function () {
      return this.get('change');
    },

    getPending: function () {
      return OB.DEC.sub(this.getTotal(), this.getPayment());
    },
    printPending: function () {
      return OB.I18N.formatCurrency(this.getPending());
    },

    getPaymentStatus: function () {
      var total = this.getTotal();
      var pay = this.getPayment();
      return {
        'done': (this.get('lines').length > 0 && OB.DEC.compare(total) >= 0 && OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0),
        'total': OB.I18N.formatCurrency(total),
        'pending': OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.I18N.formatCurrency(OB.DEC.Zero) : OB.I18N.formatCurrency(OB.DEC.sub(total, pay)),
        'change': OB.DEC.compare(this.getChange()) > 0 ? OB.I18N.formatCurrency(this.getChange()) : null,
        'overpayment': OB.DEC.compare(OB.DEC.sub(pay, total)) > 0 ? OB.I18N.formatCurrency(OB.DEC.sub(pay, total)) : null
      };
    },

    // returns true if the order is a Layaway, otherwise false
    isLayaway: function () {
      return this.getOrderType() === 2 || this.getOrderType() === 3 || this.get('isLayaway');
    },

    clear: function () {
      this.clearOrderAttributes();
      this.trigger('change');
      this.trigger('clear');
    },

    clearOrderAttributes: function () {
      this.set('id', null);
      this.set('client', null);
      this.set('organization', null);
      this.set('createdBy', null);
      this.set('updatedBy', null);
      this.set('documentType', null);
      this.set('orderType', 0); // 0: Sales order, 1: Return order
      this.set('generateInvoice', false);
      this.set('isQuotation', false);
      this.set('oldId', null);
      this.set('priceList', null);
      this.set('priceIncludesTax', null);
      this.set('currency', null);
      this.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, null);
      this.set('session', null);
      this.set('warehouse', null);
      this.set('salesRepresentative', null);
      this.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, null);
      this.set('posTerminal', null);
      this.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, null);
      this.set('orderDate', new Date());
      this.set('documentNo', '');
      this.set('undo', null);
      this.set('bp', null);
      this.set('lines', this.get('lines') ? this.get('lines').reset() : new OrderLineList());
      this.set('payments', this.get('payments') ? this.get('payments').reset() : new PaymentLineList());
      this.set('payment', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.set('qty', OB.DEC.Zero);
      this.set('gross', OB.DEC.Zero);
      this.set('net', OB.DEC.Zero);
      this.set('taxes', null);
      this.trigger('calculategross');
      this.set('hasbeenpaid', 'N');
      this.set('isbeingprocessed', 'N');
      this.set('description', '');
      this.set('print', true);
      this.set('sendEmail', false);
      this.set('isPaid', false);
      this.set('isLayaway', false);
      this.set('isEditable', true);
      this.set('openDrawer', false);
      this.set('approvals', []);
    },

    clearWith: function (_order) {
      var me = this,
          undf;
      //we need this data when IsPaid, IsLayaway changes are triggered
      this.set('documentType', _order.get('documentType'));

      this.set('isPaid', _order.get('isPaid'));
      this.set('isLayaway', _order.get('isLayaway'));
      if (!_order.get('isEditable')) {
        // keeping it no editable as much as possible, to prevent
        // modifications to trigger editable events incorrectly
        this.set('isEditable', _order.get('isEditable'));
      }
      _.each(_.keys(_order.attributes), function (key) {
        if (key !== 'isEditable' && _order.get(key) !== undf) {
          if (_order.get(key) === null) {
            me.set(key, null);
          } else if (_order.get(key).at) {
            //collection
            me.get(key).reset();
            _order.get(key).forEach(function (elem) {
              me.get(key).add(elem);
            });
          } else {
            //property
            me.set(key, _order.get(key));
          }
        }
      });
      this.set('isEditable', _order.get('isEditable'));
      this.trigger('calculategross');
      this.trigger('change');
      this.trigger('clear');
    },

    removeUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.sub(line.get('qty'), qty), OB.I18N.getLabel('OBPOS_RemoveUnits', [qty, line.get('product').get('_identifier')]));
    },

    addUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.add(line.get('qty'), qty), OB.I18N.getLabel('OBPOS_AddUnits', [qty, line.get('product').get('_identifier')]));
    },

    setUnit: function (line, qty, text) {

      if (OB.DEC.isNumber(qty)) {
        var oldqty = line.get('qty');
        if (OB.DEC.compare(qty) > 0) {
          if (line.get('product').get('groupProduct') === false) {
            this.addProduct(line.get('product'));
            return true;
          } else {
            var me = this;
            // sets the new quantity
            line.set('qty', qty);
            line.calculateGross();
            // sets the undo action
            this.set('undo', {
              text: text || OB.I18N.getLabel('OBPOS_SetUnits', [line.get('qty'), line.get('product').get('_identifier')]),
              oldqty: oldqty,
              line: line,
              undo: function () {
                line.set('qty', oldqty);
                line.calculateGross();
                me.set('undo', null);
              }
            });
          }
        } else {
          this.deleteLine(line);
        }
        this.adjustPayment();
        this.save();
      }
    },

    setPrice: function (line, price) {

      if (OB.DEC.isNumber(price)) {
        var oldprice = line.get('price');
        if (OB.DEC.compare(price) >= 0) {
          var me = this;
          // sets the new price
          line.set('price', price);
          line.calculateGross();
          // sets the undo action
          this.set('undo', {
            text: OB.I18N.getLabel('OBPOS_SetPrice', [line.printPrice(), line.get('product').get('_identifier')]),
            oldprice: oldprice,
            line: line,
            undo: function () {
              line.set('price', oldprice);
              line.calculateGross();
              me.set('undo', null);
            }
          });
        }
        this.adjustPayment();
      }
      this.save();
    },

    setLineProperty: function (line, property, value) {
      var me = this;
      var index = this.get('lines').indexOf(line);
      this.get('lines').at(index).set(property, value);
    },

    deleteLine: function (line) {
      var me = this;
      var index = this.get('lines').indexOf(line);

      // trigger
      line.trigger('removed', line);

      // remove the line
      this.get('lines').remove(line);
      // set the undo action
      this.set('undo', {
        text: OB.I18N.getLabel('OBPOS_DeleteLine', [line.get('qty'), line.get('product').get('_identifier')]),
        line: line,
        undo: function () {
          me.get('lines').add(line, {
            at: index
          });
          me.calculateGross();
          me.set('undo', null);
        }
      });
      this.adjustPayment();
      this.save();
      this.calculateGross();
    },

    addProduct: function (p, qty, options) {
      var me = this;

      if (enyo.Panels.isScreenNarrow()) {
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_AddLine', [qty ? qty : 1, p.get('_identifier')]));
      }
      if (p.get('ispack')) {
        OB.Model.Discounts.discountRules[p.get('productCategory')].addProductToOrder(this, p);
        return;
      }
      qty = qty || 1;
      if (me.get('isQuotation') && me.get('hasbeenpaid') === 'Y') {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
        return;
      }
      if (p.get('obposScale')) {
        OB.POS.hwserver.getWeight(function (data) {
          if (data.exception) {
            alert(data.exception.message);
          } else if (data.result === 0) {
            alert(OB.I18N.getLabel('OBPOS_WeightZero'));
          } else {
            me.createLine(p, data.result, options);
          }
        });
      } else {
        if (p.get('groupProduct') || (options && options.packId)) {
          var affectedByPack, line = this.get('lines').find(function (l) {
            if (l.get('product').id === p.id) {
              affectedByPack = l.isAffectedByPack();
              if (!affectedByPack) {
                return true;
              } else if (options && options.packId === affectedByPack.ruleId) {
                return true;
              }
            }
          });
          if (line) {
            this.addUnit(line, qty);
            line.trigger('selected', line);
          } else {
            this.createLine(p, qty, options);
          }
        } else {
          this.createLine(p, qty, options);
        }
      }
      this.save();
    },

    addPromotion: function (line, rule, discount) {
      var promotions = line.get('promotions') || [],
          disc = {},
          i, replaced = false;
      disc.name = discount.name || rule.get('printName') || rule.get('name');
      disc.ruleId = rule.id || rule.get('ruleId');
      disc.amt = discount.amt;
      disc.actualAmt = discount.actualAmt;
      disc.pack = discount.pack;
      disc.discountType = rule.get('discountType');
      disc.manual = discount.manual;
      disc.userAmt = discount.userAmt;

      disc.hidden = discount.hidden === true || (discount.actualAmt && !disc.amt);

      if (disc.hidden) {
        disc.displayedTotalAmount = 0;
      } else {
        disc.displayedTotalAmount = disc.amt || discount.actualAmt;
      }

      if (discount.percentage) {
        disc.percentage = discount.percentage;
      }

      if (typeof discount.applyNext !== 'undefined') {
        disc.applyNext = discount.applyNext;
      } else {
        disc.applyNext = rule.get('applyNext');
      }
      disc._idx = discount._idx || rule.get('_idx');

      for (i = 0; i < promotions.length; i++) {
        if (disc._idx !== -1 && disc._idx < promotions[i]._idx) {
          // Trying to apply promotions in incorrect order: recalculate whole line again
          OB.Model.Discounts.applyPromotions(this, line);
          return;
        }
      }

      for (i = 0; i < promotions.length; i++) {
        if (promotions[i].ruleId === rule.id) {
          promotions[i] = disc;
          replaced = true;
          break;
        }
      }

      if (!replaced) {
        promotions.push(disc);
      }

      line.set('promotions', promotions);
      line.trigger('change');
      this.save();
    },

    removePromotion: function (line, rule) {
      var promotions = line.get('promotions'),
          ruleId = rule.id,
          removed = false,
          res = [],
          i;
      if (!promotions) {
        return;
      }

      for (i = 0; i < promotions.length; i++) {
        if (promotions[i].ruleId === rule.id) {
          removed = true;
        } else {
          res.push(promotions[i]);
        }
      }

      if (removed) {
        line.set('promotions', res);
        line.trigger('change');
        this.save();

        // Recalculate promotions for all lines affected by this same rule,
        // because this rule could have prevented other ones to be applied
        this.get('lines').forEach(function (ln) {
          if (ln.get('promotionCandidates')) {
            ln.get('promotionCandidates').forEach(function (candidateRule) {
              if (candidateRule === ruleId) {
                OB.Model.Discounts.applyPromotions(this, line);
              }
            }, this);
          }
        }, this);
      }
    },

    createLine: function (p, units, options) {
      var me = this;
      var newline = new OrderLine({
        product: p,
        uOM: p.get('uOM'),
        qty: OB.DEC.number(units),
        price: OB.DEC.number(p.get('standardPrice')),
        priceList: OB.DEC.number(p.get('standardPrice')),
        priceIncludesTax: this.get('priceIncludesTax')
      });
      newline.calculateGross();

      // add the created line
      this.get('lines').add(newline, options);
      newline.trigger('created', newline);
      // set the undo action
      this.set('undo', {
        text: OB.I18N.getLabel('OBPOS_AddLine', [newline.get('qty'), newline.get('product').get('_identifier')]),
        line: newline,
        undo: function () {
          me.get('lines').remove(newline);
          me.set('undo', null);
        }
      });
      this.adjustPayment();
    },

    setBPandBPLoc: function (businessPartner, showNotif, saveChange) {
      var me = this,
          undef;
      var oldbp = this.get('bp');
      this.set('bp', businessPartner);
      // set the undo action
      if (showNotif === undef || showNotif === true) {
        this.set('undo', {
          text: businessPartner ? OB.I18N.getLabel('OBPOS_SetBP', [businessPartner.get('_identifier')]) : OB.I18N.getLabel('OBPOS_ResetBP'),
          bp: businessPartner,
          undo: function () {
            me.set('bp', oldbp);
            me.set('undo', null);
          }
        });
      }
      if (saveChange) {
        this.save();
      }
    },

    setOrderType: function (permission, orderType) {
      var me = this;
      if (OB.POS.modelterminal.hasPermission(permission)) {
        if (permission === 'OBPOS_receipt.return') {
          this.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns);
        } else {
          this.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentType);
        }
        this.set('orderType', orderType); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
        if (orderType !== 3) { //Void this Layaway, do not need to save
          this.save();
        } else {
          this.set('layawayGross', this.getGross());
          this.set('gross', this.get('payment'));
          this.set('payment', OB.DEC.Zero);
          this.get('payments').reset();
        }
        // remove promotions
        OB.Model.Discounts.applyPromotions(this);
      }
    },

    // returns the ordertype: 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
    getOrderType: function () {
      return this.get('orderType');
    },

    shouldApplyPromotions: function () {
      // Do not apply promotions in return tickets
      return this.get('orderType') !== 1;
    },

    setOrderInvoice: function () {
      if (OB.POS.modelterminal.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', true);
        this.save();
      }
    },

    updatePrices: function () {
      var order = this;
      this.get('lines').each(function (line) {
        var successCallbackPrices, criteria = {
          'product': line.get('product').get('id')
        };
        successCallbackPrices = function (dataPrices, line) {
          dataPrices.each(function (price) {
            order.setPrice(line, price.get('listPrice'));
          });
        };

        OB.Dal.find(OB.Model.Product, criteria, successCallbackPrices, function () {
          // TODO: Report error properly.
        }, line);
      });
    },

    createQuotation: function () {
      if (OB.POS.modelterminal.hasPermission('OBPOS_receipt.quotation')) {
        this.set('isQuotation', true);
        this.set('generateInvoice', false);
        this.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentTypeForQuotations);
        this.save();
      }
    },

    createOrderFromQuotation: function (updatePrices) {
      var documentseq, documentseqstr;
      this.set('id', null);
      this.set('isQuotation', false);
      this.set('generateInvoice', OB.POS.modelterminal.get('terminal').terminalType.generateInvoice);
      this.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentType);
      this.set('createdBy', OB.POS.modelterminal.get('orgUserId'));
      this.set('hasbeenpaid', 'N');
      this.set('isPaid', false);
      this.set('isEditable', true);
      this.set('orderDate', new Date());
      documentseq = OB.POS.modelterminal.get('documentsequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 7);
      OB.POS.modelterminal.set('documentsequence', documentseq);
      this.set('documentNo', OB.POS.modelterminal.get('terminal').docNoPrefix + '/' + documentseqstr);
      this.save();
      if (updatePrices) {
        this.updatePrices();
        OB.Model.Discounts.applyPromotions(this);
      }
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationCreatedOrder'));
      this.trigger('orderCreatedFromQuotation');
    },
    reactivateQuotation: function () {
      this.set('hasbeenpaid', 'N');
      this.set('isEditable', true);
      this.set('createdBy', OB.POS.modelterminal.get('orgUserId'));
      this.set('oldId', this.get('id'));
      this.set('id', null);
      this.save();
    },

    rejectQuotation: function () {
      alert('reject!!');
    },

    resetOrderInvoice: function () {
      if (OB.POS.modelterminal.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', false);
        this.save();
      }
    },
    adjustPayment: function () {
      var i, max, p;
      var payments = this.get('payments');
      var total = this.getTotal();

      var nocash = OB.DEC.Zero;
      var cash = OB.DEC.Zero;
      var origCash = OB.DEC.Zero;
      var auxCash = OB.DEC.Zero;
      var prevCash = OB.DEC.Zero;
      var paidCash = OB.DEC.Zero;
      var pcash;

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        if (p.get('rate') && p.get('rate') !== '1') {
          p.set('origAmount', OB.DEC.mul(p.get('amount'), p.get('rate')));
        } else {
          p.set('origAmount', p.get('amount'));
        }
        p.set('paid', p.get('origAmount'));
        if (p.get('kind') === OB.POS.modelterminal.get('paymentcash')) {
          // The default cash method
          cash = OB.DEC.add(cash, p.get('origAmount'));
          pcash = p;
          paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
        } else if (OB.POS.modelterminal.hasPayment(p.get('kind')) && OB.POS.modelterminal.hasPayment(p.get('kind')).paymentMethod.iscash) {
          // Another cash method
          origCash = OB.DEC.add(origCash, p.get('origAmount'));
          pcash = p;
          paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
        } else {
          nocash = OB.DEC.add(nocash, p.get('origAmount'));
        }
      }

      // Calculation of the change....
      //FIXME
      if (pcash) {
        if (pcash.get('kind') !== OB.POS.modelterminal.get('paymentcash')) {
          auxCash = origCash;
          prevCash = cash;
        } else {
          auxCash = cash;
          prevCash = origCash;
        }
        if (OB.DEC.compare(nocash - total) > 0) {
          pcash.set('paid', OB.DEC.Zero);
          this.set('payment', nocash);
          this.set('change', OB.DEC.add(cash, origCash));
        } else if (OB.DEC.compare(OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash), origCash), total)) > 0) {
          pcash.set('paid', OB.DEC.sub(total, OB.DEC.add(nocash, OB.DEC.sub(paidCash, pcash.get('origAmount')))));
          this.set('payment', total);
          //The change value will be computed through a rounded total value, to ensure that the total plus change
          //add up to the paid amount without any kind of precission loss
          this.set('change', OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash), origCash), OB.Utilities.Number.roundJSNumber(total, 2)));
        } else {
          pcash.set('paid', auxCash);
          this.set('payment', OB.DEC.add(OB.DEC.add(nocash, cash), origCash));
          this.set('change', OB.DEC.Zero);
        }
      } else {
        if (payments.length > 0) {
          if (this.get('payment') === 0 || nocash > 0) {
            this.set('payment', nocash);
          }
        } else {
          this.set('payment', OB.DEC.Zero);
        }
        this.set('change', OB.DEC.Zero);
      }
    },

    addPayment: function (payment) {
      var payments, total;
      var i, max, p;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        alert(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }

      payments = this.get('payments');
      total = this.getTotal();
      if (!payment.get('paymentData')) {
        // search for an existing payment only if there is not paymentData info.
        // this avoids to merge for example card payments of different cards.
        for (i = 0, max = payments.length; i < max; i++) {
          p = payments.at(i);
          if (p.get('kind') === payment.get('kind') && !p.get('isPrePayment')) {
            p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
            if (p.get('rate') && p.get('rate') !== '1') {
              p.set('origAmount', OB.DEC.add(payment.get('origAmount'), OB.DEC.mul(p.get('origAmount'), p.get('rate'))));
            }
            OB.UTIL.adjustPayment(total, this);
            return;
          }
        }
      }
      if (payment.get('openDrawer')) {
        this.set('openDrawer', payment.get('openDrawer'));
      }
      payment.set('date', new Date());
      payments.add(payment);
      OB.UTIL.adjustPayment(total, this);
    },

    overpaymentExists: function () {
      return this.getPaymentStatus().overpayment ? true : false;
    },

    removePayment: function (payment) {
      var payments = this.get('payments');
      payments.remove(payment);
      if (payment.get('openDrawer')) {
        this.set('openDrawer', false);
      }
      this.adjustPayment();
      this.save();
    },

    serializeToJSON: function () {
      // this.toJSON() generates a collection instance for members like "lines"
      // We need a plain array object
      var jsonorder = JSON.parse(JSON.stringify(this.toJSON()));

      // remove not needed members
      delete jsonorder.undo;
      delete jsonorder.json;

      _.forEach(jsonorder.lines, function (item) {
        delete item.product.img;
      });

      // convert returns
      if (jsonorder.orderType === 1) {
        jsonorder.gross = -jsonorder.gross;
        jsonorder.change = -jsonorder.change;
        jsonorder.payment = -jsonorder.payment;
        jsonorder.net = -jsonorder.net;
        _.forEach(jsonorder.lines, function (item) {
          item.lineGrossAmount = -item.lineGrossAmount;
          item.gross = -item.gross;
          item.net = -item.net;
          item.qty = -item.qty;
          item.taxAmount = -item.taxAmount;
          _.forEach(item.taxLines, function (itemtax) {
            itemtax.amount = -itemtax.amount;
            itemtax.net = -itemtax.net;
          });
        });
        _.forEach(jsonorder.payments, function (item) {
          item.amount = -item.amount;
          item.origAmount = -item.origAmount;
          item.paid = -item.paid;
        });
        _.forEach(jsonorder.taxes, function (item) {
          item.amount = -item.amount;
          item.net = -item.net;
        });
      }

      return jsonorder;
    },

    changeSignToShowReturns: function () {
      this.set('change', OB.DEC.mul(this.get('change'), -1));
      this.set('gross', OB.DEC.mul(this.get('gross'), -1));
      this.set('net', OB.DEC.mul(this.get('net'), -1));
      this.set('qty', OB.DEC.mul(this.get('qty'), -1));
      //lines
      _.each(this.get('lines').models, function (line) {
        line.set('gross', OB.DEC.mul(line.get('gross'), -1));
        line.set('qty', OB.DEC.mul(line.get('qty'), -1));
      }, this);

      //payments
      _.each(this.get('payments').models, function (payment) {
        payment.set('amount', OB.DEC.mul(payment.get('amount'), -1));
        payment.set('origAmount', OB.DEC.mul(payment.get('origAmount'), -1));
      }, this);

      //taxes
      _.each(this.get('taxes'), function (tax) {
        tax.amount = OB.DEC.mul(tax.amount, -1);
        tax.gross = OB.DEC.mul(tax.gross, -1);
        tax.net = OB.DEC.mul(tax.net, -1);
      }, this);

    },

    setProperty: function (_property, _value) {
      this.set(_property, _value);
      this.save();
    }
  });

  var OrderList = Backbone.Collection.extend({
    model: Order,

    constructor: function (modelOrder) {
      if (modelOrder) {
        //this._id = 'modelorderlist';
        this.modelorder = modelOrder;
      }
      Backbone.Collection.prototype.constructor.call(this);
    },

    initialize: function () {
      var me = this;
      this.current = null;
      if (this.modelorder) {
        this.modelorder.on('saveCurrent', function () {
          me.saveCurrent();
        });
      }
    },

    newOrder: function () {
      var order = new Order(),
          me = this,
          documentseq, documentseqstr;

      order.set('client', OB.POS.modelterminal.get('terminal').client);
      order.set('organization', OB.POS.modelterminal.get('terminal').organization);
      order.set('createdBy', OB.POS.modelterminal.get('orgUserId'));
      order.set('updatedBy', OB.POS.modelterminal.get('orgUserId'));
      order.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentType);
      order.set('orderType', 0); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
      order.set('generateInvoice', false);
      order.set('isQuotation', false);
      order.set('oldId', null);
      order.set('session', OB.POS.modelterminal.get('session'));
      order.set('priceList', OB.POS.modelterminal.get('terminal').priceList);
      order.set('priceIncludesTax', OB.POS.modelterminal.get('pricelist').priceIncludesTax);
      order.set('generateInvoice', OB.POS.modelterminal.get('terminal').terminalType.generateInvoice);
      order.set('currency', OB.POS.modelterminal.get('terminal').currency);
      order.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('terminal')['currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
      order.set('warehouse', OB.POS.modelterminal.get('terminal').warehouse);
      order.set('salesRepresentative', OB.POS.modelterminal.get('context').user.id);
      order.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('context').user._identifier);
      order.set('posTerminal', OB.POS.modelterminal.get('terminal').id);
      order.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('terminal')._identifier);
      order.set('orderDate', new Date());
      order.set('isPaid', false);
      order.set('isLayaway', false);
      order.set('taxes', null);

      documentseq = OB.POS.modelterminal.get('documentsequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 7);
      OB.POS.modelterminal.set('documentsequence', documentseq);
      order.set('documentNo', OB.POS.modelterminal.get('terminal').docNoPrefix + '/' + documentseqstr);

      order.set('bp', OB.POS.modelterminal.get('businessPartner'));
      order.set('print', true);
      order.set('sendEmail', false);
      order.set('openDrawer', false);
      return order;
    },

    newPaidReceipt: function (model, callback) {
      var order = new Order(),
          lines, me = this,
          documentseq, documentseqstr, bp, newline, prod, payments, curPayment, taxes, bpId, numberOfLines = model.receiptLines.length,
          orderQty = 0;

      // Call orderLoader plugings to adjust remote model to local model first
      // ej: sales on credit: Add a new payment if total payment < total receipt
      // ej: gift cards: Add a new payment for each gift card discount
      _.each(OB.Model.modelLoaders, function (f) {
        f(model);
      });

      //model.set('id', null);
      lines = new Backbone.Collection();

      // set all properties coming from the model
      order.set(model);

      // setting specific properties
      order.set('isbeingprocessed', 'N');
      order.set('hasbeenpaid', 'Y');
      order.set('isEditable', false);
      order.set('checked', model.checked); //TODO: what is this for, where it comes from?
      if (model.isQuotation) {
        order.set('isQuotation', true);
        order.set('oldId', model.orderid);
        order.set('id', null);
        order.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentTypeForQuotations);
      }
      if (model.isLayaway) {
        order.set('isLayaway', true);
        order.set('id', model.orderid);
        order.set('createdBy', OB.POS.terminal.terminal.usermodel.id);
        order.set('documentType', model.documenttypeid);
        order.set('hasbeenpaid', 'N');
        order.set('session', OB.POS.modelterminal.get('session'));
      } else {
        order.set('isPaid', true);
        order.set('id', model.orderid);
        order.set('documentType', model.documenttypeid);
        if (order.get('documentType') === OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns) {
          //return
          order.set('orderType', 1);
        }
      }

      bpId = model.businessPartner;
      OB.Dal.get(OB.Model.BusinessPartner, bpId, function (bp) {
        order.set('bp', bp);
      }, function () {
        // TODO: Report errors properly
      });
      order.set('gross', model.totalamount);
      order.set('net', model.net);
      order.trigger('calculategross');
      order.set('salesRepresentative$_identifier', model.salesrepresentative_identifier);

      _.each(model.receiptLines, function (iter) {
        var price;
        if (order.get('priceIncludesTax')) {
          price = OB.DEC.number(iter.unitPrice);
        } else {
          price = OB.DEC.number(iter.netPrice);
        }

        OB.Dal.get(OB.Model.Product, iter.id, function (prod) {
          newline = new OrderLine({
            product: prod,
            uOM: iter.uOM,
            qty: OB.DEC.number(iter.quantity),
            price: price,
            priceList: price,
            promotions: iter.promotions,
            priceIncludesTax: order.get('priceIncludesTax')
          });
          newline.calculateGross();
          // add the created line
          lines.add(newline);
          numberOfLines--;
          orderQty += iter.quantity;
          if (numberOfLines === 0) {
            order.set('lines', lines);
            order.set('qty', orderQty);
            if (order.get('orderType') === 1) {
              order.changeSignToShowReturns();
            }
            order.set('json', JSON.stringify(order.toJSON()));
            callback(order);
          }
        });
      });
      order.set('orderDate', moment(model.orderDate.toString(), "YYYY-MM-DD").toDate());
      //order.set('payments', model.receiptPayments);
      payments = new PaymentLineList();
      _.each(model.receiptPayments, function (iter) {
        var paymentProp;
        curPayment = new PaymentLine();
        for (paymentProp in iter) {
          if (iter.hasOwnProperty(paymentProp)) {
            curPayment.set(paymentProp, iter[paymentProp]);
          }
        }
        payments.add(curPayment);
      });
      order.set('payments', payments);
      order.adjustPayment();

      taxes = {};
      _.each(model.receiptTaxes, function (iter) {
        var taxProp;
        taxes[iter.taxid] = {};
        for (taxProp in iter) {
          if (iter.hasOwnProperty(taxProp)) {
            taxes[iter.taxid][taxProp] = iter[taxProp];
          }
        }
      });
      order.set('taxes', taxes);


    },
    newDynamicOrder: function (model, callback) {
      var order = new Backbone.Model(),
          undf;
      _.each(_.keys(model), function (key) {
        if (model[key] !== undf) {
          if (model[key] === null) {
            order.set(key, null);
          } else {
            order.set(key, model[key]);
          }
        }
      });
      callback(order);
    },
    addNewOrder: function () {
      this.saveCurrent();
      this.current = this.newOrder();
      this.add(this.current);
      this.loadCurrent(true);
    },

    addFirstOrder: function () {
      OB.POS.modelterminal.set('documentsequence', OB.POS.modelterminal.get('documentsequence') - 1);
      this.addNewOrder();
    },

    addPaidReceipt: function (model) {
      this.saveCurrent();
      this.current = model;
      this.add(this.current);
      this.loadCurrent(true);
      // OB.Dal.save is done here because we want to force to save with the original od, only this time.
      OB.Dal.save(model, function () {}, function () {
        window.console.error(arguments);
      }, model.get('isLayaway'));
    },
    addMultiReceipt: function (model) {
      OB.Dal.save(model, function () {}, function () {
        window.console.error(arguments);
      }, model.get('isLayaway'));
    },

    addNewQuotation: function () {
      var documentseq, documentseqstr;
      this.saveCurrent();
      this.current = this.newOrder();
      OB.POS.modelterminal.set('documentsequence', OB.POS.modelterminal.get('documentsequence') - 1);
      this.current.set('isQuotation', true);
      this.current.set('generateInvoice', false);
      this.current.set('documentType', OB.POS.modelterminal.get('terminal').terminalType.documentTypeForQuotations);
      documentseq = OB.POS.modelterminal.get('quotationDocumentSequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 7);
      OB.POS.modelterminal.set('quotationDocumentSequence', documentseq);
      this.current.set('documentNo', OB.POS.modelterminal.get('terminal').quotationDocNoPrefix + '/' + documentseqstr);
      this.add(this.current);
      this.loadCurrent();
    },
    deleteCurrentFromDatabase: function (orderToDelete) {
      OB.Dal.remove(orderToDelete, function () {
        return true;
      }, function () {
        OB.UTIL.showError('Error removing');
      });
    },
    deleteCurrent: function () {
      var isNew = false;

      if (this.current) {
        this.remove(this.current);
        if (this.length > 0) {
          this.current = this.at(this.length - 1);
        } else {
          this.current = this.newOrder();
          this.add(this.current);
          isNew = true;
        }
        this.loadCurrent(isNew);
      }
    },

    load: function (model) {
      // Workaround to prevent the pending receipts moder window from remaining open
      // when the current receipt is selected from the list
      if (model && this.current && model.get('documentNo') === this.current.get('documentNo')) {
        return;
      }
      this.saveCurrent();
      this.current = model;
      this.loadCurrent();
    },
    saveCurrent: function () {
      if (this.current) {
        this.current.clearWith(this.modelorder);
      }
    },
    loadCurrent: function (isNew) {
      if (this.current) {
        if (isNew) {
          //set values of new attrs in current,
          //this values will be copied to modelOrder
          //in the next instruction
          this.modelorder.trigger('beforeChangeOrderForNewOne', this.current);
        }
        this.modelorder.clearWith(this.current);
      }
    }

  });
  var MultiOrders = Backbone.Model.extend({
    modelName: 'MultiOrders',
    defaults: {
      //isMultiOrders: false,
      multiOrdersList: new Backbone.Collection(),
      total: OB.DEC.Zero,
      payment: OB.DEC.Zero,
      pending: OB.DEC.Zero,
      change: OB.DEC.Zero,
      payments: new Backbone.Collection(),
      openDrawer: false,
      additionalInfo: null
    },
    addPayment: function (payment) {
      var payments, total;
      var i, max, p;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        alert(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }

      payments = this.get('payments');
      total = this.getTotal();
      if (!payment.get('paymentData')) {
        // search for an existing payment only if there is not paymentData info.
        // this avoids to merge for example card payments of different cards.
        for (i = 0, max = payments.length; i < max; i++) {
          p = payments.at(i);
          if (p.get('kind') === payment.get('kind') && !p.get('isPrePayment')) {
            p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
            if (p.get('rate') && p.get('rate') !== '1') {
              p.set('origAmount', OB.DEC.add(payment.get('origAmount'), OB.DEC.mul(p.get('origAmount'), p.get('rate'))));
            }
            OB.UTIL.adjustPayment(total, this);
            return;
          }
        }
      }
      if (payment.get('openDrawer')) {
        this.set('openDrawer', payment.get('openDrawer'));
      }
      payment.set('date', new Date());
      payments.add(payment);
      OB.UTIL.adjustPayment(total, this);
    },
    removePayment: function (payment) {
      var payments = this.get('payments');
      payments.remove(payment);
      if (payment.get('openDrawer')) {
        this.set('openDrawer', false);
      }
      OB.UTIL.adjustPayment(this.get('total'), this);
    },
    getTotal: function () {
      return this.get('total');
    },
    getChange: function () {
      return this.get('change');
    },
    getPayment: function () {
      return this.get('payment');
    },
    getPending: function () {
      return OB.DEC.sub(this.getTotal(), this.getPayment());
    },
    toInvoice: function (status) {
      if (status === false) {
        this.unset('additionalInfo');
        _.each(this.get('multiOrdersList').models, function (order) {
          order.unset('generateInvoice');
        }, this);
        return;
      }
      this.set('additionalInfo', 'I');
      _.each(this.get('multiOrdersList').models, function (order) {
        order.set('generateInvoice', true);
      }, this);
    },
    resetValues: function () {
      //this.set('isMultiOrders', false);
      this.get('multiOrdersList').reset();
      this.set('total', OB.DEC.Zero);
      this.set('payment', OB.DEC.Zero);
      this.set('pending', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.get('payments').reset();
      this.set('openDrawer', false);
      this.set('additionalInfo', null);
    },
    hasDataInList: function () {
      if (this.get('multiOrdersList') && this.get('multiOrdersList').length > 0) {
        return true;
      }
      return false;
    }
  });
  var TaxLine = Backbone.Model.extend();
  OB.Data.Registry.registerModel(OrderLine);
  OB.Data.Registry.registerModel(PaymentLine);

  // order model is not registered using standard Registry method becasue list is
  // becasue collection is specific
  window.OB.Model.Order = Order;
  window.OB.Collection.OrderList = OrderList;
  window.OB.Model.TaxLine = TaxLine;
  window.OB.Model.MultiOrders = MultiOrders;

  window.OB.Model.modelLoaders = [];
}());