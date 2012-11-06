/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_,moment,Backbone,localStorage */

(function () {
  // Sales.OrderLine Model
  var OrderLine = Backbone.Model.extend({
    defaults: {
      product: null,
      productidentifier: null,
      uOM: null,
      qty: OB.DEC.Zero,
      price: OB.DEC.Zero,
      priceList: OB.DEC.Zero,
      gross: OB.DEC.Zero,
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
        this.set('promotions', attributes.promotions);
        if (attributes.product && attributes.product.price) {
          this.set('grossListPrice', attributes.product.price.standardPrice);
        }
      }
    },

    printQty: function () {
      return this.get('qty').toString();
    },

    printPrice: function () {
      return OB.I18N.formatCurrency(this.get('_price') || this.get('price'));
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
      this.set('gross', OB.DEC.mul(this.get('qty'), this.get('price')));
    },

    getGross: function () {
      return this.get('gross');
    },

    printGross: function () {
      return OB.I18N.formatCurrency(this.get('_gross') || this.getGross());
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
    model: OrderLine
  });

  // Sales.Payment Model
  var PaymentLine = Backbone.Model.extend({
    defaults: {
      'amount': OB.DEC.Zero,
      'origAmount': OB.DEC.Zero,
      'paid': OB.DEC.Zero // amount - change...
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
        this.set('isEditable', attributes.isEditable);
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
      this.adjustPrices();
      this.calculateTaxes(callback);
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
          grossListPrice = new BigDecimal(line.get('priceList').toString());
          grossUnitPrice = new BigDecimal(price.toString());
          discountPercentage = grossListPrice.subtract(grossUnitPrice).multiply(new BigDecimal('100')).divide(grossListPrice, 2, BigDecimal.prototype.ROUND_HALF_EVEN);
          discountPercentage = parseFloat(discountPercentage.setScale(2, BigDecimal.prototype.ROUND_HALF_EVEN).toString(), 10);
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

        line.set({
          grossUnitPrice: price,
          lineGrossAmount: gross
        }, {
          silent: true
        });
      }, this);
    },
    getTotal: function () {
      return this.getGross();
    },

    printTotal: function () {
      return OB.I18N.formatCurrency(this.getTotal());
    },

    calculateGross: function () {
      var gross = this.get('lines').reduce(function (memo, e) {
        var grossLine = e.getGross();
        if (e.get('promotions')) {
          grossLine = e.get('promotions').reduce(function (memo, e) {
            return OB.DEC.sub(memo, e.actualAmt || e.amt || 0);
          }, grossLine);
        }
        return OB.DEC.add(memo, grossLine);
      }, OB.DEC.Zero);
      this.set('gross', gross);
      this.trigger('calculategross');
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
      this.set('gross', OB.DEC.Zero);
      this.trigger('calculategross');
      this.set('hasbeenpaid', 'N');
      this.set('isbeingprocessed', 'N');
      this.set('description', '');
      this.set('print', true);
      this.set('sendEmail', false);
      this.set('isPaid', false);
      this.set('isEditable', true);
    },

    clearWith: function (_order) {
      var me = this,
          undf;
      this.set('isPaid', _order.get('isPaid'));
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
    },

    addProduct: function (p) {
      var me = this;
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
            me.createLine(p, data.result);
          }
        });
      } else {
        if (p.get('groupProduct')) {
          var line = this.get('lines').find(function (l) {
            return l.get('product').id === p.id;
          });
          if (line) {
            this.addUnit(line);
            line.trigger('selected', line);
          } else {
            this.createLine(p, 1);
          }
        } else {
          this.createLine(p, 1);
        }
      }
      this.save();
    },

    addPromotion: function (line, rule, discount) {
      var promotions = line.get('promotions') || [],
          disc = {},
          i, replaced = false;

      disc.name = discount.name || rule.get('printName') || rule.get('name');
      disc.ruleId = rule.id;
      disc.amt = discount.amt;
      disc.actualAmt = discount.actualAmt;

      disc.hidden = discount.hidden === true || (discount.actualAmt && !disc.amt);

      if (disc.hidden) {
        disc.displayedTotalAmount = 0;
      } else {
        disc.displayedTotalAmount = disc.amt || discount.actualAmt;
      }

      disc.applyNext = rule.get('applyNext');
      disc._idx = rule.get('_idx');

      for (i = 0; i < promotions.length; i++) {
        if (disc._idx < promotions[i]._idx) {
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

    createLine: function (p, units) {
      var me = this;
      var newline = new OrderLine({
        product: p,
        uOM: p.get('uOM'),
        qty: OB.DEC.number(units),
        price: OB.DEC.number(p.get('price').get('standardPrice')),
        priceList: OB.DEC.number(p.get('price').get('standardPrice'))
      });
      newline.calculateGross();

      // add the created line
      this.get('lines').add(newline);
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

    setOrderTypeReturn: function () {
      if (OB.POS.modelterminal.hasPermission('OBPOS_receipt.return')) {
        this.set('documentType', OB.POS.modelterminal.get('terminal').documentTypeForReturns);
        this.set('orderType', 1); // 0: Sales order, 1: Return order
        this.save();

        // remove promotions
        OB.Model.Discounts.applyPromotions(this);
      }
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
        var successCallbackPrices = function (dataPrices, line) {
            dataPrices.each(function (price) {
              order.setPrice(line, price.get('listPrice'));
            });
            }
            
        criteria = {
          'priceListVersion': OB.POS.modelterminal.get('pricelistversion').id,
          'product': line.get('product').get('id')
        };
        OB.Dal.find(OB.Model.ProductPrice, criteria, successCallbackPrices, function () {
          console.error(args);
        }, line);
      });
    },

    createQuotation: function () {
      if (OB.POS.modelterminal.hasPermission('OBPOS_receipt.quotation')) {
        this.set('isQuotation', true);
        this.set('documentType', OB.POS.modelterminal.get('terminal').documentTypeForQuotations);
        this.save();
      }
    },

    createOrderFromQuotation: function (updatePrices) {
      var documentseq;
      this.set('id', null);
      this.set('isQuotation', false);
      this.set('documentType', OB.POS.modelterminal.get('terminal').documentType);
      this.set('hasbeenpaid', 'N');
      this.set('isEditable', true);
      this.set('orderDate', new Date());
      documentseq = OB.POS.modelterminal.get('documentsequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 5);
      OB.POS.modelterminal.set('documentsequence', documentseq);
      this.set('documentNo', OB.POS.modelterminal.get('terminal').docNoPrefix + '/' + documentseqstr);
      this.save();
      if (updatePrices) {
        this.updatePrices();
        OB.Model.Discounts.applyPromotions(this);
      }
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationCreatedOrder'));
    },
    reactivateQuotation: function () {
      this.set('hasbeenpaid', 'N');
      this.save();
    },

    rejectQuotation: function () {
      alert('reject!!')
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
      var pcash;

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        if (p.get('rate') && p.get('rate') !== '1') {
          p.set('origAmount', OB.DEC.mul(p.get('amount'), p.get('rate')));
        } else {
          p.set('origAmount', p.get('amount'));
        }
        p.set('paid', p.get('origAmount'));
        if (p.get('kind') === 'OBPOS_payment.cash') {
          cash = OB.DEC.add(cash, p.get('origAmount'));
          pcash = p;
        } else if (p.get('kind').indexOf('.cash', p.get('kind').length - '.cash'.length) !== -1) {
          origCash = OB.DEC.add(origCash, p.get('origAmount'));
          pcash = p;
        } else {
          nocash = OB.DEC.add(nocash, p.get('origAmount'));
        }
      }

      // Calculation of the change....
      //FIXME
      if (pcash) {
        if (pcash.get('kind') !== 'OBPOS_payment.cash') {
          auxCash = origCash;
          prevCash = cash;
        } else {
          auxCash = cash;
          prevCash = origCash;
        }
        if (OB.DEC.compare(nocash - total) > 0) {
          pcash.set('paid', OB.DEC.Zero);
          this.set('payment', nocash);
          this.set('change', auxCash);
        } else if (OB.DEC.compare(OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash), origCash), total)) > 0) {
          pcash.set('paid', OB.DEC.sub(total, OB.DEC.add(nocash, prevCash)));
          this.set('payment', total);
          this.set('change', OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash), origCash), total));
        } else {
          pcash.set('paid', auxCash);
          this.set('payment', OB.DEC.add(OB.DEC.add(nocash, cash), origCash));
          this.set('change', OB.DEC.Zero);
        }
      } else {
        this.set('payment', nocash);
        this.set('change', OB.DEC.Zero);
      }
    },

    addPayment: function (payment) {
      var i, max, p;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        alert(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }

      if (!OB.POS.modelterminal.hasPayment(payment.get('kind'))) {
        alert(OB.I18N.getLabel('OBPOS_MsgPaymentTypeError'));
        return;
      }

      var payments = this.get('payments');

      if (!payment.get('paymentData')) {
        // search for an existing payment only if there is not paymentData info.
        // this avoids to merge for example card payments of different cards.
        for (i = 0, max = payments.length; i < max; i++) {
          p = payments.at(i);
          if (p.get('kind') === payment.get('kind')) {
            p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
            if (p.get('rate') && p.get('rate') !== '1') {
              p.set('origAmount', OB.DEC.add(payment.get('origAmount'), OB.DEC.mul(p.get('origAmount'), p.get('rate'))));
            }
            this.adjustPayment();
            return;
          }
        }
      }
      payments.add(payment);
      this.adjustPayment();
    },

    removePayment: function (payment) {
      var payments = this.get('payments');
      payments.remove(payment);
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
          item.gross = -item.gross;
          item.net = -item.net;
          item.qty = -item.qty;
        });
        _.forEach(jsonorder.payments, function (item) {
          item.amount = -item.amount;
          item.paid = -item.paid;
        });
        _.forEach(jsonorder.taxes, function (item) {
          item.amount = -item.amount;
          item.net = -item.net;
        });
      }

      return jsonorder;
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
      this.current = null;
    },

    newOrder: function () {
      var order = new Order(),
          me = this,
          documentseq, documentseqstr;

      order.set('client', OB.POS.modelterminal.get('terminal').client);
      order.set('organization', OB.POS.modelterminal.get('terminal').organization);
      order.set('createdBy', OB.POS.modelterminal.get('orgUserId'));
      order.set('updatedBy', OB.POS.modelterminal.get('orgUserId'));
      order.set('documentType', OB.POS.modelterminal.get('terminal').documentType);
      order.set('orderType', 0); // 0: Sales order, 1: Return order
      order.set('generateInvoice', false);
      order.set('isQuotation', false);
      order.set('oldId', null);
      order.set('session', OB.POS.modelterminal.get('session'));
      order.set('priceList', OB.POS.modelterminal.get('terminal').priceList);
      order.set('currency', OB.POS.modelterminal.get('terminal').currency);
      order.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('terminal')['currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
      order.set('warehouse', OB.POS.modelterminal.get('terminal').warehouse);
      order.set('salesRepresentative', OB.POS.modelterminal.get('context').user.id);
      order.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('context').user._identifier);
      order.set('posTerminal', OB.POS.modelterminal.get('terminal').id);
      order.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.POS.modelterminal.get('terminal')._identifier);
      order.set('orderDate', new Date());
      order.set('isPaid', false);

      documentseq = OB.POS.modelterminal.get('documentsequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 5);
      OB.POS.modelterminal.set('documentsequence', documentseq);
      order.set('documentNo', OB.POS.modelterminal.get('terminal').docNoPrefix + '/' + documentseqstr);

      order.set('bp', OB.POS.modelterminal.get('businessPartner'));
      order.set('print', true);
      order.set('sendEmail', false);
      return order;
    },

    newPaidReceipt: function (model, callback) {
      var order = new Order(),
          lines, me = this,
          documentseq, documentseqstr, bp, newline, lines, prod, payments, curPayment, taxes, bpId, numberOfLines = model.receiptLines.length;
      lines = new Backbone.Collection();
      order.set('documentNo', model.documentNo);
      if (model.isQuotation) {
        order.set('isQuotation', true);
        order.set('oldId', model.orderid);
      } else {
        order.set('isPaid', true);
      }
      order.set('isEditable', false);
      order.set('id', model.orderid);
      order.set('client', model.client);
      order.set('documentType', model.documenttype);
      order.set('organization', model.organization);
      order.set('posTerminal', model.posterminal);
      order.set('posTerminal$_identifier', model.posterminalidentifier);
      order.set('warehouse', model.warehouse);
      order.set('currency', model.currency);
      order.set('priceList', model.pricelist);
      order.set('salesRepresentative', model.salesRepresentative);
      order.set('currency$_identifier', model.currency_identifier);
      order.set('isbeingprocessed', 'N');
      order.set('hasbeenpaid', 'Y');


      bpId = model.businessPartner;
      OB.Dal.get(OB.Model.BusinessPartner, bpId, function (bp) {
        order.set('bp', bp);
      }, function () {
        console.error(args);
      })
      order.set('gross', model.totalamount);
      order.trigger('calculategross');
      order.set('salesRepresentative$_identifier', model.salesrepresentative_identifier);

      _.each(model.receiptLines, function (iter) {

        OB.Dal.get(OB.Model.Product, iter.id, function (prod) {
          newline = new OrderLine({
            product: prod,
            uOM: iter.uOM,
            qty: OB.DEC.number(iter.quantity),
            price: OB.DEC.number(iter.unitPrice),
            priceList: OB.DEC.number(iter.unitPrice),
            promotions: iter.promotions
          });
          newline.set('gross', iter.linegrossamount);
          newline.set('grossListPrice', iter.unitPrice);
          // add the created line
          lines.add(newline);
          numberOfLines--;
          if (numberOfLines === 0) {
            order.set('lines', lines);
            callback(order);
          }
        })
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

    addNewOrder: function () {
      this.saveCurrent();
      this.current = this.newOrder();
      this.add(this.current);
      this.loadCurrent(true);
    },
    addPaidReceipt: function (model) {
      this.saveCurrent();
      this.current = model;
      this.add(this.current);
      this.loadCurrent(true);
    },

    addNewQuotation: function () {
      var documentseq;
      this.saveCurrent();
      this.current = this.newOrder();
      this.current.set('isQuotation', true);
      this.current.set('documentType', OB.POS.modelterminal.get('terminal').documentTypeForQuotations);
      documentseq = OB.POS.modelterminal.get('quotationDocumentSequence') + 1;
      documentseqstr = OB.UTIL.padNumber(documentseq, 5);
      OB.POS.modelterminal.set('quotationDocumentSequence', documentseq);
      this.current.set('documentNo', OB.POS.modelterminal.get('terminal').quotationDocNoPrefix + '/' + documentseqstr);
      this.add(this.current);
      this.loadCurrent();
    },

    deleteCurrent: function () {
      var isNew = false;

      function deleteCurrentFromDatabase(orderToDelete) {
        OB.Dal.remove(orderToDelete, function () {
          return true;
        }, function () {
          OB.UTIL.showError('Error removing');
        });
      }

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

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.OrderLine = OrderLine;
  window.OB.Collection.OrderLineList = OrderLineList;
  window.OB.Model.PaymentLine = PaymentLine;
  window.OB.Collection.PaymentLineList = PaymentLineList;
  window.OB.Model.Order = Order;
  window.OB.Collection.OrderList = OrderList;

}());