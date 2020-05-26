/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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
    const sortByDate = (rule1, rule2) => {
      return new Date(rule2.validFromDate) - new Date(rule1.validFromDate);
    };
    const sortByDefault = (rule1, rule2) => {
      return rule2.default - rule1.default;
    };

    return rules
      .filter(
        rule =>
          checkValidFromDate(rule) && checkSummary(rule) && checkIsCashVAT(rule)
      )
      .sort(
        (rule1, rule2) =>
          sortByDate(rule1, rule2) || sortByDefault(rule1, rule2)
      );
  };

  OB.Taxes.filterRulesByTicketLine = (ticket, line, rules) => {
    const joinRuleAndZone = rule => {
      const updatedRule = rule.taxZones
        ? rule.taxZones.map(zone => ({ ...zone, ...rule }))
        : rule;

      updatedRule.zoneCountry = rule.zoneCountry || rule.country;
      updatedRule.zoneRegion = rule.zoneRegion || rule.region;
      updatedRule.zoneDestinationCountry =
        rule.zoneDestinationCountry || rule.destinationCountry;
      updatedRule.zoneDestinationRegion =
        rule.zoneDestinationRegion || rule.destinationRegion;

      return updatedRule;
    };
    const groupRuleAndZone = (rulesAndZones, rule) => {
      const updatedRule = { ...rule };
      delete updatedRule.taxRateId;
      delete updatedRule.zoneCountry;
      delete updatedRule.zoneRegion;
      delete updatedRule.zoneDestinationCountry;
      delete updatedRule.zoneDestinationRegion;
      delete updatedRule.taxZones;

      return rulesAndZones.find(
        ruleAndZone => ruleAndZone.id === updatedRule.id
      )
        ? rulesAndZones
        : [...rulesAndZones, updatedRule];
    };
    const checkCountry = rule => {
      return (
        equals(rule.destinationCountry, line.country || ticket.country) ||
        equals(rule.zoneDestinationCountry, line.country || ticket.country) ||
        (!rule.destinationCountry && !rule.zoneDestinationCountry)
      );
    };
    const checkRegion = rule => {
      return (
        equals(rule.destinationRegion, line.region || ticket.region) ||
        equals(rule.zoneDestinationRegion, line.region || ticket.region) ||
        (!rule.destinationRegion && !rule.zoneDestinationRegion)
      );
    };
    const checkTaxCategory = rule => {
      const isTaxExempt = line.taxExempt || ticket.businessPartner.taxExempt;
      return (
        (isTaxExempt
          ? equals(rule.taxExempt, isTaxExempt)
          : equals(
              rule.businessPartnerTaxCategory,
              ticket.businessPartner.taxCategory
            )) && equals(rule.taxCategory, line.product.taxCategory)
      );
    };
    const sortByCountryFrom = (rule1, rule2) => {
      const checkCountryFrom = rule => {
        return rule.country || rule.zoneCountry;
      };
      if (checkCountryFrom(rule1) && !checkCountryFrom(rule2)) {
        return -1;
      }
      if (!checkCountryFrom(rule1) && checkCountryFrom(rule2)) {
        return 1;
      }
      return 0;
    };
    const sortByCountryTo = (rule1, rule2) => {
      const checkCountryTo = rule => {
        return (
          equals(rule.destinationCountry, line.country || ticket.country) ||
          equals(rule.zoneDestinationCountry, line.country || ticket.country)
        );
      };
      if (checkCountryTo(rule1) && !checkCountryTo(rule2)) {
        return -1;
      }
      if (!checkCountryTo(rule1) && checkCountryTo(rule2)) {
        return 1;
      }
      return 0;
    };
    const sortByRegionFrom = (rule1, rule2) => {
      const checkRegionFrom = rule => {
        return rule.region || rule.zoneRegion;
      };
      if (checkRegionFrom(rule1) && !checkRegionFrom(rule2)) {
        return -1;
      }
      if (!checkRegionFrom(rule1) && checkRegionFrom(rule2)) {
        return 1;
      }
      return 0;
    };
    const sortByRegionTo = (rule1, rule2) => {
      const checkRegionTo = rule => {
        return (
          equals(rule.destinationRegion, line.region || ticket.region) ||
          equals(rule.zoneDestinationRegion, line.region || ticket.region)
        );
      };
      if (checkRegionTo(rule1) && !checkRegionTo(rule2)) {
        return -1;
      }
      if (!checkRegionTo(rule1) && checkRegionTo(rule2)) {
        return 1;
      }
      return 0;
    };
    const updateRuleLocation = rule => {
      const updatedRule = { ...rule };
      updatedRule.country = equals(rule.country, line.country || ticket.country)
        ? rule.country
        : rule.zoneCountry;
      updatedRule.region = equals(rule.region, line.region || ticket.region)
        ? rule.region
        : rule.zoneRegion;
      updatedRule.destinationCountry = equals(
        rule.destinationCountry,
        line.country || ticket.country
      )
        ? rule.destinationCountry
        : rule.zoneDestinationCountry;
      updatedRule.destinationRegion = equals(
        rule.destinationRegion,
        line.region || ticket.region
      )
        ? rule.destinationRegion
        : rule.zoneDestinationRegion;
      return updatedRule;
    };
    const checkLocationAndDate = (rule, rulesFilteredByLine) => {
      return (
        equals(
          rule.destinationCountry,
          rulesFilteredByLine[0].destinationCountry
        ) &&
        equals(
          rule.destinationRegion,
          rulesFilteredByLine[0].destinationRegion
        ) &&
        equals(rule.country, rulesFilteredByLine[0].country) &&
        equals(rule.region, rulesFilteredByLine[0].region) &&
        equals(rule.validFromDate, rulesFilteredByLine[0].validFromDate)
      );
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
      .filter(rule => checkTaxCategory(rule))
      .flatMap(rule => joinRuleAndZone(rule))
      .filter(rule => checkCountry(rule) && checkRegion(rule))
      .sort(
        (rule1, rule2) =>
          sortByRegionTo(rule1, rule2) ||
          sortByRegionFrom(rule1, rule2) ||
          sortByCountryTo(rule1, rule2) ||
          sortByCountryFrom(rule1, rule2)
      )
      .map(rule => updateRuleLocation(rule))
      .filter((rule, index, rulesFilteredByLine) =>
        checkLocationAndDate(rule, rulesFilteredByLine)
      )
      .reduce(
        (rulesAndZones, rule) => groupRuleAndZone(rulesAndZones, rule),
        []
      )
      .sort(
        (rule1, rule2) =>
          sortByLineno(rule1, rule2) ||
          sortByCascade(rule1, rule2) ||
          sortByTaxBase(rule1, rule2)
      );
  };
})();
