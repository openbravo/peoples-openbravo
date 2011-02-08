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

/* jslint */
isc.OBApplicationMenuButton.create({
    title: OB.I18N.getLabel('${data.label}'),

    // put something in the array, otherwise there 
    // are small styling issues
    baseData: [        
    <#list data.rootMenuOptions as menuOption>
        <@createMenuItem menuOption=menuOption /><#if menuOption_has_next>,</#if>
    </#list>
    ]
})

<#macro createMenuItem menuOption>
    {title: '${menuOption.label?js_string}'
    <#if menuOption.window>
        , tabId: '${menuOption.id?js_string}'
        , windowId: '${menuOption.menu.window.id?js_string}'
    <#elseif menuOption.process>
        , manualUrl: '${menuOption.id?js_string}', processId: '${menuOption.menu.process.id}', modal: ${menuOption.modal?string}
    <#elseif menuOption.processManual>
        , manualUrl: '${menuOption.id?js_string}', manualProcessId: '${menuOption.menu.process.id}'
    <#elseif menuOption.task>
        , manualUrl: '${menuOption.id?js_string}'
    <#elseif menuOption.form>
        , manualUrl: '${menuOption.id?js_string}'
        , formId: '${menuOption.formId?js_string}'
    <#elseif menuOption.external>
        , externalUrl: '${menuOption.id?js_string}'
    <#elseif menuOption.view>
        , viewId: '${menuOption.id?js_string}'
        , tabTitle: '${menuOption.label?js_string}'
    </#if>
    , singleRecord: ${menuOption.singleRecordStringValue}
    , readOnly: ${menuOption.readOnlyStringValue}
    
    <#list menuOption.parameters as parameter>
        , '${parameter.name?js_string}': '${parameter.parameterValue?js_string}'        
    </#list>
    , '_baseStyle': 'OBNavBarComponentMenuItemCell'
    <#if menuOption.children?size &gt; 0>
    , submenu: [
    <#list menuOption.children as childMenuOption>
        <@createMenuItem menuOption=childMenuOption /><#if childMenuOption_has_next>,</#if>        
    </#list>]
    </#if>
    }
</#macro>