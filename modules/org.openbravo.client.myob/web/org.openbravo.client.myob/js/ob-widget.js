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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBWidget =
//
// Implements the base class from where all My Opebravo widgets extend.
//
isc.defineClass('OBWidget', isc.Portlet).addProperties({
  CONTENT_MODE: 'content',
  EDIT_MODE: 'edit',
  
  canResizeRows: false,
  showMaximizeButton: false,
  showMinimizeButton: false,
  showCloseButton: false,
  closeConfirmationMessage: OB.I18N.getLabel('OBKMO_DeleteThisWidgetConfirmation'),
  
  dbInstanceId: '',
  
  // Parameters handling
  dbFilterProperty: 'obkmoWidgetInstance',
  entityName: 'OBKMO_WidgetInstance',
  
  autoSize: false,
  
  fieldDefinitions: [],
  parameters: {},
  
  headerProperties: {
    defaultLayoutAlign: 'center'
  },

  // note: dragappearance target gives strange results if one attempts to 
  // drag a widget outside of the portallayout, this because actually
  // the target is dragged and not a separate layout  
  dragAppearance: 'outline',
  dragRepositionStart: function(){
    // keep the widget in the portallayout
    this.keepInParentRect = OB.MyOB.portalLayout.getPageRect();
    return true;
  },
  
  // set by my openbravo  
  widgetManager: null,
  
  widgetMode: null,
  
  initWidget: function(args){
    var widget = this, headerControls = ['headerLabel'];
    
    // set the headercontrols in initWidget otherwise only  
    // one menubutton gets created for all widgets
    this.menuButton = isc.IMenuButton.create({
      widget: this,
      showIcon: false,
      showOver: false,
      showRollOver: true,
      showDown: false,
      showFocused: false,
      showFocusedAsOver: false,
      showTitle: false,
      imageType: isc.Img.CENTER,
      src: '[SKINIMG]../../org.openbravo.client.myob/images/widget/ico-edit-wgt-hover.png',
      baseStyle: 'OBWidgetMenuButton',
      
      // only show the middle image without stretch appended
      items: [{
        name: '',
        width: '*',
        height: '*'
      }],
      
      overflow: 'visible',
      width: 24,
      height: 24,
      
      editFormLayout: null,
      windowContents: null,
      
      showMenu: function(){
        var me = this;
        this.menu.menuButton = this;
        
        this.menu.setData([{
          title: OB.I18N.getLabel('OBKMO_WMO_EditSettings'),
          widget: me.widget,
          enableIf: function(target, menu, item){
            // already in edit mode
            if (widget.widgetMode === widget.EDIT_MODE) {
              return false;
            }
            return widget.fieldDefinitions.length > 0;
          },
          action: function(){
            widget.switchMode();
          }
        }, {
          isSeparator: true
        }, {
          title: OB.I18N.getLabel('OBKMO_WMO_Refresh'),
          iconHeight: 0,
          iconWidth: 0,
          widget: me.wigdet,
          action: function(){
            widget.refresh();
          }
        }, {
          title: OB.I18N.getLabel('OBKMO_WMO_DeleteThisWidget'),
          widget: me.widget,
          action: function(){
            widget.closeClick();
          }
        }]);
        
        return this.Super('showMenu', arguments);
      },
      
      menu: isc.Menu.create({
        portlet: this,
        baseStyle: 'OBWidgetMenuCell', // menu in standard SC
        styleName: 'OBWidgetMenu', // normal in standard sc
        bodyStyleName: 'OBWidgetMenuBody', // normal in standard sc
        tableStyle: 'OBWidgetMenuTable', // menuTable in standard SC
        iconBodyStyleName: 'OBWidgetMenuTable',
        
        // overridden to get reliable custom style name
        getBaseStyle: function(record, rowNum, colNum){
          var name = this.getField(colNum).name;
          return this.baseStyle + name.substr(0, 1).toUpperCase() + name.substr(1) + 'Field';
        },
        
        fields: ['icon', 'title'],
        // overridden to let the menu to expand to the left, within the widget
        // TODO: how to handle RTL?
        placeNear: function(left, top){
          var newLeft = left - this.width + this.menuButton.getVisibleWidth();
          // don't show left from the portlet, in that extremely rare
          // case use the old left
          if (newLeft < this.portlet.getPageLeft()) {
            newLeft = left;
          }
          return this.Super('placeNear', [newLeft, top]);
        }
      })
    
    });

    if(args.showMaximizeButton) {
      headerControls.push('maximizeButton');
    }

    headerControls.push(this.menuButton);

    this.headerControls = headerControls;

    this.editFormLayout = this.createEditFormLayout();
    this.windowContents = this.createWindowContents();
    
    // if not all mandatory params are set then edit mode
    // otherwise content mode
    if (!this.allRequiredParametersSet()) {
      this.widgetMode = this.EDIT_MODE;
    } else {
      this.widgetMode = this.CONTENT_MODE;
    }
    this.toMode(this.widgetMode);
    
    this.src = null;
    this.items = [this.windowContents, this.editFormLayout];
    this.Super('initWidget', arguments);
  },
  
  confirmedClosePortlet: function(ok){
    if (ok) {
      this.Super('confirmedClosePortlet', arguments);
      OB.MyOB.notifyEvent('WIDGET_REMOVED');
    }
  },
  
  // ** {{{ OBMyOpenbravo.switchMode() }}} **
  //
  // Switches the widget from edit to content mode and vice versa.
  // Edit mode is the edit parameters mode, content mode shows the 
  // normal content of the widget. 
  switchMode: function(){
    if (this.widgetMode === this.CONTENT_MODE) {
      this.toMode(this.EDIT_MODE);
    } else {
      this.refresh();
      this.toMode(this.CONTENT_MODE);
    }
  },
  
  toMode: function(targetMode){
    if (targetMode === this.EDIT_MODE) {
      this.windowContents.hide();
      this.editFormLayout.editForm.clearValues();
      this.editFormLayout.editForm.setValues(isc.addProperties({}, this.parameters));
      this.editFormLayout.show();
      this.widgetMode = this.EDIT_MODE;
    } else {
      this.windowContents.show();
      this.editFormLayout.hide();
      this.widgetMode = this.CONTENT_MODE;
    }
  },
  
  // ** {{{ OBMyOpenbravo.createEditFormLayout() }}} **
  //
  // Creates the edit form layout used to edit parameters.
  createEditFormLayout: function(){
    var formLayout = isc.VStack.create({
      defaultLayoutAlign: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });
    
    // no fields, stop here
    if (this.fieldDefinitions.length === 0) {
      return formLayout;
    }
    
    var widget = this, items = [], i, fieldDefinition, theForm = isc.DynamicForm.create({
      width: '100%',
      height: '100%',
      titleSuffix: '',
      requiredTitleSuffix: '',
      autoFocus: true
    });
    
    // set the initial values
    theForm.values = isc.addProperties({}, this.parameters);
    
    // create the fields    
    for (i = 0; i < this.fieldDefinitions.length; i++) {
      fieldDefinition = this.fieldDefinitions[i];
      
      // handle it when there are fieldProperties
      if (fieldDefinition.fieldProperties) {
        fieldDefinition = isc.addProperties(fieldDefinition, fieldDefinition.fieldProperties);
        delete fieldDefinition.fieldProperties;
      }
      
      var formItem = isc.addProperties({
        titleOrientation: 'top'
      }, fieldDefinition);
      
      items.push(formItem);
    }
    theForm.setItems(items);
    
    formLayout.addMember(isc.Label.create({
      contents: OB.I18N.getLabel('OBKMO_EditParameters'),
      className: 'OBMyOBEditParametersLabel',
      height: 1,
      width: '100%',
      overflow: 'visible'
    }));
    formLayout.addMember(theForm);
    formLayout.editForm = theForm;
    
    var buttonLayout = isc.HStack.create({
      layoutTopMargin: 10,
      membersMargin: 10,
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });
    buttonLayout.addMembers(isc.OBFormButton.create({
      autoFit: true,
      // note reusing label from navba, is fine as these are 
      // moved to client.app later
      title: OB.I18N.getLabel('UINAVBA_Save'),
      click: function(){
        if (theForm.validate(true)) {
          widget.setParameters(isc.addProperties(widget.parameters, theForm.getValues()));
          theForm.rememberValues();
          widget.saveParameters();
        }
      }
    }));
    buttonLayout.addMembers(isc.OBFormButton.create({
      autoFit: true,
      // note reusing label from navba, is fine as these are 
      // moved to client.app later
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: function(){
        if (widget.allRequiredParametersSet()) {
          widget.switchMode();
        } else {
          isc.warn(OB.I18N.getLabel('OBKMO_NotAllParametersSet'));
        }
      }
    }));
    formLayout.addMembers(buttonLayout);
    
    return formLayout;
  },
  
  allRequiredParametersSet: function(){
    for (var i = 0; i < this.fieldDefinitions.length; i++) {
      fieldDefinition = this.fieldDefinitions[i];
      if (fieldDefinition.required && !this.parameters[fieldDefinition.name] &&
      this.parameters[fieldDefinition.name] !== false) {
        return false;
      }
    }
    return true;
  },
  
  // ** {{{ OBMyOpenbravo.createWindowContents() }}} **
  //
  // Creates the Canvas which implements the normal content
  // of the window. Must be overridden by the implementing subclass.
  createWindowContents: function(){
    return isc.Label.create({
      contents: 'Implement the createWindowContents method in the subclass!'
    });
  },
  
  // ** {{{ OBMyOpenbravo.evaluateContents() }}} **
  //
  // Evaluates the str and replaces all parameters which have the form
  // ${parameter} with a value read from the javascript context. The 
  // parameters of this widget are also set as values. 
  evaluateContents: function(str){
    return str.evalDynamicString(this, this.parameters);
  },
  
  // ** {{{ OBMyOpenbravo.setParameters(parameters) }}} **
  //
  // Is called when the edit parameters form is saved, the parameters 
  // object is passed in. The default implementation sets the parameters
  // of the widget.
  setParameters: function(parameters){
    this.parameters = parameters;
  },
  
  //
  // ** {{{ OBWidget.refresh }}} **
  //
  // The refresh is called from the widget menu. The OBWidget subclass needs to
  // implement this method and handle the refresh of its contents
  //
  refresh: function(){
    isc.Log.logInfo('The subclass needs to implement this method');
  },
  
  //
  // ** {{{ OBWidget.isSameWidget }}} **
  //
  // Returns true if the object passed as parameter is the same instance.
  // 
  // Parameters:
  // {{widget}} an object to which you want to campare
  // {{isNew}} If this flag is true, the comparison is based on the ID of the
  // client side object, otherwise the dbInstanceId is used
  isSameWidget: function(widget, isNew){
    if (!widget) {
      return false;
    }
    
    if (!isNew) {
      return this.dbInstanceId === widget.dbInstanceId;
    }
    
    return this.ID === widget.ID;
  },
  
  saveParameters: function(){
    var post, i, param, paramObj, fieldDef;
    if (isc.isA.emptyObject(this.parameters)) {
      return;
    }
    
    post = {
      ID: this.ID,
      dbInstanceId: this.dbInstanceId,
      dbFilterProperty: this.dbFilterProperty,
      action: 'SAVE',
      entityName: this.entityName,
      parameters: []
    };
    
    for (param in this.parameters) {
      if (this.parameters.hasOwnProperty(param)) {
        for (i = 0; i < this.fieldDefinitions.length; i++) {
          fieldDef = this.fieldDefinitions[i];
          if (param === fieldDef.name) {
            paramObj = {};
            paramObj.name = param;
            paramObj.parameterId = fieldDef.parameterId;
            paramObj.value = this.parameters[param];
            post.parameters.push(paramObj);
          }
        }
      }
    }
    
    OB.RemoteCallManager.call('org.openbravo.client.application.ParametersActionHandler', post, {}, function(rpcResponse, data, rpcRequest){
      if (data && data.ID && window[data.ID]) {
        window[data.ID].saveParametersResponseHandler(rpcResponse, data, rpcRequest);
      }
    });
  },
  
  saveParametersResponseHandler: function(rpcReponse, data, rpcRequest){
    if (data && data.message) {
      if (data.message.type !== 'Success') {
        isc.Log.logError(data.message.message);
      }
    }
    this.switchMode();
  }
});

// = OBUrlWidget =
//
// A widget which gets its contents directly from an url.
//
isc.defineClass('OBUrlWidget', isc.OBWidget).addProperties({
  contentSource: null,
  createWindowContents: function(){
    if (!this.contentSource) {
      this.contentSource = this.evaluateContents(this.parameters.src);
    }
    return isc.HTMLFlow.create({
      contentsType: 'page',
      contentsURL: this.contentSource,
      height: '100%',
      width: '100%'
    });
  },
  refresh: function(){
    this.windowContents.setContentsURL(this.contentSource);
  }
});

// = OBShowParameterWidget =
//
// A widget which can be used to show parameter values and content.
//
isc.defineClass('OBShowParameterWidget', isc.OBWidget).addProperties({
  setParameters: function(parameters){
    this.Super('setParameters', arguments);
    var oldForm = this.displayForm;
    this.windowContents.removeMember(this.displayForm);
    this.windowContents.addMember(this.createDisplayForm());
    oldForm.destroy();
  },
  
  createWindowContents: function(){
    var layout = isc.VLayout.create({
      width: '100%',
      height: '100%',
      defaultLayoutAlign: 'center'
    });
    layout.addMember(isc.Label.create({
      contents: OB.I18N.getLabel('OBKMO_ParameterValues'),
      height: 1,
      overflow: 'visible'
    }));
    layout.addMember(isc.LayoutSpacer.create({
      height: 10
    }));
    layout.addMember(this.createDisplayForm());
    return layout;
  },
  
  createDisplayForm: function(){
    var item, theForm = isc.DynamicForm.create({
      width: '100%',
      height: '100%',
      wrapItemTitles: false
    });
    
    var items = [];
    var values = {};
    for (var i in this.parameters) {
      if (i) {
        items.push({
          name: i,
          title: i,
          type: 'text',
          width: '100%',
          editorType: 'StaticTextItem'
        });
        // get the display value
        // TODO: handle missing values somehow
        item = this.editFormLayout.editForm.getItem(i);
        if (item) {
          values[i] = item.mapValueToDisplay(this.parameters[i]);
        } else {
          values[i] = this.parameters[i];
        }
      }
    }
    theForm.setItems(items);
    theForm.setValues(values);
    
    this.displayForm = theForm;
    
    return theForm;
  }
});
