/*global define,$,_,Backbone */

define([], function () {
  // depends on jQuery
  var B;

  B = function (b, context) {

    var mycontext = context || new B.Context();
    var inst = new b.kind(mycontext);
    inst._id = b.id || inst._id;
    if (inst._id) {
      mycontext.set(inst._id, inst);
    }
    
    var attr, i, max, child;

    // attributes
    if (b.attr && inst.attr) {
      inst.attr(b.attr);
    }

    // content array
    if (b.content) {
      for (i = 0, max = b.content.length; i < max; i++) {
        child = b.content[i];
        if (typeof (child) === "string") {
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

  B.Context = function () {
    this.models = [];
    _.extend(this, Backbone.Events);
  };

  B.Context.prototype.set = function (name, value) {
    this.models[name] = value;
  };

  B.Context.prototype.get = function (name) {
    return this.models[name];
  };

  B.Kind = {
    inithandler: function (init) {
      if (init) {
        init.call(this);
      }
    }
  };

  B.KindText = function (text) {
    var F;

    F = function (context) {
      this.$ = $(document.createTextNode(text));
    };
    _.extend(F.prototype, B.Kind);
    return F;
  };

  B.KindHTML = function (html) {
    var F;

    F = function (context) {
      this.$ = $(html);
      this.context = context;
    };
    _.extend(F.prototype, B.Kind);
    return F;
  };

  B.KindJQuery = function (tag) {
    var F;

    F = function (context) {
      if (tag.jquery) {
        this.$ = tag;
      } else if (tag.nodeType) {
        this.$ = $(tag);
      } else {
        this.$ = $("<" + tag + "/>");
      }
      this.context = context;
    };

    F.prototype.attr = function (attrs) {
      var attr;
      for (attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
          this.$.attr(attr, attrs[attr]);
        }
      }      
    };

    F.prototype.append = function (child) {
      if (child.$) {
        this.$.append(child.$);
      }
    };

    _.extend(F.prototype, B.Kind);
    return F;
  };

  return B;

});