/*
	Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["dijit.layout.LayoutContainer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.LayoutContainer"] = true;
dojo.provide("dijit.layout.LayoutContainer");

dojo.require("dijit.layout._LayoutWidget");

dojo.declare("dijit.layout.LayoutContainer",
	dijit.layout._LayoutWidget,
	{
	// summary:
	//		Deprecated.  Use `dijit.layout.BorderContainer` instead.
	//
	// description:
	//		Provides Delphi-style panel layout semantics.
	//
	//		A LayoutContainer is a box with a specified size (like style="width: 500px; height: 500px;"),
	//		that contains children widgets marked with "layoutAlign" of "left", "right", "bottom", "top", and "client".
	//		It takes it's children marked as left/top/bottom/right, and lays them out along the edges of the box,
	//		and then it takes the child marked "client" and puts it into the remaining space in the middle.
	//
	//		Left/right positioning is similar to CSS's "float: left" and "float: right",
	//		and top/bottom positioning would be similar to "float: top" and "float: bottom", if there were such
	//		CSS.
	//
	//		Note that there can only be one client element, but there can be multiple left, right, top,
	//		or bottom elements.
	//
	// example:
	// |	<style>
	// |		html, body{ height: 100%; width: 100%; }
	// |	</style>
	// |	<div dojoType="dijit.layout.LayoutContainer" style="width: 100%; height: 100%">
	// |		<div dojoType="dijit.layout.ContentPane" layoutAlign="top">header text</div>
	// |		<div dojoType="dijit.layout.ContentPane" layoutAlign="left" style="width: 200px;">table of contents</div>
	// |		<div dojoType="dijit.layout.ContentPane" layoutAlign="client">client area</div>
	// |	</div>
	//
	//		Lays out each child in the natural order the children occur in.
	//		Basically each child is laid out into the "remaining space", where "remaining space" is initially
	//		the content area of this widget, but is reduced to a smaller rectangle each time a child is added.
	// tags:
	//		deprecated

	baseClass: "dijitLayoutContainer",

	constructor: function(){
		dojo.deprecated("dijit.layout.LayoutContainer is deprecated", "use BorderContainer instead", 2.0);
	},

	layout: function(){
		dijit.layout.layoutChildren(this.domNode, this._contentBox, this.getChildren());
	},

	addChild: function(/*dijit._Widget*/ child, /*Integer?*/ insertIndex){
		this.inherited(arguments);
		if(this._started){
			dijit.layout.layoutChildren(this.domNode, this._contentBox, this.getChildren());
		}
	},

	removeChild: function(/*dijit._Widget*/ widget){
		this.inherited(arguments);
		if(this._started){
			dijit.layout.layoutChildren(this.domNode, this._contentBox, this.getChildren());
		}
	}
});

// This argument can be specified for the children of a LayoutContainer.
// Since any widget can be specified as a LayoutContainer child, mix it
// into the base widget class.  (This is a hack, but it's effective.)
dojo.extend(dijit._Widget, {
	// layoutAlign: String
	//		"none", "left", "right", "bottom", "top", and "client".
	//		See the LayoutContainer description for details on this parameter.
	layoutAlign: 'none'
});

}