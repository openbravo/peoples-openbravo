/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalCategoryTree',
  classes: 'obUiModalCategoryTree',
  events: {
    onHideThisPopup: '',
    onSelectCategoryTreeItem: ''
  },
  hideCloseButton: true,
  body: {
    kind: 'OB.UI.ListCategories',
    classes: 'obUiModalCategoryTree-body',
    showBestSellers: true,
    showAllCategories: true,
    tableName: 'searchCategoryTable'
  },

  initComponents: function() {
    this.inherited(arguments);
    this.$.body.$.listCategories.$[
      this.$.body.$.listCategories.tableName
    ].$.theader.hide();
    this.$.header.hide();
  },

  executeOnShow: function() {
    var me = this;
    this.startShowing = true;
    this.$.body.$.listCategories.loadCategories(function() {
      me.$.body.$.listCategories.addClass(
        'obUiModalCategoryTree-body-listCategory'
      );
      var showOnlyReal = me.args.showOnlyReal || false;
      _.each(
        me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName].$
          .tbody.children,
        function(item) {
          if (item.renderline.model.get('realCategory') === 'N') {
            item.setShowing(!showOnlyReal);
          }
        },
        me
      );
      if (
        me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName]
          .selected
      ) {
        me.$.body.$.listCategories.categoryCollapseSibling('0');
        me.$.body.$.listCategories.categoryExpandSelected();
        me.$.body.$.listCategories.categoryExpandCollapse(this, {
          categoryId:
            me.$.body.$.listCategories.$[me.$.body.$.listCategories.tableName]
              .selected.renderline.model.id,
          expand: true
        });
        if (me.args.selectCategory) {
          setTimeout(function() {
            me.$.body.$.listCategories.categoryAdjustScroll(
              me.args.selectCategory,
              5,
              0
            );
            var category = me.$.body.$.listCategories.categories.get(
              me.args.selectCategory
            );
            if (category) {
              me.$.body.$.listCategories.$[
                me.$.body.$.listCategories.tableName
              ].setSelectedModels([category], true);
            }
          }, 200);
        }
      }
      me.startShowing = false;
    });
  },

  init: function() {
    this.$.body.$.listCategories.categories.on(
      'selected',
      function(category) {
        if (category && !this.startShowing) {
          if (
            this.args &&
            this.args.notSelectSummary &&
            category.get('issummary')
          ) {
            this.$.body.$.listCategories.categoryExpandCollapse(this, {
              categoryId: category.get('id'),
              expand: category.get('treeNode') === 'COLLAPSED'
            });
            return;
          }
          var me = this;
          this.loadSubTreeIds(category.id, "'" + category.id + "'", function(
            childrenIds
          ) {
            me.doSelectCategoryTreeItem({
              category: category,
              children: childrenIds,
              origin: me.args ? me.args.origin : null
            });
            me.doHideThisPopup();
          });
        }
      },
      this
    );
  },

  loadSubTreeIds: function(parentCategoryId, childrenIds, callbackTreeIds) {
    var me = this,
      treeProcessed = [];

    function getSubTreeIds(models, index, callback) {
      if (models.length <= index) {
        if (callback === callbackTreeIds) {
          var pending = _.find(treeProcessed, function(t) {
            return !t.processed;
          });
          if (!pending) {
            callback(childrenIds);
          }
        } else {
          callback();
        }
        return;
      }

      var categoryId = models[index].get('id'),
        processed = {
          category: categoryId,
          processed: models[index].get('childs') === 0
        };
      childrenIds += ",'" + categoryId + "'";
      treeProcessed.push(processed);
      if (models[index].get('childs') > 0) {
        me.$.body.$.listCategories.loadCategoryTreeLevel(
          models[index].get('id'),
          function(categories) {
            if (categories.models !== undefined) {
              categories = categories.models;
            }
            getSubTreeIds(categories, 0, function() {
              processed.processed = true;
              getSubTreeIds(models, index + 1, callback);
            });
          }
        );
      } else {
        getSubTreeIds(models, index + 1, callback);
      }
    }

    this.$.body.$.listCategories.loadCategoryTreeLevel(
      parentCategoryId,
      function(categories) {
        if (categories.models !== undefined || categories.length !== 0) {
          getSubTreeIds(categories, 0, callbackTreeIds);
        } else {
          callbackTreeIds(childrenIds);
        }
      }
    );
  }
});
