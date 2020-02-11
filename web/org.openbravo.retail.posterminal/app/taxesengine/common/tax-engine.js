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
      return new Date(rule.validFromDate) <= new Date(ticket.date);
    };
    const checkIsCashVAT = rule => {
      return (
        OB.Taxes.equals(rule.isCashVAT, ticket.isCashVat) ||
        (!rule.isCashVAT &&
          (rule.withholdingTax || OB.Taxes.equals(rule.rate, 0)))
      );
    };
    const checkCountry = rule => {
      const businessPartnerCountry = ticket.businessPartner.address.country;
      return (
        OB.Taxes.equals(rule.destinationCountry, businessPartnerCountry) ||
        OB.Taxes.equals(rule.zoneDestinationCountry, businessPartnerCountry) ||
        (!rule.destinationCountry && !rule.zoneDestinationCountry)
      );
    };
    const checkRegion = rule => {
      const businessPartnerRegion = ticket.businessPartner.address.region;
      return (
        OB.Taxes.equals(rule.destinationRegion, businessPartnerRegion) ||
        OB.Taxes.equals(rule.zoneDestinationRegion, businessPartnerRegion) ||
        (!rule.destinationRegion && !rule.zoneDestinationRegion)
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
        const businessPartnerCountry = ticket.businessPartner.address.country;
        return (
          OB.Taxes.equals(rule.destinationCountry, businessPartnerCountry) ||
          OB.Taxes.equals(rule.zoneDestinationCountry, businessPartnerCountry)
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
        const businessPartnerRegion = ticket.businessPartner.address.region;
        return (
          OB.Taxes.equals(rule.destinationRegion, businessPartnerRegion) ||
          OB.Taxes.equals(rule.zoneDestinationRegion, businessPartnerRegion)
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
    const sortByDate = (rule1, rule2) => {
      return new Date(rule2.validFromDate) - new Date(rule1.validFromDate);
    };
    const sortByDefault = (rule1, rule2) => {
      return rule2.default - rule1.default;
    };

    return rules
      .filter(
        rule =>
          checkValidFromDate(rule) &&
          checkIsCashVAT(rule) &&
          checkCountry(rule) &&
          checkRegion(rule)
      )
      .sort(
        (rule1, rule2) =>
          sortByRegionTo(rule1, rule2) ||
          sortByRegionFrom(rule1, rule2) ||
          sortByCountryTo(rule1, rule2) ||
          sortByCountryFrom(rule1, rule2) ||
          sortByDate(rule1, rule2) ||
          sortByDefault(rule1, rule2)
      )
      .map(rule => {
        const updatedRule = rule;
        updatedRule.country = OB.Taxes.equals(rule.country, ticket.country)
          ? rule.country
          : rule.zoneCountry;
        updatedRule.region = OB.Taxes.equals(rule.region, ticket.region)
          ? rule.region
          : rule.zoneRegion;
        updatedRule.destinationCountry = OB.Taxes.equals(
          rule.destinationCountry,
          ticket.businessPartner.address.country
        )
          ? rule.destinationCountry
          : rule.zoneDestinationCountry;
        updatedRule.destinationRegion = OB.Taxes.equals(
          rule.destinationRegion,
          ticket.businessPartner.address.region
        )
          ? rule.destinationRegion
          : rule.zoneDestinationRegion;
        return updatedRule;
      });
  };

  OB.Taxes.filterRulesByTicketLine = (ticket, line, rules) => {
    const checkTaxCategory = rule => {
      const isTaxExempt = line.taxExempt || ticket.businessPartner.taxExempt;
      return (
        (isTaxExempt
          ? OB.Taxes.equals(rule.taxExempt, isTaxExempt)
          : OB.Taxes.equals(
              rule.businessPartnerTaxCategory,
              ticket.businessPartner.taxCategory
            )) && OB.Taxes.equals(rule.taxCategory, line.product.taxCategory)
      );
    };
    const checkLocation = (rule, rulesFilteredByLine) => {
      return (
        OB.Taxes.equals(
          rule.destinationCountry,
          rulesFilteredByLine[0].destinationCountry
        ) &&
        OB.Taxes.equals(
          rule.destinationRegion,
          rulesFilteredByLine[0].destinationRegion
        ) &&
        OB.Taxes.equals(rule.country, rulesFilteredByLine[0].country) &&
        OB.Taxes.equals(rule.region, rulesFilteredByLine[0].region) &&
        OB.Taxes.equals(
          rule.validFromDate,
          rulesFilteredByLine[0].validFromDate
        )
      );
    };
    const sortByLineno = (rule1, rule2) => {
      return rule1.lineNo - rule2.lineNo;
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
      .filter((rule, index, rulesFilteredByLine) =>
        checkLocation(rule, rulesFilteredByLine)
      )
      .sort(
        (rule1, rule2) =>
          sortByLineno(rule1, rule2) || sortByTaxBase(rule1, rule2)
      );
  };

  OB.Taxes.equals = (value1, value2) => {
    if (!value1 && !value2) {
      return true;
    }
    return value1 === value2;
  };
})();
