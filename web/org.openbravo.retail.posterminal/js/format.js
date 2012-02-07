(function (OBPOS) {
  
  var Format = {};
  
  Format.formatNumber = function (num, options) {
    n = num.toFixed(options.decimals);
    x = n.split('.');
    x1 = x[0];
    x2 = x.length > 1 ? options.decimal + x[1] : '';
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
  

  
  OBPOS.Format = Format;
  
}(window.OBPOS));