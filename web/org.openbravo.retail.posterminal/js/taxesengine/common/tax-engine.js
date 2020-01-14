/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  if (window) {
    // For browser
    window.OB = window.OB || {};
  } else {
    // For graal/webservice
    OB = OB || {};
  }

  OB.Taxes = OB.Taxes || {};

  OB.Taxes.calculateTaxes = function(ticket, rules) {
    const taxRateArray = findTaxes(ticket, rules);
    const rule = ticket.priceIncludesTax
      ? new OB.Taxes.PriceIncludingTax(ticket, taxRateArray)
      : new OB.Taxes.PriceExcludingTax(ticket, taxRateArray);
    return rule.calculateTaxes();
  };

  const findTaxes = function(ticket, rules) {
    const taxRateArray = rules
      .filter(
        rule =>
          checkValidFromDate(ticket, rule) &&
          checkIsCashVAT(ticket, rule) &&
          checkCountry(ticket, rule) &&
          checkRegion(ticket, rule)
      )
      .sort(
        (rule1, rule2) =>
          sortByRegionTo(ticket, rule1, rule2) ||
          sortByRegionFrom(rule1, rule2) ||
          sortByCountryTo(ticket, rule1, rule2) ||
          sortByCountryFrom(rule1, rule2) ||
          sortByDate(rule1, rule2) ||
          sortByDefault(rule1, rule2)
      );

    taxRateArray.forEach(rule => {
      rule.country =
        rule.country === ticket.country ? rule.country : rule.zoneCountry;
      rule.region =
        rule.region === ticket.region ? rule.region : rule.zoneRegion;
      rule.destinationCountry =
        rule.destinationCountry === ticket.businessPartner.country
          ? rule.destinationCountry
          : rule.zoneDestinationCountry;
      rule.destinationRegion =
        rule.destinationRegion === ticket.businessPartner.region
          ? rule.destinationRegion
          : rule.zoneDestinationRegion;
    });

    return taxRateArray;
  };

  const checkValidFromDate = (ticket, rule) => {
    return new Date(rule.validFromDate) <= new Date(ticket.date);
  };
  const checkIsCashVAT = (ticket, rule) => {
    return (
      rule.isCashVAT === ticket.isCashVat ||
      (!rule.isCashVAT && (rule.withholdingTax || rule.rate === 0))
    );
  };
  const checkCountry = (ticket, rule) => {
    const country = ticket.businessPartner.country;
    return (
      rule.destinationCountry === country ||
      rule.zoneDestinationCountry === country ||
      (!rule.destinationCountry && !rule.zoneDestinationCountry)
    );
  };
  const checkRegion = (ticket, rule) => {
    const region = ticket.businessPartner.region;
    return (
      rule.destinationRegion === region ||
      rule.zoneDestinationRegion === region ||
      (!rule.destinationRegion && !rule.zoneDestinationRegion)
    );
  };
  const sortByCountryFrom = (rule1, rule2) => {
    const checkCountryFrom = rule => {
      return rule.country || rule.zoneCountry;
    };
    return checkCountryFrom(rule1) && !checkCountryFrom(rule2)
      ? -1
      : !checkCountryFrom(rule1) && checkCountryFrom(rule2)
      ? 1
      : 0;
  };
  const sortByCountryTo = (ticket, rule1, rule2) => {
    const checkCountryTo = (ticket, rule) => {
      const country = ticket.businessPartner.country;
      return (
        rule.destinationCountry === country ||
        rule.zoneDestinationCountry === country
      );
    };
    return checkCountryTo(ticket, rule1) && !checkCountryTo(ticket, rule2)
      ? -1
      : !checkCountryTo(ticket, rule1) && checkCountryTo(ticket, rule2)
      ? 1
      : 0;
  };
  const sortByRegionFrom = (rule1, rule2) => {
    const checkRegionFrom = rule => {
      return rule.region || rule.zoneRegion;
    };
    return checkRegionFrom(rule1) && !checkRegionFrom(rule2)
      ? -1
      : !checkRegionFrom(rule1) && checkRegionFrom(rule2)
      ? 1
      : 0;
  };
  const sortByRegionTo = (ticket, rule1, rule2) => {
    const checkRegionTo = (ticket, rule) => {
      const region = ticket.businessPartner.region;
      return (
        rule.destinationRegion === region ||
        rule.zoneDstinationRegion === region
      );
    };
    return checkRegionTo(ticket, rule1) && !checkRegionTo(ticket, rule2)
      ? -1
      : !checkRegionTo(ticket, rule1) && checkRegionTo(ticket, rule2)
      ? 1
      : 0;
  };
  const sortByDate = (rule1, rule2) => {
    return new Date(rule2.validFromDate) - new Date(rule1.validFromDate);
  };
  const sortByDefault = (rule1, rule2) => {
    return rule2.default - rule1.default;
  };
})();
