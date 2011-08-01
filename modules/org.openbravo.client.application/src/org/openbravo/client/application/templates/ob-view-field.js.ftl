<#--
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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->

<#macro createField fieldDefinition>
    {
        name: '${fieldDefinition.name?js_string}',
        title: '${fieldDefinition.label?js_string}',
        type: '${fieldDefinition.type}',
        disabled: ${fieldDefinition.readOnly?string},
        readonly: ${fieldDefinition.readOnly?string},
        updatable: ${fieldDefinition.updatable?string},
        parentProperty: ${fieldDefinition.parentProperty?string},
        colSpan: ${fieldDefinition.colSpan},
        rowSpan: ${fieldDefinition.rowSpan},
        startRow: ${fieldDefinition.startRow?string},
        endRow: ${fieldDefinition.endRow?string},
        personalizable: ${fieldDefinition.personalizable?string},
        <#if fieldDefinition.standardField>
        columnName: '${fieldDefinition.columnName?string}',
        inpColumnName: '${fieldDefinition.inpColumnName?string}',
        referencedKeyColumnName: '${fieldDefinition.referencedKeyColumnName?string}',
        targetEntity: '${fieldDefinition.targetEntity?string}',
        <#if !fieldDefinition.displayed>
        editorType: 'HiddenItem',
        alwaysTakeSpace: false,
        </#if>
        required: ${fieldDefinition.required?string},
          <#if fieldDefinition.redrawOnChange?string = "true" && fieldDefinition.displayed>
          redrawOnChange: true,
          changed: function(form, item, value) {
            if (this.pickValue && !this._pickedValue) {
                return;
            }
            this.Super('changed', arguments);
            form.onFieldChanged(form, item, value);
            form.view.toolBar.refreshCustomButtonsView(form.view);
          },
          </#if>
          <#if fieldDefinition.showIf != "" && fieldDefinition.displayed>
          showIf: function(item, value, form, values) {            
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            
            OB.Utilities.fixNull250(currentValues);

            return !this.hiddenInForm && context && (${fieldDefinition.showIf});
          },          
          </#if>
          <#if fieldDefinition.searchField>
          displayField: '${fieldDefinition.name?js_string}._identifier',
          valueField: '${fieldDefinition.name?js_string}',
          showPickerIcon: ${(!fieldDefinition.parentProperty)?string},
          </#if>
          <#if fieldDefinition.firstFocusedField>
          firstFocusedField: true,
          </#if>
        </#if>
        <#if fieldDefinition.type = "OBSectionItem" || fieldDefinition.type = "OBNoteSectionItem" || fieldDefinition.type = "OBLinkedItemSectionItem"  || fieldDefinition.type = "OBAttachmentsSectionItem" || fieldDefinition.type = "OBAuditSectionItem">
          <#if !fieldDefinition.displayed>
          visible: false,
          </#if>
        sectionExpanded: ${fieldDefinition.expanded?string},
        defaultValue: '${fieldDefinition.label?js_string}',
        itemIds: [
        <#list fieldDefinition.children as childField>
        '${childField.name?js_string}'<#if childField_has_next>,</#if>
        </#list>
        ],
        </#if>
        ${fieldDefinition.fieldProperties}
        dummy: ''
    }
</#macro>
