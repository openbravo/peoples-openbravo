/*global _, Backbone, $, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  // Order list
  OB.UI.ListView = function (tag) {
    return Backbone.View.extend({
      tagName: tag,
      registerCollection: function (collection) {
        this.collection = collection;
        if (this.collection) {
          this.collection.on('change', function (model, prop) {
            var index = this.collection.indexOf(model);
            this.$el.children().eq(index + this.header).replaceWith((new this.renderLine({
              parent: this,
              model: model
            })).render().$el);
          }, this);

          this.collection.on('add', function (model, prop, options) {
            this._addModelToCollection(model, options.index);
          }, this);

          this.collection.on('remove', function (model, prop, options) {
            var index = options.index;
            this.$el.children().eq(index + this.header).remove();
          }, this);

          this.collection.on('reset', function () {
            this.$el.empty();
            if (this.renderHeader) {
              this.$el.append((new this.renderHeader()).render().$el);
            }
            this.collection.each(function (model) {
              this._addModelToCollection(model);
            }, this);
          }, this);
        }

        if (this.renderHeader) {
          this.$el.append((new this.renderHeader()).render().$el);
        }

      },
      _addModelToCollection: function (model, index) {
        var me = this,
            tr = (new this.renderLine({
            parent: this,
            model: model
          })).render().$el;
        if (_.isNumber(index) && index < this.collection.length - 1) {
          this.$el.children().eq(index + this.header).before(tr);
        } else {
          this.$el.append(tr);
        }
      },

      attr: function (attr) {
        // Deprecated function used by Builder
        this.htmlId = attr.htmlId;
        this.className = attr.className;
        this.style = attr.style;
        this.renderLine = attr.renderLine;
        this.renderHeader = attr.renderHeader;
        this.header = this.renderHeader ? 1 : 0;

        // Init clear...
        this.$el.empty();

        if (this.htmlId) {
          this.$el.attr('id', this.htmlId);
        }
        if (this.className) {
          this.$el.addClass(this.className);
        }
        if (this.style) {
          this.$el.attr('style', this.style);
        }
        if (attr.collection) {
          this.registerCollection(attr.collection);
        }
      }
    });
  };

  enyo.kind({
    name: 'OB.UI.List',
    published: {
      collection: null
    },
    create: function () {
      var listName: this.name || '';

      this.inherited(arguments);

      // helping developers
      if (!this.renderLine) {
        throw enyo.format('Your list %s needs to define a renderLine kind', listName);
      }

      if (!this.renderEmpty) {
        throw enyo.format('Your list %s needs to define a renderEmpty kind', listName);
      }

      this.header = this.renderHeader ? 1 : 0

      if (this.collection) {
        this.collectionChanged(null);
      }
    },
    collectionChanged: function (oldCollection) {
      if (this.renderHeader) {
        this.createComponent({
          kind: this.renderHeader
        }).render();
      }

      if (!this.collection) { // set to null ?
        return;
      }

      this.collection.on('change', function (model, prop) {
        var index = this.collection.indexOf(model);
        // FIXME: instead of recreate the item, we call changed? init ?
        // repeated items needs to reset the values
        // e.g. this.controlAtIndex(index + this.header).changed();
      }, this);

      this.collection.on('add', function (model, prop, options) {
        this._addModelToCollection(model, options.index);
      }, this);

      this.collection.on('remove', function (model, prop, options) {
        var index = options.index;
        this.controlAtIndex(index + this.header).destroy();
      }, this);

      this.collection.on('reset', function () {
        this.destroyComponents();
        if (this.renderHeader) {
          this.createComponent({
            kind: this.renderHeader
          }).render();
        }
        this.collection.each(function (model) {
          this._addModelToCollection(model);
        }, this);
      }, this);

    },
    _addModelToCollection: function (model, index) {
      var tr = this.createComponent({
        kind: this.renderLine,
        model: model
      });
      tr.render();
      //FIXME: can we add a model in the middle of a collection?
      //      if (_.isNumber(index) && index < this.collection.length - 1) {
      //        this.$el.children().eq(index + this.header).before(tr);
      //      } else {
      //        this.$el.append(tr);
      //      }
    }
  });

  enyo.kind({
    name: 'OB.UI.Table',
    published: {
      collection: null
    },
    listStyle: 'list',
    components: [{
      name: 'theader'
    }, {
      name: 'tbody',
      tag: 'ul',
      classes: 'unstyled',
      showing: false
    }, {
      name: 'tinfo',
      showing: false,
      style: 'border-bottom: 1px solid #cccccc; padding: 15px; font-weight: bold; color: #cccccc'
    }, {
      name: 'tempty'
    }],
    create: function () {
      var tableName = this.name || '';

      this.inherited(arguments);

      // helping developers
      if (!this.renderLine) {
        throw enyo.format('Your list %s needs to define a renderLine kind', tableName);
      }

      if (!this.renderEmpty) {
        throw enyo.format('Your list %s needs to define a renderEmpty kind', tableName);
      }

      if (this.collection) {
        this.collectionChanged(null);
      }
    },
    collectionChanged: function (oldCollection) {
      this.selected = null;

      if (this.renderHeader && this.$.theader.getComponents().length === 0) {
        this.$.theader.createComponent({
          kind: this.renderHeader
        });
      }

      if (this.renderEmpty && this.$.tempty.getComponents().length === 0) {
        this.$.tempty.createComponent({
          kind: this.renderEmpty
        });
      }

      if (!this.collection) { // set to null?
        return;
      }

      this.collection.on('selected', function (model) {
        if (!model && this.listStyle) {
          if (this.selected) {
            this.selected.removeClass('selected');
          }
          this.selected = null;
        }
      }, this);

      this.collection.on('add', function (model, prop, options) {

        this.$.tempty.hide();
        this.$.tbody.show();

        this._addModelToCollection(model, options.index);

        if (this.listStyle === 'list') {
          if (!this.selected) {
            model.trigger('selected', model);
          }
        } else if (this.listStyle === 'edit') {
          model.trigger('selected', model);
        }
      }, this);

      this.collection.on('remove', function (model, prop, options) {
        var index = options.index;

        this.$.tbody.getComponents()[index].destroy(); // controlAtIndex ?
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
          this.$.tbody.hide();
          this.$.tempty.show();
        }
      }, this);

      this.collection.on('reset', function (a, b, c) {
        var lastmodel;

        this.$.tbody.hide();
        this.$.tempty.show();

        this.$.tbody.destroyComponents();

        if (this.collection.size() === 0) {
          this.$.tbody.hide();
          this.$.tempty.show();
          this.collection.trigger('selected');
        } else {
          this.$.tempty.hide();
          this.$.tbody.show();
          this.collection.each(function (model) {
            this._addModelToCollection(model);
          }, this);

          if (this.listStyle === 'list' || this.listStyle === 'edit') {
            lastmodel = this.collection.at(this.collection.size() - 1);
            lastmodel.trigger('selected', lastmodel);
          }
        }
      }, this);

      this.collection.on('info', function (info) {
        if (info) {
          this.$.tinfo.setContent(OB.I18N.getLabel(info));
          this.$.tinfo.show();
        } else {
          this.$.tinfo.hide();
        }
      }, this);

      // XXX: Reseting to show the collection if registered with data
      this.collection.trigger('reset');
    },

    _addModelToCollection: function (model, index) {
      var me = this,
          tr;

      tr = this.$.tbody.createComponent({
        tag: 'li'
      });
      tr.createComponent({
        kind: this.renderLine,
        model: model,
        parent: me
      }).render();

      model.on('change', function () {
        tr.destroyComponents();
        tr.createComponent({
          kind: this.renderLine,
          model: model,
          parent: me
        }).render();
      }, this);

      model.on('selected', function () {
        if (this.listStyle) {
          if (this.selected) {
            this.selected.addRemoveClass('selected', false);
          }
          this.selected = tr;
          this.selected.addRemoveClass('selected', true);
          // FIXME: OB.UTIL.makeElemVisible(this.node, this.selected);
        }
      }, this);
    }
  });

  // Table View
  OB.UI.TableView = Backbone.View.extend({
    tagName: 'div',
    initialize: function () {
      this.theader = $('<div/>');
      this.tbody = $('<ul/>').addClass('unstyled').css('display', 'none');
      this.tempty = $('<div/>');
      this.tinfo = $('<div/>').css('display', 'none').css('border-bottom', '1px solid #cccccc').css('padding', '15px').css('font-weight', 'bold').css('color', '#cccccc');
      this.$el.empty().append(this.theader).append(this.tbody).append(this.tinfo).append(this.tempty);

    },

    registerCollection: function (collection) {
      this.collection = collection;
      this.selected = null;

      if (this.renderHeader) {
        this.theader.append((new this.renderHeader()).render().$el);
      }

      if (this.renderEmpty) {
        this.tempty.append((new this.renderEmpty()).render().$el);
      }

      this.collection.on('selected', function (model) {
        if (!model && this.style) {
          if (this.selected) {
            this.selected.removeClass('selected');
          }
          this.selected = null;
        }
      }, this);

      this.collection.on('add', function (model, prop, options) {

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

      this.collection.on('reset', function (a, b, c) {
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
          this.collection.each(function (model) {
            this._addModelToCollection(model);
          }, this);

          if (this.style === 'list' || this.style === 'edit') {
            lastmodel = this.collection.at(this.collection.size() - 1);
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

      // XXX: Reseting to show the collection if registered with data
      this.collection.trigger('reset');
    },



    _addModelToCollection: function (model, index) { // means after...
      var me = this,
          tr = $('<li/>');
      tr.append((new this.renderLine({
        parent: me,
        model: model
      })).render().$el);
      // tr.click(stdClickEvent);
      model.on('change', function () {
        tr.empty().append((new this.renderLine({
          parent: me,
          model: model
        })).render().$el);
      }, this);

      model.on('selected', function () {
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
    },

    attr: function (attr) {
      // Deprecated function used by Builder
      this.style = attr.style; // none, "edit", "list"
      this.renderHeader = attr.renderHeader;
      this.renderEmpty = attr.renderEmpty;
      this.renderLine = attr.renderLine;
      if (attr.collection) {
        this.registerCollection(attr.collection);
      }
    }
  });

}());