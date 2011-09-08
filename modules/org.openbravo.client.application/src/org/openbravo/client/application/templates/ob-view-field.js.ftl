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
        type: '${fieldDefinition.type}',
        <#if fieldDefinition.required>
        required: ${fieldDefinition.required?string},
        </#if>
        <#if fieldDefinition.readOnly>
        disabled: true,
        </#if>
        <#if !fieldDefinition.updatable>
        updatable: false,
        </#if>
        <#if fieldDefinition.sessionProperty>
        sessionProperty: true,
        </#if>
        <#if fieldDefinition.parentProperty>
        parentProperty: true,
        </#if>
        <#if fieldDefinition.colSpan != 1>
        colSpan: ${fieldDefinition.colSpan},
        </#if>
        <#if fieldDefinition.rowSpan != 1>
        rowSpan: ${fieldDefinition.rowSpan},
        </#if>
        <#if fieldDefinition.startRow>
        startRow: true,
        </#if>
        <#if fieldDefinition.endRow>
        endRow: true,
        </#if>
        <#if !fieldDefinition.personalizable>
        personalizable: false,
        </#if>
        <#if fieldDefinition.hasDefaultValue>
        hasDefaultValue: true,
        </#if>
        <#if fieldDefinition.standardField>
        columnName: '${fieldDefinition.columnName?string}',
        inpColumnName: '${fieldDefinition.inpColumnName?string}',
        <#if fieldDefinition.referencedKeyColumnName != ''>
        referencedKeyColumnName: '${fieldDefinition.referencedKeyColumnName?string}',
        </#if>
        <#if fieldDefinition.targetEntity != ''>
        targetEntity: '${fieldDefinition.targetEntity?string}',
        </#if>
        <#if !fieldDefinition.displayed>
        visible: false,
        displayed: false,
        alwaysTakeSpace: false,
        </#if>
        <#if fieldDefinition.redrawOnChange && fieldDefinition.displayed>
            redrawOnChange: true,
        </#if>
        <#if fieldDefinition.showIf != "" && fieldDefinition.displayed>
          showIf: function(item, value, form, currentValues, context) {
            return (${fieldDefinition.showIf});          
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
        <#if fieldDefinition.expanded>
        sectionExpanded: ${fieldDefinition.expanded?string},
        </#if>
        defaultValue: '${fieldDefinition.label?js_string}',
        <#if fieldDefinition.hasChildren>
        itemIds: [
        <#list fieldDefinition.children as childField>
        '${childField.name?js_string}'<#if childField_has_next>,</#if>
        </#list>
        ],
        </#if>
        </#if>
        ${fieldDefinition.fieldProperties}
        title: '${fieldDefinition.label?js_string}'
    }
</#macro>
