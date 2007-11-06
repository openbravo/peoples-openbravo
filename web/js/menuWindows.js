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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
<!-- Codigo JavaScript

var classOpened = "Icon_folderOpened";
var classClosed = "Icon_folder";
var isOpened = false;

function getReference(id) {
  if (document.getElementById) return document.getElementById(id);
  else if (document.all) return document.all[id];
  else if (document.layers) return document.layers[id];
  else return null;
}

function getStyle(id, esId) {
  var ref;
  if (esId==null || esId) ref = getReference(id);
  else ref = id;
  try {
    return ((document.layers) ? ref : ref.style);
  } catch(e) {}
  return null;
}

function cambiarSituacion(evt) {
  var elemento = (!document.all) ? evt.target : event.srcElement;
  var indice = null;
  if (document.all) indice = elemento.sourceIndex;
  var hijo=null;
  var total = "";
  try {
    if (elemento.id.indexOf("folder")==-1 && elemento.id.indexOf("folderCell")==-1 && elemento.id.indexOf("folderImg")==-1) {
      if (element.onclick) return elemento.onclick();
      else return true;
    } else if (elemento.id.indexOf("folderNoChilds")==0) return true;
    else if (elemento.id.indexOf("folderCell1")==0) total = elemento.id.replace("folderCell1_", "");
    else if (elemento.id.indexOf("folderCell2")==0) total = elemento.id.replace("folderCell2_", "");
    else if (elemento.id.indexOf("folderImg")==0) total = elemento.id.replace("folderImg", "");
    else total = elemento.id.replace("folder","");
  } catch (e) {}

  hijo = getReference("parent" + total);
  var actualclass = getObjectClass("folderImg" + total);
  var selectedClass = getObjectClass("child" + total);
  if (selectedClass==null) selectedClass = "";
  selectedClass = selectedClass.replace(" Opened", "");
  var obj = getStyle(hijo, false);
  if (!obj) return;
  try {
    if (obj.display=="none") {
      obj.display="";
      actualclass = actualclass.replace(classClosed, classOpened);
      setClass("folderImg" + total, actualclass);
      setClass("child" + total, selectedClass + " Opened");
    } else {
      isOpened = false;
      obj.display="none";
      actualclass = actualclass.replace(classOpened, classClosed);
      setClass("folderImg" + total, actualclass);
      setClass("child" + total, selectedClass);
    }
  } catch (ignored) {}
  return false;
}

function checkSelected(id) {
  var selected = document.forms[0].inpSelected;
  if (selected.value && selected.value!="") {
    var actualclass = getObjectClass(selected.value);
    if (actualclass!=null && actualclass!="") {
      actualclass = actualclass.replace(" Selected", "");
      actualclass = actualclass.replace("Selected", "");
      setClass(selected.value, actualclass);
    }
    selected.value = "";
  }
  selected.value = id;
  var actualclass = getObjectClass(selected.value);
  if (actualclass!=null && actualclass!="") {
    actualclass = actualclass.replace(" Selected", "");
    actualclass = actualclass.replace("Selected", "");
    setClass(selected.value, actualclass + " Selected");
  } else setClass(selected.value, " Selected");
}


document.onclick=cambiarSituacion;

if (document.layers) {
  window.captureEvents(Event.ONCLICK);
  window.onclick=cambiarSituacion;
}

function setHover(obj) {
  isOpened = false
  var actualclass = obj.className;
  obj.className = obj.className.replace(' Opened', ' Child_hover');
  if (obj.className == actualclass) obj.className=obj.className + ' Child_hover';
  else isOpened = true;
  return true;
}

function setMouseDown(obj) {
  isOpened = false
  var actualclass = obj.className;
  if (obj.className.indexOf(" Child_active")!=-1) {
    obj.className = obj.className.replace(' Child_active', '');
  }
  obj.className = obj.className.replace(' Opened', ' Child_active');
  if (obj.className == actualclass) obj.className=obj.className + ' Child_active';
  else isOpened = true;
  return true;
}

function setMouseUp(obj) {
  var actualclass = obj.className;
  if (obj.className.indexOf(" Child_active")!=-1) {
    if (isOpened) obj.className = obj.className.replace(' Child_active', ' Opened');
    else obj.className = obj.className.replace(' Child_active', '');
    isOpened = false
  }
  return true;
}

//-->
