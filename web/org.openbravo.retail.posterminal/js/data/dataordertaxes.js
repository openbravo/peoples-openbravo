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
          taxesline = {},
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

              var discountedGross = null;
              if (element.get('promotions')) {
                discountedGross = element.get('gross');
                discountedGross = element.get('promotions').reduce(function (memo, element) {
                  return OB.DEC.sub(memo, element.actualAmt || element.amt || 0);
                }, discountedGross);
              }
              var orggross = OB.DEC.mul(element.get('grossUnitPrice') || element.get('price'), element.get('qty'));
              
              // First calculate the line rate.
              var linerate = BigDecimal.prototype.ONE;
              var linetaxid = coll.at(0).get('id');
              var validFromDate = coll.at(0).get('validFromDate');
              var taxamt = orggross;
              var taxamtdc;
              if(discountedGross){
                taxamtdc = discountedGross;
              }
              coll = _.filter(coll.models, function (taxRate) {
                return taxRate.get('validFromDate') === validFromDate;
              });
              _.each(coll, function (taxRate, taxIndex) {

                if (!taxRate.get('summaryLevel')) {
                  rate = new BigDecimal(String(taxRate.get('rate'))); // 10
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY); // 0.10 
                  if (taxRate.get('cascade')) {
                    linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                    taxamt = OB.DEC.mul(taxamt, OB.DEC.add(1,rate));
                    if(discountedGross){
                      taxamtdc = OB.DEC.mul(taxamtdc, OB.DEC.add(1,rate));
                    }
                  } else {
                    linerate = linerate.add(rate);
                    taxamt = OB.DEC.add(taxamt,OB.DEC.mul(orggross, rate));
                    if(discountedGross){
                      taxamtdc = OB.DEC.add(taxamtdc,OB.DEC.mul(discountedGross, rate));
                    }
                  }
                } else {
                  linetaxid = taxRate.get('id');
                }
              }, this);

              
              // the line net price is calculated by doing price*price/(price*rate), as it is done in
              // the database function c_get_net_price_from_gross
              
              var linenet = OB.DEC.div(OB.DEC.mul(orggross, orggross), taxamt);
              var linepricenet = OB.DEC.div(linenet, element.get('qty'));
              var linegross = element.get('lineGrossAmount') || element.get('gross');

              element.set('linerate', linerate);
              element.set('tax', linetaxid);
              element.set('taxAmount', OB.DEC.sub(linegross, linenet));
              element.set('net', linenet);
              element.set('pricenet', linepricenet);

              totalnet = OB.DEC.add(totalnet, linenet);
              
              //We follow the same formula of function c_get_net_price_from_gross to compute the discounted net
              if (discountedGross) {
                var discountedNet = OB.DEC.div(OB.DEC.mul(discountedGross,discountedGross), taxamtdc);
                pricenet = OB.DEC.div(discountedNet, element.get('qty'));
              } else {
                pricenet = new BigDecimal(String(linepricenet)); // 2 decimals properly rounded.
              }
              element.set('discountedNet', OB.DEC.mul(pricenet, element.get('qty')));
              pricenetcascade = pricenet;
              // second calculate tax lines.
              taxesline = {};
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');

                  rate = new BigDecimal(String(taxRate.get('rate')));
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY);

                  if (taxRate.get('cascade')) {
                    pricenet = pricenetcascade;
                  }
                  net = OB.DEC.mul(pricenet, element.get('qty'));
                  amount = OB.DEC.mul(net, rate);
                  pricenetcascade = OB.DEC.mul(pricenet, (rate.add(BigDecimal.prototype.ONE)));

                  taxesline[taxId] = {};
                  taxesline[taxId].name = taxRate.get('name');
                  taxesline[taxId].rate = taxRate.get('rate');
                  taxesline[taxId].net = net;
                  taxesline[taxId].amount = amount;
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

              // We need to make a final adjustment: we will sum all the tax lines,
              // and if the net amount of the line plus this sum is not equal to the gross,
              // we will adjust the tax line with the greatest amount
              var summedTaxAmt = 0;
              var expectedGross = discountedGross ? discountedGross : element.get('gross');
              var greaterTax = null;
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');
                  summedTaxAmt = OB.DEC.add(summedTaxAmt, taxesline[taxId].amount);
                  if(greaterTax === null || OB.DEC.abs(taxesline[greaterTax].amount) < OB.DEC.abs(taxesline[taxId].amount)){
                    greaterTax = taxId;
                  }
                }
              });
              var netandtax;
              if(discountedGross){
                netandtax = OB.DEC.add(discountedNet,summedTaxAmt)
              }else{
                netandtax = OB.DEC.add(OB.DEC.mul(linepricenet, element.get('qty')),summedTaxAmt)
              }
              if(expectedGross !== netandtax){
                //An adjustment is needed
                taxes[greaterTax].amount = OB.DEC.add(taxes[greaterTax].amount, OB.DEC.sub(expectedGross, netandtax));
                taxesline[greaterTax].amount = OB.DEC.add(taxesline[greaterTax].amount, OB.DEC.sub(expectedGross, netandtax));
              }
              element.set('taxLines', taxesline);

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



              var discAmt = null;
              if (element.get('promotions')) {
                discAmt = element.get('net');
                discAmt = element.get('promotions').reduce(function (memo, element) {
                  return OB.DEC.sub(memo, element.actualAmt || element.amt || 0);
                }, discAmt);
              }
              var linepricenet = element.get('price');
              var discountedprice = discAmt ? OB.DEC.div(discAmt, element.get('qty')) : element.get('price');
              var linenet = OB.DEC.mul(linepricenet, element.get('qty'));

              var discountedNet = OB.DEC.mul(discountedprice, element.get('qty'));
              var discountedGross = discountedNet;
              var linegross = linenet;
              // First calculate the line rate.
              _.each(coll, function (taxRate, taxIndex) {

                if (!taxRate.get('summaryLevel')) {
                  rate = new BigDecimal(String(taxRate.get('rate'))); // 10
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY); // 0.10 
                  if (taxRate.get('cascade')) {
                    linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                    linegross = OB.DEC.mul(linegross, rate.add(BigDecimal.prototype.ONE));
                    discountedGross =  OB.DEC.mul(discountedGross, rate.add(BigDecimal.prototype.ONE));
                  } else {
                    linerate = linerate.add(rate);
                    linegross = OB.DEC.add(linegross, OB.DEC.mul(linenet, rate));
                    discountedGross = OB.DEC.add(discountedNet, OB.DEC.mul(discountedNet, rate));
                  }
                } else {
                  linetaxid = taxRate.get('id');
                }
              }, this);              
              
              var linepricegross = OB.DEC.div(linegross, element.get('qty'));

              element.set('linerate', String(linerate));
              element.set('tax', linetaxid);
              element.set('taxAmount', OB.DEC.mul(OB.DEC.mul(discountedprice, element.get('qty')), linerate));
              element.set('net', linenet);
              element.set('pricenet', linepricenet);
              element.set('gross', linegross);
              element.set('discountedGross', discountedGross);
              element.set('discountedNet', discountedNet);
              element.set('taxAmount', OB.DEC.sub(element.get('discountedGross'), element.get('discountedNet')));
              element.set('discountedNetPrice', discountedprice);

              totalnet = OB.DEC.add(totalnet, linenet);

              pricenet = new BigDecimal(String(discountedprice)) || (new BigDecimal(String(linepricenet))); // 2 decimals properly rounded.
              pricenetcascade = pricenet;
              // second calculate tax lines.
              taxesline = {};
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');

                  rate = new BigDecimal(String(taxRate.get('rate')));
                  rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY);

                  if (taxRate.get('cascade')) {
                    pricenet = pricenetcascade;
                  }
                  net = OB.DEC.mul(pricenet, element.get('qty'));
                  amount = OB.DEC.mul(net, rate);
                  pricenetcascade = pricenet.multiply(rate.add(BigDecimal.prototype.ONE));

                  taxesline[taxId] = {};
                  taxesline[taxId].name = taxRate.get('net');
                  taxesline[taxId].rate = taxRate.get('rate');
                  taxesline[taxId].net = net;
                  taxesline[taxId].amount = amount;
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
              element.set('taxLines', taxesline);

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