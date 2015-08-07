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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//== OBFileItemLink ==
//This class is used for the link shown within the OBFileItemContainer
isc.ClassFactory.defineClass('OBFileItemLink', isc.HTMLFlow);

isc.OBFileItemLink.addProperties({
  setLink: function (text, url) {
    this.setContents('<a class="' + this.linkStyleName + '" href="' + url + '" id="' + this.randomId + '" target="_blank">' + text + '</a>');
  }
});

//== OBFileItemSize ==
//This class is used for the size label shown within the OBFileItemContainer
isc.ClassFactory.defineClass('OBFileItemSize', isc.Label);

//== OBFileItemExt ==
//This class is used for the extension shown within the OBFileItemContainer
isc.ClassFactory.defineClass('OBFileItemExt', isc.Label);

isc.OBFileItemExt.addProperties({
  setContents: function (ext) {
    var baseStyle;
    if (typeof this.baseStyle === 'string') {
      baseStyle = this.baseStyle.substring(0, (this.baseStyle.indexOf(' ') !== -1 ? this.baseStyle.indexOf(' ') : this.baseStyle.length));
    }
    if (!ext) {
      ext = '';
    }
    ext = ext.substring(0, 3).toUpperCase();
    this.baseStyle = baseStyle + ' ' + baseStyle + '_' + ext;
    return this.Super('setContents', arguments);
  }
});

//== OBFileItemContainer ==
//This class is used for the file name + file size + file extension container box
isc.ClassFactory.defineClass('OBFileItemContainer', isc.HLayout);

isc.OBFileItemContainer.addProperties({
  fileItem: null,
  initWidget: function () {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});

//== OBFileItemButton ==
//This class is used for the buttons shown in the OBFileItem
isc.ClassFactory.defineClass('OBFileItemButton', isc.ImgButton);

isc.OBFileItemButton.addProperties({
  initWidget: function () {
    this.initWidgetStyle();
    return this.Super('initWidget', arguments);
  }
});

//== OBFileCanvas ==
//This canvas contains the OBFileItemContainer shown in the OBFileItem, and the two buttons
//which are used to upload and delete files.
isc.ClassFactory.defineClass('OBFileCanvas', isc.HLayout);

isc.OBFileCanvas.addProperties({
  initWidget: function () {
    var me = this;
    this.Super('initWidget', arguments);
    this.containerLayout = isc.OBFileItemContainer.create({
      fileItem: this.creator
    });
    if (this.creator.required) {
      this.containerLayout.setStyleName(this.containerLayout.styleName + 'Required');
    }
    if (this.creator.disabled) {
      this.containerLayout.setStyleName(this.containerLayout.styleName + 'Disabled');
    }
    if (this.creator.readOnly) {
      this.containerLayout.setStyleName(this.containerLayout.styleName + 'Disabled');
    }

    this.size = isc.OBFileItemSize.create({});
    this.ext = isc.OBFileItemExt.create({});
    this.link = isc.OBFileItemLink.create({
      randomId: OB.Utilities.generateRandomString(8, true, true, false, false),
      mouseOver: function () {
        if (document.getElementById(this.randomId).offsetWidth < document.getElementById(this.randomId).scrollWidth) {
          me.size.hide();
        }
        return this.Super('mouseOver', arguments);
      },
      mouseOut: function () {
        me.size.show();
        return this.Super('mouseOut', arguments);
      }
    });

    this.containerLayout.addMember(this.link);
    this.containerLayout.addMember(this.size);
    this.containerLayout.addMember(this.ext);
    this.containerLayout.addMember(isc.HLayout.create({
      width: 4
    }));
    this.addMember(this.containerLayout);
    var buttonLayout = isc.HLayout.create({
      width: '1%'
    });
    var selectorButton = isc.OBFileItemButton.create({
      buttonType: 'upload',
      fileItem: this.creator,
      action: function () {
        var selector = isc.OBFileSelector.create({
          columnName: this.fileItem.columnName,
          form: this.fileItem.form,
          fileItem: this.fileItem
        });
        var title = OB.I18N.getLabel('OBUIAPP_FileSelectorTitle'),
            height = selector.height,
            width = selector.width,
            showMinimizeButton = false,
            showMaximizeButton = false;
        if (this.fileItem && this.fileItem.form && this.fileItem.form.view && this.fileItem.form.view.standardWindow && this.fileItem.form.view.standardWindow.openPopupInTab) {
          this.fileItem.form.view.standardWindow.openPopupInTab(selector, title, width, height, showMaximizeButton, showMaximizeButton, true, true, this.fileItem.form);
        } else {
          var selectorContainer = isc.OBPopup.create({
            showMinimizeButton: showMinimizeButton,
            showMaximizeButton: showMaximizeButton,
            title: title,
            width: width,
            height: height,
            items: [selector]
          });
          selectorContainer.show();
        }
      },
      updateState: function (value) {
        if (value) {
          this.setDisabled(false);
        } else {
          this.setDisabled(true);
        }
      }
    });
    var deleteButton = isc.OBFileItemButton.create({
      buttonType: 'erase',
      fileItem: this.creator,
      deleteFunction: function () {
        var fileItem = this.fileItem,
            fileId = this.fileItem._value,
            isNewRecord = this.fileItem.form.isNewRecord();
        fileItem.refreshFile();

        // If the record is new and the file is deleted, remove it from the database
        if (isNewRecord) {
          OB.RemoteCallManager.call('org.openbravo.client.application.window.FileDeleteActionHandler', {
            'id': fileId
          });
        }
      },
      click: function (form, item) {
        this.deleteFunction();
      },
      updateState: function (value) {
        if (value) {
          this.setDisabled(false);
          this.show();
        } else {
          this.setDisabled(true);
          this.hide();
        }
      }
    });

    if (this.parentItem.isPreviewFormItem) {
      selectorButton.showDisabled = false;
      selectorButton.showDisabledIcon = false;
    }

    this.deleteButton = deleteButton;
    this.selectorButton = selectorButton;
    buttonLayout.addMember(selectorButton);
    buttonLayout.addMember(deleteButton);
    this.addMember(buttonLayout);
  },
  setFileInfo: function (name, url, size, ext) {
    if (!url) {
      this.link.setLink('', '');
      this.size.setContents('');
      this.ext.setContents('');
      this.link.hide();
      this.size.hide();
      this.ext.hide();
    } else {
      this.link.setLink(name, url);
      this.size.setContents(size);
      this.ext.setContents(ext);
      this.link.show();
      this.size.show();
      this.ext.show();
    }
  }
});

// == OBFileItem ==
// Item used for Openbravo FileBLOB reference.
isc.ClassFactory.defineClass('OBFileItem', isc.CanvasItem);

isc.OBFileItem.addProperties({
  shouldSaveValue: true,
  canvasConstructor: 'OBFileCanvas',
  init: function () {
    this.canvasProperties = this.canvasProperties || {};
    this.canvasProperties.parentItem = this;
    this.Super('init', arguments);
  },
  //This formitem will never be disabled, so even if the form is readonly, click events will still be triggered
  isDisabled: function () {
    return false;
  },
  setValue: function (newValue) {
    var canvas = this.canvas;
    if (!newValue || newValue === '') {
      canvas.setFileInfo();
    } else {
      var d = {
        inpfileId: newValue,
        command: 'GETFILEINFO'
      };

      OB.RemoteCallManager.call('org.openbravo.client.application.window.FileActionHandler', {}, d, function (response, data, request) {
        var fileName = data.name;
        var fileSize = data.displaysize;
        var fileExt = data.ext;
        if (fileName) {
          canvas.setFileInfo(fileName, "utility/GetFile?id=" + newValue + '&nocache=' + Math.random(), fileSize, fileExt);
        } else {
          canvas.setFileInfo();
        }
      });
    }
    //Buttons will not be shown if the form is readonly
    canvas.deleteButton.updateState(newValue && (this.form && !this.form.readOnly) && !this.disabled);
    canvas.selectorButton.updateState((this.form && !this.form.readOnly) && !this.disabled);
    return this.Super('setValue', arguments);
  },
  refreshFile: function (fileId) {
    //If creating/replacing a file, the form is marked as modified
    //and the file id is set as the value of the item
    if (typeof fileId === 'undefined') {
      fileId = '';
    }
    this.setValue(fileId);
    this.form.itemChangeActions();
  },
  //This function has been overwritten because this class needs to do specific things if the object is
  //disabled. It is necessary to hide the delete and selector buttons when the status is disabled 
  //and to show them when enabled.
  setDisabled: function (disabled) {
    if (disabled) {
      this.canvas.deleteButton.hide();
      this.canvas.selectorButton.hide();
    } else {
      this.canvas.deleteButton.show();
      this.canvas.selectorButton.show();
    }
    this.Super('setDisabled', arguments);
  }
});

//== OBFileSelector ==
//This class displays a selector in a popup which can be used to upload files
isc.defineClass('OBFileSelector', isc.VLayout);

isc.OBFileSelector.addProperties({
  submitButton: null,
  addForm: null,
  initWidget: function (args) {
    var fileId = this.fileItem.getValue();
    var view = args.form.view;
    var imageSizeAction = this.fileItem.imageSizeValuesAction;
    var imageWidthValue = this.fileItem.imageWidth;
    imageWidthValue = parseInt(imageWidthValue, 10);
    if (!imageWidthValue) {
      imageWidthValue = 0;
    }
    var imageHeightValue = this.fileItem.imageHeight;
    imageHeightValue = parseInt(imageHeightValue, 10);
    if (!imageHeightValue) {
      imageHeightValue = 0;
    }
    var form = isc.DynamicForm.create({
      autoFocus: true,
      fields: [{
        name: 'inpFile',
        title: OB.I18N.getLabel('OBUIAPP_FileFile'),
        type: 'upload',
        canFocus: false,
        align: 'right'
      }, {
        name: 'Command',
        type: 'hidden',
        value: 'SAVE_OB3'
      }, {
        name: 'inpColumnName',
        type: 'hidden',
        value: args.columnName
      }, {
        name: 'inpTabId',
        type: 'hidden',
        value: view.tabId
      }, {
        name: 'inpadOrgId',
        type: 'hidden',
        value: args.form.values.organization
      }, {
        name: 'parentObjectId',
        type: 'hidden',
        value: args.form.values.id
      }, {
        name: 'fileId',
        type: 'hidden',
        value: fileId
      }, {
        name: 'imageSizeAction',
        type: 'hidden',
        value: imageSizeAction
      }, {
        name: 'imageWidthValue',
        type: 'hidden',
        value: imageWidthValue
      }, {
        name: 'imageHeightValue',
        type: 'hidden',
        value: imageHeightValue
      }, {
        name: 'inpSelectorId',
        type: 'hidden',
        value: this.ID
      }],
      height: '20px',
      encoding: 'multipart',
      action: 'utility/FileInfoBLOB',
      target: "background_target",
      redraw: function () {}
    });
    this.formDeleteFile = isc.DynamicForm.create({
      fields: [{
        name: 'Command',
        type: 'hidden',
        value: 'DELETE_OB3'
      }, {
        name: 'inpTabId',
        type: 'hidden',
        value: view.tabId
      }, {
        name: 'fileId',
        type: 'hidden',
        value: fileId
      }],
      height: '1px',
      width: '1px',
      encoding: 'normal',
      action: 'utility/FileInfoBLOB',
      target: "background_target",
      redraw: function () {}
    });

    var uploadbutton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Upload'),
      action: function () {
        var value = form.getItem('inpFile').getElement().value;
        if (!value) {
          return;
        }
        form.getField('Command').setValue('SAVE_OB3');
        form.submitForm();
      }
    });
    //TODO: The message (if apply) should notify the proper restrictions.
    var messageBarText = this.getMessageText('Warn', imageSizeAction, imageWidthValue, imageHeightValue);

    var messageBar = isc.OBMessageBar.create({
      visibility: 'hidden'
    });
    messageBar.setType(isc.OBMessageBar.TYPE_WARNING);
    messageBar.setText(null, messageBarText);
    messageBar.hideCloseIcon();
    if (messageBarText && (imageWidthValue || imageHeightValue)) {
      messageBar.show();
    }

    this.addMembers([
    isc.HLayout.create({
      width: '100%',
      height: 1,
      align: 'center',
      members: [
      messageBar]
    }), isc.HLayout.create({
      width: '100%',
      height: 20,
      layoutTopMargin: this.hlayoutTopMargin,
      layoutBottomMargin: this.hlayoutBottomMargin,
      align: 'center',
      members: [
      form, uploadbutton, this.formDeleteFile]
    })]);
    this.Super('initWidget', arguments);
  },
  getMessageText: function (type, imageSizeAction, fileName, size, msgInfo) {
    var message = '';
    if (imageSizeAction === 'N') {
      return message;
    } else {
      message = OB.I18N.getLabel('OBUIAPP_Image_' + type + '_' + imageSizeAction, [msgInfo]);
      // message = message.replace('XXX', XXX).replace('YYY', YYY).replace('AAA', AAA).replace('BBB', BBB);
      message = message.replace(/\n/g, '<br />');
      return message;
    }
  },
  deleteTempFile: function (fileId) {
    if (fileId) {
      this.formDeleteFile.getField('fileId').setValue(fileId);
      this.formDeleteFile.submitForm();
    }
  },
  callback: function (fileId, imageSizeAction, fileName, size, msgInfo) {
    size = parseInt(size, 10);
    var selector = this;
    if (imageSizeAction === 'WRONGFORMAT' || imageSizeAction === 'ERROR_UPLOADING') {
      isc.warn(this.getMessageText('Error', imageSizeAction, fileName, size, msgInfo), function () {
        return true;
      }, {
        icon: '[SKINIMG]Dialog/error.png',
        title: OB.I18N.getLabel('OBUIAPP_Error')
      });
    } else {
      this.refreshFile(fileId);
    }
  },
  refreshFile: function (fileId) {
    this.fileItem.refreshFile(fileId);
    this.parentElement.parentElement.closeClick();
  }
});