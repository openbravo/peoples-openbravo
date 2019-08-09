/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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
        this.getApplicableDiscounts(ticket)
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
          result.lines[line.get('id')].discounts;
        if (!discountInfoForLine) {
          //No discounts for this line, we clear existing discounts if they exist, and move to the next
          line.set('promotions', []);
          return;
        }

        line.set('promotions', discountInfoForLine.promotions);
        return;
        // let rule = OB.Discounts.Pos.ruleImpls.find(r => r.id === discount.id);
        // if (!rule) {
        //   OB.error('Rule not found!', discount);
        //   return;
        // }
        // let ruleModel = rule.ruleModel;
        // try {
        //   ticket.addPromotion(line, ruleModel, {
        //     amt: discount.discount
        //   });
        // } catch (e) {
        //   OB.error(e);
        // }
      });
    },

    translateTicket: function(receipt) {
      let newTicket = {};
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner._identifier = receipt.get('bp')._identifier;
      newTicket.id = receipt.get('id');
      newTicket.date = receipt.get('orderDate');
      newTicket.lines = [];
      receipt.get('lines').forEach(line => {
        let newLine = {};
        newLine.id = line.get('id');
        newLine.product = {};
        newLine.product.id = line.get('product').id;
        newLine.product._identifier = line.get('product')._identifier;
        newLine.qty = line.get('qty');
        newLine.price = line.get('price');
        newTicket.lines.push(newLine);
      });
      return newTicket;
    },

    calculateDiscounts(receipt, callback) {
      const ticketForEngine = OB.Discounts.Pos.translateTicket(receipt);
      let result;
      if (OB.Discounts.Pos.local) {
        result = OB.Discounts.Pos.calculateLocal(ticketForEngine);
        OB.Discounts.Pos.applyDiscounts(receipt, result);
        callback();
      } else {
        result = OB.Discounts.Pos.calculateRemote(receipt, callback);
      }
    },

    computeDiscountsQuery(basicParams) {
      let date =
        OB.Utilities.Date.JSToOB(new Date(), 'yyyy-MM-dd') + ' 00:00:00.000';
      let params = [
        date,
        basicParams.businessPartner,
        basicParams.businessPartner,
        basicParams.businessPartner,
        basicParams.businessPartner,
        basicParams.businessPartner,
        basicParams.businessPartner,
        OB.MobileApp.model.get('pricelist').id,
        OB.MobileApp.model.get('pricelist').id,
        OB.MobileApp.model.get('context').role.id,
        OB.MobileApp.model.get('context').role.id
      ];
      let discountsQuery =
        'SELECT * FROM M_OFFER WHERE ( ' +
        //Date Filter
        "date(?) BETWEEN DATEFROM AND COALESCE(date(DATETO), date('9999-12-31'))" +
        //BusinessPartner, BPCategory, BPSet filter
        " AND((BPARTNER_SELECTION = 'Y'" +
        ' AND NOT EXISTS' +
        ' (SELECT 1' + //
        ' FROM M_OFFER_BPARTNER' + //
        ' WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        '   AND C_BPARTNER_ID = ?' +
        ' ))' + //
        " OR(BPARTNER_SELECTION = 'N'" + //
        ' AND EXISTS' + //
        ' (SELECT 1' + //
        ' FROM M_OFFER_BPARTNER' + //
        ' WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        '   AND C_BPARTNER_ID = ?' + //
        ' )))' + //
        " AND((BP_SET_SELECTION = 'Y'" + //
        ' AND NOT EXISTS' + //
        ' (SELECT 1' + //
        ' FROM M_OFFER_BP_SET OBPS, C_BP_SET_LINE BPL' + //
        ' WHERE OBPS.C_BP_SET_ID = BPL.C_BP_SET_ID' + //
        '   AND OBPS.M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        "   AND BPL.C_BPARTNER_ID = ? AND datetime('now') BETWEEN COALESCE(datetime(BPL.STARTDATE), datetime('2000-12-31T00:00:00')) AND COALESCE(datetime(BPL.ENDDATE), datetime('9999-12-31T23:59:59'))" + //
        ' ))' + //
        " OR(BP_SET_SELECTION = 'N'" + //
        ' AND EXISTS' + //
        ' (SELECT 1' + //
        ' FROM M_OFFER_BP_SET OBPS, C_BP_SET_LINE BPL' + //
        ' WHERE OBPS.C_BP_SET_ID = BPL.C_BP_SET_ID' + //
        '   AND OBPS.M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        "   AND BPL.C_BPARTNER_ID = ? AND datetime('now') BETWEEN COALESCE(datetime(BPL.STARTDATE), datetime('2000-12-31T00:00:00')) AND COALESCE(datetime(BPL.ENDDATE), datetime('9999-12-31T23:59:59'))" + //
        ' )))' + //
        " AND((BP_GROUP_SELECTION = 'Y'" + //
        ' AND NOT EXISTS' + //
        ' (SELECT 1' + //
        ' FROM C_BPARTNER B,' + //
        '   M_OFFER_BP_GROUP OB' + //
        ' WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        '   AND B.C_BPARTNER_ID = ?' + //
        '   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID' + //
        ' ))' + //
        " OR(BP_GROUP_SELECTION = 'N'" + //
        ' AND EXISTS' + //
        ' (SELECT 1' + //
        ' FROM C_BPARTNER B,' + //
        '   M_OFFER_BP_GROUP OB' + //
        ' WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID' + //
        '   AND B.C_BPARTNER_ID = ?' + //
        '   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID' + //
        ' )))' + //
        //Pricelist filter
        " AND ((pricelist_selection = 'Y' AND NOT EXISTS" + //
        '   (SELECT 1 FROM m_offer_pricelist opl WHERE m_offer.m_offer_id = opl.m_offer_id AND opl.m_pricelist_id = ? ))' + //
        " OR (pricelist_selection = 'N' AND EXISTS" + //
        '   (SELECT 1 FROM m_offer_pricelist opl WHERE m_offer.m_offer_id = opl.m_offer_id AND opl.m_pricelist_id = ? )))' +
        //Do not include manual promotions
        ' AND M_OFFER_TYPE_ID NOT IN (' +
        OB.Model.Discounts.getManualPromotions() +
        ')' + //
        //Role filter
        " AND ((EM_OBDISC_ROLE_SELECTION = 'Y' AND NOT EXISTS (SELECT 1 FROM OBDISC_OFFER_ROLE WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID " +
        " AND AD_ROLE_ID = ?)) OR (EM_OBDISC_ROLE_SELECTION = 'N' " + //
        ' AND EXISTS (SELECT 1 FROM OBDISC_OFFER_ROLE WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID ' +
        ' AND AD_ROLE_ID = ?)))' + //
        ') OR M_OFFER_TYPE_ID IN (' +
        OB.Model.Discounts.getAutoCalculatedPromotions() +
        ')';
      const discountsObj = { query: discountsQuery, params: params };
      return discountsObj;
    },

    translateRule: function(rule) {
      rule.set('discountPercentage', rule.get('discount'));
    },

    initCache: function(basicParams, callback) {
      OB.info('[Discounts cache] Starting load...');
      const initialTime = new Date().getTime();
      OB.Discounts.Pos.ruleImpls = [];
      let discountsQueryObject = OB.Discounts.Pos.computeDiscountsQuery(
        basicParams
      );
      OB.Dal.query(
        OB.Model.Discount,
        discountsQueryObject.query,
        discountsQueryObject.params,
        rules => {
          const finishCallback = _.after(rules.length, () => {
            OB.info(
              '[Discounts cache] ...load finished. Elapsed time: ' +
                (new Date().getTime() - initialTime) +
                'ms.'
            );
            callback();
          });

          rules.forEach(rule => OB.Discounts.Pos.translateRule(rule));
          rules.forEach(rule => {
            var r = JSON.parse(JSON.stringify(rule)),
              ruleFilter = { priceAdjustment: rule.get('id') };

            OB.Dal.find(
              OB.Model.DiscountFilterProduct,
              ruleFilter,
              products => {
                r.products = JSON.parse(JSON.stringify(products));
                r.products.forEach(
                  offerProduct =>
                    (offerProduct.product = { id: offerProduct.product })
                );
                OB.Dal.find(
                  OB.Model.DiscountFilterProductCategory,
                  ruleFilter,
                  productCategories => {
                    r.productCategories = JSON.parse(
                      JSON.stringify(productCategories)
                    );
                    OB.Dal.find(
                      OB.Model.DiscountFilterBusinessPartner,
                      ruleFilter,
                      bps => {
                        r.businessPartners = JSON.parse(JSON.stringify(bps));
                        OB.Dal.find(
                          OB.Model.DiscountFilterBusinessPartnerGroup,
                          ruleFilter,
                          bpCategories => {
                            r.businessPartnerCategories = JSON.parse(
                              JSON.stringify(bpCategories)
                            );
                            r.ruleModel = rule;
                            OB.Discounts.Pos.ruleImpls.push(r);
                            finishCallback();
                          }
                        );
                      }
                    );
                  }
                );
              }
            );
          });
        }
      );
    },

    getApplicableDiscounts: function(ticket) {
      return OB.Discounts.Pos.ruleImpls
        .filter(rule => this.canApplyRuleToTicket(ticket, rule))
        .filter(rule => this.canApplyRuleToAtLeastOneLine(ticket, rule));
    },

    // This part is calculated only in POS, backend implements it in Java
    canApplyRuleToTicket(ticket, rule) {
      let elementFound = rule.businessPartners.find(
        bp => bp.businessPartner === ticket.bp.id
      );
      let onlyIncluded = rule.includedBusinessPartners === 'N';
      let applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      if (!applicable) {
        return false;
      }

      elementFound = rule.businessPartnerCategories.find(
        bpc => bpc.businessPartnerCategory === ticket.bp.businessPartnerCategory
      );
      onlyIncluded = rule.includedBPCategories === 'N';
      applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      return applicable;
    },

    canApplyRuleToAtLeastOneLine(ticket, rule) {
      return ticket.lines.find(line =>
        OB.Discounts.Discount.isApplicableToLine(line, rule)
      );
    }
  };
})();
