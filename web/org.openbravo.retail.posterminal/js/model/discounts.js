/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
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
    applyPromotionsTimeout: {},
    applyPromotions: function(receipt, line) {
      var stack = OB.UTIL.getStackTrace(
        'OB.Model.Discounts.applyPromotions',
        false
      );
      if (
        stack.indexOf('OB.Model.Discounts.applyPromotions') > -1 &&
        stack.indexOf('Backbone.Model.extend.calculateReceipt') > -1
      ) {
        OB.error(
          "It's forbidden to use applyPromotions from outside of calculateReceipt"
        );
      }
      if (!receipt.get('isBeingDiscounted')) {
        receipt.set('isBeingDiscounted', true, {
          silent: true
        });
        // if the discount algorithm already started, stop pending computations...
        this.executor.removeGroup('discounts');
        // ... and start over
        this.applyPromotionsLat(receipt, line);
      } else {
        receipt.set('reApplyDiscounts', true, {
          silent: true
        });
      }
    },
    finishPromotions: function(receipt, line) {
      _.forEach(receipt.get('lines').models, function(l) {
        l.set('orderManualPromotionsAlreadyApplied', false, {
          silent: true
        });
      });
      receipt.set('isBeingDiscounted', false, {
        silent: true
      });
      if (receipt.get('reApplyDiscounts') === true) {
        receipt.set('reApplyDiscounts', false, {
          silent: true
        });
        OB.Model.Discounts.applyPromotions(receipt, line);
      } else {
        receipt.set('reApplyDiscounts', false, {
          silent: true
        });
        receipt.trigger('applyPromotionsFinished');
      }
      if (
        !OB.UTIL.isNullOrUndefined(
          OB.Model.Discounts.discountRules['4755A35B4DA34F6CB08F15462BA123CF']
        ) &&
        _.size(
          OB.Model.Discounts.discountRules['4755A35B4DA34F6CB08F15462BA123CF']
            .discountedUnits
        ) > 0
      ) {
        receipt.calculateGross();
        OB.Model.Discounts.discountRules[
          '4755A35B4DA34F6CB08F15462BA123CF'
        ].discountedUnits = {};
      }
    },
    applyPromotionsLat: function(receipt, line) {
      var me = this;
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreCheckDiscount',
        {
          context: me,
          receipt: receipt,
          line: line
        },
        function(args) {
          if (
            receipt.get('skipApplyPromotions') ||
            receipt.get('cloningReceipt') ||
            me.preventApplyPromotions ||
            args.cancellation === true
          ) {
            OB.Model.Discounts.finishPromotions(receipt, line);
            return;
          }

          var auxReceipt = new OB.Model.Order(),
            oldLines,
            oldLines2,
            isFirstExecution = true;
          OB.UTIL.clone(receipt, auxReceipt);
          auxReceipt.groupLinesByProduct();
          auxReceipt.removeNoDiscountAllowLines();
          me.auxReceiptInExecution = auxReceipt;
          auxReceipt.on('discountsApplied', function() {
            // to avoid several calls to applyPromotions, only will be applied the changes to original receipt for the last call done to applyPromotion
            // so if the auxReceipt is distinct of the last auxReceipt created (last call) then nothing is done
            if (me.auxReceiptInExecution !== auxReceipt) {
              return;
            }

            var continueApplyPromotions = true;

            // replace the promotions with applyNext that they were applied previously
            auxReceipt.removePromotionsCascadeApplied();

            // check if the order lines have changed in the last execution of applyPromotions
            // if they didn't changed, then stop
            if (!OB.UTIL.isNullOrUndefined(oldLines) && oldLines.size() > 0) {
              isFirstExecution = false;
              oldLines2 = new Backbone.Collection();
              oldLines.forEach(function(ol) {
                oldLines2.push(ol);
              });

              oldLines2.forEach(function(ol) {
                for (i = 0; i < auxReceipt.get('lines').size(); i++) {
                  if (
                    auxReceipt.isSimilarLine(ol, auxReceipt.get('lines').at(i))
                  ) {
                    oldLines.remove(ol);
                  }
                }
              });

              if (oldLines.length === 0) {
                continueApplyPromotions = false;
              }
            } else if (
              !OB.UTIL.isNullOrUndefined(oldLines) &&
              oldLines.size() === 0 &&
              !isFirstExecution
            ) {
              continueApplyPromotions = false;
            }

            if (continueApplyPromotions) {
              receipt.fillPromotionsWith(auxReceipt, isFirstExecution);
              if (auxReceipt.hasPromotions()) {
                auxReceipt.removeQtyOffer();
                if (auxReceipt.get('lines').length > 0) {
                  oldLines = new Backbone.Collection();
                  auxReceipt.get('lines').forEach(function(l) {
                    var clonedLine = l.clone();
                    clonedLine.set(
                      'promotions',
                      _.clone(clonedLine.get('promotions'))
                    );
                    oldLines.push(clonedLine);
                  });
                  me.applyPromotionsImp(auxReceipt, undefined, true);
                } else {
                  OB.Model.Discounts.finishPromotions(receipt, line);
                }
              } else {
                OB.Model.Discounts.finishPromotions(receipt, line);
              }
            } else {
              OB.Model.Discounts.finishPromotions(receipt, line);
            }
          });

          // if preventApplyPromotions then the promotions will not be deleted, because they will not be recalculated
          if (!me.preventApplyPromotions) {
            var manualPromotions = [];
            _.each(auxReceipt.get('lines').models, function(line) {
              manualPromotions = _.filter(line.get('promotions'), function(p) {
                return p.manual === true;
              });
              line.set('manualPromotions', manualPromotions);
              line.set('promotions', []);
              line.set('promotionCandidates', []);
            });
            _.each(receipt.get('lines').models, function(line) {
              if (OB.UTIL.isNullOrUndefined(line.get('originalOrderLineId'))) {
                // Clean the promotions only if the line is not a return
                line.set('promotions', []);
                line.set('promotionCandidates', []);
              }
            });
          }
          me.applyPromotionsImp(auxReceipt, null, true);
        }
      );
    },

    applyPromotionsImp: function(receipt, line, skipSave, avoidTrigger) {
      var lines;
      if (this.preventApplyPromotions) {
        return;
      }

      if (
        receipt &&
        (!receipt.get('isEditable') ||
          (!OB.UTIL.isNullOrUndefined(receipt.get('isNewReceipt')) &&
            receipt.get('isNewReceipt')))
      ) {
        receipt.trigger('discountsApplied');
      }

      if (line) {
        this.executor.addEvent(
          new Backbone.Model({
            id: line.cid,
            groupId: 'discounts',
            receipt: receipt,
            line: line,
            skipSave: skipSave,
            avoidTrigger: avoidTrigger
          }),
          true
        );
      } else {
        lines = _.sortBy(receipt.get('lines').models, function(lo) {
          return -lo.getQty();
        });
        if (lines.length === 0) {
          receipt.trigger('discountsApplied');
        }
        lines.forEach(function(l) {
          // with new flow discounts -> skipSave =true
          this.applyPromotionsImp(receipt, l, true, true);
        }, this);
        this.executor.nextEvent();
      }
    },

    _addManualPromotionToLine: function(
      manualPromotions,
      line,
      promotionRule,
      promotionDefinition
    ) {
      if (line.get('qty') < 0 && !promotionRule.obdiscAllowinnegativelines) {
        OB.UTIL.showWarning(
          OB.I18N.getLabel('OBPOS_AvoidApplyManualPromotions')
        );
        return manualPromotions;
      }

      // look for the same m_offer_id with the same instanceid
      const manualPromoDefined = manualPromotions.find(manualPromo => {
        return (
          manualPromo.ruleId === promotionRule.id &&
          manualPromo.discountinstance ===
            promotionDefinition.discountinstance &&
          manualPromo.splitAmt === promotionDefinition.splitAmt
        );
      });

      if (
        manualPromoDefined &&
        manualPromoDefined.linesToApply.indexOf(line.get('id')) !== -1
      ) {
        return manualPromotions;
      }

      if (
        manualPromoDefined &&
        manualPromoDefined.linesToApply.indexOf(line.get('id')) === -1 &&
        manualPromoDefined.userAmt === promotionDefinition.userAmt
      ) {
        manualPromoDefined.linesToApply.push(line.get('id'));
        return manualPromotions;
      }

      if (
        !manualPromoDefined ||
        (manualPromoDefined &&
          manualPromoDefined.linesToApply.indexOf(line.get('id')) === -1 &&
          manualPromoDefined.userAmt !== promotionDefinition.userAmt)
      ) {
        let manualPromoObj = {};

        // Create an instance of current promotion
        for (let key in promotionRule) {
          if (key === 'creationDate') {
            continue;
          }
          manualPromoObj[key] = promotionRule[key];
        }
        manualPromoObj.discountinstance = promotionDefinition.discountinstance;

        // Add this line as applicable line
        manualPromoObj.linesToApply = [line.get('id')];

        // Override some configuration from manualPromotions
        if (
          manualPromoObj.discountType === '7B49D8CC4E084A75B7CB4D85A6A3A578' ||
          manualPromoObj.discountType === 'D1D193305A6443B09B299259493B272A'
        ) {
          manualPromoObj.obdiscAmt = promotionDefinition.splitAmt
            ? promotionDefinition.splitAmt
            : promotionDefinition.userAmt;
        } else if (
          manualPromoObj.discountType === '8338556C0FBF45249512DB343FEFD280' ||
          manualPromoObj.discountType === '20E4EC27397344309A2185097392D964'
        ) {
          manualPromoObj.obdiscPercentage = promotionDefinition.splitAmt
            ? promotionDefinition.splitAmt
            : promotionDefinition.userAmt;
          if (
            manualPromoObj.discountType === '20E4EC27397344309A2185097392D964'
          ) {
            manualPromoObj.identifier = `${promotionDefinition.name ||
              promotionRule.printName ||
              promotionRule.name} - ${promotionDefinition.userAmt} %`;
          }
        } else if (
          manualPromoObj.discountType === 'F3B0FB45297844549D9E6B5F03B23A82'
        ) {
          manualPromoObj.obdiscLineFinalgross = promotionDefinition.splitAmt
            ? promotionDefinition.splitAmt
            : promotionDefinition.userAmt;
        }
        manualPromoObj.ruleId = manualPromoObj.id;
        manualPromoObj.noOrder = promotionDefinition.noOrder;
        manualPromoObj.userAmt = promotionDefinition.userAmt;
        manualPromoObj.splitAmt = promotionDefinition.splitAmt;
        manualPromoObj.products = [];
        manualPromoObj.includedProducts = 'Y';
        manualPromoObj.productCategories = [];
        manualPromoObj.includedProductCategories = 'Y';
        manualPromoObj.productCharacteristics = [];
        manualPromoObj.includedCharacteristics = 'Y';
        manualPromoObj.allweekdays = true;
        manualPromoObj.rule = promotionRule;

        manualPromotions.push(manualPromoObj);

        manualPromotions = manualPromotions.sort((a, b) => {
          return a.noOrder - b.noOrder;
        });

        return manualPromotions;
      }
    },

    addManualPromotion: function(receipt, lines, promotion) {
      const promotionRule = promotion.rule.get
          ? promotion.rule.attributes
          : promotion.rule,
        promotionDefinition = promotion.definition,
        linesToApply =
          lines instanceof Backbone.Collection ? [...lines.models] : [...lines],
        rule = OB.Model.Discounts.discountRules[promotionRule.discountType];
      if (!rule || !rule.addManual) {
        OB.warn('No manual implemetation for rule ' + promotion.discountType);
        return;
      }

      if (promotionRule.obdiscAllowmultipleinstan) {
        promotionDefinition.discountinstance = OB.UTIL.get_UUID();
      }

      linesToApply.forEach(line => {
        const discountsFromUser = { ...receipt.get('discountsFromUser') } || {};
        let manualPromotions =
          discountsFromUser && discountsFromUser.manualPromotions
            ? [...discountsFromUser.manualPromotions]
            : [];
        discountsFromUser.manualPromotions = this._addManualPromotionToLine(
          manualPromotions,
          line,
          promotionRule,
          promotionDefinition
        );
        receipt.set('discountsFromUser', discountsFromUser);
      });

      receipt.setUndo('AddDiscount', {
        text: OB.I18N.getLabel('OBPOS_AddedDiscount', [promotionRule.name]),
        undo: function() {
          let manualPromotions = receipt.get('discountsFromUser')
            .manualPromotions;
          let promotionToDelete = manualPromotions.find(manualPromotion => {
            return (
              manualPromotion.id === promotionRule.id &&
              (manualPromotion.discountinstance ===
                promotionRule.discountinstance ||
                promotionRule.obdiscAllowmultipleinstan)
            );
          });
          if (promotionToDelete) {
            manualPromotions.splice(
              manualPromotions.indexOf(promotionToDelete),
              1
            );
          }
          receipt.calculateReceipt();
          receipt.set('undo', null);
          receipt.set('multipleUndo', null);
        }
      });

      receipt.calculateReceipt();
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
