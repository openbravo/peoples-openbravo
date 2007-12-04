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
DirectorDialog = function(row, editorHandler) {
	dojo.require("dojo.widget.Dialog");
	this.editorHandler = editorHandler;
	this.name = row.getValue("director");
	this.id = row.getValue("directorId");
	this.dialog = null;
}

DirectorDialog.Url = "ControllerServlet?command=directorAction";

DirectorDialog.prototype.render = function() {
	var mainDiv = document.createElement("div");
	var contentForm = document.createElement("form");
	var table = document.createElement("table");
	var tbody = document.createElement("tbody");
	this.fields = [{name: "Id", value: this.id }, {name: "Name", value: this.name}, 
			{name: "Location"}, {name: "Misc"}];
	dojo.lang.forEach(this.fields, function(field) {
		var row = document.createElement("tr");
		var labelCell = document.createElement("td");
		labelCell.appendChild(document.createTextNode(field.name + ":"));
		var valueCell = document.createElement("td");
		var input = document.createElement("input");
		input.name = field.name;
		if (field.value)
			input.value = field.value;
		field.input = input;
		valueCell.appendChild(input);
		row.appendChild(labelCell);
		row.appendChild(valueCell);
		tbody.appendChild(row);
	});
	this.fields[0].input.onblur = dojo.lang.hitch(this, function() {
		this.requestData(this.fields[0].input.value);
	});
	this.requestData(this.id);
	var buttonRow = document.createElement("tr");
	var buttonCell = document.createElement("td");
	dojo.lang.forEach(["Save", "Cancel"], function(name) {
		var button = document.createElement("input");
		button.type= "button";
		button.value= name;
		buttonCell.appendChild(button);
	});
	buttonCell.colspan = "2";
	buttonCell.align = "center";
	buttonRow.appendChild(buttonCell);
	tbody.appendChild(buttonRow);
	table.appendChild(tbody);
	contentForm.appendChild(table);
	mainDiv.appendChild(contentForm);
	var options = {
		bgColor: "white",
		bgOpacity: "0.5"
//		toggle: "fade",
//		toggleDuration: 250
	}
//	dojo.body().appendChild(mainDiv);
	var dialog = dojo.widget.createWidget("dojo:Dialog", options, contentForm);
	this.dialog = dialog;
	var _this = this;
	
	dojo.event.connect(buttonCell.childNodes[0], "onclick", this, "save");
	dojo.event.connect(buttonCell.childNodes[1], "onclick", this, "cancel");
	dojo.event.connect(dialog, "hide", dialog, "destroy");
	dialog.show();
	this.fields[0].input.focus();
}

DirectorDialog.prototype.requestData = function(id) {
	if (id) {
		var serviceUrl = {
			url: DirectorDialog.Url,
		    handler: dojo.lang.hitch(this, "directorReceived"),
		    method: "POST",
		    mimetype: "text/xml"
		};
	    openbravo.io.asyncCall(serviceUrl, {id: id, action: "retrieve"});
	}
}

DirectorDialog.prototype.directorReceived = function(type, data, evt) {
	var director = data.getElementsByTagName('director')[0];
	var name = openbravo.html.getContentAsString(director.getElementsByTagName("name")[0]);
	if (name != this.fields[1].input.value)
		this.fields[1].input.value = name;
	this.fields[2].input.value = openbravo.html.getContentAsString(director.getElementsByTagName("location")[0]);
	this.fields[3].input.value = openbravo.html.getContentAsString(director.getElementsByTagName("misc")[0]);
}

DirectorDialog.prototype.save = function() {
	var handlerRef = dojo.lang.hitch(this, function(type, data, evt) {
		var cellValues = [];
		cellValues["directorId"] = data;
		cellValues["director"] = this.fields[1].input.value;
		this.editorHandler.save(cellValues);
	});
	var serviceUrl = {
		url: DirectorDialog.Url,
	    handler: handlerRef,
	    method: "POST",
	    mimetype: "text/plain"
	};
	var content = {action: "save"};
	dojo.lang.forEach(this.fields, function(field) {
		content[field.name] = field.input.value;
	});
    openbravo.io.asyncCall(serviceUrl, content);
	this.dialog.hide();
}

DirectorDialog.prototype.cancel = function() {
	this.editorHandler.cancel();
	this.dialog.hide();
}
