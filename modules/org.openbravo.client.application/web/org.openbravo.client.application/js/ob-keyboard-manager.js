/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License+
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
// = Keyboard Manager =
//
// Manages the keyboard shortcuts which are used to open flyouts, menus etc.
//
(function(OB, isc){

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }
  
  // cache object references locally
  var O = OB, ISC = isc, keyboardMgr; // Local reference to UrlManager instance
  function KeyboardManager(){
  }
  
  KeyboardManager.prototype = {
  
    action: {
      // ** {{{ KeyboardManager.keyPress }}} **
      // Manages the keyPress event
      keyPress: function(){
      },
      
      // ** {{{ KeyboardManager.keyUp }}} **
      // Manages the keyUp event
      keyUp: function(){
      },
      
      // ** {{{ KeyboardManager.keyDown }}} **
      // Manages the keyDown event
      keyDown: function(){
        var pushedKS = {};
        pushedKS.ctrl = false;
        pushedKS.alt = false;
        pushedKS.shift = false;
        pushedKS.key = null;
        if (isc.Event.ctrlKeyDown()) {
          pushedKS.ctrl = true;
        }
        if (isc.Event.altKeyDown()) {
          pushedKS.alt = true;
        }
        if (isc.Event.shiftKeyDown()) {
          pushedKS.shift = true;
        }
        pushedKS.key = isc.Event.getKey();
        var position = keyboardMgr.KS.getPosition(pushedKS, 'keyComb');
        if (position !== null) {
          keyboardMgr.KS.execute(position);
        }
      }
    },
    
    KS: {
    
      setPredefinedKSList: function(RefList){
        var i;
        var list = [];
        
        list = OB.PropertyStore.get(RefList);
        if (list) {
          for (i = 0; i < list.length; i++) {
            this.set(list[i].id, null, null, list[i].keyComb);
          }
        }
      },

      set: function(id, action, funcParam, keyComb) {
        if (typeof id === 'undefined' || id === null) {
          return false;
        }

        var position = this.getPosition(id, 'id');
        if (position === null) {
          position = this.list.length;
          this.list[position] = {};
          this.list[position].keyComb = {};
        }
        if (typeof id !== 'undefined' && id !== null) {
          this.list[position].id = id;
        }
        if (typeof action !== 'undefined' && action !== null) {
          this.list[position].action = action;
        }
        if (typeof funcParam !== 'undefined' && funcParam !== null) {
          this.list[position].funcParam = funcParam;
        }
        if (typeof keyComb !== 'undefined' && keyComb !== null) {
          if (typeof keyComb.ctrl === 'undefined') {
            this.list[position].keyComb.ctrl = false;
          } else {
            this.list[position].keyComb.ctrl = keyComb.ctrl;
          }
          if (typeof keyComb.alt === 'undefined') {
            this.list[position].keyComb.alt = false;
          } else {
            this.list[position].keyComb.alt = keyComb.alt;
          }
          if (typeof keyComb.shift === 'undefined') {
            this.list[position].keyComb.shift = false;
          } else {
            this.list[position].keyComb.shift = keyComb.shift;
          }
          if (typeof keyComb.key === 'undefined') {
            this.list[position].keyComb.key = null;
          } else {
            this.list[position].keyComb.key = keyComb.key;
          }
        }
      },
      
      remove: function(id){
        var position = this.getPosition(id, 'id');
        if (position === null) {
          return false;
        }
        delete this.list[position];
      },
      
      getPosition: function(element, searchPattern){
        for (var i = 0; i < this.list.length; i++) {
          if (searchPattern === 'id' && this.list[i].id === element) {
            return i;
          } else if (searchPattern === 'keyComb' &&
          this.list[i].keyComb.ctrl === element.ctrl &&
          this.list[i].keyComb.alt === element.alt &&
          this.list[i].keyComb.shift === element.shift &&
          this.list[i].keyComb.key === element.key) {
            return i;
          } else if (searchPattern === 'action' &&
          this.list[i].action === element) {
            return i;
          }
        }
        return null;
      },
      
      execute: function(position){
        if (this.list[position].action !== null) {
          this.list[position].action(this.list[position].funcParam);
        }
      },
      
      list: []
    }
  
  };
  
  // Initialize KeyboardManager object
  keyboardMgr = O.KeyboardManager = new KeyboardManager();
  
  isc.Page.setEvent('keyPress', 'OB.KeyboardManager.action.keyDown()');
  
})(OB, isc);
