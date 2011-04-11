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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBAttachments =
//
// Represents the attachments section in the form.
//

isc.ClassFactory.defineClass('OBAttachmentsSectionItem', isc.OBSectionItem);

isc.OBAttachmentsSectionItem.addProperties({
  // as the name is always the same there should be at most
  // one linked item section per form
  name: '_attachments_',
  
  // note: setting these apparently completely hides the section
  // width: '100%',
  // height: '100%',
  
  overflow: 'hidden',
  
  canFocus: true,
  
  // don't expand as a default
  sectionExpanded: false,
  
  prompt: OB.I18N.getLabel('OBUIAPP_AttachmentPrompt'),
  
  canvasItem: null,
  
  visible: false,
  
  // note formitems don't have an initWidget but an init method
  init: function(){
    // override the one passed in
    this.defaultValue = OB.I18N.getLabel('OBUIAPP_AttachmentTitle');
    this.sectionExpanded = false;
    
    // tell the form who we are
    this.form.attachmentsSection = this;
    
    return this.Super('init', arguments);
  },

  getAttachmentPart: function(){
    if (!this.canvasItem) {
      this.canvasItem = this.form.getField(this.itemIds[0]);
    }
    return this.canvasItem.canvas;
  },
  
  setRecordInfo: function(entity, id, tabId){
    this.getAttachmentPart().setRecordInfo(entity, id, tabId);
  },
  
  collapseSection: function(){
    var ret = this.Super('collapseSection', arguments);
    this.getAttachmentPart().setExpanded(false);
    return ret;
  },
  
  expandSection: function(){
    // if this is not there then when clicking inside the 
    // section item will visualize it
    if (!this.isVisible()) {
      return;
    }
    var ret = this.Super('expandSection', arguments);
    this.getAttachmentPart().setExpanded(true);
    return ret;
  },
  
  fillAttachments: function(attachments){
    this.getAttachmentPart().fillAttachments(attachments);  
  }

});


isc.ClassFactory.defineClass('OBAttachmentCanvasItem', isc.CanvasItem);

isc.OBAttachmentCanvasItem.addProperties({

  canFocus: true,
  
  // setting width/height makes the canvasitem to be hidden after a few
  // clicks on the section item, so don't do that for now
  // width: '100%',
  // height: '100%',
  
  showTitle: false,
  overflow: 'auto',
  // note that explicitly setting the canvas gives an error as not
  // all props are set correctly on the canvas (for example the
  // pointer back to this item: canvasItem
  // for setting more properties use canvasProperties, etc. see
  // the docs
  canvasConstructor: 'OBAttachmentsLayout',

  // never disable this one
  isDisabled: function(){
    return false;
  }
  
});



isc.ClassFactory.defineClass('OBAttachmentsLayout', isc.VLayout);

isc.OBAttachmentsLayout.addProperties({

  // set to true when the content has been created at first expand
  isInitialized: false,
  
  layoutMargin: 5,

  width: '50%',
  
  /** 
   * Initializes the widget
   **/
  initWidget: function(){
    var ret = this.Super('initWidget', arguments);
    
    return ret;
  },
  
  
  // never disable this item
  isDisabled: function(){
    return false;
  },
  
  getForm: function(){
    return this.canvasItem.form;
  },
  
  setRecordInfo: function(entity, id, tabId){
    this.entity = entity;
    // use recordId instead of id, as id is often used to keep
    // html ids
    this.recordId = id;
    this.tabId = tabId;
    this.isInitialized = false;
  },
  
  
  setExpanded: function(expanded){
    if (expanded && !this.isInitialized) {
      this.isInitialized = true;
    }
  },
  
  addAttachmentInfo: function(attachmentLayout, attachment){
  },

  callback: function(attachmentsobj){
    var button =  this.getForm().view.toolBar.getLeftMember(isc.OBToolbar.TYPE_ATTACHMENTS);
    if(!button) {
      button =  this.getForm().view.toolBar.getLeftMember("attachExists");
    }
    button.customState = '';
    button.resetBaseStyle();
    this.fillAttachments(attachmentsobj.attachments);  
  },
  
  fileExists: function(fileName, attachments){
    if(!attachments || attachments.length === 0){
      return false;
    }
    for(var i=0; i < attachments.length; i++){
      if(attachments[i].name === fileName){
        return true;
      }
    }
    return false;
  },
  
  fillAttachments: function(attachments){
    this.savedAttachments = attachments;
    this.removeMembers(this.getMembers());
    var hLayout = isc.HLayout.create();
    this.addMember(hLayout);
    var me = this;
    var addButton = isc.OBSectionItemControlLink.create({
      contents: '[ '+OB.I18N.getLabel('OBUIAPP_AttachmentAdd')+' ]',
      width: '50%',
      canvas: me,
	  action: function(){
        var form = isc.DynamicForm.create({
            fields: [
              {name: 'inpname', type: 'upload', change: function(form, item, value, oldvalue){
                var addFunction = function(clickedOK){
                  if(clickedOK){
                    var hTempLayout = isc.HLayout.create();
                    form.theCanvas.addMember(hTempLayout,form.theCanvas.getMembers().size());
                    var uploading = isc.Label.create({
                      contents: fileName + '    '+OB.I18N.getLabel('OBUIAPP_AttachmentUploading')
                    });
                    hTempLayout.addMember(uploading);
                    var button =  form.theCanvas.getForm().view.toolBar.getLeftMember(isc.OBToolbar.TYPE_ATTACHMENTS);
                    if(!button) {
                      button =  form.theCanvas.getForm().view.toolBar.getLeftMember("attachExists");
                    }
                    button.customState = 'Progress';
                    button.resetBaseStyle();
                    form.show();
                    form.submitForm();
                  }
                };
                var lastChar=value.lastIndexOf("\\") + 1;
                var fileName = lastChar===-1?value:value.substring(lastChar);
                if(form.theCanvas.fileExists(fileName, attachments)){
                  isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmUploadOverwrite'), addFunction);
                }else{
                  addFunction(true);
                }
              }},
              {name: 'Command', type: 'hidden', value: 'SAVE_NEW_OB3'},
              {name: 'buttonId', type: 'hidden', value: this.canvas.ID},
              {name: 'inpKey', type: 'hidden', value: this.canvas.recordId},
              {name: 'inpTabId', type: 'hidden', value: this.canvas.tabId},
              {name: 'inpwindowId', type: 'hidden', value: this.canvas.windowId}
            ],
            encoding: 'multipart',
            action: './businessUtility/TabAttachments_FS.html',
            target: "background_target",
            position: 'absolute',
            left: '-9000px',
            theCanvas: this.canvas
          });
        form.show();
        form.getItem('inpname').getElement().click();
      }
    });
    hLayout.addMember(addButton);
    // If there are no attachments, we only display the "[Add]" button
    if(!attachments || attachments.length === 0){
      this.getForm().getItem('_attachments_').setValue(OB.I18N.getLabel('OBUIAPP_AttachmentTitle'));
      this.getForm().view.attachmentExists = false;
      this.getForm().view.toolBar.updateButtonState();
      return;
    }
    this.getForm().view.attachmentExists = true;
    this.getForm().view.toolBar.updateButtonState();
    isc_OBAttachmentsSectionItem_0.setValue(OB.I18N.getLabel('OBUIAPP_AttachmentTitle')+" ("+attachments.length+")");
    var downloadAllButton = isc.OBSectionItemControlLink.create({
      contents: '[ '+OB.I18N.getLabel('OBUIAPP_AttachmentDownloadAll')+' ]',
      canvas: this,
      action: function(){
        var canvas = this.canvas;
        isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmDownloadMultiple'), function(clickedOK){
          if(clickedOK){
            var d = {
              Command: 'GET_MULTIPLE_RECORDS_OB3',
              tabId: canvas.tabId,
              recordIds: canvas.recordId
            };
            OB.Utilities.postThroughHiddenForm('./businessUtility/TabAttachments_FS.html', d);
          }
        });
      }
    });
    var removeAllButton = isc.OBSectionItemControlLink.create({
      contents: '[ '+OB.I18N.getLabel('OBUIAPP_AttachmentRemoveAll')+' ]',
      canvas: me,
      action: function(){
        var d = {
          Command: 'DELETE',
          tabId: this.canvas.tabId,
          buttonId: this.canvas.ID,
          recordIds: this.canvas.recordId
        };
        var canvas = this.canvas;
        OB.RemoteCallManager.call('org.openbravo.client.application.window.AttachmentsAH', {}, d, function(response, data, request){
              canvas.fillAttachments(data.attachments);
        });
      }
    });
    hLayout.addMember(downloadAllButton);
    hLayout.addMember(removeAllButton);
    
    var downloadActions = function(){
      var d = {
        Command: 'DISPLAY_DATA',
        inpcFileId: this.attachId
      };
      OB.Utilities.postThroughHiddenForm('./businessUtility/TabAttachments_FS.html', d);
    };
    
    var removeActions = function(){
      var d = {
        Command: 'DELETE',
        tabId: this.canvas.tabId,
        buttonId: this.canvas.ID,
        recordIds: this.canvas.recordId,
        attachId: this.attachmentId
      };
      var canvas = this.canvas;
      OB.RemoteCallManager.call('org.openbravo.client.application.window.AttachmentsAH', {}, d, function(response, data, request){
        canvas.fillAttachments(data.attachments);
      });
    };
    
    for(var i=0; i < attachments.length; i++){
      var attachment = attachments[i];
      var buttonLayout = isc.HLayout.create();
      var attachmentLabel = isc.Label.create({
        contents: attachment.name,
        width: '35%'
      });
      var attachmentBy = isc.Label.create({
        contents: " <i>"+OB.I18N.getLabel('OBUIAPP_AttachmentBy')+" "+attachment.createdby+"</i>"
      });
      var attachmentCreationDate = isc.Label.create({
        contents: OB.Utilities.getTimePassed(new Date(attachment.creationDate))
      });
      var downloadAttachment = isc.OBSectionItemControlLink.create({
        contents: '[ '+OB.I18N.getLabel('OBUIAPP_AttachmentDownload')+' ]',
        attachmentName: attachment.name,
        attachId: attachment.id,
        action: downloadActions
      });
      var removeAttachment = isc.OBSectionItemControlLink.create({
        contents: '[ '+OB.I18N.getLabel('OBUIAPP_AttachmentRemove')+' ]',
        attachmentName: attachment.name,
        attachmentId: attachment.id,
        canvas: this,
        action: removeActions
      });
      buttonLayout.addMember(attachmentLabel);
      buttonLayout.addMember(attachmentCreationDate);
      buttonLayout.addMember(attachmentBy);
      buttonLayout.addMember(downloadAttachment);
      buttonLayout.addMember(removeAttachment);
      this.addMember(buttonLayout);
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
