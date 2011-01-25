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
// = Event Handler =
//
// Contains code which is called for page level events. The mouse down event
// is handled to set the correct active view.
//
(function(OB, isc){

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }
  
  function EventHandler(){
  }
  
  EventHandler.prototype = {

      mouseDown: function (canvas) {
        return this.processEvent(canvas);
      },
      
      processEvent: function(canvas) {
        if (!canvas) {
          return true;
        }
        if (canvas.pane && canvas.pane.setAsActiveView) {
          canvas.pane.setAsActiveView();
          return true;
        }
        do {
          if (canvas.view && canvas.view.setAsActiveView) {
            canvas.view.setAsActiveView();
            return true;
          }
          if (canvas.grid) {
            canvas = canvas.grid;
          } if (isc.FormItem.isA(canvas) && canvas.form) {
            canvas = canvas.form;
          } else {
            canvas = canvas.parentElement;
          }
        } while (canvas);
        return true;
      }
  };
  
  OB.EventHandler = new EventHandler();
  isc.Page.setEvent(isc.EH.MOUSE_DOWN, OB.EventHandler, null, 'mouseDown');
})(OB, isc);
