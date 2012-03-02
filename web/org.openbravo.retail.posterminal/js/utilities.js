define([], function () {
  
  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};
  
  function isScrolledIntoView(container, elem) {

    var docViewTop = container.scrollTop();
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom) && (elemBottom <= docViewBottom) && (elemTop >= docViewTop));
  }

  OB.UTIL.makeElemVisible = function (container, elem) {

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
  OB.UTIL.getThumbnail = function (base64, width, height, contentType) {
    var url = (base64) ? 'data:' + (contentType ? contentType : 'image/png') + ';base64,' + base64 : 'img/box.png';
    return $('<div/>').css('margin', 'auto').css('height', height ? height : '48').css('width', width ? width : '48').css('background', 'url(' + url + ') center center no-repeat').css('background-size', 'contain');
  };
  
  
  OB.UTIL.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + n + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, " ")) : "";
  }
  
  OB.UTIL.formatNumber = function (num, options) {
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
  
  OB.UTIL.DOM = function (dom) {
    if (typeof (dom) === "string") { // Is an String
      return document.createTextNode(dom);
    } else if (dom.nodeType) { // Is a DOM Node
      return dom;
    } else if (dom.tag) { //
      var e = document.createElement(dom.tag);

      // attributes
      if (dom.attr) {
        for (var attr in dom.attr) {
          e.setAttribute(attr, dom.attr[attr]);
        }
      }

      // children. Always an array
      if (dom.children) {
        for (var i = 0, max = dom.children.length; i < max; i++) {
          var child = dom.children[i];
          if (child.jquery) {
            for (var j = 0, maxj = child.length; j < maxj; j++) {
              e.appendChild(OB.UTIL.DOM(child[j]));
            }
          } else {
            e.appendChild(OB.UTIL.DOM(child));
          }
        }
      }
      return e;
    }
  }

  OB.UTIL.NODE = function (tag, attr, children) {
    return {
      tag: tag,
      attr: attr,
      children: children
    };
  }

});