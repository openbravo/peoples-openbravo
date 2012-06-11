/*global define, $, _ */

define([], function () {

  var B;

  B = function (b, context) {

    var mycontext = context || {};

    var inst = new b.kind(mycontext);
    inst._id = b.id || inst._id;
    if (inst._id) {
      mycontext[inst._id] = inst;
    }

    var i, max, child;

    // attributes
    if (b.attr && inst.attr) {
      inst.attr(b.attr);
    }

    // content array
    if (b.content) {
      for (i = 0, max = b.content.length; i < max; i++) {
        child = b.content[i];
        if (typeof (child) === 'string') {
          inst.append(B({
            kind: B.KindText(child)
          }, mycontext));
        } else if (child.kind) {
          inst.append(B(child, mycontext));
        } else {
          inst.append(child);
        }
      }
    }

    // initialize
    if (inst.inithandler) {
      inst.inithandler(b.init);
    }

    return inst;
  };

  B.Kind = {
    inithandler: function (init) {
      if (init) {
        init.call(this);
      }
    }
  };

  B.KindText = function (text) {
    var F = function (context) {
      this.$el = $(document.createTextNode(text));
    };
    _.extend(F.prototype, B.Kind);
    return F;
  };

  B.KindHTML = function (html) {
    var F = function (context) {
      this.$el = $(html);
      this.context = context;
    };
    _.extend(F.prototype, B.Kind);
    return F;
  };

  B.KindJQuery = function (tag) {
    var F = function (context) {
      this.$el = $("<" + tag + "/>");
      this.context = context;
    };

    F.prototype.attr = function (attrs) {
      var attr;
      for (attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
          this.$el.attr(attr, attrs[attr]);
        }
      }
    };

    F.prototype.append = function (child) {
      if (child.render) {
        this.$el.append(child.render().$el); // it is a backbone view.
      } else if (child.$el) {
        this.$el.append(child.$el);
      }
    };

    _.extend(F.prototype, B.Kind);
    return F;
  };

  return B;
});