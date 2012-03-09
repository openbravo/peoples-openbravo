define([], function () {
// depends on jQuery
  
  var B = function (b) {
        
      var inst = new b.kind(B);
      
      // attributes
      if (b.attr) {
        for (var attr in b.attr) {
          inst.attr(attr, b.attr[attr]);
        }
      }
      
      // content array
      if (b.content) {
        for (var i = 0, max = b.content.length; i < max; i++) {
          var child = b.content[i];
          if (typeof (child) === "string") {
            inst.append(B({kind: B.KindText(child) }));
          } else if (child.kind) {
            inst.append(B(child));
          } else {
            inst.append(child);
          }
        }
      }     
      
      return inst;
   };
   
  B.KindText =  function (text) {
     var F = function (bb) {
       this.$ = $(document.createTextNode(text));
     };
     F.prototype.attr = function (attr, value) {
     };
     F.prototype.append = function append(child) {
     }       
     return F;
   }
  
  B.KindJQuery = function (tag) {
     var F = function (bb) {
       if (tag.jquery) {
         this.$ = tag;
       } else if (tag.nodeType) {
         this.$ = $(tag);
       } else {
         this.$ = $("<" + tag + "/>");
       }
     };
     F.prototype.attr = function (attr, value) {
       this.$.attr(attr, value)
     };
     F.prototype.append = function append(child) {
       this.$.append(child.$);
     };       
     return F;
   };
   
   B.models = {}
   
   B.set = function (name, value) {
     this.models[name] = value;
   }
  
   B.get = function (name) {
     return this.models[name];
   }  
   
  return B;
  
});