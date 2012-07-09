/*global B, Backbone, $, _ */

(function () {

  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};

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

  OB.UTIL.padNumber = function (n, p) {
    var s = n.toString();
    while (s.length < p) {
      s = '0' + s;
    }
    return s;
  };

  OB.UTIL.encodeXMLComponent = function (s, title, type) {
    return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('\'', '&apos;').replace('\"', '&quot;');
  };

  OB.UTIL.loadResource = function (res, callback, context) {
    $.ajax({
      url: res,
      dataType: 'text',
      type: 'GET',
      success: function (data, textStatus, jqXHR) {
        callback.call(context || this, data);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        callback.call(context || this);
      }
    });
  };

  OB.UTIL.queueStatus = function (queue) {
    // Expects an object where the value element is true/false depending if is processed or not
    if (!_.isObject(queue)) {
      throw 'Object expected';
    }
    return _.reduce(queue, function (memo, val) {
      return memo && val;
    }, true);
  };
  
    
  function _initContentView(view, child) {
    var obj, inst, i, max;
    if (typeof (child) === 'string') {
      inst = $(document.createTextNode(child));
    } else if (child.tag) {
      inst = $('<' + child.tag + '/>'); 
      if (child.attributes) {
        inst.attr(child.attributes);
      }
      if (child.content) {
        for (i = 0, max = child.content.length; i < max; i++) {
          inst.append(_initContentView(view, child.content[i]));
        }        
      }
      if (child.id) {
        view[child.id] = inst;
      }      
    } else if (child.view) {
      obj = new child.view();
      inst = obj.$el;
      if (child.id) {
        view[child.id] = obj;
      }
    } else if (child.$el) {
      inst = child.$el;
    }
    return inst
  }
  
  OB.UTIL.initContentView = function (view) {
    var i, max;
    if (view.contentView) {
      for (i = 0, max = view.contentView.length; i < max; i++) {
        view.$el.append(_initContentView(view, view.contentView[i]));
      }       
    }
  };

}());