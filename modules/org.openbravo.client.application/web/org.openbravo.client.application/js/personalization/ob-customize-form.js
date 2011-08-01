/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
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

isc.ClassFactory.defineClass('OBCustomizeFormLayout', isc.VLayout);

isc.OBCustomizeFormLayout.addProperties({

  tabTitle: null,
  contextTitle: null,

  // is set when called from the personalization grid
  personalizationId: null,
  
  clientIdentifier: null,
  orgIdentifier: null,
  roleIdentifier: null,
  userIdentifier: null,
  
  clientId:null,
  orgId: null,
  roleId: null,
  userId: null,

  isNew: false,
  isChanged: false,
  isSaved: false,
  
  hasBeenSaved: false,
  
  // the form instance shown to the user
  demoForm: null,
  // retrieved from the server
  demoFormProperties: null,
  
  initWidget: function() {
    
    // if no personalization data then we need to compute it from the form
    if (!this.personalizationData) {
      this.personalizationData = OB.Personalization.getDataStructureFromForm(this.form);
    }

    if (this.personalizationData) {
      this.isNew = !this.personalizationData.personalizationId;
    }
    
    this.createAddToolbar();
    this.createAddStatusbar();
    this.createAddMainLayout();
    this.Super('initWidget', arguments);
  },

  createAddMainLayout: function() {
    var data, mainLayout = isc.VLayout.create({}, OB.Styles.Personalization.MainLayout), 
        fieldsLayout = isc.Layout.create({height: '100%'}, OB.Styles.Personalization.FieldsLayout);
    
    this.creatingMainLayout = true;
    
    this.managementLayout = isc.HLayout.create({height: '100%', width: '100%'}, OB.Styles.Personalization.ManagementLayout);
    
    data = this.personalizationData;
    
    if (data.form) {
      data = data.form;
    }
    if (data.fields) {
      data = data.fields;
    }

    this.fieldsTreeGrid = isc.OBPersonalizationTreeGrid.create({
      fieldData: data,
      customizeForm: this,
      selectionUpdated: function(record, recordList) {
        this.customizeForm.selectionUpdated(record, recordList);
      }
    });
    
    fieldsLayout.addMember(this.fieldsTreeGrid);
    this.managementLayout.addMember(fieldsLayout);
    this.managementLayout.addMember(this.createPropertiesLayout());
    mainLayout.addMember(this.managementLayout);
    this.mainLayout = mainLayout;
    this.addMember(mainLayout);
    delete this.creatingMainLayout;
  },

  createPropertiesLayout: function() {
    var NumericField, CheckboxField, 
      propertiesLayout = 
        isc.Layout.create(OB.Styles.Personalization.PropertiesLayout);
    
    // a backpointer
    propertiesLayout.customizeForm = this;
      
    CheckboxField = function(props){
      if (props) {
        isc.addProperties(this, props);
      }
    };
    CheckboxField.prototype = {
      cellStyle: OB.Styles.OBFormField.DefaultCheckbox.cellStyle,
      alwaysTakeSpace: false,
      titleStyle: OB.Styles.OBFormField.DefaultCheckbox.titleStyle,
      textBoxStyle: OB.Styles.OBFormField.DefaultCheckbox.textBoxStyle,
      showValueIconOver: OB.Styles.OBFormField.DefaultCheckbox.showValueIconOver,
      showValueIconFocused: OB.Styles.OBFormField.DefaultCheckbox.showValueIconFocused,
      showFocused: OB.Styles.OBFormField.DefaultCheckbox.showFocused,
      defaultValue: OB.Styles.OBFormField.DefaultCheckbox.defaultValue,
      checkedImage: OB.Styles.OBFormField.DefaultCheckbox.checkedImage,
      uncheckedImage: OB.Styles.OBFormField.DefaultCheckbox.uncheckedImage,
      titleOrientation: 'right',
      editorType: 'checkbox'
    };
    
    NumericField = function(props){
      if (props) {
        isc.addProperties(this, props);
      }
    };
    NumericField.prototype = {
      showFocused: true,
      alwaysTakeSpace: false,
      required: true,
      validateOnExit: true,
      showIcons: false,
      width: '*',
      titleOrientation: 'top',
      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',
      keyPressFilterNumeric: '[0-9]',
      editorType: 'OBTextItem'
    };

    propertiesLayout.formLayout = isc.VStack.create({
        align: 'center',
        overflow: 'visible',
        height: 1,
        visible: false,
        width: '100%'
      });
    
    propertiesLayout.formLayout.Title = isc.Label.create({
      width: '100%',
      height: 1,
      overflow: 'visible',
      baseStyle: OB.Styles.OBFormField.DefaultTextItem.titleStyle,
      contents: ''
    });
    
    propertiesLayout.formLayout.addMember(propertiesLayout.formLayout.Title);

    propertiesLayout.formLayout.form = isc.DynamicForm.create({
      customizeForm: this,
      overflow: 'visible',
      numCols: 1,
      width: '100%',
     
      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',
     
      errorsPreamble: '',
      showErrorIcons: false,
      showErrorStyle: true,
      showInlineErrors: true,
      fields: [
         new NumericField({
           name: 'colSpan',
           title: OB.I18N.getLabel('OBUIAPP_Personalization_Colspan')
         }),
         new NumericField({
           name: 'rowSpan',
           required: true,
           title: OB.I18N.getLabel('OBUIAPP_Personalization_Rowspan')
         }),
         new CheckboxField({
           name: 'startRow',
           title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow')
         }),
        new CheckboxField({
          name: 'hiddenInForm',
          title: OB.I18N.getLabel('OBUIAPP_Personalization_Hidden')
        }),
        new CheckboxField({
          name: 'firstFocus',
          title: OB.I18N.getLabel('OBUIAPP_Personalization_FirstFocus')
        })
      ],
      
      setRecord: function(record) {
        this.record = record;
        var i = 0;
        for (i = 0; i < this.getFields().length; i++) {
          this.setValue(this.getFields()[i].name, record[this.getFields()[i].name]);
        }
        
        // hide some fields
        if (record.isStaticStatusBarField) {
          this.hideItem('colSpan');
          this.hideItem('rowSpan');
          this.hideItem('firstFocus');
          this.hideItem('startRow');
        } else {
          this.showItem('colSpan');
          this.showItem('rowSpan');
          this.showItem('firstFocus');
          this.showItem('startRow');
        }
        
        this.rememberValues();
      },
      
      doSave: function() {
        var i, allNodes;
        this.validate();
        if (this.hasErrors()) {
          return;
        }
        // first get rid of all first focus if it was set now
        if (this.getValue('firstFocus')) {
          allNodes = this.customizeForm.fieldsTreeGrid.data.getAllNodes();
          for (i = 0; i < allNodes.length; i++) {
            if (allNodes[i].firstFocus) {
              allNodes[i].firstFocus = false;
            }
          }
        }
        
        // now it will be set, maximum one field will have 
        // the focus now
        isc.addProperties(this.record, this.getValues());        
        this.buttons.changeButtonState(false);
        this.rememberValues();
        this.focus();
        
        // items may have been hidden, which changes their colour
        this.customizeForm.fieldsTreeGrid.markForRedraw();

        // this will reset everything
        this.customizeForm.changed();
      },
      
      doCancel: function() {
        this.reset();
        this.focus();
        this.buttons.changeButtonState(false);
      },
      
      itemChanged: function(item, newValue) {
        this.buttons.changeButtonState(true);
      }
    });
    
    propertiesLayout.formLayout.addMembers(propertiesLayout.formLayout.form);
    // and the button bar
    propertiesLayout.formLayout.buttons = isc.HStack.create({
      layoutTopMargin: 10,
      membersMargin: 10,
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%',
      
      changeButtonState: function(state) {
        var i = 0;
        for (i = 0; i < this.members.length; i++) {
          if (state) {
            this.members[i].enable();
          } else {
            this.members[i].disable();            
          }
        }
      }
    });
    propertiesLayout.formLayout.buttons.addMembers(isc.OBFormButton.create({
      container: propertiesLayout.formLayout.buttons,
      disabled: true,
      form: propertiesLayout.formLayout.form,
      title: OB.I18N.getLabel('OBUIAPP_Apply'),
      click: function(){
        this.form.doSave();
      }
    }));
    propertiesLayout.formLayout.buttons.addMembers(isc.OBFormButton.create({
      container: propertiesLayout.formLayout.buttons,
      form: propertiesLayout.formLayout.form,
      disabled: true,
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: function(){
        this.form.doCancel();
      }
    }));
    
    propertiesLayout.formLayout.form.buttons = propertiesLayout.formLayout.buttons;
    propertiesLayout.formLayout.addMembers(propertiesLayout.formLayout.buttons);
    
    propertiesLayout.emptyMessage = isc.Label.create({
      width: '100%',
      height: 1,
      overflow: 'visible',
      contents: OB.I18N.getLabel('OBUIAPP_Personalization_PropertiesFormEmptyMessage')
    });
    
    propertiesLayout.addMember(propertiesLayout.formLayout);
    propertiesLayout.addMember(propertiesLayout.emptyMessage);
    propertiesLayout.hideMember(propertiesLayout.formLayout);
    propertiesLayout.showMember(propertiesLayout.emptyMessage);
    
    propertiesLayout.updatePropertiesDisplay = function(record) {
      var newRecord;
      if (!record) {
        this.hideMember(this.formLayout);
        this.emptyMessage.show();
      } else if (record.isDynamicStatusBarField) {
        newRecord = this.customizeForm.fieldsTreeGrid.data.find('name', record.originalName);
        this.updatePropertiesDisplay(newRecord);
      } else {
        this.formLayout.Title.setContents(record.title);
        this.formLayout.form.setRecord(record);
        this.formLayout.form.buttons.changeButtonState(false);
        this.hideMember(this.emptyMessage);
        this.showMember(propertiesLayout.formLayout);
        this.formLayout.form.focus();
      }
    };
    
    this.propertiesLayout = propertiesLayout;
    
    return propertiesLayout;
  },
  
  createAddStatusbar: function() {
    var owner = this;
    this.statusBar = isc.OBStatusBar.create({
      view: this,

      addCreateButtons: function() {
        this.buttonBar.setWidth(1);
        this.buttonBar.setOverflow('visible');
        this.buttonBar.defaultLayoutAlign = 'center';
        var closeButton = isc.OBStatusBarIconButton.create({
          view: this.view,
          buttonType: 'close',
          keyboardShortcutId: 'StatusBar_Close',
          prompt: OB.I18N
              .getLabel('OBUIAPP_Personalization_Statusbar_Close'),
          action: function() {
            owner.doClose();
          }
        }, OB.Styles.Personalization.closeButtonProperties);
        this.buttonBar.addMembers([ closeButton ]);
      }
    });
    this.addMember(this.statusBar);
    
    this.setStatusBarInformation();
  },

  createAddToolbar: function() {
    var saveButtonProperties, deleteButtonProperties, cancelButtonProperties;

    saveButtonProperties = {
      action: function() {
        this.view.save();
      },
      disabled: true,
      buttonType: 'save',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Save'),
      updateState: function() {
        this.setDisabled(this.view.hasNotChanged());
      },
      keyboardShortcutId: 'ToolBar_Save'
    };

    deleteButtonProperties = {
        action: function(){
          this.view.deletePersonalization();
        },
        disabled: true,
        buttonType: 'eliminate',
        prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Delete'),
        updateState: function(){
          this.setDisabled(!this.view.personalizationData.canDelete);
        },
        keyboardShortcutId: 'ToolBar_Eliminate'
      };
    
    cancelButtonProperties = {
      action: function() {
        this.view.cancel();
      },
      disabled: true,
      buttonType: 'undo',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_CancelEdit'),
      updateState: function() {
        this.setDisabled(this.view.hasNotChanged());
      },
      keyboardShortcutId: 'ToolBar_Undo'
    };

    this.toolBar = isc.OBToolbar.create({
      view: this,
      leftMembers: [ isc.OBToolbarIconButton.create(saveButtonProperties),
          isc.OBToolbarIconButton.create(deleteButtonProperties),
          isc.OBToolbarIconButton.create(cancelButtonProperties) ],
      rightMembers: []
    });
    this.addMember(this.toolBar);
  },

  // toolbar logic
  save: function() {
    var params, me = this;
    this.isNew = false;
    this.isSaved = true;
    this.isChanged = false;
    this.hasBeenSaved = true;
    this.setStatusBarInformation();

    // store it!
    if (this.personalizationData.personalizationId) {
      params = {
          action: 'store',
          target: 'form',
          personalizationId: this.personalizationData.personalizationId
      };
      
    } else {
      params = {
          action: 'store',
          target: 'form',
          clientId: this.clientId,
          orgId: this.orgId,
          roleId: this.roleId,
          userId: this.userId,
          tabId: this.tabId
      };
    }

    // create it here
    if (!this.personalizationData) {
      this.personalizationData = {};
    }
    this.personalizationData.form = this.getPersonalizationData();
    
    // store the data
    OB.RemoteCallManager.call(
        'org.openbravo.client.application.personalization.PersonalizationActionHandler', 
        this.getPersonalizationData(), params,
        function(resp, data, req){
          if (data && data.canDelete) {
            me.personalizationData.canDelete = true;            
          }
          if (data && data.personalizationId) {
            me.personalizationData.personalizationId = data.personalizationId;            
          }
          
          me.toolBar.updateButtonState();
          
          // show the new data in the demo form
          me.refresh();
        });
  },

  deletePersonalization: function(confirmed) {
    var me = this;
    if (!this.personalizationData.personalizationId) {
      return;
    }

    if (!confirmed) {
      callback = function(ok) {
        if (ok) {
          me.deletePersonalization(true);
        }
      };
      
      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmDelete'), callback);
      return;
    }

    OB.RemoteCallManager.call(
        'org.openbravo.client.application.personalization.PersonalizationActionHandler', 
        {}, 
        { 
          personalizationId: this.personalizationData.personalizationId,
          action: 'delete'
        },
        function(resp, data, req){
          me.hasBeenDeleted = true;
          // close when returned
          me.doClose(true);
        }
     );
  },
  
  cancel: function(confirmed) {
    var me = this;
    if (!confirmed) {
      callback = function(ok) {
        if (ok) {
          me.cancel(true);
        }
      };
      
      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmCancel'), callback);
      return;
    }

    this.isChanged = false;
    this.isSaved = false;
    this.removeMember(this.mainLayout);
    this.mainLayout = null;
    this.setStatusBarInformation();
    this.createAddMainLayout();
    this.buildDemoForm();
  },

  // shows the settings in the demo form
  refresh: function() {
    this.buildDemoForm();
  },

  changed: function() {
    // nothing to do here yet
    if (!this.demoForm || this.creatingMainLayout) {
      return;
    }
    this.isChanged = true;
    this.isSaved = false;
    this.setStatusBarInformation();
    this.buildDemoForm();
  },
  
  hasNotChanged: function() {
    return !this.isChanged;
  },
  
  buildDemoForm: function() {
    var statusBar, i, fld, itemClick, me = this;
    
    if (this.formLayout) {
      this.managementLayout.removeMember(this.formLayout);
    }
    this.formLayout = isc.VLayout.create({ height: '100%', width: '100%'}, OB.Styles.Personalization.FormLayout);
    
    // add a status bar to the formlayout
    statusBar = isc.OBStatusBar.create({
      addCreateButtons: function() {
      }
    });
    this.formLayout.addMember(statusBar);
    
    // create the form and add it to the formLayout
    this.demoForm = isc.OBViewForm.create(this.demoFormProperties, {
      preventAllEvents: true,
      statusBar: statusBar,
      customizeForm: this,
      isDemoForm: true,
      
      // overridden to prevent js errors when switching views
      visibilityChange: function() {}, 
      
      titleHoverHTML: function(item){
        return this.customizeForm.getHoverHTML(item);
      },
      
      itemHoverHTML: function(item){
        return this.customizeForm.getHoverHTML(item);
      },
      
      // overridden to always show a statusbar field with some spaces
      getStatusBarFields: function() {
        var statusBarFields = [[],[]], i, item, value, tmpValue;
        for(i = 0; i < this.statusBarFields.length; i++) {
          item = this.getItem(this.statusBarFields[i]);
          statusBarFields[0].push(item.getTitle());
          statusBarFields[1].push('&nbsp;&nbsp&nbsp;');
       }
        return statusBarFields;
      }

    });
    
    itemClick = function(item) {
      if (item.parentItem) {
        me.doHandleDemoFormItemClick(item.parentItem);
      } else {
        me.doHandleDemoFormItemClick(item);
      }
    };
    
    var persData = this.getPersonalizationData();
    OB.Personalization.personalizeForm(persData, this.demoForm);
    
    // expand by default
    for (i = 0; i < this.demoForm.getFields().length; i++) {
      fld = this.demoForm.getFields()[i];
      
      fld.showFocused = false;
      
      if (fld.personalizable) {
        if (isc.isA.SectionItem(fld)) {
          fld.sectionExpanded = true;
        } else {
          // replace some methods so that clicking a field in the form
          // will select it on the left
          fld.handleClick = itemClick;
          fld.iconClick = itemClick;
          fld.handleTitleClick = itemClick;
          fld.linkButtonClick = itemClick;
        }
      }
    }
    
    this.formLayout.addMember(this.demoForm);
    
    this.managementLayout.addMember(this.formLayout, 1);
  },
  
  doHandleDemoFormItemClick: function(item) {
    // select the node in the tree 
    var treeNode = this.fieldsTreeGrid.data.find('name', item.name);
    this.fieldsTreeGrid.deselectAllRecords();
    this.fieldsTreeGrid.selectRecord(treeNode);
  },

  selectionUpdated: function(record, recordList) {
    if (record && !record.isSection && recordList.length === 1) {
      this.propertiesLayout.updatePropertiesDisplay(record);
    } else {
      this.propertiesLayout.updatePropertiesDisplay(null);
    }
  },
  
  doClose: function(confirmed) {
    var callback, me = this, 
      persData = this.getPersonalizationData();
    // ask for confirmation
    if (this.isChanged && !confirmed) {
      callback = function(ok) {
        if (ok) {
          // do it with a small delay so that any mouse events are processed
          // by the button itself and not by the standard view below it
          me.delayCall('doClose', [true], 100);
        }
      };
      
      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmClose'), callback);
      return;
    }
    
    if (this.openedInForm) {
      if (this.hasBeenSaved || this.hasBeenDeleted) {
        // reread the window settings
        this.form.view.standardWindow.readWindowSettings();
      }
      var window = this.form.view.standardWindow;
      window.removeMember(this);
      
      // restores the tabtitle
      window.view.updateTabTitle();
      
      window.toolBarLayout.show();
      window.view.show();
    }
  },

  doOpen: function(retrievedInitialData) {
    var me = this;
    if (!retrievedInitialData) {
      OB.RemoteCallManager.call('org.openbravo.client.application.personalization.PersonalizationActionHandler', {}, {action: 'getFormDefinition', tabId: this.tabId}, 
          function(resp, data, req){
        me.demoFormProperties = data;
        me.doOpen(true);
      });
      return;
    }
    
    this.buildDemoForm();
    
    if (this.openedInForm) {
      var window = this.form.view.standardWindow;
      window.toolBarLayout.hide();
      window.view.hide();
      window.addMember(this);
      
      this.roleId = OB.User.roleId;
      this.clientId = OB.User.clientId;
      this.orgId = OB.User.organizationId;
      this.userId = OB.User.id;
      
      tabSet = OB.MainView.TabSet;
      tab = OB.MainView.TabSet.getTab(window.view.viewTabId);
      tabSet.setTabTitle(tab, OB.I18N.getLabel('OBUIAPP_Personalize_TitlePrefix', [this.form.view.tabTitle])); 
    }
  },
  
  getHoverHTML: function(item) {
    // TODO: show information about the item being hovered...
    return null;
//    return title + '<br/>' + 'give me more!';
  },
  
  // reads the data from the tree grid and returns it in the expected
  // format
  getPersonalizationData: function() {
    return {fields: this.fieldsTreeGrid.data.getAllNodes()};
  },
  
  setStatusBarInformation: function() {
    this.toolBar.updateButtonState();
    
    var statusBarFields = null, barFieldValues = [], barFieldTitles = [], label, icon = null, statusCode = null;
    if (this.isNew) {
      icon = this.statusBar.newIcon;
      label = 'OBUIAPP_New';
    } else if (this.isChanged) {
      icon = this.statusBar.editIcon;
      label = 'OBUIAPP_Editing';
    } else if (this.isSaved) {
      icon =  this.statusBar.savedIcon;
      label = 'OBUIAPP_Saved';
    }
    
    if (this.clientIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Client'));
      barFieldValues.push(this.clientIdentifier);
    }
    if (this.orgIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Organization'));
      barFieldValues.push(this.orgIdentifier);
    }
    if (this.roleIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Role'));
      barFieldValues.push(this.roleIdentifier);
    }
    if (this.userIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_User'));
      barFieldValues.push(this.userIdentifier);
    }
    if (this.tabTitle) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Tab'));
      barFieldValues.push(this.tabTitle);
    }
    
    if (barFieldTitles.length > 0) {
      statusBarFields = [];
      statusBarFields.push(barFieldTitles);
      statusBarFields.push(barFieldValues);
    }
    this.statusBar.setContentLabel(icon, label, statusBarFields);
  }
});
