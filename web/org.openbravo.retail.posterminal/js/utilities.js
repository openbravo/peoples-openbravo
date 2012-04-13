/*global define,$ */

define(['builder'], function (B) {
  
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
  OB.UTIL.Thumbnail = function (base64, width, height, contentType) {
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'image-wrap'}, content: [
        {kind: B.KindJQuery('div'), id: 'image'}                                                                           
      ]}
    );
    this.$ = this.component.$;
    this.image = this.component.context.image.$;
  };
  OB.UTIL.Thumbnail.prototype.attr = function (attr) {
    var url = (attr.img) ? 'data:' + (attr.contentType ? attr.contentType : 'image/png') + ';base64,' + attr.img : 'img/box.png';
    this.$.css('height', attr.height ? attr.height : '48');
    this.$.css('width', attr.width ? attr.width : '48');
    this.image
        .css('margin', 'auto')
        .css('height', '100%')
        .css('width', '100%')            
        .css('background', '#ffffff url(' + url + ') center center no-repeat')
        .css('background-size', 'contain');
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
    return function (context) {
      var obj = new model();
      obj.id = defaultid;      
      obj.context = context;
      return obj;
    };
  };

});


