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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB = OB || {};

OB.RoleInheritance = {

  execute: function (params, view) {
    var roleId, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        callback;
    callback = function (rpcResponse, data, rpcRequest) {
      if (data.message.severity === 'success') {
        view.view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, data.message.title, data.message.text);
      } else {
        view.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, data.message.title, data.message.text);
      }
      //refresh child tabs
      view.view.refresh();
    };
    // Retrieves the role id and sends it to the handler to recalculate 
    OB.RemoteCallManager.call(params.actionHandler, {
      roleId: params.roleId,
      action: params.action
    }, {}, callback);
  },

  recalculatePermissions: function (params, view) {
    var isTemplate, message, name, selection = params.button.contextView.viewGrid.getSelectedRecords();
    params.roleId = selection[0].id;
    name = selection[0].name;
    isTemplate = selection[0].template;
    if (isTemplate) {
      params.action = 'TEMPLATE';
      message = 'RecalculateTemplateRolePermissions';
    } else {
      params.action = 'DEFAULT';
      message = 'RecalculateRolePermissions';
    }
    params.actionHandler = 'org.openbravo.roleInheritance.RecalculatePermissionsHandler';
    isc.confirm(OB.I18N.getLabel(message, [name]), {
      isModal: true,
      showModalMask: true,
      title: OB.I18N.getLabel('RecalculatePermissions')
    }, function (clickedOK) {
      if (clickedOK) {
        OB.RoleInheritance.execute(params, view);
      }
    });
  }
}