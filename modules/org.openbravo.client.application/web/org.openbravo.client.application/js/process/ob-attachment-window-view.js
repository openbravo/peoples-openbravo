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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBAttachmentWindowView', isc.OBBaseParameterWindowView);

// == OBAttachmentWindowView ==
//   OBPAttachmentWindowView is the view that represent the window to upload
// edit the attachments of a tab. It includes the parameters defined on the 
// Attachment method in use.
isc.OBAttachmentWindowView.addProperties({
  // Set default properties for the OBPopup container
  showMinimizeButton: false,
  showMaximizeButton: true,
  popupWidth: '90%',
  popupHeight: '90%',
  showsItself: true,

  // Set now pure P&E layout properties
  width: '100%',
  height: '100%',
  overflow: 'auto',
  autoSize: false,

  defaultsActionHandler: 'org.openbravo.client.application.process.DefaultsAttachmentActionHandler',

  members: [],

  attachSection: null,
  ownerView: null,
  uploadMode: null,
  attachmentId: null,
  attachmentName: null,
  attachmentMethod: null,

  attachFormProps: {
    encoding: 'multipart',
    action: './businessUtility/TabAttachments_FS.html',
    target: 'background_target',
    //numCols: 2,
    align: 'center'
    //redraw: function () {}
    //theCanvas: this.canvas
  },

  initWidget: function () {
    var i, attachFields;
    if (this.uploadMode) {
      attachFields = [{
        name: 'inpname',
        title: OB.I18N.getLabel('OBUIAPP_AttachmentFile'),
        type: 'upload',
        multiple: false,
        canFocus: false
      }, {
        name: 'Command',
        type: 'hidden',
        value: 'SAVE_NEW_OB3'
      }, {
        name: 'buttonId',
        type: 'hidden',
        value: this.attachSection.ID
      }, {
        name: 'viewId',
        type: 'hidden',
        value: this.ownerView.ID
      }, {
        name: 'viewId',
        type: 'hidden',
        value: this.ownerView.ID
      }, {
        name: 'inpKey',
        type: 'hidden',
        value: this.attachSection.recordId
      }, {
        name: 'inpTabId',
        type: 'hidden',
        value: this.attachSection.tabId
      }, {
        name: 'inpDocumentOrg',
        type: 'hidden',
        value: this.attachSection.docOrganization
      }, {
        name: 'inpwindowId',
        type: 'hidden',
        value: this.attachSection.windowId
      }];
    } else {
      attachFields = [{
        name: 'inpname',
        type: 'hidden',
        value: this.attachmentName
      }, {
        name: 'Command',
        type: 'hidden',
        value: 'EDIT'
      }, {
        name: 'buttonId',
        type: 'hidden',
        value: this.attachSection.ID
      }, {
        name: 'viewId',
        type: 'hidden',
        value: this.ownerView.ID
      }, {
        name: 'inpKey',
        type: 'hidden',
        value: this.attachSection.recordId
      }, {
        name: 'inpTabId',
        type: 'hidden',
        value: this.attachSection.tabId
      }, {
        name: 'inpDocumentOrg',
        type: 'hidden',
        value: this.attachSection.docOrganization
      }, {
        name: 'inpwindowId',
        type: 'hidden',
        value: this.attachSection.windowId
      }, {
        name: 'inpAttachId',
        type: 'hidden',
        value: this.attachmentId
      }];
    }
    this.baseParams.tabId = this.attachSection.tabId;
    this.baseParams.clientId = this.attachSection.docClient;
    this.baseParams.attachmentMethod = this.attachmentMethod;

    this.formProps = isc.addProperties({}, this.formProps, this.attachFormProps);
    this.viewProperties.fields = isc.shallowClone(attachFields);
    for (i = 0; i < this.viewProperties.additionalFields.length; i++) {
      this.viewProperties.fields.push(this.viewProperties.additionalFields[i]);
    }


    this.Super('initWidget', arguments);

  },

  buildButtonLayout: function () {
    var view = this,
        buttons = [],
        submitbutton, cancelButton;

    function doClick() {
      var view = this.view,
          value = view.theForm.getItem('inpname').getElement().value,
          lastChar, fileName;

      if (view.uploadMode === false) {
        view.editFile();
        return;
      }
      if (!value) {
        isc.say(OB.I18N.getLabel('OBUIAPP_AttachmentsSpecifyFile'));
        return;
      }

      lastChar = value.lastIndexOf("\\") + 1;
      fileName = lastChar === -1 ? value : value.substring(lastChar);

      if (view.attachSection.fileExists(fileName, view.attachSection.savedAttachments)) {
        isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmUploadOverwrite'), function (clickedOK) {
          if (clickedOK !== true) {
            return;
          }
          view.submitFile(fileName);
        });
      } else {
        view.submitFile(fileName);
      }

    }

    submitbutton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_AttachmentSubmit'),
      click: doClick,
      view: view
    });
    view.firstFocusedItem = submitbutton;
    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      realTitle: '',
      click: function () {
        view.closeClick();
      }
    });

    buttons.push(isc.LayoutSpacer.create({}));
    buttons.push(submitbutton);
    buttons.push(isc.LayoutSpacer.create({}));
    buttons.push(cancelButton);
    buttons.push(isc.LayoutSpacer.create({}));

    return buttons;
  },

  submitFile: function (fileName) {
    var form = this.theForm;
    var hTempLayout = isc.HLayout.create();
    this.attachSection.addMember(hTempLayout, this.attachSection.getMembers().size());
    var uploadingFile = isc.Label.create({
      contents: fileName
    });
    var uploading = isc.Label.create({
      className: 'OBLinkButtonItemFocused',
      contents: '    ' + OB.I18N.getLabel('OBUIAPP_AttachmentUploading')
    });
    hTempLayout.addMember(uploadingFile);
    hTempLayout.addMember(uploading);
    var button = this.attachSection.getForm().view.toolBar.getLeftMember(isc.OBToolbar.TYPE_ATTACHMENTS);
    if (!button) {
      button = this.attachSection.getForm().view.toolBar.getLeftMember("attachExists");
    }
    button.customState = 'Progress';
    button.resetBaseStyle();
    if (OB.Utilities.currentUploader !== null) {
      var curAttachSection = window[OB.Utilities.currentUploader];
      if (curAttachSection && curAttachSection.resetToolbar) {
        curAttachSection.resetToolbar();
      }
    }
    OB.Utilities.currentUploader = this.attachSection.ID;
    form.submitForm();
    this.closeClick();
  },

  editFile: function () {
    var form = this.theForm,
        allProperties = {},
        params = {
        tabId: this.attachSection.tabId,
        attachmentId: this.attachmentId,
        attachmentName: this.attachmentName,
        attachmentMethod: this.attachmentMethod
        };
    allProperties._params = this.getContextInfo();
    allProperties.action = 'EDIT';

    OB.RemoteCallManager.call('org.openbravo.client.application.window.AttachmentsAH', allProperties, params, function (response, data, request) {
      OB.Utilities.uploadFinished(data.buttonId, data);
      if (data.status === -1) {
        OB.Utilities.writeErrorMessage(data.viewId, data.errorMessage);
      }
    });
    this.closeClick();
  }

});