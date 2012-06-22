/*global B */

(function () {

  // Mockup for OB.I18N

  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};

  OB.I18N.formatCurrency = function (number) {
    var symbol = OB.POS.modelterminal.get('currency').symbol,
        isSymbolRight = OB.POS.modelterminal.get('currency').currencySymbolAtTheRight,
        maskNumeric = OB.Format.formats.priceRelation,
        decSeparator = OB.Format.defaultDecimalSymbol,
        groupSeparator = OB.Format.defaultGroupingSymbol,
        groupInterval = OB.Format.defaultGroupingSize;

    maskNumeric = maskNumeric.replace(',', 'dummy').replace('.', decSeparator).replace('dummy', groupSeparator);

    var formattedNumber = OB.Utilities.Number.JSToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    if (isSymbolRight) {
      formattedNumber = formattedNumber + symbol;
    } else {
      formattedNumber = symbol + formattedNumber;
    }
    return formattedNumber;
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
}());