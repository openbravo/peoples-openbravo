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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
isc.ClassFactory.defineClass('OBToolbar', isc.ToolStrip);

isc.OBToolbar.addClassProperties({
  TYPE_SAVE: 'save',
  TYPE_NEW: 'newRow',
  TYPE_DELETE: 'eliminate',
  TYPE_UNDO: 'undo',
  TYPE_REFRESH: 'refresh',
  
  SAVE_BUTTON_PROPERTIES: {
    action: function(){
      this.view.saveRow();
    },
    disabled: true,
    buttonType: 'save',
    prompt: OB.I18N.getLabel('OBUIAPP_SaveRow')
  },
  NEW_BUTTON_PROPERTIES: {
    action: function(){
      this.view.newRow();
    },
    buttonType: 'newRow',
    prompt: OB.I18N.getLabel('OBUIAPP_NewRow')
  },
  DELETE_BUTTON_PROPERTIES: {
    action: function(){
      this.view.deleteRow();
    },
    disabled: true,
    buttonType: 'eliminate',
    prompt: OB.I18N.getLabel('OBUIAPP_DeleteRow')
  },
  REFRESH_BUTTON_PROPERTIES: {
    action: function(){
      this.view.refresh();
    },
    disabled: false,
    buttonType: 'refresh',
    prompt: OB.I18N.getLabel('OBUIAPP_RefreshData')
  },
  UNDO_BUTTON_PROPERTIES: {
    action: function(){
      this.view.undo();
    },
    disabled: true,
    buttonType: 'undo',
    prompt: OB.I18N.getLabel('OBUIAPP_UndoRow')
  }
});

// = OBToolbar =
//
// The OBToolbar is the toolbar to perform common actions within a form.
//
isc.OBToolbar.addProperties({
  initWidget: function(){
    this.Super('initWidget', arguments);
    this.members = null;
    
    var newMembers = [], i = 0, j = 0;
    
    newMembers[j] = isc.HLayout.create({
      width: this.leftMargin,
      height: 1
    });
    j++;
    
    if (this.leftMembers) {
      for (i = 0; i < this.leftMembers.length; i++) {
        newMembers[j] = this.leftMembers[i];
        OB.TestRegistry.register('org.openbravo.client.application.toolbar.button.' + this.leftMembers[i].buttonType + '.' + this.view.tabId, this.leftMembers[i]);
        
        newMembers[j].toolBar = this;
        newMembers[j].view = this.view;
        j++;
        newMembers[j] = isc.HLayout.create({
          width: this.leftMembersMargin,
          height: 1
        });
        j++;
      }
    }
    
    newMembers[j] = isc.HLayout.create({
      width: '100%',
      height: 1
    });
    j++;
    newMembers[j] = isc.HLayout.create({
      width: 40,
      height: 1
    });
    j++;
    
    if (this.rightMembers) {
      for (i = 0; i < this.rightMembers.length; i++) {
        newMembers[j] = this.rightMembers[i];
        OB.TestRegistry.register('org.openbravo.client.application.toolbar.button.' + this.rightMembers[i].property + '.' + this.view.tabId, this.rightMembers[i]);
        newMembers[j].toolBar = this;
        newMembers[j].view = this.view;
        j++;
        newMembers[j] = isc.HLayout.create({
          width: this.rightMembersMargin,
          height: 1
        });
        j++;
      }
    }
    
    newMembers[j] = isc.HLayout.create({
      width: this.rightMargin,
      height: 1
    });
    j++;
    
    this.Super('addMembers', [newMembers]);
  },
  
  // ** {{{ getLeftMember(member) }}} **
  //
  // It works just for left side members.
  // Given a numerical index or a left member type, return a pointer to the appropriate left member.
  // If passed a left member Canvas, just returns it.
  // If not found, returns undefined
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the required left member.
  //
  // Returns:  type: Canvas - left member widget.
  getLeftMember: function(member){
    var i = 0;
    if (typeof member === 'number') {
      if (member >= 0 && member < this.leftMembers.length) {
        return this.leftMembers[member];
      }
    } else if (typeof member === 'string') {
      for (i = 0; i < this.leftMembers.length; i++) {
        if (this.leftMembers[i].buttonType === member) {
          return this.leftMembers[i];
        }
      }
    } else if (typeof member === 'object') {
      for (i = 0; i < this.leftMembers.length; i++) {
        if (this.leftMembers[i] === member) {
          return this.leftMembers[i];
        }
      }
    }
    return;
  },
  
  // ** {{{ getLeftMembers() }}} **
  //
  // It works just for left side members.
  // Get the Array of left members.
  // NOTE: the returned array should not be modified.
  //
  // Returns: type: Array - the Array of left members.
  getLeftMembers: function(){
    return this.leftMembers;
  },
  
  // ** {{{ getLeftMember(attribute, value) }}} **
  //
  // It works just for left side members.
  // Given a attribute an its value, return an array of matching left members.
  // If no matches, returns an empty array.
  //
  // Parameters:
  // * {{{attribute}}} type: String - attribute for search.
  // * {{{value}}} type: String | Number | Canvas - desired value of the attribute.
  //
  // Returns: type: Array - the Array of matching left members.
  getLeftMembersByAttribute: function(attribute, value){
    var members = [], i = 0;
    for (i = 0; i < this.leftMembers.length; i++) {
      if (this.leftMembers[i][attribute] === value) {
        members.push(this.leftMembers[i]);
      }
    }
    return members;
  },
  
  // ** {{{ getLeftMemberNumber(member) }}} **
  //
  // It works just for left side members.
  // Given a left member Canvas, return its position.
  // If no matches, returns -1.
  //
  // Parameters:
  // * {{{member}}} type: Canvas - left member Canvas to obtain its position.
  //
  // Returns: type: Number - the left member Canvas position (starting from 0).
  getLeftMemberNumber: function(member){
    var i = 0;
    for (i = 0; i < this.leftMembers.length; i++) {
      if (this.leftMembers[i] === member) {
        return i;
      }
    }
    return -1;
  },
  
  // ** {{{ removeLeftMembers(members) }}} **
  //
  // It works just for left side members.
  // Removes the specified left members from the layout.
  //
  // Parameters:
  // * {{{members}}} type: Array | Canvas - array of left members to be removed, or reference to single left member.
  removeLeftMembers: function(members){
    var oldMembersSorted = [], oldArray = [], position = 0, i = 0, sortFunc = function(a, b){
      return (a - b);
    };
    if (!(typeof members.length === 'number' && !(members.propertyIsEnumerable('length')) && typeof members.splice === 'function')) {
      members = [members];
    }
    for (i = 0; i < members.length; i++) { /* Clean-up of the given input and sort */
      if (typeof members[i] !== 'number') {
        members[i] = this.getLeftMemberNumber(members[i]);
      }
      if (members[i] <= this.leftMembers.length && members[i] !== -1) {
        oldMembersSorted[oldMembersSorted.length] = members[i];
      }
      oldMembersSorted = oldMembersSorted.sort(sortFunc);
    }
    for (i = 0; i < oldMembersSorted.length; i++) { /* Generate an array to determine which elements visually will be removed */
      position = oldMembersSorted[i];
      position = position * 2;
      position = position + 1;
      oldArray.push(position, position + 1);
    }
    oldMembersSorted = oldMembersSorted.reverse();
    for (i = 0; i < oldMembersSorted.length; i++) { /* Update the 'leftMembers' array */
      this.leftMembers.splice(oldMembersSorted[i], 1);
    }
    this.removeMembers(oldArray); /* Remove visually the desired elements */
  },
  
  // ** {{{ removeAllLeftMembers() }}} **
  //
  // It works just for left side members.
  // Removes all left members from the layout.
  //
  removeAllLeftMembers: function(){
    var membersNumArray = [], i = 0;
    for (i = 0; i < this.leftMembers.length; i++) {
      membersNumArray.push(i);
    }
    this.removeLeftMembers(membersNumArray);
  },
  
  // ** {{{ addLeftMembers(newMembers, position) }}} **
  //
  // It works just for left side members.
  // Add one or more canvases to the left side of the toolbar, optionally at specific position.
  //
  // Parameters:
  // * {{{newMembers}}} type: Array || Object - array of canvases to be added, or reference to single canvas.
  // * {{{position (optional)}}} type: Number - position to add newMembers; if omitted newMembers will be added at the last position.
  addLeftMembers: function(newMembers, position){
    var i = 0;
    if (!(typeof newMembers.length === 'number' && !(newMembers.propertyIsEnumerable('length')) && typeof newMembers.splice === 'function')) {
      newMembers = [newMembers];
    }
    if (position > this.leftMembers.length || typeof position === 'undefined') {
      position = this.leftMembers.length;
    }
    for (i = 0; i < newMembers.length; i++) {
      this.leftMembers.splice(position + i, 0, newMembers[i]);
    }
    position = position * 2;
    position = position + 1;
    for (i = 0; i < newMembers.length; i++) {
      this.Super('addMembers', [newMembers[i], position]);
      position = position + 1;
      this.Super('addMembers', [isc.HLayout.create({
        width: this.leftMembersMargin,
        height: 1
      }), position]);
      position = position + 1;
    }
  },
  
  // ** {{{ setLeftMembers(newMembers, position) }}} **
  //
  // It works just for left side members.
  // Set/Display one or more canvases to the left side of the toolbar, optionally at specific position; if any exists, it will be deleted.
  //
  // Parameters:
  // * {{{newMembers}}} type: Array || Object - array of canvases to be displayed, or reference to single canvas.
  setLeftMembers: function(newMembers){
    this.removeAllLeftMembers();
    this.addLeftMembers(newMembers);
  },
  
  // ** {{{ setLeftMemberDisabled(member, state) }}} **
  //
  // It works just for left side members.
  // Set the disabled state of this left member.
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the left member to perform the action.
  // * {{{state}}} type: Boolean - new disabled state of this object; pass true to disable the left member.
  setLeftMemberDisabled: function(member, state){
    member = this.getLeftMember(member);
    if (member) {
      member.setDisabled(state);
    }
    return;
  },
  
  // ** {{{ setLeftMemberSelected(member, state) }}} **
  //
  // It works just for left side members.
  // Set the selected state of this left member.
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the left member to perform the action.
  // * {{{state}}} type: Boolean - new disabled state of this object; pass true to select the left member.
  setLeftMemberSelected: function(member, state){
    member = this.getLeftMember(member);
    if (member) {
      member.setSelected(state);
    }
    return;
  },
  
  
  // ** {{{ getRightMember(member) }}} **
  //
  // It works just for right side members.
  // Given a numerical index or a right member ID, return a pointer to the appropriate right member.
  // If passed a right member Canvas, just returns it.
  // If not found, returns undefined.
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the required right member.
  //
  // Returns: type: Canvas - right member widget.
  getRightMember: function(member){
    var i = 0;
    if (typeof member === 'number') {
      if (member >= 0 && member < this.rightMembers.length) {
        return this.rightMembers[member];
      }
    } else if (typeof member === 'string') {
      for (i = 0; i < this.rightMembers.length; i++) {
        if (this.rightMembers[i].ID === member) {
          return this.rightMembers[i];
        }
      }
    } else if (typeof member === 'object') {
      for (i = 0; i < this.rightMembers.length; i++) {
        if (this.rightMembers[i] === member) {
          return this.rightMembers[i];
        }
      }
    }
    return;
  },
  
  // ** {{{ getRightMembers() }}} **
  //
  // It works just for right side members.
  // Get the Array of right members.
  // NOTE: the returned array should not be modified.
  //
  // Returns: type: Array - the Array of right members.
  getRightMembers: function(){
    return this.rightMembers;
  },
  
  // ** {{{ getRightMembersByAttribute(attribute, value) }}} **
  //
  // It works just for right side members.
  // Given a attribute an its value, return an array of matching right members.
  // If no matches, returns an empty array.
  //
  // Parameters:
  // * {{{attribute}}} type: String - attribute for search.
  // * {{{value}}} type: String | Number | Canvas - desired value of the attribute.
  //
  // Returns: type: Array - the Array of matching right members.
  getRightMembersByAttribute: function(attribute, value){
    var members = [], i = 0;
    for (i = 0; i < this.rightMembers.length; i++) {
      if (this.rightMembers[i][attribute] === value) {
        members.push(this.rightMembers[i]);
      }
    }
    return members;
  },
  
  // ** {{{ getRightMemberNumber(member) }}} **
  //
  // It works just for right side members.
  // Given a right member Canvas, return its position.
  // If no matches, returns -1.
  //
  // Parameters:
  // * {{{member}}} type: Canvas - right member Canvas to obtain its position.
  //
  // Returns: type: Number - the right member Canvas position (starting from 0).
  getRightMemberNumber: function(member){
    var i = 0;
    for (i = 0; i < this.rightMembers.length; i++) {
      if (this.rightMembers[i] === member) {
        return i;
      }
    }
    return -1;
  },
  
  // ** {{{ removeRightMembers(members) }}} **
  //
  // It works just for right side members.
  // Removes the specified right members from the layout.
  //
  // Parameters:
  // * {{{members}}} type: Array | Canvas - array of right members to be removed, or reference to single right member.
  removeRightMembers: function(members){
    var oldMembersSorted = [], oldArray = [], position = 0, i = 0, sortFunc = function(a, b){
      return (a - b);
    };
    if (!(typeof members.length === 'number' && !(members.propertyIsEnumerable('length')) && typeof members.splice === 'function')) {
      members = [members];
    }
    for (i = 0; i < members.length; i++) { /* Clean-up of the given input and sort */
      if (typeof members[i] !== 'number') {
        members[i] = this.getRightMemberNumber(members[i]);
      }
      if (members[i] <= this.rightMembers.length && members[i] !== -1) {
        oldMembersSorted[oldMembersSorted.length] = members[i];
      }
      oldMembersSorted = oldMembersSorted.sort(sortFunc);
    }
    for (i = 0; i < oldMembersSorted.length; i++) { /* Generate an array to determine which elements visually will be removed */
      position = oldMembersSorted[i];
      position = position * 2;
      position = position + 3;
      position = position + this.leftMembers.length * 2;
      oldArray.push(position, position + 1);
    }
    oldMembersSorted = oldMembersSorted.reverse();
    for (i = 0; i < oldMembersSorted.length; i++) { /* Update the 'rightMembers' array */
      this.rightMembers.splice(oldMembersSorted[i], 1);
    }
    this.removeMembers(oldArray); /* Remove visually the desired elements */
  },
  
  // ** {{{ removeAllRightMembers() }}} **
  //
  // It works just for right side members.
  // Removes all right members from the layout.
  //
  removeAllRightMembers: function(){
    var membersNumArray = [], i = 0;
    for (i = 0; i < this.rightMembers.length; i++) {
      membersNumArray.push(i);
    }
    this.removeRightMembers(membersNumArray);
  },
  
  // ** {{{ addRightMembers(newMembers, position) }}} **
  //
  // It works just for right side members.
  // Add one or more canvases to the right side of the toolbar, optionally at specific position.
  //
  // Parameters:
  // * {{{newMembers}}} type: Array || Object - array of canvases to be added, or reference to single canvas.
  // * {{{position (optional)}}} type: Number - position to add newMembers; if omitted newMembers will be added at the last position.
  addRightMembers: function(newMembers, position){
    var i = 0;
    if (!(typeof newMembers.length === 'number' && !(newMembers.propertyIsEnumerable('length')) && typeof newMembers.splice === 'function')) {
      newMembers = [newMembers];
    }
    if (position > this.rightMembers.length || typeof position === 'undefined') {
      position = this.rightMembers.length;
    }
    for (i = 0; i < newMembers.length; i++) {
      this.rightMembers.splice(position + i, 0, newMembers[i]);
    }
    position = position * 2;
    position = position + 3;
    position = position + this.leftMembers.length * 2;
    for (i = 0; i < newMembers.length; i++) {
      this.Super('addMembers', [newMembers[i], position]);
      position = position + 1;
      this.Super('addMembers', [isc.HLayout.create({
        width: this.rightMembersMargin,
        height: 1
      }), position]);
      position = position + 1;
    }
  },
  
  // ** {{{ setRightMembers(newMembers, position) }}} **
  //
  // It works just for right side members.
  // Set/Display one or more canvases to the right side of the toolbar, optionally at specific position; if any exists, it will be deleted.
  //
  // Parameters:
  // * {{{newMembers}}} type: Array || Object - array of canvases to be displayed, or reference to single canvas.
  setRightMembers: function(newMembers){
    this.removeAllRightMembers();
    this.addRightMembers(newMembers);
  },
  
  // ** {{{ setRightMemberDisabled(member, state) }}} **
  //
  // It works just for right side members.
  // Set the disabled state of this right member.
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the right member to perform the action.
  // * {{{state}}} type: Boolean - new disabled state of this object; pass true to disable the right member.
  setRightMemberDisabled: function(member, state){
    member = this.getRightMember(member);
    if (member) {
      member.setDisabled(state);
    }
    return;
  },
  
  // ** {{{ setRightMemberSelected(member, state) }}} **
  //
  // It works just for right side members.
  // Set the selected state of this right member.
  //
  // Parameters:
  // * {{{member}}} type: String | Number | Canvas - identifier for the right member to perform the action.
  // * {{{state}}} type: Boolean - new disabled state of this object; pass true to select the right member.
  setRightMemberSelected: function(member, state){
    member = this.getRightMember(member);
    if (member) {
      member.setSelected(state);
    }
    return;
  },
  
  // ** {{{ refreshCustomButtons }}} **
  //
  // Refreshes all the custom buttons in the toolbar based on current record selection
  //
  refreshCustomButtons: function(){
    var buttons = this.getRightMembers();
    var hideAllButtons = this.view.viewGrid.getSelectedRecords().length !== 1;
    
    for (var i = 0; i < buttons.length; i++) {
      if (buttons[i].refresh) {
        buttons[i].refresh(this.view.getCurrentValues(), hideAllButtons);
      }
    }
  },
  
  addMembers: 'null',
  
  leftMembers: [],
  rightMembers: [],
  
  styleName: 'OBToolbar',
  overflow: 'auto',
  membersMargin: 0
});


/** ----------------------------- **/


isc.ClassFactory.defineClass('OBToolbarIconButton', isc.MenuButton);

isc.OBToolbarIconButton.addProperties({
  showRollOver: true,
  showDisabled: true,
  showFocused: true,
  showDown: true,
  showFocusedAsOver: false,
  title: '.',
  showHover: true,
  customState: '',
  showMenuButtonImage: false,
  
  initWidget: function(){
    this.Super('initWidget', arguments);
    this.resetBaseStyle();
  },
  
  resetBaseStyle: function(){
    var isMenu = false, extraClass;
    if (this.menu !== null) {
      isMenu = true;
    }
    if (isMenu) {
      extraClass = ' OBToolbarIconButtonMenu ';
      this.iconWidth = 3;
      this.iconHeight = 3;
    } else {
      extraClass = ' ';
      this.iconWidth = 1;
      this.iconHeight = 1;
    }
    
    this.setBaseStyle('OBToolbarIconButton_icon_' + this.buttonType + this.customState + extraClass + 'OBToolbarIconButton');
  }
});

isc.ClassFactory.defineClass('OBToolbarTextButton', isc.Button);

isc.OBToolbarTextButton.addProperties({
  baseStyle: 'OBToolbarTextButton',
  showRollOver: true,
  showDisabled: true,
  showFocused: true,
  showDown: true,
  showFocusedAsOver: false,
  action: function(){
    alert(this.title);
  }
});
