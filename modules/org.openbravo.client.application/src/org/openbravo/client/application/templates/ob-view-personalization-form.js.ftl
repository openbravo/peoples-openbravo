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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->
{
    fields: [
    <#list data.fields as fieldDefinition>
      {
        name: '${fieldDefinition.name?js_string}',
        title: '${fieldDefinition.label?js_string}',
        type: '${fieldDefinition.type}',
        colSpan: ${fieldDefinition.colSpan},
        rowSpan: ${fieldDefinition.rowSpan},
        startRow: ${fieldDefinition.startRow?string},
        endRow: ${fieldDefinition.endRow?string},
        personalizable: ${fieldDefinition.personalizable?string},
        isPreviewFormItem: true,
        disabled: true,
        showDisabled: false,
        <#if !fieldDefinition.displayed>
        width: '',
        <#else>
        width: '*',
        </#if>
        <#if fieldDefinition.showIf != "" && fieldDefinition.displayed>
          hasShowIf: true,            
        </#if>
        <#if fieldDefinition.standardField>
            <#if !fieldDefinition.displayed>
                alwaysTakeSpace: false,
                displayed: false,
            </#if>
            required: ${fieldDefinition.required?string},
            hasDefaultValue: ${fieldDefinition.hasDefaultValue?string},
            <#if fieldDefinition.searchField>
                showPickerIcon: ${(!fieldDefinition.parentProperty)?string},
            </#if>
        </#if>
        <#if fieldDefinition.type = "OBSectionItem" || fieldDefinition.type = "OBNoteSectionItem" || fieldDefinition.type = "OBLinkedItemSectionItem"  || fieldDefinition.type = "OBAttachmentsSectionItem" || fieldDefinition.type = "OBAuditSectionItem">
          <#if !fieldDefinition.displayed>
          visible: false,
          </#if>
          defaultValue: '${fieldDefinition.label?js_string}',
          itemIds: [
            <#list fieldDefinition.children as childField>
                '${childField.name?js_string}'<#if childField_has_next>,</#if>
            </#list>
            ]
        </#if>
    }
      <#if fieldDefinition_has_next>,</#if>
    </#list>    
    ],

    statusBarFields: [
    <#list data.statusBarFields as sbf>
      '${sbf?js_string}'<#if sbf_has_next>,</#if>
    </#list>
    ]
}
