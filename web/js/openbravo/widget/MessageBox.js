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

dojo.provide("openbravo.widget.MessageBox");
  dojo.require("dojo.string.extras");

dojo.widget.defineWidget(
  // class
  "openbravo.widget.MessageBox",

  // superclass	
  dojo.widget.HtmlWidget,

  // member variables/functions
  {
  // Maximum Level of the Message Box: Hidden, Success, Info, Warning, Error
  maxLevel: "Hidden",

  // Array of Messages
  // Composed by:
  //  type: Hidden, Info, Warning, Error.
  //  title: String
  //  message: String
  arrMessages: new Array(),

  // String for initial title
  title: "",

  //Type of MessageBox
  //  values: Hidden, Error, Warning, Info, Success.
  type:"Hidden",

  isContainer: "true",
  templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/MessageBox.html"),
  templateCssPath: null,  // Defined in the skin --- MessageBox.css ,

  fillInTemplate: function(args, frag){
    if((this.type!=null&&this.type!="")&&((this.title!=null&&this.title!="")||(frag.value!=null&&frag.value!=""))){
      this.setValues(this.type, this.title, frag.value);
    };
    this.render();
  },

  // Function to set the class type
  setType: function(){
    dojo.html.setClass(this.messageNode, "MessageBox"+this.maxLevel.toUpperCase());
  },

  // Function to set the contained text
  setContainer: function(/*String*/ container){
    this.containerNode.innerHTML=container;
  },

  // Function to build the message from the array of messages
  //   Messages are built as a Html table with one line for each message.
  setMessage: function(){
    var strMessage = "";
    for (var i = 0; i < this.arrMessages.length; i++) {
      if (this.arrMessages[i][0] == this.maxLevel){
        strMessage += "<DIV class=\"MessageBox_TextTitle\">"+ this.arrMessages[i][1] +"</DIV>\n<DIV class=\"MessageBox_TextDescription\">"+ this.arrMessages[i][2] +"</DIV>\n<DIV class=\"MessageBox_TextSeparator\"></DIV>";
      }
    }
    strMessage += "";
    this.setContainer(strMessage);
  },

  // Function to add a value to the array of messages
  setValues: function(/*String*/ type, /*String*/ title, /*String*/ message){
    var strType = dojo.string.capitalize(type);
    if (this.maxLevel == "ERROR"){
    }
    else if (this.maxLevel == "WARNING"){
      if (strType == "ERROR"){
        this.maxLevel = strType;
      }
    }
    else if (this.maxLevel == "INFO"){
      if (strType == "ERROR" || strType == "WARNING"){
        this.maxLevel = strType;
      }
    }
    else if (this.maxLevel == "SUCCESS"){
      if (strType == "ERROR" || strType == "WARNING" || strType == "INFO"){
        this.maxLevel = strType;
      }
    }
    else if (this.maxLevel == "HIDDEN"){
      if (strType == "ERROR" || strType == "WARNING" || strType == "INFO" || strType == "SUCCESS"){
        this.maxLevel = strType;
      }
    }
    var arrlength = this.arrMessages.length;
    this.arrMessages [arrlength] = new Array(3);
    this.arrMessages [arrlength][0] = strType;
    this.arrMessages [arrlength][1] = title;
    this.arrMessages [arrlength][2] = message;
    this.render();
  },

  // Function to reset the array of messages
  initialize: function (){
    this.maxLevel = "HIDDEN";
    this.arrMessages = new Array();
  },

  // Function to paint the message
  render: function (){
    this.setType();
    this.setMessage();
  },

  // Function to reset the array of messages and repaint the object
  clear: function (){
    this.initialize();
    this.render();
  }
	}
);