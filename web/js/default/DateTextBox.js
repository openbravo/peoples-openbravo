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
}

expandDateYear= function(/*String*/ id){
  var str_dateFormat = document.getElementById(id).getAttribute("displayformat");
  if (!str_dateFormat) str_dateFormat = defaultDateFormat; 
  if (str_dateFormat.indexOf('YYYY') != -1) {
    var centuryReference = 50;
    var str_datetime = document.getElementById(id).value;
    var dateBlock = new Array();
    dateBlock[1] = getDateBlock(str_datetime, 1);
    dateBlock[2] = getDateBlock(str_datetime, 2);
    dateBlock[3] = getDateBlock(str_datetime, 3);

    if (!dateBlock[1] || !dateBlock[2] || !dateBlock[3]) {
      return false;
    }

    if (str_dateFormat.substr(1,1) == 'Y') {
      var yearBlock = 1;
    } else if (str_dateFormat.substr(7,1) == 'Y') {
      var yearBlock = 3;
    } else {
      return false;
    }

    if (dateBlock[yearBlock].length == 1) {
      dateBlock[yearBlock] = '000' + dateBlock[yearBlock];
    } else if (dateBlock[yearBlock].length == 2) {
      if (dateBlock[yearBlock] < centuryReference) {
        dateBlock[yearBlock] = '20' + dateBlock[yearBlock];
      } else {
        dateBlock[yearBlock] = '19' + dateBlock[yearBlock];
      }
    } else if (dateBlock[yearBlock].length == 3) {
      dateBlock[yearBlock] = '0' + dateBlock[yearBlock];
    } else if (dateBlock[yearBlock].length == 4) {
      return true;
    }

    var dateSeparator = str_dateFormat.replace(/D/g,"").replace(/M/g,"").replace(/Y/g,"").substr(0,1);
    var normalizedDate = dateBlock[1] + dateSeparator + dateBlock[2] + dateSeparator + dateBlock[3];
    document.getElementById(id).value = normalizedDate;
  } else {
    return false;
  }
  return true;
}

getDateBlock= function(/*String*/ str_date, block){
  // datetime parsing and formatting routimes. modify them if you wish other datetime format 
  //function str2dt (str_datetime) { 
  var re_date = /^(\d+)[\-|\/|/|:|.|\.](\d+)[\-|\/|/|:|.|\.](\d+)$/; 
  if (!re_date.exec(str_date)) 
    return false; 
  var dateBlock = new Array();
  dateBlock[1] = RegExp.$1;
  dateBlock[2] = RegExp.$2;
  dateBlock[3] = RegExp.$3;
  if (block == 1 || block == '1') return dateBlock[1];
  else if (block == 2 || block == '2') return dateBlock[2];
  else if (block == 3 || block == '3') return dateBlock[3];
  else dateBlock;
}

isValidDateTextBox= function(/*String*/ id){
  var isValid = this.isValidDate(document.getElementById(id).value, document.getElementById(id).getAttribute("displayformat"));
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

isValidDate = function(/*String*/str_datetime, /*String*/str_dateFormat) {
  if (this.getDate(str_datetime,str_dateFormat)){ 
    return true 
  } else { 
    return false;
  }
}


purgeDateFormat= function(/*String*/ str_format){
  str_format = str_format.replace("mm","MM").replace("dd","DD").replace("yyyy","YYYY");
  str_format = str_format.replace("mm","MM").replace("dd","DD").replace("yy","YY");
  str_format = str_format.replace("%D","%d").replace("%M","%m");
  str_format = str_format.replace("/","-").replace("/","-").replace("/","-");
  str_format = str_format.replace(".","-").replace(".","-").replace(".","-");
  str_format = str_format.replace(":","-").replace(":","-").replace(":","-");
  return str_format;
}

getDate = function(/*String*/str_datetime, /*String*/str_dateFormat) { 
  var inputDate=new Date(0,0,0); 
  if (str_datetime.length == 0) return inputDate; 


  var dateBlock = new Array();
  var fullYear = false;
  dateBlock[1] = getDateBlock(str_datetime, 1);
  dateBlock[2] = getDateBlock(str_datetime, 2);
  dateBlock[3] = getDateBlock(str_datetime, 3);

  if (!dateBlock[1] || !dateBlock[2] || !dateBlock[3]) {
    return false;
  }
  if (!str_dateFormat) str_dateFormat = defaultDateFormat; 

  str_dateFormat = purgeDateFormat(str_dateFormat);

  switch (str_dateFormat) { 
    case "MM-DD-YYYY": 
    case "YY-MM-DDDD": 
    case "DD-MM-YYYY": 
    case "%m-%d-%Y": 
    case "%Y-%m-%d": 
    case "%d-%m-%Y": 
      fullYear = true;
  }
  switch (str_dateFormat) { 
    case "MM-DD-YYYY": 
    case "MM-DD-YY": 
    case "%m-%d-%Y": 
    case "%m-%d-%y": 
      if (dateBlock[2] < 1 || dateBlock[2] > 31) return false; 
      if (dateBlock[1] < 1 || dateBlock[1] > 12) return false; 
      if (dateBlock[3] < 1 || dateBlock[3] > 9999) return false; 
      inputDate=new Date(parseFloat(dateBlock[3]), parseFloat(dateBlock[1])-1, parseFloat(dateBlock[2])); 
      if (fullYear) { inputDate.setFullYear(dateBlock[3]); }
      return inputDate; 
    case "YYYY-MM-DD": 
    case "YY-MM-DD": 
    case "%Y-%m-%d": 
    case "%y-%m-%d": 
      if (dateBlock[3] < 1 || dateBlock[3] > 31) return false; 
      if (dateBlock[2] < 1 || dateBlock[2] > 12) return false; 
      if (dateBlock[1] < 1 || dateBlock[1] > 9999) return false; 
      inputDate=new Date(parseFloat(dateBlock[1]), parseFloat(dateBlock[2])-1, parseFloat(dateBlock[3])); 
      if (fullYear) { inputDate.setFullYear(dateBlock[1]); }
      return inputDate; 
    case "DD-MM-YYYY": 
    case "DD-MM-YY": 
    case "%d-%m-%Y": 
    case "%d-%m-%y": 
    default: 
      if (dateBlock[1] < 1 || dateBlock[1] > 31) return false; 
      if (dateBlock[2] < 1 || dateBlock[2] > 12) return false; 
      if (dateBlock[3] < 1 || dateBlock[3] > 9999) return false; 
      inputDate=new Date(parseFloat(dateBlock[3]), parseFloat(dateBlock[2])-1, parseFloat(dateBlock[1])); 
      if (fullYear) { inputDate.setFullYear(dateBlock[3]); }
      return inputDate; 
  }
  return false; 
}


/****************************************
Test insertion of mask at inserting time
*****************************************/
function autoCompleteDate(field, fmt) {
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
