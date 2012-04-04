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
  
  // Table View
  OB.COMP.TableView = function (defaults) {
  
    var me = this;
    
    this.renderHeader = defaults.renderHeader;
    this.renderLine = defaults.renderLine;
    this.renderEmpty = defaults.renderEmpty;
    this.style = defaults.style; // none, "edit", "list"

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
    this.selected = null;  
    
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
      var tr = OB.UTIL.EL({tag: 'li'});
      tr.append(this.renderLine(model));
      tr.click(function (e) {
        e.preventDefault();
        model.trigger('selected', model);
        model.trigger('click', model);
      });
      
      model.on('change', function() {
        tr.empty().append(this.renderLine(model));
      }, this);
      
      model.on('selected', function() {
        if (this.style) {
          if (this.selected) {
            this.selected.removeClass('selected');
          }
          this.selected = tr;
          this.selected.addClass('selected');
          OB.UTIL.makeElemVisible(this.div, this.selected);
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
    
    this.collection.on('reset', function() {
      
      this.tbody.hide();
      this.tempty.show();
      
      this.tbody.empty();  
      this.collection.trigger('selected');
    }, this);    
  }

}); 