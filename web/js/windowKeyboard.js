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

var focusedWindowElement = null;
var focusedWindowElement_tmp = null;
var focusedWindowElement_tmp2 = null;
var windowTableParentElement = null;
var focusedWindowTable = 0;
var frameLocked = false;
var currentWindowElementType = null;
var previousWindowElementType = null;

var selectedArea = 'window';
var isGridFocused = null;
var isClickOnGrid = null;
var fixFocusedElement = null;
var fixFocusedElementArrayPosition = null;

var isTabPressed = null;

var isGoingDown = null;
var isGoingUp = null;

document.onclick=cursorFocus;
//document.onfocus=activeElementFocus;

if (document.layers) {
  window.captureEvents(Event.ONCLICK);
  window.onclick=cursorFocus;
  //window.captureEvents(Event.FOCUS);
  //window.onfocus=activeElementFocus;
}

function activateFixFocus(){
  if (fixFocusedElement != null || fixFocusedElement != 'null' || fixFocusedElement != '') {
    keyArray[fixFocusedElementArrayPosition] = new keyArrayItem("ENTER", "executeWindowButton(fixFocusedElement.getAttribute('id'));", null, null, false, 'onkeydown');
    drawWindowElementFocus(fixFocusedElement);
  }
}

function disableFixFocus() {
  if (fixFocusedElement != null || fixFocusedElement != 'null' || fixFocusedElement != '') {
    keyArray[fixFocusedElementArrayPosition] = new keyArrayItem(null, null, null, null, false, null);
    eraseWindowElementFocus(fixFocusedElement);
  }
}

function fixFocus(id) {
  fixFocusedElement = document.getElementById(id);
  fixFocusedElementArrayPosition = keyArray.length
  activateFixFocus();
}

function fixFocusLogic(obj) {
  try {
    if (obj.tagName == 'INPUT' || obj.tagName == 'SELECT') {
      activateFixFocus();
      for (var i = 0; i < keyArray.length; i++) {
        if (keyArray[i] != null && keyArray[i]) {
          if (keyArray[i].key == 'ENTER') {
            if (keyArray[i].auxKey == null || keyArray[i].auxKey == '' || keyArray[i].auxKey == 'null') {
              if (keyArray[i].field == obj.getAttribute('name')) {
                disableFixFocus();
                break;
              }
            }
          }
        }
      }
    } else {
      disableFixFocus();
    }
  } catch(e) {}
}

function activeElementFocus() {
  if (focusedWindowElement && selectedArea=='window') putWindowElementFocus(focusedWindowElement);
}

function setSelectedArea(area) {
  selectedArea = area;
}

function swichSelectedArea() {
  if (selectedArea=='window' && tabsTables[0].tabTableId) {
    focusedWindowElement_tmp2 = focusedWindowElement;
    selectedArea = 'tabs';
    removeWindowElementFocus(focusedWindowElement);
    setActiveTab();
  } else if (selectedArea=='tabs') {
    selectedArea = 'window';
    setWindowElementFocus(focusedWindowElement_tmp2);
  } else {
    return false;
  }
}

function windowTableId(tableId, frameName) {
  this.tableId = tableId;
  this.frameName = frameName;  
}


function setWindowTableParentElement() {
  windowTableParentElement = windowTables[focusedWindowTable].tableId;
}



function cursorFocus(evt, obj) {
  if(obj == null) {
    obj = (!document.all) ? evt.target : event.srcElement;
  }
  if(!isClickOnGrid==true) blurGrid();
  isClickOnGrid=false;
  setSelectedArea('window');
  if (isInsideWindowTable(obj) && couldHaveFocus(obj)) {
    frameLocked = false;
    //removeWindowElementFocus(focusedWindowElement);
    focusedWindowElement = obj;
    setWindowElementFocus(focusedWindowElement);
  } else {
    setWindowElementFocus(focusedWindowElement);
    //focusedWindowElement = document.getElementById(windowTables[0].tableId);
    //focusedWindowTable = 0;
    //setWindowTableParentElement();
  }
  //alert(obj.getAttribute('id'));
}

function isInsideWindowTable(obj) {
  try {
    for(;;) {
      obj=obj.parentNode;
      for(var i=0;i<windowTables.length;i++) {
        if (obj==document.getElementById(windowTables[i].tableId)) {
          focusedWindowTable = i;
          setWindowTableParentElement();
          return true;
        } 
      }
    }
  } catch(e) {
    return false;
  }
}

function setWindowElementFocus(obj, type) {
  if (type == null || type == 'null' || type == '' || type == 'obj') {
    if (obj == 'firstElement') {
      setFirstWindowElementFocus();
    } else if (obj == 'lastElement') {
      setLastWindowElementFocus();
    } else {
      removeWindowElementFocus(focusedWindowElement_tmp);
      focusedWindowElement = obj;
    focusedWindowElement_tmp = focusedWindowElement;
      if(!frameLocked)
      putWindowElementFocus(focusedWindowElement);
    }
  } else if (type == 'id') {
    obj = document.getElementById(obj);
    removeWindowElementFocus(focusedWindowElement_tmp);
    focusedWindowElement = obj;
    focusedWindowElement_tmp = focusedWindowElement;
    if(!frameLocked) putWindowElementFocus(obj);
    putWindowElementFocus(focusedWindowElement);    
  }
}

function drawWindowElementFocus(obj) {
  try {
    if(obj.tagName == 'A') {
      if (obj.className.indexOf(' Popup_Client_Help_LabelLink_focus') == -1 && obj.className.indexOf('Popup_Client_Help_LabelLink') != -1) {
        obj.className = obj.className + ' Popup_Client_Help_LabelLink_focus';
      } else if (obj.className.indexOf(' Popup_Client_UserOps_LabelLink_Selected_focus') == -1 && obj.className.indexOf('Popup_Client_UserOps_LabelLink_Selected') != -1) {
        obj.className = obj.className + ' Popup_Client_UserOps_LabelLink_Selected_focus';
      } else if (obj.className.indexOf(' Popup_Client_UserOps_LabelLink_focus') == -1 && obj.className.indexOf('Popup_Client_UserOps_LabelLink') != -1) {
        obj.className = obj.className + ' Popup_Client_UserOps_LabelLink_focus';
      } else if (obj.className.indexOf(' LabelLink_noicon_focus') == -1 && obj.className.indexOf('LabelLink_noicon') != -1) {
        obj.className = obj.className + ' LabelLink_noicon_focus';
      } else if (obj.className.indexOf('LabelLink_focus') == -1 && obj.className.indexOf('LabelLink') != -1) {
        obj.className = obj.className + ' LabelLink_focus';
      } else if (obj.className.indexOf('FieldButtonLink_focus') == -1 && obj.className.indexOf('FieldButtonLink') != -1) {
        obj.className = 'FieldButtonLink_focus';
      } else if (obj.className.indexOf('ButtonLink_focus') == -1 && obj.className.indexOf('ButtonLink') != -1) {
        obj.className = 'ButtonLink_focus';
      } else if (obj.className.indexOf('List_Button_TopLink_focus') == -1 && obj.className.indexOf('List_Button_TopLink') != -1) {
        obj.className = 'List_Button_TopLink_focus';
      } else if (obj.className.indexOf('List_Button_MiddleLink_focus') == -1 && obj.className.indexOf('List_Button_MiddleLink') != -1) {
        obj.className = 'List_Button_MiddleLink_focus';
      } else if (obj.className.indexOf('List_Button_BottomLink_focus') == -1 && obj.className.indexOf('List_Button_BottomLink') != -1) {
        obj.className = 'List_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_LeftRight_Button_TopLink_focus') == -1 && obj.className.indexOf('Dimension_LeftRight_Button_TopLink') != -1) {
        obj.className = 'Dimension_LeftRight_Button_TopLink_focus';
      } else if (obj.className.indexOf('Dimension_LeftRight_Button_BottomLink_focus') == -1 && obj.className.indexOf('Dimension_LeftRight_Button_BottomLink') != -1) {
        obj.className = 'Dimension_LeftRight_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_UpDown_Button_BottomLink_focus') == -1 && obj.className.indexOf('Dimension_UpDown_Button_BottomLink') != -1) {
        obj.className = 'Dimension_UpDown_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_UpDown_Button_TopLink_focus') == -1 && obj.className.indexOf('Dimension_UpDown_Button_TopLink') != -1) {
        obj.className = 'Dimension_UpDown_Button_TopLink_focus';
      }
    } else if (obj.tagName == 'SELECT') {
      if (obj.className.indexOf(' Combo_focus') == -1) {
        obj.className = obj.className + ' Combo_focus';
      }
    } else if (obj.tagName == 'INPUT') {
      if ((obj.className.indexOf(' TextBox_focus') == -1) &&
      (obj.className.indexOf('dojoValidateEmpty') != -1 ||
      obj.className.indexOf('dojoValidateValid') != -1 ||
      obj.className.indexOf('dojoValidateInvalid') != -1 ||
      obj.className.indexOf('dojoValidateRange') != -1)) {
        obj.className = obj.className + ' TextBox_focus';
      } else if (obj.getAttribute('type') == 'checkbox') {
        obj.className = 'Checkbox_Focused';
        var obj_tmp = obj;
        try {
          for (;;) {
            if (obj_tmp.getAttribute('class') == 'Checkbox_container_NOT_Focused') {
              obj_tmp.className = 'Checkbox_container_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      } else if (obj.getAttribute('type') == 'radio') {
        obj.className='Radio_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Radio_container_NOT_Focused') {
              obj_tmp.className='Radio_container_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      }
    } else if (obj.tagName == 'TEXTAREA') {
      if ((obj.className.indexOf(' TextBox_focus') == -1)
      && (obj.className.indexOf('dojoValidateEmpty') != -1
      || obj.className.indexOf('dojoValidateValid') != -1
      || obj.className.indexOf('dojoValidateInvalid') != -1
      || obj.className.indexOf('dojoValidateRange') != -1)) {
        obj.className = obj.className + ' TextBox_focus';
      }
    } else if (currentWindowElementType == 'grid') {
    } else {
    }
  } catch (e) {
  }
}

function putWindowElementFocus(obj) {
  previousWindowElementType=currentWindowElementType;
  drawWindowElementFocus(obj);
  fixFocusLogic(obj);
  try {
    if (currentWindowElementType == 'grid') {
      focusGrid();
    } else {
      obj.focus();
    }
  } catch (e) {
  }
}

function eraseWindowElementFocus(obj) {
  try {
    if(obj.tagName == 'A') {
      obj.className = obj.className.replace(' Popup_Client_UserOps_LabelLink_focus','');
      obj.className = obj.className.replace(' Popup_Client_UserOps_LabelLink_Selected_focus','');
      obj.className = obj.className.replace(' Popup_Client_Help_LabelLink_focus','');
      obj.className = obj.className.replace(' LabelLink_focus','');
      obj.className = obj.className.replace(' LabelLink_noicon_focus','');
      obj.className = obj.className.replace('FieldButtonLink_focus','FieldButtonLink');
      obj.className = obj.className.replace('ButtonLink_focus','ButtonLink');
      obj.className = obj.className.replace('List_Button_TopLink_focus','List_Button_TopLink');
      obj.className = obj.className.replace('List_Button_MiddleLink_focus','List_Button_MiddleLink');
      obj.className = obj.className.replace('List_Button_BottomLink_focus','List_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_LeftRight_Button_TopLink_focus','Dimension_LeftRight_Button_TopLink');
      obj.className = obj.className.replace('Dimension_LeftRight_Button_BottomLink_focus','Dimension_LeftRight_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_UpDown_Button_BottomLink_focus','Dimension_UpDown_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_UpDown_Button_TopLink_focus','Dimension_UpDown_Button_TopLink');
    } else if (obj.tagName == 'SELECT') {
      obj.className = obj.className.replace(' Combo_focus','');
    } else if (obj.tagName == 'INPUT') {
      obj.className = obj.className.replace(' TextBox_focus','');
      if (obj.getAttribute('type')=='checkbox') {
        obj.className='Checkbox_NOT_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Checkbox_container_Focused') {
              obj_tmp.className='Checkbox_container_NOT_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      } else if (obj.getAttribute('type')=='radio') {
        obj.className='Radio_NOT_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Radio_container_Focused') {
              obj_tmp.className='Radio_container_NOT_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      }
    } else if (obj.tagName == 'TEXTAREA') {
      obj.className = obj.className.replace(' TextBox_focus','');
    } else if (previousWindowElementType == 'grid') {
      blurGrid();
    } else {
    }
  } catch (e) {
  }
}

function removeWindowElementFocus(obj) {
  eraseWindowElementFocus(obj);
  try {
    if (previousWindowElementType == 'grid') {
      blurGrid();
    } else {
      //obj.blur();
    }
  } catch (e) {
  }
}

function mustBeJumped(obj) {
  if (obj.style.display == 'none') return true;
  return false;
}

function mustBeIgnored(obj) {
  if (obj.style.display == 'none') return true;
  if (obj.getAttribute('type') == 'hidden') return true;
  if (obj.getAttribute('readonly') == 'true') return true;
  if (obj.readOnly) return true;
  if (obj.getAttribute('disabled') == 'true') return true;
  if (obj.disabled) return true;
  return false;
}

function canHaveFocus(obj) {
  //alert('chequeo');
  if (mustBeIgnored(obj)) return false;
  if (couldHaveFocus(obj)) return true;
  return false;
}

function couldHaveFocus(obj) {
  if (obj.tagName == 'INPUT') {
    currentWindowElementType='input';
    return true;
  } 
  if (obj.tagName == 'A') {
    currentWindowElementType='a';
    return true;
  } 
  if (obj.tagName == 'SELECT') {
    currentWindowElementType = 'select';
    return true;
  }
  if (obj.tagName == 'TEXTAREA') {
    currentWindowElementType = 'textarea';
    return true;
  }
  try {
    if (obj.tagName == 'TABLE' && obj.getAttribute('id') == 'grid_table') {
      currentWindowElementType = 'grid';
      return true;
    }
  } catch(e) {
  }
  return false;
}

function getNextWindowElement() {
  //for
    //Mirar si tiene hijo
    //if
      //yes
        //Mirar si es destino
          //if
            //yes
              //Devolver
          //endif
      //no
        //for
          //Mirar si tiene hermano
            //if
              //yes
                //break
              //no
                //Subir al padre
                  //Mirar si el padre es origen
                    //Si
                      //Devolver primer elemento
                    //No

  var success = null;
  var nextElementTmp = null;
  var nextElement = focusedWindowElement;
  if (nextElement==null) {
    nextElement = getFirstWindowElement();
    return nextElement;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        //alert('con hijo - id del hijo: ' + nextElement.getAttribute('id'));
        if(canHaveFocus(nextElement)) return nextElement;
      } else {
        //alert('sin hijo, vamos a mirar si hay o no hermano');
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
            
          }
          if (success) {
            //alert('con hermano - id del hermano: ' + nextElement.getAttribute('id'));
            if(canHaveFocus(nextElement)) return nextElement;            
            break;
          } else {
            //alert('sin hermano, vamos a un nivel superior')
            nextElement = nextElement.parentNode
            //alert('id del hermano: ' + nextElement.getAttribute('id'));            
            if (nextElement==document.getElementById(windowTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              //alert('hemos llegado al padre!')
              goToNextWindowTable();
              return getCurrentWindowTableFirstElement();
            }
          }
        }
      }
    }
  }
}

function getPreviousWindowElement() {
  //for
    //Mirar si tiene hijo
    //if
      //yes
        //Mirar si es destino
          //if
            //yes
              //Devolver
          //endif
      //no
        //for
          //Mirar si tiene hermano previo
            //if
              //yes
                //break
              //no
                //Subir al padre
                  //Mirar si el padre es origen
                    //Si
                      //Devolver ultimo elemento
                    //No

  var success = null;
  var previousElementTmp = null;
  var previousElement = focusedWindowElement;
  if (previousElement==null) {
    previousElement = getLastWindowElement();
    return previousElement;
  } else {
    for(;;){
      previousElementTmp = previousElement;
      try {
        previousElement = previousElement.lastChild;
        for (;;) {
          for (;;) {
            if (previousElement.nodeType != '1') {
              previousElement = previousElement.previousSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(previousElement)) {
            break;
          } else {
            previousElement = previousElement.previousSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        previousElement=previousElementTmp;
      }
      if(success) {
        //alert('con hijo - id del hijo: ' + previousElement.getAttribute('id'));
        if(canHaveFocus(previousElement)) return previousElement;
      } else {
        //alert('sin hijo, vamos a mirar si hay o no hermano');
        for(;;) {
          previousElementTmp = previousElement;
          try {
            previousElement = previousElement.previousSibling;
            for (;;) {
              for (;;) {
                if (previousElement.nodeType != '1') {
                  previousElement = previousElement.previousSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(previousElement)) break;
              else previousElement = previousElement.previousSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            previousElement=previousElementTmp;
            
          }
          if (success) {
            //alert('con hermano - id del hermano: ' + previousElement.getAttribute('id'));
            if(canHaveFocus(previousElement)) return previousElement;            
            break;
          } else {
            //alert('sin hermano, vamos a un nivel superior')
            previousElement = previousElement.parentNode
            //alert('id del hermano: ' + previousElement.getAttribute('id'));            
            if (previousElement==document.getElementById(windowTableParentElement)) {
              //alert('hemos llegado al padre!')
              goToPreviousWindowTable();
              return getCurrentWindowTableLastElement();
            }
          }
        }
      }
    }
  }
}

function goToNextWindowTable() {

  var oldFrameName = windowTables[focusedWindowTable].frameName;
  if (focusedWindowTable < windowTables.length-1) {
    focusedWindowTable = focusedWindowTable + 1;
  } else {
    focusedWindowTable = 0;
  }
  var newFrameName = windowTables[focusedWindowTable].frameName;
  if (oldFrameName != newFrameName) {
    top.frames[oldFrameName].frameLocked = true;
    top.frames[newFrameName].frameLocked = false;
    top.frames[oldFrameName].focusedWindowTable = focusedWindowTable;
    top.frames[newFrameName].focusedWindowTable = focusedWindowTable;
    top.frames[newFrameName].setWindowTableParentElement();
    //alert(newFrameName + ' ' + top.frames[newFrameName].windowTableParentElement + ' ' + top.frames[newFrameName].focusedWindowTable + ' ' + top.frames[newFrameName].frameLocked);
    top.frames[newFrameName].setCurrentWindowTableFirstElementFocus();
  } else {
    setWindowTableParentElement();
  }
}

function goToPreviousWindowTable() {
  var oldFrameName = windowTables[focusedWindowTable].frameName;
  if (focusedWindowTable > 0) {
    focusedWindowTable = focusedWindowTable - 1;
  } else {
    focusedWindowTable = windowTables.length-1;
  }
  var newFrameName = windowTables[focusedWindowTable].frameName;  
  if (oldFrameName != newFrameName) {
    top.frames[oldFrameName].frameLocked = true;
    top.frames[newFrameName].frameLocked = false;
    top.frames[oldFrameName].focusedWindowTable = focusedWindowTable;
    top.frames[newFrameName].focusedWindowTable = focusedWindowTable;
    top.frames[newFrameName].setWindowTableParentElement();
    top.frames[newFrameName].setCurrentWindowTableLastElementFocus();
  } else {
    setWindowTableParentElement();
  }
}

function getCurrentWindowTableFirstElement() {
  focusedWindowElement = document.getElementById(windowTables[focusedWindowTable].tableId);
  var obj = getNextWindowElement();
  return obj;
}

function getCurrentWindowTableLastElement() {
  focusedWindowElement = document.getElementById(windowTables[focusedWindowTable].tableId);
  var obj = getPreviousWindowElement();
  return obj;
}


function getFirstWindowElement() {
  focusedWindowElement = document.getElementById(windowTables[0].tableId);
  focusedWindowTable = 0;
  var obj = getNextWindowElement();
  return obj;
}

function getLastWindowElement() {
  focusedWindowElement = document.getElementById(windowTables[windowTables.length-1].tableId);
  focusedWindowTable = windowTables.length-1;  
  var obj = getPreviousWindowElement();
  return obj;
}

function windowTabKey(state) {
  if (state==true) {
    isTabPressed = true;
    if (selectedArea == 'window') {
      var obj = getNextWindowElement();
      setWindowElementFocus(obj);
    } else if (selectedArea == 'tabs') {
      var obj = getNextTab();
      setTabFocus(obj);
    }
  } else {
    isTabPressed = false;
  }
  return false;
}

function windowShiftTabKey(state) {
  if (state==true) {
    isTabPressed = true;
    if (selectedArea == 'window') {
      var obj = getPreviousWindowElement();
      setWindowElementFocus(obj);
    } else if (selectedArea == 'tabs') {
      var obj = getPreviousTab();
      setTabFocus(obj);
    }
  } else {
    isTabPressed = false;
  }
  return false;
}

function setFirstWindowElementFocus() {
  var obj = getFirstWindowElement();
  setWindowElementFocus(obj);
}

function setLastWindowElementFocus() {
  var obj = getLastWindowElement();
  setWindowElementFocus(obj);
}

function setCurrentWindowTableFirstElementFocus() {
  var obj = getCurrentWindowTableFirstElement();
  setWindowElementFocus(obj); 
}

function setCurrentWindowTableLastElementFocus() {
  var obj = getCurrentWindowTableLastElement();
  setWindowElementFocus(obj);
}


// Tabs functions

var focusedTab = null;
var tabTableParentElement = null;
var focusedTabTable = 0;

function tabTableId(tabTableId) {
  this.tabTableId = tabTableId;
}

function isTabActive(obj) {
  if (obj.className.indexOf('Tabcurrent')!=-1) {
    return true;
  } else {
    return false;
  }
}


function getFirstTab() {
  focusedTab = document.getElementById(tabsTables[0].tabTableId);
  focusedTabTable = 0;
  var obj = getNextTab();
  return obj;
}

function getLastTab() {
  focusedTab = document.getElementById(tabsTables[tabsTables.length-1].tabTableId);
  focusedTabTable = tabsTables.length-1;  
  var obj = getPreviousTab();
  return obj;
}

function getNextTab() {
  //for
    //Mirar si tiene hijo
    //if
      //yes
        //Mirar si es destino
          //if
            //yes
              //Devolver
          //endif
      //no
        //for
          //Mirar si tiene hermano
            //if
              //yes
                //break
              //no
                //Subir al padre
                  //Mirar si el padre es origen
                    //Si
                      //Devolver primer elemento
                    //No

  var success = null;
  var nextElementTmp = null;
  var nextElement = focusedTab;
  if (nextElement==null) {
    nextElement = getActiveTab();
    return nextElement;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        //alert('con hijo - id del hijo: ' + nextElement.getAttribute('id'));
        if(canHaveFocus(nextElement)) return nextElement;
      } else {
        //alert('sin hijo, vamos a mirar si hay o no hermano');
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
            
          }
          if (success) {
            //alert('con hermano - id del hermano: ' + nextElement.getAttribute('id'));
            if(canHaveFocus(nextElement)) return nextElement;            
            break;
          } else {
            //alert('sin hermano, vamos a un nivel superior')
            nextElement = nextElement.parentNode
            //alert('id del hermano: ' + nextElement.getAttribute('id'));            
            if (nextElement==document.getElementById(tabTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              //alert('hemos llegado al padre!')
              goToNextTabs();
              return getFirstTab();
            }
          }
        }
      }
    }
  }
}

function getPreviousTab() {
  //for
    //Mirar si tiene hijo
    //if
      //yes
        //Mirar si es destino
          //if
            //yes
              //Devolver
          //endif
      //no
        //for
          //Mirar si tiene hermano previo
            //if
              //yes
                //break
              //no
                //Subir al padre
                  //Mirar si el padre es origen
                    //Si
                      //Devolver ultimo elemento
                    //No

  var success = null;
  var previousElementTmp = null;
  var previousElement = focusedTab;
  if (previousElement==null) {
    previousElement = getActiveTab();
    return previousElement;
  } else {
    for(;;){
      previousElementTmp = previousElement;
      try {
        previousElement = previousElement.lastChild;
        for (;;) {
          for (;;) {
            if (previousElement.nodeType != '1') {
              previousElement = previousElement.previousSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(previousElement)) {
            break;
          } else {
            previousElement = previousElement.previousSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        previousElement=previousElementTmp;
      }
      if(success) {
        //alert('con hijo - id del hijo: ' + previousElement.getAttribute('id'));
        if(canHaveFocus(previousElement)) return previousElement;
      } else {
        //alert('sin hijo, vamos a mirar si hay o no hermano');
        for(;;) {
          previousElementTmp = previousElement;
          try {
            previousElement = previousElement.previousSibling;
            for (;;) {
              for (;;) {
                if (previousElement.nodeType != '1') {
                  previousElement = previousElement.previousSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(previousElement)) break;
              else previousElement = previousElement.previousSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            previousElement=previousElementTmp;
            
          }
          if (success) {
            //alert('con hermano - id del hermano: ' + previousElement.getAttribute('id'));
            if(canHaveFocus(previousElement)) return previousElement;            
            break;
          } else {
            //alert('sin hermano, vamos a un nivel superior')
            previousElement = previousElement.parentNode
            //alert('id del hermano: ' + previousElement.getAttribute('id'));            
            if (previousElement==document.getElementById(tabTableParentElement)) {
              //alert('hemos llegado al padre!')
              goToPreviousTabs();
              return getLastTab();
            }
          }
        }
      }
    }
  }
}


function setTabFocus(obj, type) {
  if (type == null || type == 'null' || type == '' || type == 'obj') {
    if (obj == 'firstElement') {
      setFirstTabFocus();
    } else if (obj == 'lastElement') {
      setLastTabFocus();
    } else {
      focusedTab = obj;
      putWindowElementFocus(obj);
    }
  } else if (type == 'id') {
    obj = document.getElementById(obj);
    focusedTab = obj;
    putWindowElementFocus(obj);
  }
}

function setFirstTabFocus() {
  var obj = getFirstTab();
  setTabFocus(obj);
}

function setLastTabFocus() {
  var obj = getLastTab();
  setTabFocus(obj);
}

function goToNextTabs() {
  if (focusedTabTable < tabsTables.length-1) {
    focusedTabTable = focusedTabTable + 1;
  } else {
    focusedTabTable = 0;
  }
  setTabTableParentElement();
}

function goToPreviousTabs() {
  if (focusedTabTable > 0) {
    focusedTabTable = focusedTabTable - 1;
  } else {
    focusedTabTable = tabsTables.length-1;
  }
  setTabTableParentElement();
}

function setActiveTab() {
  var obj = getActiveTab();
  setTabFocus(obj);
}

function setTabTableParentElement() {
  tabTableParentElement = tabsTables[focusedTabTable].tabTableId;
}

function getActiveTab() {
  var obj = getActiveTabContainer();
  var focusedTab_tmp = focusedTab;
  focusedTab = obj;
  obj = getNextTab();
  focusedTab = focusedTab_tmp;
  return obj;
}

function getActiveTabContainer() {
  //for
    //Mirar si tiene hijo
    //if
      //yes
        //Mirar si es activa
          //if
            //yes
              //Devolver
          //endif
      //no
        //for
          //Mirar si tiene hermano
            //if
              //yes
                //break
              //no
                //Subir al padre
                  //Mirar si el padre es origen
                    //Si
                      //Devolver primer elemento
                    //No


  var success = null;
  var nextElementTmp = null;
  var nextElement = document.getElementById(tabsTables[0].tabTableId);
  if (nextElement==null) {
    return false;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        //alert('con hijo - id del hijo: ' + nextElement.getAttribute('id'));
        if(isTabActive(nextElement)) return nextElement;
      } else {
        //alert('sin hijo, vamos a mirar si hay o no hermano');
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
            
          }
          if (success) {
            //alert('con hermano - id del hermano: ' + nextElement.getAttribute('id'));
            if(isTabActive(nextElement)) return nextElement;            
            break;
          } else {
            //alert('sin hermano, vamos a un nivel superior')
            nextElement = nextElement.parentNode
            //alert('id del hermano: ' + nextElement.getAttribute('id'));            
            if (nextElement==document.getElementById(windowTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              //alert('hemos llegado al padre!')
              goToNextTabs();
              return false;
            }
          }
        }
      }
    }
  }
}

function focusGrid() {
  try {
    dojo.widget.byId('grid').focusGrid();
  } catch(e){}
}

function blurGrid() {
  try {
    dojo.widget.byId('grid').blurGrid();
  } catch(e){}
}

function windowUpKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToPreviousRow();
  }
}

function windowDownKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToNextRow();
  }
}

function windowHomeKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToFirstRow();
  }
}

function windowEndKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToLastRow();
  }
}

function windowAvpageKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToNextPage();
  }
}

function windowRepageKey() {
  if (isGridFocused) {
    dojo.widget.byId('grid').goToPreviousPage();
  }
}

function windowEnterKey() {
  if (isGridFocused) {
    onRowDblClick();
  }
}
