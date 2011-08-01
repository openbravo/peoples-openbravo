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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s): ___________
 ************************************************************************
 */

// = Defines the OBPersonalizationTree =
// The type of the tree shown on the left for ordering and grouping
// of fields.
isc.ClassFactory.defineClass('OBPersonalizationTreeGrid', isc.TreeGrid);

isc.OBPersonalizationTreeGrid.addProperties({
  showHeader: false,
  canEdit: true,
  canReorderRecords: true,
  canAcceptDroppedRecords: true,
  leaveScrollbarGap: false,
  showCellContextMenus: true,

  bodyStyleName: 'OBGridBody',
  baseStyle: 'OBPersonalizationTreeGridCell',

  showOpener: false,
  // eventhough showOpener is false, still space is taken for an opener
  // icon, set to a small number, should be > 0 (otherwise it it not used)
  openerIconSize: 2,
  nodeIcon: OB.Styles.Personalization.Icons.field,
  folderIcon: OB.Styles.Personalization.Icons.fieldGroup,
  showDropIcons: true,
  showOpenIcons: true,
  dropIconSuffix: 'open',
  closedIconSuffix: 'closed',
  openIconSuffix: 'open',
  
  width: '100%',
  indentSize: 15,
  
  showHeaderContextMenu: false,
  fields: [
    {name: 'title', canHover: true, showHover: true, 
      treeField: true,
      showTitle: false, type: 'text', width: '100%', canEdit: false}
//    {name: 'colSpan', title: OB.I18N.getLabel('OBUIAPP_Personalization_Colspan'), type: 'number', editorType: 'TextItem', keyPressFilterNumeric: '[0-9]'}, 
//    {name: 'rowSpan', title: OB.I18N.getLabel('OBUIAPP_Personalization_Rowspan'),  type: 'number', editorType: 'TextItem', keyPressFilterNumeric: '[0-9]'}, 
//    {name: 'startRow', title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow'), type: 'boolean'}, 
//    {name: 'hiddenInForm', title: OB.I18N.getLabel('OBUIAPP_Personalization_Hidden'), type: 'boolean'}
    ],
    
  initWidget: function() {
    var i = 0, me = this, changedFunction = function() {
      me.customizeForm.changed();
    };

    this.fields[0].hoverHTML = function(record, value) {
      return me.customizeForm.getHoverHTML(me.fields[0].name, value);
    };
    
    for (i = 0; i < this.fields.length; i++) {
      this.fields[i].changed = changedFunction;
    }
    
   this.data = isc.Tree.create({
     modelType: 'parent',
     idField: 'name',
     parentIdField: 'parentName',
     data: this.fieldData,
     dataChanged: function() {
       me.customizeForm.changed();
     }
   });
   
//   this.data.getRoot().canAcceptDrop = false;
   
//   this.data.openAll();
   
   this.Super('initWidget', arguments);
  },
  
  folderClick: function (viewer, folder, recordNum) {
    if (this.data.isOpen(folder)) {
      this.closeFolder(folder);
    } else {
      this.openFolder(folder);
    }
  },
  
  // overridden to copy a node when it is dragged to the status bar
  // from the outside, in all other cases assume standard behavior
  folderDrop : function (nodes, folder, index, sourceWidget, callback) {
    var i, oldNode, oldValue, newCallback;
    
    // if the statusbar group has nothing to do with this all
    // then ignore it
    if (folder.name !== OB.Personalization.STATUSBAR_GROUPNAME) {
      
      // check if the nodes are valid
      for (i = 0; i < nodes.length; i++) {
        if (nodes[i].parentName === OB.Personalization.STATUSBAR_GROUPNAME) {
          // can not move status bar fields out of the status bar group
          return;
        }
      }
      
      this.transferNodes(nodes, folder, index, sourceWidget, newCallback);
      return;
    }
    
    if (!nodes) {
      return;
    }
    
    // copy the ones which are from the outside
    for (i = 0; i < nodes.length; i++) {
      if (nodes[i].parentName !== OB.Personalization.STATUSBAR_GROUPNAME) {
        oldNode = nodes[i];
        nodes[i] = {
            name: oldNode.name + '_statusbar',
            title: oldNode.title,
            originalName: oldNode.name,
            isDynamicStatusBarField: true
        };
      }
    }
    
    this.transferNodes(nodes, folder, index, sourceWidget, newCallback);
  },
  
  // show hidden items in a different style
  getBaseStyle: function (record, rowNum, colNum) {
    if (record.hiddenInForm) {
      return this.baseStyle + 'Hidden';
    }
    return this.baseStyle;
  },
  
  // no context menu on folders
  folderContextClick: function(me, record, recordNum) {
    return false;
  },
  
  // overridden to create context menu items specific 
  // for the clicked record
  cellContextClick: function(record, rowNum, colNum) {
    // select when right clicking, this has some side effects
    // focus and menus appearing/disappearing
    //    this.deselectAllRecords();
    //    this.selectRecord(record);
    
    // create the context items for the clicked record
    this.cellContextItems = this.createCellContextItems(record);
    // continue with normal behavior
    return true;
  },
  
  // overridden to be able to do specific actions in the tree
  createCellContextItems: function(record){
    var menuItems = [], updatePropertyFunction, me = this,
      customizeForm = this.customizeForm;
    
    updatePropertyFunction = function(record, property, value) {
      record[property] = value;
      
      // make sure only one record has first focus
      if (record.firstFocus) {
        allNodes = customizeForm.fieldsTreeGrid.data.getAllNodes();
        for (i = 0; i < allNodes.length; i++) {
          if (allNodes[i].firstFocus) {
            allNodes[i].firstFocus = false;
          }
        }
        record.firstFocus = true;
      }

      // items may have been hidden, which changes their colour
      customizeForm.fieldsTreeGrid.markForRedraw();

      // this will reset everything
      customizeForm.changed();
    };
    
    if (record.isStaticStatusBarField) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Hidden'),
        checked: record.hiddenInForm,
        click: function() {
          updatePropertyFunction(record, 'hiddenInForm', !record.hiddenInForm);
        }        
      });
    } else if (record.isDynamicStatusBarField) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_RemoveFromStatusBar'),
        click: function() {
          me.removeData(record);
        }        
      });
    } else {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow'),
        checked: record.startRow,
        click: function() {
          updatePropertyFunction(record, 'startRow', !record.startRow);
        }        
      });
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Hidden'),
        checked: record.hiddenInForm,
        click: function() {
          updatePropertyFunction(record, 'hiddenInForm', !record.hiddenInForm);
        }        
      });
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_FirstFocus'),
        checked: record.firstFocus,
        click: function() {
          updatePropertyFunction(record, 'firstFocus', !record.firstFocus);
        }        
      });
    }

    return menuItems;
  },
  
  // for group items checkboxes are displayed, prevent that
  getValueIcon: function (field, value, record, rowNum) {
    if (record[this.recordEditProperty] === false) {
      return null;
    }
    return this.Super('getValueIcon', arguments);
  }
});
