(function (OBPOS) {

  var Sales = {};
  
  function isScrolledIntoView(container, elem) {

    var docViewTop = container.scrollTop();
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom) && (elemBottom <= docViewBottom) && (elemTop >= docViewTop));
  }

  Sales.makeElemVisible = function (container, elem) {

    var docViewTop = container.offset().top;
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    var currentScroll = container.scrollTop();

    if (elemTop < docViewTop) {
      container.scrollTop(currentScroll - docViewTop + elemTop);
    } else if (elemBottom > docViewBottom) {
      container.scrollTop(currentScroll + elemBottom - docViewBottom);
    }
  }

  // public
  Sales.getThumbnail = function (base64, width, height, contentType) {
    var url = (base64) ? 'data:' + (contentType ? contentType : 'image/png') + ';base64,' + base64 : 'img/box.png';
    return $('<div/>').css('margin', 'auto').css('height', height ? height : '48').css('width', width ? width : '48').css('background', 'url(' + url + ') center center no-repeat').css('background-size', 'contain');
  };
  
  
  OBPOS.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + n + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, " ")) : "";
  }

  // Public Sales
  OBPOS.Sales = Sales;

}(window.OBPOS));