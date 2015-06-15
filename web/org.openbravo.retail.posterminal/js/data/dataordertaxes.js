/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window,Promise,Backbone,_ */

(function () {

  function navigateTaxesTree(taxrates, taxid, iteratee) {
    _.each(taxrates, function (tax) {
      if (tax.get('taxBase') === taxid) {
        iteratee(tax);
        navigateTaxesTree(taxrates, tax.get('id'), iteratee);
      }
    });
  }

  var isTaxCategoryBOM = function (taxcategory) {
      return new Promise(function (fulfill) {
        OB.Dal.findUsingCache('taxCategoryBOM', OB.Model.TaxCategoryBOM, {
          'id': taxcategory
        }, function (data) {
          fulfill(data.length > 0);
        });
      });
      };

  var getProductBOM = function (product) {
      return new Promise(function (fulfill) {
        OB.Dal.findUsingCache('taxProductBOM', OB.Model.ProductBOM, {
          'product': product
        }, fulfill);
      });
      };

  var findTaxesCollection = function (receipt, line, product) {
      return new Promise(function (fulfill, reject) {
        // sql parameters 
        var fromRegionOrg = OB.MobileApp.model.get('terminal').organizationRegionId,
            fromCountryOrg = OB.MobileApp.model.get('terminal').organizationCountryId,
            bpTaxCategory = receipt.get('bp').get('taxCategory'),
            bpIsExempt = receipt.get('bp').get('taxExempt'),
            bpLocId = receipt.get('bp').get('locId');
        // SQL build
        // the query is ordered by countryId desc and regionId desc
        // (so, the first record will be the tax with the same country or
        // region that the customer,
        // or if toCountryId and toRegionId are nulls then will be ordered
        // by validfromdate)            
        var sql = "select c_tax.c_tax_id, c_tax.name,  c_tax.description, c_tax.taxindicator, c_tax.validfrom, c_tax.issummary, c_tax.rate, c_tax.parent_tax_id, (case when c_tax.c_country_id = '" + fromCountryOrg + "' then c_tax.c_country_id else tz.from_country_id end) as c_country_id, (case when c_tax.c_region_id = '" + fromRegionOrg + "' then c_tax.c_region_id else tz.from_region_id end) as c_region_id, (case when c_tax.to_country_id = bpl.countryId then c_tax.to_country_id else tz.to_country_id end) as to_country_id, (case when c_tax.to_region_id = bpl.regionId then c_tax.to_region_id else tz.to_region_id end)  as to_region_id, c_tax.c_taxcategory_id, c_tax.isdefault, c_tax.istaxexempt, c_tax.sopotype, c_tax.cascade, c_tax.c_bp_taxcategory_id,  c_tax.line, c_tax.iswithholdingtax, c_tax.isnotaxable, c_tax.deducpercent, c_tax.originalrate, c_tax.istaxundeductable,  c_tax.istaxdeductable, c_tax.isnovat, c_tax.baseamount, c_tax.c_taxbase_id, c_tax.doctaxamount, c_tax.iscashvat,  c_tax._identifier,  c_tax._idx,  (case when (c_tax.to_country_id = bpl.countryId or tz.to_country_id= bpl.countryId) then 0 else 1 end) as orderCountryTo,  (case when (c_tax.to_region_id = bpl.regionId or tz.to_region_id = bpl.regionId) then 0 else 1 end) as orderRegionTo,  (case when coalesce(c_tax.c_country_id, tz.from_country_id) is null then 1 else 0 end) as orderCountryFrom,  (case when coalesce(c_tax.c_region_id, tz.from_region_id) is null then 1 else 0 end) as orderRegionFrom  from c_tax left join c_tax_zone tz on tz.c_tax_id = c_tax.c_tax_id  join c_bpartner_location bpl on bpl.c_bpartner_location_id = '" + bpLocId + "'   where c_tax.sopotype in ('B', 'S') ";
        if (bpIsExempt) {
          sql = sql + " and c_tax.istaxexempt = 'true'";
        } else {
          sql = sql + " and c_tax.c_taxCategory_id = '" + product.get('taxCategory') + "'";
          if (bpTaxCategory) {
            sql = sql + " and c_tax.c_bp_taxcategory_id = '" + bpTaxCategory + "'";
          } else {
            sql = sql + " and c_tax.c_bp_taxcategory_id is null";
          }
        }
        sql = sql + " and c_tax.validFrom <= date()";
        sql = sql + " and (c_tax.to_country_id = bpl.countryId   or tz.to_country_id = bpl.countryId   or (c_tax.to_country_id is null       and (not exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id)           or exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id and z.to_country_id = bpl.countryId)           or exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id and z.to_country_id is null))))";
        sql = sql + " and (c_tax.to_region_id = bpl.regionId   or tz.to_region_id = bpl.regionId  or (c_tax.to_region_id is null       and (not exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id)           or exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id and z.to_region_id = bpl.regionId)           or exists (select 1 from c_tax_zone z where z.c_tax_id = c_tax.c_tax_id and z.to_region_id is null))))";
        sql = sql + " order by orderRegionTo, orderRegionFrom, orderCountryTo, orderCountryFrom, c_tax.validFrom desc, c_tax.isdefault desc";

        OB.UTIL.HookManager.executeHooks('OBPOS_FindTaxRate', {
          context: receipt,
          line: line,
          sql: sql
        }, function (args) {
          OB.Dal.queryUsingCache(OB.Model.TaxRate, args.sql, [], function (coll, args) { // success
            if (coll && coll.length > 0) {
              fulfill(coll);
            } else {
              reject(OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [args.get('_identifier')]));
            }
          }, function (tx, error) { // error
            reject(OB.I18N.getLabel('OBPOS_TaxCalculationError_Message'));
          }, product);
        });
      });
      };

  var calcProductTaxesIncPrice = function (receipt, line, product, orggross, discountedGross) {

      return findTaxesCollection(receipt, line, product).then(function (coll) {

        // First calculate the line rate.
        var linerate = BigDecimal.prototype.ONE;
        var linetaxid = coll.at(0).get('id');
        var validFromDate = coll.at(0).get('validFromDate');
        var taxamt = new BigDecimal(String(orggross));
        var baseTaxAmt; // Defined here?
        var baseTaxdcAmt; // Defined here?
        var taxamtdc;
        if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
          taxamtdc = new BigDecimal(String(discountedGross));
        }
        var fromCountryId = coll.at(0).get('country');
        var fromRegionId = coll.at(0).get('region');
        var toCountryId = coll.at(0).get('destinationCountry');
        var toRegionId = coll.at(0).get('destinationRegion');
        coll = _.filter(coll.models, function (taxRate) {
          return (taxRate.get('destinationCountry') === toCountryId) && (taxRate.get('destinationRegion') === toRegionId) && (taxRate.get('country') === fromCountryId) && (taxRate.get('region') === fromRegionId) && (taxRate.get('validFromDate') === validFromDate);
        });

        var taxeslineAux = {};

        var callbackTaxRate = function (taxRate, taxIndex, taxList) {
            if (!taxRate.get('summaryLevel')) {

              var taxId = taxRate.get('id');
              var rate = new BigDecimal(String(taxRate.get('rate'))); // 10
              rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_HALF_UP); // 0.10
              if (taxRate.get('cascade')) {
                linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                taxamt = taxamt.multiply(new BigDecimal(String(OB.DEC.add(1, rate))));
                if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                  taxamtdc = taxamtdc.multiply(new BigDecimal(String(OB.DEC.add(1, rate))));
                }
              } else if (taxRate.get('taxBase')) {

                var baseTax = taxeslineAux[taxRate.get('taxBase')];
                if (!_.isUndefined(baseTax)) { //if the baseTax of this tax have been processed, we calculate the taxamt taking into account baseTax amount
                  linerate = linerate.add(rate);
                  baseTaxAmt = new BigDecimal(String(orggross)).add(new BigDecimal(String(baseTax.amount)));
                  taxamt = taxamt.add(baseTaxAmt.multiply(rate));
                  if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                    baseTaxdcAmt = new BigDecimal(String(discountedGross)).add(new BigDecimal(String(baseTax.discAmount)));
                    taxamtdc = taxamtdc.add(baseTaxdcAmt.multiply(rate));
                  }
                } else { //if the baseTax of this tax have not been processed yet, we skip this tax till baseTax is processed.
                  return;
                }
              } else {
                linerate = linerate.add(rate);
                taxamt = taxamt.add(new BigDecimal(String(orggross)).multiply(rate));
                if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                  taxamtdc = taxamtdc.add(new BigDecimal(String(new BigDecimal(String(discountedGross)).multiply(rate))));
                }
              }
              //We could have other taxes based on this, we save tha amount in case it is needed.
              taxeslineAux[taxId] = {};
              taxeslineAux[taxId].amount = new BigDecimal(String(orggross)).multiply(rate);
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                taxeslineAux[taxId].discAmount = new BigDecimal(String(discountedGross)).multiply(rate);
              }
            } else {
              linetaxid = taxRate.get('id');
            }
            //Remove processed tax from the collection
            taxList.splice(taxList.indexOf(taxRate), 1);
            };
        var collClone = coll.slice(0);
        while (collClone.length > 0) { //Iterate taxes until the collection is empty
          _.each(collClone, callbackTaxRate);
        }

        // the line net price is calculated by doing price*price/(price*rate), as it is done in
        // the database function c_get_net_price_from_gross
        var linenet, calculatedLineNet, roundedLinePriceNet, linepricenet, pricenet, discountedNet, pricenetcascade, discountedLinePriceNet, roundedDiscountedLinePriceNet, calculatedDiscountedNet;
        if (orggross === 0) {
          linenet = new BigDecimal('0');
          linepricenet = new BigDecimal('0');
          roundedLinePriceNet = 0;
          calculatedLineNet = 0;
        } else {
          linenet = new BigDecimal(String(orggross)).multiply(new BigDecimal(String(orggross))).divide(new BigDecimal(String(taxamt)), 20, BigDecimal.prototype.ROUND_HALF_UP);
          linepricenet = linenet.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
          //round and continue with rounded values
          roundedLinePriceNet = OB.DEC.toNumber(linepricenet);
          calculatedLineNet = OB.DEC.mul(roundedLinePriceNet, new BigDecimal(String(line.get('qty'))));
        }

        if (!line.get('tax')) {
          line.set('tax', linetaxid, {
            silent: true
          });
        }
        line.set({
          'taxAmount': OB.DEC.add(line.get('taxAmount'), OB.DEC.sub(orggross, linenet)),
          'net': OB.DEC.add(line.get('net'), calculatedLineNet),
          'pricenet': OB.DEC.add(line.get('pricenet'), roundedLinePriceNet)
        });

        receipt.set('net', OB.DEC.add(receipt.get('net'), calculatedLineNet), {
          silent: true
        });

        //We follow the same formula of function c_get_net_price_from_gross to compute the discounted net
        if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
          if (taxamtdc && OB.DEC.toNumber(taxamtdc) !== 0) {
            discountedNet = new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))).divide(new BigDecimal(String(taxamtdc)), 20, BigDecimal.prototype.ROUND_HALF_UP);
            discountedLinePriceNet = discountedNet.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
            roundedDiscountedLinePriceNet = OB.DEC.toNumber(discountedLinePriceNet);
            calculatedDiscountedNet = OB.DEC.mul(roundedDiscountedLinePriceNet, new BigDecimal(String(line.get('qty'))));
            //In advance we will work with rounded prices
            discountedNet = OB.DEC.toNumber(discountedNet);
            pricenet = roundedDiscountedLinePriceNet; //discounted rounded NET unit price
            //pricenet = new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))).divide(taxamtdc, 20, BigDecimal.prototype.ROUND_HALF_UP).divide(new BigDecimal(String(element.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
          } else {
            //taxamtdc === 0
            discountedNet = 0;
            pricenet = 0;
          }
        } else {
          //net unit price (rounded)
          pricenet = roundedLinePriceNet; // 2 decimals properly rounded.
          discountedNet = OB.DEC.mul(pricenet, new BigDecimal(String(line.get('qty'))));
        }
        pricenetcascade = pricenet;
        line.set('discountedNet', OB.DEC.add(line.get('discountedNet'), discountedNet), {
          silent: true
        });

        // second calculate tax lines.  
        var taxesline = {};
        var callbackTaxLinesCreate = function (taxRate, taxIndex, taxList) {
            var pricenetAux = pricenet;
            if (!taxRate.get('summaryLevel')) {

              var taxId = taxRate.get('id');
              var rate = new BigDecimal(String(taxRate.get('rate')));
              rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_HALF_UP);
              var net = discountedNet;
              if (taxRate.get('cascade')) {

                pricenetAux = pricenetcascade;
              } else if (taxRate.get('taxBase')) {

                var baseTax = taxesline[taxRate.get('taxBase')];
                if (!_.isUndefined(baseTax)) { //if the baseTax of this tax have been processed, we skip this tax till baseTax is processed.
                  net = OB.DEC.add(OB.DEC.mul(pricenetAux, line.get('qty')), baseTax.amount);
                } else { //if the baseTax of this tax have not been processed yet, we skip this tax till baseTax is processed.
                  return;
                }
              }

              var amount = OB.DEC.mul(net, rate);
              pricenetcascade = OB.DEC.mul(pricenetAux, rate.add(BigDecimal.prototype.ONE));

              taxesline[taxId] = {};
              taxesline[taxId].name = taxRate.get('name');
              taxesline[taxId].rate = taxRate.get('rate');
              taxesline[taxId].net = net;
              taxesline[taxId].amount = amount;
            }
            //Remove processed tax from the collection
            taxList.splice(taxList.indexOf(taxRate), 1);
            };
        collClone = coll.slice(0);
        while (collClone.length > 0) { //Iterate taxes until the collection is empty
          _.each(collClone, callbackTaxLinesCreate);
        }

        // We need to make a final adjustment: we will sum all the tax lines,
        // and if the net amount of the line plus this sum is not equal to the gross,
        // we will adjust the tax line with the greatest amount
        var summedTaxAmt = 0;
        var expectedGross = (_.isNull(discountedGross) || _.isUndefined(discountedGross)) ? orggross : discountedGross;
        var greaterTax = null;
        _.each(coll, function (taxRate, taxIndex, taxList) {
          if (!taxRate.get('summaryLevel')) {
            var taxId = taxRate.get('id');
            summedTaxAmt = OB.DEC.add(summedTaxAmt, taxesline[taxId].amount);
            if ((greaterTax === null || Math.abs(taxesline[greaterTax].amount) < Math.abs(taxesline[taxId].amount))) {
              greaterTax = taxId;
            }
          }
        });
        var netandtax, adjustment;
        if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
          netandtax = OB.DEC.add(discountedNet, summedTaxAmt);
        } else {
          netandtax = OB.DEC.add(OB.DEC.mul(pricenet, line.get('qty')), summedTaxAmt);
        }
        if (expectedGross !== netandtax) {
          //An adjustment is needed
          adjustment = OB.DEC.sub(expectedGross, netandtax);
          taxesline[greaterTax].amount = OB.DEC.add(taxesline[greaterTax].amount, adjustment); // adjust the amout of taxline with greater amount
          navigateTaxesTree(coll, greaterTax, function (tax) {
            taxesline[tax.get('id')].net = OB.DEC.add(taxesline[tax.get('id')].net, adjustment); // adjust the net of taxlines that are son of the taxline with greater amount
          });
        }

        // Accumulate to taxes line
        var accumtaxesline = line.get('taxLines');
        _.each(taxesline, function (taxline, taxid) {
          if (accumtaxesline[taxid]) {
            accumtaxesline[taxid].net = OB.DEC.add(accumtaxesline[taxid].net, taxline.net);
            accumtaxesline[taxid].amount = OB.DEC.add(accumtaxesline[taxid].amount, taxline.amount);
          } else {
            accumtaxesline[taxid] = {};
            accumtaxesline[taxid].name = taxline.name;
            accumtaxesline[taxid].rate = taxline.rate;
            accumtaxesline[taxid].net = taxline.net;
            accumtaxesline[taxid].amount = taxline.amount;
          }
        });

        // Calculate receipt taxes
        var taxes = receipt.get('taxes');
        _.each(coll, function (taxRate, taxIndex) {
          var taxId = taxRate.get('id');

          delete taxes[taxId];
          receipt.get('lines').each(function (line, lineindex) {
            var taxLines = line.get('taxLines');
            if (!taxLines || !taxLines[taxId]) {
              return;
            }
            if (!taxRate.get('summaryLevel')) {
              if (taxes[taxId]) {
                taxes[taxId].net = OB.DEC.add(taxes[taxId].net, taxLines[taxId].net);
                taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, taxLines[taxId].amount);
              } else {
                taxes[taxId] = {};
                taxes[taxId].name = taxRate.get('name');
                taxes[taxId].rate = taxRate.get('rate');
                taxes[taxId].net = taxLines[taxId].net;
                taxes[taxId].amount = taxLines[taxId].amount;
              }
            }
          });
        });
        _.each(coll, function (taxRate, taxIndex) {
          var taxId = taxRate.get('id');
          if (taxes[taxId]) {
            taxes[taxId].net = OB.DEC.toNumber(taxes[taxId].net);
            taxes[taxId].amount = OB.DEC.toNumber(taxes[taxId].amount);
          }
        });
      });
      };

  var calcLineTaxesIncPrice = function (receipt, line) {

      // Initialize line properties
      line.set('taxLines', {}, {
        silent: true
      });
      line.set('tax', null, {
        silent: true
      });
      line.set('taxAmount', OB.DEC.Zero, {
        silent: true
      });
      line.set('net', OB.DEC.Zero, {
        silent: true
      });
      line.set('pricenet', OB.DEC.Zero, {
        silent: true
      });
      line.set('discountedNet', OB.DEC.Zero, {
        silent: true
      });
      line.set('linerate', BigDecimal.prototype.ZERO, {
        silent: true
      });


      // Calculate product, orggross, and discountedGross.
      var product = line.get('product');
      var orggross = line.get('gross');
      var discountedGross = null;
      if (line.get('promotions')) {
        discountedGross = line.get('gross');
        discountedGross = line.get('promotions').reduce(function (memo, element) {
          return OB.DEC.sub(memo, element.actualAmt || element.amt || 0);
        }, discountedGross);
      }

      return isTaxCategoryBOM(product.get('taxCategory')).then(function (isbom) {
        if (isbom) {
          // Find the taxid
          return findTaxesCollection(receipt, line, product).then(function (coll) {
            // complete the taxid
            line.set('tax', coll.at(0).get('id'), {
              silent: true
            });

            // BOM, calculate taxes based on the products list
            return getProductBOM(product.get('id')).then(function (data) {

              // Calculate the total BOM
              var totalbom = data.reduce(function (s, productbom) {
                return OB.DEC.add(s, OB.DEC.mul(productbom.get('bomprice'), productbom.get('bomquantity')));
              }, OB.DEC.Zero);

              // Calculate the corresponding gross and discounted gross for each product in BOM
              var accorggross = orggross;
              var accdiscountedgross = discountedGross;
              data.forEach(function (productbom) {
                var ratebom = OB.DEC.mul(productbom.get('bomprice'), productbom.get('bomquantity'));

                var orggrossbom = OB.DEC.div(OB.DEC.mul(ratebom, orggross), totalbom);
                accorggross = OB.DEC.sub(accorggross, orggrossbom);
                productbom.set('bomgross', orggrossbom);
                if (!_.isNull(discountedGross)) {
                  var discountedgrossbom = OB.DEC.div(OB.DEC.mul(ratebom, discountedGross), totalbom);
                  accdiscountedgross = OB.DEC.sub(accdiscountedgross, discountedgrossbom);
                  productbom.set('bomdiscountedgross', discountedgrossbom);
                }
              });
              // Adjust rounding in the first item of the bom
              var lastproductbom = data.at(0);
              lastproductbom.set('bomgross', OB.DEC.add(lastproductbom.get('bomgross'), accorggross));
              if (!_.isNull(discountedGross)) {
                lastproductbom.set('bomdiscountedgross', OB.DEC.add(lastproductbom.get('bomdiscountedgross'), accdiscountedgross));
              }

              // return calcProductTaxesIncPrice(receipt, line, product, orggross, discountedGross);      
              return Promise.all(data.map(function (productbom) {
                return calcProductTaxesIncPrice(receipt, line, new Backbone.Model({
                  id: productbom.get('bomproduct'),
                  taxCategory: productbom.get('bomtaxcategory')
                }), productbom.get('bomgross'), productbom.get('bomdiscountedgross'));
              }));
            });
          });
        } else {
          // Not BOM, calculate taxes based on the line product
          return calcProductTaxesIncPrice(receipt, line, product, orggross, discountedGross);
        }
      }).then(function () {
        // Calculate linerate
        line.set('linerate', OB.DEC.div(orggross, line.get('net')), {
          silent: true
        });
      })['catch'](function (reason) {
        receipt.deleteLine(line);
        OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
          popup: 'OB_UI_MessageDialog',
          args: {
            header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
            message: reason
          }
        });
      });
      };

  var calcTaxesIncPrice = function (receipt) {

      // Initialize receipt properties
      receipt.set('taxes', {}, {
        silent: true
      });
      receipt.set('net', OB.DEC.Zero, {
        silent: true
      });

      // Calculate
      return Promise.all(_.map(receipt.get('lines').models, function (line, index, list) {
        return calcLineTaxesIncPrice(receipt, line);
      }));
      };

  var calcProductTaxesExcPrice = function (receipt, line, product, linepricenet, linenet, discountedprice, discountedNet) {

      return findTaxesCollection(receipt, line, product).then(function (coll) {

        // First calculate the line rate.
        var linetaxid = coll.at(0).get('id');
        var validFromDate = coll.at(0).get('validFromDate');
        var fromCountryId = coll.at(0).get('country');
        var fromRegionId = coll.at(0).get('region');
        var toCountryId = coll.at(0).get('destinationCountry');
        var toRegionId = coll.at(0).get('destinationRegion');
        coll = _.filter(coll.models, function (taxRate) {
          return (taxRate.get('destinationCountry') === toCountryId) && (taxRate.get('destinationRegion') === toRegionId) && (taxRate.get('country') === fromCountryId) && (taxRate.get('region') === fromRegionId) && (taxRate.get('validFromDate') === validFromDate);
        });

        var discountedGross = new BigDecimal(String(discountedNet));
        var linegross = new BigDecimal(String(linenet));
        var pricenet = new BigDecimal(String(discountedprice)) || (new BigDecimal(String(linepricenet))); // 2 decimals properly rounded.
        var pricenetcascade = pricenet;

        // second calculate tax lines.
        var taxes = receipt.get('taxes');
        var taxesline = {};
        var callbackNotInclTax = function (taxRate, taxIndex, taxList) {
            var pricenetAux = pricenet;
            if (!taxRate.get('summaryLevel')) {

              var taxId = taxRate.get('id');
              var rate = new BigDecimal(String(taxRate.get('rate')));
              rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_HALF_UP);
              var net = OB.DEC.mul(pricenetAux, line.get('qty')); //=== discountedNet
              if (taxRate.get('cascade')) {

                linegross = linegross.multiply(rate.add(BigDecimal.prototype.ONE));
                discountedGross = discountedGross.add(new BigDecimal(OB.DEC.toNumber(discountedGross.multiply(rate)).toString()));
                pricenetAux = pricenetcascade;
              } else if (taxRate.get('taxBase')) {

                var baseTax = taxesline[taxRate.get('taxBase')];
                if (!_.isUndefined(baseTax)) { //if the baseTax of this tax have been processed, we skip this tax till baseTax is processed.
                  net = OB.DEC.add(OB.DEC.mul(pricenetAux, line.get('qty')), baseTax.amount);
                  var baseAmount = new BigDecimal(String(linenet)).add(new BigDecimal(String(baseTax.amount)));
                  linegross = linegross.add(baseAmount.multiply(new BigDecimal(String(rate))));
                  var discBaseAmount = new BigDecimal(String(discountedNet)).add(new BigDecimal(String(baseTax.amount)));
                  discountedGross = discountedGross.add(discBaseAmount.multiply(new BigDecimal(String(rate))));
                } else { //if the baseTax of this tax have not been processed yet, we skip this tax till baseTax is processed.
                  return;
                }
              } else {
                linegross = linegross.add(new BigDecimal(String(linenet)).multiply(new BigDecimal(String(rate))));
                discountedGross = discountedGross.add(new BigDecimal(OB.DEC.toNumber(new BigDecimal(String(discountedNet)).multiply(rate)).toString()));
              }

              var roundingLoses;
              var amount = OB.DEC.mul(net, rate);
              pricenetcascade = pricenetAux.multiply(rate.add(BigDecimal.prototype.ONE));

              taxesline[taxId] = {};
              taxesline[taxId].name = taxRate.get('name');
              taxesline[taxId].rate = taxRate.get('rate');
              taxesline[taxId].net = net;
              taxesline[taxId].amount = amount;
              if (taxes[taxId]) {
                taxes[taxId].net = OB.DEC.add(taxes[taxId].net, OB.DEC.mul(discountedprice, line.get('qty')));
                if (discountedNet !== linenet) {
                  roundingLoses = discountedprice.subtract(discountedprice).multiply(new BigDecimal('2'));
                  if (roundingLoses.compareTo(BigDecimal.prototype.ZERO) !== 0) {
                    roundingLoses = OB.DEC.toNumber(roundingLoses);
                    taxes[taxId].net = OB.DEC.add(taxes[taxId].net, roundingLoses);
                  }
                }
                taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, amount);
              } else {
                taxes[taxId] = {};
                taxes[taxId].name = taxRate.get('name');
                taxes[taxId].rate = taxRate.get('rate');
                taxes[taxId].net = net;
                if (discountedNet !== linenet) {
                  //If we lost precision because the price that we are showing is not the real one
                  //we correct this small number in tax net.
                  roundingLoses = discountedprice.subtract(discountedprice).multiply(new BigDecimal('2'));
                  if (roundingLoses.compareTo(BigDecimal.prototype.ZERO) !== 0) {
                    roundingLoses = OB.DEC.toNumber(roundingLoses);
                    taxes[taxId].net = OB.DEC.add(taxes[taxId].net, roundingLoses);
                  }
                }
                taxes[taxId].amount = amount;
              }
            } else {
              linetaxid = taxRate.get('id');
            }
            //Remove processed tax from the collection
            taxList.splice(taxList.indexOf(taxRate), 1);
            };
        var collClone = coll.slice(0);
        while (collClone.length > 0) { //Iterate taxes until the collection is empty
          _.each(collClone, callbackNotInclTax);
        }

        // Accumulate to taxes line
        var accumtaxesline = line.get('taxLines');
        _.each(taxesline, function (taxline, taxid) {
          if (accumtaxesline[taxid]) {
            accumtaxesline[taxid].net = OB.DEC.add(accumtaxesline[taxid].net, taxline.net);
            accumtaxesline[taxid].amount = OB.DEC.add(accumtaxesline[taxid].amount, taxline.amount);
          } else {
            accumtaxesline[taxid] = {};
            accumtaxesline[taxid].name = taxline.name;
            accumtaxesline[taxid].rate = taxline.rate;
            accumtaxesline[taxid].net = taxline.net;
            accumtaxesline[taxid].amount = taxline.amount;
          }
        });

        // Accumulate gross and discounted gross with the taxes calculated in this invocation.
        if (!line.get('tax')) {
          line.set('tax', linetaxid, {
            silent: true
          });
        }
        line.set({
          'gross': OB.DEC.add(line.get('gross'), OB.DEC.sub(OB.DEC.toNumber(linegross), linenet)),
          'discountedGross': OB.DEC.add(line.get('discountedGross'), OB.DEC.sub(OB.DEC.toNumber(discountedGross), discountedNet))
        }, {
          silent: true
        });
      });
      };

  var calcLineTaxesExcPrice = function (receipt, line) {

      line.set({
        'pricenet': line.get('price'),
        'net': OB.DEC.mul(line.get('price'), line.get('qty')),
        'linerate': OB.DEC.Zero,
        'tax': null,
        'taxAmount': OB.DEC.Zero,
        'taxLines': {}
      }, {
        silent: true
      });

      var resultpromise;
      var product = line.get('product');
      if (line.get('ignoreTaxes') === true || product.get('ignoreTaxes') === true) {
        // No taxes calculation for this line.
        var taxLine = line.get('taxLines');
        taxLine[OB.MobileApp.model.get('terminal').taxexempid] = {
          amount: 0,
          rate: 0,
          net: line.get('net')
        };

        line.set({
          'tax': OB.MobileApp.model.get('terminal').taxexempid,
          'discountedNet': line.get('net'),
          'discountedNetPrice': new BigDecimal(String(line.get('price'))),
          'gross': line.get('net'),
          'discountedGross': line.get('net')
        }, {
          silent: true
        });

        resultpromise = Promise.resolve();
      } else {

        var linepricenet = line.get('price');
        var linenet = line.get('net');
        var discAmt = null;
        var discountedprice, discountedNet;
        if (line.get('promotions') && line.get('promotions').length > 0) {
          discAmt = new BigDecimal(String(line.get('net')));
          discAmt = line.get('promotions').reduce(function (memo, element) {
            return memo.subtract(new BigDecimal(String(element.actualAmt || element.amt || 0)));
          }, discAmt);
          discountedprice = discAmt.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
          discountedNet = OB.DEC.toNumber(discAmt);
        } else {
          discountedprice = new BigDecimal(String(line.get('price')));
          discountedNet = linenet;
        }

        // Initialize line calculations
        line.set({
          'discountedNet': discountedNet,
          'discountedNetPrice': discountedprice,
          'gross': linenet,
          'discountedGross': discountedNet
        }, {
          silent: true
        });

        resultpromise = isTaxCategoryBOM(product.get('taxCategory')).then(function (isbom) {
          if (isbom) {
            // Find the taxid
            return findTaxesCollection(receipt, line, product).then(function (coll) {
              // complete the taxid
              line.set('tax', coll.at(0).get('id'), {
                silent: true
              });

              // BOM, calculate taxes based on the products list
              return getProductBOM(product.get('id')).then(function (data) {

                // Calculate the total BOM
                var totalbom = data.reduce(function (s, productbom) {
                  return OB.DEC.add(s, OB.DEC.mul(productbom.get('bomprice'), productbom.get('bomquantity')));
                }, OB.DEC.Zero);

                // Calculate the corresponding gross and discounted gross for each product in BOM
                var acclinenet = linenet;
                var accdiscountedNet = discountedNet;
                var acclinepricenet = linepricenet;

                data.forEach(function (productbom) {
                  var ratebom = OB.DEC.mul(productbom.get('bomprice'), productbom.get('bomquantity'));

                  var linenetbom = OB.DEC.div(OB.DEC.mul(ratebom, linenet), totalbom);
                  acclinenet = OB.DEC.sub(acclinenet, linenetbom);
                  productbom.set('bomnet', linenetbom);

                  var discountedNetbom = OB.DEC.div(OB.DEC.mul(ratebom, discountedNet), totalbom);
                  accdiscountedNet = OB.DEC.sub(accdiscountedNet, discountedNetbom);
                  productbom.set('bomdiscountednet', discountedNetbom);

                  var linepricenetbom = OB.DEC.div(OB.DEC.mul(ratebom, linepricenet), totalbom);
                  acclinepricenet = OB.DEC.sub(acclinepricenet, linepricenetbom);
                  productbom.set('bomlinepricenet', linepricenetbom);
                });
                // Adjust rounding in the first item of the bom
                var lastproductbom = data.at(0);
                lastproductbom.set('bomnet', OB.DEC.add(lastproductbom.get('bomnet'), acclinenet));
                lastproductbom.set('bomdiscountednet', OB.DEC.add(lastproductbom.get('bomdiscountednet'), accdiscountedNet));
                lastproductbom.set('bomlinepricenet', OB.DEC.add(lastproductbom.get('bomlinepricenet'), acclinepricenet));

                return Promise.all(data.map(function (productbom) {
                  return calcProductTaxesExcPrice(receipt, line, new Backbone.Model({
                    id: productbom.get('bomproduct'),
                    taxCategory: productbom.get('bomtaxcategory')
                  }), productbom.get('bomlinepricenet'), productbom.get('bomnet'), new BigDecimal(String(productbom.get('bomdiscountednet'))).divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP), productbom.get('bomdiscountednet'));
                }));
              });
            });
          } else {
            // Not BOM, calculate taxes based on the line product
            return calcProductTaxesExcPrice(receipt, line, product, linepricenet, linenet, discountedprice, discountedNet);
          }
        });
      }

      return resultpromise.then(function () {
        // Calculate linerate and taxamount
        line.set('linerate', OB.DEC.div(line.get('gross'), line.get('net')), {
          silent: true
        });
        line.set('taxAmount', OB.DEC.sub(line.get('discountedGross'), line.get('discountedNet')), {
          silent: true
        });
      })['catch'](function (reason) {
        receipt.deleteLine(line);
        OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
          popup: 'OB_UI_MessageDialog',
          args: {
            header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
            message: reason
          }
        });
      });
      };

  var calcTaxesExcPrice = function (receipt) {

      // Initialize receipt
      receipt.set('taxes', {}, {
        silent: true
      });

      // Calculate
      return Promise.all(_.map(receipt.get('lines').models, function (line, index, list) {
        return calcLineTaxesExcPrice(receipt, line);
      }));
      };

  // Just calc the right function depending on prices including or excluding taxes
  var calcTaxes = function (receipt) {
      if (receipt.get('priceIncludesTax')) {
        return calcTaxesIncPrice(receipt);
      } else {
        return calcTaxesExcPrice(receipt);
      }
      };

  var getTaxesInfo = function (receipt) {
      return {
        taxlines: receipt.get('lines').map(function (line) {
          return {
            linerate: line.get('linerate'),
            tax: line.get('tax'),
            taxAmount: line.get('taxAmount'),
            net: line.get('net'),
            pricenet: line.get('pricenet'),
            discountedNet: line.get('discountedNet'),
            taxes: line.get('taxLines')
          };
        }),
        net: receipt.get('net'),
        taxes: receipt.get('taxes')
      };
      };

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  OB.DATA.OrderTaxes = function (modelOrder) {
    modelOrder.calculateTaxes = function (callback) {
      var me = this;
      var mytaxes, mytaxesold;
      var synchId;
      synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('taxescalculation');
      calcTaxes(me).then(function () {
        me.trigger('paintTaxes');
        callback();
        OB.UTIL.SynchronizationHelper.finished(synchId, 'taxescalculation');
      });
    };
  };
}());