/**************************************************
Original Version (1.0):
Glenn G. Vergara
http://www21.brinkster.com/gver/
glenngv AT yahoo DOT com
Makati City, Philippines

Object-Based Version:
Eric C. Davis
http://www.10mar2001.com/
eric AT 10mar2001 DOT com
Atlanta, GA, US

(Keep the above intact if you want to use it! Thanks.)

Current Version: 2.3
Last Update: 11 September 2003

********
Change Log:
New in version 2.4b:
	- Added accepting of non-existent option
	- Added punctuation as acceptable input
	- Added setValueByValue() convenience method

New in version 2.2:
	- Many properties made private
	- Getters and setters for nearly all properties

New in version 2.0:
	- Object-oriented properties and methods using prototype
	- Constructor can accept a select element object or a select element object's ID string
	- Invocation reduced to single line of script: varName = new TypeAheadCombo('selectElementID');

New in version 1.4:
	- Allowable character set ranges use dynamic evaluation
	- Display of typed characters in status bar can be disabled

New in version 1.2:
	- Replaced major if/elseif/.../else statement with switch/case
	- Correction of characters typed on the numpad, reassigning to actual character values
********

********
API:
Constructor:
	new TypeAheadCombo(someSelectElement) // as an object or object reference
	new TypeAheadCombo('someSelectElementID') // as a string
	new TypeAheadCombobox('someSelectElementID', true) // to allow an undefined value

Priviledged Methods: (these interact with private properties and act as helper functions)
	getTyped()
		- returns the string typed by the user since the last timeout
	setTyped(str)
		- argument "str" - string which will replace the value in the type buffer
	type(str)
		- argument "str" - string which will be appended to the type buffer
	resetTyped()
		- clears what has been typed from the buffer
	getIndex()
		- returns the location of the option currently selected
	setIndex(val)
		- stores the location of the option being selected
	getPrev()
		- returns the location of the option previously selected
	setPrev(val)
		- stores the location of the option previously selected
	setResetTime(val)
		- sets the timeout interval for the reset timers
	getResetTime()
		- returns the timeout interval for the reset timers
	setResetTimer()
		- sets the timeout for the reset of the typed buffer
	clearResetTimer()
		- clears the timeout of the reset of the typed buffer
	validChar(charCode)
		- validates that the charCode passed is acceptable to the typed buffer
	setDisplayStatus(bool)
		- set whether to display the typed buffer in the status bar
	getDisplayStatus()
		- returns the current setting for status bar display of the typed buffer

Public Methods:
	detectKey()
		- detects the keyCode, parses whether it is acceptable, and adds it to the typed buffer if so
	selectItem()
		- finds the first option that matches the typed buffer and selects it
	reset()
		- clears the typed buffer and the status display
	updateIndex()
		- handles the onclick and onblur events
	elementFocus()
		- handles the onfocus event
	elementKeydown()
		- handles the onkeydown event
********

***************************************************/

/**
* @fileoverview Deprecated library of functions that supported list box searches
* (as you positioned yourself on a listbox, you could start typing in the first 
* letter of the item you were looking for). Deprecated because new browsers 
* already support this functionality out of the box.
*/

function TypeAheadCombo(anElement,acceptNewValue, identifierID, _autoload) {
	// DEGRADE UNSUPPORTED
	if (document.layers) {
		return;
	}
	// VALIDATION
	if (!anElement) {
		return false;
	}
	if (typeof anElement == "string") { // try for the ID
		anElement = document.getElementById ? document.getElementById(anElement) : document.all ? document.all[anElement] : anElement;
	}
	if (typeof anElement == "string") { // the grab failed: typeof null yields "object"
		return false;
	}
	// ASSOCIATION
	this.element = anElement;
	this.element.combo = this;
	// ELEMENT EVENT HANDLERS
	this.element.onkeydown = this.elementKeydown;
  if (this.element.onfocus) this.prevoiusFocus = this.element.onfocus;
  this.element.onfocus = this.elementFocus;
	this.element.onclick = this.updateIndex;
  if (this.element.onblur) this.previousBlur = this.element.onblur;
	this.element.onblur = this.updateIndex;
	this.element.reset = this.reset;
  this.element.autoload=((_autoload==null)?false:_autoload);
	// PRIVATE PROPERTIES
	var self = this,	// corrects privatization bug
		typed = "",
		index = prev = 0,
		displayStatus = true,
    IDAsidentifier = ((identifierID==null)?false:identifierID),
		selector, resetter, nullStarter, acceptNew,
		resetTime = 1600,
		numberRangeStart = 48,
		numberRangeEnd = 57,
		charRangeStart = 65,
		charRangeEnd = 90,
		punctRangeStart = 146,
		punctRangeEnd = 223;
	if (this.element.options.length>0 && (this.element.options[0].text==null || this.element.options[0].text.length == 0) && (this.element.options[0].value==null || this.element.options[0].value.length == 0)){
		nullStarter = true;
	} else {
		nullStarter = false;
	}
	if (typeof acceptNewValue != 'undefined' && acceptNewValue) {
		acceptNew = true;
		resetTime = 2400;
	} else {
		acceptNew = false;
	}
	// PRIVATE METHODS
	var getResetTime = function getResetTime() {
		return resetTime;
	}
	var charInRanges = function charInRanges(charCode) {
		if ((charCode >= numberRangeStart && charCode <= numberRangeEnd) || (charCode >= charRangeStart && charCode <= charRangeEnd) || (charCode >= punctRangeStart && charCode <= punctRangeEnd)) {
			return true;
		} else {
			return false;
		}
	}
	// PRIVILEDGED METHODS
	this.hasNullStarter = function() {
		return nullStarter;
	}
	this.getAcceptsNew = function() {
		return acceptNew;
	}
	this.getTyped = function () {
		return typed;
	}
	this.setTyped = function (str) {
		typed = str;
		return true;
	}
	this.resetTyped = function () {
		typed = "";
		return true;
	}
	this.type = function (str) {
		typed += str;
		return true;
	}
	this.getIndex = function () {
		return index;
	}
	this.setIndex = function (val) {
		if (!isNaN(val)) {
			index = val;
		}
	}
	this.getPrev = function () {
		return (prev ? prev : 0);
	}
	this.setPrev = function (val) {
		if (!isNaN(val)) {
			prev = val;
		}
	}
	this.setResetTime = function (val) {
		if (!isNaN(val)) {
			prev = val;
		}
	}
  var isIDIdentifier = function isIDIdentifier() {
		return IDAsidentifier;
	}
	this.setResetTimer = function () {
		if (isIDIdentifier()) resetter = setTimeout("getReference('"+self.element.id+"').reset();", getResetTime());
		else resetter = setTimeout("document.forms['"+self.element.form.name+"'].elements['"+self.element.name+"'].reset();", getResetTime());
	}
	this.clearResetTimer = function () {
		clearTimeout(resetter);
	}
	this.delayedSelect = function () {
    if (isIDIdentifier()) selector = setTimeout("getReference('"+self.element.id+"').combo.selectItem()", 10);
		else selector = setTimeout("document.forms['"+self.element.form.name+"'].elements['"+self.element.name+"'].combo.selectItem()", 10);
	}
	this.cancelDelay = function () {
		clearTimeout(selector);
	}
	this.validChar = function (evt, charCode) {
		if ((evt.ctrlKey) || (evt.altKey)) {
			return false;
		} else if ((evt.shiftKey) && charInRanges(charCode)) {
			return true;
		} else if (evt.shiftKey) {
			return false;
		} else {
			return charInRanges(charCode);
		}
	}
	this.setDisplayStatus = function (bool) {
		if (bool == true || bool == false) {
			displayStatus = bool;
		}
	}
	this.getDisplayStatus = function () {
		return displayStatus;
	}
}

/*
PUBLIC METHODS
*/

TypeAheadCombo.prototype.detectKey = function (evt){
	this.clearResetTimer();
	this.cancelDelay();
	var combo_letter = "";
	var combo_code = (document.all) ? window.event.keyCode : evt.which;
	var event = (document.all) ? window.event : evt;
	if (combo_code <= 105 && combo_code >= 96) { // make up for numPad typing
		combo_code = combo_code - 48;
	}
	switch (combo_code) {
		case 27:	//ESC key
			this.reset();
			this.setIndex(this.getPrev());
			// Put a little delay to override NS6/Mozilla's built-in behavior of ESC inside select element
			setTimeout("document.forms['"+this.element.form.name+"'].elements['"+this.element.name+"'].selectedIndex = document.forms['"+this.element.form.name+"'].elements['"+this.element.name+"'].index",0);
			return false;
			break;
		case 13:	//ENTER key
      var haschanged = (this.element.selectedIndex!=this.getPrev());
			this.reset();
			if (this.element.onchange && haschanged) {
				this.element.onchange();
			}
		case 9:		//TAB key	(don't break from ENTER - all TAB needs to do is return true. ENTER needs above and return true.
			return true;
			break;
		case 8:		//BACKSPACE key
      event.returnValue=false;
      event.cancelBubble=true;
			this.setTyped(this.getTyped().substring(0,this.getTyped().length-1));
			if (this.getAcceptsNew()) {
				this.makeNewValue();
			}
			if (this.getTyped() == "") {
				this.reset();
				this.setIndex(this.getPrev());
				this.element.selectedIndex = this.getIndex();
				return false;
			} else {
				this.setResetTimer();
			}
			break;
		case 33:	//PAGEUP key
		case 34:	//PAGEDOWN key
		case 35:	//END key
		case 36:	//HOME key
		case 38:	//UP arrow
		case 40:	//DOWN arrow
			this.reset();
			return true;
			break;
		case 37:	//LEFT arrow	(translates to %)
		case 39:	//RIGHT arrow	(translates to ')
			this.reset();
			return false;
			break;
		case 32:	//SPACE key	(not in accepted ranges)
			combo_letter = " ";
			this.setResetTimer();
			break;
		default:
			if (this.validChar(event, combo_code)) {
				combo_letter = String.fromKeyCode(combo_code);
				if (combo_letter.length > 1) {
					if (event.shiftKey) {
						combo_letter = combo_letter[1];
					} else {
						combo_letter = combo_letter[0];
					}
				}
				this.setResetTimer();
			} else {
				return true;
			}
			break;
	}
	this.type(combo_letter);
	if (this.getDisplayStatus()) {
		window.status = this.getTyped();
	}
	if (document.all) {
		if (!this.selectItem()) {
      event.returnValue=false;
      event.cancelBubble=true;
      //this.element.onchange();
      return false;
    } else return true;
	} else {
		return this.delayedSelect();
	}
}

TypeAheadCombo.prototype.selectItem = function (){
	for (var i=0; i<this.element.options.length; i++){
		if (this.element.options[i].text.toUpperCase().indexOf(this.getTyped()) == 0){
			this.element.selectedIndex = i;
			this.setIndex(i);	//remember selected index
			return false;
		}
	}
	if (this.getAcceptsNew()) {
		this.makeNewValue();
	} else {
		this.element.selectedIndex = this.getIndex();	//re-select previously selected option even if there's no match
	}
	return false;  //always return false
}

TypeAheadCombo.prototype.makeNewValue = function () {
	this.removeNewValue();
	if (this.hasNullStarter()) {
		newOption = this.element.options[0];
	} else {
		newOption = document.createElement("option");
		this.element.insertBefore(newOption, this.element.firstChild);
		this.newOption = newOption;
	}
	var tmpText = this.getTyped(),tmpStart = tmpEnd = "",tmpArr,i;
	tmpArr = tmpText.split(" ");
	i = tmpArr.length;
	if (tmpText.indexOf(" ") >= 0) {
		do {
			tmpStart = tmpArr[--i].substring(0,1);
			tmpEnd = tmpArr[i].substring(1,tmpArr[i].length);
			tmpArr[i] = tmpStart.toUpperCase() + tmpEnd.toLowerCase();
		} while (i);
		tmpText = tmpArr.join(" ");
	} else {
		tmpStart = tmpText.substring(0,1);
		tmpEnd = tmpText.substring(1,tmpText.length);
		tmpText = tmpStart.toUpperCase() + tmpEnd.toLowerCase();
	}
	newOption.value = tmpText;
	newOption.text = tmpText;
	this.element.selectedIndex = 0;
	this.setIndex(0);
}

TypeAheadCombo.prototype.removeNewValue = function () {
	if (this.hasNullStarter()) {
		this.element.options[0].text = '';
		this.element.options[0].value = '';
	} else if (this.newOption) {
		this.element.remove(this.newOption);
	}
}

TypeAheadCombo.prototype.setValueByValue = function (aValue) {
	var i = this.element.options.length;
	do {
		if (this.element.options[--i].value == aValue) {
			this.element.selectedIndex = i;
			break;
		}
	} while (i);
}

TypeAheadCombo.prototype.reset = function () {
	theCombo = this;
	if (this.combo) {
		theCombo = this.combo;
	}
	theCombo.resetTyped();
	if (theCombo.getDisplayStatus()) {
		window.status = window.defaultStatus ? window.defaultStatus : '';
	}
}

TypeAheadCombo.prototype.updateIndex = function (){
	this.combo.setIndex(this.selectedIndex);
  var haschanged = (this.combo.getIndex()!=this.combo.getPrev());
	this.combo.setPrev(this.combo.getIndex);
	this.combo.reset();
  if (this.previousBlur) eval(this.previousBlur);
  else if (this.onchange && haschanged) this.onchange();
}

TypeAheadCombo.prototype.elementFocus = function () {
  if (this.prevoiusFocus) eval(this.prevoiusFocus);
	this.combo.setIndex(this.selectedIndex);
  this.combo.setPrev(this.selectedIndex);
}

TypeAheadCombo.prototype.elementKeydown = function (event) {
	var aux = this.combo.detectKey(event);
  if (this.autoload && this.onchange) this.onchange();
  return aux;
}
