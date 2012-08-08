/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

enyo.kind({
  name: 'OB.UI.RenderCategory',
  kind: 'OB.UI.SelectButton',
  components: [{
    style: 'float: left; width: 25%',
    components: [{
      kind: 'OB.UI.Thumbnail'
    }]
  }, {
    style: 'float: left; width: 75%;',
    components: [{
      name: 'identifier',
      style: 'padding-left: 5px;'
    }, {
      style: 'clear:both;'
    }]
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.addClass('btnselect-browse');
    this.$.identifier.setContent(this.model.get('_identifier'));
  }
});

(function() {



  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderCategory = OB.COMP.SelectButton.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        style: 'float: left; width: 25%'
      },
      content: [{
        id: 'modelthumbnail',
        view: OB.UTIL.Thumbnail
      }]
    }, {
      tag: 'div',
      attributes: {
        style: 'float: left; width: 75%;'
      },
      content: [{
        id: 'modelidentifier',
        tag: 'div',
        attributes: {
          style: 'padding-left: 5px;'
        }
      }, {
        tag: 'div',
        attributes: {
          style: 'clear: both;'
        }
      }]
    }],
    render: function() {
      this.$el.addClass('btnselect-browse');
      this.modelthumbnail.img = this.model.get('img');
      this.modelthumbnail.render();
      this.modelidentifier.text(this.model.get('_identifier'));
      return this;
    }
  });
}());