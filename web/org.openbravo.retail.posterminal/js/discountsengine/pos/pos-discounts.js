/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
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

    calculateLocal: function(ticket, rules) {
      return OB.Discounts.applyDiscounts(
        ticket,
        rules,
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
            result = OB.Discounts.Pos.calculateLocal(
              ticketForEngine,
              args.rules
            );
            this.transformNewEngineManualPromotions(
              receipt,
              ticketForEngine.discountsFromUser.manualPromotions,
              result
            );
            OB.Discounts.Pos.applyDiscounts(receipt, result);
            callback();
          }
        );
      } else {
        result = OB.Discounts.Pos.calculateRemote(receipt, callback);
      }
    },

    /**
     * Retrieves the list of manual promotions
     * @return {string[]} An array containg the manual promotions
     */
    getManualPromotions: function() {
      return Object.keys(OB.Model.Discounts.discountRules);
    },

    /**
     * Adds a discount child filter to every discount record inside a list.
     * Thus, it will be possible to use the filter to decide if the discount should be applied or not.
     * @param {Object[]} discounts - The list of discount records where child filter will be added.
     * @param {Class<MasterdataModel>} filterModel - The masterdata model of the filter added.
     * @param {string} filterName - The name of the property that will be created in each record of the list with the filter data.
     * @param {string} filterEntity - In case the filter must include an entity.id property instead of an id property, it indicates the name of entity. By default it will be null.
     * @param {string} filterGroup - Indicates the name of the property used to group the filter. By default it will be 'priceAdjustment'.
     * @param {function} filterFunction - Allows to define a function that can be executed to add grandchild filters. By default it will be null.
     * @return {Object[]} The list of discount records with child filter added.
     */
    addDiscountFilter: async function(
      discounts,
      filterModel,
      filterName,
      filterEntity = null,
      filterGroup = 'priceAdjustment',
      filterFunction = null
    ) {
      const filterArray = await filterModel.find(
        new OB.App.Class.Criteria()
          .orderBy([filterGroup, '_identifier', 'id'], 'asc')
          .limit(10000)
          .build()
      );

      if (filterEntity) {
        filterArray.forEach(
          filter => (filter[filterEntity] = { id: filter[filterEntity] })
        );
      }

      if (filterFunction) {
        await filterFunction(filterArray);
      }

      const filterArrayByDiscount = OB.App.ArrayUtils.groupBy(
        filterArray,
        filterGroup
      );

      discounts.forEach(
        discount =>
          (discount[filterName] = filterArrayByDiscount[discount.id] || [])
      );

      return discounts;
    },

    /**
     * Adds role filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where role filter will be added.
     * @return {Object[]} The list of discount records with role filter added.
     */
    addDiscountsByRoleFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterRole,
        'roles'
      );
    },

    /**
     * Adds product filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where product filter will be added.
     * @return {Object[]} The list of discount records with product filter added.
     */
    addDiscountsByProductFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterProduct,
        'products',
        'product'
      );
    },

    /**
     * Adds product category filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where role product category will be added.
     * @return {Object[]} The list of discount records with product category filter added.
     */
    addDiscountsByProductCategoryFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterProductCategory,
        'productCategories',
        'productCategory'
      );
    },

    /**
     * Adds characteristic filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where characteristic filter will be added.
     * @return {Object[]} The list of discount records with characteristic filter added.
     */
    addDiscountsByCharacteristicFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterCharacteristic,
        'productCharacteristics'
      );
    },

    /**
     * Adds business partner filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where business partner filter will be added.
     * @return {Object[]} The list of discount records with business partner filter added.
     */
    addDiscountsByBusinessPartnerFilter: async function(discounts) {
      // FIXME: Make query remotely in case OBPOS_remote.discount.bp filtering by OB.MobileApp.model.get('businessPartner').id
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterBusinessPartner,
        'cbpartners',
        'businessPartner'
      );
    },

    /**
     * Adds business partner group filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where business partner group filter will be added.
     * @return {Object[]} The list of discount records with business partner group filter added.
     */
    addDiscountsByBusinessPartnerGroupFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterBusinessPartnerGroup,
        'cbpartnerGroups',
        'businessPartnerCategory'
      );
    },

    /**
     * Adds business partner set filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where business partner set filter will be added.
     * @return {Object[]} The list of discount records with business partner set filter added.
     */
    addDiscountsByBusinessPartnerSetFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterBusinessPartnerSet,
        'cbpartnerSets'
      );
    },

    /**
     * Adds price list filter to every discount record inside a list.
     * @param {Object[]} discounts - The list of discount records where price list filter will be added.
     * @return {Object[]} The list of discount records with price list filter added.
     */
    addDiscountsByPriceListFilter: async function(discounts) {
      return OB.Discounts.Pos.addDiscountFilter(
        discounts,
        OB.App.MasterdataModels.DiscountFilterPriceList,
        'pricelists'
      );
    },

    /**
     * Filters a list of discount records applying a given filter.
     * @param {Object[]} discounts - The list of discount records where filter will be applied.
     * @param {string} filterIncludeName - The name of the property indicating if the filter is including or excluding.
     * @param {string} filterChildrenName - The name of the child filter property.
     * @param {string} filterIdName - The name of the property with the id that will be filtered.
     * @param {string} id - Id value used to filter.
     * @return {Object[]} The list of discounts matching the filter.
     */
    filterDiscountById: function(
      discounts,
      filterIncludeName,
      filterChildrenName,
      filterIdName,
      id
    ) {
      return discounts.filter(
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

    /**
     * Filters a list of discount records applying a given date.
     * @param {Object[]} discounts - The list of discount records where date filter will be applied.
     * @param {Object} date - Date to filter discounts.
     * @return {Object[]} The list of discounts matching the date filter.
     */
    filterDiscountsByDate: function(discounts, date) {
      return discounts.filter(
        discount =>
          new Date(discount.startingDate) <= date &&
          (!discount.endingDate || new Date(discount.endingDate) >= date)
      );
    },

    /**
     * Filters a list of discount records applying a given role.
     * @param {Object[]} discounts - The list of discount records where role filter will be applied.
     * @param {Object} date - Role id to filter discounts.
     * @return {Object[]} The list of discounts matching the role filter.
     */
    filterDiscountsByRole: function(discounts, roleId) {
      return OB.Discounts.Pos.filterDiscountById(
        discounts,
        'oBDISCIncludedRoles',
        'roles',
        'role',
        roleId
      );
    },

    /**
     * Filters a list of discount records applying a given price list.
     * @param {Object[]} discounts - The list of discount records where price list filter will be applied.
     * @param {Object} date - Price list id to filter discounts.
     * @return {Object[]} The list of discounts matching the price list filter.
     */
    filterDiscountsByPriceList: function(discounts, priceListId) {
      return OB.Discounts.Pos.filterDiscountById(
        discounts,
        'includePriceLists',
        'pricelists',
        'm_pricelist_id',
        priceListId
      );
    },

    /**
     * Reads discount masterdata models from database and creates different caches to use them:
     *   OB.Discounts.Pos.manualRuleImpls: array with manual discounts and promotions including children filters, filtered by current role and sorted by name.
     *   OB.Discounts.Pos.ruleImpls: array with not manual discounts and promotions including children filters, filtered by current role and sorted by priority and id (null priorities first).
     *   OB.Discounts.Pos.bpSets: array with business partner sets.
     * It also runs OBPOS_DiscountsCacheInitialization hook.
     * Discount masterdata models should be read from database only here. Wherever discount data is needed, any of these caches should be used.
     */
    initCache: async function(callback) {
      if (OB.Discounts.Pos.isCalculatingCache) {
        return callback();
      }
      OB.Discounts.Pos.isCalculatingCache = true;
      const execution = OB.UTIL.ProcessController.start(
        'discountCacheInitialization'
      );

      const data = await OB.Discounts.Pos.loadData();
      Object.assign(OB.Discounts.Pos, data);

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
    },

    /**
     * Reads the discount masterdata model information from database.
     * This information is used to initialize the discount caches.
     * @return {Object} The discount masterdata model information.
     * @see {@link OB.Discounts.Pos.initCache}
     */
    loadData: async function() {
      const data = {};
      const manualPromotions = OB.Discounts.Pos.getManualPromotions();

      // Manual discounts must be sorted by name
      const manualDiscounts = await OB.App.MasterdataModels.Discount.find(
        new OB.App.Class.Criteria()
          .criterion('discountType', manualPromotions, 'in')
          .orderBy('name', 'asc')
          .limit(10000)
          .build()
      );

      // No manual discounts must be sorted by priority (done in memory as null priority goes first) and id
      const noManualDiscounts = await OB.App.MasterdataModels.Discount.find(
        new OB.App.Class.Criteria()
          .criterion('discountType', manualPromotions, 'notIn')
          .orderBy('id', 'asc')
          .limit(10000)
          .build()
      );
      noManualDiscounts.sort((a, b) => a.priority - b.priority);

      let discounts = manualDiscounts.concat(noManualDiscounts);

      discounts = await OB.Discounts.Pos.addDiscountsByRoleFilter(discounts);

      discounts = await OB.Discounts.Pos.addDiscountsByProductFilter(discounts);

      discounts = await OB.Discounts.Pos.addDiscountsByProductCategoryFilter(
        discounts
      );

      discounts = await OB.Discounts.Pos.addDiscountsByCharacteristicFilter(
        discounts
      );

      discounts = await OB.Discounts.Pos.addDiscountsByBusinessPartnerFilter(
        discounts
      );

      discounts = await OB.Discounts.Pos.addDiscountsByBusinessPartnerGroupFilter(
        discounts
      );

      discounts = await OB.Discounts.Pos.addDiscountsByBusinessPartnerSetFilter(
        discounts
      );

      discounts = await OB.Discounts.Pos.addDiscountsByPriceListFilter(
        discounts
      );

      discounts = OB.Discounts.Pos.filterDiscountsByRole(
        discounts,
        OB.MobileApp.model.get('context').role.id
      );

      discounts.forEach(
        discount => (discount.discountPercentage = discount.discount)
      );

      data.manualRuleImpls = discounts.filter(discount =>
        manualPromotions.includes(discount.discountType)
      );

      data.ruleImpls = discounts.filter(
        discount => !manualPromotions.includes(discount.discountType)
      );

      //BPSets
      const bpSetLines = await OB.App.MasterdataModels.BPSetLine.find(
        new OB.App.Class.Criteria().limit(10000).build()
      );
      data.bpSets = OB.App.ArrayUtils.groupBy(bpSetLines, 'bpSet');

      return data;
    }
  };
})();
