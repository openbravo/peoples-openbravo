/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBLinkedItems =
//
// Represents the linked items section shown in the bottom of the form.
// Note is not shown for new records.
//
isc.ClassFactory.defineClass('OBLinkedItemSectionItem', isc.OBSectionItem);

isc.OBLinkedItemSectionItem.addProperties({
  // as the name is always the same there should be at most 
  // one linked item section per form
  name: '_linkedItem_',
  
  width: '100%',
  height: '100%',
  overflow: 'hidden',
  canFocus: true,
  
  // don't expand as a default
  sectionExpanded: false,
  
  prompt: OB.I18N.getLabel('OBUIAPP_LinkedItemsPrompt'),
  
  canvasItem: null,
  
  // note formitems don't have an initWidget but an init method
  init: function(){
  
    // override the one passed in
    this.defaultValue = OB.I18N.getLabel('OBUIAPP_LinkedItemsTitle');
    this.sectionExpanded = false;
    
    // tell the form who we are
    this.form.linkedItemSection = this;
    
    return this.Super('init', arguments);
  },
  
  getLinkedItemPart: function(){
    if (!this.canvasItem) {
      this.canvasItem = this.form.getField(this.itemIds[0]);
    }
    return this.canvasItem.canvas;
  },
  
  setRecordInfo: function(entity, id){
    this.getLinkedItemPart().setRecordInfo(entity, id);
  },
  
  collapseSection: function(){
    var ret = this.Super('collapseSection', arguments);
    this.getLinkedItemPart().setExpanded(false);
    return ret;
  },
  
  expandSection: function(){
    var ret = this.Super('expandSection', arguments);
    this.getLinkedItemPart().setExpanded(true);
    
    // after doing the linked item section check if this 
    // can replace the scrollto below
    //    this.canvasItem.focusInItem();
    
    // NOTE: if the layout structure changes then this needs to be 
    // changed probably to see where the scrollbar is to scroll
    
    // call with a small delay to let the item be expanded
    // form.parentElement is the one holding the scrollbar apparently
    this.form.parentElement.delayCall('scrollToBottom', null, 200);
    
    return ret;
  },
  
  hide: function(){
    this.collapseSection();
    return this.Super('hide', arguments);
  }
});

isc.ClassFactory.defineClass('OBLinkedItemLayout', isc.VLayout);

isc.OBLinkedItemLayout.addProperties({

  // set to true when the content has been created at first expand
  isInitialized: false,
  
  width: '100%',
  height: '100%',
  
  initWidget: function(){
    var ret = this.Super('initWidget', arguments);
    
    // just for demo purposes have some content
    // Valery: instead of the label the grid component showing linked 
    // items should be created, note that the linked items should not 
    // be read until the expand actually takes place (see below)
    this.label = isc.Label.create({
      width: '100%',
      height: '100%',
      contents: 'Implement me'
    });
    this.addMember(this.label);
    
    return ret;
  },
  
  // never disable this item
  isDisabled: function(){
    return false;
  },
  
  getForm: function(){
    return this.canvasItem.form;
  },
  
  // is called when a new record is loaded in the form
  // in this method the linked item section should be cleared
  // but not reload its content, that's done when the section
  // gets expanded
  setRecordInfo: function(entity, id){
    this.entity = entity;
    // use recordId instead of id, as id is often used to keep
    // html ids
    this.recordId = id;
    this.isInitialized = false;
  },
  
  // is called when the section expands/collapse
  // the linked items should not be loaded before the section actually expands
  setExpanded: function(expanded){
    if (expanded && !this.isInitialized) {
      // TODO: when expanding the complete linked items section has to be shown
      
      // set our demo label
      // for the linked items: load the linked items here
      this.label.setContents('Showing record with id ' + this.recordId + ' and entity ' + this.entity);
      
      // this part should stay also for linked items      
      this.isInitialized = true;
    }
  },
  
  // ensure that the view gets activated
  focusChanged: function(){
    var view = this.getForm().view;
    if (view && view.setAsActiveView) {
      view.setAsActiveView();
    }
    return this.Super('focusChanged', arguments);
  }
});

isc.ClassFactory.defineClass('OBLinkedItemCanvasItem', isc.CanvasItem);

isc.OBLinkedItemCanvasItem.addProperties({

  width: '100%',
  height: '100%',
  
  showTitle: false,
  overflow: 'auto',
  // note that explicitly setting the canvas gives an error as not 
  // all props are set correctly on the canvas (for example the 
  // pointer back to this item: canvasItem
  // for setting more properties use canvasProperties, etc. see 
  // the docs
  canvasConstructor: 'OBLinkedItemLayout',
  
  // never disable this one
  isDisabled: function(){
    return false;
  }
  
});
