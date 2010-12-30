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

/* jslint */
isc.OBQuickRun.create(OB.QuickLaunchNavbarComponentStylingProperties, {
     title: '',
     prompt: OB.I18N.getLabel('${data.label}'),
     src: '${data.icon}',

     recentPropertyName: '${data.recentPropertyName}',

     beforeShow : function() {
        var recent = OB.RecentUtilities.getRecentValue(this.recentPropertyName);
        
        var RecentFieldType = function(recentObject) {
            var prefixLabel = '${data.prefixRecent}';
            if (prefixLabel.length > 0) {
                this.linkTitle = OB.I18N.getLabel(prefixLabel) + ' ' + recentObject.tabTitle;
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
            handleClick: function() {
                OB.RecentUtilities.addRecent('${data.recentPropertyName}', this.recentObject);
                if (this.recentObject.viewId) {
                    OB.Layout.ViewManager.openView(this.recentObject.viewId, this.recentObject);
                } else {
                    OB.Layout.ViewManager.openView('OBClassicWindow', this.recentObject);
                }
                
                if (isc.OBQuickRun.currentQuickRun) {
                  isc.OBQuickRun.currentQuickRun.doHide();
                }
            },
            getValueIcon: function(value) {            
                if (this.recentObject.icon) {
                    if (this.recentObject.icon === 'Process') {
                        return '[SKINIMG]../../org.openbravo.client.application/images/icons/iconProcess.png';
                    } else if (this.recentObject.icon === 'Report') {
                        return '[SKINIMG]../../org.openbravo.client.application/images/icons/iconReport.png';
                    } else {
                        return '[SKINIMG]../../org.openbravo.client.application/images/icons/iconAutoForm.png';
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
        }
     },

     keyboardShortcutId : '${data.keyboardShortcutId}',

    initWidget: function() {
        this.Super('initWidget', arguments);
        // register the field in the registry
        var suggestionField = this.members[1].getField('value');
        OB.TestRegistry.register(this.recentPropertyName + '_FORM', this.members[1]);
        OB.TestRegistry.register(this.recentPropertyName + '_BUTTON', this);
        OB.TestRegistry.register(this.recentPropertyName + '_FIELD', suggestionField);
    },

     members: [ isc.DynamicForm.create({ visibility: 'hidden', numCols: 1}),
     isc.DynamicForm.create({
       autoFocus: true,
       width: '100%',
       titleSuffix: '',       
       fields: [{name: 'value',
         cellStyle: OB.DefaultPickListStyleProperties.cellStyle,
         titleStyle: OB.DefaultPickListStyleProperties.titleStyle,
         textBoxStyle: OB.DefaultPickListStyleProperties.textBoxStyle,
         controlStyle: OB.DefaultPickListStyleProperties.controlStyle,
         pickListBaseStyle: OB.DefaultPickListStyleProperties.pickListBaseStyle,
         pickerIconStyle:  OB.DefaultPickListStyleProperties.pickerIconStyle,
         pickerIconSrc: OB.DefaultPickListStyleProperties.pickerIconSrc,
         height: OB.DefaultPickListStyleProperties.height,
         pickerIconWidth: OB.DefaultPickListStyleProperties.pickerIconWidth,
         pickListWidth: OB.DefaultPickListStyleProperties.quickRunPickListWidth, 
         // fixes issue https://issues.openbravo.com/view.php?id=15105
         pickListCellHeight: OB.DefaultPickListStyleProperties.quickRunPickListCellHeight,

         getControlTableCSS: function() {
           // prevent extra width settings, super class
           // sets width to 0 on purpose
           return 'cursor:default;';
         },
         
         selectOnFocus: true,
         textMatchStyle: 'substring',
         width: '100%',
         
         // client filtering does not always work great...         
         pickListProperties: {dataProperties: {useClientFiltering: false}, textMatchStyle: 'substring', bodyStyleName:'OBPickListBody'},
         pickListHeaderHeight: 0,

         getPickListFilterCriteria: function() {
            var criteria = this.Super('getPickListFilterCriteria', arguments) || {};
            criteria[OB.Constants.IDENTIFIER] = this.getDisplayValue();
            return criteria;
         },
         pickListFields: [
         {
           showValueIconOnly: true,
           name: 'icon',
           valueIcons: {Process: '[SKINIMG]../../org.openbravo.client.application/images/icons/iconProcess.png',
              Report: '[SKINIMG]../../org.openbravo.client.application/images/icons/iconReport.png',
              Window: '[SKINIMG]../../org.openbravo.client.application/images/icons/iconAutoForm.png'}          
         },
         { name: OB.Constants.IDENTIFIER,
           displayField : OB.Constants.IDENTIFIER,
           valueField : OB.Constants.ID}
         ],
         autoFetchData : false,
         titleOrientation: 'top',
         title: OB.I18N.getLabel('${data.label}'),
         editorType: 'comboBox',
         
         // note local filtering is better but gave strange results
         // retry this after upgrading to newer smartclient
         //filterLocally: false,
         
         optionDataSource: OB.Datasource.get('${data.dataSourceId}'),      

         emptyPickListMessage: OB.I18N.getLabel('OBUISC_ListGrid.emptyMessage'),
     
         isTestEnvironment: OB.Utilities.hasUrlParameter('window', 'new'),
         
         pickValue: function() {
            this.Super('pickValue', arguments);
            if (this.getSelectedRecord()) {
             var value = this.getValue();
             var record  = this.getSelectedRecord();
             isc.OBQuickRun.currentQuickRun.doHide();
             var openObject = null; 
             if (record.optionType && record.optionType === 'tab') {
                if (this.isTestEnvironment) {
                  openObject = {viewId: '_' + record.windowId, windowId: record.windowId, id: value, tabId: value, tabTitle: record[OB.Constants.IDENTIFIER]};
                } else {
                  openObject = {viewId: 'OBClassicWindow', windowId: record.windowId, id: value, tabId: value, command: '${data.command}', tabTitle: record[OB.Constants.IDENTIFIER]};
                }
             } else if (record.optionType && record.optionType === 'external') {
                openObject = {viewId: 'OBExternalPage', id: value, contentsUrl: value, tabTitle: record[OB.Constants.IDENTIFIER]};
             } else if (record.optionType && record.optionType === 'process') {
                openObject = {viewId: 'OBPopupClassicWindow', processId:  record.processId, id: record.processId, obManualURL: value, command: 'BUTTON' + record.processId, tabTitle: record[OB.Constants.IDENTIFIER]};
             } else if (record.optionType && record.optionType === 'processManual') {
                openObject = {viewId: 'OBClassicWindow', processId:  record.processId, id: record.processId, obManualURL: value, command: 'DEFAULT', tabTitle: record[OB.Constants.IDENTIFIER]};
             } else if (record.viewId) {
                openObject = record; 
             } else if (record.formId) {
                openObject = {viewId: 'OBClassicWindow', formId: record.formId, id: value, obManualURL: value, command: '${data.command}', tabTitle: record[OB.Constants.IDENTIFIER]};
             } else {
                openObject = {viewId: 'OBClassicWindow', id: value, obManualURL: value, command: '${data.command}', tabTitle: record[OB.Constants.IDENTIFIER]};
             }
             openObject.singleRecord = record.singleRecord;
             openObject.readOnly = record.readOnly;
             
             openObject.icon = record.icon;
             
             OB.Layout.ViewManager.openView(openObject.viewId, openObject);
             
             OB.RecentUtilities.addRecent('${data.recentPropertyName}', openObject);
             
             this.setValue(null);
           }
         },
         
         handleKeyPress : function() {
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
         })]
})