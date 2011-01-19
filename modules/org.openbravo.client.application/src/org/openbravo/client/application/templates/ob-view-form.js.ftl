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

isc.OBViewForm.create({
    titleOrientation: 'top',
    fields: [
    <#list data.fields as field>
      <@createField field/><#if field_has_next>,</#if>
    </#list>
    ],
    onFieldChanged: function(form, item, value) {
      var context = this.view.getContextInfo(false, true);
      form = form || this;
      <#list data.fields as field>
      <#if field.readOnlyIf != "">
        if(${field.readOnlyIf}) {
          form.getItem('${field.name}').disable();
        } else {
          form.getItem('${field.name}').enable();
        }
      </#if>
      </#list>
    }
})
