/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB */

(function() {
  OB.Discounts = OB.Discounts || {};
  OB.Discounts.Pos = {
    local: true,

    calculateLocal: function(ticket) {
      if (!OB.Discounts.Pos.ruleImpls) {
        throw 'Local discount cache is not yet initialized, execute: OB.Discounts.Pos.initCache()';
      }
      return OB.Discounts.applyDiscounts(
        ticket,
        OB.Discounts.Pos.ruleImpls,
        OB.Discounts.Pos.bpSets
      );
    },

    calculateRemote: function(ticket) {
      ticket = JSON.stringify(ticket);
      fetch('../../discount', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: ticket
      })
        .then(response => response.json())
        .then(disc => OB.info(disc));
    },

    applyDiscounts: function(ticket, result) {
      ticket.get('lines').forEach(line => {
        const discountInfoForLine =
            result.lines[line.get('id')] &&
            result.lines[line.get('id')].discounts.promotions,
          excludedFromEnginePromotions = line.get('promotions')
            ? line.get('promotions').filter(promo => {
                return !promo.calculatedOnDiscountEngine;
              })
            : [];
        if (!discountInfoForLine) {
          //No discounts for this line, we keep existing discounts if they exist, and move to the next
          line.set('promotions', excludedFromEnginePromotions);
          return;
        }

        // Concatenate new promotions and excluded promotions in line
        line.set('promotions', [
          ...excludedFromEnginePromotions,
          ...discountInfoForLine
        ]);
        return;
      });
    },

    translateManualPromotionsForEngine(receipt) {
      let manualPromotions = [];
      receipt.get('lines').models.forEach(line => {
        if (line.get('promotions') && line.get('promotions').length > 0) {
          let lineManualPromos =
            line.get('promotions').filter(promo => {
              return promo.manual;
            }) || [];
          lineManualPromos.forEach(lineManualPromo => {
            let manualPromoObj = manualPromotions.find(manualPromo => {
              return (
                manualPromo.ruleId === lineManualPromo.ruleId &&
                manualPromo.discountinstance ===
                  lineManualPromo.discountinstance &&
                manualPromo.noOrder === lineManualPromo.noOrder &&
                manualPromo.splitAmt === lineManualPromo.splitAmt
              );
            });
            if (manualPromoObj) {
              manualPromoObj.linesToApply.push(line.get('id'));
            } else {
              let manualPromoObj = {};

              // Create an instance of current promotion
              for (let key in lineManualPromo) {
                manualPromoObj[key] = lineManualPromo[key];
              }

              // Add this line as applicable line
              manualPromoObj.linesToApply = [line.get('id')];

              // Override some configuration from manualPromotions
              if (
                manualPromoObj.discountType ===
                  '7B49D8CC4E084A75B7CB4D85A6A3A578' ||
                manualPromoObj.discountType ===
                  'D1D193305A6443B09B299259493B272A'
              ) {
                manualPromoObj.obdiscAmt = manualPromoObj.userAmt;
              } else if (
                manualPromoObj.discountType ===
                  '8338556C0FBF45249512DB343FEFD280' ||
                manualPromoObj.discountType ===
                  '20E4EC27397344309A2185097392D964'
              ) {
                manualPromoObj.obdiscPercentage = manualPromoObj.userAmt;
              } else if (
                manualPromoObj.discountType ===
                'F3B0FB45297844549D9E6B5F03B23A82'
              ) {
                manualPromoObj.obdiscLineFinalgross = manualPromoObj.userAmt;
              }
              manualPromoObj.id = manualPromoObj.ruleId;
              manualPromoObj.products = [];
              manualPromoObj.includedProducts = 'Y';
              manualPromoObj.productCategories = [];
              manualPromoObj.includedProductCategories = 'Y';
              manualPromoObj.productCharacteristics = [];
              manualPromoObj.includedCharacteristics = 'Y';
              manualPromoObj.allweekdays = true;
              manualPromoObj.rule = lineManualPromo.rule;

              manualPromotions.push(manualPromoObj);
            }
          });
        }
      });

      return manualPromotions.sort((a, b) => {
        return a.noOrder - b.noOrder;
      });
    },

    removeManualPromotionFromLines(receipt) {
      receipt.get('lines').models.forEach(line => {
        if (line.get('promotions') && line.get('promotions').length > 0) {
          let exludeManualPromotions = line.get('promotions').filter(promo => {
            return !promo.manual;
          });
          line.set('promotions', exludeManualPromotions || []);
        }
      });
    },

    translateTicket: function(receipt) {
      let newTicket = {};
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner.businessPartnerCategory = receipt
        .get('bp')
        .get('businessPartnerCategory');
      newTicket.businessPartner._identifier = receipt.get('bp')._identifier;
      if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
        newTicket.pricelist = receipt.get('bp').get('priceList');
      } else {
        newTicket.pricelist = OB.MobileApp.model.get('pricelist').id;
      }
      newTicket.id = receipt.get('id');
      newTicket.date = receipt.get('orderDate');
      newTicket.discountsFromUser = {};
      newTicket.lines = [];
      receipt.get('lines').forEach(line => {
        let newLine = {};

        newLine.id = line.get('id');
        newLine.product = line.get('product').toJSON();
        newLine.qty = line.get('qty');
        newLine.price = line.get('price');
        newLine.promotions = [];
        newTicket.lines.push(newLine);
      });
      if (receipt.get('coupons')) {
        newTicket.discountsFromUser.coupons = JSON.parse(
          JSON.stringify(receipt.get('coupons'))
        );
      }
      if (receipt.get('orderManualPromotions')) {
        let bytotalManualPromotions = [];
        let orderManualPromotions = receipt.get('orderManualPromotions').models;
        orderManualPromotions.forEach(bytotalManualPromotion => {
          let rule = new Backbone.Model(
              bytotalManualPromotion.get('discountRule')
            ),
            bytotalManualPromotionObj = {};
          for (let key in rule.attributes) {
            bytotalManualPromotionObj[key] = rule.attributes[key];
          }

          // Override some configuration from manualPromotions
          if (bytotalManualPromotionObj.disctTotalamountdisc) {
            bytotalManualPromotionObj.disctTotalamountdisc = bytotalManualPromotion.get(
              'rule'
            ).userAmt;
          } else if (bytotalManualPromotionObj.disctTotalpercdisc) {
            bytotalManualPromotionObj.disctTotalpercdisc = bytotalManualPromotion.get(
              'rule'
            ).userAmt;
          }
          bytotalManualPromotionObj.noOrder = bytotalManualPromotion.get(
            'rule'
          ).noOrder;
          bytotalManualPromotionObj.discountinstance = bytotalManualPromotion.get(
            'rule'
          ).discountinstance;
          if (
            OB.Model.Discounts.discountRules[
              bytotalManualPromotionObj.discountType
            ] &&
            OB.Model.Discounts.discountRules[
              bytotalManualPromotionObj.discountType
            ].getIdentifier
          ) {
            let promotionName = OB.Model.Discounts.discountRules[
              bytotalManualPromotionObj.discountType
            ].getIdentifier(rule, bytotalManualPromotionObj);
            bytotalManualPromotionObj.name = promotionName;
            bytotalManualPromotionObj._identifier = promotionName;
          }
          bytotalManualPromotionObj.products = [];
          bytotalManualPromotionObj.includedProducts = 'Y';
          bytotalManualPromotionObj.productCategories = [];
          bytotalManualPromotionObj.includedProductCategories = 'Y';
          bytotalManualPromotionObj.productCharacteristics = [];
          bytotalManualPromotionObj.includedCharacteristics = 'Y';
          bytotalManualPromotionObj.allweekdays = true;

          bytotalManualPromotions.push(bytotalManualPromotionObj);
        });
        newTicket.discountsFromUser.bytotalManualPromotions = bytotalManualPromotions;
      }

      let manualPromotions = this.translateManualPromotionsForEngine(receipt);
      newTicket.discountsFromUser.manualPromotions = manualPromotions;
      this.removeManualPromotionFromLines(receipt);
      return newTicket;
    },

    transformNewEngineManualPromotions(ticket, ticketManualPromos, result) {
      ticket.get('lines').forEach(line => {
        const discountInfoForLine =
          result.lines[line.get('id')] &&
          result.lines[line.get('id')].discounts.promotions;
        if (!discountInfoForLine || discountInfoForLine.length === 0) {
          return;
        }
        // Create new instances of original definitions for manual promotions
        discountInfoForLine.forEach(promotion => {
          if (promotion.manual) {
            let promotionRuleId = promotion.ruleId,
              promotionDiscountInstance = promotion.discountinstance,
              promotionNoOrder = promotion.noOrder,
              promotionSplitAmt = promotion.splitAmt;

            let discountInstance = ticketManualPromos.find(
              ticketManualPromo => {
                return (
                  ticketManualPromo.ruleId === promotionRuleId &&
                  ticketManualPromo.discountinstance ===
                    promotionDiscountInstance &&
                  ticketManualPromo.noOrder === promotionNoOrder &&
                  ticketManualPromo.splitAmt === promotionSplitAmt
                );
              }
            );

            let newPromoInstance = {};

            for (let key in discountInstance) {
              newPromoInstance[key] = discountInstance[key];
            }

            for (let key in promotion) {
              newPromoInstance[key] = promotion[key];
            }

            delete newPromoInstance.linesToApply;

            for (let key in newPromoInstance) {
              promotion[key] = newPromoInstance[key];
            }
          }
          promotion.calculatedOnDiscountEngine = true;
          promotion.obdiscQtyoffer = promotion.qtyOffer;
          promotion.displayedTotalAmount = promotion.amt;
          promotion.fullAmt = promotion.amt;
          promotion.actualAmt = promotion.amt;
        });
      });
    },

    calculateDiscounts(receipt, callback) {
      const ticketForEngine = OB.Discounts.Pos.translateTicket(receipt);
      let result;
      if (OB.Discounts.Pos.local) {
        result = OB.Discounts.Pos.calculateLocal(ticketForEngine);
        this.transformNewEngineManualPromotions(
          receipt,
          ticketForEngine.discountsFromUser.manualPromotions,
          result
        );
        OB.Discounts.Pos.applyDiscounts(receipt, result);
        callback();
      } else {
        result = OB.Discounts.Pos.calculateRemote(receipt, callback);
      }
    },

    getDiscounts: async function(orderBy) {
      const discountArrayPromise = orderBy
        ? await OB.App.MasterdataModels.Discount.orderedBy(orderBy)
        : await OB.App.MasterdataModels.Discount.find();
      return discountArrayPromise.result;
    },

    addDiscountFilter: async function(
      discountArray,
      filterModel,
      filterName,
      filterEntity,
      filterGroup,
      filterFunction
    ) {
      const filterArrayPromise = await filterModel.orderedBy('_identifier');
      const filterArray = filterArrayPromise.result;

      if (filterEntity) {
        filterArray.forEach(
          filter => (filter[filterEntity] = { id: filter[filterEntity] })
        );
      }

      if (filterFunction) {
        await filterFunction(filterArray);
      }

      const filterArrayByDiscount = filterArray.reduce(
        (filterArrayByDiscount, filter) => {
          const discountFilterGroup = filterGroup
            ? filter[filterGroup]
            : filter['priceAdjustment'];
          (filterArrayByDiscount[discountFilterGroup] =
            filterArrayByDiscount[discountFilterGroup] || []).push(filter);
          return filterArrayByDiscount;
        },
        {}
      );

      discountArray.forEach(
        discount =>
          (discount[filterName] = filterArrayByDiscount[discount.id] || [])
      );

      return discountArray;
    },

    addDiscountsByRoleFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterRole,
        'roles'
      );
    },

    addDiscountsByProductFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterProduct,
        'products',
        'product'
      );
    },

    addDiscountsByProductCategoryFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterProductCategory,
        'productCategories',
        'productCategory'
      );
    },

    addDiscountsByCharacteristicFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterCharacteristic,
        'productCharacteristics'
      );
    },

    addDiscountsByBusinessPartnerFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterBusinessPartner,
        'cbpartners',
        'businessPartner'
      );
    },

    addDiscountsByBusinessPartnerGroupFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterBusinessPartnerGroup,
        'cbpartnerGroups',
        'businessPartnerCategory'
      );
    },

    addDiscountsByBusinessPartnerSetFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterBusinessPartnerSet,
        'cbpartnerSets'
      );
    },

    addDiscountsByPriceListFilter: async function(discountArray) {
      return OB.Discounts.Pos.addDiscountFilter(
        discountArray,
        OB.App.MasterdataModels.DiscountFilterPriceList,
        'pricelists'
      );
    },

    filterDiscountById: function(
      discountArray,
      filterIncludeName,
      filterChildrenName,
      filterIdName,
      id
    ) {
      return discountArray.filter(
        discount =>
          (discount[filterIncludeName] === 'Y' &&
            (!discount[filterChildrenName] ||
              !discount[filterChildrenName].find(
                filter => filter[filterIdName] === id
              ))) ||
          (discount[filterIncludeName] === 'N' &&
            (discount[filterChildrenName] &&
              discount[filterChildrenName].find(
                filter => filter[filterIdName] === id
              )))
      );
    },

    filterDiscountsByManual: function(discountArray, isManual) {
      const manualDiscountArray = OB.Model.Discounts.getManualPromotions(true);

      return discountArray.filter(discount =>
        isManual
          ? manualDiscountArray.includes(discount.discountType)
          : !manualDiscountArray.includes(discount.discountType)
      );
    },

    filterDiscountsByDate: function(discountArray, date) {
      return discountArray.filter(
        discount =>
          new Date(discount.startingDate) <= date &&
          (!discount.endingDate || new Date(discount.endingDate) >= date)
      );
    },

    filterDiscountsByRole: function(discountArray, roleId) {
      return OB.Discounts.Pos.filterDiscountById(
        discountArray,
        'oBDISCIncludedRoles',
        'roles',
        'role',
        roleId
      );
    },

    filterDiscountsByPriceList: function(discountArray, priceListId) {
      return OB.Discounts.Pos.filterDiscountById(
        discountArray,
        'includePriceLists',
        'pricelists',
        'm_pricelist_id',
        priceListId
      );
    },

    initCache: async function(basicParams, callback) {
      if (OB.Discounts.Pos.isCalculatingCache) {
        return callback();
      }
      OB.Discounts.Pos.isCalculatingCache = true;
      const execution = OB.UTIL.ProcessController.start(
        'discountCacheInitialization'
      );

      let discountArray = await OB.Discounts.Pos.getDiscounts('id');

      discountArray = await OB.Discounts.Pos.addDiscountsByRoleFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByProductFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByProductCategoryFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByCharacteristicFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByBusinessPartnerFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByBusinessPartnerGroupFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByBusinessPartnerSetFilter(
        discountArray
      );

      discountArray = await OB.Discounts.Pos.addDiscountsByPriceListFilter(
        discountArray
      );

      discountArray = OB.Discounts.Pos.filterDiscountsByRole(
        discountArray,
        OB.MobileApp.model.get('context').role.id
      );

      discountArray.forEach(
        discount => (discount.discountPercentage = discount.discount)
      );

      OB.Discounts.Pos.manualRuleImpls = OB.Discounts.Pos.filterDiscountsByManual(
        discountArray,
        true
      );
      OB.Discounts.Pos.manualRuleImpls = OB.Discounts.Pos.manualRuleImpls.sort(
        (a, b) => a.name.localeCompare(b.name)
      );

      OB.Discounts.Pos.ruleImpls = OB.Discounts.Pos.filterDiscountsByManual(
        discountArray,
        false
      );
      OB.Discounts.Pos.ruleImpls = OB.Discounts.Pos.ruleImpls.sort(
        (a, b) => a.priority - b.priority
      );

      //BPSets
      const bpSetLineArrayPromise = await OB.App.MasterdataModels.BPSetLine.find();
      const bpSetLineArray = bpSetLineArrayPromise.result;
      const bpSetLineArrayByBPSet = bpSetLineArray.reduce(
        (bpSetLineArrayByBPSet, bpSetLine) => {
          (bpSetLineArrayByBPSet[bpSetLine.bpSet] =
            bpSetLineArrayByBPSet[bpSetLine.bpSet] || []).push(bpSetLine);
          return bpSetLineArrayByBPSet;
        },
        {}
      );
      OB.Discounts.Pos.bpSets = bpSetLineArrayByBPSet;

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
    }
  };
})();
