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

/* jslint */
isc.OBQuickLaunch.create({
  title: '',
  prompt: OB.I18N.getLabel('${data.label}'),
  buttonType: '${data.buttonType}',
  prefixLabel: '${data.prefixRecent}',
  keyboardShortcutId : '${data.keyboardShortcutId}',
  recentPropertyName: '${data.recentPropertyName}',
  titleLabel: '${data.label}',
  dataSourceId: '${data.dataSourceId}',
  command: '${data.command}',
  initWidget: function(){
   if (this.buttonType) {
     if (this.buttonType === 'createNew' && this.createNew_src) {
       this.setSrc(this.createNew_src);
     } else if (this.buttonType === 'quickLaunch' && this.quickLaunch_src) {
       this.setSrc(this.quickLaunch_src);
     }
   }
   this.Super('initWidget', arguments);
  }
})