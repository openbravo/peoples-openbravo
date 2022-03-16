/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  if (window) {
    // For browser
    window.OB = window.OB || {};
  } else {
    // For graal/webservice
    OB = OB || {};
  }

  const equals = (value1, value2) => {
    if (!value1 && !value2) {
      return true;
    }
    return value1 === value2;
  };

  OB.Taxes = OB.Taxes || {};

  OB.Taxes.applyTaxes = (ticket, rules) => {
    const rulesFilteredByTicket = OB.Taxes.filterRulesByTicket(ticket, rules);
    const rule = ticket.priceIncludesTax
      ? new OB.Taxes.PriceIncludingTax(ticket, rulesFilteredByTicket)
      : new OB.Taxes.PriceExcludingTax(ticket, rulesFilteredByTicket);
    return rule.applyTaxes();
  };

  OB.Taxes.filterRulesByTicket = (ticket, rules) => {
    const ticketLinesSet = ticket.lines.reduce(
      (obj, line) => ({
        taxCategory: new Set([
          ...obj.taxCategory,
          ...new Set(
            [line.product.taxCategory].concat(
              (line.product.productBOM || []).map(
                bomLine => bomLine.product.taxCategory
              )
            )
          )
        ]),
        country: obj.country.add(line.country),
        destinationCountry: obj.destinationCountry.add(line.destinationCountry),
        region: obj.region.add(line.region),
        destinationRegion: obj.destinationRegion.add(line.destinationRegion)
      }),
      {
        taxCategory: new Set(),
        country: new Set(ticket.country),
        destinationCountry: new Set(ticket.destinationCountry),
        region: new Set(ticket.region),
        destinationRegion: new Set(ticket.destinationRegion)
      }
    );
    const checkTaxCategory = rule => {
      return (
        (ticket.businessPartner.taxExempt
          ? equals(rule.taxExempt, true)
          : true) &&
        equals(
          rule.businessPartnerTaxCategory,
          ticket.businessPartner.taxCategory
        ) &&
        ticketLinesSet.taxCategory.has(rule.taxCategory)
      );
    };
    const checkValidFromDate = rule => {
      return new Date(rule.validFromDate) <= new Date(ticket.orderDate);
    };
    const checkSummary = rule => {
      return !rule.summaryLevel || rule.isBom;
    };
    const checkIsCashVAT = rule => {
      return (
        equals(rule.isCashVAT, ticket.cashVAT) ||
        (!rule.isCashVAT && (rule.withholdingTax || equals(rule.rate, 0)))
      );
    };
    const checkandUpdateLocation = rule => {
      const matchLocation = (
        ruleOrZone,
        ruleOrZoneLocationProperty,
        lineLocationProperty
      ) => {
        // Rules apply if they match for each location property or if location property is empty
        const ruleLocation = ruleOrZone[ruleOrZoneLocationProperty];
        const lineLocation = ticketLinesSet[lineLocationProperty];
        return !ruleLocation || lineLocation.has(ruleLocation);
      };

      // If rule matches any line country and region we keep the rule as it is
      if (
        matchLocation(rule, 'country', 'country') &&
        matchLocation(rule, 'destinationCountry', 'destinationCountry') &&
        matchLocation(rule, 'region', 'region') &&
        matchLocation(rule, 'destinationRegion', 'destinationRegion')
      ) {
        return rule;
      }

      // If rule includes a zone that matches any line country and region we keep the zone
      const taxZones = (rule.taxZones || []).filter(
        zone =>
          matchLocation(zone, 'zoneCountry', 'country') &&
          matchLocation(zone, 'zoneDestinationCountry', 'destinationCountry') &&
          matchLocation(zone, 'zoneRegion', 'region') &&
          matchLocation(zone, 'zoneDestinationRegion', 'destinationRegion')
      );
      if (taxZones.length) {
        return {
          ...rule,
          taxZones
        };
      }

      // If no match we remove the rule
      return [];
    };
    const sortByDate = (rule1, rule2) => {
      return new Date(rule2.validFromDate) - new Date(rule1.validFromDate);
    };
    const sortByDefault = (rule1, rule2) => {
      return rule2.default - rule1.default;
    };
    const sortByLineno = (rule1, rule2) => {
      return rule1.lineNo - rule2.lineNo;
    };
    const sortByCascade = (rule1, rule2) => {
      return rule1.cascade - rule2.cascade;
    };
    const sortByTaxBase = (rule1, rule2) => {
      const checkTaxBase = rule => {
        return rule.taxBase;
      };
      if (!checkTaxBase(rule1) && checkTaxBase(rule2)) {
        return -1;
      }
      if (checkTaxBase(rule1) && !checkTaxBase(rule2)) {
        return 1;
      }
      return 0;
    };

    return rules
      .filter(
        rule =>
          checkTaxCategory(rule) &&
          checkValidFromDate(rule) &&
          checkSummary(rule) &&
          checkIsCashVAT(rule)
      )
      .flatMap(rule => checkandUpdateLocation(rule))
      .sort(
        (rule1, rule2) =>
          sortByDate(rule1, rule2) ||
          sortByDefault(rule1, rule2) ||
          sortByLineno(rule1, rule2) ||
          sortByCascade(rule1, rule2) ||
          sortByTaxBase(rule1, rule2)
      );
  };

  OB.Taxes.filterRulesByTicketLine = (ticket, line, rules) => {
    const checkTaxCategory = rule => {
      return (
        (line.taxExempt ? equals(rule.taxExempt, true) : true) &&
        equals(rule.taxCategory, line.product.taxCategory)
      );
    };
    const checkValidFromDate = rule => {
      return (
        new Date(rule.validFromDate) <=
        (line.originalOrderDate ? new Date(line.originalOrderDate) : new Date())
      );
    };
    const checkandUpdateLocation = rule => {
      const matchLocation = (
        ruleOrZone,
        ruleOrZoneLocationProperty,
        lineLocationProperty
      ) => {
        // Rules apply if they match for each location property or if location property is empty
        const ruleLocation = ruleOrZone[ruleOrZoneLocationProperty];
        const lineLocation =
          line[lineLocationProperty] || ticket[lineLocationProperty];
        return !ruleLocation || equals(ruleLocation, lineLocation);
      };

      // If rule matches line country and region we keep the rule as it is
      if (
        matchLocation(rule, 'country', 'country') &&
        matchLocation(rule, 'destinationCountry', 'destinationCountry') &&
        matchLocation(rule, 'region', 'region') &&
        matchLocation(rule, 'destinationRegion', 'destinationRegion')
      ) {
        return { ...rule, taxZones: undefined };
      }

      // If rule includes a zone that matches line country and region we update rule with zone information
      const taxZone = (rule.taxZones || []).find(
        zone =>
          matchLocation(zone, 'zoneCountry', 'country') &&
          matchLocation(zone, 'zoneDestinationCountry', 'destinationCountry') &&
          matchLocation(zone, 'zoneRegion', 'region') &&
          matchLocation(zone, 'zoneDestinationRegion', 'destinationRegion')
      );
      if (taxZone) {
        return {
          ...rule,
          country: taxZone.zoneCountry,
          destinationCountry: taxZone.zoneDestinationCountry,
          region: taxZone.zoneRegion,
          destinationRegion: taxZone.zoneDestinationRegion,
          taxZones: undefined
        };
      }

      // If no match we remove the rule
      return [];
    };
    const sortByLocation = (rule1, rule2) => {
      const sortLocation = ruleLocationProperty => {
        if (rule1[ruleLocationProperty] && !rule2[ruleLocationProperty]) {
          return -1;
        }
        if (!rule1[ruleLocationProperty] && rule2[ruleLocationProperty]) {
          return 1;
        }
        return 0;
      };

      // Rules with defined country and region have more priority than rules with empty country and region
      return (
        sortLocation('destinationRegion') ||
        sortLocation('region') ||
        sortLocation('destinationCountry') ||
        sortLocation('country')
      );
    };
    const checkDateAndLocationWithFirstMatchingRule = (
      rule,
      rulesFilteredByLine
    ) => {
      // We return every rule with the same match for date property and for each location property
      const firstMatchingRule = rulesFilteredByLine[0];
      return (
        equals(rule.validFromDate, firstMatchingRule.validFromDate) &&
        equals(rule.country, firstMatchingRule.country) &&
        equals(rule.destinationCountry, firstMatchingRule.destinationCountry) &&
        equals(rule.region, firstMatchingRule.region) &&
        equals(rule.destinationRegion, firstMatchingRule.destinationRegion)
      );
    };

    return rules
      .filter(rule => checkTaxCategory(rule) && checkValidFromDate(rule))
      .flatMap(rule => checkandUpdateLocation(rule))
      .sort((rule1, rule2) => sortByLocation(rule1, rule2))
      .filter((rule, index, rulesFilteredByLine) =>
        checkDateAndLocationWithFirstMatchingRule(rule, rulesFilteredByLine)
      );
  };
})();
