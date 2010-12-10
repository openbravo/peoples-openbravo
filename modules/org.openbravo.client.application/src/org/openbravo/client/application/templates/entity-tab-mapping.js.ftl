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

// creates the mapping from entities to windows/tabs to support direct linking
OB.EntityToWindowMapping = {    
    <#list data.entityWindowMappings as mapping>
    '${mapping.entityName?js_string}': {viewId: '_${mapping.windowId?js_string}', tabId: '${mapping.tabId?js_string}', tabTitle: '${mapping.tabTitle?js_string}'}<#if mapping_has_next>,</#if>
    </#list>
};

OB.EntityToWindowMapping.openView = function(entity, recordId) {
    var mapping = OB.EntityToWindowMapping[entity];
    var openObject;
    if (!mapping) {
        // replace with a translatable warning
        isc.warn(OB.I18N.getLabel('OBUIAPP_NoAccess'), 'OB');
    } else {
        // create shallow copy
        openObject = isc.addProperties({}, mapping);
        // set the record id
        openObject.targetRecordId = recordId;
        openObject.targetEntity = entity;
        OB.Layout.ViewManager.openView(openObject.viewId, openObject);
    }
}; 


