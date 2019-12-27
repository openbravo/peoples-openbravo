/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  // Because of problems with module dependencies, it is possible this object to already
  // be defined with some rules.
  var alreadyDefinedRules =
      (OB &&
        OB.Model &&
        OB.Model.Discounts &&
        OB.Model.Discounts.discountRules) ||
      {},
    onLoadActions =
      (OB &&
        OB.Model &&
        OB.Model.Discounts &&
        OB.Model.Discounts.onLoadActions) ||
      [],
    i;

  OB.Model.Discounts = {
    discountRules: alreadyDefinedRules,
    preventApplyPromotions: false,
    addManualPromotion: function(receipt, lines, promotion) {
      var rule =
        OB.Model.Discounts.discountRules[
          promotion.rule.get
            ? promotion.rule.get('discountType')
            : promotion.rule.discountType
        ];
      if (!rule || !rule.addManual) {
        OB.warn('No manual implemetation for rule ' + promotion.discountType);
        return;
      }

      if (promotion.rule.get('obdiscAllowmultipleinstan')) {
        promotion.definition.discountinstance = OB.UTIL.get_UUID();
      }

      lines.forEach(function(line) {
        if (line.get('promotions')) {
          line.get('promotions').forEach(function(promotion) {
            promotion.lastApplied = undefined;
          });
        }
        line.unset('noDiscountCandidates', {
          silent: true
        });
        if (
          line.get('qty') > 0 ||
          (line.get('qty') < 0 &&
            promotion.rule.get('obdiscAllowinnegativelines'))
        ) {
          rule.addManual(receipt, line, promotion);
          line.set('singleManualPromotionApplied', true);
        } else {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_AvoidApplyManualPromotions')
          );
        }
      });

      receipt.setUndo('AddDiscount', {
        text: OB.I18N.getLabel('OBPOS_AddedDiscount', [
          promotion.rule.get('name')
        ]),
        undo: function() {
          receipt.get('lines').forEach(function(line) {
            receipt.removePromotion(line, {
              id: promotion.rule.get('id'),
              discountinstance: promotion.definition.discountinstance
            });
          });
          receipt.calculateReceipt();
          receipt.set('undo', null);
          receipt.set('multipleUndo', null);
        }
      });

      if (!promotion.alreadyCalculated) {
        // Recalculate all promotions again
        receipt.calculateReceipt();
      }
    },

    registerRule: function(name, rule) {
      this.discountRules[name] = rule;
    }
  };

  // Because of dependency models cannot be directly registered in promotions module
  if (OB && OB.Model && OB.Model.Discounts && OB.Model.Discounts.extraModels) {
    for (i = 0; i < OB.Model.Discounts.extraModels.length; i++) {
      OB.Data.Registry.registerModel(OB.Model.Discounts.extraModels[i]);
    }
  }

  for (i = 0; i < onLoadActions.length; i++) {
    if (onLoadActions[i].execute) {
      onLoadActions[i].execute();
    }
  }
})();
