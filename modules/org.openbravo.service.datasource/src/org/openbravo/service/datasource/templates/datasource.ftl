<#--
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
-->
/* jslint */
<#-- 
if the createStatement parameter is passed then only create the 
javascript with the isc.RestDataSource.create statement.

use these directives to compress during template generation
although the same is done by the compressor
<@compress single_line=true>
</@compress>
-->
<#if data.getParameter("_onlyGenerateCreateStatement") != "">
    <@generateDataSource data=data/>
</#if>
<#if data.getParameter("create") != "">
    <@generateDataSource data=data/>;
</#if>

<#macro generateDataSource data>
    OB.Datasource.create({
<#if data.getParameter("_onlyGenerateCreateStatement") == "" || data.getParameter("create") != "">
        ID:'${data.id}',
</#if>
        dataURL:'${data.dataUrl?js_string}${data.name?js_string}'
        , recordXPath: '/response/data', dataFormat: 'json'
        , operationBindings:  [{operationType: 'fetch', dataProtocol: 'postParams', requestProperties:{httpMethod: 'POST'}} 
        , {operationType: 'add', dataProtocol: 'postMessage'}
        , {operationType: 'remove', dataProtocol: 'postParams', requestProperties:{httpMethod: 'DELETE'}}
        , {operationType: 'update', dataProtocol: 'postMessage', requestProperties:{httpMethod: 'PUT'}}
        ], requestProperties : { params : {
                <#list data.getParameterNames() as key>
                    ${key} : '${data.getParameter(key)?js_string}'<#if key_has_next>,</#if>     
                </#list>
                }
        }, fields:[
<#list data.dataSourceProperties as property>
    <@generateField property=property /><#if property_has_next>,</#if>
</#list>
    ]})
</#macro>

<#macro generateField property>
  {name: '${property.name?js_string}',
    type: '${property.type}'
<#if property.id>
    , hidden: true, primaryKey: true
</#if>
<#if property.mandatory && property.updatable && !property.auditInfo && !property.boolean>
    , required: true
</#if>
<#if !property.updatable || property.auditInfo>
    , canSave: false
</#if>
<#if 0 < property.fieldLength && property.primitive && !property.id && property.primitiveObjectType.name="java.lang.String">
      , length: ${property.fieldLength?c}
</#if>
    <#--TODO: translate this-->
    , title: '${property.name?js_string}'
<#if (property.allowedValues)?? && 0 < property.allowedValues?size>
    , valueMap: {
    <#list property.valueMapContent as entry>
    <#-- 
        Note the replace is needed because freemarker js_string will replace > with \> resulting in jslint escape errors
        Note uses the _;_;_;_ as a trick assuming that it will never occur.
        see this issue: https://issues.openbravo.com/view.php?id=14487
    -->
    '${entry.value?js_string}': '${entry.label?replace(">", "_;_;_;_")?js_string?replace("_;_;_;_", ">")}'<#if entry_has_next>,</#if>
    </#list>
    }
</#if>
<#if !property.primitive>
   <#if !property.id>
    , hidden: true
   </#if>
   <#-- , foreignKey: '${property.targetEntity.name}.id' -->
</#if>
    }
<#if !property.primitive>
    <#-- Note the subPropName are constants from the JsonConstants class -->
    <#-- , <@generateReferenceField property=property subPropName='id'/> -->
    , <@generateReferenceField property=property subPropName='_identifier'/>
</#if>
    
</#macro>

<#macro generateReferenceField property subPropName>
    {name: '${property.name?js_string}.${subPropName}'
    , type: 'text', hidden: true
    <#if property.mandatory>
      <#--, required: true-->
    </#if>

    <#if !property.updatable || property.auditInfo>
      , canSave: false
    </#if>
    <#--, valueXPath: '${property.name?js_string}/${subPropName}'-->
    <#--TODO Translate this-->
    , title: '${property.name?js_string}'
    }
    
<#--
    if (subPropName.equals(JsonConstants.IDENTIFIER)) {
      // sb.append(", editorType: 'comboBox'");
      // sb.append(", editorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "'");
      // sb.append(", optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
      // sb.append(", filterEditorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.IDENTIFIER + "', editorType: 'comboBox'");
      // sb.append(", required: false, optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
    } else if (subPropName.equals(JsonConstants.ID)) {
      // sb.append(", editorType: 'comboBox'");
      // sb.append(", editorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "'");
      // sb.append(", optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
      // sb.append(", filterEditorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "', editorType: 'comboBox'");
      // sb.append(", required: false, optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
    } else {
      // sb.append(", filterEditorProperties: {required: false}");
    }
-->
</#macro>
