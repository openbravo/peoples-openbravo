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

isc.OBStatusBarLeftBar.addProperties( {
  // to allow setting the active view when clicking in the statusbar
    canFocus : true
  });

isc.ClassFactory.defineClass('OBStatusBarTextLabel', isc.Label);

isc.OBStatusBarTextLabel.addProperties( {
  // to allow setting the active view when clicking in the statusbar
    canFocus : true
  });

isc.ClassFactory.defineClass('OBStatusBarIconButtonBar', isc.HLayout);

isc.OBStatusBarIconButtonBar.addProperties( {
  // to allow setting the active view when clicking in the statusbar
    canFocus : true
  });

isc.ClassFactory.defineClass('OBStatusBarIconButton', isc.ImgButton);

isc.OBStatusBarIconButton.addProperties( {
  buttonType : null,
  view : null,
  // to allow setting the active view when clicking in the statusbar
  canFocus : true,

  // always go through the autosave of the window
  action : function() {
    // don't do autosave if new and nothing changed
    if (this.buttonType === 'close' && !this.view.viewForm.hasChanged && this.view.viewForm.isNew) {
      this.view.standardWindow.setDirtyEditForm(null);
    }
    var actionObject = {
      target : this,
      method : this.doAction,
      parameters : []
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
  },

  doAction : function() {
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

  initWidget : function() {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }

});

isc.ClassFactory.defineClass('OBStatusBar', isc.HLayout);

isc.OBStatusBar.addProperties( {
  view : null,
  iconButtonGroupSpacerWidth : 0, // Set in the skin

  nextButton : null,
  previousButton : null,
  newIcon : null,
  showingIcon : false,

  initWidget : function() {
    this.stateLabel = isc.OBStatusBarTextLabel.create( {
      contents : '&nbsp;',
      width : '100%',
      height : '100%'
    });

    this.leftStatusBar = isc.OBStatusBarLeftBar.create( {});
    this.leftStatusBar.addMember(this.stateLabel);

    this.previousButton = isc.OBStatusBarIconButton.create( {
      view : this.view,
      buttonType : 'previous',
      prompt : OB.I18N.getLabel('OBUIAPP_PREVIOUSBUTTON')
    });
    this.nextButton = isc.OBStatusBarIconButton.create( {
      view : this.view,
      buttonType : 'next',
      prompt : OB.I18N.getLabel('OBUIAPP_NEXTBUTTON')
    });
    this.closeButton = isc.OBStatusBarIconButton.create( {
      view : this.view,
      buttonType : 'close',
      prompt : OB.I18N.getLabel('OBUIAPP_CLOSEBUTTON')
    });
    this.maximizeButton = isc.OBStatusBarIconButton.create( {
      view : this.view,
      buttonType : 'maximize',
      prompt : OB.I18N.getLabel('OBUIAPP_MAXIMIZEBUTTON')
    });
    this.restoreButton = isc.OBStatusBarIconButton.create( {
      visibility : 'hidden',
      view : this.view,
      buttonType : 'restore',
      prompt : OB.I18N.getLabel('OBUIAPP_RESTOREBUTTON')
    });
    var buttonSpacer = isc.HLayout.create( {
      width : this.iconButtonGroupSpacerWidth
    });
    var buttonBar = isc.OBStatusBarIconButtonBar.create( {});

    buttonBar.addMembers( [ this.previousButton, this.nextButton, buttonSpacer,
        this.maximizeButton, this.restoreButton, this.closeButton ]);
    for ( var i = 0; i < buttonBar.members.length; i++) {
      if (buttonBar.members[i].buttonType) {
        OB.TestRegistry.register(
            'org.openbravo.client.application.statusbar.button.' + buttonBar.members[i].buttonType + '.' + this.view.tabId,
            buttonBar.members[i]);
      }
    }

    this.checkedIcon = isc.Img.create(this.checkedIconDefaults);
    this.newIcon = isc.Img.create(this.newIconDefaults);
    this.spacer = isc.LayoutSpacer.create( {
      width : 14
    });
    this.leftStatusBar.addMember(this.spacer, 0);

    this.addMembers( [ this.leftStatusBar, buttonBar ]);
    this.Super('initWidget', arguments);
  },

  addIcon : function(icon) {
    // remove any existing icon or spacer
  this.leftStatusBar.removeMember(this.leftStatusBar.members[0]);
  this.leftStatusBar.addMember(icon, 0);
},

removeIcon : function() {
  // remove any existing icon or spacer
  this.leftStatusBar.removeMember(this.leftStatusBar.members[0]);
  this.leftStatusBar.addMember(this.spacer, 0);
},

setNewState : function(isNew) {
  this.previousButton.setDisabled(isNew);
  this.nextButton.setDisabled(isNew);
  if (isNew) {
    this.setStateLabel('OBUIAPP_New', this.newIcon);
  }
},

setStateLabel : function(labelCode, icon) {
  var msg = '&nbsp;';
  if (labelCode) {
    msg = OB.I18N.getLabel(labelCode);
  }
  this.stateLabel.setContents(msg);
  if (icon) {
    this.addIcon(icon);
  } else {
    this.removeIcon(icon);
  }
}

});
