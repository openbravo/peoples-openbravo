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

<#macro createField fieldDefinition>
    {
        name: '${fieldDefinition.name?js_string}',
        title: '${fieldDefinition.label?js_string}',
        type: '${fieldDefinition.type}',
        disabled: ${fieldDefinition.readOnly?string},
        parentProperty: ${fieldDefinition.parentProperty?string},
        colSpan: ${fieldDefinition.colSpan},
        rowSpan: ${fieldDefinition.rowSpan},
        startRow: ${fieldDefinition.startRow},
        endRow: ${fieldDefinition.endRow},
        width: '*',
        <#if fieldDefinition.standardField>
        columnName: '${fieldDefinition.columnName?string}',
        inpColumnName: '${fieldDefinition.inpColumnName?string}',
        referencedKeyColumnName: '${fieldDefinition.referencedKeyColumnName?string}',
        targetEntity: '${fieldDefinition.targetEntity?string}',
        required: ${fieldDefinition.required?string},
          <#if fieldDefinition.searchField>
          displayField: '${fieldDefinition.name?js_string}._identifier',
          valueField: '${fieldDefinition.name?js_string}',
          showPickerIcon: ${(!fieldDefinition.parentProperty)?string},
          </#if>
        </#if>
        <#if fieldDefinition.type = "OBSectionItem">
        sectionExpanded: true,
        defaultValue: '${fieldDefinition.label?js_string}',
        itemIds: [
        <#list fieldDefinition.children as childField>
        '${childField.name?js_string}'<#if childField_has_next>,</#if>
        </#list>
        ],
        </#if>
        ${fieldDefinition.fieldProperties}
        dummy: "dummy"
    }
</#macro>