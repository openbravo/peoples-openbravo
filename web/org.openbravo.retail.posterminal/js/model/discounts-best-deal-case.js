/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _, console, Backbone*/

OB.Model.Discounts.calculateBestDealCase = function (originalReceipt, callback) {
  var allCombinations = [],
      cases = 0,
      originalDeal, bestDiscount, totalBestDiscount = [],
      receipt, originalWindowError, linesAfterSplit, nextCase;

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

  function finalize() {
    var lines = receipt.get('lines'),
        totalDiscount = 0;

    _.forEach(totalBestDiscount, function (bestSubCase) {
      totalDiscount += bestSubCase.totalDiscount;
    });

    if (totalDiscount > originalDeal) {
      // Best Deal found, applying it to the cloned receipt...
      console.log('found best deal case', totalBestDiscount);
      _.forEach(linesAfterSplit, function (line) {
        lines.remove(line);
      });

      _.forEach(totalBestDiscount, function (bestSubCase) {
        lines.add(bestSubCase.lines);
      });

      // ...and reseting original receipt's lines with best case ones
      originalReceipt.get('lines').reset();
      originalReceipt.get('lines').add(lines.models);

      originalReceipt.mergeLinesWithSamePromotions();

      originalReceipt.calculateGross();

      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BDC.Found', [OB.DEC.add(totalDiscount, 0), originalDeal]));
      originalReceipt.save();
    } else {
      console.log('Already in Best Deal, no change required');
    }
    endProcess();
  }

  function evalCase(currentCase, pos) {
    var currentCaseLine, rule, ruleListener, lines;

    if (pos === currentCase.length) {
      //already evaluated all lines in current case
      lines = receipt.get('lines');
      lines.reset();
      _.forEach(linesAfterSplit, function (line) {
        lines.add(line);
      });
      nextCase(currentCase);
      return;
    }

    currentCaseLine = currentCase[pos];
    rule = OB.Model.Discounts.discountRules[currentCaseLine.rule.get('discountType')];

    if (rule.async) {
      // waiting listener to trigger completed to move to next line
      ruleListener = new Backbone.Model();
      ruleListener.on('completed', function (obj) {
        ruleListener.off();
        evalCase(currentCase, pos + 1);
      }, this);
    }

    rule.implementation(currentCaseLine.rule, receipt, currentCaseLine.line.line, ruleListener);
    if (!rule.async) {
      // done, move to next line
      evalCase(currentCase, pos + 1);
    }
  }

  function evaluateSubBestDealCase() {
    var lines = receipt.get('lines'),
        currentCase;

    currentCase = allCombinations[allCombinations.length - 1].pop();

    lines.reset();

    lines.reset();
    _.forEach(linesAfterSplit, function (line) {
      line.set('qty', 1);
      lines.add(line);
    });

    _.forEach(currentCase, function (currentCaseLine) {
      var line = currentCaseLine.line.line;
      line.set({
        promotions: null,
        promotionCandidates: [currentCaseLine.rule.id],
        discountedLinePrice: null,
        qty: 1
      }, {
        silent: true
      });
      lines.remove(line);
      lines.add(line);
    });

    evalCase(currentCase, 0);
  }

  nextCase = function (evaluatedCase) {
    var currentDiscount, bestLines, lines = receipt.get('lines');
    currentDiscount = 0;

    _.forEach(evaluatedCase, function (evaluatedCaseLine) {
      var line = evaluatedCaseLine.line.line;
      if (line.get('promotions')) {
        _.forEach(line.get('promotions'), function (promo) {
          currentDiscount += promo.amt;
        });
      }
    });

    if (!bestDiscount || bestDiscount.totalDiscount < currentDiscount) {
      bestLines = [];
      _.forEach(evaluatedCase, function (evaluatedCaseLine) {
        var line = evaluatedCaseLine.line.line;
        bestLines.push(line.clone());
      });

      bestDiscount = {
        totalDiscount: currentDiscount,
        lines: bestLines
      };
    }

    if (allCombinations[allCombinations.length - 1].length === 0) {
      allCombinations.pop(); //evaluated top case, go for next one
      totalBestDiscount.push({
        totalDiscount: bestDiscount.totalDiscount,
        lines: bestDiscount.lines
      });
      bestDiscount = null;
    }

    if (allCombinations.length && allCombinations[allCombinations.length - 1].length > 0) {
      evaluateSubBestDealCase();
    } else {
      finalize();
    }
  };

  /**
   * Calculates all possible combinations of lines with discounts that need to be evaluated
   */

  function calculateCombinations(groups) {
    var totalNumOfCombinations = 0;

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

    function pick(grp, got, pos, lnum, result, tnum) {
      var l = [],
          z, i;
      if (got.length === grp[tnum].lines.length) {
        for (z = 0; z < got.length; z++) {
          l.push({
            line: got[z].line,
            rule: got[z].rule
          });
        }

        result.push(l);
        return result;
      }

      for (i = pos; i < grp[tnum].rules.length; i++) {
        got.push({
          line: grp[tnum].lines[lnum],
          rule: grp[tnum].rules.at(i)
        });
        pick(grp, got, i, lnum + 1, result, tnum);
        got.pop();
      }
      return result;
    }

    function calculate(grp) {
      var res, i, result = [],
          picked;
      for (i = 0; i < grp.length; i++) {
        res = [];
        picked = pick(grp, [], 0, 0, res, i);
        result.push(picked);
      }
      return combine(result);
    }

    _.forEach(groups, function (group) {
      var grpCombinations = calculate(group.subGrps);

      console.log(grpCombinations.length, 'combinations for grp', group, grpCombinations);
      totalNumOfCombinations += grpCombinations.length;
      allCombinations.push(grpCombinations);
    });

    console.log(totalNumOfCombinations, 'allCombinations', allCombinations);

    if (allCombinations.length > 0) {
      evaluateSubBestDealCase(allCombinations[0]);
    } else {
      // nothing to do
      finalize();
    }
  }

  /**
   * Splits lines with quantity bigger than 1. All the resultant lines
   * will have a quantity of 1 unit. Also lines are associated to the 
   * correct group and subgroup.
   */

  function splitLines(candidates, groups) {
    var i = 0,
        lines = receipt.get('lines'),
        newLines = [],
        numberOfCases = 1;

    console.log('num of lines before split', lines.length);
    lines.forEach(function (line) {
      var originalQty, l, productId = line.get('product').id;


      i += 1;
      if (candidates[productId] && candidates[productId].length > 0 && line.get('qty') > 1) {
        // there are candidates for the product, let's split the line if needed
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
        console.log('not split line', line.get('product').get('_identifier'));
      }
    });

    lines.add(newLines);

    linesAfterSplit = [];
    lines.forEach(function (line) {
      linesAfterSplit.push(line.clone());
    });

    _.forEach(groups, function (group) {
      linesAfterSplit.forEach(function (line) {
        var prodId = line.get('product').id,
            productCases = candidates[prodId],
            appliedPromosToLine;
        if (!productCases || productCases.length === 0 || group.products.indexOf(prodId) === -1) {
          return; //continue
        }
        appliedPromosToLine = line.get('promotions');
        if (appliedPromosToLine && appliedPromosToLine.length > 0) {
          // assuming just one promo per line
          if (OB.Model.Discounts.getManualPromotions(true).indexOf(appliedPromosToLine[0].discountType) !== -1) {
            console.log('line has manual promo', line);
            return; //continue
          }
        }

        group.productsInSubGrps[prodId].lines.push({
          line: line,
          candidates: candidates[line.get('product').id],
          pointer: 0
        });
      });

    });

    console.log('num of lines after split', lines.length, receipt.get('lines').length);
    console.log('groups', groups);

    calculateCombinations(groups);
  }

  /**
   * Based on candidates for each product, independent groups are created. 
   * 
   * An independent group is a set of products for which promotions can be 
   * independently applied without taking into account other groups. The 
   * algorithm will calculate partial best deal cases for each of these groups.
   * 
   * Each group has a series of subgroups, each subgroup is a list of products
   * for which the same discount rules can be applied.
   */

  function doGroups(candidates) {
    var groups = [],
        newGroups = [],
        myProdId, productId, i, g, foundGroup, rules, ruleIDs, addToGroup, findGroup, pushRule, findGrpByCandidate, subGrps;

    addToGroup = function (rule) {
      if (groups[g].ruleIDs.indexOf(rule.id) === -1) {
        groups[g].rules.push(rule);
        groups[g].ruleIDs.push(rule.id);
        // TODO: merge groups
      }
    };

    findGroup = function (rule) {
      var rules, ruleIDs;
      if (foundGroup) {
        return;
      }
      for (g = 0; g < groups.length; g++) {
        if (groups[g].ruleIDs.indexOf(rule.id) !== -1) {
          foundGroup = true;
          groups[g].products.push(productId);
          candidates[productId].forEach(addToGroup);
        }
      }
    };

    pushRule = function (rule) {
      rules.push(rule);
      ruleIDs.push(rule.id);
    };

    findGrpByCandidate = function (candidate) {
      if (subGrps[i].ruleIds.indexOf(candidate.id) !== -1) {
        foundGroup = true;
      }
    };

    for (myProdId in candidates) {
      if (candidates.hasOwnProperty(myProdId)) {
        productId = myProdId;
        foundGroup = false;
        if (candidates[productId].length === 0) {
          continue;
        }
        candidates[productId].forEach(findGroup); // loop of rules within candidate
        if (!foundGroup) {
          rules = [];
          ruleIDs = [];
          candidates[productId].forEach(pushRule);
          groups.push({
            rules: rules,
            ruleIDs: ruleIDs,
            products: [productId],
            productsInSubGrps: {}
          });
        }
      }
    } // loop of candidates
    // remove groups having just one rule, they don't need to be calculated
    // because they are already in best deal case
    _.forEach(groups, function (group) {
      if (group.ruleIDs.length > 1) {
        newGroups.push(group);
      }
    });
    groups = newGroups;

    // now we have groups of products with conflicts between them,
    // lets split these groups in subgroups each of them with the 
    // same rules candidates
    _.forEach(groups, function (group) {
      subGrps = [];
      _.forEach(group.products, function (productId) {
        var prodCandidates = candidates[productId];
        foundGroup = false;
        for (i = 0; i < subGrps.length; i++) {
          if (subGrps[i].ruleIds.length === prodCandidates.length) {
            prodCandidates.forEach(findGrpByCandidate);
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

  /**
   * Gets all the discount candidates that can be applied to each line in the ticket
   */

  function getCandidatesForProducts() {
    var criteria, de = new OB.Model.DiscountsExecutor(),
        whereClause = OB.Model.Discounts.standardFilter + ' AND M_OFFER_TYPE_ID NOT IN (' + OB.Model.Discounts.getManualPromotions() + ')',
        lines, candidates = {},
        i = 0;

    lines = receipt.get('lines');
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
  };

  OB.Model.Discounts.calculatingBestDealCase = true;

  // clone original ticket to bypass ui changes during the calculation
  receipt = originalReceipt.clone();

  // calculate original discount not to do modifications in the ticket in case
  // it is already the best deal
  originalDeal = 0;
  receipt.get('lines').forEach(function (line) {
    if (line.get('promotions')) {
      _.forEach(line.get('promotions'), function (promo) {
        originalDeal += promo.amt || 0;
      });
    }
  });

  originalDeal = OB.DEC.add(originalDeal, 0);

  getCandidatesForProducts();
};