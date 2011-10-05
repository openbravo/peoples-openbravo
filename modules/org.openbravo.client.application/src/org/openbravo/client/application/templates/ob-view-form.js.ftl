<#--
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
-->
{
    // use theFields instead of fields, when the form
    // gets created, initialized, the datasource is
    // set (ob-standard-view.js buildStructure) 
    // causing re-initialization of the fields,
    // removing the current ones and recreating new ones
    // by using theFields, the form initially does not
    // have fields, which prevents this initial destroy step
    theFields: [
    <#list data.fields as field>
      <@createField field/><#if field_has_next>,</#if>
    </#list>    
    ],

    statusBarFields: [
    <#list data.statusBarFields as sbf>
      '${sbf?js_string}'<#if sbf_has_next>,</#if>
    </#list>
    ],

    // except for the fields all other form properties should be added to the formProperties
    // the formProperties are re-used for inline grid editing
    obFormProperties: {
      onFieldChanged: function(form, item, value) {
        var f = form || this,
            context = this.view.getContextInfo(false, true),
            currentValues = f.view.getCurrentValues(), otherItem;
        <#list data.fields as field>
        <#if field.readOnlyIf != "">
          otherItem = f.getItem('${field.name}');
          if (otherItem && otherItem.disable && otherItem.enable) {
            if (f.readOnly) {
              otherItem.disable();
            } else if(${field.readOnlyIf}) {
              otherItem.disable();
            } else {
              otherItem.enable();
            }
          }
        </#if>
        </#list>
      }
    }
}
