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

dojo.provide("openbravo.widget.HyperLink");
dojo.require("dojo.widget.InternetTextbox");
dojo.require("dojo.widget.PopupContainer");
dojo.require("dojo.event.*");

dojo.widget.defineWidget(
  // class
  "openbravo.widget.HyperLink",

  // superclass	
  dojo.widget.HtmlWidget,

  // member variables/functions
  {

		// name: String
		//	name used when submitting form; same as "name" attribute or plain HTML elements
		name: "",

  iconURL: dojo.uri.dojoUri("../openbravo/widget/templates/linkIcon.gif"),
  // iconAlt: dojo.uri.Uri: alt text for the dropdown button icon
  iconAlt: "Go to",

		value: "",

		// containerToggle: String: toggle property of the dropdown
		containerToggle: "plain",

		// containerToggleDuration: Integer: toggle duration property of the dropdown
		containerToggleDuration: 150,

  templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/HyperLink.html"),
  templateCssPath: null, // Defined in the skin --- HyperLink.css

  fillInTemplate: function(/*Object*/ args, /*Object*/ frag){
			dojo.event.connect(this.inputHyperLinkNode, "onchange", this, "onInputChange");
			dojo.event.connect(this.inputHyperLinkNode, "onkeyup", this, "onInputKeyUp");
  },

  onClick: function() {},

		onIconClick: function(){
      window.open(this.value);
		},

	  onChange: function() {},

		onInputChange: function(){
			// summary: signal for changes in the input box
      this.value = this.inputHyperLinkNode.value;
		},

	  onKeyUp: function() {},

		onInputKeyUp: function(){
		}

	}
);