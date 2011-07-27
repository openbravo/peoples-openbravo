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

//== OBImageItemSmallImage ==
//This class is used for the small image shown within the OBImageItemSmallImageContainer
isc.ClassFactory.defineClass('OBImageItemSmallImage', isc.Img);

isc.OBImageItemSmallImage.addProperties({
  imageType: "stretch"
});

//== OBImageItemSmallImageContainer ==
//This class is used for the small image container box
isc.ClassFactory.defineClass('OBImageItemSmallImageContainer', isc.HLayout);

isc.OBImageItemSmallImageContainer.addProperties({
  imageItem: null,
  click: function() {
    var imageId=this.imageItem.getValue();
    if (!imageId) {
      return;
    }
    var d = {
      inpimageId: imageId,
      command: 'GETSIZE'
    };
    OB.RemoteCallManager.call('org.openbravo.client.application.window.ImagesActionHandler', {}, d, function(response, data, request){
      var pageHeight = Page.getHeight()-100;
      var pageWidth = Page.getWidth()-100;
      var height;
      var width;
      var ratio = data.width/data.height;
      if (ratio > pageWidth/pageHeight) {
        width = data.width > pageWidth?pageWidth:data.width;
        height = width/ratio;
      } else {
        height = data.height > pageHeight?pageHeight:data.height;
        width = height*ratio;
      }
      var imagePopup = isc.OBPopup.create({
        height: height,
        width: width,
        showMinimizeButton : false,
        showMaximizeButton : false
      });
      var image = isc.OBImageItemBigImage.create({
        popupContainer: imagePopup,
        height: height,
        width: width,
        click: function() { this.popupContainer.closeClick(); },
        src: "../utility/ShowImage?id=" + imageId + '&nocache=' + Math.random()
      });
      image.setImageType('stretch');
      imagePopup.addItem(image);
      imagePopup.show();
    });
  }
});

//== OBImageItemBigImage ==
//This class is used for the big image shown within the popup
isc.ClassFactory.defineClass('OBImageItemBigImage', isc.Img);

//== OBImageItemButton ==
//This class is used for the buttons shown in the OBImageItem
isc.ClassFactory.defineClass('OBImageItemButton', isc.ImgButton);

isc.OBImageItemButton.addProperties({
  initWidget: function() {
    this.initWidgetStyle();
    return this.Super('initWidget', arguments);
  }
});

//== OBImageCanvas ==
//This canvas contains the image shown in the OBImageItem, and the two buttons
//which are used to upload and delete images.
isc.ClassFactory.defineClass('OBImageCanvas', isc.HLayout);

OBImageCanvas.addProperties({
    height: '0px',
    initWidget: function(){
      var imageLayout = isc.OBImageItemSmallImageContainer.create({
        imageItem: this.creator
      });
      if (this.creator.required) {
        imageLayout.setStyleName(imageLayout.styleName + 'Required');
      } else {
        imageLayout.setStyleName(imageLayout.styleName);
      }
      this.addMember(imageLayout);
      this.image=isc.OBImageItemSmallImage.create({
        width: '100%'
      });
      imageLayout.addMember(this.image);
      this.image.setSrc('../web/skins/ltr/Default/Common/Image/imageNotAvailable_medium.png');
      var buttonLayout = isc.VLayout.create({
          width: '1%'
      });
      var selectorButton = isc.OBImageItemButton.create({
          buttonType: 'upload',
          imageItem: this.creator,
          action: function(){
            var selector = isc.OBImageSelector.create({
              columnName: this.imageItem.columnName,
              form: this.imageItem.form,
              imageItem: this.imageItem
            });
            selector.show();
          }
      });
      var deleteButton = isc.OBImageItemButton.create({
        buttonType: 'erase',
        imageItem: this.creator,
        deleteFunction: function(){
          var imageId = this.imageItem.getValue();
          var view = this.imageItem.form.view;
          var d = {
                  inpimageId: this.imageItem.getValue(),
                  inpTabId: this.imageItem.form.view.tabId,
                  inpColumnName: this.imageItem.columnName,
                  parentObjectId: this.imageItem.form.values.id,
                  command: 'DELETE'
          };
          var imageItem = this.imageItem;
          isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmDeleteImage'), function(clickedOK){
            if(clickedOK){
              OB.RemoteCallManager.call('org.openbravo.client.application.window.ImagesActionHandler', {}, d, function(response, data, request){
                 imageItem.refreshImage();
              });
            }
          });
        },
        click: function(form, item){
          //On click, we autosave before showing the selector,
          //because after delete we will reload the form
          if (!this.imageItem.form.validateForm()) {
            return;
          }
          var actionObject = {
            target: this,
            method: this.deleteFunction,
            parameters: []
          };
          this.imageItem.form.view.standardWindow.doActionAfterAutoSave(actionObject, true, true);
        },
        updateState: function(value){
          if(value){
            this.setDisabled(false);
          }else{
            this.setDisabled(true);
          }
        }
      });
      this.deleteButton = deleteButton;
      buttonLayout.addMember(selectorButton);
      buttonLayout.addMember(deleteButton);
      this.addMember(buttonLayout);
    },
    setImage: function(url){
      this.image.setSrc(url);
      if(url.contains('ShowImage')){
        this.image.setImageType('stretch');
      }else{
        this.image.setImageType('normal');
      }
    }
});

// == OBImageItem ==
// Item used for Openbravo ImageBLOB images.
isc.ClassFactory.defineClass('OBImageItem', CanvasItem);

isc.OBImageItem.addProperties({
  shouldSaveValue: true,
  canvasConstructor: 'OBImageCanvas',
  setValue: function(newValue){
    if(!newValue || newValue === ''){
      this.canvas.setImage('../web/skins/ltr/Default/Common/Image/imageNotAvailable_medium.png');
    }else{
      this.canvas.setImage("../utility/ShowImage?id="+newValue+'&nocache='+Math.random());
      var d = {
        inpimageId: newValue,
        command: 'GETSIZE'
      };
      var image = this.canvas.image;
      OB.RemoteCallManager.call('org.openbravo.client.application.window.ImagesActionHandler', {}, d, function(response, data, request){
          var ratio = data.width/data.height;
          if(ratio>5){
            var maxwidth = data.width>350?350:data.width;
            image.setHeight(maxwidth/ratio);
            image.setWidth(maxwidth+'px');
          }else{
            var maxheight = data.height>45?45:data.height;
            image.setHeight(maxheight+'px');
            image.setWidth(maxheight*ratio);
          }
      });
    }
    this.canvas.deleteButton.updateState(newValue);
    return this.Super('setValue', arguments);
  },
  refreshImage: function(imageId){
    if(imageId){
      //If creating/replacing an image, the form is marked as modified
      //and the image id is set as the value of the item
      this.setValue(imageId);
      this.form.itemChangeActions();
    }else{
      //However, if the image is being deleted, we reload the form,
      //as the process has automatically changed the record and we need to reload
      //to avoid concurrent changes problem
      this.setValue(imageId);
      this.form.view.refresh();
    }
  },
  changed: function (form, item, value) {
    form.setValue(identifierFieldName, value);
    return this.Super('changed', arguments);
  }

});

//== OBImageSelector ==
//This class displays a selector in a popup which can be used to upload images
isc.defineClass('OBImageSelector', isc.OBPopup);

isc.OBImageSelector.addProperties({
  submitButton: null,
  addForm: null,
  showMinimizeButton : false,
  showMaximizeButton : false,
  height: '40px',
  width: '200px',
  align: 'center',
  title: OB.I18N.getLabel('OBUIAPP_ImageSelectorTitle'),
  initWidget: function(args){
    var imageId = this.imageItem.getValue();
    var view = args.form.view;
    var form = isc.DynamicForm.create({
      fields: [
        {name: 'inpFile', title: OB.I18N.getLabel('OBUIAPP_ImageFile'), type: 'upload', canFocus: false, align:'right'},
        {name: 'Command', type: 'hidden', value: 'SAVE_OB3'},
        {name: 'inpColumnName', type: 'hidden', value: args.columnName},
        {name: 'inpTabId', type: 'hidden', value: view.tabId},
        {name: 'inpadOrgId', type: 'hidden', value: args.form.values.organization},
        {name: 'parentObjectId', type: 'hidden', value: args.form.values.id},
        {name: 'imageId', type: 'hidden', value: imageId},
        {name: 'inpSelectorId', type: 'hidden', value: this.ID}
      ],
      height: '20px',
      encoding: 'multipart',
      action: 'utility/ImageInfoBLOB',
      target: "background_target",
      redraw: function(){
      }
    });
    
    if(imageId){
      var deletebutton = isc.OBFormButton.create({
        title: OB.I18N.getLabel('OBUIAPP_Delete'),
        action: function(){
          form.getField('Command').setValue('DELETE_OB3');
          form.submitForm();
        }
      });
    }
    var closebutton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Close'),
      thisPopup: this,
      action: function(){
        this.thisPopup.hide();
      }
    });
    var uploadbutton = isc.OBFormButton.create({
        title: OB.I18N.getLabel('OBUIAPP_Upload'),
        action: function(){
          var value = form.getItem('inpFile').getElement().value;
          if(!value){
              return;
          }
          if(!form.getField('imageId').getValue()){
            form.getField('Command').setValue('SAVE_OB3');
            form.submitForm();
          }else{
            var theForm = form;
            isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmOverwriteImage'), function(clickedOK){
              if(clickedOK){
                theForm.getField('Command').setValue('SAVE_OB3');
                theForm.submitForm();
              }
            });
          }
        }
    });
    this.addItem(
      isc.HLayout.create({
        width: '100%',
          height: '20px',
        layoutTopMargin: this.hlayoutTopMargin,
        align: 'center',
        members: [
          form,
          uploadbutton
        ]
      })
    );
    this.Super('initWidget', arguments);
  },
  callback: function(imageId){
      this.imageItem.refreshImage(imageId);
      this.hide();
  }
});
