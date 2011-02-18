/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
isc.OBQuickRun.create({
     title: "",
     prompt: OB.I18N.getLabel('${data.label}'),
     src: '${data.icon}',
     
     // todo: it is nicer to move this to a style but then this issue occurs:
     // https://issues.openbravo.com/view.php?id=13786
     width: 37,
     
     layoutProperties: {width: 250, membersMargin: 10},

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
            editorType: "link",
            value: null,
            showTitle: false,
            target: "javascript",
            shouldSaveValue: false,
            handleClick: function() {
                OB.RecentUtilities.addRecent('${data.recentPropertyName}', this.recentObject);
                if (this.recentObject.viewId) {
                    OB.Layout.ViewManager.openView(this.recentObject.viewId, this.recentObject);
                } else {
                    OB.Layout.ViewManager.openView("ClassicOBWindow", this.recentObject);
                }
                
                if (isc.OBQuickRun.currentQuickRun) {
                  isc.OBQuickRun.currentQuickRun.doHide();
                }
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
          this.members[1].getField("value").setValue(null);
        }
     },
     
     members: [ isc.DynamicForm.create({ visibility: 'hidden', numCols: 1}),
     isc.DynamicForm.create({
       autoFocus: true,
       titleSuffix: "",       
       fields: [
       {name: "value",
         selectOnFocus: true,
         textMatchStyle: "substring",         
         pickListProperties: {textMatchStyle: "substring"},
         autoFetchData : false,
         width: 200,
         titleOrientation: 'top',
         title: OB.I18N.getLabel('${data.label}'),
         editorType: "comboBox",
         displayField : OB.Constants.IDENTIFIER,
         valueField : OB.Constants.ID,
         
         // note local filtering is better but gave strange results
         // retry this after upgrading to newer smartclient
         //filterLocally: true,
         
         optionDataSource: OB.Utilities.getDataSource('${data.dataSourceId}'),      

         pickListWidth: 200,
         emptyPickListMessage: OB.I18N.getLabel('OBUISC_ListGrid.emptyMessage'),
         
         pickValue: function() {
            this.Super("pickValue", arguments);
            if (this.getSelectedRecord()) {
             var value = this.getValue();
             isc.OBQuickRun.currentQuickRun.doHide();
             var openObject = null; 
             if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'tab') {
                openObject = {viewId: "ClassicOBWindow", windowId: this.getSelectedRecord().windowId, id: value, tabId: value, command: '${data.command}', tabTitle: this.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'external') {
                openObject = {viewId: "ExternalPage", id: value, contentsUrl: value, tabTitle: this.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'process') {
                openObject = {viewId: "PopupClassicOBWindow", processId:  this.getSelectedRecord().processId, id: this.getSelectedRecord().processId, obManualURL: value, command: 'BUTTON' + this.getSelectedRecord().processId, tabTitle: this.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'processManual') {
                openObject = {viewId: "ClassicOBWindow", processId:  this.getSelectedRecord().processId, id: this.getSelectedRecord().processId, obManualURL: value, command: 'DEFAULT', tabTitle: this.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'processManual') {
                openObject = {viewId: "ClassicOBWindow", processId:  this.getSelectedRecord().processId, id: this.getSelectedRecord().processId, obManualURL: value, command: 'DEFAULT', tabTitle: this.getDisplayValue()};
             } else if (this.getSelectedRecord().viewId) {
                openObject = this.getSelectedRecord(); 
             } else if (this.getSelectedRecord().formId) {
                openObject = {viewId: "ClassicOBWindow", formId: this.getSelectedRecord().formId, id: value, obManualURL: value, command: '${data.command}', tabTitle: this.getDisplayValue()};
             } else {
                openObject = {viewId: "ClassicOBWindow", id: value, obManualURL: value, command: '${data.command}', tabTitle: this.getDisplayValue()};
             }
             OB.Layout.ViewManager.openView(openObject.viewId, openObject);
             
             OB.RecentUtilities.addRecent('${data.recentPropertyName}', openObject);
             
             this.setValue(null);
           }
         },
         
         handleKeyPress : function() {
           var result = this.Super("handleKeyPress", arguments);
            
           var key = isc.EH.lastEvent.keyName;
           if (key === "Escape" || key === "Enter") {
             if (isc.OBQuickRun.currentQuickRun) {
               isc.OBQuickRun.currentQuickRun.doHide();
             }
           }
           return result;    
         }         
         }]
         })]
})