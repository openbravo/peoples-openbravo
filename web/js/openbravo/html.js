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
dojo.provide("openbravo.html");

dojo.require("dojo.html.*");

openbravo.html = {
	
	setContent: function(element, content) {
		if (element.innerText != undefined)
			element.innerText = content;
		else
			element.textContent = content;	
	},
	
	prereplaceClass: function(node, newClass, oldClass) {
		if (dojo.html.hasClass(node, oldClass))
			dojo.html.removeClass(node, oldClass);
		if (!dojo.html.hasClass(node, newClass))
			dojo.html.prependClass(node, newClass);
	},
	
	stripNbsp: function(string) {
		return string;
	},
	
	extractPx: function(property) {
		return parseInt(property.substring(0, property.indexOf("px")));
	},
	
	getContent: function(node) {
		return node.innerText != undefined ? node.innerText : node.textContent;
	},
	
	getContentAsString:function(node){
		if (typeof node.xml != "undefined")
			return this.getContentAsStringIE(node);
		else if (typeof XMLSerializer != "undefined" )
			return this.getContentAsStringMozilla(node);
		else
			return this.getContentAsStringGeneric(node);
	},
	
	getContentAsStringIE:function(node){
		var s="";
    	for (var i = 0; i < node.childNodes.length; i++)
        	s += node.childNodes[i].xml;
    	return s;
	},
	
	getContentAsStringMozilla:function(node){
		var xmlSerializer = new XMLSerializer();
	    var s = "";
	    for (var i = 0; i < node.childNodes.length; i++) {
	        s += xmlSerializer.serializeToString(node.childNodes[i]);
	        if (s == "undefined")
		        return this.getContentAsStringGeneric(node);
	    }
	    return s;
	},
	
	getContentAsStringGeneric:function(node){
		var s="";
		if (node == null) { return s; }
		for (var i = 0; i < node.childNodes.length; i++) {
			switch (node.childNodes[i].nodeType) {
				case 1: // ELEMENT_NODE
				case 5: // ENTITY_REFERENCE_NODE
					s += this.getElementAsStringGeneric(node.childNodes[i]);
					break;
				case 3: // TEXT_NODE
				case 2: // ATTRIBUTE_NODE
				case 4: // CDATA_SECTION_NODE
					s += node.childNodes[i].nodeValue;
					break;
				default:
					break;
			}
		}
		return s;	
	},
	
	getElementAsStringGeneric:function(node){
		if (!node) { return ""; }
		
		var s='<' + node.nodeName;
		// add attributes
		if (node.attributes && node.attributes.length > 0) {
			for (var i=0; i < node.attributes.length; i++) {
				s += " " + node.attributes[i].name + "=\"" + node.attributes[i].value + "\"";	
			}
		}
		// close start tag
		s += '>';
		// content of tag
		s += this.getContentAsStringGeneric(node);
		// end tag
		s += '</' + node.nodeName + '>';
		return s;
	},
	
	clearElement: function(element) {
		if (typeof(element) == 'string')
			element = dojo.byId(element);
		while (element.firstChild)
			element.removeChild(element.firstChild);
	}
	
}