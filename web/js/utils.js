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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
/**
* @fileoverview This JavaScript library contains basic functionality used on all HTML pages
* Basic functions like positioning the focus control, numeric field validation, etc
*/

<!--
var baseFrameServlet = "http://localhost:8880/openbravo/security/Login_FS.html";
var gColorSelected = "#c0c0c0";
var gWhiteColor = "#F2EEEE";
var arrGeneralChange=new Array();
var dateFormat;
var defaultDateFormat = "%d-%m-%Y";

//Days of a Month
daysOfMonth = new Array( 
new Array(0,31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31), //No leap year
new Array (0,31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31) //Leap year
);

/**
* Esta librería JavaScript contiene las funciones y procedimientos básicos que se utilizan en
* todas las páginas html. Son elementos básicos como el posicionamiento del foco en un control
* de la página, control de campos numéricos...
*/

var gByDefaultAction;
var gEnviado=false;
var keyArray=null;
var gAUXILIAR=0;
var gWaitingCallOut=false;

var isKeyboardLocked=false;

var isPopupLoadingWindowLoaded=false;


/**
* Set the focus on the first visible control in the form
* @param {Form} form Optional- Defines the form containing the field, where we want to set the focus. If is not present, the first form of the page will be used.
* @param {String} field Optional - Name of the control where we want to set the focus. If is not present the first field will be used.
*/
function setFocusFirstControl(form, field) {
  var encontrado = false;
  if (form==null) form=document.forms[0];
  var total = form.length;
  for(var i=0;i<total; i++)
  {
    if ((form.elements[i].type != "hidden") && (form.elements[i].type != "button") && (form.elements[i].type != "submit") && (form.elements[i].type != "image") && (form.elements[i].type != "reset")) 
    { 
      if(field!=null) {
        if (field == form.elements[i].name && !form.elements[i].readonly && !form.elements[i].disabled) {
          form.elements[i].focus();
          encontrado=true;
          break;
        }
      } else if (!form.elements[i].readonly && !form.elements[i].disabled) {
        try {
          form.elements[i].focus();
          encontrado=true;
          break;
        } catch (ignore) {}
      }
    }
  }
  if (encontrado && form.elements[i].type && form.elements[i].type.indexOf("select")==-1)
    form.elements[i].select();
}

/** 
* Clean the content of all text fields in a form
* @param {Form} Formulario Optional - Form where the fields that we want to clean, are contained. If not exist, the first form in the page will be used.
*/
function limpiar(Formulario) {
  if (Formulario == null)
    Formulario = document.forms[0];

  var total = Formulario.length;
  for (var i=0;i<total;i++){
    if (Formulario.elements[i].type == "text" || Formulario.elements[i].type == "password")
      Formulario.elements[i].value = "";
  }
}

/**
* Display a message depending on the accion parameter. Is used by other functions to submit forms.
* @param {String} accion Text that identify the Command to execute
* @returns True in case of not having a message associated to the Command or by the user confirmation. False if the user cancel the confirmation message.
* @type Boolean
*/
function confirmar(accion) {
  switch (accion)
  {
  case 'DELETE': return mensaje(2);
  case 'DELETE_RELATION': return mensaje(2);
  case 'GUARDAR': return mensaje(3);
  default: return true;
  }
}

/**
* Submit the first form in the page with GET parameters method 
* @param {String} Command The command String to execute
* @param {String} action The URL to receive the form
* @returns Always return true
* @type Boolean
*/
function submitFormGetParams(Command, action) {
  var frm = document.forms[0];
  frm.action=action + "?Command=" + Command;
  var params="";
  for (var i=2;arguments[i]!=null;i++) {
    params += "&" + arguments[i] + ((arguments[i+1]!=null)?("=" + arguments[i+1]):"");
    i++;
  }
  if (params!="") frm.action += params;
  frm.target="_self";
  frm.submit();
  return true;
}

/** 
* Receive a form with filled fields and transform it into a String of GET paramaters
* @param {Form} Formulario Form that we want to transform.
* @returns The transformed string for GET method submition.
* @type String
*/
function getParamsScript(Formulario) {
  if (Formulario==null) return "";
  var script="";
  var total = Formulario.length;
  for (var i=0;i<total;i++) {
    if (Formulario.elements[i].type && (Formulario.elements[i].type != "button") && (Formulario.elements[i].type != "submit") && (Formulario.elements[i].type != "image") && (Formulario.elements[i].type != "reset") && (Formulario.elements[i].readonly!="true") && (Formulario.elements[i].name != "Command") && (Formulario.elements[i].name!="") && !Formulario.elements[i].disabled) {
      if (Formulario.elements[i].type.toUpperCase().indexOf("SELECT")!=-1 && Formulario.elements[i].selectedIndex!=-1) {
        script += ((script=="")?"":"&") + Formulario.elements[i].name + "=" + escape(Formulario.elements[i].options[Formulario.elements[i].selectedIndex].value);
      } else if (Formulario.elements[i].type.toUpperCase().indexOf("CHECKBOX")!=-1 || Formulario.elements[i].type.toUpperCase().indexOf("RADIO")!=-1) {
        if (radioValue(Formulario.elements[i]) != null) script += ((script=="")?"":"&") + Formulario.elements[i].name + "=" + escape(radioValue(Formulario.elements[i]));
      } else if (Formulario.elements[i].value!=null && Formulario.elements[i].value!="") {
        script += ((script=="")?"":"&") + Formulario.elements[i].name + "=" + escape(Formulario.elements[i].value);
      }
    }
  }
  return script;
}


/**
* Submit a form after setting a value to a field and control a single form submition
* @param {Object} camp Reference to the field in the form
* @param {String} valor Value to set in the field
* @param {Form} Formulario Reference to the form, to submit 
* @param {Boolean} bolComprobar To control if we want to validate only one form submition
* @param {Boolean} isCallout Verify if we will wait for a CallOut response
* @returns True if the form is sent correctly, false if an error occours and is not possible to send the data.
* @type Boolean
*/
function submitForm(campo, valor, Formulario, bolComprobar, isCallOut) {
  if (Formulario == null) Formulario = document.forms[0];
  if (isCallOut==null) isCallOut = false;
  if (bolComprobar!=null && bolComprobar) {
    if (gEnviado==1) {
      mensaje(16);
      return false;
    } else {
      gEnviado=1;
      if (isCallOut) setGWaitingCallOut(true);
      campo.value = valor;
      Formulario.submit();
    }
  } else {
    if (isCallOut) setGWaitingCallOut(true);
    campo.value = valor;
    Formulario.submit();
  }
  return true;
}

/**
* Delays a Command execution
* @param {String} text String that contains the JavaScript command
* @returns An identificator for the timer.
*/
function reloadFunction(text) {
  return setTimeout(text, 1000);
}

/**
* Identify the last field changed, for on screen debugging. This function requires the inpLastFieldChanged field.
* @param {Object} campo Reference to the modified field. 
* @param {Form} Formulario Form where the inpLastFieldChanged is located 
* @returns True if everything was correct. False if the inpLastFieldChanged was not found
* @type Boolean
*/
function setChangedField(campo, Formulario) {
  if (Formulario==null || !Formulario) Formulario = document.forms[0];
  if (Formulario.inpLastFieldChanged==null) return false;
  Formulario.inpLastFieldChanged.value = campo.name;
  return true;
}

/**
* Check for changes in a Form. This function requires the inpLastFieldChanged field. Is a complementary function to {@link #setChangedField}
* @param {Form} Formulario Reference to a form where the inpLastFieldChanged is located.
* @returns True if the inpLastFieldChanged has data and the user confirm the pop-up message. False if the field has no data or the user no confirm the pop-up message.
* @type Boolean
*/
function checkForChanges(Formulario) {
  if (Formulario==null) Formulario = document.forms[0];
  if (inputValue(Formulario.inpLastFieldChanged)!="") {
    if (!mensaje(10)) return false;
  }
  return true;
}


/**
* Function Description
* @param {Form} Formulario
* @param {String} columName
* @param {String} parentKey
* @param {String} url
* @param {String} keyId
* @param {String} tableId
* @param {String} newTarget
* @param {Boolean} bolComprobarCambios
* @returns
* @type Boolean
*/
function sendDirectLink(Formulario, columnName, parentKey, url, keyId, tableId, newTarget, bolComprobarCambios) {
  if (Formulario == null) Formulario = document.forms[0];
  var frmDepuracion = document.forms[0];
  var accion = "DEFAULT";
  if (bolComprobarCambios==null) bolComprobarCambios = false;
  if (arrGeneralChange!=null && arrGeneralChange.length>0 && bolComprobarCambios) {
    var strFunction = "sendDirectLink('" + Formulario.name + "', '" + columnName + "', '" + parentKey + "', '" + url + "', '" + keyId + "', '" + tableId + "', " + ((newTarget==null)?"null":"'" + newTarget + "'") + ", " + bolComprobarCambios + ")";
    reloadFunction(strFunction);
    return false;
  }
  if (bolComprobarCambios && !checkForChanges(frmDepuracion)) return false;
  if (confirmar(accion)) {
    Formulario.action = url;
    if (newTarget != null) Formulario.target = newTarget;
    Formulario.inpKeyReferenceColumnName.value = columnName;
    Formulario.inpSecondKey.value = parentKey;
    Formulario.inpKeyReferenceId.value = keyId;
    Formulario.inpTableReferenceId.value = tableId;
    submitForm(Formulario.Command, accion, Formulario, false, false);
  }
  return true;
}

/**
* Fires the onChange event on a specified field
* @param {Object} target Reference to the evaluated field.
* @returns True
* @type Boolean
*/
function dispatchEventChange(target) {
  if (!target) return true;
  if (!target.type) return true;
  if (target.onchange && target.defaultValue && target.defaultValue != inputValue(target)) target.onchange();
  else if (target.onblur) target.onblur();
  return true;
}

/**
* Submit a form after setting a value to the Command field. The Command field is a string to define the type of operation that the servlet will execute. Also allows to debug previous the submition. This function execution requires a hidden field with name Command in the form.
* @param {String} accion Identify the operation that the servlet will execute.
* @param {Boolean} bolDepurar Set if you want to debug previous the form submition. The default value is false. If is true, you must implement a boolean returning function named depurar that makes all the debugging functionality. If depurar returns false the form will not be submited.
* @param {Form} Formulario A reference to the form that will be submited. If is null, the first form in the page will be used.
* @param {String} newAction Set the URL where we want to send the form. If is null the URL in the form's action attribute will be used.
* @param {String} newTarget Set the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} bolComprobar Verify the form submition, waits for a server response. Prevents a multiple submition. The default value false.
* @param {Boolean} bolComprobarCambios  If we want to check for changes in the window, and presents a pop-up message.
* @param {Boolean} isCallOut Defines if we are making a submition to a CallOut.
* @param {Boolean} controlEvt
* @param {Event} evt
* @returns True if everything goes correct and the data is sent. False on any problem, is no able to send the data or by the user cancelation un the pop-up message.
* @type Boolean
*/
function submitCommandForm(accion, bolDepurar, Formulario, newAction, newTarget, bolComprobar, bolComprobarCambios, isCallOut, controlEvt, evt) {
  if (Formulario == null) Formulario = document.forms[0];
  if (bolDepurar!=null && bolDepurar==true) if (!depurar(accion, Formulario, "")) return false;
  if (bolComprobarCambios==null) bolComprobarCambios = false;
  if (isCallOut==null) isCallOut = false;
  if (controlEvt==null) controlEvt = false;
  if (controlEvt) {
    if (!evt) evt = window.event;
    var target = (document.layers) ? evt.target : evt.srcElement;
    dispatchEventChange(target);
  }
  if (gWaitingCallOut || (arrGeneralChange!=null && arrGeneralChange.length>0 && bolComprobarCambios)) {
    var strFunction = "submitCommandForm('" + accion + "', " + bolDepurar + ", " + Formulario.name + ", " + ((newAction!=null)?("'" + newAction + "'"):"null") + ", " + ((newTarget!=null)?("'" + newTarget + "'"):"null") + ", " + bolComprobar + ", " + bolComprobarCambios + ")";
    reloadFunction(strFunction);
    return false;
  }
  if (bolComprobarCambios && !checkForChanges(Formulario)) return false;
  if (confirmar(accion)) {
    if (newAction != null) Formulario.action = newAction;
    if (newTarget != null) Formulario.target = newTarget;
    submitForm(Formulario.Command, accion, Formulario, bolComprobar, isCallOut);
  }
  return true;
}


/**
* Submit a form after setting a value to the Command field, and adding an additional parameter/value to the form. This function requires a hidden Command field in the form.
* @param {String} accion Identify the operation to be executed by the servlet. 
* @param {Object} campo Reference to the field where we want to set the value.
* @param {String} valor Value to set at the selected field.
* @param {Boolean} bolDepurar Set if you want to debug previous the form submition. The default value is false. If is true, you must implement a boolean returning function named depurar that makes all the debugging functionality. If depurar returns false the form will not be submited.
* @param {Form} Formulario A reference to the form that will be submited. If is null, the first form in the page will be used.
* @param {String} formAction Set the URL where we want to send the form. If is null the URL in the form's action attribute will be used.
* @param {String} newTarget Set the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} bolComprobar Verify the form submition, waits for a server response. Prevents a multiple submition. The default value false.
* @param {Boolean} bolComprobarCambios If we want to check for changes in the window, and presents a pop-up message.
* @param {Boolean} isCallOut Defines if we are sending the data to a CallOut 
* @param {Boolean} controlEvt Set if the function should control the events.
* @param {Event} evt Event handling object
* @returns True if everything works correctly. False on any problem or by the user cancelation at the pop-up message.
* @type Boolean
*/
function submitCommandFormParameter(accion, campo, valor, bolDepurar, Formulario, formAction, newTarget, bolComprobar, bolComprobarCambios, isCallOut, controlEvt, evt) {
  if (Formulario == null) Formulario = document.forms[0];
  if (bolDepurar!=null && bolDepurar==true) if (!depurar(accion, Formulario, valor)) return false;
  if (bolComprobarCambios==null) bolComprobarCambios = false;
  if (isCallOut==null) isCallOut = false;
  if (controlEvt==null) controlEvt = false;
  if (controlEvt) {
    if (!evt) evt = window.event;
    var target = (document.layers) ? evt.target : evt.srcElement;
    dispatchEventChange(target);
  }
  if (gWaitingCallOut || (arrGeneralChange!=null && arrGeneralChange.length>0 && bolComprobarCambios)) {
    var strFunction = "submitCommandFormParameter('" + accion + "', " + campo.form.name + "." + campo.name + ", '" + valor + "', " + bolDepurar + ", " + Formulario.name + ", " + ((formAction!=null)?("'" + formAction + "'"):"null") + ", " + ((newTarget!=null)?("'" + newTarget + "'"):"null") + ", " + bolComprobar + ", " + bolComprobarCambios + ", " + isCallOut + ")";
    reloadFunction(strFunction);
    return false;
  }

  if (bolComprobarCambios && !checkForChanges(Formulario)) return false;

  if (confirmar(accion)) {
    campo.value = valor;
    if (formAction != null) Formulario.action = formAction;
    if (newTarget != null) Formulario.target = newTarget;
    submitForm(Formulario.Command, accion, Formulario, bolComprobar, isCallOut);
  }
  return true;
}


/**
* Verify if a text is an allowed number.
* @param {String} strValorNumerico Text to evaluate.
* @param {Boolean} bolDecimales Set if a float number is allowed
* @param {Boolean} bolNegativo Set if a negative number is allowed
* @returns True if the text is a allowed number, false if not is a number or not an allowed number.
* @type Boolean
*/
function esNumero(strValorNumerico, bolDecimales, bolNegativo) {
  var bolComa = false;
  var esNegativo = false;
  var i=0;
  if (strValorNumerico == null || strValorNumerico=="") return true;
  if (strValorNumerico.substring(i, i+1)=="-") {
    if (bolNegativo !=null && bolNegativo) {
      esNegativo = true;
      i++;
    } else {
      return false;
    }
  } else if (strValorNumerico.substring(i, i+1)=="+")
    i++;
  var total = strValorNumerico.length;
  for (i=i;i<total;i++) {
    if (isNaN(strValorNumerico.substring(i,i+1))) {
      if (bolDecimales && strValorNumerico.substring(i,i+1)=="." && !bolComa) 
        bolComa = true;
      else
        return false;
    }
  }
  return true;
}

/**
* Validate that the information entered in a field is a number, if not, this function displays an error message and set the focus on the field. Also you can control if the number is an Integer, positive or negative number.
* @param {Object} CampoNumerico A reference to a field that will be evaluated.
* @param {Boolean} bolDecimales Set if a float number is allowed.
* @param {Boolean} bolNegativo Set if a negative number is allowed.
* @returns True if the field's content is a number, false if the field's content is not a number or does not accomplish the requirements
* @type Boolean
* @see #esNumero
*/
function campoNumerico(CampoNumerico, bolDecimales, bolNegativo) {
  if (!esNumero(CampoNumerico.value, bolDecimales, bolNegativo))
  {
    mensaje(4);
    CampoNumerico.focus();
    CampoNumerico.select();
    return false;
  }
  return true;
}

/**
* Search in the array, and return the value from a specified index.
* @param {Array} data Array into search for
* @param {String} name The index to look for
* @param {String} defaultValue The default value if the index is not found
* @returns The value of the array if the index name was found, otherwise returns the defaultValue. If the defaultValue is null returns an empty String.
* @type String
*/
function getArrayValue(data, name, defaultValue) {
  if (data==null || data.length<=0) return ((defaultValue!=null)?defaultValue:"");
  var total = data.length;
  for (var i=0;i<total;i++) {
    if (data[i][0]==name) return data[i][1];
  }
  return ((defaultValue!=null)?defaultValue:"");
}

/**
* Add a value to Array
* @param {Array} data Array where the value will be added
* @param {String} name Index of the new value
* @param {String} value Value to add
* @param {Boolean} isUrlParameter Set if is a URL Parameter
* @returns An array with the added value.
* @type Array
*/
function addArrayValue(data, name, value, isUrlParameter) {
  if (isUrlParameter==null) isUrlParameter=false;
  if (data==null || data.length<=0) {
    data = new Array();
    data[0] = new Array(name, value, (isUrlParameter?"true":"false"));
    return data;
  }
  var total = data.length;
  for (var i=0;i<total;i++) {
    if (data[i][0]==name) {
      data[i][1] = value;
      return data;
    }
  }
  data[total] = new Array(name, value, (isUrlParameter?"true":"false"));
  return data;
}

/**
* Extract the parameters from the array 
* @param {Array} data Array to extract all the parameters
* @returns A String in the form variable1=value1[&variablen=valuen]
* @type String
*/
function addUrlParameters(data) {
  if (data==null || data.length<=0) return "";
  var total = data.length;
  var text = "";
  for (var i=0;i<total;i++) {
    if (data[i][2]=="true") text += ((text!=null && text!="")?"&":"") + data[i][0] + "=" + escape(data[i][1]);
  }
  if (text!=null && text!="") text = "?" + text;
  return text;
}

/**
* Opens a pop-up window and adds custom properties to it 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @param {String} target Specifies the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} doSubmit Specifies whether or not should submit the form. If is true this function calls {@link #submitCommandForm} function
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Array} parameters Array list of the available parameters for the new window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #addUrlParameters 
* @see #getArrayValue 
* @see #submitCommandForm
*/
function openPopUp(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters, hasLoading) {
  var adds = "";
  var isPopup = null;
  if (_name!='frameAplicacion' && _name!='frameMenu') isPopup =  true;
  else isPopup = false;
  if (height==null) height = screen.height - 50;
  if (width==null) width = screen.width;
  if (top==null) top = (screen.height - height) / 2;
  if (left==null) left = (screen.width - width) / 2;
  if (checkChanges==null) checkChanges = false;
  if (closeControl==null) closeControl = false;
  if (doSubmit==null) doSubmit = false;
  if (checkChanges && !checkForChanges()) return false;
  if (url!=null && url!="") url += addUrlParameters(parameters);
  if (target!=null && target!="" && target.indexOf("_")!=0) {
    var objFrame = eval("parent." + target);
    objFrame.location.href=url;
    return true;
  }
  if (hasLoading==null) hasLoading = true;
  adds = "height=" + height + ", width=" + width + ", left=" + left + ", top=" + top;
  if (navigator.appName.indexOf("Netscape")) {
    adds += ", alwaysRaised=" + getArrayValue(parameters, "alwaysRaised", "1");
    adds += ", dependent=" + getArrayValue(parameters, "dependent", "1");
    adds += ", directories=" + getArrayValue(parameters, "directories", "0");
    adds += ", hotkeys=" + getArrayValue(parameters, "hotkeys", "0");
  }
  adds += ", location=" + getArrayValue(parameters, "location", "0");
  adds += ", scrollbars=" + getArrayValue(parameters, "scrollbars", "0");
  adds += ", status=" + getArrayValue(parameters, "status", "1");
  adds += ", menubar=" + getArrayValue(parameters, "menubar", "0");
  adds += ", toolbar=" + getArrayValue(parameters, "toolbar", "0");
  adds += ", resizable=" + getArrayValue(parameters, "resizable", "1");
  if (doSubmit && (getArrayValue(parameters, "debug", false)==true)) {
    if (!depurar(getArrayValue(parameters, "Command", "DEFAULT"), null, "")) return false;
  }
  if (isPopup == true && hasLoading == true) {
    isPopupLoadingWindowLoaded=false;
    var urlLoading = '../utility/PopupLoading.html'
    var winPopUp = window.open((doSubmit?urlLoading:url), _name, adds);
  } else {
    var winPopUp = window.open((doSubmit?"":url), _name, adds);
  }

  if (closeControl) window.onunload = function(){winPopUp.close();}
  if (doSubmit) {
    if (isPopup==true && hasLoading == true) synchronizedSubmitCommandForm(getArrayValue(parameters, "Command", "DEFAULT"), (getArrayValue(parameters, "debug", false)==true), null, url, _name, target, checkChanges);
    else submitCommandForm(getArrayValue(parameters, "Command", "DEFAULT"), (getArrayValue(parameters, "debug", false)==true), null, url, _name, target, checkChanges);
  }
  winPopUp.focus();
  return winPopUp;
}

function synchronizedSubmitCommandForm(accion, bolDepurar, Formulario, newAction, newTarget, bolComprobar, bolComprobarCambios, isCallOut, controlEvt, evt) {
  if (isPopupLoadingWindowLoaded==false) {
    setTimeout(function() {synchronizedSubmitCommandForm(accion, bolDepurar, Formulario, newAction, newTarget, bolComprobar, bolComprobarCambios, isCallOut, controlEvt, evt);},50);
    return;
  } else {
    submitCommandForm(accion, bolDepurar, Formulario, newAction, newTarget, bolComprobar, bolComprobarCambios, isCallOut, controlEvt, evt);
  }
}

function setPopupLoadingWindowLoaded(value) {
  if (value == '' || value == 'null' || value == null) value = true;
  isPopupLoadingWindowLoaded = value;
}

/**
* Opens a pop-up window and adds custom properties to it 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @param {String} target Specifies the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} doSubmit Specifies whether or not should submit the form. If is true this function calls {@link #submitCommandForm} function
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Array} parameters Array list of the available parameters for the new window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function openNewLink(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters) {
  parameters = addArrayValue(parameters, "location", "1");
  parameters = addArrayValue(parameters, "scrollbars", "1");
  parameters = addArrayValue(parameters, "status", "1");
  parameters = addArrayValue(parameters, "menubar", "1");
  parameters = addArrayValue(parameters, "toolbar", "1");
  parameters = addArrayValue(parameters, "resizable", "1");
  return openPopUp(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openNewLink
*/
function abrirNuevoBrowser(url, _name, height, width, top, left) {
  return openNewLink(url, _name, height, width, top, left, null, null, null, true, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function abrirExcel(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, null, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function abrirPDF(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, null, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function abrirPDFFiltered(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, true, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels. If is null, a fixed height of 250 pixels is used.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels. If is null, a fixed width of 230 pixels is used.
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Boolean} showstatus
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function abrirPopUp(url, _name, height, width, closeControl, showstatus) {
  if (height==null) height = 250;
  if (width==null) width = 230;
  return openPopUp(url, _name, height, width, null, null, null, null, null, closeControl, null);
}

/**
* Opens a PDF session
* @param {String} strPagina
* @param {String} strDirectPrinting
* @param {String} strHiddenKey
* @param {String} strHiddenValue
* @param {Boolean} bolComprobarCambios
* @returns
* @type Boolean
* @see #submitCommandForm
*/
function abrirPDFSession(strPagina, strDirectPrinting, strHiddenKey, strHiddenValue, bolComprobarCambios) {
  var direct = (strDirectPrinting!="")?"Y":"N";
  return submitCommandForm("DEFAULT", false, null, "../businessUtility/PrinterReports.html?inppdfpath=" + escape(strPagina) + "&inpdirectprint=" + escape(direct) + "&inphiddenkey=" + escape(strHiddenKey) + ((strHiddenValue!=null)?"&inphiddenvalue=" + escape(strHiddenValue):""), "frameOculto", null, bolComprobarCambios);
}

/**
* Opens a pop-up window after setting the necessary parameters
* @param {String} url
* @param {String} _name
* @param {String} tabId
* @param {String} windowName
* @param {String} windowId
* @param {String} checkChanges
* @returns An ID reference pointing to the newly opened browser window.
* @type Object 
* @see #addArrayValue 
* @see #openPopUp 
*/
function abrirBusqueda(url, _name, tabId, windowName, windowId, checkChanges) {
  var parameters = new Array();
  parameters = addArrayValue(parameters, "inpTabId", tabId, true);
  parameters = addArrayValue(parameters, "inpWindow", windowName, true);
  parameters = addArrayValue(parameters, "inpWindowId", windowId, true);
  return openPopUp(url, _name, 450, 600, null, null, checkChanges, null, null, true, parameters);
}

/**
* Function Description
* @param {String} windowId
* @param {String} url
* @param {String} _name
* @param {Boolean} checkChanges
* @param {Number} height
* @param {Number} width
* @param {String} windowType
* @param {String} windowName
* @returns A reference pointing to the newly opened window
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function openHelp(windowId, url, _name, checkChanges, height, width, windowType, windowName) {
  if (height==null) height = 450;
  if (width==null) width = 700;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "inpwindowId", windowId, true);
  parameters = addArrayValue(parameters, "inpwindowType", windowType, true);
  parameters = addArrayValue(parameters, "inpwindowName", windowName, true);
  return openPopUp(url, _name, height, width, null, null, checkChanges, null, null, true, parameters);
}

/**
* Function Description
* @param {String} Command
* @param {Boolean} depurar
* @param {String} url
* @param {String} _name
* @returns An ID reference pointing to the newly opened browser window.
* @type Object 
* @see #openPopUp 
* @see #addArrayValue
*/
function openServletNewWindow(Command, depurar, url, _name, processId, checkChanges, height, width, resizable, hasStatus, closeControl, hasLoading) {
  if (height==null) height = 350;
  if (width==null) width = 500;
  if (closeControl==null) closeControl = true;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "scrollbars", "1");
  parameters = addArrayValue(parameters, "debug", depurar, false);
  if (processId!=null && processId!="") parameters = addArrayValue(parameters, "inpProcessId", processId, true);
  if (Command!=null && Command!="") parameters = addArrayValue(parameters, "Command", Command, false);

  if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
    setTimeout(function() {return openPopUp(url, _name, height, width, null, null, checkChanges, null, true, closeControl, parameters, hasLoading);},10);
  } else {
    return openPopUp(url, _name, height, width, null, null, checkChanges, null, true, closeControl, parameters, hasLoading);
  }
}


/**
* Opens a pop-up window with default parameter values 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openNewLink
*/
function openLink(url, _name, height, width) {
  return openNewLink(url, ((_name.indexOf("_")==0)?"":_name), height, width, null, null, null, ((_name.indexOf("_")==0)?_name:""), false, false, null);
}

/**
* Opens a pop-up window 
* @param {String} url
* @param {String} tipo
* @param {String} id
* @param {String} value
* @param {Number} height
* @param {Number} width
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function editHelp(url, tipo, id, value, height, width) {
  if (height==null) height = 500;
  if (width==null) width = 600;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "Command", tipo, true);
  parameters = addArrayValue(parameters, "inpClave", value, true);
  return openPopUp(url, "HELP_EDIT", height, width, null, null, null, null, false, true, parameters);
}

/**
* Handles window events. This function handles events such as KeyDown; when a user hit the ENTER key to do somethig by default.
* @param {Number} keyCode ASCII code of the key pressed.
* @returns True if the key pressed is not ment to be handled. False if is a handled key. 
* @type Boolean
*/
function keyPress(keyCode) {
  if (gByDefaultAction!=null)
  {
    var tecla = (!keyCode) ? window.event.keyCode : keyCode.which;
    if (tecla == 13)
    {
      eval(gByDefaultAction);
      return false;
    }
  }
  return true;
}


/**
* Defines a defult action on each page, the one that will be executed when the user hit the ENTER key. This function is shared in pages containing frames.
* @param {String} action Default command to be executed when the user hit the ENTER key.
* @returns Always retrun true.
* @type Boolean
* @see #keyPress
*/
function byDefaultAction(action) {
  gByDefaultAction = action;
  if (!document.all)
  {
    document.captureEvents(Event.KEYDOWN);
  }
  document.onkeydown=keyPress;
  return true;
}


/**
* Stops the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
function stopKeyPressEvent(evt) {
  try {
    if (evt.ctrlKey) {
      evt.cancelBubble = true;
      evt.returnValue = false;
        if (evt.stopPropagation) {
          evt.preventDefault();
        }
    } else if (evt.altKey) {
      evt.cancelBubble = true;
      evt.returnValue = false;
        if (evt.stopPropagation) {
          evt.preventDefault();
        }
    }
  } catch(e) {}
}


/**
* Enables the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
function startKeyPressEvent(evt) {
  return true;
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
* Returns the ASCII code of the given key
* @param {String} code Text version of a key
* @returns The ASCII code of the key
* @type Number
*/
function obtainKeyCode(code) {
  if (code==null) return 0;
  else if (code.length==1) return code.toUpperCase().charCodeAt(0);
  switch (code.toUpperCase()) {
    case "BACKSPACE": return 8;
    case "TAB": return 9;
    case "ENTER": return 13;
    case "SPACE": return 32;
    case "DELETE": return 46;
    case "INSERT": return 45;
    case "END": return 35;
    case "HOME": return 36;
    case "REPAGE": return 33;
    case "AVPAGE": return 34;
    case "LEFTARROW": return 37;
    case "RIGHTARROW": return 39;
    case "UPARROW": return 38;
    case "DOWNARROW": return 40;
    case "NEGATIVE": return 189;
    case "NUMBERNEGATIVE": return 109;
    case "DECIMAL": return 190;
    case "NUMBERDECIMAL": return 110;
    case "ESCAPE": return 27;
    case "F1": return 112;
    case "F2": return 113;
    case "F3": return 114;
    case "F4": return 115;
    case "F5": return 116;
    case "F6": return 117;
    case "F7": return 118;
    case "F8": return 119;
    case "F9": return 120;
    case "F10": return 121;
    case "F11": return 122;
    case "F12": return 123;
    case "P": return 80;
/*    case "shiftKey": return 16;
    case "ctrlKey": return 17;
    case "altKey": return 18;*/
    default: return 0;
  }
}


/**
* Handles the events execution of keys pressed, based on the events registered in the keyArray global array.
* @param {Event} pushedKey Code of the key pressed.
* @returns True if the key is not registered in the array, false if a event for this key is registered in keyArray array.
* @type Boolean
* @see #obtenerCodigoTecla
*/
function keyControl(pushedKey) {
  try {
    if (keyArray==null || keyArray.length==0) return true;
  } catch (e) {
    return true;
  }
  if (!pushedKey) pushedKey = window.event;
 // alert(pushedKey.type);
  var keyCode = pushedKey.keyCode ? pushedKey.keyCode : pushedKey.which ? pushedKey.which : pushedKey.charCode;
  if (isKeyboardLocked==false) {
    var keyTarget = pushedKey.target ? pushedKey.target: pushedKey.srcElement;
    var total = keyArray.length;
    for (var i=0;i<total;i++) {
      if (keyArray[i] != null && keyArray[i] && keyArray[i].eventShotter != 'onkeyup' && pushedKey.type=='keydown') {
        if (keyCode == obtainKeyCode(keyArray[i].key)) {
          if (keyArray[i].auxKey == null || keyArray[i].auxKey == "" || keyArray[i].auxKey == "null") {
            if (!pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              if (!keyArray[i].propagateKey) {
                if (window.event && window.event.keyCode == 116) { //F5 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 121) { //F10 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 27) { //ESC Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
              }
              if (!keyArray[i].propagateKey) 
                document.onkeypress = stopKeyPressEvent;
              if (keyArray[i].field==null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
                var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
                try {
                  eval(evalfuncTrl);
                  if (!keyArray[i].propagateKey) 
                    return false; else 
                    return true;
                } catch (e) {
                  document.onkeypress = startKeyPressEvent;
                  return true;
                }
                document.onkeypress = startKeyPressEvent;
                return true;
              }
            }
          } else if (keyArray[i].field == null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
            var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
            if (!keyArray[i].propagateKey) document.onkeypress = stopKeyPressEvent;
            if (keyArray[i].auxKey == "ctrlKey" && pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "altKey" && !pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "shiftKey" && !pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "ctrlKey+shiftKey" && pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            }
          }
        }
      } else if (keyArray[i] != null && keyArray[i] && keyArray[i].eventShotter == 'onkeyup'  && pushedKey.type=='keyup') {
        if (keyCode == obtainKeyCode(keyArray[i].key)) {
          if (keyArray[i].auxKey == null || keyArray[i].auxKey == "" || keyArray[i].auxKey == "null") {
            if (!pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              if (!keyArray[i].propagateKey) {
                if (window.event && window.event.keyCode == 116) { //F5 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 121) { //F10 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 27) { //ESC Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
              }
              if (!keyArray[i].propagateKey) 
                document.onkeypress = stopKeyPressEvent;
              if (keyArray[i].field==null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
                var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
                try {
                  eval(evalfuncTrl);
                  if (!keyArray[i].propagateKey) 
                    return false; else 
                    return true;
                } catch (e) {
                  document.onkeypress = startKeyPressEvent;
                  return true;
                }
                document.onkeypress = startKeyPressEvent;
                return true;
              }
            }
          } else if (keyArray[i].field == null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
            var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
            if (!keyArray[i].propagateKey) document.onkeypress = stopKeyPressEvent;
            if (keyArray[i].auxKey == "ctrlKey" && pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "altKey" && !pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "shiftKey" && !pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            } else if (keyArray[i].auxKey == "ctrlKey+shiftKey" && pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) {
              try {
                eval(evalfuncTrl);
                document.onkeypress = startKeyPressEvent;
                if (!keyArray[i].propagateKey) 
                  return false; else 
                  return true;
              } catch (e) {
                document.onkeypress = startKeyPressEvent;
                return true;
              }
              document.onkeypress = startKeyPressEvent;
              return true;
            }
          }
        }
      }
    }
  } else {
    return false;
  }
  return true;
}

/**
* Put the focus on the Menu frame
*/
function putFocusOnMenu(){
  parent.frameMenu.focus();
  return true;
}

/**
* Put the focus on the Window frame
*/
function putFocusOnWindow(){
  parent.frameAplicacion.selectedArea = 'window'
  parent.frameAplicacion.focus();
  parent.frameAplicacion.setWindowElementFocus(parent.frameAplicacion.focusedWindowElement);
  return true;
  //parent.frameAplicacion.focus();
}

/**
* Used to activate the key-press handling. Must be called after set the keys global array <em>keyArray</em>.
*/
function enableShortcuts(type) {
  if (type!=null && type!='null' && type!='') {
    try {
      this.keyArray = new Array();
      if (type=='menu') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('menuSpecificKeys');
      } else if (type=='edition') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('editionSpecificKeys');
        enableDefaultAction();
      } else if (type=='relation') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('relationSpecificKeys');
        getShortcuts('gridKeys');
      } else if (type=='popup') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('editionSpecificKeys');
        getShortcuts('popupSpecificKeys');
        getShortcuts('gridKeys');
        enableDefaultAction();
      }
    } catch (e) {
    }
  }
  document.onkeydown=keyControl;
  document.onkeyup=keyControl;  
}

/**
* Locks the Keyboard
*/
function lockKeyboard(){
  isKeyboardLocked=true;
  return true;
}

/**
* Unlocks the Keyboard
*/
function unlockKeyboard(){
  isKeyboardLocked=false;
  return true;
}

/**
* Validates the name of a Field
* @param {String} nombreArray Name of the field to verify
* @param {String} nombreActual Name of the field to verify
* @returns True, False
* @type Boolean
*/
function isIdenticalField(nombreArray, nombreActual) {
  if (nombreArray.substring(nombreArray.length-1)=="%") return (nombreActual.indexOf(nombreArray.substring(0, nombreArray.length-1))==0);
  else return (nombreArray == nombreActual);
}

/**
* Function Description
* @param {String} eventoJS
* @param {String} inputname
* @param {String} arrayName
* @returns
* @type String
* @see #ReplaceText
*/
function replaceEventString(eventoJS, inputname, arrayName) {
  eventoJS = ReplaceText(eventoJS, "@inputname@", inputname);
  if (arrayName!=null && arrayName!="" && arrayName.substring(arrayName.length-1)=="%") {
    var endname = inputname.substring(arrayName.length-1);
    eventoJS = ReplaceText(eventoJS, "@endinputname@", endname);
  }
  return eventoJS;
}


/**
* Search an array for a given parent key, and fills a combo with the founded values.
* @param {Object} combo A reference to the combo that will be filled.
* @param {Array} arrayDatos A data array that contents the combo info. Must be in the following format:
*               <ol>
*                 <li>Parent key or grouping element</li>
*                 <li>Element key</li>
*                 <li>Element's string. That will be presented on the screen</li>
*                 <li>Boolean flag to indicate if the element is a selected item</li>
*               </ol>
* @param {String} padre Parent key for search common elements.
* @param {Boolean} bolSelected Sets if the array's last field will be used for select an item. If this parameters is null or false, the last field on the array is no mandatory. 
* @param {Boolean} sinBlanco Set if a blank element should be added to the combo. The default value is false.
* @returns True if everything was right, otherwise false.
* @Type Boolean
*/
function rellenarComboHijo(combo, arrayDatos, padre, bolSelected, sinBlanco) {
  var i, value="";
  for (i = combo.options.length;i>=0;i--)
    combo.options[i] = null;
  i=0;
  if (sinBlanco==null || !sinBlanco)
    combo.options[i++] = new Option("", "");
  if (arrayDatos==null) return false;

  var total = arrayDatos.length;
  for (var j=0;j<total;j++) {
    if (arrayDatos[j][0]==padre) {
      combo.options[i] = new Option(arrayDatos[j][2], arrayDatos[j][1]);
      if (bolSelected!=null && bolSelected && arrayDatos[j][3]=="true") {
        value = arrayDatos[j][1];
        combo.options[i].selected = true;
      }
      else combo.options[i].selected = false;
      i++;
    }
  }
  return value;
}

/**
* Allows to set a text in a SPAN, DIV object. Used to dinamically change the text in a section of a HTML page.
* @param {Object} nodo A reference to a SPAN or DIV object. Or an ID of an object existing in the page.
* @param {String} strTexto The text that we want to change or set.
* @param {Boolean} esId Set if the first parameter is an ID. True for an ID and false for an Object reference.
* @param {Boolean} isAppend Set if we want to append the text to the existing one.
*/
function layer(nodo, strTexto, esId, isAppend) {
  if (strTexto==null)
    strTexto = "";
  if (isAppend==null) isAppend=false;

  if (document.layers)
  {
    if (esId!=null && esId)
      nodo = document.layers[nodo];
    if (nodo==null) return;
    nodo.document.write(strTexto);
    nodo.document.close();
  }
  else if (document.all)
  {
    if (esId!=null && esId)
      nodo = document.all[nodo];
    if (nodo==null) return;
    //nodo.innerHTML = '';
    try {
      if (isAppend) {
        strTexto = ((nodo.innerHTML==null)?"":nodo.innerHTML) + strTexto;
        isAppend = false;
      }
      nodo.innerHTML = strTexto;
    } catch (e) {
      if (isAppend) {
        strTexto = ((nodo.outterHTML==null)?"":nodo.outterHTML) + strTexto;
        isAppend = false;
      }
      nodo.outterHTML = strTexto;
    }
    nodo=null;
  }
  else if (document.getElementById) 
  {
    if (esId!=null && esId)
      nodo = document.getElementById(nodo);
    if (nodo==null) return;
    var range = document.createRange();
    range.setStartBefore(nodo);
    var domfrag = range.createContextualFragment(strTexto);
    while (nodo.hasChildNodes())
    {
      nodo.removeChild(nodo.lastChild);
    }
    nodo.appendChild(domfrag);
    nodo=null;
  }
}

/**
* Gets the inner HTML structure of an Layer
* @param {Object} nodo The Id or reference to the layer.
* @param {Boolean} esId Set if the first parameter is an ID or a reference to an object.
* @returns A inner HTML structure of a given ID
* @type String
* @see #getChildText
*/
function readLayer(nodo, esId) {
  if (document.layers) {
    if (esId!=null && esId) nodo = document.layers[nodo];
    if (nodo==null) return "";
    return getChildText(nodo);
  } else if (document.all) {
    if (esId!=null && esId) nodo = document.all[nodo];
    if (nodo==null) return "";
    try {
      return nodo.innerHTML;
    } catch (e) {
      return nodo.outterHTML;
    }
  } else if (document.getElementById) {
    if (esId!=null && esId) nodo = document.getElementById(nodo);
    if (nodo==null) return "";
    return getChildText(nodo);
  }
  return "";
}

/**
* Gets the data of an HTML node. Used for getting the text of a layer, div or span.
* @param {Object} nodo
* @returns The data of a node.
* @type String
*/
function getChildText(nodo) {
  if (nodo==null) return "";
  if (nodo.data) return nodo.data;
  else return getChildText(nodo.firstChild);
}

/**
* Returns the object child of a HTML object
* @param {obj} object
* @returns the object if exist. Else it returns false
* @type Object
*/
function getObjChild(obj) {
  try {
    obj = obj.firstChild;
    for (;;) {
      if (obj.nodeType != '1') {
        obj = obj.nextSibling;
      } else {
        break;
      }
    }
    return obj;
  } catch(e) {
    return false;
  }
}

/**
* Fills a combo with a data from an Array. Allows to set a default selected item, defined as boolean field in the Array.
* @param {Object} combo A reference to the combo object.
* @param {Array} arrayDatos Array containing the data for the combo. The structure of the array must be value, text, selected. Value is value of the item, text the string that will show the combo, an selected a boolean value to set if the item should appear selected.
* @param {Boolean} bolSelected Sets if the an item will be selected based on the last field of the Array.
* @param {Boolean} sinBlanco Set if the first blank element should be removed.
* @returns A string with the new combo structure. An empty string if an error ocurred.
* @type String
*/
function rellenarCombo(combo, arrayDatos, bolSelected, sinBlanco) {
  var i, value="";
  for (i = combo.options.length;i>=0;i--)
    combo.options[i] = null;
  i=0;
  if (sinBlanco==null || !sinBlanco)
    combo.options[i++] = new Option("", "");
  if (arrayDatos==null) return "";

  var total = arrayDatos.length;
  for (var j=0;j<total;j++) {
    combo.options[i] = new Option(arrayDatos[j][1], arrayDatos[j][0]);
    if (bolSelected!=null && bolSelected && arrayDatos[j][2]=="true") {
      value = arrayDatos[j][0];
      combo.options[i].selected = true;
    }
    else combo.options[i].selected = false;
    i++;
  }
  return value;
}

/**
* Search for an element in an Array
* @param {Array} arrayDatos Array of data. The structure of the array is:
*         <ol>
*           <li>key - Element key</li>
*           <li>text - Element text</li>
*           <li>selected - Boolean sets if the element is selected</li>
*         </ol>
* @param {Boolean} bolSelected Set if the element should be selected.
* @returns Returns the key of the founded element. An empty string if was not found or for an empty array.
* @type String
*/
function selectDefaultValueFromArray (arrayDatos, bolSelected) {
  var value="";
  if (arrayDatos==null) return "";

  value = arrayDatos[0][0];
  var total = arrayDatos.length;
  for (var j=0;j<total;j++) {
    if (bolSelected!=null && bolSelected && arrayDatos[j][2]=="true") {
      value = arrayDatos[j][0];
    }
  }
  return value;
}

/**
* Change the sorting direction of a list.
* @param {Object} sourceList A reference to the list that will be sorted.
* @returns True if everything was right, otherwise false.
* @type Boolean
*/
function changeOrderBy(sourceList) {
  if (sourceList == null) return false;
  for (var j=sourceList.length-1;j>=0;j--) {
    if (sourceList.options[j].selected==true) {
      var text = sourceList.options[j].text;
      var value = sourceList.options[j].value;
      if (value.indexOf("-")!=-1) {
        value = value.substring(1);
        text = text.substring(2);
        text = "/\\" + text;
      } else {
        value = "-" + value;
        text = text.substring(2);
        text = "\\/" + text;
      }      
      sourceList.options[j].value = value;
      sourceList.options[j].text = text;
    }
  }
  return true;
}

/**
* Function Description
* @param {Object} sourceList A reference to the source list 
* @param {Object} destinationList A reference to the destination list
* @param {Boolean} withPrefix
* @param {Boolean} selectAll
* @returns Returns false if source or destination list is null.
* @type Boolean
*/
function addListOrderBy(sourceList, destinationList, withPrefix, selectAll) {
  if (sourceList==null || destinationList==null) return false;
  if (selectAll==null) selectAll=false;
  if (withPrefix==null) withPrefix=false;
  for (var j=sourceList.length-1;j>=0;j--) {
    if (selectAll || sourceList.options[j].selected==true) {
      var text = sourceList.options[j].text;
      var value = sourceList.options[j].value;
      if (withPrefix) {
        if (value.indexOf("-")!=-1) value = value.substring(1);
        if (text.indexOf("/\\")!=-1 || text.indexOf("\\/")!=-1) text = text.substring(2);
      } else {
        text = "/\\" + text;
      }
      destinationList.options[destinationList.length] = new Option(text, value);
      sourceList.options[j]=null;
    }
  }
  return true;
}

/**
* Moves elements from one list to another.
* @param {Object} sourceList A reference to the source list, where the items come from.
* @param {Object} destinationList A reference to the destination list, where the items will be copied.
* @param {Boolean} selectAll Sets if we want to copy all the items.
* @returns True is the process was correct, otherwise false.
*/
function addList(sourceList, destinationList, selectAll) {
  if (sourceList==null || destinationList==null) return false;
  if (selectAll==null) selectAll=false;
  for (var j=sourceList.length-1;j>=0;j--) {
    if (selectAll || sourceList.options[j].selected==true) {
      destinationList.options[destinationList.length] = new Option(sourceList.options[j].text, sourceList.options[j].value);
      sourceList.options[j]=null;
    }
  }
  return true;
}

/**
* Moves an element or elements selected from a list, incrementing the position
* @param {Object} list A reference to the list where the items are contained.
* @param {Object} incr A integer that sets the number of positions added to the items. If is a negative number, the elements will move up; and if is null a default value of 1 will be used.  
*/
function moveElementInList(list, incr) {
  if (list==null) return false;
  else if (list.length<2) return false;
  if (incr==null) incr=1;
  if (incr>0) {
    for (var i=list.length-2;i>=0;i--) {
      if (list.options[i].selected==true && ((i+incr)>=0 || (i+incr)<list.length)) {
        list.options[i].selected=false;
        var text = list.options[i+incr].text;
        var value = list.options[i+incr].value;
        list.options[i+incr].value = list.options[i].value;
        list.options[i+incr].text = list.options[i].text;
        list.options[i+incr].selected=true;
        list.options[i].value = value;
        list.options[i].text = text;
      }
    }
  } else {
    var total = list.length;
    for (var i=1;i<total;i++) {
      if (list.options[i].selected==true && ((i+incr)>=0 || (i+incr)<list.length)) {
        list.options[i].selected=false;
        var text = list.options[i+incr].text;
        var value = list.options[i+incr].value;
        list.options[i+incr].value = list.options[i].value;
        list.options[i+incr].text = list.options[i].text;
        list.options[i+incr].selected=true;
        list.options[i].value = value;
        list.options[i].text = text;
      }
    }
  }
  return true;
}


/**
* Search for a key and returns the value in the {intDevolverPosicion} index position of the Array.
* @param {Array} arrDatos Array of elements 
* @param {String} strClave Key to search for
* @param {Number} intDevolverPosicion Index position of the returning value.
* @returns The value of the given index position, or an empty string if not was found.
* @type String
*/
function valorArray(arrDatos, strClave, intDevolverPosicion)
{
  if (arrDatos == null) return "";
  else if (strClave==null) return "";
  if (intDevolverPosicion==null) intDevolverPosicion = 1;

  var total = arrDatos.length;
  for (var i=0;i<total;i++) {
    if (arrDatos[i][0] == strClave) {
      return arrDatos[i][intDevolverPosicion];
    }
  }
  return "";
}


/**
* Gets the value of a radio button element or array.
* @param {Object} radio A reference to the object where we want to get the value.
* @returns The value of the given radio, or null if was not found.
* @type String
*/
function radioValue(radio)
{
  if (!radio) return null;
  else if (!radio.length)
    return ((radio.checked)?radio.value:null);
  var total = radio.length;
  for (var i=0;i<total;i++)
  {
    if (radio[i].checked)
      return radio[i].value;
  }
  return null;
}


/**
* Checks or unchecks all the elements associated to the parameter.
* @param {Object} chk A reference to the check button that will be marked or unmarked.
* @param {Boolean} bolMarcar Set if we will check or uncheck the element.
* @returns False if the element was not found, otherwise true.
*/
function marcarTodos(chk, bolMarcar)
{
  if (bolMarcar==null) bolMarcar = false;
  if (!chk) return false;
  else if (!chk.length) chk.checked = bolMarcar;
  else {
    var total = chk.length;
    for (var i=0;i<total;i++) chk[i].checked = bolMarcar;
  }
  return true;
}

/**
* Changes a combo's selected value based on an Array passed as parameter
* @param {Object} combo A reference to the combo that will be filled with the new values 
* @param {Array} arrayDatos Array that contains the data for the combo values
* @param {String} clave Sets the array's key (index) that will be the value data of our combo
* @param {Boolean} blanco Sets if we will add a blank value to the combo.
*/
function cambiarListaCombo(combo, arrayDatos, clave, blanco) {
  var i;
  var n=0;
  if (combo.options.length!=null) {
    for (i = combo.options.length;i>=0;i--)
      combo.options[i] = null;
  }

  if (blanco)
    combo.options[n++] = new Option("", "");
  if (arrayDatos==null) return false;

  var total = arrayDatos.length;
  for (i=0;i<total;i++) {
    if (arrayDatos[i][0]==clave)
      combo.options[n++] = new Option(arrayDatos[i][2], arrayDatos[i][1]);
  }
}


/**
* Removes all elements from a list
* @param {Object} campo A referece to the list that holds all the elements
* @returns True if was processed correctly, otherwise false.
*/
function limpiarLista(campo) {
  if (campo==null) return false;
  for (var i = campo.options.length - 1;i>=0;i--) campo.options[i] = null;
  return true;
}

/**
* Removes elements from list. Used when the elements are passed to another list.
* @param {Object} campo A reference to the list where the elements are contained.
* @returns True is was processed correctly, otherwise false.
*/
function eliminarElementosList(campo) {
  if (campo==null) return false;
  for (var i = campo.options.length - 1;i>=0;i--) {
    if (campo.options[i].selected) campo.options[i] = null;
  }
  return true;
}

/**
* Generates an Array based on a list of checkboxs selected.
* @param {Form} frm A reference to the form where the checkbox are contained.
* @param {Object} check A reference to the checkboxs list
* @param {String} text The textbox that has the name/index of the array. 
* @param {Array} resultado A reference parameter with the modified/generated array
*/
function generarArrayChecks(frm, check, text, resultado) {
  var n=0;
  if (check==null) {
    resultado=null;
    return;
  }
  if (!check.length || check.length<=1) {
    if (check.checked) {
      var texto = eval(frm.name + "." + text + check.value);
      var valor = "";
      if (texto!=null) {
        if (!texto.length || texto.length<=1) valor = texto.value;
        else valor = texto[0].value;
      }
      resultado[0] = new Array(check.value, valor);
    }
  } else {
    for (var i = check.length-1;i>=0;i--) {
      if (check[i].checked) {
        var valor = "";
        var texto = eval(frm.name + "." + text + check[i].value);
        if (texto!=null) {
          if (!texto.length || texto.length<=1) valor = texto.value;
          else valor = texto[0].value;
        }
        resultado[n++] = new Array(check[i].value, valor);
      }
    }
  }
}

/**
* Search for a key in a combo elements.
* @param {Object} combo A reference to the combo object.
* @param {String} clave The search key to look for in the comobo elements
* @returns True if was found, otherwise false.
*/
function estaEnCombo(combo, clave) {
  if (combo==null || clave==null) return false;
  var total = combo.options.length;
  for (var i=0;i<total;i++) {
    if (combo.options[i].value == clave) return true;
  }
  return false;
}

/**
* Adds new elements to a list based on a data Array.
* @param {Object} campoDestino A reference to the object where the elements will be added.
* @param {Array} arrayValores An array with the new data to add.
* @param True if the array was processed correclty, false on any problem.
*/
function insertarElementosList(campoDestino, arrayValores) {
  if (campoDestino == null || arrayValores == null) return false;
  var i = campoDestino.options.length;
  var total = arrayValores.length;
  for (var j=0; j<total;j++) {
      if (!estaEnCombo(campoDestino, arrayValores[j][0]))
        campoDestino.options[i++] = new Option(arrayValores[j][1], arrayValores[j][0]);
  }
  return true;
}

/**
* Selects all the elements of a list or combo. Used on multiple selectors where all the values will be selected prior the form submition.
* @param {Object} campo A reference to the combo that we want to select.
* @returns True is everything was right, otherwise false.
*/
function seleccionarListCompleto(campo) {
  if (campo==null || campo==null) return false;
  var total = campo.options.length;
  for (var i=0;i<total;i++) {
    campo.options[i].selected = true;
  }
  return true;
}

/**
* Handles the keypress event on textarea fields. Used to control the max length of a field.
* @param {Object} campo A reference to the object in the page. Usually use the 'this' reference.
* @param {Number} tamano Max length of the field.
* @param {Event} evt Event handling object.
* @returns True if is allowed to keep entering text, otherwise false.
*/
function tamanoMaximo(campo, tamano, evt) {
  if (campo==null || !campo) return false;
  if (campo.value.length>=tamano) {
    if (document.layers) keyCode.which=0;
    else {
      if (evt==null) evt = window.event;
      evt.keyCode=0;
      evt.returnValue = false;
      evt.cancelBubble = true 
    }
    mensaje(11);
    return false;
  }
  return true;
}

/**
* Function Description
* @param {Object} combo A reference the the combo object
* @param {String} clave The element key to select
* @returns True if the element was selected, otherwise false.
* @type Boolean
*/
function selectCombo(combo, clave) {
  if (!combo || combo==null) return false;
  var total = combo.length;
  for (var i=0;i<total;i++) {
    combo.options[i].selected = (combo.options[i].value == clave);
  }
  return true;
}

/**
* Function Description
* Shows or hides a window in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function updateMenuIcon(id) {
  if (!top.frameMenu) return false;
  else {
    var frame = top.document;
    var frameset = frame.getElementById("framesetMenu");
    if (!frameset) return false;
    try {
      if (top.isMenuHide==true) changeClass(id, "_hide", "_show", true);
      else changeClass(id, "_show", "_hide", true);
    } catch (ignored) {}
    return true;
  } 
}

/**
* Function Description
* Shows or hides a window in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuShowHide(id) {
  if (!top.frameMenu)
    window.open(baseFrameServlet, "_blank");
  else {
    if (id==null) id = 'buttonMenu';
    var frame = top.document;
    var frameset = frame.getElementById("framesetMenu");
    if (!frameset) 
      return false;
    /*try {
   var frm2 = frame.getElementById("frameMenu");
   var obj = document.onresize;
   var obj2 = frm2.onresize;
   document.onresize = null;
   frm2.document.onresize = null;
   progressiveHideMenu("framesetMenu", 30);
   document.onresize = obj;
   frm2.document.onresize = obj2;
   } catch (e) {*/
    if (top.isMenuHide == true) {
      frameset.cols = "0%,25%,*";
      top.isMenuHide = false;
      try {
        putFocusOnMenu();
      } catch(e) {
      }
    } else {
      frameset.cols = "0%,0%,*";
      top.isMenuHide = true;
    }
      //}
    try {
      updateMenuIcon(id);
    } catch (e) {}
    return true;
  }
}

/**
* Function Description
* Expands the whole content of the menu
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuExpand() {
  putFocusOnMenu();
  submitCommandForm('ALL', false, null, '../utility/VerticalMenu.html', 'frameMenu');
  return false;
}

/**
* Function Description
* Collapse the whole content of the menu
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuCollapse() {
  putFocusOnMenu();
  submitCommandForm('DEFAULT', false, null, '../utility/VerticalMenu.html', 'frameMenu');
  return false;
}

/**
* Function Description
* Collapse the whole content of the menu if the menu is expanded
* Expand the whole content of the menu if the menu is collapsed 
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuExpandCollapse() {
  var menuExpandCollapse_status = getMenuExpandCollapse_status();
  if (menuExpandCollapse_status == 'expanded') {
    menuCollapse();
  } else if (menuExpandCollapse_status == 'collapsed') {
    menuExpand();
  }
  return false;
}

function getMenuExpandCollapse_status() {
//  alert(top.frameMenu.getElementById('paramfieldDesplegar').getAttribute('id'));
  var menuExpandCollapse_status;
  if (top.frames['frameMenu'].document.getElementById('paramfieldDesplegar')) menuExpandCollapse_status = 'collapsed';
  if (top.frames['frameMenu'].document.getElementById('paramfieldContraer')) menuExpandCollapse_status = 'expanded';
  return menuExpandCollapse_status;
}

function menuUserOptions() {
  openServletNewWindow('DEFAULT', false, '../ad_forms/Role.html', 'ROLE', null, null, '460', '775');
  return true;
}

function menuQuit() {
  submitCommandForm('DEFAULT', false, null, '../security/Logout.html', '_top');
  return false;
}

function menuAlerts() {
  openLink('../ad_forms/AlertManagement.html', 'frameAplicacion');
  return true;
}

function isVisibleElement(obj, appWindow) {
  if (appWindow == null || appWindow == 'null' || appWindow == '') {
    appWindow = top;
  }
  var parentElement = obj;
  try {
    for(;;) {
      if (parentElement.style.display == 'none') {
        return false;
      } else if (parentElement == appWindow.document.getElementsByTagName('BODY')[0]) {
        break;
      }
      parentElement=parentElement.parentNode;
    }
  } catch(e) {
    return false;
  }
  return true;
}

function executeWindowButton(id,focus) {
  if (focus==null) focus=false;
  var appWindow = top;
  if(top.frames['frameAplicacion'] || top.frames['frameMenu']) {
    appWindow = top.frames['frameAplicacion'];
  } 
  if (appWindow.document.getElementById(id) && isVisibleElement(appWindow.document.getElementById(id), appWindow)) {
    if (focus==true) appWindow.document.getElementById(id).focus();
    appWindow.document.getElementById(id).onclick();
    if (focus==true) putWindowElementFocus(focusedWindowElement);
  }
}

function executeMenuButton(id) {
  var appWindow = top;
  if(top.frames['frameAplicacion'] || top.frames['frameMenu']) {
    appWindow = top.frames['frameMenu'];
  } 
  if (appWindow.document.getElementById(id) && isVisibleElement(appWindow.document.getElementById(id), appWindow)) {
    appWindow.document.getElementById(id).onclick();
  }
}

function getAppUrl() {
  var url = window.location.href;
  var http = url.split('//')[0];
  var nohttp = url.split('//')[1];
  var urlItem = nohttp.split('/')
  var appUrl=http + '//';
  for (var i=0; i<urlItem.length-2; i++) {
    appUrl = appUrl + urlItem[i] + '/';
  }
  return appUrl;
}

/**
* Function Description
* Resize a window progressively
* @param {String} id ID of the window
* @param {Number} topSize 
* @param {Number} newSize The new size of the window
* @param {Boolean} grow If the window should 'grow' or set the new size immediately
*/
function progressiveHideMenu(id, topSize, newSize, grow) {
  var frame = top.document;
  var object = frame.getElementById(id);
  if (newSize==null) {
    var sizes = object.cols.split(",");
    size = sizes[0];
    size = size.replace("%", "");
    size = size.replace("px", "");
    newSize = parseInt(size);
  }
  if (grow==null) grow = !(newSize>0);
  if (grow) {
    newSize += 5;
    if (newSize>=topSize) {
      object.cols = topSize + "%, *";
      return true;
    } else object.cols = newSize + "%, *";
  } else {
    newSize -= 5;
    if (newSize<=0) {
      object.cols = "0%, *";
      return true;
    } else object.cols = newSize + "%, *";
  }
  return setTimeout('progressiveHideMenu("' + id + '", ' + topSize + ', ' + newSize + ', ' + grow + ')', 100);
}

/**
* Function Description
* Change the class of an element on the page
* @param {String} id ID of the element
* @param {String} class1 The class to search for
* @param {String} class2 The class to replace
* @param {Boolean} forced 
* @returns False if the element was not found, otherwise True.
* @type Boolean
*/
function changeClass(id, class1, class2, forced) {
  if (forced==null) forced = false;
  var element = document.getElementById(id);
  if (!element) return false;
  if (element.className.indexOf(class1)!=-1) element.className = element.className.replace(class1, class2);
  else if (!forced && element.className.indexOf(class2)!=-1) element.className = element.className.replace(class2, class1);
  return true;
}

/**
* Function Description
* Change the readonly status of a textbox or a textarea
* @param {String} id ID of the element
* @param {Boolean} forced: it could be "true" or "false"
* @returns False if the element was not found, otherwise True.
* @type Boolean
*/
function changeReadOnly(id, forced) {
  if (forced==null) forced = false;
  var element = document.getElementById(id);
  if (!element) return false;
  if (!forced) {
    if (element.readOnly!=true) element.readOnly=true;
    else element.readOnly=false;
  } else {
//    forced = forced.toLowerCase();
    if (forced=="true") element.readOnly=true;
    else if (forced=="false") element.readOnly=false;
    else return false;
  }
  return true;
}


/**
* Function Description
* Gets a reference to a window
* @param {String} id ID of the element
* @returns A reference to the object, or null if the element was not found.
* @type Object
*/
function getReference(id) {
  if (document.getElementById) return document.getElementById(id);
  else if (document.all) return document.all[id];
  else if (document.layers) return document.layers[id];
  else return null;
}

/**
* Function Description
* Gets the style attribute of an element
* @param {String} id ID of the element
* @returns A reference to the style attribyte of the element or null if the element was not found.
* @type Object
* @see #getReference
*/
function getStyle(id) {
  var ref = getReference(id);
  if (ref==null || !ref) return null;
  return ((document.layers) ? ref : ref.style);
}

/**
* Returns a "modified version" of a name
* @param {String} name A string to modify
* @returns The string modified
*/
function idName(name) {
  return (name.substring(0,9) + name.substring(10));
}

/**
* Returns the position of a element in a form
* @param {Form} Formulario A reference to the form in the page.
* @param {Object} name Name of the element to search.
* @returns If was found returns the position of the element, if not returns null.
*/
function findElementPosition(Formulario, name) {
  var total = Formulario.length;
  for (var i=0;i<total;i++) {
    if (Formulario.elements[i].name==name) return i;
  }
  return null;
}

/**
* Function Description
* @param {Form} Formulario A reference to the form in the page
* @param {Object} field The currect field selected
* @returns The position of the element
* @type Number
* @see #findElementPosition 
* @see #recordSelectExplicit
*/
function deselectActual(Formulario, field) {
  if (field==null || field.value==null || field.value=="") return null;
  var i=findElementPosition(Formulario, "inpRecordW" + field.value);
  if (i==null) return null;
  recordSelectExplicit("inpRecord" + field.value, false);
  field.value="";
  return i;
}

/**
* Returns the first element on a form
* @param {Form} A reference to the form in the page
* @returns The first element on a form
* @type Object
*/
function findFirstElement(Formulario) {
  if (Formulario==null) return null;
  var n=null;
  var total = Formulario.length;
  for (var i=0;i<total;i++) {
    if (Formulario.elements[i].name.indexOf("inpRecordW")==0) {
      n=i;
      break;
    }
  }
  return n;
}

/**
* Returns the last element on a form
* @param {Form} A reference to the form in the page
* @returns The last element on a form
* @type Object
*/
function findLastElement(Formulario) {
  if (Formulario==null) return null;
  var n=null;
  for (var i=Formulario.length-1;i>=0;i--) {
    if (Formulario.elements[i].name.indexOf("inpRecordW")==0) {
      n=i;
      break;
    }
  }
  return n;
}

/**
* Selects the next element on a form
* @param {Form} Formulario A reference to the form in the page
* @param {Object} field The currect field selected
* @returns True
* @type Boolean 
* @see #deselectActual 
* @see #findFirstElement 
* @see #findLastElement 
* @see #recordSelectExplicit 
*/
function nextElement(Formulario, field) {
  var i=deselectActual(Formulario, field);
  if (i==null) {
    i=findFirstElement(Formulario);
    if (i==null) return;
  } else if (i<findLastElement(Formulario)) i++;
  field.value = Formulario.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + Formulario.elements[i].name.substring(10) , true);
  Formulario.elements[i].focus();
  return true;
}

/**
* Selects the previous element on a form
* @param {Form} Formulario A reference to the form in the page
* @param {Object} field The currect field selected
* @returns True
* @type Boolean 
* @see #deselectActual 
* @see #findFirstElement 
* @see #recordSelectExplicit 
*/
function previousElement(Formulario, field) {
  var i=deselectActual(Formulario, field);
  var menor = findFirstElement(Formulario);
  if (menor==null) return;
  else if (i==null) {
    i=menor;
    if (i==null) return;
  } if (i>menor) i--;
  field.value = Formulario.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + Formulario.elements[i].name.substring(10) , true);
  Formulario.elements[i].focus();
  return true;
}

/**
* Selects the first element on a form
* @param {Form} Formulario A reference to the form in the page
* @param {Object} field The currect field selected
* @returns True
* @type Boolean
* @see #deselectActual 
* @see #findFirstElement 
* @see #recordSelectExplicit
*/
function firstElement(Formulario, field) {
  var i=deselectActual(Formulario, field);
  i=findFirstElement(Formulario);
  if (i==null) return;
  field.value = Formulario.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + Formulario.elements[i].name.substring(10) , true);
  Formulario.elements[i].focus();
  return true;
}

/**
* Selects the las element on a form
* @param {Form} Formulario A reference to the form in the page
* @param {Object} field The currect field selected
* @returns True
* @type Boolean
* @see #deselectActual 
* @see #findLastElement 
* @see #recordSelectExplicit
*/
function lastElement(Formulario, field) {
  var i=deselectActual(Formulario, field);
  i=findLastElement(Formulario);
  if (i==null) return;
  field.value = Formulario.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + Formulario.elements[i].name.substring(10) , true);
  Formulario.elements[i].focus();
  return true;
}

/**
* Highlight an element on the page
* @param {String} name The id of the element on the page 
* @param {Boolean} seleccionar Sets if we want to highlight an element. 
* @returns The seleccionar value
* @type Boolean
* @see #getStyle
*/
function recordSelectExplicit(name, seleccionar) {
  var obj = getStyle(name);
  if (obj==null) return false;
  if (document.layers) {
    if (seleccionar) obj.bgColor=gColorSelected;
    else obj.bgColor=gWhiteColor;
  } else {
    if (seleccionar) obj.backgroundColor = gColorSelected;
    else obj.backgroundColor=gWhiteColor;
  }
  return seleccionar;
}

/**
* Select an element from a set (array) of radio buttons.
* @param {Object} radio A reference to the radio button
* @param {String} Value The value to select
* @returns * @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function selectRadioButton(radio, Value) {
  if (!radio) return false;
  else if (!radio.length) radio.checked=true;
  else {
    var total = radio.length;
    for (var i=0;i<total;i++) radio[i].checked = (radio[i].value==Value);
  }
  return true;
}
/**
* Selects a value from a set of radio buttons.
* @param {Object} Reference to the radio(s) element(s).
* @param {String} Value of the element that we want to check.
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function selectCheckbox(obj, Value) {

  if (!obj) return false;
  else {
    obj.checked = (obj.value==Value);
  }
  return true;
}


/**
* Sets the message to display.  
* @param {Form} Formulario A reference to the form
* @param {String} ElementName The name of the element (INFO, ERROR, SUCCESS, WARNING, EXECUTE, DISPLAY, HIDE, CURSOR_FIELD).
* @param {Value} The value to set
* @returns True if the value was setted, othewise false.
* @type Boolean
* @see #setValues_MessageBox
*/
function formElementValue(Formulario, ElementName, Value) {
  var bolReadOnly=false;
  var onChangeFunction = "";
  if (Formulario==null) {
    Formulario=document.forms[0];
    if (Formulario==null) return false;
  } else if (ElementName==null) return false;
  if (ElementName=="MESSAGE") {
    try {
      setValues_MessageBox('messageBoxID', "INFO", "", Value);
    } catch (err) {
      alert(Value);
    }
  } else if (ElementName=="ERROR" || ElementName=="SUCCESS" || ElementName=="WARNING" || ElementName=="INFO") {
    try {
      setValues_MessageBox('messageBoxID', ElementName, "", Value);
    } catch (err) {
      alert(Value);
    }
  } else if (ElementName=="EXECUTE") {
    eval(Value);
  } else if (ElementName=="DISPLAY") {
    displayLogicElement(Value, true);
  } else if (ElementName=="HIDE") {
    displayLogicElement(Value, false);
  } else if (ElementName=="CURSOR_FIELD") {
    var obj = eval("document." + Formulario.name + "." + Value + ";");
    if (obj==null || !obj || !obj.type || obj.type.toUpperCase()=="HIDDEN") return false;
    setWindowElementFocus(obj);
    if (obj.type.toUpperCase().indexOf("SELECT")==-1) obj.select();
    //document.focus();
  } else {
    if (ElementName.indexOf("_BTN")!=-1) {
      if (Value==null || Value=="null") Value="";
      layer(ElementName, Value, true);
      return true;
    }
    var obj = eval("document." + Formulario.name + "." + ElementName + ";");
    if (obj==null || !obj || !obj.type) return false;
    if (obj.getAttribute("readonly")=="true") bolReadOnly=true;
    if (bolReadOnly) {
      onChangeFunction = obj.onchange;
      obj.onchange = "";
      obj.setAttribute("readonly", "false");
      //obj.readOnly="false";
    }
    if (obj.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (Value!=null && typeof Value!="object") {
        var total = obj.length;
        var index = -1;
        var hasMultiSelect = false;
        var selectedOption = false;
        if ((Value==null || Value=="") && total>0) Value = obj.options[0].value;
        for (var i=0;i<total;i++) {
          selectedOption = (obj.options[i].value == Value);
          obj.options[i].selected = selectedOption;
          if (selectedOption) {
            if (index!=-1) hasMultiSelect = true;
            index = i;
          }
        }
        if (!hasMultiSelect) obj.selectedIndex = index;
      } else Value = rellenarCombo(obj, Value, true, ((obj.className.toUpperCase().indexOf("REQUIRED")!=-1) || obj.className.toUpperCase().indexOf("KEY")!=-1 || (obj.className.toUpperCase().indexOf("READONLY")!=-1)));
    } else if (obj.type.toUpperCase().indexOf("CHECKBOX")!=-1) {
      selectCheckbox(obj, Value);
    } else if (obj.type.toUpperCase().indexOf("RADIO")!=-1 || obj.type.toUpperCase().indexOf("CHECK")!=-1) {
      selectRadioButton(obj, Value);
    } else {
      if (Value==null || Value=="null") Value="";
      if (typeof Value!="object") {
        obj.value = Value;
      } else //if (obj.className.toUpperCase().indexOf("REQUIRED")!=-1 || obj.className.toUpperCase().indexOf("KEY")!=-1 || obj.className.toUpperCase().indexOf("READONLY")!=-1) 
        obj.value = selectDefaultValueFromArray(Value, true);
    }
    if (bolReadOnly && onChangeFunction) {
      var i = onChangeFunction.toString().indexOf("selectCombo(this,");
      var search = "\"";
      if (i!=-1) {
        var first = onChangeFunction.toString().indexOf(search, i+1);
        if (first==-1) {
          search = "'";
          first = onChangeFunction.toString().indexOf(search, i+1);
        }
        if (first!=-1) {
          var end = onChangeFunction.toString().indexOf(search, first+1);
          if (end!=-1) {
            onChangeFunction = onChangeFunction.toString().substring(0, first+1) + Value + onChangeFunction.toString().substring(end);
            onChangeFunction = onChangeFunction.toString().replace("function anonymous()", "");
          }
        }
      }
      if (onChangeFunction.toString().indexOf("function anonymous()")==-1) obj.onchange = new Function("", onChangeFunction.toString());
      else obj.onchange = onChangeFunction.toString();
      //obj.onchange = function anonymous() {selectCombo(this, Value);return true;};
      obj.setAttribute("readonly", "true");
    }
  }
  return true;
}

/**
* Sets the class attribute of an element
* @param {String} id The ID of the element
* @param {String} selectClass The class to be setted.
* @returns null if the element was not found.
*/
function setClass(id, selectClass) {
  var obj = getReference(id);
  if (obj==null) return null;
  obj.className = selectClass;
}

/**
* Returns the class attibute of an element
* @param {String} id ID of the html element
* @param {String} previousClass Default class to be returned if an error ocurred.
* @returns The class if the element was found, otherwise returns the text of the previousClass parameter. 
* @type String
*/
function getObjectClass(id, previousClass) {
  var obj = getReference(id);
  if (obj==null) return previousClass;
  return(obj.className);
}

/**
* Function Description
* @param {Form} Formulario A reference to the form.
* @param {String} ElementName The name of the element.
* @param {String} callout The CallOut associated.
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function formElementEvent(Formulario, ElementName, calloutName) {
  if (Formulario==null) Formulario=document.forms[0].name;
  else if (ElementName==null) return false;
  var isReload=false;
  if (ElementName!="MESSAGE" && ElementName!="CURSOR_FIELD" && ElementName!="EXECUTE" && ElementName!="DISPLAY" && ElementName!="HIDE" && ElementName.indexOf("_BTN")==-1) {
    var obj = eval("document." + Formulario + "." + ElementName + ";");
    if (obj==null || !obj || !obj.type) return false;
    if (obj.type.toUpperCase().indexOf("RADIO")!=-1) {
      if (obj.onclick!=null && obj.onclick.toString().indexOf(calloutName)==-1) {
        if (obj.onclick.toString().indexOf("callout")!=-1 || obj.onclick.toString().indexOf("reload")!=-1) isReload=true;
        obj.onclick();
      }
    } else {
      var bolReadOnly = false;
      if (obj.onchange!=null && obj.onchange.toString().indexOf(calloutName)==-1) {
        if (obj.onchange.toString().indexOf("callout")!=-1 || obj.onchange.toString().indexOf("reload")!=-1) isReload=true;
        if (obj.getAttribute("readonly")=="true") {
          bolReadOnly=true;
          obj.removeAttribute("readonly");
        }
        obj.onchange();
        if (bolReadOnly) obj.setAttribute("readonly", "true");
      }
    }
  }
  return (isReload);
}

/**
* Struct to be used on the fillElementsFromArray function
* @param {Form} frm A reference to the form
* @param {String} name 
* @param {String} callout The CallOut to be associated
* @see #fillElementsFromArray
*/
function fillElements(frm, name, callout) {
  this.formName = frm;
  this.name = name;
  this.callout = callout;
}

/**
* Set the value of the variable gWaitingCallOut
* @param {state} boolean: true to set the variable to true, false to set the variable to false.
* @returns True if everything goes right, otherwise false.
*/
function setGWaitingCallOut(state) {
  if (state==true) {
    try {
      setCalloutProcessing(true);
    }
    catch (e) {}
    gWaitingCallOut=true;
  } else if (state==false) {
    try {
      setCalloutProcessing(false);
    }
    catch (e) {}
    gWaitingCallOut=false;
  } else {
    return false;
  }
  return true;
}


/**
* Function Description
* @param {Array} arrElements The array of elements
* @param {String} calloutName The CallOut to be associated.
* @param {Form} Formulario A reference to the form, if is null, the first form in the page will be used.
* @returns True if everything goes right, otherwise false.
* @type Boolean
* @see #formElementEvent
*/
function fillElementsFromArray(arrElements, calloutName, Formulario) {
  if (arrElements==null && arrGeneralChange==null) return false;
  if (Formulario==null || !Formulario) Formulario=document.forms[0];
  if (arrElements!=null) {
    var total = arrElements.length;
    for (var x=0;x<total;x++) {
      formElementValue(Formulario, arrElements[x][0], arrElements[x][1]);
    }
  }
  if (arrGeneralChange==null) arrGeneralChange=new Array();
  if (arrElements!=null) {
    var n=arrGeneralChange.length;
    var total = arrElements.length;
    for (var x=0;x<total;x++) {
        arrGeneralChange[x+n] = new fillElements(Formulario.name , arrElements[x][0], calloutName);
    }
  }
  while (arrGeneralChange!=null && arrGeneralChange.length>0) {
    var obj = arrGeneralChange[0].formName;
    var name = arrGeneralChange[0].name;
    var callout = arrGeneralChange[0].callout;
    {
      if (arrGeneralChange==null || arrGeneralChange.length==0) return true;
      var arrDataNew = new Array();
      var total = arrGeneralChange.length;
      for (var i=1;i<total;i++) {
        arrDataNew[i-1] = new fillElements(arrGeneralChange[i].formName, arrGeneralChange[i].name, arrGeneralChange[i].callout);
      }
      arrGeneralChange=null;
      arrGeneralChange = new Array();
      total = arrDataNew.length;
      for (var i=0;i<total;i++) {
        arrGeneralChange[i] = new fillElements(arrDataNew[i].formName, arrDataNew[i].name, arrDataNew[i].callout);
      }
    }
    if (formElementEvent(obj, name, callout)) return true;
  }
  /*try {
    document.focus();
  } catch (e) {}*/
  return true;
}

/**
* Returns the values of a selected field in a GET format method
* @param {String} name The name of the command
* @param {Object} campo A reference to the field where the values will be extracted.
* @returns A string with the extracted values in the form name=value
* @type String
*/
function inputValueForms(name, campo) {
  var result = "";
  if (campo==null || !campo) return "";
  if (!campo.type && campo.length>1) campo = campo[0];
  if (campo.type) {
    if (campo.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (campo.selectedIndex==-1) return "";
      else {
        var length = campo.options.length;
        for (var fieldsCount=0;fieldsCount<length;fieldsCount++) {
          if (campo.options[fieldsCount].selected) {
            if (result!="") result += "&";
            result += name + "=" + escape(campo.options[fieldsCount].value);
          }
        }
        return result;
      }
    } else if (campo.type.toUpperCase().indexOf("RADIO")!=-1 || campo.type.toUpperCase().indexOf("CHECK")!=-1) {
      if (!campo.length) {
        if (campo.checked) return (name + "=" + escape(campo.value));
        else return "";
      } else {
        var total = campo.length;
        for (var i=0;i<total;i++) {
          if (campo[i].checked) {
            if (result!="") result += "&";
            result += name + "=" + escape(campo[i].value);
          }
        }
        return result;
      }
    } else return name + "=" + escape(campo.value);
  }

  return "";
}

/**
* Set the focus on the specified field.
* @param {Object} campo A reference to the field where the focus will be set.
* @returns An empty string
* @type String
*/
function setFocus(campo) {
  if (campo==null || !campo) return "";
  if (!campo.type && campo.length>1) campo = campo[0];
  try {
    campo.focus();
  } catch (ignored) {}

  return "";
}

/**
* Gets the value of a field.
* @param {Object} camp A reference to the object where the value will be extracted
* @returns An empty string if the field does not exist, or the field's value.
* @type String
*/
function inputValue(campo) {
  if (campo==null || !campo) return "";
  if (!campo.type && campo.length>1) campo = campo[0];
  if (campo.type) {
    if (campo.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (campo.selectedIndex==-1) return "";
      else return campo.options[campo.selectedIndex].value;
    } else if (campo.type.toUpperCase().indexOf("RADIO")!=-1 || campo.type.toUpperCase().indexOf("CHECK")!=-1) {
      if (!campo.length)
      return ((campo.checked)?campo.value:"N");
      var total = campo.length;
      for (var i=0;i<total;i++) {
        if (campo[i].checked) return campo[i].value;
      }
      return "N";
    } else return campo.value;
  }

  return "";
}

/**
* Sets a value to a field
* @param {Object} campo A reference to the field
* @param {String} myvalue The value to set.
* @returns True if the value was set, otherwise false.
* @type Boolean
*/
function setInputValue(campo, myvalue) {
  if (campo==null || campo=="") return false;
  var obj = document.forms[0].elements[campo];
  if (obj==null) return false;
  if (obj.length>1) {
    var total = obj.length;
    for (var i=0;i<total;i++) obj[i].value = myvalue;
  } else obj.value = myvalue;
  return true;
}

/**
* Shows and hides an element on the screen. Implements the display logic in the window.
* @param {Object} id A reference to the object that will be handled.
* @param {Boolean} display Set if we want to show or hide the element
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function displayLogicElement(id, display) {
  var obj = getStyle(id);
  if (obj==null) return false;
  if (id.indexOf("_td")!=-1) {
    obj = getReference(id);
    if (display) obj.className = obj.className.replace("_Nothing","");
    else {
      obj.className = obj.className.replace("_Nothing","");
      obj.className = obj.className + "_Nothing";
    }
  } else {
    if (display) obj.display="";
    else obj.display="none";
  }
  return true;
}

/**
* Sets elements as readonly or not depending on the logic
* @param {Object} id A reference to the object that will be handled.
* @param {Boolean} readonly set readonly or not depending on this field
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function readOnlyLogicElement(id, readonly) {
  obj = getStyle(id);
  if (obj==null) return false;

  obj = getReference(id);
  className = obj.className;
 
  if (readonly) {
    obj.className = className.replace("ReadOnly","");
    obj.readOnly = true;
      
    if (className.indexOf("Combo ")!=-1) {
       obj.className = className.replace("Combo","ComboReadOnly");
       obj.setAttribute("onChange", "selectCombo(this, '"+obj.value+"');"+obj.getAttribute("onChange"));
     }
    if (className.indexOf("ComboKey ")!=-1) {
      obj.className = className.replace("ComboKey","ComboKeyReadOnly");
      obj.setAttribute("onChange", "selectCombo(this, '"+obj.value+"');"+obj.getAttribute("onChange"));
    }
    if (className.indexOf("LabelText ")!=-1)
      obj.className = className.replace("LabelText","LabelTextReadOnly");
    if ((className.indexOf("TextBox_")!=-1)||(className.indexOf("TextArea_")!=-1)) {
      if (className.indexOf("readonly")==-1) changeClass(id,'readonly ', '');
    }
  } else { //not readonly
    obj.className = obj.className.replace("ReadOnly","");
    obj.className = obj.className.replace("readonly","");
    obj.readOnly = false;
    if (className.indexOf("Combo")!=-1) {
      onchange = obj.getAttribute("onChange");
      if (onchange.indexOf("selectCombo")!=-1) 
        obj.setAttribute("onChange", onchange.substring(0,onchange.indexOf("selectCombo"))+onchange.substring(onchange.indexOf(";",onchange.indexOf("selectCombo"))+1, onchange.length));
    }
  }
  return true;
}


/**
* Search for a key in a combo elements.
* @param {Object} combo A reference to the combo list.
* @param {String} clave A key to search in the list.
* @returns True if the key was found in the list, otherwise false.
* @type Boolean
*/
function estaEnCombo(combo, clave) {
  if (combo==null || clave==null) return false;
  var total = combo.options.length;
  for (var i=0;i<total;i++) {
    if (combo.options[i].value == clave) return true;
  }
  return false;
}

/**
* Handles the onKeyDown and onKeyUp event, for an specific numeric typing control.
* @param {Object} obj Field where the numeric typing will be evaluated.
* @param {Boolean} bolDecimal Defines if a float number is allowed.
* @param {Boolean} bolNegativo Defines if a negative number is allowed.
* @param {Event} evt The event handling object associated with the field.
* @returns True if is an allowed number, otherwise false.
* @type Boolean
* @see #obtainKeyCode
*/
function auto_complete_number(obj, bolDecimal, bolNegativo, evt) {
  var number;
  if (document.all) evt = window.event;
  if (document.layers) { number = evt.which; }
  if (document.all)    { number = evt.keyCode;}
  if (number != obtainKeyCode("ENTER") && number != obtainKeyCode("LEFTARROW") && number != obtainKeyCode("RIGHTARROW") && number != obtainKeyCode("UPARROW") && number != obtainKeyCode("DOWNARROW") && number != obtainKeyCode("DELETE") && number != obtainKeyCode("BACKSPACE") && number != obtainKeyCode("END") && number != obtainKeyCode("HOME") && !evt["ctrlKey"]) {
    if (number>95 && number <106) { //Teclado numÃ©rico
      number = number - 96;
      if(isNaN(number)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (number!=obtainKeyCode("DECIMAL") && number != obtainKeyCode("NUMBERDECIMAL") && number != obtainKeyCode("NEGATIVE") && number != obtainKeyCode("NUMBERNEGATIVE")) { //No es "-" ni "."
      number = String.fromCharCode(number);
      if(isNaN(number)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (number==obtainKeyCode("DECIMAL") || number==obtainKeyCode("NUMBERDECIMAL")) { //Es "."
      if (bolDecimal) {
        if (obj.value==null || obj.value=="") return true;
        else {
          var point = obj.value.indexOf(".");
          if (point != -1) {
            point = obj.value.indexOf(".", point+1);
            if (point==-1) return true;
          } else return true;
        }
      }
      if (document.all) evt.returnValue = false;
      return false;
    } else { //Es "-"
      if (bolNegativo && (obj.value==null || obj.value.indexOf("-")==-1)) return true;
      if (document.all) evt.returnValue = false;
      return false;
    }
  }
  return true;
}

/**
* Used on the onChange event for field changes logging. Requires a field named inpLastFieldChanged in the form.
* @param {Object} campo Reference to the field that will be logged. 
* @returns True if everything goes right, false if the field does not exist or an error occurred. 
* @type Boolean
* @see #setChangedField
*/
function logChanges(campo) {
  if (campo==null || !campo) return false;
  changeToEditingMode()
  return setChangedField(campo, campo.form);
}

/**
* Used on the onKeyDown event for isEditing status.
*/
function changeToEditingMode() {
  try {
    if (!isTabPressed && isKeyboardLocked==false) {
      setWindowEditing(true);
    }
  } catch (e) {}
}

/**
* Used on the undo toolbar button
* @param {Object} form Reference to the application form
*/
function windowUndo(form) {
  form.reset();
  form.inpLastFieldChanged.value = '';
  setWindowEditing(false);
  displayLogic();
}

/**
* Opens a pop-up window with a processing message. Used for long wait calls.
* @retunrs A reference ID of the newly opened window 
* @type Object
*/
function processingPopUp() {
  var complementosNS4 = ""

  var strHeight=100, strWidth=200;
  var strTop=parseInt((screen.height - strHeight)/2);
  var strLeft=parseInt((screen.width - strWidth)/2);
  
  if (navigator.appName.indexOf("Netscape"))
    complementosNS4 = "alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ";
  var complementos = complementosNS4 + "height=" + strHeight + ", width=" + strWidth + ", left=" + strLeft + ", top=" + strTop + ", screenX=" + strLeft + ", screenY=" + strTop + ", location=0, resizable=0, status=0, toolbar=0, titlebar=0";
  var winPopUp = window.open("", "_blank", complementos);
  if (winPopUp!=null) {
    document.onunload = function(){winPopUp.close();};
    document.onmousedown = function(){winPopUp.close();};
    winPopUp.document.writeln("<html>\n");
    winPopUp.document.writeln("<head>\n");
    winPopUp.document.writeln("<title>Proceso petici&oacute;n</title>\n");
    winPopUp.document.writeln("<script language=\"javascript\" type=\"text/javascript\">\n");
    winPopUp.document.writeln("function selectTD(name, seleccionar) {\n");
    winPopUp.document.writeln("  var obj = getStyle(name);\n");
    winPopUp.document.writeln("  if (document.layers) {\n");
    winPopUp.document.writeln("    if (seleccionar) obj.bgColor=\"" + gColorSelected + "\";\n");
    winPopUp.document.writeln("    else obj.bgColor=\"" + gWhiteColor + "\";\n");
    winPopUp.document.writeln("  } else {\n");
    winPopUp.document.writeln("    if (seleccionar) obj.backgroundColor = \"" + gColorSelected + "\";\n");
    winPopUp.document.writeln("    else obj.backgroundColor=\"" + gWhiteColor + "\";\n");
    winPopUp.document.writeln("  }\n");
    winPopUp.document.writeln("  return seleccionar;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("function getReference(id) {\n");
    winPopUp.document.writeln("  if (document.getElementById) return document.getElementById(id);\n");
    winPopUp.document.writeln("  else if (document.all) return document.all[id];\n");
    winPopUp.document.writeln("  else if (document.layers) return document.layers[id];\n");
    winPopUp.document.writeln("  else return null;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("function getStyle(id) {\n");
    winPopUp.document.writeln("  var ref = getReference(id);\n");
    winPopUp.document.writeln("  if (ref==null || !ref) return null;\n");
    winPopUp.document.writeln("  return ((document.layers) ? ref : ref.style);\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("var total=5;\n");
    winPopUp.document.writeln("function loading(num) {\n");
    winPopUp.document.writeln(" if (num>=total) {\n");
    winPopUp.document.writeln("   for (var i=0;i<total;i++) {\n");
    winPopUp.document.writeln("     selectTD(\"TD\" + i, false);\n");
    winPopUp.document.writeln("   }\n");
    winPopUp.document.writeln("   num=-1;\n");
    winPopUp.document.writeln(" } else {\n");
    winPopUp.document.writeln("   selectTD(\"TD\" + num, true);\n");
    winPopUp.document.writeln(" }\n");
    winPopUp.document.writeln(" setTimeout('loading(' + (++num) + ')', 1000);\n");
    winPopUp.document.writeln(" return true;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("</script>\n");
    winPopUp.document.writeln("</head>\n");
    winPopUp.document.writeln("<body leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" onLoad=\"loading(0);\">\n");
    winPopUp.document.writeln("  <table width=\"80%\" border=\"0\" cellspacing=\"3\" cellpadding=\"0\" align=\"center\">\n");
    winPopUp.document.writeln("    <tr>\n");
    winPopUp.document.writeln("      <td colspan=\"5\" align=\"center\"><font color=\"navy\" size=\"5\">PROCESSING...</font></td>\n");
    winPopUp.document.writeln("    </tr>\n");
    winPopUp.document.writeln("    <tr bgcolor=\"" + gWhiteColor + "\">\n");
    winPopUp.document.writeln("      <td width= \"20%\" id=\"TD0\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD1\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD2\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD3\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD4\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("    </tr>\n");
    winPopUp.document.writeln("  </table>\n");
    winPopUp.document.writeln("</body>\n");
    winPopUp.document.writeln("</html>\n");
    winPopUp.document.close();
    winPopUp.focus();
  }
  return winPopUp;
}

/**
* Returns the rounded value of a number to specified precision (number of digits after the decimal point).
* @param The value to round
* @param The number of decimal digits to round to.
* @return Returns the rounded value.
* @type Number
*/
function round(number,X) {
  X = (!X ? 2 : X);
  if (!number || isNaN(number)) return 0;
  return Math.round(number*Math.pow(10,X))/Math.pow(10,X);
}

/**
* Replace the occurrence of the search string with the replacement string
* @param {String} texto The original String.
* @param {String} replaceWhat The search String.
* @param {String} replaceWith The replacement String.
* @returns A String with the replaced text.
* @type String
*/
function ReplaceText(texto, replaceWhat, replaceWith) {
  if (texto==null || texto.length==0) return "";
  texto += "";
  var i = texto.indexOf(replaceWhat);
  var j = 0;
  while (i!=-1) {
    var partial = texto.substring(0, i);
    texto = texto.substring(i+replaceWhat.length);
    texto = partial + replaceWith + texto;
    j = i + replaceWith.length;
    i = texto.indexOf(replaceWhat, j);
  }
  return texto;
}

/**
* Fires the onChange event on the field passed as parameter and updates the inpLastFieldChanged in the form.
* @param {Object} field A reference to the field that will be updated.
* @returns True
* @type Boolean
* @see #setInputValue
*/
function updateOnChange(field) {
  if (field==null) return false;
  try {
    var lastChanged = inputValue(document.forms[0].inpLastFieldChanged);
    if (field.name!="inpadClientId" && field.name!="inpadOrgId") field.onchange();
    setInputValue(document.forms[0].inpLastFieldChanged, lastChanged);
  } catch (e) {}
  return true;
}

/**
* Dummy function. To be substituted in the &lt;script&gt; tag on the page.
* @returns True
* @type Boolean
*/
function xx()
{
  return true;
}

/**
* @name menuContextual
* @format function menuContextual()
* @comment Se trata de un funciÃ³n manejadora de eventos que sirve para el control del click con el 
*          botÃ³n derecho sobre la pÃ¡gina. Esta funciÃ³n no permite dicho evento, presentando un mensaje 
*          en tal caso.
*/
function menuContextual(evt) {
  var boton = (evt==null)?event.button:evt.which;
  if (boton == 3 || boton == 2) {
    if (document.all) alert('El boton derecho estÃ¡ deshabilitado por pruebas');
    return false;
  }
  return true;
}

/*
* Calendar compatibility with external elements
*/
/**
* This function gets called when the end-user clicks on some date.
* @param {Object} cal 
* @param {String} date 
* 
*/ 
function selected(cal, date) {
  cal.sel.value = date; // just update the date in the input field.
  if (cal.dateClicked && (cal.sel.id == "sel1" || cal.sel.id == "sel3"))
    // if we add this call we close the calendar on single-click.
    // just to exemplify both cases, we are using this only for the 1st
    // and the 3rd field, while 2nd and 4th will still require double-click.
    cal.callCloseHandler();
}

/**
* And this gets called when the end-user clicks on the _selected_ date,
* or clicks on the "Close" button.  It just hides the calendar without
* destroying it
* @param {Type} cal
*/
// .
function closeHandler(cal) {
  if (typeof (cal.sel.onchange)!="undefined") cal.sel.onchange();
  cal.sel.focus();
  cal.hide();                        // hide the calendar
//  cal.destroy();
  _dynarch_popupCalendar = null;
}

/**
* Function Description
* @param {String} str_format
* @returns
* @type String
*/
function getDateFormat(str_format) {
  var format = "";
  str_format = str_format.replace("mm","MM").replace("dd","DD").replace("yyyy","YYYY");
  str_format = str_format.replace("%D","%d").replace("%M","%m").replace("%y","%Y");
  if (str_format!=null && str_format!="" && str_format!="null") {
         if (str_format.indexOf('DD-MM-YYYY')!=-1)  format = "%d-%m-%Y";
    else if (str_format.indexOf('MM-DD-YYYY')!=-1)  format = "%m-%d-%Y";
    else if (str_format.indexOf('YYYY-MM-DD')!=-1)  format = "%Y-%m-%d";
    else if (str_format.indexOf('DD/MM/YYYY')!=-1)  format = "%d/%m/%Y";
    else if (str_format.indexOf('MM/DD/YYYY')!=-1)  format = "%m/%d/%Y";
    else if (str_format.indexOf('YYYY/MM/DD')!=-1)  format = "%Y/%m/%d";
    else if (str_format.indexOf('DD.MM.YYYY')!=-1)  format = "%d.%m.%Y";
    else if (str_format.indexOf('MM.DD.YYYY')!=-1)  format = "%m.%d.%Y";
    else if (str_format.indexOf('YYYY.MM.DD')!=-1)  format = "%Y.%m.%d";
    else if (str_format.indexOf('DD:MM:YYYY')!=-1)  format = "%d:%m:%Y";
    else if (str_format.indexOf('MM:DD:YYYY')!=-1)  format = "%m:%d:%Y";
    else if (str_format.indexOf('YYYY:MM:DD')!=-1)  format = "%Y:%m:%d";

    else if (str_format.indexOf('%d-%m-%Y')!=-1)  format = "%d-%m-%Y";
    else if (str_format.indexOf('%m-%d-%Y')!=-1)  format = "%m-%d-%Y";
    else if (str_format.indexOf('%Y-%m-%d')!=-1)  format = "%Y-%m-%d";
    else if (str_format.indexOf('%d/%m/%Y')!=-1)  format = "%d/%m/%Y";
    else if (str_format.indexOf('%m/%d/%Y')!=-1)  format = "%m/%d/%Y";
    else if (str_format.indexOf('%Y/%m/%d')!=-1)  format = "%Y/%m/%d";
    else if (str_format.indexOf('%d.%m.%Y')!=-1)  format = "%d.%m.%Y";
    else if (str_format.indexOf('%m.%d.%Y')!=-1)  format = "%m.%d.%Y";
    else if (str_format.indexOf('%Y.%m.%d')!=-1)  format = "%Y.%m.%d";
    else if (str_format.indexOf('%d:%m:%Y')!=-1)  format = "%d:%m:%Y";
    else if (str_format.indexOf('%m:%d:%Y')!=-1)  format = "%m:%d:%Y";
    else if (str_format.indexOf('%Y:%m:%d')!=-1)  format = "%Y:%m:%d";
  }
  if (str_format==null || str_format=="" || str_format=="null") str_format = defaultDateFormat;
  else if (str_format.indexOf(" %H:%M:%S")!=-1) format += " %H:%M:%S";
  else if (str_format.indexOf(" %H:%M")!=-1) format += " %H:%M";
  else if (str_format.indexOf(" %H.%M.%S")!=-1) format += " %H.%M.%S";
  else if (str_format.indexOf(" %H.%M")!=-1) format += " %H.%M";
  return format;
}

/**
* Shows a calendar window
* @param {String} id A reference to the textbox that will hold the returned value
* @param {String} value Starting value of the calendar
* @param {Boolean} debug
* @param {Boolean} format
* @param {Boolean} showsTime
* @param {Boolean} showsOtherMonths
* @returns
* @type Boolean
*/
function showCalendar(id, value, debug, format, showsTime, showsOtherMonths) {
  //var el = document.getElementById(id);
  var el = eval("document." + id);
  if (showsTime==null) showsTime = "";
  if (showsOtherMonths==null) showsOtherMonths = false;
  if (format==null || format=="") format = getDateFormat(el.getAttribute("displayformat"));
  else format = getDateFormat(format);
  if (format.indexOf(" %H:%M")!=-1) showsTime = "24";
  else if (format.indexOf(" %H.%M")!=-1) showsTime = "24";
  
  if (_dynarch_popupCalendar != null) {
    // we already have some calendar created
    _dynarch_popupCalendar.hide();                 // so we hide it first.
  } else {
    // first-time call, create the calendar.
    var cal = new Calendar(1, null, selected, closeHandler);
    // uncomment the following line to hide the week numbers
    cal.weekNumbers = false;
    if (typeof showsTime == "string" && showsTime!="") {
      cal.showsTime = true;
      cal.time24 = (showsTime == "24");
    }
    if (showsOtherMonths) {
      cal.showsOtherMonths = true;
    }
    _dynarch_popupCalendar = cal;                  // remember it in the global var
    cal.setRange(1900, 2070);        // min/max year allowed.
    cal.create();
  }
  dateFormat = format;
  _dynarch_popupCalendar.setDateFormat(format);    // set the specified date format
  _dynarch_popupCalendar.parseDate(el.value);      // try to parse the text in field
  _dynarch_popupCalendar.sel = el;                 // inform it what input field we use

  // the reference element that we pass to showAtElement is the button that
  // triggers the calendar.  In this example we align the calendar bottom-right
  // to the button.
  _dynarch_popupCalendar.showAtElement(el, "Br");        // show the calendar

  return false;
}

/**
* Compares two dates based on the given format.
* @param {String} date1 First date to compare.
* @param {String} date2 Second date to compare.
* @param {String} String format of the date.
* @returns null when a null or an empty String is passed as parameter, returns -1 if date1 < date 2, returns 0 if date 1 = date2 or 1 if date1 > date2
* @type Number
*/
function datecmp(date1, date2, fmt) {
  if (date1==null || date1 == "") return null;
  else if (date2==null || date2 == "") return null;
  fmt = getDateFormat(fmt);
  var mydate1 = Date.parseDate(date1, fmt);
  var mydate2 = Date.parseDate(date2, fmt);
  if (mydate1==null || mydate1=="" || mydate2==null || mydate2=="") return null;
  if (mydate1.getFullYear() > mydate2.getFullYear()) return 1;
  else if (mydate1.getFullYear() == mydate2.getFullYear()) {
    if (mydate1.getMonth() > mydate2.getMonth()) return 1;
    else if (mydate1.getMonth() == mydate2.getMonth()) {
      if (mydate1.getDate() > mydate2.getDate()) return 1;
      else if (mydate1.getDate() == mydate2.getDate()) {
        if (mydate1.getHours() > mydate2.getHours()) return 1;
        else if (mydate1.getHours() == mydate2.getHours()) {
          if (mydate1.getMinutes() > mydate2.getMinutes()) return 1;
          else if (mydate1.getMinutes() == mydate2.getMinutes()) return 0;
          else return -1;
        } else return -1;
      } else return -1;
    } else return -1;
  } else return -1;
}

/**
* Returns the number of digtis based on the given parameter (part of a date)
* @param {String} formatType The part of the date to evaluate
* @returns 4 when 'Y', 2 when 'm', 2 when 'd', otherwise 2. 
* @type Number
*/
function checkFormat(formatType) {
  switch (formatType) {
    case 'Y': return 4;
    case 'm': return 2;
    case 'd': return 2;
    default: return 2;
  }
  return 0;
}

/**
* Returns an Array by splitting a String from the % character 
* @param {String} format String to split
* @returns An Array with the splitted elements
* @type Array
*/
function getSeparators(format) {
  if (format==null || format.length==0) return null;
  var result = new Array();
  var pos = format.indexOf("%");
  var last = 0;
  var i=0;
  while (pos!=-1) {
    if (pos>last) {
      result[i++] = format.substring(last, pos);
    }
    last = pos+2;
    pos = format.indexOf("%", last);
  }
  if (last < format.length) result[i] = format.substring(last);
  return result;
}

/**
* Search for a text in an Array
* @param {Array} obj The array of elements
* @param {String} text The text to look for
* @returns True if is found, otherwise false.
* @type Boolean
*/
function isInArray(obj, text) {
  if (obj==null || obj.length==0) return false;
  if (text==null || text.length==0) return false;
  var total = obj.length;
  for (var i = 0;i<total;i++) {
    if (obj[i].toUpperCase()==text.toUpperCase()) return true;
  }
  return false;
}

/**
* Opens the Openbravo's about window
*/
function about() {
  var complementosNS4 = ""

  var strHeight=500;
  var strWidth=600;
  var strTop=parseInt((screen.height - strHeight)/2);
  var strLeft=parseInt((screen.width - strWidth)/2);
  if (navigator.appName.indexOf("Netscape"))
    complementosNS4 = "alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ";
  var complementos = complementosNS4 + "height=" + strHeight + ", width=" + strWidth + ", left=" + strLeft + ", top=" + strTop + ", screenX=" + strLeft + ", screenY=" + strTop + ", location=0, resizable=yes, scrollbars=yes, status=0, toolbar=0, titlebar=0";
  var winPopUp = window.open(baseDirection + "../ad_forms/about.html", "ABOUT", complementos);
  if (winPopUp!=null) {
    winPopUp.focus();
    document.onunload = function(){winPopUp.close();};
    document.onmousedown = function(){winPopUp.close();};
  }
  return winPopUp;
}
/**
* Function Description
* @param {Boolean} isOnResize
*/

function resizeArea(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var mnu = document.getElementById("client");

  var mleft = document.getElementById("tdLeftTabsBars");
  var mleftSeparator = document.getElementById("tdleftSeparator");
  var mright = document.getElementById("tdrightSeparator");
  var mtop = document.getElementById("tdtopNavButtons");
  var mtopToolbar = document.getElementById("tdToolBar");
  var mtopTabs = document.getElementById("tdtopTabs");
  var mbottombut = document.getElementById("tdbottomButtons");
  var mbottom = document.getElementById("tdbottomSeparator");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  mnu.style.width = w - ((mleft?mleft.clientWidth:0) + (mleftSeparator?mleftSeparator.clientWidth:0) + (mright?mright.clientWidth:0)) - ((name.indexOf("Microsoft")==-1)?2:0);
  mnu.style.height = h -((mtop?mtop.clientHeight:0) + (mtopToolbar?mtopToolbar.clientHeight:0) + (mtopTabs?mtopTabs.clientHeight:0) + (mbottom?mbottom.clientHeight:0) + (mbottombut?mbottombut.clientHeight:0)) - ((name.indexOf("Microsoft")==-1)?1:0);
  var mbottomButtons = document.getElementById("tdbottomButtons");
  if (mbottomButtons) mbottomButtons.style.width = w - ((mleft?mleft.clientWidth:0) + (mleftSeparator?mleftSeparator.clientWidth:0) + (mright?mright.clientWidth:0)) - ((name.indexOf("Microsoft")==-1)?2:0);

/*  try {
    dojo.addOnLoad(dojo.widget.byId('grid').onResize);
  } catch (e) {}*/
  try {
    if (isOnResize) dojo.widget.byId('grid').onResize();
  } catch (e) {}
  mnu.style.display = "";
}

/**
* Function Description
*/
function resizeAreaHelp() {
  var mnu = document.getElementById("client");
  var mnuIndex = document.getElementById("clientIndex");
  var mTopSeparator = document.getElementById("tdSeparator");
  var mTopNavigation = document.getElementById("tdNavigation");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
//  mnu.style.width = w - 18 - ((name.indexOf("Microsoft")==-1)?2:0);
  mnu.style.height = h -(mTopSeparator.clientHeight + mTopNavigation.clientHeight) - 2;
  mnuIndex.style.height = mnu.style.height;

  mnu.style.display = "";
  mnuIndex.style.display = "";
}

/**
* Function Description
*/
function resizeAreaUserOps() {
  var mnu = document.getElementById("client");
  var mnuIndex = document.getElementById("clientIndex");
  var mTopSeparator = document.getElementById("tdSeparator");
  var mVerSeparator = document.getElementById("tdVerSeparator");
  var mTopNavigation = document.getElementById("tdNavigation");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
//  mnu.style.width = w - 18 - ((name.indexOf("Microsoft")==-1)?2:0);
  mnu.style.height = h -(mTopSeparator.clientHeight + mTopNavigation.clientHeight) - 2;
  mnuIndex.style.height = mnu.style.height;

  mnuIndex.style.display = "";

  mnu.style.width= w - (mVerSeparator.clientWidth + mnuIndex.clientWidth) - 2;

  mnu.style.display = "";
}

/**
* Function Description
*/
function resizeAreaInfo(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var table_header = document.getElementById("table_header");
  var client_top = document.getElementById("client_top");
  var client_middle = document.getElementById("client_middle");
  var client_bottom = document.getElementById("client_bottom");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  client_middle.style.height = h -((table_header?table_header.clientHeight:0) + (client_top?client_top.clientHeight:0) + (client_bottom?client_bottom.clientHeight:0)) - ((name.indexOf("Microsoft")==-1)?1:0);

  try {
    if (isOnResize) dojo.widget.byId('grid').onResize();
  } catch (e) {}
}

/**
* Function Description
*/
function resizePopup() {
  var mnu = document.getElementById("client");
  var table_header = document.getElementById("table_header");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  mnu.style.height = h -(table_header?table_header.clientHeight:0);
  mnu.style.width = w;
  mnu.style.display = "";
}

/**
* Function Description
*/
function calculateMsgBoxWidth() {
  var client_width = document.getElementById("client").clientWidth;
  var msgbox_table = document.getElementById("messageBoxID");
  msgbox_table.style.width = client_width;
 }
 
/**
* Change the status for show audit in Edition mode, in local javascript variable and in session value (with ajax)
**/
function changeAuditStatus() {
  if (strShowAudit=="Y") strShowAudit="N";
  else strShowAudit="Y";
  displayLogic();
  changeAuditIcon(strShowAudit);
  submitXmlHttpRequest(xx, null, 'CHANGE', "../utility/ChangeAudit", false);
  return true;
}

/**
* Change the status for show audit in Relation mode, in local javascript variable and in session value (with ajax)
**/
function changeAuditStatusRelation() {
  
  submitXmlHttpRequest(document.getElementById("buttonRefresh").onclick, null, 'CHANGE', "../utility/ChangeAudit", false);
  return true;
}

function changeAuditIcon(newStatus) {
  obj = document.getElementById("linkButtonAudit");
  if (obj == null) return false;
  obj.className="Main_ToolBar_Button"+(newStatus=="Y"?"_Selected":"");
  obj.title=""; //TODO: get the correct title depending on the language
}
/*if (!document.all)
  document.captureEvents(Event.MOUSEDOWN);
document.onmousedown=menuContextual;*/

//-->









/**
* Start of deprecated functions in 2.40
*/

var gBotonPorDefecto;
var arrTeclas=null;

/**
* Deprecated in 2.40: Set the focus on the first visible control in the form
* @param {Form} Formulario Optional- Defines the form containing the field, where we want to set the focus. If is not present, the first form of the page will be used.
* @param {String} Campo Optional - Name of the control where we want to set the focus. If is not present the first field will be used.
*/
function focoPrimerControl(Formulario, Campo) {
  var encontrado = false;
  if (Formulario==null) Formulario=document.forms[0];
  var total = Formulario.length;
  for(var i=0;i<total; i++)
  {
    if ((Formulario.elements[i].type != "hidden") && (Formulario.elements[i].type != "button") && (Formulario.elements[i].type != "submit") && (Formulario.elements[i].type != "image") && (Formulario.elements[i].type != "reset")) 
    { 
      if(Campo!=null) {
        if (Campo == Formulario.elements[i].name && !Formulario.elements[i].readonly && !Formulario.elements[i].disabled) {
          Formulario.elements[i].focus();
          encontrado=true;
          break;
        }
      } else if (!Formulario.elements[i].readonly && !Formulario.elements[i].disabled) {
        try {
          Formulario.elements[i].focus();
          encontrado=true;
          break;
        } catch (ignore) {}
      }
    }
  }
  if (encontrado && Formulario.elements[i].type && Formulario.elements[i].type.indexOf("select")==-1)
    Formulario.elements[i].select();
}

/**
* Deprecated in 2.40: Handles window events. This function handles events such as KeyDown; when a user hit the ENTER key to do somethig by default.
* @param {Number} CodigoTecla ASCII code of the key pressed.
* @returns True if the key pressed is not ment to be handled. False if is a handled key. 
* @type Boolean
*/
function pulsarTecla(CodigoTecla) {
  if (gBotonPorDefecto!=null)
  {
    var tecla = (!CodigoTecla) ? window.event.keyCode : CodigoTecla.which;
    if (tecla == 13)
    {
      eval(gBotonPorDefecto);
      return false;
    }
  }
  return true;
}

/**
* Deprecated in 2.40: Defines a defult action on each page, the one that will be executed when the user hit the ENTER key. This function is shared in pages containing frames.
* @param {String} accion Default command to be executed when the user hit the ENTER key.
* @returns Always retrun true.
* @type Boolean
* @see #pulsarTecla
*/
function porDefecto(accion) {
  gBotonPorDefecto = accion;
  if (!document.all)
  {
    document.captureEvents(Event.KEYDOWN);
  }
  document.onkeydown=pulsarTecla;
  return true;
}

/**
* Deprecated in 2.40: Builds the keys array on each screen. Each key that we want to use should have this structure.
* @param {Sting} tecla A text version of the handled key.
* @param {String} evento Event that we want to fire when the key is is pressed.
* @param {String} campo Name of the field on the window. If is null, is a global event, for the hole window.
* @param {String} teclaAuxiliar Text defining the auxiliar key. The value could be CTRL for the Control key, ALT for the Alt, null if we don't have to use an auxiliar key.
*/
function Teclas(tecla, evento, campo, teclaAuxiliar) {
  this.tecla = tecla;
  this.evento = evento;
  this.campo = campo;
  this.teclaAuxiliar = teclaAuxiliar;
}

/**
* Deprecated in 2.40: Returns the ASCII code of the given key
* @param {String} codigo Text version of a key
* @returns The ASCII code of the key
* @type Number
*/
function obtenerCodigoTecla(codigo) {
  if (codigo==null) return 0;
  else if (codigo.length==1) return codigo.toUpperCase().charCodeAt(0);
  switch (codigo.toUpperCase()) {
    case "BACKSPACE": return 8;
    case "TAB": return 9;
    case "ENTER": return 13;
    case "SPACE": return 32;
    case "DELETE": return 46;
    case "INSERT": return 45;
    case "END": return 35;
    case "HOME": return 36;
    case "REPAGE": return 33;
    case "AVPAGE": return 34;
    case "LEFTARROW": return 37;
    case "RIGHTARROW": return 39;
    case "UPARROW": return 38;
    case "DOWNARROW": return 40;
    case "NEGATIVE": return 189;
    case "NUMBERNEGATIVE": return 109;
    case "DECIMAL": return 190;
    case "NUMBERDECIMAL": return 110;
    case "ESCAPE": return 27;
    case "F1": return 112;
    case "F2": return 113;
    case "F3": return 114;
    case "F4": return 115;
    case "F5": return 116;
    case "F6": return 117;
    case "F7": return 118;
    case "F8": return 119;
    case "F9": return 120;
    case "F10": return 121;
    case "F11": return 122;
    case "F12": return 123;
    case "P": return 80;
/*    case "shiftKey": return 16;
    case "ctrlKey": return 17;
    case "altKey": return 18;*/
    default: return 0;
  }
}

/**
* Deprecated in 2.40: Handles the events execution of keys pressed, based on the events registered in the arrTeclas global array.   
* @param {Event} CodigoTecla Code of the key pressed.
* @returns True if the key is not registered in the array, false if a event for this key is registered in arrTeclas array.
* @type Boolean
* @see #obtenerCodigoTecla
*/
function controlTecla(CodigoTecla) {
  if (arrTeclas==null || arrTeclas.length==0) return true;
  if (!CodigoTecla) CodigoTecla = window.event;
  var tecla = window.event ? CodigoTecla.keyCode : CodigoTecla.which;
  var target = (CodigoTecla.target?CodigoTecla.target: CodigoTecla.srcElement);
  //var target = (document.layers) ? CodigoTecla.target : CodigoTecla.srcElement;
  var total = arrTeclas.length;
  for (var i=0;i<total;i++) {
    if (arrTeclas[i]!=null && arrTeclas[i]) {
      if (tecla == obtenerCodigoTecla(arrTeclas[i].tecla)) {
        if (arrTeclas[i].teclaAuxiliar==null || arrTeclas[i].teclaAuxiliar=="" || arrTeclas[i].teclaAuxiliar=="null") {
          if (arrTeclas[i].campo==null || (target!=null && target.name!=null && isIdenticalField(arrTeclas[i].campo, target.name))) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          }
        } else if (arrTeclas[i].campo==null || (target!=null && target.name!=null && isIdenticalField(arrTeclas[i].campo, target.name))) {
          if (arrTeclas[i].teclaAuxiliar=="ctrlKey" && CodigoTecla.ctrlKey && !CodigoTecla.altKey && !CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          } else if (arrTeclas[i].teclaAuxiliar=="altKey" && !CodigoTecla.ctrlKey && CodigoTecla.altKey && !CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          } else if (arrTeclas[i].teclaAuxiliar=="shiftKey" && !CodigoTecla.ctrlKey && !CodigoTecla.altKey && CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          }
        }
      }
    }
  }
  return true;
}


/**
* Deprecated in 2.40: Used to activate the key-press handling. Must be called after set the keys global array <em>arraTeclas</em>.
*/
function activarControlTeclas() {
  if (arrTeclas==null || arrTeclas.length==0) return true;

    var agt=navigator.userAgent.toLowerCase();

/*   if (agt.indexOf('gecko') != -1) { 
        document.releaseEvents(Event.KEYDOWN);
     }
  if (agt.indexOf('gecko') != -1)
    document.captureEvents(Event.KEYDOWN);*/
  
  document.onkeydown=controlTecla;
  return true;
}

/**
* Deprecated in 2.40: Function Description
* Shows or hides a window in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function mostrarMenu(id) {
  if (!top.frameMenu) window.open(baseFrameServlet, "_blank");
  else {
    var frame = top.document;
    var frameset = frame.getElementById("framesetMenu");
    if (!frameset) return false;
    /*try {
      var frm2 = frame.getElementById("frameMenu");
      var obj = document.onresize;
      var obj2 = frm2.onresize;
      document.onresize = null;
      frm2.document.onresize = null;
      progressiveHideMenu("framesetMenu", 30);
      document.onresize = obj;
      frm2.document.onresize = obj2;
    } catch (e) {*/
      if (frameset.cols.substring(0,1)=="0") frameset.cols = "25%,*";
      else  frameset.cols = "0%,*";
    //}
    try {
      changeClass(id, "_hide", "_show");
    } catch (e) {}
    return true;
  }
}

/**
* Deprecated in 2.40: Handles the onKeyDown and onKeyUp event, for an specific numeric typing control.
* @param {Object} obj Field where the numeric typing will be evaluated.
* @param {Boolean} bolDecimal Defines if a float number is allowed.
* @param {Boolean} bolNegativo Defines if a negative number is allowed.
* @param {Event} evt The event handling object associated with the field.
* @returns True if is an allowed number, otherwise false.
* @type Boolean
* @see #obtenerCodigoTecla
*/
function auto_completar_numero(obj, bolDecimal, bolNegativo, evt) {
  var numero;
  if (document.all) evt = window.event;
  if (document.layers) { numero = evt.which; }
  if (document.all)    { numero = evt.keyCode;}
  if (numero != obtenerCodigoTecla("ENTER") && numero != obtenerCodigoTecla("LEFTARROW") && numero != obtenerCodigoTecla("RIGHTARROW") && numero != obtenerCodigoTecla("UPARROW") && numero != obtenerCodigoTecla("DOWNARROW") && numero != obtenerCodigoTecla("DELETE") && numero != obtenerCodigoTecla("BACKSPACE") && numero != obtenerCodigoTecla("END") && numero != obtenerCodigoTecla("HOME") && !evt["ctrlKey"]) {
    if (numero>95 && numero <106) { //Teclado numérico
      numero = numero - 96;
      if(isNaN(numero)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (numero!=obtenerCodigoTecla("DECIMAL") && numero != obtenerCodigoTecla("NUMBERDECIMAL") && numero != obtenerCodigoTecla("NEGATIVE") && numero != obtenerCodigoTecla("NUMBERNEGATIVE")) { //No es "-" ni "."
      numero = String.fromCharCode(numero);
      if(isNaN(numero)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (numero==obtenerCodigoTecla("DECIMAL") || numero==obtenerCodigoTecla("NUMBERDECIMAL")) { //Es "."
      if (bolDecimal) {
        if (obj.value==null || obj.value=="") return true;
        else {
          var point = obj.value.indexOf(".");
          if (point != -1) {
            point = obj.value.indexOf(".", point+1);
            if (point==-1) return true;
          } else return true;
        }
      }
      if (document.all) evt.returnValue = false;
      return false;
    } else { //Es "-"
      if (bolNegativo && (obj.value==null || obj.value.indexOf("-")==-1)) return true;
      if (document.all) evt.returnValue = false;
      return false;
    }
  }
  return true;
}

/**
* End of deprecated functions in 2.40
*/