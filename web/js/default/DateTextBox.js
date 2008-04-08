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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

// Function to build the validation for text box
validateDateTextBox= function(/*String*/ id){
  isValidDateTextBox(id);
  var required = document.getElementById(id).getAttribute("required");
  if (required == "true") isMissingDateTextBox(id);
  isInRangeDateTextBox(id);
}

isValidDateTextBox= function(/*String*/ id){
  var isValid = this.isValidDate(document.getElementById(id).value, getDateFormat(document.getElementById(id).getAttribute("displayformat")));
  var element = document.getElementById(id+"invalidSpan");
  if (isValid)
    element.style.display="none";
  else
    element.style.display="";
}

isMissingDateTextBox= function(/*String*/ id){
  var isMissing = document.getElementById(id).value.length == 0;
  var element = document.getElementById(id+"missingSpan");
  if (isMissing)
    element.style.display="";
  else
    element.style.display="none";
}

isInRangeDateTextBox= function(/*String*/ id){
  var isInRange = true;
  var element = document.getElementById(id+"rangeSpan");
  if (isInRange)
    element.style.display="none";
  else
    element.style.display="";
}

isValidDate = function(/*String*/str_datetime, /*String*/str_dateFormat) {
  if (this.getDate(str_datetime,str_dateFormat)){ 
    return true 
  } else { 
    return false;
  }
}

getDate = function(/*String*/str_datetime, /*String*/str_dateFormat) { 
  var inputDate=new Date(0,0,0); 
  if (str_datetime.length == 0) return inputDate; 
  // datetime parsing and formatting routimes. modify them if you wish other datetime format 
  //function str2dt (str_datetime) { 
  var re_date = /^(\d+)[\-|\/|/|:|.|\.](\d+)[\-|\/|/|:|.|\.](\d+)$/; 
  if (!re_date.exec(str_datetime)) 
    return false; 
  if (!str_dateFormat) str_dateFormat = defaultDateFormat; 
  switch (str_dateFormat) { 
    case "%m-%d-%Y": 
    case "%m/%d/%Y": 
    case "%m.%d.%Y": 
    case "%m:%d:%Y": 
      if (RegExp.$2 < 1 || RegExp.$2 > 31) return false; 
      if (RegExp.$1 < 1 || RegExp.$1 > 12) return false; 
      if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false; 
      inputDate=new Date(parseFloat(RegExp.$3), parseFloat(RegExp.$1)-1, parseFloat(RegExp.$2)); 
      return inputDate; 
    case "%Y-%m-%d": 
    case "%Y/%m/%d": 
    case "%Y.%m.%d": 
    case "%Y:%m:%d": 
      if (RegExp.$3 < 1 || RegExp.$3 > 31) return false; 
      if (RegExp.$2 < 1 || RegExp.$2 > 12) return false; 
      if (RegExp.$1 < 1 || RegExp.$1 > 9999) return false; 
      inputDate=new Date(parseFloat(RegExp.$1), parseFloat(RegExp.$2)-1, parseFloat(RegExp.$3)); 
      return inputDate; 
    case "%d-%m-%Y": 
    case "%d/%m/%Y": 
    case "%d.%m.%Y": 
    case "%d:%m:%Y": 
    default: 
      if (RegExp.$1 < 1 || RegExp.$1 > 31) return false; 
      if (RegExp.$2 < 1 || RegExp.$2 > 12) return false; 
      if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false; 
      inputDate=new Date(parseFloat(RegExp.$3), parseFloat(RegExp.$2)-1, parseFloat(RegExp.$1)); 
      return inputDate; 
  } 
  return false; 
}


/****************************************
Test insertion of mask at inserting time
*****************************************/
function auto_complete_date(field, fmt) {
  if (!isTabPressed) {
    try {
    if (getCaretPosition(field).start != field.value.length) return; //If we are inserting in a position different from the last one, we don't autocomplete
    } catch (ignored) {}
    if (fmt == null || fmt == "") fmt = field.getAttribute("displayformat");
    fmt = getDateFormat(fmt);
    var strDate = field.value;
    var b = fmt.match(/%./g);
    var i = 0, j = -1;
    var text = "";
    var length = 0;
    var pos = fmt.indexOf(b[0]) + b[0].length;
    var separator = fmt.substring(pos, pos+1);
    var separatorH = "";
    pos = fmt.indexOf("%H");
    if (pos!=-1) separatorH = fmt.substring(pos + 2, pos + 3);
    while (strDate.charAt(i)) {
      if (strDate.charAt(i)==separator || strDate.charAt(i)==separatorH) {
        i++;
        continue;
      }
      if (length<=0) {
        j++;
        if (j>0) {
          if (b[j]=="%H") text += " ";
          else if (b[j]=="%M" || b[j]=="%S") text += separatorH;
          else text += separator;
        }
        switch (b[j]) {
            case "%d":
            case "%e":
                text += strDate.charAt(i);
                length = 2;
                break;
            case "%m":
                text += strDate.charAt(i);
                length = 2;
                break;
            case "%Y":
                text += strDate.charAt(i);
                length = 4;
                break;
            case "%y":
                text += strDate.charAt(i);
                length = 2;
                break;
            case "%H":
            case "%I":
            case "%k":
            case "%l":
                text += strDate.charAt(i);
                length = 2;
                break;
            case "%M":
                text += strDate.charAt(i);
                length = 2;
                break;
            case "%S":
                text += strDate.charAt(i);
                length = 2;
                break;
        }
      } else text += strDate.charAt(i);
      length--;
      i++;
    }
    field.value = text;
    //IE doesn't detect the onchange event if text value is modified programatically, so it's here called
    if (i > 7 && (typeof (field.onchange)!="undefined")) field.onchange();
  }
}

// CaretPosition object
function CaretPosition()
{
 var start = null;
 var end = null;
}

/* Function that returns actual position of -1 if we are at last position*/
function getCaretPosition(oField)
{
 var oCaretPos = new CaretPosition();

 // IE support
 if(document.selection)
 {
  oField.focus();
  var oSel = document.selection.createRange();
  var selectionLength = oSel.text.length;
  oSel.moveStart ('character', -oField.value.length);
  oCaretPos.start = oSel.text.length - selectionLength;
  oCaretPos.end = oSel.text.length;
 }
 // Firefox support
 else if(oField.selectionStart || oField.selectionStart == '0')
 {
  // This is a whole lot easier in Firefox
  oCaretPos.start = oField.selectionStart;
  oCaretPos.end = oField.selectionEnd;
 }

 // Return results
 return (oCaretPos);
}
