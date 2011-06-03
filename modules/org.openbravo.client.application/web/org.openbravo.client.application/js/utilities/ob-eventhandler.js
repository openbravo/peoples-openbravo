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
        var onClickTarget = null, lastEvent = isc.EventHandler.lastEvent;
        
        // handle a special case:
        // https://issues.openbravo.com/view.php?id=17439
        // when setting the active view we loose the click
        if (lastEvent && lastEvent.eventType === 'mouseDown' &&
            lastEvent.DOMevent && lastEvent.DOMevent.target && lastEvent.DOMevent.target.onclick) {
          onClickTarget = lastEvent.DOMevent.target;
        }
        
        if (!canvas) {
          return true;
        }
        if (canvas.pane && canvas.pane.setAsActiveView) {
          canvas.pane.setAsActiveView();
          return true;
        }
        
        // when clicking in the tabbar
        if (canvas.tabSet && canvas.tabSet.getSelectedTab() && canvas.tabSet.getSelectedTab().pane 
            && canvas.tabSet.getSelectedTab().pane.setAsActiveView) {
          canvas.tabSet.getSelectedTab().pane.setAsActiveView();
          return true;
        }
        
        do {
          if (canvas.view && canvas.view.setAsActiveView) {
            // don't do this if already activec
            if (canvas.view.isActiveView()) {
              onClickTarget = null;
            }
            canvas.view.setAsActiveView();
            if (onClickTarget) {
              onClickTarget.onclick();
            }
            return true;
          }
          if (isc.FormItem.isA(canvas)) {
            var view = OB.Utilities.determineViewOfFormItem(item);
            if (view && view.setAsActiveView) {
              view.setAsActiveView();
              return true;
            }
          }
          if (canvas.mouseDownCancelParentPropagation) { // Added to be able to scroll the toolbar without focusing top level view
            canvas = null;
          } else if (!canvas.parentElement && canvas.grid) {
            canvas = canvas.grid;
          } else {
            canvas = canvas.parentElement;
          }
        } while (canvas);
        return true;
      }
  };
  
  OB.EventHandler = new EventHandler();
  isc.Page.setEvent(isc.EH.MOUSE_DOWN, OB.EventHandler, null, 'mouseDown');
}(OB, isc));
