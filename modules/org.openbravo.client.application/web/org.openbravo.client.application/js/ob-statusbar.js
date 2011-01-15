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
isc.ClassFactory.defineClass('OBStatusBarLeftBar', isc.HLayout);

isc.OBStatusBarLeftBar.addProperties({
  // to allow setting the active view when clicking in the statusbar 
  canFocus: true
});

isc.ClassFactory.defineClass('OBStatusBarTextLabel', isc.Label);

isc.OBStatusBarTextLabel.addProperties({
  // to allow setting the active view when clicking in the statusbar 
  canFocus: true
});

isc.ClassFactory.defineClass('OBStatusBarIconButtonBar', isc.HLayout);

isc.OBStatusBarIconButtonBar.addProperties({
  // to allow setting the active view when clicking in the statusbar 
  canFocus: true
});

isc.ClassFactory.defineClass('OBStatusBarIconButton', isc.ImgButton);

isc.OBStatusBarIconButton.addProperties({
  buttonType: null,
  view: null,
  // to allow setting the active view when clicking in the statusbar 
  canFocus: true,
  
  // always go through the autosave of the form
  action: function() {
    var actionObject = {
      target: this,
      method: this.doAction,
      parameters: []
    };
    this.view.viewForm.autoSave(actionObject);
  },
  
  doAction: function(){
    var rowNum, newRowNum, newRecord;
    if (this.buttonType === 'previous') {
      this.view.editNextPreviousRecord(false);
    } else if (this.buttonType === 'maximize') {
      this.view.maximize();
    } else if (this.buttonType === 'restore') {
      this.view.restore();
    } else if (this.buttonType === 'next') {
      this.view.editNextPreviousRecord(true);
    } else if (this.buttonType === 'close') {
      this.view.switchFormGridVisibility();
      this.view.messageBar.hide();
    }
  },
  
  initWidget: function(){
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
  
});


isc.ClassFactory.defineClass('OBStatusBar', isc.HLayout);

isc.OBStatusBar.addProperties({
  view: null,
  iconButtonGroupSpacerWidth: 0, //Set in the skin
  
  nextButton: null,
  previousButton: null,
  newIcon: null,
  leftBar: null,
  
  initWidget: function(){
    this.leftBar = isc.OBStatusBarLeftBar.create({
    });
//    leftBar.addMember(isc.OBStatusBarTextLabel.create({
//      contents: '' //'Status:'
//    }));
    
    this.previousButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'previous'
    });
    this.nextButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'next'
    });
    var closeButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'close'
    });
    this.maximizeButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'maximize'
    });
    this.restoreButton = isc.OBStatusBarIconButton.create({
      visibility: 'hidden',
      view: this.view,
      buttonType: 'restore'
    });
    var buttonSpacer = isc.HLayout.create({
      width: this.iconButtonGroupSpacerWidth
    });
    var buttonBar = isc.OBStatusBarIconButtonBar.create({});
    
    buttonBar.addMembers([this.previousButton, this.nextButton, buttonSpacer, this.maximizeButton, this.restoreButton, closeButton]);
    for (var i = 0; i < buttonBar.members.length; i++) {
      if (buttonBar.members[i].buttonType) {
        OB.TestRegistry.register('org.openbravo.client.application.statusbar.button.' + buttonBar.members[i].buttonType + '.' + this.view.tabId, buttonBar.members[i]);        
      }
    }
    this.addMembers([this.leftBar, buttonBar]);
  },
  
  setNewIcon: function(show) {
    if (show) {      
      if (!this.newIcon) {
        this.newIcon = isc.Img.create(this.newIconDefaults);
      }
      this.leftBar.addMember(this.newIcon, 0);
    } else if (this.newIcon) {
      this.leftBar.removeMember(this.newIcon);
    }
  },
  
  setNewState: function(isNew) {
    this.previousButton.setDisabled(isNew);
    this.nextButton.setDisabled(isNew);
    this.setNewIcon(isNew);
  }
  
});
