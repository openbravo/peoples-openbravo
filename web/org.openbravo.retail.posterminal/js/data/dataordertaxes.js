/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_ */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  // Order taxes in descent order by lineNo
  OB.Collection.TaxRateList.prototype.comparator = function (tax) {
    return tax.get('lineNo');
  };

  OB.DATA.OrderTaxes = function (modelOrder) {
    this._id = 'logicOrderTaxes';

    this.receipt = modelOrder;

    this.receipt.calculateTaxes = function (callback) {
      var me = this,
          bpTaxCategory = this.get('bp').get('taxCategory'),
          lines = this.get('lines'),
          len = lines.length,
          taxes = {},
          totalnet = OB.DEC.Zero,
          queue = {},
          triggerNext = false,
          gross = OB.DEC.Zero;
      if (len === 0) {
        me.set('taxes', {});
        if (callback) {
          callback();
        }
        return;
      }
      if (this.get('priceIncludesTax')) {

        _.each(lines.models, function (element, index, list) {
          var product = element.get('product');

          // OB.Dal.find(model, criteria, success, error);
          OB.Dal.find(OB.Model.TaxRate, {
            taxCategory: product.get('taxCategory'),
            businessPartnerTaxCategory: bpTaxCategory
          }, function (coll, args) { // success
            var rate, taxAmt, net, gross, pricenet, pricenetcascade, amount, taxId;
            if (coll && coll.length > 0) {

              var linerate = BigDecimal.prototype.ONE;
              var linetaxid = coll.at(0).get('id');
              var validFromDate = coll.at(0).get('validFromDate');
              coll = _.filter(coll.models, function (taxRate) {
                return taxRate.get('validFromDate') === validFromDate;
              });

              // First calculate the line rate.
              _.each(coll, function (taxRate, taxIndex) {

                if (!taxRate.get('summaryLevel')) {
                  rate = new BigDecimal(String(taxRate.get('rate'))); // 10
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY); // 0.10 
                  if (taxRate.get('cascade')) {
                    linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                  } else {
                    linerate = linerate.add(rate);
                  }
                } else {
                  linetaxid = taxRate.get('id');
                }
              }, this);

              var linepricenet = OB.DEC.div(element.get('grossUnitPrice') || element.get('price'), linerate);
              var linenet = OB.DEC.mul(linepricenet, element.get('qty'));
              var linegross = element.get('lineGrossAmount') || element.get('gross');

              element.set('linerate', linerate);
              element.set('tax', linetaxid);
              element.set('taxAmount', OB.DEC.sub(linegross, linenet));
              element.set('net', linenet);
              element.set('pricenet', linepricenet);

              totalnet = OB.DEC.add(totalnet, linenet);

              pricenet = new BigDecimal(String(linepricenet)); // 2 decimals properly rounded.
              pricenetcascade = pricenet;
              // second calculate tax lines.
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');

                  rate = new BigDecimal(String(taxRate.get('rate')));
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY);

                  if (taxRate.get('cascade')) {
                    pricenet = pricenetcascade;
                  }
                  gross = OB.DEC.mul(element.get('discountedLinePrice') || element.get('price'), element.get('qty'));
                  net = OB.DEC.div(gross, OB.DEC.add(1, rate));
                  amount = OB.DEC.sub(gross, net);
                  pricenetcascade = pricenet.multiply(rate.add(BigDecimal.prototype.ONE));

                  if (taxes[taxId]) {
                    taxes[taxId].net = OB.DEC.add(taxes[taxId].net, net);
                    taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, amount);
                  } else {
                    taxes[taxId] = {};
                    taxes[taxId].name = taxRate.get('name');
                    taxes[taxId].rate = taxRate.get('rate');
                    taxes[taxId].net = net;
                    taxes[taxId].amount = amount;
                  }
                }
              }, this);

              // processed = yes
              queue[element.cid] = true;

              // checking queue status
              triggerNext = OB.UTIL.queueStatus(queue);

              // triggering next steps
              if (triggerNext) {
                me.set('taxes', taxes);
                me.set('net', totalnet);
                if (callback) {
                  callback();
                }
              }
            } else {
              OB.UTIL.showError("OBDAL error: Not tax found for " + args.get('_identifier'));
            }
          }, function (tx, error) { // error
            OB.UTIL.showError("OBDAL error: " + error);
          }, product);

          // add line to queue of pending to be processed
          queue[element.cid] = false;
        });
      } else {
        //In case the pricelist doesn't include taxes, the way to calculate taxes is different
        _.each(lines.models, function (element, index, list) {
          var product = element.get('product');

          // OB.Dal.find(model, criteria, success, error);
          OB.Dal.find(OB.Model.TaxRate, {
            taxCategory: product.get('taxCategory'),
            businessPartnerTaxCategory: bpTaxCategory
          }, function (coll, args) { // success
            var rate, taxAmt, net, pricenet, pricenetcascade, amount, taxId;
            if (coll && coll.length > 0) {

              var linerate = BigDecimal.prototype.ONE;
              var linetaxid = coll.at(0).get('id');
              var validFromDate = coll.at(0).get('validFromDate');
              coll = _.filter(coll.models, function (taxRate) {
                return taxRate.get('validFromDate') === validFromDate;
              });

              // First calculate the line rate.
              _.each(coll, function (taxRate, taxIndex) {

                if (!taxRate.get('summaryLevel')) {
                  rate = new BigDecimal(String(taxRate.get('rate'))); // 10
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY); // 0.10 
                  if (taxRate.get('cascade')) {
                    linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                  } else {
                    linerate = linerate.add(rate);
                  }
                } else {
                  linetaxid = taxRate.get('id');
                }
              }, this);

              var linepricenet = element.get('price');
              var discountedprice = element.get('discountedLinePrice') || element.get('price');
              var linenet = OB.DEC.mul(linepricenet, element.get('qty'));
              var linegross = OB.DEC.mul(linenet, linerate);
              var linepricegross = OB.DEC.mul(linepricenet, linerate);

              element.set('linerate', String(linerate));
              element.set('tax', linetaxid);
              element.set('taxAmount', OB.DEC.mul(OB.DEC.mul(discountedprice, element.get('qty')), linerate));
              element.set('net', linenet);
              element.set('pricenet', linepricenet);
              element.set('gross', linegross);
              element.set('discountedGross', OB.DEC.mul(OB.DEC.mul(discountedprice, element.get('qty')), linerate));
              element.set('discountedNet', OB.DEC.mul(discountedprice, element.get('qty')));
              element.set('taxAmount', OB.DEC.sub(element.get('discountedGross'), element.get('discountedNet')));
              element.set('discountedNetPrice', discountedprice);

              totalnet = OB.DEC.add(totalnet, linenet);

              pricenet = new BigDecimal(String(linepricenet)); // 2 decimals properly rounded.
              pricenetcascade = pricenet;
              // second calculate tax lines.
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');

                  rate = new BigDecimal(String(taxRate.get('rate')));
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY);

                  if (taxRate.get('cascade')) {
                    pricenet = pricenetcascade;
                  }
                  net = OB.DEC.mul(element.get('discountedLinePrice') || pricenet, element.get('qty'));
                  amount = OB.DEC.mul(net, rate);
                  pricenetcascade = pricenet.multiply(rate.add(BigDecimal.prototype.ONE));

                  if (taxes[taxId]) {
                    taxes[taxId].net = OB.DEC.add(taxes[taxId].net, net);
                    taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, amount);
                  } else {
                    taxes[taxId] = {};
                    taxes[taxId].name = taxRate.get('name');
                    taxes[taxId].rate = taxRate.get('rate');
                    taxes[taxId].net = net;
                    taxes[taxId].amount = amount;
                  }
                }
              }, this);

              // processed = yes
              queue[element.cid] = true;

              // checking queue status
              triggerNext = OB.UTIL.queueStatus(queue);

              // triggering next steps
              if (triggerNext) {
                me.set('taxes', taxes);
                if (callback) {
                  callback();
                }
              }
            } else {
              OB.UTIL.showError("OBDAL error: Not tax found for " + args.get('_identifier'));
            }
          }, function (tx, error) { // error
            OB.UTIL.showError("OBDAL error: " + error);
          }, product);

          // add line to queue of pending to be processed
          queue[element.cid] = false;
        });
      }
    };
  };
}());