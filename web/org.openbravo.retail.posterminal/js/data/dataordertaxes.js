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
  //  OB.Collection.TaxRateList.prototype.comparator = function (tax) {
  //    return tax.get('lineNo');
  //  };
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
          discountedNet, gross = OB.DEC.Zero;
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
              var taxamt = new BigDecimal(String(orggross));
              var taxamtdc;
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                taxamtdc = new BigDecimal(String(discountedGross));
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
                    taxamt = taxamt.multiply(new BigDecimal(String(OB.DEC.add(1, rate))));
                    if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                      taxamtdc = taxamtdc.multiply(new BigDecimal(String(OB.DEC.add(1, rate))));
                    }
                  } else {
                    linerate = linerate.add(rate);
                    taxamt = taxamt.add(new BigDecimal(String(orggross)).multiply(rate));
                    if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                      taxamtdc = taxamtdc.add(new BigDecimal(String(new BigDecimal(String(discountedGross)).multiply(rate))));
                    }
                  }
                } else {
                  linetaxid = taxRate.get('id');
                }
              }, this);


              // the line net price is calculated by doing price*price/(price*rate), as it is done in
              // the database function c_get_net_price_from_gross
              var linenet, linepricenet, linegross;
              if (orggross === 0) {
                linenet = new BigDecimal('0');
                linepricenet = new BigDecimal('0');
                linegross = 0;
              } else {
                linenet = new BigDecimal(String(orggross)).multiply(new BigDecimal(String(orggross))).divide(new BigDecimal(String(taxamt)));
                linepricenet = linenet.divide(new BigDecimal(String(element.get('qty'))));
                linegross = element.get('lineGrossAmount') || element.get('gross');
              }

              element.set('linerate', linerate);
              element.set('tax', linetaxid);
              element.set('taxAmount', OB.DEC.sub(linegross, linenet));
              element.set('net', OB.DEC.toNumber(linenet));
              element.set('pricenet', OB.DEC.toNumber(linepricenet));

              totalnet = OB.DEC.add(totalnet, linenet);

              //We follow the same formula of function c_get_net_price_from_gross to compute the discounted net
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                if (taxamtdc && OB.DEC.toNumber(taxamtdc)!==0) {
                  discountedNet = OB.DEC.div(new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))), taxamtdc);
                  pricenet = new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))).divide(taxamtdc).divide(new BigDecimal(String(element.get('qty'))));
                } else {
                  //taxamtdc === 0
                  discountedNet = new BigDecimal("0");
                  pricenet = new BigDecimal("0");
                }
              } else {
                pricenet = linepricenet; // 2 decimals properly rounded.
              }
              element.set('discountedNet', pricenet.multiply(new BigDecimal(String(element.get('qty')))));
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
                  taxesline[taxId].name = taxRate.get('name');
                  taxesline[taxId].rate = taxRate.get('rate');
                  taxesline[taxId].net = OB.DEC.toNumber(new BigDecimal(String(pricenet)).multiply(new BigDecimal(String(element.get('qty')))));
                  taxesline[taxId].amount = amount;
                  taxesline[taxId].fullamount = new BigDecimal(String(net)).multiply(rate);
                  taxesline[taxId].fullnet = new BigDecimal(String(pricenet)).multiply(new BigDecimal(String(element.get('qty'))));
                }
              }, this);

              // We need to make a final adjustment: we will sum all the tax lines,
              // and if the net amount of the line plus this sum is not equal to the gross,
              // we will adjust the tax line with the greatest amount
              var summedTaxAmt = 0;
              var expectedGross;
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                expectedGross = discountedGross;
              } else {
                expectedGross = element.get('gross');
              }

              var greaterTax = null;
              _.each(coll, function (taxRate, taxIndex) {
                if (!taxRate.get('summaryLevel')) {
                  taxId = taxRate.get('id');
                  summedTaxAmt = OB.DEC.add(summedTaxAmt, taxesline[taxId].amount);
                  if (me.get('orderType') === 1) {
                    if (greaterTax === null || taxesline[greaterTax].amount > taxesline[taxId].amount) {
                      greaterTax = taxId;
                    }
                  } else {
                    if (greaterTax === null || taxesline[greaterTax].amount < taxesline[taxId].amount) {
                      greaterTax = taxId;
                    }
                  }
                }
              });
              var netandtax;
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                netandtax = OB.DEC.add(discountedNet, summedTaxAmt);
              } else {
                netandtax = OB.DEC.add(OB.DEC.mul(pricenet, element.get('qty')), summedTaxAmt);
              }
              if (expectedGross !== netandtax) {
                //An adjustment is needed
                taxesline[greaterTax].amount = OB.DEC.add(taxesline[greaterTax].amount, OB.DEC.sub(expectedGross, netandtax));
              }
              element.set('taxLines', taxesline);

              // processed = yes
              queue[element.cid] = true;

              // checking queue status
              triggerNext = OB.UTIL.queueStatus(queue);


              _.each(coll, function (taxRate, taxIndex) {
                var taxId = taxRate.get('id');
                delete taxes[taxId];
                me.get('lines').each(function (line, taxIndex) {
                  var taxLines = line.get('taxLines');
                  if(!taxLines || !taxLines[taxId]){
                    return;
                  }
                  if (!taxRate.get('summaryLevel')) {
                    if (taxes[taxId]) {
                      taxes[taxId].net = taxes[taxId].net.add(taxLines[taxId].fullnet);
                      taxes[taxId].amount =taxes[taxId].amount.add(taxLines[taxId].fullamount);
                    } else {
                      taxes[taxId] = {};
                      taxes[taxId].name = taxRate.get('name');
                      taxes[taxId].rate = taxRate.get('rate');
                      taxes[taxId].net = taxLines[taxId].fullnet;
                      taxes[taxId].amount = taxLines[taxId].fullamount;
                    }
                  }
                });
              });

              _.each(coll, function (taxRate, taxIndex) {
                var taxId = taxRate.get('id');
                if(taxes[taxId]){
                  taxes[taxId].net = OB.DEC.toNumber(taxes[taxId].net);
                  taxes[taxId].amount = OB.DEC.toNumber(taxes[taxId].amount);
                }
              });
              
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
          if (element.get('ignoreTaxes') === true || product.get('ignoreTaxes') === true) {
            var taxLine = {};
            element.set('linerate', BigDecimal.prototype.ONE);
            element.set('tax', OB.MobileApp.model.get('terminal').taxexempid);
            element.set('taxAmount', OB.DEC.Zero);
            element.set('net', element.get('net'));
            element.set('pricenet', element.get('net'));
            element.set('gross', element.get('net'));
            element.set('discountedGross', element.get('net'));
            element.set('discountedNet', new BigDecimal(String(element.get('net'))));
            element.set('taxAmount', OB.DEC.Zero);
            element.set('discountedNetPrice', new BigDecimal(String(element.get('net'))));
            taxLine[OB.MobileApp.model.get('terminal').taxexempid] = {
              amount: 0,
              rate: 0,
              net: element.get('net')
            };
            element.set('taxLines', taxLine);
          } else {
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
                  discAmt = new BigDecimal(String(element.get('net')));
                  discAmt = element.get('promotions').reduce(function (memo, element) {
                    return memo.subtract(new BigDecimal(String(element.actualAmt || element.amt || 0)));
                  }, discAmt);
                }
                var linepricenet = element.get('price');
                var discountedprice;
                if (!(_.isNull(discAmt) || _.isUndefined(discAmt))) {
                  discountedprice = discAmt.divide(new BigDecimal(String(element.get('qty'))));
                  discountedNet = discAmt;
                } else {
                  discountedprice = new BigDecimal(String(element.get('price')));
                  discountedNet = discountedprice.multiply(new BigDecimal(String(element.get('qty'))));
                }

                var linenet = OB.DEC.mul(linepricenet, element.get('qty'));

                var discountedGross = new BigDecimal(String(discountedNet));
                var linegross = new BigDecimal(String(linenet));
                // First calculate the line rate.
                _.each(coll, function (taxRate, taxIndex) {

                  if (!taxRate.get('summaryLevel')) {
                    rate = new BigDecimal(String(taxRate.get('rate'))); // 10
                    rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY); // 0.10
                    if (taxRate.get('cascade')) {
                      linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                      linegross = linegross.multiply(rate.add(BigDecimal.prototype.ONE));
                      discountedGross = discountedGross.multiply(rate.add(BigDecimal.prototype.ONE));
                    } else {
                      linerate = linerate.add(rate);
                      linegross = linegross.add(new BigDecimal(String(linenet)).multiply(new BigDecimal(String(rate))));
                      discountedGross = discountedGross.add(new BigDecimal(String(discountedNet)).multiply(rate));
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
                element.set('gross', OB.DEC.toNumber(linegross));
                element.set('fullgross', linegross);
                element.set('discountedGross', OB.DEC.toNumber(discountedGross));
                element.set('fulldiscountedGross', discountedGross);
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
                      taxes[taxId].net = taxes[taxId].fullnet.add(pricenet.multiply(new BigDecimal(String(element.get('qty')))));
                      taxes[taxId].fullnet = taxes[taxId].fullnet.add(pricenet.multiply(new BigDecimal(String(element.get('qty')))));
                      taxes[taxId].amount = taxes[taxId].fullamount.add(pricenet.multiply(new BigDecimal(String(element.get('qty')))).multiply(rate));
                      taxes[taxId].fullamount = taxes[taxId].fullamount.add(pricenet.multiply(new BigDecimal(String(element.get('qty')))).multiply(rate));
                    } else {
                      taxes[taxId] = {};
                      taxes[taxId].name = taxRate.get('name');
                      taxes[taxId].rate = taxRate.get('rate');
                      taxes[taxId].net = pricenet.multiply(new BigDecimal(String(element.get('qty'))));
                      taxes[taxId].fullnet = pricenet.multiply(new BigDecimal(String(element.get('qty'))));
                      taxes[taxId].amount = taxes[taxId].fullnet.multiply(rate);
                      taxes[taxId].fullamount = taxes[taxId].fullnet.multiply(rate);
                    }
                  }
                }, this);
                element.set('taxLines', taxesline);

                // processed = yes
                queue[element.cid] = true;

                // checking queue status
                triggerNext = OB.UTIL.queueStatus(queue);

                _.each(coll, function (taxRate, taxIndex) {
                  var taxId = taxRate.get('id');
                  if(taxes[taxId]){
                    taxes[taxId].net = OB.DEC.toNumber(taxes[taxId].net);
                    taxes[taxId].amount = OB.DEC.toNumber(taxes[taxId].amount);
                  }
                });
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
          }
        });
      }
    };
  };
}());