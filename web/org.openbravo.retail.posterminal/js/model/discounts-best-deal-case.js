/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.Model.Discounts.calculateBestDealCase = function (originalReceipt, callback) {
  var promotionCandidates = [],
      evaluated = [],
      cases = 0,
      originalDeal, bestDiscount, receipt, originalWindowError, linesAfterSplit;

  function getCandidatesForProducts() {
    var criteria, de = new OB.Model.DiscountsExecutor(),
        whereClause = OB.Model.Discounts.standardFilter + " AND M_OFFER_TYPE_ID NOT IN (" + OB.Model.Discounts.getManualPromotions() + ")",
        lines, candidates = {},
        i = 0;

    lines = receipt.get('lines'); // TODO: only different products
    if (lines.length === 0) {
      finalize();
    }

    lines.forEach(function (line) {
      criteria = {
        '_whereClause': whereClause,
        params: de.convertParams(null, line, receipt, de.paramsTranslation)
      };

      OB.Dal.find(OB.Model.Discount, criteria, function (discountRules) {
        candidates[line.get('product').id] = discountRules;
        i += 1;
        if (i === lines.length) {
          // we're done with all the lines, continue with next step
          //splitLines(candidates);
          doGroups(candidates);
        }
      });
    });
  }

  function doGroups(candidates) {
    var groups = [],
        productId, g;
    for (productId in candidates) {
      if (candidates.hasOwnProperty(productId)) {
        foundGroup = false;
        if (candidates[productId].length === 0) {
          continue;
        }
        candidates[productId].forEach(function (rule) {
          var rules, ruleIDs;
          if (foundGroup) {
            return;
          }
          for (g = 0; g < groups.length; g++) {
            if (groups[g].ruleIDs.indexOf(rule.id) !== -1) {
              foundGroup = true;
              groups[g].products.push(productId);
              candidates[productId].forEach(function (rule) {
                if (groups[g].ruleIDs.indexOf(rule.id) === -1) {
                  groups[g].rules.push(rule);
                  groups[g].ruleIDs.push(rule.id);
                  // TODO: merge groups
                }
              });
            }
          }
        }); // loop of rules within candidate
        if (!foundGroup) {
          rules = [], ruleIDs = [];
          candidates[productId].forEach(function (rule) {
            rules.push(rule);
            ruleIDs.push(rule.id);
          });
          groups.push({
            rules: rules,
            ruleIDs: ruleIDs,
            products: [productId],
            productsInSubGrps: {}
          });
        }
      }
    } // loop of candidates
    // now we have groups of products with conflicts between them,
    // lets split these groups in subgroups each of them with the 
    // same rules candidates
    _.forEach(groups, function (group) {
      var subGrps = [];

      _.forEach(group.products, function (productId) {
        var prodCandidates = candidates[productId];
        foundGroup = false;
        for (i = 0; i < subGrps.length; i++) {
          if (subGrps[i].ruleIds.length === prodCandidates.length) {
            prodCandidates.forEach(function (candidate) {
              if (subGrps[i].ruleIds.indexOf(candidate.id) !== -1) {
                foundGroup = true;
              }
            });
            if (foundGroup) {
              subGrps[i].products.push(productId);
              group.productsInSubGrps[productId] = subGrps[i];
              break;
            }
          }
        }

        if (!foundGroup) {
          var candidateIds = [],
              sub;
          candidates[productId].forEach(function (candidate) {
            candidateIds.push(candidate.id);
          });
          sub = {
            rules: candidates[productId],
            ruleIds: candidateIds,
            products: [productId],
            lines: []
          };

          subGrps.push(sub);

          group.productsInSubGrps[productId] = sub;
        }
      });
      group.subGrps = subGrps;
    });
    console.log('groups', groups);
    splitLines(candidates, groups);
  }

  function splitLines(candidates, groups) {
    var i = 0,
        lines = receipt.get('lines'),
        newLines = [],
        numberOfCases = 1;

    console.log('num of lines before split', lines.length)
    lines.forEach(function (line) {
      var originalQty, l, productId = line.get('product').id;

      function shouldSplit() {
        return true;
      }

      i += 1;
      if (candidates[productId] && candidates[productId].length > 0 && line.get('qty') > 1) {
        // there are candidates for the product, let's split the line if needed
        if (shouldSplit()) {
          originalQty = line.get('qty');

          console.log('split line', line.get('product').get('_identifier'));
          line.set({
            qty: 1,
            gross: line.get('price')
          });
          for (l = 1; l < originalQty; l++) {
            newLines.push(line.clone());
          }
        } else {
          lines.add(line);
        }

      } else {
        console.log('not split line', line.get('product').get('_identifier'));
      }
    });

    lines.add(newLines);

    linesAfterSplit = [];
    lines.forEach(function (line) {
      linesAfterSplit.push(line.clone());
    });

    linesAfterSplit.forEach(function (line) {
      var productCases = candidates[line.get('product').id];

      if (!productCases || productCases.length === 0) {
        return; // continue
      }

      numberOfCases = numberOfCases * productCases.length;

      promotionCandidates.push({
        line: line,
        candidates: candidates[line.get('product').id],
        shouldSplit: line.get('qty') === 1,
        pointer: 0
      });
    });
    console.log('Evalutaing', numberOfCases, 'cases');


    var promotionCandidates2 = [];
    _.forEach(groups, function (group) {
      var promoGroup, linesInGrp = [];
      linesAfterSplit.forEach(function (line) {
        var prodId = line.get('product').id,
            productCases = candidates[prodId];
        if (!productCases || productCases.length === 0 || group.products.indexOf(prodId) === -1) {
          return; //continue
        }
        linesInGrp.push({
          line: line,
          candidates: candidates[line.get('product').id],
          shouldSplit: line.get('qty') === 1,
          pointer: 0
        });

        group.productsInSubGrps[prodId].lines.push({
          line: line,
          candidates: candidates[line.get('product').id],
          shouldSplit: line.get('qty') === 1,
          pointer: 0
        });
      });
      promoGroup = {
        grp: group,
        lines: linesInGrp
      }
      promotionCandidates2.push(promoGroup);
    });

    console.log('num of lines after split', lines.length, receipt.get('lines').length)
    console.log('candiates', promotionCandidates);
    console.log('candiates2', promotionCandidates2);
    console.log('groups', groups);

    calculateCombinations(groups);
    //evaluateBestDealCase();
    //finalize();
  }

  function calculateCombinations(groups) {

    function pick(grp, got, pos, lnum, result, tnum) {
      var l = [],
          z, i;
      if (got.length === grp[tnum].lines.length) {
        for (z = 0; z < got.length; z++) {
          l.push({
            l: got[z].l,
            d: got[z].d
          })
        }

        result.push(l);
        return result;
      }

      for (i = pos; i < grp[tnum].ruleIds.length; i++) {
        got.push({
          l: grp[tnum].lines[lnum],
          d: grp[tnum].ruleIds[i]
        });
        pick(grp, got, i, lnum + 1, result, tnum)
        got.pop();
      }
      return result;
    }

    function calculate(grp) {
      var res, i, result = [];

      for (i = 0; i < grp.length; i++) {
        res = [];
        picked = pick(grp, [], 0, 0, res, i);
        result.push(picked);
      }

      console.log('r,', result)
      return combine(result);
    }

    function combine(c, n) {
      var r = [],
          pointers = [],
          l, i;
      for (i = 0; i < c.length; i++) {
        pointers.push(0);
      }

      do {
        l = [];
        for (i = 0; i < pointers.length; i++) {
          l = l.concat(c[i][pointers[i]]);
        }
        r.push(l);
      } while (movePointer(pointers, c));
      return r;
    }

    function movePointer(p, c) {
      var i, k;
      for (i = c.length - 1; i >= 0; i--) {
        if (p[i] < c[i].length - 1) {
          p[i]++;
          for (k = i + 1; k < c.length; k++) {
            p[k] = 0;
          }
          return true;
        }
      }
      return false;
    }



    //----
    var allCombinations = [],
        totalNumOfCombinations = 0;
    _.forEach(groups, function (group) {
      var grpCombinations = calculate(group.subGrps);

      console.log(grpCombinations.length, 'combinations for grp', group); //, combinations);
      totalNumOfCombinations += grpCombinations.length;
      allCombinations.push(grpCombinations);
    });

    console.log(totalNumOfCombinations, 'allCombinations', allCombinations)

    //evaluateBestDealCase();
    finalize();
  }

  function evaluateBestDealCase() {
    var currentEval, lines = receipt.get('lines'),
        foundCaseToEval = false;

    console.timeStamp('evaluateBestDealCase');

    do {
      cases++;

      currentEval = {};

      lines.reset();
      _.forEach(linesAfterSplit, function (line) {
        line.set('qty', 1)
        lines.add(line);
      });


      _.forEach(promotionCandidates, function (candidate) {
        var line = candidate.line,
            prodId = line.get('product').id,
            ruleId = candidate.candidates.at(candidate.pointer).id;
        currentEval[prodId] = currentEval[prodId] || {};
        currentEval[prodId][ruleId] = (currentEval[prodId][ruleId] || 0) + 1;


        line.set({
          promotions: null,
          promotionCandidates: [ruleId],
          discountedLinePrice: null,
          qty: 1
        }, {
          silent: true
        });
        lines.remove(line);
        lines.add(line);
      });

      foundCaseToEval = !alreadyEvaluated(evaluated, currentEval);
      if (!foundCaseToEval) {
        if (!movePointer()) {
          finalize();
          return;
        }
      }
    } while (!foundCaseToEval);

    console.log('=== case ===', cases)
    console.log('num of lines', receipt.get('lines').length)

    evaluated.push(currentEval);

    evalCandidate(0);
  }

  function nextCase(currentEvaluated) {
    var currentDiscount, bestLines, lines = receipt.get('lines');
    if (currentEvaluated) {
      currentDiscount = 0;
      lines.forEach(function (line) {
        if (line.get('promotions')) {
          _.forEach(line.get('promotions'), function (promo) {
            currentDiscount += promo.amt;
          });
        }
      });
      console.log('====================================================== case', cases, 'discounts', currentDiscount);

      if (!bestDiscount || bestDiscount.totalDiscount < currentDiscount) {
        bestLines = [];
        lines.forEach(function (line) {
          bestLines.push(line.clone());
        })

        bestDiscount = {
          totalDiscount: currentDiscount,
          lines: bestLines
        };
      }
    }

    if (movePointer()) {
      evaluateBestDealCase();
    } else {
      finalize();
    }
  }

  function finalize() {
    var lines = receipt.get('lines');
    if (bestDiscount && bestDiscount.totalDiscount > originalDeal) {
      // Best Deal found, applying it to the cloned receipt...
      console.log('found best deal case', bestDiscount);
      _.forEach(promotionCandidates, function (candidate) {
        lines.remove(candidate.line);
      });
      lines.add(bestDiscount.lines);

      // ...and reseting original receipt's lines with best case ones
      originalReceipt.get('lines').reset();
      originalReceipt.get('lines').add(lines.models);
      originalReceipt.calculateGross();

      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BDC.Found', [originalDeal, bestDiscount.totalDiscount]));
      originalReceipt.save();
    } else {
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_BDC.NotFound'));
      console.log('Already in Best Deal, no change required');
    }

    console.log('Evaluated', evaluated.length, 'of', cases, 'cases--------------------------------------');

    endProcess();
  }

  function endProcess() {
    OB.Model.Discounts.calculatingBestDealCase = false;
    window.onerror = originalWindowError;

    console.timeEnd('calculateBestDealCase');
    OB.UTIL.showLoading(false);
    if (callback) {
      callback();
    }
    console.profileEnd();
  }

/**
    Evaluates a promotionCandidate with current pointers. This is, 
    a line with a promo
  **/

  function evalCandidate(candidateNum) {
    var candidate = promotionCandidates[candidateNum],
        lines;
    if (candidateNum >= promotionCandidates.length) {
      lines = receipt.get('lines');
      lines.reset();
      _.forEach(linesAfterSplit, function (line) {
        lines.add(line);
      });
      nextCase(true);
      return;
    }

    var disc = candidate.candidates.at(candidate.pointer),
        rule = rule = OB.Model.Discounts.discountRules[disc.get('discountType')],
        line = candidate.line,
        ruleListener;
    if (rule.async) {

      // waiting listener to trigger completed to move to next action
      ruleListener = new Backbone.Model();
      ruleListener.on('completed', function (obj) {
        ruleListener.off();
        console.log('ueoe')
        evalCandidate(candidateNum + 1);
      }, this);
    }

    rule.implementation(disc, receipt, line, ruleListener);
    if (!rule.async) {
      // done, move to next action
      evalCandidate(candidateNum + 1);
    }
  }

  function alreadyEvaluated(evaluated, currentEval) {
    var c, i;
    for (i = 0; i < evaluated.length; i++) {
      alreadyEval = true;
      for (c in currentEval) {
        if (currentEval.hasOwnProperty(c)) {
          for (p in currentEval[c]) {
            if (currentEval[c].hasOwnProperty(p)) {
              alreadyEval = alreadyEval && ((evaluated[i][c] && evaluated[i][c][p] === currentEval[c][p]));
              if (!alreadyEval) {
                continue;
              }
            }
          }
          if (!alreadyEval) {
            continue;
          }
        }
      }
      if (alreadyEval) {
        return true;
      }
    }
    return false;
  }

  function movePointer() {
    var i = 0,
        moved = false;
    for (i = promotionCandidates.length - 1; i >= 0; i--) {
      if (promotionCandidates[i].pointer < promotionCandidates[i].candidates.length - 1) {
        moved = true;
        promotionCandidates[i].pointer += 1;
        for (j = i + 1; j < promotionCandidates.length; j++) {
          promotionCandidates[j].pointer = 0;
        }
        break;
      }
    }
    return moved;
  }

  console.profile('calculateBestDealCase');
  console.time('calculateBestDealCase');
  OB.UTIL.showLoading(true);

  // we want this process to be "secure", in case of any unexpected exception
  // we need to at least reset OB.Model.Discounts.calculatingBestDealCase; if
  // not, adding lines would result in not computing discounts nor gross
  originalWindowError = window.onerror;
  window.onerror = function () {
    if (originalWindowError) {
      originalWindowError(arguments);
    }
    window.console.error('error calculating best deal case', arguments);
    endProcess();
  }

  OB.Model.Discounts.calculatingBestDealCase = true;
  receipt = originalReceipt.clone();

  originalDeal = 0;
  receipt.get('lines').forEach(function (line) {
    if (line.get('promotions')) {
      _.forEach(line.get('promotions'), function (promo) {
        originalDeal += promo.amt;
      });
    }
  });

  getCandidatesForProducts();
}


//OB.Model.Discounts.calculateBestDealCase(OB.POS.terminal.$.containerWindow.children[0].model.get('order'))