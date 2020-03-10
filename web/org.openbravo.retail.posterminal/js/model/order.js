/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, moment, Backbone, enyo, BigDecimal*/

(function() {
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
      description: '',
      attributeValue: '',
      obposCanbedelivered: true
    },

    // When copying a line coming from servers these properties are copied
    // manually
    // the rest are considered extra information coming from modules and are
    // copied verbatim.
    ownProperties: {
      id: true,
      lineId: true,
      product: true,
      productidentifier: true,
      name: true,
      qty: true,
      quantity: true,
      uOM: true,
      price: true,
      unitPrice: true,
      baseNetUnitPrice: true,
      priceList: true,
      priceIncludesTax: true,
      gross: true,
      lineGrossAmount: true,
      grossListPrice: true,
      description: true,
      promotions: true,
      shipmentlines: true,
      relatedLines: true,
      hasRelatedServices: true,
      warehouse: true,
      warehousename: true,
      attributeValue: true
    },

    initialize: function(attributes) {
      if (attributes && attributes.product) {
        this.set('product', new OB.Model.Product(attributes.product));
        this.set('productidentifier', attributes.productidentifier);
        this.set('uOM', attributes.uOM);
        this.set('qty', attributes.qty);
        this.set('price', attributes.price);
        this.set('priceList', attributes.priceList);
        this.set('gross', attributes.gross);
        this.set('lineGrossAmount', attributes.lineGrossAmount);
        this.set('net', attributes.net);
        this.set('promotions', attributes.promotions);
        this.set('priceIncludesTax', attributes.priceIncludesTax);
        this.set('description', attributes.description);
        this.set('attributeValue', attributes.attributeValue);
        this.set('obposCanbedelivered', attributes.obposCanbedelivered);
        this.set('loaded', attributes.loaded);
        if (
          !attributes.grossListPrice &&
          attributes.product &&
          _.isNumber(attributes.priceList)
        ) {
          this.set('grossListPrice', attributes.priceList);
        } else {
          this.set('grossListPrice', attributes.grossListPrice);
        }
        if (attributes.relatedLines && _.isArray(attributes.relatedLines)) {
          this.set('relatedLines', attributes.relatedLines);
        }
        if (!OB.UTIL.isNullOrUndefined(attributes.hasRelatedServices)) {
          this.set('hasRelatedServices', attributes.hasRelatedServices);
        }
      }
    },

    getAttributeValue: function() {
      return this.get('attributeValue');
    },

    getQty: function() {
      return this.get('qty');
    },

    getDeliveredQuantity: function() {
      return this.has('deliveredQuantity')
        ? this.get('deliveredQuantity')
        : OB.DEC.Zero;
    },

    printQty: function() {
      return OB.DEC.toNumber(
        OB.DEC.toBigDecimal(this.get('qty')),
        OB.I18N.qtyScale()
      ).toString();
    },

    printPrice: function() {
      return OB.I18N.formatCurrency(
        this.get('_price') ||
          this.get('nondiscountedprice') ||
          this.get('price')
      );
    },

    isPrintableService: function() {
      var product = this.get('product');
      if (
        product.get('productType') === 'S' &&
        !product.get('isPrintServices')
      ) {
        return false;
      }

      return true;
    },

    getDiscount: function() {
      return this.getTotalAmountOfPromotions();
    },

    printDiscount: function() {
      return OB.I18N.formatCurrency(this.getDiscount());
    },

    // returns the discount to substract in total
    discountInTotal: function() {
      var disc = OB.DEC.mul(
        OB.DEC.sub(this.get('product').get('standardPrice'), this.get('price')),
        this.get('qty')
      );
      // if there is a discount no promotion then total is price*qty
      // otherwise total is price*qty - discount
      if (OB.DEC.compare(disc) === 0) {
        return this.getTotalAmountOfPromotions();
      } else {
        return 0;
      }
    },

    calculateGross: function() {
      // calculate the total amount depending on the tax plan
      // setting the oposite variable to null, ensures that other logic is not using them in a wrong way
      if (this.get('priceIncludesTax')) {
        this.set('net', null, {
          silent: true
        });
        this.set('gross', OB.DEC.mul(this.getQty(), this.get('price')));
        this.set(
          'discountedGross',
          OB.DEC.compare(this.get('gross')) === 0
            ? this.get('gross')
            : OB.DEC.sub(this.get('gross'), this.getDiscount())
        );
        this.set(
          'discountedPrice',
          this.getQty() === 0
            ? 0
            : OB.DEC.div(this.get('discountedGross'), this.getQty())
        );
      } else {
        this.set('gross', null, {
          silent: true
        });
        this.set('net', OB.DEC.mul(this.getQty(), this.get('price')));
        this.set(
          'discountedNet',
          OB.DEC.compare(this.get('net')) === 0
            ? this.get('net')
            : OB.DEC.sub(this.get('net'), this.getDiscount())
        );
        this.set(
          'discountedNetPrice',
          this.getQty() === 0
            ? 0
            : OB.DEC.div(this.get('discountedNet'), this.getQty())
        );
      }
    },

    getGross: function() {
      return this.get('gross');
    },

    getTotalLine: function() {
      if (!OB.UTIL.isNullOrUndefined(this.get('price'))) {
        return (
          OB.DEC.mul(this.get('price'), this.get('qty')) - this.getDiscount()
        );
      }
    },

    getNet: function() {
      return this.get('net');
    },

    printGross: function() {
      return OB.I18N.formatCurrency(this.get('_gross') || this.getGross());
    },

    printNet: function() {
      return OB.I18N.formatCurrency(
        this.get('nondiscountednet') || this.getNet()
      );
    },

    printTotalLine: function() {
      return OB.I18N.formatCurrency(this.getTotalLine());
    },

    getTotalAmountOfPromotions: function() {
      var memo = 0;
      if (this.get('promotions') && this.get('promotions').length > 0) {
        return _.reduce(
          this.get('promotions'),
          function(memo, prom) {
            if (OB.UTIL.isNullOrUndefined(prom.amt)) {
              return memo;
            }
            return memo + prom.amt;
          },
          memo,
          this
        );
      } else {
        return 0;
      }
    },
    isAffectedByPack: function() {
      return _.find(
        this.get('promotions'),
        function(promotion) {
          if (promotion.pack) {
            return true;
          }
        },
        this
      );
    },

    stopApplyingPromotions: function() {
      var promotions = this.get('promotions'),
        i;
      if (promotions) {
        if (
          OB.MobileApp.model.get('terminal').bestDealCase &&
          promotions.length > 0
        ) {
          // best deal case can only apply one promotion per line
          return true;
        }
        for (i = 0; i < promotions.length; i++) {
          if (!promotions[i].manual && !promotions[i].applyNext) {
            return true;
          }
        }
      }
      return false;
    },

    lastAppliedPromotion: function() {
      var promotions = this.get('promotions'),
        i;
      if (this.get('promotions')) {
        for (i = 0; i < promotions.length; i++) {
          if (promotions[i].lastApplied) {
            return promotions[i];
          }
        }
      }
      return null;
    },

    isReturnable: function() {
      if (this.get('product').get('returnable')) {
        return true;
      } else {
        return false;
      }
    }
  });

  // Sales.OrderLineCol Model.
  var OrderLineList = Backbone.Collection.extend({
    model: OrderLine,
    isProductPresent: function(product) {
      var result = null;
      if (this.length > 0) {
        result = _.find(
          this.models,
          function(line) {
            if (line.get('product').get('id') === product.get('id')) {
              return true;
            }
          },
          this
        );
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
      amount: OB.DEC.Zero,
      origAmount: OB.DEC.Zero,
      paid: OB.DEC.Zero,
      // amount - change...
      date: null
    },
    printAmount: function() {
      if (this.get('rate')) {
        return OB.I18N.formatCurrency(
          this.get('origAmount') ||
            OB.DEC.div(this.get('amount'), this.get('mulrate'))
        );
      } else {
        return OB.I18N.formatCurrency(this.get('amount'));
      }
    },
    printForeignAmount: function() {
      return (
        '(' +
        OB.I18N.formatCurrency(this.get('amount')) +
        ' ' +
        this.get('isocode') +
        ')'
      );
    },
    printAmountWithSignum: function(order) {
      var paidReturn =
        !this.get('isPrePayment') &&
        OB.DEC.compare(order.getGross()) !== -1 &&
        order.isNegative();
      // if the ticket is a paid return, new payments must be displayed in negative
      if (paidReturn) {
        return OB.I18N.formatCurrency(OB.DEC.mul(this.printAmount(), -1));
      } else {
        return OB.I18N.formatCurrency(this.printAmount());
      }
    }
  });

  // Sales.OrderLineCol Model.
  var PaymentLineList = Backbone.Collection.extend({
    model: PaymentLine
  });

  // Sales.Order Model.
  var Order = Backbone.Model.extend({
    includeDocNoSeperator: true,
    modelName: 'Order',
    tableName: 'c_order',
    entityName: 'Order',
    source: '',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
    properties: ['id', 'json', 'session', 'hasbeenpaid', 'isbeingprocessed'],
    propertyMap: {
      id: 'c_order_id',
      json: 'json',
      session: 'ad_session_id',
      hasbeenpaid: 'hasbeenpaid',
      isbeingprocessed: 'isbeingprocessed'
    },

    defaults: {
      hasbeenpaid: 'N',
      isbeingprocessed: 'N'
    },

    createStatement:
      'CREATE TABLE IF NOT EXISTS c_order (c_order_id TEXT PRIMARY KEY, json CLOB, ad_session_id TEXT, hasbeenpaid TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS c_order',
    insertStatement:
      'INSERT INTO c_order(c_order_id, json, ad_session_id, hasbeenpaid, isbeingprocessed) VALUES (?,?,?,?,?)',
    local: true,
    _id: 'modelorder',
    initialize: function(attributes) {
      var orderId;
      if (attributes && attributes.id && attributes.json) {
        // The attributes of the order are stored in attributes.json
        // Makes sure that the id is copied
        orderId = attributes.id;
        attributes = JSON.parse(attributes.json);
        attributes.id = orderId;
      }
      var bpModel;
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
        this.set(
          'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
          attributes[
            'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER
          ]
        );
        this.set('session', attributes.session);
        this.set('warehouse', attributes.warehouse);
        this.set('salesRepresentative', attributes.salesRepresentative);
        this.set(
          'salesRepresentative' +
            OB.Constants.FIELDSEPARATOR +
            OB.Constants.IDENTIFIER,
          attributes[
            'salesRepresentative' +
              OB.Constants.FIELDSEPARATOR +
              OB.Constants.IDENTIFIER
          ]
        );
        this.set('posTerminal', attributes.posTerminal);
        this.set(
          'posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
          attributes[
            'posTerminal' +
              OB.Constants.FIELDSEPARATOR +
              OB.Constants.IDENTIFIER
          ]
        );
        this.set('orderDate', OB.I18N.normalizeDate(attributes.orderDate));
        this.set(
          'creationDate',
          OB.I18N.normalizeDate(attributes.creationDate)
        );
        this.set('documentnoPrefix', attributes.documentnoPrefix);
        this.set('quotationnoPrefix', attributes.quotationnoPrefix);
        this.set('returnnoPrefix', attributes.returnnoPrefix);
        this.set('documentnoSuffix', attributes.documentnoSuffix);
        this.set('quotationnoSuffix', attributes.quotationnoSuffix);
        this.set('returnnoSuffix', attributes.returnnoSuffix);
        this.set('documentNo', attributes.documentNo);
        this.setUndo('InitializeAttr', attributes.undo);
        bpModel = new OB.Model.BusinessPartner(attributes.bp);
        bpModel.set(
          'locationModel',
          new OB.Model.BPLocation(attributes.bp.locationModel)
        );
        if (attributes.bp.locationBillModel) {
          bpModel.set(
            'locationBillModel',
            new OB.Model.BPLocation(attributes.bp.locationBillModel)
          );
        }
        this.set('bp', bpModel);
        this.set('lines', new OrderLineList().reset(attributes.lines));
        this.set(
          'orderManualPromotions',
          new OB.Collection.OrderManualPromotionsList().reset(
            attributes.orderManualPromotions
          )
        );
        this.set('payments', new PaymentLineList().reset(attributes.payments));
        if (attributes.canceledorder) {
          this.set(
            'canceledorder',
            new OB.Model.Order(attributes.canceledorder)
          );
        }
        this.set('payment', attributes.payment);
        this.set('paymentWithSign', attributes.paymentWithSign);
        this.set('change', attributes.change);
        this.set('qty', attributes.qty);
        this.set('gross', attributes.gross);
        this.set('net', attributes.net);
        this.set('taxes', attributes.taxes);
        this.set('hasbeenpaid', attributes.hasbeenpaid);
        this.set('isbeingprocessed', attributes.isbeingprocessed);
        this.set('description', attributes.description);
        this.set('attributeValue', attributes.attributeValue);
        this.set('print', attributes.print);
        this.set('sendEmail', attributes.sendEmail);
        this.set('isPaid', attributes.isPaid);
        this.set('isLayaway', attributes.isLayaway);
        this.set('isEditable', attributes.isEditable);
        this.set('openDrawer', attributes.openDrawer);
        this.set('isBeingDiscounted', false);
        this.set('reApplyDiscounts', false);
        this.set('calculateReceiptCallbacks', []);
        this.set('loaded', attributes.loaded);
        this.set('isModified', attributes.isModified);
        this.set(
          'obposPrepaymentamt',
          attributes.obposPrepaymentamt || attributes.gross
        );
        this.set(
          'obposPrepaymentlimitamt',
          attributes.obposPrepaymentlimitamt || attributes.gross
        );
        this.set(
          'obposPrepaymentlaylimitamt',
          attributes.obposPrepaymentlaylimitamt || OB.DEC.Zero
        );
        _.each(
          _.keys(attributes),
          function(key) {
            if (!this.has(key)) {
              this.set(key, attributes[key]);
            }
          },
          this
        );
      } else {
        this.clearOrderAttributes();
      }
    },

    preventOrderSave: function(value) {
      if (value) {
        if (this.has('preventOrderSave')) {
          this.set(
            'preventOrderSave',
            OB.DEC.add(this.get('preventOrderSave'), OB.DEC.One)
          );
        } else {
          this.set('preventOrderSave', OB.DEC.One);
        }
      } else {
        if (this.has('preventOrderSave')) {
          if (
            OB.DEC.compare(
              OB.DEC.sub(this.get('preventOrderSave'), OB.DEC.One)
            ) === 1
          ) {
            this.set(
              'preventOrderSave',
              OB.DEC.sub(this.get('preventOrderSave'), OB.DEC.One)
            );
          } else {
            this.unset('preventOrderSave');
          }
        }
      }
    },

    save: function(callback) {
      if (
        !OB.MobileApp.model.get('preventOrderSave') &&
        !this.get('preventOrderSave') &&
        !this.pendingCalculateReceipt
      ) {
        var undoCopy = this.get('undo'),
          me = this,
          forceInsert = false;

        if (this.get('isBeingClosed')) {
          var diffReceipt = OB.UTIL.diffJson(
            this.serializeToJSON(),
            this.get('json')
          );
          var error = new Error();
          OB.error(
            'The receipt is being save during the closing: ' + diffReceipt
          );
          OB.error('The stack trace is: ' + error.stack);
        }

        var now = new Date();
        this.set('timezoneOffset', now.getTimezoneOffset());

        if (!this.get('id') || !this.id) {
          var uuid = OB.UTIL.get_UUID();
          this.set('id', uuid);
          OB.info('[NewOrder] New order set with id ' + uuid);
          this.id = uuid;
          forceInsert = true;
        }

        this.set('json', JSON.stringify(this.serializeToJSON()));
        if (callback === undefined || !(callback instanceof Function)) {
          callback = function() {};
        }

        if (
          (this.get('isQuotation') && !this.get('isEditable')) ||
          this.get('isCancelling')
        ) {
          if (callback) {
            callback();
          }
        } else {
          OB.Dal.save(
            this,
            function() {
              OB.info(
                '[NewOrder] Order (' +
                  (forceInsert ? 'insert' : 'update') +
                  ') saved in DB with id ' +
                  me.id
              );
              if (callback) {
                callback();
              }
            },
            function() {
              OB.error(arguments);
            },
            forceInsert
          );
        }

        this.setUndo('SaveOrder', undoCopy);
      } else {
        if (callback) {
          callback();
        }
      }
    },

    calculateTaxes: function(callback) {
      var me = this;
      OB.DATA.OrderTaxes(me);
      me.calculateTaxes(callback);
    },
    prepareToSend: function(callback) {
      this.adjustPrices();
      if (!OB.UTIL.isNullOrUndefined(callback)) {
        callback(this);
      }
    },

    adjustPrices: function() {
      // Apply calculated discounts and promotions to price and gross prices
      // so ERP saves them in the proper place
      this.get('lines').each(function(line) {
        var price = line.get('price'),
          gross = line.get('gross'),
          totalDiscount = 0,
          grossListPrice = line.get('grossListPrice') || line.get('priceList'),
          creationPriceList =
            line.get('creationListPrice') ||
            line.get('grossListPrice') ||
            line.get('priceList'),
          grossUnitPrice,
          discountPercentage,
          base;

        // Calculate inline discount: discount applied before promotions
        if (
          (line.get('product').get('standardPrice') &&
            line.get('product').get('standardPrice') !== price) ||
          (_.isNumber(line.get('discountedLinePrice')) &&
            line.get('discountedLinePrice') !==
              line.get('product').get('standardPrice')) ||
          (OB.UTIL.isNullOrUndefined(line.get('discountedLinePrice')) &&
            line.get('grossListPrice') !== line.get('grossUnitPrice'))
        ) {
          grossUnitPrice = new BigDecimal(price.toString());
          if (OB.DEC.compare(grossListPrice) === 0) {
            discountPercentage = OB.DEC.Zero;
          } else {
            discountPercentage = OB.DEC.toBigDecimal(creationPriceList)
              .subtract(grossUnitPrice)
              .multiply(new BigDecimal('100'))
              .divide(
                OB.DEC.toBigDecimal(creationPriceList),
                2,
                BigDecimal.prototype.ROUND_HALF_UP
              );
            discountPercentage = parseFloat(
              discountPercentage
                .setScale(2, BigDecimal.prototype.ROUND_HALF_UP)
                .toString(),
              10
            );
          }
        } else {
          discountPercentage = line.get('discountPercentage')
            ? line.get('discountPercentage')
            : OB.DEC.Zero;
        }
        line.set(
          {
            discountPercentage: discountPercentage
          },
          {
            silent: true
          }
        );

        // Calculate prices after promotions
        base = line.get('price');
        _.forEach(
          line.get('promotions') || [],
          function(discount) {
            var discountAmt = discount.actualAmt || discount.amt || 0;
            discount.basePrice = base;
            discount.unitDiscount = OB.DEC.div(
              discountAmt,
              discount.obdiscQtyoffer || line.get('qty')
            );
            totalDiscount = OB.DEC.add(totalDiscount, discountAmt);
            base = OB.DEC.sub(base, discount.unitDiscount);
          },
          this
        );

        gross = OB.DEC.sub(gross, totalDiscount);
        price = line.get('qty') !== 0 ? OB.DEC.div(gross, line.get('qty')) : 0;

        if (grossListPrice === undefined) {
          grossListPrice = price;
        }

        if (this.get('priceIncludesTax')) {
          line.set(
            {
              net: OB.DEC.toNumber(line.get('discountedNet')),
              pricenet:
                line.get('qty') !== 0
                  ? OB.DEC.div(line.get('discountedNet'), line.get('qty'))
                  : 0,
              listPrice: 0,
              standardPrice: 0,
              grossListPrice: grossListPrice,
              grossUnitPrice: price,
              lineGrossAmount: gross
            },
            {
              silent: true
            }
          );
        } else {
          line.set(
            {
              nondiscountedprice: line.get('price'),
              nondiscountednet: line.get('net'),
              standardPrice: line.get('price'),
              net: line.get('discountedNet'),
              pricenet: OB.DEC.toNumber(line.get('discountedNetPrice')),
              listPrice: line.get('priceList'),
              grossListPrice: 0,
              lineGrossAmount: 0
            },
            {
              silent: true
            }
          );
        }
      }, this);
    },
    getTotal: function() {
      return this.getGross();
    },
    getNet: function() {
      return this.get('net');
    },

    printTotal: function() {
      return OB.I18N.formatCurrency(this.getTotal());
    },

    getLinesByProduct: function(productId) {
      var affectedLines;
      if (this.get('lines') && this.get('lines').length > 0) {
        affectedLines = _.filter(this.get('lines').models, function(line) {
          return line.get('product').id === productId;
        });
      }
      return affectedLines ? affectedLines : null;
    },

    isCalculateGrossLocked: null,

    isCalculateReceiptLocked: null,

    setIsCalculateGrossLockState: function(state) {
      this.isCalculateGrossLocked = state;
    },

    setIsCalculateReceiptLockState: function(state) {
      this.isCalculateReceiptLocked = state;
    },

    calculateGross: function(callback) {
      // check if it's all ok and calculateGross is being called from where it's supposed to
      var stack = OB.UTIL.getStackTrace(
        'Backbone.Model.extend.calculateGross',
        false
      );
      if (
        stack.indexOf('Backbone.Model.extend.calculateGross') > -1 &&
        stack.indexOf('Backbone.Model.extend.calculateReceipt') > -1
      ) {
        OB.error(
          "It's forbidden to use calculateGross from outside of calculateReceipt"
        );
      }

      // verify that the calculateGross is not locked
      if (this.isCalculateGrossLocked === true) {
        OB.error('calculateGross execution is forbidden right now');
        return;
      } else if (
        this.isCalculateGrossLocked !== false &&
        !this.get('belongsToMultiOrder')
      ) {
        OB.error(
          'setting the isCalculateGrossLocked state is mandatory before executing it the first time'
        );
      }

      // verify that there is no other calculatingGross running
      if (this.calculatingGross) {
        this.pendingCalculateGross = true;
        return;
      }

      // verify that the ui receipt is the only one in which calculateGross is executed
      var isTheUIReceipt =
        this === OB.MobileApp.model.receipt ||
        this.get('belongsToMultiOrder') ||
        this.get('ignoreCheckIfIsActiveOrder');
      if (!isTheUIReceipt) {
        OB.error('calculateGross should only be called by the UI receipt');
      }

      this.calculateGrossAndSave(true, callback);
    },

    calculateGrossAndSave: function(save, callback) {
      this.calculatingGross = true;
      var me = this;
      // reset some vital receipt values because, at this point, they are obsolete. do not fire the change event
      me.set(
        {
          net: OB.DEC.Zero,
          gross: OB.DEC.Zero,
          taxes: null,
          qty: OB.DEC.Zero
        },
        {
          silent: true
        }
      );
      var saveAndTriggerEvents = function(gross, save) {
        var now = new Date();
        me.set('timezoneOffset', now.getTimezoneOffset());
        //total qty
        var qty = me.get('lines').reduce(function(memo, e) {
          var qtyLine = e.getQty();
          if (qtyLine > 0) {
            return OB.DEC.add(memo, qtyLine, OB.I18N.qtyScale());
          } else {
            return memo;
          }
        }, OB.DEC.Zero);

        // all attributes are set at once, preventing the change event of each attribute to be fired until all values are set
        me.set({
          gross: gross,
          qty: qty
        });
        me.adjustPayment();
        if (save) {
          me.save(function() {
            // Reset the flag that protects reentrant invocations to calculateGross().
            // And if there is pending any execution of calculateGross(), do it and do not continue.
            me.calculatingGross = false;
            me.calculatingReceipt = false;
            if (me.pendingCalculateGross) {
              me.pendingCalculateGross = false;
              me.calculateGross(callback);
              return;
            }
            me.trigger('calculategross');
            me.trigger('saveCurrent');
            if (callback) {
              callback();
            }
          });
        } else {
          me.calculatingGross = false;
          me.calculatingReceipt = false;
          if (callback) {
            callback();
          }
        }
      };

      if (
        this.get('isEditable') ||
        this.get('forceCalculateTaxes') ||
        this.get('isCancelling')
      ) {
        this.get('lines').forEach(function(line) {
          line.calculateGross();
        });
      }
      this.calculateTaxes(function() {
        var gross, grossLine;
        if (me.get('priceIncludesTax')) {
          gross = me.get('lines').reduce(function(memo, e) {
            grossLine = e.getGross();
            if (e.get('qty') !== 0 && e.get('promotions')) {
              grossLine = e.get('promotions').reduce(function(memo, e) {
                return OB.DEC.sub(
                  memo,
                  e.actualAmt ||
                    OB.DEC.toNumber(
                      OB.DEC.toBigDecimal(e.amt || 0),
                      OB.DEC.getScale()
                    ) ||
                    0
                );
              }, grossLine);
            }
            return OB.DEC.add(memo, grossLine);
          }, OB.DEC.Zero);
        } else {
          // If the price doesn't include tax, the discounted gross has already been calculated
          gross = me.get('lines').reduce(function(memo, e) {
            if (_.isUndefined(e.get('discountedGross'))) {
              return memo;
            }
            grossLine = e.get('discountedGross');
            if (grossLine) {
              return OB.DEC.add(memo, grossLine);
            } else {
              return memo;
            }
          }, OB.DEC.Zero);
        }
        saveAndTriggerEvents(gross, save);
      });
    },

    addToListOfCallbacks: function(callback) {
      if (OB.UTIL.isNullOrUndefined(this.get('calculateReceiptCallbacks'))) {
        this.set('calculateReceiptCallbacks', []);
      }
      if (!OB.UTIL.isNullOrUndefined(callback)) {
        var list = this.get('calculateReceiptCallbacks');
        list.push(callback);
      }
    },

    // This function calculate the promotions, taxes and gross of all the receipt
    calculateReceipt: function(callback, line, forceCalculateReceipt) {
      // verify if we are cloning the receipt
      if (this.get('cloningReceipt')) {
        this.addToListOfCallbacks(callback);
        return;
      }

      // verify that calculateReceipt it's not been executed
      if (this.calculatingReceipt) {
        this.pendingCalculateReceipt = true;
        this.addToListOfCallbacks(callback);
        return;
      }
      // verify that the calculateReceipt is not locked
      if (this.isCalculateReceiptLocked === true) {
        OB.error('calculateReceipt execution is forbidden right now');
        return;
      } else if (
        this.isCalculateReceiptLocked !== false &&
        !this.get('belongsToMultiOrder')
      ) {
        OB.error(
          'setting the isCalculateReceiptLocked state is mandatory before executing it the first time'
        );
      }
      // verify that the ui receipt is the only one in which calculateReceipt is executed
      var isTheUIReceipt =
        this === OB.MobileApp.model.receipt ||
        this.get('belongsToMultiOrder') ||
        this.get('ignoreCheckIfIsActiveOrder');
      if (!isTheUIReceipt) {
        OB.error('calculateReceipt should only be called by the UI receipt');
      }
      // Verify if it's necesary to skip applying the function
      if (this.get('skipCalculateReceipt') && !forceCalculateReceipt) {
        OB.debug('Skipping calculateReceipt function');
        if (callback) {
          callback();
        }
        return;
      }

      const finalCallbacksAndFinish = function() {
        var finishCalculateReceipt = function(callback) {
          me.calculatingReceipt = false;
          OB.MobileApp.view.waterfall('calculatedReceipt');
          OB.UTIL.ProcessController.finish('calculateReceipt', execution);
          me.trigger('calculatedReceipt');
          me.getPrepaymentAmount(function() {
            me.trigger('updatePending');
            if (callback && callback instanceof Function) {
              callback();
            }
          });
        };
        if (
          me.get('calculateReceiptCallbacks') &&
          me.get('calculateReceiptCallbacks').length > 0
        ) {
          var calculateReceiptCallbacks = me
            .get('calculateReceiptCallbacks')
            .slice(0);
          me.unset('calculateReceiptCallbacks');
          finishCalculateReceipt(function() {
            var executeCallback;
            executeCallback = function(listOfCallbacks) {
              if (listOfCallbacks.length === 0) {
                listOfCallbacks = null;
                return;
              }
              var callbackToExe = listOfCallbacks.shift();
              callbackToExe();
              executeCallback(listOfCallbacks);
            };
            executeCallback(calculateReceiptCallbacks);
          });
        } else {
          finishCalculateReceipt();
        }
      };

      const calculateGrossThenCallbacks = function() {
        me.on('calculategross', function() {
          me.off('calculategross');
          if (me.pendingCalculateReceipt) {
            OB.UTIL.ProcessController.finish('calculateReceipt', execution);
            OB.MobileApp.view.waterfall('calculatedReceipt');
            me.trigger('calculatedReceipt');
            me.pendingCalculateReceipt = false;
            me.calculatingReceipt = false;
            me.calculateReceipt();
            return;
          } else {
            finalCallbacksAndFinish();
          }
        });
        me.calculateGross();
      };

      OB.MobileApp.view.waterfall('calculatingReceipt');
      this.trigger('calculatingReceipt');
      this.calculatingReceipt = true;
      var execution = OB.UTIL.ProcessController.start('calculateReceipt');
      this.addToListOfCallbacks(callback);
      var me = this;

      if (
        this.get('skipApplyPromotions') ||
        this.get('cloningReceipt') ||
        me.preventApplyPromotions
      ) {
        calculateGrossThenCallbacks();
      } else {
        OB.Discounts.Pos.calculateDiscounts(this, () =>
          calculateGrossThenCallbacks()
        );
      }
    },

    setDocumentNo: function(isReturn, isOrder) {
      var order = this,
        nextDocumentNo;
      if (order.get('isModified')) {
        return;
      }
      if (
        isOrder &&
        order.get('documentnoPrefix') !==
          OB.MobileApp.model.get('terminal').docNoPrefix
      ) {
        nextDocumentNo = OB.MobileApp.model.getNextDocumentno();
        order.set('returnnoPrefix', -1);
        order.set('returnnoSuffix', -1);
        order.set(
          'documentnoPrefix',
          OB.MobileApp.model.get('terminal').docNoPrefix
        );
        order.set('documentnoSuffix', nextDocumentNo.documentnoSuffix);
        order.set('quotationnoPrefix', -1);
        order.set('quotationnoSuffix', -1);
        order.set('documentNo', nextDocumentNo.documentNo);
        order.trigger('saveCurrent');
      } else if (
        OB.MobileApp.model.get('terminal').returnDocNoPrefix &&
        isReturn
      ) {
        if (
          order.get('returnnoPrefix') !==
          OB.MobileApp.model.get('terminal').returnDocNoPrefix
        ) {
          nextDocumentNo = OB.MobileApp.model.getNextReturnno();
          order.set(
            'returnnoPrefix',
            OB.MobileApp.model.get('terminal').returnDocNoPrefix
          );
          order.set('returnnoSuffix', nextDocumentNo.documentnoSuffix);
          order.set('documentnoPrefix', -1);
          order.set('documentnoSuffix', -1);
          order.set('quotationnoPrefix', -1);
          order.set('quotationnoSuffix', -1);
          order.set('documentNo', nextDocumentNo.documentNo);
          order.trigger('saveCurrent');
        }
      }
    },

    getAttributeValue: function() {
      return this.get('attributeValue');
    },

    getQty: function() {
      return this.get('qty');
    },

    getGross: function() {
      return this.get('gross');
    },

    printGross: function() {
      return OB.I18N.formatCurrency(this.getGross());
    },

    getPayment: function() {
      return this.get('payment');
    },

    getPaymentWithSign: function() {
      return this.get('paymentWithSign');
    },

    isFullyPaid: function() {
      return (
        (!this.isNegative() && this.getPaymentWithSign() >= this.getGross()) ||
        (this.isNegative() && this.getPaymentWithSign() <= this.getGross())
      );
    },

    getNettingPayment: function() {
      return this.has('nettingPayment')
        ? this.get('nettingPayment')
        : OB.DEC.Zero;
    },

    getCredit: function() {
      return this.get('creditAmount');
    },

    getChange: function() {
      return this.get('change');
    },

    getPending: function() {
      var pending;
      if (this.get('prepaymentChangeMode')) {
        var paymentsAmt = _.reduce(
          this.get('payments').models,
          function(memo, payment) {
            return OB.DEC.add(memo, payment.get('origAmount'));
          },
          OB.DEC.Zero
        );
        pending = OB.DEC.abs(OB.DEC.sub(this.getGross(), paymentsAmt));
      } else {
        pending = OB.DEC.abs(
          OB.DEC.sub(this.getGross(), this.getPaymentWithSign())
        );
      }
      return pending;
    },

    getDeliveredQuantityAmount: function() {
      return this.get('deliveredQuantityAmount')
        ? this.get('deliveredQuantityAmount')
        : OB.DEC.Zero;
    },

    printPending: function() {
      return OB.I18N.formatCurrency(this.getPending());
    },

    getInvoiceTerms: function() {
      return this.get('invoiceTerms');
    },

    isNegative: function() {
      var isNegative;
      if (OB.UTIL.isNullOrUndefined(this.get('isNegative'))) {
        var processedPaymentsAmount = OB.DEC.Zero,
          loadedFromBackend = this.get('isLayaway') || this.get('isPaid');
        if (loadedFromBackend) {
          isNegative = OB.DEC.compare(this.getGross()) === -1;
        } else {
          _.each(this.get('payments').models, function(payment) {
            if (payment.get('isPrePayment')) {
              processedPaymentsAmount = OB.DEC.add(
                processedPaymentsAmount,
                payment.get('origAmount')
              );
            }
          });
          processedPaymentsAmount = OB.DEC.add(
            processedPaymentsAmount,
            this.getNettingPayment()
          );
          if (OB.DEC.compare(this.getGross()) === -1) {
            isNegative = processedPaymentsAmount >= this.getGross();
          } else {
            isNegative = processedPaymentsAmount > this.getGross();
          }
          this.set('isNegative', isNegative, {
            silent: true
          });
        }
      } else {
        isNegative = this.get('isNegative');
      }
      return isNegative;
    },

    getPrepaymentAmount: function(callback, ignorePanel) {
      var me = this,
        total = this.getTotal();

      function executeCallback(
        prepaymentAmount,
        prepaymentLimitAmount,
        prepaymentLayawayLimitAmount
      ) {
        me.set('obposPrepaymentamt', prepaymentAmount);
        me.set('obposPrepaymentlimitamt', prepaymentLimitAmount);
        me.set('obposPrepaymentlaylimitamt', prepaymentLayawayLimitAmount);
        me.trigger('saveCurrent');
        if (callback instanceof Function) {
          callback();
        }
      }

      if (
        !ignorePanel &&
        OB.MobileApp.model.get('lastPaneShown') !== 'payment'
      ) {
        if (callback instanceof Function) {
          callback();
        }
        return;
      }

      //Execute the Prepayments Algorithm only if the receipt is a normal ticket or a layaway
      //Otherwise return the total of the receipt so the prepayments logic is not taken into account
      if (
        OB.MobileApp.model.get('terminal').terminalType.calculateprepayments &&
        OB.MobileApp.model.get('terminal').prepaymentAlgorithm &&
        me.get('lines').length > 0
      ) {
        if (
          !this.isNegative() &&
          !this.get('cancelLayaway') &&
          this.get('bp') &&
          this.get('bp').get('id') !==
            OB.MobileApp.model.get('businessPartner').get('id')
        ) {
          OB.UTIL.prepaymentRules[
            OB.MobileApp.model.get('terminal').prepaymentAlgorithm
          ].execute(this, function(
            prepaymentAmount,
            prepaymentLimitAmount,
            prepaymentLayawayLimitAmount
          ) {
            executeCallback(
              prepaymentAmount,
              prepaymentLimitAmount,
              prepaymentLayawayLimitAmount
            );
          });
        } else {
          executeCallback(
            total,
            total,
            OB.DEC.div(
              OB.DEC.mul(
                total,
                OB.MobileApp.model.get('terminal').obposPrepayPercLayLimit
              ),
              100
            )
          );
        }
      } else {
        executeCallback(total, total, OB.DEC.Zero);
      }
    },

    getPaymentStatus: function() {
      var gross = this.getGross(),
        change = this.getChange(),
        nettingPayment = this.getNettingPayment(),
        isReturn = true,
        isReversal = false,
        loadedFromBackend = this.get('isLayaway') || this.get('isPaid'),
        processedPaymentsAmount = OB.DEC.Zero,
        paymentsAmount = OB.DEC.Zero,
        isNegative = this.isNegative(),
        remainingToPay,
        done,
        pending,
        pendingAmt,
        overpayment;

      isReturn =
        this.get('orderType') === 1 ||
        (!_.find(this.get('lines').models, function(line) {
          return OB.DEC.compare(line.get('qty')) !== -1;
        }) &&
          this.get('orderType') !== 3);

      _.each(this.get('payments').models, function(payment) {
        if (payment.get('isPrePayment')) {
          processedPaymentsAmount = OB.DEC.add(
            processedPaymentsAmount,
            payment.get('origAmount')
          );
        } else {
          if (
            loadedFromBackend ||
            !isNegative ||
            payment.get('isReversePayment')
          ) {
            paymentsAmount = OB.DEC.add(
              paymentsAmount,
              payment.get('origAmount')
            );
          } else {
            paymentsAmount = OB.DEC.sub(
              paymentsAmount,
              payment.get('origAmount')
            );
          }
          if (!isReversal && payment.get('reversedPaymentId')) {
            isReversal = true;
          }
        }
      });

      processedPaymentsAmount = OB.DEC.add(
        processedPaymentsAmount,
        nettingPayment
      );

      remainingToPay = OB.DEC.sub(
        gross,
        OB.DEC.add(processedPaymentsAmount, paymentsAmount)
      );

      if (isNegative) {
        // Check if the DONE must be enabled
        done =
          OB.DEC.compare(this.get('lines').length) === 1 &&
          OB.DEC.compare(remainingToPay) !== -1;
        // Check the pending and the pending amount
        pending =
          OB.DEC.compare(remainingToPay) === -1
            ? OB.I18N.formatCurrency(OB.DEC.mul(remainingToPay, -1))
            : OB.I18N.formatCurrency(OB.DEC.Zero);
        pendingAmt =
          OB.DEC.compare(remainingToPay) === -1
            ? OB.DEC.mul(remainingToPay, -1)
            : OB.DEC.Zero;
        // Check the over payment
        overpayment =
          OB.DEC.compare(remainingToPay) === 1
            ? OB.DEC.sub(OB.DEC.abs(remainingToPay), change)
            : OB.DEC.Zero;
      } else {
        // Check if the DONE must be enabled
        done =
          OB.DEC.compare(this.get('lines').length) === 1 &&
          OB.DEC.compare(remainingToPay) !== 1;
        // Check the pending and the pending amount
        pending =
          OB.DEC.compare(remainingToPay) === 1
            ? OB.I18N.formatCurrency(remainingToPay)
            : OB.I18N.formatCurrency(OB.DEC.Zero);
        pendingAmt =
          OB.DEC.compare(remainingToPay) === 1 ? remainingToPay : OB.DEC.Zero;
        // Check the over payment
        overpayment =
          OB.DEC.compare(remainingToPay) === -1
            ? OB.DEC.sub(OB.DEC.abs(remainingToPay), change)
            : OB.DEC.Zero;
      }

      return {
        done: done,
        total: OB.I18N.formatCurrency(gross),
        pending: pending,
        overpayment: overpayment,
        isReturn: isReturn,
        isNegative: isNegative,
        totalAmt: gross,
        pendingAmt: pendingAmt,
        payments: this.get('payments'),
        isReversal: isReversal
      };
    },

    // returns the quantity amount of the synchronized payments
    getPrePaymentQty: function() {
      return _.reduce(
        _.filter(this.get('payments').models, function(payment) {
          return payment.get('isPrePayment');
        }),
        function(memo, pymnt) {
          return OB.DEC.add(
            memo,
            OB.DEC.sub(pymnt.get('origAmount'), pymnt.get('overpayment') || 0)
          );
        },
        OB.DEC.Zero
      );
    },

    // returns true if there is any reversal payment that is not synchronized
    isNewReversed: function() {
      return !_.isUndefined(
        _.find(this.get('payments').models, function(payment) {
          return (
            !payment.get('isPrePayment') && payment.get('isReversePayment')
          );
        })
      );
    },

    // returns true if the reversed quantity has been paid
    isReversedPaid: function() {
      return (
        !this.isNewReversed() ||
        OB.DEC.abs(this.getPrePaymentQty()) <= OB.DEC.abs(this.getPayment())
      );
    },

    // returns true if the order is a Layaway, otherwise false
    isLayaway: function() {
      return (
        this.getOrderType() === 2 ||
        this.getOrderType() === 3 ||
        this.get('isLayaway')
      );
    },

    clear: function() {
      this.clearOrderAttributes();
      this.trigger('change');
      this.trigger('clear');
    },

    clearOrderAttributes: function() {
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
      this.set(
        'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        null
      );
      this.set('session', null);
      this.set('warehouse', null);
      this.set('salesRepresentative', null);
      this.set(
        'salesRepresentative' +
          OB.Constants.FIELDSEPARATOR +
          OB.Constants.IDENTIFIER,
        null
      );
      this.set('posTerminal', null);
      this.set(
        'posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
        null
      );
      this.set('orderDate', OB.I18N.normalizeDate(new Date()));
      this.set('creationDate', null);
      this.set('documentnoPrefix', -1);
      this.set('quotationnoPrefix', -1);
      this.set('returnnoPrefix', -1);
      this.set('documentnoSuffix', -1);
      this.set('quotationnoSuffix', -1);
      this.set('returnnoSuffix', -1);
      this.set('documentNo', '');
      this.set('undo', null);
      this.set('bp', null);
      this.set(
        'lines',
        this.get('lines') ? this.get('lines').reset() : new OrderLineList()
      );
      this.set(
        'orderManualPromotions',
        this.get('orderManualPromotions')
          ? this.get('orderManualPromotions').reset()
          : new OB.Collection.OrderManualPromotionsList()
      );
      this.set(
        'payments',
        this.get('payments')
          ? this.get('payments').reset()
          : new PaymentLineList()
      );
      this.set('payment', OB.DEC.Zero);
      this.set('paymentWithSign', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.set('qty', OB.DEC.Zero);
      this.set('gross', OB.DEC.Zero);
      this.set('net', OB.DEC.Zero);
      this.set('taxes', {});
      this.set('hasbeenpaid', 'N');
      this.set('isbeingprocessed', 'N');
      this.set('description', '');
      this.set('print', true);
      this.set('sendEmail', false);
      this.set('isPaid', false);
      this.set('creditAmount', OB.DEC.Zero);
      this.set('paidPartiallyOnCredit', false);
      this.set('paidOnCredit', false);
      this.set('isLayaway', false);
      this.set('isEditable', true);
      this.set('openDrawer', false);
      this.set('totalamount', null);
      this.set('approvals', []);
      this.set('isPartiallyDelivered', false);
      this.set('isModified', false);
      this.set('obposPrepaymentamt', OB.DEC.Zero);
      this.set('obposPrepaymentlimitamt', OB.DEC.Zero);
      this.set('obposPrepaymentlaylimitamt', OB.DEC.Zero);
      this.set(
        'cashVAT',
        OB.MobileApp.model.get('terminal')
          ? OB.MobileApp.model.get('terminal').cashVat
          : null
      );
    },

    clearWith: function(_order) {
      var execution = OB.UTIL.ProcessController.start('clearWith');

      // verify that the clearWith is not used for any other purpose than to update and fire the events of the UI receipt
      OB.UTIL.Debug.execute(function() {
        var isTheUIReceipt = this.cid === OB.MobileApp.model.receipt.cid;
        if (!isTheUIReceipt) {
          OB.error(
            'The target of the clearWith should only be the UI receipt. Use OB.UTIL.clone instead'
          );
        }
      }, this);

      var idExecution;

      // we set first this property to avoid that the apply promotions is triggered
      this.set('isNewReceipt', _order.get('isNewReceipt'));
      //we need this data when IsPaid, IsLayaway changes are triggered
      this.set('documentType', _order.get('documentType'));
      this.set('isQuotation', _order.get('isQuotation'));

      //Prevent recalculating service relations during executions of clearWith
      this.set('preventServicesUpdate', true);

      this.set('isPaid', _order.get('isPaid'));
      this.set('creditAmount', _order.get('creditAmount'));
      this.set('paidPartiallyOnCredit', _order.get('paidPartiallyOnCredit'));
      this.set('paidOnCredit', _order.get('paidOnCredit'));
      this.set('isLayaway', _order.get('isLayaway'));
      this.set('isPartiallyDelivered', _order.get('isPartiallyDelivered'));
      this.set('isModified', _order.get('isModified'));
      if (!_order.get('isEditable')) {
        // keeping it no editable as much as possible, to prevent
        // modifications to trigger editable events incorrectly
        this.set('isEditable', _order.get('isEditable'));
      }

      if (_order.get('isLayaway')) {
        if (
          OB.MobileApp.model.get('terminal').terminalType.generateInvoice &&
          OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice', true)
        ) {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_retail.restricttaxidinvoice',
              true
            ) &&
            !_order.get('bp').get('taxID')
          ) {
            _order.set('generateInvoice', false);
          } else {
            _order.set('generateInvoice', true);
          }
        }
      }

      if (_order.get('replacedorder_documentNo')) {
        this.set(
          'replacedorder_documentNo',
          _order.get('replacedorder_documentNo')
        );
      }

      if (_order.get('replacedorder')) {
        this.set('replacedorder', _order.get('replacedorder'));
      }

      if (_order.get('canceledorder')) {
        this.set('canceledorder', _order.get('canceledorder'));
      }

      if (_order.get('doCancelAndReplace')) {
        this.set('doCancelAndReplace', _order.get('doCancelAndReplace'));
      }

      // the idExecution is saved so only this execution of clearWith will check cloningReceipt to false
      if (
        OB.UTIL.isNullOrUndefined(this.get('idExecution')) &&
        OB.UTIL.isNullOrUndefined(_order.get('idExecution'))
      ) {
        idExecution = new Date().getTime();
        _order.set('idExecution', idExecution);
        _order.set('cloningReceipt', true);
        this.set('cloningReceipt', true);
        this.set('idExecution', idExecution);
      }

      OB.UTIL.clone(_order, this);

      for (var index = 0; index < this.get('payments').models.length; index++) {
        var payment = this.get('payments').models[index],
          oldPaymentRoundingLine = payment.has('paymentRoundingLine')
            ? payment.get('paymentRoundingLine')
            : null,
          paymentRoundingLine = null;
        if (oldPaymentRoundingLine) {
          paymentRoundingLine = _.find(this.get('payments').models, function(
            pay
          ) {
            if (
              pay.get('kind') === oldPaymentRoundingLine.kind &&
              pay.get('amount') === oldPaymentRoundingLine.amount
            ) {
              return true;
            }
          });
          payment.set('paymentRoundingLine', paymentRoundingLine);
        }
      }

      if (
        !OB.UTIL.isNullOrUndefined(this.get('idExecution')) &&
        this.get('idExecution') === idExecution
      ) {
        _order.set('cloningReceipt', false);
        this.set('cloningReceipt', false);
        _order.unset('idExecution');
        this.unset('idExecution');
      }

      //Enable recalculating service relations after cloning
      this.unset('preventServicesUpdate');

      this.set('isEditable', _order.get('isEditable'));
      this.trigger('change');
      this.trigger('clear');
      OB.UTIL.ProcessController.finish('clearWith', execution);
    },

    removeUnit: function(line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(
        line,
        OB.DEC.sub(line.get('qty'), qty, OB.I18N.qtyScale()),
        OB.I18N.getLabel('OBPOS_RemoveUnits', [
          qty,
          line.get('product').get('_identifier')
        ])
      );
    },

    addUnit: function(line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(
        line,
        OB.DEC.add(line.get('qty'), qty, OB.I18N.qtyScale()),
        OB.I18N.getLabel('OBPOS_AddUnits', [
          OB.DEC.toNumber(new BigDecimal(String(qty.toString()))),
          line.get('product').get('_identifier')
        ])
      );
    },

    setUnit: function(line, qty, text) {
      var permission,
        me = this,
        showProductCard =
          line.get('qty') < 0 &&
          qty > 0 &&
          OB.UTIL.isCrossStoreProduct(line.get('product')),
        params = {};
      if (OB.DEC.isNumber(qty) && qty !== 0) {
        var oldqty = line.get('qty');
        permission = 'OBPOS_ReturnLine';
        if (
          (!OB.MobileApp.model.hasPermission(permission, true) ||
            this.get('isQuotation')) &&
          qty < 0 &&
          oldqty > 0
        ) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddNegative'));
          return;
        }
        if (qty > 0 && oldqty < 0 && this.get('orderType') === 1) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_MsgCannotAddPostiveToReturn')
          );
          return;
        }
        if (this.get('replacedorder')) {
          if (oldqty > 0 && qty < line.get('remainingQuantity')) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_CancelReplaceQtyEdit')
            );
            return;
          } else if (oldqty < 0 && qty > line.get('remainingQuantity')) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn')
            );
            return;
          }
        }
        if (line.get('product').get('groupProduct') === false) {
          this.addProduct(line.get('product'));
          return true;
        } else {
          var setQuantity = function() {
            // sets the new quantity
            line.set('qty', qty);
            if (showProductCard) {
              params.leftSubWindow =
                OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
              params.product = line.get('product');
              params.line = line;
              params.forceSelectStore = true;
              params.warehouse = line.get('warehouse');
              OB.MobileApp.view.$.containerWindow
                .getRoot()
                .showLeftSubWindow({}, params);
            }
          };
          // sets the undo action
          if (this.get('multipleUndo')) {
            var undoText = '',
              oldqtys = [],
              lines = [],
              undo = this.get('undo');
            if (undo && undo.oldqtys) {
              undoText = undo.text + ', ';
              oldqtys = undo.oldqtys;
              lines = undo.lines;
            }
            undoText +=
              text ||
              OB.I18N.getLabel('OBPOS_SetUnits', [
                line.get('qty'),
                line.get('product').get('_identifier')
              ]);
            oldqtys.push(oldqty);
            lines.push(line);
            this.setUndo(
              'EditLine',
              {
                text: undoText,
                oldqtys: oldqtys,
                lines: lines,
                undo: function() {
                  var i,
                    thisUndo = me.get('undo');
                  for (i = 0; i < thisUndo.lines.length; i++) {
                    //Changing the qty of a line modifies the undo attribute, so we need a copy
                    thisUndo.lines[i].set('qty', thisUndo.oldqtys[i]);
                  }
                  me.calculateReceipt();
                  me.set('undo', null);
                }
              },
              setQuantity
            );
          } else {
            this.setUndo(
              'EditLine',
              {
                text:
                  text ||
                  OB.I18N.getLabel('OBPOS_SetUnits', [
                    line.get('qty'),
                    line.get('product').get('_identifier')
                  ]),
                oldqty: oldqty,
                line: line,
                undo: function() {
                  line.set('qty', oldqty);
                  me.calculateReceipt();
                  me.set('undo', null);
                }
              },
              setQuantity
            );
          }
        }
      } else {
        if (line.get('deleteApproved')) {
          // The approval to delete the line has already been granted
          line.unset('deleteApproved');
          this.deleteLinesFromOrder([line]);
        } else {
          // We don't have the approval to delete the line yet; request it
          OB.UTIL.Approval.requestApproval(
            OB.MobileApp.view.$.containerWindow.getRoot().model,
            'OBPOS_approval.deleteLine',
            function(approved) {
              if (approved) {
                me.deleteLinesFromOrder([line]);
              }
            }
          );
        }
      }
    },

    setPrices: function(selectedModels, price, options, callback) {
      var me = this,
        cancelChange = false,
        finalCallback = function() {
          if (callback && callback instanceof Function) {
            callback();
          }
        };
      if (selectedModels.length > 1) {
        var setNextPrice;
        this.set('undo', null);
        this.set('multipleUndo', true);
        _.each(selectedModels, function(model) {
          if (model.get('replacedorderline') && model.get('qty') < 0) {
            cancelChange = true;
          }
        });
        if (cancelChange) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CancelReplaceReturnPriceChange')
          );
          return;
        }
        setNextPrice = function(idx) {
          if (idx === selectedModels.length) {
            me.preventOrderSave(false);
            me.unset('skipCalculateReceipt');
            me.set('multipleUndo', null);
            me.calculateReceipt();
            finalCallback();
            return;
          }
          me.setPrice(selectedModels[idx], price, options, function() {
            setNextPrice(idx + 1);
          });
        };
        this.set('skipCalculateReceipt', true);
        this.preventOrderSave(true);
        setNextPrice(0);
      } else {
        this.setPrice(selectedModels[0], price, options, finalCallback);
      }
    },

    setPrice: function(line, price, options, callback) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreSetPrice',
        {
          context: this,
          line: line,
          price: price,
          options: options
        },
        function(args) {
          var me = args.context;
          if (args.cancellation && args.cancellation === true) {
            return;
          }

          options = args.options || {};
          options.setUndo =
            _.isUndefined(options.setUndo) ||
            _.isNull(options.setUndo) ||
            options.setUndo !== false
              ? true
              : options.setUndo;

          var allowModifyVerifyReturnLinePrice =
            OB.MobileApp.model.hasPermission(
              'OBPOS_ModifyPriceVerifiedReturns',
              true
            ) &&
            args.line.get('originalDocumentNo') &&
            !args.context.get('isPaid');
          if (
            !args.line.get('isEditable') &&
            !(
              allowModifyVerifyReturnLinePrice &&
              args.price < args.line.get('price')
            )
          ) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotChangePrice'));
          } else if (
            args.line.get('replacedorderline') &&
            args.line.get('qty') < 0
          ) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_CancelReplaceReturnPriceChange')
            );
            return;
          } else if (OB.DEC.isNumber(args.price)) {
            var oldprice = args.line.get('price');
            if (OB.DEC.compare(args.price) >= 0) {
              // sets the new listPrice and price
              args.line.set(
                'priceList',
                args.line.get('product').get('listPrice')
              );
              args.line.set('price', args.price);
              // sets the undo action
              if (options.setUndo) {
                if (me.get('multipleUndo')) {
                  var text = '',
                    oldprices = [],
                    lines = [],
                    undo = me.get('undo');
                  if (undo && undo.oldprices) {
                    text = undo.text + ', ';
                    oldprices = undo.oldprices;
                    lines = undo.lines;
                  }
                  text += OB.I18N.getLabel('OBPOS_SetPrice', [
                    args.line.printPrice(),
                    args.line.get('product').get('_identifier')
                  ]);
                  oldprices.push(oldprice);
                  lines.push(args.line);
                  me.setUndo('EditLine', {
                    text: text,
                    oldprices: oldprices,
                    lines: lines,
                    undo: function() {
                      var i;
                      me.set('skipCalculateReceipt', true);
                      me.preventOrderSave(true);
                      for (i = 0; i < me.get('undo').lines.length; i++) {
                        me.get('undo').lines[i].set(
                          'price',
                          me.get('undo').oldprices[i]
                        );
                      }
                      me.preventOrderSave(false);
                      me.unset('skipCalculateReceipt');
                      me.set('undo', null);
                      me.calculateReceipt();
                    }
                  });
                } else {
                  me.setUndo('EditLine', {
                    text: OB.I18N.getLabel('OBPOS_SetPrice', [
                      args.line.printPrice(),
                      args.line.get('product').get('_identifier')
                    ]),
                    oldprice: oldprice,
                    line: args.line,
                    undo: function() {
                      me.set('undo', null);
                      args.line.set('price', oldprice);
                    }
                  });
                }
              }
              me.save();
            }
            if (callback && callback instanceof Function) {
              callback();
            }
          }
        }
      );
    },

    setLineProperty: function(line, property, value) {
      var index = this.get('lines').indexOf(line);
      this.get('lines')
        .at(index)
        .set(property, value);
    },

    setUndo: function(action, data, callback) {
      var me = this;
      if (data) {
        data.action = action;
      }
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreSetUndo_' + action,
        {
          data: data
        },
        function(args) {
          me.set('undo', args.data);
          if (callback) {
            callback();
          }
        }
      );
    },

    removeDeleteLine: function(line) {
      var deleteIndex = -1;
      _.each(this.get('deletedLines'), function(d, index) {
        if (d.id === line.id) {
          deleteIndex = index;
        }
      });
      if (deleteIndex !== -1) {
        this.get('deletedLines').splice(deleteIndex, 1);
        if (this.get('deletedLines').length === 0) {
          this.unset('deletedLines');
        }
      }
      line.unset('obposIsDeleted');
    },

    deleteLinesFromOrder: function(selectedModels, callback) {
      var me = this,
        pointofsale = OB.MobileApp.view.$.containerWindow.getRoot(),
        i,
        execution;

      function postDeleteLine() {
        var cleanReceipt,
          hasServices = me.get('hasServices'),
          linesToDelete = _.filter(me.get('lines').models, function(line) {
            return line.get('obposIsDeleted');
          });

        cleanReceipt = function() {
          me.preventOrderSave(false);
          if (hasServices) {
            var services = _.find(me.get('lines').models, function(line) {
              return line.get('relatedLines');
            });
            if (services) {
              me.set('hasServices', true);
            }
          }
          me.unset('preventServicesUpdate');
          me.unset('deleting');
          me.get('lines').trigger('updateRelations');
          me.save();
          OB.UTIL.ProcessController.finish('deleteLine', execution);
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostDeleteLine',
            {
              order: me,
              selectedLines: selectedModels
            },
            function(args) {
              if (callback) {
                callback(true);
              }
            }
          );
        };

        if (me.get('undo')) {
          var text, lines, relations;
          text = me.get('undo').text;
          lines = me.get('undo').lines;
          relations = me.get('undo').relations;

          me.setUndo('DeleteLine', {
            text: text,
            lines: lines,
            relations: relations,
            undo: function() {
              var i;
              var execution2 = OB.UTIL.ProcessController.start(
                'undoDeleteLine'
              );
              me.set('preventServicesUpdate', true);
              me.set('skipCalculateReceipt', true);
              me.set('deleting', true);
              if (
                OB.MobileApp.model.get('terminal').businessPartner ===
                me.get('bp').get('id')
              ) {
                for (i = 0; i < me.get('undo').lines.length; i++) {
                  if (
                    !me
                      .get('undo')
                      .lines[i].get('product')
                      .get('oBPOSAllowAnonymousSale')
                  ) {
                    OB.UTIL.showConfirmation.display(
                      OB.I18N.getLabel('OBMOBC_Error'),
                      OB.I18N.getLabel(
                        'OBPOS_AnonymousSaleForProductNotAllowed',
                        [
                          me
                            .get('undo')
                            .lines[i].get('product')
                            .get('_identifier')
                        ]
                      )
                    );
                    return;
                  }
                }
              }
              me.get('undo').lines.sort(function(a, b) {
                if (a.get('undoPosition') > b.get('undoPosition')) {
                  return 1;
                }
                if (a.get('undoPosition') < b.get('undoPosition')) {
                  return -1;
                }
                // a must be equal to b
                return 0;
              });

              lines.forEach(function(line) {
                if (
                  OB.MobileApp.model.hasPermission(
                    'OBPOS_remove_ticket',
                    true
                  ) &&
                  line.get('obposQtyDeleted')
                ) {
                  line.set('qty', line.get('obposQtyDeleted'));
                  line.set('obposQtyDeleted', 0);
                }
                me.removeDeleteLine(line);
                me.get('lines').add(line, {
                  at: line.get('undoPosition')
                });
                if (
                  OB.UTIL.RfidController.isRfidConfigured() &&
                  line.get('obposEpccode')
                ) {
                  OB.UTIL.RfidController.addEpcLine(line);
                }
              });
              relations.forEach(function(rel) {
                var rls = rel[0].get('relatedLines').slice(),
                  lineToAddRelated = _.filter(me.get('lines').models, function(
                    line
                  ) {
                    return line.id === rel[0].id;
                  });
                rls.push(rel[1]);
                lineToAddRelated[0].set('relatedLines', rls);
              });
              if (hasServices) {
                me.set('hasServices', true);
              }
              me.set('undo', null);
              me.unset('preventServicesUpdate');
              me.unset('skipCalculateReceipt');
              me.unset('deleting');
              me.get('lines').trigger('updateRelations');
              OB.UTIL.ProcessController.finish('undoDeleteLine', execution2);
              me.calculateReceipt();
            }
          });
        }

        if (hasServices) {
          me.unset('hasServices');
        }
        me.preventOrderSave(true);
        if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
          me.calculateReceipt(
            function() {
              if (!me.get('deletedLines')) {
                me.set('deletedLines', []);
              }
              _.each(linesToDelete, function(line) {
                // Move to deleted lines
                var deletedLine = new OrderLine(line.attributes);
                me.get('deletedLines').push(deletedLine);
              });
              me.get('lines').remove(linesToDelete);
              cleanReceipt();
            },
            undefined,
            true
          );
        } else {
          me.get('lines').remove(linesToDelete);
          me.calculateReceipt(function() {
            cleanReceipt();
          });
        }
      }

      function preDeleteLine() {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_PreDeleteLine',
          {
            order: me,
            selectedLines: selectedModels
          },
          function(args) {
            if (args && args.cancelOperation && args.cancelOperation === true) {
              return;
            }
            execution = OB.UTIL.ProcessController.start('deleteLine');
            me.get('lines').forEach(function(line, idx) {
              line.set('undoPosition', idx);
            });
            me.set('undo', null);
            me.set('preventServicesUpdate', true);
            me.set('deleting', true);
            me.setUndo(
              'DeleteLine',
              {
                text: '',
                lines: [],
                relations: []
              },
              function() {
                me._deleteLines(
                  selectedModels,
                  0,
                  selectedModels.length,
                  postDeleteLine
                );
              }
            );
            me.trigger('scan');
          }
        );
      }

      function deleteApproval() {
        var approvalNeeded = _.find(selectedModels, function(line) {
          return !line.get('forceDeleteLine');
        });
        if (approvalNeeded) {
          OB.UTIL.Approval.requestApproval(
            pointofsale.model,
            'OBPOS_approval.deleteLine',
            function(approved, supervisor, approvalType) {
              if (approved) {
                selectedModels.forEach(function(line, idx) {
                  line.set('deleteApproved', true);
                });
                preDeleteLine();
              } else {
                if (callback) {
                  callback(false);
                }
                return;
              }
            }
          );
        } else {
          preDeleteLine();
        }
      }

      function checkLineStock(idx) {
        if (idx === selectedModels.length) {
          deleteApproval();
        } else {
          var line = selectedModels[idx],
            product = line.get('product'),
            negativeQty = OB.DEC.compare(line.get('qty')) < 0,
            productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(
              product
            ),
            checkStockActions = [];

          var getLineStock = function() {
            if (checkStockActions.length) {
              var qtyAdded = -line.get('qty'),
                options = {
                  line: line
                };
              me.getStoreStock(
                product,
                qtyAdded,
                options,
                null,
                checkStockActions,
                function(hasStock) {
                  if (hasStock) {
                    checkLineStock(idx + 1);
                  }
                }
              );
            } else {
              checkLineStock(idx + 1);
            }
          };

          if (
            negativeQty &&
            OB.MobileApp.model.hasPermission(
              'OBPOS_EnableStockValidation',
              true
            )
          ) {
            checkStockActions.push('stockValidation');
          }

          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_CheckStockForNotSaleWithoutStock',
              true
            )
          ) {
            if (negativeQty && productStatus.restrictsaleoutofstock) {
              checkStockActions.push('discontinued');
            }

            if (negativeQty && OB.UTIL.isCrossStoreLine(line)) {
              checkStockActions.push('crossStore');
            }

            OB.UTIL.HookManager.executeHooks(
              'OBPOS_CheckStockDeleteLine',
              {
                order: me,
                line: line,
                checkStockActions: checkStockActions
              },
              function(args) {
                if (args.cancelOperation) {
                  return;
                }
                getLineStock();
              }
            );
          } else {
            getLineStock();
          }
        }
      }

      //If there are no lines to delete, continue
      if (!selectedModels || !selectedModels.length) {
        if (callback) {
          callback(true);
        }
        return;
      }

      //Editable Validation
      if (this.get('isEditable') === false) {
        pointofsale.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }

      //C&R lines with delivered quantities cannot be removed
      for (i = 0; i < selectedModels.length; i++) {
        if (
          me.get('replacedorder') &&
          selectedModels[i].get('deliveredQuantity') &&
          selectedModels[i].get('deliveredQuantity')
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine')
          );
          if (callback) {
            callback(false);
          }
          return;
        }
      }

      //Services validation
      if (this.get('hasServices')) {
        var unGroupedServiceLines = _.filter(selectedModels, function(line) {
          return (
            line.get('product').get('productType') === 'S' &&
            line.get('product').get('quantityRule') === 'PP' &&
            !line.get('groupService') &&
            line.has('relatedLines') &&
            line.get('relatedLines').length > 0 &&
            line.get('isEditable')
          );
        });
        if (unGroupedServiceLines && unGroupedServiceLines.length > 0) {
          var serviceQty,
            productQty,
            uniqueServices,
            getServiceQty,
            getProductQty;
          uniqueServices = _.uniq(unGroupedServiceLines, false, function(line) {
            return (
              line.get('product').get('id') +
              line.get('relatedLines')[0].orderlineId
            );
          });
          getServiceQty = function(service) {
            return _.filter(unGroupedServiceLines, function(line) {
              return (
                line.get('product').get('id') ===
                  service.get('product').get('id') &&
                line.get('relatedLines')[0].orderlineId ===
                  service.get('relatedLines')[0].orderlineId
              );
            }).length;
          };
          getProductQty = function(service) {
            return _.find(me.get('lines').models, function(line) {
              return (
                _.indexOf(
                  _.pluck(service.get('relatedLines'), 'orderlineId'),
                  line.get('id')
                ) !== -1
              );
            }).get('qty');
          };

          for (i = 0; i < uniqueServices.length; i++) {
            serviceQty = getServiceQty(uniqueServices[i]);
            productQty = getProductQty(uniqueServices[i]);
            if (productQty && productQty !== serviceQty) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBPOS_LineCanNotBeDeleted'),
                OB.I18N.getLabel('OBPOS_AllServiceLineMustSelectToDelete'),
                [
                  {
                    label: OB.I18N.getLabel('OBMOBC_LblOk')
                  }
                ]
              );
              return;
            }
          }
        }
      }

      if (
        !this.get('isQuotation') &&
        (OB.MobileApp.model.hasPermission(
          'OBPOS_CheckStockForNotSaleWithoutStock',
          true
        ) ||
          OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true))
      ) {
        // Check the stock for the negative lines to remove
        checkLineStock(0);
      } else {
        deleteApproval();
      }
    },

    _deleteLines: function(lines, idx, length, callback) {
      var me = this,
        line = lines[idx];

      if (idx === length) {
        if (callback) {
          callback();
        }
        return;
      }
      if (
        this.get('lines')
          .get(line)
          .get('obposIsDeleted')
      ) {
        this._deleteLines(lines, idx + 1, length, callback);
      } else {
        this._deleteLine(line, lines, function() {
          me._deleteLines(lines, idx + 1, length, callback);
        });
      }
    },

    _deleteLine: function(line, selectedLines, callback) {
      var me = this,
        isSelectedLine = selectedLines.includes(line),
        pack = line.isAffectedByPack(),
        productId = line.get('product').id,
        deletedQty,
        deleteLineOnceChecked;

      //Defensive code: Do not remove non existing line
      if (!this.get('lines').get(line)) {
        if (callback) {
          callback();
        }
        return;
      }

      if (line.get('obposIsDeleted')) {
        if (callback) {
          callback();
        }
        return;
      }

      if (!line.get('isDeletable')) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_NotDeletableLine', [
            line.get('product').get('_identifier')
          ])
        );
        if (callback) {
          callback();
        }
        return;
      }

      if (
        !isSelectedLine &&
        me.get('replacedorder') &&
        line.get('deliveredQuantity')
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine')
        );
        if (callback) {
          callback();
        }
        return;
      }

      if (pack) {
        // When deleting a line, check lines with other product that are affected by
        // same pack than deleted one and merge splitted lines created for those
        this.get('lines').forEach(function(l) {
          var affected;
          if (productId === l.get('product').id) {
            return; //continue
          }
          affected = l.isAffectedByPack();
          if (affected && affected.ruleId === pack.ruleId) {
            this.mergeLines(l);
          }
        }, this);
      }

      deleteLineOnceChecked = function() {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_PreDeleteSingleLine',
          {
            line: line
          },
          function(args) {
            if (args && args.cancelOperation) {
              if (callback) {
                callback();
              }
              return;
            }
            // trigger
            line.trigger('removed', line);

            if (
              OB.UTIL.RfidController.isRfidConfigured() &&
              line.get('obposEpccode')
            ) {
              OB.UTIL.RfidController.removeEpcLine(line);
            }

            if (line.has('obposQtytodeliver')) {
              line.unset('obposQtytodeliver', {
                silent: true
              });
            }

            if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
              if (!line.get('hasTaxError')) {
                line.set('obposQtyDeleted', line.get('qty'));
                line.set('qty', 0, {
                  silent: true
                });
              } else {
                // The line must be removed here because when the preference OBPOS_remove_ticket is enabled, the
                // calculateReceipt process is executed before removing the lines, causing a js error during the
                // tax calculation (due to the tax error)
                me.get('lines').remove(line);
              }
            }

            line.set('obposIsDeleted', true, {
              silent: true
            });

            if (
              OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true) &&
              line.get('obposQtyDeleted')
            ) {
              deletedQty = line.get('obposQtyDeleted');
            } else {
              deletedQty = line.get('qty');
            }

            if (!me.get('undo').lines.length) {
              me.get('undo').text =
                OB.I18N.getLabel('OBPOS_DeleteLine') +
                ': ' +
                deletedQty +
                ' x ' +
                line.get('product').get('_identifier');
              me.get('undo').lines.push(line);
            } else {
              var linesToDelete = me.get('undo').lines,
                text = me.get('undo').text;
              if (!linesToDelete) {
                linesToDelete = [];
              }
              linesToDelete.push(line);
              if (text) {
                text +=
                  ', ' +
                  deletedQty +
                  ' x ' +
                  line.get('product').get('_identifier');
              } else {
                text =
                  OB.I18N.getLabel('OBPOS_DeleteLine') +
                  ': ' +
                  deletedQty +
                  ' x ' +
                  line.get('product').get('_identifier');
              }
              me.get('undo').text = text;
              me.get('undo').lines = linesToDelete;
            }

            // If all lines are selected to remove, it is not necessary to find the related lines
            if (selectedLines.length === me.get('lines').length) {
              if (callback) {
                callback();
              }
              return;
            }

            me.removeRelatedServices(line, selectedLines, function() {
              // This hook is used for any external module that need also to remove any related line.
              // The related line must be introduced in the 'linesToRemove' array and will also be removed.
              OB.UTIL.HookManager.executeHooks(
                'OBPOS_PostDeleteRelatedServices',
                {
                  receipt: me,
                  removedLine: line,
                  linesToRemove: [],
                  selectedLines: selectedLines
                },
                function(args) {
                  if (args && args.cancellation) {
                    if (callback) {
                      callback();
                    }
                    return;
                  }
                  var removeRelatedLine;
                  removeRelatedLine = function(idx) {
                    if (idx === args.linesToRemove.length) {
                      if (callback) {
                        callback();
                      }
                    } else {
                      me._deleteLine(
                        args.linesToRemove[idx],
                        selectedLines,
                        function() {
                          removeRelatedLine(idx + 1);
                        }
                      );
                    }
                  };
                  removeRelatedLine(0);
                }
              );
            });
          }
        );
      };

      // Check the stock for each negative discontinued line that is related to a deleting line
      if (
        !isSelectedLine &&
        !this.get('isQuotation') &&
        (OB.MobileApp.model.hasPermission(
          'OBPOS_CheckStockForNotSaleWithoutStock',
          true
        ) ||
          OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true))
      ) {
        var product = line.get('product'),
          negativeQty = OB.DEC.compare(line.get('qty')) < 0,
          productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product),
          checkStockActions = [];

        var getLineStock = function() {
          if (checkStockActions.length) {
            var qtyAdded = -line.get('qty'),
              options = {
                line: line
              };
            me.getStoreStock(
              product,
              qtyAdded,
              options,
              null,
              checkStockActions,
              function(hasStock) {
                if (hasStock) {
                  deleteLineOnceChecked();
                } else if (callback) {
                  callback();
                }
              }
            );
          } else {
            deleteLineOnceChecked();
          }
        };

        if (
          negativeQty &&
          OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true)
        ) {
          checkStockActions.push('stockValidation');
        }

        if (
          OB.MobileApp.model.hasPermission(
            'OBPOS_CheckStockForNotSaleWithoutStock',
            true
          )
        ) {
          if (negativeQty && productStatus.restrictsaleoutofstock) {
            checkStockActions.push('discontinued');
          }

          if (negativeQty && OB.UTIL.isCrossStoreLine(line)) {
            checkStockActions.push('crossStore');
          }

          OB.UTIL.HookManager.executeHooks(
            'OBPOS_CheckStockDeleteLine',
            {
              order: me,
              line: line,
              checkStockActions: checkStockActions
            },
            function(args) {
              if (args.cancelOperation) {
                if (callback && callback instanceof Function) {
                  callback();
                }
                return;
              }
              getLineStock();
            }
          );
        } else {
          getLineStock();
        }
      } else {
        deleteLineOnceChecked();
      }
    },

    removeRelatedServices: function(lineToDelete, selectedLines, callback) {
      var me = this,
        removedId = lineToDelete.get('id'),
        serviceLinesToCheck = [],
        deletedQty;

      // Do not search for services if the ticket doesn't have anyone
      if (!me.get('hasServices')) {
        if (callback) {
          callback();
        }
        return;
      }

      if (
        OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true) &&
        lineToDelete.get('obposQtyDeleted')
      ) {
        deletedQty = lineToDelete.get('obposQtyDeleted');
      } else {
        deletedQty = lineToDelete.get('qty');
      }

      _.filter(this.get('lines').models, function(line) {
        return (
          !line.get('obposIsDeleted') &&
          line.has('relatedLines') &&
          line.get('relatedLines').length > 0
        );
      }).forEach(function(line, idx) {
        var relationIds = _.pluck(line.get('relatedLines'), 'orderlineId');
        if (_.indexOf(relationIds, removedId) !== -1) {
          serviceLinesToCheck.push(line);
        }
      });
      if (serviceLinesToCheck.length > 0) {
        var removeNextRelatedService;
        removeNextRelatedService = function(idx) {
          if (idx === serviceLinesToCheck.length) {
            if (callback) {
              callback();
            }
          } else {
            var lineToCheck = serviceLinesToCheck[idx],
              rl,
              rls;
            if (lineToCheck.get('relatedLines').length > 1) {
              rl = _.filter(lineToCheck.get('relatedLines'), function(rl) {
                return rl.orderlineId === lineToDelete.get('id');
              });
              me.get('undo').relations.push([lineToCheck, rl[0]]);
              //Effectively remove the relation from the service line
              rls = lineToCheck.get('relatedLines').slice();
              rls.splice(lineToCheck.get('relatedLines').indexOf(rl[0]), 1);
              lineToCheck.set('relatedLines', rls);
              if (lineToCheck.get('product').get('quantityRule') === 'PP') {
                me.get('undo').text +=
                  ', ' +
                  deletedQty +
                  ' x ' +
                  lineToCheck.get('product').get('_identifier');
              }
              removeNextRelatedService(idx + 1);
            } else {
              me._deleteLine(lineToCheck, selectedLines, function() {
                removeNextRelatedService(idx + 1);
              });
            }
          }
        };
        removeNextRelatedService(0);
      } else {
        if (callback) {
          callback();
        }
      }
    },

    deleteLines: function(lines, idx, length, callback) {
      var me = this,
        line = lines[idx],
        i;
      var removesLines = function(idx, callback) {
        idx++;
        if (me.get('lines').get(line)) {
          if (idx < length) {
            me.set('skipCalculateReceipt', true);
            me.deleteLine(line, true, null, false);
            me.deleteLines(lines, idx, length, callback);
          } else {
            me.set('skipCalculateReceipt', false);
            me.deleteLine(line, false, callback, true);
          }
        } else {
          // If there is a line and other related service line selected to delete, the service is deleted when the product is deleted
          // This causes that when this recursive line tries to delete the service line, the service line is not in the array which stores the lines to delete
          // The 'else' clause catches this situation and continues with the next line
          if (idx === length) {
            me.set('skipCalculateReceipt', false);
            if (callback) {
              callback();
            }
          } else {
            me.deleteLines(lines, idx, length, callback);
          }
        }
      };

      OB.UTIL.VersionManagement.registerDeprecation(
        35741,
        {
          year: 18,
          major: 1,
          minor: 0
        },
        'DEPRECATED deleteLines FUNCTION!!! Use the deleteLinesFromOrder function.  The related services will be removed but will not maintain a trace.'
      );
      if (idx === 0) {
        for (i = 0; i < lines.length; i++) {
          if (me.get('replacedorder') && lines[i].get('remainingQuantity')) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine')
            );
            if (callback) {
              callback();
            }
            return;
          }
        }
        if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
          for (i = 0; i < lines.length; i++) {
            lines[i].set('obposIsDeleted', true);
            lines[i].set('obposQtyDeleted', lines[i].get('qty'));
            lines[i].set('qty', 0, {
              silent: true
            });
          }
          this.set('undo', null);
          this.calculateReceipt(function() {
            removesLines(idx, callback);
          });
        } else {
          removesLines(idx, callback);
        }
      } else {
        removesLines(idx, callback);
      }
    },

    deleteLine: function(line, doNotSave, callback, isLastLine) {
      var me = this,
        pack = line.isAffectedByPack(),
        productId = line.get('product').id;

      OB.UTIL.VersionManagement.registerDeprecation(
        35741,
        {
          year: 18,
          major: 1,
          minor: 0
        },
        'DEPRECATED deleteLine FUNCTION!!! Use the deleteLinesFromOrder function. The related services will be removed but will not maintain a trace.'
      );
      //Defensive code: Do not remove non existing line
      if (!this.get('lines').get(line)) {
        if (callback) {
          callback();
        }
        return;
      }

      if (!line.get('isDeletable')) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_NotDeletableLine', [
            line.get('product').get('_identifier')
          ])
        );
        if (callback) {
          callback();
        }
        return;
      }

      if (me.get('replacedorder') && line.get('remainingQuantity')) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine')
        );
        if (callback) {
          callback();
        }
        return;
      }

      if (pack) {
        // When deleting a line, check lines with other product that are affected by
        // same pack than deleted one and merge splitted lines created for those
        this.get('lines').forEach(function(l) {
          var affected;
          if (productId === l.get('product').id) {
            return; //continue
          }
          affected = l.isAffectedByPack();
          if (affected && affected.ruleId === pack.ruleId) {
            this.mergeLines(l);
          }
        }, this);
      }

      // trigger
      line.trigger('removed', line);

      function finishDelete() {
        if (
          OB.UTIL.RfidController.isRfidConfigured() &&
          line.get('obposEpccode')
        ) {
          OB.UTIL.RfidController.removeEpcLine(line);
        }
        var text, lines, relations, i;

        me.get('lines').remove(line);

        if (me.get('undo')) {
          text = me.get('undo').text;
          lines = me.get('undo').lines;
          relations = me.get('undo').relations;

          me.setUndo('DeleteLine', {
            text: text,
            lines: lines,
            relations: relations,
            undo: function() {
              if (
                OB.UTIL.RfidController.isRfidConfigured() &&
                line.get('obposEpccode')
              ) {
                OB.UTIL.RfidController.addEpcLine(line);
              }
              me.set('preventServicesUpdate', true);
              me.set('deleting', true);
              if (
                OB.MobileApp.model.get('terminal').businessPartner ===
                me.get('bp').get('id')
              ) {
                for (i = 0; i < me.get('undo').lines.length; i++) {
                  if (
                    !me
                      .get('undo')
                      .lines[i].get('product')
                      .get('oBPOSAllowAnonymousSale')
                  ) {
                    OB.UTIL.showConfirmation.display(
                      OB.I18N.getLabel('OBMOBC_Error'),
                      OB.I18N.getLabel(
                        'OBPOS_AnonymousSaleForProductNotAllowed',
                        [
                          me
                            .get('undo')
                            .lines[i].get('product')
                            .get('_identifier')
                        ]
                      )
                    );
                    return;
                  }
                }
              }
              me.get('undo').lines.sort(function(a, b) {
                if (a.get('undoPosition') > b.get('undoPosition')) {
                  return 1;
                }
                if (a.get('undoPosition') < b.get('undoPosition')) {
                  return -1;
                }
                // a must be equal to b
                return 0;
              });

              lines.forEach(function(line) {
                if (
                  OB.MobileApp.model.hasPermission(
                    'OBPOS_remove_ticket',
                    true
                  ) &&
                  line.get('obposQtyDeleted')
                ) {
                  line.set('qty', line.get('obposQtyDeleted'));
                  line.set('obposQtyDeleted', 0);
                }
                me.removeDeleteLine(line);
                me.get('lines').add(line, {
                  at: line.get('undoPosition')
                });
              });
              relations.forEach(function(rel) {
                var rls = rel[0].get('relatedLines').slice(),
                  lineToAddRelated = _.filter(me.get('lines').models, function(
                    line
                  ) {
                    return line.id === rel[0].id;
                  });
                rls.push(rel[1]);
                lineToAddRelated[0].set('relatedLines', rls);
              });
              me.set('undo', null);
              me.unset('preventServicesUpdate');
              me.unset('deleting');
              me.get('lines').trigger('updateRelations');
              me.calculateReceipt();
            }
          });
        }

        me.adjustPayment();
        if (!doNotSave) {
          me.save(callback);
        } else if (callback) {
          callback();
        }
      }

      // If the OBPOS_remove_ticket preference is active then mark the line as deleted
      if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
        if (!this.get('deletedLines')) {
          this.set('deletedLines', []);
        }
        if (!line.get('hasTaxError')) {
          // Clone the line to be moved as deleted.
          var deletedline = new OrderLine(line.attributes);
          if (!line.get('obposIsDeleted')) {
            // Set the line as deleted
            deletedline.set('obposIsDeleted', true);
            deletedline.set('obposQtyDeleted', line.get('qty'));
            // set quantity as 0
            deletedline.set('qty', 0);
            // Set prices as 0
            deletedline.set('gross', 0);
          }
          // Sets the tax if it has been deleted
          deletedline.set(
            'tax',
            deletedline.get('tax')
              ? deletedline.get('tax')
              : deletedline.get('taxUndo')
          );
          // Move to deleted lines
          this.get('deletedLines').push(deletedline);
        }
      }
      // remove the line
      finishDelete();
    },
    getStoreStock: function(
      p,
      qty,
      options,
      attrs,
      checkStockActions,
      callback
    ) {
      var me = this,
        lines = this.get('lines'),
        line = !OB.UTIL.isNullOrUndefined(options) ? options.line : null,
        stockScreen = options && options.stockScreen,
        allLinesQty = qty,
        warehouseId,
        warehouse;

      if (p.get('productType') === 'S') {
        callback(true);
        return;
      }

      if (!line && p.get('groupProduct')) {
        var affectedByPack;
        line = lines.find(function(l) {
          if (
            l.get('product').id === p.id &&
            ((l.get('qty') > 0 && qty > 0) || (l.get('qty') < 0 && qty < 0))
          ) {
            affectedByPack = l.isAffectedByPack();
            if (!affectedByPack) {
              return true;
            } else if (
              (options && options.packId === affectedByPack.ruleId) ||
              !(options && options.packId)
            ) {
              return true;
            }
          }
        });
      }

      if (attrs && attrs.warehouse) {
        warehouseId = attrs.warehouse.id;
      } else if (line) {
        warehouseId = line.get('warehouse').id;
      } else {
        warehouseId = OB.MobileApp.model.get('warehouses')[0].warehouseid;
      }

      _.forEach(lines.models, function(l) {
        if (
          (l.get('product').get('id') === p.get('id') &&
            l.get('warehouse').id === warehouseId) ||
          (line && l.get('id') === line.get('id'))
        ) {
          allLinesQty = OB.DEC.add(
            allLinesQty,
            OB.DEC.sub(l.get('qty'), l.getDeliveredQuantity())
          );
        }
      });

      if (allLinesQty > 0) {
        if (
          stockScreen &&
          attrs &&
          attrs.warehouse &&
          !OB.UTIL.isNullOrUndefined(attrs.warehouse.warehouseqty)
        ) {
          OB.UTIL.StockUtils.checkStockCallback(
            checkStockActions,
            p,
            line,
            me,
            attrs,
            attrs.warehouse,
            allLinesQty,
            stockScreen,
            callback
          );
        } else {
          if (!OB.MobileApp.model.get('connectedToERP') || !navigator.onLine) {
            OB.UTIL.StockUtils.noConnectionCheckStockCallback(
              p,
              line,
              me,
              allLinesQty,
              callback
            );
          } else {
            OB.UTIL.StockUtils.getReceiptLineStock(
              p.get('id'),
              line,
              function(data) {
                if (data && data.exception) {
                  OB.UTIL.showConfirmation.display(
                    OB.I18N.getLabel('OBMOBC_Error'),
                    OB.I18N.getLabel('OBPOS_ErrorServerGeneric') +
                      data.exception.message
                  );
                  if (callback) {
                    callback(false);
                  }
                } else {
                  warehouse = _.find(data.warehouses, function(warehouse) {
                    return warehouse.warehouseid === warehouseId;
                  });
                  if (!warehouse) {
                    warehouse = {
                      bins: [],
                      warehouseid: OB.MobileApp.model.get('warehouses')[0]
                        .warehouseid,
                      warehousename: OB.MobileApp.model.get('warehouses')[0]
                        .warehousename,
                      warehouseqty: OB.DEC.Zero
                    };
                  }
                  OB.UTIL.StockUtils.checkStockCallback(
                    checkStockActions,
                    p,
                    line,
                    me,
                    attrs,
                    warehouse,
                    allLinesQty,
                    stockScreen,
                    callback
                  );
                }
              },
              function(data) {
                OB.UTIL.StockUtils.noConnectionCheckStockCallback(
                  p,
                  line,
                  me,
                  allLinesQty,
                  callback
                );
              }
            );
          }
        }
      } else if (callback) {
        callback(true);
      }
    },

    //Attrs is an object of attributes that will be set in order
    _addProduct: function(p, qty, options, attrs, callback) {
      var newLine = true,
        line = null,
        me = this,
        productHavingSameAttribute = false,
        productHasAttribute = p.get('hasAttributes'),
        attributeSearchAllowed = OB.MobileApp.model.hasPermission(
          'OBPOS_EnableSupportForProductAttributes',
          true
        ),
        productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(p);
      if (OB.UI.MultiColumn.isSingleColumn()) {
        OB.UTIL.showSuccess(
          OB.I18N.getLabel('OBPOS_AddLine', [
            qty ? qty : 1,
            p.get('_identifier')
          ])
        );
      }
      if (attributeSearchAllowed && productHasAttribute) {
        var lines = me.get('lines'),
          i,
          currentline;
        if (options && options.line) {
          productHavingSameAttribute = true;
        } else {
          if (attrs && !this.checkSerialAttribute(p, attrs.attributeValue)) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_ProductDefinedAsSerialNo')
            );
            if (callback) {
              callback(false, null);
            }
            return;
          }
          for (i = 0; i < lines.length; i++) {
            currentline = lines.models[i].attributes;
            if (
              attrs &&
              attrs.attributeValue &&
              currentline.attributeValue === attrs.attributeValue &&
              p.id === currentline.product.id
            ) {
              productHavingSameAttribute = true;
              line = currentline;
            }
          }
        }
      }
      if (p.get('ispack')) {
        OB.Data.PackDiscount[p.get('productCategory')].addPackToOrder(
          this,
          p,
          attrs
        );
        if (callback) {
          callback(true);
        }
        return;
      }
      if (this.get('orderType') === 1) {
        qty = qty ? -qty : -1;
      } else {
        qty = qty || 1;
      }
      if (
        (options && options.line ? options.line.get('qty') + qty : qty) < 0 &&
        !p.get('returnable')
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_UnreturnableProduct'),
          OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [
            p.get('_identifier')
          ])
        );
        if (callback) {
          callback(false, null);
        }
        return;
      }
      if (me.get('isQuotation') && me.get('hasbeenpaid') === 'Y') {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
        if (callback) {
          callback(false, null);
        }
        return false;
      }
      if (
        productStatus &&
        productStatus.restrictsalefrompos &&
        OB.DEC.compare(qty) === 1
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_ErrorProductLocked', [
            p.get('_identifier'),
            productStatus.name
          ])
        );
        if (callback) {
          callback(false, null);
        }
        return false;
      }

      function addProductToOrder() {
        function checkLineStock(stockCallback) {
          if (me.get('isQuotation')) {
            stockCallback();
            return;
          }
          var positiveQty = OB.DEC.compare(qty) > 0,
            isCrossStore = false,
            checkStockActions = [],
            stockValidation = false,
            scanningProduct =
              !_.isUndefined(attrs) &&
              attrs.kindOriginator === 'OB.OBPOSPointOfSale.UI.KeyboardOrder' &&
              attrs.isScanning
                ? true
                : false;

          function getLineStock() {
            if (
              checkStockActions.length &&
              (stockValidation || !scanningProduct)
            ) {
              me.getStoreStock(
                p,
                qty,
                options,
                attrs,
                checkStockActions,
                function(hasStock) {
                  if (hasStock) {
                    stockCallback();
                  } else if (callback && callback instanceof Function) {
                    callback(false, null);
                  }
                }
              );
            } else {
              stockCallback();
            }
          }

          if (
            positiveQty &&
            OB.MobileApp.model.hasPermission(
              'OBPOS_EnableStockValidation',
              true
            )
          ) {
            checkStockActions.push('stockValidation');
            stockValidation = true;
          }

          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_CheckStockForNotSaleWithoutStock',
              true
            )
          ) {
            if (positiveQty && productStatus.restrictsaleoutofstock) {
              checkStockActions.push('discontinued');
            }

            if (line) {
              isCrossStore = OB.UTIL.isCrossStoreLine(line);
            } else if (attrs && attrs.organization) {
              isCrossStore = OB.UTIL.isCrossStoreOrganization(
                attrs.organization
              );
            }

            if (
              positiveQty &&
              isCrossStore &&
              (!line || OB.DEC.compare(OB.DEC.add(line.get('qty'), qty)) > 0)
            ) {
              checkStockActions.push('crossStore');
            }

            OB.UTIL.HookManager.executeHooks(
              'OBPOS_CheckStockAddProduct',
              {
                order: me,
                product: p,
                line: line,
                qty: qty,
                checkStockActions: checkStockActions
              },
              function(args) {
                if (args && args.cancelOperation) {
                  if (callback && callback instanceof Function) {
                    callback(false, null);
                  }
                  return;
                }
                getLineStock();
              }
            );
          } else {
            getLineStock();
          }
        }

        function execPostAddProductToOrderHook() {
          if (me.isCalculateReceiptLocked === true || !line) {
            OB.error(
              'Save ignored before execute OBPOS_PostAddProductToOrder hook, system has detected that a line is being added when calculate receipt is closed. Ignore line creation'
            );
            if (attrs) {
              if (
                OB.UTIL.RfidController.isRfidConfigured() &&
                attrs.obposEpccode
              ) {
                OB.UTIL.RfidController.removeEpc(attrs.obposEpccode);
              }
              attrs.cancelOperation = true;
            }
            return null;
          }

          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostAddProductToOrder',
            {
              receipt: me,
              productToAdd: p,
              orderline: line,
              qtyToAdd: qty,
              attrs: attrs,
              options: options,
              newLine: newLine
            },
            function(args) {
              if (
                (!args.options || !args.options.isSilentAddProduct) &&
                args.newLine &&
                me.get('lines').contains(line) &&
                args.productToAdd.get('productType') !== 'S'
              ) {
                // Display related services after calculate gross, if it is new line
                // and if the line has not been deleted.
                // The line might has been deleted during calculate gross for
                // examples if there was an error in taxes.
                if (args.orderline.get('hasRelatedServices')) {
                  args.orderline.trigger('showServicesButton');
                }
                if (args.orderline.get('hasMandatoryServices')) {
                  var splitline =
                    !OB.UTIL.isNullOrUndefined(args.orderline) &&
                    !OB.UTIL.isNullOrUndefined(
                      args.orderline.get('splitline')
                    ) &&
                    args.orderline.get('splitline');
                  if (!splitline) {
                    args.receipt.trigger(
                      'showProductList',
                      args.orderline,
                      'mandatory'
                    );
                  } else {
                    args.orderline.set('hasMandatoryServices', false);
                  }
                }
              }
              if (callback) {
                callback(true, args.orderline);
              }
            }
          );
        }

        if (
          p.get('obposScale') &&
          (OB.UTIL.isNullOrUndefined(options) || !options.isVerifiedReturn)
        ) {
          OB.POS.hwserver.getWeight(function(data) {
            if (data.exception) {
              OB.UTIL.showConfirmation.display('', data.exception.message);
              if (callback) {
                callback(false, null);
              }
            } else if (data.result === 0) {
              OB.UTIL.showConfirmation.display(
                '',
                OB.I18N.getLabel('OBPOS_WeightZero')
              );
              if (callback) {
                callback(false, null);
              }
            } else {
              line = me.createLine(
                p,
                options && options.isVerifiedReturn
                  ? -data.result
                  : data.result,
                options,
                attrs
              );
              execPostAddProductToOrderHook();
            }
          });
        } else {
          if (p.get('groupProduct')) {
            if (options && options.line) {
              line = options.line;
            } else {
              line = me.get('lines').find(function(l) {
                if (
                  l.get('product').id === p.id &&
                  l.get('isEditable') &&
                  ((l.get('qty') > 0 && qty > 0) ||
                    (l.get('qty') < 0 && qty < 0))
                ) {
                  if (attributeSearchAllowed && attrs && attrs.attributeValue) {
                    if (l.get('attributeValue') === attrs.attributeValue) {
                      return true;
                    }
                  } else {
                    return true;
                  }
                }
              });
            }
            if (me.isCalculateReceiptLocked === true) {
              OB.error(
                'Before execute OBPOS_GroupedProductPreCreateLine hook, system has detected that line is being added when calculate receipt is closed. Ignore line creation'
              );
              if (
                OB.UTIL.RfidController.isRfidConfigured() &&
                attrs &&
                attrs.obposEpccode
              ) {
                OB.UTIL.RfidController.removeEpc(attrs.obposEpccode);
              }
              return null;
            }
            checkLineStock(function() {
              OB.UTIL.HookManager.executeHooks(
                'OBPOS_GroupedProductPreCreateLine',
                {
                  receipt: me,
                  line: line,
                  allLines: me.get('lines'),
                  p: p,
                  qty: qty,
                  options: options,
                  attrs: attrs
                },
                function(args) {
                  if (args && args.cancelOperation) {
                    return;
                  }
                  if (args.receipt.isCalculateReceiptLocked === true) {
                    OB.error(
                      'After execute OBPOS_GroupedProductPreCreateLine hook, system has detected that line is being added when calculate receipt is closed. Ignore line creation'
                    );
                    if (
                      OB.UTIL.RfidController.isRfidConfigured() &&
                      args &&
                      args.attrs &&
                      args.attrs.obposEpccode
                    ) {
                      OB.UTIL.RfidController.removeEpc(args.attrs.obposEpccode);
                    }
                    return null;
                  }
                  if (OB.MobileApp.model.get('inPaymentTab')) {
                    if (args.options && args.options.blockAddProduct) {
                      OB.error(
                        'An add product is executed. At this point, this action is not allowed. Skipping product ' +
                          p.get('_identifier')
                      );
                      if (
                        OB.UTIL.RfidController.isRfidConfigured() &&
                        args &&
                        args.attrs &&
                        args.attrs.obposEpccode
                      ) {
                        OB.UTIL.RfidController.removeEpc(
                          args.attrs.obposEpccode
                        );
                      }
                      return;
                    }
                  }
                  var splitline =
                    !(options && options.line) &&
                    !OB.UTIL.isNullOrUndefined(args.line) &&
                    !OB.UTIL.isNullOrUndefined(args.line.get('splitline')) &&
                    args.line.get('splitline');
                  var serviceProduct =
                    args.line &&
                    (qty !== 1 ||
                      args.line.get('qty') !== -1 ||
                      args.p.get('productType') !== 'S' ||
                      (args.p.get('productType') === 'S' &&
                        !args.p.get('isLinkedToProduct')));
                  var groupedByAttributeValues =
                    ((productHasAttribute && productHavingSameAttribute) ||
                      (!productHasAttribute && !productHavingSameAttribute)) &&
                    attributeSearchAllowed;
                  if (
                    args.line &&
                    !splitline &&
                    (args.line.get('qty') > 0 ||
                      !args.line.get('replacedorderline')) &&
                    serviceProduct &&
                    (groupedByAttributeValues || !groupedByAttributeValues)
                  ) {
                    args.receipt.addUnit(args.line, args.qty);
                    if (!_.isUndefined(args.attrs)) {
                      _.each(_.keys(args.attrs), function(key) {
                        if (
                          args.p.get('productType') === 'S' &&
                          key === 'relatedLines' &&
                          args.line.get('relatedLines')
                        ) {
                          args.line.set(
                            'relatedLines',
                            OB.UTIL.mergeArrays(
                              args.line.get('relatedLines'),
                              attrs[key]
                            )
                          );
                        } else {
                          args.line.set(key, attrs[key]);
                        }
                      });
                    }
                    args.line.trigger('selected', args.line);
                    line = args.line;
                    newLine = false;
                  } else {
                    if (
                      args.attrs &&
                      args.attrs.relatedLines &&
                      args.attrs.relatedLines[0].deferred &&
                      args.p.get('quantityRule') === 'PP' &&
                      args.qty > 0
                    ) {
                      line = args.receipt.createLine(
                        args.p,
                        args.attrs.relatedLines[0].qty,
                        args.options,
                        args.attrs
                      );
                    } else {
                      line = args.receipt.createLine(
                        args.p,
                        args.qty,
                        args.options,
                        args.attrs
                      );
                    }
                  }
                  execPostAddProductToOrderHook();
                }
              );
            });
          } else {
            if (OB.MobileApp.model.get('inPaymentTab')) {
              if (options && options.blockAddProduct) {
                OB.error(
                  'An add product is executed. At this point, this action is not allowed. Skipping product ' +
                    p.get('_identifier')
                );
                if (
                  OB.UTIL.RfidController.isRfidConfigured() &&
                  attrs &&
                  attrs.obposEpccode
                ) {
                  OB.UTIL.RfidController.removeEpc(attrs.obposEpccode);
                }
                return;
              }
            }
            if (me.isCalculateReceiptLocked === true) {
              OB.error(
                'An add product is executed. At this point, this action is not allowed because calculate Receipt is blocked. Skipping product ' +
                  p.get('_identifier')
              );
              if (
                OB.UTIL.RfidController.isRfidConfigured() &&
                attrs &&
                attrs.obposEpccode
              ) {
                OB.UTIL.RfidController.removeEpc(attrs.obposEpccode);
              }
              return null;
            }
            checkLineStock(function() {
              var count;
              //remove line even it is a grouped line
              if (options && options.line && qty === -1) {
                me.addUnit(options.line, qty);
                line = options.line;
                newLine = false;
              } else {
                if (p.get('avoidSplitProduct')) {
                  line = me.createLine(p, qty, options, attrs);
                } else {
                  if (qty >= 0) {
                    for (count = 0; count < qty; count++) {
                      line = me.createLine(p, 1, options, attrs);
                    }
                  } else {
                    for (count = 0; count > qty; count--) {
                      line = me.createLine(p, -1, options, attrs);
                    }
                  }
                }
              }
              execPostAddProductToOrderHook();
            });
          }
        }
      } // End addProductToOrder

      if (
        (options && options.line ? options.line.get('qty') + qty : qty) < 0 &&
        p.get('productType') === 'S' &&
        !p.get('ignoreReturnApproval')
      ) {
        if (options && options.isVerifiedReturn) {
          OB.UTIL.showLoading(false);
        }
        OB.UTIL.Approval.requestApproval(
          OB.MobileApp.view.$.containerWindow.getRoot().model,
          'OBPOS_approval.returnService',
          function(approved, supervisor, approvalType) {
            if (options && options.isVerifiedReturn) {
              OB.UTIL.showLoading(true);
            }
            if (approved) {
              addProductToOrder();
            } else {
              if (callback) {
                callback(true);
              }
            }
          }
        );
      } else {
        addProductToOrder();
      }
    },

    checkSerialAttribute: function(product, attributeValue) {
      if (!attributeValue) {
        return true;
      }
      var lines = this.get('lines');
      var i;
      for (i = 0; i < lines.length; i++) {
        var currentline = lines.at(i);
        if (
          (currentline.get('attSetInstanceDesc') === attributeValue ||
            currentline.get('attributeValue') === attributeValue) &&
          product.id === currentline.get('product').id
        ) {
          if (product.get('isSerialNo')) {
            return false;
          }
        }
      }
      return true;
    },

    checkAllAttributesHasValue: function() {
      var lines = this.get('lines');
      var i;
      for (i = 0; i < lines.length; i++) {
        var currentline = lines.at(i);
        if (currentline.get('product').get('hasAttributes')) {
          if (
            !currentline.get('attSetInstanceDesc') &&
            !currentline.get('attributeValue')
          ) {
            return false;
          }
        }
      }
      return true;
    },

    _loadRelatedServices: async function(
      productType,
      productId,
      productCategory,
      callback,
      line
    ) {
      if (productType !== 'S' && (!line || !line.get('originalOrderLineId'))) {
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          var params = {},
            date = new Date();
          params.terminalTime = date;
          params.terminalTimeOffset = date.getTimezoneOffset();

          const body = {
            product: productId,
            productCategory: productCategory,
            parameters: params,
            remoteFilters: [
              {
                columns: [],
                operator: 'filter',
                value: 'OBRDM_DeliveryServiceFilter',
                params: [false]
              }
            ]
          };
          try {
            let data = await OB.App.Request.mobileServiceRequest(
              'org.openbravo.retail.posterminal.process.HasServices',
              body
            );
            data = data.response.data;

            if (data && data.exception) {
              //ERROR or no connection
              OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              callback(null);
            } else if (data) {
              callback(data);
            } else {
              callback(null);
            }
          } catch (error) {
            OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
            callback(null);
          }
        } else {
          //non-high volumes: indexedDB
          let criteria = new OB.App.Class.Criteria();
          criteria = await OB.UTIL.servicesFilter(
            criteria,
            productId,
            productCategory
          );
          criteria.criterion('obrdmIsdeliveryservice', false);
          try {
            const products = await OB.App.MasterdataModels.Product.find(
              criteria.build()
            );
            let data = [];
            for (let i = 0; i < products.length; i++) {
              data.push(OB.Dal.transform(OB.Model.Product, products[i]));
            }

            if (data) {
              data.hasservices = data.length > 0;
              data.hasmandatoryservices = _.find(data[0], function(model) {
                return model.proposalType === 'MP';
              });
              callback(data);
            } else {
              callback(null);
            }
          } catch (error) {
            OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
            callback(null);
          }
        }
      } else {
        callback(null);
      }
    },

    _drawLinesDistribution: function(data) {
      var me = this,
        checkLinesToAdd = function() {
          if (data && data.linesToAdd && data.linesToAdd.length > 0) {
            _.each(
              data.linesToAdd,
              function(lineToAdd) {
                me.createLine(lineToAdd.product, lineToAdd.qtyToAdd);
              },
              me
            );
          }
        };
      if (data && data.linesToModify && data.linesToModify.length > 0) {
        _.each(
          data.linesToModify,
          function(lineToChange) {
            var line = this.get('lines').getByCid(lineToChange.lineCid);
            var unitsToAdd = lineToChange.newQty - line.get('qty');
            if (unitsToAdd > 0) {
              this.addUnit(line, unitsToAdd);
            } else if (unitsToAdd < 0) {
              this.removeUnit(line, -unitsToAdd);
            }
            this.setPrice(line, lineToChange.newPrice, {
              setUndo: false
            });
            _.each(lineToChange.productProperties, function(propToSet) {
              line.get('product').set(propToSet.name, propToSet.value);
            });
            _.each(lineToChange.lineProperties, function(propToSet) {
              line.set(propToSet.name, propToSet.value);
            });
          },
          this
        );
      }
      if (data && data.linesToRemove && data.linesToRemove.length > 0) {
        var linesToRemove = [];
        _.each(data.linesToRemove, function(lineCidToRemove) {
          var line = this.get('lines').getByCid(lineCidToRemove);
          linesToRemove.push(line);
        });
        this.deleteLinesFromOrder(linesToRemove, function() {
          checkLinesToAdd();
        });
      } else {
        checkLinesToAdd();
      }
    },

    //Attrs is an object of attributes that will be set in order
    addProduct: async function(p, qty, options, attrs, callback) {
      function successCallback(productPrices) {
        if (productPrices.length > 0) {
          p = p.clone();
          if (
            OB.UTIL.isNullOrUndefined(p.get('updatePriceFromPricelist')) ||
            p.get('updatePriceFromPricelist')
          ) {
            p.set('standardPrice', productPrices[0].get('pricestd'));
            p.set('listPrice', productPrices[0].get('pricelist'));
          }
          me.addProductToOrder(p, qty, options, attrs, function(
            success,
            orderline
          ) {
            OB.UTIL.ProcessController.finish('addProduct', execution);
            if (callback) {
              callback(success, orderline);
            }
          });
        } else {
          OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
          OB.UTIL.ProcessController.finish('addProduct', execution);
          if (callback) {
            callback(false, null);
          }
        }
      }
      function errorCallback() {
        OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
        OB.UTIL.ProcessController.finish('addProduct', execution);
        if (callback) {
          callback(false, null);
        }
      }

      var execution = OB.UTIL.ProcessController.start('addProduct');
      OB.debug('_addProduct');
      var me = this;
      if (
        OB.MobileApp.model.hasPermission('EnableMultiPriceList', true) &&
        !p.get('ispack') &&
        OB.UTIL.isCrossStoreProduct(p)
      ) {
        p.set('standardPrice', null);
        p.set('listPrice', null);
        p.set('currentPrice', null);
        if (p.has('productPrices')) {
          _.each(p.get('productPrices'), function(productPrice) {
            if (
              productPrice.priceListId ===
              OB.MobileApp.model.receipt.get('bp').get('priceList')
            ) {
              p = p.clone();
              if (
                OB.UTIL.isNullOrUndefined(p.get('updatePriceFromPricelist')) ||
                p.get('updatePriceFromPricelist')
              ) {
                p.set('standardPrice', productPrice.price);
                p.set('listPrice', productPrice.price);
                p.set('currentPrice', productPrice);
              }
              me.addProductToOrder(p, qty, options, attrs, function(
                success,
                orderline
              ) {
                OB.UTIL.ProcessController.finish('addProduct', execution);
                if (callback) {
                  callback(success, orderline);
                }
              });
              return;
            }
          });
          if (OB.UTIL.isNullOrUndefined(p.get('standardPrice'))) {
            OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
            OB.UTIL.ProcessController.finish('addProduct', execution);
            if (callback) {
              callback(false, null);
            }
          }
        } else {
          OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
          OB.UTIL.ProcessController.finish('addProduct', execution);
          if (callback) {
            callback(false, null);
          }
        }
      } else if (
        OB.MobileApp.model.hasPermission('EnableMultiPriceList', true) &&
        this.get('priceList') !==
          OB.MobileApp.model.get('terminal').priceList &&
        !p.get('ispack') &&
        !OB.UTIL.isCrossStoreProduct(p)
      ) {
        var criteria = {};
        if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          criteria = new OB.App.Class.Criteria()
            .criterion('m_pricelist_id', this.get('priceList'))
            .criterion('m_product_id', p.id)
            .build();
          try {
            let productPriceResult = await OB.App.MasterdataModels.ProductPrice.find(
              criteria
            );
            let productPrices = [];
            for (let i = 0; i < productPriceResult.length; i++) {
              productPrices.push(
                OB.Dal.transform(OB.Model.ProductPrice, productPriceResult[i])
              );
            }
            successCallback(productPrices);
          } catch (error) {
            errorCallback();
          }
        } else {
          var remoteCriteria = [];
          var productId = {
              columns: ['m_product_id'],
              operator: 'equals',
              value: p.id,
              isId: true
            },
            pricelistId = {
              columns: ['m_pricelist_id'],
              operator: 'equals',
              value: this.get('priceList'),
              isId: true
            };
          remoteCriteria.push(productId);
          remoteCriteria.push(pricelistId);
          criteria.remoteFilters = remoteCriteria;

          OB.Dal.findUsingCache(
            'productPrice',
            OB.Model.ProductPrice,
            criteria,
            function(productPrices) {
              successCallback(productPrices.models);
            },
            errorCallback,
            {
              modelsAffectedByCache: ['ProductPrice']
            }
          );
        }
      } else {
        // With the preference OBPOS_allowProductsNoPriceInMainPricelist
        // it is possible to add product without price in the terminal's main list
        if (OB.UTIL.isNullOrUndefined(p.get('listPrice')) && !p.get('ispack')) {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_productWithoutPriceInPriceList', [
              p.get('_identifier')
            ])
          );
          OB.UTIL.ProcessController.finish('addProduct', execution);
          if (callback) {
            callback(false, null);
          }
        } else {
          me.addProductToOrder(
            p,
            qty,
            options,
            attrs,
            function(success, orderline) {
              OB.UTIL.ProcessController.finish('addProduct', execution);
              if (callback) {
                callback(success, orderline);
              }
            },
            function() {
              OB.UTIL.ProcessController.finish('addProduct', execution);
            }
          );
        }
      }
    },

    addProductToOrder: async function(
      p,
      qty,
      options,
      attrs,
      callback,
      cancelCallback
    ) {
      var executeAddProduct,
        addProductBOMToProduct,
        addProdCharsToProduct,
        finalCallback,
        me = this,
        attributeSearchAllowed = OB.MobileApp.model.hasPermission(
          'OBPOS_EnableSupportForProductAttributes',
          true
        );
      finalCallback = function(success, orderline) {
        if (callback) {
          callback(success, orderline);
        }
      };

      // do not allow generic products to be added to the receipt
      if (p && p.get('isGeneric')) {
        OB.UTIL.showI18NWarning('OBPOS_GenericNotAllowed');
        finalCallback(false, null);
        return;
      }
      if (
        options &&
        options.line &&
        options.line.get('replacedorderline') &&
        options.line.get('qty') < 0
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn')
        );
        finalCallback(false, null);
        return;
      }
      if (
        OB.MobileApp.model.get('terminal').businessPartner ===
          me.get('bp').get('id') &&
        p &&
        p.has('oBPOSAllowAnonymousSale') &&
        !p.get('oBPOSAllowAnonymousSale')
      ) {
        if (me && me.get('deferredOrder')) {
          me.unset('deferredOrder', {
            silent: true
          });
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_AnonymousSaleNotAllowedDeferredSale')
          );
        } else {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_AnonymousSaleNotAllowed')
          );
        }
        finalCallback(false, null);
        return;
      }

      addProductBOMToProduct = async function() {
        const productBOM = await OB.App.MasterdataModels.ProductBOM.find(
          new OB.App.Class.Criteria().criterion('product', p.id).build()
        );
        if (productBOM.length > 0) {
          p.set('productBOM', productBOM);
        }
      };

      addProdCharsToProduct = async function(
        productWithChars,
        addProdCharCallback
      ) {
        //Add prod char information to product object
        if (
          !productWithChars.get('productCharacteristics') &&
          productWithChars.get('characteristicDescription') &&
          !OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)
        ) {
          const criteria = new OB.App.Class.Criteria()
            .criterion('product', productWithChars.get('id'))
            .build();
          try {
            const productCharacteristics = await OB.App.MasterdataModels.ProductCharacteristicValue.find(
              criteria
            );
            productWithChars.set(
              'productCharacteristics',
              productCharacteristics
            );
            addProdCharCallback();
          } catch (error) {
            OB.UTIL.showError(error);
          }
        } else {
          addProdCharCallback();
        }
      };

      var context = this;

      // In case product is BOM and it doesn't have BOM information yet, add it
      if (
        !p.has('productBOM') &&
        OB.Taxes.Pos.taxCategoryBOM.find(
          taxCategory => taxCategory.id === p.get('taxCategory')
        )
      ) {
        await addProductBOMToProduct();
      }

      var productWithChars = OB.UTIL.clone(p);
      addProdCharsToProduct(productWithChars, function() {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_AddProductToOrder',
          {
            receipt: context,
            productToAdd: productWithChars,
            qtyToAdd: qty,
            options: options,
            attrs: attrs
          },
          function(args) {
            if (args && args.receipt && args.receipt.get('deferredOrder')) {
              args.receipt.unset('deferredOrder', {
                silent: true
              });
            }
            if (args && args.useLines) {
              me._drawLinesDistribution(args);
              finalCallback(false, null);
              return;
            }
            if (OB.UTIL.isNullOrUndefined(args.attrs)) {
              args.attrs = {};
            }
            args.attrs.hasMandatoryServices = false;
            args.attrs.hasRelatedServices = false;
            executeAddProduct = function() {
              var isQuotationAndAttributeAllowed =
                args.receipt.get('isQuotation') &&
                OB.MobileApp.model.hasPermission(
                  'OBPOS_AskForAttributesWhenCreatingQuotation',
                  true
                );
              if (
                (!args || !args.options || !args.options.line) &&
                attributeSearchAllowed &&
                productWithChars.get('hasAttributes') &&
                qty >= 1 &&
                (!args.receipt.get('isQuotation') ||
                  isQuotationAndAttributeAllowed)
              ) {
                OB.MobileApp.view.waterfall('onShowPopup', {
                  popup: 'modalProductAttribute',
                  args: {
                    callback: function(attributeValue) {
                      if (!OB.UTIL.isNullOrUndefined(attributeValue)) {
                        if (_.isEmpty(attributeValue)) {
                          // the attributes for layaways accepts empty values, but for manage later easy to be null instead ""
                          attributeValue = null;
                        }
                        attrs.attributeValue = attributeValue;
                        me._addProduct(
                          productWithChars,
                          qty,
                          options,
                          attrs,
                          function(success, orderline) {
                            finalCallback(success, orderline);
                          }
                        );
                      } else {
                        finalCallback(false, null);
                      }
                    },
                    options: options
                  }
                });
              } else {
                me._addProduct(productWithChars, qty, options, attrs, function(
                  success,
                  orderline
                ) {
                  finalCallback(success, orderline);
                });
              }
            };

            if (
              (!args.options || !args.options.isSilentAddProduct) &&
              args.productToAdd.get('productType') !== 'S' &&
              (!args.attrs || !args.attrs.originalOrderLineId)
            ) {
              var productId =
                args.productToAdd.get('isNew') &&
                OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)
                  ? null
                  : args.productToAdd.get('forceFilterId')
                  ? args.productToAdd.get('forceFilterId')
                  : args.productToAdd.id;
              args.receipt._loadRelatedServices(
                args.productToAdd.get('productType'),
                productId,
                args.productToAdd.get('productCategory'),
                function(data) {
                  if (data) {
                    if (data.hasservices) {
                      args.attrs.hasRelatedServices = true;
                    }
                    if (data.hasmandatoryservices) {
                      args.attrs.hasMandatoryServices = true;
                    }
                  }
                  executeAddProduct();
                  if (
                    !OB.UTIL.isNullOrUndefined(args.attrs) &&
                    args.attrs.cancelOperation
                  ) {
                    if (cancelCallback instanceof Function) {
                      cancelCallback();
                    }
                  }
                }
              );
            } else {
              executeAddProduct();
            }
          }
        );
      });
    },

    /**
     * Splits a line from the ticket keeping in the line the qtyToKeep quantity,
     * the rest is moved to another line with the same product and no packs, or
     * to a new one if there's no other line. In case a new is created it is returned.
     */
    splitLine: function(line, qtyToKeep) {
      var originalQty = line.get('qty'),
        newLine,
        p,
        qtyToMove;

      if (originalQty === qtyToKeep) {
        return;
      }

      qtyToMove = originalQty - qtyToKeep;

      this.setUnit(line, qtyToKeep, null);

      p = line.get('product');

      newLine = this.get('lines').find(function(l) {
        return (
          l !== line && l.get('product').id === p.id && !l.isAffectedByPack()
        );
      });

      if (!newLine) {
        newLine = line.clone();
        newLine.set({
          promotions: null,
          addedBySplit: true
        });
        this.get('lines').add(newLine);
        this.setUnit(newLine, qtyToMove, null);
        return newLine;
      } else {
        this.setUnit(newLine, newLine.get('qty') + qtyToMove, null);
      }
    },

    /**
     * Checks other lines with the same product to be merged in a single one
     */
    mergeLines: function(line) {
      var p = line.get('product'),
        lines = this.get('lines');
      lines.forEach(function(l) {
        if (l === line) {
          return;
        }

        if (
          l.get('product').id === p.id &&
          l.get('price') === line.get('price')
        ) {
          line.set({
            qty: line.get('qty') + l.get('qty'),
            promotions: null
          });
          lines.remove(l);
        }
      }, this);
    },

    /**
     *  It looks for different lines for same product with exactly the same promotions
     *  to merge them in a single line
     */
    mergeLinesWithSamePromotions: function() {
      var lines = this.get('lines'),
        line,
        i,
        j,
        k,
        otherLine,
        toRemove = [],
        matches,
        otherPromos,
        found,
        compareRule;

      compareRule = function(p) {
        var basep = line.get('promotions')[k];
        return (
          p.ruleId === basep.ruleId &&
          ((!p.family && !basep.family) ||
            (p.family && basep.family && p.family === basep.family))
        );
      };

      for (i = 0; i < lines.length; i++) {
        line = lines.at(i);
        for (j = i + 1; j < lines.length; j++) {
          otherLine = lines.at(j);
          if (
            otherLine.get('product').id !== line.get('product').id ||
            !line.get('product').groupProduct
          ) {
            continue;
          }

          if (
            (!line.get('promotions') || line.get('promotions').length === 0) &&
            (!otherLine.get('promotions') ||
              otherLine.get('promotions').length === 0)
          ) {
            line.set('qty', line.get('qty') + otherLine.get('qty'));
            toRemove.push(otherLine);
          } else if (
            line.get('promotions') &&
            otherLine.get('promotions') &&
            line.get('promotions').length ===
              otherLine.get('promotions').length &&
            line.get('price') === otherLine.get('price')
          ) {
            matches = true;
            otherPromos = otherLine.get('promotions');
            for (k = 0; k < line.get('promotions').length; k++) {
              found = _.find(otherPromos, compareRule);
              if (!found) {
                matches = false;
                break;
              }
            }
            if (matches) {
              line.set('qty', line.get('qty') + otherLine.get('qty'));
              for (k = 0; k < line.get('promotions').length; k++) {
                found = _.find(otherPromos, compareRule);
                line.get('promotions')[k].amt += found.amt;
                line.get('promotions')[k].displayedTotalAmount +=
                  found.displayedTotalAmount;
              }
              toRemove.push(otherLine);
            }
          }
        }
      }

      _.forEach(toRemove, function(l) {
        lines.remove(l);
      });
    },

    checkAvailableUnitsPerLine: function(discountRule) {
      var offered,
        rest,
        i,
        promotion,
        applyingToLines = new Backbone.Collection();
      this.get('lines').forEach(function(l) {
        offered = BigDecimal.prototype.ZERO;
        rest = BigDecimal.prototype.ZERO;
        if (l.get('promotions') && l.get('promotions').length > 0) {
          for (i = 0; i < l.get('promotions').length; i++) {
            promotion = l.get('promotions')[i];
            if (promotion.qtyOffer && !promotion.applyNext) {
              offered = offered.add(OB.DEC.toBigDecimal(promotion.qtyOffer));
            }
          }
        }
        if (l.get('promotionCandidates')) {
          l.get('promotionCandidates').forEach(function(candidateRule) {
            // If there is any line to apply the promotion, we add it
            if (candidateRule === discountRule.id) {
              if (
                OB.DEC.toBigDecimal(l.get('qty')).subtract(
                  OB.DEC.toBigDecimal(offered)
                ) > 0
              ) {
                applyingToLines.add(l);
                rest = OB.DEC.toBigDecimal(l.get('qty')).subtract(
                  OB.DEC.toBigDecimal(offered)
                );
                l.set('qtyAvailable', OB.DEC.toNumber(rest));
              } else {
                l.set(
                  'qtyAvailable',
                  OB.DEC.toNumber(BigDecimal.prototype.ZERO)
                );
              }
            }
          });
        }
      });
      return applyingToLines;
    },

    addManualPromotionToList: function(promotionToApply) {
      var me = this;
      var singlePromotionsList = [];
      var rule = promotionToApply.rule;
      if (!this.get('orderManualPromotions')) {
        this.set(
          'orderManualPromotions',
          new OB.Collection.OrderManualPromotionsList()
        );
      }
      if (
        !rule.obdiscAllowmultipleinstan ||
        this.get('orderManualPromotions').length <= 0
      ) {
        // Check there is no other manual promotion with the same ruleId and hasMultiDiscount set as false or undefined
        singlePromotionsList = _.filter(
          this.get('orderManualPromotions').models,
          function(promotion) {
            return (
              promotion.get('rule').obdiscAllowmultipleinstan ===
                rule.obdiscAllowmultipleinstan &&
              promotion.get('rule').id === rule.id
            );
          }
        );

        if (singlePromotionsList.length > 0) {
          //  There should be only one rule in the list with previous conditions in manual promotions list
          _.forEach(singlePromotionsList, function(singlePromotion) {
            me.get('orderManualPromotions').remove(singlePromotion);
          });
        }
      }
      if (
        rule.obdiscAllowmultipleinstan &&
        OB.UTIL.isNullOrUndefined(rule.discountinstance)
      ) {
        rule.discountinstance = OB.UTIL.get_UUID();
      }
      this.get('orderManualPromotions').push(promotionToApply);
    },

    getCurrentDiscountedLinePrice: function(line, ignoreExecutedAtTheEndPromo) {
      var i,
        currentDiscountedLinePrice = 0,
        allDiscountedAmt = 0;
      if (line.get('promotions')) {
        for (i = 0; i < line.get('promotions').length; i++) {
          if (!line.get('promotions')[i].hidden) {
            if (
              ignoreExecutedAtTheEndPromo &&
              line.get('promotions')[i].executedAtTheEndPromo
            ) {
              continue;
            } else {
              allDiscountedAmt += line.get('promotions')[i].amt;
            }
          }
        }
      }

      if (allDiscountedAmt > 0 && line.get('qty') > 0) {
        currentDiscountedLinePrice = OB.DEC.sub(
          line.get('price'),
          OB.DEC.div(
            allDiscountedAmt,
            line.get('qty'),
            OB.DEC.getRoundingMode()
          ),
          OB.DEC.getRoundingMode()
        );
      } else {
        currentDiscountedLinePrice = line.get('price');
      }

      return currentDiscountedLinePrice;
    },

    calculateDiscountedLinePrice: function(line) {
      if (line.get('qty') === 0) {
        line.unset('discountedLinePrice');
      } else {
        line.set(
          'discountedLinePrice',
          this.getCurrentDiscountedLinePrice(line, false)
        );
      }
    },

    addPromotion: function(line, rule, discount) {
      var promotions = line.get('promotions') || [],
        disc = {},
        i,
        replaced = false,
        unitsConsumed = 0,
        unitsConsumedByNoCascadeRules = 0,
        unitsConsumedByTheSameRule = 0,
        discountRule =
          OB.Model.Discounts.discountRules[rule.attributes.discountType];
      if (discountRule && discountRule.getIdentifier) {
        disc.identifier = discountRule.getIdentifier(rule, discount);
      }
      disc.name = rule.get('printName') || discount.name || rule.get('name');
      disc.ruleId = rule.id || rule.get('ruleId');
      disc.discountinstance = discount.discountinstance;
      disc.rule = rule;
      disc.amt = discount.amt;
      disc.fullAmt = discount.amt ? discount.amt : 0;
      disc.actualAmt = discount.actualAmt;
      disc.splitAmt = discount.splitAmt;
      disc.pack = discount.pack;
      disc.discountType = rule.get('discountType');
      disc.priority = rule.get('priority');
      disc.manual = discount.manual;
      disc.noOrder = discount.noOrder;
      disc.userAmt = discount.userAmt;
      disc.lastApplied = discount.lastApplied;
      disc.obdiscQtyoffer = OB.UTIL.isNullOrUndefined(rule.get('qtyOffer'))
        ? line.get('qty')
        : OB.DEC.toNumber(rule.get('qtyOffer'));
      disc.qtyOffer = disc.obdiscQtyoffer;
      disc.doNotMerge = discount.doNotMerge;
      disc.qtyToGift = discount.qtyToGift;
      disc.qtyToPay = discount.qtyToPay;
      if (!OB.UTIL.isNullOrUndefined(discount.chunks)) {
        disc.chunks = discount.chunks;
      } else {
        disc.chunks = undefined;
      }

      disc.obdiscLineFinalgross =
        discount.obdiscLineFinalgross || rule.get('obdiscLineFinalgross');
      disc.hidden =
        discount.hidden === true || (discount.actualAmt && !disc.amt);
      disc.preserve = discount.preserve === true;

      if (
        OB.UTIL.isNullOrUndefined(discount.actualAmt) &&
        !disc.amt &&
        disc.pack
      ) {
        disc.hidden = true;
      }

      if (disc.hidden) {
        disc.displayedTotalAmount = 0;
      } else {
        disc.displayedTotalAmount = disc.amt || discount.actualAmt;
      }

      if (discount.percentage) {
        disc.percentage = discount.percentage;
      }

      if (discount.family) {
        disc.family = discount.family;
      }

      if (typeof discount.applyNext !== 'undefined') {
        disc.applyNext = discount.applyNext;
      } else {
        disc.applyNext = rule.get('applyNext');
      }
      if (!disc.applyNext) {
        disc.qtyOfferReserved = disc.obdiscQtyoffer;
      } else {
        disc.qtyOfferReserved = 0;
      }
      if (!OB.UTIL.isNullOrUndefined(discount.extraProperties)) {
        disc.extraProperties = {};
        var key;
        for (key in discount.extraProperties) {
          if (discount.extraProperties.hasOwnProperty(key)) {
            disc[key] = discount.extraProperties[key];
            disc.extraProperties[key] = discount.extraProperties[key];
          }
        }
      }

      disc._idx = discount._idx || rule.get('_idx');

      disc.obdiscApplyafter = !OB.UTIL.isNullOrUndefined(
        rule.get('obdiscApplyafter')
      )
        ? rule.get('obdiscApplyafter')
        : false;
      disc.obdiscAllowinnegativelines = !OB.UTIL.isNullOrUndefined(
        rule.get('obdiscAllowinnegativelines')
      )
        ? rule.get('obdiscAllowinnegativelines')
        : false;
      disc.executedAtTheEndPromo = discount.executedAtTheEndPromo || false;

      if (!disc.manual) {
        for (i = 0; i < promotions.length; i++) {
          if (!promotions[i].applyNext) {
            unitsConsumedByNoCascadeRules += promotions[i].qtyOffer;
          } else if (promotions[i].ruleId === disc.ruleId) {
            unitsConsumedByTheSameRule += promotions[i].qtyOffer;
          }
        }

        if (disc.applyNext && unitsConsumedByTheSameRule === 0) {
          unitsConsumed = unitsConsumedByNoCascadeRules;
        } else {
          unitsConsumed =
            unitsConsumedByNoCascadeRules +
            unitsConsumedByTheSameRule +
            disc.qtyOffer;
        }
      }

      for (i = 0; i < promotions.length; i++) {
        if (!disc.manual && unitsConsumed > line.get('qty')) {
          if (discount.forceReplace) {
            if (
              promotions[i].ruleId === rule.id &&
              discount.discountinstance === promotions[i].discountinstance
            ) {
              if (promotions[i].hidden !== true) {
                promotions[i] = disc;
              }
            }
          }
          replaced = true;
          break;
        } else if (disc.manual || discount.forceReplace) {
          if (
            promotions[i].ruleId === rule.id &&
            discount.discountinstance === promotions[i].discountinstance
          ) {
            if (promotions[i].hidden !== true) {
              if (disc.applyNext === false || discount.forceReplace === true) {
                promotions[i] = disc;
              }
              replaced = true;
              break;
            }
          }
        }
      }

      if (!replaced) {
        promotions.push(disc);
      }

      line.set('promotions', promotions);
      // Calculate discountedLinePrice for the next promotion
      this.calculateDiscountedLinePrice(line);
      line.trigger('change');
    },

    removePromotion: function(line, rule) {
      var promotions = line.get('promotions'),
        discountinstance = rule.discountinstance,
        removed = false,
        res = [],
        i;
      if (!promotions) {
        return;
      }

      for (i = 0; i < promotions.length; i++) {
        if (
          promotions[i].ruleId === rule.id &&
          promotions[i].discountinstance === discountinstance
        ) {
          removed = true;
        } else {
          res.push(promotions[i]);
        }
      }

      if (removed) {
        line.set('promotions', res);
        // Calculate discountedLinePrice for the next promotion
        this.calculateDiscountedLinePrice(line);
        line.trigger('change');
      }
    },

    //Attrs is an object of attributes that will be set in order line
    createLine: function(p, units, options, attrs) {
      var me = this,
        orgId,
        orgName,
        country,
        region;
      if (
        OB.UTIL.isNullOrUndefined(attrs) ||
        OB.UTIL.isNullOrUndefined(attrs.organization)
      ) {
        if (!OB.UTIL.isCrossStoreProduct(p)) {
          orgId = OB.MobileApp.model.get('terminal').organization;
          orgName = OB.I18N.getLabel('OBPOS_LblThisStore', [
            OB.MobileApp.model.get('terminal').organization$_identifier
          ]);
          country = OB.MobileApp.model.get('terminal').organizationCountryId;
          region = OB.MobileApp.model.get('terminal').organizationRegionId;
        } else {
          orgId = me.get('organization');
          _.each(OB.MobileApp.model.get('store'), function(s) {
            if (s.id === orgId) {
              orgName = s.name;
              country = s.country;
              region = s.region;
              return;
            }
          });
        }
      } else {
        orgId = attrs.organization.id;
        orgName = attrs.organization.name;
        country = attrs.organization.country;
        region = attrs.organization.region;
      }

      function createLineAux(p, units, options, attrs, me) {
        if (
          me.validateAllowSalesWithReturn(
            units,
            (options && options.allowLayawayWithReturn) || false
          )
        ) {
          return;
        }
        // Get prices from BP pricelist
        var newline = new OrderLine({
          id: OB.UTIL.get_UUID(),
          product: p,
          uOM: p.get('uOM'),
          qty: OB.DEC.number(units, p.get('uOMstandardPrecision')),
          price: OB.DEC.number(p.get('standardPrice')),
          priceList: OB.DEC.number(p.get('listPrice')),
          priceIncludesTax: me.get('priceIncludesTax'),
          organization: {
            id: orgId,
            name: orgName,
            country: country,
            region: region
          },
          warehouse: {
            id:
              OB.UTIL.isNullOrUndefined(attrs) ||
              (!OB.UTIL.isNullOrUndefined(attrs) &&
                OB.UTIL.isNullOrUndefined(attrs.splitline))
                ? OB.MobileApp.model.get('warehouses')[0].warehouseid
                : attrs.originalLine.get('warehouse').id,
            warehousename:
              OB.UTIL.isNullOrUndefined(attrs) ||
              (!OB.UTIL.isNullOrUndefined(attrs) &&
                OB.UTIL.isNullOrUndefined(attrs.splitline))
                ? OB.MobileApp.model.get('warehouses')[0].warehousename
                : attrs.originalLine.get('warehouse').warehousename
          },
          isEditable:
            options && options.hasOwnProperty('isEditable')
              ? options.isEditable
              : true,
          isDeletable:
            options && options.hasOwnProperty('isDeletable')
              ? options.isDeletable
              : true
        });

        if (!_.isUndefined(attrs)) {
          _.each(_.keys(attrs), function(key) {
            newline.set(key, attrs[key]);
          });
        }

        if (newline.get('relatedLines')) {
          newline.set(
            'groupService',
            newline.get('product').get('groupProduct')
          );
          // Set the 'hasServices' property if the new line is adding a service related to a product to the order
          // Without the 'hasServices' property the quantity rules for services are not executed
          if (!me.get('hasServices')) {
            me.set('hasServices', true);
          }
        }

        //issue 25448: Show stock screen is just shown when a new line is created.
        if (newline.get('product').get('showstock') === true) {
          newline.get('product').set('showstock', false);
          newline.get('product').set('_showstock', true);
        }

        if (me.isCalculateReceiptLocked === true) {
          OB.error(
            'Create line - Trying to add a line when calculate receipt is closed. Ignore line creation'
          );
          if (
            OB.UTIL.RfidController.isRfidConfigured() &&
            attrs &&
            attrs.obposEpccode
          ) {
            OB.UTIL.RfidController.removeEpc(attrs.obposEpccode);
          }
          return null;
        }

        // add the created line
        me.get('lines').add(newline, options);
        newline.trigger('created', newline);
        // set the undo action
        me.setUndo('CreateLine', {
          text: OB.I18N.getLabel('OBPOS_AddLine', [
            newline.get('qty'),
            newline.get('product').get('_identifier')
          ]),
          line: newline,
          undo: function(modelObj) {
            // Instead of using 'me' as order, is necessary to use 'OB.MobileApp.model.receipt' to avoid references to not active orders
            // This happens while adding a deferred sale to a paid receipt
            var order = OB.MobileApp.model.receipt;
            order.deleteLinesFromOrder([newline], function() {
              order.set('undo', null);
              if (
                OB.UTIL.RfidController.isRfidConfigured() &&
                newline.get('obposEpccode')
              ) {
                OB.UTIL.RfidController.removeEpcLine(newline);
              }
            });
          }
        });
        return newline;
      }

      return createLineAux(p, units, options, attrs, me);
    },

    returnLine: function(line, options, skipValidaton) {
      var me = this,
        showProductCard =
          line.get('qty') < 0 &&
          OB.UTIL.isCrossStoreProduct(line.get('product')),
        params = {};
      if (line.get('qty') > 0) {
        line.get('product').set('ignorePromotions', true);
      } else {
        line.get('product').set('ignorePromotions', false);
      }
      this.set('skipCalculateReceipt', true);

      line.set('qty', -line.get('qty'));
      if (
        line.get('qty') > 0 &&
        line.get('product').get('groupProduct') &&
        !line.get('splitline')
      ) {
        this.mergeLines(line);
      }

      // set the undo action
      if (me.get('multipleUndo')) {
        var text = '',
          lines = [],
          undo = me.get('undo');
        if (undo && undo.lines) {
          text = undo.text + ', ';
          lines = undo.lines;
        }
        text += OB.I18N.getLabel('OBPOS_ReturnLine', [
          line.get('qty'),
          line.get('product').get('_identifier')
        ]);
        lines.push(line);
        me.setUndo('ReturnLine', {
          text: text,
          lines: lines,
          undo: function() {
            _.each(lines, function(line) {
              line.set('qty', -line.get('qty'));
            });
            me.calculateReceipt();
            me.set('undo', null);
          }
        });
      } else {
        this.setUndo('ReturnLine', {
          text: OB.I18N.getLabel('OBPOS_ReturnLine', [
            line.get('product').get('_identifier')
          ]),
          line: line,
          undo: function() {
            line.set('qty', -line.get('qty'));
            me.set('undo', null);
          }
        });
      }

      if (line.get('promotions')) {
        if (line.get('qty') < 0) {
          var promotions = _.filter(line.get('promotions'), function(
            promotion
          ) {
            return promotion.obdiscAllowinnegativelines;
          });
          line.set('promotions', promotions);
        }
      }
      if (showProductCard) {
        params.leftSubWindow =
          OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
        params.product = line.get('product');
        params.line = line;
        params.forceSelectStore = true;
        params.warehouse = line.get('warehouse');
        OB.MobileApp.view.$.containerWindow
          .getRoot()
          .showLeftSubWindow({}, params);
      }
      this.set('skipCalculateReceipt', false);
      this.calculateReceipt();
    },

    checkReturnableProducts: function(selectedModels, model, callback) {
      if (this.get('hasServices')) {
        this.checkReturnableServices(selectedModels, model, callback);
      } else {
        var notReturnableLine = _.find(selectedModels, function(line) {
          return !line.isReturnable() && line.get('net') > 0;
        });
        if (notReturnableLine) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_UnreturnableProduct'),
            OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [
              notReturnableLine.get('product').get('_identifier')
            ])
          );
          return;
        }
        if (callback) {
          callback();
        }
      }
    },

    checkReturnableServices: function(selectedModels, model, callback) {
      var me = this,
        approvalNeeded = false,
        notReturnableProducts = false,
        selectedProducts,
        notSelectedServices,
        servicesToApprove = '',
        servicesList = [];

      selectedModels.every(function(line) {
        if (!line.isReturnable()) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_UnreturnableProduct'),
            OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [
              line.get('product').get('_identifier')
            ])
          );
          notReturnableProducts = true;
          return false;
        } else {
          if (line.get('product').get('productType') === 'S') {
            // A service with its related product selected doesn't need to be returned, because later it will be modified to returned status depending in the product status
            // In any other case it would require two approvals
            if (line.get('relatedLines')) {
              selectedProducts = selectedProducts
                ? selectedProducts
                : _.filter(selectedModels, function(model) {
                    return !model.get('relatedLines');
                  });
              line.get('relatedLines').every(function(relatedLine) {
                if (
                  _.find(selectedProducts, function(selectedProduct) {
                    return selectedProduct.id === relatedLine.orderlineId;
                  })
                ) {
                  if (line.get('net') > 0) {
                    servicesToApprove +=
                      '<br>' +
                      OB.I18N.getLabel('OBMOBC_Character')[1] +
                      ' ' +
                      line.get('product').get('_identifier');
                    servicesList.push(line.get('product'));
                  }
                } else {
                  // A service cannot be returned it the related product is not also selected
                  OB.UTIL.showWarning(
                    OB.I18N.getLabel('OBPOS_NotProductSelectedToReturn', [
                      line.get('product').get('_identifier')
                    ])
                  );
                  notReturnableProducts = true;
                  return false;
                }
                return true;
              });
            } else if (line.get('net') > 0) {
              servicesToApprove +=
                '<br>' +
                OB.I18N.getLabel('OBMOBC_Character')[1] +
                ' ' +
                line.get('product').get('_identifier');
              servicesList.push(line.get('product'));
            }
            if (!approvalNeeded && line.get('net') > 0) {
              approvalNeeded = true;
            }
          } else {
            // Check if there is any not returnable related service to a selected line
            // Ask also for approval for non selected returnable services, related to selected products
            notSelectedServices = notSelectedServices
              ? notSelectedServices
              : _.filter(me.get('lines').models, function(notSelectedService) {
                  return (
                    notSelectedService.get('relatedLines') &&
                    !_.contains(selectedModels, notSelectedService)
                  );
                });
            notSelectedServices.every(function(notSelectedService) {
              if (
                _.find(notSelectedService.get('relatedLines'), function(
                  relatedLine
                ) {
                  return line.id === relatedLine.orderlineId;
                })
              ) {
                if (!notSelectedService.isReturnable()) {
                  OB.UTIL.showConfirmation.display(
                    OB.I18N.getLabel('OBPOS_UnreturnableRelatedService'),
                    OB.I18N.getLabel(
                      'OBPOS_UnreturnableRelatedServiceMessage',
                      [
                        line.get('product').get('_identifier'),
                        notSelectedService.productName
                      ]
                    )
                  );
                  notReturnableProducts = true;
                  return false;
                } else {
                  if (
                    notSelectedService.get('net') > 0 &&
                    !_.contains(servicesList, notSelectedService.get('product'))
                  ) {
                    servicesToApprove +=
                      '<br>' +
                      OB.I18N.getLabel('OBMOBC_Character')[1] +
                      ' ' +
                      notSelectedService.get('product').get('_identifier');
                    servicesList.push(notSelectedService.get('product'));
                    if (!approvalNeeded) {
                      approvalNeeded = true;
                    }
                  }
                }
              }
              return true;
            });
          }
          if (notReturnableProducts) {
            return false;
          } else {
            return true;
          }
        }
      });

      if (notReturnableProducts) {
        return;
      }

      if (approvalNeeded) {
        OB.UTIL.Approval.requestApproval(
          model,
          [
            {
              approval: 'OBPOS_approval.returnService',
              message: 'OBPOS_approval.returnService',
              params: [servicesToApprove]
            }
          ],
          function(approved, supervisor, approvalType) {
            if (approved) {
              me.set('notApprove', true);
              callback();
              me.unset('notApprove');
            }
          }
        );
      } else {
        callback();
      }
    },

    setBPandBPLoc: async function(
      businessPartner,
      showNotif,
      saveChange,
      callback
    ) {
      var me = this,
        undef,
        i,
        oldbp = this.get('bp'),
        setAndSaveBP,
        setPriceList,
        finishSaveData;

      setAndSaveBP = function(bp, saveBPCallback) {
        me.set('bp', bp);
        me.save(function() {
          OB.MobileApp.model.orderList.saveCurrent();
          if (saveBPCallback) {
            saveBPCallback();
          }
        });
      };

      setPriceList = function(bp) {
        var priceIncludesTax = bp.get('priceIncludesTax');
        if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
          priceIncludesTax = OB.MobileApp.model.get('pricelist')
            .priceIncludesTax;
        }
        if (
          priceIncludesTax !==
            OB.MobileApp.model.get('pricelist').priceIncludesTax ||
          bp.get('priceListCurrency') !==
            OB.MobileApp.model.get('pricelist').currency
        ) {
          me.set('priceList', OB.MobileApp.model.get('pricelist').id);
          me.set(
            'priceIncludesTax',
            OB.MobileApp.model.get('pricelist').priceIncludesTax
          );
          me.set('currency', OB.MobileApp.model.get('pricelist').currency);
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_ChangeOfPriceList'),
            OB.I18N.getLabel('OBPOS_ChangeOfPriceListConfig', [
              bp.get('priceListName'),
              OB.MobileApp.model.get('pricelist')._identifier
            ]),
            null,
            {
              onHideFunction: function() {
                me.trigger('change:documentNo', me);
              }
            }
          );
        } else {
          me.set('priceList', bp.get('priceList'));
          me.set('priceIncludesTax', priceIncludesTax);
        }
      };

      finishSaveData = function(callback) {
        // set the undo action
        if (showNotif === undef || showNotif === true) {
          me.setUndo('SetBPartner', {
            text: businessPartner
              ? OB.I18N.getLabel('OBPOS_SetBP', [
                  businessPartner.get('_identifier')
                ])
              : OB.I18N.getLabel('OBPOS_ResetBP'),
            bp: businessPartner,
            undo: function() {
              me.set('undo', null);
              setPriceList(oldbp);
              setAndSaveBP(oldbp, function() {
                me.calculateReceipt(function() {
                  me.save();
                });
              });
            }
          });
        }
        if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
          if (oldbp.get('priceList') !== businessPartner.get('priceList')) {
            setPriceList(businessPartner);
            me.removeAndInsertLines(function(deleted) {
              if (OB.UTIL.isNullOrUndefined(deleted) || deleted === true) {
                setAndSaveBP(businessPartner, function() {
                  me.calculateReceipt(function() {
                    if (saveChange) {
                      me.save();
                    }
                    if (callback) {
                      callback();
                    }
                  });
                });
              } else {
                setPriceList(oldbp);
                setAndSaveBP(oldbp, function() {
                  if (callback) {
                    callback();
                  }
                });
              }
            });
          } else {
            setAndSaveBP(businessPartner, function() {
              me.calculateReceipt(function() {
                if (saveChange) {
                  me.save();
                }
                if (callback) {
                  callback();
                }
              });
            });
          }
        } else {
          setAndSaveBP(businessPartner, function() {
            if (callback) {
              callback();
            }
          });
        }
      };

      if (
        OB.MobileApp.model.get('terminal').businessPartner ===
        businessPartner.id
      ) {
        for (i = 0; i < me.get('lines').models.length; i++) {
          if (
            !me
              .get('lines')
              .models[i].get('product')
              .get('oBPOSAllowAnonymousSale')
          ) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_AnonymousSaleForProductNotAllowed', [
                me
                  .get('lines')
                  .models[i].get('product')
                  .get('_identifier')
              ])
            );
            return;
          }
        }
      }
      if (OB.UTIL.isNullOrUndefined(businessPartner.get('paymentTerms'))) {
        OB.UTIL.showWarning(
          enyo.format(
            OB.I18N.getLabel('OBPOS_MsgBPNotPaymentTerm'),
            businessPartner.get('name')
          )
        );
        businessPartner.set(
          'paymentTerms',
          OB.MobileApp.model.get('terminal').defaultbp_paymentterm
        );
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        if (oldbp.id !== businessPartner.id) {
          //Business Partner have changed
          OB.Dal.saveOrUpdate(
            businessPartner,
            function() {},
            function() {
              OB.error(arguments);
            }
          );
          if (
            OB.MobileApp.model.hasPermission('OBPOS_remote.discount.bp', true)
          ) {
            OB.Discounts.Pos.manualRuleImpls = await OB.Discounts.Pos.addDiscountsByBusinessPartnerFilter(
              OB.Discounts.Pos.manualRuleImpls
            );
            OB.Discounts.Pos.ruleImpls = await OB.Discounts.Pos.addDiscountsByBusinessPartnerFilter(
              OB.Discounts.Pos.ruleImpls
            );
          }
        }

        var saveBP = function() {
          if (
            !businessPartner.get('locId') ||
            !businessPartner.get('shipLocId')
          ) {
            businessPartner.loadBPLocations(
              null,
              null,
              function(shipping, billing, locations) {
                businessPartner.set('locations', locations);
                businessPartner.set(
                  'locationModel',
                  shipping ? shipping : billing
                );
                businessPartner.set('locationBillModel', billing);
                businessPartner.set('locId', billing.get('id'));
                businessPartner.set('locName', billing.get('name'));
                businessPartner.set('postalCode', billing.get('postalCode'));
                businessPartner.set('cityName', billing.get('cityName'));
                businessPartner.set('countryName', billing.get('countryName'));
                if (shipping && !businessPartner.get('assignedShipAddr')) {
                  businessPartner.set('shipLocId', shipping.get('id'));
                  businessPartner.set('shipLocName', shipping.get('name'));
                  businessPartner.set(
                    'shipPostalCode',
                    shipping.get('postalCode')
                  );
                  businessPartner.set('shipCityName', shipping.get('cityName'));
                  businessPartner.set(
                    'shipCountryName',
                    shipping.get('countryName')
                  );
                  businessPartner.set('shipRegionId', shipping.get('regionId'));
                  businessPartner.set(
                    'shipCountryId',
                    shipping.get('countryId')
                  );
                }
                finishSaveData(callback);
              },
              businessPartner.get('id')
            );
          } else {
            finishSaveData(callback);
          }
        };

        var saveLocModel = function(locModel, lid, callback) {
          if (businessPartner.get(locModel)) {
            OB.Dal.saveOrUpdate(
              businessPartner.get(locModel),
              function() {},
              function(tx, error) {
                OB.UTIL.showError(error);
              }
            );
            if (callback) {
              callback();
            }
          } else if (businessPartner.get(lid)) {
            OB.Dal.get(
              OB.Model.BPLocation,
              businessPartner.get(lid),
              function(location) {
                OB.Dal.saveOrUpdate(
                  location,
                  function() {},
                  function(tx, error) {
                    OB.UTIL.showError(error);
                  }
                );
                businessPartner.set(locModel, location);
                if (callback) {
                  callback();
                }
              },
              function() {
                OB.error(arguments);
                if (callback) {
                  callback();
                }
              },
              function() {
                if (callback) {
                  callback();
                }
              }
            );
          } else {
            if (callback) {
              callback();
            }
          }
        };

        saveLocModel('locationModel', 'shipLocId', function() {
          saveLocModel('locationBillModel', 'locId', function() {
            saveBP();
          });
        });
      } else {
        if (
          !businessPartner.get('locId') ||
          !businessPartner.get('shipLocId')
        ) {
          businessPartner.loadBPLocations(
            null,
            null,
            function(shipping, billing, locations) {
              businessPartner.set('locations', locations);
              businessPartner.set(
                'locationModel',
                shipping ? shipping : billing
              );
              businessPartner.set('locationBillModel', billing);
              businessPartner.set('locId', billing.get('id'));
              businessPartner.set('locName', billing.get('name'));
              businessPartner.set('postalCode', billing.get('postalCode'));
              businessPartner.set('cityName', billing.get('cityName'));
              businessPartner.set('countryName', billing.get('countryName'));
              if (shipping && !businessPartner.get('assignedShipAddr')) {
                businessPartner.set('shipLocId', shipping.get('id'));
                businessPartner.set('shipLocName', shipping.get('name'));
                businessPartner.set(
                  'shipPostalCode',
                  shipping.get('postalCode')
                );
                businessPartner.set('shipCityName', shipping.get('cityName'));
                businessPartner.set(
                  'shipCountryName',
                  shipping.get('countryName')
                );
                businessPartner.set('shipRegionId', shipping.get('regionId'));
                businessPartner.set('shipCountryId', shipping.get('countryId'));
              }
              finishSaveData(callback);
            },
            businessPartner.get('id')
          );
        } else {
          finishSaveData(callback);
        }
      }
    },

    validateAllowSalesWithReturn: function(qty, skipValidaton, selectedModels) {
      if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_NotAllowSalesWithReturn',
          true
        ) &&
        !skipValidaton
      ) {
        var negativeLines,
          receiptLines = this.get('lines').length,
          selectedLines = selectedModels ? selectedModels.length : 0;
        negativeLines = _.filter(this.get('lines').models, function(line) {
          return line.get('qty') < 0;
        }).length;
        if (
          qty < 0 &&
          negativeLines === 0 &&
          selectedLines > 0 &&
          receiptLines === selectedLines
        ) {
          this.setOrderType('OBPOS_receipt.return', OB.DEC.One, {
            applyPromotions: false,
            saveOrder: true
          });
          return true;
        }
        if (qty > 0 && negativeLines > 0) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddPositive'));
          if (
            !OB.UTIL.isNullOrUndefined(OB.MobileApp.model.receipt.addProcess)
          ) {
            OB.MobileApp.model.receipt.addProcess = {};
          }
          return true;
        } else if (qty < 0 && negativeLines !== receiptLines) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddNegative'));
          return true;
        }
      }
      if (
        !OB.MobileApp.model.hasPermission(
          'OBPOS_AllowLayawaysNegativeLines',
          true
        ) &&
        this.isLayaway() &&
        qty < 0 &&
        !skipValidaton
      ) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed')
        );
        return true;
      }
      return false;
    },

    removeAndInsertLines: function(callback) {
      var me = this,
        orderlines = [],
        promotionlines = [],
        addProductsOfLines = null;

      // Remove all lines and insert again with new prices
      addProductsOfLines = async function(
        receipt,
        lines,
        index,
        callback,
        promotionLines
      ) {
        var success = function(product) {
          var attrs;
          if (!OB.UTIL.isNullOrUndefined(lines[index].get('splitline'))) {
            attrs = {
              splitline: lines[index].get('splitline'),
              originalLine: lines[index]
            };
          }
          attrs = attrs || {};
          attrs.organization = lines[index].get('organization');
          attrs.warehouse = lines[index].get('warehouse');
          me.addProduct(
            product,
            lines[index].get('qty'),
            undefined,
            attrs,
            function(isInPriceList) {
              if (isInPriceList) {
                me.get('lines')
                  .at(index)
                  .set('promotions', promotionLines[index]);
                me.get('lines')
                  .at(index)
                  .calculateGross();
                addProductsOfLines(
                  receipt,
                  lines,
                  index + 1,
                  callback,
                  promotionLines
                );
              } else {
                promotionLines.splice(index, 1);
                lines.splice(index, 1);
                addProductsOfLines(
                  receipt,
                  lines,
                  index,
                  callback,
                  promotionLines
                );
              }
            }
          );
        };
        if (index === lines.length) {
          me.set('skipCalculateReceipt', false);
          if (callback) {
            callback(true);
          }
          return;
        }
        if (OB.UTIL.isCrossStoreProduct(lines[index].get('product'))) {
          success(lines[index].get('product'));
        } else {
          try {
            const product = await OB.App.MasterdataModels.Product.withId(
              lines[index].get('product').id
            );
            if (product) {
              success(OB.Dal.transform(OB.Model.Product, product));
            } else {
              // Product doesn't exists, execute the same code as it was not included in pricelist
              promotionLines.splice(index, 1);
              lines.splice(index, 1);
              addProductsOfLines(
                receipt,
                lines,
                index,
                callback,
                promotionLines
              );
            }
          } catch (error) {
            OB.error(error.message);
          }
        }
      };
      _.each(this.get('lines').models, function(line) {
        orderlines.push(line);
        promotionlines.push(line.get('promotions'));
      });
      me.set('skipCalculateReceipt', true);
      this.deleteLinesFromOrder(orderlines, function(deleted) {
        if (deleted) {
          addProductsOfLines(me, orderlines, 0, callback, promotionlines);
        } else {
          me.set('skipCalculateReceipt', false);
          callback(false);
        }
      });
    },

    setOrderType: function(permission, orderType, options) {
      var me = this,
        i,
        approvalNeeded,
        servicesToApprove;

      function finishSetOrderType() {
        me.set('orderType', orderType); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
        if (orderType !== 3) {
          //Void this Layaway, do not need to save
          if (
            !(
              options &&
              !OB.UTIL.isNullOrUndefined(options.saveOrder) &&
              options.saveOrder === false
            )
          ) {
            me.save();
          }
        }
      }

      function returnLines() {
        me.set('preventServicesUpdate', true);
        _.each(
          me.get('lines').models,
          function(line) {
            if (line.get('qty') > 0) {
              me.returnLine(line, null, true);
            }
          },
          me
        );
        me.unset('preventServicesUpdate');
        finishSetOrderType();
      }
      if (orderType === OB.DEC.One) {
        this.set(
          'documentType',
          OB.UTIL.isCrossStoreReceipt(this)
            ? this.get('documentType')
            : OB.MobileApp.model.get('terminal').terminalType
                .documentTypeForReturns
        );
        if (options.saveOrder !== false) {
          approvalNeeded = false;
          servicesToApprove = '';
          for (i = 0; i < this.get('lines').models.length; i++) {
            var line = this.get('lines').models[i];
            if (!line.isReturnable()) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBPOS_UnreturnableProduct'),
                OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [
                  line.get('product').get('_identifier')
                ])
              );
              return;
            } else {
              if (line.get('product').get('productType') === 'S') {
                if (!approvalNeeded) {
                  approvalNeeded = true;
                }
                servicesToApprove +=
                  '<br>' +
                  OB.I18N.getLabel('OBMOBC_Character')[1] +
                  ' ' +
                  line.get('product').get('_identifier');
              }
            }
          }
          if (approvalNeeded) {
            OB.UTIL.Approval.requestApproval(
              OB.MobileApp.view.$.containerWindow.getRoot().model,
              [
                {
                  approval: 'OBPOS_approval.returnService',
                  message: 'OBPOS_approval.returnService',
                  params: [servicesToApprove]
                }
              ],
              function(approved, supervisor, approvalType) {
                if (approved) {
                  returnLines();
                }
              }
            );
          } else {
            returnLines();
          }
        }
      } else {
        if (!OB.UTIL.isCrossStoreReceipt(this)) {
          this.set(
            'documentType',
            OB.MobileApp.model.get('terminal').terminalType.documentType
          );
        }
        finishSetOrderType();
      }
    },

    // returns the ordertype: 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
    getOrderType: function() {
      return this.get('orderType');
    },

    shouldApplyPromotions: function() {
      // Do not apply promotions in return tickets
      return this.get('orderType') !== 1;
    },

    hasOneLineToIgnoreDiscounts: function() {
      return _.some(this.get('lines').models, function(line) {
        return line.get('product').get('ignorePromotions');
      });
    },

    setOrderInvoice: function() {
      var me = this;
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', true);
        this.save(function() {
          me.trigger('saveCurrent');
        });
      }
    },

    updatePrices: function(callback) {
      var order = this,
        newAllLinesCalculated;

      function allLinesCalculated() {
        callback(order);
      }

      newAllLinesCalculated = _.after(
        this.get('lines').length,
        allLinesCalculated
      );

      this.get('lines').each(async function(line) {
        //remove promotions
        line.unset('promotions');

        let criteria = {},
          successCallbackPrices = function(dataPrices) {
            if (
              !OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)
            ) {
              order.setPrice(
                line,
                dataPrices.get('standardPrice', {
                  setUndo: false
                })
              );
            } else {
              dataPrices.each(function(price) {
                order.setPrice(
                  line,
                  price.get('standardPrice', {
                    setUndo: false
                  })
                );
              });
            }

            newAllLinesCalculated();
          };
        if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          try {
            const product = await OB.App.MasterdataModels.Product.withId(
              line.get('product').get('id')
            );
            successCallbackPrices(OB.Dal.transform(OB.Model.Product, product));
          } catch (error) {
            OB.error(error.message);
          }
        } else {
          criteria = {};
          var remoteCriteria = [];
          var productId = {
            columns: ['id'],
            operator: 'equals',
            value: line.get('product').get('id'),
            isId: true
          };
          remoteCriteria.push(productId);
          criteria.remoteFilters = remoteCriteria;

          OB.Dal.find(
            OB.Model.Product,
            criteria,
            successCallbackPrices,
            function() {
              // TODO: Report error properly.
            },
            line
          );
        }

        successCallbackPrices = function(dataPrices) {
          dataPrices.each(function(price) {
            order.setPrice(line, price.get('standardPrice'), {
              setUndo: false
            });
          });
          newAllLinesCalculated();
        };
      });
    },

    setQuantitiesToDeliver: function() {
      var me = this,
        fullyPaid = this.isFullyPaid() || this.get('payOnCredit'),
        receiptCompleted =
          this.get('completeTicket') || this.get('payOnCredit'),
        prePaid = !fullyPaid && this.get('completeTicket'),
        isCrossStoreReceipt = OB.UTIL.isCrossStoreReceipt(this);
      if (fullyPaid || prePaid) {
        _.each(this.get('lines').models, function(line) {
          if (fullyPaid) {
            line.set('obposCanbedelivered', true);
            line.set('obposIspaid', true);
          } else if (prePaid && line.get('obposCanbedelivered')) {
            line.set('obposIspaid', true);
          }
        });
      }

      _.each(this.get('lines').models, function(line) {
        if (isCrossStoreReceipt && !line.has('originalOrderLineId')) {
          line.set('obposQtytodeliver', line.getDeliveredQuantity());
        } else if (!line.has('obposQtytodeliver')) {
          if (receiptCompleted) {
            if (
              line.get('product').get('productType') === 'S' &&
              line.get('product').get('isLinkedToProduct')
            ) {
              if (line.get('qty') > 0) {
                var qtyToDeliver = OB.DEC.Zero;
                line.get('relatedLines').forEach(function(relatedLine) {
                  var orderline = me.get('lines').get(relatedLine.orderlineId);
                  if (orderline && orderline.get('obposIspaid')) {
                    qtyToDeliver = OB.DEC.add(
                      qtyToDeliver,
                      orderline.get('qty')
                    );
                  } else if (relatedLine.obposIspaid) {
                    qtyToDeliver = OB.DEC.add(
                      qtyToDeliver,
                      relatedLine.deliveredQuantity
                    );
                  }
                });
                if (
                  qtyToDeliver &&
                  line.get('product').get('quantityRule') === 'UQ'
                ) {
                  qtyToDeliver = OB.DEC.One;
                }
                line.set('obposQtytodeliver', qtyToDeliver);
                if (qtyToDeliver) {
                  line.set('obposCanbedelivered', true);
                }
              } else if (line.get('qty') < 0) {
                line.set('obposQtytodeliver', line.get('qty'));
                line.set('obposCanbedelivered', true);
              }
            } else {
              if (line.get('obposCanbedelivered')) {
                line.set('obposQtytodeliver', line.get('qty'));
              } else {
                line.set('obposQtytodeliver', line.getDeliveredQuantity());
              }
            }
          } else {
            line.set('obposQtytodeliver', line.getDeliveredQuantity());
          }
        }
      });

      if (receiptCompleted) {
        var lineToDeliver = _.find(this.get('lines').models, function(line) {
          var qtyToDeliver = line.has('obposQtytodeliver')
            ? line.get('obposQtytodeliver')
            : line.get('qty');
          return qtyToDeliver !== line.getDeliveredQuantity();
        });
        var linePendingToDeliver = _.find(this.get('lines').models, function(
          line
        ) {
          var qtyToDeliver = line.has('obposQtytodeliver')
            ? line.get('obposQtytodeliver')
            : line.get('qty');
          return qtyToDeliver !== line.get('qty');
        });
        this.set('generateShipment', !_.isUndefined(lineToDeliver));
        this.set('deliver', _.isUndefined(linePendingToDeliver));
      } else {
        this.set('generateShipment', false);
        this.set('deliver', false);
      }
    },
    canCancelOrder: function(
      cancellingReceipt,
      params,
      successCallback,
      errorCallback
    ) {
      var process = new OB.DS.Process(
          'org.openbravo.retail.posterminal.process.IsOrderCancelled'
        ),
        receipt = cancellingReceipt || this,
        orderLines = [];
      _.each(receipt.get('lines').models, function(line) {
        orderLines.push({
          id: line.get('id'),
          loaded: line.get('loaded')
        });
      });
      process.exec(
        {
          orderId: receipt.get('id'),
          documentNo: receipt.get('documentNo'),
          orderLoaded: receipt.get('loaded'),
          orderLines: orderLines,
          checkNotEditableLines: params ? params.checkNotEditableLines : false,
          checkNotDeliveredDeferredServices: params
            ? params.checkNotDeliveredDeferredServices
            : false
        },
        successCallback,
        errorCallback
      );
    },
    verifyCancelAndReplace: function(context) {
      var me = this;
      this.checkNotProcessedPayments(function() {
        me.canCancelOrder(
          null,
          {
            checkNotEditableLines: true
          },
          function(data) {
            if (data && data.exception) {
              if (data.exception.message) {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBMOBC_Error'),
                  data.exception.message
                );
                return;
              }
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
              );
              return;
            } else if (data && data.orderCancelled) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBPOS_OrderReplacedError')
              );
              return;
            } else if (
              data &&
              data.notDeliveredDeferredServices &&
              data.notDeliveredDeferredServices.length
            ) {
              var components = [];
              components.push({
                content: OB.I18N.getLabel('OBPOS_CannotCancelLayWithDeferred'),
                classes:
                  'confirmationPopup-body_cannotCancelLayWithDeferred confirmationPopup-body_generic'
              });
              components.push({
                content: OB.I18N.getLabel('OBPOS_RelatedOrders'),
                classes:
                  'confirmationPopup-body_relatedOrders confirmationPopup-body_generic'
              });
              _.each(data.notDeliveredDeferredServices, function(documentNo) {
                components.push({
                  content:
                    OB.I18N.getLabel('OBMOBC_Character')[1] + ' ' + documentNo,
                  classes:
                    'confirmationPopup-body_character confirmationPopup-body_generic'
                });
              });
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                components
              );
              return;
            } else {
              OB.UTIL.HookManager.executeHooks(
                'OBPOS_PreCancelAndReplace',
                {
                  context: context
                },
                function(args) {
                  if (args && args.cancelOperation) {
                    return;
                  }
                  me.cancelAndReplaceOrder(context, data.deferredLines);
                }
              );
            }
          },
          function() {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
            );
          }
        );
      });
    },

    cancelAndReplaceOrder: function(context, deferredLines) {
      var idMap = {},
        me = this,
        splittedDocNo = [],
        terminalDocNoPrefix,
        newDocNo = '',
        cancelAndReplaceSeparator =
          OB.MobileApp.model.get('terminal').cancelAndReplaceSeparator || '-';

      //Cloning order to be canceled
      var clonedreceipt = new OB.Model.Order();
      OB.UTIL.clone(me, clonedreceipt);

      OB.Dal.remove(
        this,
        function() {
          OB.MobileApp.model.orderList.remove(me, {
            silent: true
          });
          var deliveredLine,
            linesWithDeferred = [];

          me.preventOrderSave(true);
          me.set('preventServicesUpdate', true);
          me.unset('orderid');

          if (me.get('paidOnCredit')) {
            me.set('paidOnCredit', false);
            me.set('paidPartiallyOnCredit', false);
            me.set('creditAmount', OB.DEC.Zero);
          }

          me.set('canceledorder', clonedreceipt);
          me.set('doCancelAndReplace', true);

          me.set('hasbeenpaid', 'N');
          me.set('isPaid', false);
          me.set('isEditable', true);

          deliveredLine = _.find(me.get('lines').models, function(line) {
            return (
              line.get('deliveredQuantity') &&
              OB.DEC.compare(line.get('deliveredQuantity')) === 1
            );
          });
          if (
            me.get('isLayaway') ||
            (OB.MobileApp.model.get('terminal').terminalType.layawayorder &&
              !deliveredLine)
          ) {
            OB.MobileApp.view.$.containerWindow.getRoot().showDivText(null, {
              permission: null,
              orderType: 2
            });
          }
          me.set('isLayaway', false);

          me.get('lines').each(function(line) {
            idMap[line.get('id')] = OB.UTIL.get_UUID();
            line.set('replacedorderline', line.get('id'));
            line.set('id', idMap[line.get('id')]);
            line.unset('invoicedQuantity');
            line.unset('grossUnitPrice');
            line.unset('lineGrossAmount');
            if (
              !line.get('obposCanbedelivered') &&
              line.get('deliveredQuantity') === line.get('qty')
            ) {
              line.set('obposCanbedelivered', true);
            }
            line.set('obposIspaid', false);
            line.set('documentType', me.get('documentType'));
          });

          // The lines must be iterated a second time after finishing the first loop, to ensure that
          // all lines are included in the idMap map when updating the service relations
          if (me.get('hasServices')) {
            me.get('lines').each(function(line) {
              if (line.get('relatedLines')) {
                line.get('relatedLines').forEach(function(rl) {
                  rl.orderId = me.get('id');
                  if (idMap[rl.orderlineId]) {
                    rl.orderlineId = idMap[rl.orderlineId];
                  }
                });
              }
            });
          }

          me.set('replacedorder_documentNo', me.get('documentNo'));
          me.set('replacedorder', me.get('id'));
          me.unset('id');
          me.set('session', OB.MobileApp.model.get('session'));

          me.unset('invoiceCreated');
          me.set(
            'generateInvoice',
            OB.MobileApp.model.get('terminal').terminalType.generateInvoice
          );
          me.set(
            'documentType',
            OB.MobileApp.model.get('terminal').terminalType.documentType
          );

          me.set('createdBy', OB.MobileApp.model.get('orgUserId'));
          me.set('cashVAT', OB.MobileApp.model.get('terminal').cashVat);
          if (!me.get('salesRepresentative')) {
            if (OB.MobileApp.model.get('context').isSalesRepresentative) {
              me.set(
                'salesRepresentative',
                OB.MobileApp.model.get('context').user.id
              );
            } else {
              me.unset('salesRepresentative');
            }
          }

          if (deferredLines.length) {
            linesWithDeferred.push(
              OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBody')
            );
          }
          //Set to not editable and not deletable to all deferred lines or lines that have deferred services
          _.each(deferredLines, function(deferredLine) {
            var deffLine = _.find(me.get('lines').models, function(line) {
              return (
                deferredLine ===
                OB.DEC.mul(OB.DEC.add(line.get('linepos'), 1), 10)
              );
            });
            deffLine.set('isEditable', false);
            deffLine.set('isDeletable', false);
            linesWithDeferred.push(
              OB.I18N.getLabel('OBMOBC_Character')[1] +
                ' ' +
                deffLine.get('product').get('_identifier') +
                ' (' +
                OB.I18N.getLabel('OBPOS_LineQuantity') +
                ': ' +
                deffLine.get('qty') +
                ')'
            );
          });
          if (deferredLines.length) {
            linesWithDeferred.push(
              OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBodyFooter')
            );
            linesWithDeferred.push(
              OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBodyFooter2')
            );
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_NotModifiableLines'),
              linesWithDeferred
            );
          }

          me.set('orderDate', new Date());
          me.set('creationDate', null);

          me.set('negativeDocNo', me.get('documentNo') + '*R*');
          newDocNo = '';
          terminalDocNoPrefix =
            OB.MobileApp.model.attributes.terminal.docNoPrefix;
          splittedDocNo = me
            .get('documentNo')
            .substring(
              terminalDocNoPrefix.length +
                (OB.Model.Order.prototype.includeDocNoSeperator ? 1 : 0),
              me.get('documentNo').length
            )
            .split(cancelAndReplaceSeparator);
          if (splittedDocNo.length > 1) {
            var nextNumber =
              parseInt(splittedDocNo[splittedDocNo.length - 1], 10) + 1;
            newDocNo =
              me
                .get('documentNo')
                .substring(
                  0,
                  me.get('documentNo').lastIndexOf(cancelAndReplaceSeparator)
                ) +
              cancelAndReplaceSeparator +
              nextNumber;
          } else {
            newDocNo = me.get('documentNo') + cancelAndReplaceSeparator + '1';
          }
          me.set('documentNo', newDocNo);
          me.set('posTerminal', OB.MobileApp.model.get('terminal').id);

          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostCancelAndReplace',
            {
              context: context,
              receipt: me
            },
            function(args) {
              OB.UTIL.showSuccess(
                OB.I18N.getLabel('OBPOS_OrderReplaced', [
                  me.get('replacedorder_documentNo'),
                  me.get('documentNo')
                ])
              );
              me.calculateReceipt(function() {
                me.unset('skipApplyPromotions');
                me.unset('preventServicesUpdate');
                me.preventOrderSave(false);
                me.save();
                OB.MobileApp.model.orderList.unshift(me, {
                  silent: true
                });
              });
            }
          );
          // Set the last line as selected to call the 'onRearrangeEditButtonBar' event and update the isEditable and
          // isDeletable status for the lines (to hide or show the buttons)
          if (deferredLines.length) {
            me.get('lines')
              .at(me.get('lines').models.length - 1)
              .trigger(
                'selected',
                me.get('lines').at(me.get('lines').models.length - 1)
              );
          }
        },
        function() {
          OB.UTIL.showError('Error removing');
        }
      );
    },

    checkNotProcessedPayments: function(callback) {
      var notPrePayments;
      notPrePayments = _.filter(this.get('payments').models, function(payment) {
        return !payment.get('isPrePayment');
      });
      if (notPrePayments.length) {
        var paymentList = [OB.I18N.getLabel('OBPOS_C&RDeletePaymentsBodyInit')];
        var symbol = OB.MobileApp.model.get('terminal').symbol;
        var symbolAtRight = OB.MobileApp.model.get('terminal')
          .currencySymbolAtTheRight;
        _.each(notPrePayments, function(payment) {
          paymentList.push(
            OB.I18N.getLabel('OBMOBC_Character')[1] +
              ' ' +
              payment.get('name') +
              ' (' +
              OB.I18N.formatCurrencyWithSymbol(
                payment.get('amount'),
                symbol,
                symbolAtRight
              ) +
              ')'
          );
        });
        paymentList.push(OB.I18N.getLabel('OBPOS_C&RDeletePaymentsBodyEnd'));
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_C&RDeletePaymentsHeader'),
          paymentList
        );
      } else {
        callback();
      }
    },

    cancelLayaway: function(context) {
      var me = this;
      this.checkNotProcessedPayments(function() {
        me.canCancelOrder(
          null,
          {
            checkNotDeliveredDeferredServices: true
          },
          function(data) {
            if (data && data.exception) {
              if (data.exception.message) {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBMOBC_Error'),
                  data.exception.message
                );
                return;
              }
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
              );
              return;
            } else if (data && data.orderCancelled) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBPOS_OrderCanceledError')
              );
              return;
            } else if (
              data &&
              data.notDeliveredDeferredServices &&
              data.notDeliveredDeferredServices.length
            ) {
              var components = [];
              components.push({
                content: OB.I18N.getLabel('OBPOS_CannotCancelLayWithDeferred'),
                classes:
                  'confirmationPopup-body_cannotCancelLayWithDeferred confirmationPopup-body_generic'
              });
              components.push({
                content: OB.I18N.getLabel('OBPOS_RelatedOrders'),
                classes:
                  'confirmationPopup-body_relatedOrders confirmationPopup-body_generic'
              });
              _.each(data.notDeliveredDeferredServices, function(documentNo) {
                components.push({
                  content:
                    OB.I18N.getLabel('OBMOBC_Character')[1] + ' ' + documentNo,
                  classes:
                    'confirmationPopup-body_character confirmationPopup-body_generic'
                });
              });
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                components
              );
              return;
            } else {
              var cancelLayawayOrder = function() {
                OB.UTIL.HookManager.executeHooks(
                  'OBPOS_PreCancelLayaway',
                  {
                    context: context
                  },
                  function(args) {
                    if (args && args.cancelOperation) {
                      return;
                    }
                    //Cloning order to be canceled
                    var clonedReceipt = new OB.Model.Order();
                    OB.UTIL.clone(me, clonedReceipt);
                    OB.Dal.remove(me, function() {
                      OB.MobileApp.model.orderList.remove(me, {
                        silent: true
                      });
                      var idMap = {};
                      me.set('skipCalculateReceipt', true);
                      me.preventOrderSave(true);
                      me.set('preventServicesUpdate', true);
                      me.set('isEditable', true);
                      me.set('cancelLayaway', true);
                      me.set('fromLayaway', me.get('isLayaway'));
                      me.set('isLayaway', false);
                      me.set('isPaid', false);
                      // Set the order type
                      context.doShowDivText({
                        permission: context.permission,
                        orderType: 3
                      });
                      me.set(
                        'posTerminal',
                        OB.MobileApp.model.get('terminal').id
                      );
                      me.set(
                        'obposAppCashup',
                        OB.MobileApp.model.get('terminal').cashUpId
                      );
                      me.set('timezoneOffset', new Date().getTimezoneOffset());
                      var linesToDelete = [];
                      _.each(me.get('lines').models, function(line) {
                        if (
                          OB.DEC.compare(line.getQty()) === 1 &&
                          line.getDeliveredQuantity() !== line.getQty()
                        ) {
                          var canceledQty =
                            line.getDeliveredQuantity() - line.getQty();
                          _.each(line.get('promotions'), function(promotion) {
                            promotion.amt = OB.DEC.mul(
                              OB.DEC.mul(
                                promotion.amt,
                                OB.DEC.div(
                                  OB.DEC.abs(canceledQty),
                                  line.getQty()
                                )
                              ),
                              -1
                            );
                            promotion.actualAmt = OB.DEC.mul(
                              OB.DEC.mul(
                                promotion.actualAmt,
                                OB.DEC.div(
                                  OB.DEC.abs(canceledQty),
                                  line.getQty()
                                )
                              ),
                              -1
                            );
                            promotion.displayedTotalAmount = OB.DEC.mul(
                              OB.DEC.mul(
                                promotion.displayedTotalAmount,
                                OB.DEC.div(
                                  OB.DEC.abs(canceledQty),
                                  line.getQty()
                                )
                              ),
                              -1
                            );
                          });
                          line.set('canceledLine', line.get('id'));
                          var newId = OB.UTIL.get_UUID();
                          idMap[line.get('id')] = newId;
                          line.set('id', newId);
                          line.set('qty', canceledQty);
                          line.unset('deliveredQuantity');
                          line.unset('invoicedQuantity');
                          line.set('obposCanbedelivered', true);
                          line.set('obposIspaid', false);
                        } else {
                          linesToDelete.push(line);
                        }
                      });
                      if (linesToDelete.length) {
                        me.get('lines').remove(linesToDelete);
                      }
                      // Remove or update the related lines id
                      _.each(me.get('lines').models, function(line) {
                        if (
                          line.get('product').get('productType') === 'S' &&
                          line.get('product').get('isLinkedToProduct')
                        ) {
                          var relationsToRemove = [];
                          _.each(line.get('relatedLines'), function(
                            relatedLine
                          ) {
                            if (idMap[relatedLine.orderlineId]) {
                              relatedLine.orderlineId =
                                idMap[relatedLine.orderlineId];
                            } else if (!relatedLine.deferred) {
                              relationsToRemove.push(relatedLine);
                            }
                          });
                          // Remove the lines that have been deleted from the inverse ticket
                          if (relationsToRemove.length) {
                            _.each(relationsToRemove, function(
                              relationToRemove
                            ) {
                              var idx = line
                                .get('relatedLines')
                                .map(function(l) {
                                  return l.orderlineId;
                                })
                                .indexOf(relationToRemove.orderlineId);
                              line.get('relatedLines').splice(idx, 1);
                            });
                          }
                        }
                      });
                      if (me.get('paidOnCredit')) {
                        me.set('paidOnCredit', false);
                        me.set('paidPartiallyOnCredit', false);
                        me.set('creditAmount', OB.DEC.Zero);
                      }
                      me.set('canceledorder', clonedReceipt);
                      me.set('orderDate', new Date());
                      me.set('documentNo', me.get('documentNo') + '*R*');
                      me.unset('generateInvoice');
                      me.set(
                        'nettingPayment',
                        OB.DEC.sub(me.getPayment(), me.getGross())
                      );
                      me.get('payments').reset();
                      me.set('forceCalculateTaxes', true);
                      me.unset('id');
                      me.unset('skipCalculateReceipt');
                      me.calculateReceipt(function() {
                        me.getPrepaymentAmount(function() {
                          me.set('isEditable', false);
                          me.unset('preventServicesUpdate');
                          me.preventOrderSave(false);
                          me.save();
                          OB.MobileApp.model.orderList.unshift(me, {
                            silent: true
                          });
                          OB.MobileApp.model.orderList.saveCurrent();
                          me.trigger('updatePending', true);
                          // Finally change to the payments tab
                          context.doTabChange({
                            tabPanel: 'payment',
                            keyboard: 'toolbarpayment',
                            edit: false
                          });
                        }, true);
                      });
                    });
                  }
                );
              };

              if (me.getPayment() < me.getDeliveredQuantityAmount()) {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBPOS_Attention'),
                  OB.I18N.getLabel('OBPOS_DeliveredMoreThanPaid'),
                  [
                    {
                      label: OB.I18N.getLabel('OBMOBC_LblOk'),
                      action: function() {
                        cancelLayawayOrder();
                      }
                    },
                    {
                      label: OB.I18N.getLabel('OBMOBC_LblCancel')
                    }
                  ]
                );
              } else {
                cancelLayawayOrder();
              }
            }
          },
          function() {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
            );
          }
        );
      });
    },

    createQuotation: function() {
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.quotation')) {
        this.set('isQuotation', true);
        this.set('generateInvoice', false);
        this.set(
          'documentType',
          OB.MobileApp.model.get('terminal').terminalType
            .documentTypeForQuotations
        );
        this.save();
      }
    },

    setQuotationProperties: function() {
      this.set('isQuotation', true);
      this.set('generateInvoice', false);
      this.set('orderType', 0);
      this.set(
        'documentType',
        OB.MobileApp.model.get('terminal').terminalType
          .documentTypeForQuotations
      );
      var nextQuotationno = OB.MobileApp.model.getNextQuotationno();
      this.set(
        'quotationnoPrefix',
        OB.MobileApp.model.get('terminal').quotationDocNoPrefix
      );
      this.set('quotationnoSuffix', nextQuotationno.quotationnoSuffix);
      this.set('documentNo', nextQuotationno.documentNo);
    },

    createQuotationFromOrder: function() {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreCreateQuotationFromOrder',
        {
          order: this
        },
        function(args) {
          if (args && args.cancelOperation && args.cancelOperation === true) {
            return;
          }
          args.order.setQuotationProperties();
          args.order.trigger('scan');
          args.order.save();
        }
      );
    },

    createOrderFromQuotation: function(updatePrices, callback) {
      var me = this,
        execution = OB.UTIL.ProcessController.start('createOrderFromQuotation'),
        idMap = {},
        oldIdMap = {},
        oldId,
        productHasAttribute = false,
        productWithAttributeValue = [],
        needAttributeWhenCreatingQuotation = OB.MobileApp.model.hasPermission(
          'OBPOS_AskForAttributesWhenCreatingQuotation',
          true
        ),
        attributeSearchAllowed = OB.MobileApp.model.hasPermission(
          'OBPOS_EnableSupportForProductAttributes',
          true
        ),
        callQuotationAttrs;
      OB.UTIL.StockUtils.checkOrderLinesStock([this], function(hasStock) {
        if (hasStock) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PreCreateOrderFromQuotation',
            {
              updatePrices: updatePrices,
              order: me
            },
            function(args) {
              if (
                args &&
                args.cancelOperation &&
                args.cancelOperation === true
              ) {
                OB.UTIL.ProcessController.finish(
                  'createOrderFromQuotation',
                  execution
                );
                if (callback) {
                  callback(false);
                }
                return;
              }
              args.order.get('lines').each(function(line) {
                oldId = line.get('id');
                line.set('id', OB.UTIL.get_UUID());
                //issue 25055 -> If we don't do the following prices and taxes are calculated
                //wrongly because the calculation starts with discountedNet instead of
                //the real net.
                //It only happens if the order is created from quotation just after save the quotation
                //(without load the quotation from quotations window)
                if (!this.get('priceIncludesTax')) {
                  line.set('net', line.get('nondiscountednet'));
                }

                //issues 24994 & 24993
                //if the order is created from quotation just after save the quotation
                //(without load the quotation from quotations window). The order has the fields added
                //by adjust prices. We need to work without these values
                //price not including taxes
                line.unset('nondiscountedprice');
                line.unset('nondiscountednet');
                //price including taxes
                line.unset('netFull');
                line.unset('grossListPrice');
                line.unset('grossUnitPrice');
                line.unset('lineGrossAmount');
                line.unset('obposQtytodeliver');
                idMap[line.get('id')] = OB.UTIL.get_UUID();
                line.set('id', idMap[line.get('id')]);
                if (line.get('hasRelatedServices')) {
                  oldIdMap[oldId] = line.get('id');
                }
              }, args.order);

              args.order.set('oldId', args.order.get('id'));
              args.order.set('id', null);
              args.order.set('isQuotation', false);
              args.order.set(
                'orderType',
                OB.MobileApp.model.get('terminal').terminalType.layawayorder
                  ? 2
                  : 0
              );
              args.order.set(
                'generateInvoice',
                OB.MobileApp.model.get('terminal').terminalType.generateInvoice
              );
              args.order.set(
                'documentType',
                OB.UTIL.isCrossStoreReceipt(args.order)
                  ? args.order.get('lines').models[0].get('documentTypeId')
                  : OB.MobileApp.model.get('terminal').terminalType.documentType
              );
              args.order.set('createdBy', OB.MobileApp.model.get('orgUserId'));
              if (
                OB.MobileApp.model.get('context').user.isSalesRepresentative
              ) {
                args.order.set(
                  'salesRepresentative',
                  OB.MobileApp.model.get('context').user.id
                );
              } else {
                args.order.set('salesRepresentative', null);
              }
              args.order.set('hasbeenpaid', 'N');
              args.order.set('skipApplyPromotions', false);
              args.order.set('isPaid', false);
              args.order.set('isEditable', true);
              args.order.set('orderDate', OB.I18N.normalizeDate(new Date()));
              args.order.set('creationDate', null);
              var nextDocumentno = OB.MobileApp.model.getNextDocumentno();
              args.order.set(
                'documentnoPrefix',
                OB.MobileApp.model.get('terminal').docNoPrefix
              );
              args.order.set(
                'documentnoSuffix',
                nextDocumentno.documentnoSuffix
              );
              args.order.set('quotationnoPrefix', -1);
              args.order.set('quotationnoSuffix', -1);
              args.order.set('returnnoPrefix', -1);
              args.order.set('returnnoSuffix', -1);
              args.order.set('documentNo', nextDocumentno.documentNo);
              args.order.set(
                'posTerminal',
                OB.MobileApp.model.get('terminal').id
              );
              args.order.set('session', OB.MobileApp.model.get('session'));
              args.order.unset('deletedLines');
              args.order.save();

              args.order.get('lines').each(function(line) {
                var productAttributes = line
                  .get('product')
                  .get('hasAttributes');
                if (
                  OB.UTIL.isNullOrUndefined(productAttributes) === false &&
                  productAttributes
                ) {
                  productWithAttributeValue.push(line);
                  productHasAttribute = productAttributes;
                }
                if (line.get('relatedLines')) {
                  line.get('relatedLines').forEach(function(rl) {
                    rl.orderId = args.order.get('id');
                    rl.orderDocumentNo = args.order.get('documentNo');
                    if (oldIdMap[rl.orderlineId]) {
                      rl.orderlineId = oldIdMap[rl.orderlineId];
                    }
                  });
                }
              }, args.order);

              callQuotationAttrs = function(order) {
                OB.UTIL.showSuccess(
                  OB.I18N.getLabel('OBPOS_QuotationCreatedOrder')
                );
                OB.UTIL.ProcessController.finish(
                  'createOrderFromQuotation',
                  execution
                );
                // This event is used in stock validation module.
                order.trigger('orderCreatedFromQuotation');
                //call quotation attributes popup
                if (
                  attributeSearchAllowed &&
                  needAttributeWhenCreatingQuotation === false &&
                  productHasAttribute
                ) {
                  OB.MobileApp.view.waterfall('onShowPopup', {
                    popup: 'modalQuotationProductAttributes',
                    args: {
                      lines: productWithAttributeValue,
                      quotationProductAttribute: order
                    }
                  });
                }
                if (callback) {
                  callback(true);
                }
              };
              if (updatePrices) {
                args.order.updatePrices(function(order) {
                  order.calculateReceipt(function() {
                    callQuotationAttrs(order);
                  });
                });
              } else {
                args.order.set('skipApplyPromotions', true);
                args.order.calculateReceipt(function() {
                  args.order.unset('skipApplyPromotions');
                  callQuotationAttrs(args.order);
                });
              }
            }
          );
        } else {
          OB.UTIL.ProcessController.finish(
            'createOrderFromQuotation',
            execution
          );
        }
      });
    },

    reactivateQuotation: function() {
      var nextQuotationno,
        idMap = {},
        oldIdMap = {},
        oldId,
        me = this;
      this.get('lines').each(function(line) {
        oldId = line.get('id');
        line.set('id', OB.UTIL.get_UUID());
        if (!this.get('priceIncludesTax')) {
          line.set('net', line.get('nondiscountednet'));
        }
        line.unset('nondiscountedprice');
        line.unset('nondiscountednet');
        line.unset('netFull');
        line.unset('grossListPrice');
        line.unset('grossUnitPrice');
        line.unset('lineGrossAmount');
        line.unset('promotions');
        idMap[line.get('id')] = OB.UTIL.get_UUID();
        line.set('id', idMap[line.get('id')]);
        if (line.get('hasRelatedServices')) {
          oldIdMap[oldId] = line.get('id');
        }
      }, this);
      this.set('hasbeenpaid', 'N');
      this.set('isPaid', false);
      this.set('isEditable', true);
      this.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      this.set('session', OB.MobileApp.model.get('session'));
      this.set('orderDate', OB.I18N.normalizeDate(new Date()));
      this.set('skipApplyPromotions', false);
      this.unset('deletedLines');
      //Sometimes the Id of Quotation is null.
      if (this.get('id') && !_.isNull(this.get('id'))) {
        this.set('oldId', this.get('id'));
        nextQuotationno = OB.MobileApp.model.getNextQuotationno();
        this.set(
          'quotationnoPrefix',
          OB.MobileApp.model.get('terminal').quotationDocNoPrefix
        );
        this.set('quotationnoSuffix', nextQuotationno.quotationnoSuffix);
        this.set('documentNo', nextQuotationno.documentNo);
      } else {
        //this shouldn't happen.
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_QuotationCannotBeReactivated_title'),
          OB.I18N.getLabel('OBPOS_QuotationCannotBeReactivated_body')
        );
        return;
      }
      if (this.get('hasServices')) {
        this.get('lines').each(function(line) {
          if (line.get('relatedLines')) {
            line.get('relatedLines').forEach(function(rl) {
              rl.orderId = me.get('id');
              if (oldIdMap[rl.orderlineId]) {
                rl.orderlineId = oldIdMap[rl.orderlineId];
              }
            });
          }
        }, this);
      }
      this.set('id', null);
      this.save();
      this.calculateReceipt();
    },
    rejectQuotation: function(rejectReasonId, scope, callback) {
      if (!this.get('id')) {
        OB.error(
          'The Id of the order is not defined (current value: ' +
            this.get('id') +
            "'"
        );
      }
      var process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.QuotationsReject'
      );
      OB.UTIL.showLoading(true);
      process.exec(
        {
          messageId: OB.UTIL.get_UUID(),
          data: [
            {
              id: this.get('id'),
              orderid: this.get('id'),
              rejectReasonId: rejectReasonId
            }
          ]
        },
        function(data) {
          OB.UTIL.showLoading(false);
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_SuccessRejectQuotation'));
          if (callback) {
            callback.call(scope, data !== null);
          }
        }
      );
    },
    resetOrderInvoice: function() {
      var me = this;
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', false);
        this.save(function() {
          me.trigger('saveCurrent');
        });
      }
    },
    getPrecision: function(payment) {
      var terminalpayment =
        OB.MobileApp.model.paymentnames[payment.get('kind')];
      return terminalpayment
        ? terminalpayment.obposPosprecision
        : OB.DEC.getScale();
    },
    getSumOfOrigAmounts: function(paymentToIgnore) {
      //returns a result with the sum up of every payments based on origAmount field
      //if paymentToIignore parameter is provided the result will exclude that payment
      var payments = this.get('payments');
      var sumOfPayments = OB.DEC.Zero;
      if (payments && payments.length > 0) {
        sumOfPayments = _.reduce(
          payments.models,
          function(memo, pymnt, index) {
            if (
              paymentToIgnore &&
              pymnt.get('kind') === paymentToIgnore.get('kind')
            ) {
              return OB.DEC.add(memo, OB.DEC.Zero);
            } else {
              return OB.DEC.add(memo, pymnt.get('origAmount'));
            }
          },
          OB.DEC.Zero
        );
        return sumOfPayments;
      } else {
        return sumOfPayments;
      }
    },
    getDifferenceBetweenPaymentsAndTotal: function(paymentToIgnore) {
      //Returns the difference (abs) between total to pay and payments.
      //if paymentToIignore parameter is provided the result will exclude that payment.
      return OB.DEC.abs(
        OB.DEC.sub(
          OB.DEC.abs(this.getTotal()),
          OB.DEC.sub(
            this.getSumOfOrigAmounts(paymentToIgnore),
            this.getChange()
          )
        )
      );
    },
    getDifferenceRemovingSpecificPayment: function(currentPayment) {
      //Returns the difference (abs) between total to pay and payments without take into account currentPayment
      //Result is returned in the currency used by current payment
      var differenceInDefaultCurrency;
      var differenceInForeingCurrency;
      var p = this.getPrecision(currentPayment);
      differenceInDefaultCurrency = this.getDifferenceBetweenPaymentsAndTotal(
        currentPayment
      );
      if (currentPayment && currentPayment.get('rate')) {
        differenceInForeingCurrency = OB.DEC.div(
          differenceInDefaultCurrency,
          currentPayment.get('rate'),
          p
        );
        return differenceInForeingCurrency;
      } else {
        return differenceInDefaultCurrency;
      }
    },
    adjustPayment: function() {
      var me = this,
        i,
        max,
        p,
        sumCash,
        setOrigAmount,
        pcash,
        precision,
        multiCurrencyDifference,
        isNegative,
        payments = this.get('payments'),
        total = this.get('prepaymentChangeMode')
          ? this.get('obposPrepaymentamt')
          : this.getTotal(),
        noCash = OB.DEC.Zero,
        defaultCash = OB.DEC.Zero,
        nonDefaultCash = OB.DEC.Zero,
        totalCash = OB.DEC.Zero,
        totalPaid = OB.DEC.Zero,
        processedPaymentsAmount = OB.DEC.Zero,
        reversedPaymentsAmount = OB.DEC.Zero,
        notModifiableAmount = OB.DEC.Zero,
        loadedFromBackend = this.get('isLayaway') || this.get('isPaid');

      setOrigAmount = function(payment) {
        precision = me.getPrecision(payment);
        if (payment.get('rate') && payment.get('rate') !== '1') {
          payment.set(
            'origAmount',
            OB.DEC.div(payment.get('amount'), payment.get('mulrate'))
          );
          //Here we are trying to know if the current payment is making the pending to pay 0.
          //to know that we are suming up every payments except the current one (getSumOfOrigAmounts)
          //then we substract this amount from the total (getDifferenceBetweenPaymentsAndTotal)
          //and finally we transform this difference to the foreign amount
          //if the payment in the foreign amount makes pending to pay zero, then we will ensure that the payment
          //in the default currency is satisfied
          if (
            OB.DEC.compare(
              OB.DEC.sub(
                me.getDifferenceRemovingSpecificPayment(payment),
                OB.DEC.abs(payment.get('amount'), precision),
                precision
              )
            ) === OB.DEC.Zero
          ) {
            multiCurrencyDifference = me.getDifferenceBetweenPaymentsAndTotal(
              payment
            );
            if (
              OB.DEC.abs(payment.get('origAmount')) !==
              OB.DEC.abs(multiCurrencyDifference)
            ) {
              payment.set(
                'origAmount',
                payment.get('changePayment')
                  ? OB.DEC.mul(multiCurrencyDifference, -1)
                  : multiCurrencyDifference
              );
            }
          }
        } else {
          payment.set('origAmount', payment.get('amount'));
        }
        payment.set('paid', payment.get('origAmount'));
        payment.set('precision', precision);
      };

      _.each(payments.models, function(payment) {
        if (payment.get('isPrePayment')) {
          setOrigAmount(payment);
          processedPaymentsAmount = OB.DEC.add(
            processedPaymentsAmount,
            payment.get('origAmount'),
            precision
          );
        }
      });

      // Add the netting amount (for CL) to the processed payments amount
      processedPaymentsAmount = OB.DEC.add(
        processedPaymentsAmount,
        this.getNettingPayment(),
        precision
      );

      // Set the 'isNegative' value
      if (loadedFromBackend) {
        isNegative = OB.DEC.compare(this.getGross()) === -1;
      } else {
        if (OB.DEC.compare(this.getGross()) === -1) {
          isNegative = processedPaymentsAmount >= this.getGross();
        } else {
          isNegative = processedPaymentsAmount > this.getGross();
        }
      }
      if (
        OB.UTIL.isNullOrUndefined(this.get('isNegative')) ||
        this.get('isNegative') !== isNegative
      ) {
        this.set('isNegative', isNegative);
      }

      sumCash = function() {
        if (p.get('kind') === OB.MobileApp.model.get('paymentcash')) {
          // The default cash method
          if (!isNegative || loadedFromBackend) {
            defaultCash = OB.DEC.add(defaultCash, p.get('origAmount'));
          } else {
            defaultCash = OB.DEC.sub(defaultCash, p.get('origAmount'));
          }
          pcash = p;
        } else if (
          OB.MobileApp.model.hasPayment(p.get('kind')) &&
          OB.MobileApp.model.hasPayment(p.get('kind')).paymentMethod.iscash
        ) {
          // Another cash method
          if (!isNegative || loadedFromBackend) {
            nonDefaultCash = OB.DEC.add(nonDefaultCash, p.get('origAmount'));
          } else {
            nonDefaultCash = OB.DEC.sub(nonDefaultCash, p.get('origAmount'));
          }
          pcash = p;
        } else {
          if (!isNegative || loadedFromBackend) {
            noCash = OB.DEC.add(noCash, p.get('origAmount'));
          } else {
            noCash = OB.DEC.sub(noCash, p.get('origAmount'));
          }
        }
      };

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        if (p.get('isPrePayment')) {
          continue;
        }
        setOrigAmount(p);
        // When doing a reverse payment in a negative ticket, the payments introduced to pay again the same quantity
        // must be set to negative (Web POS creates payments in positive by default).
        // This doesn't affect to reversal payments but to the payments introduced to add the quantity reversed
        if (
          isNegative &&
          loadedFromBackend &&
          !p.get('reversedPaymentId') &&
          !p.get('signChanged')
        ) {
          p.set('signChanged', true);
          p.set('amount', -p.get('amount'));
          p.set('origAmount', -p.get('origAmount'));
          p.set('paid', -p.get('paid'));
        }
        if (p.get('isReversePayment')) {
          reversedPaymentsAmount = OB.DEC.add(
            reversedPaymentsAmount,
            p.get('origAmount')
          );
        } else {
          sumCash();
        }
      }

      // Sum the total amount of the payments that cannot generate change or over payment
      notModifiableAmount = OB.DEC.add(
        processedPaymentsAmount,
        reversedPaymentsAmount,
        precision
      );

      totalCash = OB.DEC.add(defaultCash, nonDefaultCash, precision);
      totalPaid = OB.DEC.add(
        notModifiableAmount,
        OB.DEC.add(noCash, totalCash, precision),
        precision
      );

      if (pcash) {
        var payment;
        if (isNegative) {
          if (OB.DEC.add(notModifiableAmount, noCash, precision) < total) {
            payment = OB.DEC.add(notModifiableAmount, noCash, precision);
            pcash.set('paid', OB.DEC.Zero);
            this.set('payment', OB.DEC.abs(payment));
            this.set('paymentWithSign', payment);
            this.set('change', OB.DEC.abs(totalCash));
          } else if (totalPaid < total) {
            pcash.set(
              'paid',
              OB.DEC.sub(
                pcash.get('origAmount'),
                OB.DEC.abs(OB.DEC.sub(total, totalPaid)),
                precision
              )
            );
            this.set('payment', OB.DEC.abs(total));
            this.set('paymentWithSign', total);
            //The change value will be computed through a rounded total value, to ensure that the total plus change
            //add up to the paid amount without any kind of precission loss
            this.set(
              'change',
              OB.DEC.abs(OB.DEC.sub(totalPaid, total, precision))
            );
          } else {
            pcash.set('paid', pcash.get('origAmount'));
            this.set('payment', OB.DEC.abs(totalPaid));
            this.set('paymentWithSign', totalPaid);
            this.set('change', OB.DEC.Zero);
          }
        } else {
          if (OB.DEC.add(notModifiableAmount, noCash, precision) > total) {
            payment = OB.DEC.add(notModifiableAmount, noCash, precision);
            pcash.set('paid', OB.DEC.Zero);
            this.set('payment', OB.DEC.abs(payment));
            this.set('paymentWithSign', payment);
            this.set('change', OB.DEC.abs(totalCash));
          } else if (totalPaid > total) {
            pcash.set(
              'paid',
              OB.DEC.sub(
                pcash.get('origAmount'),
                OB.DEC.abs(OB.DEC.sub(totalPaid, total)),
                precision
              )
            );
            this.set('payment', OB.DEC.abs(total));
            this.set('paymentWithSign', total);
            //The change value will be computed through a rounded total value, to ensure that the total plus change
            //add up to the paid amount without any kind of precission loss
            this.set(
              'change',
              OB.DEC.abs(OB.DEC.sub(totalPaid, total, precision))
            );
          } else {
            pcash.set('paid', pcash.get('origAmount'));
            this.set('payment', OB.DEC.abs(totalPaid));
            this.set('paymentWithSign', totalPaid);
            this.set('change', OB.DEC.Zero);
          }
        }
      } else {
        this.set('payment', OB.DEC.abs(totalPaid));
        this.set('paymentWithSign', totalPaid);
        this.set('change', OB.DEC.Zero);
      }
      if (!OB.UTIL.ProcessController.isProcessActive('calculateReceipt')) {
        this.trigger('updatePending');
      }
    },

    addPayment: function(payment, callback) {
      var execution = OB.UTIL.ProcessController.start('addPayment');
      var me = this,
        payments,
        i,
        max,
        p,
        order,
        paymentSign,
        finalCallback,
        precision;

      if (
        this.get('isPaid') &&
        !payment.get('isReversePayment') &&
        OB.DEC.abs(this.getPrePaymentQty()) >= OB.DEC.abs(this.getTotal()) &&
        !this.isNewReversed()
      ) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotIntroducePayment'));
        OB.UTIL.ProcessController.finish('addPayment', execution);
        return;
      }

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        OB.UTIL.ProcessController.finish('addPayment', execution);
        return;
      }
      if (this.stopAddingPayments) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
        OB.UTIL.ProcessController.finish('addPayment', execution);
        return;
      }
      if (
        !payment.get('isReversePayment') &&
        this.getPending() <= 0 &&
        payment.get('amount') > 0 &&
        !payment.get('forceAddPayment')
      ) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_PaymentsExact'));
        OB.UTIL.ProcessController.finish('addPayment', execution);
        return;
      }

      order = this;
      if (order.get('orderType') === 3 && order.getGross() === 0) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_MsgVoidLayawayPaymentError')
        );
        OB.UTIL.ProcessController.finish('addPayment', execution);
        return;
      }

      finalCallback = function() {
        if (callback instanceof Function) {
          callback(order);
        }
      };

      payments = this.get('payments');
      precision = this.getPrecision(payment);
      payment.set('amount', OB.DEC.toNumber(payment.get('amount'), precision));
      this.addPaymentRounding(
        payment,
        OB.MobileApp.model.paymentnames[payment.get('kind')]
      );

      if (this.get('prepaymentChangeMode')) {
        this.unset('prepaymentChangeMode');
        this.adjustPayment();
      }
      OB.UTIL.PrepaymentUtils.managePrepaymentChange(
        this,
        payment,
        payments,
        function() {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_preAddPayment',
            {
              paymentToAdd: payment,
              payments: payments,
              receipt: me
            },
            function(args) {
              var executeFinalCallback = function(saveChanges) {
                if (saveChanges && !payment.get('changePayment')) {
                  order.adjustPayment();
                  order.trigger('displayTotal');
                }
                OB.UTIL.HookManager.executeHooks(
                  'OBPOS_postAddPayment',
                  {
                    paymentAdded: payment,
                    payments: payments,
                    receipt: order,
                    saveChanges: saveChanges
                  },
                  function(args2) {
                    if (args2.saveChanges && !payment.get('changePayment')) {
                      order.save();
                      order.trigger('saveCurrent');
                    }
                    finalCallback();
                  }
                );
              };

              if (args && args.cancellation) {
                if (payment.get('reverseCallback')) {
                  var reverseCallback = payment.get('reverseCallback');
                  reverseCallback();
                }
                executeFinalCallback(false);
                OB.UTIL.ProcessController.finish('addPayment', execution);
                return;
              }
              // search for an existing payment only if is not a reverser payment.
              if (!payment.get('reversedPaymentId')) {
                if (
                  !payment.get('paymentData') ||
                  payment.get('paymentData').mergeable
                ) {
                  // search for an existing payment only if there is not paymentData info or if there is, when there is any other paymentData with same groupingCriteria.
                  // this avoids to merge for example card payments of different cards.
                  for (i = 0, max = payments.length; i < max; i++) {
                    p = payments.at(i);
                    if (
                      p.get('kind') === payment.get('kind') &&
                      !p.get('isPrePayment') &&
                      !p.get('reversedPaymentId')
                    ) {
                      paymentSign =
                        p.get('signChanged') && p.get('amount') < 0 ? -1 : 1;
                      p.set(
                        'amount',
                        OB.DEC.add(
                          OB.DEC.mul(
                            payment.get('amount'),
                            paymentSign,
                            precision
                          ),
                          p.get('amount'),
                          precision
                        )
                      );
                      if (p.get('rate') && p.get('rate') !== '1') {
                        p.set(
                          'origAmount',
                          OB.DEC.div(p.get('amount'), p.get('mulrate'))
                        );
                      } else {
                        p.set('origAmount', p.get('amount'));
                      }
                      if (payment.has('paymentRoundingLine')) {
                        p.set(
                          'paymentRoundingLine',
                          payment.get('paymentRoundingLine')
                        );
                        p.get('paymentRoundingLine').set(
                          'roundedPaymentId',
                          p.get('id')
                        );
                      }
                      payment.set('date', new Date());
                      executeFinalCallback(true);
                      OB.UTIL.ProcessController.finish('addPayment', execution);
                      return;
                    }
                  }
                } else {
                  for (i = 0, max = payments.length; i < max; i++) {
                    p = payments.at(i);
                    if (
                      p.get('kind') === payment.get('kind') &&
                      p.get('paymentData') &&
                      payment.get('paymentData') &&
                      p.get('paymentData').groupingCriteria &&
                      payment.get('paymentData').groupingCriteria &&
                      p.get('paymentData').groupingCriteria ===
                        payment.get('paymentData').groupingCriteria &&
                      !p.get('reversedPaymentId') &&
                      !p.get('isPrePayment')
                    ) {
                      paymentSign =
                        p.get('signChanged') && p.get('amount') < 0 ? -1 : 1;
                      p.set(
                        'amount',
                        OB.DEC.add(
                          OB.DEC.mul(
                            payment.get('amount'),
                            paymentSign,
                            precision
                          ),
                          p.get('amount'),
                          precision
                        )
                      );
                      if (p.get('rate') && p.get('rate') !== '1') {
                        p.set(
                          'origAmount',
                          OB.DEC.div(p.get('amount'), p.get('mulrate'))
                        );
                      }
                      payment.set('date', new Date());
                      executeFinalCallback(true);
                      OB.UTIL.ProcessController.finish('addPayment', execution);
                      return;
                    }
                  }
                }
              }
              if (
                payment.get('openDrawer') &&
                (payment.get('allowOpenDrawer') || payment.get('isCash'))
              ) {
                order.set('openDrawer', payment.get('openDrawer'));
              }
              payment.set('date', new Date());
              payment.set('id', OB.UTIL.get_UUID());
              payment.set(
                'oBPOSPOSTerminal',
                OB.MobileApp.model.get('terminal').id
              );
              payment.set('orderGross', order.getGross());
              payment.set('isPaid', order.get('isPaid'));
              payment.set('isReturnOrder', order.getPaymentStatus().isNegative);
              if (
                order.get('doCancelAndReplace') &&
                order.get('replacedorder')
              ) {
                // Added properties to payment related with cancel an replace order
                payment.set(
                  'cancelAndReplace',
                  order.get('doCancelAndReplace')
                );
              }

              payments.add(payment, {
                at: payment.get('index')
              });
              // If there is a reversed payment set isReversed properties
              if (payment.get('reversedPayment')) {
                payment.get('reversedPayment').set('isReversed', true);
              }
              if (payment.has('paymentRoundingLine')) {
                payment
                  .get('paymentRoundingLine')
                  .set('roundedPaymentId', payment.get('id'));
              }
              executeFinalCallback(true);
              OB.UTIL.ProcessController.finish('addPayment', execution);
              return;
            }
          ); // call with callback, no args
        }
      );
    },
    //Add a payment rounding line related to the payment if it's needed
    addPaymentRounding: function(payment, terminalPayment) {
      if (
        OB.MobileApp.model.paymentnames[payment.get('kind')].paymentRounding &&
        !payment.get('isReversePayment')
      ) {
        var paymentStatus = this.getPaymentStatus(),
          roundingAmount = OB.DEC.sub(
            paymentStatus.pendingAmt,
            payment.get('amount')
          ),
          terminalPaymentRounding =
            OB.MobileApp.model.paymentnames[
              terminalPayment.paymentRounding.paymentRoundingType
            ],
          amountDifference = null,
          paymentLine = null,
          precision = this.getPrecision(payment),
          multiplyBy = paymentStatus.isReturn
            ? terminalPayment.paymentRounding.returnRoundingMultiple
            : terminalPayment.paymentRounding.saleRoundingMultiple,
          rounding = paymentStatus.isReturn
            ? terminalPayment.paymentRounding.returnRoundingMode
            : terminalPayment.paymentRounding.saleRoundingMode,
          roundingEnabled = paymentStatus.isReturn
            ? terminalPayment.paymentRounding.returnRounding
            : terminalPayment.paymentRounding.saleRounding,
          pow = Math.pow(10, precision),
          paymentDifference =
            OB.DEC.mul(paymentStatus.pendingAmt, pow) %
            OB.DEC.mul(multiplyBy, pow);

        //If receipt total amount is less than equal rounding multiple in Sales/Return
        //no rounding payment line is created
        if (OB.DEC.abs(paymentStatus.totalAmt) <= multiplyBy) {
          return;
        }
        //If receipt is totally paid the last payment paid amount is used to compute the
        //rounding amount
        if (paymentStatus.pendingAmt === 0) {
          paymentDifference =
            OB.DEC.mul(payment.get('paid'), pow) % OB.DEC.mul(multiplyBy, pow);
        }
        //(Rounding enabled for Sales/Returns) &&
        //((the remaining to paid is less than rounding multiple in Sales/Return) ||
        // (the paid amount is less than rounding multiple in Sales/Return with a rounding difference) ||
        // (the payment totally paid the receipt or create an overpayment with a rounding difference))
        if (
          roundingEnabled &&
          ((roundingAmount !== 0 && OB.DEC.abs(roundingAmount) < multiplyBy) ||
            (payment.get('paid') !== 0 &&
              paymentDifference !== 0 &&
              paymentStatus.pendingAmt < multiplyBy) ||
            (payment.get('paid') === 0 &&
              paymentDifference !== 0 &&
              payment.get('amount') >= paymentStatus.pendingAmt))
        ) {
          if (rounding === 'UR') {
            if (paymentDifference !== 0) {
              amountDifference = OB.DEC.sub(
                multiplyBy,
                OB.DEC.div(paymentDifference, pow)
              );
              roundingAmount = OB.DEC.mul(amountDifference, -1);
              if (payment.get('amount') < paymentStatus.pendingAmt) {
                amountDifference = OB.DEC.add(
                  amountDifference,
                  OB.DEC.div(paymentDifference, pow)
                );
              }
            }
            if (payment.get('amount') <= paymentStatus.pendingAmt) {
              payment.set(
                'amount',
                OB.DEC.add(payment.get('amount'), amountDifference)
              );
            }
          } else if (
            paymentDifference !== 0 &&
            payment.get('amount') >= paymentStatus.pendingAmt
          ) {
            roundingAmount = OB.DEC.div(paymentDifference, pow);
            //Substract the rounding amount when the payment totally paid the receipt
            if (payment.get('amount') === paymentStatus.pendingAmt) {
              payment.set(
                'amount',
                OB.DEC.sub(payment.get('amount'), roundingAmount)
              );
            }
          }

          payment.set('index', this.get('payments').length);
          //Create the rounding payment line
          paymentLine = new OB.Model.PaymentLine({
            kind: terminalPayment.paymentRounding.paymentRoundingType,
            name: OB.MobileApp.model.getPaymentName(
              terminalPayment.paymentRounding.paymentRoundingType
            ),
            amount: roundingAmount,
            rate: terminalPaymentRounding.rate,
            mulrate: terminalPaymentRounding.mulrate,
            isocode: terminalPaymentRounding.isocode,
            isCash: terminalPaymentRounding.paymentMethod.iscash,
            allowOpenDrawer:
              terminalPaymentRounding.paymentMethod.allowopendrawer,
            openDrawer: terminalPaymentRounding.paymentMethod.openDrawer,
            printtwice: terminalPaymentRounding.paymentMethod.printtwice,
            date: new Date(),
            id: OB.UTIL.get_UUID(),
            oBPOSPOSTerminal: OB.MobileApp.model.get('terminal').id,
            orderGross: this.getGross(),
            isPaid: false,
            isReturnOrder: paymentStatus.isNegative
          });
          paymentLine.set('paymentRounding', true);
          payment.set('paymentRoundingLine', paymentLine);
          this.get('payments').add(paymentLine);

          if (
            paymentStatus.pendingAmt === 0 ||
            paymentStatus.pendingAmt < multiplyBy
          ) {
            if (payment.has('id')) {
              paymentLine.set('roundedPaymentId', payment.get('id'));
            }
            //When exists a rounding payment line and the receipt total amount change it's necessary
            //to calculate the receipt to properly set the receipt payment status
            this.calculateReceipt();
          }
        }
      }
    },

    overpaymentExists: function() {
      return this.getPaymentStatus().overpayment ? true : false;
    },

    removePayment: function(payment, cancellationCallback, removeCallback) {
      var payments = this.get('payments'),
        max,
        i,
        p,
        me = this;
      if (this.get('isBeingClosed')) {
        var error = new Error();
        OB.error('The receipt is being save, you cannot remove payments.');
        OB.error('The stack trace is: ' + error.stack);
        return;
      }

      if (this.get('prepaymentChangeMode')) {
        this.unset('prepaymentChangeMode');
        this.adjustPayment();
      }
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_preRemovePayment',
        {
          paymentToRem: payment,
          payments: payments,
          receipt: this
        },
        function(args) {
          var reversedPaymentRoundingId;
          if (args.cancellation) {
            if (cancellationCallback) {
              cancellationCallback();
            }
            return true;
          }
          payments.remove(payment);
          if (payment.has('paymentRoundingLine')) {
            payments.remove(payment.get('paymentRoundingLine'));
            reversedPaymentRoundingId = payment
              .get('paymentRoundingLine')
              .get('reversedPaymentId');
          }

          if (!me.get('deletedPayments')) {
            me.set('deletedPayments', [payment]);
          } else {
            me.get('deletedPayments').push(payment);
          }
          // Remove isReversed attribute from payment reversed by removed payment
          if (payment.get('reversedPaymentId')) {
            for (i = 0, max = payments.length; i < max; i++) {
              p = payments.at(i);
              if (
                p.get('paymentId') === payment.get('reversedPaymentId') ||
                (!OB.UTIL.isNullOrUndefined(reversedPaymentRoundingId) &&
                  p.get('paymentId') === reversedPaymentRoundingId)
              ) {
                p.unset('isReversed');
                if (
                  OB.UTIL.isNullOrUndefined(reversedPaymentRoundingId) ||
                  p.get('paymentId') === reversedPaymentRoundingId
                ) {
                  break;
                }
              }
            }
          }
          if (payment.get('openDrawer')) {
            args.receipt.set('openDrawer', false);
          }
          args.receipt.adjustPayment();
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_postRemovePayment',
            {
              receipt: args.receipt,
              payment: payment
            },
            function(args) {
              args.receipt.save(removeCallback);
              args.receipt.trigger('saveCurrent');
            }
          );
        }
      );
    },

    reversePayment: function(payment, sender, reverseCallback) {
      var payments = this.get('payments'),
        me = this,
        usedPayment,
        reversalPayment,
        reversalPaymentLine,
        reversalPaymentRounding,
        reversalPaymentRoundingLine,
        paymentRounding = payment.has('paymentRoundingLine')
          ? payment.get('paymentRoundingLine')
          : null;

      function createReversePayment(originalPayment) {
        var reversePayment = new Backbone.Model();
        OB.UTIL.clone(originalPayment, reversePayment);

        // Remove the cloned properties that must not be in the payment
        reversePayment.unset('date');
        reversePayment.unset('isPaid');
        reversePayment.unset('isPrePayment');
        reversePayment.unset('paymentAmount');
        reversePayment.unset('paymentDate');
        reversePayment.unset('paymentId');
        reversePayment.unset('paymentRoundingLine');

        // Modify other properties for the reverse payment
        reversePayment.set(
          'amount',
          OB.DEC.sub(OB.DEC.Zero, originalPayment.get('amount'))
        );
        reversePayment.set(
          'origAmount',
          OB.DEC.sub(OB.DEC.Zero, originalPayment.get('origAmount'))
        );
        reversePayment.set(
          'paid',
          OB.DEC.sub(OB.DEC.Zero, originalPayment.get('paid'))
        );
        if (originalPayment.has('overpayment')) {
          reversePayment.set(
            'overpayment',
            OB.DEC.sub(OB.DEC.Zero, originalPayment.get('overpayment'))
          );
        }
        reversePayment.set(
          'reversedPaymentId',
          originalPayment.get('paymentId')
        );
        reversePayment.set('reversedPayment', originalPayment);
        reversePayment.set(
          'index',
          OB.DEC.add(
            OB.DEC.One,
            originalPayment.has('paymentRounding') &&
              originalPayment.get('paymentRounding')
              ? payments.indexOf(originalPayment) + OB.DEC.One
              : payments.indexOf(originalPayment)
          )
        );
        reversePayment.set('reverseCallback', reverseCallback);
        reversePayment.set('isReversePayment', true);
        reversePayment.set(
          'paymentData',
          originalPayment.get('paymentData')
            ? originalPayment.get('paymentData')
            : null
        );
        reversePayment.set(
          'oBPOSPOSTerminal',
          originalPayment.get('oBPOSPOSTerminal')
            ? originalPayment.get('oBPOSPOSTerminal')
            : null
        );
        return reversePayment;
      }

      function reversePaymentConfirmed() {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_preReversePayment',
          {
            paymentToReverse: payment,
            payments: payments,
            receipt: me
          },
          function(args) {
            if (args.cancellation) {
              if (reverseCallback) {
                reverseCallback();
              }
              return true;
            }

            reversalPayment = createReversePayment(payment);
            if (!OB.UTIL.isNullOrUndefined(paymentRounding)) {
              reversalPaymentRounding = createReversePayment(paymentRounding);
            }

            OB.UTIL.HookManager.executeHooks(
              'OBPOS_PreAddReversalPayment',
              {
                paymentToReverse: payment,
                reversalPayment: reversalPayment,
                receipt: me
              },
              function(args) {
                if (args.cancelOperation) {
                  if (reverseCallback) {
                    reverseCallback();
                  }
                  return true;
                }
                var provider;
                var firstpayment =
                  OB.MobileApp.model.paymentnames[payment.get('kind')];
                if (
                  firstpayment.providerGroup &&
                  firstpayment.providerGroup.id !== '0'
                ) {
                  // Create a provider group instance that allows to return only with the same payment method.
                  var providerGroup = {
                    provider: firstpayment.providerGroup,
                    _payments: [firstpayment]
                  };
                  OB.MobileApp.view.waterfall('onShowPopup', {
                    popup: 'modalprovidergroup',
                    args: {
                      receipt: me,
                      refund: true,
                      amount: payment.get('amount'),
                      currency: firstpayment.isocode,
                      providerGroup: providerGroup,
                      providername: providerGroup.provider.provider,
                      attributes: {
                        isReversePayment: true,
                        reversedPaymentId: payment.get('paymentId'),
                        reversedPayment: payment,
                        reverseCallback: reverseCallback
                      }
                    }
                  });
                } else {
                  provider =
                    me.getTotal() > 0
                      ? OB.MobileApp.model.paymentnames[payment.get('kind')]
                          .paymentMethod.paymentProvider
                      : OB.MobileApp.model.paymentnames[payment.get('kind')]
                          .paymentMethod.refundProvider;
                  if (provider) {
                    // Remove properties from the payment that ar not needed for a payment provider
                    reversalPayment.unset('kind');
                    // Add new properties for the payment provider
                    reversalPayment.set('receipt', me);
                    reversalPayment.set('provider', provider);
                    reversalPayment.set(
                      'paymentMethod',
                      OB.MobileApp.model.paymentnames[payment.get('kind')]
                        .paymentMethod
                    );

                    OB.MobileApp.view.waterfall('onShowPopup', {
                      popup: 'modalpayment',
                      args: reversalPayment.attributes
                    });
                  } else {
                    reversalPaymentLine = new OB.Model.PaymentLine(
                      reversalPayment.attributes
                    );
                    me.addPayment(reversalPaymentLine);
                    if (!OB.UTIL.isNullOrUndefined(paymentRounding)) {
                      reversalPaymentRoundingLine = new OB.Model.PaymentLine(
                        reversalPaymentRounding.attributes
                      );
                      reversalPaymentLine.set(
                        'paymentRoundingLine',
                        reversalPaymentRoundingLine
                      );
                      me.addPayment(reversalPaymentRoundingLine);
                    }
                  }
                }
              }
            );
          }
        );
      }

      function stopReverse() {
        sender.deleting = false;
        sender.removeClass('btn-icon-loading');
        sender.addClass('btn-icon-reversePayment');
      }

      usedPayment = _.filter(OB.MobileApp.model.get('payments'), function(
        paymentType
      ) {
        return paymentType.payment.searchKey === payment.get('kind');
      });
      if (usedPayment.length === 1) {
        var usedPaymentMethod = usedPayment[0].paymentMethod;
        if (usedPaymentMethod.isreversable) {
          var currentDate = new Date();
          currentDate.setHours(0);
          currentDate.setMinutes(0);
          currentDate.setSeconds(0);
          currentDate.setMilliseconds(0);
          if (
            !OB.UTIL.isNullOrUndefined(
              usedPaymentMethod.availableReverseDelay
            ) &&
            currentDate.getTime() <=
              new Date(payment.get('paymentDate')).getTime() +
                usedPaymentMethod.availableReverseDelay * 86400000
          ) {
            reversePaymentConfirmed();
          } else {
            OB.UTIL.Approval.requestApproval(
              OB.MobileApp.view.$.containerWindow.getRoot().model,
              [
                {
                  approval: 'OBPOS_approval.reversePayment',
                  message: 'OBPOS_approval.reversePayment'
                }
              ],
              function(approved, supervisor, approvalType) {
                if (approved) {
                  reversePaymentConfirmed();
                } else {
                  stopReverse();
                }
              }
            );
          }
        } else {
          stopReverse();
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_NotReversablePaymentHeader'),
            OB.I18N.getLabel('OBPOS_NotReversablePayment')
          );
        }
      } else if (usedPayment.length < 1) {
        stopReverse();
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_NotReversablePayment', [payment.get('name')])
        );
      } else {
        stopReverse();
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_MoreThanOnePaymentMethod', [
            payment.get('name')
          ])
        );
      }
    },

    serializeToJSON: function() {
      // this.toJSON() generates a collection instance for members like "lines"
      // We need a plain array object
      var jsonorder = JSON.parse(JSON.stringify(this.toJSON()));

      // remove not needed members
      delete jsonorder.undo;
      delete jsonorder.json;

      _.forEach(jsonorder.lines, function(item) {
        delete item.product.img;
        delete item.product._filter;
      });

      return jsonorder;
    },

    serializeToSaveJSON: function() {
      // this.toJSON() generates a collection instance for members like "lines"
      // We need a plain array object
      var jsonorder = JSON.parse(JSON.stringify(this.toJSON())),
        jsonOrderLines = jsonorder.lines;

      // remove not needed members
      delete jsonorder.undo;
      delete jsonorder.json;

      var productProps = _.filter(OB.Model.Product.getProperties(), function(
        prop
      ) {
        return !prop.saveToReceipt;
      });

      if (
        !OB.UTIL.isNullOrUndefined(jsonorder.deletedLines) &&
        jsonorder.deletedLines.length > 0
      ) {
        jsonOrderLines = jsonorder.lines.concat(jsonorder.deletedLines);
      }

      _.forEach(jsonOrderLines, function(item) {
        delete item.sortedTaxCollection;
        if (OB.UTIL.isNullOrUndefined(item.product.saveToReceipt)) {
          _.forEach(productProps, function(prop) {
            delete item.product[prop.name];
          });
        }
      });

      return jsonorder;
    },

    changeSignToShowReturns: function() {
      this.set('change', OB.DEC.mul(this.get('change'), -1));
      this.set('gross', OB.DEC.mul(this.get('gross'), -1));
      this.set('net', OB.DEC.mul(this.get('net'), -1));
      this.set('qty', OB.DEC.mul(this.get('qty'), -1));
      //lines
      _.each(
        this.get('lines').models,
        function(line) {
          line.set('gross', OB.DEC.mul(line.get('gross'), -1));
          line.set('qty', OB.DEC.mul(line.get('qty'), -1));
        },
        this
      );

      //payments
      _.each(
        this.get('payments').models,
        function(payment) {
          payment.set('amount', OB.DEC.mul(payment.get('amount'), -1));
          payment.set('origAmount', OB.DEC.mul(payment.get('origAmount'), -1));
        },
        this
      );

      //taxes
      _.each(
        this.get('taxes'),
        function(tax) {
          tax.amount = OB.DEC.mul(tax.amount, -1);
          tax.gross = OB.DEC.mul(tax.gross, -1);
          tax.net = OB.DEC.mul(tax.net, -1);
        },
        this
      );
    },

    setProperty: function(_property, _value) {
      this.set(_property, _value);
      this.save();
    },

    removeNoDiscountAllowLines: function() {
      var linesToRemove = [];
      var me = this;

      this.get('lines').each(function(line) {
        if (line.get('noDiscountAllow')) {
          linesToRemove.push(line);
        }
      });

      if (linesToRemove && linesToRemove.length > 0) {
        _.forEach(linesToRemove, function(lineToRemove) {
          me.get('lines').remove(lineToRemove);
        });
      }
    },

    groupLinesByProduct: function() {
      var lineToMerge,
        lines = this.get('lines'),
        auxLines = lines.models.slice(0),
        localSkipApplyPromotions = this.get('skipApplyPromotions');
      this.set(
        {
          skipApplyPromotions: true
        },
        {
          silent: true
        }
      );
      _.each(auxLines, function(l) {
        lineToMerge = _.find(lines.models, function(line) {
          if (
            l !== line &&
            l.get('product').id === line.get('product').id &&
            l.get('price') === line.get('price') &&
            line.get('qty') > 0 &&
            l.get('qty') > 0 &&
            !_.find(line.get('promotions'), function(promo) {
              return promo.manual;
            }) &&
            !_.find(l.get('promotions'), function(promo) {
              return promo.manual;
            })
          ) {
            return line;
          }
        });
        //When it Comes To Technically , Consider The Product As Non-Grouped When scaled and groupproduct Are Checked
        if (
          lineToMerge &&
          lineToMerge.get('product').get('groupProduct') &&
          !(
            lineToMerge.get('product').get('groupProduct') &&
            lineToMerge.get('product').get('obposScale')
          )
        ) {
          lineToMerge.set(
            {
              qty: lineToMerge.get('qty') + l.get('qty')
            },
            {
              silent: true
            }
          );
          lines.remove(l);
        }
      });
      this.set(
        {
          skipApplyPromotions: localSkipApplyPromotions
        },
        {
          silent: true
        }
      );
    },
    fillPromotionsStandard: function(groupedOrder, isFirstTime) {
      var me = this,
        copiedPromo,
        linesToMerge,
        linesToCreate = [],
        qtyToReduce,
        lineToEdit,
        lineProm,
        linesToReduce,
        linesCreated = false;

      //reset pendingQtyOffer value of each promotion
      groupedOrder.get('lines').forEach(function(l) {
        _.each(l.get('promotions'), function(promo) {
          promo.pendingQtyOffer = promo.qtyOffer;
          if (
            !l.get('product').get('groupProduct') ||
            promo.rule.get('obdiscAllowmultipleinstan')
          ) {
            promo.doNotMerge = true;
          }
          if (
            l.get('product').get('groupProduct') &&
            l.get('product').get('obposScale')
          ) {
            promo.doNotMerge = true;
          }
        });
        //copy lines from virtual ticket to original ticket when they have promotions which avoid us to merge lines
        if (
          _.find(l.get('promotions'), function(promo) {
            return promo.doNotMerge;
          })
        ) {
          //First, try to find lines with the same id
          lineToEdit = _.find(me.get('lines').models, function(line) {
            if (l.get('id') === line.get('id')) {
              return line;
            }
          });
          //Second, try to find lines with the same qty
          if (!lineToEdit) {
            lineToEdit = _.find(me.get('lines').models, function(line) {
              if (
                l !== line &&
                l.get('product').id === line.get('product').id &&
                l.get('price') === line.get('price') &&
                line.get('qty') === l.get('qty') &&
                !_.find(line.get('promotions'), function(promo) {
                  return promo.doNotMerge;
                })
              ) {
                return line;
              }
            });
          }
          //if we cannot find lines with same qty, find lines with qty > 0
          if (!lineToEdit) {
            lineToEdit = _.find(me.get('lines').models, function(line) {
              if (
                l !== line &&
                l.get('product').id === line.get('product').id &&
                l.get('price') === line.get('price') &&
                line.get('qty') > 0
              ) {
                return line;
              }
            });
          }
          if (OB.UTIL.isNullOrUndefined(lineToEdit)) {
            return;
          }
          lineToEdit.set(
            'noDiscountCandidates',
            l.get('noDiscountCandidates'),
            {
              silent: true
            }
          );

          //if promotion affects only to few quantities of the line, create a new line with quantities not affected by the promotion
          if (lineToEdit.get('qty') > l.get('qty')) {
            linesToCreate.push({
              product: lineToEdit.get('product'),
              qty: l.get('qty'),
              attrs: {
                promotions: l.get('promotions'),
                promotionCandidates: l.get('promotionCandidates'),
                qtyToApplyDiscount: l.get('qtyToApplyDiscount'),
                price: l.get('price')
              }
            });
            lineToEdit.set(
              'qty',
              OB.DEC.sub(lineToEdit.get('qty'), l.get('qty')),
              {
                silent: true
              }
            );
            me.mergeLines(lineToEdit);
            //if promotion affects to several lines, edit first line with the promotion info and then remove the affected lines
          } else if (lineToEdit.get('qty') < l.get('qty')) {
            qtyToReduce = OB.DEC.sub(l.get('qty'), lineToEdit.get('qty'));
            linesToReduce = _.filter(me.get('lines').models, function(line) {
              if (
                l !== line &&
                l.get('product').id === line.get('product').id &&
                l.get('price') === line.get('price') &&
                line.get('qty') > 0 &&
                !_.find(line.get('promotions'), function(promo) {
                  return promo.manual || promo.doNotMerge;
                })
              ) {
                return line;
              }
            });
            lineProm = linesToReduce.shift();
            lineProm.set('qty', l.get('qty'));
            lineProm.set('promotions', l.get('promotions'));
            lineProm.set('promotionCandidates', l.get('promotionCandidates'));
            lineProm.set('qtyToApplyDiscount', l.get('qtyToApplyDiscount'));
            lineProm.trigger('change');
            _.each(linesToReduce, function(line) {
              if (line.get('qty') > qtyToReduce) {
                line.set(
                  {
                    qty: line.get('qty') - qtyToReduce
                  },
                  {
                    silent: true
                  }
                );
                qtyToReduce = OB.DEC.Zero;
              } else if (line.get('qty') === qtyToReduce) {
                me.get('lines').remove(line);
                qtyToReduce = OB.DEC.Zero;
              } else {
                qtyToReduce = qtyToReduce - line.get('qty');
                me.get('lines').remove(line);
              }
            });
            //when qty of the promotion is equal to the line qty, we copy line info.
          } else {
            lineToEdit.set('qty', l.get('qty'));
            lineToEdit.set('promotions', l.get('promotions'));
            lineToEdit.set('promotionCandidates', l.get('promotionCandidates'));
            lineToEdit.set('qtyToApplyDiscount', l.get('qtyToApplyDiscount'));
            lineToEdit.trigger('change');
          }
        } else {
          //Filter lines which can be merged
          linesToMerge = _.filter(me.get('lines').models, function(line) {
            var qtyReserved = 0;
            var promotions = line.get('promotions') || [];
            if (promotions.length > 0) {
              promotions.forEach(function(p) {
                qtyReserved = OB.DEC.add(qtyReserved, p.qtyOfferReserved || 0);
              });
            }
            if (
              l !== line &&
              l.get('product').id === line.get('product').id &&
              l.get('price') === line.get('price') &&
              OB.UTIL.Math.sign(line.get('qty')) ===
                OB.UTIL.Math.sign(l.get('qty'))
            ) {
              if (OB.DEC.sub(Math.abs(line.get('qty')), qtyReserved) > 0) {
                var isManualAdded = false;
                var isManualOrNotMerge = _.find(
                  line.get('promotions'),
                  function(promo) {
                    //Verify if manual promotions was added.
                    _.each(l.get('promotions'), function(p) {
                      if (p.ruleId === promo.ruleId) {
                        isManualAdded = true;
                      }
                    });
                    isManualAdded = !isManualAdded ? promo.manual : false;
                    return isManualAdded || promo.doNotMerge;
                  }
                );
                if (!isManualOrNotMerge) {
                  return line;
                }
              }
            }
          });
          // sort by qty asc to fix issue 28120
          // firstly the discount is applied to the lines with minus quantity, so the discount is applied to all quantity of the line
          // and if it is needed (promotion.qty > line.qty) the rest of promotion will be applied to the other line
          linesToMerge = _.sortBy(linesToMerge, function(lsb) {
            lsb.getQty();
          });
          if (linesToMerge.length > 0) {
            _.each(linesToMerge, function(line) {
              line.set(
                {
                  promotionCandidates: l.get('promotionCandidates'),
                  promotionMessages: me.showMessagesPromotions(
                    line.get('promotionMessages'),
                    l.get('promotionMessages')
                  ),
                  qtyToApplyDiscount: l.get('qtyToApplyDiscount'),
                  noDiscountCandidates: l.get('noDiscountCandidates')
                },
                {
                  silent: true
                }
              );
              _.each(l.get('promotions'), function(promo) {
                copiedPromo = JSON.parse(JSON.stringify(promo));
                //when ditributing the promotion between different lines, we save accumulated amount
                promo.distributedAmt = promo.distributedAmt
                  ? promo.distributedAmt
                  : OB.DEC.Zero;
                //pendingQtyOffer is the qty of the promotion which need to be apply (we decrease this qty in each loop)
                promo.pendingQtyOffer = !_.isUndefined(promo.pendingQtyOffer)
                  ? promo.pendingQtyOffer
                  : promo.qtyOffer;
                if (
                  promo.pendingQtyOffer &&
                  promo.pendingQtyOffer >= line.get('qty')
                ) {
                  //if _.isUndefined(promo.actualAmt) is true we do not distribute the discount
                  if (_.isUndefined(promo.actualAmt)) {
                    if (promo.pendingQtyOffer !== promo.qtyOffer) {
                      copiedPromo.hidden = true;
                      copiedPromo.amt = OB.DEC.Zero;
                    }
                  } else {
                    copiedPromo.actualAmt =
                      (promo.fullAmt / promo.qtyOffer) * line.get('qty');
                    copiedPromo.amt =
                      (promo.fullAmt / promo.qtyOffer) * line.get('qty');
                    copiedPromo.obdiscQtyoffer = line.get('qty');
                    promo.distributedAmt = OB.DEC.add(
                      promo.distributedAmt,
                      OB.DEC.toNumber(
                        OB.DEC.toBigDecimal(
                          (promo.fullAmt / promo.qtyOffer) * line.get('qty')
                        )
                      )
                    );
                  }

                  if (promo.pendingQtyOffer === line.get('qty')) {
                    if (
                      !_.isUndefined(promo.actualAmt) &&
                      promo.actualAmt &&
                      promo.actualAmt !== promo.distributedAmt
                    ) {
                      copiedPromo.actualAmt = OB.DEC.add(
                        copiedPromo.actualAmt,
                        OB.DEC.sub(promo.actualAmt, promo.distributedAmt)
                      );
                      copiedPromo.amt = promo.amt
                        ? OB.DEC.add(
                            copiedPromo.amt,
                            OB.DEC.sub(promo.amt, promo.distributedAmt)
                          )
                        : promo.amt;
                    }
                    promo.pendingQtyOffer = null;
                  } else {
                    promo.pendingQtyOffer =
                      promo.pendingQtyOffer - line.get('qty');
                  }
                  if (line.get('promotions')) {
                    line.get('promotions').push(copiedPromo);
                  } else {
                    line.set('promotions', [copiedPromo]);
                  }
                } else if (!OB.UTIL.isNullOrUndefined(promo.pendingQtyOffer)) {
                  if (_.isUndefined(promo.actualAmt)) {
                    if (promo.pendingQtyOffer !== promo.qtyOffer) {
                      copiedPromo.hidden = true;
                      copiedPromo.amt = OB.DEC.Zero;
                    }
                  } else {
                    copiedPromo.actualAmt =
                      (promo.fullAmt / promo.qtyOffer) * promo.pendingQtyOffer;
                    copiedPromo.amt =
                      (promo.fullAmt / promo.qtyOffer) * promo.pendingQtyOffer;
                    copiedPromo.obdiscQtyoffer = promo.pendingQtyOffer;
                    promo.distributedAmt = OB.DEC.add(
                      promo.distributedAmt,
                      OB.DEC.toNumber(
                        OB.DEC.toBigDecimal(
                          (promo.fullAmt / promo.qtyOffer) *
                            promo.pendingQtyOffer
                        )
                      )
                    );
                  }
                  if (
                    !_.isUndefined(promo.actualAmt) &&
                    promo.actualAmt &&
                    promo.actualAmt !== promo.distributedAmt
                  ) {
                    copiedPromo.actualAmt = OB.DEC.add(
                      copiedPromo.actualAmt,
                      OB.DEC.sub(promo.actualAmt, promo.distributedAmt)
                    );
                    copiedPromo.amt = promo.amt
                      ? OB.DEC.add(
                          copiedPromo.amt,
                          OB.DEC.sub(promo.amt, promo.distributedAmt)
                        )
                      : promo.amt;
                  }

                  if (line.get('promotions')) {
                    line.get('promotions').push(copiedPromo);
                  } else {
                    line.set('promotions', [copiedPromo]);
                  }
                  promo.pendingQtyOffer = null;
                  //if it is the first we enter in this method, promotions which are not in the virtual ticket are deleted.
                }
              });
              line.trigger('change');
            });
          }
        }
      });
      _.each(me.get('lines').models, function(line) {
        var orderPromotions = false;
        var masterKey = 0;
        var position;
        var groupProm = {};
        var prom = line.get('promotions');
        var validProm = _.filter(prom, function(p) {
          return !p.hidden;
        });
        // Group multipromotions with the same instanceid
        var multiProm = _.filter(validProm, function(p) {
          return p.discountinstance;
        });
        var groupInstanceProm = _.groupBy(multiProm, function(p) {
          return p.discountinstance;
        });
        var groupMultiProm = {};
        _.each(groupInstanceProm, function(p, key) {
          groupMultiProm[masterKey] = p;
          masterKey++;
        });
        // Group singlepromotions with the same ruleid
        var singleProm = _.filter(validProm, function(p) {
          return !p.discountinstance;
        });
        var groupRuleIdProm = _.groupBy(singleProm, function(p) {
          return p.ruleId;
        });
        var groupSingleProm = {};
        _.each(groupRuleIdProm, function(p, key) {
          groupSingleProm[masterKey] = p;
          masterKey++;
        });
        // Merge multipromotion and singlepromotion arrays
        _.extend(groupProm, groupMultiProm, groupSingleProm);
        for (position = 0; position < _.keys(groupProm).length; position++) {
          var i;
          var key = _.keys(groupProm)[position];
          var promList = groupProm[key];
          if (promList && promList.length > 1) {
            orderPromotions = true;
            var finalAmt = 0;
            var finalQtyOffer = 0;
            copiedPromo = JSON.parse(JSON.stringify(promList[0]));
            if (!copiedPromo.manual) {
              for (i = 0; i < promList.length; i++) {
                finalAmt += promList[i].amt;
                finalQtyOffer += promList[i].qtyOffer;
              }
              if (finalQtyOffer <= line.get('qty')) {
                copiedPromo.amt = finalAmt;
                copiedPromo.qtyOffer = finalQtyOffer;
                copiedPromo.chunks = promList.length;
              }
            }
            for (i = 0; i < line.get('promotions').length; i++) {
              if (
                line.get('promotions')[i].ruleId === promList[0].ruleId &&
                line.get('promotions')[i].discountinstance ===
                  promList[0].discountinstance
              ) {
                break;
              }
            }
            me.removePromotion(line, {
              id: promList[0].ruleId,
              discountinstance: promList[0].discountinstance
            });
            line.get('promotions').splice(i, 0, copiedPromo);
          }
          if (orderPromotions) {
            var lineNoNormalized = 10;
            var promos = line.get('promotions');
            for (i = 0; i < promos.length; i++) {
              promos[i].lineNo = lineNoNormalized;
              lineNoNormalized += 10;
            }
          }
        }
      });

      if (!linesCreated) {
        _.each(linesToCreate, function(line) {
          me.createLine(line.product, line.qty, null, line.attrs);
        });
        linesCreated = true;
      }
    },

    fillPromotionsSplitted: function(groupedOrder, isFirstTime) {
      var receipt = this;
      // Receipt with split lines
      _.forEach(groupedOrder.get('lines').models, function(gli, index) {
        if (gli.get('promotions') && gli.get('promotions').length > 0) {
          var linesToApply = new Backbone.Collection();
          _.forEach(receipt.get('lines').models, function(rli) {
            if (gli.get('product').get('groupProduct')) {
              if (
                gli.get('product').id === rli.get('product').id &&
                gli.get('price') === rli.get('price')
              ) {
                if (rli.get('promotions') && rli.get('promotions').length > 0) {
                  var samePromos = [];
                  var qtyOffer = 0;
                  _.forEach(rli.get('promotions'), function(promot) {
                    if (!promot.applyNext) {
                      samePromos.push(promot);
                    }
                  });
                  if (samePromos && samePromos.length > 0) {
                    _.forEach(samePromos, function(samePromo) {
                      qtyOffer += samePromo.qtyOffer;
                    });
                    if (rli.get('qty') - qtyOffer === 0) {
                      return;
                    } else if (rli.get('qty') - qtyOffer > 0) {
                      rli.set('lineQtyOffer', rli.get('qty') - qtyOffer);
                      linesToApply.add(rli);
                    }
                  }
                } else {
                  if (
                    !gli.get('singleManualPromotionApplied') ||
                    (gli.get('id') === rli.get('id') &&
                      gli.get('singleManualPromotionApplied'))
                  ) {
                    linesToApply.add(rli);
                  }
                }
              }
            } else {
              if (gli.get('id') === rli.get('id')) {
                linesToApply.add(rli);
              }
            }
          });

          var groupedPromos = gli.get('promotions');
          _.forEach(groupedPromos, function(promotion) {
            if (!promotion.manual) {
              var promoAmt = 0,
                promotionQtyOffer =
                  promotion.lineQtyOffer || promotion.qtyOffer,
                promoQtyoffer = promotionQtyOffer;

              _.forEach(linesToApply.models, function(line) {
                var samePromos = [];
                var qtyOffer = 0;
                var qtyToCheck = line.get('qty');
                _.forEach(line.get('promotions'), function(promot) {
                  if (
                    promot.hidden !== promotion.hidden &&
                    promot.discountType === promotion.discountType
                  ) {
                    samePromos.push(promot);
                  }
                });
                if (samePromos && samePromos.length > 0) {
                  _.forEach(samePromos, function(samePromo) {
                    qtyOffer += samePromo.qtyOffer;
                  });
                  if (line.get('qty') - qtyOffer === 0) {
                    return;
                  } else if (line.get('qty') - qtyOffer > 0) {
                    qtyToCheck = line.get('qty') - qtyOffer;
                  }
                }

                var clonedPromotion = JSON.parse(JSON.stringify(promotion));
                if (promoQtyoffer > 0) {
                  clonedPromotion.obdiscQtyoffer =
                    qtyToCheck - promoQtyoffer >= 0
                      ? promoQtyoffer
                      : qtyToCheck;
                  if (!promotion.hidden) {
                    clonedPromotion.amt = OB.DEC.toNumber(
                      OB.DEC.toBigDecimal(
                        promotion.amt *
                          (clonedPromotion.obdiscQtyoffer / promotionQtyOffer)
                      )
                    );
                    clonedPromotion.fullAmt = OB.DEC.toNumber(
                      OB.DEC.toBigDecimal(clonedPromotion.amt)
                    );
                    clonedPromotion.displayedTotalAmount = OB.DEC.toNumber(
                      OB.DEC.toBigDecimal(
                        (promotion.displayedTotalAmount || 0) *
                          (clonedPromotion.obdiscQtyoffer / promotionQtyOffer)
                      )
                    );
                  } else {
                    clonedPromotion.amt = 0;
                    clonedPromotion.fullAmt = 0;
                    clonedPromotion.displayedTotalAmount = 0;
                  }
                  clonedPromotion.pendingQtyoffer =
                    line.get('qty') - clonedPromotion.obdiscQtyoffer;
                  clonedPromotion.qtyOffer = clonedPromotion.obdiscQtyoffer;
                  clonedPromotion.qtyOfferReserved =
                    clonedPromotion.obdiscQtyoffer;
                  clonedPromotion.lineQtyOffer = clonedPromotion.obdiscQtyoffer;
                  clonedPromotion.doNotMerge = true;
                  if (!line.get('promotions')) {
                    line.set('promotions', []);
                  }
                  line.get('promotions').push(clonedPromotion);
                  line.trigger('change');
                  promoQtyoffer -= clonedPromotion.obdiscQtyoffer;
                  promoAmt += clonedPromotion.amt;
                } else if (promoQtyoffer < 0) {
                  OB.error(
                    'There is more units consumed than the original promotion'
                  );
                }
              });

              // Check the amount discount is the same
              if (promotion.amt !== promoAmt && !promotion.hidden) {
                // Adjust splitted promotion amount
                var splittedAmount = _.reduce(
                  linesToApply.models,
                  function(sum, line) {
                    var linePromo = _.find(line.get('promotions'), function(
                      lp
                    ) {
                      return (
                        lp.ruleId === promotion.ruleId &&
                        lp.discountType === promotion.discountType &&
                        !lp.hidden
                      );
                    });
                    if (linePromo) {
                      return (
                        sum +
                        OB.DEC.toNumber(OB.DEC.toBigDecimal(linePromo.amt))
                      );
                    }
                    return sum;
                  },
                  0
                );
                var bdSplittedAmount = OB.DEC.toBigDecimal(splittedAmount),
                  bdPromoAmount = OB.DEC.toBigDecimal(promotion.amt);
                if (bdPromoAmount.compareTo(bdSplittedAmount) !== 0) {
                  var linePromo = _.find(
                    linesToApply.map(function(lta) {
                      return lta.get('promotions').find(function(lp) {
                        return (
                          lp.discountType === promotion.discountType &&
                          !lp.hidden
                        );
                      });
                    }),
                    function(ltapromo) {
                      return ltapromo;
                    }
                  );
                  if (linePromo) {
                    var amount = OB.DEC.toNumber(
                      bdPromoAmount
                        .subtract(bdSplittedAmount)
                        .add(OB.DEC.toBigDecimal(linePromo.amt))
                    );
                    linePromo.amt = amount;
                    linePromo.displayedTotalAmount = amount;
                    linePromo.fullAmt = amount;
                  }
                }
              }
            } else {
              var appliedPromotion = false;
              _.forEach(linesToApply.models, function(l) {
                if (!appliedPromotion) {
                  if (l.get('qty') === gli.get('qty')) {
                    if (
                      _.find(l.get('promotions'), function(promo) {
                        return promo.discountType === promotion.discountType;
                      }) === undefined
                    ) {
                      l.get('promotions').push(promotion);
                      l.trigger('change');
                      appliedPromotion = true;
                    }
                  }
                }
              });
            }
          });
        }
      });
    },

    fillPromotionsWith: function(groupedOrder, isFirstTime) {
      var countSplited = _.reduce(
        this.get('lines').models,
        function(count, line) {
          return count + (line.get('splitline') ? 1 : 0);
        },
        0
      );
      var localSkipApplyPromotions = this.get('skipApplyPromotions');
      this.set(
        {
          skipApplyPromotions: true
        },
        {
          silent: true
        }
      );
      if (countSplited > 1) {
        this.fillPromotionsSplitted(groupedOrder, isFirstTime);
      } else {
        this.fillPromotionsStandard(groupedOrder, isFirstTime);
      }
      this.set(
        {
          skipApplyPromotions: localSkipApplyPromotions
        },
        {
          silent: true
        }
      );
      this.trigger('promotionsUpdated');
    },

    // for each line, decrease the qtyOffer of promotions and remove the lines with qty 0
    removeQtyOffer: function() {
      var linesPending = new Backbone.Collection();
      this.get('lines').forEach(function(l) {
        var promotionsApplyNext = [],
          promotionsCascadeApplied = [],
          qtyReserved = 0,
          qtyPending;
        if (l.get('promotions')) {
          promotionsApplyNext = [];
          promotionsCascadeApplied = [];
          l.get('promotions').forEach(function(p) {
            if (p.qtyOfferReserved > 0) {
              qtyReserved = OB.DEC.add(qtyReserved, p.qtyOfferReserved);
            }
            // if it is a promotions with applyNext, the line is related to the promotion, so, when applyPromotions is called again,
            // if the promotion is similar to this promotion, then no changes have been done, then stop
            if (p.applyNext) {
              promotionsApplyNext.push(p);
              promotionsCascadeApplied.push(p);
            }
          });
        }
        qtyPending = OB.DEC.sub(l.get('qty'), qtyReserved);
        l.set('qty', qtyPending);
        l.set('promotions', promotionsApplyNext);
        l.set('promotionsCascadeApplied', promotionsCascadeApplied);
      });

      _.each(this.get('lines').models, function(line) {
        if (line.get('qty') > 0) {
          linesPending.add(line);
        }
      });
      this.get('lines').reset(linesPending.models);
    },

    removeLinesWithoutPromotions: function() {
      var linesPending = new Backbone.Collection();
      _.each(this.get('lines').models, function(l) {
        if (l.get('promotions') && l.get('promotions').length > 0) {
          linesPending.push(l);
        }
      });
      this.set('lines', linesPending);
    },

    hasPromotions: function() {
      var hasPromotions = false;
      this.get('lines').forEach(function(l) {
        if (l.get('promotions') && l.get('promotions').length > 0) {
          hasPromotions = true;
        }
      });
      return hasPromotions;
    },

    isSimilarLine: function(line1, line2) {
      var equalPromotions = function(x, y) {
        var isEqual = true;
        if (x.length !== y.length) {
          isEqual = false;
        } else {
          x.forEach(function(p1, ind) {
            if (
              p1.amt !== y[ind].amt ||
              p1.displayedTotalAmount !== y[ind].displayedTotalAmount ||
              p1.qtyOffer !== y[ind].qtyOffer ||
              p1.qtyOfferReserved !== y[ind].qtyOfferReserved ||
              p1.ruleId !== y[ind].ruleId ||
              p1.obdiscQtyoffer !== y[ind].obdiscQtyoffer
            ) {
              isEqual = false;
            }
          });
        }
        return isEqual;
      };
      if (
        line1.get('product').get('id') === line2.get('product').get('id') &&
        line1.get('price') === line2.get('price') &&
        line1.get('discountedLinePrice') === line2.get('discountedLinePrice') &&
        line1.get('qty') === line2.get('qty')
      ) {
        return equalPromotions(
          line1.get('promotions') || [],
          line2.get('promotions') || []
        );
      } else {
        return false;
      }
    },
    // if there is a promtion of type "applyNext" that it has been applied previously in the line, then It is replaced
    // by the first promotion applied. Ex:
    // Ex: prod1 - qty 5 - disc3x2 & discPriceAdj -> priceAdj is applied first to 5 units
    //     it is called to applyPromotions, with the 2 units frees, and priceAdj is applied again to this 2 units
    // it is wrong, only to 5 units should be applied priceAdj, no 5 + 2 units
    removePromotionsCascadeApplied: function() {
      this.get('lines').forEach(function(l) {
        if (
          !OB.UTIL.isNullOrUndefined(l.get('promotions')) &&
          l.get('promotions').length > 0 &&
          !OB.UTIL.isNullOrUndefined(l.get('promotionsCascadeApplied')) &&
          l.get('promotionsCascadeApplied').length > 0
        ) {
          l.get('promotions').forEach(function(p, ind) {
            l.get('promotionsCascadeApplied').forEach(function(pc) {
              if (
                p.ruleId === pc.ruleId &&
                p.discountinstance === pc.discountinstance
              ) {
                l.get('promotions')[ind] = pc;
              }
            });
          });
        }
      });
    },

    showMessagesPromotions: function(arrayMessages1, arrayMessages2) {
      arrayMessages1 = arrayMessages1 || [];
      (arrayMessages2 || []).forEach(function(m2) {
        if (
          _.filter(arrayMessages1, function(m1) {
            return m1 === m2;
          }).length === 0
        ) {
          arrayMessages1.push(m2);
          OB.UTIL.showAlert.display(m2);
        }
      });
      return arrayMessages1;
    },

    getOrderDescription: function() {
      var desc =
        "{id: '" +
        this.get('id') +
        "', Docno: '" +
        this.get('documentNo') +
        "', Total gross: '" +
        this.get('gross') +
        "', Lines: ['";
      var i = 0;
      var propt;
      this.get('lines').forEach(function(l) {
        if (i !== 0) {
          desc += ',';
        }
        desc +=
          "'{Product: '" +
          l.get('product').get('_identifier') +
          "', Quantity: '" +
          l.get('qty') +
          "', Gross: '" +
          l.get('gross') +
          "', LineGrossAmount: '" +
          l.get('lineGrossAmount') +
          "', DiscountedGross: '" +
          l.get('discountedGross') +
          "', Net: '" +
          l.get('net') +
          "', DiscountedNet: '" +
          l.get('discountedNet') +
          "', NonDiscountedNet: '" +
          l.get('nondiscountednet') +
          "', TaxAmount: '" +
          l.get('taxAmount') +
          "', GrossUnitPrice: '" +
          l.get('grossUnitPrice') +
          "'}";
        i++;
      });
      desc += '], Payments: [';
      i = 0;
      this.get('payments').forEach(function(l) {
        if (i !== 0) {
          desc += ',';
        }
        desc +=
          "{PaymentMethod: '" +
          l.get('kind') +
          "', Amount: '" +
          l.get('amount') +
          "', OrigAmount: '" +
          l.get('origAmount') +
          "', Date: '" +
          l.get('date') +
          "', isocode: '" +
          l.get('isocode') +
          "'}";
        i++;
      });
      desc += '], Taxes: [';
      i = 0;
      for (propt in this.get('taxes')) {
        if (this.get('taxes').hasOwnProperty(propt)) {
          var obj = this.get('taxes')[propt];
          if (i !== 0) {
            desc += ',';
          }
          desc +=
            "{TaxId: '" +
            propt +
            "', TaxRate: '" +
            obj.rate +
            "', TaxNet: '" +
            obj.net +
            "', TaxAmount: '" +
            obj.amount +
            "', TaxName: '" +
            obj.name +
            "'}";
          i++;
        }
      }
      desc += ']';
      desc += '}';
      return desc;
    },

    canAddAsServices: function(model, product, callback, scope) {
      if (product.get('productType') === 'S') {
        // do not allow to add not linked services to non editable orders
        if (
          product.get('isLinkedToProduct') === false &&
          model.get('order').get('isEditable') === false
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_modalNoEditableHeader'),
            OB.I18N.getLabel('OBPOS_modalNoEditableBody'),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk')
              }
            ]
          );
          callback.call(scope, 'NOT_ALLOW');
          return;
        }
        if (
          !OB.UTIL.isNullOrUndefined(product.get('allowDeferredSell')) &&
          product.get('allowDeferredSell')
        ) {
          if (
            model.get('order') &&
            model.get('order').get('isQuotation') &&
            model.get('order').get('isEditable') === false
          ) {
            // Not allow deferred sell in quotation under evaluation
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_modalNoEditableHeader'),
              OB.I18N.getLabel('OBPOS_modalNoEditableBody'),
              [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk')
                }
              ]
            );
            callback.call(scope, 'NOT_ALLOW');
          } else if (
            !OB.UTIL.isNullOrUndefined(product.get('deferredSellMaxDays'))
          ) {
            var oneDay = 24 * 60 * 60 * 1000,
              today = new Date(),
              orderDate = new Date(this.get('orderDate'));
            today.setHours(0, 0, 0, 0);
            orderDate.setHours(0, 0, 0, 0);
            var diffDays = Math.round(
              OB.DEC.abs(today.getTime() - orderDate.getTime()) / oneDay
            );
            if (diffDays > product.get('deferredSellMaxDays')) {
              // Need approval exceeds max days
              OB.UTIL.Approval.requestApproval(
                model,
                [
                  {
                    approval: 'OBPOS_approval.deferred_sell_max_days',
                    message: 'OBPOS_approval.deferred_sell_max_days',
                    params: [product.get('deferredSellMaxDays')]
                  }
                ],
                function(approved, supervisor, approvalType) {
                  callback.call(scope, approved ? 'OK' : 'NOT_ALLOW_MAX_DAYS');
                }
              );
            } else {
              callback.call(scope, 'OK');
            }
          } else {
            callback.call(scope, 'OK');
          }
        } else {
          // Not allow deferred sell
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_msgDeferredSellCaption'),
            OB.I18N.getLabel('OBPOS_msgNotDeferredSell'),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk')
              }
            ]
          );
          callback.call(scope, 'NOT_ALLOW');
        }
      } else {
        // Not is a service
        callback.call(scope, 'ABORT');
      }
    },
    getOrderlLineIndex: function(orderlineId) {
      var index = 0;
      this.get('lines').forEach(function(line, indx) {
        if (line.id === orderlineId) {
          index = indx;
        }
      });
      return index;
    },
    deleteOrder: function(context, callback) {
      var i;

      function removePayments(receipt, callback) {
        var payments = receipt.get('payments');
        if (receipt.get('isEditable') && payments && payments.length > 0) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_preRemovePayment',
            {
              paymentToRem: payments.at(0),
              payments: payments,
              receipt: receipt
            },
            function(args) {
              if (args.cancellation) {
                callback(false);
              } else {
                payments.remove(payments.at(0));
                removePayments(receipt, function(success) {
                  callback(success);
                });
              }
            }
          );
        } else {
          callback(true);
        }
      }

      function removeReceiptFromDatabase(receipt, callback) {
        var model,
          orderList = OB.MobileApp.model.orderList;
        if (
          orderList &&
          receipt &&
          receipt.get('session') === OB.MobileApp.model.get('session')
        ) {
          model = _.find(orderList.models, function(model) {
            return model.get('id') === receipt.get('id');
          });
          if (model) {
            orderList.saveCurrent();
            orderList.load(model);
            if (model.get('id')) {
              OB.Dal.remove(model);
            }
          }
          orderList.deleteCurrent();
        } else if (receipt && receipt.get('id')) {
          OB.Dal.remove(receipt);
        }
        if (callback && callback instanceof Function) {
          callback();
        }
      }

      function markOrderAsDeleted(model, orderList, callback) {
        var creationDate;
        if (model.get('creationDate')) {
          creationDate = new Date(model.get('creationDate'));
        } else {
          creationDate = new Date();
        }
        model.setIsCalculateGrossLockState(true);
        model.set('creationDate', creationDate);
        model.set('timezoneOffset', creationDate.getTimezoneOffset());
        model.set('created', creationDate.getTime());
        model.set('obposCreatedabsolute', OB.I18N.formatDateISO(creationDate));
        model.set('obposIsDeleted', true);
        OB.info(
          'markOrderAsDeleted has set order with documentNo ' +
            model.get('documentNo') +
            ' and id ' +
            model.get('id') +
            ' as obposIsDeleted to true'
        );
        model.set(
          'obposAppCashup',
          OB.MobileApp.model.get('terminal').cashUpId
        );
        for (i = 0; i < model.get('lines').length; i++) {
          model
            .get('lines')
            .at(i)
            .set('obposIsDeleted', true);
          model
            .get('lines')
            .at(i)
            .set('listPrice', 0);
          model
            .get('lines')
            .at(i)
            .set('standardPrice', 0);
          model
            .get('lines')
            .at(i)
            .set('grossUnitPrice', 0);
          model
            .get('lines')
            .at(i)
            .set('lineGrossAmount', 0);
        }
        model.get('approvals').forEach(function(approval) {
          if (typeof approval.approvalType === 'object') {
            approval.approvalMessage = OB.I18N.getLabel(
              approval.approvalType.message,
              approval.approvalType.params
            );
            approval.approvalType = approval.approvalType.approval;
          }
        });
        OB.Dal.transaction(function(tx) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PreSyncReceipt',
            {
              receipt: model,
              model: model,
              tx: tx
            },
            function(args) {
              model.set('json', JSON.stringify(model.serializeToSaveJSON()));
              model.set('hasbeenpaid', 'Y');
              OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(
                model.get('documentnoSuffix'),
                model.get('quotationnoSuffix'),
                model.get('returnnoSuffix'),
                function() {
                  OB.Dal.saveInTransaction(tx, model, function() {
                    if (
                      orderList &&
                      model.get('session') === OB.MobileApp.model.get('session')
                    ) {
                      var orderListModel = _.find(orderList.models, function(
                        m
                      ) {
                        return m.get('id') === model.get('id');
                      });
                      if (orderListModel) {
                        orderList.saveCurrent();
                        orderList.load(orderListModel);
                      }
                      orderList.deleteCurrent();
                      orderList.synchronizeCurrentOrder();
                    }
                    model.setIsCalculateGrossLockState(false);
                    if (callback && callback instanceof Function) {
                      callback();
                    }
                  });
                },
                tx
              );
            }
          );
        });
      }

      function removeOrder(receipt, callback) {
        var orderList = OB.MobileApp.model.orderList;
        var isPaidQuotation =
          receipt.has('isQuotation') &&
          receipt.get('isQuotation') &&
          receipt.has('hasbeenpaid') &&
          receipt.get('hasbeenpaid') === 'Y';
        if (OB.UTIL.RfidController.isRfidConfigured()) {
          OB.UTIL.RfidController.eraseEpcOrder(receipt);
        }

        function finishRemoveOrder() {
          if (
            receipt.get('lines') &&
            receipt.get('lines').length > 0 &&
            receipt.get('isEditable')
          ) {
            if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
              receipt.prepareToSend(function() {
                receipt.set('skipApplyPromotions', true);
                receipt.set('skipCalculateReceipt', true);
                receipt.set('preventServicesUpdate', true);
                _.each(receipt.get('lines').models, function(line) {
                  line.set('obposQtyDeleted', line.get('qty'));
                  line.set('obposIsDeleted', true);
                  line.set('qty', 0, {
                    silent: true
                  });
                });
                if (receipt.get('hasServices')) {
                  receipt.unset('hasServices');
                }
                receipt.unset('preventServicesUpdate');
                receipt.set('skipCalculateReceipt', false);
                // These setIsCalculateReceiptLockState and setIsCalculateGrossLockState calls must be done because this function
                // may be called out of the pointofsale window, and in order to call the calculateReceipt function, the
                // isCalculateReceiptLockState and isCalculateGrossLockState properties must be initialized
                receipt.setIsCalculateReceiptLockState(false);
                receipt.setIsCalculateGrossLockState(false);
                receipt.calculateReceipt(function() {
                  markOrderAsDeleted(receipt, orderList, callback);
                });
              });
            } else {
              removeReceiptFromDatabase(receipt, callback);
            }
          } else if (
            receipt.has('deletedLines') &&
            !receipt.get('isQuotation')
          ) {
            if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
              receipt.set('skipCalculateReceipt', false);
              // These setIsCalculateReceiptLockState and setIsCalculateGrossLockState calls must be done because this function
              // may be called out of the pointofsale window, and in order to call the calculateReceipt function, the
              // isCalculateReceiptLockState and isCalculateGrossLockState properties must be initialized
              receipt.setIsCalculateReceiptLockState(false);
              receipt.setIsCalculateGrossLockState(false);
              markOrderAsDeleted(receipt, orderList, callback);
            } else {
              removeReceiptFromDatabase(receipt, callback);
            }
          } else if (
            receipt.has('lines') &&
            receipt.get('lines').length === 0 &&
            receipt.get('isEditable') &&
            !receipt.get('isQuotation')
          ) {
            if (
              OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true) &&
              receipt.get('id') !== null &&
              (receipt.get('documentnoSuffix') <=
                OB.MobileApp.model.documentnoThreshold ||
                OB.MobileApp.model.documentnoThreshold === 0)
            ) {
              receipt.setIsCalculateReceiptLockState(true);
              receipt.setIsCalculateGrossLockState(true);
              markOrderAsDeleted(receipt, orderList, callback);
            } else {
              removeReceiptFromDatabase(receipt, callback);
            }
          } else {
            removeReceiptFromDatabase(receipt, callback);
          }
        }

        function validateRemoveOrder() {
          if (receipt.get('id') && !isPaidQuotation) {
            removePayments(receipt, function(success) {
              if (success) {
                finishRemoveOrder();
              } else {
                OB.MobileApp.view.scanningFocus(true);
              }
            });
          } else {
            finishRemoveOrder();
          }
        }

        if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          OB.UTIL.rebuildCashupFromServer(function() {
            OB.UTIL.showLoading(false);
            validateRemoveOrder();
          });
        } else {
          validateRemoveOrder();
        }
      }

      OB.MobileApp.view.setOriginalScanMode(OB.MobileApp.view.scanMode);
      OB.MobileApp.view.scanningFocus(false);
      if (this.get('isEditable') === true) {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_PreDeleteCurrentOrder',
          {
            context: context,
            receipt: this
          },
          function(args) {
            if (args && args.cancelOperation && args.cancelOperation === true) {
              if (callback instanceof Function) {
                OB.MobileApp.view.scanningFocus(true);
                callback();
              }
              return;
            }
            removeOrder(args.receipt, callback);
          }
        );
      } else {
        removeOrder(this, callback);
      }

      return true;
    },
    generateInvoice: function(callback) {
      var receiptShouldBeInvoiced = false,
        me = this,
        invoice,
        isDeleted = this.get('obposIsDeleted'),
        deliveredNotInvoicedLine;

      function finalCallback(invoice) {
        if (callback && callback instanceof Function) {
          callback(invoice);
        }
      }

      if (
        isDeleted ||
        (!this.get('payOnCredit') && !this.get('completeTicket'))
      ) {
        finalCallback();
        return;
      }

      if (
        (this.getInvoiceTerms() === 'I' && this.get('generateInvoice')) ||
        this.get('payOnCredit')
      ) {
        receiptShouldBeInvoiced = true;
      } else if (this.getInvoiceTerms() === 'O') {
        if (this.get('deliver')) {
          receiptShouldBeInvoiced = true;
        }
      } else if (this.getInvoiceTerms() === 'D') {
        if (this.get('generateShipment')) {
          receiptShouldBeInvoiced = true;
        } else {
          deliveredNotInvoicedLine = _.find(this.get('lines').models, function(
            line
          ) {
            return line.getDeliveredQuantity() !== line.get('invoicedQuantity');
          });
          receiptShouldBeInvoiced = !_.isUndefined(deliveredNotInvoicedLine);
        }
      }

      if (receiptShouldBeInvoiced) {
        invoice = new OB.Model.Order();
        OB.UTIL.clone(this, invoice);
        //TODO: check & generate ids
        invoice.set('orderId', this.get('id'));
        invoice.set('id', OB.UTIL.get_UUID());
        invoice.unset('calculateReceiptCallbacks');
        invoice.unset('calculatedInvoice');
        invoice.unset('canceledorder');
        invoice.get('payments').reset();
        invoice.unset('json');
        invoice.unset('undo');
        invoice.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        invoice.set('isInvoice', true);
        invoice.unset('isBeingClosed');
        invoice.get('lines').reset();

        this.get('lines').forEach(function(ol) {
          var originalQty = ol.get('qty'),
            qtyAlreadyInvoiced = ol.get('invoicedQuantity') || OB.DEC.Zero,
            qtyPendingToBeInvoiced = OB.DEC.sub(
              ol.get('qty'),
              qtyAlreadyInvoiced
            ),
            qtyToDeliver = !OB.UTIL.isNullOrUndefined(
              ol.get('obposQtytodeliver')
            )
              ? ol.get('obposQtytodeliver')
              : ol.get('qty'),
            qtyToInvoice = OB.DEC.Zero,
            lineToInvoice;
          if (me.getInvoiceTerms() === 'D') {
            qtyToInvoice = OB.DEC.sub(qtyToDeliver, qtyAlreadyInvoiced);
          } else if (
            me.getInvoiceTerms() === 'I' ||
            me.getInvoiceTerms() === 'O'
          ) {
            qtyToInvoice = qtyPendingToBeInvoiced;
          }
          if (
            qtyToInvoice &&
            (ol.get('obposCanbedelivered') ||
              ol.get('obposIspaid') ||
              me.getInvoiceTerms() === 'I')
          ) {
            lineToInvoice = new OB.Model.OrderLine();
            OB.UTIL.clone(ol, lineToInvoice);
            lineToInvoice.set('id', OB.UTIL.get_UUID());
            lineToInvoice.set('qty', qtyToInvoice);
            lineToInvoice.set('orderLineId', ol.get('id'));
            lineToInvoice.get('product').set('ignorePromotions', true);
            lineToInvoice.get('product').unset('img');
            if (OB.DEC.abs(qtyAlreadyInvoiced) > 0) {
              lineToInvoice.get('promotions').forEach(function(p) {
                if (
                  OB.DEC.abs(qtyToInvoice) < OB.DEC.abs(qtyPendingToBeInvoiced)
                ) {
                  p.amt = OB.DEC.mul(
                    p.amt,
                    OB.DEC.div(qtyToInvoice, originalQty)
                  );
                  p.obdiscQtyoffer = qtyToInvoice;
                  if (p.actualAmt) {
                    p.actualAmt = OB.DEC.mul(
                      p.actualAmt,
                      OB.DEC.div(qtyToInvoice, originalQty)
                    );
                  }
                  if (p.displayedTotalAmount) {
                    p.displayedTotalAmount = OB.DEC.mul(
                      p.displayedTotalAmount,
                      OB.DEC.div(qtyToInvoice, originalQty)
                    );
                  }
                  if (p.fullAmt) {
                    p.fullAmt = OB.DEC.mul(
                      p.fullAmt,
                      OB.DEC.div(qtyToInvoice, originalQty)
                    );
                  }
                  if (p.qtyOffer) {
                    p.qtyOffer = qtyToInvoice;
                  }
                  if (p.pendingQtyOffer) {
                    p.pendingQtyOffer = qtyToInvoice;
                  }
                } else {
                  p.amt = OB.DEC.sub(
                    p.amt,
                    OB.DEC.mul(
                      p.amt,
                      OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                    )
                  );
                  p.obdiscQtyoffer = qtyToInvoice;
                  if (p.actualAmt) {
                    p.actualAmt = OB.DEC.sub(
                      p.actualAmt,
                      OB.DEC.mul(
                        p.actualAmt,
                        OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                      )
                    );
                  }
                  if (p.displayedTotalAmount) {
                    p.displayedTotalAmount = OB.DEC.sub(
                      p.displayedTotalAmount,
                      OB.DEC.mul(
                        p.displayedTotalAmount,
                        OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                      )
                    );
                  }
                  if (p.fullAmt) {
                    p.fullAmt = OB.DEC.sub(
                      p.fullAmt,
                      OB.DEC.mul(
                        p.fullAmt,
                        OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                      )
                    );
                  }
                  if (p.qtyOffer) {
                    p.qtyOffer = qtyToInvoice;
                  }
                  if (p.pendingQtyOffer) {
                    p.pendingQtyOffer = qtyToInvoice;
                  }
                }
              });
            }
            invoice.get('lines').add(lineToInvoice);
          }
        });

        if (invoice.get('lines').length) {
          if (invoice.get('lines').length !== me.get('lines').length) {
            invoice.isCalculateReceiptLocked = false;
            invoice.isCalculateGrossLocked = false;
            invoice.set('skipApplyPromotions', true);
            invoice.set('ignoreCheckIfIsActiveOrder', true);
            invoice.set('forceCalculateTaxes', true);
            invoice.preventOrderSave(true);
            invoice.calculateReceipt(function() {
              invoice.preventOrderSave(false);
              invoice.adjustPrices();
              finalCallback(invoice);
            });
          } else {
            // The full order is being invoiced
            finalCallback(invoice);
          }
        } else {
          //No invoice lines were generated; do not save the invoice, just trigger the event
          finalCallback();
        }
      } else {
        // The invoice musn't be created
        finalCallback();
      }
    },
    checkOrderPayment: function() {
      var hasPayments = false;
      if (this.get('payments').length > 0) {
        if (
          this.get('receiptPayments') &&
          this.get('payments').length > this.get('receiptPayments').length
        ) {
          hasPayments = true;
        } else if (!this.get('receiptPayments')) {
          hasPayments = true;
        }
      }

      if (hasPayments) {
        OB.UTIL.showConfirmation.display(
          '',
          OB.I18N.getLabel('OBPOS_RemoveReceiptWithPayment')
        );
        return true;
      }
      return false;
    },
    getScannableDocumentNo: function() {
      return this.get('documentNo')
        .replace(/-/g, '\\-')
        .replace(/\+/g, '\\+');
    },
    turnEditable: function(callback) {
      if (
        this.get('payment') > 0 ||
        this.get('isPartiallyDelivered') ||
        this.get('isFullyDelivered')
      ) {
        return;
      }

      this.set('isModified', true);
      this.set('isEditable', true);
      if (this.get('isLayaway')) {
        this.set('isLayaway', false);
        this.set('orderType', 2);
      }
      this.unset('skipApplyPromotions');
      this.save(callback);
    },
    isAnonymousBlindReturn: function() {
      var me = this;
      if (
        me.get('bp').id ===
          OB.MobileApp.model.get('terminal').businessPartner &&
        !me.get('obposIsDeleted')
      ) {
        // Checking blind returned lines
        var isBlindReturn = false;
        var i,
          lines = me.get('lines');
        for (i = 0; i < lines.length; i++) {
          if (
            lines.models[i].get('qty') < 0 &&
            !lines.models[i].get('originalDocumentNo')
          ) {
            isBlindReturn = true;
            break;
          }
        }
        return isBlindReturn;
      } else {
        return false;
      }
    }
  });

  var OrderList = Backbone.Collection.extend(
    {
      model: Order,

      constructor: function(modelOrder) {
        if (modelOrder) {
          //this._id = 'modelorderlist';
          this.modelorder = modelOrder;
        }
        Backbone.Collection.prototype.constructor.call(this);
      },

      initialize: function() {
        var me = this;
        this.current = null;
        if (this.modelorder) {
          this.modelorder.on('saveCurrent', function() {
            me.saveCurrent();
          });
        }
      },

      newOrder: function(bp) {
        var i,
          p,
          receiptProperties,
          propertiesToReset = [];
        // reset in new order properties defined in Receipt Properties dialog
        if (
          OB.MobileApp.view.$.containerWindow &&
          OB.MobileApp.view.$.containerWindow.getRoot() &&
          OB.MobileApp.view.$.containerWindow.getRoot().$
            .receiptPropertiesDialog
        ) {
          receiptProperties = OB.MobileApp.view.$.containerWindow.getRoot().$
            .receiptPropertiesDialog.newAttributes;
          for (i = 0; i < receiptProperties.length; i++) {
            if (receiptProperties[i].modelProperty) {
              var properties = {
                propertyName: receiptProperties[i].modelProperty
              };
              if (
                !OB.UTIL.isNullOrUndefined(receiptProperties[i].defaultValue)
              ) {
                properties.defaultValue = receiptProperties[i].defaultValue;
              }
              propertiesToReset.push(properties);
            }
            if (receiptProperties[i].extraProperties) {
              for (
                p = 0;
                p < receiptProperties[i].extraProperties.length;
                p++
              ) {
                propertiesToReset.push({
                  propertyName: receiptProperties[i].extraProperties[p]
                });
              }
            }
          }
        }
        return OB.Collection.OrderList.newOrder(bp, propertiesToReset);
      },

      loadCustomer: async function(model, callback) {
        var bpId,
          bpLocId,
          bpBillLocId,
          bpLoc,
          bpBillLoc,
          loadBusinesPartner,
          loadLocations,
          finalCallback,
          isLoadedPartiallyFromBackend = false;

        bpId = model.bpId;
        bpLocId = model.bpLocId;
        bpBillLocId = model.bpBillLocId || model.bpLocId;

        finalCallback = function(bp, bpLoc, bpBillLoc) {
          bp.set('locations', bp.get('locations') || []);
          bp.set('shipLocId', bpLoc.get('id'));
          bp.set('shipLocName', bpLoc.get('name'));
          bp.set('shipPostalCode', bpLoc.get('postalCode'));
          bp.set('shipCityName', bpLoc.get('cityName'));
          bp.set('shipCountryId', bpLoc.get('countryId'));
          bp.set('shipCountryName', bpLoc.get('countryName'));
          bp.set('shipRegionId', bpLoc.get('regionId'));
          bp.set('locId', (bpBillLoc || bpLoc).get('id'));
          bp.set('locName', (bpBillLoc || bpLoc).get('name'));
          bp.set('postalCode', (bpBillLoc || bpLoc).get('postalCode'));
          bp.set('cityName', (bpBillLoc || bpLoc).get('cityName'));
          bp.set('countryName', (bpBillLoc || bpLoc).get('countryName'));
          bp.set('locationModel', bpLoc);
          bp.get('locations').push(bpLoc);
          if (bpBillLoc) {
            bp.set('locationBillModel', bpBillLoc);
            bp.get('locations').push(bpBillLoc);
          }
          callback(bp, bpLoc, bpBillLoc);
        };

        loadBusinesPartner = function(
          bpartnerId,
          bpLocationId,
          bpBillLocationId,
          callback
        ) {
          var loadCustomerParameters = {
            bpartnerId: bpartnerId,
            bpLocationId: bpLocationId
          };
          if (bpLocationId !== bpBillLocationId) {
            loadCustomerParameters.bpBillLocationId = bpBillLocationId;
          }
          new OB.DS.Request(
            'org.openbravo.retail.posterminal.master.LoadedCustomer'
          ).exec(
            loadCustomerParameters,
            function(data) {
              if (data.length >= 2) {
                isLoadedPartiallyFromBackend = true;
                callback({
                  bpartner: OB.Dal.transform(OB.Model.BusinessPartner, data[0]),
                  bpLoc: OB.Dal.transform(OB.Model.BPLocation, data[1]),
                  bpBillLoc:
                    bpLocationId !== bpBillLocationId
                      ? OB.Dal.transform(OB.Model.BPLocation, data[2])
                      : null
                });
              } else {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBPOS_InformationTitle'),
                  OB.I18N.getLabel('OBPOS_NoCustomerForPaidReceipt'),
                  [
                    {
                      label: OB.I18N.getLabel('OBPOS_LblOk'),
                      isConfirmButton: true
                    }
                  ]
                );
              }
            },
            function() {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBPOS_InformationTitle'),
                OB.I18N.getLabel('OBPOS_NoCustomerForPaidReceipt'),
                [
                  {
                    label: OB.I18N.getLabel('OBPOS_LblOk'),
                    isConfirmButton: true
                  }
                ]
              );
            }
          );
        };

        loadLocations = function(bp) {
          if (bpLocId === bpBillLocId) {
            if (isLoadedPartiallyFromBackend) {
              finalCallback(bp, bpLoc, null);
            } else {
              OB.Dal.get(
                OB.Model.BPLocation,
                bpLocId,
                function(bpLoc) {
                  finalCallback(bp, bpLoc, null);
                },
                function(tx, error) {
                  OB.UTIL.showError(error);
                },
                function() {
                  loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(
                    data
                  ) {
                    finalCallback(bp, data.bpLoc, null);
                  });
                }
              );
            }
          } else {
            if (
              isLoadedPartiallyFromBackend &&
              !OB.UTIL.isNullOrUndefined(bpLoc) &&
              !OB.UTIL.isNullOrUndefined(bpBillLoc)
            ) {
              finalCallback(bp, bpLoc, bpBillLoc);
            } else {
              var criteria = {};
              if (
                OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)
              ) {
                var remoteCriteria = [
                  {
                    columns: ['id'],
                    operator: 'equals',
                    value: [bpLocId, bpBillLocId]
                  }
                ];
                criteria.remoteFilters = remoteCriteria;
              } else {
                criteria._whereClause =
                  'where c_bpartner_location_id in (?, ?)';
                criteria.params = [bpLocId, bpBillLocId];
              }
              OB.Dal.find(
                OB.Model.BPLocation,
                criteria,
                function(locations) {
                  if (locations.models.length === 2) {
                    _.each(locations.models, function(l) {
                      if (l.id === bpLocId) {
                        bpLoc = l;
                      } else if (l.id === bpBillLocId) {
                        bpBillLoc = l;
                      }
                    });
                    finalCallback(bp, bpLoc, bpBillLoc);
                  } else {
                    loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(
                      data
                    ) {
                      finalCallback(bp, data.bpLoc, data.bpBillLoc);
                    });
                  }
                },
                function(tx, error) {
                  OB.UTIL.showError(error);
                },
                bpLoc
              );
            }
          }
        };
        OB.Dal.get(
          OB.Model.BusinessPartner,
          bpId,
          function(bp) {
            loadLocations(bp);
          },
          null,
          function() {
            //Empty
            loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(data) {
              bpLoc = data.bpLoc;
              if (bpLocId !== bpBillLocId) {
                bpBillLoc = data.bpBillLoc;
              }
              loadLocations(data.bpartner);
            });
          }
        );
      },

      newPaidReceipt: async function(model, callback) {
        var order = new Order(),
          lines,
          newline,
          payments,
          curPayment,
          taxes,
          numberOfLines = model.receiptLines.length,
          orderQty = 0,
          NoFoundProduct = true,
          execution = OB.UTIL.ProcessController.start('newPaidReceipt');

        // Each payment that has been reverted stores the id of the reversal payment
        // Web POS, instead of that, need to have the information of the payment reverted on the reversal payment
        // This loop switches the information between them
        _.each(
          _.filter(model.receiptPayments, function(payment) {
            return payment.isReversed;
          }),
          function(payment) {
            var reversalPayment = _.find(model.receiptPayments, function(
              currentPayment
            ) {
              return currentPayment.paymentId === payment.reversedPaymentId;
            });
            reversalPayment.reversedPaymentId = payment.paymentId;
            reversalPayment.isReversePayment = true;
            delete payment.reversedPaymentId;
          }
        );

        // Call orderLoader plugings to adjust remote model to local model first
        // ej: sales on credit: Add a new payment if total payment < total receipt
        // ej: gift cards: Add a new payment for each gift card discount
        _.each(OB.Model.modelLoaders, function(f) {
          f(model);
        });

        //model.set('id', null);
        lines = new Backbone.Collection();

        // set all properties coming from the model
        order.set(model);

        // setting specific properties
        order.set('isbeingprocessed', 'N');
        order.set('hasbeenpaid', 'N');
        order.set('isEditable', false);
        order.set('isModified', false);
        order.set('checked', model.checked); //TODO: what is this for, where it comes from?
        order.set('orderDate', OB.I18N.normalizeDate(model.orderDate));
        order.set('creationDate', OB.I18N.normalizeDate(model.creationDate));
        order.set('updatedBy', OB.MobileApp.model.usermodel.id);
        order.set('paidPartiallyOnCredit', false);
        order.set('paidOnCredit', false);
        order.set('session', OB.MobileApp.model.get('session'));
        order.set('skipApplyPromotions', true);
        order.set(
          'documentnoPrefix',
          OB.MobileApp.model.get('terminal').docNoPrefix
        ); // hack to prevent change number
        if (model.isQuotation) {
          order.set('isQuotation', true);
          order.set('oldId', model.orderid);
          order.set('id', null);
          order.set(
            'documentType',
            OB.MobileApp.model.get('terminal').terminalType
              .documentTypeForQuotations
          );
          order.set('hasbeenpaid', 'Y');
          // TODO: this commented lines are kept just in case this issue happens again
          // Set creationDate milliseconds to 0, if the date is with milisecond, the date with miliseconds is rounded to seconds:
          // so, the second can change, and the creationDate in quotation should not be changed when quotation is reactivated
          // order.set('creationDate', moment(model.creationDate.toString(), "YYYY-MM-DD hh:m:ss").toDate());
        }
        if (model.isLayaway) {
          order.set('isLayaway', true);
          order.set('id', model.orderid);
          order.set('hasbeenpaid', 'N');
        } else {
          order.set('isPaid', true);
          var paidByPayments = OB.DEC.Zero;
          _.each(model.receiptPayments, function(receiptPayment) {
            paidByPayments = OB.DEC.add(
              paidByPayments,
              OB.DEC.mul(receiptPayment.amount, receiptPayment.rate)
            );
          });

          var creditAmount = OB.DEC.sub(model.totalamount, paidByPayments);
          if (
            OB.DEC.compare(model.totalamount) > 0 &&
            OB.DEC.compare(creditAmount) > 0 &&
            !model.isQuotation
          ) {
            order.set('creditAmount', creditAmount);
            if (paidByPayments) {
              order.set('paidPartiallyOnCredit', true);
            }
            order.set('paidOnCredit', true);
          }
          order.set('id', model.orderid);
          if (
            order.get('documentType') ===
            OB.MobileApp.model.get('terminal').terminalType
              .documentTypeForReturns
          ) {
            //It's a return
            order.set('orderType', 1);
          }
        }
        let loadProducts = async function() {
          var linepos = 0,
            hasDeliveredProducts = false,
            hasNotDeliveredProducts = false,
            i,
            sortedPayments = false;

          function getReverserPayment(payment, Payments) {
            return _.filter(model.receiptPayments, function(receiptPayment) {
              return receiptPayment.paymentId === payment.reversedPaymentId;
            })[0];
          }
          i = 0;
          // Sort payments array, puting reverser payments inmediatly after their reversed payment
          while (i < model.receiptPayments.length) {
            var payment = model.receiptPayments[i];
            if (payment.reversedPaymentId && !payment.isSorted) {
              var reversed_index = model.receiptPayments.indexOf(
                getReverserPayment(payment, model.receiptPayments)
              );
              payment.isSorted = true;
              if (i < reversed_index) {
                model.receiptPayments.splice(i, 1);
                model.receiptPayments.splice(reversed_index, 0, payment);
                sortedPayments = true;
              } else if (i > reversed_index + 1) {
                model.receiptPayments.splice(i, 1);
                model.receiptPayments.splice(reversed_index + 1, 0, payment);
                sortedPayments = true;
              }
            } else {
              i++;
            }
          }
          if (sortedPayments) {
            model.receiptPayments.forEach(function(receitPayment) {
              if (receitPayment.isSorted) {
                delete receitPayment.isSorted;
              }
            });
          }
          //order.set('payments', model.receiptPayments);
          payments = new PaymentLineList();
          _.each(model.receiptPayments, function(iter) {
            var paymentProp;
            curPayment = new PaymentLine();
            for (paymentProp in iter) {
              if (iter.hasOwnProperty(paymentProp)) {
                if (paymentProp === 'paymentDate') {
                  if (
                    !OB.UTIL.isNullOrUndefined(iter[paymentProp]) &&
                    moment(iter[paymentProp]).isValid()
                  ) {
                    curPayment.set(
                      paymentProp,
                      OB.I18N.normalizeDate(new Date(iter[paymentProp]))
                    );
                  } else {
                    curPayment.set(paymentProp, null);
                  }
                } else {
                  curPayment.set(paymentProp, iter[paymentProp]);
                }
              }
            }
            curPayment.set('orderGross', order.get('gross'));
            curPayment.set('isPaid', order.get('isPaid'));
            curPayment.set('date', new Date(iter.paymentDate));
            payments.add(curPayment);
          });
          order.set('payments', payments);
          order.adjustPayment();

          taxes = {};
          _.each(model.receiptTaxes, function(iter) {
            var taxProp;
            taxes[iter.taxid] = {};
            for (taxProp in iter) {
              if (iter.hasOwnProperty(taxProp)) {
                taxes[iter.taxid][taxProp] = iter[taxProp];
              }
            }
          });
          order.set('taxes', taxes);

          if (!model.isLayaway && !model.isQuotation) {
            if (
              model.totalamount > 0 &&
              order.get('payment') < model.totalamount
            ) {
              order.set('paidOnCredit', true);
            } else if (
              model.totalamount < 0 &&
              (order.get('payment') === 0 ||
                OB.DEC.abs(model.totalamount) > order.get('payment'))
            ) {
              order.set('paidOnCredit', true);
            }
          }
          if (model.receiptLines.length === 0) {
            order.set('json', JSON.stringify(order.toJSON()));
            callback(order);
            OB.UTIL.ProcessController.finish('newPaidReceipt', execution);
          }

          for (let i = 0; i < model.receiptLines.length; i++) {
            let iter = model.receiptLines[i];
            var price = OB.DEC.number(iter.unitPrice),
              lineNet = order.get('priceIncludesTax')
                ? null
                : OB.DEC.number(iter.lineGrossAmount || iter.linenetamount),
              lineGross = order.get('priceIncludesTax')
                ? OB.DEC.number(iter.lineGrossAmount)
                : null;
            iter.linepos = linepos;
            var addLineForProduct = async function(prod) {
              // Set product services
              await order._loadRelatedServices(
                prod.get('productType'),
                prod.get('id'),
                prod.get('productCategory'),
                async function(data) {
                  let hasservices;
                  if (
                    !OB.UTIL.isNullOrUndefined(data) &&
                    OB.DEC.number(iter.quantity) > 0
                  ) {
                    hasservices = data.hasservices;
                  }

                  for (let promotion of iter.promotions) {
                    try {
                      const discount = OB.Discounts.Pos.manualRuleImpls.find(
                        discount => discount.id === promotion.ruleId
                      );
                      if (
                        discount &&
                        OB.Discounts.Pos.getManualPromotions().includes(
                          discount.discountType
                        )
                      ) {
                        var percentage;
                        if (discount.obdiscPercentage) {
                          percentage = OB.DEC.mul(
                            OB.DEC.div(promotion.amt, iter.lineGrossAmount),
                            new BigDecimal('100')
                          );
                        }
                        promotion.userAmt = percentage
                          ? percentage
                          : promotion.amt;
                        promotion.discountType = discount.discountType;
                        promotion.manual = true;
                      }
                    } catch (error) {
                      OB.UTIL.showError(error);
                    }
                  }

                  if (
                    OB.MobileApp.model.hasPermission(
                      'OBPOS_EnableSupportForProductAttributes',
                      true
                    )
                  ) {
                    if (
                      iter.attributeValue &&
                      _.isString(iter.attributeValue)
                    ) {
                      var processedAttValues = OB.UTIL.AttributeUtils.generateDescriptionBasedOnJson(
                        iter.attributeValue
                      );
                      if (
                        processedAttValues &&
                        processedAttValues.keyValue &&
                        _.isArray(processedAttValues.keyValue) &&
                        processedAttValues.keyValue.length > 0
                      ) {
                        iter.attSetInstanceDesc =
                          processedAttValues.description;
                      }
                    }
                  }
                  newline = new OrderLine({
                    id: iter.lineId,
                    product: prod,
                    uOM: iter.uOM,
                    qty: OB.DEC.number(
                      iter.quantity,
                      prod.get('uOMstandardPrecision')
                    ),
                    price: price,
                    priceList:
                      prod.get('listPrice') !== price
                        ? price
                        : prod.get('listPrice'),
                    grossListPrice: iter.grossListPrice,
                    net: lineNet,
                    gross: lineGross,
                    promotions: iter.promotions,
                    description: iter.description,
                    priceIncludesTax: order.get('priceIncludesTax'),
                    hasRelatedServices: hasservices,
                    attributeValue: iter.attributeValue,
                    warehouse: {
                      id: iter.warehouse,
                      warehousename: iter.warehousename
                    },
                    relatedLines: iter.relatedLines,
                    groupService: prod.get('groupProduct'),
                    isEditable: true,
                    isDeletable: true,
                    attSetInstanceDesc: iter.attSetInstanceDesc
                      ? iter.attSetInstanceDesc
                      : null,
                    lineGrossAmount: iter.lineGrossAmount
                  });

                  if (!order.get('isQuotation')) {
                    newline.set(
                      'creationListPrice',
                      iter.priceIncludesTax
                        ? iter.grossListPrice
                        : iter.listPrice
                    );
                  }

                  // copy verbatim not owned properties -> modular properties.
                  _.each(iter, function(value, key) {
                    if (!newline.ownProperties[key]) {
                      newline.set(key, value);
                    }
                  });

                  // add the created line
                  lines.add(newline, {
                    at: iter.linepos
                  });
                  numberOfLines--;
                  orderQty = OB.DEC.add(iter.quantity, orderQty);
                  if (numberOfLines === 0) {
                    lines.reset(
                      lines.sortBy(function(line) {
                        return line.get('linepos');
                      })
                    );
                    order.set('lines', lines);
                    order.set('qty', orderQty);
                    order.set(
                      'isPartiallyDelivered',
                      hasDeliveredProducts && hasNotDeliveredProducts
                        ? true
                        : false
                    );
                    if (hasDeliveredProducts && !hasNotDeliveredProducts) {
                      order.set('isFullyDelivered', true);
                    }
                    if (order.get('isPartiallyDelivered')) {
                      var partiallyPaid = 0;
                      _.each(
                        _.filter(order.get('receiptLines'), function(
                          reciptLine
                        ) {
                          return reciptLine.deliveredQuantity;
                        }),
                        function(deliveredLine) {
                          partiallyPaid = OB.DEC.add(
                            partiallyPaid,
                            OB.DEC.mul(
                              deliveredLine.deliveredQuantity,
                              deliveredLine.grossUnitPrice
                            )
                          );
                        }
                      );
                      order.set('deliveredQuantityAmount', partiallyPaid);
                      if (
                        order.get('deliveredQuantityAmount') &&
                        order.get('deliveredQuantityAmount') >
                          order.get('payment')
                      ) {
                        order.set('isDeliveredGreaterThanGross', true);
                      }
                    }
                    order.set('json', JSON.stringify(order.toJSON()));
                    callback(order);
                    OB.UTIL.ProcessController.finish(
                      'newPaidReceipt',
                      execution
                    );
                  }
                }
              );
            };

            if (!iter.deliveredQuantity) {
              hasNotDeliveredProducts = true;
            } else {
              hasDeliveredProducts = true;
              if (iter.deliveredQuantity < iter.quantity) {
                hasNotDeliveredProducts = true;
              }
            }

            if (iter.relatedLines && !order.get('hasServices')) {
              order.set('hasServices', true);
            }
            try {
              const product = await OB.App.MasterdataModels.Product.withId(
                iter.id
              );
              if (product) {
                await addLineForProduct(
                  OB.Dal.transform(OB.Model.Product, product)
                );
              } else {
                //Empty
                const body = {
                  productId: iter.id,
                  salesOrderLineId: iter.lineId
                };
                try {
                  let data = await OB.App.Request.mobileServiceRequest(
                    'org.openbravo.retail.posterminal.master.LoadedProduct',
                    body
                  );
                  data = data.response.data;
                  await addLineForProduct(
                    OB.Dal.transform(OB.Model.Product, data[0])
                  );
                } catch (error) {
                  if (NoFoundProduct) {
                    NoFoundProduct = false;
                    OB.UTIL.showConfirmation.display(
                      OB.I18N.getLabel('OBPOS_InformationTitle'),
                      OB.I18N.getLabel('OBPOS_NoReceiptLoadedText'),
                      [
                        {
                          label: OB.I18N.getLabel('OBPOS_LblOk'),
                          isConfirmButton: true
                        }
                      ]
                    );
                  }
                }
              }
            } catch (error) {
              OB.error(error.message);
            }
            linepos++;
          }
        };

        await this.loadCustomer(
          {
            bpId: model.bp,
            bpLocId: model.bpLocId,
            bpBillLocId: model.bpBillLocId || model.bpLocId
          },
          async function(bp, loc, billLoc) {
            order.set('bp', bp);
            order.set('gross', model.totalamount);
            order.set('net', model.totalNetAmount);
            order.trigger('change:bp', order);
            await loadProducts();
          }
        );
      },
      newDynamicOrder: function(model, callback) {
        var order = new OB.Model.Order(),
          undf;
        _.each(_.keys(model), function(key) {
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

      addNewOrder: function(isFirstOrder) {
        var me = this;
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
          me.doRemoteBPSettings(OB.MobileApp.model.get('businessPartner'));
        }
        this.saveCurrent();
        this.current = this.newOrder();
        this.unshift(this.current);
        this.loadCurrent(true);
      },

      addThisOrder: function(model) {
        this.saveCurrent();
        this.current = model;
        this.unshift(this.current);
        this.loadCurrent();
      },

      addFirstOrder: function() {
        this.addNewOrder(true);
      },

      addPaidReceipt: function(model, callback) {
        var me = this,
          execution = OB.UTIL.ProcessController.start('addPaidReceipt');

        function executeFinalCallback() {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_PostAddPaidReceipt',
            {
              order: model
            },
            function(args) {
              if (callback instanceof Function) {
                callback(me.modelorder);
              }
            }
          );
        }

        if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
          this.doRemoteBPSettings(model.get('bp'));
        } else {
          OB.UTIL.showLoading(false);
        }

        this.saveCurrent();
        this.current = model;
        this.unshift(this.current);
        this.loadCurrent(true);

        if (!model.get('isQuotation')) {
          // OB.Dal.save is done here because we want to force to save with the original id, only this time.
          OB.Dal.save(
            model,
            function() {
              OB.UTIL.ProcessController.finish('addPaidReceipt', execution);
              executeFinalCallback();
            },
            function() {
              OB.UTIL.ProcessController.finish('addPaidReceipt', execution);
              OB.error(arguments);
              executeFinalCallback();
            },
            true
          );
        } else {
          OB.UTIL.ProcessController.finish('addPaidReceipt', execution);
          executeFinalCallback();
        }
      },

      addMultiReceipt: function(model) {
        OB.Dal.save(
          model,
          function() {},
          function() {
            OB.error(arguments);
          },
          model.get('loadedFromServer')
        );
      },

      doRemoteBPSettings: function(businessPartner) {
        OB.Dal.saveOrUpdate(
          businessPartner,
          function() {},
          function() {
            OB.error(arguments);
          }
        );
        OB.Dal.saveOrUpdate(
          businessPartner.get('locationModel'),
          function() {},
          function() {
            OB.error(arguments);
          }
        );
        OB.UTIL.showLoading(false);
      },

      addNewQuotation: function() {
        this.saveCurrent();
        this.current = this.newOrder();
        this.current.setQuotationProperties();
        this.unshift(this.current);
        this.loadCurrent();
      },
      deleteCurrentFromDatabase: function(orderToDelete) {
        OB.Dal.remove(
          orderToDelete,
          function() {
            return true;
          },
          function() {
            OB.UTIL.showError('Error removing');
          }
        );
      },
      deleteCurrent: function(forceCreateNew) {
        if (!this.current) {
          return;
        }

        this.remove(this.current);
        var createNew = forceCreateNew || this.length === 0;
        if (createNew) {
          var order = this.newOrder();

          this.unshift(order);
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            this.doRemoteBPSettings(OB.MobileApp.model.get('businessPartner'));
          }
        }
        this.current = this.at(0);
        this.loadCurrent(createNew);

        // Refresh Master Data
        OB.UTIL.checkRefreshMasterData();
      },

      load: function(model) {
        // Workaround to prevent the pending receipts moder window from remaining open
        // when the current receipt is selected from the list
        if (
          model &&
          this.current &&
          model.get('documentNo') === this.current.get('documentNo')
        ) {
          return;
        }
        this.saveCurrent();
        this.current = model;
        this.loadCurrent();
      },
      loadById: function(id) {
        var mdl = _.find(OB.MobileApp.model.orderList.models, function(model) {
          return model.get('id') === id;
        });
        if (mdl) {
          this.load(mdl);
        }
      },
      saveCurrent: function() {
        if (this.current) {
          OB.UTIL.clone(this.modelorder, this.current);
          this.current.trigger('updateView');
        }
      },
      loadCurrent: function(isNew) {
        OB.App.State.TerminalLog.setContext({
          context: this.current.get('documentNo')
        });
        // Check if the current order to be loaded should be deleted
        if (this.current.get('obposIsDeleted') && this.current.get('id')) {
          var deletedOrderDocNo = this.current.get('documentNo');
          this.current.set('ignoreCheckIfIsActiveOrder', true); // Ignore this receipt is not loaded in the UI
          this.current.deleteOrder(this.current, function() {
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_OrderMarkedToBeDeleted', [
                deletedOrderDocNo
              ])
            );
          });
          return;
        }

        if (this.current) {
          if (isNew) {
            //set values of new attrs in current,
            //this values will be copied to modelOrder
            //in the next instruction
            this.modelorder.trigger('beforeChangeOrderForNewOne', this.current);
            this.current.set('isNewReceipt', true);
          }
          this.modelorder.clearWith(this.current);
          this.modelorder.set('isNewReceipt', false);
          this.modelorder.trigger('paintTaxes');
          this.modelorder.trigger('updatePending');
          this.modelorder.setIsCalculateReceiptLockState(false);
          this.modelorder.setIsCalculateGrossLockState(false);
        }
      },
      checkOrderListPayment: function() {
        var i;
        for (i = 0; i < this.models.length; i++) {
          if (this.models[i].checkOrderPayment()) {
            return true;
          }
        }
        return false;
      },
      synchronizeCurrentOrder: function() {
        // NOTE: No need to execute any business logic here
        // The new functionality of loading document no, makes this function obsolete.
        // The function is not removed to avoid api changes
      },
      checkForDuplicateReceipts: function(
        model,
        callback,
        errorCallback,
        calledFrom
      ) {
        function openReceiptPermissionError(orderType) {
          if (calledFrom === 'orderSelector' || calledFrom === 'return') {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_OpenReceiptPermissionError', [orderType])
            );
          } else {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_OpenReceiptPermissionError', [orderType])
            );
          }
          if (errorCallback) {
            errorCallback();
          }
        }

        //Check Permissions
        switch (model.get('orderType')) {
          case 'QT':
            if (!OB.MobileApp.model.hasPermission('OBPOS_retail.quotations')) {
              openReceiptPermissionError(OB.I18N.getLabel('OBPOS_Quotations'));
              return;
            }
            break;
          case 'LAY':
            if (!OB.MobileApp.model.hasPermission('OBPOS_retail.layaways')) {
              openReceiptPermissionError(OB.I18N.getLabel('OBPOS_LblLayaways'));
              return;
            }
            break;
          default:
            if (
              !OB.MobileApp.model.hasPermission('OBPOS_retail.paidReceipts')
            ) {
              openReceiptPermissionError(
                OB.I18N.getLabel('OBPOS_LblPaidReceipts')
              );
              return;
            }
            break;
        }

        var orderTypeMsg,
          i,
          showErrorMessage = function(errorMsg) {
            if (calledFrom === 'orderSelector' || calledFrom === 'return') {
              OB.POS.terminal.$.containerWindow.getRoot().doShowPopup({
                popup: 'OB_UI_MessageDialog',
                args: {
                  message: errorMsg
                }
              });
            } else {
              OB.UTIL.showWarning(errorMsg);
            }
            if (errorCallback) {
              errorCallback();
            }
          };

        // Check in Current Session
        for (i = 0; i < this.length; i++) {
          if (
            this.at(i).get('id') === model.get('id') ||
            (!_.isNull(this.at(i).get('oldId')) &&
              this.at(i).get('oldId') === model.get('id')) ||
            (this.at(i).get('canceledorder') &&
              this.at(i)
                .get('canceledorder')
                .get('id') === model.get('id'))
          ) {
            var errorMsg;
            orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
            errorMsg = enyo.format(
              OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
              orderTypeMsg,
              this.at(i).get('documentNo')
            );
            if (this.at(i).get('isLayaway')) {
              orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
              errorMsg = enyo.format(
                OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
                orderTypeMsg,
                this.at(i).get('documentNo')
              );
            } else if (
              OB.MobileApp.model.orderList.models[i].get('isQuotation')
            ) {
              orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
              errorMsg = enyo.format(
                OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
                orderTypeMsg,
                this.at(i).get('documentNo')
              );
            } else if (
              !_.isNull(this.at(i).get('oldId')) &&
              this.at(i).get('oldId') === model.get('id')
            ) {
              var SoFromQtDocNo = this.at(i).get('documentNo');
              var QtDocumentNo = model.get('documentNo');
              errorMsg = OB.I18N.getLabel(
                'OBPOS_OrderAssociatedToQuotationInProgress',
                [QtDocumentNo, SoFromQtDocNo, QtDocumentNo, SoFromQtDocNo]
              );
            }
            showErrorMessage(errorMsg);
            if (
              OB.MobileApp.model.receipt.get('documentNo') !==
              model.get('documentNo')
            ) {
              this.load(this.at(i));
            }
            if (model.get('searchSynchId')) {
              model.unset('searchSynchId');
            }
            return true;
          }
        }

        // Check in Other Session
        OB.Dal.find(
          OB.Model.Order,
          {
            hasbeenpaid: 'N'
          },
          function(ordersNotProcessed) {
            if (ordersNotProcessed.length > 0) {
              var existingOrder = _.find(ordersNotProcessed.models, function(
                order
              ) {
                return (
                  order.get('id') === model.get('id') ||
                  order.get('oldId') === model.get('id') ||
                  (order.get('canceledorder') &&
                    order.get('canceledorder').get('id') === model.get('id'))
                );
              });
              if (existingOrder) {
                var orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
                if (existingOrder.get('isLayaway')) {
                  orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
                } else if (existingOrder.get('isQuotation')) {
                  orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
                }
                // Getting Other Session User's username
                OB.Dal.find(
                  OB.Model.Session,
                  {
                    id: existingOrder.get('session')
                  },
                  function(sessions) {
                    if (sessions.length > 0) {
                      OB.Dal.find(
                        OB.Model.User,
                        {
                          id: sessions.models[0].get('user')
                        },
                        function(users) {
                          if (users.length > 0) {
                            OB.UTIL.showConfirmation.display(
                              enyo.format(
                                OB.I18N.getLabel(
                                  'OBPOS_ticketAlreadyOpenedInSession'
                                ),
                                orderTypeMsg,
                                existingOrder.get('documentNo'),
                                users.models[0].get('name')
                              ),
                              enyo.format(
                                OB.I18N.getLabel(
                                  'OBPOS_MsgConfirmSaveInCurrentSession'
                                ),
                                users.models[0].get('name')
                              ),
                              [
                                {
                                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                                  action: function() {
                                    OB.Dal.remove(
                                      existingOrder,
                                      function() {
                                        callback(model);
                                      },
                                      OB.UTIL.showError
                                    );
                                  }
                                },
                                {
                                  label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                                  action: function() {
                                    if (errorCallback) {
                                      errorCallback();
                                    }
                                  }
                                }
                              ],
                              {
                                onHideFunction: function(dialog) {
                                  return true;
                                }
                              }
                            );
                          }
                        }
                      );
                    }
                  }
                );
              } else {
                return callback(model);
              }
            } else {
              return callback(model);
            }
          }
        );
      }
    },
    {
      newOrder: function(bp, propertiesToReset) {
        var order = new Order(),
          i;
        bp = bp ? bp : OB.MobileApp.model.get('businessPartner');

        if (propertiesToReset && _.isArray(propertiesToReset)) {
          for (i = 0; i < propertiesToReset.length; i++) {
            if (!OB.UTIL.isNullOrUndefined(propertiesToReset[i].defaultValue)) {
              order.set(
                propertiesToReset[i].propertyName,
                propertiesToReset[i].defaultValue
              );
            } else {
              order.set(propertiesToReset[i].propertyName, '');
            }
          }
        }

        order.set('client', OB.MobileApp.model.get('terminal').client);
        order.set(
          'organization',
          OB.MobileApp.model.get('terminal').organization
        );
        order.set(
          'organizationAddressIdentifier',
          OB.MobileApp.model.get('terminal').organizationAddressIdentifier
        );
        order.set(
          'trxOrganization',
          OB.MobileApp.model.get('terminal').organization
        );
        order.set('createdBy', OB.MobileApp.model.get('orgUserId'));
        order.set('updatedBy', OB.MobileApp.model.get('orgUserId'));
        order.set(
          'documentType',
          OB.MobileApp.model.get('terminal').terminalType.documentType
        );
        order.set(
          'orderType',
          OB.MobileApp.model.get('terminal').terminalType.layawayorder ? 2 : 0
        ); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
        order.set('generateInvoice', false);
        order.set('isQuotation', false);
        order.set('oldId', null);
        order.set('session', OB.MobileApp.model.get('session'));
        order.set('cashVAT', OB.MobileApp.model.get('terminal').cashVat);
        order.set('bp', bp);
        order.set('invoiceTerms', bp.get('invoiceTerms'));
        if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
          // Set price list for order
          order.set('priceList', bp.get('priceList'));
          var priceIncludesTax = bp.get('priceIncludesTax');
          if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
            priceIncludesTax = OB.MobileApp.model.get('pricelist')
              .priceIncludesTax;
          }
          order.set('priceIncludesTax', priceIncludesTax);
        } else {
          order.set('priceList', OB.MobileApp.model.get('terminal').priceList);
          order.set(
            'priceIncludesTax',
            OB.MobileApp.model.get('pricelist').priceIncludesTax
          );
        }
        if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_retail.restricttaxidinvoice',
              true
            ) &&
            !bp.get('taxID')
          ) {
            if (
              OB.MobileApp.model.get('terminal').terminalType.generateInvoice
            ) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
            } else {
              OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
            }
            order.set('generateInvoice', false);
          } else {
            order.set(
              'generateInvoice',
              OB.MobileApp.model.get('terminal').terminalType.generateInvoice
            );
          }
        } else {
          order.set('generateInvoice', false);
        }
        order.set('currency', OB.MobileApp.model.get('terminal').currency);
        order.set(
          'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
          OB.MobileApp.model.get('terminal')[
            'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER
          ]
        );
        order.set('warehouse', OB.MobileApp.model.get('terminal').warehouse);
        if (OB.MobileApp.model.get('context').user.isSalesRepresentative) {
          order.set(
            'salesRepresentative',
            OB.MobileApp.model.get('context').user.id
          );
          order.set(
            'salesRepresentative' +
              OB.Constants.FIELDSEPARATOR +
              OB.Constants.IDENTIFIER,
            OB.MobileApp.model.get('context').user._identifier
          );
        } else {
          order.set('salesRepresentative', null);
          order.set(
            'salesRepresentative' +
              OB.Constants.FIELDSEPARATOR +
              OB.Constants.IDENTIFIER,
            null
          );
        }
        order.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        order.set(
          'posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
          OB.MobileApp.model.get('terminal')._identifier
        );
        order.set('orderDate', OB.I18N.normalizeDate(new Date()));
        order.set('creationDate', null);
        order.set('isPaid', false);
        order.set('creditAmount', OB.DEC.Zero);
        order.set('paidPartiallyOnCredit', false);
        order.set('paidOnCredit', false);
        order.set('isLayaway', false);
        order.set('isPartiallyDelivered', false);
        order.set('taxes', {});

        var nextDocumentno = OB.MobileApp.model.getNextDocumentno();
        order.set(
          'documentnoPrefix',
          OB.MobileApp.model.get('terminal').docNoPrefix
        );
        order.set('documentnoSuffix', nextDocumentno.documentnoSuffix);
        order.set('documentNo', nextDocumentno.documentNo);
        order.set('print', true);
        order.set('sendEmail', false);
        order.set('openDrawer', false);
        order.set(
          'orderManualPromotions',
          new OB.Collection.OrderManualPromotionsList()
        );
        OB.UTIL.HookManager.executeHooks('OBPOS_NewReceipt', {
          newOrder: order
        });
        return order;
      }
    }
  );

  var MultiOrders = Backbone.Model.extend({
    modelName: 'MultiOrders',
    defaults: {
      total: OB.DEC.Zero,
      payment: OB.DEC.Zero,
      pending: OB.DEC.Zero,
      change: OB.DEC.Zero,
      openDrawer: false,
      additionalInfo: null,
      obposPrepaymentamt: OB.DEC.Zero
    },
    initialize: function() {
      this.set('multiOrdersList', new Backbone.Collection());
      this.set('payments', new Backbone.Collection());
      // ISSUE 24487: Callbacks of this collection still exists if you come back from other page.
      // Force to remove callbacks
      this.get('multiOrdersList').off();
    },
    getPaymentStatus: function() {
      var total = OB.DEC.abs(this.getTotal()),
        pay = this.getPayment();
      return {
        total: OB.I18N.formatCurrency(total),
        pending:
          OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0
            ? OB.I18N.formatCurrency(OB.DEC.Zero)
            : OB.I18N.formatCurrency(OB.DEC.sub(total, pay)),
        overpayment:
          OB.DEC.compare(OB.DEC.sub(pay, total)) > 0
            ? OB.DEC.sub(pay, total)
            : null,
        isReturn: this.get('gross') < 0 ? true : false,
        isNegative: this.get('gross') < 0 ? true : false,
        totalAmt: total,
        pendingAmt:
          OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0
            ? OB.DEC.Zero
            : OB.DEC.sub(total, pay),
        payments: this.get('payments')
      };
    },
    getPrecision: function(payment) {
      var terminalpayment =
        OB.MobileApp.model.paymentnames[payment.get('kind')];
      return terminalpayment
        ? terminalpayment.obposPosprecision
        : OB.DEC.getScale();
    },
    getSumOfOrigAmounts: function(paymentToIgnore) {
      //returns a result with the sum up of every payments based on origAmount field
      //if paymentToIignore parameter is provided the result will exclude that payment
      var payments = this.get('payments');
      var sumOfPayments = OB.DEC.Zero;
      if (payments && payments.length > 0) {
        sumOfPayments = _.reduce(
          payments.models,
          function(memo, pymnt, index) {
            if (
              paymentToIgnore &&
              pymnt.get('kind') === paymentToIgnore.get('kind')
            ) {
              return OB.DEC.add(memo, OB.DEC.Zero);
            } else {
              return OB.DEC.add(memo, pymnt.get('origAmount'));
            }
          },
          OB.DEC.Zero
        );
        return sumOfPayments;
      } else {
        return sumOfPayments;
      }
    },
    getDifferenceBetweenPaymentsAndTotal: function(paymentToIgnore) {
      //Returns the difference (abs) between total to pay and payments.
      //if paymentToIignore parameter is provided the result will exclude that payment.
      return OB.DEC.abs(
        OB.DEC.sub(
          OB.DEC.abs(this.getTotal()),
          OB.DEC.sub(
            this.getSumOfOrigAmounts(paymentToIgnore),
            this.getChange()
          )
        )
      );
    },
    getDifferenceRemovingSpecificPayment: function(currentPayment) {
      //Returns the difference (abs) between total to pay and payments without take into account currentPayment
      //Result is returned in the currency used by current payment
      var differenceInDefaultCurrency;
      var differenceInForeingCurrency;
      var p = this.getPrecision(currentPayment);
      differenceInDefaultCurrency = this.getDifferenceBetweenPaymentsAndTotal(
        currentPayment
      );
      if (currentPayment && currentPayment.get('rate')) {
        differenceInForeingCurrency = OB.DEC.div(
          differenceInDefaultCurrency,
          currentPayment.get('rate'),
          p
        );
        return differenceInForeingCurrency;
      } else {
        return differenceInDefaultCurrency;
      }
    },
    adjustPayment: function() {
      var i,
        max,
        p,
        pcash,
        precision,
        multiCurrencyDifference,
        payments = this.get('payments'),
        total = this.get('prepaymentChangeMode')
          ? this.get('obposPrepaymentamt')
          : OB.DEC.abs(this.getTotal()),
        paidCash = OB.DEC.Zero,
        defaultCash = OB.DEC.Zero,
        nonDefaultCash = OB.DEC.Zero,
        noCash = OB.DEC.Zero;

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        precision = this.getPrecision(p);
        if (p.get('rate') && p.get('rate') !== '1') {
          p.set('origAmount', OB.DEC.div(p.get('amount'), p.get('mulrate')));
          //Here we are trying to know if the current payment is making the pending to pay 0.
          //to know that we are suming up every payments except the current one (getSumOfOrigAmounts)
          //then we substract this amount from the total (getDifferenceBetweenPaymentsAndTotal)
          //and finally we transform this difference to the foreign amount
          //if the payment in the foreign amount makes pending to pay zero, then we will ensure that the payment
          //in the default currency is satisfied
          if (
            OB.DEC.compare(
              OB.DEC.sub(
                this.getDifferenceRemovingSpecificPayment(p),
                OB.DEC.abs(p.get('amount'), precision),
                precision
              )
            ) === OB.DEC.Zero
          ) {
            multiCurrencyDifference = this.getDifferenceBetweenPaymentsAndTotal(
              p
            );
            if (p.get('origAmount') !== multiCurrencyDifference) {
              p.set(
                'origAmount',
                p.get('changePayment')
                  ? OB.DEC.mul(multiCurrencyDifference, -1)
                  : multiCurrencyDifference
              );
            }
          }
        } else {
          p.set('origAmount', p.get('amount'));
        }
        p.set('paid', p.get('origAmount'));
        if (p.get('kind') === OB.MobileApp.model.get('paymentcash')) {
          // The default cash method
          paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
          defaultCash = OB.DEC.add(defaultCash, p.get('origAmount'));
          pcash = p;
        } else if (
          OB.MobileApp.model.hasPayment(p.get('kind')) &&
          OB.MobileApp.model.hasPayment(p.get('kind')).paymentMethod.iscash
        ) {
          // Another cash method
          paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
          nonDefaultCash = OB.DEC.add(nonDefaultCash, p.get('origAmount'));
          if (!pcash) {
            pcash = p;
          }
        } else {
          noCash = OB.DEC.add(noCash, p.get('origAmount'));
        }
      }

      // Calculation of the change....
      //FIXME
      noCash = OB.DEC.abs(noCash);
      paidCash = OB.DEC.abs(paidCash);
      if (pcash) {
        if (OB.DEC.compare(noCash - total) > 0) {
          pcash.set('paid', OB.DEC.Zero);
          this.set('payment', noCash);
          this.set('change', OB.DEC.abs(paidCash));
        } else if (
          OB.DEC.compare(OB.DEC.sub(OB.DEC.add(noCash, paidCash), total)) > 0
        ) {
          pcash.set(
            'paid',
            OB.DEC.abs(
              OB.DEC.sub(
                total,
                OB.DEC.add(
                  noCash,
                  OB.DEC.sub(paidCash, pcash.get('origAmount'))
                )
              )
            )
          );
          this.set('payment', OB.DEC.abs(total));
          this.set('change', OB.DEC.sub(OB.DEC.add(noCash, paidCash), total));
        } else {
          pcash.set(
            'paid',
            pcash.get('kind') === OB.MobileApp.model.get('paymentcash')
              ? defaultCash
              : nonDefaultCash
          );
          this.set('payment', OB.DEC.add(noCash, paidCash));
          this.set('change', OB.DEC.Zero);
        }
      } else {
        if (payments.length > 0) {
          this.set('payment', OB.DEC.add(noCash, paidCash));
        } else {
          this.set('payment', OB.DEC.Zero);
        }
        this.set('change', OB.DEC.Zero);
      }
    },
    addPayment: function(payment, callback) {
      var me = this,
        i,
        max,
        p,
        order,
        payments,
        finalCallback,
        precision;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }
      if (this.stopAddingPayments) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
        return;
      }

      if (
        !payment.get('isReversePayment') &&
        this.getPending() <= 0 &&
        payment.get('amount') > 0
      ) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_PaymentsExact'));
        return;
      }

      finalCallback = function() {
        if (callback instanceof Function) {
          callback(order);
        }
      };

      payments = this.get('payments');
      precision = this.getPrecision(payment);
      payment.set('amount', OB.DEC.toNumber(payment.get('amount'), precision));
      order = this;
      if (this.get('prepaymentChangeMode')) {
        this.unset('prepaymentChangeMode');
        this.adjustPayment();
      }
      OB.UTIL.PrepaymentUtils.managePrepaymentChange(
        this,
        payment,
        payments,
        function() {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_preAddPayment',
            {
              paymentToAdd: payment,
              payments: payments,
              receipt: me
            },
            function(args) {
              var executeFinalCallback = function() {
                OB.UTIL.HookManager.executeHooks(
                  'OBPOS_postAddPayment',
                  {
                    paymentAdded: payment,
                    payments: payments,
                    receipt: order
                  },
                  function(args2) {
                    finalCallback();
                  }
                );
              };

              if (args && args.cancellation) {
                executeFinalCallback();
                return;
              }

              if (
                !payment.get('paymentData') ||
                payment.get('paymentData').mergeable
              ) {
                // search for an existing payment only if there is not paymentData info.
                // this avoids to merge for example card payments of different cards.
                for (i = 0, max = payments.length; i < max; i++) {
                  p = payments.at(i);
                  if (
                    p.get('kind') === payment.get('kind') &&
                    !p.get('isPrePayment')
                  ) {
                    p.set(
                      'amount',
                      OB.DEC.add(
                        payment.get('amount'),
                        p.get('amount'),
                        precision
                      )
                    );
                    if (p.get('rate') && p.get('rate') !== '1') {
                      p.set(
                        'origAmount',
                        OB.DEC.div(p.get('amount'), p.get('mulrate'))
                      );
                    } else {
                      p.set('origAmount', p.get('amount'));
                    }
                    payment.set('date', new Date());
                    order.adjustPayment();
                    order.trigger('displayTotal');
                    executeFinalCallback();
                    return;
                  }
                }
              } else {
                for (i = 0, max = payments.length; i < max; i++) {
                  p = payments.at(i);
                  if (
                    p.get('kind') === payment.get('kind') &&
                    p.get('paymentData') &&
                    payment.get('paymentData') &&
                    p.get('paymentData').groupingCriteria &&
                    payment.get('paymentData').groupingCriteria &&
                    p.get('paymentData').groupingCriteria ===
                      payment.get('paymentData').groupingCriteria
                  ) {
                    p.set(
                      'amount',
                      OB.DEC.add(
                        payment.get('amount'),
                        p.get('amount'),
                        precision
                      )
                    );
                    if (p.get('rate') && p.get('rate') !== '1') {
                      p.set(
                        'origAmount',
                        OB.DEC.div(p.get('amount'), p.get('mulrate'))
                      );
                    }
                    payment.set('date', new Date());
                    order.adjustPayment();
                    order.trigger('displayTotal');
                    executeFinalCallback();
                    return;
                  }
                }
              }
              if (
                payment.get('openDrawer') &&
                (payment.get('allowOpenDrawer') || payment.get('isCash'))
              ) {
                order.set('openDrawer', payment.get('openDrawer'));
              }
              payment.set('date', new Date());
              payment.set('isReturnOrder', false);
              payment.set('id', OB.UTIL.get_UUID());
              if (payment.has('paymentRoundingLine')) {
                payment
                  .get('paymentRoundingLine')
                  .set('roundedPaymentId', payment.get('id'));
              }
              payments.add(payment);
              order.adjustPayment();
              order.trigger('displayTotal');
              executeFinalCallback();
              return;
            }
          );
        }
      );
    },
    removePayment: function(payment, cancellationCallback, removeCallback) {
      var me = this,
        payments = this.get('payments');
      var finalCallback = function() {
        if (removeCallback) {
          removeCallback();
        }
      };
      _.each(this.get('multiOrdersList').models, function(ord) {
        if (ord.get('isBeingClosed')) {
          var error = new Error();
          OB.error('The receipt is being save, you cannot remove payments.');
          OB.error('The stack trace is: ' + error.stack);
          return;
        }
      });
      if (this.get('prepaymentChangeMode')) {
        this.unset('prepaymentChangeMode');
        this.adjustPayment();
      }
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_preRemovePaymentMultiOrder',
        {
          paymentToRem: payment,
          payments: payments,
          multiOrdersList: this.get('multiOrdersList')
        },
        function(args) {
          if (args.cancellation) {
            if (cancellationCallback) {
              cancellationCallback();
            }
            return true;
          }
          args.payments.remove(args.paymentToRem);
          if (args.paymentToRem.get('openDrawer')) {
            me.set('openDrawer', false);
          }
          me.adjustPayment();
          if (finalCallback) {
            finalCallback();
          }
        }
      );
    },
    printGross: function() {
      return OB.I18N.formatCurrency(this.getTotal());
    },
    getTotal: function() {
      return this.get('total');
    },
    getGross: function() {
      return this.getTotal();
    },
    getChange: function() {
      return this.get('change');
    },
    getPayment: function() {
      return this.get('payment');
    },
    getPaymentWithSign: function() {
      return this.getPayment();
    },
    getPending: function() {
      var pending;
      if (this.get('prepaymentChangeMode')) {
        var paymentsAmt = _.reduce(
          this.get('payments').models,
          function(memo, payment) {
            return OB.DEC.add(memo, payment.get('origAmount'));
          },
          OB.DEC.Zero
        );
        pending = OB.DEC.sub(this.getTotal(), paymentsAmt);
      } else {
        pending = OB.DEC.sub(this.getTotal(), this.getPayment());
      }
      return pending;
    },
    isNegative: function() {
      return false;
    },
    isFullyPaid: function() {
      return this.getPayment() >= this.getTotal();
    },
    toInvoice: function(status) {
      if (status === false) {
        this.unset('additionalInfo');
        _.each(
          this.get('multiOrdersList').models,
          function(order) {
            order.unset('generateInvoice');
          },
          this
        );
        return;
      }
      this.set('additionalInfo', 'I');
      _.each(
        this.get('multiOrdersList').models,
        function(order) {
          order.set('generateInvoice', true);
        },
        this
      );
    },
    resetValues: function() {
      //this.set('isMultiOrders', false);
      this.get('multiOrdersList').reset();
      this.set('total', OB.DEC.Zero);
      this.set('payment', OB.DEC.Zero);
      this.set('pending', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.get('payments').reset();
      this.set('openDrawer', false);
      this.set('additionalInfo', null);
      OB.MobileApp.model.set('isMultiOrderState', false);
      OB.UTIL.localStorage.removeItem('multiOrdersPayment');
    },
    checkMultiOrderPayment: function() {
      if (this.get('payments').length > 0) {
        OB.UTIL.showConfirmation.display(
          '',
          OB.I18N.getLabel('OBPOS_RemoveReceiptWithPayment')
        );
        return true;
      }
      return false;
    },
    hasDataInList: function() {
      if (
        this.get('multiOrdersList') &&
        this.get('multiOrdersList').length > 0
      ) {
        return true;
      }
      return false;
    }
  });
  var TaxLine = Backbone.Model.extend();
  OB.Data.Registry.registerModel(OrderLine);
  OB.Data.Registry.registerModel(PaymentLine);

  OB.Collection.OrderLineList = OB.Collection.OrderLineList.extend({
    isProductPresent: function(product) {
      var result = null;
      if (this.length > 0) {
        result = _.find(
          this.models,
          function(line) {
            if (line.get('product').get('id') === product.get('id')) {
              return true;
            }
          },
          this
        );
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

  OB.Collection.OrderManualPromotionsList = Backbone.Collection.extend({
    model: Backbone.Model.extend({
      defaults: {
        discountRule: null,
        rule: null
      },
      initialize: function(attributes) {
        if (attributes && attributes.discountRule) {
          this.set('discountRule', new Backbone.Model(attributes.discountRule));
        }
      }
    })
  });

  OB.Data.PackDiscount = {};
  OB.Data.PackDiscount['BE5D42E554644B6AA262CCB097753951'] = {
    addPackToOrder: function(order, pack, attrs) {
      try {
        const discount = OB.Discounts.Pos.ruleImpls.find(
          discount => discount.id === pack.get('id')
        );

        if (discount.endingDate && discount.endingDate.length > 0) {
          var objDate = new Date(discount.endingDate);
          var now = new Date();
          var nowWithoutTime = new Date(now.toISOString().split('T')[0]);
          if (nowWithoutTime > objDate) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_PackExpired_header'),
              OB.I18N.getLabel('OBPOS_PackExpired_body', [
                discount._identifier,
                objDate.toLocaleDateString()
              ])
            );
            return;
          }
        }

        var addProductsAndCalculateDiscounts = async function(
          products,
          index,
          callback,
          errorCallback
        ) {
          if (index === products.length) {
            return callback();
          }
          try {
            const productResult = await OB.App.MasterdataModels.Product.withId(
              products[index].product.id
            );
            let product = OB.Dal.transform(OB.Model.Product, productResult);
            if (product) {
              order.addProduct(
                product,
                products[index].obdiscQty,
                {
                  belongsToPack: true,
                  blockAddProduct: true
                },
                attrs,
                function() {
                  addProductsAndCalculateDiscounts(
                    products,
                    index + 1,
                    callback,
                    errorCallback
                  );
                }
              );
            }
          } catch (error) {
            errorCallback();
          }
        };
        var errorCallback = function(error) {
          OB.error('OBDAL error: ' + error, arguments);
        };
        order.set('skipApplyPromotions', true);
        addProductsAndCalculateDiscounts(
          discount.products,
          0,
          function() {
            order.set('skipApplyPromotions', false);
            order.calculateReceipt();
          },
          errorCallback
        );
      } finally {
        /* continue regardless of error */
      }
    }
  };

  // order model is not registered using standard Registry method because list is
  // because collection is specific
  window.OB.Model.Order = Order;
  window.OB.Collection.OrderList = OrderList;
  window.OB.Model.TaxLine = TaxLine;
  window.OB.Model.MultiOrders = MultiOrders;

  window.OB.Model.modelLoaders = [];
})();
