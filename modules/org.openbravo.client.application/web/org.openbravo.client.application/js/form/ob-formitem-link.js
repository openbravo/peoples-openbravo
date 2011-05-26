/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//== OBLinkItem ==
//Input for normal strings (links) with an added icon to navigate to the link  
isc.ClassFactory.defineClass('OBLinkItem', TextItem);

isc.OBLinkItem.addProperties({
  validateOnExit: true,
  icons: [{
    src : '[SKIN]/../../org.openbravo.client.application/images/form/search_picker.png',
    click: function(form, item) {
      var url = item.getValue();
      if(!url || url.indexOf('://') === -1) {
        return;
      }
      window.open(url);
    }
  }],
  validate: function() {
    var url = this.getValue();
    if(!url) {
      return true;
    }
    return OB.Utilities.isValidURL(url);
  }
});

