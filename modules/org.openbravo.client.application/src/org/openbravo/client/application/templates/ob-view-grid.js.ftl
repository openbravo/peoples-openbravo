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

isc.OBViewGrid.create({
    fields:[
    <#list data.fields as field>
        { 
        autoExpand: ${field.autoExpand}, type: '${field.type}',
        <#if field.cellAlign??>
        cellAlign: '${field.cellAlign?js_string}',
        </#if>
        editorProperties: {
          ${field.gridEditorFieldProperties}
          // note need to be repeated for editor fields
          , columnName: '${field.columnName?js_string}'
          , inpColumnName: '${field.inpColumnName?js_string}'
          , referencedKeyColumnName: '${field.referencedKeyColumnName?js_string}'        
          , targetEntity: '${field.targetEntity?js_string}'
          , disabled: ${field.readOnly?string}
          , updatable: ${field.updatable?string}
          <#if field.redrawOnChange?string = "true" >
          , redrawOnChange: true
          , changed: function(form, item, value) {
              this.Super('changed', arguments);
              form.onFieldChanged(form, item, value);
            }
          </#if>
          <#if field.firstFocusedField>
          , firstFocusedField: true
          </#if>          
          <#if field.showIf != "">
          , showIf: function(item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (${field.showIf});
            }
          </#if>
          
        }
        ${field.gridFieldProperties}
        ${field.filterEditorProperties}
        , title: '${field.title?js_string}'
        , prompt: '${field.title?js_string}'
        , escapeHTML: true
        , showIf: '${field.initialShow?string}'
        , columnName: '${field.columnName?js_string}'
        , inpColumnName: '${field.inpColumnName?js_string}'
        , referencedKeyColumnName: '${field.referencedKeyColumnName?js_string}'        
        , targetEntity: '${field.targetEntity?js_string}'
       }
       <#if field_has_next>,</#if>
    </#list>
    <#list data.auditFields as field>
     ,
       { 
        autoExpand: false, type: '${field.type}',
        editorProperties: {
          width: '*'
          , columnName: '${field.columnName?js_string}'
          , targetEntity: '${field.targetEntity?js_string}'
          , disabled: true
          , updatable: false
        }
        , showHover: false, 
        width: isc.OBGrid.getDefaultColumnWidth(30), 
        name: '${field.columnName?js_string}', 
        canExport: true, 
        canHide: true, 
        editorType: '${field.editorType?js_string}',
        filterEditorType: '${field.filterEditorType?js_string}',
        ${field.displayFieldJS}
         filterOnKeypress: true, canFilter:true, required: false
        , title: '${field.title?js_string}'
        , prompt: '${field.title?js_string}'
        , escapeHTML: true
        , showIf: 'false'
        , columnName: '${field.columnName?js_string}'
        , inpColumnName: ''
        , targetEntity: '${field.targetEntity?js_string}'
       }
    </#list>
    ],
    autoExpandFieldNames:[
    <#list data.autoExpandFields as field>
        '${field}'<#if field_has_next>,</#if>
    </#list>
    ],
    whereClause: '${data.whereClause?js_string}',
    orderByClause: '${data.orderByClause?js_string}',
    sortField: '${data.sortField?js_string}',
    filterClause: '${data.filterClause?js_string}',
    filterName: '${data.filterName?js_string}',
    
    foreignKeyFieldNames:[
    <#list data.foreignKeyFields as field>
        '${field}'<#if field_has_next>,</#if>
    </#list>
    ]
})
