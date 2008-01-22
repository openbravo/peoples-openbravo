/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-200/ Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
/**
* @fileoverview This JavaScript library contains the shortcuts definition of openbravo
*/

HTMLElement.prototype.click = function() {
var evt = this.ownerDocument.createEvent('MouseEvents');
evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
this.dispatchEvent(evt);
}


/**
* Builds the keys array on each screen. Each key that we want to use should have this structure.
* @param {String} key A text version of the handled key.
* @param {String} evalfunc Function that will be eval when the key is is pressed.
* @param {String} field Name of the field on the window. If is null, is a global event, for the hole window.
* @param {String} auxKey Text defining the auxiliar key. The value could be CTRL for the Control key, ALT for the Alt, null if we don't have to use an auxiliar key.
* @param {Boolean} propagateKey True if the key is going to be prograpated or false if is not going to be propagated.
* @param {String} eventShotter Function that will launch the process.
*/
function keyArrayItem(key, evalfunc, field, auxKey, propagateKey, event) {
  this.key = key;
  this.evalfunc = evalfunc;
  this.field = field;
  this.auxKey = auxKey;
  this.propagateKey = propagateKey;
  this.eventShotter = event;
}

/**
* Defines the keys array for all the application.
*/
function getShortcuts(type) {
  if (type==null || type=="" || type=="null") {
    this.keyArray = new Array();
  } else if (type=='window') {
    this.keyArray = new Array(
    new keyArrayItem("M", "putFocusOnMenu();", null, "ctrlKey", false, 'onkeydown')
    );
  } else if (type=='menu') {
    this.keyArray = new Array(
    new keyArrayItem("UPARROW", "menuUpKey(true);", null, null, false, 'onkeydown'),
    new keyArrayItem("RIGHTARROW", "menuRightKey();", null, null, false, 'onkeydown'),
    new keyArrayItem("DOWNARROW", "menuDownKey(true);", null, null, false, 'onkeydown'),
    new keyArrayItem("LEFTARROW", "menuLeftKey();", null, null, false, 'onkeydown'),
    new keyArrayItem("HOME", "menuHomeKey();", null, null, false, 'onkeydown'),
    new keyArrayItem("END", "menuEndKey();", null, null, false, 'onkeydown'),
    new keyArrayItem("ENTER", "menuEnterKey();", null, null, false, 'onkeydown'),
    new keyArrayItem("UPARROW", "menuUpKey(false);", null, null, null, 'onkeyup'),
    new keyArrayItem("DOWNARROW", "menuDownKey(false);", null, null, null, 'onkeyup'),    
    new keyArrayItem("M", "alert('M');", null, "ctrlKey", false, 'onkeydown')
    );
  }
}