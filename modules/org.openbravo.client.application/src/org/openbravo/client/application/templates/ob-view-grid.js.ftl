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

isc.OBViewGrid.create({
    fields:[
    <#list data.fields as field>
        { 
        autoExpand: ${field.autoExpand}, type: '${field.type}' 
        ${field.gridFieldProperties}
        ${field.filterEditorProperties}
        , targetEntity: '${field.targetEntity?js_string}'
        , title: '${field.title?js_string}'
        , showIf: '${field.initialShow?string}'
        , inpColumnName: '${field.inpColumnName?string}'
        , referencedKeyColumnName: '${field.referencedKeyColumnName?string}'        
       }
       <#if field_has_next>,</#if>
    </#list>
    ],
    whereClause: '${data.whereClause?js_string}',
    orderByClause: '${data.orderByClause?js_string}',
    
    foreignKeyFieldNames:[
    <#list data.foreignKeyFields as field>
        '${field}'<#if field_has_next>,</#if>
    </#list>
    ]
})
