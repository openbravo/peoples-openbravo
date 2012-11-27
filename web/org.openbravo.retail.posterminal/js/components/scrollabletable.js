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
  name: 'OB.UI.ScrollableTableHeader',
  style: 'border-bottom: 1px solid #cccccc;',
  handlers: {
    onScrollableTableHeaderChanged: 'scrollableTableHeaderChanged_handler'
  }
});

enyo.kind({
  name: 'OB.UI.ScrollableTable',
  published: {
    collection: null,
    listStyle: null
  },
  components: [{
    name: 'theader'
  }, {
    components: [{
      kind: 'Scroller',
      name: 'scrollArea',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'tbody',
        tag: 'ul',
        classes: 'unstyled',
        showing: false
      }]
    }]
  }, {
    name: 'tlimit',
    showing: false,
    style: 'border-bottom: 1px solid #cccccc; padding: 15px; text-align:center; font-weight: bold; color: #a1a328'
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

    this.$.tlimit.setContent(OB.I18N.getLabel('OBPOS_DataLimitReached'));
  },

  listStyleChanged: function (oldSTyle) {
    if (this.listStyle === 'checkboxlist') {
      this.collection.trigger('unSelectAll');
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

    if (this.scrollAreaMaxHeight) {
      this.$.scrollArea.setMaxHeight(this.scrollAreaMaxHeight);
    }

    if (!this.collection) { // set to null?
      return;
    }

    this.collection.on('selected', function (model) {
      if (!model && this.listStyle && this.listStyle !== 'checkboxlist') {
        if (this.selected) {
          this.selected.addRemoveClass('selected', false);
        }
        this.selected = null;
      }
    }, this);

    this.collection.on('unSelectAll', function (col) {
      this.collection.each(function (model) {
        model.trigger('unselected');
      });
    }, this);

    this.collection.on('checkAll', function (col) {
      this.collection.each(function (model) {
        model.trigger('check');
      });
    }, this);

    this.collection.on('unCheckAll', function (col) {
      this.collection.each(function (model) {
        model.trigger('uncheck');
      });
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
      //Put scroller in the position of new item
      this.getScrollArea().scrollToControl(this.$.tbody.getComponents()[options.index]);
    }, this);

    this.collection.on('remove', function (model, prop, options) {
      var index = options.index,
          indexToPoint = index - 1;

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
      } else {
        //Put scroller in the previous item of deleted one
        //Issue 0021835 except when the deleted is the first one.
        if (indexToPoint < 0) {
          indexToPoint = 0;
        }
        this.getScrollArea().scrollToControl(this.$.tbody.getComponents()[indexToPoint]);
      }
    }, this);

    this.collection.on('reset', function (a, b, c) {
      var modelsel, dataLimit;

      this.$.tlimit.hide();
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

        dataLimit = this.collection.at(0).dataLimit;
        if (dataLimit && dataLimit <= this.collection.length) {
          this.$.tlimit.show();
        }

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
  getScrollArea: function () {
    return this.$.scrollArea;
  },
  getHeader: function () {
    var tableName = this.name || '';
    if (this.$.theader.getComponents()) {
      if (this.$.theader.getComponents().length > 0) {
        if (this.$.theader.getComponents().length === 1) {
          return this.$.theader.getComponents()[0];
        } else {
          //developers help
          throw enyo.format('Each scrolleable table ahould have only one component as header', tableName);
        }
      }
    }
    return null;
  },
  _addModelToCollection: function (model, index) {
    var tr = this.$.tbody.createComponent({
      tag: 'li'
    });

    tr.render();

    tr.createComponent({
      kind: this.renderLine,
      model: model
    }).render();

    model.on('change', function () {
      tr.destroyComponents();
      tr.createComponent({
        kind: this.renderLine,
        model: model
      }).render();
    }, this);

    model.on('selected', function () {
      if (this.listStyle && this.listStyle !== 'checkboxlist') {
        if (this.selected) {
          this.selected.addRemoveClass('selected', false);
        }
        this.selected = tr;
        this.selected.addRemoveClass('selected', true);
        // FIXME: OB.UTIL.makeElemVisible(this.node, this.selected);
      } else if (this.listStyle === 'checkboxlist') {
        var components = tr.getComponents();
        if (components.length === 1) {
          if (components[0].$.checkBoxColumn.checked) {
            model.trigger('uncheck', model);
          } else {
            model.trigger('check', model);
          }
        }
      }
    }, this);

    model.on('check', function () {
      if (this.listStyle === 'checkboxlist') {
        var components = tr.getComponents(),
            allChecked = null,
            checkedLines = [];

        if (components.length === 1) {
          components[0].$.checkBoxColumn.check();
          tr.checked = true;

          _.each(tr.getParent().getComponents(), function (comp) {
            if (comp.checked) {
              checkedLines.push(comp.getComponents()[0].model);
              if (allChecked !== false) {
                allChecked = true;
              }
            } else {
              allChecked = false;
            }
          });

          components[0].doLineChecked({
            action: 'check',
            line: model,
            checkedLines: checkedLines,
            allChecked: allChecked
          });
        }
      }
    }, this);

    model.on('uncheck', function () {
      if (this.listStyle === 'checkboxlist') {
        var components = tr.getComponents(),
            checkedLines = [];

        if (components.length === 1) {
          components[0].$.checkBoxColumn.unCheck();
          tr.checked = false;

          _.each(tr.getParent().getComponents(), function (comp) {
            if (comp.checked) {
              checkedLines.push(comp.getComponents()[0].model);
            }
          });

          components[0].doLineChecked({
            action: 'uncheck',
            line: model,
            checkedLines: checkedLines,
            allChecked: false
          });
        }
      }
    }, this);

    model.on('unselected', function () {
      if (this.selected) {
        this.selected.removeClass('selected', false);
      }
      // FIXME: OB.UTIL.makeElemVisible(this.node, this.selected);
      this.selected = null;
    }, this);
  }
});