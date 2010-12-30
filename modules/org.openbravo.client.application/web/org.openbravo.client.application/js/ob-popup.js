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

isc.ClassFactory.defineClass('OBPopup', isc.Window);

// = OBPopup =
//
// The OBPopup is the Openbravo popup implementator.
//
isc.OBPopup.addProperties( {
  title : '',
  autoSize : true,
  autoCenter : true,
  canDragReposition : true,
  canDragResize : true,
  showFooter : false,
  isModal : true,
  showModalMask : true,
  dismissOnEscape : true,
  animateMinimize : false,
  showHeader : true,
  showHeaderIcon : true,
  showMinimizeButton : true,
  showMaximizeButton : true,
  showCloseButton : true,
  autoDraw : false,
  dragAppearance : 'target',
  closeClick : function() {
    this.Super('closeClick', arguments);
    this.Super('destroy', arguments);
    /*
     * getElementsByClass = function (className, tag) { var resultArray = [],
     * inputs; if (!tag || tag == '' || tag == null || typeof tag ==
     * 'undefined') { inputs = document.all; } else { tag = tag.toLowerCase()
     * inputs = document.getElementsByTagName(tag); } for (var i=0; i<inputs.length;
     * i++){ if (inputs.item(i).getAttribute('class') == className){
     * resultArray.push(inputs.item(i)); } } return resultArray; } var
     * dragOutline = getElementsByClass('dragOutline', 'div')[0];
     * //dragOutline.style.display = 'none'; dragOutline.style.top = 0;
     * dragOutline.style.left = 0; dragOutline.style.width = 0;
     * dragOutline.style.height = 0;
     */
  }
});

isc.OBPopup.changeDefaults('headerLabelDefaults', {
  wrap : false,
  width : '100%',
  inherentWidth : true
});

isc.OBPopup.changeDefaults('restoreButtonDefaults', {
  showRollOver : true,
  showDisabled : true,
  showFocused : true,
  showDown : true,
  showFocusedAsOver : false
});

isc.OBPopup.changeDefaults('closeButtonDefaults', {
  showRollOver : true,
  showDisabled : true,
  showFocused : true,
  showDown : true,
  showFocusedAsOver : false
});

isc.OBPopup.changeDefaults('maximizeButtonDefaults', {
  showRollOver : true,
  showDisabled : true,
  showFocused : true,
  showDown : true,
  showFocusedAsOver : false
});

isc.OBPopup.changeDefaults('minimizeButtonDefaults', {
  showRollOver : true,
  showDisabled : true,
  showFocused : true,
  showDown : true,
  showFocusedAsOver : false
});

isc.OBPopup.changeDefaults('toolbarDefaults', {
  buttonConstructor : 'IButton'
});

isc.ClassFactory.defineClass('OBPopupHTMLFlow', isc.HTMLFlow);

// = OBPopupHTMLFlow =
//
// The OBPopupHTMLFlow is the iframe container to open classic OB popups with
// the new implementation
//
isc.OBPopupHTMLFlow.addProperties( {
  showEdges : false,
  width : '100%',
  height : '100%',
  contentsType : 'page'
});