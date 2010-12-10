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

// Deprecated and replaced by the MyOB module.

// = My Openbravo =
//
// Implements the my openbravo widget. 
//
isc.defineClass('OBMyOpenbravoStatic', isc.HTMLPane).addProperties( {
  contentsType : 'page',
  tabTitle: OB.I18N.getLabel('OBUIAPP_StartPageTitle'),
  contentsURL: OB.PropertyStore.get('OBUIAPP_UIStartTabURL'),
  loadingMessage : OB.I18N.getLabel('OBUIAPP_Loading')
});

isc.OBMyOpenbravoStatic.addMethods( {

  getBookMarkParams: function() {
    var result = {};
    result.myOB = true;
    // are passed on to the tab
    result.canClose = false;
    return result;
  },
  
  isEqualParams: function(params) {
    // if the params are for a my ob return true
    if (params.myOB) {
      return true;
    }
    // a non my ob tab
    return false;
  },
  
  isSameTab : function(viewId, params) {
    if (viewId !== 'OBMyOpenbravo') {
      return false;
    }   
    return this.isEqualParams(params);
  }  
});
