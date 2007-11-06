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

dojo.provide("openbravo.widget.DropdownDatePicker");
dojo.require("dojo.widget.DropdownDatePicker");

// <namespace>, <namespace>.widget is now considered 'conventional'
// therefore the registerNamespace call below is no longer necessary here

// Tell dojo that widgets prefixed with "openbravo:" namespace are found in the "acme.widget" module
//dojo.registerNamespace("acme", "acme.widget");

// define UserButton's constructor
dojo.widget.defineWidget(
	// class
	"openbravo.widget.DropdownDatePicker",

	// superclass	
	dojo.widget.DropdownDatePicker,
	
	// member variables/functions
	{
    // override iconURL
    iconURL: dojo.uri.dojoUri("../openbravo/widget/templates/dateIcon.gif"),
		// override displayFormat: String
		//	optional pattern used format date.  Uses locale-specific format by default.  See dojo.date.format.
		displayFormat: "",
		// override saveFormat: String
		//	optional pattern used format date.  Uses locale-specific format by default.  See dojo.date.format.
		saveFormat: "",

		templateString: "",
		templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/DropdownDatePicker.html"),
		templateCssPath: null,

		postMixInProperties: function(localProperties, frag){
      this.value = this.dateToRfc3339(this.value, this.displayFormat);
    },

		fillInTemplate: function(args, frag){
      this.displayFormat = this.displayFormat.replace("mm", "MM").replace("DD", "dd").replace("YYYY", "yyyy");
      this.saveFormat = this.displayFormat;
			// summary: see dojo.widget.DomWidget

			dojo.widget.DropdownDatePicker.superclass.fillInTemplate.call(this, args, frag);
			//attributes to be passed on to DatePicker
			var dpArgs = {widgetContainerId: this.widgetId, lang: this.lang, value: this.value,
				startDate: this.startDate, endDate: this.endDate, displayWeeks: this.displayWeeks,
				weekStartsOn: this.weekStartsOn, adjustWeeks: this.adjustWeeks, staticDisplay: this.staticDisplay, templateCssPath: null};

			//build the args for DatePicker based on the public attributes of DropdownDatePicker
			this.datePicker = dojo.widget.createWidget("DatePicker", dpArgs, this.containerNode, "child");
			dojo.event.connect(this.datePicker, "onValueChanged", this, "_updateText");
			
			if(this.value){
				this._updateText();
			}
			this.containerNode.explodeClassName = "calendarBodyContainer";
			this.valueNode.name=this.name;
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
    }
	}
);