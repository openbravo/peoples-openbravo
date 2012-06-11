/*global define */

define(['utilities'], function () {

  // Mockup for OB.I18N

  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};

  OB.I18N.formatCurrency = function (num) {
    // Hardcoded to US Locale.
    return OB.I18N.formatGeneralNumber(num, {
      decimals: 2,
      decimal: '.',
      group: ',',
      currency: '$#'});
  };

  OB.I18N.formatRate = function (num) {
    // Hardcoded to US Locale.
    return OB.I18N.formatGeneralNumber(num * 100, {
      decimals: 2,
      decimal: '.',
      group: ',',
      currency: '#%'});
  };

  OB.I18N.formatGeneralNumber = function (num, options) {
    var n = num.toFixed(options.decimals);
    var x = n.split('.');
    var x1 = x[0];
    var x2 = x.length > 1 ? options.decimal + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
      x1 = x1.replace(rgx, '$1' + options.group + '$2');
    }
    if (options.currency) {
      return options.currency.replace("#", x1 + x2);
    } else {
      return x1 + x2;
    }
  };

  OB.I18N.formatDate = function (d) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    return OB.UTIL.padNumber(curr_date, 2) + '/' + OB.UTIL.padNumber(curr_month + 1, 2) + '/' + curr_year;
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
});