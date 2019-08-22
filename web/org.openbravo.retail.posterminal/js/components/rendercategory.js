/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.RenderCategory',
  kind: 'OB.UI.listItemButton',
  classes: 'obUiRenderCategory',
  components: [
    {
      classes: 'obUiRenderCategory-thumbnailContainer',
      components: [
        {
          classes: 'obUiRenderCategory-thumbnailContainer-thumbnail',
          kind: 'OB.UI.Thumbnail',
          name: 'thumbnail'
        }
      ]
    },
    {
      classes: 'obUiRenderCategory-identifierContainer',
      components: [
        {
          name: 'identifier',
          classes: 'obUiRenderCategory-identifierContainer-identifier'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.thumbnail.setImg(this.model.get('img'));

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_RenderCategory',
      {
        context: this
      },
      function(args) {
        return this;
      }
    );
  }
});

enyo.kind({
  name: 'OB.UI.RenderCategoryExpand',
  kind: 'Image',
  classes: 'obUiRenderCategoryExpand',
  src: '../org.openbravo.retail.posterminal/img/iconExpand.png',
  sizing: 'cover'
});

enyo.kind({
  name: 'OB.UI.RenderCategoryCollapse',
  kind: 'Image',
  classes: 'obUiRenderCategoryCollapse',
  src: '../org.openbravo.retail.posterminal/img/iconCollapse.png',
  sizing: 'cover',
  showing: false
});

enyo.kind({
  name: 'OB.UI.RenderCategoryTree',
  kind: 'OB.UI.listItemButton',
  classes: 'obUiRenderCategoryTree',
  handlers: {
    onkeydown: 'keydownHandler'
  },
  keydownHandler: function(inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13) {
      // Handle ENTER key in list item
      this.tap();
      return true;
    }
    if (keyCode === 32) {
      // Handle SPACE key in list item
      if (this.model.get('issummary')) {
        this.categoryExpandCollapse();
      } else {
        this.tap();
      }
      return true;
    }
    return false;
  },
  categoryExpandCollapse: function() {
    this.bubble('onCategoryExpandCollapse', {
      categoryId: this.model.get('id'),
      expand: this.$.expand.getShowing()
    });
  },
  isExpanded: function() {
    return !(this.$.expand && this.$.expand.getShowing() === true);
  },
  components: [
    {
      classes: 'obUiRenderCategoryTree-container1',
      components: [
        {
          classes: 'obUiRenderCategoryTree-container1-identifier',
          name: 'identifier'
        }
      ]
    },
    {
      classes: 'obUiRenderCategoryTree-container2',
      components: [
        {
          name: 'expandCollapse',
          classes: 'obUiRenderCategoryTree-container2-expandCollapse',
          components: [
            {
              classes: 'obUiRenderCategoryTree-expandCollapse-expand',
              kind: 'OB.UI.RenderCategoryExpand',
              name: 'expand'
            },
            {
              classes: 'obUiRenderCategoryTree-expandCollapse-collapse',
              kind: 'OB.UI.RenderCategoryCollapse',
              name: 'collapse'
            }
          ],
          tap: function() {
            this.owner.categoryExpandCollapse();
            return true;
          }
        }
      ]
    }
  ],

  initComponents: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.identifier.setStyle(
      'padding-left: ' +
        14 * this.model.get('level') +
        'px; ' +
        (this.model.id === '__all__' ? 'font-weight: bold; ' : '')
    );
    this.$.expandCollapse.setShowing(this.model.get('issummary'));
    this.$.expand.setShowing(this.model.get('treeNode') !== 'EXPANDED');
    this.$.collapse.setShowing(this.model.get('treeNode') === 'EXPANDED');
    var showOnlyReal =
      (this.owner.owner.owner.owner.owner.owner.args &&
        this.owner.owner.owner.owner.owner.owner.args.showOnlyReal) ||
      false;
    this.owner.setShowing(
      this.model.get('realCategory') === 'N' && showOnlyReal
        ? false
        : this.model.get('display')
    );
  }
});
