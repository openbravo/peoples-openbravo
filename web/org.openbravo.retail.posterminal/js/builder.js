define([], function () {
// depends on jQuery
  
  var B = function (b, context) {
        
      var mycontext = context || new B.Context();
      var inst = new b.kind(context, b.id);
      
      // attributes
      if (b.attr) {
        if (inst.attr) {
          for (var attr in b.attr) {
            inst.attr(attr, b.attr[attr]);
          }
        } else if (inst.attrs) {
          inst.attrs(b.attr);
        }
      }
      
      // content array
      if (b.content) {
        for (var i = 0, max = b.content.length; i < max; i++) {
          var child = b.content[i];
          if (typeof (child) === "string") {
            inst.append(B({kind: B.KindText(child) }, mycontext));
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
  }
  B.Context.prototype.set = function (name, value) {
    this.models[name] = value;
  }  
  B.Context.prototype.get = function (name) {
    return this.models[name];
  }   
  
  B.Kind = {  
     inithandler: function (init) {
       if (init) {
         init.call(this);
       }
     }      
  };
  
  B.KindText =  function (text) {
     var F = function (context) {
       this.$ = $(document.createTextNode(text));
     };
     F.prototype.attr = function (attr, value) {
     };
     F.prototype.append = function (child) {
     };        
     _.extend(F.prototype, B.Kind);    
     return F;
   }
  
  
  B.KindJQuery = function (tag) {
    var F = function (context) {
      if (tag.jquery) {
        this.$ = tag;
      } else if (tag.nodeType) {
        this.$ = $(tag);
      } else {
        this.$ = $("<" + tag + "/>");
      }
      this.context = context;
    };
    F.prototype.attr = function (attr, value) {
      this.$.attr(attr, value);
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