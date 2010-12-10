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
isc.OBWidget.addProperties({
  headerStyle: 'OBWidgetHeader',
  showEdges:true,
  edgeImage: "[SKINIMG]../../org.openbravo.client.myob/images/widget/window.png",
  customEdges:null,
  edgeSize:6,
  edgeTop:27,
  edgeBottom:6,
  edgeOffsetTop:2,
  edgeOffsetRight:5,
  edgeOffsetBottom:5,
  showHeaderBackground:false, // part of edges
  showHeaderIcon:true,

      // clear backgroundColor and style since corners are rounded
  backgroundColor:null,
  border: null,
  edgeCenterBackgroundColor:"#FFFFFF",
  bodyColor:"transparent",
  bodyStyle:"windowBody",

  layoutMargin:0,
  membersMargin:0,

  showFooter:false,

  showShadow:false,
  shadowDepth:5
});

isc.OBWidget.changeDefaults('headerDefaults', {
  layoutMargin: 0,
  height: 25
});

isc.OBWidget.changeDefaults('headerLabelDefaults', {
  styleName: 'OBWidgetHeaderText',
  align: isc.Canvas.CENTER
});


// MyOpenbravo dialogs (left menu)

isc.OBMyOBDialog.addProperties({
  headerStyle : 'OBMyOBDialogHeader',
  showEdges : true,
  edgeImage : "[SKINIMG]../../org.openbravo.client.myob/images/dialog/window.png",
  customEdges : null,
  edgeSize : 6,
  edgeTop : 23,
  edgeBottom : 6,
  edgeOffsetTop : 2,
  edgeOffsetRight : 5,
  edgeOffsetBottom : 5,
  showHeaderBackground : false, // part of edges
  showHeaderIcon : true,

  backgroundColor : '#E6F0DB',
  border : null,
  bodyStyle : "OBMyOBDialogBody",

  layoutMargin : 0,
  membersMargin : 0,

  showFooter : false,

  showShadow : false,
  shadowDepth : 5
});

isc.OBMyOBDialog.changeDefaults('headerDefaults', {
  layoutMargin : 0,
  height : 24
});

isc.OBMyOBDialog.changeDefaults('headerLabelDefaults', {
  styleName : 'OBMyOBDialogHeaderText',
  align : isc.Canvas.CENTER
});

isc.OBMyOBDialog.changeDefaults("closeButtonDefaults", {
  src:"[SKINIMG]../../org.openbravo.client.myob/images/dialog/headerIcons/close.png",
  showRollOver:true,
  showDown:false,
  width:15,
  height:15
});