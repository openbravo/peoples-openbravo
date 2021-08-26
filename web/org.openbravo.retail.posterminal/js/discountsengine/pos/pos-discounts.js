/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Discounts = OB.Discounts || {};
  OB.Discounts.Pos = OB.Discounts.Pos || {};

  const applyDiscounts = (ticket, result) => {
    ticket.get('lines').forEach(line => {
      const discountLine = result.lines.find(l => l.id === line.get('id'));
      const excludedFromEnginePromotions = line.get('promotions')
        ? line.get('promotions').filter(promo => {
            return !promo.calculatedOnDiscountEngine;
          })
        : [];
      if (!discountLine) {
        //No discounts for this line, we keep existing discounts if they exist, and move to the next
        line.set('promotions', excludedFromEnginePromotions);
        return;
      }

      // Concatenate new promotions and excluded promotions in line
      line.set('promotions', [
        ...excludedFromEnginePromotions,
        ...discountLine.discounts
      ]);
      return;
    });
  };

  OB.Discounts.Pos.calculateDiscounts = (receipt, callback) => {
    if (!OB.Discounts.Pos.ruleImpls) {
      throw 'Local discount cache is not yet initialized, execute: OB.Discounts.Pos.initCache()';
    }
    // This hook cannot be asynchronous
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreApplyNewDiscountEngine',
      {
        receipt: receipt,
        rules: [...OB.Discounts.Pos.ruleImpls]
      },
      args => {
        const ticket = OB.App.StateBackwardCompatibility.getInstance(
          'Ticket'
        ).toStateObject(receipt);
        const result = OB.Discounts.Pos.applyDiscounts(
          ticket,
          args.rules,
          OB.Discounts.Pos.bpSets
        );
        applyDiscounts(receipt, result);
        callback();
      }
    );
  };

  /**
   * Reads discount masterdata models from database and creates different caches to use them:
   *   OB.Discounts.Pos.manualRuleImpls: array with manual discounts and promotions including children filters, filtered by current role and sorted by name.
   *   OB.Discounts.Pos.ruleImpls: array with not manual discounts and promotions including children filters, filtered by current role and sorted by priority and id (null priorities first).
   *   OB.Discounts.Pos.bpSets: array with business partner sets.
   * It also runs OBPOS_DiscountsCacheInitialization hook.
   * Discount masterdata models should be read from database only here. Wherever discount data is needed, any of these caches should be used.
   */
  OB.Discounts.Pos.initCache = async callback => {
    if (OB.Discounts.Pos.isCalculatingCache) {
      return callback();
    }
    OB.Discounts.Pos.isCalculatingCache = true;
    const execution = OB.UTIL.ProcessController.start(
      'discountCacheInitialization'
    );

    try {
      const data = await OB.Discounts.Pos.loadData();
      Object.assign(OB.Discounts.Pos, data);
    } catch (e) {
      callback();
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_LblWarning'),
        OB.I18N.getLabel('OBPOS_DiscountDataExceedsCacheSize'),
        null,
        {
          autoDismiss: false,
          hideCloseButton: true,
          closeOnEscKey: false
        }
      );
    }

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_DiscountsCacheInitialization',
      {
        discounts: OB.Discounts.Pos.ruleImpls
      },
      function(args) {
        OB.UTIL.ProcessController.finish(
          'discountCacheInitialization',
          execution
        );
        callback();
        delete OB.Discounts.Pos.isCalculatingCache;
      }
    );
  };
})();
