/*global define */

define(['builder', 'utilities'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  // Order list
  OB.COMP.ListView = function (tag) {
    
    var F = function (context) {     
      this.component = B(
        {kind: B.KindJQuery(tag)}
      );
      this.$ = this.component.$;
    };
    
    F.prototype.attr = function (attr) {
      this.renderLine = attr.renderLine;
      this.renderHeader = attr.renderHeader;
      this.header = this.renderHeader ? 1 : 0;    
      this.collection = attr.collection;
      
      this.collection.on('change', function(model, prop) {          
        var index = this.collection.indexOf(model);
        this.$.children().eq(index + this.header)
          .replaceWith(this.renderLine(model).$);      
      }, this);
      
      this.collection.on('add', function(model, prop, options) {     
        var index = options.index;
        var me = this;
        var tr = this.renderLine(model).$;
        if (index === this.collection.length - 1) {
          this.$.append(tr);
        } else {
          this.$.children().eq(index + this.header).before(tr);
        }
      }, this);
      
      this.collection.on('remove', function (model, prop, options) {        
        var index = options.index;
        this.$.children().eq(index + this.header).remove();         
      }, this);
      
      this.collection.on('reset', function() {
        this.$.empty();
        if (this.renderHeader) {
          this.$.append(this.renderHeader().$);
        }
      }, this);   
      
      // Init clear...
      this.$.empty();
      if (this.renderHeader) {
        this.$.append(this.renderHeader().$);
      }    
    };   
    
    return F;
  };

  
  // Table View
  OB.COMP.TableView = function (context) {
  
    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), id: 'header'},
        {kind: B.KindJQuery('ul'), id: 'body', attr: {'class': 'unstyled', style: 'display: none'}},
        {kind: B.KindJQuery('div'), id: 'empty'}
      ]}
    );
    this.$ = this.component.$;
    this.theader = this.component.context.header.$;
    this.tbody = this.component.context.body.$;
    this.tempty = this.component.context.empty.$;   
  };
  
  OB.COMP.TableView.prototype.attr = function (attr) {
    this.style = attr.style; // none, "edit", "list"        
    this.renderHeader = attr.renderHeader;
    this.renderLine = attr.renderLine;
    this.renderEmpty = attr.renderEmpty;
    this.collection = attr.collection;
    this.selected = null;      
    
    if (this.renderHeader) {      
      this.theader.append(this.renderHeader().$);  
    }   
    
    if (this.renderEmpty) {
      this.tempty.append(this.renderEmpty().$);
    }    
    
    this.collection.on('selected', function (model) {
      if (!model && this.style) {
        if (this.selected) {
          this.selected.removeClass('selected');
        }
        this.selected = null;
      }        
    }, this);
       
    this.collection.on('add', function(model, prop, options) {     
      
      this.tempty.hide();
      this.tbody.show();
      
      var me = this;
      var tr = B({kind: B.KindJQuery('li')}).$;
      tr.append(this.renderLine(model).$);
      tr.click(function (e) {
        e.preventDefault();
        model.trigger('selected', model);
        model.trigger('click', model);
      });
      
      model.on('change', function() {
        tr.empty().append(this.renderLine(model).$);
      }, this);
      
      model.on('selected', function() {
        if (this.style) {
          if (this.selected) {
            this.selected.removeClass('selected');
          }
          this.selected = tr;
          this.selected.addClass('selected');
          OB.UTIL.makeElemVisible(this.$, this.selected);
        }
      }, this);

      var index = options.index;
      if (index === this.collection.length - 1) {
        this.tbody.append(tr);
      } else {
        this.tbody.children().eq(index).before(tr);
      }
      
      if (this.style === 'list') {
        if (!this.selected) {
          model.trigger('selected', model);
        }
      } else if (this.style === 'edit') {
        model.trigger('selected', model);
      }
    }, this);
    
    this.collection.on('remove', function (model, prop, options) {        
      var index = options.index;
      this.tbody.children().eq(index).remove();

      if (index >= this.collection.length) {
        if (this.collection.length === 0) {
          this.collection.trigger('selected');
        } else {
          this.collection.at(this.collection.length - 1).trigger('selected', this.collection.at(this.collection.length - 1));
        }
      } else {
        this.collection.at(index).trigger('selected', this.collection.at(index));
      }  
      
      if (this.collection.length === 0) {             
        this.tbody.hide();
        this.tempty.show();
      }
    }, this);
    
    this.collection.on('reset', function(a,b,c) {
      
      this.tbody.hide();
      this.tempty.show();
      
      this.tbody.empty();  
      this.collection.trigger('selected');
    }, this);   
    
    this.collection.on('info', function (info) {
//      if (info) {
//        console.log(info);
//      }
    }, this);
  };

}); 