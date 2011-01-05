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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */


isc.ClassFactory.defineClass('OBMessageBarMainIcon', isc.Img);

isc.ClassFactory.defineClass('OBMessageBarDescriptionText', isc.HTMLFlow);

isc.ClassFactory.defineClass('OBMessageBarCloseIcon', isc.ImgButton);

isc.OBMessageBarCloseIcon.addProperties({
  messageBar: null,
  action: function() {
    this.messageBar.hide();
  }
})


isc.ClassFactory.defineClass('OBMessageBar', isc.HLayout);

isc.OBMessageBar.addProperties({
  view: null,
  type: null,
  mainIcon: null,
  text: null,
  closeIcon: null,

  initWidget: function() {
    this.mainIcon = isc.OBMessageBarMainIcon.create({});
    this.text = isc.OBMessageBarDescriptionText.create({
      contents: ''
    });
    this.closeIcon = isc.OBMessageBarCloseIcon.create({messageBar: this});

    this.addMembers([this.mainIcon, this.text, this.closeIcon]);
  },

  setType: function(type) {
    if (this.setTypeStyle) {
      this.setTypeStyle(type);
    }
    this.type = type;
  },

  setText: function(title, text) {
    if (!title) {
      this.text.setContents(text);
    } else {
      this.text.setContents('<b>' + title + '</b><br />' + text);
    }
  },

  setMessage: function(type, title, text) {
    this.setType(type);
    this.setText(title, text);
    this.show();
  }
});