/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window,Promise,_ */

(function () {

  var calcLineTaxesIncPrice = function (receipt, line) {
      return new Promise(function (fulfill, reject) {
        var product = line.get('product'),
            
            
            // sql parameters 
            fromRegionOrg = OB.MobileApp.model.get('terminal').organizationRegionId,
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

        OB.MobileApp.model.hookManager.executeHooks('OBPOS_FindTaxRate', {
          context: receipt,
          line: line,
          sql: sql
        }, function (args) {
          OB.Dal.query(OB.Model.TaxRate, args.sql, [], function (coll, args) { // success
            // var  gross,  
            if (coll && coll.length > 0) {

              var discountedGross = null;
              if (line.get('promotions')) {
                discountedGross = line.get('gross');
                discountedGross = line.get('promotions').reduce(function (memo, element) {
                  return OB.DEC.sub(memo, element.actualAmt || element.amt || 0);
                }, discountedGross);
              }
              var orggross = OB.DEC.mul(line.get('grossUnitPrice') || line.get('price'), line.get('qty'));

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
              var collClone = coll.slice(0);
              while (collClone.length > 0) { //Iterate taxes until the collection is empty
                _.each(collClone, function (taxRate, taxIndex, taxList) {
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
                });
              }

              // the line net price is calculated by doing price*price/(price*rate), as it is done in
              // the database function c_get_net_price_from_gross
              var linenet, calculatedLineNet, roundedLinePriceNet, linepricenet, linegross, pricenet, discountedNet, pricenetcascade, discountedLinePriceNet, roundedDiscountedLinePriceNet, calculatedDiscountedNet;
              if (orggross === 0) {
                linenet = new BigDecimal('0');
                linepricenet = new BigDecimal('0');
                roundedLinePriceNet = 0;
                linegross = 0;
                calculatedLineNet = 0;
              } else {
                linenet = new BigDecimal(String(orggross)).multiply(new BigDecimal(String(orggross))).divide(new BigDecimal(String(taxamt)), 20, BigDecimal.prototype.ROUND_HALF_UP);
                linepricenet = linenet.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
                //round and continue with rounded values
                roundedLinePriceNet = OB.DEC.toNumber(linepricenet);
                calculatedLineNet = OB.DEC.mul(roundedLinePriceNet, new BigDecimal(String(line.get('qty'))));
                linegross = line.get('lineGrossAmount') || line.get('gross');
              }

              line.set('linerate', OB.DEC.toNumber(linerate));
              line.set('tax', linetaxid);
              line.set('taxAmount', OB.DEC.sub(linegross, linenet));
              line.set('net', calculatedLineNet);
              line.set('pricenet', roundedLinePriceNet);

              receipt.set('net', OB.DEC.add(receipt.get('net'), calculatedLineNet));

              //We follow the same formula of function c_get_net_price_from_gross to compute the discounted net
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                if (taxamtdc && OB.DEC.toNumber(taxamtdc) !== 0) {
                  discountedNet = new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))).divide(new BigDecimal(String(taxamtdc)), 20, BigDecimal.prototype.ROUND_HALF_UP);
                  discountedLinePriceNet = discountedNet.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
                  roundedDiscountedLinePriceNet = OB.DEC.toNumber(discountedLinePriceNet);
                  calculatedDiscountedNet = OB.DEC.mul(roundedDiscountedLinePriceNet, new BigDecimal(String(line.get('qty'))));
                  //In advance we will work with rounded prices
                  pricenet = roundedDiscountedLinePriceNet; //discounted rounded NET unit price
                  discountedNet = calculatedDiscountedNet; //discounted rounded NET line price
                  //pricenet = new BigDecimal(String(discountedGross)).multiply(new BigDecimal(String(discountedGross))).divide(taxamtdc, 20, BigDecimal.prototype.ROUND_HALF_UP).divide(new BigDecimal(String(element.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
                } else {
                  //taxamtdc === 0
                  discountedNet = 0;
                  pricenet = 0;
                }
              } else {
                //net unit price (rounded)
                pricenet = roundedLinePriceNet; // 2 decimals properly rounded.
              }
              line.set('discountedNet', OB.DEC.mul(pricenet, new BigDecimal(String(line.get('qty')))));
              discountedNet = line.get('discountedNet');
              pricenetcascade = pricenet;

              // second calculate tax lines.          
              var taxesline = {};
              collClone = coll.slice(0);
              while (collClone.length > 0) { //Iterate taxes until the collection is empty
                _.each(collClone, function (taxRate, taxIndex, taxList) {
                  var pricenetAux = pricenet;
                  if (!taxRate.get('summaryLevel')) {

                    var taxId = taxRate.get('id');
                    var rate = new BigDecimal(String(taxRate.get('rate')));
                    rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_HALF_UP);
                    var net = OB.DEC.mul(pricenetAux, line.get('qty')); //=== discountedNet
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
                });
              }

              // We need to make a final adjustment: we will sum all the tax lines,
              // and if the net amount of the line plus this sum is not equal to the gross,
              // we will adjust the tax line with the greatest amount
              var summedTaxAmt = 0;
              var expectedGross;
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                expectedGross = discountedGross;
              } else {
                expectedGross = line.get('gross');
              }

              var greaterTax = null;
              _.each(coll, function (taxRate, taxIndex, taxList) {
                if (!taxRate.get('summaryLevel')) {
                  var taxId = taxRate.get('id');
                  summedTaxAmt = OB.DEC.add(summedTaxAmt, taxesline[taxId].amount);
                  if (receipt.get('orderType') === 1) { //A baseTax cannot be the greatest tax because if we change it's value we should change the dependent tax value too
                    if ((greaterTax === null || taxesline[greaterTax].amount > taxesline[taxId].amount) && (_.filter(taxList, function (tax) {
                      return tax.get('taxBase') && tax.get('taxBase') === taxId;
                    }).length === 0)) {
                      greaterTax = taxId;
                    }
                  } else { //A baseTax cannot be the greatest tax because if we change it's value we should change the dependent tax value too
                    if ((greaterTax === null || taxesline[greaterTax].amount < taxesline[taxId].amount) && (_.filter(taxList, function (tax) {
                      return tax.get('taxBase') && tax.get('taxBase') === taxId;
                    }).length === 0)) {
                      greaterTax = taxId;
                    }
                  }
                }
              });
              var netandtax;
              if (!(_.isNull(discountedGross) || _.isUndefined(discountedGross))) {
                netandtax = OB.DEC.add(discountedNet, summedTaxAmt);
              } else {
                netandtax = OB.DEC.add(OB.DEC.mul(pricenet, line.get('qty')), summedTaxAmt);
              }
              if (expectedGross !== netandtax) {
                //An adjustment is needed
                taxesline[greaterTax].amount = OB.DEC.add(taxesline[greaterTax].amount, OB.DEC.sub(expectedGross, netandtax));
              }

              line.set('taxLines', taxesline);

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
              fulfill();
            } else {
              receipt.deleteLine(line);
              OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                popup: 'OB_UI_MessageDialog',
                args: {
                  header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
                  message: OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [args.get('_identifier')])
                }
              });
              reject();
            }
          }, function (tx, error) { // error
            receipt.deleteLine(line);
            OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
              popup: 'OB_UI_MessageDialog',
              args: {
                header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
                message: OB.I18N.getLabel('OBPOS_TaxCalculationError_Message')
              }
            });
            reject();
          }, product);
        });
      });
      };


  var calcTaxesIncPrice = function (receipt) {

      // Initialize receipt
      receipt.set('taxes', {});
      receipt.set('net', OB.DEC.Zero);

      // Calculate
      return Promise.all(_.map(receipt.get('lines').models, function (line, index, list) {
        return calcLineTaxesIncPrice(receipt, line);
      }));
      };

  var calcLineTaxesExcPrice = function (receipt, line) {
      return new Promise(function (fulfill, reject) {
        var product = line.get('product'),
            
            
            // sql parameters 
            fromRegionOrg = OB.MobileApp.model.get('terminal').organizationRegionId,
            fromCountryOrg = OB.MobileApp.model.get('terminal').organizationCountryId,
            bpTaxCategory = receipt.get('bp').get('taxCategory'),
            bpIsExempt = receipt.get('bp').get('taxExempt'),
            bpLocId = receipt.get('bp').get('locId');

        if (line.get('ignoreTaxes') === true || product.get('ignoreTaxes') === true) {
          var taxLine = {};
          line.set('linerate', OB.DEC.toNumber(BigDecimal.prototype.ONE));
          line.set('tax', OB.MobileApp.model.get('terminal').taxexempid);
          line.set('taxAmount', OB.DEC.Zero);
          line.set('net', line.get('net'));
          line.set('pricenet', line.get('net'));
          line.set('gross', line.get('net'));
          line.set('discountedGross', line.get('net'));
          line.set('discountedNet', new BigDecimal(String(line.get('net'))));
          line.set('taxAmount', OB.DEC.Zero);
          line.set('discountedNetPrice', new BigDecimal(String(line.get('net'))));
          taxLine[OB.MobileApp.model.get('terminal').taxexempid] = {
            amount: 0,
            rate: 0,
            net: line.get('net')
          };
          line.set('taxLines', taxLine);
        } else {

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

          // the query is ordered by countryId desc and regionId desc 
          // (so, the first record will be the tax with the same country or region that the customer, 
          // or if toCountryId and toRegionId are nulls then will be ordered by validfromdate)    
          OB.MobileApp.model.hookManager.executeHooks('OBPOS_FindTaxRate', {
            context: receipt,
            line: line,
            sql: sql
          }, function (args) {
            OB.Dal.query(OB.Model.TaxRate, args.sql, [], function (coll, args) { // success
              // var rate, taxAmt, net, pricenet, pricenetcascade, amount, taxId, roundingLoses, pricenetAux, baseTax, collClone, baseAmount, discBaseAmount;
              if (coll && coll.length > 0) {
                // First calculate the line rate.
                var linerate = BigDecimal.prototype.ONE;
                var linetaxid = coll.at(0).get('id');
                var validFromDate = coll.at(0).get('validFromDate');
                var fromCountryId = coll.at(0).get('country');
                var fromRegionId = coll.at(0).get('region');
                var toCountryId = coll.at(0).get('destinationCountry');
                var toRegionId = coll.at(0).get('destinationRegion');
                coll = _.filter(coll.models, function (taxRate) {
                  return (taxRate.get('destinationCountry') === toCountryId) && (taxRate.get('destinationRegion') === toRegionId) && (taxRate.get('country') === fromCountryId) && (taxRate.get('region') === fromRegionId) && (taxRate.get('validFromDate') === validFromDate);
                });

                var discAmt = null;
                if (line.get('promotions') && line.get('promotions').length > 0) {
                  discAmt = new BigDecimal(String(line.get('net')));
                  discAmt = line.get('promotions').reduce(function (memo, element) {
                    return memo.subtract(new BigDecimal(String(element.actualAmt || element.amt || 0)));
                  }, discAmt);
                }
                var linepricenet = line.get('price');
                var discountedprice, calculatedDiscountedprice, orgDiscountedprice, discountedNet, pricenet, pricenetcascade;
                if (!(_.isNull(discAmt) || _.isUndefined(discAmt))) {
                  discountedprice = discAmt.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
                  discountedNet = OB.DEC.toNumber(discAmt);
                  //sometimes with packs or discounts we are losing precision and this precision is reflected wrongly in the tax net
                  //we save the number with total precision and bellow if there are differences they are corrected.
                  calculatedDiscountedprice = true;
                  orgDiscountedprice = discAmt.divide(new BigDecimal(String(line.get('qty'))), 20, BigDecimal.prototype.ROUND_HALF_UP);
                } else {
                  discountedprice = new BigDecimal(String(line.get('price')));
                  discountedNet = OB.DEC.mul(discountedprice, line.get('qty'));
                }

                var linenet = OB.DEC.mul(linepricenet, line.get('qty'));

                var discountedGross = new BigDecimal(String(discountedNet));
                var linegross = new BigDecimal(String(linenet));
                pricenet = new BigDecimal(String(discountedprice)) || (new BigDecimal(String(linepricenet))); // 2 decimals properly rounded.
                pricenetcascade = pricenet;

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

                        linerate = linerate.multiply(rate.add(BigDecimal.prototype.ONE));
                        linegross = linegross.multiply(rate.add(BigDecimal.prototype.ONE));
                        discountedGross = discountedGross.add(new BigDecimal(OB.DEC.toNumber(discountedGross.multiply(rate)).toString()));
                        pricenetAux = pricenetcascade;
                      } else if (taxRate.get('taxBase')) {

                        var baseTax = taxesline[taxRate.get('taxBase')];
                        if (!_.isUndefined(baseTax)) { //if the baseTax of this tax have been processed, we skip this tax till baseTax is processed.
                          net = OB.DEC.add(OB.DEC.mul(pricenetAux, line.get('qty')), baseTax.amount);
                          linerate = linerate.add(rate);
                          var baseAmount = new BigDecimal(String(linenet)).add(new BigDecimal(String(baseTax.amount)));
                          linegross = linegross.add(baseAmount.multiply(new BigDecimal(String(rate))));
                          var discBaseAmount = new BigDecimal(String(discountedNet)).add(new BigDecimal(String(baseTax.amount)));
                          discountedGross = discountedGross.add(discBaseAmount.multiply(new BigDecimal(String(rate))));
                        } else { //if the baseTax of this tax have not been processed yet, we skip this tax till baseTax is processed.
                          return;
                        }
                      } else {
                        linerate = linerate.add(rate);
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
                        if (calculatedDiscountedprice) {
                          roundingLoses = orgDiscountedprice.subtract(discountedprice).multiply(new BigDecimal('2'));
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
                        if (calculatedDiscountedprice) {
                          //If we lost precision because the price that we are showing is not the real one
                          //we correct this small number in tax net.
                          roundingLoses = orgDiscountedprice.subtract(discountedprice).multiply(new BigDecimal('2'));
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

                var linepricegross = OB.DEC.div(linegross, line.get('qty'));
                line.set('linerate', OB.DEC.toNumber(linerate));
                line.set('tax', linetaxid);
                line.set('taxAmount', OB.DEC.mul(OB.DEC.mul(discountedprice, line.get('qty')), linerate));
                line.set('net', linenet);
                line.set('pricenet', linepricenet);
                line.set('gross', OB.DEC.toNumber(linegross));
                line.set('fullgross', linegross);
                line.set('discountedGross', OB.DEC.toNumber(discountedGross));
                line.set('discountedNet', discountedNet);
                line.set('taxAmount', OB.DEC.sub(line.get('discountedGross'), line.get('discountedNet')));
                line.set('discountedNetPrice', discountedprice);

                // totalnet = OB.DEC.add(totalnet, linenet);
                line.set('taxLines', taxesline);

                // Calculate receipt taxes
                _.each(coll, function (taxRate, taxIndex) {
                  var taxId = taxRate.get('id');
                  if (taxes[taxId]) {
                    //taxes[taxId].net = taxes[taxId].net;
                    taxes[taxId].amount = OB.DEC.toNumber(taxes[taxId].amount);
                  }
                });

                fulfill();
              } else {
                receipt.deleteLine(line);
                OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                  popup: 'OB_UI_MessageDialog',
                  args: {
                    header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
                    message: OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [args.get('_identifier')])
                  }
                });
                reject();
              }
            }, function (tx, error) { // error
              receipt.deleteLine(line);
              OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                popup: 'OB_UI_MessageDialog',
                args: {
                  header: OB.I18N.getLabel('OBPOS_TaxNotFound_Header'),
                  message: OB.I18N.getLabel('OBPOS_TaxCalculationError_Message')
                }
              });
              reject();
            }, product);
          });
        }
      });
      };

  var calcTaxesExcPrice = function (receipt) {

      // Initialize receipt
      receipt.set('taxes', {});

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

  // Taxes logic calculation: 'DEBUG', 'OLDLOGIC', 'NEWLOGIC' (default)
  window.TAXESLOGIC = 'DEBUG';

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  OB.DATA.OrderTaxes = function (modelOrder) {
    this._id = 'logicOrderTaxes';
    this.receipt = modelOrder;

    this.receipt.calculateTaxes = function (callback) {
      if (window.TAXESLOGIC === 'DEBUG') {
        var me = this;
        var mytaxes, mytaxesold;
        calcTaxes(me).then(function () {
          mytaxes = JSON.stringify(me.get('taxes'));
          window.console.log(mytaxes);
          OB.DATA.legacyCalculateTaxes.call(me, function () {
            mytaxesold = JSON.stringify(me.get('taxes'));
            window.console.log(mytaxesold);
            if (mytaxes !== mytaxesold) {
              window.console.error('Wrong taxes calculation');
            }
            callback();
          });
        });
      } else if (window.TAXESLOGIC === 'OLDLOGIC') {
        legacyCalculateTaxes(callback);
      } else { // 'NEWLOGIC' (default)
        calcTaxes(this).then(callback);
      }
    };
  };
}());