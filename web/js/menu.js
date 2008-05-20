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

/**
* @fileoverview Deprecated. (used in 2.2x and earlier versions to support 
* the dynamic tree menu on the left)
*/

<!-- Codigo JavaScript

var strImagenDesplegado = baseDirection + "images/CarpetaAbierta.gif";
var strImagenCerrado = baseDirection + "images/CarpetaCerrada.gif";

var strSufijo = "Abierta";

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
  return ((document.layers) ? ref : ref.style);
}

function cambiarSituacion(evt) {
  var elemento = (!document.all) ? evt.target : event.srcElement;
  var indice = null;
  if (document.all) indice = elemento.sourceIndex;
  var hijo=null;
  if (elemento.id.indexOf("folder")==-1 && elemento.id.indexOf("folderHref")==-1) return;
  else if (elemento.id.indexOf("folderNoChilds")==0 || elemento.id.indexOf("folderHrefNoChilds")==0) return;
  else if (elemento.id.indexOf("folderHref")==0) {
    elemento = (document.all)?document.all[indice-1]: evt.target.parentNode;
    hijo = (document.all)? document.all[indice+1] : elemento.nextSibling;
  } else {
    if (document.all) hijo = document.all[indice+2];
    else hijo = elemento.nextSibling;
  }
  if (!document.all && !getStyle(hijo, false)) hijo = hijo.nextSibling;
  if (getStyle(hijo, false).display=="none") {
    getStyle(hijo, false).display="";
    if (!elemento.className) getStyle(elemento, false).listStyleImage="url(" + strImagenDesplegado + ")";
    else elemento.className = elemento.className + strSufijo;
  } else {
    getStyle(hijo, false).display="none";
    if (!elemento.className) getStyle(elemento, false).listStyleImage="url(" + strImagenCerrado + ")";
    else {
      var strClass = elemento.className;
      elemento.className = strClass.substring(0, strClass.length - strSufijo.length);
    }
  }
  return false;
}


document.onclick=cambiarSituacion;

if (document.layers) {
  window.captureEvents(Event.ONCLICK);
  window.onclick=cambiarSituacion;
}

//-->
