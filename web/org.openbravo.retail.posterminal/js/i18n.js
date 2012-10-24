/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B */

(function () {

  // Mockup for OB.I18N
  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};

  OB.I18N.formatCurrency = function (number) {
    var maskNumeric = OB.Format.formats.priceRelation,
        decSeparator = OB.Format.defaultDecimalSymbol,
        groupSeparator = OB.Format.defaultGroupingSymbol,
        groupInterval = OB.Format.defaultGroupingSize;

    maskNumeric = maskNumeric.replace(',', 'dummy').replace('.', decSeparator).replace('dummy', groupSeparator);

    return OB.Utilities.Number.JSToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
  };

  OB.I18N.formatCoins = function (number) {
    var val = OB.I18N.formatCurrency(number);
    var decSeparator = OB.Format.defaultDecimalSymbol;
    return val.replace(new RegExp('[' + decSeparator + '][0]+$'), '');
  };

  OB.I18N.formatRate = function (number) {
    var symbol = '%',
        maskNumeric = OB.Format.formats.euroEdition,
        decSeparator = OB.Format.defaultDecimalSymbol,
        groupSeparator = OB.Format.defaultGroupingSymbol,
        groupInterval = OB.Format.defaultGroupingSize;

    maskNumeric = maskNumeric.replace(',', 'dummy').replace('.', decSeparator).replace('dummy', groupSeparator);

    var formattedNumber = OB.Utilities.Number.JSToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    formattedNumber = formattedNumber + symbol;
    return formattedNumber;
  };

  OB.I18N.formatDate = function (JSDate) {
    var dateFormat = OB.Format.date;
    return OB.Utilities.Date.JSToOB(JSDate, dateFormat);
  };

  OB.I18N.formatHour = function (d) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    return OB.UTIL.padNumber(curr_hour, 2) + ':' + OB.UTIL.padNumber(curr_min, 2);
  };


  OB.I18N.parseNumber = function (s) {
    if (OB.Format.defaultDecimalSymbol !== '.') {
      s = s.toString();
      while (s.indexOf(OB.Format.defaultDecimalSymbol) !== -1) {
        s = s.replace(OB.Format.defaultDecimalSymbol, '.');
      }
    }
    return parseFloat(s, 10);
  };

}());