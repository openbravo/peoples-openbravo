/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _, Backbone, $, enyo */

enyo.kind({
  name: 'OB.UI.iterateArray',
  published: {
    collection: null
  },
  create: function () {
    var listName = this.name || '';

    this.inherited(arguments);

    // helping developers
    if (!this.renderLine) {
      throw enyo.format('Your list %s needs to define a renderLine kind', listName);
    }

    if (!this.renderEmpty) {
      throw enyo.format('Your list %s needs to define a renderEmpty kind', listName);
    }


    if (this.collection) {
      this.collectionChanged(null);
    }
  },

  collectionChanged: function (oldCollection) {
    var int = 0;
    this.destroyComponents();
    if (!this.collection) { // set to null ?
      return;
    }

    for (int; int < this.collection.length; int++) {
      this._addModelToCollection(this.collection[int]);
    }
    this.render();
  },
  _addModelToCollection: function (model, index) {
    var tr = this.createComponent({
      kind: this.renderLine,
      model: model,
      lblProperty: this.lblProperty,
      qtyProperty: this.qtyProperty
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
  name: 'OB.UI.List',
  kind: 'enyo.Select',
  published: {
    collection: null
  },
  create: function () {
    var listName = this.name || '';

    this.inherited(arguments);

    // helping developers
    if (!this.renderLine) {
      throw enyo.format('Your list %s needs to define a renderLine kind', listName);
    }

    if (!this.renderEmpty) {
      throw enyo.format('Your list %s needs to define a renderEmpty kind', listName);
    }

    this.header = this.renderHeader ? 1 : 0;

    if (this.collection) {
      this.collectionChanged(null);
    }
  },

  collectionChanged: function (oldCollection) {
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
          this.selected.addRemoveClass('selected', false);
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
      var modelsel;

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

        if (this.listStyle === 'list') {
          modelsel = this.collection.at(0);
          modelsel.trigger('selected', modelsel);
        } else if (this.listStyle === 'edit') {
          modelsel = this.collection.at(this.collection.size() - 1);
          modelsel.trigger('selected', modelsel);
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
    var tr = this.$.tbody.createComponent({
      tag: 'li'
    });
    tr.createComponent({
      kind: this.renderLine,
      model: model
    });

    tr.render();

    model.on('change', function () {
      tr.destroyComponents();
      tr.createComponent({
        kind: this.renderLine,
        model: model
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