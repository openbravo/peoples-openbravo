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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Code used for displaying various UI elements indicating the 
* status of the application (data changed - save button enabled, rotating
* OB logo indicating data processing, etc.).
*/

function disableToolBarButton(id) {
  var link = null;
  var img = null;
  try {
    link = document.getElementById(id);
    img = getObjChild(link);
    if (link.className.indexOf('Main_ToolBar_Button') != -1 && link.className.indexOf('Main_ToolBar_Button_disabled') == -1) {
      link.className = link.className.replace('Main_ToolBar_Button', 'Main_ToolBar_Button_disabled');
      if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
        link.setAttribute('onclick', 'return true; tmp_water_mark; ' + link.getAttribute('onclick'));
      } else {
        var link_onclick = link.getAttribute('onclick').toString();
        link_onclick = link_onclick.replace("function anonymous()","");
        link_onclick = link_onclick.replace("{\n","");
        link_onclick = link_onclick.replace("\n","");
        link_onclick = link_onclick.replace("}","");
        link_onclick = 'return true; tmp_water_mark; ' + link_onclick;
        link['onclick']=new Function(link_onclick);
      }
      link.setAttribute('id', link.getAttribute('id') + '_disabled');
      img.className = img.className + ('_disabled tmp_water_mark');
    }
  } catch (e) {
    return false;
  }
  return true;
}

function enableToolBarButton(id) {
  var link = null;
  var img = null;
  try {
    link = document.getElementById(id + "_disabled");
    img = getObjChild(link);
    if (link.className.indexOf('Main_ToolBar_Button_disabled') != -1) {
      link.className = link.className.replace('Main_ToolBar_Button_disabled', 'Main_ToolBar_Button');
      if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
        link.setAttribute('onclick', link.getAttribute('onclick').replace('return true; tmp_water_mark; ', ''));
      } else {
        var link_onclick = link.getAttribute('onclick').toString();
        link_onclick = link_onclick.replace("function anonymous()","");
        link_onclick = link_onclick.replace("{\n","");
        link_onclick = link_onclick.replace("\n","");
        link_onclick = link_onclick.replace("}","");
        link_onclick = link_onclick.replace('return true; tmp_water_mark; ', '');
        link['onclick']=new Function(link_onclick);
      }
      link.setAttribute('id', link.getAttribute('id').replace('_disabled', ''));
      img.className = img.className.replace('_disabled tmp_water_mark', '');
    }
  } catch (e) {
    return false;
  }
  return true;
}

function disableAttributeWithFunction(element, type, attribute) {
  if (type == 'obj') { var obj = element }
  if (type == 'id') { var obj = document.getElementById(id); }
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
    obj.setAttribute(attribute, 'return true; tmp_water_mark; ' + obj.getAttribute(attribute));
  } else {
    var obj_attribute = obj.getAttribute(attribute).toString();
    obj_attribute = obj_attribute.replace("function anonymous()","");
    obj_attribute = obj_attribute.replace("{\n","");
    obj_attribute = obj_attribute.replace("\n","");
    obj_attribute = obj_attribute.replace("}","");
    obj_attribute = 'return true; tmp_water_mark; ' + obj_attribute;
    obj[attribute]=new Function(obj_attribute);
  }
}

function enableAttributeWithFunction(element, type, attribute) {
  if (type == 'obj') { var obj = element }
  if (type == 'id') { var obj = document.getElementById(id); }
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
    obj.setAttribute(attribute, obj.getAttribute(attribute).replace('return true; tmp_water_mark; ', ''));
  } else {
    var obj_attribute = obj.getAttribute(attribute).toString();
    obj_attribute = obj_attribute.replace("function anonymous()","");
    obj_attribute = obj_attribute.replace("{\n","");
    obj_attribute = obj_attribute.replace("\n","");
    obj_attribute = obj_attribute.replace("}","");
    obj_attribute = obj_attribute.replace('return true; tmp_water_mark; ', '');
    obj[attribute]=new Function(obj_attribute);
  }
}

function disableButton(id) {
  var link = null;
  var img = null;                                                                                              //Used in old r2.40 button definition
  try {
    link = document.getElementById(id);
    img = getObjChild(link);                                                                                   //Used in old r2.40 button definition
    if (link.tagName == 'A') {                                                                                 //Used in old r2.40 button definition
      if (link.className.indexOf('ButtonLink') != -1 && link.className.indexOf('ButtonLink_disabled') == -1) { //Used in old r2.40 button definition
        link.className = link.className.replace('ButtonLink_default', 'ButtonLink');                           //Used in old r2.40 button definition
        link.className = link.className.replace('ButtonLink', 'ButtonLink_disabled');                          //Used in old r2.40 button definition
        disableAttributeWithFunction(link, 'obj', 'onclick');                                                  //Used in old r2.40 button definition
        disableAttributeWithFunction(link, 'obj', 'onfocus');                                                  //Used in old r2.40 button definition
        disableAttributeWithFunction(link, 'obj', 'onkeypress');                                               //Used in old r2.40 button definition
        disableAttributeWithFunction(link, 'obj', 'onkeyup');                                                  //Used in old r2.40 button definition
        link.setAttribute('id', link.getAttribute('id') + '_disabled');                                        //Used in old r2.40 button definition
        disableAttributeWithFunction(img, 'obj', 'onmouseout');                                                //Used in old r2.40 button definition
        disableAttributeWithFunction(img, 'obj', 'onmouseover');                                               //Used in old r2.40 button definition
        disableAttributeWithFunction(img, 'obj', 'onmousedown');                                               //Used in old r2.40 button definition
        disableAttributeWithFunction(img, 'obj', 'onmouseup');                                                 //Used in old r2.40 button definition
      }                                                                                                        //Used in old r2.40 button definition
    } else {                                                                                                   //Used in old r2.40 button definition
      if (link.className.indexOf('ButtonLink') != -1 && link.className.indexOf('ButtonLink_disabled') == -1) {
        link.className = link.className.replace('ButtonLink_default', 'ButtonLink');
        link.className = link.className.replace('ButtonLink', 'ButtonLink_disabled');
        link.setAttribute('id', link.getAttribute('id') + '_disabled');
        link.disabled = true;
        disableAttributeWithFunction(link, 'obj', 'onclick');
      }
    }

  } catch (e) {
    return false;
  }
  return true;
}

function enableButton(id) {
  var link = null;
  var img = null;                                                                     //Used in old r2.40 button definition
  try {
    link = document.getElementById(id + "_disabled");
    img = getObjChild(link);                                                          //Used in old r2.40 button definition
    if (link.tagName == 'A') {                                                        //Used in old r2.40 button definition
      if (link.className.indexOf('ButtonLink_disabled') != -1) {                      //Used in old r2.40 button definition
        link.className = link.className.replace('ButtonLink_disabled', 'ButtonLink'); //Used in old r2.40 button definition
        enableAttributeWithFunction(link, 'obj', 'onclick');                          //Used in old r2.40 button definition
        enableAttributeWithFunction(link, 'obj', 'onfocus');                          //Used in old r2.40 button definition
        enableAttributeWithFunction(link, 'obj', 'onkeypress');                       //Used in old r2.40 button definition
        enableAttributeWithFunction(link, 'obj', 'onkeyup');                          //Used in old r2.40 button definition
        link.setAttribute('id', link.getAttribute('id').replace('_disabled', ''));    //Used in old r2.40 button definition
        enableAttributeWithFunction(img, 'obj', 'onmouseout');                        //Used in old r2.40 button definition
        enableAttributeWithFunction(img, 'obj', 'onmouseover');                       //Used in old r2.40 button definition
        enableAttributeWithFunction(img, 'obj', 'onmousedown');                       //Used in old r2.40 button definition
        enableAttributeWithFunction(img, 'obj', 'onmouseup');                         //Used in old r2.40 button definition
      }                                                                               //Used in old r2.40 button definition
    } else {                                                                          //Used in old r2.40 button definition
      if (link.className.indexOf('ButtonLink_disabled') != -1) {
        link.className = link.className.replace('ButtonLink_disabled', 'ButtonLink');
        link.setAttribute('id', link.getAttribute('id').replace('_disabled', ''));
        link.disabled = false;
        enableAttributeWithFunction(link, 'obj', 'onclick');
      }
    }

  } catch (e) {
    return false;
  }
  activateDefaultAction();
  return true;
}

function setWindowEditing(value) {
  var isNewWindow;
  if (document.getElementById('linkButtonEdition').className.indexOf('Main_LeftTabsBar_ButtonRight_Icon_edition_selected') != -1) {
    isNewWindow = false;
  } else {
    isNewWindow = true;
  }

  if (isNewWindow==false) {
    var Buttons = new Array(
      'linkButtonSave',
      'linkButtonSave_Next',
      'linkButtonSave_Relation',
      'linkButtonSave_New',
      'linkButtonUndo'
    );
  } else if (isNewWindow==true) {
    var Buttons = new Array(
      'linkButtonUndo'
    );
  }

  if (value==true) {
    for (var i = 0; i < Buttons.length; i++) {
      isUserChanges = true;
      enableToolBarButton(Buttons[i]);
    }
  } else if (value==false) {
    for (var i = 0; i < Buttons.length; i++) {
      isUserChanges = false;
      disableToolBarButton(Buttons[i]);
    }
  }
}

function setCalloutProcessing(value) {
  var icon = null;
  if (document.getElementById('TabStatusIcon')) {
    icon = document.getElementById('TabStatusIcon');
  } else {
    return false;
  }
  try {
    if (value == true) {
      if (icon.className.indexOf('tabTitle_elements_image_normal_icon') != -1) {
        icon.className = icon.className.replace('tabTitle_elements_image_normal_icon', 'tabTitle_elements_image_processing_icon');
      }
    } else if (value == false) {
      if (icon.className.indexOf('tabTitle_elements_image_processing_icon') != -1) {
        icon.className = icon.className.replace('tabTitle_elements_image_processing_icon', 'tabTitle_elements_image_normal_icon');
      }
    }
  }
  catch (e) {
    return false;
  }
  return true;
}

function setGridRefreshing(value) {
  try {
    setCalloutProcessing(value);
  }
  catch (e) {
    return false;
  }
  return true;
}

function setMenuLoading(value) {
  var frame = top.document;
  var frameset = frame.getElementById("framesetMenu");
  if (!frameset) 
    return false;
  try {
    if (value == true) {
      if (top.isRTL == true) {
        frameset.cols = "*,0%," + top.menuWidth;
      } else {
        frameset.cols = top.menuWidth + ",0%,*";
      }
    } else if (value == false) {
      if (top.isRTL == true) {
        frameset.cols = "*," + top.menuWidth + ",0%";
      } else {
        frameset.cols = "0%," + top.menuWidth + ",*";
      }
    }
  }
  catch (e) {
    return false;
  }
  return true;
}

function processingModeCode(target, display) {
  var string = '';
  string += "<div class=\"" + target + "_Status_Processing_Container\" id=\"Processing_Container\"";
  if (display==false) string += " style=\"display: none\"";
  string += ">\n";
  string += "  <div class=\"" + target + "_Status_Processing_Elements_Container\">\n";
  string += "    <div class=\"" + target + "_Status_Processing_logo\">\n";
  string += "      <div class=\"" + target + "_Status_Processing_logo_dimension\"></div>";
  string += "    </div>\n";
  string += "    <div class=\"" + target + "_Status_Processing_text\">Processing...</div>\n";
  string += "  </div>\n";
  string += "</div>\n";
  return string;
}

function setProcessingMode(target, value) {
  if (target=='popup') {
    var popup_code = document.getElementsByTagName('BODY')[0].innerHTML;
    isKeyboardLocked=value;
    if (document.getElementById('Processing_Container')) {
      document.getElementById('Processing_Container').style.display = (value?"":"none");
    } else {
      document.getElementsByTagName('BODY')[0].innerHTML = processingModeCode('Popup', value) + popup_code;
    }
  } else {
    var frame_menu = top.frames['frameMenu'];
    var frame_window = top.frames['appFrame'];
    var menu_code = frame_menu.document.getElementsByTagName('BODY')[0].innerHTML;
    var window_code = frame_window.document.getElementsByTagName('BODY')[0].innerHTML;
    isKeyboardLocked=value;
    if (frame_window.document.getElementById('Processing_Container')) {
      frame_window.document.getElementById('Processing_Container').style.display = (value?"":"none");
    } else {
      frame_window.document.getElementsByTagName('BODY')[0].innerHTML = processingModeCode('Main', value) + window_code;
    }
    if (frame_menu.document.getElementById('Processing_Container')) {
      frame_menu.document.getElementById('Processing_Container').style.display = (value?"":"none");
    } else {
      frame_menu.document.getElementsByTagName('BODY')[0].innerHTML = processingModeCode('Menu', value) + menu_code;
    }
  }
}

function setAlertIcon(value) {
    if (value==true) changeClass("alertImage", "Menu_ToolBar_Button_Icon_alert", "Menu_ToolBar_Button_Icon_alertActive");
    if (value==false) changeClass("alertImage", "Menu_ToolBar_Button_Icon_alertActive", "Menu_ToolBar_Button_Icon_alert");
    return true;
}

function setAttachmentIcon(value) {
  if (value==true) changeClass("buttonAttachment", "Main_ToolBar_Button_Icon_Attachment", "Main_ToolBar_Button_Icon_AttachedDocuments");
  if (value==false) changeClass("buttonAttachment", "Main_ToolBar_Button_Icon_AttachedDocuments", "Main_ToolBar_Button_Icon_Attachment");
  return true;
}

function callbackAttachmentIcon(){
  var strText = "";
  if (getReadyStateHandler(xmlreq,null,false)) {
    try {
      if (xmlreq.responseText) strText = xmlreq.responseText;
    } catch (e) {
    }
    if(strText == "true" && document.getElementById("buttonAttachment").className.indexOf("Main_ToolBar_Button_Icon_AttachedDocuments") == -1)
      setAttachmentIcon(true);
    else if(strText == "false" && document.getElementById("buttonAttachment").className.indexOf("Main_ToolBar_Button_Icon_AttachedDocuments") != -1) 
      setAttachmentIcon(false);
  }
  return true;
}

function checkAttachmentIcon(){
  if(document.getElementById('buttonAttachment'))
    submitXmlHttpRequest(callbackAttachmentIcon, null, 'CHECK', "../businessUtility/TabAttachments_FS.html?inpKey=" +document.getElementsByName(document.frmMain.inpKeyName.value)[0].value , false);
}

function checkAttachmentIconRelation(){
  var value = dijit.byId('grid').getSelectedRows();
  if (value==null || value=="" || value.length>1) return false;
  if (document.frmMain) {
    setInputValue(document.frmMain.inpKeyName.value, value);
    checkAttachmentIcon();
  }
}
