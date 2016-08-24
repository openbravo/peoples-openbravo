/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, moment, Backbone, enyo, BigDecimal, localStorage */

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

    // When copying a line coming from servers these properties are copied manually
    // the rest are considered extra information coming from modules and are copied verbatim.
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
      linegrossamount: true,
      grossListPrice: true,
      description: true,
      promotions: true,
      shipmentlines: true,
      relatedLines: true,
      hasRelatedServices: true,
      warehouse: true,
      warehousename: true
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
        this.set('description', attributes.description);
        if (!attributes.grossListPrice && attributes.product && _.isNumber(attributes.priceList)) {
          this.set('grossListPrice', attributes.priceList);
        }
        if (attributes.relatedLines && _.isArray(attributes.relatedLines)) {
          this.set('relatedLines', attributes.relatedLines);
        }
        if (!OB.UTIL.isNullOrUndefined(attributes.hasRelatedServices)) {
          this.set('hasRelatedServices', attributes.hasRelatedServices);
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

    isPrintableService: function () {
      var product = this.get('product');
      if (product.get('productType') === 'S' && !product.get('isPrintServices')) {
        return false;
      }

      return true;
    },

    getDiscount: function () {
      return this.getTotalAmountOfPromotions();
    },

    printDiscount: function () {
      return OB.I18N.formatCurrency(this.getDiscount());
    },

    // returns the discount to substract in total
    discountInTotal: function () {
      var disc = OB.DEC.mul(OB.DEC.sub(this.get('product').get('standardPrice'), this.get('price')), this.get('qty'));
      // if there is a discount no promotion then total is price*qty
      // otherwise total is price*qty - discount
      if (OB.DEC.compare(disc) === 0) {
        return this.getTotalAmountOfPromotions();
      } else {
        return 0;
      }
    },

    calculateGross: function () {
      // calculate the total amount depending on the tax plan
      // setting the oposite variable to null, ensures that other logic is not using them in a wrong way
      if (this.get('priceIncludesTax')) {
        this.set('net', null, {
          silent: true
        });
        this.set('gross', OB.DEC.mul(this.get('qty'), this.get('price')));
      } else {
        this.set('gross', null, {
          silent: true
        });
        this.set('net', OB.DEC.mul(this.get('qty'), this.get('price')));
      }
    },

    getGross: function () {
      return this.get('gross');
    },

    getTotalLine: function () {
      if (!OB.UTIL.isNullOrUndefined(this.get('price'))) {
        return (OB.DEC.mul(this.get('price'), this.get('qty'))) - this.getDiscount();
      }
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

    printTotalLine: function () {
      return OB.I18N.formatCurrency(this.getTotalLine());
    },

    getTotalAmountOfPromotions: function () {
      var memo = 0;
      if (this.get('promotions') && this.get('promotions').length > 0) {
        return _.reduce(this.get('promotions'), function (memo, prom) {
          if (OB.UTIL.isNullOrUndefined(prom.amt)) {
            return memo;
          }
          return memo + prom.amt;
        }, memo, this);
      } else {
        return 0;
      }
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
      if (promotions) {
        if (OB.MobileApp.model.get('terminal').bestDealCase && promotions.length > 0) {
          // best deal case can only apply one promotion per line
          return true;
        }
        for (i = 0; i < promotions.length; i++) {
          if (!promotions[i].applyNext) {
            return true;
          }
        }
      }
      return false;
    },

    lastAppliedPromotion: function () {
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

    isReturnable: function () {
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
        return OB.I18N.formatCurrency(OB.DEC.div(this.get('amount'), this.get('mulrate')));
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
    includeDocNoSeperator: true,
    modelName: 'Order',
    tableName: 'c_order',
    entityName: 'Order',
    source: '',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
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
        this.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('session', attributes.session);
        this.set('warehouse', attributes.warehouse);
        this.set('salesRepresentative', attributes.salesRepresentative);
        this.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('posTerminal', attributes.posTerminal);
        this.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, attributes['posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
        this.set('orderDate', OB.I18N.normalizeDate(attributes.orderDate));
        this.set('creationDate', OB.I18N.normalizeDate(attributes.creationDate));
        this.set('documentnoPrefix', attributes.documentnoPrefix);
        this.set('quotationnoPrefix', attributes.quotationnoPrefix);
        this.set('returnnoPrefix', attributes.returnnoPrefix);
        this.set('documentnoSuffix', attributes.documentnoSuffix);
        this.set('quotationnoSuffix', attributes.quotationnoSuffix);
        this.set('returnnoSuffix', attributes.returnnoSuffix);
        this.set('documentNo', attributes.documentNo);
        this.setUndo('InitializeAttr', attributes.undo);
        bpModel = new OB.Model.BusinessPartner(attributes.bp);
        bpModel.set('locationModel', new OB.Model.BPLocation(attributes.bp.locationModel));
        this.set('bp', bpModel);
        this.set('lines', new OrderLineList().reset(attributes.lines));
        this.set('payments', new PaymentLineList().reset(attributes.payments));
        this.set('payment', attributes.payment);
        this.set('change', attributes.change);
        this.set('qty', attributes.qty);
        this.set('gross', attributes.gross);
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
        this.set('isBeingDiscounted', false);
        this.set('reApplyDiscounts', false);
        this.set('calculateReceiptCallbacks', []);
        this.set('loaded', attributes.loaded);
        this.set('updated', attributes.updated);
        _.each(_.keys(attributes), function (key) {
          if (!this.has(key)) {
            this.set(key, attributes[key]);
          }
        }, this);

      } else {
        this.clearOrderAttributes();
      }
    },

    save: function (callback) {
      var undoCopy = this.get('undo'),
          me = this,
          previousOrder;

      var now = new Date();
      this.set('updated', OB.I18N.normalizeDate(now));
      this.set('timezoneOffset', now.getTimezoneOffset());
      this.set('json', JSON.stringify(this.serializeToJSON()));
      if (callback === undefined || !callback instanceof Function) {
        callback = function () {};
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
        previousOrder = _.max(_.filter(OB.MobileApp.model.orderList.models, function (previousOrder) {
          return previousOrder.get('documentnoSuffix') < me.get('documentnoSuffix') && previousOrder.get('session') === OB.MobileApp.model.get('session');
        }), function (maxDocumentNoOrder) {
          return maxDocumentNoOrder.get('documentnoSuffix');
        });
        if (previousOrder) {
          previousOrder.save();
        }
      }
      if (!OB.MobileApp.model.get('preventOrderSave')) {
        OB.Dal.save(this, function () {
          if (callback) {
            callback();
          }
        }, function () {
          OB.error(arguments);
        });
      } else {
        if (callback) {
          callback();
        }
      }
      this.setUndo('SaveOrder', undoCopy);
    },

    calculateTaxes: function (callback) {
      var me = this;
      OB.DATA.OrderTaxes(me);
      me.calculateTaxes(callback);
    },
    prepareToSend: function (callback) {
      this.adjustPrices();
      if (!OB.UTIL.isNullOrUndefined(callback)) {
        callback(this);
      }
    },

    adjustPrices: function () {
      // Apply calculated discounts and promotions to price and gross prices
      // so ERP saves them in the proper place
      this.get('lines').each(function (line) {
        var price = line.get('price'),
            gross = line.get('gross'),
            totalDiscount = 0,
            grossListPrice = line.get('priceList'),
            grossUnitPrice, discountPercentage, base;

        // Calculate inline discount: discount applied before promotions
        if ((line.get('product').get('standardPrice') && line.get('product').get('standardPrice') !== price) || (_.isNumber(line.get('discountedLinePrice')) && line.get('discountedLinePrice') !== line.get('product').get('standardPrice'))) {
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
          discount.unitDiscount = OB.DEC.div(discountAmt, line.get('qtyToApplyDisc') || line.get('qty'));
          totalDiscount = OB.DEC.add(totalDiscount, discountAmt);
          base = OB.DEC.sub(base, totalDiscount);
        }, this);

        gross = OB.DEC.sub(gross, totalDiscount);
        price = OB.DEC.div(gross, line.get('qty'));

        if (grossListPrice === undefined) {
          grossListPrice = price;
        }

        if (this.get('priceIncludesTax')) {
          line.set({
            net: OB.UTIL.getFirstValidValue([OB.DEC.toNumber(line.get('discountedNet')), line.get('net'), OB.DEC.div(gross, line.get('linerate'))]),
            pricenet: line.get('discountedNet') ? OB.DEC.div(line.get('discountedNet'), line.get('qty')) : OB.DEC.div(OB.DEC.div(gross, line.get('linerate')), line.get('qty')),
            listPrice: OB.DEC.div(grossListPrice, line.get('linerate')),
            standardPrice: OB.DEC.div((grossListPrice || price), line.get('linerate')),
            grossListPrice: grossListPrice,
            grossUnitPrice: price,
            lineGrossAmount: gross
          }, {
            silent: true
          });
        } else {
          line.set({
            nondiscountedprice: line.get('price'),
            nondiscountednet: line.get('net'),
            standardPrice: line.get('price'),
            net: line.get('discountedNet'),
            pricenet: OB.DEC.toNumber(line.get('discountedNetPrice')),
            listPrice: line.get('priceList'),
            grossListPrice: 0,
            lineGrossAmount: 0
          }, {
            silent: true
          });
        }
      }, this);
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

    getLinesByProduct: function (productId) {
      var affectedLines;
      if (this.get('lines') && this.get('lines').length > 0) {
        affectedLines = _.filter(this.get('lines').models, function (line) {
          return line.get('product').id === productId;
        });
      }
      return affectedLines ? affectedLines : null;
    },

    isCalculateGrossLocked: null,

    isCalculateReceiptLocked: null,

    setIsCalculateGrossLockState: function (state) {
      this.isCalculateGrossLocked = state;
    },

    setIsCalculateReceiptLockState: function (state) {
      this.isCalculateReceiptLocked = state;
    },

    calculateGross: function () {
      // check if it's all ok and calculateGross is being called from where it's supposed to
      var stack = OB.UTIL.getStackTrace('Backbone.Model.extend.calculateGross', false);
      if (stack.indexOf('Backbone.Model.extend.calculateGross') > -1 && stack.indexOf('Backbone.Model.extend.calculateReceipt') > -1) {
        OB.error("It's forbidden to use calculateGross from outside of calculateReceipt");
      }

      // verify that the calculateGross is not locked
      if (this.isCalculateGrossLocked === true) {
        OB.error("calculateGross execution is forbidden right now");
        return;
      } else if (this.isCalculateGrossLocked !== false && !this.get('belongsToMultiOrder')) {
        OB.error("setting the isCalculateGrossLocked state is mandatory before executing it the first time");
      }

      // verify that there is no other calculatingGross running
      if (this.calculatingGross) {
        this.pendingCalculateGross = true;
        return;
      }

      // verify that the ui receipt is the only one in which calculateGross is executed
      var isTheUIReceipt = this === OB.MobileApp.model.receipt || this.get('belongsToMultiOrder');
      if (!isTheUIReceipt) {
        OB.error("calculateGross should only be called by the UI receipt");
      }

      this.calculatingGross = true;
      var me = this;
      // reset some vital receipt values because, at this point, they are obsolete. do not fire the change event
      me.set({
        'net': OB.DEC.Zero,
        'gross': OB.DEC.Zero,
        'taxes': null,
        'qty': OB.DEC.Zero
      }, {
        silent: true
      });
      var saveAndTriggerEvents = function (gross) {
          var now = new Date();
          me.set('loaded', OB.I18N.normalizeDate(now));
          me.set('timezoneOffset', now.getTimezoneOffset());
          var net = me.get('lines').reduce(function (memo, e) {
            var netLine = e.get('discountedNet');
            if (netLine) {
              return OB.DEC.add(memo, netLine);
            } else {
              return memo;
            }
          }, OB.DEC.Zero);
          //total qty
          var qty = me.get('lines').reduce(function (memo, e) {
            var qtyLine = e.getQty();
            if (qtyLine > 0) {
              return OB.DEC.add(memo, qtyLine, OB.I18N.qtyScale());
            } else {
              return memo;
            }
          }, OB.DEC.Zero);

          // all attributes are set at once, preventing the change event of each attribute to be fired until all values are set
          me.set({
            'gross': gross,
            'qty': qty
          });

          me.adjustPayment();
          me.save(function () {
            // Reset the flag that protects reentrant invocations to calculateGross().
            // And if there is pending any execution of calculateGross(), do it and do not continue.
            me.calculatingGross = false;
            me.calculatingReceipt = false;
            if (me.pendingCalculateGross) {
              me.pendingCalculateGross = false;
              me.calculateGross();
              return;
            }
            me.trigger('calculategross');
            me.trigger('saveCurrent');
          });
          };

      this.get('lines').forEach(function (line) {
        line.calculateGross();
      });

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
          saveAndTriggerEvents(gross);
        });
      } else {
        this.calculateTaxes(function () {
          //If the price doesn't include tax, the discounted gross has already been calculated
          var gross = me.get('lines').reduce(function (memo, e) {
            if (_.isUndefined(e.get('discountedGross'))) {
              return memo;
            }
            var grossLine = e.get('discountedGross');
            if (grossLine) {
              return OB.DEC.add(memo, grossLine);
            } else {
              return memo;
            }
          }, OB.DEC.Zero);
          saveAndTriggerEvents(gross);
        });
      }
    },

    addToListOfCallbacks: function (callback) {
      if (OB.UTIL.isNullOrUndefined(this.get('calculateReceiptCallbacks'))) {
        this.set('calculateReceiptCallbacks', []);
      }
      if (!OB.UTIL.isNullOrUndefined(callback)) {
        var list = this.get('calculateReceiptCallbacks');
        list.push(callback);
      }
    },

    // This function calculate the promotions, taxes and gross of all the receipt
    calculateReceipt: function (callback, line) {
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
        OB.error("calculateReceipt execution is forbidden right now");
        return;
      } else if (this.isCalculateReceiptLocked !== false && !this.get('belongsToMultiOrder')) {
        OB.error("setting the isCalculateGrossLocked state is mandatory before executing it the first time");
      }
      // verify that the ui receipt is the only one in which calculateReceipt is executed
      var isTheUIReceipt = this === OB.MobileApp.model.receipt || this.get('belongsToMultiOrder');
      if (!isTheUIReceipt) {
        OB.error("calculateReceipt should only be called by the UI receipt");
      }
      // Verify if it's necesary to skip applying the function
      if (this.get('skipCalculateReceipt')) {
        OB.debug('Skipping calculateReceipt function');
        return;
      }
      OB.MobileApp.view.waterfall('calculatingReceipt');
      this.calculatingReceipt = true;

      this.addToListOfCallbacks(callback);
      var executeCallback;
      executeCallback = function (listOfCallbacks, callback) {
        if (listOfCallbacks.length === 0) {
          callback();
          listOfCallbacks = null;
          return;
        }
        var callbackToExe = listOfCallbacks.shift();
        callbackToExe();
        executeCallback(listOfCallbacks, callback);
      };
      var me = this;
      this.on('applyPromotionsFinished', function () {
        me.off('applyPromotionsFinished');
        me.on('calculategross', function () {
          me.off('calculategross');
          if (me.pendingCalculateReceipt) {
            OB.MobileApp.view.waterfall('calculatedReceipt');
            me.pendingCalculateReceipt = false;
            me.calculatingReceipt = false;
            me.calculateReceipt();
            return;
          } else {
            if (me.get('calculateReceiptCallbacks') && me.get('calculateReceiptCallbacks').length > 0) {
              executeCallback(me.get('calculateReceiptCallbacks'), function () {
                me.calculatingReceipt = false;
                OB.MobileApp.view.waterfall('calculatedReceipt');
              });
            } else {
              me.calculatingReceipt = false;
              OB.MobileApp.view.waterfall('calculatedReceipt');
            }
          }
        });
        me.calculateGross();
      });
      // If line is null or undefined, we calculate the Promotions of the receipt
      if (OB.UTIL.isNullOrUndefined(line)) {
        OB.Model.Discounts.applyPromotions(this);
      } else {
        OB.Model.Discounts.applyPromotions(this, line);
      }
    },

    setDocumentNo: function (isReturn, isOrder) {
      var order = this,
          nextDocumentNo;
      if (isOrder && (order.get('documentnoPrefix') !== OB.MobileApp.model.get('terminal').docNoPrefix)) {
        nextDocumentNo = OB.MobileApp.model.getNextDocumentno();
        order.set('returnnoPrefix', -1);
        order.set('returnnoSuffix', -1);
        order.set('documentnoPrefix', OB.MobileApp.model.get('terminal').docNoPrefix);
        order.set('documentnoSuffix', nextDocumentNo.documentnoSuffix);
        order.set('quotationnoPrefix', -1);
        order.set('quotationnoSuffix', -1);
        order.set('documentNo', nextDocumentNo.documentNo);
        order.trigger('saveCurrent');
      } else if (OB.MobileApp.model.get('terminal').returnDocNoPrefix && isReturn) {
        if (order.get('returnnoPrefix') !== OB.MobileApp.model.get('terminal').returnDocNoPrefix) {
          nextDocumentNo = OB.MobileApp.model.getNextReturnno();
          order.set('returnnoPrefix', OB.MobileApp.model.get('terminal').returnDocNoPrefix);
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
      if (_.isUndefined(this.get('paidInNegativeStatusAmt'))) {
        return OB.DEC.sub(OB.DEC.abs(this.getTotal()), this.getPayment());
      } else {
        return OB.DEC.abs(OB.DEC.sub(OB.DEC.abs(this.getTotal()), this.get('paidInNegativeStatusAmt')));
      }
    },

    getDeliveredQuantityAmount: function () {
      return this.get('deliveredQuantityAmount') ? this.get('deliveredQuantityAmount') : 0;
    },

    printPending: function () {
      return OB.I18N.formatCurrency(this.getPending());
    },

    getPaymentStatus: function () {
      var total = OB.DEC.abs(this.getTotal()),
          pay = this.getPayment(),
          isReturn = true,
          processedPaymentsAmount = OB.DEC.Zero,
          paymentsAmount = OB.DEC.Zero,
          isNegative, paidInNegativeStatus, done, pending, overpayment, totalToReturn, pendingAmt;

      _.each(this.get('lines').models, function (line) {
        if (line.get('qty') > 0) {
          isReturn = false;
        }
      }, this);

      _.each(this.get('payments').models, function (payment) {
        if (payment.get('isPrePayment')) {
          processedPaymentsAmount = OB.DEC.add(processedPaymentsAmount, payment.get('origAmount'));
        } else {
          paymentsAmount = OB.DEC.add(paymentsAmount, payment.get('origAmount'));
        }
      });

      isNegative = this.get('gross') < 0 || (this.get('gross') > 0 && this.get('orderType') === 3 && (!this.get('isPartiallyDelivered') || (this.get('isPartiallyDelivered') && this.get('isDeliveredGreaterThanGross'))));
      // Check if the total amount is lower than the already paid (processed)
      if (!isNegative && this.get('gross') >= 0 && OB.DEC.compare(OB.DEC.sub(processedPaymentsAmount, total)) === 1) {
        isNegative = true;
        paidInNegativeStatus = OB.DEC.sub(processedPaymentsAmount, paymentsAmount);
        totalToReturn = OB.DEC.sub(processedPaymentsAmount, total);
      }

      if (_.isUndefined(paidInNegativeStatus)) {
        this.unset('paidInNegativeStatusAmt');
        done = this.get('lines').length > 0 && OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0;
        pending = OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.I18N.formatCurrency(OB.DEC.Zero) : OB.I18N.formatCurrency(OB.DEC.sub(total, pay));
        overpayment = OB.DEC.compare(OB.DEC.sub(pay, total)) > 0 ? OB.I18N.formatCurrency(OB.DEC.sub(pay, total)) : null;
        pendingAmt = OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.DEC.Zero : OB.DEC.sub(total, pay);
      } else {
        this.set('paidInNegativeStatusAmt', paidInNegativeStatus);
        done = this.get('lines').length > 0 && OB.DEC.compare(OB.DEC.sub(paymentsAmount, totalToReturn)) >= 0;
        pending = OB.DEC.compare(OB.DEC.sub(totalToReturn, paymentsAmount)) === 1 ? OB.I18N.formatCurrency(OB.DEC.sub(totalToReturn, paymentsAmount)) : null;
        overpayment = OB.DEC.compare(OB.DEC.sub(OB.DEC.sub(paymentsAmount, totalToReturn), this.getChange())) === 1 ? OB.I18N.formatCurrency(OB.DEC.sub(OB.DEC.sub(paymentsAmount, totalToReturn), this.getChange())) : null;
        pendingAmt = OB.DEC.compare(OB.DEC.sub(totalToReturn, paymentsAmount)) === 1 ? OB.DEC.sub(totalToReturn, paymentsAmount) : OB.DEC.Zero;
      }

      return {
        'done': done,
        'total': OB.I18N.formatCurrency(total),
        'pending': pending,
        'change': OB.DEC.compare(this.getChange()) > 0 ? OB.I18N.formatCurrency(this.getChange()) : null,
        'overpayment': overpayment,
        'isReturn': isReturn,
        'isNegative': isNegative,
        'changeAmt': this.getChange(),
        'pendingAmt': OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.DEC.Zero : OB.DEC.sub(total, pay),
        'payments': this.get('payments')
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
      this.set('lines', this.get('lines') ? this.get('lines').reset() : new OrderLineList());
      this.set('payments', this.get('payments') ? this.get('payments').reset() : new PaymentLineList());
      this.set('payment', OB.DEC.Zero);
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
      this.set('paidOnCredit', false);
      this.set('isLayaway', false);
      this.set('isEditable', true);
      this.set('openDrawer', false);
      this.set('totalamount', null);
      this.set('approvals', []);
      this.set('isPartiallyDelivered', false);
    },

    clearWith: function (_order) {
      // verify that the clearWith is not used for any other purpose than to update and fire the events of the UI receipt
      OB.UTIL.Debug.execute(function () {
        var isTheUIReceipt = this.cid === OB.MobileApp.model.receipt.cid;
        if (!isTheUIReceipt) {
          OB.error("The target of the clearWith should only be the UI receipt. Use OB.UTIL.clone instead");
        }
      }, this);

      var idExecution;

      // we set first this property to avoid that the apply promotions is triggered
      this.set('isNewReceipt', _order.get('isNewReceipt'));
      //we need this data when IsPaid, IsLayaway changes are triggered
      this.set('documentType', _order.get('documentType'));

      //Prevent recalculating service relations during executions of clearWith
      this.set('preventServicesUpdate', true);

      this.set('isPaid', _order.get('isPaid'));
      this.set('paidOnCredit', _order.get('paidOnCredit'));
      this.set('isLayaway', _order.get('isLayaway'));
      this.set('isPartiallyDelivered', _order.get('isPartiallyDelivered'));
      if (!_order.get('isEditable')) {
        // keeping it no editable as much as possible, to prevent
        // modifications to trigger editable events incorrectly
        this.set('isEditable', _order.get('isEditable'));
      }

      if (_order.get('isLayaway')) {
        if (OB.MobileApp.model.get('terminal').terminalType.generateInvoice && OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice', true)) {
          if (OB.MobileApp.model.hasPermission('OBPOS_retail.restricttaxidinvoice', true) && !_order.get('bp').get('taxID')) {
            _order.set('generateInvoice', false);
          } else {
            _order.set('generateInvoice', true);
          }
        }
      }

      if (_order.get('replacedorder_documentNo')) {
        this.set('replacedorder_documentNo', _order.get('replacedorder_documentNo'));
      }

      // the idExecution is saved so only this execution of clearWith will check cloningReceipt to false
      if (OB.UTIL.isNullOrUndefined(this.get('idExecution')) && OB.UTIL.isNullOrUndefined(_order.get('idExecution'))) {
        idExecution = new Date().getTime();
        _order.set('idExecution', idExecution);
        _order.set('cloningReceipt', true);
        this.set('cloningReceipt', true);
        this.set('idExecution', idExecution);
      }

      OB.UTIL.clone(_order, this);

      if (!OB.UTIL.isNullOrUndefined(this.get('idExecution')) && this.get('idExecution') === idExecution) {
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
    },

    removeUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.sub(line.get('qty'), qty, OB.I18N.qtyScale()), OB.I18N.getLabel('OBPOS_RemoveUnits', [qty, line.get('product').get('_identifier')]));
    },

    addUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.add(line.get('qty'), qty, OB.I18N.qtyScale()), OB.I18N.getLabel('OBPOS_AddUnits', [OB.DEC.toNumber(new BigDecimal((String)(qty.toString()))), line.get('product').get('_identifier')]));
    },

    setUnit: function (line, qty, text, doNotSave) {
      var permission, me = this;

      if (OB.DEC.isNumber(qty) && qty !== 0) {
        var oldqty = line.get('qty');
        permission = 'OBPOS_ReturnLine';
        if ((!OB.MobileApp.model.hasPermission(permission, true) || this.get('isQuotation')) && qty < 0 && oldqty > 0) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddNegative'));
          return;
        }
        if (qty > 0 && oldqty < 0 && this.get('orderType') === 1) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddPostiveToReturn'));
          return;
        }
        if (this.get('replacedorder')) {
          if (oldqty > 0 && qty < line.get('remainingQuantity')) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceQtyEdit'));
            return;
          } else if ((oldqty < 0 && qty > line.get('remainingQuantity'))) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn'));
            return;
          }
        }
        if (line.get('product').get('groupProduct') === false) {
          this.addProduct(line.get('product'));
          return true;
        } else {
          // sets the new quantity
          line.set('qty', qty);
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
            undoText += text || OB.I18N.getLabel('OBPOS_SetUnits', [line.get('qty'), line.get('product').get('_identifier')]);
            oldqtys.push(oldqty);
            lines.push(line);
            this.setUndo('EditLine', {
              text: undoText,
              oldqtys: oldqtys,
              lines: lines,
              undo: function () {
                var i, thisUndo = me.get('undo');
                for (i = 0; i < thisUndo.lines.length; i++) {
                  //Changing the qty of a line modifies the undo attribute, so we need a copy
                  thisUndo.lines[i].set('qty', thisUndo.oldqtys[i]);
                }
                me.calculateReceipt();
                me.set('undo', null);
              }
            });
          } else {
            this.setUndo('EditLine', {
              text: text || OB.I18N.getLabel('OBPOS_SetUnits', [line.get('qty'), line.get('product').get('_identifier')]),
              oldqty: oldqty,
              line: line,
              undo: function () {
                line.set('qty', oldqty);
                me.calculateReceipt();
                me.set('undo', null);
              }
            });
          }
        }
        this.adjustPayment();
        if (!doNotSave) {
          this.save();
        }
      } else {
        if (line.get('deleteApproved')) {
          // The approval to delete the line has already been granted
          line.unset('deleteApproved');
          this.set('preventServicesUpdate', true);
          this.set('deleting', true);
          this.deleteLine(line);
          this.unset('preventServicesUpdate');
          this.unset('deleting');
          this.get('lines').trigger('updateRelations');
        } else {
          // We don't have the approval to delete the line yet; request it
          OB.UTIL.Approval.requestApproval(OB.MobileApp.view.$.containerWindow.getRoot().model, 'OBPOS_approval.deleteLine', function (approved) {
            if (approved) {
              me.set('preventServicesUpdate', true);
              me.set('deleting', true);
              me.deleteLine(line);
              me.unset('preventServicesUpdate');
              me.unset('deleting');
              me.get('lines').trigger('updateRelations');
            }
          });
        }
      }
    },

    setPrice: function (line, price, options) {
      if (this.get('isQuotation') && this.get('hasbeenpaid') === 'Y') {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
        return;
      }
      OB.UTIL.HookManager.executeHooks('OBPOS_PreSetPrice', {
        context: this,
        line: line,
        price: price,
        options: options
      }, function (args) {
        var me = args.context;
        if (args.cancellation && args.cancellation === true) {
          return;
        }

        options = args.options || {};
        options.setUndo = (_.isUndefined(options.setUndo) || _.isNull(options.setUndo) || options.setUndo !== false) ? true : options.setUndo;

        if (!OB.UTIL.isNullOrUndefined(args.line.get('originalOrderLineId'))) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CannotChangePrice'));
        } else if (args.line.get('replacedorderline') && args.line.get('qty') < 0) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceReturnPriceChange'));
          return;
        } else if (OB.DEC.isNumber(args.price)) {
          var oldprice = args.line.get('price');
          if (OB.DEC.compare(args.price) >= 0) {
            // sets the new price
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
                text += OB.I18N.getLabel('OBPOS_SetPrice', [args.line.printPrice(), args.line.get('product').get('_identifier')]);
                oldprices.push(oldprice);
                lines.push(args.line);
                me.setUndo('EditLine', {
                  text: text,
                  oldprices: oldprices,
                  lines: lines,
                  undo: function () {
                    var i;
                    for (i = 0; i < me.get('undo').lines.length; i++) {
                      me.get('undo').lines[i].set('price', me.get('undo').oldprices[i]);
                    }
                    me.calculateReceipt();
                    me.set('undo', null);
                  }
                });
              } else {
                me.setUndo('EditLine', {
                  text: OB.I18N.getLabel('OBPOS_SetPrice', [args.line.printPrice(), args.line.get('product').get('_identifier')]),
                  oldprice: oldprice,
                  line: args.line,
                  undo: function () {
                    args.line.set('price', oldprice);
                    me.calculateReceipt();
                    me.set('undo', null);
                  }
                });
              }
            }
          }
          me.adjustPayment();
        }
        me.save();
      });
    },

    setLineProperty: function (line, property, value) {
      var index = this.get('lines').indexOf(line);
      this.get('lines').at(index).set(property, value);
    },

    setUndo: function (action, data) {
      var me = this;
      if (data) {
        data.action = action;
      }
      OB.UTIL.HookManager.executeHooks('OBPOS_PreSetUndo_' + action, {
        data: data
      }, function (args) {
        me.set('undo', args.data);
      });
    },

    deleteLines: function (lines, idx, length, callback) {
      var me = this,
          line = lines[idx],
          i;
      if (idx === 0) {
        for (i = 0; i < lines.length; i++) {
          if (me.get('replacedorder') && lines[i].get('remainingQuantity')) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine'));
            if (callback) {
              callback();
            }
            return;
          }
        }
        this.set('undo', null);
      }
      idx++;
      if (this.get('lines').get(line)) {
        if (idx < length) {
          this.deleteLine(line, true);
          this.deleteLines(lines, idx, length, callback);
        } else {
          this.deleteLine(line, false, callback);
        }
      } else {
        // If there is a line and other related service line selected to delete, the service is deleted when the product is deleted
        // This causes that when this recursive line tries to delete the service line, the service line is not in the array which stores the lines to delete
        // The 'else' clause catches this situation and continues with the next line
        if (idx === length) {
          if (callback) {
            callback();
          }
        } else {
          this.deleteLines(lines, idx, length, callback);
        }
      }
    },

    removeDeleteLine: function (line) {
      var deleteIndex = -1;
      _.each(this.get('deletedLines'), function (d, index) {
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

    deleteLine: function (line, doNotSave, callback) {
      var me = this,
          pack = line.isAffectedByPack(),
          productId = line.get('product').id;


      if (me.get('replacedorder') && line.get('remainingQuantity')) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceDeleteLine'));
        if (callback) {
          callback();
        }
        return;
      }

      if (pack) {
        // When deleting a line, check lines with other product that are affected by
        // same pack than deleted one and merge splitted lines created for those
        this.get('lines').forEach(function (l) {
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
        if (OB.UTIL.RfidController.isRfidConfigured() && line.get('obposEpccode')) {
          OB.UTIL.RfidController.removeEpcLine(line);
        }
        var text, lines, indexes, relations, rl, rls, i;

        me.get('lines').remove(line);
        text = me.get('undo').text;
        lines = me.get('undo').lines;
        relations = me.get('undo').relations;

        me.setUndo('DeleteLine', {
          text: text,
          lines: lines,
          relations: relations,
          undo: function () {
            if (OB.UTIL.RfidController.isRfidConfigured() && line.get('obposEpccode')) {
              OB.UTIL.RfidController.addEpcLine(line);
            }
            enyo.$.scrim.show();
            me.set('preventServicesUpdate', true);
            me.set('deleting', true);
            if (OB.MobileApp.model.get('terminal').businessPartner === me.get('bp').get('id')) {
              for (i = 0; i < me.get('undo').lines.length; i++) {
                if (!me.get('undo').lines[i].get('product').get('oBPOSAllowAnonymousSale')) {
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_AnonymousSaleForProductNotAllowed', [me.get('undo').lines[i].get('product').get('_identifier')]));
                  return;
                }
              }
            }
            me.get('undo').lines.sort(function (a, b) {
              if (a.get('undoPosition') > b.get('undoPosition')) {
                return 1;
              }
              if (a.get('undoPosition') < b.get('undoPosition')) {
                return -1;
              }
              // a must be equal to b
              return 0;
            });

            lines.forEach(function (line) {
              me.removeDeleteLine(line);
              me.get('lines').add(line, {
                at: line.get('undoPosition')
              });
            });
            relations.forEach(function (rel) {
              var rls = rel[0].get('relatedLines').slice(),
                  lineToAddRelated = _.filter(me.get('lines').models, function (line) {
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
            enyo.$.scrim.hide();
          }
        });

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
          line.set('obposIsDeleted', true);
          this.get('deletedLines').push(new OrderLine(line.attributes));
        }
      }
      // remove the line
      finishDelete();
    },

    //Attrs is an object of attributes that will be set in order
    _addProduct: function (p, qty, options, attrs, callback) {
      var newLine = true,
          line = null,
          me = this;
      if (enyo.Panels.isScreenNarrow()) {
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_AddLine', [qty ? qty : 1, p.get('_identifier')]));
      }
      if (p.get('ispack')) {
        OB.Model.Discounts.discountRules[p.get('productCategory')].addProductToOrder(this, p);
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
      if (((options && options.line) ? options.line.get('qty') + qty : qty) < 0 && p.get('productType') === 'S' && !p.get('returnable')) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [p.get('_identifier')]));
        if (callback) {
          callback(false);
        }
        return;
      }

      function addProductToOrder() {
        if (me.get('isQuotation') && me.get('hasbeenpaid') === 'Y') {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
          if (callback) {
            callback(false);
          }
          return false;
        }
        if (p.get('obposScale')) {
          OB.POS.hwserver.getWeight(function (data) {
            if (data.exception) {
              OB.UTIL.showConfirmation.display('', data.exception.message);
            } else if (data.result === 0) {
              OB.UTIL.showConfirmation.display('', OB.I18N.getLabel('OBPOS_WeightZero'));
            } else {
              line = me.createLine(p, data.result, options, attrs);
            }
          });
        } else {
          if (p.get('groupProduct')) {
            if (options && options.line) {
              line = options.line;
            } else {
              line = me.get('lines').find(function (l) {
                if (l.get('product').id === p.id && ((l.get('qty') > 0 && qty > 0) || (l.get('qty') < 0 && qty < 0))) {
                  return true;
                }
              });
            }
            OB.UTIL.HookManager.executeHooks('OBPOS_GroupedProductPreCreateLine', {
              receipt: me,
              line: line,
              allLines: me.get('lines'),
              p: p,
              qty: qty,
              options: options,
              attrs: attrs
            }, function (args) {
              if (args && args.cancelOperation) {
                if (callback) {
                  callback(false);
                }
                return;
              }
              if (args.line && (args.line.get('qty') > 0 || !args.line.get('replacedorderline')) && (qty !== 1 || args.line.get('qty') !== -1 || args.p.get('productType') !== 'S' || (args.p.get('productType') === 'S' && !args.p.get('isLinkedToProduct')))) {
                args.receipt.addUnit(args.line, args.qty);
                if (!_.isUndefined(args.attrs)) {
                  _.each(_.keys(args.attrs), function (key) {
                    if (args.p.get('productType') === 'S' && key === 'relatedLines' && args.line.get('relatedLines')) {
                      args.line.set('relatedLines', OB.UTIL.mergeArrays(args.line.get('relatedLines'), attrs[key]));
                    } else {
                      args.line.set(key, attrs[key]);
                    }
                  });
                }
                args.line.trigger('selected', args.line);
                line = args.line;
                newLine = false;
              } else {
                if (args.attrs && args.attrs.relatedLines && args.attrs.relatedLines[0].deferred && args.p.get('quantityRule') === 'PP') {
                  line = args.receipt.createLine(args.p, args.attrs.relatedLines[0].qty, args.options, args.attrs);
                } else {
                  line = args.receipt.createLine(args.p, args.qty, args.options, args.attrs);
                }
              }
            });

          } else {
            //remove line even it is a grouped line
            if (options && options.line && qty === -1) {
              me.addUnit(options.line, qty);
              line = options.line;
              newLine = false;
            } else {
              line = me.createLine(p, qty, options, attrs);
            }
          }
        }
        me.save();
        OB.UTIL.HookManager.executeHooks('OBPOS_PostAddProductToOrder', {
          receipt: me,
          productToAdd: p,
          orderline: line,
          qtyToAdd: qty,
          options: options,
          newLine: newLine
        }, function (args) {
          if (args.newLine && me.get('lines').contains(line) && args.productToAdd.get('productType') !== 'S') {
            var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('HasServices');
            // Display related services after calculate gross, if it is new line and if the line has not been deleted.
            // The line might has been deleted during calculate gross for examples if there was an error in taxes.
            var productId = args.productToAdd.get('forceFilterId') ? args.productToAdd.get('forceFilterId') : args.productToAdd.id;
            args.receipt._loadRelatedServices(args.productToAdd.get('productType'), productId, args.productToAdd.get('productCategory'), function (data) {
              if (data) {
                if (data.hasservices) {
                  args.orderline.set('hasRelatedServices', true);
                  args.orderline.trigger('showServicesButton');
                } else {
                  args.orderline.set('hasRelatedServices', false);
                }
                args.receipt.save();
                if (data.hasmandatoryservices) {
                  args.receipt.trigger('showProductList', args.orderline, 'mandatory');
                }
              }
              OB.UTIL.SynchronizationHelper.finished(synchId, 'HasServices');
              if (callback) {
                callback(true);
              }
            }, args.orderline);
          } else {
            if (callback) {
              callback(true);
            }
          }
        });
      }
      if (((options && options.line) ? options.line.get('qty') + qty : qty) < 0 && p.get('productType') === 'S' && !p.get('ignoreReturnApproval')) {
        if (options && options.isVerifiedReturn) {
          OB.UTIL.showLoading(false);
        }
        OB.UTIL.Approval.requestApproval(
        OB.MobileApp.view.$.containerWindow.getRoot().model, 'OBPOS_approval.returnService', function (approved, supervisor, approvalType) {
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
        });
      } else {
        addProductToOrder();
      }
    },

    _loadRelatedServices: function (productType, productId, productCategory, callback, line) {
      if (productType !== 'S' && (!line || !line.get('originalOrderLineId'))) {
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          var process = new OB.DS.Process('org.openbravo.retail.posterminal.process.HasServices');
          var params = {},
              date = new Date(),
              i, prod, synchId;
          params.terminalTime = date;
          params.terminalTimeOffset = date.getTimezoneOffset();
          process.exec({
            product: productId,
            productCategory: productCategory,
            parameters: params
          }, function (data, message) {
            if (data && data.exception) {
              //ERROR or no connection
              OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              callback(null);
            } else if (data) {
              callback(data);
            } else {
              callback(null);
            }
          }, function (error) {
            OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
            callback(null);
          });
        } else {
          //non-high volumes: websql
          var criteria = {};

          criteria._whereClause = '';
          criteria.params = [];

          criteria._whereClause = " as product where product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

          //including/excluding products
          criteria._whereClause += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
          criteria._whereClause += "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
          criteria._whereClause += "or product.includeProducts is null) ";

          //including/excluding product categories
          criteria._whereClause += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
          criteria._whereClause += "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
          criteria._whereClause += "or product.includeProductCategories is null)) ";

          criteria.params.push(productId);
          criteria.params.push(productId);
          criteria.params.push(productCategory);
          criteria.params.push(productCategory);
          OB.Dal.find(OB.Model.Product, criteria, function (data) {
            if (data) {
              data.hasservices = data.length > 0;
              data.hasmandatoryservices = _.find(data.models, function (model) {
                return model.get('proposalType') === 'MP';
              });
              callback(data);
            } else {
              callback(null);
            }
          }, function (trx, error) {
            OB.error(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
            callback(null);
          });
        }
      } else {
        callback(null);
      }
    },

    _drawLinesDistribution: function (data) {
      if (data && data.linesToModify && data.linesToModify.length > 0) {
        _.each(data.linesToModify, function (lineToChange) {
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
          _.each(lineToChange.productProperties, function (propToSet) {
            line.get('product').set(propToSet.name, propToSet.value);
          });
          _.each(lineToChange.lineProperties, function (propToSet) {
            line.set(propToSet.name, propToSet.value);
          });
        }, this);
      }
      if (data && data.linesToRemove && data.linesToRemove.length > 0) {
        _.each(data.linesToRemove, function (lineCidToRemove) {
          var line = this.get('lines').getByCid(lineCidToRemove);
          this.set('preventServicesUpdate', true);
          this.set('deleting', true);
          this.deleteLine(line);
          this.unset('preventServicesUpdate');
          this.unset('deleting');
          this.get('lines').trigger('updateRelations');
        }, this);
      }
      if (data && data.linesToAdd && data.linesToAdd.length > 0) {
        _.each(data.linesToAdd, function (lineToAdd) {
          this.createLine(lineToAdd.product, lineToAdd.qtyToAdd);
        }, this);
      }
    },

    //Attrs is an object of attributes that will be set in order
    addProduct: function (p, qty, options, attrs, callback) {
      OB.debug('_addProduct');
      var me = this;
      if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true) && this.get('priceList') !== OB.MobileApp.model.get('terminal').priceList) {
        var criteria = {};
        if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          criteria = {
            m_pricelist_id: this.get('priceList'),
            m_product_id: p.id
          };
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
        }
        OB.Dal.find(OB.Model.ProductPrice, criteria, function (productPrices) {
          if (productPrices.length > 0) {
            p = p.clone();
            if (OB.UTIL.isNullOrUndefined(p.get('updatePriceFromPricelist')) || p.get('updatePriceFromPricelist')) {
              p.set('standardPrice', productPrices.at(0).get('pricestd'));
              p.set('listPrice', productPrices.at(0).get('pricelist'));
            }
            me.addProductToOrder(p, qty, options, attrs, function (success) {
              if (callback) {
                callback(success);
              }
            });
          } else {
            OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
            if (callback) {
              callback(false);
            }
          }
        }, function () {
          OB.UTIL.showI18NWarning('OBPOS_ProductNotFoundInPriceList');
          if (callback) {
            callback(false);
          }
        });
      } else {
        me.addProductToOrder(p, qty, options, attrs, function (success) {
          if (callback) {
            callback(success);
          }
        });
      }
    },

    addProductToOrder: function (p, qty, options, attrs, callback) {
      var me = this;
      OB.UTIL.HookManager.executeHooks('OBPOS_AddProductToOrder', {
        receipt: this,
        productToAdd: p,
        qtyToAdd: qty,
        options: options
      }, function (args) {
        // do not allow generic products to be added to the receipt
        if (args && args.productToAdd && args.productToAdd.get('isGeneric')) {
          OB.UTIL.showI18NWarning('OBPOS_GenericNotAllowed');
          if (callback) {
            callback(false);
          }
          return;
        }
        if (args && args.options && args.options.line && args.options.line.get('replacedorderline') && args.options.line.get('qty') < 0) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn'));
          if (callback) {
            callback(false);
          }
          return;
        }
        if (OB.MobileApp.model.get('terminal').businessPartner === me.get('bp').get('id') && args && args.productToAdd && args.productToAdd.has('oBPOSAllowAnonymousSale') && !args.productToAdd.get('oBPOSAllowAnonymousSale')) {
          if (args.receipt && args.receipt.get('deferredOrder')) {
            args.receipt.unset("deferredOrder", {
              silent: true
            });
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_AnonymousSaleNotAllowedDeferredSale'));
          } else {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_AnonymousSaleNotAllowed'));
          }
          if (callback) {
            callback(false);
          }
          return;
        }
        if (args && args.receipt && args.receipt.get('deferredOrder')) {
          args.receipt.unset("deferredOrder", {
            silent: true
          });
        }
        if (args && args.useLines) {
          me._drawLinesDistribution(args);
          if (callback) {
            callback(false);
          }
          return;
        }
        me._addProduct(p, qty, options, attrs, function (success) {
          if (callback) {
            callback(success);
          }
        });
      });
    },

    /**
     * Splits a line from the ticket keeping in the line the qtyToKeep quantity,
     * the rest is moved to another line with the same product and no packs, or
     * to a new one if there's no other line. In case a new is created it is returned.
     */
    splitLine: function (line, qtyToKeep) {
      var originalQty = line.get('qty'),
          newLine, p, qtyToMove;

      if (originalQty === qtyToKeep) {
        return;
      }

      qtyToMove = originalQty - qtyToKeep;

      this.setUnit(line, qtyToKeep, null, true);

      p = line.get('product');

      newLine = this.get('lines').find(function (l) {
        return l !== line && l.get('product').id === p.id && !l.isAffectedByPack();
      });

      if (!newLine) {
        newLine = line.clone();
        newLine.set({
          promotions: null,
          addedBySplit: true
        });
        this.get('lines').add(newLine);
        this.setUnit(newLine, qtyToMove, null, true);
        return newLine;
      } else {
        this.setUnit(newLine, newLine.get('qty') + qtyToMove, null, true);
      }
    },

    /**
     * Checks other lines with the same product to be merged in a single one
     */
    mergeLines: function (line) {
      var p = line.get('product'),
          lines = this.get('lines'),
          merged = false;
      line.set('promotions', null);
      lines.forEach(function (l) {
        if (l === line) {
          return;
        }

        if (l.get('product').id === p.id && l.get('price') === line.get('price')) {
          line.set({
            qty: line.get('qty') + l.get('qty'),
            promotions: null
          });
          lines.remove(l);
          merged = true;
        }
      }, this);
    },

    /**
     *  It looks for different lines for same product with exactly the same promotions
     *  to merge them in a single line
     */
    mergeLinesWithSamePromotions: function () {
      var lines = this.get('lines'),
          line, i, j, k, otherLine, toRemove = [],
          matches, otherPromos, found, compareRule;

      compareRule = function (p) {
        var basep = line.get('promotions')[k];
        return p.ruleId === basep.ruleId && ((!p.family && !basep.family) || (p.family && basep.family && p.family === basep.family));
      };

      for (i = 0; i < lines.length; i++) {
        line = lines.at(i);
        for (j = i + 1; j < lines.length; j++) {
          otherLine = lines.at(j);
          if (otherLine.get('product').id !== line.get('product').id) {
            continue;
          }

          if ((!line.get('promotions') || line.get('promotions').length === 0) && (!otherLine.get('promotions') || otherLine.get('promotions').length === 0)) {
            line.set('qty', line.get('qty') + otherLine.get('qty'));
            toRemove.push(otherLine);
          } else if (line.get('promotions') && otherLine.get('promotions') && line.get('promotions').length === otherLine.get('promotions').length && line.get('price') === otherLine.get('price')) {
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
                line.get('promotions')[k].displayedTotalAmount += found.displayedTotalAmount;
              }
              toRemove.push(otherLine);
            }
          }
        }
      }

      _.forEach(toRemove, function (l) {
        lines.remove(l);
      });
    },

    addPromotion: function (line, rule, discount) {
      var promotions = line.get('promotions') || [],
          disc = {},
          i, replaced = false,
          discountRule = OB.Model.Discounts.discountRules[rule.attributes.discountType];
      if (discountRule.getIdentifier) {
        disc.identifier = discountRule.getIdentifier(rule, discount);
      }
      disc.name = discount.name || rule.get('printName') || rule.get('name');
      disc.ruleId = rule.id || rule.get('ruleId');
      disc.amt = discount.amt;
      disc.fullAmt = discount.amt ? discount.amt : 0;
      disc.actualAmt = discount.actualAmt;
      disc.pack = discount.pack;
      disc.discountType = rule.get('discountType');
      disc.priority = rule.get('priority');
      disc.manual = discount.manual;
      disc.userAmt = discount.userAmt;
      disc.lastApplied = discount.lastApplied;
      disc.obdiscQtyoffer = rule.get('qtyOffer') ? OB.DEC.toNumber(rule.get('qtyOffer')) : line.get('qty');
      disc.qtyOffer = disc.obdiscQtyoffer;
      disc.doNotMerge = discount.doNotMerge;
      if (!OB.UTIL.isNullOrUndefined(discount.chunks)) {
        disc.chunks = discount.chunks;
      } else {
        disc.chunks = undefined;
      }


      disc.hidden = discount.hidden === true || (discount.actualAmt && !disc.amt);
      disc.preserve = discount.preserve === true;

      if (OB.UTIL.isNullOrUndefined(discount.actualAmt) && !disc.amt && disc.pack) {
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
      disc._idx = discount._idx || rule.get('_idx');

      for (i = 0; i < promotions.length; i++) {
        if (disc._idx !== -1 && disc._idx < promotions[i]._idx) {
          // Trying to apply promotions in incorrect order: recalculate whole line again
          OB.Model.Discounts.applyPromotionsImp(this, line, true);
          return;
        }
      }

      for (i = 0; i < promotions.length; i++) {
        if (promotions[i].ruleId === rule.id) {
          if (promotions[i].hidden !== true) {
            promotions[i] = disc;
            replaced = true;
            break;
          }
        }
      }

      if (!replaced) {
        promotions.push(disc);
      }

      line.set('promotions', promotions);
      line.trigger('change');
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

      }
    },

    //Attrs is an object of attributes that will be set in order line
    createLine: function (p, units, options, attrs) {
      var newline, me = this;

      function createLineAux(p, units, options, attrs, me) {
        if (me.validateAllowSalesWithReturn(units, false)) {
          return;
        }
        // Get prices from BP pricelist 
        var newline = new OrderLine({
          id: OB.UTIL.get_UUID(),
          product: p,
          uOM: p.get('uOM'),
          qty: OB.DEC.number(units),
          price: OB.DEC.number(p.get('standardPrice')),
          priceList: OB.DEC.number(p.get('listPrice')),
          priceIncludesTax: me.get('priceIncludesTax'),
          warehouse: {
            id: OB.MobileApp.model.get('warehouses')[0].warehouseid,
            warehousename: OB.MobileApp.model.get('warehouses')[0].warehousename
          }
        });

        if (!_.isUndefined(attrs)) {
          _.each(_.keys(attrs), function (key) {
            newline.set(key, attrs[key]);
          });
        }

        if (newline.get('relatedLines')) {
          newline.set('groupService', newline.get('product').get('groupProduct'));
        }

        //issue 25448: Show stock screen is just shown when a new line is created.
        if (newline.get('product').get("showstock") === true) {
          newline.get('product').set("showstock", false);
          newline.get('product').set("_showstock", true);
        }

        // add the created line
        me.get('lines').add(newline, options);
        newline.trigger('created', newline);
        // set the undo action
        me.setUndo('CreateLine', {
          text: OB.I18N.getLabel('OBPOS_AddLine', [newline.get('qty'), newline.get('product').get('_identifier')]),
          line: newline,
          undo: function (modelObj) {
            // Instead of using 'me' as order, is necessary to use 'OB.MobileApp.model.receipt' to avoid references to not active orders
            // This happens while adding a deferred sale to a paid receipt
            var order = OB.MobileApp.model.receipt;
            OB.UTIL.Approval.requestApproval((modelObj ? modelObj : this.model), 'OBPOS_approval.deleteLine', function (approved) {
              if (approved) {
                if (OB.UTIL.RfidController.isRfidConfigured() && newline.get('obposEpccode')) {
                  OB.UTIL.RfidController.removeEpcLine(newline);
                }
                // If the OBPOS_remove_ticket preference is active then mark the line as deleted
                if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
                  if (!order.get('deletedLines')) {
                    order.set('deletedLines', []);
                  }
                  newline.set('obposIsDeleted', true);
                  order.get('deletedLines').push(new OrderLine(newline.attributes));
                  order.save(function () {
                    order.get('lines').remove(newline);
                    order.calculateGross();
                    order.set('undo', null);
                  });
                } else {
                  // remove the line
                  order.get('lines').remove(newline);
                  order.calculateGross();
                  order.set('undo', null);
                }
              }
            });
          }
        });
        me.adjustPayment();
        return newline;
      }


      if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
        OB.Dal.saveIfNew(p, function () {}, function () {
          OB.error(arguments);
        });

        var productcriteria = {
          columns: ['product'],
          operator: 'equals',
          value: p.id,
          isId: true
        };
        var remoteCriteria = [productcriteria];
        var criteriaFilter = {};
        criteriaFilter.remoteFilters = remoteCriteria;
        OB.Dal.find(OB.Model.ProductCharacteristicValue, criteriaFilter, function (productcharacteristic) {
          function saveCharacteristics(characteristics, i) {
            if (i === characteristics.length) {
              me.calculateReceipt();
            } else {
              OB.Dal.saveIfNew(characteristics[i], function () {
                saveCharacteristics(characteristics, i + 1);
              }, function () {
                OB.error(arguments);
              });
            }
          }
          if (productcharacteristic.models.length !== 0) {
            saveCharacteristics(productcharacteristic.models, 0);
          }

        }, function () {
          OB.error(arguments);
        });
      }
      return createLineAux(p, units, options, attrs, me);
    },

    returnLine: function (line, options, skipValidaton) {
      var me = this;
      //The value of qty need to be negate because we want to change it
      if (this.validateAllowSalesWithReturn(-line.get('qty'), skipValidaton)) {
        return;
      }
      if (line.get('qty') > 0) {
        line.get('product').set('ignorePromotions', true);
      } else {
        line.get('product').set('ignorePromotions', false);
      }
      line.set('qty', -line.get('qty'));
      if (line.get('qty') > 0 && line.get('product').get('groupProduct')) {
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
        text += OB.I18N.getLabel('OBPOS_ReturnLine', [line.get('qty'), line.get('product').get('_identifier')]);
        lines.push(line);
        me.setUndo('ReturnLine', {
          text: text,
          lines: lines,
          undo: function () {
            _.each(lines, function (line) {
              line.set('qty', -line.get('qty'));
            });
            me.calculateReceipt();
            me.set('undo', null);
          }
        });
      } else {
        this.setUndo('ReturnLine', {
          text: OB.I18N.getLabel('OBPOS_ReturnLine', [line.get('product').get('_identifier')]),
          line: line,
          undo: function () {
            line.set('qty', -line.get('qty'));
            me.set('undo', null);
          }
        });
      }
      this.adjustPayment();
      if (line.get('promotions')) {
        line.unset('promotions');
      }
      this.save();

    },

    setBPandBPLoc: function (businessPartner, showNotif, saveChange, callback) {
      var me = this,
          undef;
      var i, oldbp = this.get('bp');
      if (OB.MobileApp.model.get('terminal').businessPartner === businessPartner.id) {
        for (i = 0; i < me.get('lines').models.length; i++) {
          if (!me.get('lines').models[i].get('product').get('oBPOSAllowAnonymousSale')) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_AnonymousSaleForProductNotAllowed', [me.get('lines').models[i].get('product').get('_identifier')]));
            return;
          }
        }
      }
      if (OB.UTIL.isNullOrUndefined(businessPartner.get('paymentTerms'))) {
        OB.UTIL.showWarning(enyo.format(OB.I18N.getLabel('OBPOS_MsgBPNotPaymentTerm'), businessPartner.get('name')));
        businessPartner.set('paymentTerms', OB.MobileApp.model.get('terminal').defaultbp_paymentterm);
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        if (oldbp.id !== businessPartner.id) { //Business Partner have changed
          OB.Dal.saveIfNew(businessPartner, function () {}, function () {
            OB.error(arguments);
          });
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.discount.bp', true)) {
            var bp = {
              columns: ['businessPartner'],
              operator: 'equals',
              value: businessPartner.id,
              isId: true
            };
            var remoteCriteria = [bp];
            var criteriaFilter = {};
            criteriaFilter.remoteFilters = remoteCriteria;
            OB.Dal.find(OB.Model.DiscountFilterBusinessPartner, criteriaFilter, function (discountsBP) {
              _.each(discountsBP.models, function (dsc) {
                OB.Dal.saveIfNew(dsc, function () {}, function () {
                  OB.error(arguments);
                });
              });
            }, function () {
              OB.error(arguments);
            });
          }

          OB.Dal.get(OB.Model.BPLocation, businessPartner.get('locId'), function (location) {

            OB.Dal.saveIfNew(location, function () {
              businessPartner.set('locationModel', location);
              me.set('bp', businessPartner);
              me.save();
            }, function () {
              OB.error(arguments);
            });

          }, function () {
            OB.error(arguments);
          });

        } else if (businessPartner.get('locationModel')) { //Location has changed or we are assigning current bp
          OB.Dal.saveIfNew(businessPartner.get('locationModel'), function () {
            me.set('bp', businessPartner);
            me.save();
          }, function () {
            OB.error(arguments);
          });
        }
      } else {
        this.set('bp', businessPartner);
        this.save();
      }
      // set the undo action
      if (showNotif === undef || showNotif === true) {
        this.setUndo('SetBPartner', {
          text: businessPartner ? OB.I18N.getLabel('OBPOS_SetBP', [businessPartner.get('_identifier')]) : OB.I18N.getLabel('OBPOS_ResetBP'),
          bp: businessPartner,
          undo: function () {
            me.set('bp', oldbp);
            me.save();
            me.set('undo', null);
          }
        });
      }
      if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
        if (oldbp.get('priceList') !== businessPartner.get('priceList')) {
          me.set('priceList', businessPartner.get('priceList'));
          var priceIncludesTax = businessPartner.get('priceIncludesTax');
          if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
            priceIncludesTax = OB.MobileApp.model.get('pricelist').priceIncludesTax;
          }
          me.set('priceIncludesTax', priceIncludesTax);
          me.removeAndInsertLines(function () {
            me.calculateReceipt(function () {
              if (saveChange) {
                me.save();
              }
              if (callback) {
                callback();
              }
            });
          });
        } else {
          me.calculateReceipt(function () {
            if (saveChange) {
              me.save();
            }
            if (callback) {
              callback();
            }
          });
        }
      } else {
        if (saveChange) {
          this.save();
        }
        if (callback) {
          callback();
        }
      }
    },

    validateAllowSalesWithReturn: function (qty, skipValidaton) {
      if (OB.MobileApp.model.hasPermission('OBPOS_NotAllowSalesWithReturn', true) && !skipValidaton) {
        var negativeLines = _.filter(this.get('lines').models, function (line) {
          return line.get('qty') < 0;
        }).length;
        if (this.get('lines').length > 0) {
          if (qty > 0 && negativeLines > 0) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddPositive'));
            return true;
          } else if (qty < 0 && negativeLines !== this.get('lines').length) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgCannotAddNegative'));
            return true;
          }
        }
      }
      if (this.isLayaway() && qty < 0) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed'));
        return true;
      }
      return false;
    },

    removeAndInsertLines: function (callback) {
      this.set('skipCalculateReceipt', true);
      var me = this;
      // Remove all lines and insert again with new prices
      var orderlines = [];
      var promotionlines = [];
      var addProductsOfLines = null;

      addProductsOfLines = function (receipt, lines, index, callback, promotionLines) {
        if (index === lines.length) {
          me.set('skipCalculateReceipt', false);
          if (callback) {
            callback();
          }
          return;
        }
        OB.Dal.get(OB.Model.Product, lines[index].get('product').id, function (product) {
          me.addProduct(product, lines[index].get('qty'), undefined, undefined, function (isInPriceList) {
            if (isInPriceList) {
              me.get('lines').at(index).set('promotions', promotionLines[index]);
              me.get('lines').at(index).calculateGross();
              addProductsOfLines(receipt, lines, index + 1, callback, promotionLines);
            } else {
              promotionLines.splice(index, 1);
              lines.splice(index, 1);
              addProductsOfLines(receipt, lines, index, callback, promotionLines);
            }
          });
        });
      };
      _.each(me.get('lines').models, function (line) {
        orderlines.push(line);
        promotionlines.push(line.get('promotions'));
      });
      this.set('preventServicesUpdate', true);
      this.set('deleting', true);
      _.each(orderlines, function (line) {
        me.deleteLine(line, true);
      });
      this.unset('preventServicesUpdate');
      this.unset('deleting');
      this.get('lines').trigger('updateRelations');
      addProductsOfLines(this, orderlines, 0, callback, promotionlines);
    },

    setOrderType: function (permission, orderType, options) {
      var me = this,
          i, approvalNeeded, servicesToApprove;

      function finishSetOrderType() {
        me.set('orderType', orderType); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
        if (orderType !== 3) { //Void this Layaway, do not need to save
          if (!(options && !OB.UTIL.isNullOrUndefined(options.saveOrder) && options.saveOrder === false)) {
            me.save();
          }
        } else {
          me.set('layawayGross', me.getGross());
          me.set('gross', me.get('payment'));
          me.set('payment', OB.DEC.Zero);
          me.get('payments').reset();
        }
        // remove promotions
        if (!(options && !OB.UTIL.isNullOrUndefined(options.applyPromotions) && options.applyPromotions === false)) {
          OB.Model.Discounts.applyPromotions(me);
        }
      }

      function returnLines() {
        me.set('preventServicesUpdate', true);
        _.each(me.get('lines').models, function (line) {
          if (line.get('qty') > 0) {
            me.returnLine(line, null, true);
          }
        }, me);
        me.unset('preventServicesUpdate');
        finishSetOrderType();
      }
      if (orderType === OB.DEC.One) {
        this.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns);
        if (options.saveOrder !== false) {
          approvalNeeded = false;
          servicesToApprove = '';
          for (i = 0; i < this.get('lines').models.length; i++) {
            var line = this.get('lines').models[i];
            if (line.get('product').get('productType') === 'S' && !line.isReturnable()) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [line.get('product').get('_identifier')]));
              return;
            } else {
              if (line.get('product').get('productType') === 'S') {
                if (!approvalNeeded) {
                  approvalNeeded = true;
                }
                servicesToApprove += '<br>· ' + line.get('product').get('_identifier');
              }
            }
          }
          if (approvalNeeded) {
            OB.UTIL.Approval.requestApproval(
            OB.MobileApp.view.$.containerWindow.getRoot().model, [{
              approval: 'OBPOS_approval.returnService',
              message: 'OBPOS_approval.returnService',
              params: [servicesToApprove]
            }], function (approved, supervisor, approvalType) {
              if (approved) {
                returnLines();
              }
            });
          } else {
            returnLines();
          }
        }
      } else {
        this.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentType);
        finishSetOrderType();
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

    hasOneLineToIgnoreDiscounts: function () {
      return _.some(this.get('lines').models, function (line) {
        return line.get('product').get('ignorePromotions');
      });
    },

    setOrderInvoice: function () {
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', true);
        this.save();
      }
    },

    updatePrices: function (callback) {
      var order = this,
          newAllLinesCalculated;

      function allLinesCalculated() {
        callback(order);
      }

      newAllLinesCalculated = _.after(this.get('lines').length, allLinesCalculated);

      this.get('lines').each(function (line) {

        //remove promotions
        line.unset('promotions');

        var successCallbackPrices, criteria = {};

        if (!OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          criteria = {
            'id': line.get('product').get('id')
          };
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
        }

        successCallbackPrices = function (dataPrices) {
          dataPrices.each(function (price) {
            order.setPrice(line, price.get('standardPrice', {
              setUndo: false
            }));
          });
          newAllLinesCalculated();
        };

        OB.Dal.find(OB.Model.Product, criteria, successCallbackPrices, function () {
          // TODO: Report error properly.
        }, line);
      });
    },

    verifyCancelAndReplace: function (context) {
      var me = this,
          process = new OB.DS.Process('org.openbravo.retail.posterminal.process.IsOrderCancelled');

      process.exec({
        orderId: this.get('id'),
        documentNo: this.get('documentNo'),
        setCancelled: false
      }, function (data) {
        if (data && data.exception) {
          if (data.exception.message) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), data.exception.message);
            return;
          }
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline'));
          return;
        } else if (data && data.orderCancelled) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_OrderReplacedError'));
          return;
        } else {
          OB.UTIL.HookManager.executeHooks('OBPOS_PreCancelAndReplace', {
            context: context
          }, function (args) {
            if (args && args.cancelOperation) {
              return;
            }

            if (me.get('payments').length) {
              var notPrePayments = _.filter(me.get('payments').models, function (payment) {
                return !payment.get('isPrePayment');
              });
              if (notPrePayments.length !== 0) {
                var paymentList = [OB.I18N.getLabel('OBPOS_C&RDeletePaymentsBodyInit')];
                var symbol = OB.MobileApp.model.get('terminal').symbol;
                var symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
                _.each(notPrePayments, function (payment) {
                  paymentList.push('· ' + payment.get('name') + ' (' + OB.I18N.formatCurrencyWithSymbol(payment.get('amount'), symbol, symbolAtRight) + ')');
                });
                paymentList.push(OB.I18N.getLabel('OBPOS_C&RDeletePaymentsBodyEnd'));
                OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_C&RDeletePaymentsHeader'), paymentList, [{
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true,
                  order: me,
                  notPrePayments: notPrePayments,
                  action: function () {
                    var confirmationPopup = this;
                    _.each(this.notPrePayments, function (payment) {
                      var location = confirmationPopup.order.get('payments').models.indexOf(payment);
                      confirmationPopup.order.get('payments').remove(confirmationPopup.order.get('payments').at(location));
                    });
                    this.order.cancelAndReplaceOrder();
                  }
                }, {
                  label: OB.I18N.getLabel('OBMOBC_LblCancel')
                }]);
              } else {
                me.cancelAndReplaceOrder();
              }
            } else {
              me.cancelAndReplaceOrder();
            }
          });
        }
      }, function () {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline'));
      });
    },

    cancelAndReplaceOrder: function () {
      var documentseq, documentseqstr, idMap = {},
          me = this,
          i, splittedDocNo = [],
          newDocNo = '',
          nextNumber;

      //Cloning order to be canceled
      var clonedreceipt = new OB.Model.Order();
      OB.UTIL.clone(me, clonedreceipt);
      me.set('canceledorder', clonedreceipt);

      OB.Dal.remove(this, function () {
        me.get('lines').each(function (line) {
          idMap[line.get('id')] = OB.Dal.get_uuid();
          line.set('replacedorderline', line.get('id'));
          line.set('id', idMap[line.get('id')]);
        }, me);

        me.set('replacedorder_documentNo', me.get('documentNo'));
        me.set('replacedorder', me.get('id'));
        me.set('id', null);
        me.set('session', OB.MobileApp.model.get('session'));

        me.set('generateInvoice', OB.MobileApp.model.get('terminal').terminalType.generateInvoice);
        me.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentType);

        if (me.get('isLayaway')) {
          me.set('orderType', 2);
        }
        me.set('isLayaway', false);

        me.set('createdBy', OB.MobileApp.model.get('orgUserId'));
        if (!me.get('salesRepresentative')) {
          if (OB.MobileApp.model.get('context').isSalesRepresentative) {
            me.set('salesRepresentative', OB.MobileApp.model.get('context').user.id);
          } else {
            me.set('salesRepresentative', null);
          }
        }

        me.set('hasbeenpaid', 'N');
        me.set('isPaid', false);
        me.set('isEditable', true);

        me.set('orderDate', new Date());
        me.set('creationDate', null);

        me.set('negativeDocNo', me.get('documentNo') + '*R*');
        newDocNo = '';
        splittedDocNo = me.get('documentNo').split('-');
        if (splittedDocNo.length > 1) {
          nextNumber = parseInt(splittedDocNo[splittedDocNo.length - 1], 10) + 1;
          for (i = 0; i < splittedDocNo.length; i++) {
            if (i === 0) {
              newDocNo = splittedDocNo[i] + '-';
            } else if (i < splittedDocNo.length - 1) {
              newDocNo += splittedDocNo[i] + '-';
            } else {
              newDocNo += nextNumber;
            }
          }
        } else {
          newDocNo = me.get('documentNo') + '-1';
        }
        me.set('documentNo', newDocNo);
        me.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        me.save(function () {
          me.get('lines').each(function (line) {
            if (line.get('relatedLines')) {
              line.get('relatedLines').forEach(function (rl) {
                rl.orderId = me.get('id');
                if (idMap[rl.orderlineId]) {
                  rl.orderlineId = idMap[rl.orderlineId];
                }
              });
            }
          }, me);
          me.get('payments').reset(me.get('payments').models);
          me.set('doCancelAndReplace', true);

          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_OrderReplaced', [me.get('replacedorder_documentNo'), me.get('documentNo')]));
          me.calculateReceipt(function () {
            me.unset('skipApplyPromotions');
          });
        });
      }, function () {
        OB.UTIL.showError('Error removing');
      });
    },

    cancelLayaway: function (context) {
      var me = this,
          cancelAllowed = true,
          notValid, i;
      OB.UTIL.HookManager.executeHooks('OBPOS_PreCancelLayaway', {
        context: context
      }, function (args) {
        if (args && args.cancelOperation) {
          return;
        }

        // Verify if there is any payment that is not prePayment. In that case the cancel layaway process stops immediately
        for (i = 0; i < me.get('payments').models.length; i++) {
          var curPayment = me.get('payments').models[i];
          if (_.isUndefined(curPayment.get('isPrePayment')) || _.isNull(curPayment.get('isPrePayment'))) {
            cancelAllowed = false;
            notValid = curPayment;
            break;
          }
        }

        if (cancelAllowed) {
          me.set('cancelLayaway', true);
          context.doShowDivText({
            permission: context.permission,
            orderType: 3
          });
          // If the canceling layaway is partially delivered, the payment and gross must be updated to don't create payments
          // to return the delivered quantity
          if (me.get('isPartiallyDelivered')) {
            me.set('gross', OB.DEC.sub(me.get('deliveredQuantityAmount'), me.get('gross')));
            me.set('payment', OB.DEC.Zero);
          }
          context.doTabChange({
            tabPanel: 'payment',
            keyboard: 'toolbarpayment',
            edit: false
          });
        } else {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_lblPaymentNotProcessedHeader'), OB.I18N.getLabel('OBPOS_lblPaymentNotProcessedMessage', [notValid.get('name'), notValid.get('origAmount'), OB.MobileApp.model.paymentnames[notValid.get('kind')].isocode]));
        }
      });
    },

    createQuotation: function () {
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.quotation')) {
        this.set('isQuotation', true);
        this.set('generateInvoice', false);
        this.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentTypeForQuotations);
        this.save();
      }
    },

    createOrderFromQuotation: function (updatePrices) {
      var idMap = {},
          me = this;
      this.get('lines').each(function (line) {
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
        idMap[line.get('id')] = OB.Dal.get_uuid();
        line.set('id', idMap[line.get('id')]);
      }, this);

      this.set('id', null);
      this.set('isQuotation', false);
      this.set('orderType', OB.MobileApp.model.get('terminal').terminalType.layawayorder ? 2 : 0);
      this.set('generateInvoice', OB.MobileApp.model.get('terminal').terminalType.generateInvoice);
      this.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentType);
      this.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      if (OB.MobileApp.model.get('context').user.isSalesRepresentative) {
        this.set('salesRepresentative', OB.MobileApp.model.get('context').user.id);
      } else {
        this.set('salesRepresentative', null);
      }
      this.set('hasbeenpaid', 'N');
      this.set('skipApplyPromotions', false);
      this.set('isPaid', false);
      this.set('isEditable', true);
      this.set('orderDate', OB.I18N.normalizeDate(new Date()));
      this.set('creationDate', null);
      var nextDocumentno = OB.MobileApp.model.getNextDocumentno();
      this.set('documentnoPrefix', OB.MobileApp.model.get('terminal').docNoPrefix);
      this.set('documentnoSuffix', nextDocumentno.documentnoSuffix);
      this.set('quotationnoPrefix', -1);
      this.set('quotationnoSuffix', -1);
      this.set('returnnoPrefix', -1);
      this.set('returnnoSuffix', -1);
      this.set('documentNo', nextDocumentno.documentNo);
      this.set('posTerminal', OB.MobileApp.model.get('terminal').id);
      this.set('session', OB.MobileApp.model.get('session'));
      this.save();

      this.get('lines').each(function (line) {
        if (line.get('relatedLines')) {
          line.get('relatedLines').forEach(function (rl) {
            rl.orderId = me.get('id');
            if (idMap[rl.orderlineId]) {
              rl.orderlineId = idMap[rl.orderlineId];
            }
          });
        }
      }, this);

      if (updatePrices) {
        this.updatePrices(function (order) {
          order.calculateReceipt(function () {
            OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationCreatedOrder'));
            // This event is used in stock validation module.
            order.trigger('orderCreatedFromQuotation');
          });
        });
      } else {
        this.set('skipApplyPromotions', true);
        this.calculateReceipt(function () {
          me.unset('skipApplyPromotions');
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationCreatedOrder'));
          me.trigger('orderCreatedFromQuotation');
        });
      }
      this.calculateReceipt();
    },

    reactivateQuotation: function () {
      var nextQuotationno, idMap = {},
          me = this;
      this.get('lines').each(function (line) {
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
        idMap[line.get('id')] = OB.Dal.get_uuid();
        line.set('id', idMap[line.get('id')]);
      }, this);
      this.set('hasbeenpaid', 'N');
      this.set('isEditable', true);
      this.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      this.set('session', OB.MobileApp.model.get('session'));
      this.set('orderDate', OB.I18N.normalizeDate(new Date()));
      this.set('skipApplyPromotions', false);
      //Sometimes the Id of Quotation is null.
      if (this.get('id') && !_.isNull(this.get('id'))) {
        this.set('oldId', this.get('id'));
        nextQuotationno = OB.MobileApp.model.getNextQuotationno();
        this.set('quotationnoPrefix', OB.MobileApp.model.get('terminal').quotationDocNoPrefix);
        this.set('quotationnoSuffix', nextQuotationno.quotationnoSuffix);
        this.set('documentNo', nextQuotationno.documentNo);
      } else {
        //this shouldn't happen.
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_QuotationCannotBeReactivated_title'), OB.I18N.getLabel('OBPOS_QuotationCannotBeReactivated_body'));
        return;
      }
      this.get('lines').each(function (line) {
        if (line.get('relatedLines')) {
          line.get('relatedLines').forEach(function (rl) {
            rl.orderId = me.get('id');
            if (idMap[rl.orderlineId]) {
              rl.orderlineId = idMap[rl.orderlineId];
            }
          });
        }
      }, this);
      this.set('id', null);
      this.save();
      this.calculateReceipt();
    },
    rejectQuotation: function (rejectReasonId, scope, callback) {
      if (!this.get('id')) {
        OB.error("The Id of the order is not defined (current value: " + this.get('id') + "'");
      }
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.QuotationsReject');
      OB.UTIL.showLoading(true);
      process.exec({
        messageId: OB.UTIL.get_UUID(),
        data: [{
          id: this.get('id'),
          orderid: this.get('id'),
          rejectReasonId: rejectReasonId
        }]
      }, function (data) {
        OB.UTIL.showLoading(false);
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_SuccessRejectQuotation'));
        if (callback) {
          callback.call(scope, data !== null);
        }
      });
    },
    resetOrderInvoice: function () {
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
        this.set('generateInvoice', false);
        this.save();
      }
    },
    getPrecision: function (payment) {
      var i, p, max;
      for (i = 0, max = OB.MobileApp.model.get('payments').length; i < max; i++) {
        p = OB.MobileApp.model.get('payments')[i];
        if (p.payment.searchKey === payment.paymenttype) {
          if (p.obposPrecision) {
            return p.obposPrecision;
          }
        }
      }
    },
    adjustPayment: function () {
      var i, max, p;
      var payments = this.get('payments');
      var total = OB.DEC.abs(this.getTotal());

      var nocash = OB.DEC.Zero;
      var cash = OB.DEC.Zero;
      var origCash = OB.DEC.Zero;
      var auxCash = OB.DEC.Zero;
      var prevCash = OB.DEC.Zero;
      var paidCash = OB.DEC.Zero;
      var pcash;
      var precision;
      var processedPaymentsAmount = OB.DEC.Zero;
      var paymentstatus = this.getPaymentStatus();
      var sumCash = function () {
          if (p.get('kind') === OB.MobileApp.model.get('paymentcash')) {
            // The default cash method
            cash = OB.DEC.add(cash, p.get('origAmount'));
            pcash = p;
            paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
          } else if (OB.MobileApp.model.hasPayment(p.get('kind')) && OB.MobileApp.model.hasPayment(p.get('kind')).paymentMethod.iscash) {
            // Another cash method
            origCash = OB.DEC.add(origCash, p.get('origAmount'));
            pcash = p;
            paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
          } else {
            nocash = OB.DEC.add(nocash, p.get('origAmount'));
          }
          };

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        precision = this.getPrecision(p);
        if (p.get('rate') && p.get('rate') !== '1') {
          p.set('origAmount', OB.DEC.div(p.get('amount'), p.get('mulrate')));
        } else {
          p.set('origAmount', p.get('amount'));
        }
        p.set('paid', p.get('origAmount'));
        if (_.isUndefined(this.get('paidInNegativeStatusAmt'))) {
          sumCash();
        } else {
          if (!p.get('isPrePayment')) {
            sumCash();
          } else {
            processedPaymentsAmount = OB.DEC.add(processedPaymentsAmount, p.get('origAmount'));
          }
        }
      }

      if (!_.isUndefined(this.get('paidInNegativeStatusAmt'))) {
        total = OB.DEC.sub(processedPaymentsAmount, total);
        payments = _.filter(payments.models, function (payment) {
          return !payment.get('isPrePayment');
        });
      }

      // Calculation of the change....
      //FIXME
      if (pcash) {
        if (pcash.get('kind') !== OB.MobileApp.model.get('paymentcash')) {
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
          this.set('change', OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash, precision), origCash, precision), OB.Utilities.Number.roundJSNumber(total, 2), precision));
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
      var i, max, p, order;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }
      if (this.stopAddingPayments) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
        return;
      }

      order = this;
      if (order.get('orderType') === 3 && order.getGross() === 0) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_MsgVoidLayawayPaymentError'));
        return;
      }

      payments = this.get('payments');
      total = OB.DEC.abs(this.getTotal());
      OB.UTIL.HookManager.executeHooks('OBPOS_preAddPayment', {
        paymentToAdd: payment,
        payments: payments,
        receipt: this
      }, function () {
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
              order.adjustPayment();
              order.trigger('displayTotal');
              return;
            }
          }
        } else {
          for (i = 0, max = payments.length; i < max; i++) {
            p = payments.at(i);
            if (p.get('kind') === payment.get('kind') && p.get('paymentData') && payment.get('paymentData') && p.get('paymentData').groupingCriteria && payment.get('paymentData').groupingCriteria && p.get('paymentData').groupingCriteria === payment.get('paymentData').groupingCriteria) {
              p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
              if (p.get('rate') && p.get('rate') !== '1') {
                p.set('origAmount', OB.DEC.add(payment.get('origAmount'), OB.DEC.mul(p.get('origAmount'), p.get('rate'))));
              }
              payment.set('date', new Date());
              order.adjustPayment();
              order.trigger('displayTotal');
              return;
            }
          }
        }
        if (payment.get('openDrawer') && (payment.get('allowOpenDrawer') || payment.get('isCash'))) {
          order.set('openDrawer', payment.get('openDrawer'));
        }
        payment.set('date', new Date());
        payment.set('id', OB.UTIL.get_UUID());
        payments.add(payment);
        order.adjustPayment();
        order.trigger('displayTotal');
        order.save();
      }); // call with callback, no args
    },

    overpaymentExists: function () {
      return this.getPaymentStatus().overpayment ? true : false;
    },

    removePayment: function (payment) {
      var payments = this.get('payments');
      OB.UTIL.HookManager.executeHooks('OBPOS_preRemovePayment', {
        paymentToRem: payment,
        payments: payments,
        receipt: this
      }, function (args) {
        if (args.cancellation) {
          return true;
        }
        payments.remove(payment);
        if (payment.get('openDrawer')) {
          args.receipt.set('openDrawer', false);
        }
        args.receipt.adjustPayment();
        args.receipt.save();
      });
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
        delete item.product._filter;
      });

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
    },

    groupLinesByProduct: function () {
      var lineToMerge, lines = this.get('lines'),
          auxLines = lines.models.slice(0),
          localSkipApplyPromotions;

      localSkipApplyPromotions = this.get('skipApplyPromotions');
      this.set({
        'skipApplyPromotions': true
      }, {
        silent: true
      });
      _.each(auxLines, function (l) {
        lineToMerge = _.find(lines.models, function (line) {
          if (l !== line && l.get('product').id === line.get('product').id && l.get('price') === line.get('price') && line.get('qty') > 0 && l.get('qty') > 0 && !_.find(line.get('promotions'), function (promo) {
            return promo.manual;
          }) && !_.find(l.get('promotions'), function (promo) {
            return promo.manual;
          })) {
            return line;
          }
        });
        if (lineToMerge && lineToMerge.get('product').get('groupProduct')) {
          lineToMerge.set({
            qty: lineToMerge.get('qty') + l.get('qty')
          }, {
            silent: true
          });
          lines.remove(l);
        }
      });
      this.set({
        'skipApplyPromotions': localSkipApplyPromotions
      }, {
        silent: true
      });
    },
    fillPromotionsWith: function (groupedOrder, isFirstTime) {
      var me = this,
          copiedPromo, linesToMerge, auxPromo, idx, actProm, linesToCreate = [],
          qtyToReduce, lineToEdit, lineProm, linesToReduce, linesCreated = false,
          localSkipApplyPromotions;

      localSkipApplyPromotions = this.get('skipApplyPromotions');
      this.set({
        'skipApplyPromotions': true
      }, {
        silent: true
      });
      //reset pendingQtyOffer value of each promotion
      groupedOrder.get('lines').forEach(function (l) {
        _.each(l.get('promotions'), function (promo) {
          promo.pendingQtyOffer = promo.qtyOffer;
          if (!l.get('product').get('groupProduct')) {
            promo.doNotMerge = true;
          }
        });
        //copy lines from virtual ticket to original ticket when they have promotions which avoid us to merge lines
        if (_.find(l.get('promotions'), function (promo) {
          return promo.doNotMerge;
        })) {
          //First, try to find lines with the same qty
          lineToEdit = _.find(me.get('lines').models, function (line) {
            if (l !== line && l.get('product').id === line.get('product').id && l.get('price') === line.get('price') && line.get('qty') === l.get('qty') && !_.find(line.get('promotions'), function (promo) {
              return promo.doNotMerge;
            })) {
              return line;
            }
          });
          //if we cannot find lines with same qty, find lines with qty > 0
          if (!lineToEdit) {
            lineToEdit = _.find(me.get('lines').models, function (line) {
              if (l !== line && l.get('product').id === line.get('product').id && l.get('price') === line.get('price') && line.get('qty') > 0) {
                return line;
              }
            });
          }
          if (OB.UTIL.isNullOrUndefined(lineToEdit)) {
            return;
          }
          lineToEdit.set('noDiscountCandidates', l.get('noDiscountCandidates'), {
            silent: true
          });

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
            lineToEdit.set('qty', OB.DEC.sub(lineToEdit.get('qty'), l.get('qty')), {
              silent: true
            });
            me.mergeLines(lineToEdit);
            //if promotion affects to several lines, edit first line with the promotion info and then remove the affected lines
          } else if (lineToEdit.get('qty') < l.get('qty')) {
            qtyToReduce = OB.DEC.sub(l.get('qty'), lineToEdit.get('qty'));
            linesToReduce = _.filter(me.get('lines').models, function (line) {
              if (l !== line && l.get('product').id === line.get('product').id && l.get('price') === line.get('price') && line.get('qty') > 0 && !_.find(line.get('promotions'), function (promo) {
                return promo.manual || promo.doNotMerge;
              })) {
                return line;
              }
            });
            lineProm = linesToReduce.shift();
            lineProm.set('qty', l.get('qty'));
            lineProm.set('promotions', l.get('promotions'));
            lineProm.set('promotionCandidates', l.get('promotionCandidates'));
            lineProm.set('qtyToApplyDiscount', l.get('qtyToApplyDiscount'));
            lineProm.trigger('change');
            _.each(linesToReduce, function (line) {
              if (line.get('qty') > qtyToReduce) {
                line.set({
                  qty: line.get('qty') - qtyToReduce
                }, {
                  silent: true
                });
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
          linesToMerge = _.filter(me.get('lines').models, function (line) {
            var qtyReserved = 0;
            var promotions = line.get('promotions') || [];
            if (promotions.length > 0) {
              promotions.forEach(function (p) {
                qtyReserved = OB.DEC.add(qtyReserved, p.qtyOfferReserved || 0);
              });
            }
            if (l !== line && l.get('product').id === line.get('product').id && l.get('price') === line.get('price') && OB.UTIL.Math.sign(line.get('qty')) === OB.UTIL.Math.sign(l.get('qty'))) {
              if (OB.DEC.sub(Math.abs(line.get('qty')), qtyReserved) > 0) {
                var isManualOrNotMerge = _.find(line.get('promotions'), function (promo) {
                  return promo.manual || promo.doNotMerge;
                });
                if (!isManualOrNotMerge) {
                  return line;
                }
              }
            }
          });
          // sort by qty asc to fix issue 28120
          // firstly the discount is applied to the lines with minus quantity, so the discount is applied to all quantity of the line
          // and if it is needed (promotion.qty > line.qty) the rest of promotion will be applied to the other line
          linesToMerge = _.sortBy(linesToMerge, function (lsb) {
            lsb.getQty();
          });
          if (linesToMerge.length > 0) {
            _.each(linesToMerge, function (line) {
              line.set({
                promotionCandidates: l.get('promotionCandidates'),
                promotionMessages: me.showMessagesPromotions(line.get('promotionMessages'), l.get('promotionMessages')),
                qtyToApplyDiscount: l.get('qtyToApplyDiscount'),
                noDiscountCandidates: l.get('noDiscountCandidates')
              }, {
                silent: true
              });
              _.each(l.get('promotions'), function (promo) {
                copiedPromo = JSON.parse(JSON.stringify(promo));
                //when ditributing the promotion between different lines, we save accumulated amount
                promo.distributedAmt = promo.distributedAmt ? promo.distributedAmt : OB.DEC.Zero;
                //pendingQtyOffer is the qty of the promotion which need to be apply (we decrease this qty in each loop)
                promo.pendingQtyOffer = !_.isUndefined(promo.pendingQtyOffer) ? promo.pendingQtyOffer : promo.qtyOffer;
                if (promo.pendingQtyOffer && promo.pendingQtyOffer >= line.get('qty')) {
                  //if _.isUndefined(promo.actualAmt) is true we do not distribute the discount
                  if (_.isUndefined(promo.actualAmt)) {
                    if (promo.pendingQtyOffer !== promo.qtyOffer) {
                      copiedPromo.hidden = true;
                      copiedPromo.amt = OB.DEC.Zero;
                    }
                  } else {
                    copiedPromo.actualAmt = (promo.fullAmt / promo.qtyOffer) * line.get('qty');
                    copiedPromo.amt = (promo.fullAmt / promo.qtyOffer) * line.get('qty');
                    copiedPromo.obdiscQtyoffer = line.get('qty');
                    promo.distributedAmt = OB.DEC.add(promo.distributedAmt, OB.DEC.toNumber(OB.DEC.toBigDecimal((promo.fullAmt / promo.qtyOffer) * line.get('qty'))));
                  }

                  if (promo.pendingQtyOffer === line.get('qty')) {

                    if (!_.isUndefined(promo.actualAmt) && promo.actualAmt && promo.actualAmt !== promo.distributedAmt) {
                      copiedPromo.actualAmt = OB.DEC.add(copiedPromo.actualAmt, OB.DEC.sub(promo.actualAmt, promo.distributedAmt));
                      copiedPromo.amt = promo.amt ? OB.DEC.add(copiedPromo.amt, OB.DEC.sub(promo.amt, promo.distributedAmt)) : promo.amt;
                    }
                    promo.pendingQtyOffer = null;
                  } else {
                    promo.pendingQtyOffer = promo.pendingQtyOffer - line.get('qty');
                  }
                  if (line.get('promotions')) {
                    auxPromo = _.find(line.get('promotions'), function (promo) {
                      return promo.ruleId === copiedPromo.ruleId;
                      // return promo.ruleId === copiedPromo.ruleId && promo.hidden !== true && promo.actualAmt > 0;
                    });
                    if (auxPromo) {
                      idx = line.get('promotions').indexOf(auxPromo);
                      line.get('promotions').splice(idx, 1, copiedPromo);
                    } else {
                      line.get('promotions').push(copiedPromo);
                    }
                  } else {
                    line.set('promotions', [copiedPromo]);
                  }
                } else if (promo.pendingQtyOffer) {
                  if (_.isUndefined(promo.actualAmt)) {
                    if (promo.pendingQtyOffer !== promo.qtyOffer) {
                      copiedPromo.hidden = true;
                      copiedPromo.amt = OB.DEC.Zero;
                    }
                  } else {
                    copiedPromo.actualAmt = (promo.fullAmt / promo.qtyOffer) * promo.pendingQtyOffer;
                    copiedPromo.amt = (promo.fullAmt / promo.qtyOffer) * promo.pendingQtyOffer;
                    copiedPromo.obdiscQtyoffer = promo.pendingQtyOffer;
                    promo.distributedAmt = OB.DEC.add(promo.distributedAmt, OB.DEC.toNumber(OB.DEC.toBigDecimal((promo.fullAmt / promo.qtyOffer) * promo.pendingQtyOffer)));
                  }
                  if (!_.isUndefined(promo.actualAmt) && promo.actualAmt && promo.actualAmt !== promo.distributedAmt) {
                    copiedPromo.actualAmt = OB.DEC.add(copiedPromo.actualAmt, OB.DEC.sub(promo.actualAmt, promo.distributedAmt));
                    copiedPromo.amt = promo.amt ? OB.DEC.add(copiedPromo.amt, OB.DEC.sub(promo.amt, promo.distributedAmt)) : promo.amt;
                  }

                  if (line.get('promotions')) {
                    auxPromo = _.find(line.get('promotions'), function (promo) {
                      return promo.ruleId === copiedPromo.ruleId && promo.preserve !== true;
                      // return promo.ruleId === copiedPromo.ruleId;
                    });
                    if (auxPromo) {
                      idx = line.get('promotions').indexOf(auxPromo);
                      line.get('promotions').splice(idx, 1, copiedPromo);
                    } else {
                      line.get('promotions').push(copiedPromo);
                    }
                  } else {
                    line.set('promotions', [copiedPromo]);
                  }
                  promo.pendingQtyOffer = null;
                  //if it is the first we enter in this method, promotions which are not in the virtual ticket are deleted.
                } else if (isFirstTime) {
                  actProm = _.find(line.get('promotions'), function (prom) {
                    return prom.ruleId === promo.ruleId;
                  });
                  if (actProm) {
                    idx = line.get('promotions').indexOf(actProm);
                    if (idx > -1) {
                      line.get('promotions').splice(idx, 1);
                    }
                  }
                }
              });
              line.trigger('change');
            });
          }
        }
      });
      if (!linesCreated) {
        _.each(linesToCreate, function (line) {
          me.createLine(line.product, line.qty, null, line.attrs);
        });
        linesCreated = true;
      }
      this.set({
        'skipApplyPromotions': localSkipApplyPromotions
      }, {
        silent: true
      });
      this.trigger('promotionsUpdated');
    },
    // for each line, decrease the qtyOffer of promotions and remove the lines with qty 0
    removeQtyOffer: function () {
      var linesPending = new Backbone.Collection();
      this.get('lines').forEach(function (l) {
        var promotionsApplyNext = [],
            promotionsCascadeApplied = [],
            qtyReserved = 0,
            qtyPending;
        if (l.get('promotions')) {
          promotionsApplyNext = [];
          promotionsCascadeApplied = [];
          l.get('promotions').forEach(function (p) {
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

      _.each(this.get('lines').models, function (line) {
        if (line.get('qty') > 0) {
          linesPending.add(line);
        }
      });
      this.get('lines').reset(linesPending.models);
    },

    removeLinesWithoutPromotions: function () {
      var linesPending = new Backbone.Collection();
      _.each(this.get('lines').models, function (l) {
        if (l.get('promotions') && l.get('promotions').length > 0) {
          linesPending.push(l);
        }
      });
      this.set('lines', linesPending);
    },

    hasPromotions: function () {
      var hasPromotions = false;
      this.get('lines').forEach(function (l) {
        if (l.get('promotions') && l.get('promotions').length > 0) {
          hasPromotions = true;
        }
      });
      return hasPromotions;
    },

    isSimilarLine: function (line1, line2) {
      var equalPromotions = function (x, y) {
          var isEqual = true;
          if (x.length !== y.length) {
            isEqual = false;
          } else {
            x.forEach(function (p1, ind) {
              if (p1.amt !== y[ind].amt || p1.displayedTotalAmount !== y[ind].displayedTotalAmount || p1.qtyOffer !== y[ind].qtyOffer || p1.qtyOfferReserved !== y[ind].qtyOfferReserved || p1.ruleId !== y[ind].ruleId || p1.obdiscQtyoffer !== y[ind].obdiscQtyoffer) {
                isEqual = false;
              }
            });
          }
          return isEqual;
          };
      if (line1.get('product').get('id') === line2.get('product').get('id') && line1.get('price') === line2.get('price') && line1.get('discountedLinePrice') === line2.get('discountedLinePrice') && line1.get('qty') === line2.get('qty')) {
        return equalPromotions(line1.get('promotions') || [], line2.get('promotions') || []);
      } else {
        return false;
      }
    },
    // if there is a promtion of type "applyNext" that it has been applied previously in the line, then It is replaced
    // by the first promotion applied. Ex:
    // Ex: prod1 - qty 5 - disc3x2 & discPriceAdj -> priceAdj is applied first to 5 units
    //     it is called to applyPromotions, with the 2 units frees, and priceAdj is applied again to this 2 units
    // it is wrong, only to 5 units should be applied priceAdj, no 5 + 2 units
    removePromotionsCascadeApplied: function () {
      this.get('lines').forEach(function (l) {
        if (!OB.UTIL.isNullOrUndefined(l.get('promotions')) && l.get('promotions').length > 0 && !OB.UTIL.isNullOrUndefined(l.get('promotionsCascadeApplied')) && l.get('promotionsCascadeApplied').length > 0) {
          l.get('promotions').forEach(function (p, ind) {
            l.get('promotionsCascadeApplied').forEach(function (pc) {
              if (p.ruleId === pc.ruleId) {
                l.get('promotions')[ind] = pc;
              }
            });
          });
        }
      });
    },

    showMessagesPromotions: function (arrayMessages1, arrayMessages2) {
      arrayMessages1 = arrayMessages1 || [];
      (arrayMessages2 || []).forEach(function (m2) {
        if (_.filter(arrayMessages1, function (m1) {
          return m1 === m2;
        }).length === 0) {
          arrayMessages1.push(m2);
          OB.UTIL.showAlert.display(m2);
        }
      });
      return arrayMessages1;
    },

    getOrderDescription: function () {
      var desc = "{id: '" + this.get('id') + "', Docno: '" + this.get('documentNo') + "', Total gross: '" + this.get('gross') + "', Lines: ['";
      var i = 0;
      var propt;
      this.get('lines').forEach(function (l) {
        if (i !== 0) {
          desc += ",";
        }
        desc += "'{Product: '" + l.get('product').get('_identifier') + "', Quantity: '" + l.get('qty') + "', Gross: '" + l.get('gross') + "', LineGrossAmount: '" + l.get('lineGrossAmount') + "', DiscountedGross: '" + l.get('discountedGross') + "', Net: '" + l.get('net') + "', DiscountedNet: '" + l.get('discountedNet') + "', NonDiscountedNet: '" + l.get('nondiscountednet') + "', TaxAmount: '" + l.get('taxAmount') + "', GrossUnitPrice: '" + l.get('grossUnitPrice') + "'}";
        i++;
      });
      desc += "], Payments: [";
      i = 0;
      this.get('payments').forEach(function (l) {
        if (i !== 0) {
          desc += ",";
        }
        desc += "{PaymentMethod: '" + l.get('kind') + "', Amount: '" + l.get('amount') + "', OrigAmount: '" + l.get('origAmount') + "', Date: '" + l.get('date') + "', isocode: '" + l.get('isocode') + "'}";
        i++;
      });
      desc += "], Taxes: [";
      i = 0;
      for (propt in this.get('taxes')) {
        if (this.get('taxes').hasOwnProperty(propt)) {
          var obj = this.get('taxes')[propt];
          if (i !== 0) {
            desc += ",";
          }
          desc += "{TaxId: '" + propt + "', TaxRate: '" + obj.rate + "', TaxNet: '" + obj.net + "', TaxAmount: '" + obj.amount + "', TaxName: '" + obj.name + "'}";
          i++;
        }
      }
      desc += "]";
      desc += "}";
      return desc;
    },

    canAddAsServices: function (model, product, callback, scope) {
      if (product.get('productType') === 'S') {
        if (!OB.UTIL.isNullOrUndefined(product.get('allowDeferredSell')) && product.get('allowDeferredSell')) {
          if (model.get('order') && model.get('order').get('isQuotation') && model.get('order').get('isEditable') === false) {
            // Not allow deferred sell in quotation under evaluation
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_modalNoEditableHeader'), OB.I18N.getLabel('OBPOS_modalNoEditableBody'), [{
              label: OB.I18N.getLabel('OBMOBC_LblOk')
            }]);
            callback.call(scope, 'NOT_ALLOW');
          } else if (!OB.UTIL.isNullOrUndefined(product.get('deferredSellMaxDays'))) {
            var oneDay = 24 * 60 * 60 * 1000,
                today = new Date(),
                orderDate = new Date(this.get('orderDate'));
            today.setHours(0, 0, 0, 0);
            orderDate.setHours(0, 0, 0, 0);
            var diffDays = Math.round(OB.DEC.abs(today.getTime() - orderDate.getTime()) / oneDay);
            if (diffDays > product.get('deferredSellMaxDays')) {
              // Need approval exceeds max days
              OB.UTIL.Approval.requestApproval(
              model, [{
                approval: 'OBPOS_approval.deferred_sell_max_days',
                message: 'OBPOS_approval.deferred_sell_max_days',
                params: [product.get('deferredSellMaxDays')]
              }], function (approved, supervisor, approvalType) {
                callback.call(scope, approved ? 'OK' : 'NOT_ALLOW_MAX_DAYS');
              });
            } else {
              callback.call(scope, 'OK');
            }
          } else {
            callback.call(scope, 'OK');
          }
        } else {
          // Not allow deferred sell
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_msgDeferredSellCaption'), OB.I18N.getLabel('OBPOS_msgNotDeferredSell'), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk')
          }]);
          callback.call(scope, 'NOT_ALLOW');
        }
      } else {
        // Not is a service
        callback.call(scope, 'ABORT');
      }
    },
    getOrderlLineIndex: function (orderlineId) {
      var index = 0;
      this.get('lines').forEach(function (line, indx) {
        if (line.id === orderlineId) {
          index = indx;
        }
      });
      return index;
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

    newOrder: function (bp) {
      var order = new Order(),
          receiptProperties, i, p;
      bp = bp ? bp : OB.MobileApp.model.get('businessPartner');

      // reset in new order properties defined in Receipt Properties dialog
      if (OB.MobileApp.view.$.containerWindow && OB.MobileApp.view.$.containerWindow.getRoot() && OB.MobileApp.view.$.containerWindow.getRoot().$.receiptPropertiesDialog) {
        receiptProperties = OB.MobileApp.view.$.containerWindow.getRoot().$.receiptPropertiesDialog.newAttributes;
        for (i = 0; i < receiptProperties.length; i++) {
          if (receiptProperties[i].modelProperty) {
            order.set(receiptProperties[i].modelProperty, '');
          }
          if (receiptProperties[i].extraProperties) {
            for (p = 0; p < receiptProperties[i].extraProperties.length; p++) {
              order.set(receiptProperties[i].extraProperties[p], '');
            }
          }
        }
      }

      order.set('client', OB.MobileApp.model.get('terminal').client);
      order.set('organization', OB.MobileApp.model.get('terminal').organization);
      order.set('createdBy', OB.MobileApp.model.get('orgUserId'));
      order.set('updatedBy', OB.MobileApp.model.get('orgUserId'));
      order.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentType);
      order.set('orderType', OB.MobileApp.model.get('terminal').terminalType.layawayorder ? 2 : 0); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
      order.set('generateInvoice', false);
      order.set('isQuotation', false);
      order.set('oldId', null);
      order.set('session', OB.MobileApp.model.get('session'));
      order.set('bp', bp);
      if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
        // Set price list for order
        order.set('priceList', bp.get('priceList'));
        var priceIncludesTax = bp.get('priceIncludesTax');
        if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
          priceIncludesTax = OB.MobileApp.model.get('pricelist').priceIncludesTax;
        }
        order.set('priceIncludesTax', priceIncludesTax);
      } else {
        order.set('priceList', OB.MobileApp.model.get('terminal').priceList);
        order.set('priceIncludesTax', OB.MobileApp.model.get('pricelist').priceIncludesTax);
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
        if (OB.MobileApp.model.hasPermission('OBPOS_retail.restricttaxidinvoice', true) && !bp.get('taxID')) {
          if (OB.MobileApp.model.get('terminal').terminalType.generateInvoice) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
          } else {
            OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
          }
          order.set('generateInvoice', false);
        } else {
          order.set('generateInvoice', OB.MobileApp.model.get('terminal').terminalType.generateInvoice);
        }
      } else {
        order.set('generateInvoice', false);
      }
      order.set('currency', OB.MobileApp.model.get('terminal').currency);
      order.set('currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.MobileApp.model.get('terminal')['currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER]);
      order.set('warehouse', OB.MobileApp.model.get('terminal').warehouse);
      if (OB.MobileApp.model.get('context').user.isSalesRepresentative) {
        order.set('salesRepresentative', OB.MobileApp.model.get('context').user.id);
        order.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.MobileApp.model.get('context').user._identifier);
      } else {
        order.set('salesRepresentative', null);
        order.set('salesRepresentative' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, null);
      }
      order.set('posTerminal', OB.MobileApp.model.get('terminal').id);
      order.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.MobileApp.model.get('terminal')._identifier);
      order.set('orderDate', OB.I18N.normalizeDate(new Date()));
      order.set('creationDate', null);
      order.set('isPaid', false);
      order.set('paidOnCredit', false);
      order.set('isLayaway', false);
      order.set('isPartiallyDelivered', false);
      order.set('taxes', {});

      var nextDocumentno = OB.MobileApp.model.getNextDocumentno();
      order.set('documentnoPrefix', OB.MobileApp.model.get('terminal').docNoPrefix);
      order.set('documentnoSuffix', nextDocumentno.documentnoSuffix);
      order.set('documentNo', nextDocumentno.documentNo);
      order.set('print', true);
      order.set('sendEmail', false);
      order.set('openDrawer', false);
      OB.UTIL.HookManager.executeHooks('OBPOS_NewReceipt', {
        newOrder: order
      });
      return order;
    },

    newPaidReceipt: function (model, callback) {
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('newPaidReceipt');
      enyo.$.scrim.show();
      var order = new Order(),
          lines, newline, payments, curPayment, taxes, bpId, bpLocId, bpLoc, numberOfLines = model.receiptLines.length,
          orderQty = 0,
          NoFoundProduct = true,
          NoFoundCustomer = true,
          isLoadedPartiallyFromBackend = false;


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
      order.set('orderDate', OB.I18N.normalizeDate(model.orderDate));
      order.set('creationDate', OB.I18N.normalizeDate(model.creationDate));
      order.set('paidOnCredit', false);
      order.set('skipApplyPromotions', true);
      if (model.isQuotation) {
        order.set('isQuotation', true);
        order.set('oldId', model.orderid);
        order.set('id', null);
        order.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentTypeForQuotations);
        // TODO: this commented lines are kept just in case this issue happens again
        // Set creationDate milliseconds to 0, if the date is with milisecond, the date with miliseconds is rounded to seconds:
        // so, the second can change, and the creationDate in quotation should not be changed when quotation is reactivated
        // order.set('creationDate', moment(model.creationDate.toString(), "YYYY-MM-DD hh:m:ss").toDate());
      }
      if (model.isLayaway) {
        order.set('isLayaway', true);
        order.set('id', model.orderid);
        order.set('createdBy', OB.MobileApp.model.usermodel.id);
        order.set('hasbeenpaid', 'N');
        order.set('session', OB.MobileApp.model.get('session'));
      } else {
        order.set('isPaid', true);
        if (model.receiptPayments.length === 0 && model.totalamount > 0 && !model.isQuotation) {
          order.set('paidOnCredit', true);
        }
        order.set('id', model.orderid);
        if (order.get('documentType') === OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns) {
          //It's a return
          order.set('orderType', 1);
        }
      }
      bpLocId = model.bpLocId;
      bpId = model.bp;
      var bpartnerForProduct = function (bp) {
          var locationForBpartner = function (bpLoc) {
              bp.set('locName', bpLoc.get('name'));
              bp.set('locId', bpLoc.get('id'));
              bp.set('locationModel', bpLoc);
              order.set('bp', bp);
              order.set('gross', model.totalamount);
              order.set('net', model.totalNetAmount);

              var linepos = 0,
                  hasDeliveredProducts = false,
                  hasNotDeliveredProducts = false;
              _.each(model.receiptLines, function (iter) {
                var price;
                iter.linepos = linepos;
                var addLineForProduct = function (prod) {
                    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
                      OB.Dal.saveIfNew(prod, function () {
                        var productcriteria = {
                          columns: ['product'],
                          operator: 'equals',
                          value: prod.id,
                          isId: true
                        };
                        var remoteCriteria = [productcriteria];
                        var criteriaFilter = {};
                        criteriaFilter.remoteFilters = remoteCriteria;
                        OB.Dal.find(OB.Model.ProductCharacteristicValue, criteriaFilter, function (productcharacteristic) {
                          _.each(productcharacteristic.models, function (pchv) {
                            OB.Dal.saveIfNew(pchv, function () {}, function () {
                              OB.error(arguments);
                            });
                          });
                        }, function () {
                          OB.error(arguments);
                        });

                      }, function () {
                        OB.error(arguments);
                      });

                    }
                    // Set product services
                    order._loadRelatedServices(prod.get('productType'), prod.get('id'), prod.get('productCategory'), function (data) {
                      var hasservices;
                      if (!OB.UTIL.isNullOrUndefined(data) && OB.DEC.number(iter.quantity) > 0) {
                        hasservices = data.hasservices;
                      }
                      _.each(iter.promotions, function (promotion) {
                        OB.Dal.get(OB.Model.Discount, promotion.ruleId, function (discount) {
                          if (discount && OB.Model.Discounts.discountRules[discount.get('discountType')].addManual) {
                            var percentage;
                            if (discount.get('obdiscPercentage')) {
                              percentage = OB.DEC.mul(OB.DEC.div(promotion.amt, iter.linegrossamount), new BigDecimal('100'));
                            }
                            promotion.userAmt = percentage ? percentage : promotion.amt;
                            promotion.discountType = discount.get('discountType');
                            promotion.manual = true;
                          }
                        }, function (tx, error) {
                          OB.UTIL.showError("OBDAL error: " + error);
                        });
                      });
                      newline = new OrderLine({
                        id: iter.lineId,
                        product: prod,
                        uOM: iter.uOM,
                        qty: OB.DEC.number(iter.quantity),
                        price: price,
                        priceList: prod.get('listPrice'),
                        promotions: iter.promotions,
                        description: iter.description,
                        priceIncludesTax: order.get('priceIncludesTax'),
                        hasRelatedServices: hasservices,
                        warehouse: {
                          id: iter.warehouse,
                          warehousename: iter.warehousename
                        },
                        relatedLines: iter.relatedLines
                      });

                      // copy verbatim not owned properties -> modular properties.
                      _.each(iter, function (value, key) {
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
                        order.set('lines', lines);
                        order.set('qty', orderQty);
                        order.set('json', JSON.stringify(order.toJSON()));
                        callback(order);
                        enyo.$.scrim.hide();
                        OB.UTIL.SynchronizationHelper.finished(synchId, 'newPaidReceipt');
                      }
                    });
                    };

                if (order.get('priceIncludesTax')) {
                  price = OB.DEC.number(iter.unitPrice);
                } else {
                  price = OB.DEC.number(iter.baseNetUnitPrice);
                }

                if (!iter.remainingQuantity) {
                  hasNotDeliveredProducts = true;
                } else {
                  hasDeliveredProducts = true;
                  if (iter.remainingQuantity < iter.quantity) {
                    hasNotDeliveredProducts = true;
                  }
                }

                OB.Dal.get(OB.Model.Product, iter.id, function (product) {
                  addLineForProduct(product);
                }, null, function () {
                  //Empty
                  new OB.DS.Request('org.openbravo.retail.posterminal.master.LoadedProduct').exec({
                    productId: iter.id
                  }, function (data) {
                    addLineForProduct(OB.Dal.transform(OB.Model.Product, data[0]));
                  }, function () {
                    if (NoFoundProduct) {
                      NoFoundProduct = false;
                      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_NoReceiptLoadedText'), [{
                        label: OB.I18N.getLabel('OBPOS_LblOk'),
                        isConfirmButton: true
                      }]);
                    }
                  });
                });
                linepos++;
              });
              order.set('isPartiallyDelivered', hasDeliveredProducts && hasNotDeliveredProducts ? true : false);
              if (hasDeliveredProducts && !hasNotDeliveredProducts) {
                order.set('isFullyDelivered', true);
              }
              if (order.get('isPartiallyDelivered')) {
                var partiallyPaid = 0;
                _.each(_.filter(order.get('receiptLines'), function (reciptLine) {
                  return reciptLine.remainingQuantity;
                }), function (deliveredLine) {
                  partiallyPaid += deliveredLine.remainingQuantity * deliveredLine.unitPrice;
                });
                if (partiallyPaid) {
                  order.set('deliveredQuantityAmount', partiallyPaid);
                }
                if (order.get('deliveredQuantityAmount') && order.get('deliveredQuantityAmount') > order.get('gross')) {
                  order.set('isDeliveredGreaterThanGross', true);
                }
              }
              //order.set('payments', model.receiptPayments);
              payments = new PaymentLineList();
              _.each(model.receiptPayments, function (iter) {
                var paymentProp;
                curPayment = new PaymentLine();
                for (paymentProp in iter) {
                  if (iter.hasOwnProperty(paymentProp)) {
                    if (paymentProp === "paymentDate") {
                      if (!OB.UTIL.isNullOrUndefined(iter[paymentProp]) && moment(iter[paymentProp]).isValid()) {
                        curPayment.set(paymentProp, new Date(iter[paymentProp]));
                      } else {
                        curPayment.set(paymentProp, null);
                      }
                    } else {
                      curPayment.set(paymentProp, iter[paymentProp]);
                    }
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

              if (!model.isLayaway && !model.isQuotation) {
                if (model.totalamount > 0 && order.get('payment') < model.totalamount) {
                  order.set('paidOnCredit', true);
                } else if (model.totalamount < 0 && (order.get('payment') === 0 || (order.get('payment') > model.totalamount))) {
                  order.set('paidOnCredit', true);
                }
              }
              };

          if (isLoadedPartiallyFromBackend) {
            locationForBpartner(bpLoc);
          } else {
            OB.Dal.get(OB.Model.BPLocation, bpLocId, function (bpLoc) {
              locationForBpartner(bpLoc);
            }, function () {
              // TODO: Report errors properly
            });
          }
          };
      OB.Dal.get(OB.Model.BusinessPartner, bpId, function (bp) {
        bpartnerForProduct(bp);
      }, null, function () {
        //Empty
        new OB.DS.Request('org.openbravo.retail.posterminal.master.LoadedCustomer').exec({
          bpartnerId: bpId,
          bpLocationId: bpLocId
        }, function (data) {
          isLoadedPartiallyFromBackend = true;
          bpLoc = OB.Dal.transform(OB.Model.BPLocation, data[1]);
          bpartnerForProduct(OB.Dal.transform(OB.Model.BusinessPartner, data[0]));
        }, function () {
          if (NoFoundCustomer) {
            NoFoundCustomer = false;
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_NoReceiptLoadedText'), [{
              label: OB.I18N.getLabel('OBPOS_LblOk'),
              isConfirmButton: true
            }]);
          }
        });
      });
    },
    newDynamicOrder: function (model, callback) {
      var order = new OB.Model.Order(),
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

    addNewOrder: function (isFirstOrder) {
      var me = this;
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        me.doRemoteBPSettings(OB.MobileApp.model.get('businessPartner'));
      }
      this.saveCurrent();
      this.current = this.newOrder();
      this.unshift(this.current);
      this.loadCurrent(true);
    },

    addThisOrder: function (model) {
      this.saveCurrent();
      this.current = model;
      this.unshift(this.current);
      this.loadCurrent();
    },

    addFirstOrder: function () {
      this.addNewOrder(true);
    },

    addPaidReceipt: function (model) {
      var synchId = null;
      enyo.$.scrim.show();
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        this.doRemoteBPSettings(model.get('bp'));
      } else {
        OB.UTIL.showLoading(false);
      }

      this.saveCurrent();
      this.current = model;
      this.unshift(this.current);
      this.loadCurrent(true);

      if (model.get('isLayaway')) {
        synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('addPaidReceipt');
        // OB.Dal.save is done here because we want to force to save with the original id, only this time.
        OB.Dal.save(model, function () {
          enyo.$.scrim.hide();
          OB.UTIL.SynchronizationHelper.finished(synchId, 'addPaidReceipt');
        }, function () {
          OB.error(arguments);
        }, true);
      }

    },
    addMultiReceipt: function (model) {
      OB.Dal.save(model, function () {}, function () {
        OB.error(arguments);
      }, model.get('isLayaway'));
    },

    doRemoteBPSettings: function (businessPartner) {
      OB.Dal.saveIfNew(businessPartner, function () {}, function () {
        OB.error(arguments);
      });
      OB.Dal.saveIfNew(businessPartner.get('locationModel'), function () {}, function () {
        OB.error(arguments);
      });
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.discount.bp', true)) {
        var bp = {
          columns: ['businessPartner'],
          operator: 'equals',
          value: businessPartner.id,
          isId: true
        };
        var remoteCriteria = [bp];
        var criteria = {};
        criteria.remoteFilters = remoteCriteria;
        OB.Dal.find(OB.Model.DiscountFilterBusinessPartner, criteria, function (discountsBP) {
          _.each(discountsBP.models, function (dsc) {
            OB.Dal.saveIfNew(dsc, function () {}, function () {
              OB.error(arguments);
            });
          });
          OB.UTIL.showLoading(false);
        }, function () {
          OB.error(arguments);
        });
      } else {
        OB.UTIL.showLoading(false);
      }
    },

    addNewQuotation: function () {
      this.saveCurrent();
      this.current = this.newOrder();
      this.current.set('isQuotation', true);
      this.current.set('generateInvoice', false);
      this.current.set('orderType', 0);
      this.current.set('documentType', OB.MobileApp.model.get('terminal').terminalType.documentTypeForQuotations);
      var nextQuotationno = OB.MobileApp.model.getNextQuotationno();
      this.current.set('quotationnoPrefix', OB.MobileApp.model.get('terminal').quotationDocNoPrefix);
      this.current.set('quotationnoSuffix', nextQuotationno.quotationnoSuffix);
      this.current.set('documentNo', nextQuotationno.documentNo);

      this.unshift(this.current);
      this.loadCurrent();
    },
    deleteCurrentFromDatabase: function (orderToDelete) {
      OB.Dal.remove(orderToDelete, function () {
        return true;
      }, function () {
        OB.UTIL.showError('Error removing');
      });
    },
    deleteCurrent: function (forceCreateNew) {
      var i, max, me = this,
          successCallback = function () {
          return true;
          },
          errorCallback = function () {
          OB.UTIL.showError('Error removing');
          };
      if (!this.current) {
        return;
      }

      function finishDeleteCurrent() {
        me.remove(me.current);
        var createNew = forceCreateNew || me.length === 0;
        if (createNew) {
          var order = me.newOrder();
          if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true)) {
            order.save();
          }
          me.add(order);
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            me.doRemoteBPSettings(OB.MobileApp.model.get('businessPartner'));
          }
        }
        me.current = me.at(0);
        me.loadCurrent(createNew);
      }

      if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true) && !this.current.get('isQuotation') && OB.MobileApp.model.receipt.id === this.current.id && this.current.get('lines').length === 0 && (this.current.get('documentnoSuffix') <= OB.MobileApp.model.documentnoThreshold || OB.MobileApp.model.documentnoThreshold === 0)) {
        OB.MobileApp.model.receipt.setIsCalculateGrossLockState(true);
        OB.MobileApp.model.receipt.set('obposIsDeleted', true);
        OB.MobileApp.model.receipt.prepareToSend(function () {
          OB.MobileApp.model.receipt.trigger('closed', {
            callback: function () {
              OB.MobileApp.model.receipt.setIsCalculateGrossLockState(false);
              finishDeleteCurrent();
            }
          });
        });
      } else {
        finishDeleteCurrent();
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
        OB.UTIL.clone(this.modelorder, this.current);
        this.current.trigger('updateView');
      }
    },
    loadCurrent: function (isNew) {
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
        this.modelorder.setIsCalculateReceiptLockState(false);
        this.modelorder.setIsCalculateGrossLockState(false);
      }
    },
    synchronizeCurrentOrder: function () {
      // NOTE: No need to execute any business logic here
      // The new functionality of loading document no, makes this function obsolete.
      // The function is not removed to avoid api changes
    }
  });

  var MultiOrders = Backbone.Model.extend({
    modelName: 'MultiOrders',
    defaults: {
      total: OB.DEC.Zero,
      payment: OB.DEC.Zero,
      pending: OB.DEC.Zero,
      change: OB.DEC.Zero,
      openDrawer: false,
      additionalInfo: null
    },
    initialize: function () {
      this.set('multiOrdersList', new Backbone.Collection());
      this.set('payments', new Backbone.Collection());
      // ISSUE 24487: Callbacks of this collection still exists if you come back from other page.
      // Force to remove callbacks
      this.get('multiOrdersList').off();
    },
    getPaymentStatus: function () {
      var total = OB.DEC.abs(this.getTotal());
      var pay = this.getPayment();
      return {
        'total': OB.I18N.formatCurrency(total),
        'pending': OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.I18N.formatCurrency(OB.DEC.Zero) : OB.I18N.formatCurrency(OB.DEC.sub(total, pay)),
        'change': OB.DEC.compare(this.getChange()) > 0 ? OB.I18N.formatCurrency(this.getChange()) : null,
        'overpayment': OB.DEC.compare(OB.DEC.sub(pay, total)) > 0 ? OB.I18N.formatCurrency(OB.DEC.sub(pay, total)) : null,
        'isReturn': this.get('gross') < 0 ? true : false,
        'isNegative': this.get('gross') < 0 ? true : false,
        'changeAmt': this.getChange(),
        'pendingAmt': OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.DEC.Zero : OB.DEC.sub(total, pay),
        'payments': this.get('payments')
      };
    },
    getPrecision: function (payment) {
      var i, p, max;
      for (i = 0, max = OB.MobileApp.model.get('payments').length; i < max; i++) {
        p = OB.MobileApp.model.get('payments')[i];
        if (p.payment.searchKey === payment.paymenttype) {
          if (p.obposPrecision) {
            return p.obposPrecision;
          }
        }
      }
    },
    adjustPayment: function () {
      var i, max, p;
      var payments = this.get('payments');
      var total = OB.DEC.abs(this.getTotal());

      var nocash = OB.DEC.Zero;
      var cash = OB.DEC.Zero;
      var origCash = OB.DEC.Zero;
      var auxCash = OB.DEC.Zero;
      var prevCash = OB.DEC.Zero;
      var paidCash = OB.DEC.Zero;
      var pcash;
      var precision;

      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        precision = this.getPrecision(p);
        if (p.get('rate') && p.get('rate') !== '1') {
          p.set('origAmount', OB.DEC.div(p.get('amount'), p.get('mulrate')));
        } else {
          p.set('origAmount', p.get('amount'));
        }
        p.set('paid', p.get('origAmount'));
        if (p.get('kind') === OB.MobileApp.model.get('paymentcash')) {
          // The default cash method
          cash = OB.DEC.add(cash, p.get('origAmount'));
          pcash = p;
          paidCash = OB.DEC.add(paidCash, p.get('origAmount'));
        } else if (OB.MobileApp.model.hasPayment(p.get('kind')) && OB.MobileApp.model.hasPayment(p.get('kind')).paymentMethod.iscash) {
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
        if (pcash.get('kind') !== OB.MobileApp.model.get('paymentcash')) {
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
          this.set('change', OB.DEC.sub(OB.DEC.add(OB.DEC.add(nocash, cash, precision), origCash, precision), OB.Utilities.Number.roundJSNumber(total, 2), precision));
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
      var i, max, p, order;

      if (!OB.DEC.isNumber(payment.get('amount'))) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_MsgPaymentAmountError'));
        return;
      }
      if (this.stopAddingPayments) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CannotAddPayments'));
        return;
      }

      payments = this.get('payments');
      total = OB.DEC.abs(this.getTotal());
      order = this;
      OB.UTIL.HookManager.executeHooks('OBPOS_preAddPayment', {
        paymentToAdd: payment,
        payments: payments,
        receipt: this
      }, function () {
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
              payment.set('date', new Date());
              order.adjustPayment();
              order.trigger('displayTotal');
              return;
            }
          }
        } else {
          for (i = 0, max = payments.length; i < max; i++) {
            p = payments.at(i);
            if (p.get('kind') === payment.get('kind') && p.get('paymentData') && payment.get('paymentData') && p.get('paymentData').groupingCriteria && payment.get('paymentData').groupingCriteria && p.get('paymentData').groupingCriteria === payment.get('paymentData').groupingCriteria) {
              p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
              if (p.get('rate') && p.get('rate') !== '1') {
                p.set('origAmount', OB.DEC.add(payment.get('origAmount'), OB.DEC.mul(p.get('origAmount'), p.get('rate'))));
              }
              payment.set('date', new Date());
              order.adjustPayment();
              order.trigger('displayTotal');
              return;
            }
          }
        }
        if (payment.get('openDrawer') && (payment.get('allowOpenDrawer') || payment.get('isCash'))) {
          order.set('openDrawer', payment.get('openDrawer'));
        }
        payment.set('date', new Date());
        payment.set('id', OB.UTIL.get_UUID());
        payments.add(payment);
        order.adjustPayment();
        order.trigger('displayTotal');
      });
    },
    removePayment: function (payment) {
      var payments = this.get('payments');
      payments.remove(payment);
      if (payment.get('openDrawer')) {
        this.set('openDrawer', false);
      }
      this.adjustPayment();
    },
    printGross: function () {
      return OB.I18N.formatCurrency(this.getTotal());
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
      return OB.DEC.sub(OB.DEC.abs(this.getTotal()), this.getPayment());
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

  OB.Collection.OrderLineList = OB.Collection.OrderLineList.extend({
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
  // order model is not registered using standard Registry method because list is
  // because collection is specific
  window.OB.Model.Order = Order;
  window.OB.Collection.OrderList = OrderList;
  window.OB.Model.TaxLine = TaxLine;
  window.OB.Model.MultiOrders = MultiOrders;

  window.OB.Model.modelLoaders = [];
}());