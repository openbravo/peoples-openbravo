/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
// jslint

// remarks if there is an editor type defined in the simpletype then the validators of the 
// FormItem itself are not executed anymore.
<#list data.simpleTypes as simpleType>

isc.SimpleType.create({
    inheritsFrom: "${simpleType.inheritsFrom?js_string}",
    name: "${simpleType.name?js_string}",
    <#if simpleType.editorTypeSet>
    editorType: ${simpleType.editorType},
    </#if>
    <#if simpleType.date>
    shortDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Date.JSToOB(value, OB.Format.date);
    },  
    normalDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Date.JSToOB(value, OB.Format.date);
    },
    <#elseif simpleType.boolean>
    valueMap: [null, true, false],
    shortDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.getYesNoDisplayValue(value);
    },  
    normalDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.getYesNoDisplayValue(value);
    },  
    <#elseif simpleType.dateTime>
    shortDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Date.JSToOB(value, OB.Format.dateTime);
    },  
    normalDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Date.JSToOB(value, OB.Format.dateTime);
    },
    <#elseif simpleType.number>
        <#if simpleType.shortFormatPresent>
    shortDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Number.OBPlainToOBMasked(value, 
            "${simpleType.shortDisplayFormat.format?js_string}",
            "${simpleType.shortDisplayFormat.decimalSymbol?js_string}", 
            "${simpleType.shortDisplayFormat.groupingSymbol?js_string}", OB.Format.defaultGroupingSize);
    },
        </#if>
        <#if simpleType.normalFormatPresent>
    normalDisplayFormatter: function(value, field, component, record) {
        return OB.Utilities.Number.OBPlainToOBMasked(value, 
            "${simpleType.normalDisplayFormat.format?js_string}",
            "${simpleType.normalDisplayFormat.decimalSymbol?js_string}", 
            "${simpleType.normalDisplayFormat.groupingSymbol?js_string}", OB.Format.defaultGroupingSize);
    },
        </#if>
    </#if>
    dummy: "dummy" // added to ensure that comma delimiter is always valid
});
</#list>       

// set the global date format
isc.Date.setShortDisplayFormat(function(value) {
    return OB.Utilities.Date.JSToOB(value, OB.Format.date);
});

isc.Date.setNormalDisplayFormat(function(value) {
    return OB.Utilities.Date.JSToOB(value, OB.Format.date);
});

isc.Date.setInputFormat(function(value) {
    return OB.Utilities.Date.OBToJS(value, OB.Format.date);
});

