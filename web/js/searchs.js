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
* @fileoverview Contains Javascript functions used by the selectors 
* (eg. selecting a business partner or a product within a sales order).
*/

var winSelector=null;

var gForm=null;
var gCampoClave=null;
var gCampoTexto=null;
var gDepurar=false;
var gIsMultiLineSearch=false;
var baseImage="Question.jpg";

function selGetRef(id) 
{
  if (document.getElementById) return document.getElementById(id);
  if (document.all) return document.all[id];
  if (document.layers) return document.layers[id];
}

function SearchElements(campo, esRef, valor) {
  this.campo = campo;
  this.esRef = esRef;
  this.valor = valor;
}

/*  Saca el valor seleccionado del combo que se le indique. No admite selecciones múltiples y
  devolverá null en caso de que no se halla seleccionado ninguna opción
*/
function valorCombo(combo)
{
  if (combo.selectedIndex == -1)
    return null;
  else
    return combo.options[combo.selectedIndex].value;
}

function textoCombo(combo)
{
  if (combo.selectedIndex == -1)
    return "";
  else
    return combo.options[combo.selectedIndex].text;
}


function windowSearch(strPagina, strHeight, strWidth, strTop, strLeft, strVentana, parametros, strValueID) {
  var complementosNS4 = ""
  var camposAccesorios = "";
  closeWindowSearch();
  winSelector=null;
  if (strHeight==null)
    strHeight=(screen.height - 100);
  if (strWidth==null)
    strWidth=(screen.width - 10);
  if (strTop==null) 
    strTop=parseInt((screen.height - strHeight)/2);
  if (strLeft==null) 
    strLeft=parseInt((screen.width - strWidth)/2);
  if (strVentana==null)
    strVentana="SELECTOR";
  if (strValueID!=null && strValueID!="") {
    camposAccesorios = "inpNameValue=" + escape(strValueID);
  }
  var oculta;
  if (parametros!=null) {
    var total = parametros.length;
    for (var i=0;i<total;i++) {
      if (camposAccesorios!="") camposAccesorios+="&";
      if (parametros[i]=="isMultiLine" && parametros[i+1]=="Y") gIsMultiLineSearch=true;
      camposAccesorios += parametros[i] + "=" + ((parametros[i+1]!=null)?escape(parametros[i+1]):"");
      if (parametros[i]=="Command") oculta=true;
      i++;
    }
  }
  
  if (navigator.appName.indexOf("Netscape"))
    complementosNS4 = "alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ";
  var complementos = complementosNS4 + "height=" + strHeight + ", width=" + strWidth + ", left=" + strLeft + ", top=" + strTop + ", screenX=" + strLeft + ", screenY=" + strTop + ", location=0, resizable=0, scrollbars=1, status=0, toolbar=0, titlebar=0";
  winSelector = window.open(strPagina + ((camposAccesorios=="")?"":"?" + camposAccesorios), strVentana, complementos);
  if (winSelector!=null) {
    if (oculta) window.focus();
    else winSelector.focus();
    //winSelector.onunload = function(){top.opener.closeWindowSearch();};
    activarEventos();
  }
}

function closeWindowSearch() {
  if (winSelector && !winSelector.closed) {
    winSelector.close();
    winSelector=null;
    desactivarEventos();
  }
}


function openSearch(strTop, strLeft, strNombreSelector, strWindowName, depurar, strForm, strItem, strSpanId, strValueID)
{
  if (strNombreSelector!=null) {
    gForm = (strForm==null)?"forms[0]":strForm;
    gCampoClave=strItem;
    gCampoTexto=strSpanId;
    strValueId = (strValueID==null)?"":strValueID;
    gDepurar = (depurar==null)?false:depurar;
    var parametros=new Array();
    for (var i=9;arguments[i]!=null;i++) {
      parametros[i-9] = arguments[i];
    }
    if (strNombreSelector.indexOf("Location")!=-1) windowSearch(strNombreSelector, 300, 600, strTop, strLeft, strWindowName, parametros, strValueID);
    else windowSearch(strNombreSelector, null, 900, strTop, strLeft, strWindowName, parametros, strValueID);
  }
}

function openMultiSearch(strTop, strLeft, strNombreSelector, strWindowName, depurar, strForm, strItem)
{
  if (strNombreSelector!=null) {
    gForm = (strForm==null)?"forms[0]":strForm;
    gCampoClave=strItem;
    gDepurar = (depurar==null)?false:depurar;
    var parametros=new Array();
    for (var i=7;arguments[i]!=null;i++) {
      parametros[i-7] = arguments[i];
    }
    windowSearch(strNombreSelector, null, 900, strTop, strLeft, strWindowName, parametros, null);
  }
}

function openPAttribute(strTop, strLeft, strNombreSelector, strWindowName, depurar, strForm, strItem, strSpanId, strValueID)
{
  if (strNombreSelector!=null) {
    gForm = (strForm==null)?"forms[0]":strForm;
    gCampoClave=strItem;
    gCampoTexto=strSpanId;
    strValueId = (strValueID==null)?"":strValueID;
    gDepurar = (depurar==null)?false:depurar;
    var parametros=new Array();
    for (var i=9;arguments[i]!=null;i++) {
      parametros[i-9] = arguments[i];
    }
    windowSearch(strNombreSelector, 450, 650, strTop, strLeft, strWindowName, parametros, strValueID);
  }
}
/*
function openLocation(strTop, strLeft, strNombreSelector, strWindowName, depurar, strForm, strItem, strSpanId, strValueID)
{
  if (strNombreSelector!=null) {
    gForm = (strForm==null)?"forms[0]":strForm;
    gCampoClave=strItem;
    gCampoTexto=strSpanId;
    strValueId = (strValueID==null)?"":strValueID;
    gDepurar = (depurar==null)?false:depurar;
    var parametros=new Array();
    for (var i=9;arguments[i]!=null;i++) {
      parametros[i-9] = arguments[i];
    }
    windowSearch(strNombreSelector, 290, 500, strTop, strLeft, strWindowName, parametros, strValueID);
  }
}*/

function getField(fieldName) {
  if (gIsMultiLineSearch) {
    return buscarHijo(gFilaActual, "name", fieldName);
  } else {
    return eval("document." + gForm + "." + fieldName);
  }
}

function closeSearch(action, strClave, strTexto, parametros, wait) {
  if (wait!=false) {
    setTimeout(function() {closeSearch(action, strClave, strTexto, parametros, false);},100);
    return;
  } else {
    if (winSelector==null) return true;
    if (gForm!=null && gCampoClave!=null && gCampoTexto!=null) {
      var clave = getField(gCampoClave);
      if (clave!=null) {
        if (action=="SAVE") {
          if (strClave==null || strClave=="") {
            mensaje(31);
            winSelector.focus();
            return false;
          }
          
          clave.value = strClave;
          var text = getField(gCampoTexto);
          //if (text!=null) text.value = ReplaceText(strTexto, "\"", "\\\"");
          if (text!=null) text.value = strTexto;
          if (parametros!=null && parametros.length>0) {
            var total = parametros.length;
            for (var i=0;i<total;i++) {
              //var obj = eval("document." + gForm + "." + ((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
              var obj = getField(((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
              if (obj!=null && obj.type) obj.value=parametros[i].valor;
            }
          }
          if (clave.onchange) clave.onchange();
        } else if (action=="CLEAR") {
          strClave="";
          strTexto="";
          clave.value= "";
          var text = getField(gCampoTexto);
          text.value="";
          if (parametros!=null && parametros.length>0) {
            var total = parametros.length;
            for (var i=0;i<total;i++) {
              var obj = getField(((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
              if (obj!=null && obj.type) obj.value="";
            }
          }
          if (clave.onchange) clave.onchange();
        } else if (action=="SAVE_IMAGE") {
          if (strClave==null || strClave=="") {
            mensaje(31);
            winSelector.focus();
            return false;
          }
          
          clave.value=strClave;
          eval("document.images['" + gCampoTexto + "'].src=\"" + baseDirection + "images/" + strTexto + "\"");
          if (parametros!=null && parametros.length>0) {
            var total = parametros.length;
            for (var i=0;i<total;i++) {
              var obj = getField(((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
              if (obj!=null && obj.type) obj.value=parametros[i].valor;
            }
          }
          if (clave.onchange) clave.onchange();
        } else if (action=="CLEAR_IMAGE") {
          strClave="";
          strTexto="";
          clave.value="";
          var text = getField(gCampoTexto);
          text.src= baseDirection + "images/" + baseImage ;
          if (parametros!=null && parametros.length>0) {
            var total = parametros.length;
            for (var i=0;i<total;i++) {
              var obj = getField(((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
              if (obj!=null && obj.type) obj.value="";
            }
          }
          if (clave.onchange) clave.onchange();
        }
      }
    }
    closeWindowSearch();
    if (gDepurar) {
      if (!debugSearch(strClave, strTexto, gCampoClave, parametros)) {
        return false;
      }
    }
    window.focus();
    return true;
  }
}

function closeMultiSearch(action, data, parametros) {
  if (winSelector==null) return true;
  
  if (gForm!=null && gCampoClave!=null) {
    var clave = eval("document." + gForm + "." + gCampoClave);
    if (clave!=null) {
      if (action=="SAVE") {
        if (data==null || data.length==0) {
          mensaje(31);
          winSelector.focus();
          return false;
        }
        
        insertarElementosList(clave, data);
        if (parametros!=null && parametros.length>0) {
          var total = parametros.length;
          for (var i=0;i<total;i++) {
            var obj = eval("document." + gForm + "." + ((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
            if (obj!=null && obj.type) obj.value=parametros[i].valor;
          }
        }
      } else if (action=="CLEAR") {
        limpiarLista(clave);
        if (parametros!=null && parametros.length>0) {
          var total = parametros.length;
          for (var i=0;i<total;i++) {
            var obj = eval("document." + gForm + "." + ((parametros[i].esRef)?gCampoClave:"") + parametros[i].campo);
            if (obj!=null && obj.type) obj.value="";
          }
        }
      }
    }
  }

  closeWindowSearch();
  if (gDepurar) {
    if (!debugSearch(data, gCampoClave)) {
      return false;
    }
  }
  window.focus();
  return true;
}


function toLayer(strHtml, strLayer) {
  var ref = selGetRef(strLayer);
  if (strHtml==null)
    strHtml = "";

  if (document.layers)
  {
    ref.document.write(strHtml);
    ref.document.close();
  }
  else if (document.all)
  {
    ref.innerHTML = strHtml;
  }
  else if (document.getElementById) 
  {
    range=document.createRange();
    range.setStartBefore(ref);
    domfrag=range.createContextualFragment(strHtml);
    while (ref.hasChildNodes())
    {
      ref.removeChild(ref.lastChild);
    }
    ref.appendChild(domfrag);
  }
}

function activarEventos() {
if (document.layers) {
    document.captureEvents(Event.MOUSEDOWN);
    document.captureEvents(Event.UNLOAD);
  }
  window.onunload = function(){closeSearch();};
  document.onmousedown=function(){closeWindowSearch();};
}

function desactivarEventos() {
  if (document.layers) {
    document.releaseEvents(Event.MOUSEDOWN);
    window.releaseEvents(Event.UNLOAD);
  }
  document.onmousedown=function(){};
  window.onunload=function(){};
}

function infoSelectFilters(params) {
    setGridFilters(params);
    updateGridDataAfterFilter();
    dojo.widget.byId('grid').requestParams["newFilter"] = "0";
    return true;
}

function updateHeader(liveGrid, offset) {
      return true;
  }

  function onRowDblClick(cell) {
    var value = dojo.widget.byId('grid').getSelectedRows();
    if (value==null || value=="" || value.length>1) return false;    
    depurarSelector('SAVE');
  }

  function getSelectedValues() {
    var value = dojo.widget.byId('grid').getSelectedRows();
    if (value==null || value.length==0) return "";
    return value[0];
  }

  function getSelectdText() {
    var value = dojo.widget.byId('grid').getSelectedRows();
    if (value==null || value.length==0) return "";
    return value[0];
  }

  function getSelectedPos() {
    var value = dojo.widget.byId('grid').getSelectedRowsPos();
    if (value==null || value.length==0) return "";
    return value[0];
  }

  function isMultipleSelected() {
    var value = dojo.widget.byId('grid').getSelectedRows();
    if (value==null || value=="") return false;
    return (value.length>1);
  }

  function onGridLoadDo() {
    if (selectedRow==null) return true;
    if (selectedRow<=0) dojo.widget.byId('grid').goToFirstRow();
    else dojo.widget.byId('grid').goToRow(selectedRow);
    // Set off numRows calculation
    var params = new Array();
    params["newFilter"] = "0";
    dojo.widget.byId('grid').setRequestParams(params);
    return true;
  }

 function setGridFilters(newparams) {
   var params = [];
   params["newFilter"] = "1";
   if (newparams!=null && newparams.length>0) {
     var total = newparams.length;
     for (var i=0;i<total;i++) {
       params[newparams[i][0]] = newparams[i][1];
     }
   }
   dojo.widget.byId('grid').setRequestParams(params);
   return true;
 }

 function updateGridData() {
   dojo.widget.byId('grid').refreshGridData();
   return true;
 }

 function updateGridDataAfterFilter() {
   dojo.widget.byId('grid').refreshGridDataAfterFilter();
   return true;
 }
 
 function setFilters() {
  	var frm = document.forms[0];
  	var paramsData = new Array();
  	var count = 0;
    paramsData[count++] = new Array("clear","true");
  	var tags = frm.getElementsByTagName('INPUT');
  	for(var i=0; i < tags.length; i++) {
  		if(tags[i].name.toUpperCase() != "COMMAND" &&
  		   tags[i].name.toUpperCase() != "ISPOPUPCALL") {
  		   if(tags[i].type.toUpperCase() == "RADIO") {
  		   		if(tags[i].checked)
  		   			paramsData[count++] = new Array(tags[i].name, tags[i].value);
  		   }else if(tags[i].type.toUpperCase() == "CHECKBOX") {
            if(tags[i].checked) paramsData[count++] = new Array(tags[i].name, tags[i].value);
            else paramsData[count++] = new Array(tags[i].name, "N");
         }
  		   else
  		   		paramsData[count++] = new Array(tags[i].name, tags[i].value);
  		}
  	}
  	var selects = frm.getElementsByTagName('SELECT');
  	for(var i=0; i < selects.length; i++) {
  		paramsData[count++] = new Array(selects[i].name, selects[i].options[selects[i].selectedIndex].value);
  	}
  	infoSelectFilters(paramsData);
  }
  
function calculateNumRows() {
   resizeAreaInfo();
   document.getElementById("grid_sample").style.display = "block";
   var grid_header_height = document.getElementById("grid_sample_header").clientHeight + 1;
   var grid_row_height = document.getElementById("grid_sample_row").clientHeight + 1;
   var messagebox_cont = document.getElementById("messageBoxID");
   var related_info_cont = document.getElementById("related_info_cont");
   var client_height = document.getElementById("client_middle").clientHeight;
   client_height = client_height - grid_header_height - (related_info_cont?related_info_cont.clientHeight:0) - (messagebox_cont?messagebox_cont.clientHeight:0);
   client_height = client_height - 20;
   var numRows = (client_height)/(grid_row_height);
   numRows = parseInt(numRows);
   document.getElementById("grid_sample").style.display = "none";
   return numRows;
 }

