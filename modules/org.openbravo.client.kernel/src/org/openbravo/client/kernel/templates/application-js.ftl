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
//jslint

var OB = {
    Application : {
        language : '${data.languageId?js_string}',
        systemVersion : '${data.systemVersion?js_string}', // global version used in all hyperlinks
        contextUrl: '${data.contextUrl}',
        communityBrandingUrl: '${data.communityBrandingUrl?js_string}',
        communityBrandingStaticUrl: '${data.communityBrandingStaticUrl?js_string}',
        butlerUtilsUrl: '${data.butlerUtilsUrl?js_string}',
        purpose: '${data.instancePurpose?js_string}',
        licenseType: '${data.licenseType?js_string}',
        versionDescription: '${data.versionDescription?js_string}'
    },

    User : {
        id : "${data.user.id}",
        firstName : "${(data.user.firstName!'')?js_string}",
        lastName : "${(data.user.lastName!'')?js_string}",
        userName : "${(data.user.username!'')?js_string}",
        name : "${(data.user.name!'')?js_string}",
        email : "${(data.user.email!'')?js_string}",
        roleId: "${data.role.id}",
        roleName: "${data.role.name}",
        clientId: "${data.client.id}",
        clientName: "${data.client.name}",
        organizationId: "${data.organization.id}",
        organizationName: "${data.organization.name}"
    },

    Format : {
        defaultGroupingSize: 3,
        defaultGroupingSymbol: "${data.defaultGroupingSymbol}",
        defaultDecimalSymbol: "${data.defaultDecimalSymbol}",
        defaultNumericMask: "${data.defaultNumericMask}",
        date: "${data.dateFormat}",
        dateTime: "${data.dateTimeFormat}"
    },
    
    Constants : {
        IDENTIFIER : "_identifier",
        ID : "id",
        WHERE_PARAMETER : "_where",
        ORG_PARAMETER : "_org",
        ORDERBY_PARAMETER : "_orderBy",
        FILTER_PARAMETER : "_filter",
        SORTBY_PARAMETER : "_sortBy",
        OR_EXPRESSION: "_OrExpression",
        TEXT_MATCH_PARAMETER_OVERRIDE: "_textMatchStyleOverride",
        SUCCESS : "success",
        DBL_CLICK_DELAY: 300,
        ERROR : "error"        
    },
    
    I18N: {}
};

OB.Application.moduleVersionParameters = {
<#list data.moduleVersionParameters as moduleVersionParameter>
'${moduleVersionParameter.id?js_string}' : '${moduleVersionParameter.value?js_string}'<#if moduleVersionParameter_has_next>,</#if>
</#list>};

OB.Constants.VERSION_QUERYSTRING = "_version=" + OB.Application.systemVersion + "&_language=" + OB.Application.language;
