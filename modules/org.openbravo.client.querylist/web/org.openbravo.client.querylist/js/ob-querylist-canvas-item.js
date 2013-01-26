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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBQLCanvasItem_Link', isc.OBGridLinkItem);

isc.OBQLCanvasItem_Link.addProperties({
  setRecord: function () {
    this.setTitle(this.grid.formatLinkValue(this.record, this.field, this.colNum, this.rowNum, this.record[this.field.name]));
  },
  doAction: function () {
    if (this.field.OB_TabId && this.field.OB_LinkExpression) {
      //To open the tab provided in the widget column. Refer https://issues.openbravo.com/view.php?id=17411.
      OB.Utilities.openDirectTab(this.field.OB_TabId, this.record[this.field.OB_LinkExpression]);
    }
  }
});
