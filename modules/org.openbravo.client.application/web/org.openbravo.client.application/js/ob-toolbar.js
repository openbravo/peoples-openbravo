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
 * Contributor(s):   Sreedhar Sirigiri (TDS), Mallikarjun M (TDS)
 ************************************************************************
 */
isc.ClassFactory.defineClass('OBToolbar', isc.ToolStrip);

isc.OBToolbar.addClassProperties({
  TYPE_SAVE: 'save',
  TYPE_NEW_ROW: 'newRow',
  TYPE_NEW_DOC: 'newDoc',
  TYPE_DELETE: 'eliminate',
  TYPE_UNDO: 'undo',
  TYPE_REFRESH: 'refresh',
  TYPE_EXPORT: 'export',
  TYPE_ATTACHMENTS: 'attach',
  TYPE_CLONE: 'clone',
  
  SAVE_BUTTON_PROPERTIES: {
    action: function(){
      this.view.saveRow();
    },
    disabled: true,
    buttonType: 'save',
    prompt: OB.I18N.getLabel('OBUIAPP_SaveRow'),
    updateState: function(){
      var view = this.view, form = view.viewForm, hasErrors = false, editRow;
      if (view.isShowingForm) {
        this.setDisabled(!form.isNew &&
        (form.isSaving || form.readOnly ||
        !view.hasValidState() ||
        !form.hasChanged));
      } else if (view.isEditingGrid) {
        form = view.viewGrid.getEditForm();
        editRow = view.viewGrid.getEditRow();
        hasErrors = view.viewGrid.rowHasErrors(editRow);
        this.setDisabled(!form.isNew && !hasErrors &&
        (form.isSaving || form.readOnly ||
        !view.hasValidState() ||
        !form.hasChanged));
      } else {
        this.setDisabled(true);
      }
    },
    keyboardShortcutId: 'ToolBar_Save'
  },
  SAVECLOSE_BUTTON_PROPERTIES: {
    action: function(){
      var actionObject = {
        target: this,
        method: this.saveAndClose,
        parameters: []
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true, true);      
    },
    
    saveAndClose: function(){
      this.view.switchFormGridVisibility();
      this.view.messageBar.hide();
    },
    
    buttonType: 'savecloseX',
    prompt: OB.I18N.getLabel('OBUIAPP_CLOSEBUTTON'),
    updateState: function(){
      var view = this.view, form = view.viewForm;
      if (view.isShowingForm) {
        this.setDisabled(false);
        var saveDisabled = (!form.isNew &&
        (form.isSaving || form.readOnly ||
        !view.hasValidState() ||
        !form.hasChanged));
        if (saveDisabled) {
          this.buttonType = 'savecloseX';
          this.prompt = OB.I18N.getLabel('OBUIAPP_CLOSEBUTTON');
        } else {
          this.buttonType = 'saveclose';
          this.prompt = OB.I18N.getLabel('OBUIAPP_SaveClose');
        }
      } else {
        this.setDisabled(true);
      }
      this.resetBaseStyle();
    },
    keyboardShortcutId: 'ToolBar_SaveClose'
  },
  NEW_ROW_BUTTON_PROPERTIES: {
    action: function(){
      var view = this.view, 
          grid = view.viewGrid;
      
      // In case of no record selected getRecordIndex(undefined) returns -1,
      // which is the top position, other case it adds bellow current selected row.
      view.newRow(grid.getRecordIndex(grid.getSelectedRecord()));
    },
    buttonType: 'newRow',
    prompt: OB.I18N.getLabel('OBUIAPP_NewRow'),
    updateState: function(){
      var view = this.view, 
          selectedRecords = view.viewGrid.getSelectedRecords();
      this.setDisabled(view.isShowingForm || view.readOnly || view.singleRecord || !view.hasValidState() || 
                       (selectedRecords && selectedRecords.length > 1));
    },
    keyboardShortcutId: 'ToolBar_NewRow'
  },
  NEW_DOC_BUTTON_PROPERTIES: {
    action: function(){
      this.view.newDocument();
    },
    buttonType: 'newDoc',
    prompt: OB.I18N.getLabel('OBUIAPP_NewDoc'),
    updateState: function(){
      var view = this.view, form = view.viewForm;
      if (view.isShowingForm) {
        this.setDisabled(form.isSaving || view.readOnly || view.singleRecord || !view.hasValidState());
      } else {
        this.setDisabled(view.readOnly || view.singleRecord || !view.hasValidState());
      }
    },
    keyboardShortcutId: 'ToolBar_NewDoc'
  },
  DELETE_BUTTON_PROPERTIES: {
    action: function(){
      this.view.deleteSelectedRows();
    },
    disabled: true,
    buttonType: 'eliminate',
    prompt: OB.I18N.getLabel('OBUIAPP_DeleteRow'),
    updateState: function(){
      var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords(), i;
      for (i = 0; i < selectedRecords.length; i++) {
        if (!grid.isWritable(selectedRecords[i])) {
          this.setDisabled(true);
          return;
        }
        if (selectedRecords[i]._new) {
          this.setDisabled(true);
          return;
        }
      }
      if (view.isShowingForm) {
        this.setDisabled(form.isSaving || form.readOnly || view.singleRecord ||
        !view.hasValidState() ||
        form.isNew);
      } else {
        this.setDisabled(view.readOnly || view.singleRecord || !view.hasValidState() ||
        !grid.getSelectedRecords() ||
        grid.getSelectedRecords().length === 0);
      }
    },
    keyboardShortcutId: 'ToolBar_Eliminate'
  },
  REFRESH_BUTTON_PROPERTIES: {
    action: function(){
      this.view.refresh();
    },
    disabled: false,
    buttonType: 'refresh',
    prompt: OB.I18N.getLabel('OBUIAPP_RefreshData'),
    updateState: function(){
      var view = this.view, form = view.viewForm;
      if (view.isShowingForm) {
        this.setDisabled(form.isSaving || form.isNew || !view.hasValidState());
      } else {
        this.setDisabled(!view.hasValidState());
      }
    },
    keyboardShortcutId: 'ToolBar_Refresh'
  },
  UNDO_BUTTON_PROPERTIES: {
    action: function(){
      this.view.undo();
    },
    disabled: true,
    buttonType: 'undo',
    prompt: OB.I18N.getLabel('OBUIAPP_ConfirmCancelEdit'),
    updateState: function(){
      var view = this.view, form = view.viewForm, grid = view.viewGrid, hasErrors = false, editRow, i;
      if (view.isShowingForm) {
        this.setDisabled(form.isSaving || form.readOnly || !view.hasValidState() ||
        !form.hasChanged);
      } else if (view.isEditingGrid) {
        editRow = view.viewGrid.getEditRow();
        hasErrors = view.viewGrid.rowHasErrors(editRow);
        form = grid.getEditForm();
        this.setDisabled(!form.isNew && !hasErrors &&
        (form.isSaving || form.readOnly ||
        !view.hasValidState() ||
        !form.hasChanged));
      } else {
        var selectedRecords = grid.getSelectedRecords(), allRowsHaveErrors = true;
        for (i = 0; i < selectedRecords.length; i++) {
          var rowNum = grid.getRecordIndex(selectedRecords[i]);
          allRowsHaveErrors = allRowsHaveErrors && grid.rowHasErrors(rowNum);
        }
        this.setDisabled(selectedRecords.length === 0 || !allRowsHaveErrors);
      }
    },
    keyboardShortcutId: 'ToolBar_Undo'
  },
  EXPORT_BUTTON_PROPERTIES: {
    action: function(){
      var requestProperties = {
        exportAs: 'csv',
        exportDisplay: 'download',
        params: {
          exportToFile: true
        },
        viewState: this.view.viewGrid.getViewState(),
        tab: this.view.tabId
      };
      this.view.viewGrid.exportData(requestProperties);
    },
    disabled: false,
    buttonType: 'export',
    prompt: OB.I18N.getLabel('OBUIAPP_ExportGrid'),
    updateState: function(){
      this.setDisabled(this.view.isShowingForm || this.view.viewGrid.getTotalRows() === 0);
    },
    keyboardShortcutId: 'ToolBar_Export'
  },
  ATTACHMENTS_BUTTON_PROPERTIES: {
    action: function(){
      var selectedRows = this.view.viewGrid.getSelectedRecords(),
          attachmentExists = this.view.attachmentExists, i;
      if(this.view.isShowingForm){
          this.view.viewForm.getItem('_attachments_').expandSection();
        this.view.viewForm.scrollToBottom();
        return;
      }
      if(attachmentExists){
        if(selectedRows.size() === 1){
          this.view.viewForm.setFocusItem(this.view.viewForm.getItem('_attachments_'));
          this.view.viewForm.forceFocusedField = '_attachments_';
          this.view.viewForm.expandAttachments = true;
          this.view.editRecord(selectedRows[0]);
        } else {
          var recordIds = "";
          for(i=0; i<selectedRows.size(); i++){
            if(i>0){
              recordIds = recordIds + ",";  
            }
            recordIds=recordIds + selectedRows[i].id;
          }
          var vTabId = this.view.tabId;
          var vbuttonId = this.ID;
             isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmDownloadMultiple'), function(clickedOK){
            if(clickedOK){
              var d = {
                Command: 'GET_MULTIPLE_RECORDS_OB3',
                tabId: vTabId,
                buttonId: vbuttonId,
                recordIds: recordIds
              };
              OB.Utilities.postThroughHiddenForm('./businessUtility/TabAttachments_FS.html', d);
            }
          });
        }
      }else{
        var attachmentFile = OB.I18N.getLabel('OBUIAPP_AttachmentFile');
        var form = isc.DynamicForm.create({
          fields: [
            {name: 'inpname', title: attachmentFile, type: 'upload'},
            {name: 'Command', type: 'hidden', value: 'SAVE_NEW_OB3'},
            {name: 'buttonId', type: 'hidden', value: this.ID},
            {name: 'inpKey', type: 'hidden', value: this.view.viewGrid.getSelectedRecord().id},
            {name: 'inpTabId', type: 'hidden', value: this.view.tabId},
            {name: 'inpwindowId', type: 'hidden', value: this.view.windowId}
          ],
          encoding: 'multipart',
          action: './businessUtility/TabAttachments_FS.html',
          target: "background_target",
          position: 'absolute',
          left: '-9000px',
          numCols: 4,
          colWidths: ['24%', '24%', '24%', '24%'],
          width: '10px',
          redraw: function(){
          },
          button: this
        });

        var submitbutton = isc.Button.create({
            title: OB.I18N.getLabel('OBUIAPP_AttachmentSubmit'),
            theForm: form,
            click: function(){
              var form = this.theForm;
              form.button.customState = 'Progress';
              form.button.resetBaseStyle();
              form.submitForm();
              popup.hide();
            }
        });
        this.oldForm = form;
        var horizontalLayout = isc.HLayout.create({
          width: '100%',
          height: '30px',
          align: 'right'
        });
        var popup = isc.OBPopup.create({
          height: 30,
          width: 300,
          align: 'right',
          initWidget: function(args){
          horizontalLayout.addMember(form);
          horizontalLayout.addMember(submitbutton);
          this.items = horizontalLayout;
          this.Super('initWidget', arguments);
          }
        });
        form.popup = popup;
        popup.show();
      }
    },
    callback: function(){
      if(this.oldForm){
        this.oldForm.destroy();
      }
      this.view.attachmentExists = true;
      this.customState = '';
      this.updateState();
    },
    disabled: false,
    buttonType: 'attach',
    updateState: function(){
      var selectedRows = this.view.viewGrid.getSelectedRecords();
      var attachmentExists = this.view.attachmentExists;
      if(attachmentExists){
        if(selectedRows.size()===1){
          this.prompt = OB.I18N.getLabel('OBUIAPP_GoToAttachments');
        } else {
          this.prompt = OB.I18N.getLabel('OBUIAPP_DownloadAttachments');
        }
        this.buttonType = 'attachExists';
      }else{
        this.prompt = OB.I18N.getLabel('OBUIAPP_CreateAttachments');  
        this.buttonType = 'attach';
      }
      if(!selectedRows || selectedRows.size()===0){
        // If there are now selected rows then attachments button will be disabled
        this.setDisabled(true);  
      }else if(selectedRows.size() > 1 && !this.view.attachmentExists){
        // If there are more than one rows selected, and no one has attachments,
        // then attachments button will be disabled
        this.setDisabled(true);
      }else{
        this.setDisabled(false);  
      }
      this.resetBaseStyle();
    },
    keyboardShortcutId: 'ToolBar_Attachments'
  },
  LINK_BUTTON_PROPERTIES: {
    action: function(){
      var url = this.view.getDirectLinkUrl();
      var form = isc.OBViewForm.create({
        width: 390,
        height: 1,
        numCols: 1,
        overflow: 'visible',
        fields: [{
          type: 'OBTextAreaItem', selectOnFocus: true, 
          width: 390, height: 50, canFocus: true, 
          name:'url', 
          title: OB.I18N.getLabel('OBUIAPP_PasteLink'), value: url},
          {
            type: 'CanvasItem', showTitle: false, 
            width: '100%', height: 1, overFlow: 'visible',  
            cellStyle: 'OBFormField',
            titleStyle: 'OBFormFieldLabel',
            textBoxStyle: 'OBFormFieldInput',
            name:'url', canvas: isc.Label.create({
              width: 360,
              contents: OB.I18N.getLabel('OBUIAPP_DeepLinkNote'), height: 1, overflow: 'visible'
            })
          }
        ],        
        show: function() {
          var fld = this.getFields()[0];
          this.setFocusItem(fld);
          this.Super('show', arguments);
          // do the focus with a delay to give the popup time to draw
          this.fireOnPause('link_button_show', this.focus, 50, this);
        }
      });
      var dialog = OB.Utilities.createDialog(OB.I18N.getLabel('OBUIAPP_Document_Link'));
      dialog.setContent(form);
      dialog.show();
    },
    disabled: false,
    buttonType: 'link',
    prompt: OB.I18N.getLabel('OBUIAPP_GetDirectLink'),
    updateState: function(){
    },
    keyboardShortcutId: 'ToolBar_Link'    
  },
  // This offers a mechanism to add properties at runtime to buttons created through
  // templates and java
  BUTTON_PROPERTIES: {
    'audit': {
      updateState: function(){
        var view = this.view, form = view.viewForm, grid = view.viewGrid;
        var selectedRecords = grid.getSelectedRecords();
        var disabled = false;
        if (selectedRecords && selectedRecords.length > 1) {
          disabled = true;
        } else if (view.isShowingForm && form.isNew) {
          disabled = true;
        } else if (view.isEditingGrid && grid.getEditForm().isNew) {
          disabled = true;
        } else if ((selectedRecords && selectedRecords.length > 1) || 
                  (selectedRecords && selectedRecords.lenght === 1 && selectedRecords[0].updated && selectedRecords[0].creationDate && selectedRecords[0].updated.getTime() === selectedRecords[0].creationDate.getTime())) {
          disabled = true;
        }
        this.setDisabled(disabled);
      }
    }
  },
  CLONE_BUTTON_PROPERTIES: {
	    action: function(){
	      alert('this method must be overridden when registering the button');
	    },
	    disabled: false,
	    buttonType: 'clone',
	    prompt: OB.I18N.getLabel('OBUIAPP_CloneData'),
	    updateState: function(){
	        var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
	        if (selectedRecords && selectedRecords.length > 1) {
	          this.setDisabled(true);
	        } else if (view.isShowingForm && form.isNew) {
	          this.setDisabled(true);
	        } else if (view.isEditingGrid && grid.getEditForm().isNew) {
		        this.setDisabled(true);
		      } else {
		        this.setDisabled(selectedRecords.length === 0);
		      }
	    },
	    keyboardShortcutId: 'ToolBar_Clone'
	  }
});

// = OBToolbar =
//
// The OBToolbar is the toolbar to perform common actions within a form.
//
isc.OBToolbar.addProperties({
  randomId: null,
  initWidget: function(){
    this.Super('initWidget', arguments);
    function getRandomId(){
      var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz', stringLength = 8, randomString = '', i, rnum;
      for (i = 0; i < stringLength; i++) {
        rnum = Math.floor(Math.random() * chars.length);
        randomString += chars.substring(rnum, rnum + 1);
      }
      return randomString;
    }
    this.randomId = getRandomId();
    this.members = null;
    
    var newMembers = [], i = 0, j = 0;
    this.leftMembers = OB.ToolbarRegistry.getButtons(this.view.tabId);
    newMembers[j] = isc.HLayout.create({
      width: this.leftMargin,
      height: 1
    });
    j++;
    
    if (this.leftMembers) {
      for (i = 0; i < this.leftMembers.length; i++) {
       
        newMembers[j] = this.leftMembers[i];
        
        if (newMembers[j].buttonType && isc.OBToolbar.BUTTON_PROPERTIES[newMembers[j].buttonType]) {
       
          isc.addProperties(newMembers[j], isc.OBToolbar.BUTTON_PROPERTIES[newMembers[j].buttonType]);
         
        }
        
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
 
  
  addMems: function(m) {
      this.Super('addMembers', m );
   },
  
  // ** {{{ updateButtonState }}} **
  //
  // Updates the visible and disabled state of buttons using the view's form and
  // grid information.
  // 
  // NOTE: new buttons should implement the updateState method.
  //
  updateButtonState: function(noSetSession){

    for (i = 0; i < this.leftMembers.length; i++) {
      if (this.leftMembers[i].updateState) {
        this.leftMembers[i].updateState();
      }
    }
    
    // and refresh the process toolbar buttons
    this.refreshCustomButtons(noSetSession);
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
  refreshCustomButtons: function(noSetSession){
    function doRefresh(buttons, currentValues, hideAllButtons, me) {
      var i;
      for (i = 0; i < buttons.length; i++) {
        if (buttons[i].updateState) {
          me.defineRightMembersShortcuts();
          buttons[i].updateState(currentValues, hideAllButtons);
          me.rightMembers[i].enableShortcut();
        }
      }
      for (i = 0; i < me.leftMembers.length; i++) {
        if (me.leftMembers[i].updateState) {
            me.leftMembers[i].updateState();
        }
      }
    }

    var buttons = this.getRightMembers(), buttonContexts = [], currentContext, buttonsByContext = [];
    for (i = 0; i < buttons.length; i++) {
      if (!currentContext || currentContext !== buttons[i].contextView) {
        // Adding new context
        currentContext = buttons[i].contextView;
        buttonContexts.push(currentContext);
        buttonsByContext[currentContext] = [];
      }
      buttonsByContext[currentContext].push(buttons[i]);
    }

    var iButtonContext;

    // This is needed to prevent JSLint complaining about "Don't make functions within a loop.
    var callbackHandler = function (currentContext, me) {
      return function(response, data, request) {
        var sessionAttributes = data.sessionAttributes, auxInputs = data.auxiliaryInputValues, attachmentExists = data.attachmentExists, prop;
        if (sessionAttributes) {
          currentContext.viewForm.sessionAttributes = sessionAttributes;
        }

        if (auxInputs) {
          this.auxInputs = {};
          for (prop in auxInputs) {
            if (auxInputs.hasOwnProperty(prop)) {
              currentContext.viewForm.setValue(prop, auxInputs[prop].value);
              currentContext.viewForm.auxInputs[prop] = auxInputs[prop].value;
            }
          }
        }
        currentContext.viewForm.view.attachmentExists = attachmentExists;
        doRefresh(buttonsByContext[currentContext], currentValues || {}, noneOrMultipleRecordsSelected, me);
      };
    };


    for (iButtonContext = 0; iButtonContext < buttonContexts.length; iButtonContext++) {
      currentContext = buttonContexts[iButtonContext];
      var numOfSelRecords = 0, 
          isNew = currentContext.viewForm.isNew, 
          hideAllButtons = typeof(isNew) !== 'undefined' && !isNew && (!currentContext.isShowingForm && (currentContext.viewGrid.getSelectedRecords().size()===0)), currentValues = currentContext.getCurrentValues();

      var noneOrMultipleRecordsSelected = currentContext.viewGrid.getSelectedRecords().length !== 1;
      if (currentContext.viewGrid.getSelectedRecords()) {
        numOfSelRecords = currentContext.viewGrid.getSelectedRecords().length;
      }

      if (currentValues && !noSetSession && !currentContext.isShowingForm && !isNew && !hideAllButtons) {
        var me = this;
        // Call FIC to obtain possible session attributes and set them in form
        requestParams = {
          MODE: 'SETSESSION',
          PARENT_ID: currentContext.getParentId(),
          TAB_ID: currentContext.tabId,
          ROW_ID: currentValues.id
        };
        var multipleSelectedRowIds = [];
        var selectedRecords = currentContext.viewGrid.getSelectedRecords();
        if(selectedRecords.size() > 1){
          for (i = 0; i < selectedRecords.size(); i++) {
            multipleSelectedRowIds[i] = selectedRecords[i].id;
          }
          requestParams.MULTIPLE_ROW_IDS = multipleSelectedRowIds;
        }
        var allProperties = currentContext.getContextInfo(false, true, false, true);
        OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, callbackHandler(currentContext, me));
      } else {
        doRefresh(buttonsByContext[currentContext], currentValues || {}, hideAllButtons || noneOrMultipleRecordsSelected, this);
      }
    }



  },

  visibilityChanged: function(state){
    if (state) {
      this.enableShortcuts();
    } else {
      this.disableShortcuts();
    }
  },

  draw: function(){
    this.Super('draw', arguments);
    this.defineRightMembersShortcuts();
    this.enableShortcuts();
  },

  rightMembersShortcuts: [],

  defineRightMembersShortcuts: function(){
    var i, j, k, l, id, character, position;
    function isAssignedCharacter(character, me){
      character = character.toString();
      character = character.toUpperCase();
      for (k = 0; k < me.rightMembersShortcuts.length; k++) {
        if (me.rightMembersShortcuts[k][0] === character) {
          return true;
        }
      }
      return false;
    }
    for (i = 0; i < this.rightMembers.length; i++) {
      var title = this.rightMembers[i].realTitle, haveToContinue = true;
      this.rightMembersShortcuts[i] = [];
      if (haveToContinue) { // Check if free character and assign
        haveToContinue = true;
        for (j = 0; j < title.length; j++) {
          if (!isAssignedCharacter(title.substring(j, j + 1), this)) {
            this.rightMembersShortcuts[i][0] = title.substring(j, j + 1).toUpperCase();
            this.rightMembersShortcuts[i][1] = j + 1;
            haveToContinue = false;
            break;
          }
        }
      }
      if (haveToContinue) { // Check if free number and assign
        haveToContinue = true;
        for (l = 1; l < 10; l++) {
          if (!isAssignedCharacter(l, this)) {
            this.rightMembersShortcuts[i][0] = l;
            this.rightMembersShortcuts[i][1] = 'end';
            haveToContinue = false;
            break;
          }
        }
      }
      if (haveToContinue) {
        this.rightMembersShortcuts[i][0] = '';
        this.rightMembersShortcuts[i][1] = 0;
      }
      this.rightMembers[i].keyboardShortcutId = this.randomId + '_' + i;
      this.rightMembers[i].keyboardShortcutCharacter = this.rightMembersShortcuts[i][0];
      this.rightMembers[i].keyboardShortcutPosition = this.rightMembersShortcuts[i][1];
    }
  },
  
  enableShortcuts: function(){
    if (this.leftMembers) {
      for (i = 0; i < this.leftMembers.length; i++) {
        if (this.leftMembers[i].enableShortcut) {
          this.leftMembers[i].enableShortcut();
        }
      }
    }
    if (this.rightMembers) {
      this.defineRightMembersShortcuts();
      for (i = 0; i < this.rightMembers.length; i++) {
        if (this.rightMembers[i].enableShortcut) {
          this.rightMembers[i].enableShortcut();
        }
      }
    }
  },
  
  disableShortcuts: function(){
    if (this.leftMembers) {
      for (i = 0; i < this.leftMembers.length; i++) {
        if (this.leftMembers[i].disableShortcut) {
          this.leftMembers[i].disableShortcut();
        }
      }
    }
    if (this.rightMembers) {
      for (i = 0; i < this.rightMembers.length; i++) {
        if (this.rightMembers[i].disableShortcut) {
          this.rightMembers[i].disableShortcut();
        }
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
  },
  
  keyboardShortcutId: null,
  enableShortcut: function(){
    if (this.keyboardShortcutId) {
      var me = this;
      var ksAction = function(){
        if (!me.disabled) {
          me.action();
        }
      };
      OB.KeyboardManager.KS.set(this.keyboardShortcutId, ksAction);
    }
  },
  disableShortcut: function(){
    if (this.keyboardShortcutId) {
      OB.KeyboardManager.KS.set(this.keyboardShortcutId, function(){
        return true;
      });
    }
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
  title: '',
  realTitle: '', // difference between title and realTitle is just the <u>xx</u> for keyboard shortcut notation.
  action: function(){
    alert(this.title);
  },
  initWidget: function(){
    this.Super('initWidget', arguments);
    this.realTitle = this.title;
  },
  
  keyboardShortcutId: null,
  keyboardShortcutCharacter: null,
  keyboardShortcutPosition: null,
  enableShortcut: function(){
    if (this.keyboardShortcutId) {
      var me = this;
      var newTitle = this.realTitle;
      var ksAction = function(){
        if (!me.disabled && me.visible) {
          me.action();
        }
      };
      if (this.keyboardShortcutPosition === 'end') {
        newTitle = newTitle + ' (<u>' + this.keyboardShortcutCharacter + '</u>)';
      } else {
        newTitle = newTitle.substring(0, this.keyboardShortcutPosition - 1) + '<u>' + newTitle.substring(this.keyboardShortcutPosition - 1, this.keyboardShortcutPosition) + '</u>' + newTitle.substring(this.keyboardShortcutPosition, newTitle.length);
      }
      this.setTitle(newTitle);
      if (this.keyboardShortcutPosition) { // If 'this.keyboardShortcutPosition' equals 0 means that there is no shortcut assigned
        OB.KeyboardManager.KS.set(this.keyboardShortcutId, ksAction, null, {
          'ctrl': true,
          'alt': true,
          'shift': true,
          'key': this.keyboardShortcutCharacter
        });
      }
    }
  },
  disableShortcut: function(){
    if (this.keyboardShortcutId) {
      var newTitle = this.realTitle;
      this.setTitle(newTitle);
      OB.KeyboardManager.KS.set(this.keyboardShortcutId, function(){
        return true;
      }, '', {
        'ctrl': true,
        'alt': true,
        'shift': true,
        'key': 'xyz'
      });
    }
  }
});

OB.ToolbarUtils = {};

OB.ToolbarUtils.print = function(view, url, directPrint){
  var selectedRecords = view.viewGrid.getSelectedRecords();

  if (selectedRecords.length === 0) {
    view.messageBar.setMessage(OBMessageBar.TYPE_WARNING, '', OB.I18N.getLabel('OBUIAPP_PrintNoRecordSelected'));
    return;
  }

  var popupParams = 'Command=DEFAULT', allProperties = view.getContextInfo(false, true, false, true),
      sessionProperties = view.getContextInfo(true, true, false, true), param, i;

  for (param in allProperties) {
    if (allProperties.hasOwnProperty(param)) {
      var value = allProperties[param];

      if (typeof value === 'boolean') {
        value = value ? 'Y' : 'N';
      }

      popupParams += '&' + param + '=' + value;
    }
  }

  popupParams += '&inppdfpath=' + url;
  popupParams += '&inphiddenkey=' + view.standardProperties.inpKeyName;
  popupParams += '&inpdirectprint=' + (directPrint ? 'Y' : 'N');

  var selectedIds = '';
  for (i = 0; i < selectedRecords.length; i++) {
    selectedIds += (i > 0 ? ',' : '') + selectedRecords[i].id;
  }

  popupParams += '&inphiddenvalue=' + selectedIds;

  view.setContextInfo(sessionProperties, function(){
    OB.Layout.ClassicOBCompatibility.Popup.open('print', 0, 0, OB.Application.contextUrl + 'businessUtility/PrinterReports.html?' + popupParams, '', window, false, false, true);
  });
};

OB.ToolbarUtils.showAuditTrail = function(view){
  var selectedRecords = view.viewGrid.getSelectedRecords();

  if (selectedRecords.length > 1) {
    var setWarning = {
      set: function(label){
        view.messageBar.setMessage(OBMessageBar.TYPE_WARNING, '', label);
      }
    };
    OB.I18N.getLabel('JS28', null, setWarning, 'set');
    return;
  }

  var popupParams = 'Command=POPUP_HISTORY';
  popupParams += '&inpTabId=' + view.tabId;
  popupParams += '&inpTableId=' + view.standardProperties.inpTableId;

  if (view.viewGrid.getSelectedRecord()) {
    popupParams += '&inpRecordId=' + view.viewGrid.getSelectedRecord().id;
  }

  OB.Layout.ClassicOBCompatibility.Popup.open('audit', 900, 600, OB.Application.contextUrl + 'businessUtility/AuditTrail.html?' + popupParams, '', window, false, false, true);
};

OB.ToolbarUtils.showTree = function(view){
  function openPopupTree() {
    // Open tree through menu to have hidden vertical menu which is needed to show old JS messages
    var popupParams = 'url=/utility/WindowTree.html';
    popupParams += '&Command=DEFAULT';
    popupParams += '&inpTabId=' + tabId;
    popupParams += '&hideMenu=true&noprefs=true';
    OB.Layout.ClassicOBCompatibility.Popup.open('tree', 750, 625, OB.Application.contextUrl + 'security/Menu.html?' + popupParams, '', window, false, false, true);
  }
  var tabId = view.tabId;
  
  view.setContextInfo(view.getContextInfo(true, true, true, true), openPopupTree, true);
};

OB.ToolbarRegistry = {
    buttonDefinitions: [],
    registerButton: function(buttonId, clazz, properties, sortOrder, tabId) {
      // declare the vars and the object which will be stored
      var i, index = 0, buttonDef = {
          buttonId: buttonId,
          clazz: clazz,
          properties: properties,
          sortOrder: sortOrder,
          tabId: tabId
      };
 
      // already registered, bail
      for (i = 0; i < this.buttonDefinitions.length; i++) {   
        if (this.buttonDefinitions[i].buttonId === buttonId) {
          return;
        }
      }
      
      index = this.buttonDefinitions.length;
      for (i = 0; i < this.buttonDefinitions.length; i++) {   
        if (this.buttonDefinitions[i].sortOrder > sortOrder) {
          index = i;
          break;
        }
      }

      if (index === this.buttonDefinitions.length) {
        this.buttonDefinitions[index] = buttonDef;
      } else {
        this.buttonDefinitions.splice(index, 0, buttonDef);
      }
    },

		getButtons: function(tabId) {
		  // get the buttons for the tabId, this includes all buttons with that tabId or with no tabId set
		  // as the button defs are already stored by their sortorder we can just iterate over the array
		  // and pick them up in the correct order
		  // the return should be an array of button instances created by doing 
		  //  btnDefinitionClass.create(btnDefinitionProperties);
		  var result = [], resultIndex = 0, i;	
		  for (i = 0; i < this.buttonDefinitions.length; i++) {	
		    if (!this.buttonDefinitions[i].tabId || tabId === this.buttonDefinitions[i].tabId) {
		      result[resultIndex++] = this.buttonDefinitions[i].clazz.create(isc.clone(this.buttonDefinitions[i].properties));
		    }
		  }
		  return result;
		}
};

//These are the icon toolbar buttons shown in all the tabs 
OB.ToolbarRegistry.registerButton(isc.OBToolbar.NEW_DOC_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.NEW_DOC_BUTTON_PROPERTIES, 10, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.NEW_ROW_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.NEW_ROW_BUTTON_PROPERTIES, 20, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.SAVE_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.SAVE_BUTTON_PROPERTIES, 30, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.SAVECLOSE_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.SAVECLOSE_BUTTON_PROPERTIES, 40, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.UNDO_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.UNDO_BUTTON_PROPERTIES, 50, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.DELETE_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.DELETE_BUTTON_PROPERTIES, 60, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.REFRESH_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.REFRESH_BUTTON_PROPERTIES, 70, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.EXPORT_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.EXPORT_BUTTON_PROPERTIES, 80, null);
OB.ToolbarRegistry.registerButton(isc.OBToolbar.ATTACHMENTS_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.ATTACHMENTS_BUTTON_PROPERTIES, 90, null);
          
//and add the direct link at the end
OB.ToolbarRegistry.registerButton(isc.OBToolbar.LINK_BUTTON_PROPERTIES.buttonType, isc.OBToolbarIconButton, isc.OBToolbar.LINK_BUTTON_PROPERTIES, 300, null);
