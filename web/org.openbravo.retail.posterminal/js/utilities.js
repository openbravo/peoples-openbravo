/*global define,$ */

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
  };

  // public
  OB.UTIL.getThumbnail = function (base64, width, height, contentType) {
    var url = (base64) ? 'data:' + (contentType ? contentType : 'image/png') + ';base64,' + base64 : 'img/box.png';
    return $('<div/>')
    .addClass('image-wrap')
    .css('margin', 'auto')
    .css('height', height ? height : '48')
    .css('width', width ? width : '48')
    .append($('<div/>')
        .css('margin', 'auto')
        .css('height', '100%')
        .css('width', '100%')            
        .css('background', '#ffffff url(' + url + ') center center no-repeat')
        .css('background-size', 'contain')
     );
  }; 
  
  OB.UTIL.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regexS = '[\\?&]' + n + '=([^&#]*)';
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, ' ')) : '';
  };
    
  OB.UTIL.escapeRegExp = function (text) {
    return text.replace(/[\-\[\]{}()+?.,\\\^$|#\s]/g, '\\$&');
  };
  
  OB.UTIL.recontext = function (model, defaultid) {
    return function (context, id) {
      var obj = new model();
      obj.context = context;
      context.set(id || defaultid, obj);
      return obj;
    };
  };
  
  OB.UTIL.DOM = function (dom) {
    var e, attr, i, max, j, maxj;

    if (typeof (dom) === "string") { // Is an String
      return document.createTextNode(dom);
    } else if (dom.nodeType) { // Is a DOM Node
      return dom;
    } else if (dom.tag) { //
      e = document.createElement(dom.tag);

      // attributes
      if (dom.attr) {
        for (attr in dom.attr) {
          if(dom.attr.hasOwnProperty(attr)) {
            e.setAttribute(attr, dom.attr[attr]);
          }
        }
      }

      // children. Always an array
      if (dom.children) {
        for (i = 0, max = dom.children.length; i < max; i++) {
          var child = dom.children[i];
          if (child.jquery) {
            for (j = 0, maxj = child.length; j < maxj; j++) {
              e.appendChild(OB.UTIL.DOM(child[j]));
            }
          } else {
            e.appendChild(OB.UTIL.DOM(child));
          }
        }
      }
      return e;
    }
  };

  OB.UTIL.NODE = function (tag, attr, children) {
    return {
      tag: tag,
      attr: attr,
      children: children
    };
  };

  OB.UTIL.EL = function (def) {
    var attr, j, maxj;

    if (def.jquery) {
      return def;
    } else if (def.tag) {
      var el = $('<' + def.tag + '/>');
      if (def.attr) {
        for (attr in def.attr) {
          if(def.attr.hasOwnProperty(attr)) {
            el.attr(attr, def.attr[attr]);
          }
        }      
      }
      if (def.content) {
        for (j = 0, maxj = def.content.length; j < maxj; j++) {
          el.append(OB.UTIL.EL(def.content[j]));
        }        
      }
      if (def.init) {
        def.init.call(el);
      }
      return el;
    } else if (typeof (def) === 'string') {
      return $(document.createTextNode(def));
    } else if (def.nodeType) {
      return $(def);
    }
  };
});


