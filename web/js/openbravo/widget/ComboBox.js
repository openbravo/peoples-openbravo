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

dojo.provide("openbravo.widget.ComboBox");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.string.*");
dojo.require("dojo.io.*");

openbravo.widget.incrementalComboBoxDataProvider = function(url, limit, timeout){
	this.searchUrl = url;
	this.inFlight = false;
	this.activeRequest = null;
	this.allowCache = false;
	this.fnd = "";  // for .NET

	this.cache = {};
	this.addToCache = function(keyword, data){
		if(this.allowCache){
			this.cache[keyword] = data;
		}
	}

	this.searchTimeout = 500;
	this.prevSearchTimeoutID = null;
	this.startSearch =  function(searchStr, type, ignoreLimit, pagerStr, pagerType){
		// No timeout for clicking on arrow, or for pager
		var pgTp = pagerType || "";
		if (type=="a" || pgTp!="")
		{
			clearTimeout(this.prevSearchTimeoutID);
			this.prevSearchTimeoutID=null;
			this._performSearch(searchStr, type, ignoreLimit, pagerStr, pagerType);
		}
		else
		{		
			if (this.prevSearchTimeoutID!=null)
			{
				clearTimeout(this.prevSearchTimeoutID);
				this.prevSearchTimeoutID=null;
			}
			this.prevSearchTimeoutID = dojo.lang.setTimeout(this, "_performSearch", this.searchTimeout, searchStr, type, ignoreLimit, pagerStr, pagerType);
		}
	}

	this._performSearch = function(searchStr, type, ignoreLimit, pagerStr, pagerType){
		if(this.inFlight){
			// FIXME: implement backoff!
		}

		// FIXME: implement caching

		var tss = encodeURIComponent(searchStr);
		var realUrl = dojo.string.paramString(this.searchUrl, {"searchString": tss});
		
		var pager = pagerStr || "";
		var pgTp = pagerType || "";
		var wasArrow = (type=="a");

		realUrl = dojo.string.paramString(realUrl, {"pagerString": pager});

		var _this = this;
		var request = dojo.io.bind({
			url: realUrl,
			method: "post",
			mimetype: "text/json",
			formNode: (this.fnd ? dojo.byId(this.fnd) : null),          // THIS IS REQUIRED FOR .NET SUBMIT, MAINTAINS VIEWSTATE
			load: function(type, data, evt){
				_this.inFlight = false;
				// object support
				if(!dojo.lang.isArray(data)){
					var arrData = [];
					for(var key in data){
						if (key=="_prev") {
							arrData.unshift(data[key]);
						}
						else if (key=="_next") {
							arrData.push(data[key]);
						}
						else {
							arrData.push([data[key], key]);
						}
					}
					data = arrData;
				}
				_this.addToCache(searchStr, data);

				// no results
				if (data.length==0)
				{
					_this.provideSearchResults(data);
					return;
				}

				var activeIndex = -7;

				// process pager object. Definition:
				// data = [  {pagerObject} , [text,value] , [text,value] , ... ]
				// pagerObject = {  _prev: { text: "<prev>", pgStr: "10:19" },   // optional
				//                  _next: { text: "<next>", pgStr: "20:29" },   // optional
				//                  _idx:  1 }   // optional
				// _idx parameter:   n/a   use default logic
				//                   -7    use default logic
				//                   -2    display data if arrow, otherwise hide data				
				//                   -ve   these wont match an index, nothing selected
				//                   else  if there is matching index, selects it   
				if (!dojo.lang.isArray(data[0])) {
					//console.debug(data[0]._prev);
					var pagerObj = data.shift();
					//console.debug(pagerObj._prev);
					if (typeof pagerObj._prev != "undefined")
					{
						pagerObj._prev.pgTp = "p";
						data.unshift(pagerObj._prev); // previous as first item
					}
					if (typeof pagerObj._next != "undefined") 
					{
						pagerObj._next.pgTp = "n";
						data.push(pagerObj._next);    // next as last item
					}
					if (typeof pagerObj._idx != "undefined") 
						activeIndex = pagerObj._idx;

					// server returned only a pager, no data
					if (data.length==0)
					{
						_this.provideSearchResults(data);
						return;
					}
				}

				// special case, display data if arrow, otherwise hide data
				if (activeIndex==-2) {
					if (wasArrow) {
						activeIndex = -1;    // don't autocomplete (i.e. click arrow, no match)
					} else {
						_this.provideSearchResults([]); // keyboard entry, not found
						return;
					}
				}

				// default selection
				if (activeIndex==-7 || activeIndex==-2) {
					if (pgTp=="p")       // previous was pressed, select last
						activeIndex = -11; 
					else if (pgTp=="n")  // next was pressed, select first
						activeIndex = -10; 
					else 
						activeIndex = (!dojo.lang.isArray(data[0]) ? 1 : 0); // select first, but skip <prev>
				}
				//console.debug(data[activeIndex-1]);
				//console.debug(data[activeIndex]);
				_this.provideSearchResults(data, activeIndex);
			}
		});
		this.inFlight = true;
	}
}

openbravo.widget.ComboBoxDataProvider = function(dataPairs, limit, timeout){
	// NOTE: this data provider is designed as a naive reference
	// implementation, and as such it is written more for readability than
	// speed. A deployable data provider would implement lookups, search
	// caching (and invalidation), and a significantly less naive data
	// structure for storage of items.
	this.data = [];
	this.searchTimeout = 500;
	this.searchLimit = 30;
	this.searchType = "STARTSTRING"; // may also be "STARTWORD" or "SUBSTRING"
	this.caseSensitive = false;
	// for caching optimizations
	this._lastSearch = "";
	this._lastSearchResults = null;


	this.prevSearchTimeoutID = null;  // NEW CODE
	this.startSearch = function(searchStr, type, ignoreLimit){
		// No timeout for clicking on arrow
		if (type=="a")
		{
			clearTimeout(this.prevSearchTimeoutID);
			this.prevSearchTimeoutID=null;
			this._performSearch(searchStr, type, ignoreLimit);
		}
		else
		{
			if (this.prevSearchTimeoutID!=null)
			{
				clearTimeout(this.prevSearchTimeoutID);
				this.prevSearchTimeoutID=null;
			}
			this.prevSearchTimeoutID = dojo.lang.setTimeout(this, "_performSearch", this.searchTimeout, searchStr, type, ignoreLimit);
		}
	}

	this._performSearch = function(searchStr, type, ignoreLimit){		
		//var ret = [];
		var ret = new Array();
		ret = ret.concat(this.data);

		if(!this.caseSensitive){
			searchStr = searchStr.toLowerCase();
		}
		var firstInstance=-1;
		for(var x=0; x<this.data.length; x++){
			var dataLabel = new String((!this.caseSensitive) ? this.data[x][0].toLowerCase() : this.data[x][0]);
			if(firstInstance==-1 && searchStr.length>0 && searchStr == dataLabel.substr(0, searchStr.length)){
				firstInstance = x;
			}
			//ret.push(this.data[x]);
		}
		if (firstInstance>=0)
			this.provideSearchResults(ret, firstInstance); // found item
		else if (type=="a")
			this.provideSearchResults(ret, -1);  // don't autocomplete (i.e. click arrow, no match)
		else
			this.provideSearchResults([]);       // hide results (i.e. typing in box, no match)
	}

	/* original code */
	this._preformSearch = function(searchStr, type, ignoreLimit){
		//
		//	NOTE: this search is LINEAR, which means that it exhibits perhaps
		//	the worst possible speed charachteristics of any search type. It's
		//	written this way to outline the responsibilities and interfaces for
		//	a search.
		//
		var st = type||this.searchType;
		// FIXME: this is just an example search, which means that we implement
		// only a linear search without any of the attendant (useful!) optimizations
		var ret = [];
		if(!this.caseSensitive){
			searchStr = searchStr.toLowerCase();
		}
		for(var x=0; x<this.data.length; x++){
			if((!ignoreLimit)&&(ret.length >= this.searchLimit)){
				break;
			}
			// FIXME: we should avoid copies if possible!
			var dataLabel = new String((!this.caseSensitive) ? this.data[x][0].toLowerCase() : this.data[x][0]);
			if(dataLabel.length < searchStr.length){
				// this won't ever be a good search, will it? What if we start
				// to support regex search?
				continue;
			}

			if(st == "STARTSTRING"){
				// jum.debug(dataLabel.substr(0, searchStr.length))
				// jum.debug(searchStr);
				if(searchStr == dataLabel.substr(0, searchStr.length)){
					ret.push(this.data[x]);
				}
			}else if(st == "SUBSTRING"){
				// this one is a gimmie
				if(dataLabel.indexOf(searchStr) >= 0){
					ret.push(this.data[x]);
				}
			}else if(st == "STARTWORD"){
				// do a substring search and then attempt to determine if the
				// preceeding char was the beginning of the string or a
				// whitespace char.
				var idx = dataLabel.indexOf(searchStr);
				if(idx == 0){
					// implicit match
					ret.push(this.data[x]);
				}
				if(idx <= 0){
					// if we didn't match or implicily matched, march onward
					continue;
				}
				// otherwise, we have to go figure out if the match was at the
				// start of a word...
				// this code is taken almost directy from nWidgets
				var matches = false;
				while(idx!=-1){
					// make sure the match either starts whole string, or
					// follows a space, or follows some punctuation
					if(" ,/(".indexOf(dataLabel.charAt(idx-1)) != -1){
						// FIXME: what about tab chars?
						matches = true; break;
					}
					idx = dataLabel.indexOf(searchStr, tti+1);
				}
				if(!matches){
					continue;
				}else{
					ret.push(this.data[x]);
				}
			}
		}
		this.provideSearchResults(ret);
	}

	this.provideSearchResults = function(resultsDataPairs){
	}

	this.addData = function(pairs){
		// FIXME: incredibly naive and slow!
		this.data = this.data.concat(pairs);
	}

	this.setData = function(pdata){
		// populate this.data and initialize lookup structures
		this.data = pdata;
	}
	
	if(dataPairs){
		this.setData(dataPairs);
	}
}

openbravo.widget.ComboBox = function(){
	dojo.widget.Widget.call(this);
}

dojo.inherits(openbravo.widget.ComboBox, dojo.widget.Widget);

openbravo.widget.ComboBox.defaults = {
	widgetType: "ComboBox",
	isContainer: false,

	forceValidOption: false,
	searchType: "stringstart",
	dataProvider: null,

	startSearch: function(searchString){},
	openResultList: function(results){},
	clearResultList: function(){},
	hideResultList: function(){},
	selectNextResult: function(){},
	selectPrevResult: function(){},
	setSelectedResult: function(){}
};

dojo.lang.extend(openbravo.widget.ComboBox, openbravo.widget.ComboBox.defaults);

openbravo.widget.DomComboBox = function(){
	openbravo.widget.ComboBox.call(this);
	dojo.widget.DomWidget.call(this, true);
}

dojo.inherits(openbravo.widget.DomComboBox, dojo.widget.DomWidget);

// render-specific includes
dojo.requireAfterIf("html", "openbravo.widget.html.ComboBox");

