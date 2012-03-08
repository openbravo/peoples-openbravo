define(['utilities'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.TableView = function (defaults) {
  
    var me = this;
    
    this.renderHeader = defaults.renderHeader;
    this.renderLine = defaults.renderLine;
    this.style = defaults.style; // none, "edit", "list"
    
    this.stack = new Backbone.Model({
      initialize : function () {
        this.set('selected', -1);
      }
    });
                                                                     
    var intable = [];
    if (this.renderHeader) {
      var trheader = $(OB.UTIL.DOM(OB.UTIL.NODE('tr', {}, [])));
      trheader.append(this.renderHeader());  
      intable.push(OB.UTIL.NODE('thead', {}, [trheader]));
    }
    this.tbody = $(OB.UTIL.DOM(OB.UTIL.NODE('tbody', {}, [])));
    intable.push(this.tbody);
    
    this.div = $(OB.UTIL.DOM(
      OB.UTIL.NODE('div', {'style': 'overflow:auto; height: 300px; margin-bottom:30px;'}, [
        OB.UTIL.NODE('table', {'class': 'table table-bordered'}, intable)
      ])
    ));    
  }

  OB.COMP.TableView.prototype.setModel = function (collection) {
    this.collection = collection;
    this.selected = -1;   
    
    this.collection.on('change', function(model, prop) {          
      var index = this.collection.indexOf(model);
      this.tbody.children().eq(index)
        .empty()
        .append(this.renderLine(model));      
    }, this);
    
    this.collection.on('add', function(model, prop, options) {     
      var index = options.index;
      var me = this;
      var tr = $('<tr/>');
      tr.append(this.renderLine(model));
      tr.click(function () {
        var index = me.collection.indexOf(model)
        me.stack.set('selected', index);
        me.stack.trigger('click', model, index);
      });
      if (index === this.collection.length - 1) {
        this.tbody.append(tr);
      } else {
        this.tbody.children().eq(index).before(tr);
      }
      
      if (this.style === 'list') {
        if (this.stack.get('selected') < 0) {
          this.stack.set('selected', index); 
        }
      } else if (this.style === 'edit') {
        this.stack.set('selected', index);
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
    }, this);
    
    this.collection.on('reset', function() {
      this.tbody.empty();  
      this.stack.set('selected', -1);
    }, this);    
        
    if (this.style) {
      // mark the selected element...
      this.stack.on('change:selected', function () {
        var children = this.tbody.children();
        if (this.selected > -1) {
          children.eq(this.selected).css('background-color', '').css('color', '');
        }         
        this.selected = this.stack.get('selected');
        if (this.selected > -1) {
          var elemselected = children.eq(this.selected);      
          elemselected.css('background-color', '#049cdb').css('color', '#fff');
          OB.UTIL.makeElemVisible(this.div, elemselected);
        }      
      }, this);
    }
  }

}); 