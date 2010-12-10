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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/* jslint */
sc_${data.columnName} = isc.OBSelectorWidget.create({
    selectorDefinitionId: '${data.id}',
    popupTextMatchStyle: '${data.selector.popuptextmatchstyle}',
    suggestionTextMatchStyle: '${data.selector.suggestiontextmatchstyle}',
    openbravoField : document.getElementById("${data.columnName}"),
    defaultPopupFilterField : '${data.defaultPopupFilterField}',
    disabled: ${data.disabled},
    required: ${data.required},
    numCols : ${data.numCols},
    displayField: '${data.displayField?js_string}',
    valueField: '${data.valueField?js_string}',
    pickListFields: [
    <#list data.pickListFields as pickListField>
        {<#list pickListField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>       
        }<#if pickListField_has_next>,</#if>
    </#list>
    ],
    showSelectorGrid: ${data.showSelectorGrid},
    selectorGridFields : [
    <#list data.selectorGridFields as selectorGridField>
        {<#list selectorGridField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>
        }<#if selectorGridField_has_next>,</#if>
    </#list>
    ],
    outFields : {
    <#list data.outFields as selectorOutField>
    '${selectorOutField.outFieldName}':'${selectorOutField.tabFieldName}'<#if selectorOutField_has_next>,</#if>
    </#list>
    },
    extraSearchFields: [${data.extraSearchFields}],
    dataSource: ${data.dataSourceJavascript},
    whereClause : '${data.whereClause?js_string}',
    callOut: ${data.callOut},
    title : '${data.title}',
    comboReload: ${data.comboReload}
});
