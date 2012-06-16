/*global define, _, $ */

define(['builder', 'utilities', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.ListView = function (tag) {

    var F = function (context) {
      this.component = B(
        {kind: B.KindJQuery(tag)}
      );
      this.$el = this.component.$el;
    };
    
    F.prototype._addModelToCollection = function (model, index) {
      var me = this;
      var tr = B(this.renderLine(model)).$el;
      if (_.isNumber(index) && index < this.collection.length - 1) {
        this.$el.children().eq(index + this.header).before(tr);
      } else {
        this.$el.append(tr);  
      }      
    };
    
    F.prototype.attr = function (attr) {
      this.renderLine = attr.renderLine;
      this.renderHeader = attr.renderHeader;
      this.header = this.renderHeader ? 1 : 0;
      this.collection = attr.collection;

      this.collection.on('change', function(model, prop) {
        var index = this.collection.indexOf(model);
        this.$el.children().eq(index + this.header)
          .replaceWith(B(this.renderLine(model)).$el);
      }, this);

      this.collection.on('add', function(model, prop, options) {
        this._addModelToCollection(model, options.index);
      }, this);

      this.collection.on('remove', function (model, prop, options) {
        var index = options.index;
        this.$el.children().eq(index + this.header).remove();
      }, this);

      this.collection.on('reset', function() {
        this.$el.empty();
        if (this.renderHeader) {
          this.$el.append(B(this.renderHeader()).$el);
        }
        this.collection.each(function (model) { this._addModelToCollection(model); }, this);        
      }, this);

      // Init clear...
      this.$el.empty();
      if (this.renderHeader) {
        this.$el.append(B(this.renderHeader()).$el);
      }
    };

    return F;
  };


  // Table View
  OB.COMP.TableView = function (context) {

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), id: 'header'},
        {kind: B.KindJQuery('ul'), id: 'body', attr: {'class': 'unstyled', 'style': 'display: none'}},
        {kind: B.KindJQuery('div'), id: 'info', attr: {'style': 'display: none; border-bottom: 1px solid #cccccc; padding: 15px; font-weight:bold; color: #cccccc'}},
        {kind: B.KindJQuery('div'), id: 'empty'}
      ]}
    );
    this.$el = this.component.$el;
    this.theader = this.component.context.header.$el;
    this.tbody = this.component.context.body.$el;
    this.tempty = this.component.context.empty.$el;
    this.tinfo = this.component.context.info.$el;
  };

  OB.COMP.TableView.prototype.renderLineModel = function (model) {
    var b;
    if (this.renderLine.prototype.initialize) { // it is a backbone view
      b = (new this.renderLine({model: model})).render();
    } else {
      // old fashioned render
      b = B(this.renderLine(model));
      b.$el.click(function (e) {
        model.trigger('selected', model);
        model.trigger('click', model);
        b.$el.parents('.modal').filter(':first').modal('hide'); // If in a modal dialog, close it
      });
    }
    return b;
  };
  
  OB.COMP.TableView.prototype._addModelToCollection = function (model, index) { // means after...
      var me = this;
      var tr = $('<li/>');
      tr.append(this.renderLineModel(model).$el);
      // tr.click(stdClickEvent);

      model.on('change', function() {
        tr.empty().append(this.renderLineModel(model).$el);
      }, this);

      model.on('selected', function() {
        if (this.style) {
          if (this.selected) {
            this.selected.removeClass('selected');
          }
          this.selected = tr;
          this.selected.addClass('selected');
          OB.UTIL.makeElemVisible(this.$el, this.selected);
        }
      }, this);

      if (_.isNumber(index) && index < this.collection.length - 1) {
        this.tbody.children().eq(index).before(tr);        
      } else {
        this.tbody.append(tr);
      }    
  };

  OB.COMP.TableView.prototype.attr = function (attr) {
    this.style = attr.style; // none, "edit", "list"
    this.renderHeader = attr.renderHeader;
    this.renderLine = attr.renderLine;
    this.renderEmpty = attr.renderEmpty;
    this.collection = attr.collection;
    this.selected = null;

    if (this.renderHeader) {
      this.theader.append(B(this.renderHeader()).$el);
    }

    if (this.renderEmpty) {
      this.tempty.append(B(this.renderEmpty()).$el);
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
      
      this._addModelToCollection(model, options.index);

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
      var lastmodel;
      
      this.tbody.hide();
      this.tempty.show();

      this.tbody.empty();
      
      if (this.collection.size() === 0) {
        this.tbody.hide();
        this.tempty.show();
        this.collection.trigger('selected');
      } else {
        this.tempty.hide();
        this.tbody.show();
        this.collection.each(function (model) { this._addModelToCollection(model); }, this);        
        
        if (this.style === 'list' || this.style === 'edit') {
          lastmodel = this.collection.at(this.collection.size() -1);
          lastmodel.trigger('selected', lastmodel);
        }               
      }          
    }, this);

    this.collection.on('info', function (info) {
      if (info) {
        this.tinfo.text(OB.I18N.getLabel(info));
        this.tinfo.show();
      } else {
        this.tinfo.hide();
      }
    }, this);
  };

});