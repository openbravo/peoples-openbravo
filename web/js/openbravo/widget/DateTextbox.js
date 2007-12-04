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

dojo.provide("openbravo.widget.DateTextbox");
dojo.require("dojo.widget.DateTextbox");
dojo.require("openbravo.messages.common");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "acme.widget" module
//dojo.registerNamespace("acme", "acme.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.DateTextbox",

	// superclass	
	dojo.widget.DateTextbox,
	
	// member variables/functions
	{
		// override displayFormat: String
		//	optional pattern used format date.  Uses locale-specific format by default.  See dojo.date.format.
		displayFormat: "",
		// override saveFormat: String
		//	optional pattern used format date.  Uses locale-specific format by default.  See dojo.date.format.
		saveFormat: "",
		listenOnKeyPress:false,
    // Id of the other member of the comparison.
    greaterThan:"",
    lowerThan:"",
    isWrong:false,
    inputDate:new Date(0,0,0),
		// override css
		templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/DateTextbox.html"),
		templateCssPath: null, // Defined in the skin --- ValidationTextbox.css

		postMixInProperties:function(_b04,frag){
		  openbravo.widget.DateTextbox.superclass.postMixInProperties.apply(this,arguments);
		  //this.value=this.dateToRfc3339(this.value,this.displayFormat);
    this.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
    this.messages.invalidMessage= openbravo.messages.getDataBaseMessage("Invalid");
    this.messages.missingMessage= openbravo.messages.getDataBaseMessage("Missing");
    this.messages.rangeMessage= openbravo.messages.getDataBaseMessage("Range");
		},

		fillInTemplate:function(args,frag){
		  this.displayFormat=this.displayFormat.replace("mm","MM").replace("DD","dd").replace("YYYY","yyyy");
		  this.saveFormat=this.displayFormat;
		  openbravo.widget.DateTextbox.superclass.fillInTemplate.apply(this,arguments);
      this.invalidSpan.style.display="none";
      this.missingSpan.style.display="none";
      this.rangeSpan.style.display="none";
		},

    isValid: function(){ 
      if (this.getDate(this.textbox.value,this.displayFormat)){
        return true
      } else {
        return false;
      }
    },

    getDate: function(str_datetime, str_dateFormat){
      var inputDate=new Date(0,0,0);
      if (str_datetime.length == 0) return inputDate;
      // datetime parsing and formatting routimes. modify them if you wish other datetime format
      //function str2dt (str_datetime) {
      var re_date = /^(\d+)[\-|\/|\.](\d+)[\-|\/|\.](\d+)$/;
      if (!re_date.exec(str_datetime))
        return false;
      if (!str_dateFormat) str_dateFormat = defaultDateFormat;
      switch (str_dateFormat) {
        case "MM\/DD\/YYYY":
        case "MM\/DD\/YY":
        case "MM-DD-YYYY":
        case "MM-DD-YY":
        case "MM.DD.YYYY":
        case "MM.DD.YY":
        case "%m-%d-%Y":
          if (RegExp.$2 < 1 || RegExp.$2 > 31) return false;
          if (RegExp.$1 < 1 || RegExp.$1 > 12) return false;
          if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false;
          inputDate=new Date(parseFloat(RegExp.$3), parseFloat(RegExp.$1)-1, parseFloat(RegExp.$2));
          return inputDate;
        case "YYYY\/MM\/DD":
        case "YY\/MM\/DD":
        case "YYYY-MM-DD":
        case "YY-MM-DD":
        case "YYYY.MM.DD":
        case "YY.MM.DD":
        case "%Y-%m-%d":
          if (RegExp.$3 < 1 || RegExp.$3 > 31) return false;
          if (RegExp.$2 < 1 || RegExp.$2 > 12) return false;
          if (RegExp.$1 < 1 || RegExp.$1 > 9999) return false;
          inputDate=new Date(parseFloat(RegExp.$1), parseFloat(RegExp.$2)-1, parseFloat(RegExp.$3));
          return inputDate;
        case "DD\/MM\/YYYY":
        case "DD\/MM\/YY":
        case "DD-MM-YYYY":
        case "DD-MM-YY":
        case "DD.MM.YYYY":
        case "DD.MM.YY":
        case "%d-%m-%Y":
        default:
          if (RegExp.$1 < 1 || RegExp.$1 > 31) return false;
          if (RegExp.$2 < 1 || RegExp.$2 > 12) return false;
          if (RegExp.$3 < 1 || RegExp.$3 > 9999) return false;
          inputDate=new Date(parseFloat(RegExp.$3), parseFloat(RegExp.$2)-1, parseFloat(RegExp.$1));
          return inputDate;
      }
      return false;
    },


		isInRange: function(){
      if ((this.greaterThan == "" || this.greaterThan == null) && (this.lowerThan == "" || this.lowerThan == null)) {
        return true;
      }
      if (this.greaterThan != "") {
        if (document.getElementById(this.greaterThan).value == null || document.getElementById(this.greaterThan).value == "") {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        } else if (this.textbox.value == "" || this.textbox.value == null) {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        } else if (this.getDate(this.textbox.value,this.displayFormat) < this.getDate(document.getElementById(this.greaterThan).value,this.displayFormat)) {
          if (this.isWrong == false) {
            this.isWrong = true;
            dojo.widget.byId(this.greaterThan).update();
          }
          return false;
        } else {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.greaterThan).update();
          }
          return true;
        }
      }
      if (this.lowerThan != "") {
        if (document.getElementById(this.lowerThan).value == null || document.getElementById(this.lowerThan).value == "") {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.lowerThan).update();
          }
          return true;
        } else if (this.textbox.value == "" || this.textbox.value == null) {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.lowerThan).update();
          }
          return true;
        } else if (this.getDate(this.textbox.value,this.displayFormat) > this.getDate(document.getElementById(this.lowerThan).value,this.displayFormat)) {
      //  } else if ((parseFloat(this.textbox.value) < parseFloat(document.getElementById(this.lowerThan).value)) && (this.textbox.value != "" || this.textbox.value != null)) {
          if (this.isWrong == false) {
            this.isWrong = true;
            dojo.widget.byId(this.lowerThan).update();
          }
          return false;
        } else {
          if (this.isWrong == true) {
            this.isWrong = false;
            dojo.widget.byId(this.lowerThan).update();
          }
          return true;
        }
      }
		},



		dateToRfc3339: function(/*String*/dateObject, /*String*/formattedString){
		  // summary: sets a Date object based on an ISO 8601 formatted string (date only)
		  var year = null;
		  var month = null;
		  var day = null;

		  switch (formattedString.toUpperCase())
		  {
				case "DD-MM-YYYY":
				case "DD/MM/YYYY":
						  year = dateObject.substring(6,10);
						  month = dateObject.substring(3,5);
						  day = dateObject.substring(0,2);
						  dateObject = year+"-"+month+"-"+day;
				break;
				case "MM-DD-YYYY":
				case "MM/DD/YYYY":
						  year = dateObject.substring(6,10);
						  month = dateObject.substring(0,2);
						  day = dateObject.substring(3,5);
						  dateObject = year+"-"+month+"-"+day;
				break;
		  }
		  return dateObject; // Date
		},

		update: function() {
			// summary:
			//		Called by oninit, onblur, and onkeypress.
			// description:
			//		Show missing or invalid messages if appropriate, and highlight textbox field.
			this.lastCheckedValue = this.textbox.value;
			this.missingSpan.style.display = "none";
			this.invalidSpan.style.display = "none";
			this.rangeSpan.style.display = "none";
	
			var empty = this.isEmpty();
			var valid = true;
			if(this.promptMessage != this.textbox.value){ 
				valid = this.isValid(); 
			}
			var missing = this.isMissing();
	
			// Display at most one error message
			if(missing){
				this.missingSpan.style.display = "";
			}else if( !empty && !valid ){
				this.invalidSpan.style.display = "";
			}else if( !this.isInRange() ){
				this.rangeSpan.style.display = "";
			}
			this.highlight();
		}

	}
);
