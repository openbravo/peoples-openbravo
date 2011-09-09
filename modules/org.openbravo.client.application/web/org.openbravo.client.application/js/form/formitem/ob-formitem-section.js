/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBSectionItem ==
// Form sections, used for notes, more information, attachment etc.

isc.ClassFactory.defineClass('OBSectionItem', SectionItem);

isc.OBSectionItem.addProperties({
  sectionExpanded: false,
  
  // revisit when/if we allow disabling of section items
  // visual state of disabled or non-disabled stays the same now
  showDisabled: false,

  // some defaults, note if this changes then also the 
  // field generation logic needs to be checked
  colSpan: 4, 
  startRow: true, 
  endRow: true,
  
  canTabToHeader: true,
  
  alwaysTakeSpace: false,

  setSectionItemInContent: function(form) {
    var i = 0, length = this.itemIds.length;
    for (i = 0; i < length; i++) {
      if (form.getItem(this.itemIds[i])) {
        form.getItem(this.itemIds[i]).section = this;
      }
    }
  },
  
  // never disable a section item
  isDisabled: function(){
    return false;
  },

  // Update the property alwaysTakeSpace when collapsing/expanding a section
  // Note: The hidden fields are not updated, they always have alwaysTakeSpace to false
  updateAlwaysTakeSpace: function(flag) {
    var i, f = this.form, item, length = this.itemIds.length;

    for(i = 0; i < length; i++) {
      item = f.getItem(this.itemIds[i]);
      if (item) {
        // note different cases can occur, these properties may be set, maybe
        // undefined, false or true, undefined is not always false, as a field
        // is not always processed through all logic in the system which sets
        // these properties
        if (item.hiddenInForm) {
          continue;
        }
        if (item.visible || item.displayed !== false) {
          item.alwaysTakeSpace = flag;
        }
      }
    }
  },
  
  collapseSection: function(preventFocusChange) {
    // when collapsing set the focus to the header
    this.updateAlwaysTakeSpace(false);
    if (!preventFocusChange && this.isDrawn() && this.isVisible()) {
      this.form.setFocusItem(this);
    }
    var ret = this.Super('collapseSection', arguments);
    return ret;
  },
  
  expandSection: function() {
    var enabled = this.isDrawn() && this.isVisible();

    this.updateAlwaysTakeSpace(true);

    if (enabled && this.form.getFocusItem()) {
      this.form.getFocusItem().blurItem();
    }

    var ret = this.Super('expandSection', arguments);
    
    if (enabled && !this.form._preventFocusChanges) {
      // when expanding set the focus to the first focusable item     
      // set focus late to give the section time to draw and let
      // other items to loose focus/redraw
      this.delayCall('setNewFocusItemExpanding', [], 100);
      
      // NOTE: if the layout structure changes then this needs to be 
      // changed probably to see where the scrollbar is to scroll
      // the parentElement is not set initially when drawing
      if (this.form.parentElement) {
        // scroll after things have been expanded
        this.form.parentElement.delayCall('scrollTo', [null, this.getTop()], 100);    
      }
    }

    return ret;
  },

  setNewFocusItemExpanding: function(){
    var newFocusItem = null, i, length = this.itemIds.length;
    for (i = 0; i < length; i++) {
      var itemName = this.itemIds[i], item = this.form.getItem(itemName);
      // isFocusable is a method added in ob-smartclient.js
      if (item.isFocusable()) {
        newFocusItem = item;
        break;
      }
    }
    if (!newFocusItem && this.handleFocus && this.handleFocus()) {
      return;
    } else if (!newFocusItem) {
      this.focusInItem();
    } else {
      newFocusItem.focusInItem();
    }
  },

  showIf: function(item, value, form, values) {
    var i, field, length;

    if(!this.itemIds) {
      return false;
    }
    
    length = this.itemIds.length;
    
    for (i = 0; i < length; i++) {
      field = form.getItem(this.itemIds[i]);

      if(!field || (item.visible === false || item.displayed === false || item.hiddenInForm === true)) {
        continue;
      }

      if (field.obShowIf) {
        if(field.obShowIf(field, value, form)) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }
});
