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
isc.ClassFactory.defineClass('OBQuickLaunch', isc.OBQuickRun);

isc.OBQuickLaunch.addProperties({

  beforeShow: function(){
    var recent = OB.RecentUtilities.getRecentValue(this.recentPropertyName);
    
    var RecentFieldType = function(recentObject){
      if (this.prefixLabel.length > 0) {
        this.linkTitle = OB.I18N.getLabel(this.prefixLabel) + ' ' + recentObject.tabTitle;
      } else {
        this.linkTitle = recentObject.tabTitle;
      }
      this.value = recentObject.tabTitle;
      this.recentObject = recentObject;
    };
    RecentFieldType.prototype = {
      editorType: 'link',
      value: null,
      showTitle: false,
      target: 'javascript',
      shouldSaveValue: false,
      recentPropertyName: this.recentPropertyName,
      prefixLabel: this.prefixLabel,
      handleClick: function(){
        OB.RecentUtilities.addRecent(this.recentPropertyName, this.recentObject);
        if (this.recentObject.viewId) {
          OB.Layout.ViewManager.openView(this.recentObject.viewId, this.recentObject);
        } else {
          OB.Layout.ViewManager.openView('OBClassicWindow', this.recentObject);
        }
        
        if (isc.OBQuickRun.currentQuickRun) {
          isc.OBQuickRun.currentQuickRun.doHide();
        }
      },
      getValueIcon: function(value){
        if (this.recentObject.icon) {
          if (this.recentObject.icon === 'Process') {
            return '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconProcess.png';
          } else if (this.recentObject.icon === 'Report') {
            return '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconReport.png';
          } else if (this.recentObject.icon === 'Form') {
            return '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconForm.png';
          } else {
            return '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconWindow.png';
          }
        }
        return null;
      }
    };
    
    if (recent && recent.length > 0) {
      var newFields = [];
      var index = 0;
      for (var recentIndex = 0; recentIndex < recent.length; recentIndex++) {
        if (recent[recentIndex]) {
          newFields[index] = new RecentFieldType(recent[recentIndex]);
          index++;
        }
      }
      this.members[0].setFields(newFields);
      this.layout.showMember(this.members[0]);
      this.members[1].getField('value').setValue(null);
      this.members[1].getField('value').setElementValue('', null);      
    }
  },
  
  // handle the case that someone entered a url in the quick launch
  doHide: function(){
    if (this.members[1].getField('value').getValue() && this.members[1].getField('value').getValue().contains('?')) {
      var params = OB.Utilities.getUrlParameters(this.members[1].getField('value').getValue());
      if (params && params.tabId) {
        OB.Utilities.openDirectTab(params.tabId, params.recordId, params.command);
      }      
    }
    this.Super('doHide', arguments);
  },
  
  initWidget: function(){
    this.members = [isc.DynamicForm.create({
      visibility: 'hidden',
      numCols: 1
    }), isc.DynamicForm.create({
      autoFocus: true,
      width: '100%',
      titleSuffix: '',
      fields: [{
        name: 'value',
        cellStyle: OB.DefaultPickListStyleProperties.cellStyle,
        titleStyle: OB.DefaultPickListStyleProperties.titleStyle,
        textBoxStyle: OB.DefaultPickListStyleProperties.textBoxStyle,
        controlStyle: OB.DefaultPickListStyleProperties.controlStyle,
        pickListBaseStyle: OB.DefaultPickListStyleProperties.pickListBaseStyle,
        pickerIconStyle: OB.DefaultPickListStyleProperties.pickerIconStyle,
        pickerIconSrc: OB.DefaultPickListStyleProperties.pickerIconSrc,
        height: OB.DefaultPickListStyleProperties.height,
        pickerIconWidth: OB.DefaultPickListStyleProperties.pickerIconWidth,
        pickListWidth: OB.DefaultPickListStyleProperties.quickRunPickListWidth,
        // fixes issue https://issues.openbravo.com/view.php?id=15105
        pickListCellHeight: OB.DefaultPickListStyleProperties.quickRunPickListCellHeight,
        recentPropertyName : this.recentPropertyName,
        
        getControlTableCSS: function(){
          // prevent extra width settings, super class
          // sets width to 0 on purpose
          return 'cursor:default;';
        },
        
        selectOnFocus: true,
        textMatchStyle: 'substring',
        width: '100%',
        
        // client filtering does not always work great...         
        pickListProperties: {
          dataProperties: {
            useClientFiltering: false
          },
          textMatchStyle: 'substring',
          fetchDelay: 50,
          bodyStyleName: 'OBPickListBody'
        },
        pickListHeaderHeight: 0,
        
        getPickListFilterCriteria: function(){
          // only filter on identifier
          var criteria = {};
          criteria[OB.Constants.IDENTIFIER] = this.getDisplayValue();
          return criteria;
        },
        pickListFields: [{
          showValueIconOnly: true,
          name: 'icon',
          valueIcons: {
            Process: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconProcess.png',
            Report: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconReport.png',
            Form: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconForm.png',
            Window: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconWindow.png'
          }
        }, {
          name: OB.Constants.IDENTIFIER,
          displayField: OB.Constants.IDENTIFIER,
          valueField: OB.Constants.ID
        }],
        autoFetchData: false,
        titleOrientation: 'top',
        title: OB.I18N.getLabel(this.titleLabel),
        editorType: 'comboBox',
        
        // local filtering enabled, remove the Id filter
        // explicitly from the criteria list, see getPickListFilter
        filterLocally: true,
        
        optionDataSource: OB.Datasource.get(this.dataSourceId),
        
        emptyPickListMessage: OB.I18N.getLabel('OBUISC_ListGrid.emptyMessage'),
        
        command: this.command,

        pickValue: function(theValue){
          // HACK: set this temporary value to prevent a temporary 
          // display of the db id
          if (!this.getValueMap()) {
            this.setValueMap({});
          }
          this.getValueMap()[theValue] = '';
          
          this.Super('pickValue', arguments);
          
          if (this.getSelectedRecord()) {
            var record = this.getSelectedRecord();
            var viewValue = record.viewValue;
            isc.OBQuickRun.currentQuickRun.doHide();
            var openObject = null;
            if (record.optionType && record.optionType === 'tab') {
              openObject = OB.Utilities.openView(record.windowId, viewValue, record[OB.Constants.IDENTIFIER], null, this.command, record.icon);
              if (openObject) {
                OB.RecentUtilities.addRecent(this.recentPropertyName, openObject);
              }
              return;
            } else if (record.optionType && record.optionType === 'external') {
              openObject = {
                viewId: 'OBExternalPage',
                id: viewValue,
                contentsUrl: viewValue,
                tabTitle: record[OB.Constants.IDENTIFIER]
              };
            } else if (record.optionType && record.optionType === 'process') {
              var viewName = record.modal ? 'OBClassicPopupModal' : 'OBPopupClassicWindow';
              openObject = {
                viewId: viewName,
                processId: record.processId,
                id: record.processId,
                obManualURL: viewValue,
                command: 'BUTTON' + record.processId,
                tabTitle: record[OB.Constants.IDENTIFIER]
              };
            } else if (record.optionType && record.optionType === 'processManual') {
              openObject = {
                viewId: 'OBClassicWindow',
                processId: record.processId,
                id: record.processId,
                obManualURL: viewValue,
                command: 'DEFAULT',
                tabTitle: record[OB.Constants.IDENTIFIER]
              };
            } else if (record.viewId) {
              openObject = record;
            } else if (record.formId) {
              openObject = {
                viewId: 'OBClassicWindow',
                formId: record.formId,
                id: viewValue,
                obManualURL: viewValue,
                command: this.command,
                tabTitle: record[OB.Constants.IDENTIFIER]
              };
            } else {
              openObject = {
                viewId: 'OBClassicWindow',
                id: viewValue,
                obManualURL: viewValue,
                command: this.command,
                tabTitle: record[OB.Constants.IDENTIFIER]
              };
            }
            openObject.singleRecord = record.singleRecord;
            openObject.readOnly = record.readOnly;
            
            openObject.icon = record.icon;
            
            OB.Layout.ViewManager.openView(openObject.viewId, openObject);
            
            OB.RecentUtilities.addRecent(this.recentPropertyName, openObject);
            
            this.setValue(null);
          }
        },
        
        handleKeyPress: function(){
          var result = this.Super('handleKeyPress', arguments);
          
          var key = isc.EH.lastEvent.keyName;
          if (key === 'Escape' || key === 'Enter') {
            if (isc.OBQuickRun.currentQuickRun) {
              isc.OBQuickRun.currentQuickRun.doHide();
            }
          }
          return result;
        }
      }]
    })];

    var ret = this.Super('initWidget', arguments);
    
    // register the field in the registry
    var suggestionField = this.members[1].getField('value');
    OB.TestRegistry.register(this.recentPropertyName + '_RECENTFORM', this.members[0]);
    OB.TestRegistry.register(this.recentPropertyName + '_FORM', this.members[1]);
    OB.TestRegistry.register(this.recentPropertyName + '_BUTTON', this);
    OB.TestRegistry.register(this.recentPropertyName + '_FIELD', suggestionField);
    
    return ret;
  }
  
});
