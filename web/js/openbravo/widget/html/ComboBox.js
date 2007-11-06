/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml

	Modified by Kenneth Ko, Aug 2006.
*/

dojo.provide("openbravo.widget.html.ComboBox");
dojo.require("openbravo.widget.ComboBox");
dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.lfx.*");
dojo.require("dojo.dom");
dojo.require("dojo.html.*");
dojo.require("dojo.string.*");
dojo.require("dojo.widget.html.stabile");

openbravo.widget.html.ComboBox = function(){
	openbravo.widget.ComboBox.call(this);
	dojo.widget.HtmlWidget.call(this);

	this.autoComplete = true;
	this.formInputName = "";
	this.name = ""; // clone in the name from the DOM node
	this.clientid = "";  //   comboBoxValue.id=clientid , comboBoxSelectionValue.id=clientid_selected
	this.textInputNode = null;
	this.comboBoxValue = null;
	this.comboBoxSelectionValue = null;
	this.optionsListWrapper = null;
	this.optionsListNode = null;
	this.downArrowNode = null;
	this.cbTableNode = null;
	this.searchTimer = null;
	this.searchDelay = 100;
	this.dataUrl = "";
	this.fadeTime = 200;
	// maxListLength limits list to X visible rows, scroll on rest 
	this.maxListLength = 8; 
	// mode can also be "remote" for JSON-returning live search or "html" for
	// dumber live search
	this.mode = "local"; 
	this.selectedResult = null;
	this._highlighted_option = null;
	this._prev_key_backspace = false;
	this._prev_key_esc = false;
	this._result_list_open = false;
	this._gotFocus = false;
	this._mouseover_list = false;
	this.selVal = "";
	this.selTxt = "";
	this.disabled = false;
	this.ro = "";  // indicates ReadOnly
	this.searchTimeout = 500; // typing delay, overrides the timeout set in the data provider
	this.formId = ""; // used for .NET, dataprovider will submit form to maintain viewstate
	this.invalid=false;
	this.autopostback="";
	this.isBlank=false; // used by local combo
}

dojo.inherits(openbravo.widget.html.ComboBox, dojo.widget.HtmlWidget);

// copied from superclass since we can't really over-ride via prototype
dojo.lang.extend(openbravo.widget.html.ComboBox, openbravo.widget.ComboBox.defaults);

dojo.lang.extend(openbravo.widget.html.ComboBox, {


	templatePath: dojo.uri.dojoUri("../openbravo/widget/templates/HtmlComboBox.html"),
	templateCssPath: dojo.uri.dojoUri("../openbravo/widget/templates/HtmlComboBox.css"),  // NOTE: override this in a <script> to change the css location. 

	setValue: function(value) {
		//console.debug("setValue called => " + value);
		this.comboBoxValue.value = value;
		if (this.textInputNode.value != value) { // prevent mucking up of selection
			this.textInputNode.value = value;
		}
		dojo.widget.html.stabile.setState(this.widgetId, this.getState(), true);
		//console.debug("state was saved as: " + this.getState().value + "," + this.getState().txtVal);
	},

	getValue: function() {
		return this.comboBoxValue.value;
	},

	getState: function() {
		return {value: this.getValue(), txtVal: this.textInputNode.value};
	},

	setState: function(state) {
		//console.debug("setting state as: " + state.value + "," + state.txtVal);
		this.setValue(state.value);
		this.textInputNode.value = state.txtVal;
	},

	getCaretPos: function(element){
		// khtml 3.5.2 has selection* methods as does webkit nightlies from 2005-06-22
		if(dojo.lang.isNumber(element.selectionStart)){
			// FIXME: this is totally borked on Moz < 1.3. Any recourse?
			return element.selectionStart;
		}else if(dojo.render.html.ie){
			// in the case of a mouse click in a popup being handled,
			// then the document.selection is not the textarea, but the popup
			// var r = document.selection.createRange();
			// hack to get IE 6 to play nice. What a POS browser.
			var tr = document.selection.createRange().duplicate();
			var ntr = element.createTextRange();
			tr.move("character",0);
			ntr.move("character",0);
			try {
				// If control doesnt have focus, you get an exception.
				// Seems to happen on reverse-tab, but can also happen on tab (seems to be a race condition - only happens sometimes).
				// There appears to be no workaround for this - googled for quite a while.
				ntr.setEndPoint("EndToEnd", tr);
				return String(ntr.text).replace(/\r/g,"").length;
			} catch (e) {
				return 0; // If focus has shifted, 0 is fine for caret pos.
			}
			
		}
	},

	setCaretPos: function(element, location){
		location = parseInt(location);
		this.setSelectedRange(element, location, location);
	},

	setSelectedRange: function(element, start, end){
		if(!end){ end = element.value.length; }  // NOTE: Strange - should be able to put caret at start of text?
		// Mozilla
		// parts borrowed from http://www.faqts.com/knowledge_base/view.phtml/aid/13562/fid/130
		if(element.setSelectionRange){
			element.focus();
			element.setSelectionRange(start, end);
		}else if(element.createTextRange){ // IE
			var range = element.createTextRange();
			with(range){
				collapse(true);
				moveEnd('character', end);
				moveStart('character', start);
				select();
			}
		}else{ //otherwise try the event-creation hack (our own invention)
			// do we need these?
			element.value = element.value;
			element.blur();
			element.focus();
			// figure out how far back to go
			var dist = parseInt(element.value.length)-end;
			var tchar = String.fromCharCode(37);
			var tcc = tchar.charCodeAt(0);
			for(var x = 0; x < dist; x++){
				var te = document.createEvent("KeyEvents");
				te.initKeyEvent("keypress", true, true, null, false, false, false, false, tcc, tcc);
				twe.dispatchEvent(te);
			}
		}
	},

	// does the keyboard related stuff
	_handleKeyEvents: function(evt){
		if(evt.ctrlKey || evt.altKey){ return; }

		// reset these
		this._prev_key_backspace = false;
		this._prev_key_esc = false;

		var k = dojo.event.browser.keys;
		var doSearch = true;

		// mozilla quirk 
		// space has no keyCode in mozilla
		var keyCode = evt.keyCode;
		if(keyCode==0 && evt.charCode==k.KEY_SPACE){
			keyCode = k.KEY_SPACE;
		}
		switch(keyCode){
 			case k.KEY_DOWN_ARROW:
				if(!this._result_list_open && !this.textInputNode.readOnly){
					this.startSearchArrow();
				}
				else
				{
					this.tryFocus();
					this.highlightNextOption();
				}
				dojo.event.browser.stopEvent(evt);
				return;
			case k.KEY_UP_ARROW:
				this.highlightPrevOption();
				dojo.event.browser.stopEvent(evt);
				return;
//			case k.KEY_ENTER:  // NEW CODE old enter code wasn't written
//				// prevent to send form if we press enter with list open
//				if(this._result_list_open){
//					dojo.event.browser.stopEvent(evt);
//				}
				// falltrough
			case k.KEY_TAB:
				// using linux alike tab for autocomplete
				if(!this.autoComplete && this._result_list_open && this._highlighted_option){
					dojo.event.browser.stopEvent(evt);
					this.selectOption({ 'target': this._highlighted_option, 'noHide': true });

					// put caret last
					this.setSelectedRange(this.textInputNode, this.textInputNode.value.length, null);
				}else {					
					this.selectOption();
					return;
				}
				break;
//			case k.KEY_SPACE:    // NEW CODE ignore space in the text box
			case k.KEY_ENTER:				
				if(this._result_list_open && this._highlighted_option){
					dojo.event.browser.stopEvent(evt);
					this.selectOption();
					return;
				}
				if (this.textInputNode.readOnly) {
					dojo.event.browser.stopEvent(evt);
					doSearch = false;
				}	
				dojo.event.browser.stopEvent(evt);
				break;
			case k.KEY_ESCAPE:
				this.hideResultList();
				this._prev_key_esc = true;
				return;
			case k.KEY_BACKSPACE:
			case k.KEY_DELETE:
				//console.debug(this.textInputNode.value);
				this._prev_key_backspace = true; // dont autocomplete
				if(!this.textInputNode.value.length){
					this.hideResultList();
					doSearch = false;
				}
				if (this.textInputNode.readOnly) {
					dojo.event.browser.stopEvent(evt);
					doSearch = false;
				}				
				break;
			case k.KEY_RIGHT_ARROW: // falltrough
			case k.KEY_LEFT_ARROW: // falltrough
			case k.KEY_SHIFT:
				doSearch = false;
				break;
			default:// non char keys (F1-F12 etc..)  shouldnt open list
				if(evt.charCode==0){
					doSearch = false;
				}
		}

		// if readOnly, shouldnt open list
		if(this.textInputNode.readOnly){
			doSearch = false;
		}

// 		this.setValue(this.textInputNode.value);
		if(this.searchTimer){
			clearTimeout(this.searchTimer);
		}
		if(doSearch){
			//console.debug('searching');
			// if we have gotten this far we dont want to keep our highlight
			this.blurOptionNode();

			// need to wait a tad before start search so that the event bubbels through DOM and we have value visible
			this.searchTimer = setTimeout(dojo.lang.hitch(this, this.startSearchFromInput), this.searchDelay);
		}
	},

	onKeyDown: function(evt){
		// IE needs to stop keyDown others need to stop keyPress
		if(!document.createEvent){ // only IE
			this._handleKeyEvents(evt);
		}
		// FIXME: What about ESC ??
	},

	onKeyPress: function(evt){
		if(document.createEvent){ // never IE
			this._handleKeyEvents(evt);
		}
	},

	onKeyUp: function(evt){
		//console.debug("onkeyup");
		this.setValue(this.textInputNode.value);
	},

	setSelectedValue: function(value){
		// FIXME, not sure what to do here!
		this.comboBoxSelectionValue.value = value;
		this.hideResultList();
	},

//  code from new version
		setAllValues: function(value1, value2){
			//console.debug("setAllValues");
			this.setValue(value1);
			this.setSelectedValue(value2);
		},
//--------

	// opera, khtml, safari doesnt support node.scrollIntoView(), workaround
	scrollIntoView: function(nd){             
		var node = nd || this._highlighted_option;   // can take in node to scroll as parameter
		var parent = this.optionsListNode;   
		// dont rely on that node.scrollIntoView works just because the function is there
		// it doesnt work in Konqueror or Opera even though the function is there and probably
		// not safari either
		// dont like browser sniffs implementations but sometimes you have to use it
		//if(dojo.render.html.ie || dojo.render.html.mozilla){
		//	// IE, mozilla
		//	if (node!=null) // NEW CODE, BUGFIX, when there is no list, node is null.
		//		node.scrollIntoView(false);	
		//}else{
			if (node==null || parent==null)
			{
				return;
			}
			/* NOTE: scrollIntoView() will move item to last of list, and change window scroll position! use the manual scrolling method */
			var parentBottom = parent.scrollTop + dojo.style.getOuterHeight(parent);
			var nodeBottom = node.offsetTop + dojo.style.getOuterHeight(node);
			if(parentBottom < nodeBottom){
				parent.scrollTop += (nodeBottom - parentBottom) + dojo.style.getBorderWidth(parent) ;
			}else if(parent.scrollTop > node.offsetTop){
				parent.scrollTop -= (parent.scrollTop - node.offsetTop);
			}
		//}
	},

	highlightNode: function(nd){
		this.focusOptionNode(nd);
		this.scrollIntoView();
	},

	// does the actual highlight
	focusOptionNode: function(node){
		if(this._highlighted_option != node){
			this.blurOptionNode();
			this._highlighted_option = node;
			dojo.html.addClass(this._highlighted_option, "dojoComboBoxItemHighlight");
		}
	},

	// removes highlight on highlighted
	blurOptionNode: function(){
		if(this._highlighted_option){
			dojo.html.removeClass(this._highlighted_option, "dojoComboBoxItemHighlight");
			this._highlighted_option = null;
		}
	},

	highlightNextOption: function(){
		if((!this._highlighted_option) || !this._highlighted_option.parentNode){
			this.focusOptionNode(this.optionsListNode.firstChild);
		}else if(this._highlighted_option.nextSibling){
			this.focusOptionNode(this._highlighted_option.nextSibling);
		}
		this.scrollIntoView();
	},

	highlightPrevOption: function(){
		if(this._highlighted_option && this._highlighted_option.previousSibling){
			this.focusOptionNode(this._highlighted_option.previousSibling);
		}//else if (!this._highlighted_option.parentNode){
			//this._highlighted_option = null;
			//this.hideResultList();   // this UI feature feels annoying
			//this.blurOptionNode();     // do this instead
			//return;
		//}
		this.scrollIntoView();
	},

	itemMouseOver: function(evt){
		this.focusOptionNode(evt.target);
		dojo.html.addClass(this._highlighted_option, "dojoComboBoxItemHighlight");
	},

	itemMouseOut: function(evt){
		this.blurOptionNode();
	},

	fillInTemplate: function(args, frag){
		// FIXME: need to get/assign DOM node names for form participation here.
		this.comboBoxValue.name = this.name;
		this.comboBoxSelectionValue.name = this.name+"_selected";

		// .NET validation will look for value with matching ClientID
		this.comboBoxValue.id = this.clientid;
		this.comboBoxSelectionValue.id = this.clientid+"_selected";

		// NOTE: this doesn't copy style info inherited from classes;
		// it's just primitive support for direct style setting
		var source = this.getFragNodeRef(frag);
		if ( source.style ){
			// get around opera wich doesnt have cssText, and IE wich bugs on setAttribute 
			if(dojo.lang.isUndefined(source.style.cssText)){ 
				this.domNode.setAttribute("style", source.getAttribute("style")); 
			}else{
				this.domNode.style.cssText = source.style.cssText; 
			}
		}

		// FIXME: add logic
		this.dataProvider = new openbravo.widget.ComboBoxDataProvider();
		this.dataProvider.searchTimeout = this.searchTimeout;

		if(!dojo.string.isBlank(this.dataUrl)){
			if("local" == this.mode){
				var _this = this;
				dojo.io.bind({
					url: this.dataUrl,
					load: function(type, data, evt){ 
						if(type=="load"){
							if(!dojo.lang.isArray(data)){
								var arrData = [];
								for(var key in data){
									arrData.push([data[key], key]);
								}
								data = arrData;
							}
							_this.dataProvider.setData(data);
						}
					},
					mimetype: "text/json"
				});
			}else if("remote" == this.mode){
				this.dataProvider = new openbravo.widget.incrementalComboBoxDataProvider(this.dataUrl);		
				this.dataProvider.searchTimeout = this.searchTimeout;				
				this.comboBoxSelectionValue.value = this.selVal;
				this.comboBoxValue.value = this.selTxt;
				this.textInputNode.value = this.selTxt;
				//console.debug("clientid=" + this.clientid);
				//console.debug("autoComplete=" + this.autoComplete);				
				//console.debug(dojo.byId(formId));
				//console.debug(this.formId);
				if (this.formId!="") this.dataProvider.fnd = this.formId;
			}
		}else{
			// check to see if we can populate the list from <option> elements
			var node = frag["dojo:"+this.widgetType.toLowerCase()]["nodeRef"];
			if((node)&&(node.nodeName.toLowerCase() == "select")){

				// setting text using setTxt
				//console.debug("fillInTemplate(): " + this.selTxt);

				// NOTE: we're not handling <optgroup> here yet
				var opts = node.getElementsByTagName("option");
				var ol = opts.length;
				var data = [];
				for(var x=0; x<ol; x++){
					var keyValArr = [new String(opts[x].innerHTML), new String(opts[x].value)];
					data.push(keyValArr);
					if(opts[x].selected){
						//console.debug("opts " + x);
						this.setValue(keyValArr[0]); 
						this.comboBoxSelectionValue.value = keyValArr[1];
						this.invalid=false;
					}
				}

				if (this.isBlank || this.selTxt)
				{
					//console.debug("in here");
					this.comboBoxSelectionValue.value = '';
					this.comboBoxValue.value = this.selTxt;
					this.textInputNode.value = this.selTxt;
					this.invalid=true;
				}

				this.dataProvider.setData(data);
			}
		}

		if (this.disabled || this.ro!="")
		{
			if (this.disabled)
				this.textInputNode.disabled = "disabled";
			if (this.ro!="")
			{
				this.textInputNode.readOnly = 'readonly';
			}
			dojo.style.setOpacity(this.downArrowNode, 0.5);
		}
			
		if (this.invalid)
			dojo.html.addClass(this.textInputNode, "dojoComboBoxInputInvalid");
		else
			dojo.html.removeClass(this.textInputNode, "dojoComboBoxInputInvalid");

		// Prevent IE bleed-through problem
		this.optionsIframe = new dojo.html.BackgroundIframe(this.optionsListWrapper);
		this.optionsIframe.size([0,0,0,0]);
		//this.optionsIframe.tabindex=-1;
	},


	focus: function(){
		// summary
		//	set focus to input node from code
		this.tryFocus();
	},

	/* @param selectedIndex:    (not given)    don't make a selection
	                             -1            dont autocomplete (e.g. click on arrow, but no match, dont autocomplete with first match)
								 (number)      element at that index will be active
	*/
	openResultList: function(results, selectedIndex){
		//console.debug(results[selectedIndex]);

		// change the CSS class of input box to show that there is error
		if (!results.length || selectedIndex==-1)
			dojo.html.addClass(this.textInputNode, "dojoComboBoxInputInvalid");
		else
			dojo.html.removeClass(this.textInputNode, "dojoComboBoxInputInvalid");

		this.clearResultList();
		if(!results.length){
			this.hideResultList();
			return;
		}

		var makeSelection = (arguments.length==2);   // only select item if selectedIndex given

		// by default, select first element
		selectedIndex = selectedIndex || 0;

		//console.debug(this.autoComplete);
		//console.debug(results.length);
		//console.debug(selectedIndex);
		//console.debug(!this._prev_key_backspace);
		//console.debug(this.textInputNode.value.length > 0);

		if(	(this.autoComplete)&&
			(results.length)&& 
			(selectedIndex!=-1) &&  // -1 indicates to NOT autocomplete
			(selectedIndex!=-10) && (selectedIndex!=-11) && // dont autocomplete when paging
			(!this._prev_key_backspace)&&
			(this.textInputNode.value.length > 0)){
			var cpos = this.getCaretPos(this.textInputNode);
			//console.debug(cpos);
			// only try to extend if we added the last charachter at the end of the input
			if((cpos+1) > this.textInputNode.value.length){

				// only add to input node as we would overwrite Capitalisation of chars
				// shift by one result if first is the "PREV" tag
				//if (!dojo.lang.isArray(results[0]))
				//	this.textInputNode.value += results[selectedIndex+1][0].substr(cpos);   
				//else
					this.textInputNode.value += results[selectedIndex][0].substr(cpos);

				// build a new range that has the distance from the earlier
				// caret position to the end of the first string selected
				this.setSelectedRange(this.textInputNode, cpos, this.textInputNode.value.length);
			}
		}
		
		if (selectedIndex==-10) selectedIndex=0;  // select the first element when paging next
		if (selectedIndex==-11) selectedIndex=results.length-1; // select the last element when paging prev

		var even = true;
		var i=0;
		var activeNode = null;
		while(results.length){
			var tr = results.shift();
			if(tr){
				var td = document.createElement("div");
				// if not array, it is a pager object. process this
				if(!dojo.lang.isArray(tr)){			
					td.appendChild(document.createTextNode(tr.text));
					td.setAttribute("resultName", tr.text);
					td.setAttribute("resultValue", tr.pgStr);
					td.setAttribute("pagerType", tr.pgTp);  // either "n" or "p"
					td.setAttribute("isPager", true);
				}
				else
				{
					td.appendChild(document.createTextNode(tr[0]));
					td.setAttribute("resultName", tr[0]);
					td.setAttribute("resultValue", tr[1]);
				}
				td.className = "dojoComboBoxItem "+((even) ? "dojoComboBoxItemEven" : "dojoComboBoxItemOdd");
				even = (!even);
				this.optionsListNode.appendChild(td);
				dojo.event.connect(td, "onmouseover", this, "itemMouseOver");
				dojo.event.connect(td, "onmouseout", this, "itemMouseOut");	
				
				// try to make a node active if necessary
				if (makeSelection && activeNode==null && i==selectedIndex)
				{
					activeNode = td;
				}
			}
			i++;
		}

		// show our list (only if we have content, else nothing)
		this.showResultList();

		// highlight the node if an active node was found
		if (activeNode!=null)
		{
			//this.scrollIntoView(activeNode, this.optionsListNode);
			this.highlightNode(activeNode);
		}

	},

	onFocusInput: function(){
		this._hasFocus = true;
	},

	onBlurInput: function(){
		this._hasFocus = false;
		this._handleBlurTimer(true, 500);
	},

	// collect all blur timers issues here
	_handleBlurTimer: function(/*Boolean*/clear, /*Number*/ millisec){
		if(this.blurTimer && (clear || millisec)){
			clearTimeout(this.blurTimer);
		}
		if(millisec){ // we ignore that zero is false and never sets as that never happens in this widget
			this.blurTimer = dojo.lang.setTimeout(this, "checkBlurred", millisec);
		}
	},

	// these 2 are needed in IE and Safari as inputTextNode looses focus when scrolling optionslist
	_onMouseOver: function(evt){
		if(!this._mouseover_list){
			this._handleBlurTimer(true, 0);
			this._mouseover_list = true;
		}
	},

	_onMouseOut:function(evt){
		var relTarget = evt.relatedTarget;
		if(!relTarget || relTarget.parentNode!=this.optionsListNode){
			this._mouseover_list = false;
			this._handleBlurTimer(true, 100);
			this.tryFocus();
		}
	},


// code from new version
		_isInputEqualToResult: function(result){
			input = this.textInputNode.value;
			if(!this.dataProvider.caseSensitive){
				input = input.toLowerCase();
				result = result.toLowerCase();
			}
			return (input == result);
		},

		_isValidOption: function(){
			tgt = dojo.dom.firstElement(this.optionsListNode);
			
			// DODGY: tgt will be null if the list has never been opened. Just say it's valid
			if (tgt==null)
				return true;

			isValidOption = false;
			while(!isValidOption && tgt){
				if(this._isInputEqualToResult(tgt.getAttribute("resultName"))){
					isValidOption = true;
					this.comboBoxSelectionValue.value = tgt.getAttribute("resultValue"); //ensures value is set, called by checkBlurred
				}else{
					tgt = dojo.dom.nextElement(tgt);
				}
			}
			return isValidOption;
		},
//----------------

	checkBlurred: function(){
		if(!this._hasFocus && !this._mouseover_list){
			this.hideResultList();

			// code from new version
				// clear the list if the user empties field and moves away.
				if(!this.textInputNode.value.length){
					this.setAllValues("", "");
					return;
				}
				
				isValidOption = this._isValidOption();
				// enforce selection from option list
				if(this.forceValidOption && !isValidOption){
					this.setAllValues("", "");
					return;
				}
				if(!isValidOption){// clear
					this.setSelectedValue("");
				}
			//----------
		}
	},

	sizeBackgroundIframe: function(){
		var w = dojo.style.getOuterWidth(this.optionsListNode);
		var h = dojo.style.getOuterHeight(this.optionsListNode);
		if( w==0 || h==0 ){
			// need more time to calculate size
			dojo.lang.setTimeout(this, "sizeBackgroundIframe", 100);
			return;
		}
		if(this._result_list_open){
			this.optionsIframe.size([0,0,w,h]);
		}
	},

	selectOption: function(evt){
		if(!evt){
			evt = { target: this._highlighted_option };
		}

		if(!dojo.dom.isDescendantOf(evt.target, this.optionsListNode)){
			return;
		}

		var tgt = evt.target;
		while((tgt.nodeType!=1)||(!tgt.getAttribute("resultName"))){
			tgt = tgt.parentNode;
			if(tgt === document.body){
				return false;
				//return;
			}
		}

		// selecting pager
		if (tgt.getAttribute("isPager"))
		{
			this._highlighted_option = null;
			this._handleBlurTimer(true, 100);
			//var pgTp = evt.keyboardSelect ? tgt.getAttribute("pagerType") : null; // send pager info when using keyboard
			this.startSearch("", null, null, tgt.getAttribute("resultValue"),  tgt.getAttribute("pagerType"));
			this.tryFocus();
		}
		// sucessful selection
		else
		{			
			this.textInputNode.value = tgt.getAttribute("resultName");
			this.selectedResult = [tgt.getAttribute("resultName"), tgt.getAttribute("resultValue")];
			//console.debug("selectOption");
			this.setValue(tgt.getAttribute("resultName"));
			this.comboBoxSelectionValue.value = tgt.getAttribute("resultValue");
			if(!evt.noHide){
				this.hideResultList();
				this.setSelectedRange(this.textInputNode, 0, null);
			}
			this.tryFocus();
			dojo.html.removeClass(this.textInputNode, "dojoComboBoxInputInvalid");

			//console.debug("autopostback:" + this.autopostback);
			if (this.autopostback)
				dojo.lang.setTimeout(__doPostBack, this.fadeTime+200, this.clientid);
		}
	},

	clearResultList: function(){
		this._highlighted_option = null; // NEW CODE, IE had issues where down button no longer worked.
		var oln = this.optionsListNode;
		while(oln.firstChild){
			dojo.event.disconnect(oln.firstChild, "onmouseover", this, "itemMouseOver");
			dojo.event.disconnect(oln.firstChild, "onmouseout", this, "itemMouseOut");
			oln.removeChild(oln.firstChild);
		}
	},

	hideResultList: function(){
		if(this._result_list_open){
			this._highlighted_option = null; // NEW CODE, IE had issues where down button no longer worked.
			this._result_list_open = false;
			this.optionsIframe.size([0,0,0,0]);
			dojo.style.hide(this.optionsListNode);
	
//			dojo.lfx.fadeHide(this.optionsListNode, this.fadeTime).play();
		}
	},

	showResultList: function(){
		// Our dear friend IE doesnt take max-height so we need to calculate that on our own every time
		var childs = this.optionsListNode.childNodes;
		if(childs.length){
			var isShort = false;
			var visibleCount = this.maxListLength;
			if(childs.length < visibleCount){
				visibleCount = childs.length;
				isShort = true;
			}

			with(this.optionsListNode.style){
				display = "";
				//height = ((visibleCount) ? (dojo.style.getOuterHeight(childs[0]) * visibleCount) : 0)+"px";
				//width = dojo.html.getOuterWidth(this.cbTableNode)+"px";
				// the above does not take into account box model. use dojo.style methods
				var h = ((visibleCount) ? (dojo.style.getOuterHeight(childs[0]) * visibleCount) + dojo.style.getBorderWidth(this.optionsListNode) : 0);
				dojo.style.setContentBoxHeight(this.optionsListNode, h);
				dojo.style.setMarginBoxHeight(this.optionsListNode, h);
				var w = dojo.html.getOuterWidth(this.cbTableNode);
				dojo.style.setContentBoxWidth(this.optionsListNode, w);
				dojo.style.setMarginBoxWidth(this.optionsListNode, w);
				overflow = (isShort ? "hidden" : "auto"); // fixes an IE bug where hitting last page causes horizontal scrollbars to appear
			}
			// only fadein once (flicker)
			if(!this._result_list_open){
				dojo.html.setOpacity(this.optionsListNode, 0);
				dojo.lfx.fadeIn(this.optionsListNode, this.fadeTime).play();
			}
			
			// prevent IE bleed through
			this._iframeTimer = dojo.lang.setTimeout(this, "sizeBackgroundIframe", 200);
			this._result_list_open = true;
		}else{
			this.hideResultList();
		}
	},

	ding: function() {
		alert('ding');
	},

	handleArrowClick: function(){
		if (this.disabled || this.ro)
			return;

		this._handleBlurTimer(true, 0);
		this.tryFocus();
		if(this._result_list_open){
			this.hideResultList();
		}else{
			this.startSearchArrow();
		}
	},
	
	tryFocus: function(){
		try {    
			this.textInputNode.focus();
		} catch (e) {
			// element isnt focusable if disabled, or not visible etc - not easy to test for.
 		};
	},

	/* you want to get default list of items, even if no match. e.g. click arrow, pressing down for the first time */
	startSearchArrow: function(){
		//console.debug("startSearchArrow");
		this.startSearch(this.textInputNode.value, "a");
	},

	startSearchFromInput: function(){
		this.startSearch(this.textInputNode.value);
	},

	postCreate: function(){
		dojo.event.connect(this, "startSearch", this.dataProvider, "startSearch");
		dojo.event.connect(this.dataProvider, "provideSearchResults", this, "openResultList");
		dojo.event.connect(this.textInputNode, "onblur", this, "onBlurInput");
		dojo.event.connect(this.textInputNode, "onfocus", this, "onFocusInput");

		//var s = dojo.widget.html.stabile.getState(this.widgetId);
		//if (s) {
			//console.debug("postCreate");
		//	this.setState(s);
		//}
	}
});
