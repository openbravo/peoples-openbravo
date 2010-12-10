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
isc.defineClass("OBMyOpenbravoImplementation", OBMyOpenbravo);

isc.OBMyOpenbravoImplementation.addProperties({
  enableAdminMode: ${data.enableAdminMode},
  adminModeValueMap: ${data.adminModeValueMap},

  availableWidgetClasses: [
    <#list data.availableWidgetClasses as widgetClassDefinition>
      ${widgetClassDefinition}<#if widgetClassDefinition_has_next>,</#if>
    </#list>
    ],
  widgets: [
    <#list data.widgetInstanceDefinitions as widgetInstanceDefinition>
      ${widgetInstanceDefinition}<#if widgetInstanceDefinition_has_next>,</#if>
    </#list>
    ]
});

//isc.Page.loadStyleSheet('[SKIN]../org.openbravo.client.myob/ob-widget-styles.css?' + OB.Application.moduleVersionParameters['2758CD25B2704AF6BBAD10365FC82C06']);
