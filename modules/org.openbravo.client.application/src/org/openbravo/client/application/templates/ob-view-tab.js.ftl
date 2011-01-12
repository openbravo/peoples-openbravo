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
<@createView data/>      

<#macro createView tabComponent>    
    tabTitle: '${tabComponent.tabTitle}',
    entity:  '${tabComponent.entityName}',
    
    <#if tabComponent.parentProperty != ''>
        parentProperty: '${tabComponent.parentProperty?js_string}',
    </#if>
    <#if tabComponent.tabSet>
        tabId: '${tabComponent.tabId}',
        dataSourceId: '${tabComponent.dataSourceId?js_string}',
        moduleId: '${tabComponent.moduleId}',
    </#if>
    
    defaultEditMode: ${tabComponent.defaultEditMode},
    
    propertyToColumns:[
      <#list tabComponent.allFields as field>
        {
          property: '${field.propertyName?js_string}',
          column: '${field.columnName?js_string}', 
          dbColumn: '${field.dbColumnName?js_string}', 
          sessionProperty: ${field.session}
        }<#if field_has_next>,</#if>
      </#list>
    ],
    
    actionToolbarButtons: [
    <#list tabComponent.buttonFields as field>
      {id: '${field.id?js_string}', 
       title: '${field.label?js_string}',
       obManualURL: '${field.url}',
       command: '${field.command}',
       property: '${field.propertyName?js_string}',
       labelValue: {<#list field.labelValues as value>
           '${value.value}': '${value.label?js_string}'<#if field_has_next>,</#if>
       </#list>
         }
      }<#if field_has_next>,</#if>
    </#list>],
    
    <#if tabComponent.childTabs?size &gt; 0>
        hasChildTabs: true,
    </#if>
    initWidget: function() {
        this.viewForm = ${tabComponent.viewForm}; 
        this.viewGrid = ${tabComponent.viewGrid};
        this.Super('initWidget', arguments);
      },
    createViewStructure: function() {
        <#list tabComponent.childTabs as childTabComponent>
        this.addChildView(
            isc.OBStandardView.create({
                <@createView childTabComponent/>
            })
        );
        </#list>
    }
</#macro>  
