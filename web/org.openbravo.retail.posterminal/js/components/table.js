define(['utilities', 'model/stack'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  // Order list
  OB.COMP.ListView = function (defaults) {
   
    this.renderLine = defaults.renderLine;
    this.renderHeader = defaults.renderHeader;
    this.header = this.renderHeader ? 1 : 0;
    this.$ = defaults.$ || OB.UTIL.EL({tag: 'div'});
  };
  
  OB.COMP.ListView.prototype.setModel = function (collection) {
    this.collection = collection;
    
    this.collection.on('change', function(model, prop) {          
      var index = this.collection.indexOf(model);
      this.$.children().eq(index + this.header)
        .replaceWith(this.renderLine(model));      
    }, this);
    
    this.collection.on('add', function(model, prop, options) {     
      var index = options.index;
      var me = this;
      var tr = this.renderLine(model);
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
        this.$.append(this.renderHeader());
      }
    }, this);   
    
    // Init clear...
    this.$.empty();
    if (this.renderHeader) {
      this.$.append(this.renderHeader());
    }    
  }; 
  

  // Order table
  OB.COMP.TableView = function (defaults) {
  
    var me = this;
    
    this.renderHeader = defaults.renderHeader;
    this.renderLine = defaults.renderLine;
    this.renderEmpty = defaults.renderEmpty;
    this.style = defaults.style; // none, "edit", "list"
    
    this.stack = defaults.stack
    ? defaults.stack
    : new OB.MODEL.Stack();
       
    this.theader = OB.UTIL.EL({tag: 'div'});                                                             
    if (this.renderHeader) {      
      this.theader.append(this.renderHeader());  
    }
    this.tempty = OB.UTIL.EL({tag: 'div'});
    if (this.renderEmpty) {
      this.tempty.append(this.renderEmpty());
    }
    this.tbody = OB.UTIL.EL({tag: 'ul', attr: {'class': 'unstyled', style: 'display: none'}});
    
    this.div = OB.UTIL.EL({tag: 'div', content: [this.theader, this.tbody, this.tempty]});
  };

  OB.COMP.TableView.prototype.setModel = function (collection) {
    this.collection = collection;
    this.selected = -1;   
    
    this.collection.on('change', function(model, prop) {          
      
      var index = this.collection.indexOf(model);
      this.tbody.children().eq(index)
        .empty().append(this.renderLine(model));  
      
    }, this);
    
    this.collection.on('add', function(model, prop, options) {     
      
      this.tempty.hide();
      this.tbody.show();
      
      var me = this;
      var tr = OB.UTIL.EL({tag: 'li'});
      tr.append(this.renderLine(model));
      tr.click(function (e) {
        e.preventDefault();
        var index = me.collection.indexOf(model)
        me.stack.set('selected', index);
        me.stack.trigger('click', model, index);
      });

      // remove the old selected class
      if (this.style === 'edit') {
        var children = this.tbody.children();
        if (this.selected > -1) {
          children.eq(this.selected).removeClass('selected');
        }
      }
      
      var index = options.index;
      if (index === this.collection.length - 1) {
        this.tbody.append(tr);
      } else {
        this.tbody.children().eq(index).before(tr);
      }
      
      if (this.style === 'list') {
        if (this.stack.get('selected') < 0) {
          this.stack.set('selected', index, {silent: true}); 
          this.stack.trigger('change:selected'); // forcing the change to be fired...
        }
      } else if (this.style === 'edit') {
        this.stack.set('selected', index, {silent: true});
        this.stack.trigger('change:selected'); // forcing the change to be fired...
      }
    }, this);
    
    this.collection.on('remove', function (model, prop, options) {        
      var index = options.index;
      this.tbody.children().eq(index).remove();

      if (index >= this.collection.length) {
        this.stack.set('selected', this.collection.length - 1);
      } else {
        this.stack.trigger('change:selected'); // we need to force the change event.
        // this.stack.set('selected', index);
      }  
      
      if (this.collection.length === 0) {             
        this.tbody.hide();
        this.tempty.show();
      }
    }, this);
    
    this.collection.on('reset', function() {
      
      this.tbody.hide();
      this.tempty.show();
      
      this.tbody.empty();  
      this.stack.set('selected', -1);
    }, this);    
        
    if (this.style) {
      // mark the selected element...
      this.stack.on('change:selected', function () {
        var children = this.tbody.children();
        if (this.selected > -1) {
          children.eq(this.selected).removeClass('selected');
        }         
        this.selected = this.stack.get('selected');
        if (this.selected > -1) {
          var elemselected = children.eq(this.selected);      
          elemselected.addClass('selected');
          OB.UTIL.makeElemVisible(this.div, elemselected);
        }      
      }, this);
    }
  }

}); 